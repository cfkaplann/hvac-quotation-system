import React, { useState, useEffect, useCallback } from 'react';
import axios from 'axios';


const API = axios.create({ baseURL: 'http://localhost:8080/api' });


export default function AdminPage() {
  const [token, setToken]       = useState(localStorage.getItem('auth_token') || '');

  const [urunler, setUrunler]   = useState([]);
  const [secUrun, setSecUrun]   = useState(null);
  const [sekme, setSekme]       = useState('fiyat');
  const [matris, setMatris]     = useState(null);
  const [yukleniyor, setYukleniyor] = useState(false);
  const [editHucre, setEditHucre] = useState(null); // {rowVal, colVal, fiyat}
  const [kayitMesaj, setKayitMesaj] = useState('');
  const [topluOran, setTopluOran] = useState('');
  const [topluOnay, setTopluOnay] = useState(false);

  const headers = { 'X-Token': token };

  // Giriş
  const giris = async () => {
    setGirisHata('');
    try {
      const r = await API.post('/admin/giris', { kullanici, sifre });
      const t = r.data.token;
      setToken(t);
      localStorage.setItem('admin_token', t);
    } catch(e) {
      setGirisHata(e.response?.data?.hata || 'Giriş başarısız.');
    }
  };

  const cikis = () => {
    setToken('');
    setSecUrun(null); setMatris(null);
  };

  // Ürün listesi
  useEffect(() => {
    const t = localStorage.getItem('auth_token');
    if (t) setToken(t);
  }, []);

  useEffect(() => {
    if (!token) return;
    API.get('/admin/urunler', { headers }).then(r => setUrunler(r.data)).catch(() => {});

  }, [token]);

  // Matris yükle
  const matrisYukle = useCallback(async (urun) => {
    setSecUrun(urun); setMatris(null); setYukleniyor(true); setEditHucre(null); setKayitMesaj('');
    try {
      const r = await API.get(`/admin/fiyatlar/${urun.id}`, { headers });
      setMatris(r.data);
    } catch(e) { alert('Matris yüklenemedi.'); }
    finally { setYukleniyor(false); }
  }, [token]);

  // Hücre kaydet
  const hucreSave = async () => {
    if (!editHucre) return;
    const rowStr = typeof editHucre.rowVal === 'string' && isNaN(Number(editHucre.rowVal));
    try {
      await API.put(`/admin/fiyatlar/${secUrun.id}/hucre`, {
        rowVal: editHucre.rowVal,
        colVal: editHucre.colVal,
        fiyat: parseFloat(editHucre.fiyat),
        rowStr, colStr: rowStr
      }, { headers });
      // Matrisi güncelle
      const key = `${editHucre.rowVal}|${editHucre.colVal}`;
      setMatris(m => ({ ...m, hücreler: { ...m.hücreler, [key]: parseFloat(editHucre.fiyat) } }));
      setKayitMesaj('✓ Kaydedildi');
      setTimeout(() => setKayitMesaj(''), 2000);
      setEditHucre(null);
    } catch(e) { alert('Kayıt hatası: ' + (e.response?.data?.hata || e.message)); }
  };

  // Excel'den sıfırla
  const excelSifirla = async () => {
    if (!window.confirm('Tüm fiyatlar silinip Excel\'den yeniden yüklenecek. Emin misiniz?')) return;
    try {
      const r = await API.post('/admin/excel-sifirla', {}, { headers });
      alert(r.data.mesaj);
      setSecUrun(null); setMatris(null);
      API.get('/admin/urunler', { headers }).then(r => setUrunler(r.data));
    } catch(e) { alert('Hata: ' + (e.response?.data?.hata || e.message)); }
  };

  // Toplu güncelle
  const topluGuncelle = async () => {
    if (!topluOran || isNaN(Number(topluOran))) { alert('Geçerli bir oran girin.'); return; }
    try {
      const r = await API.post(`/admin/fiyatlar/${secUrun.id}/toplu-guncelle`,
        { oran: parseFloat(topluOran) }, { headers });
      alert(`${r.data.guncellendi} hücre güncellendi.`);
      setTopluOran(''); setTopluOnay(false);
      matrisYukle(secUrun);
    } catch(e) { alert('Hata: ' + (e.response?.data?.hata || e.message)); }
  };

  if (!token) return <div style={{padding:40,color:'var(--muted)'}}>Lütfen önce giriş yapın.</div>;

  const rows = matris?.rows || [];
  const cols = matris?.cols || [];
  const hücreler = matris?.hücreler || {};

  // ── Admin paneli ──────────────────────────────────────
  return (
    <div style={{ padding:28, flex:1 }}>
      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginBottom:24 }}>
        <h1 style={{ fontFamily:'var(--font-head)', fontSize:24, fontWeight:800 }}>Fiyat Yönetimi</h1>
        <div style={{display:'flex',gap:8}}>
          <div style={{display:'flex',gap:6,marginRight:'auto'}}>
          <button className="btn btn-sm"
            style={{background: sekme==='fiyat'?'var(--accent)':'var(--surface2)',
                    color: sekme==='fiyat'?'#000':'var(--text)', border:'1px solid var(--border)'}}
            onClick={()=>setSekme('fiyat')}>Fiyat Matrisi</button>


        </div>
        <button className="btn btn-secondary btn-sm" onClick={excelSifirla}
            style={{fontSize:12,color:'var(--red)',borderColor:'var(--red)'}}>
            ⚠️ Excel'den Sıfırla
          </button>
          <button className="btn btn-secondary" onClick={cikis}>Çıkış</button>
        </div>
      </div>

      {sekme === 'fiyat' && <div style={{ display:'grid', gridTemplateColumns:'260px 1fr', gap:20 }}>

        {/* Sol: ürün listesi */}
        <div style={{ background:'var(--surface2)', border:'1px solid var(--border)', borderRadius:10, padding:12 }}>
          <div style={{ fontSize:10, fontWeight:700, letterSpacing:'.08em', textTransform:'uppercase',
            color:'var(--muted)', marginBottom:10 }}>Ürünler ({urunler.length})</div>
          <div style={{ maxHeight:'70vh', overflowY:'auto' }}>
            {urunler.map(u => (
              <div key={u.id} onClick={() => matrisYukle(u)}
                style={{ padding:'8px 10px', borderRadius:6, cursor:'pointer', fontSize:12,
                  marginBottom:3,
                  background: secUrun?.id===u.id ? 'var(--accent)20' : 'transparent',
                  color: secUrun?.id===u.id ? 'var(--accent)' : 'var(--text)',
                  border: secUrun?.id===u.id ? '1px solid var(--accent)40' : '1px solid transparent',
                }}>
                <div style={{ fontWeight:600 }}>{u.sheetName}</div>
                <div style={{ fontSize:10, color:'var(--muted)', marginTop:2 }}>{u.strategy}</div>
              </div>
            ))}
          </div>
        </div>

        {/* Sağ: matris */}
        <div>
          {!secUrun && (
            <div style={{ display:'flex', alignItems:'center', justifyContent:'center',
              height:300, color:'var(--muted)', fontSize:14 }}>
              ← Soldaki listeden bir ürün seçin
            </div>
          )}

          {yukleniyor && (
            <div style={{ display:'flex', alignItems:'center', justifyContent:'center', height:300, color:'var(--muted)' }}>
              Yükleniyor...
            </div>
          )}

          {secUrun && matris && !yukleniyor && (
            <>
              {/* Başlık + Toplu güncelle */}
              <div style={{ display:'flex', alignItems:'center', gap:12, marginBottom:16, flexWrap:'wrap' }}>
                <span style={{ fontFamily:'var(--font-head)', fontWeight:700, fontSize:16 }}>
                  {secUrun.sheetName}
                </span>
                <span style={{ fontSize:11, color:'var(--muted)', background:'var(--surface2)',
                  padding:'2px 8px', borderRadius:4 }}>{secUrun.strategy}</span>
                {kayitMesaj && (
                  <span style={{ color:'var(--green)', fontSize:13 }}>{kayitMesaj}</span>
                )}
                <div style={{ marginLeft:'auto', display:'flex', gap:8, alignItems:'center' }}>
                  {!topluOnay ? (
                    <button className="btn btn-secondary btn-sm" onClick={() => setTopluOnay(true)}>
                      % Toplu Güncelle
                    </button>
                  ) : (
                    <>
                      <input className="input" style={{ width:80, padding:'5px 8px', fontSize:12 }}
                        placeholder="örn: 10" value={topluOran} onChange={e => setTopluOran(e.target.value)}
                        autoFocus/>
                      <span style={{ fontSize:12, color:'var(--muted)' }}>% artış/azalış</span>
                      <button className="btn btn-primary btn-sm" onClick={topluGuncelle}>Uygula</button>
                      <button className="btn btn-secondary btn-sm" onClick={() => { setTopluOnay(false); setTopluOran(''); }}>İptal</button>
                    </>
                  )}
                </div>
              </div>

              {/* Hücre düzenleme popup */}
              {editHucre && (
                <div style={{ background:'var(--surface2)', border:'1px solid var(--accent)40',
                  borderRadius:8, padding:'12px 16px', marginBottom:12,
                  display:'flex', alignItems:'center', gap:10 }}>
                  <span style={{ fontSize:12, color:'var(--muted)' }}>
                    {editHucre.rowVal} × {editHucre.colVal} →
                  </span>
                  <input className="input" type="number" step="0.01" autoFocus
                    style={{ width:120, padding:'6px 8px', fontSize:13 }}
                    value={editHucre.fiyat}
                    onChange={e => setEditHucre(h => ({ ...h, fiyat: e.target.value }))}
                    onKeyDown={e => { if (e.key==='Enter') hucreSave(); if (e.key==='Escape') setEditHucre(null); }}/>
                  <span style={{ fontSize:11, color:'var(--muted)' }}>₺</span>
                  <button className="btn btn-primary btn-sm" onClick={hucreSave}>Kaydet</button>
                  <button className="btn btn-secondary btn-sm" onClick={() => setEditHucre(null)}>İptal</button>
                </div>
              )}

              {/* Matris tablosu */}
              <div style={{ overflowX:'auto', overflowY:'auto', maxHeight:'65vh',
                border:'1px solid var(--border)', borderRadius:8 }}>
                <table style={{ borderCollapse:'collapse', fontSize:11 }}>
                  <thead>
                    <tr>
                      <th style={{ background:'var(--surface2)', padding:'8px 10px',
                        border:'1px solid var(--border)', position:'sticky', top:0, left:0, zIndex:3,
                        color:'var(--muted)', fontSize:10 }}>H \ W</th>
                      {cols.map(c => (
                        <th key={c} style={{ background:'var(--surface2)', padding:'6px 8px',
                          border:'1px solid var(--border)', position:'sticky', top:0, zIndex:2,
                          fontWeight:600, color:'var(--accent)', whiteSpace:'nowrap', minWidth:70 }}>
                          {c}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {rows.map(row => (
                      <tr key={row}>
                        <td style={{ background:'var(--surface2)', padding:'6px 10px',
                          border:'1px solid var(--border)', position:'sticky', left:0, zIndex:1,
                          fontWeight:600, color:'var(--accent)', whiteSpace:'nowrap' }}>
                          {row}
                        </td>
                        {cols.map(col => {
                          const norm = v => { const s=String(v); return s.endsWith('.0')?s.slice(0,-2):s; };
                          const key = `${norm(row)}|${norm(col)}`;
                          const fiyat = hücreler[key];
                          const aktif = editHucre?.rowVal===row && editHucre?.colVal===col;
                          return (
                            <td key={col}
                              onClick={() => setEditHucre({ rowVal:row, colVal:col, fiyat: fiyat??0 })}
                              style={{ padding:'5px 8px', border:'1px solid var(--border)',
                                textAlign:'right', cursor:'pointer', whiteSpace:'nowrap',
                                background: aktif ? 'var(--accent)25' :
                                  fiyat==null ? 'var(--red)15' : 'transparent',
                                color: aktif ? 'var(--accent)' : 'var(--text)',
                                transition:'background .1s',
                              }}
                              onMouseEnter={e => { if (!aktif) e.currentTarget.style.background='var(--surface2)'; }}
                              onMouseLeave={e => { if (!aktif) e.currentTarget.style.background = fiyat==null?'var(--red)15':'transparent'; }}
                            >
                              {fiyat != null ? fiyat.toLocaleString('tr-TR', { minimumFractionDigits:2 }) : '—'}
                            </td>
                          );
                        })}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
              <div style={{ fontSize:11, color:'var(--muted)', marginTop:8 }}>
                {rows.length} satır × {cols.length} sütun = {rows.length * cols.length} hücre · Hücreye tıklayarak düzenle
              </div>
            </>
          )}
        </div>
      </div>}
    </div>
  );
}
