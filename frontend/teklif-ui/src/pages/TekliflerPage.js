import React, { useState, useEffect, useCallback } from 'react';
import { getTeklifler, getTeklif, getKurlar, deleteTeklif, updateDurum, revizeTeklif, getNotlar, addNot, deleteNot } from '../services/api';
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
  return (
    <a
      href={`/api/teklifler/${teklifId}/excel`}
      target="_blank" rel="noreferrer"
      className="btn btn-secondary btn-sm"
      style={{ textDecoration: 'none' }}
    >
      XLS
    </a>
  );
}


export default function TekliflerPage({ onStatsChange, kullanici }) {
  const [teklifler, setTeklifler]  = useState([]);
  const [loading, setLoading]      = useState(true);
  const [filtreDurum, setFiltreDurum] = useState('');
  const [aramaText, setAramaText]  = useState('');
  const [formOpen, setFormOpen]    = useState(false);
  const [editTeklif, setEditTeklif] = useState(null);
  const [kurlar, setKurlar] = useState({ EUR: 42, USD: 38 });
  const [confirm, setConfirm]      = useState(null);
  const [notModal, setNotModal]    = useState(null);

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
    getKurlar().then(r => setKurlar(r.data)).catch(() => {});
  };

  const handleRevize = async (id) => {
    await revizeTeklif(id);
    load();
    getKurlar().then(r => setKurlar(r.data)).catch(() => {});
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
                  <td style={{ color: 'var(--muted)', fontSize: 13 }}>{t.musteri?.firmaAdi || t.musteriAdi || '—'}</td>
                  <td style={{ color: 'var(--muted)', fontSize: 13 }}>{t.teklifiVeren || '—'}</td>
                  <td style={{ color: 'var(--muted)', fontSize: 13, whiteSpace: 'nowrap' }}>{t.teklifTarihi}</td>
                  <td style={{ fontWeight: 600, color: 'var(--green)', whiteSpace: 'nowrap' }}>
                    {(() => {
                      const pb = t.paraBirimi;
                      const kur = kurlar.EUR || 42;
                      const kurUSD = kurlar.USD || 38;
                      const tl = t.genelToplam || 0;
                      const val = pb === 'EUR' ? tl / kur : pb === 'USD' ? tl / kurUSD : tl;
                      return val.toLocaleString('tr-TR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
                    })()} <span style={{ fontSize: 11, fontWeight: 400, color: 'var(--muted)' }}>{t.paraBirimi}</span>
                  </td>
                  <td><StatusBadge durum={t.durum} /></td>
                  <td>
                    <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end', flexWrap: 'nowrap' }}>
                      <DurumMenu teklif={t} onRefresh={load} />
                      <a className="btn btn-secondary btn-sm" href={`/api/teklifler/${t.id}/pdf`} target="_blank" rel="noreferrer" title="PDF indir">PDF</a>
                      <ExcelBtn teklifId={t.id} />
                      <button className="btn btn-secondary btn-sm" onClick={() => setNotModal(t.id)} title="Notlar">📝</button>
                      <button className="btn btn-secondary btn-sm" onClick={async () => { const r = await getTeklif(t.id); setEditTeklif(r.data); setFormOpen(true); }}>Düzenle</button>
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
          kullanici={kullanici}
          onSave={() => { setFormOpen(false); load(); }}
          onClose={() => setFormOpen(false)}
        />
      )}


      {/* Not Modali */}
      {notModal && (
        <NotModal
          teklifId={notModal}
          onKapat={() => setNotModal(null)}
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

/* ── Not Modali ─────────────────────────────────────────── */
function NotModal({ teklifId, onKapat }) {
  const [notlar, setNotlar] = React.useState([]);
  const [yeniNot, setYeniNot] = React.useState('');
  const [yukleniyor, setYukleniyor] = React.useState(true);

  const yukle = React.useCallback(() => {
    setYukleniyor(true);
    getNotlar(teklifId).then(r => setNotlar(r.data || [])).catch(() => {}).finally(() => setYukleniyor(false));
  }, [teklifId]);

  React.useEffect(() => { yukle(); }, [yukle]);

  const kaydet = async () => {
    if (!yeniNot.trim()) return;
    try {
      await addNot(teklifId, { icerik: yeniNot.trim() });
      setYeniNot('');
      yukle();
    } catch(e) { alert('Hata: ' + e.message); }
  };

  const sil = async (notId) => {
    if (!window.confirm('Bu notu silmek istediğinize emin misiniz?')) return;
    await deleteNot(teklifId, notId);
    yukle();
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onKapat()}>
      <div className="modal" style={{maxWidth:500}} onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Notlar</h2>
          <button className="close-btn" onClick={onKapat}>×</button>
        </div>
        <div className="modal-body">
          {/* Yeni not ekle */}
          <div style={{marginBottom:16}}>
            <textarea className="input textarea" rows={3}
              placeholder="Not ekleyin..."
              value={yeniNot}
              onChange={e => setYeniNot(e.target.value)}
              onKeyDown={e => { if (e.ctrlKey && e.key === 'Enter') kaydet(); }}
              style={{marginBottom:8}}/>
            <button className="btn btn-primary btn-sm" onClick={kaydet}>+ Not Ekle</button>
            <span style={{fontSize:11,color:'var(--muted)',marginLeft:8}}>veya Ctrl+Enter</span>
          </div>

          {/* Not listesi */}
          {yukleniyor && <div style={{color:'var(--muted)',fontSize:13}}>Yükleniyor...</div>}
          {!yukleniyor && notlar.length === 0 && (
            <div style={{color:'var(--muted)',fontSize:13}}>Henüz not yok.</div>
          )}
          {notlar.map(n => (
            <div key={n.id} style={{
              background:'var(--surface2)', border:'1px solid var(--border)',
              borderRadius:8, padding:'10px 14px', marginBottom:8,
              display:'flex', justifyContent:'space-between', alignItems:'flex-start', gap:8
            }}>
              <div style={{flex:1}}>
                <div style={{fontSize:13, color:'var(--text)', whiteSpace:'pre-wrap'}}>{n.icerik}</div>
                <div style={{fontSize:11, color:'var(--muted)', marginTop:4}}>
                  {n.tarih?.slice(0,16).replace('T',' ')} {n.yazan ? `— ${n.yazan}` : ''}
                </div>
              </div>
              <button onClick={() => sil(n.id)}
                style={{background:'none',border:'none',color:'var(--red)',cursor:'pointer',fontSize:16,padding:'2px 4px',flexShrink:0}}>×</button>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
