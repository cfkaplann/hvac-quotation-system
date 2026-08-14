import axios from 'axios';

const API = axios.create({ baseURL: '/api' });

// Her istekte token header'ı otomatik ekle
API.interceptors.request.use(config => {
  const token = localStorage.getItem('auth_token');
  if (token) config.headers['X-Token'] = token;
  return config;
});

// ── Teklifler ──────────────────────────────────────
export const getTeklifler     = (durum) => API.get('/teklifler', { params: durum ? { durum } : {} });
export const getTeklif        = (id)    => API.get(`/teklifler/${id}`);
export const createTeklif     = (data)  => API.post('/teklifler', data);
export const updateTeklif     = (id, d) => API.put(`/teklifler/${id}`, d);
export const deleteTeklif     = (id)    => API.delete(`/teklifler/${id}`);
export const updateDurum      = (id, d) => API.patch(`/teklifler/${id}/durum`, { durum: d });
export const revizeTeklif     = (id)    => API.post(`/teklifler/${id}/revize`);
export const getYeniNo        = ()      => API.get('/teklifler/yeni-no');

// ── Müşteriler ────────────────────────────────────
export const getMusteriler    = (ara)   => API.get('/musteriler', { params: ara ? { ara } : {} });
export const createMusteri    = (data)  => API.post('/musteriler', data);
export const updateMusteri    = (id, d) => API.put(`/musteriler/${id}`, d);
export const deleteMusteri    = (id)    => API.delete(`/musteriler/${id}`);

// ── Ürünler & Fiyatlama ───────────────────────────────
export const getUrunler    = ()              => API.get('/urunler');
export const fiyatHesapla  = (kod, data)     => API.post(`/urunler/${kod}/fiyat`, data);

export const getKurlar = () => API.get('/kur');

// Notlar
export const getNotlar   = (teklifId)       => API.get(`/teklifler/${teklifId}/notlar`);
export const addNot      = (teklifId, not)  => API.post(`/teklifler/${teklifId}/notlar`, not);
export const deleteNot   = (teklifId, notId)=> API.delete(`/teklifler/${teklifId}/notlar/${notId}`);

// Auth
export const girisYap     = (data)       => API.post('/auth/giris', data);
export const tokenDogrula = ()           => API.get('/auth/dogrula');

// Özellik Oranları
export const getOzellikOranlari  = ()       => API.get('/admin/ozellik-oranlari');
export const updateOzellikOrani  = (data)   => API.put('/admin/ozellik-oranlari', data);
export const addOzellikOrani     = (data)   => API.post('/admin/ozellik-oranlari', data);
export const deleteOzellikOrani  = (data)   => API.delete('/admin/ozellik-oranlari', { data });

// Ürün Özellik Yönetimi
export const getUrunOzellikler       = (kod)        => API.get(`/admin/urun-ozellikler/${kod}`);
export const updateUrunOzellikler    = (kod, data)   => API.put(`/admin/urun-ozellikler/${kod}`, data);
export const deleteUrunOzellikOverride = (kod, tip)  => API.delete(`/admin/urun-ozellikler/${kod}/${tip}`);

// Ürün Tanım Yönetimi
export const getUrunTanimlar    = ()        => API.get('/admin/urun-tanim');
export const matrisOlustur      = (tid,data) => API.post(`/admin/fiyatlar/${tid}/matris-olustur`, data);
export const addUrunTanim       = (data)    => API.post('/admin/urun-tanim', data);
export const deleteUrunTanim    = (kod)     => API.delete(`/admin/urun-tanim/${kod}`);

// Sipariş / Üretim Takip
export const getSiparisler      = ()           => API.get('/siparisler');
export const siparisDurumGuncelle = (id, data) => API.put(`/siparisler/${id}/durum`, data);
export const siparisNotGuncelle = (id, data)   => API.put(`/siparisler/${id}/not`, data);
export const getSiparisGecmis   = (id)         => API.get(`/siparisler/${id}/gecmis`);
export const siparisTerminGuncelle = (id, data) => API.put(`/siparisler/${id}/termin`, data);

// Kategori Bazlı Oranlar
export const getKategoriOranlari   = ()      => API.get('/admin/kategori-oranlari');
export const addKategoriOrani      = (data)  => API.post('/admin/kategori-oranlari', data);
export const deleteKategoriOrani   = (data)  => API.delete('/admin/kategori-oranlari', { data });

// Sistem Ayarları
export const getSistemAyarlari  = ()              => API.get('/admin/ayarlar');
export const updateSistemAyar   = (anahtar, data) => API.put(`/admin/ayarlar/${anahtar}`, data);

// Ürün Bazlı Oran Override
export const getUrunOranlari  = (kod)        => API.get(`/admin/urun-oranlari/${kod}`);
export const addUrunOrani     = (kod, data)  => API.post(`/admin/urun-oranlari/${kod}`, data);
export const deleteUrunOrani  = (kod, data)  => API.delete(`/admin/urun-oranlari/${kod}`, { data });

// Kullanıcılar
export const getKullanicilar  = ()       => API.get('/kullanicilar');
export const addKullanici     = (data)   => API.post('/kullanicilar', data);
export const updateKullanici  = (id,data)=> API.put(`/kullanicilar/${id}`, data);
export const deleteKullanici  = (id)     => API.delete(`/kullanicilar/${id}`);
