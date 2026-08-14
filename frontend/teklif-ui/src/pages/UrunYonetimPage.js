import React, { useState } from 'react';
import { getUrunOzellikler, updateUrunOzellikler, deleteUrunOzellikOverride,
  getUrunTanimlar, addUrunTanim, deleteUrunTanim, matrisOlustur,
  getOzellikOranlari, updateOzellikOrani, addOzellikOrani, deleteOzellikOrani,
  getKategoriOranlari, addKategoriOrani, deleteKategoriOrani } from '../services/api';

/* ── Ürün Ekle Paneli ── */
function UrunEklePanel() {
  const KATEGORILER = [
    'MENFEZ','SLOT','DIKDORTGEN_DAMPER','DAIRESEL_DAMPER',
    'KARE_ANEMOSTAD','DAIRESEL_ANEMOSTAD','KARE_SWIRL','DAIRESEL_SWIRL',
    'PANJUR','KAPAK','KUTU'
  ];
  const KAT_LABEL = {
    MENFEZ:'Menfez', SLOT:'Slot / Lineer', DIKDORTGEN_DAMPER:'Dikdörtgen Damper',
    DAIRESEL_DAMPER:'Dairesel Damper', KARE_ANEMOSTAD:'Kare Anemostad',
    DAIRESEL_ANEMOSTAD:'Dairesel Anemostad', KARE_SWIRL:'Kare Swirl',
    DAIRESEL_SWIRL:'Dairesel Swirl', PANJUR:'Panjur', KAPAK:'Kapak', KUTU:'Kutu'
  };
  const OLCU_SECENEKLER = [
    {val:'GENISLIK',lbl:'Genişlik W (mm)'},
    {val:'YUKSEKLIK',lbl:'Yükseklik H (mm)'},
    {val:'UZUNLUK',lbl:'Uzunluk L (mm)'},
    {val:'CAP',lbl:'Çap Ø (mm)'},
    {val:'KASA_WH',lbl:'Kasa Ölçüsü (WxH)'},
    {val:'BOGAZ_WH',lbl:'Boğaz Ölçüsü (WxH)'},
    {val:'BOGAZ_CAP',lbl:'Boğaz Çap'},
    {val:'KASA_CAP',lbl:'Kasa Çap'},
    {val:'SLOT_SAYISI',lbl:'Slot Sayısı'},
  ];
  const OZELLIK_SECENEKLER = [
    {val:'CERCEVE_TIPI',lbl:'Çerçeve Tipi'},
    {val:'DAMPER_TIPI',lbl:'Damper Tipi'},
    {val:'RAL',lbl:'RAL / Renk'},
    {val:'MONTAJ',lbl:'Montaj'},
    {val:'AKSESUAR_TIPI',lbl:'Aksesuar Tipi'},
    {val:'MENFEZ_TIPI',lbl:'Menfez Tipi'},
  ];
  const STRATEJI_SECENEKLER = [
    {val:'WH',lbl:'WxH Matris (Genişlik × Yükseklik)'},
    {val:'L',lbl:'Uzunluk Matris'},
    {val:'CAP',lbl:'Çap Matris'},
    {val:'WH_STR',lbl:'String WxH (Kasa/Boğaz)'},
  ];

  const BOSH = {kod:'',ad:'',kategori:'',zorunluOlcular:[],ozellikTipleri:[],fiyatStratejisi:'WH'};
  const [form, setForm] = React.useState(BOSH);
  const [hata, setHata] = React.useState('');
  const [mesaj, setMesaj] = React.useState('');
  const [dbUrunler, setDbUrunler] = React.useState([]);

  React.useEffect(() => {
    yukle();
  }, []);

  const yukle = () => {
    getUrunTanimlar().then(r => setDbUrunler(r.data)).catch(() => {});
  };

  const toggle = (field, val) => {
    setForm(f => {
      const arr = f[field].includes(val)
        ? f[field].filter(x => x !== val)
        : [...f[field], val];
      return {...f, [field]: arr};
    });
  };

  const [adim, setAdim] = React.useState('form'); // 'form' | 'fiyat'
  const [tableId, setTableId] = React.useState(null);
  const [eklenenUrun, setEklenenUrun] = React.useState(null);

  const kaydet = async () => {
    setHata(''); setMesaj('');
    if (!form.kod.trim()) { setHata('Ürün kodu zorunludur.'); return; }
    if (!form.ad.trim())  { setHata('Ürün adı zorunludur.');  return; }
    if (!form.kategori)   { setHata('Kategori seçiniz.');      return; }
    try {
      const r = await addUrunTanim(form);
      setTableId(r.data.tableId);
      setEklenenUrun({...form});
      setAdim('fiyat');
      yukle();
    } catch(e) { setHata(e.response?.data?.hata || e.message); }
  };

  const fiyatTamam = () => {
    setAdim('form');
    setForm(BOSH);
    setTableId(null);
    setEklenenUrun(null);
    setMesaj('Ürün ve fiyat listesi eklendi!');
    setTimeout(() => setMesaj(''), 3000);
  };

  const sil = async (kod) => {
    if (!window.confirm(`"${kod}" silinsin mi?`)) return;
    try {
      await deleteUrunTanim(kod);
      yukle();
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const inputStyle = {fontSize:12, padding:'6px 8px'};
  const colHead = {fontSize:10,fontWeight:700,letterSpacing:'.08em',textTransform:'uppercase',color:'var(--muted)',marginBottom:8};

  return (
    <div style={{display:'flex',flexDirection:'column',gap:20}}>

      {adim === 'fiyat' && tableId && (
        <FiyatMatrisGiris tableId={tableId} urun={eklenenUrun} onTamam={fiyatTamam}
          onAtla={fiyatTamam}/>
      )}

      {adim === 'form' && <>
      {/* Form */}
      <div style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,padding:20}}>
        <div style={{fontWeight:700,fontFamily:'var(--font-head)',fontSize:16,marginBottom:16}}>Yeni Ürün Ekle</div>

        <div style={{display:'grid',gridTemplateColumns:'1fr 1fr 1fr 1fr',gap:12,marginBottom:16}}>
          <div>
            <label style={colHead}>Ürün Kodu *</label>
            <input className="input" style={inputStyle} placeholder="Örn: MNZ_YENI"
              value={form.kod} onChange={e=>setForm(f=>({...f,kod:e.target.value.toUpperCase()}))}/>
            <div style={{fontSize:10,color:'var(--muted)',marginTop:3}}>Büyük harf, alt çizgi</div>
          </div>
          <div>
            <label style={colHead}>Ürün Adı *</label>
            <input className="input" style={inputStyle} placeholder="Örn: Yeni Tip Menfez"
              value={form.ad} onChange={e=>setForm(f=>({...f,ad:e.target.value}))}/>
          </div>
          <div>
            <label style={colHead}>Kategori *</label>
            <select className="select" style={inputStyle} value={form.kategori}
              onChange={e=>setForm(f=>({...f,kategori:e.target.value}))}>
              <option value="">— Seçiniz —</option>
              {KATEGORILER.map(k=><option key={k} value={k}>{KAT_LABEL[k]||k}</option>)}
            </select>
          </div>
          <div>
            <label style={colHead}>Fiyat Stratejisi</label>
            <select className="select" style={inputStyle} value={form.fiyatStratejisi}
              onChange={e=>setForm(f=>({...f,fiyatStratejisi:e.target.value}))}>
              {STRATEJI_SECENEKLER.map(s=><option key={s.val} value={s.val}>{s.lbl}</option>)}
            </select>
          </div>
        </div>

        <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:16}}>
          {/* Ölçüler */}
          <div style={{background:'var(--surface)',border:'1px solid var(--border)',borderRadius:8,padding:12}}>
            <div style={colHead}>Zorunlu Ölçüler</div>
            <div style={{display:'flex',flexDirection:'column',gap:4}}>
              {OLCU_SECENEKLER.map(o=>(
                <label key={o.val} style={{display:'flex',alignItems:'center',gap:8,
                  cursor:'pointer',fontSize:12,padding:'3px 0'}}>
                  <input type="checkbox"
                    checked={form.zorunluOlcular.includes(o.val)}
                    onChange={()=>toggle('zorunluOlcular',o.val)}/>
                  {o.lbl}
                </label>
              ))}
            </div>
          </div>

          {/* Özellikler */}
          <div style={{background:'var(--surface)',border:'1px solid var(--border)',borderRadius:8,padding:12}}>
            <div style={colHead}>Özellikler</div>
            <div style={{display:'flex',flexDirection:'column',gap:4}}>
              {OZELLIK_SECENEKLER.map(o=>(
                <label key={o.val} style={{display:'flex',alignItems:'center',gap:8,
                  cursor:'pointer',fontSize:12,padding:'3px 0'}}>
                  <input type="checkbox"
                    checked={form.ozellikTipleri.includes(o.val)}
                    onChange={()=>toggle('ozellikTipleri',o.val)}/>
                  {o.lbl}
                </label>
              ))}
            </div>
          </div>
        </div>

        <div style={{marginTop:16,display:'flex',alignItems:'center',gap:12}}>
          <button className="btn btn-primary" style={{padding:'8px 24px'}} onClick={kaydet}>
            + Ürün Ekle
          </button>
          {hata  && <span style={{color:'var(--red)',fontSize:12}}>⚠️ {hata}</span>}
          {mesaj && <span style={{color:'var(--green)',fontSize:12}}>✓ {mesaj}</span>}
        </div>
      </div>

      {/* DB'den eklenen ürünler listesi */}
      {dbUrunler.length > 0 && (
        <div style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,overflow:'hidden'}}>
          <div style={{padding:'10px 16px',background:'var(--surface)',borderBottom:'1px solid var(--border)',
            fontWeight:700,fontFamily:'var(--font-head)',fontSize:14}}>
            Panelden Eklenen Ürünler ({dbUrunler.length})
          </div>
          <table style={{width:'100%',borderCollapse:'collapse'}}>
            <thead>
              <tr style={{background:'var(--surface)'}}>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)'}}>Kod</th>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)'}}>Ad</th>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)'}}>Kategori</th>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)'}}>Ölçüler</th>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)'}}>Özellikler</th>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)'}}>Strateji</th>
                <th style={{padding:'8px 16px',textAlign:'center',fontSize:12,color:'var(--muted)'}}>Sil</th>
              </tr>
            </thead>
            <tbody>
              {dbUrunler.map((u,i)=>(
                <tr key={u.kod} style={{borderTop:'1px solid var(--border)',
                  background:i%2===0?'transparent':'var(--surface)'}}>
                  <td style={{padding:'8px 16px',fontSize:12,fontFamily:'monospace'}}>{u.kod}</td>
                  <td style={{padding:'8px 16px',fontSize:13}}>{u.ad}</td>
                  <td style={{padding:'8px 16px',fontSize:12}}>{KAT_LABEL[u.kategori]||u.kategori}</td>
                  <td style={{padding:'8px 16px',fontSize:11,color:'var(--muted)'}}>{u.zorunluOlcular.join(', ')}</td>
                  <td style={{padding:'8px 16px',fontSize:11,color:'var(--muted)'}}>{u.ozellikTipleri.join(', ')}</td>
                  <td style={{padding:'8px 16px',fontSize:11}}>{u.fiyatStratejisi}</td>
                  <td style={{padding:'8px 16px',textAlign:'center'}}>
                    <button style={{background:'none',border:'none',color:'var(--red)',
                      cursor:'pointer',fontSize:18,lineHeight:1}}
                      onClick={()=>sil(u.kod)}>×</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>}
    </div>
  );
}

