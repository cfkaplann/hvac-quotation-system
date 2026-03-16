import React, { useState, useEffect, useCallback } from 'react';
import { getMusteriler, createMusteri, updateMusteri, deleteMusteri } from '../services/api';

const EMPTY = { firmaAdi: '', yetkili: '', telefon: '', eposta: '', adres: '', notlar: '' };

function MusteriForm({ musteri, onSave, onClose }) {
  const [form, setForm] = useState(musteri ? { ...musteri } : { ...EMPTY });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = async () => {
    if (!form.firmaAdi.trim()) { setError('Firma adı zorunludur.'); return; }
    setLoading(true); setError('');
    try {
      if (musteri?.id) await updateMusteri(musteri.id, form);
      else await createMusteri(form);
      onSave();
    } catch (e) {
      setError(e.response?.data?.hata || 'Bir hata oluştu.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{musteri ? 'Müşteri Düzenle' : 'Yeni Müşteri'}</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">
          <div className="form-row col2">
            <div className="form-group">
              <label>Firma Adı *</label>
              <input className="input" placeholder="ABC Ltd. Şti." value={form.firmaAdi} onChange={e => set('firmaAdi', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Yetkili</label>
              <input className="input" placeholder="Ad Soyad" value={form.yetkili || ''} onChange={e => set('yetkili', e.target.value)} />
            </div>
          </div>
          <div className="form-row col2">
            <div className="form-group">
              <label>Telefon</label>
              <input className="input" placeholder="0212 000 00 00" value={form.telefon || ''} onChange={e => set('telefon', e.target.value)} />
            </div>
            <div className="form-group">
              <label>E-Posta</label>
              <input className="input" type="email" placeholder="info@firma.com" value={form.eposta || ''} onChange={e => set('eposta', e.target.value)} />
            </div>
          </div>
          <div className="form-group">
            <label>Adres</label>
            <input className="input" placeholder="Açık adres..." value={form.adres || ''} onChange={e => set('adres', e.target.value)} />
          </div>
          <div className="form-group">
            <label>Notlar</label>
            <textarea className="textarea" placeholder="İsteğe bağlı..." value={form.notlar || ''} onChange={e => set('notlar', e.target.value)} />
          </div>
          {error && <div style={{ color: 'var(--red)', fontSize: 13 }}>{error}</div>}
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>İptal</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Kaydediliyor...' : (musteri ? 'Güncelle' : 'Kaydet')}
          </button>
        </div>
      </div>
    </div>
  );
}

export default function MusterilerPage() {
  const [musteriler, setMusteriler] = useState([]);
  const [loading, setLoading]       = useState(true);
  const [ara, setAra]               = useState('');
  const [formOpen, setFormOpen]     = useState(false);
  const [editMusteri, setEditMusteri] = useState(null);
  const [confirm, setConfirm]       = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const r = await getMusteriler(ara || null);
      setMusteriler(r.data);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  }, [ara]);

  useEffect(() => {
    const t = setTimeout(load, 300);
    return () => clearTimeout(t);
  }, [load]);

  const handleDelete = async (id) => {
    await deleteMusteri(id);
    setConfirm(null);
    load();
  };

  return (
    <div style={{ padding: 28, flex: 1 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 24 }}>
        <div>
          <h1 style={{ fontFamily: 'var(--font-head)', fontSize: 26, fontWeight: 800 }}>Müşteriler</h1>
          <p style={{ color: 'var(--muted)', fontSize: 13, marginTop: 4 }}>{musteriler.length} kayıt</p>
        </div>
        <button className="btn btn-primary" onClick={() => { setEditMusteri(null); setFormOpen(true); }}>
          + Yeni Müşteri
        </button>
      </div>

      <input
        className="input" style={{ maxWidth: 320, marginBottom: 20 }}
        placeholder="🔍  Firma adı veya yetkili ara..."
        value={ara} onChange={e => setAra(e.target.value)}
      />

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <table>
          <thead>
            <tr>
              <th>Firma Adı</th>
              <th>Yetkili</th>
              <th>Telefon</th>
              <th>E-Posta</th>
              <th>Kayıt Tarihi</th>
              <th style={{ textAlign: 'right' }}>İşlemler</th>
            </tr>
          </thead>
          <tbody>
            {loading && <tr><td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--muted)' }}>Yükleniyor...</td></tr>}
            {!loading && musteriler.length === 0 && (
              <tr><td colSpan={6} style={{ textAlign: 'center', padding: 40, color: 'var(--muted)' }}>
                Müşteri bulunamadı.
              </td></tr>
            )}
            {musteriler.map(m => (
              <tr key={m.id}>
                <td style={{ fontWeight: 600 }}>{m.firmaAdi}</td>
                <td style={{ color: 'var(--muted)' }}>{m.yetkili || '—'}</td>
                <td style={{ color: 'var(--muted)', fontSize: 13 }}>{m.telefon || '—'}</td>
                <td style={{ color: 'var(--muted)', fontSize: 13 }}>{m.eposta || '—'}</td>
                <td style={{ color: 'var(--muted)', fontSize: 12 }}>{m.olusturmaTarihi?.slice(0, 10) || '—'}</td>
                <td>
                  <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                    <button className="btn btn-secondary btn-sm" onClick={() => { setEditMusteri(m); setFormOpen(true); }}>Düzenle</button>
                    <button className="btn btn-danger btn-sm" onClick={() => setConfirm(m.id)}>Sil</button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {formOpen && (
        <MusteriForm
          musteri={editMusteri}
          onSave={() => { setFormOpen(false); load(); }}
          onClose={() => setFormOpen(false)}
        />
      )}

      {confirm && (
        <div className="modal-overlay" onClick={() => setConfirm(null)}>
          <div className="modal" style={{ maxWidth: 380 }} onClick={e => e.stopPropagation()}>
            <div className="modal-header"><h2>Müşteri Sil</h2><button className="close-btn" onClick={() => setConfirm(null)}>×</button></div>
            <div className="modal-body"><p style={{ color: 'var(--muted)' }}>Bu müşteri kalıcı olarak silinecek. Emin misiniz?</p></div>
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
