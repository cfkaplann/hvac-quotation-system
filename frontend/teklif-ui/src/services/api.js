import axios from 'axios';

const API = axios.create({ baseURL: 'http://localhost:8080/api' });

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
