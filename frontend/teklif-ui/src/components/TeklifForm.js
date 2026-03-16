import React, { useState, useEffect, useCallback } from 'react';
import { createTeklif, updateTeklif, getYeniNo, getMusteriler, getKurlar, getUrunler, fiyatHesapla } from '../services/api';

/* ── Sabitler ─────────────────────────────────────────── */
const EMPTY = {
  teklifNo:'', isAdi:'', musteriId:'',
  teklifTarihi: new Date().toISOString().slice(0,10),
  gecerlilikTarihi:'', teklifiVeren:'',
  paraBirimi:'TL', kdvOrani:20, notlar:'', kalemler:[]
};
const EMPTY_KALEM = { urunKodu:'', urunAdi:'', adet:1, birim:'Adet', birimFiyat:0, iskonto:0 };

const OLCU_LABEL = {
  GENISLIK:'Genişlik W (mm)', YUKSEKLIK:'Yükseklik H (mm)', UZUNLUK:'Uzunluk L (mm)',
  CAP:'Çap Ø (mm)', KASA_WH:'Kasa Ölçüsü', BOGAZ_WH:'Boğaz Ölçüsü',
  BOGAZ_CAP:'Boğaz Çap (mm)', NETIC_CAP:'Netice Çap (mm)',
  KASA_CAP:'Kasa Çap', SLOT_SAYISI:'Slot Sayısı',
};
const OZELLIK_LABEL = {
  CERCEVE_TIPI:'Çerçeve Tipi', MENFEZ_TIPI:'Tip', DAMPER_TIPI:'Damper',
  RAL:'RAL / Renk', MONTAJ:'Montaj', AKSESUAR_TIPI:'Aksesuar',
};
const KATEGORI_LABEL = {
  MENFEZ:'Menfez', SLOT:'Slot / Lineer', DIKDORTGEN_DAMPER:'Dikdörtgen Damper',
  DAIRESEL_DAMPER:'Dairesel Damper', KARE_ANEMOSTAD:'Kare Anemostad',
  DAIRESEL_ANEMOSTAD:'Dairesel Anemostad', KARE_SWIRL:'Kare Swirl',
  DAIRESEL_SWIRL:'Dairesel Swirl', PANJUR:'Panjur', KAPAK:'Kapak', KUTU:'Kutu',
};
const STRING_OLCULAR = new Set(['KASA_WH','BOGAZ_WH','KASA_CAP','BOGAZ_CAP']);
const INT_OLCULAR    = new Set(['SLOT_SAYISI']);

/* ── Yardımcı ─────────────────────────────────────────── */
const pbSembol = pb => pb==='EUR'?'€':pb==='USD'?'$':'₺';

