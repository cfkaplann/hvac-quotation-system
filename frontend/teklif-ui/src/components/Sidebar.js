import React, { useState } from 'react';

const statusColors = {
  BEKLIYOR:'#f59e0b', ONAYLANDI:'#10b981',
  REDDEDILDI:'#ef4444', REVIZE:'#3b82f6', IPTAL:'#64748b',
};

export default function Sidebar({ page, setPage, stats, kullanici, onCikis }) {
  const isAdmin = kullanici?.rol === 'ADMIN';
  const [yonetimAcik, setYonetimAcik] = useState(
    ['admin','kullanicilar'].includes(page)
  );

  const yonetimSayfalar = [
    { key:'admin',        label:'Fiyat Yönetimi',    icon:'💰' },
    { key:'urun-yonetim', label:'Ürün Yönetimi',     icon:'📦' },
    { key:'kullanicilar', label:'Kullanıcı Yönetimi', icon:'👥' },
    { key:'sistem-ayar',   label:'Sistem Ayarları',    icon:'🔧' },
  ];

  const isYonetim = yonetimSayfalar.some(n => n.key === page);

  const navBtn = (key, label, icon) => (
    <button key={key} onClick={() => setPage(key)} style={{
      display:'flex', alignItems:'center', gap:10,
      width:'100%', padding:'10px 12px', borderRadius:8,
      border:'none', cursor:'pointer',
      background: page===key ? 'var(--surface2)' : 'transparent',
      color: page===key ? 'var(--text)' : 'var(--muted)',
      fontFamily:'var(--font-body)', fontSize:14,
      fontWeight: page===key ? 500 : 400,
      marginBottom:4, textAlign:'left',
      borderLeft: page===key ? '2px solid var(--accent)' : '2px solid transparent',
      transition:'all .15s',
    }}>
      <span>{icon}</span>{label}
    </button>
  );

  return (
    <aside style={{
      width:220, flexShrink:0, background:'var(--surface)',
      borderRight:'1px solid var(--border)', display:'flex',
      flexDirection:'column', height:'100vh', position:'sticky', top:0,
    }}>
      {/* Logo */}
      <div style={{ padding:'24px 20px 20px', borderBottom:'1px solid var(--border)' }}>
        <div style={{ fontFamily:'var(--font-head)', fontSize:18, fontWeight:800, color:'var(--accent)' }}>
          TEKLİF
        </div>
        <div style={{ fontSize:11, color:'var(--muted)', letterSpacing:'.1em', marginTop:2 }}>
          TAKİP SİSTEMİ
        </div>
      </div>

      {/* Nav */}
      <nav style={{ padding:'16px 12px', flex:1, overflowY:'auto' }}>

        {navBtn('teklifler',  'Teklifler',  '📋')}
        {navBtn('musteriler', 'Müşteriler', '🏢')}
        {navBtn('uretim-takip', 'Üretim Takip', '🏭')}

        {/* Yönetim grubu */}
        {isAdmin && (
          <>
            <button onClick={() => setYonetimAcik(v => !v)} style={{
              display:'flex', alignItems:'center', justifyContent:'space-between',
              width:'100%', padding:'10px 12px', borderRadius:8,
              border:'none', cursor:'pointer', marginBottom:4,
              background: isYonetim ? 'var(--surface2)' : 'transparent',
              color: isYonetim ? 'var(--accent)' : 'var(--muted)',
              fontFamily:'var(--font-body)', fontSize:14,
              fontWeight: isYonetim ? 600 : 400, textAlign:'left',
              borderLeft: isYonetim ? '2px solid var(--accent)' : '2px solid transparent',
              transition:'all .15s',
            }}>
              <span style={{display:'flex',alignItems:'center',gap:10}}>
                <span>⚙️</span>Yönetim
              </span>
              <span style={{fontSize:10,transition:'transform .2s',
                transform: yonetimAcik ? 'rotate(180deg)' : 'rotate(0deg)'}}>▼</span>
            </button>

            {yonetimAcik && (
              <div style={{paddingLeft:16, borderLeft:'1px solid var(--border)',
                marginLeft:12, marginBottom:4}}>
                {yonetimSayfalar.map(n => (
                  <button key={n.key} onClick={() => setPage(n.key)} style={{
                    display:'flex', alignItems:'center', gap:8,
                    width:'100%', padding:'8px 10px', borderRadius:6,
                    border:'none', cursor:'pointer', marginBottom:2,
                    background: page===n.key ? 'var(--accent)15' : 'transparent',
                    color: page===n.key ? 'var(--accent)' : 'var(--muted)',
                    fontFamily:'var(--font-body)', fontSize:13,
                    fontWeight: page===n.key ? 500 : 400, textAlign:'left',
                    transition:'all .15s',
                  }}>
                    <span>{n.icon}</span>{n.label}
                  </button>
                ))}
              </div>
            )}
          </>
        )}

        {/* Durum özeti */}
        {stats && (
          <div style={{ marginTop:24 }}>
            <div style={{ fontSize:11, color:'var(--muted)', letterSpacing:'.08em',
              textTransform:'uppercase', marginBottom:10, padding:'0 4px' }}>
              Durum Özeti
            </div>
            {Object.entries(stats).map(([durum,sayi]) => (
              <div key={durum} style={{ display:'flex', justifyContent:'space-between',
                alignItems:'center', padding:'6px 8px', borderRadius:6, marginBottom:3 }}>
                <span style={{ fontSize:12, color:statusColors[durum]||'var(--muted)' }}>{durum}</span>
                <span style={{ fontSize:11, fontWeight:600,
                  background:(statusColors[durum]||'#64748b')+'20',
                  color:statusColors[durum]||'var(--muted)',
                  padding:'1px 8px', borderRadius:10 }}>
                  {sayi}
                </span>
              </div>
            ))}
          </div>
        )}
      </nav>

      {/* Kullanıcı bilgisi + çıkış */}
      <div style={{ padding:'12px 16px', borderTop:'1px solid var(--border)' }}>
        <div style={{ display:'flex', alignItems:'center', gap:8, marginBottom:8 }}>
          <div style={{ width:32, height:32, borderRadius:'50%',
            background:'var(--accent)20', display:'flex', alignItems:'center',
            justifyContent:'center', fontSize:14, color:'var(--accent)', flexShrink:0 }}>
            {(kullanici?.adSoyad||kullanici?.kullaniciAdi||'?')[0].toUpperCase()}
          </div>
          <div style={{ flex:1, overflow:'hidden' }}>
            <div style={{ fontSize:12, fontWeight:600, color:'var(--text)',
              overflow:'hidden', textOverflow:'ellipsis', whiteSpace:'nowrap' }}>
              {kullanici?.adSoyad || kullanici?.kullaniciAdi}
            </div>
            <div style={{ fontSize:10, color:'var(--muted)' }}>{kullanici?.rol}</div>
          </div>
        </div>
        <button onClick={onCikis} style={{ width:'100%', padding:'6px', fontSize:12,
          background:'transparent', border:'1px solid var(--border)', borderRadius:6,
          color:'var(--muted)', cursor:'pointer' }}>
          Çıkış Yap
        </button>
      </div>
    </aside>
  );
}
