import React, { useState, useEffect } from 'react';
import { getSistemAyarlari, updateSistemAyar } from '../services/api';

const AYAR_LABEL = {
  'yay_fiyati': 'Yay Fiyatı (TL)'
};

const AYAR_ACIKLAMA = {
  'yay_fiyati': '"Spot Yaylı" özelliği seçildiğinde toplam fiyata eklenir: Yay Fiyatı × 2'
};

export default function SistemAyarPage() {
  const [ayarlar, setAyarlar] = useState([]);
  const [degerler, setDegerler] = useState({});
  const [mesajlar, setMesajlar] = useState({});

  useEffect(() => {
    getSistemAyarlari().then(r => {
      setAyarlar(r.data);
      const d = {};
      r.data.forEach(a => { d[a.anahtar] = a.deger; });
      setDegerler(d);
    }).catch(() => {});
  }, []);

  const kaydet = async (anahtar) => {
    try {
      await updateSistemAyar(anahtar, { deger: degerler[anahtar] });
      setMesajlar(prev => ({ ...prev, [anahtar]: '✓ Kaydedildi' }));
      setTimeout(() => setMesajlar(prev => ({ ...prev, [anahtar]: '' })), 2000);
    } catch(e) { alert('Hata: ' + e.message); }
  };

  return (
    <div style={{ padding: 28, flex: 1, maxWidth: 700 }}>
      <h1 style={{ fontFamily: 'var(--font-head)', fontSize: 24, fontWeight: 800, marginBottom: 24 }}>
        🔧 Sistem Ayarları
      </h1>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        {ayarlar.map(a => (
          <div key={a.anahtar} style={{
            background: 'var(--surface2)', border: '1px solid var(--border)',
            borderRadius: 10, padding: 20
          }}>
            <div style={{ fontWeight: 700, fontFamily: 'var(--font-head)', fontSize: 15, marginBottom: 4 }}>
              {AYAR_LABEL[a.anahtar] || a.anahtar}
            </div>
            <div style={{ fontSize: 12, color: 'var(--muted)', marginBottom: 12 }}>
              {AYAR_ACIKLAMA[a.anahtar] || a.aciklama}
            </div>
            <div style={{ display: 'flex', gap: 10, alignItems: 'center' }}>
              <input
                className="input"
                type="number"
                step="0.01"
                min="0"
                style={{ width: 160, fontSize: 14, padding: '8px 10px', fontWeight: 600 }}
                value={degerler[a.anahtar] ?? ''}
                onChange={e => setDegerler(prev => ({ ...prev, [a.anahtar]: e.target.value }))}
                onKeyDown={e => e.key === 'Enter' && kaydet(a.anahtar)}
              />
              <button className="btn btn-primary" style={{ padding: '8px 20px' }}
                onClick={() => kaydet(a.anahtar)}>
                Kaydet
              </button>
              {mesajlar[a.anahtar] && (
                <span style={{ color: 'var(--green)', fontSize: 13, fontWeight: 600 }}>
                  {mesajlar[a.anahtar]}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
