import React, { useState, useEffect } from 'react';
import { createTeklif, updateTeklif, getYeniNo, getMusteriler, getKurlar } from '../services/api';
import UrunSecimModal from './UrunSecimModal';

const EMPTY = {
  teklifNo: '', isAdi: '', musteriId: '',
  teklifTarihi: new Date().toISOString().slice(0,10),
  gecerlilikTarihi: '', teklifiVeren: '',
  paraBirimi: 'TL', kdvOrani: 20, notlar: '',
  kalemler: []
};

const EMPTY_KALEM = { urunKodu: '', urunAdi: '', adet: 1, birim: 'Adet', birimFiyat: 0, iskonto: 0, aciklama: '' };

export default function TeklifForm({ teklif, onSave, onClose }) {
  const [form, setForm]         = useState(EMPTY);
  const [musteriler, setMusteriler] = useState([]);
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');
  const [urunModal, setUrunModal] = useState(false);
  const [kurlar, setKurlar] = useState({ EUR: 42, USD: 38 });

  useEffect(() => {
    getMusteriler().then(r => setMusteriler(r.data)).catch(() => {});
    getKurlar().then(r => setKurlar(r.data)).catch(() => {});
    if (teklif) {
      setForm({ ...EMPTY, ...teklif, kalemler: teklif.kalemler || [] });
    } else {
      getYeniNo().then(r => setForm(f => ({ ...f, teklifNo: r.data.teklifNo }))).catch(() => {});
    }
  }, [teklif]);

  const tleCevir = (fiyat, pb) => {
    if (pb === 'EUR') return fiyat / (kurlar.EUR || 42);
    if (pb === 'USD') return fiyat / (kurlar.USD || 38);
    return fiyat;
  };

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const kalemSet = (i, k, v) => {
    const arr = [...form.kalemler];
    arr[i] = { ...arr[i], [k]: v };
    setForm(f => ({ ...f, kalemler: arr }));
  };

  const addKalem = () => setForm(f => ({ ...f, kalemler: [...f.kalemler, { ...EMPTY_KALEM }] }));

  const removeKalem = (i) => {
    const arr = form.kalemler.filter((_, idx) => idx !== i);
    setForm(f => ({ ...f, kalemler: arr }));
  };

  // birimFiyat her zaman TL saklanır, gösterimde çevrilir
  const kalemToplamTL = (k) => {
    const net = (k.birimFiyat || 0) * (k.adet || 1);
    return net - (net * (k.iskonto || 0) / 100);
  };
  const kalemToplam = (k) => tleCevir(kalemToplamTL(k), form.paraBirimi);
  const birimFiyatGoster = (fiyat) => tleCevir(fiyat || 0, form.paraBirimi);

  const araToplam = form.kalemler.reduce((s, k) => s + kalemToplam(k), 0);
  const pbSembol = form.paraBirimi === 'EUR' ? '€' : form.paraBirimi === 'USD' ? '$' : '₺';
  const kdvTutari = araToplam * (form.kdvOrani / 100);
  const genelToplam = araToplam + kdvTutari;

  const handleSubmit = async () => {
    if (!form.isAdi.trim()) { setError('İş adı zorunludur.'); return; }
    setLoading(true); setError('');
    try {
      if (teklif?.id) {
        await updateTeklif(teklif.id, form);
      } else {
        await createTeklif(form);
      }
      onSave();
    } catch (e) {
      setError(e.response?.data?.hata || 'Bir hata oluştu.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={e => e.target === e.currentTarget && onClose()}>
      <div className="modal modal-lg">
        <div className="modal-header">
          <h2>{teklif ? `Düzenle — ${teklif.teklifNo}` : 'Yeni Teklif'}</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        <div className="modal-body">

          {/* Temel Bilgiler */}
          <div className="form-row col3" style={{ marginBottom: 16 }}>
            <div className="form-group">
              <label>Teklif No</label>
              <input className="input" value={form.teklifNo} readOnly style={{ opacity: .6 }} />
            </div>
            <div className="form-group">
              <label>Teklif Tarihi *</label>
              <input className="input" type="date" value={form.teklifTarihi} onChange={e => set('teklifTarihi', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Geçerlilik Tarihi</label>
              <input className="input" type="date" value={form.gecerlilikTarihi} onChange={e => set('gecerlilikTarihi', e.target.value)} />
            </div>
          </div>

          <div className="form-row col2" style={{ marginBottom: 16 }}>
            <div className="form-group">
              <label>İş Adı *</label>
              <input className="input" placeholder="Proje / iş adı" value={form.isAdi} onChange={e => set('isAdi', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Müşteri</label>
              <select className="select" value={form.musteriId || ''} onChange={e => set('musteriId', e.target.value || null)}>
                <option value="">— Seç —</option>
                {musteriler.map(m => <option key={m.id} value={m.id}>{m.firmaAdi}</option>)}
              </select>
            </div>
          </div>

          <div className="form-row col3" style={{ marginBottom: 20 }}>
            <div className="form-group">
              <label>Teklifi Veren</label>
              <input className="input" placeholder="Ad Soyad" value={form.teklifiVeren || ''} onChange={e => set('teklifiVeren', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Para Birimi</label>
              <select className="select" value={form.paraBirimi} onChange={e => set('paraBirimi', e.target.value)}>
                <option value="TL">TL ₺</option>
                <option value="EUR">EUR €</option>
                <option value="USD">USD $</option>
              </select>
            </div>
            <div className="form-group">
              <label>KDV %</label>
              <input className="input" type="number" value={form.kdvOrani} onChange={e => set('kdvOrani', parseFloat(e.target.value) || 0)} />
            </div>
          </div>

          {/* Kalemler */}
          <div style={{ marginBottom: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span style={{ fontFamily: 'var(--font-head)', fontWeight: 700, fontSize: 15 }}>Kalemler</span>
            <div style={{display:"flex",gap:8}}>
            <button className="btn btn-secondary btn-sm" onClick={() => setUrunModal(true)}>🧮 Üründen Ekle</button>
            <button className="btn btn-secondary btn-sm" onClick={addKalem}>+ Manuel Ekle</button>
          </div>
          </div>

          <div className="table-wrap" style={{ border: '1px solid var(--border)', borderRadius: 8, marginBottom: 16 }}>
            <table>
              <thead>
                <tr>
                  <th style={{ width: 40 }}>#</th>
                  <th>Ürün Adı *</th>
                  <th style={{ width: 70 }}>Adet</th>
                  <th style={{ width: 80 }}>Birim</th>
                  <th style={{ width: 110 }}>Birim Fiyat</th>
                  <th style={{ width: 70 }}>İskonto %</th>
                  <th style={{ width: 100, textAlign: 'right' }}>Toplam</th>
                  <th style={{ width: 36 }}></th>
                </tr>
              </thead>
              <tbody>
                {form.kalemler.length === 0 && (
                  <tr><td colSpan={8} style={{ textAlign: 'center', color: 'var(--muted)', padding: 20 }}>Henüz kalem yok — yukarıdan ekleyin</td></tr>
                )}
                {form.kalemler.map((k, i) => (
                  <tr key={i}>
                    <td style={{ color: 'var(--muted)', fontSize: 12 }}>{i + 1}</td>
                    <td>
                      <input className="input" style={{ padding: '6px 8px' }} placeholder="Ürün adı" value={k.urunAdi} onChange={e => kalemSet(i, 'urunAdi', e.target.value)} />
                    </td>
                    <td>
                      <input className="input" style={{ padding: '6px 8px' }} type="number" min="1" value={k.adet} onChange={e => kalemSet(i, 'adet', parseInt(e.target.value) || 1)} />
                    </td>
                    <td>
                      <select className="select" style={{ padding: '6px 8px' }} value={k.birim} onChange={e => kalemSet(i, 'birim', e.target.value)}>
                        <option>Adet</option><option>m²</option><option>m</option><option>Takım</option>
                      </select>
                    </td>
                    <td>
                      <input className="input" style={{ padding: '6px 8px' }} type="number" step="0.01" value={Math.round(birimFiyatGoster(k.birimFiyat)*100)/100} onChange={e => { const v = parseFloat(e.target.value)||0; const tlV = form.paraBirimi==='EUR'?v*(kurlar.EUR||42):form.paraBirimi==='USD'?v*(kurlar.USD||38):v; kalemSet(i,'birimFiyat',tlV); }} />
                    </td>
                    <td>
                      <input className="input" style={{ padding: '6px 8px' }} type="number" min="0" max="100" value={k.iskonto} onChange={e => kalemSet(i, 'iskonto', parseFloat(e.target.value) || 0)} />
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 500, color: 'var(--accent)', whiteSpace: 'nowrap' }}>
                      {kalemToplam(k).toLocaleString('tr-TR', { minimumFractionDigits: 2 })}
                    </td>
                    <td>
                      <button onClick={() => removeKalem(i)} style={{ background: 'none', border: 'none', color: 'var(--red)', cursor: 'pointer', fontSize: 16, padding: '2px 4px' }}>×</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Totaller */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: 16 }}>
            <div style={{ background: 'var(--surface2)', borderRadius: 8, padding: '12px 20px', minWidth: 240 }}>
              {[
                ['Ara Toplam', araToplam],
                [`KDV (%${form.kdvOrani})`, kdvTutari],
                ['Genel Toplam', genelToplam],
              ].map(([lbl, val], i) => (
                <div key={lbl} style={{ display: 'flex', justifyContent: 'space-between', padding: '4px 0', borderTop: i === 2 ? '1px solid var(--border)' : 'none', fontWeight: i === 2 ? 600 : 400, color: i === 2 ? 'var(--accent)' : 'var(--text)' }}>
                  <span style={{ fontSize: 13 }}>{lbl}</span>
                  <span style={{ fontSize: 13 }}>{val.toLocaleString('tr-TR', { minimumFractionDigits: 2 })} {form.paraBirimi}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label>Notlar</label>
            <textarea className="textarea" placeholder="İsteğe bağlı notlar..." value={form.notlar || ''} onChange={e => set('notlar', e.target.value)} />
          </div>

          {error && <div style={{ color: 'var(--red)', fontSize: 13, marginTop: 8 }}>{error}</div>}

      {urunModal && (
        <UrunSecimModal
          paraBirimi={form.paraBirimi}
          kurlar={kurlar}
          onEkle={(kalem) => {
            setForm(f => ({ ...f, kalemler: [...f.kalemler, kalem] }));
            setUrunModal(false);
          }}
          onClose={() => setUrunModal(false)}
        />
      )}
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>İptal</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading ? 'Kaydediliyor...' : (teklif ? 'Güncelle' : 'Kaydet')}
          </button>
        </div>
      </div>
    </div>
  );
}
