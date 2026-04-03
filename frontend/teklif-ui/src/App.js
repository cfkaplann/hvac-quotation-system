import React, { useState, useEffect } from 'react';
import './index.css';
import Sidebar from './components/Sidebar';
import TekliflerPage from './pages/TekliflerPage';
import MusterilerPage from './pages/MusterilerPage';
import AdminPage from './pages/AdminPage';
import UrunYonetimPage from './pages/UrunYonetimPage';
import UretimTakipPage from './pages/UretimTakipPage';
import SistemAyarPage from './pages/SistemAyarPage';
import KullanicilarPage from './pages/KullanicilarPage';
import GirisPage from './pages/GirisPage';
import { tokenDogrula } from './services/api';

export default function App() {
  const [page, setPage]         = useState('teklifler');
  const [stats, setStats]       = useState(null);
  const [kullanici, setKullanici] = useState(null);
  const [yukleniyor, setYukleniyor] = useState(true);

  // Sayfa açılışında token kontrol et
  useEffect(() => {
    const token = localStorage.getItem('auth_token');
    if (!token) { setYukleniyor(false); return; }
    tokenDogrula()
      .then(r => { setKullanici(r.data); })
      .catch(() => { localStorage.removeItem('auth_token'); localStorage.removeItem('auth_kullanici'); })
      .finally(() => setYukleniyor(false));
  }, []);

  const cikisYap = () => {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_kullanici');
    setKullanici(null);
    setPage('teklifler');
  };

  if (yukleniyor) return (
    <div style={{ display:'flex', alignItems:'center', justifyContent:'center', minHeight:'100vh', color:'var(--muted)' }}>
      Yükleniyor...
    </div>
  );

  if (!kullanici) return <GirisPage onGiris={setKullanici} />;

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar page={page} setPage={setPage} stats={stats} kullanici={kullanici} onCikis={cikisYap} />
      <main style={{ flex: 1, overflow: 'auto' }}>
        {page === 'teklifler'   && <TekliflerPage onStatsChange={setStats} kullanici={kullanici} />}
        {page === 'musteriler'  && <MusterilerPage />}
        {page === 'admin'       && kullanici.rol === 'ADMIN' && <AdminPage />}
        {page === 'kullanicilar'&& kullanici.rol === 'ADMIN' && <KullanicilarPage />}
        {page === 'urun-yonetim'  && kullanici.rol === 'ADMIN' && <UrunYonetimPage />}
        {page === 'uretim-takip'  && <UretimTakipPage />}
        {page === 'sistem-ayar'   && kullanici.rol === 'ADMIN' && <SistemAyarPage />}
        {page === 'admin'       && kullanici.rol !== 'ADMIN' && (
          <div style={{ padding:40, color:'var(--muted)' }}>Bu sayfaya erişim yetkiniz yok.</div>
        )}
      </main>
    </div>
  );
}
