import React, { useState, useEffect } from 'react';
import { getSiparisler, siparisDurumGuncelle, siparisNotGuncelle, getSiparisGecmis, siparisTerminGuncelle } from '../services/api';

const DURUMLAR = [
  { key:'URETIM_BEKLIYOR', label:'Bekliyor',       renk:'#f59e0b', bg:'#f59e0b20', sira:0 },
  { key:'URETIM_ALINDI',   label:'Üretime Alındı', renk:'#3b82f6', bg:'#3b82f620', sira:1 },
  { key:'URETIMDE',        label:'Üretimde',        renk:'#8b5cf6', bg:'#8b5cf620', sira:2 },
  { key:'HAZIR',           label:'Hazır',           renk:'#10b981', bg:'#10b98120', sira:3 },
  { key:'SEVK_EDILDI',     label:'Sevk Edildi',     renk:'#f97316', bg:'#f9731620', sira:4 },
  { key:'TESLIM_EDILDI',   label:'Teslim Edildi',   renk:'#10b981', bg:'#10b98120', sira:5 },
  { key:'IPTAL',           label:'İptal',           renk:'#ef4444', bg:'#ef444420', sira:6 },
];

const durumBul = (key) => DURUMLAR.find(d => d.key === key) || DURUMLAR[0];

const SONRAKI = {
  URETIM_BEKLIYOR: 'URETIM_ALINDI',
  URETIM_ALINDI:   'URETIMDE',
  URETIMDE:        'HAZIR',
  HAZIR:           'SEVK_EDILDI',
  SEVK_EDILDI:     'TESLIM_EDILDI',
};

