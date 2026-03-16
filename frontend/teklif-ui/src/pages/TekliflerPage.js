import React, { useState, useEffect, useCallback } from 'react';
import { getTeklifler, deleteTeklif, updateDurum, revizeTeklif } from '../services/api';
import TeklifForm from '../components/TeklifForm';

const DURUMLAR = ['', 'BEKLIYOR', 'ONAYLANDI', 'REDDEDILDI', 'REVIZE', 'IPTAL'];

const DURUM_LABEL = {
  BEKLIYOR: 'Bekliyor', ONAYLANDI: 'Onaylandı',
  REDDEDILDI: 'Reddedildi', REVIZE: 'Revize', IPTAL: 'İptal',
};

function StatusBadge({ durum }) {
  return <span className={`badge badge-${durum?.toLowerCase()}`}>{DURUM_LABEL[durum] || durum}</span>;
}

function DurumMenu({ teklif, onRefresh }) {
  const [open, setOpen] = useState(false);
  const durumlar = ['BEKLIYOR', 'ONAYLANDI', 'REDDEDILDI', 'REVIZE', 'IPTAL'];
  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <button className="btn btn-secondary btn-sm" onClick={() => setOpen(o => !o)}>
        Durum ▾
      </button>
      {open && (
        <div style={{
          position: 'absolute', top: '110%', right: 0, zIndex: 100,
          background: 'var(--surface)', border: '1px solid var(--border)',
          borderRadius: 8, overflow: 'hidden', minWidth: 140,
          boxShadow: '0 8px 24px rgba(0,0,0,.4)',
        }}>
          {durumlar.map(d => (
            <button key={d} onClick={async () => {
              setOpen(false);
              await updateDurum(teklif.id, d);
              onRefresh();
            }} style={{
              display: 'block', width: '100%', padding: '9px 14px',
              textAlign: 'left', background: teklif.durum === d ? 'var(--surface2)' : 'transparent',
              border: 'none', color: 'var(--text)', cursor: 'pointer',
              fontFamily: 'var(--font-body)', fontSize: 13,
            }}
              onMouseEnter={e => e.target.style.background = 'var(--surface2)'}
              onMouseLeave={e => e.target.style.background = teklif.durum === d ? 'var(--surface2)' : 'transparent'}
            >
              {DURUM_LABEL[d]}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}


function ExcelBtn({ teklifId }) {
  const [open, setOpen] = React.useState(false);
  const opts = [
    { label: '₺ TL', val: 'TRY' },
    { label: '€ EUR', val: 'EUR' },
    { label: '$ USD', val: 'USD' },
  ];
  return (
    <div style={{ position: 'relative', display: 'inline-block' }}>
      <button className="btn btn-secondary btn-sm" onClick={() => setOpen(o => !o)}>
        XLS ▾
      </button>
      {open && (
        <div style={{
          position: 'absolute', right: 0, top: '110%', zIndex: 100,
          background: 'var(--surface)', border: '1px solid var(--border)',
          borderRadius: 8, padding: '4px 0', minWidth: 90,
          boxShadow: '0 4px 16px #0006',
        }}
          onMouseLeave={() => setOpen(false)}
        >
          {opts.map(o => (
            <a key={o.val}
              href={`http://localhost:8080/api/teklifler/${teklifId}/excel?paraBirimi=${o.val}`}
              target="_blank" rel="noreferrer"
              onClick={() => setOpen(false)}
              style={{ display: 'block', padding: '7px 14px', fontSize: 13, color: 'var(--text)', textDecoration: 'none', cursor: 'pointer' }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--surface2)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
            >
              {o.label}
            </a>
          ))}
        </div>
      )}
    </div>
  );
}


export default function TekliflerPage({ onStatsChange }) {
  const [teklifler, setTeklifler]  = useState([]);
  const [loading, setLoading]      = useState(true);
  const [filtreDurum, setFiltreDurum] = useState('');
  const [aramaText, setAramaText]  = useState('');
  const [formOpen, setFormOpen]    = useState(false);
  const [editTeklif, setEditTeklif] = useState(null);
  const [confirm, setConfirm]      = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await getTeklifler(filtreDurum || null);
      setTeklifler(r.data);
      // stats hesapla
      const stats = {};
      r.data.forEach(t => { stats[t.durum] = (stats[t.durum] || 0) + 1; });
      onStatsChange?.(stats);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, [filtreDurum, onStatsChange]);

  useEffect(() => { load(); }, [load]);

  const filtered = teklifler.filter(t =>
    !aramaText || [t.teklifNo, t.isAdi, t.musteri?.firmaAdi, t.teklifiVeren]
      .some(f => f?.toLowerCase().includes(aramaText.toLowerCase()))
  );

  const handleDelete = async (id) => {
    await deleteTeklif(id);
    setConfirm(null);
    load();
  };

  const handleRevize = async (id) => {
    await revizeTeklif(id);
    load();
  };

  return (
    <div style={{ padding: 28, flex: 1, minHeight: '100vh' }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontFamily: 'var(--font-head)', fontSize: 26, fontWeight: 800 }}>Teklifler</h1>
          <p style={{ color: 'var(--muted)', fontSize: 13, marginTop: 4 }}>
            {filtered.length} teklif gösteriliyor
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => { setEditTeklif(null); setFormOpen(true); }}>
          + Yeni Teklif
        </button>
      </div>

      {/* Filtreler */}
      <div style={{ display: 'flex', gap: 12, marginBottom: 20, flexWrap: 'wrap' }}>
        <input
          className="input" style={{ maxWidth: 280 }}
          placeholder="🔍  Teklif no, iş adı, firma..."
          value={aramaText} onChange={e => setAramaText(e.target.value)}
        />
        <div style={{ display: 'flex', gap: 6 }}>
          {DURUMLAR.map(d => (
            <button key={d} onClick={() => setFiltreDurum(d)}
              className="btn btn-secondary btn-sm"
              style={{ borderColor: filtreDurum === d ? 'var(--accent)' : 'var(--border)', color: filtreDurum === d ? 'var(--accent)' : 'var(--muted)' }}>
              {d || 'Tümü'}
            </button>
          ))}
        </div>
      </div>

      {/* Tablo */}
      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Teklif No</th>
                <th>İş Adı</th>
                <th>Müşteri</th>
                <th>Teklifi Veren</th>
                <th>Tarih</th>
                <th>Tutar</th>
                <th>Durum</th>
                <th style={{ textAlign: 'right' }}>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {loading && (
                <tr><td colSpan={8} style={{ textAlign: 'center', padding: 40, color: 'var(--muted)' }}>Yükleniyor...</td></tr>
              )}
              {!loading && filtered.length === 0 && (
                <tr><td colSpan={8} style={{ textAlign: 'center', padding: 40, color: 'var(--muted)' }}>
                  Teklif bulunamadı. Yeni teklif oluşturun.
                </td></tr>
              )}
              {filtered.map(t => (
                <tr key={t.id}>
                  <td>
                    <span style={{ fontFamily: 'var(--font-head)', fontSize: 13, color: 'var(--accent)' }}>{t.teklifNo}</span>
                    {t.revizeNo > 0 && <span style={{ marginLeft: 6, fontSize: 11, color: 'var(--accent2)', background: 'var(--accent2)20', padding: '1px 6px', borderRadius: 4 }}>R{t.revizeNo}</span>}
                  </td>
                  <td style={{ fontWeight: 500, maxWidth: 200 }}>
                    <div style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{t.isAdi}</div>
                  </td>
                  <td style={{ color: 'var(--muted)', fontSize: 13 }}>{t.musteri?.firmaAdi || '—'}</td>
                  <td style={{ color: 'var(--muted)', fontSize: 13 }}>{t.teklifiVeren || '—'}</td>
                  <td style={{ color: 'var(--muted)', fontSize: 13, whiteSpace: 'nowrap' }}>{t.teklifTarihi}</td>
                  <td style={{ fontWeight: 600, color: 'var(--green)', whiteSpace: 'nowrap' }}>
                    {t.genelToplam?.toLocaleString('tr-TR', { minimumFractionDigits: 2 })} <span style={{ fontSize: 11, fontWeight: 400, color: 'var(--muted)' }}>{t.paraBirimi}</span>
                  </td>
                  <td><StatusBadge durum={t.durum} /></td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end', flexWrap: 'nowrap' }}>
                      <DurumMenu teklif={t} onRefresh={load} />
                      <a className="btn btn-secondary btn-sm" href={`http://localhost:8080/api/teklifler/${t.id}/pdf`} target="_blank" rel="noreferrer" title="PDF indir">PDF</a>
                      <ExcelBtn teklifId={t.id} />
                      <button className="btn btn-secondary btn-sm" onClick={() => { setEditTeklif(t); setFormOpen(true); }}>Düzenle</button>
                      <button className="btn btn-secondary btn-sm" onClick={() => handleRevize(t.id)} title="Revize oluştur">↩</button>
                      <button className="btn btn-danger btn-sm" onClick={() => setConfirm(t.id)}>Sil</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Form Modal */}
      {formOpen && (
        <TeklifForm
          teklif={editTeklif}
          onSave={() => { setFormOpen(false); load(); }}
          onClose={() => setFormOpen(false)}
        />
      )}

      {/* Silme onayı */}
      {confirm && (
        <div className="modal-overlay" onClick={() => setConfirm(null)}>
          <div className="modal" style={{ maxWidth: 380 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h2>Teklifi Sil</h2><button className="close-btn" onClick={() => setConfirm(null)}>×</button></div>
            <div className="modal-body"><p style={{ color: 'var(--muted)' }}>Bu teklif kalıcı olarak silinecek. Emin misiniz?</p></div>
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={() => setConfirm(null)}>İptal</button>
              <button className="btn btn-danger" onClick={() => handleDelete(confirm)}>Evet, Sil</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