/* ── Fiyat Matrisi Giriş ── */
function FiyatMatrisGiris({ tableId, urun, onTamam, onAtla }) {
  const strateji = urun?.fiyatStratejisi || 'WH';
  const isWH = strateji === 'WH';
  const isStr = strateji === 'WH_STR';

  const [rowInput, setRowInput] = React.useState('');   // satır değerleri (virgüllü)
  const [colInput, setColInput] = React.useState('');   // sütun değerleri
  const [matris, setMatris]     = React.useState([]);   // [[f,f,...],...]
  const [hata, setHata]         = React.useState('');
  const [kaydedildi, setKaydedildi] = React.useState(false);

  const rowVals = rowInput.split(',').map(s=>s.trim()).filter(Boolean);
  const colVals = (isWH||isStr) ? colInput.split(',').map(s=>s.trim()).filter(Boolean) : [];

  // Matris boyutu değişince yeniden oluştur
  React.useEffect(() => {
    if (rowVals.length === 0) { setMatris([]); return; }
    const cols = colVals.length || 1;
    setMatris(prev => {
      return rowVals.map((_, ri) => {
        const eskiSatir = prev[ri] || [];
        return Array.from({length: cols}, (_, ci) => eskiSatir[ci] ?? '');
      });
    });
  }, [rowInput, colInput]);

  const setHucre = (ri, ci, val) => {
    setMatris(prev => {
      const arr = prev.map(r=>[...r]);
      arr[ri][ci] = val;
      return arr;
    });
  };

  const kaydet = async () => {
    setHata('');
    if (rowVals.length === 0) { setHata('En az bir satır değeri girin.'); return; }
    try {
      await matrisOlustur(tableId, {
        rowValues: rowVals,
        colValues: colVals,
        fiyatlar: matris,
        stringAxis: isStr
      });
      setKaydedildi(true);
    } catch(e) { setHata(e.response?.data?.hata || e.message); }
  };

  const rowLabel = isWH||isStr ? 'Genişlik W değerleri' : strateji==='CAP' ? 'Çap değerleri' : 'Uzunluk değerleri';
  const inputStyle = {fontSize:11,padding:'3px 5px',width:'100%',textAlign:'center',
    background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:3,color:'var(--text)'};

  return (
    <div style={{background:'var(--surface2)',border:'2px solid var(--accent)',borderRadius:10,padding:20}}>
      <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:16}}>
        <div>
          <div style={{fontWeight:700,fontFamily:'var(--font-head)',fontSize:16}}>
            Fiyat Listesi: {urun?.ad}
          </div>
          <div style={{fontSize:12,color:'var(--muted)',marginTop:2}}>
            Strateji: {urun?.fiyatStratejisi} · Tablo ID: {tableId}
          </div>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={onAtla}>Şimdilik Atla</button>
      </div>

      <div style={{display:'grid',gridTemplateColumns: (isWH||isStr)?'1fr 1fr':'1fr',gap:12,marginBottom:16}}>
        <div>
          <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>
            {rowLabel} (virgülle ayır)
          </label>
          <input className="input" style={{fontSize:12,padding:'6px 8px'}}
            placeholder={isStr ? 'Örn: 300x300, 400x400, 600x600' : 'Örn: 300, 400, 500, 600'}
            value={rowInput} onChange={e=>setRowInput(e.target.value)}/>
        </div>
        {(isWH||isStr) && (
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>
              Yükseklik H değerleri (virgülle ayır)
            </label>
            <input className="input" style={{fontSize:12,padding:'6px 8px'}}
              placeholder={isStr ? 'Örn: 100x100, 150x150' : 'Örn: 200, 300, 400, 500'}
              value={colInput} onChange={e=>setColInput(e.target.value)}/>
          </div>
        )}
      </div>

      {/* Matris */}
      {matris.length > 0 && (
        <div style={{overflowX:'auto',marginBottom:16}}>
          <table style={{borderCollapse:'collapse',fontSize:11}}>
            <thead>
              <tr>
                <th style={{padding:'4px 8px',background:'var(--surface)',border:'1px solid var(--border)',
                  fontSize:10,color:'var(--muted)'}}>W \ H</th>
                {(colVals.length ? colVals : ['']).map((cv,ci)=>(
                  <th key={ci} style={{padding:'4px 8px',background:'var(--surface)',
                    border:'1px solid var(--border)',fontSize:10,color:'var(--accent)',minWidth:70}}>
                    {cv||'Fiyat'}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {rowVals.map((rv,ri)=>(
                <tr key={ri}>
                  <td style={{padding:'4px 8px',background:'var(--surface)',border:'1px solid var(--border)',
                    fontSize:10,color:'var(--accent)',fontWeight:600}}>{rv}</td>
                  {(colVals.length ? colVals : ['']).map((_,ci)=>(
                    <td key={ci} style={{border:'1px solid var(--border)',padding:2}}>
                      <input style={inputStyle} type="number" step="0.01" min="0"
                        value={matris[ri]?.[ci] ?? ''}
                        onChange={e=>setHucre(ri,ci,e.target.value)}/>
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <div style={{display:'flex',alignItems:'center',gap:12}}>
        {!kaydedildi ? (
          <button className="btn btn-primary" onClick={kaydet}>Fiyatları Kaydet</button>
        ) : (
          <button className="btn btn-primary" onClick={onTamam}>✓ Tamamlandı</button>
        )}
        {hata      && <span style={{color:'var(--red)',  fontSize:12}}>⚠️ {hata}</span>}
        {kaydedildi && <span style={{color:'var(--green)',fontSize:12}}>✓ Fiyatlar kaydedildi</span>}
      </div>
    </div>
  );
}

/* ── Ürün Özellik Paneli ── */
function UrunOzellikPanel({ onYeniTipAc, onOzelliklerChange, onSecUrunChange }) {
  const [urunler, setUrunler] = React.useState([]);
  const [secUrun, setSecUrun] = React.useState(null);
  const [ozellikler, setOzellikler] = React.useState([]);
  const [yukleniyor, setYukleniyor] = React.useState(false);
  const [kaydMesaj, setKaydMesaj] = React.useState('');
  const [ticariKod, setTicariKod] = React.useState('');
  const [ticariKodMesaj, setTicariKodMesaj] = React.useState('');
  const [ratioSecenekler, setRatioSecenekler] = React.useState({});

  React.useEffect(() => {
    fetch('/api/urunler')
      .then(r => r.json())
      .then(data => setUrunler(data))
      .catch(() => {});
    // Özellik oranlarını yükle (dropdown için)
    getOzellikOranlari().then(r => {
      const gruplar = {};
      r.data.forEach(o => {
        if (!gruplar[o.featureType]) gruplar[o.featureType] = [];
        gruplar[o.featureType].push(o.optionName);
      });
      setRatioSecenekler(gruplar);
    }).catch(() => {});
  }, []);

  const tipLabel = {
    CERCEVE_TIPI:'Çerçeve Tipi', DAMPER_TIPI:'Damper Tipi', RAL:'RAL / Renk',
    MONTAJ:'Montaj', AKSESUAR_TIPI:'Aksesuar Tipi', MENFEZ_TIPI:'Menfez Tipi'
  };

  const urunSec = async (kod) => {
    setSecUrun(kod); setYukleniyor(true); setKaydMesaj(''); setTicariKodMesaj('');
    if (onSecUrunChange) onSecUrunChange(kod);
    try {
      const [r, tkResp] = await Promise.all([
        getUrunOzellikler(kod),
        fetch(`/api/admin/ticari-kod/${kod}`, { headers:{ 'X-Token': localStorage.getItem('auth_token')||'' } }).then(r=>r.json()).catch(()=>({ticariKod:''}))
      ]);
      const data2 = r.data.map(o => ({...o, secenekler:[...o.secenekler], yeniSecenek:''}));
      setOzellikler(data2);
      setTicariKod(tkResp.ticariKod || '');
      if (onOzelliklerChange) onOzelliklerChange(data2);
    } catch(e) { alert('Hata: ' + e.message); }
    finally { setYukleniyor(false); }
  };

  const secenek = (tipIdx, secIdx, val) => {
    setOzellikler(prev => {
      const arr = [...prev];
      arr[tipIdx] = {...arr[tipIdx], secenekler: arr[tipIdx].secenekler.map((s,i)=>i===secIdx?val:s)};
      return arr;
    });
  };

  const secenek_sil = (tipIdx, secIdx) => {
    setOzellikler(prev => {
      const arr = [...prev];
      arr[tipIdx] = {...arr[tipIdx], secenekler: arr[tipIdx].secenekler.filter((_,i)=>i!==secIdx)};
      return arr;
    });
  };

  const secenek_ekle = (tipIdx) => {
    setOzellikler(prev => {
      const arr = [...prev];
      const yeni = arr[tipIdx].yeniSecenek.trim();
      if (!yeni) return arr;
      arr[tipIdx] = {...arr[tipIdx], secenekler:[...arr[tipIdx].secenekler, yeni], yeniSecenek:''};
      return arr;
    });
  };

  const kaydet = async (tipIdx) => {
    const o = ozellikler[tipIdx];
    if (!o) { alert('Özellik bulunamadı'); return; }
    const token = localStorage.getItem('auth_token');
    try {
      const resp = await fetch(`/api/admin/urun-ozellikler/${secUrun}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', 'X-Token': token || '' },
        body: JSON.stringify({ ozellikTip: o.ozellikTip, secenekler: o.secenekler })
      });
      const data = await resp.json();
      if (data.ok) {
        setKaydMesaj(o.ozellikTip + ' kaydedildi ✓');
        setTimeout(() => setKaydMesaj(''), 2000);
      } else {
        alert('Hata: ' + (data.hata || 'Bilinmeyen hata'));
      }
    } catch(e) {
      alert('Hata: ' + e.message);
    }
  };

  const sifirla = async (tipIdx) => {
    const o = ozellikler[tipIdx];
    if (!window.confirm('Varsayılan değerlere dön?')) return;
    try {
      await deleteUrunOzellikOverride(secUrun, o.ozellikTip);
      setOzellikler(prev => {
        const arr = [...prev];
        arr[tipIdx] = {...arr[tipIdx], secenekler:[...arr[tipIdx].varsayilan], overrideVar:false};
        return arr;
      });
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const tipSil = async (tipIdx) => {
    const o = ozellikler[tipIdx];
    if (!window.confirm(`"${o.ozellikTip}" özellik tipi silinsin mi?`)) return;
    try {
      await deleteUrunOzellikOverride(secUrun, o.ozellikTip);
      const yeni = ozellikler.filter((_, i) => i !== tipIdx);
      setOzellikler(yeni);
      if (onOzelliklerChange) onOzelliklerChange(yeni);
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const gruplarABak = {};
  urunler.forEach(u => {
    if (!gruplarABak[u.kategori]) gruplarABak[u.kategori] = [];
    gruplarABak[u.kategori].push(u);
  });

  return (
    <div style={{display:'grid', gridTemplateColumns:'260px 1fr', gap:20, position:'relative'}}>
      {/* Sol: Ürün Listesi */}
      <div style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,padding:12}}>
        <div style={{fontSize:10,fontWeight:700,letterSpacing:'.08em',textTransform:'uppercase',
          color:'var(--muted)',marginBottom:10}}>Ürünler</div>
        {Object.entries(gruplarABak).map(([kat, urunListesi]) => (
          <div key={kat}>
            <div style={{fontSize:10,fontWeight:700,color:'var(--muted)',padding:'6px 4px 2px',
              textTransform:'uppercase',letterSpacing:'.06em'}}>{kat}</div>
            {urunListesi.map(u => (
              <div key={u.kod} onClick={()=>urunSec(u.kod)}
                style={{padding:'6px 10px',borderRadius:6,cursor:'pointer',fontSize:12,
                  background: secUrun===u.kod?'var(--accent)':'transparent',
                  color: secUrun===u.kod?'#000':'var(--text)'}}>
                {u.ad}
              </div>
            ))}
          </div>
        ))}
      </div>

      {/* Sağ: Özellik Düzenleyici */}
      <div>
        {!secUrun && (
          <div style={{display:'flex',alignItems:'center',justifyContent:'center',
            height:200,color:'var(--muted)',fontSize:14}}>
            ← Soldan bir ürün seçin
          </div>
        )}
        {yukleniyor && <div style={{padding:20,color:'var(--muted)'}}>Yükleniyor...</div>}
        {secUrun && !yukleniyor && (
          <div style={{display:'flex',flexDirection:'column',gap:12}}>
            {/* Seçilen ürün başlığı */}
            <div style={{background:'var(--surface2)',border:'1px solid var(--border)',
              borderRadius:8,padding:'10px 16px',borderLeft:'3px solid var(--accent)'}}>
              <div style={{fontSize:10,fontWeight:700,letterSpacing:'.08em',
                textTransform:'uppercase',color:'var(--muted)',marginBottom:2}}>Seçilen Ürün</div>
              <div style={{fontFamily:'var(--font-head)',fontSize:16,fontWeight:700}}>
                {urunler.find(u=>u.kod===secUrun)?.ad || secUrun}
              </div>
              <div style={{fontSize:11,color:'var(--muted)',marginTop:1}}>{secUrun}</div>
              {/* Ticari Kod */}
              <div style={{marginTop:12,display:'flex',alignItems:'center',gap:8}}>
                <div style={{fontSize:11,color:'var(--muted)',fontWeight:700,letterSpacing:'.06em',textTransform:'uppercase',minWidth:80}}>Ticari Kod</div>
                <input
                  className="input"
                  style={{width:160,padding:'5px 8px',fontSize:13}}
                  placeholder="örn: GLEA SD"
                  value={ticariKod}
                  onChange={e=>setTicariKod(e.target.value)}
                  onKeyDown={async e=>{
                    if(e.key==='Enter'){
                      await fetch(`/api/admin/ticari-kod/${secUrun}`,{method:'PUT',headers:{'Content-Type':'application/json','X-Token':localStorage.getItem('auth_token')||''},body:JSON.stringify({ticariKod})});
                      setTicariKodMesaj('✓ Kaydedildi'); setTimeout(()=>setTicariKodMesaj(''),2000);
                    }
                  }}
                />
                <button className="btn btn-primary btn-sm" onClick={async()=>{
                  await fetch(`/api/admin/ticari-kod/${secUrun}`,{method:'PUT',headers:{'Content-Type':'application/json','X-Token':localStorage.getItem('auth_token')||''},body:JSON.stringify({ticariKod})});
                  setTicariKodMesaj('✓ Kaydedildi'); setTimeout(()=>setTicariKodMesaj(''),2000);
                }}>Kaydet</button>
                {ticariKodMesaj && <span style={{color:'var(--green)',fontSize:12}}>{ticariKodMesaj}</span>}
              </div>
            </div>
            <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:8}}>
              {kaydMesaj && <div style={{color:'var(--green)',fontSize:13,fontWeight:600}}>{kaydMesaj}</div>}
              <button className="btn btn-secondary btn-sm" style={{fontSize:12,marginLeft:'auto'}}
                onClick={onYeniTipAc}>+ Yeni Özellik Tipi Ekle</button>
            </div>
            {ozellikler.length === 0 && (
              <div style={{color:'var(--muted)',fontSize:13}}>Bu ürün için özellik tanımlı değil.</div>
            )}
            {ozellikler.map((o, tipIdx) => (
              <div key={o.ozellikTip} style={{background:'var(--surface2)',
                border:'1px solid var(--border)',borderRadius:10,overflow:'hidden'}}>
                <div style={{padding:'10px 16px',background:'var(--surface)',
                  borderBottom:'1px solid var(--border)',display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                  <span style={{fontWeight:700,fontFamily:'var(--font-head)',fontSize:14}}>
                    {tipLabel[o.ozellikTip]||o.ozellikTip}
                    {o.overrideVar && <span style={{fontSize:10,color:'var(--accent)',marginLeft:8}}>● Özelleştirilmiş</span>}
                  </span>
                  <div style={{display:'flex',gap:8}}>
                    <button className="btn btn-primary btn-sm" style={{fontSize:11}}
                      onClick={()=>kaydet(tipIdx)}>Kaydet</button>
                    <button className="btn btn-secondary btn-sm"
                      style={{fontSize:11,color:'var(--red)',borderColor:'var(--red)'}}
                      onClick={()=>tipSil(tipIdx)}>Sil</button>
                  </div>
                </div>
                <div style={{padding:12,display:'flex',flexDirection:'column',gap:6}}>
                  {o.secenekler.map((s, secIdx) => (
                    <div key={secIdx} style={{display:'flex',gap:8,alignItems:'center'}}>
                      <input className="input" value={s} style={{flex:1,fontSize:12,padding:'5px 8px'}}
                        onChange={e=>secenek(tipIdx,secIdx,e.target.value)}/>
                      <button style={{background:'none',border:'none',color:'var(--red)',
                        cursor:'pointer',fontSize:18,lineHeight:1,padding:'0 4px'}}
                        onClick={()=>secenek_sil(tipIdx,secIdx)}>×</button>
                    </div>
                  ))}
                  {/* Yeni seçenek ekle — feature_ratio'dan dropdown */}
                  <div style={{display:'flex',gap:8,marginTop:4}}>
                    <select className="select"
                      style={{flex:1,fontSize:12,padding:'5px 8px',borderStyle:'dashed',
                        background:'var(--surface)',color:'var(--muted)'}}
                      value={o.yeniSecenek||''}
                      onChange={e=>setOzellikler(prev=>{
                        const arr=[...prev];
                        arr[tipIdx]={...arr[tipIdx],yeniSecenek:e.target.value};
                        return arr;
                      })}>
                      <option value="">— Eklenecek seçeneği seç —</option>
                      {(ratioSecenekler[o.ozellikTip]||[])
                        .filter(s => !o.secenekler.includes(s))
                        .map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                    <button className="btn btn-secondary btn-sm" style={{fontSize:12}}
                      disabled={!o.yeniSecenek}
                      onClick={()=>secenek_ekle(tipIdx)}>+ Ekle</button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}


/* ── Özellik Oranları Paneli ── */
function OzellikOranlariPanel({ oranlar, oranEdit, setOranEdit, onSave, onAdd, onDelete }) {
  const [yeni, setYeni] = React.useState({ featureType:'', optionName:'', ratio:0, isSabit:false });
  const [ekleHata, setEkleHata] = React.useState('');

  const gruplar = {};
  oranlar.forEach(o => {
    if (!gruplar[o.featureType]) gruplar[o.featureType] = [];
    gruplar[o.featureType].push(o);
  });

  const tipLabel = {
    CERCEVE_TIPI:'Çerçeve Tipi', DAMPER_TIPI:'Damper Tipi', RAL:'RAL / Renk',
    MONTAJ:'Montaj', AKSESUAR_TIPI:'Aksesuar Tipi', MENFEZ_TIPI:'Menfez Tipi'
  };

  const tipSecenekleri = [
    'CERCEVE_TIPI','DAMPER_TIPI','RAL','MONTAJ','AKSESUAR_TIPI','MENFEZ_TIPI'
  ];

  const handleEkle = async () => {
    if (!yeni.featureType) { setEkleHata('Özellik tipi seçiniz.'); return; }
    if (!yeni.optionName.trim()) { setEkleHata('Seçenek adı giriniz.'); return; }
    setEkleHata('');
    await onAdd(yeni);
    setYeni({ featureType:'', optionName:'', ratio:0, isSabit:false });
  };

  return (
    <div style={{display:'flex',flexDirection:'column',gap:16}}>

      {/* Yeni Ekle Formu */}
      <div style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,padding:16}}>
        <div style={{fontWeight:700,fontFamily:'var(--font-head)',fontSize:14,marginBottom:12}}>
          + Yeni Seçenek Ekle
        </div>
        <div style={{display:'grid',gridTemplateColumns:'1.5fr 2fr 1fr 1fr auto',gap:10,alignItems:'end'}}>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Özellik Tipi</label>
            <select className="select" style={{fontSize:12,padding:'6px 8px'}}
              value={yeni.featureType} onChange={e=>setYeni(v=>({...v,featureType:e.target.value}))}>
              <option value="">— Seç —</option>
              {tipSecenekleri.map(t=><option key={t} value={t}>{tipLabel[t]||t}</option>)}
            </select>
          </div>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Seçenek Adı</label>
            <input className="input" style={{fontSize:12,padding:'6px 8px'}}
              placeholder="Örn: Galvaniz Kaplı" value={yeni.optionName}
              onChange={e=>setYeni(v=>({...v,optionName:e.target.value}))}/>
          </div>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Oran (%)</label>
            <input className="input" type="number" step="0.01" min="0"
              style={{fontSize:12,padding:'6px 8px'}} value={yeni.ratio}
              onChange={e=>setYeni(v=>({...v,ratio:parseFloat(e.target.value||0)}))}/>
          </div>
          <div style={{textAlign:'center'}}>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Sabit</label>
            <input type="checkbox" checked={yeni.isSabit}
              onChange={e=>setYeni(v=>({...v,isSabit:e.target.checked}))}/>
          </div>
          <button className="btn btn-primary" style={{padding:'6px 16px',fontSize:12}}
            onClick={handleEkle}>Ekle</button>
        </div>
        {ekleHata && <div style={{color:'var(--red)',fontSize:12,marginTop:8}}>⚠️ {ekleHata}</div>}
      </div>

      {/* Mevcut oranlar */}
      {Object.entries(gruplar).map(([tip, oran_listesi]) => (
        <div key={tip} style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,overflow:'hidden'}}>
          <div style={{padding:'10px 16px',background:'var(--surface)',borderBottom:'1px solid var(--border)',
            fontWeight:700,fontFamily:'var(--font-head)',fontSize:14}}>
            {tipLabel[tip]||tip}
          </div>
          <table style={{width:'100%',borderCollapse:'collapse'}}>
            <thead>
              <tr style={{background:'var(--surface)'}}>
                <th style={{padding:'8px 16px',textAlign:'left',fontSize:12,color:'var(--muted)',fontWeight:600}}>Seçenek</th>
                <th style={{padding:'8px 16px',textAlign:'center',fontSize:12,color:'var(--muted)',fontWeight:600}}>Oran (%)</th>
                <th style={{padding:'8px 16px',textAlign:'center',fontSize:12,color:'var(--muted)',fontWeight:600}}>Sabit</th>
                <th style={{padding:'8px 16px',textAlign:'center',fontSize:12,color:'var(--muted)',fontWeight:600}}>Kaydet</th>
                <th style={{padding:'8px 16px',textAlign:'center',fontSize:12,color:'var(--muted)',fontWeight:600}}>Sil</th>
              </tr>
            </thead>
            <tbody>
              {oran_listesi.map((o,i) => {
                const key = o.featureType+'|'+o.optionName;
                const currentRatio = oranEdit[key] ?? o.ratio;
                return (
                  <tr key={o.optionName} style={{borderTop:'1px solid var(--border)',
                    background: i%2===0?'transparent':'var(--surface)'}}>
                    <td style={{padding:'8px 16px',fontSize:13}}>{o.optionName}</td>
                    <td style={{padding:'8px 16px',textAlign:'center'}}>
                      <input type="number" step="0.01" min="0"
                        style={{width:80,textAlign:'center',padding:'4px 6px',
                          background:'var(--surface2)',border:'1px solid var(--border)',
                          borderRadius:4,color:'var(--text)',fontSize:12}}
                        value={Math.round(currentRatio*100*100)/100}
                        disabled={o.isSabit}
                        onChange={e=>setOranEdit(prev=>({...prev,[key]:parseFloat(e.target.value||0)/100}))}/>
                    </td>
                    <td style={{padding:'8px 16px',textAlign:'center'}}>
                      <input type="checkbox" checked={o.isSabit}
                        onChange={e=>onSave(o.featureType,o.optionName,currentRatio,e.target.checked)}/>
                    </td>
                    <td style={{padding:'8px 16px',textAlign:'center'}}>
                      <button className="btn btn-primary btn-sm" style={{fontSize:11,padding:'3px 10px'}}
                        onClick={()=>onSave(o.featureType,o.optionName,currentRatio,o.isSabit)}>
                        Kaydet
                      </button>
                    </td>
                    <td style={{padding:'8px 16px',textAlign:'center'}}>
                      <button style={{background:'none',border:'none',color:'var(--red)',
                        cursor:'pointer',fontSize:16,padding:'2px 6px'}}
                        onClick={()=>onDelete(o.featureType,o.optionName)}>×</button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ))}

    </div>
  );
}

/* ── Ürün Bazlı Oran Paneli ── */
function UrunOranlariPanel() {
  const OZELLIK_TIPLERI = [
    {val:'DAMPER_TIPI',lbl:'Damper Tipi'},{val:'CERCEVE_TIPI',lbl:'Çerçeve Tipi'},
    {val:'RAL',lbl:'RAL / Renk'},{val:'MONTAJ',lbl:'Montaj'},
    {val:'AKSESUAR_TIPI',lbl:'Aksesuar Tipi'},{val:'MENFEZ_TIPI',lbl:'Menfez Tipi'},
  ];

  const [urunler, setUrunler] = React.useState([]);
  const [secUrun, setSecUrun] = React.useState('');
  const [oranlar, setOranlar] = React.useState([]);
  const [tumOranlar, setTumOranlar] = React.useState([]);
  const [form, setForm] = React.useState({featureType:'DAMPER_TIPI', optionName:'', ratio:0, isSabit:false});
  const [hata, setHata] = React.useState('');
  const [mesaj, setMesaj] = React.useState('');

  const mevcutSecenekler = [...new Set(
    tumOranlar.filter(o => o.featureType === form.featureType).map(o => o.optionName)
  )];

  React.useEffect(() => {
    fetch('/api/urunler')
      .then(r=>r.json()).then(setUrunler).catch(()=>{});
    getOzellikOranlari().then(r => setTumOranlar(r.data)).catch(()=>{});
  }, []);

  const yukle = (kod) => {
    if (!kod) return;
    getUrunOranlari(kod).then(r => setOranlar(r.data)).catch(()=>{});
  };

  const kaydet = async () => {
    setHata(''); setMesaj('');
    if (!secUrun)          { setHata('Ürün seçiniz.'); return; }
    if (!form.optionName)  { setHata('Seçenek seçiniz.'); return; }
    try {
      await addUrunOrani(secUrun, {...form, ratio: parseFloat(form.ratio)/100});
      setMesaj('Kaydedildi ✓');
      setTimeout(()=>setMesaj(''),2000);
      yukle(secUrun);
    } catch(e) { setHata(e.response?.data?.hata||e.message); }
  };

  const sil = async (o) => {
    if (!window.confirm(`"${o.optionName}" silinsin mi?`)) return;
    try {
      await deleteUrunOrani(secUrun, {featureType:o.featureType, optionName:o.optionName});
      yukle(secUrun);
    } catch(e) { alert('Hata: '+e.message); }
  };

  const tipLabel = (t) => OZELLIK_TIPLERI.find(x=>x.val===t)?.lbl||t;

  const gruplar = {};
  oranlar.forEach(o => {
    if (!gruplar[o.featureType]) gruplar[o.featureType]=[];
    gruplar[o.featureType].push(o);
  });

  return (
    <div style={{display:'flex',flexDirection:'column',gap:16}}>
      <div style={{background:'var(--surface2)',border:'1px solid var(--accent)40',
        borderRadius:10,padding:'12px 16px',fontSize:13,color:'var(--muted)'}}>
        Belirli bir ürün için varsayılan oranı geçersiz kılın.
        Ürün bazlı oran, kategori ve varsayılan oranın önüne geçer.
      </div>

      {/* Ürün seç + form */}
      <div style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,padding:16}}>
        <div style={{fontWeight:700,fontFamily:'var(--font-head)',fontSize:14,marginBottom:12}}>
          Ürün Seç ve Oran Ekle
        </div>
        <div style={{marginBottom:12}}>
          <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Ürün</label>
          <select className="select" style={{fontSize:12,padding:'6px 8px',width:300}}
            value={secUrun} onChange={e=>{setSecUrun(e.target.value);yukle(e.target.value);}}>
            <option value="">— Ürün Seçiniz —</option>
            {urunler.map(u=><option key={u.kod} value={u.kod}>{u.ad} ({u.kod})</option>)}
          </select>
        </div>
        {secUrun && (
          <div style={{display:'grid',gridTemplateColumns:'1.5fr 2fr 1fr 1fr auto',gap:10,alignItems:'end'}}>
            <div>
              <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Özellik Tipi</label>
              <select className="select" style={{fontSize:12,padding:'6px 8px'}}
                value={form.featureType}
                onChange={e=>setForm(f=>({...f,featureType:e.target.value,optionName:''}))}>
                {OZELLIK_TIPLERI.map(t=><option key={t.val} value={t.val}>{t.lbl}</option>)}
              </select>
            </div>
            <div>
              <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Seçenek</label>
              <select className="select" style={{fontSize:12,padding:'6px 8px'}}
                value={form.optionName} onChange={e=>setForm(f=>({...f,optionName:e.target.value}))}>
                <option value="">— Seç —</option>
                {mevcutSecenekler.map(s=><option key={s} value={s}>{s}</option>)}
              </select>
            </div>
            <div>
              <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Oran (%)</label>
              <input className="input" type="number" step="0.01" min="0"
                style={{fontSize:12,padding:'6px 8px'}}
                value={form.ratio} onChange={e=>setForm(f=>({...f,ratio:e.target.value}))}/>
            </div>
            <div style={{textAlign:'center'}}>
              <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Sabit</label>
              <input type="checkbox" checked={form.isSabit}
                onChange={e=>setForm(f=>({...f,isSabit:e.target.checked}))}/>
            </div>
            <button className="btn btn-primary" style={{padding:'6px 16px',fontSize:12}} onClick={kaydet}>
              Ekle
            </button>
          </div>
        )}
        {hata  && <div style={{color:'var(--red)',  fontSize:12,marginTop:8}}>⚠️ {hata}</div>}
        {mesaj && <div style={{color:'var(--green)',fontSize:12,marginTop:8}}>{mesaj}</div>}
      </div>

      {/* Mevcut ürün oranları */}
      {secUrun && Object.keys(gruplar).length === 0 && (
        <div style={{color:'var(--muted)',fontSize:13,padding:20,textAlign:'center'}}>
          Bu ürün için özel oran tanımlanmamış.
        </div>
      )}
      {secUrun && Object.keys(gruplar).length > 0 && (
        <div style={{background:'var(--surface2)',border:'1px solid var(--border)',
          borderRadius:10,overflow:'hidden'}}>
          <div style={{padding:'10px 16px',background:'var(--surface)',
            borderBottom:'1px solid var(--border)',fontWeight:700,
            fontFamily:'var(--font-head)',fontSize:14,color:'var(--accent)'}}>
            {urunler.find(u=>u.kod===secUrun)?.ad || secUrun} — Özel Oranlar
          </div>
          {Object.entries(gruplar).map(([tip,liste])=>(
            <div key={tip}>
              <div style={{padding:'6px 16px',background:'var(--surface2)',fontSize:11,
                fontWeight:700,letterSpacing:'.06em',textTransform:'uppercase',
                color:'var(--muted)',borderBottom:'1px solid var(--border)'}}>
                {tipLabel(tip)}
              </div>
              <table style={{width:'100%',borderCollapse:'collapse'}}>
                <tbody>
                  {liste.map((o,i)=>(
                    <tr key={o.optionName} style={{borderBottom:'1px solid var(--border)',
                      background:i%2===0?'transparent':'var(--surface)'}}>
                      <td style={{padding:'8px 16px',fontSize:13}}>{o.optionName}</td>
                      <td style={{padding:'8px 16px',fontSize:13,textAlign:'center',
                        color:'var(--accent)',fontWeight:600}}>
                        {o.isSabit?'Sabit (0%)':`%${Math.round(o.ratio*100*100)/100}`}
                      </td>
                      <td style={{padding:'8px 16px',textAlign:'center'}}>
                        <button style={{background:'none',border:'none',color:'var(--red)',
                          cursor:'pointer',fontSize:18}} onClick={()=>sil(o)}>×</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

/* ── Kategori Oranları Paneli ── */
function KategoriOranlariPanel() {
  const KATEGORILER = [
    {val:'MENFEZ',label:'Menfez'}, {val:'SLOT',label:'Slot / Lineer'},
    {val:'DIKDORTGEN_DAMPER',label:'Dikdörtgen Damper'}, {val:'DAIRESEL_DAMPER',label:'Dairesel Damper'},
    {val:'KARE_ANEMOSTAD',label:'Kare Anemostad'}, {val:'DAIRESEL_ANEMOSTAD',label:'Dairesel Anemostad'},
    {val:'KARE_SWIRL',label:'Kare Swirl'}, {val:'DAIRESEL_SWIRL',label:'Dairesel Swirl'},
    {val:'PANJUR',label:'Panjur'}, {val:'KAPAK',label:'Kapak'}, {val:'KUTU',label:'Kutu'},
  ];
  const OZELLIK_TIPLERI = [
    {val:'DAMPER_TIPI',label:'Damper Tipi'},{val:'CERCEVE_TIPI',label:'Çerçeve Tipi'},
    {val:'RAL',label:'RAL / Renk'},{val:'MONTAJ',label:'Montaj'},
    {val:'AKSESUAR_TIPI',label:'Aksesuar Tipi'},{val:'MENFEZ_TIPI',label:'Menfez Tipi'},
  ];

  const [oranlar, setOranlar] = React.useState([]);
  const [tumOranlar, setTumOranlar] = React.useState([]); // varsayılan oranlar (seçenek listesi için)
  const [form, setForm] = React.useState({
    urunKategori:'', featureType:'DAMPER_TIPI', optionName:'', ratio:0, isSabit:false
  });
  const [hata, setHata] = React.useState('');
  const [mesaj, setMesaj] = React.useState('');

  // Seçili özellik tipine göre mevcut seçenekler
  const mevcutSecenekler = [...new Set(
    tumOranlar.filter(o => o.featureType === form.featureType).map(o => o.optionName)
  )];

  React.useEffect(() => {
    yukle();
    getOzellikOranlari().then(r => setTumOranlar(r.data)).catch(() => {});
  }, []);

  const yukle = () => {
    getKategoriOranlari().then(r => setOranlar(r.data)).catch(() => {});
  };

  const kaydet = async () => {
    setHata(''); setMesaj('');
    if (!form.urunKategori) { setHata('Kategori seçiniz.'); return; }
    if (!form.optionName)   { setHata('Seçenek seçiniz.');  return; }
    try {
      await addKategoriOrani({...form, ratio: parseFloat(form.ratio)/100});
      setMesaj('Kaydedildi ✓');
      setTimeout(() => setMesaj(''), 2000);
      yukle();
    } catch(e) { setHata(e.response?.data?.hata || e.message); }
  };

  const sil = async (o) => {
    if (!window.confirm(`"${o.optionName}" / ${o.urunKategori} silinsin mi?`)) return;
    try {
      await deleteKategoriOrani({ featureType: o.featureType, optionName: o.optionName, urunKategori: o.urunKategori });
      yukle();
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const katLabel = (k) => KATEGORILER.find(c=>c.val===k)?.label || k;
  const tipLabel = (t) => OZELLIK_TIPLERI.find(c=>c.val===t)?.label || t;

  // Grupla: kategori → tip → oranlar
  const gruplar = {};
  oranlar.forEach(o => {
    const k = o.urunKategori;
    if (!gruplar[k]) gruplar[k] = {};
    if (!gruplar[k][o.featureType]) gruplar[k][o.featureType] = [];
    gruplar[k][o.featureType].push(o);
  });

  return (
    <div style={{display:'flex',flexDirection:'column',gap:16}}>

      {/* Açıklama */}
      <div style={{background:'var(--surface2)',border:'1px solid var(--accent)40',
        borderRadius:10,padding:'12px 16px',fontSize:13,color:'var(--muted)'}}>
        Burada belirli bir kategorideki tüm ürünler için özel oran tanımlayabilirsiniz.
        Kategori oranı, varsayılan oranın önüne geçer. Tanımlanmamış seçenekler için
        varsayılan oran kullanılmaya devam eder.
      </div>

      {/* Yeni oran formu */}
      <div style={{background:'var(--surface2)',border:'1px solid var(--border)',borderRadius:10,padding:16}}>
        <div style={{fontWeight:700,fontFamily:'var(--font-head)',fontSize:14,marginBottom:12}}>
          + Kategori Bazlı Oran Ekle
        </div>
        <div style={{display:'grid',gridTemplateColumns:'1.5fr 1.5fr 2fr 1fr 1fr auto',gap:10,alignItems:'end'}}>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Kategori</label>
            <select className="select" style={{fontSize:12,padding:'6px 8px'}}
              value={form.urunKategori} onChange={e=>setForm(f=>({...f,urunKategori:e.target.value}))}>
              <option value="">— Seç —</option>
              {KATEGORILER.map(k=><option key={k.val} value={k.val}>{k.label}</option>)}
            </select>
          </div>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Özellik Tipi</label>
            <select className="select" style={{fontSize:12,padding:'6px 8px'}}
              value={form.featureType}
              onChange={e=>setForm(f=>({...f,featureType:e.target.value,optionName:''}))}>
              {OZELLIK_TIPLERI.map(t=><option key={t.val} value={t.val}>{t.label}</option>)}
            </select>
          </div>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Seçenek</label>
            <select className="select" style={{fontSize:12,padding:'6px 8px'}}
              value={form.optionName} onChange={e=>setForm(f=>({...f,optionName:e.target.value}))}>
              <option value="">— Seç —</option>
              {mevcutSecenekler.map(s=><option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Oran (%)</label>
            <input className="input" type="number" step="0.01" min="0"
              style={{fontSize:12,padding:'6px 8px'}}
              value={form.ratio} onChange={e=>setForm(f=>({...f,ratio:e.target.value}))}/>
          </div>
          <div style={{textAlign:'center'}}>
            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:4}}>Sabit</label>
            <input type="checkbox" checked={form.isSabit}
              onChange={e=>setForm(f=>({...f,isSabit:e.target.checked}))}/>
          </div>
          <button className="btn btn-primary" style={{padding:'6px 16px',fontSize:12}} onClick={kaydet}>
            Ekle
          </button>
        </div>
        {hata  && <div style={{color:'var(--red)',  fontSize:12,marginTop:8}}>⚠️ {hata}</div>}
        {mesaj && <div style={{color:'var(--green)',fontSize:12,marginTop:8}}>{mesaj}</div>}
      </div>

      {/* Mevcut kategori oranları */}
      {Object.keys(gruplar).length === 0 ? (
        <div style={{color:'var(--muted)',fontSize:13,padding:'20px',textAlign:'center'}}>
          Henüz kategori bazlı oran tanımlanmamış.
        </div>
      ) : Object.entries(gruplar).map(([kat, tipler]) => (
        <div key={kat} style={{background:'var(--surface2)',border:'1px solid var(--border)',
          borderRadius:10,overflow:'hidden'}}>
          <div style={{padding:'10px 16px',background:'var(--surface)',
            borderBottom:'1px solid var(--border)',fontWeight:700,
            fontFamily:'var(--font-head)',fontSize:14,color:'var(--accent)'}}>
            {katLabel(kat)}
          </div>
          {Object.entries(tipler).map(([tip, oranListesi]) => (
            <div key={tip}>
              <div style={{padding:'6px 16px',background:'var(--surface2)',fontSize:11,
                fontWeight:700,letterSpacing:'.06em',textTransform:'uppercase',
                color:'var(--muted)',borderBottom:'1px solid var(--border)'}}>
                {tipLabel(tip)}
              </div>
              <table style={{width:'100%',borderCollapse:'collapse'}}>
                <tbody>
                  {oranListesi.map((o,i) => (
                    <tr key={o.optionName} style={{borderBottom:'1px solid var(--border)',
                      background:i%2===0?'transparent':'var(--surface)'}}>
                      <td style={{padding:'8px 16px',fontSize:13}}>{o.optionName}</td>
                      <td style={{padding:'8px 16px',fontSize:13,textAlign:'center',
                        color:'var(--accent)',fontWeight:600}}>
                        {o.isSabit ? 'Sabit (0%)' : `%${Math.round(o.ratio*100*100)/100}`}
                      </td>
                      <td style={{padding:'8px 16px',textAlign:'center'}}>
                        <button style={{background:'none',border:'none',color:'var(--red)',
                          cursor:'pointer',fontSize:18}} onClick={()=>sil(o)}>×</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ))}
        </div>
      ))}
    </div>
  );
}

export default function UrunYonetimPage() {
  const [sekme, setSekme] = useState('ekle');
  const [yeniTipModal, setYeniTipModal] = useState(false);
  const [yeniTip, setYeniTip] = useState('');
  const [ozelliklerRef, setOzelliklerRef] = useState([]);
  const [secUrunRef, setSecUrunRef] = useState(null);
  const [oranlar, setOranlar] = useState([]);
  const [oranEdit, setOranEdit] = useState({});

  const oranlarYukle = () => {
    getOzellikOranlari().then(r => {
      setOranlar(r.data);
      const edits = {};
      r.data.forEach(o => { edits[o.featureType+'|'+o.optionName] = o.ratio; });
      setOranEdit(edits);
    }).catch(() => {});
  };

  React.useEffect(() => {
    if (sekme === 'oranlar') oranlarYukle();
  }, [sekme]);

  React.useEffect(() => {
    oranlarYukle();
  }, []);

  const tabBtn = (key, label) => (
    <button className="btn btn-sm" key={key}
      style={{
        background: sekme===key ? 'var(--accent)' : 'var(--surface2)',
        color: sekme===key ? '#000' : 'var(--text)',
        border: '1px solid var(--border)'
      }}
      onClick={() => setSekme(key)}>{label}</button>
  );

  return (
    <div style={{ padding:28, flex:1 }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:24 }}>
        <h1 style={{ fontFamily:'var(--font-head)', fontSize:24, fontWeight:800 }}>Ürün Yönetimi</h1>
        <div style={{ display:'flex', gap:6 }}>
          {tabBtn('ekle',    '+ Ürün Ekle')}
          {tabBtn('ozellik', 'Ürün Özellikleri')}
          {tabBtn('oranlar', 'Özellik Oranları')}
          {tabBtn('kat-oranlar', 'Kategori Oranları')}
          {tabBtn('urun-oranlar', 'Ürün Oranları')}
        </div>
      </div>

      {sekme === 'ekle'    && <UrunEklePanel/>}
      {sekme === 'ozellik' && <UrunOzellikPanel
          onYeniTipAc={() => setYeniTipModal(true)}
          onOzelliklerChange={setOzelliklerRef}
          onSecUrunChange={setSecUrunRef}/>}

      {yeniTipModal && (
        <div style={{position:'fixed',top:0,left:0,width:'100vw',height:'100vh',
          background:'rgba(0,0,0,.5)',display:'flex',alignItems:'center',
          justifyContent:'center',zIndex:9999}}
          onClick={()=>setYeniTipModal(false)}>
          <div style={{background:'var(--surface)',borderRadius:14,padding:28,width:380,
            border:'1px solid var(--border)'}} onClick={e=>e.stopPropagation()}>
            <h3 style={{fontFamily:'var(--font-head)',marginBottom:16}}>Yeni Özellik Tipi Ekle</h3>
            <label style={{fontSize:12,color:'var(--muted)',display:'block',marginBottom:6}}>Özellik Tipi</label>
            <select className="select" style={{width:'100%',fontSize:13,marginBottom:16}}
              value={yeniTip} onChange={e=>setYeniTip(e.target.value)}>
              <option value="">— Seçiniz —</option>
              {['CERCEVE_TIPI','DAMPER_TIPI','RAL','MONTAJ','AKSESUAR_TIPI','MENFEZ_TIPI']
                .filter(t => !ozelliklerRef.some(o => o.ozellikTip === t))
                .map(t => {
                  const lbl = {CERCEVE_TIPI:'Çerçeve Tipi',DAMPER_TIPI:'Damper Tipi',
                    RAL:'RAL / Renk',MONTAJ:'Montaj',AKSESUAR_TIPI:'Aksesuar Tipi',
                    MENFEZ_TIPI:'Menfez Tipi'}[t]||t;
                  return <option key={t} value={t}>{lbl}</option>;
                })}
            </select>
            <div style={{display:'flex',gap:8}}>
              <button className="btn btn-primary" style={{flex:1}}
                disabled={!yeniTip || !secUrunRef}
                onClick={async () => {
                  try {
                    await updateUrunOzellikler(secUrunRef, { ozellikTip: yeniTip, secenekler: [] });
                    setYeniTipModal(false); setYeniTip('');
                  } catch(e) { alert('Hata: ' + e.message); }
                }}>Ekle</button>
              <button className="btn btn-secondary"
                onClick={()=>{setYeniTipModal(false);setYeniTip('');}}>İptal</button>
            </div>
          </div>
        </div>
      )}
      {sekme === 'kat-oranlar' && <KategoriOranlariPanel/>}
      {sekme === 'urun-oranlar' && <UrunOranlariPanel/>}

      {sekme === 'oranlar' && (
        <OzellikOranlariPanel oranlar={oranlar} oranEdit={oranEdit} setOranEdit={setOranEdit}
          onSave={async (featureType, optionName, ratio, isSabit) => {
            try {
              await updateOzellikOrani({ featureType, optionName, ratio, isSabit });
              oranlarYukle();
            } catch(e) { alert('Hata: ' + (e.response?.data?.hata||e.message)); }
          }}
          onAdd={async (yeni) => {
            try {
              await addOzellikOrani({ ...yeni, ratio: yeni.ratio/100 });
              oranlarYukle();
            } catch(e) { alert('Hata: ' + (e.response?.data?.hata||e.message)); }
          }}
          onDelete={async (featureType, optionName) => {
            if (!window.confirm(`"${optionName}" silinsin mi?`)) return;
            try {
              await deleteOzellikOrani({ featureType, optionName });
              oranlarYukle();
            } catch(e) { alert('Hata: ' + (e.response?.data?.hata||e.message)); }
          }}/>
      )}
    </div>
  );
}