export default function UretimTakipPage() {
  const [siparisler, setSiparisler] = useState([]);
  const [filtre, setFiltre]         = useState('AKTIF'); // AKTIF | TESLIM | HEPSI
  const [arama, setArama]           = useState('');
  const [secili, setSecili]         = useState(null);
  const [gecmis, setGecmis]         = useState([]);
  const [aciklamaModal, setAciklamaModal] = useState(null); // {id, yeniDurum}
  const [aciklama, setAciklama]     = useState('');
  const [notDuzenle, setNotDuzenle] = useState(false);
  const [terminDuzenle, setTerminDuzenle] = useState(false);
  const [terminText, setTerminText] = useState('');
  const [notText, setNotText]       = useState('');
  const [yukleniyor, setYukleniyor] = useState(false);

  useEffect(() => { yukle(); }, []);

  const yukle = async () => {
    setYukleniyor(true);
    try {
      const r = await getSiparisler();
      setSiparisler(r.data);
    } catch(e) {} finally { setYukleniyor(false); }
  };

  const siparisAc = async (s) => {
    setSecili(s);
    setNotText(s.notlar || '');
    setNotDuzenle(false);
    setTerminText(s.terminTarihi || '');
    setTerminDuzenle(false);
    try {
      const r = await getSiparisGecmis(s.id);
      setGecmis(r.data);
    } catch(e) {}
  };

  const durumIlerlet = (s) => {
    const sonraki = SONRAKI[s.uretimDurumu];
    if (!sonraki) return;
    setAciklamaModal({ id: s.id, yeniDurum: sonraki });
    setAciklama('');
  };

  const durumDegistir = async (id, yeniDurum, belirliAciklama) => {
    try {
      await siparisDurumGuncelle(id, { durum: yeniDurum, aciklama: belirliAciklama || aciklama });
      await yukle();
      if (secili?.id === id) {
        const guncel = (await getSiparisler()).data.find(s => s.id === id);
        if (guncel) { setSecili(guncel); setNotText(guncel.notlar || ''); }
        const r = await getSiparisGecmis(id);
        setGecmis(r.data);
      }
      setAciklamaModal(null); setAciklama('');
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const terminKaydet = async () => {
    try {
      await siparisTerminGuncelle(secili.id, { terminTarihi: terminText });
      setTerminDuzenle(false);
      setSecili(prev => ({...prev, terminTarihi: terminText}));
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const notKaydet = async () => {
    try {
      await siparisNotGuncelle(secili.id, { notlar: notText });
      setNotDuzenle(false);
      setSecili(prev => ({...prev, notlar: notText}));
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const filtrelenmisSiparisler = siparisler
    .filter(s => {
      if (filtre === 'AKTIF') return !['TESLIM_EDILDI','IPTAL'].includes(s.uretimDurumu);
      if (filtre === 'TESLIM') return s.uretimDurumu === 'TESLIM_EDILDI';
      return true;
    })
    .filter(s => {
      if (!arama) return true;
      const a = arama.toLowerCase();
      return (s.teklifNo||'').toLowerCase().includes(a) ||
             (s.musteriAdi||'').toLowerCase().includes(a) ||
             (s.isAdi||'').toLowerCase().includes(a);
    });

  // Özet sayılar
  const ozet = {};
  DURUMLAR.forEach(d => { ozet[d.key] = siparisler.filter(s => s.uretimDurumu === d.key).length; });

  const fmt = (v, sym) => v ? `${parseFloat(v).toLocaleString('tr-TR',{minimumFractionDigits:2})} ${sym||''}` : '—';

  return (
    <div style={{ display:'flex', flex:1, height:'100vh', overflow:'hidden' }}>

      {/* SOL: Liste */}
      <div style={{ width:420, flexShrink:0, borderRight:'1px solid var(--border)',
        display:'flex', flexDirection:'column', height:'100vh' }}>

        {/* Başlık + filtreler */}
        <div style={{ padding:'20px 16px 12px', borderBottom:'1px solid var(--border)' }}>
          <div style={{ fontFamily:'var(--font-head)', fontSize:20, fontWeight:800, marginBottom:12 }}>
            🏭 Üretim Takip
          </div>
          {/* Özet badges */}
          <div style={{ display:'flex', gap:6, flexWrap:'wrap', marginBottom:10 }}>
            {DURUMLAR.filter(d=>d.key!=='IPTAL').map(d => ozet[d.key] > 0 && (
              <span key={d.key} style={{ fontSize:11, padding:'2px 8px', borderRadius:10,
                background:d.bg, color:d.renk, fontWeight:600 }}>
                {d.label}: {ozet[d.key]}
              </span>
            ))}
          </div>
          {/* Filtre tabları */}
          <div style={{ display:'flex', gap:6, marginBottom:8 }}>
            {[['AKTIF','Aktif'],['TESLIM','Teslim'],['HEPSI','Hepsi']].map(([k,l]) => (
              <button key={k} onClick={() => setFiltre(k)}
                style={{ fontSize:12, padding:'4px 12px', borderRadius:6, border:'none',
                  cursor:'pointer',
                  background: filtre===k ? 'var(--accent)' : 'var(--surface2)',
                  color: filtre===k ? '#000' : 'var(--muted)',
                  fontWeight: filtre===k ? 600 : 400 }}>
                {l}
              </button>
            ))}
          </div>
          <input className="input" placeholder="Ara... (teklif no, müşteri, iş adı)"
            style={{ fontSize:12, padding:'6px 10px' }}
            value={arama} onChange={e => setArama(e.target.value)}/>
        </div>

        {/* Sipariş kartları */}
        <div style={{ flex:1, overflowY:'auto', padding:'8px' }}>
          {yukleniyor && <div style={{ textAlign:'center', color:'var(--muted)', padding:20 }}>Yükleniyor...</div>}
          {!yukleniyor && filtrelenmisSiparisler.length === 0 && (
            <div style={{ textAlign:'center', color:'var(--muted)', padding:30, fontSize:13 }}>
              {filtre === 'AKTIF' ? 'Aktif sipariş yok.\nOnaylanan teklifler burada görünür.' : 'Kayıt bulunamadı.'}
            </div>
          )}
          {filtrelenmisSiparisler.map(s => {
            const d = durumBul(s.uretimDurumu);
            const sonraki = SONRAKI[s.uretimDurumu];
            const sonrakiD = sonraki ? durumBul(sonraki) : null;
            return (
              <div key={s.id} onClick={() => siparisAc(s)}
                style={{ padding:'12px 14px', borderRadius:10, marginBottom:6, cursor:'pointer',
                  background: secili?.id===s.id ? 'var(--surface2)' : 'var(--surface)',
                  border: `1px solid ${secili?.id===s.id ? 'var(--accent)' : 'var(--border)'}`,
                  transition:'all .15s' }}>
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start' }}>
                  <div>
                    <div style={{ fontWeight:600, fontSize:13 }}>{s.teklifNo}</div>
                    <div style={{ fontSize:12, color:'var(--muted)', marginTop:1 }}>{s.musteriAdi}</div>
                    <div style={{ fontSize:12, color:'var(--muted)' }}>{s.isAdi}</div>
                    {s.terminTarihi && (
                      <div style={{ fontSize:11, marginTop:3,
                        color: new Date(s.terminTarihi) < new Date() ? 'var(--red)' : 'var(--accent)',
                        fontWeight:600 }}>
                        ⏰ {new Date(s.terminTarihi).toLocaleDateString('tr-TR',{day:'2-digit',month:'short',year:'numeric'})}
                      </div>
                    )}
                  </div>
                  <div style={{ textAlign:'right' }}>
                    <span style={{ fontSize:11, padding:'3px 8px', borderRadius:8,
                      background:d.bg, color:d.renk, fontWeight:600, display:'block', marginBottom:4 }}>
                      {d.label}
                    </span>
                    <div style={{ fontSize:11, color:'var(--muted)' }}>
                      {fmt(s.genelToplam, s.paraBirimi)}
                    </div>
                  </div>
                </div>
                {/* İleri butonu */}
                {sonrakiD && (
                  <button onClick={e => { e.stopPropagation(); durumIlerlet(s); }}
                    style={{ marginTop:8, width:'100%', padding:'5px', fontSize:11,
                      background:sonrakiD.bg, color:sonrakiD.renk,
                      border:`1px solid ${sonrakiD.renk}40`, borderRadius:6, cursor:'pointer',
                      fontWeight:600 }}>
                    → {sonrakiD.label}
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* SAĞ: Detay */}
      <div style={{ flex:1, overflowY:'auto', padding:24 }}>
        {!secili ? (
          <div style={{ display:'flex', alignItems:'center', justifyContent:'center',
            height:'100%', color:'var(--muted)', fontSize:14 }}>
            ← Soldan bir sipariş seçin
          </div>
        ) : (
          <div style={{ maxWidth:700 }}>
            {/* Başlık */}
            <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:20 }}>
              <div>
                <h2 style={{ fontFamily:'var(--font-head)', fontSize:20, fontWeight:800, marginBottom:4 }}>
                  {secili.teklifNo}
                </h2>
                <div style={{ fontSize:13, color:'var(--muted)' }}>
                  {secili.musteriAdi} · {secili.isAdi}
                </div>
                <div style={{ fontSize:12, color:'var(--muted)', marginTop:2 }}>
                  Sipariş: {secili.siparisTarihi?.slice(0,10)} · Son güncelleme: {secili.sonGuncelleme?.slice(0,10)}
                </div>
              </div>
              <div style={{ fontSize:15, fontWeight:700, color:'var(--accent)' }}>
                {fmt(secili.genelToplam, secili.paraBirimi)}
              </div>
            </div>

            {/* Durum akışı */}
            <div style={{ background:'var(--surface2)', borderRadius:10, padding:16, marginBottom:16 }}>
              <div style={{ fontSize:11, fontWeight:700, letterSpacing:'.08em',
                textTransform:'uppercase', color:'var(--muted)', marginBottom:12 }}>Durum Akışı</div>
              <div style={{ display:'flex', alignItems:'center', gap:0, overflowX:'auto' }}>
                {DURUMLAR.filter(d=>d.key!=='IPTAL').map((d, i) => {
                  const aktif = secili.uretimDurumu === d.key;
                  const gecti = DURUMLAR.findIndex(x=>x.key===secili.uretimDurumu) > i;
                  return (
                    <React.Fragment key={d.key}>
                      <div onClick={() => {
                          if (!aktif) {
                            setAciklamaModal({ id: secili.id, yeniDurum: d.key });
                            setAciklama('');
                          }
                        }}
                        style={{ display:'flex', flexDirection:'column', alignItems:'center',
                          cursor: aktif ? 'default' : 'pointer', minWidth:80 }}>
                        <div style={{ width:32, height:32, borderRadius:'50%', display:'flex',
                          alignItems:'center', justifyContent:'center', fontSize:14,
                          background: aktif ? d.renk : gecti ? d.renk+'80' : 'var(--surface)',
                          border: `2px solid ${aktif ? d.renk : gecti ? d.renk+'60' : 'var(--border)'}`,
                          color: aktif||gecti ? '#fff' : 'var(--muted)',
                          fontWeight:700, transition:'all .2s' }}>
                          {gecti ? '✓' : i+1}
                        </div>
                        <div style={{ fontSize:10, marginTop:4, textAlign:'center',
                          color: aktif ? d.renk : gecti ? 'var(--text)' : 'var(--muted)',
                          fontWeight: aktif ? 700 : 400, maxWidth:72 }}>
                          {d.label}
                        </div>
                      </div>
                      {i < DURUMLAR.filter(d=>d.key!=='IPTAL').length - 1 && (
                        <div style={{ flex:1, height:2, minWidth:12,
                          background: gecti ? d.renk+'60' : 'var(--border)',
                          transition:'all .2s' }}/>
                      )}
                    </React.Fragment>
                  );
                })}
              </div>
            </div>

            {/* Hızlı aksiyon butonları */}
            {SONRAKI[secili.uretimDurumu] && (
              <div style={{ display:'flex', gap:8, marginBottom:16 }}>
                {(() => {
                  const sonraki = SONRAKI[secili.uretimDurumu];
                  const d = durumBul(sonraki);
                  return (
                    <button className="btn btn-primary"
                      onClick={() => { setAciklamaModal({id:secili.id, yeniDurum:sonraki}); setAciklama(''); }}
                      style={{ flex:1, background:d.renk, borderColor:d.renk, color:'#fff' }}>
                      → {d.label}
                    </button>
                  );
                })()}
                <button className="btn btn-secondary" style={{ color:'var(--red)', borderColor:'var(--red)' }}
                  onClick={() => { setAciklamaModal({id:secili.id, yeniDurum:'IPTAL'}); setAciklama(''); }}>
                  İptal
                </button>
              </div>
            )}

            {/* Termin Tarihi */}
            <div style={{ background:'var(--surface2)', borderRadius:10, padding:16, marginBottom:16 }}>
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
                <div style={{ fontSize:11, fontWeight:700, letterSpacing:'.08em',
                  textTransform:'uppercase', color:'var(--muted)' }}>Termin Tarihi</div>
                {!terminDuzenle
                  ? <button className="btn btn-secondary btn-sm" style={{ fontSize:11 }}
                      onClick={() => setTerminDuzenle(true)}>Düzenle</button>
                  : <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-primary btn-sm" style={{ fontSize:11 }} onClick={terminKaydet}>Kaydet</button>
                      <button className="btn btn-secondary btn-sm" style={{ fontSize:11 }}
                        onClick={() => { setTerminDuzenle(false); setTerminText(secili.terminTarihi || ''); }}>İptal</button>
                    </div>
                }
              </div>
              {terminDuzenle
                ? <input type="date" className="input"
                    style={{ fontSize:13, padding:'6px 8px' }}
                    value={terminText}
                    onChange={e => setTerminText(e.target.value)}/>
                : <div style={{ fontSize:14, fontWeight:600,
                    color: secili.terminTarihi ? 'var(--accent)' : 'var(--muted)' }}>
                    {secili.terminTarihi
                      ? new Date(secili.terminTarihi).toLocaleDateString('tr-TR', {day:'2-digit',month:'long',year:'numeric'})
                      : 'Termin tarihi belirlenmedi.'}
                  </div>
              }
            </div>

            {/* Notlar */}
            <div style={{ background:'var(--surface2)', borderRadius:10, padding:16, marginBottom:16 }}>
              <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:8 }}>
                <div style={{ fontSize:11, fontWeight:700, letterSpacing:'.08em',
                  textTransform:'uppercase', color:'var(--muted)' }}>Notlar</div>
                {!notDuzenle
                  ? <button className="btn btn-secondary btn-sm" style={{ fontSize:11 }}
                      onClick={() => setNotDuzenle(true)}>Düzenle</button>
                  : <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-primary btn-sm" style={{ fontSize:11 }} onClick={notKaydet}>Kaydet</button>
                      <button className="btn btn-secondary btn-sm" style={{ fontSize:11 }} onClick={() => { setNotDuzenle(false); setNotText(secili.notlar||''); }}>İptal</button>
                    </div>
                }
              </div>
              {notDuzenle
                ? <textarea value={notText} onChange={e=>setNotText(e.target.value)}
                    rows={3} className="input"
                    style={{ width:'100%', resize:'vertical', fontSize:12, padding:'6px 8px' }}/>
                : <div style={{ fontSize:13, color: secili.notlar ? 'var(--text)' : 'var(--muted)',
                    whiteSpace:'pre-wrap', minHeight:40 }}>
                    {secili.notlar || 'Not eklenmedi.'}
                  </div>
              }
            </div>

            {/* Geçmiş */}
            <div style={{ background:'var(--surface2)', borderRadius:10, padding:16 }}>
              <div style={{ fontSize:11, fontWeight:700, letterSpacing:'.08em',
                textTransform:'uppercase', color:'var(--muted)', marginBottom:12 }}>Durum Geçmişi</div>
              {gecmis.length === 0
                ? <div style={{ color:'var(--muted)', fontSize:13 }}>Henüz geçmiş yok.</div>
                : gecmis.map((g, i) => {
                    const d = durumBul(g.durum);
                    return (
                      <div key={i} style={{ display:'flex', gap:10, marginBottom:10,
                        paddingBottom:10, borderBottom: i<gecmis.length-1?'1px solid var(--border)':'none' }}>
                        <div style={{ width:8, height:8, borderRadius:'50%', marginTop:5,
                          background:d.renk, flexShrink:0 }}/>
                        <div style={{ flex:1 }}>
                          <div style={{ display:'flex', justifyContent:'space-between' }}>
                            <span style={{ fontSize:13, fontWeight:600, color:d.renk }}>{d.label}</span>
                            <span style={{ fontSize:11, color:'var(--muted)' }}>
                              {g.tarih?.slice(0,16)} {g.yapan ? `· ${g.yapan}` : ''}
                            </span>
                          </div>
                          {g.aciklama && <div style={{ fontSize:12, color:'var(--muted)', marginTop:2 }}>{g.aciklama}</div>}
                        </div>
                      </div>
                    );
                  })
              }
            </div>
          </div>
        )}
      </div>

      {/* Açıklama Modal */}
      {aciklamaModal && (
        <div style={{ position:'fixed', inset:0, background:'rgba(0,0,0,.5)',
          display:'flex', alignItems:'center', justifyContent:'center', zIndex:1000 }}
          onClick={() => setAciklamaModal(null)}>
          <div style={{ background:'var(--surface)', borderRadius:14, padding:28, width:400,
            border:'1px solid var(--border)' }}
            onClick={e => e.stopPropagation()}>
            {(() => {
              const d = durumBul(aciklamaModal.yeniDurum);
              return (
                <>
                  <h3 style={{ fontFamily:'var(--font-head)', marginBottom:16 }}>
                    Durum: <span style={{ color:d.renk }}>{d.label}</span>
                  </h3>
                  <label style={{ fontSize:12, color:'var(--muted)', display:'block', marginBottom:6 }}>
                    Açıklama (opsiyonel)
                  </label>
                  <textarea className="input" rows={3} value={aciklama}
                    onChange={e=>setAciklama(e.target.value)}
                    placeholder="Notunuzu buraya yazın..."
                    style={{ width:'100%', resize:'vertical', fontSize:13, marginBottom:16 }}/>
                  <div style={{ display:'flex', gap:8 }}>
                    <button className="btn btn-primary" style={{ flex:1, background:d.renk, borderColor:d.renk }}
                      onClick={() => durumDegistir(aciklamaModal.id, aciklamaModal.yeniDurum)}>
                      Onayla
                    </button>
                    <button className="btn btn-secondary" onClick={() => setAciklamaModal(null)}>İptal</button>
                  </div>
                </>
              );
            })()}
          </div>
        </div>
      )}
    </div>
  );
}
