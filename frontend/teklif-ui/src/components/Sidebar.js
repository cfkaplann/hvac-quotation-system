import React from 'react';

const nav = [
  { key: 'teklifler',  label: 'Teklifler',   icon: '📋' },
  { key: 'musteriler', label: 'Müşteriler',  icon: '🏢' },
];

const statusColors = {
  BEKLIYOR:   '#f59e0b',
  ONAYLANDI:  '#10b981',
  REDDEDILDI: '#ef4444',
  REVIZE:     '#3b82f6',
  IPTAL:      '#64748b',
};

export default function Sidebar({ page, setPage, stats }) {
  return (
    <aside style={{
      width: 220, flexShrink: 0,
      background: 'var(--surface)',
      borderRight: '1px solid var(--border)',
      display: 'flex', flexDirection: 'column',
      height: '100vh', position: 'sticky', top: 0,
    }}>
      {/* Logo */}
      <div style={{ padding: '24px 20px 20px', borderBottom: '1px solid var(--border)' }}>
        <div style={{ fontFamily: 'var(--font-head)', fontSize: 18, fontWeight: 800, color: 'var(--accent)' }}>
          TEKLİF
        </div>
        <div style={{ fontSize: 11, color: 'var(--muted)', letterSpacing: '.1em', marginTop: 2 }}>
          TAKİP SİSTEMİ
        </div>
      </div>

      {/* Nav */}
      <nav style={{ padding: '16px 12px', flex: 1 }}>
        {nav.map(n => (
          <button key={n.key} onClick={() => setPage(n.key)} style={{
            display: 'flex', alignItems: 'center', gap: 10,
            width: '100%', padding: '10px 12px',
            borderRadius: 8, border: 'none', cursor: 'pointer',
            background: page === n.key ? 'var(--surface2)' : 'transparent',
            color: page === n.key ? 'var(--text)' : 'var(--muted)',
            fontFamily: 'var(--font-body)', fontSize: 14,
            fontWeight: page === n.key ? 500 : 400,
            marginBottom: 4, textAlign: 'left',
            borderLeft: page === n.key ? '2px solid var(--accent)' : '2px solid transparent',
            transition: 'all .15s',
          }}>
            <span>{n.icon}</span>
            {n.label}
          </button>
        ))}

        {/* Durum özeti */}
        {stats && (
          <div style={{ marginTop: 24 }}>
            <div style={{ fontSize: 11, color: 'var(--muted)', letterSpacing: '.08em', textTransform: 'uppercase', marginBottom: 10, padding: '0 4px' }}>
              Durum Özeti
            </div>
            {Object.entries(stats).map(([durum, sayi]) => (
              <div key={durum} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '6px 8px', borderRadius: 6, marginBottom: 3,
              }}>
                <span style={{ fontSize: 12, color: statusColors[durum] || 'var(--muted)' }}>
                  {durum}
                </span>
                <span style={{
                  fontSize: 11, fontWeight: 600,
                  background: (statusColors[durum] || '#64748b') + '20',
                  color: statusColors[durum] || 'var(--muted)',
                  padding: '1px 8px', borderRadius: 10,
                }}>
                  {sayi}
                </span>
              </div>
            ))}
          </div>
        )}
      </nav>

      {/* Footer */}
      <div style={{ padding: '16px 20px', borderTop: '1px solid var(--border)', fontSize: 11, color: 'var(--muted)' }}>
        v1.0 · localhost:8080
      </div>
    </aside>
  );
}
