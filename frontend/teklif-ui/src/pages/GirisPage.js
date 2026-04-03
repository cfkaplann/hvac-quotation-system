import React, { useState } from 'react';
import { girisYap } from '../services/api';

export default function GirisPage({ onGiris }) {
  const [kullaniciAdi, setKullaniciAdi] = useState('');
  const [sifre, setSifre]               = useState('');
  const [hata, setHata]                 = useState('');
  const [yukleniyor, setYukleniyor]     = useState(false);

  const giris = async () => {
    if (!kullaniciAdi.trim() || !sifre.trim()) { setHata('Kullanıcı adı ve şifre zorunludur.'); return; }
    setYukleniyor(true); setHata('');
    try {
      const r = await girisYap({ kullaniciAdi, sifre });
      localStorage.setItem('auth_token', r.data.token);
      localStorage.setItem('auth_kullanici', JSON.stringify(r.data));
      onGiris(r.data);
    } catch(e) {
      setHata(e.response?.data?.hata || 'Giriş başarısız.');
    } finally { setYukleniyor(false); }
  };

  return (
    <div style={{
      display:'flex', alignItems:'center', justifyContent:'center',
      minHeight:'100vh', background:'var(--bg)'
    }}>
      <div style={{
        background:'var(--surface)', border:'1px solid var(--border)',
        borderRadius:16, padding:'48px 52px', width:380,
        boxShadow:'0 8px 40px #0008'
      }}>
        {/* Logo */}
        <div style={{ textAlign:'center', marginBottom:32 }}>
          <div style={{ fontFamily:'var(--font-head)', fontSize:28, fontWeight:800, color:'var(--accent)' }}>
            TEKLİF
          </div>
          <div style={{ fontSize:11, color:'var(--muted)', letterSpacing:'.15em', marginTop:4 }}>
            TAKİP SİSTEMİ
          </div>
        </div>

        <div style={{ marginBottom:14 }}>
          <label style={{ fontSize:12, color:'var(--muted)', display:'block', marginBottom:5 }}>Kullanıcı Adı</label>
          <input className="input" value={kullaniciAdi} autoFocus
            onChange={e=>setKullaniciAdi(e.target.value)}
            onKeyDown={e=>e.key==='Enter'&&giris()}
            placeholder="kullanici_adi" style={{fontSize:14,padding:'10px 12px'}}/>
        </div>
        <div style={{ marginBottom:20 }}>
          <label style={{ fontSize:12, color:'var(--muted)', display:'block', marginBottom:5 }}>Şifre</label>
          <input className="input" type="password" value={sifre}
            onChange={e=>setSifre(e.target.value)}
            onKeyDown={e=>e.key==='Enter'&&giris()}
            placeholder="••••••••" style={{fontSize:14,padding:'10px 12px'}}/>
        </div>

        {hata && (
          <div style={{ color:'var(--red)', fontSize:13, marginBottom:14, padding:'8px 12px',
            background:'var(--red)15', borderRadius:6, border:'1px solid var(--red)30' }}>
            {hata}
          </div>
        )}

        <button className="btn btn-primary" style={{ width:'100%', padding:'11px', fontSize:14 }}
          onClick={giris} disabled={yukleniyor}>
          {yukleniyor ? 'Giriş yapılıyor...' : 'Giriş Yap'}
        </button>
      </div>
    </div>
  );
}
