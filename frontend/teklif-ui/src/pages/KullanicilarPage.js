import React, { useState, useEffect } from 'react';
import { getKullanicilar, addKullanici, updateKullanici, deleteKullanici } from '../services/api';

const EMPTY = { kullaniciAdi:'', sifre:'', adSoyad:'', rol:'KULLANICI', aktif:true };

export default function KullanicilarPage() {
  const [liste, setListe]       = useState([]);
  const [form, setForm]         = useState(EMPTY);
  const [editId, setEditId]     = useState(null);
  const [hata, setHata]         = useState('');
  const [basari, setBasari]     = useState('');

  const load = () => getKullanicilar().then(r=>setListe(r.data)).catch(()=>{});
  useEffect(() => { load(); }, []);

  const set = (k,v) => setForm(f=>({...f,[k]:v}));

  const kaydet = async () => {
    setHata(''); setBasari('');
    try {
      if (editId) {
        await updateKullanici(editId, form);
        setBasari('Kullanıcı güncellendi.');
      } else {
        await addKullanici(form);
        setBasari('Kullanıcı eklendi.');
      }
      setForm(EMPTY); setEditId(null); load();
    } catch(e) { setHata(e.response?.data?.hata || 'Hata oluştu.'); }
  };

  const duzenle = (k) => {
    setForm({ kullaniciAdi:k.kullaniciAdi, sifre:'', adSoyad:k.adSoyad||'', rol:k.rol, aktif:k.aktif });
    setEditId(k.id); setHata(''); setBasari('');
  };

  const sil = async (id) => {
    if (!window.confirm('Bu kullanıcıyı silmek istediğinize emin misiniz?')) return;
    await deleteKullanici(id); load();
  };

  const iptal = () => { setForm(EMPTY); setEditId(null); setHata(''); setBasari(''); };

  return (
    <div style={{ padding:28, flex:1 }}>
      <h1 style={{ fontFamily:'var(--font-head)', fontSize:24, fontWeight:800, marginBottom:24 }}>
        Kullanıcı Yönetimi
      </h1>

      <div style={{ display:'grid', gridTemplateColumns:'380px 1fr', gap:24 }}>

        {/* Form */}
        <div style={{ background:'var(--surface2)', border:'1px solid var(--border)', borderRadius:10, padding:20 }}>
          <div style={{ fontSize:13, fontWeight:700, marginBottom:16, color:'var(--accent)' }}>
            {editId ? '✏️ Kullanıcı Düzenle' : '+ Yeni Kullanıcı'}
          </div>

          <div style={{ marginBottom:10 }}>
            <label>Kullanıcı Adı *</label>
            <input className="input" value={form.kullaniciAdi} style={{fontSize:13}}
              onChange={e=>set('kullaniciAdi',e.target.value)} disabled={!!editId}
              placeholder="ornek_kullanici"/>
          </div>
          <div style={{ marginBottom:10 }}>
            <label>Ad Soyad</label>
            <input className="input" value={form.adSoyad} style={{fontSize:13}}
              onChange={e=>set('adSoyad',e.target.value)} placeholder="Ad Soyad"/>
          </div>
          <div style={{ marginBottom:10 }}>
            <label>Şifre {editId && <span style={{color:'var(--muted)',fontSize:11}}>(boş bırakılırsa değişmez)</span>}</label>
            <input className="input" type="password" value={form.sifre} style={{fontSize:13}}
              onChange={e=>set('sifre',e.target.value)} placeholder="••••••••"/>
          </div>
          <div style={{ marginBottom:10 }}>
            <label>Rol</label>
            <select className="select" value={form.rol} onChange={e=>set('rol',e.target.value)} style={{fontSize:13}}>
              <option value="KULLANICI">Kullanıcı</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
          {editId && (
            <div style={{ marginBottom:14, display:'flex', alignItems:'center', gap:8 }}>
              <label style={{ margin:0 }}>
                <input type="checkbox" checked={form.aktif} onChange={e=>set('aktif',e.target.checked)}
                  style={{marginRight:6}}/>
                Aktif
              </label>
            </div>
          )}

          {hata   && <div style={{color:'var(--red)',  fontSize:12,marginBottom:10}}>{hata}</div>}
          {basari && <div style={{color:'var(--green)',fontSize:12,marginBottom:10}}>{basari}</div>}

          <div style={{ display:'flex', gap:8 }}>
            <button className="btn btn-primary" onClick={kaydet} style={{flex:1}}>
              {editId ? 'Güncelle' : 'Ekle'}
            </button>
            {editId && <button className="btn btn-secondary" onClick={iptal}>İptal</button>}
          </div>
        </div>

        {/* Liste */}
        <div style={{ border:'1px solid var(--border)', borderRadius:10, overflow:'hidden' }}>
          <table style={{ width:'100%', borderCollapse:'collapse' }}>
            <thead>
              <tr style={{ background:'var(--surface2)' }}>
                <th style={{ padding:'10px 14px', textAlign:'left', fontSize:12 }}>Kullanıcı Adı</th>
                <th style={{ padding:'10px 14px', textAlign:'left', fontSize:12 }}>Ad Soyad</th>
                <th style={{ padding:'10px 14px', textAlign:'left', fontSize:12 }}>Rol</th>
                <th style={{ padding:'10px 14px', textAlign:'left', fontSize:12 }}>Durum</th>
                <th style={{ padding:'10px 14px', textAlign:'left', fontSize:12 }}>Oluşturma</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {liste.map(k => (
                <tr key={k.id} style={{ borderTop:'1px solid var(--border)' }}>
                  <td style={{ padding:'10px 14px', fontSize:13, fontWeight:600 }}>{k.kullaniciAdi}</td>
                  <td style={{ padding:'10px 14px', fontSize:13 }}>{k.adSoyad || '—'}</td>
                  <td style={{ padding:'10px 14px' }}>
                    <span style={{ fontSize:11, padding:'2px 8px', borderRadius:4,
                      background: k.rol==='ADMIN' ? 'var(--accent)20' : 'var(--surface2)',
                      color: k.rol==='ADMIN' ? 'var(--accent)' : 'var(--muted)' }}>
                      {k.rol}
                    </span>
                  </td>
                  <td style={{ padding:'10px 14px' }}>
                    <span style={{ fontSize:11, padding:'2px 8px', borderRadius:4,
                      background: k.aktif ? 'var(--green)20' : 'var(--red)20',
                      color: k.aktif ? 'var(--green)' : 'var(--red)' }}>
                      {k.aktif ? 'Aktif' : 'Pasif'}
                    </span>
                  </td>
                  <td style={{ padding:'10px 14px', fontSize:12, color:'var(--muted)' }}>
                    {k.olusturmaTarihi?.slice(0,10)}
                  </td>
                  <td style={{ padding:'10px 14px' }}>
                    <div style={{ display:'flex', gap:6 }}>
                      <button className="btn btn-secondary btn-sm" onClick={()=>duzenle(k)}>Düzenle</button>
                      <button className="btn btn-danger btn-sm" onClick={()=>sil(k.id)}>Sil</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
