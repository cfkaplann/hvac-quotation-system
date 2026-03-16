import React, { useState } from 'react';
import './index.css';
import Sidebar from './components/Sidebar';
import TekliflerPage from './pages/TekliflerPage';
import MusterilerPage from './pages/MusterilerPage';

export default function App() {
  const [page, setPage]   = useState('teklifler');
  const [stats, setStats] = useState(null);

  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar page={page} setPage={setPage} stats={stats} />
      <main style={{ flex: 1, overflow: 'auto' }}>
        {page === 'teklifler'  && <TekliflerPage  onStatsChange={setStats} />}
        {page === 'musteriler' && <MusterilerPage />}
      </main>
    </div>
  );
}