export default function TeklifForm({ teklif, onSave, onClose }) {
  const [form, setForm]         = useState(EMPTY);
  const [musteriler, setMust]   = useState([]);
  const [kurlar, setKurlar]     = useState({ EUR:42, USD:38 });
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  /* Ürün seçim state */
  const [urunler, setUrunler]         = useState([]);
  const [kategoriler, setKategoriler] = useState([]);
  const [secKategori, setSecKategori] = useState('');
  const [secUrun, setSecUrun]         = useState(null);
  const [olcular, setOlcular]         = useState({});      // sayısal
  const [strOlcular, setStrOlcular]   = useState({});      // string combo
  const [ozellikler, setOzellikler]   = useState({});
  const [miktar, setMiktar]           = useState(1);
  const [fiyatHata, setFiyatHata]     = useState('');
  const [hesaplaniyor, setHesaplaniyor] = useState(false);
  const [ralKod, setRalKod]           = useState(''); // Boyalı seçilince RAL kodu

  useEffect(() => {
    getMusteriler().then(r => setMust(r.data)).catch(()=>{});
    getKurlar().then(r => setKurlar(r.data)).catch(()=>{});
    getUrunler().then(r => {
      setUrunler(r.data);
      setKategoriler([...new Set(r.data.map(u=>u.kategori))]);
    }).catch(()=>{});
    if (teklif) {
      setForm({...EMPTY,...teklif, kalemler:teklif.kalemler||[]});
    } else {
      getYeniNo().then(r => setForm(f=>({...f, teklifNo:r.data.teklifNo}))).catch(()=>{});
    }
  }, [teklif]);

  /* Ürün değişince formu sıfırla + ilk değerleri otomatik seç */
  const urunSec = useCallback((u) => {
    setSecUrun(u);
    setOlcular({}); setStrOlcular({}); setFiyatHata(''); setMiktar(1);
    setRalKod('');
    if (!u) { setOzellikler({}); return; }
    // Her özellik için ilk seçeneği otomatik set et
    const ilkler = {};
    for (const [tip, sec] of Object.entries(u.ozellikler||{})) {
      if (!sec||sec.length===0) continue;
      const isMulti = u.multiSelectOzellikler?.includes(tip);
      if (tip==='AKSESUAR_TIPI') continue; // aksesuar opsiyonel, boş kalsın
      ilkler[tip] = isMulti ? [sec[0]] : sec[0];
    }
    setOzellikler(ilkler);
  }, []);

  const katDegisti = (kat) => {
    setSecKategori(kat);
    setSecUrun(null);
    setOlcular({}); setStrOlcular({}); setOzellikler({});
    setFiyatHata('');
  };

  /* Ölçü setters */
  const setO  = (tip,val) => setOlcular(o=>({...o,[tip]:val}));
  const setS  = (tip,val) => {
    if (tip==='KASA_WH'||tip==='KASA_CAP')
      setStrOlcular(o=>({...o,[tip]:val, BOGAZ_WH:'', BOGAZ_CAP:''}));
    else setStrOlcular(o=>({...o,[tip]:val}));
  };
  const setOz = (tip,val,multi) => {
    if (multi) {
      setOzellikler(o=>{
        const m=Array.isArray(o[tip])?o[tip]:[];
        return {...o,[tip]:m.includes(val)?m.filter(x=>x!==val):[...m,val]};
      });
    } else setOzellikler(o=>({...o,[tip]:val}));
  };

  /* Boğaz filtresi */
  const getBogazSec = (olcuTip) => {
    if (!secUrun) return [];
    const kasa = strOlcular['KASA_WH']||strOlcular['KASA_CAP'];
    const fm = secUrun.bogazFiltreMap;
    if (fm&&kasa&&fm[kasa]) return fm[kasa];
    return secUrun.izinliOlcuDegerleri?.[olcuTip]||[];
  };

  /* Validasyon */
  const validateUrun = () => {
    if (!secUrun) return ['Ürün seçiniz.'];
    const errs = [];
    for (const olcu of secUrun.zorunluOlcular) {
      if (STRING_OLCULAR.has(olcu)) {
        if (!strOlcular[olcu]) errs.push(`"${OLCU_LABEL[olcu]||olcu}" zorunludur.`);
      } else {
        const v = olcular[olcu];
        if (!v||isNaN(parseFloat(v))||parseFloat(v)<=0)
          errs.push(`"${OLCU_LABEL[olcu]||olcu}" zorunludur.`);
      }
    }
    for (const [tip,sec] of Object.entries(secUrun.ozellikler)) {
      if (tip==='AKSESUAR_TIPI'||sec.length===0) continue;
      const s=ozellikler[tip];
      if (!s||(Array.isArray(s)?s.length===0:s===''))
        errs.push(`"${OZELLIK_LABEL[tip]||tip}" zorunludur.`);
    }
    return errs;
  };

  /* Ürün kaleme ekle */
  const urunEkle = async () => {
    // RAL Boyalı ise kod zorunlu
    if (ozellikler['RAL']==='Boyalı' && !ralKod.trim()) {
      setFiyatHata('Boyalı seçildi — RAL kodu giriniz.'); return;
    }
    const errs = validateUrun();
    if (errs.length>0) { setFiyatHata(errs[0]); return; }
    setHesaplaniyor(true); setFiyatHata('');
    try {
      const oNum={};
      for (const [k,v] of Object.entries(olcular)) {
        const n=parseFloat(v); if(!isNaN(n)) oNum[k]=n;
      }
      // RAL Boyalı değerini birleştir
      const ozelliklerGonder = {...ozellikler};
      if (ozellikler['RAL']==='Boyalı' && ralKod.trim())
        ozelliklerGonder['RAL'] = `Boyalı - ${ralKod.trim()}`;

      const r = await fiyatHesapla(secUrun.kod, {
        olcular:oNum, stringOlcular:strOlcular, ozellikler:ozelliklerGonder
      });
      const tlFiyat = r.data.toplam;
      const pb = form.paraBirimi;
      const fiyat = pb==='EUR' ? tlFiyat/(kurlar.EUR||42)
                  : pb==='USD' ? tlFiyat/(kurlar.USD||38)
                  : tlFiyat;

      /* Ürün adı oluştur - Swing gibi */
      const olcuEki = (() => {
        const kasa=strOlcular['KASA_WH'], bogaz=strOlcular['BOGAZ_WH'];
        const kasaCap=strOlcular['KASA_CAP'];
        if (kasa&&bogaz) return ` (${kasa}/${bogaz})`;
        if (kasa) return ` (${kasa})`;
        if (kasaCap) return ` (${kasaCap})`;
        const w=olcular['GENISLIK'],h=olcular['YUKSEKLIK'],cap=olcular['CAP']||olcular['BOGAZ_CAP'];
        if (w&&h) return ` ${w}x${h}`;
        if (cap) return ` Ø${cap}`;
        return '';
      })();

      const ozellikSuffix = Object.entries(ozelliklerGonder)
        .filter(([,v])=>v&&(Array.isArray(v)?v.length>0:v!==''))
        .map(([,v])=>Array.isArray(v)?v.join(', '):v)
        .join(' / ');

      let ad = secUrun.ad + olcuEki;
      if (ozellikSuffix) ad += ' — ' + ozellikSuffix;

      /* Slot prefix */
      if (secUrun.kod.startsWith('SLT') && olcular['SLOT_SAYISI'])
        ad = olcular['SLOT_SAYISI'] + ' Yarıklı ' + ad;

      // Ölçü alanlarını ayrı çıkar (DB'ye ayrı kaydedilecek)
      const yeniKalem = {
        urunKodu:    secUrun.kod,
        urunAdi:     ad,
        adet:        miktar,
        birim:       'Adet',
        birimFiyat:  Math.round(tlFiyat*100)/100, // HER ZAMAN TL
        iskonto:     0,
        // Ölçüler
        genislik:    olcular['GENISLIK']  || '',
        yukseklik:   olcular['YUKSEKLIK'] || '',
        uzunluk:     olcular['UZUNLUK']   || '',
        cap:         olcular['CAP'] || olcular['BOGAZ_CAP'] || olcular['NETIC_CAP'] || '',
        // Özellikler
        cerceveTipi: ozelliklerGonder['CERCEVE_TIPI'] || ozelliklerGonder['MENFEZ_TIPI'] || '',
        damperTipi:  ozelliklerGonder['DAMPER_TIPI']  || '',
        ralKodu:     ozelliklerGonder['RAL']           || '',
        montaj:      ozelliklerGonder['MONTAJ']        || '',
      };
      setForm(f=>({...f, kalemler:[...f.kalemler, yeniKalem]}));
      /* Sadece ölçüler ve miktar sıfırla, kategori/ürün/özellikler kalsın */
      setOlcular({}); setStrOlcular({}); setMiktar(1);
    } catch(e) {
      setFiyatHata(e.response?.data?.hata||'Fiyat hesaplanamadı.');
    } finally { setHesaplaniyor(false); }
  };

  /* Kur çevirme */
  const tleCevir = (fiyat) => {
    const pb=form.paraBirimi;
    if (pb==='EUR') return fiyat/(kurlar.EUR||42);
    if (pb==='USD') return fiyat/(kurlar.USD||38);
    return fiyat;
  };
  const fmt = (v) => v.toLocaleString('tr-TR',{minimumFractionDigits:2});

  /* Kalem işlemleri */
  const set       = (k,v) => setForm(f=>({...f,[k]:v}));
  const kalemSet  = (i,k,v) => {
    const arr=[...form.kalemler]; arr[i]={...arr[i],[k]:v};
    setForm(f=>({...f,kalemler:arr}));
  };
  const removeKalem = (i) => setForm(f=>({...f,kalemler:f.kalemler.filter((_,idx)=>idx!==i)}));
  const addManuel   = () => setForm(f=>({...f,kalemler:[...f.kalemler,{...EMPTY_KALEM}]}));

  /* Toplam hesap - birimFiyat HER ZAMAN TL saklanır, gösterimde kura çevrilir */
  const kalemToplam = (k) => {
    const net = tleCevir(k.birimFiyat||0) * (k.adet||1);
    return net - net*(k.iskonto||0)/100;
  };
  const araToplam   = form.kalemler.reduce((s,k)=>s+kalemToplam(k),0);
  const kdvTutari   = araToplam*(form.kdvOrani/100);
  const genelToplam = araToplam+kdvTutari;

  const handleSubmit = async () => {
    if (!form.isAdi.trim()) { setError('İş adı zorunludur.'); return; }
    setLoading(true); setError('');
    try {
      if (teklif?.id) await updateTeklif(teklif.id,form);
      else await createTeklif(form);
      onSave();
    } catch(e) {
      setError(e.response?.data?.hata||'Bir hata oluştu.');
    } finally { setLoading(false); }
  };

  const katUrunler = urunler.filter(u=>u.kategori===secKategori);
  const sym = pbSembol(form.paraBirimi);

  /* Ölçü input render */
  const renderOlcu = (olcu) => {
    const isStr = STRING_OLCULAR.has(olcu);
    const isInt = INT_OLCULAR.has(olcu);
    const isBogaz = olcu==='BOGAZ_WH'||olcu==='BOGAZ_CAP';
    const combo = isBogaz ? getBogazSec(olcu) : secUrun?.izinliOlcuDegerleri?.[olcu]||[];
    const val = isStr?(strOlcular[olcu]||''):(olcular[olcu]||'');
    const dolu = val!=='';
    const bc = dolu?'var(--green)':'var(--border)';

    if (isStr&&combo.length>0) return (
      <select className="select" style={{borderColor:bc,fontSize:12,padding:'6px 8px'}} value={val} onChange={e=>setS(olcu,e.target.value)}>
        <option value="">— Seç —</option>
        {combo.map(s=><option key={s} value={s}>{s}</option>)}
      </select>
    );
    return (
      <input className="input" type="number" step={isInt?'1':'any'} min={isInt?'1':'0'}
        placeholder={isInt?'Sayı':'mm'} value={val} style={{borderColor:bc,fontSize:12,padding:'6px 8px'}}
        onChange={e=>isStr?setS(olcu,e.target.value):setO(olcu,e.target.value)} />
    );
  };

  /* Özellik input render */
  const renderOzellik = (tip, secenekler) => {
    const isMulti = secUrun?.multiSelectOzellikler?.includes(tip);
    const isOps   = tip==='AKSESUAR_TIPI';
    const s = ozellikler[tip];
    const dolu = isMulti?(Array.isArray(s)&&s.length>0):(s&&s!=='');
    const bc = isOps?(dolu?'var(--green)':'var(--border)'):(dolu?'var(--green)':'var(--red)');

    if (isMulti) return (
      <div style={{border:`1px solid ${bc}`,borderRadius:6,padding:'6px 8px',background:'var(--surface)'}}>
        {secenekler.map(sv=>(
          <label key={sv} style={{display:'flex',alignItems:'center',gap:6,cursor:'pointer',
            textTransform:'none',letterSpacing:0,fontWeight:400,color:'var(--text)',fontSize:12,padding:'2px 0'}}>
            <input type="checkbox" checked={Array.isArray(s)&&s.includes(sv)} onChange={()=>setOz(tip,sv,true)}/>
            {sv}
          </label>
        ))}
      </div>
    );
    return (
      <select className="select" style={{borderColor:bc,fontSize:12,padding:'6px 8px'}} value={s||''} onChange={e=>setOz(tip,e.target.value,false)}>
        <option value="">— Seç —</option>
        {secenekler.map(sv=><option key={sv} value={sv}>{sv}</option>)}
      </select>
    );
  };

  /* Özellikleri 3 gruba ayır - Swing gibi */
  const ozellikGruplari = secUrun ? (() => {
    const olcuGrup=[]; const teknikGrup=[]; const ralGrup=[];
    for (const [tip,sec] of Object.entries(secUrun.ozellikler)) {
      if (tip==='RAL'||tip==='AKSESUAR_TIPI') ralGrup.push([tip,sec]);
      else if (tip==='CERCEVE_TIPI'||tip==='DAMPER_TIPI'||tip==='MONTAJ'||tip==='MENFEZ_TIPI') teknikGrup.push([tip,sec]);
      else teknikGrup.push([tip,sec]);
    }
    return { olcuGrup, teknikGrup, ralGrup };
  })() : { olcuGrup:[], teknikGrup:[], ralGrup:[] };

  const colStyle = {
    background:'var(--surface2)', border:'1px solid var(--border)',
    borderRadius:8, padding:'10px 12px', display:'flex', flexDirection:'column', gap:8
  };
  const colHeadStyle = {
    fontSize:10, fontWeight:700, letterSpacing:'.08em',
    textTransform:'uppercase', color:'var(--muted)', marginBottom:4
  };

  return (
    <div className="modal-overlay" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div style={{
        background:'var(--surface)', border:'1px solid var(--border)',
        borderRadius:14, width:'98%', maxWidth:1200,
        maxHeight:'96vh', display:'flex', flexDirection:'column',
        animation:'slideUp .2s ease'
      }}>
        {/* HEADER */}
        <div className="modal-header">
          <h2 style={{fontFamily:'var(--font-head)'}}>
            {teklif?`Düzenle — ${teklif.teklifNo}`:'Yeni Teklif'}
          </h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>

        <div style={{flex:1, overflowY:'auto', padding:'16px 20px', display:'flex', flexDirection:'column', gap:14}}>

          {/* ── BÖLÜM 1: Teklif bilgileri ── */}
          <div style={{background:'var(--surface2)',borderRadius:8,padding:'12px 16px'}}>
            <div style={colHeadStyle}>Teklif Bilgileri</div>
            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr 1fr 1fr 0.7fr', gap:10}}>
              <div>
                <label>Teklif No</label>
                <input className="input" value={form.teklifNo} readOnly style={{opacity:.6,fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>İş Adı *</label>
                <input className="input" placeholder="Proje / iş adı" value={form.isAdi}
                  onChange={e=>set('isAdi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>Müşteri</label>
                <select className="select" value={form.musteriId||''} onChange={e=>set('musteriId',e.target.value||null)} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="">— Seç —</option>
                  {musteriler.map(m=><option key={m.id} value={m.id}>{m.firmaAdi}</option>)}
                </select>
              </div>
              <div>
                <label>Teklifi Veren</label>
                <input className="input" placeholder="Ad Soyad" value={form.teklifiVeren||''}
                  onChange={e=>set('teklifiVeren',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>Para Birimi</label>
                <select className="select" value={form.paraBirimi} onChange={e=>set('paraBirimi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="TL">TL ₺</option>
                  <option value="EUR">EUR €</option>
                  <option value="USD">USD $</option>
                </select>
              </div>
              <div>
                <label>KDV %</label>
                <input className="input" type="number" value={form.kdvOrani}
                  onChange={e=>set('kdvOrani',parseFloat(e.target.value)||0)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
            </div>
            <div style={{display:'grid',gridTemplateColumns:'1fr 1fr 1fr 3fr',gap:10,marginTop:8}}>
              <div>
                <label>Teklif Tarihi *</label>
                <input className="input" type="date" value={form.teklifTarihi}
                  onChange={e=>set('teklifTarihi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>Geçerlilik Tarihi</label>
                <input className="input" type="date" value={form.gecerlilikTarihi||''}
                  onChange={e=>set('gecerlilikTarihi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div></div>
              <div>
                <label>Notlar</label>
                <input className="input" placeholder="İsteğe bağlı notlar..." value={form.notlar||''}
                  onChange={e=>set('notlar',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
            </div>
          </div>

          {/* ── BÖLÜM 2: Ürün Seçim (Swing Toolbar gibi) ── */}
          <div style={{background:'var(--surface2)',borderRadius:8,padding:'10px 16px'}}>
            <div style={colHeadStyle}>Ürün Ekle</div>

            {/* Toolbar: Kategori → Ürün → Para Birimi göstergesi */}
            <div style={{display:'flex',gap:10,alignItems:'flex-end',flexWrap:'wrap',marginBottom:10}}>
              <div style={{minWidth:200}}>
                <label>Kategori</label>
                <select className="select" value={secKategori} onChange={e=>katDegisti(e.target.value)} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="">— Kategori Seçiniz —</option>
                  {kategoriler.map(k=><option key={k} value={k}>{KATEGORI_LABEL[k]||k}</option>)}
                </select>
              </div>
              <div style={{minWidth:260}}>
                <label>Ürün</label>
                <select className="select" value={secUrun?.kod||''} onChange={e=>urunSec(urunler.find(u=>u.kod===e.target.value)||null)} disabled={!secKategori} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="">— Ürün Seçiniz —</option>
                  {katUrunler.map(u=><option key={u.kod} value={u.kod}>{u.ad}</option>)}
                </select>
              </div>
              <div style={{display:'flex',alignItems:'center',gap:6,padding:'6px 10px',
                background:'var(--surface)',borderRadius:6,border:'1px solid var(--border)',fontSize:12,color:'var(--muted)'}}>
                Kur: €{(kurlar.EUR||42).toFixed(2)} / ${(kurlar.USD||38).toFixed(2)}
              </div>
            </div>

            {/* 3 Kolon form - Swing gibi: Ölçüler | Teknik | RAL+Aksesuar */}
            {secUrun && (
              <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:10}}>

                {/* KOLON 1: ÖLÇÜLER + MİKTAR */}
                <div style={colStyle}>
                  <div style={colHeadStyle}>Ölçüler <span style={{color:'var(--red)'}}>*</span></div>
                  {secUrun.zorunluOlcular.map(olcu=>(
                    <div key={olcu}>
                      <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                        {OLCU_LABEL[olcu]||olcu} <span style={{color:'var(--red)'}}>*</span>
                      </label>
                      {renderOlcu(olcu)}
                    </div>
                  ))}
                  <div>
                    <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                      Miktar *
                    </label>
                    <input className="input" type="number" min="1" value={miktar}
                      onChange={e=>setMiktar(parseInt(e.target.value)||1)}
                      style={{fontSize:12,padding:'6px 8px',borderColor:miktar>0?'var(--green)':'var(--border)'}}/>
                  </div>
                </div>

                {/* KOLON 2: TEKNİK (Çerçeve, Damper, Montaj, Tip) */}
                <div style={colStyle}>
                  <div style={colHeadStyle}>Teknik Özellikler</div>
                  {ozellikGruplari.teknikGrup.length===0
                    ? <div style={{color:'var(--muted)',fontSize:12}}>Bu ürün için teknik özellik yok.</div>
                    : ozellikGruplari.teknikGrup.map(([tip,sec])=>(
                    <div key={tip}>
                      <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                        {OZELLIK_LABEL[tip]||tip}
                        {tip!=='AKSESUAR_TIPI'&&<span style={{color:'var(--red)',marginLeft:2}}>*</span>}
                      </label>
                      {renderOzellik(tip,sec)}
                    </div>
                  ))}
                </div>

                {/* KOLON 3: RAL + AKSESUAR */}
                <div style={colStyle}>
                  <div style={colHeadStyle}>RAL / Aksesuar</div>
                  {ozellikGruplari.ralGrup.length===0
                    ? <div style={{color:'var(--muted)',fontSize:12}}>Bu ürün için RAL/Aksesuar yok.</div>
                    : ozellikGruplari.ralGrup.map(([tip,sec])=>(
                    <div key={tip}>
                      <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                        {OZELLIK_LABEL[tip]||tip}
                        {tip!=='AKSESUAR_TIPI'&&<span style={{color:'var(--red)',marginLeft:2}}>*</span>}
                        {tip==='AKSESUAR_TIPI'&&<span style={{color:'var(--muted)',marginLeft:3,fontSize:10}}>(opsiyonel)</span>}
                      </label>
                      {renderOzellik(tip,sec)}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Hata + Ekle butonu */}
            {secUrun && (
              <div style={{display:'flex',alignItems:'center',gap:10,marginTop:10}}>
                <button className="btn btn-primary" onClick={urunEkle} disabled={hesaplaniyor}
                  style={{padding:'8px 20px'}}>
                  {hesaplaniyor?'Hesaplanıyor...':'+ Kaleme Ekle'}
                </button>
                {fiyatHata && (
                  <span style={{color:'var(--red)',fontSize:12}}>⚠️ {fiyatHata}</span>
                )}
              </div>
            )}
          </div>

          {/* ── BÖLÜM 3: Kalem Tablosu (Swing TeklifTablePanel gibi) ── */}
          <div>
            <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:6}}>
              <span style={{fontFamily:'var(--font-head)',fontWeight:700,fontSize:14}}>
                Kalemler ({form.kalemler.length})
              </span>
              <button className="btn btn-secondary btn-sm" onClick={addManuel}>+ Manuel Ekle</button>
            </div>

            <div style={{border:'1px solid var(--border)',borderRadius:8,overflow:'hidden'}}>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th style={{width:36}}>#</th>
                      <th>Ürün Adı</th>
                      <th style={{width:60}}>Adet</th>
                      <th style={{width:70}}>Birim</th>
                      <th style={{width:110}}>Birim Fiyat ({sym})</th>
                      <th style={{width:70}}>İsk %</th>
                      <th style={{width:110,textAlign:'right'}}>Toplam ({sym})</th>
                      <th style={{width:32}}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {form.kalemler.length===0 && (
                      <tr><td colSpan={8} style={{textAlign:'center',color:'var(--muted)',padding:20,fontSize:13}}>
                        Henüz kalem yok — yukarıdan ürün seçip ekleyin
                      </td></tr>
                    )}
                    {form.kalemler.map((k,i)=>(
                      <tr key={i}>
                        <td style={{color:'var(--muted)',fontSize:12}}>{i+1}</td>
                        <td>
                          <input className="input" style={{padding:'4px 6px',fontSize:12}} value={k.urunAdi}
                            onChange={e=>kalemSet(i,'urunAdi',e.target.value)}/>
                        </td>
                        <td>
                          <input className="input" type="number" min="1" style={{padding:'4px 6px',fontSize:12}} value={k.adet}
                            onChange={e=>kalemSet(i,'adet',parseInt(e.target.value)||1)}/>
                        </td>
                        <td>
                          <select className="select" style={{padding:'4px 6px',fontSize:12}} value={k.birim}
                            onChange={e=>kalemSet(i,'birim',e.target.value)}>
                            <option>Adet</option><option>m²</option><option>m</option><option>Takım</option>
                          </select>
                        </td>
                        <td>
                          <input className="input" type="number" step="0.01" style={{padding:'4px 6px',fontSize:12}}
                            value={Math.round(tleCevir(k.birimFiyat||0)*100)/100}
                            onChange={e=>{const v=parseFloat(e.target.value)||0;const pb=form.paraBirimi;const tlV=pb==="EUR"?v*(kurlar.EUR||42):pb==="USD"?v*(kurlar.USD||38):v;kalemSet(i,"birimFiyat",tlV);}}/>
                        </td>
                        <td>
                          <input className="input" type="number" min="0" max="100" style={{padding:'4px 6px',fontSize:12}} value={k.iskonto}
                            onChange={e=>kalemSet(i,'iskonto',parseFloat(e.target.value)||0)}/>
                        </td>
                        <td style={{textAlign:'right',fontWeight:500,color:'var(--accent)',whiteSpace:'nowrap',fontSize:13}}>
                          {fmt(kalemToplam(k))}
                        </td>
                        <td>
                          <button onClick={()=>removeKalem(i)} style={{background:'none',border:'none',color:'var(--red)',cursor:'pointer',fontSize:16,padding:'2px 4px'}}>×</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Toplam satırı - tablo içinde sağa hizalı */}
              {form.kalemler.length>0 && (
                <div style={{display:'flex',justifyContent:'flex-end',padding:'10px 16px',
                  borderTop:'1px solid var(--border)',background:'var(--surface2)'}}>
                  <div style={{display:'flex',flexDirection:'column',gap:4,minWidth:220}}>
                    {[['Ara Toplam',araToplam],[`KDV (%${form.kdvOrani})`,kdvTutari],['Genel Toplam',genelToplam]]
                      .map(([lbl,val],i)=>(
                      <div key={lbl} style={{display:'flex',justifyContent:'space-between',
                        borderTop:i===2?'1px solid var(--border)':'none',
                        paddingTop:i===2?6:0,
                        fontWeight:i===2?700:400,
                        color:i===2?'var(--accent)':'var(--text)',fontSize:13}}>
                        <span>{lbl}</span>
                        <span>{fmt(val)} {sym}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          </div>

          {error && <div style={{color:'var(--red)',fontSize:13}}>{error}</div>}
        </div>

        {/* FOOTER */}
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>İptal</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading?'Kaydediliyor...':(teklif?'Güncelle':'Kaydet')}
          </button>
        </div>
      </div>
    </div>
  );
}
