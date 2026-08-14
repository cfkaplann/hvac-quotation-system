import React, { useState, useEffect, useCallback } from 'react';
import { createTeklif, updateTeklif, getYeniNo, getMusteriler, getKurlar, getUrunler, fiyatHesapla, getOzellikOranlari } from '../services/api';

/* ── Sabitler ─────────────────────────────────────────── */
const EMPTY = {
  teklifNo:'', isAdi:'', musteriId:'', musteriAdi:'',
  teklifTarihi: new Date().toISOString().slice(0,10),
  gecerlilikTarihi: (() => { const d=new Date(); d.setDate(d.getDate()+7); return d.toISOString().slice(0,10); })(),
  teklifiVeren:'',
  paraBirimi:'TL', kdvOrani:20, iskonto:0, notlar:'', kalemler:[]
};
const EMPTY_KALEM = { urunKodu:'', urunAdi:'', adet:1, birim:'Adet', birimFiyat:0, iskonto:0, toplam:0 };

const OLCU_LABEL = {
  GENISLIK:'Genişlik W (mm)', YUKSEKLIK:'Yükseklik H (mm)', UZUNLUK:'Uzunluk L (mm)',
  CAP:'Çap Ø (mm)', KASA_WH:'Kasa Ölçüsü', BOGAZ_WH:'Boğaz Ölçüsü',
  BOGAZ_CAP:'Boğaz Çap (mm)', NETIC_CAP:'Netice Çap (mm)',
  KASA_CAP:'Kasa Çap', SLOT_SAYISI:'Slot Sayısı',
};
const OZELLIK_LABEL = {
  CERCEVE_TIPI:'Çerçeve Tipi', MENFEZ_TIPI:'Tip', DAMPER_TIPI:'Damper',
  RAL:'RAL / Renk', MONTAJ:'Montaj', AKSESUAR_TIPI:'Aksesuar',
};
const KATEGORI_LABEL = {
  MENFEZ:'Menfez', SLOT:'Slot', DIKDORTGEN_DAMPER:'Dikdörtgen Damper',
  DAIRESEL_DAMPER:'Dairesel Damper', KARE_ANEMOSTAD:'Kare Anemostad',
  DAIRESEL_ANEMOSTAD:'Dairesel Anemostad', KARE_SWIRL:'Kare Swirl',
  DAIRESEL_SWIRL:'Dairesel Swirl', PANJUR:'Panjur', KAPAK:'Kapak', KUTU:'Kutu',
};
const STRING_OLCULAR = new Set(['KASA_WH','BOGAZ_WH','KASA_CAP','BOGAZ_CAP','NETIC_CAP']);
const INT_OLCULAR    = new Set(['SLOT_SAYISI']);

/* ── Yardımcı ─────────────────────────────────────────── */
const pbSembol = pb => pb==='EUR'?'€':pb==='USD'?'$':'₺';

export default function TeklifForm({ teklif, onSave, onClose, kullanici }) {
  const [form, setForm]         = useState(EMPTY);
  const [musteriler, setMust]   = useState([]);
  const [kurlar, setKurlar]     = useState({ EUR:42, USD:38 });
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState('');

  /* Ürün seçim state */
  const [urunler, setUrunler]         = useState([]);
  const [kategoriler, setKategoriler] = useState([]);
  const [secKategori, setSecKategori] = useState('');
  const [secUrun, setSecUrun]         = useState(null);
  const [olcular, setOlcular]         = useState({});      // sayısal
  const [strOlcular, setStrOlcular]   = useState({});      // string combo
  const [ozellikler, setOzellikler]   = useState({});
  const [miktar, setMiktar]           = useState(1);
  const [degisti, setDegisti]         = useState(false);
  const [fiyatHata, setFiyatHata]     = useState('');
  const [hesaplaniyor, setHesaplaniyor] = useState(false);
  const [motorFiyat, setMotorFiyat] = useState('');
  const [kalemOzellikAc, setKalemOzellikAc] = useState(null);
  const [kalemDuzenle, setKalemDuzenle] = useState(null); // index
  const [tumOzellikSecenekler, setTumOzellikSecenekler] = useState([]);
  const [ozellikGruplari2, setOzellikGruplari2] = useState({}); // hangi kalem indexi açık
  const [ralKod, setRalKod]           = useState(''); // Boyalı seçilince RAL kodu

  useEffect(() => {
    getMusteriler().then(r => setMust(r.data)).catch(()=>{});
    getOzellikOranlari().then(r => {
      const secenekler = [...new Set(r.data.map(o => o.optionName))].filter(Boolean);
      setTumOzellikSecenekler(secenekler);
      // Tiplere göre grupla
      const gruplar = {};
      r.data.forEach(o => {
        if (!o.featureType || !o.optionName) return;
        if (!gruplar[o.featureType]) gruplar[o.featureType] = [];
        if (!gruplar[o.featureType].includes(o.optionName))
          gruplar[o.featureType].push(o.optionName);
      });
      setOzellikGruplari2(gruplar);
    }).catch(()=>{});
    getKurlar().then(r => setKurlar(r.data)).catch(()=>{});
    getUrunler().then(r => {
      setUrunler(r.data);
      setKategoriler([...new Set(r.data.map(u=>u.kategori))]);
    }).catch(()=>{});
    if (teklif) {
      setForm({...EMPTY,...teklif, kalemler:teklif.kalemler||[]});
    } else {
      const adSoyad = (kullanici?.adSoyad && kullanici.adSoyad.trim())
        ? kullanici.adSoyad.trim()
        : (kullanici?.kullaniciAdi || '');
      getYeniNo().then(r => setForm(f=>({
        ...f,
        teklifNo: r.data.teklifNo,
        teklifiVeren: f.teklifiVeren || adSoyad  // düzenleme modunda mevcut değeri koru
      }))).catch(()=>{});
    }
  }, [teklif]);

  /* Ürün değişince formu sıfırla + ilk değerleri otomatik seç */
  const urunSec = useCallback((u) => {
    setSecUrun(u);
    setOlcular({}); setStrOlcular({}); setFiyatHata(''); setMiktar(1);
    setRalKod('');
    if (!u) { setOzellikler({}); return; }
    // Her özellik için ilk seçeneği otomatik set et
    const ilkler = {};
    for (const [tip, sec] of Object.entries(u.ozellikler||{})) {
      if (!sec||sec.length===0) continue;
      const isMulti = u.multiSelectOzellikler?.includes(tip);
      if (tip==='AKSESUAR_TIPI') continue; // aksesuar opsiyonel, boş kalsın
      ilkler[tip] = isMulti ? [sec[0]] : sec[0];
    }
    setOzellikler(ilkler);
  }, []);

  const katDegisti = (kat) => {
    setSecKategori(kat);
    setSecUrun(null);
    setOlcular({}); setStrOlcular({}); setOzellikler({});
    setFiyatHata('');
  };

  /* Ölçü setters */
  const setO  = (tip,val) => setOlcular(o=>({...o,[tip]:val}));
  const setS  = (tip,val) => {
    if (tip==='KASA_WH'||tip==='KASA_CAP')
      setStrOlcular(o=>({...o,[tip]:val, BOGAZ_WH:'', BOGAZ_CAP:''}));
    else setStrOlcular(o=>({...o,[tip]:val}));
  };
  const setOz = (tip,val,multi) => {
    if (multi) {
      setOzellikler(o=>{
        const m=Array.isArray(o[tip])?o[tip]:[];
        return {...o,[tip]:m.includes(val)?m.filter(x=>x!==val):[...m,val]};
      });
    } else setOzellikler(o=>({...o,[tip]:val}));
  };

  /* Boğaz filtresi */
  const getBogazSec = (olcuTip) => {
    if (!secUrun) return [];
    const kasa = strOlcular['KASA_WH']||strOlcular['KASA_CAP'];
    const fm = secUrun.bogazFiltreMap;
    if (fm&&kasa&&fm[kasa]) return fm[kasa];
    return secUrun.izinliOlcuDegerleri?.[olcuTip]||[];
  };

  /* Validasyon */
  const validateUrun = () => {
    if (!secUrun) return ['Ürün seçiniz.'];
    const errs = [];
    for (const olcu of secUrun.zorunluOlcular) {
      if (STRING_OLCULAR.has(olcu)) {
        if (!strOlcular[olcu]) errs.push(`"${OLCU_LABEL[olcu]||olcu}" zorunludur.`);
      } else {
        const v = olcular[olcu];
        if (!v||isNaN(parseFloat(v))||parseFloat(v)<=0)
          errs.push(`"${OLCU_LABEL[olcu]||olcu}" zorunludur.`);
      }
    }
    for (const [tip,sec] of Object.entries(secUrun.ozellikler)) {
      if (tip==='AKSESUAR_TIPI'||sec.length===0) continue;
      if (tip==='MENFEZ_TIPI' && secUrun.kategori==='PANJUR') continue;
      const s=ozellikler[tip];
      if (!s||(Array.isArray(s)?s.length===0:s===''))
        errs.push(`"${OZELLIK_LABEL[tip]||tip}" zorunludur.`);
    }
    return errs;
  };

  /* Ürün kaleme ekle */
  const urunEkle = async () => {
    // RAL Boyalı ise kod zorunlu
    // RAL kodu opsiyonel — zorunlu değil
    const errs = validateUrun();
    if (errs.length>0) { setFiyatHata(errs[0]); return; }
    setHesaplaniyor(true); setFiyatHata('');
    try {
      const oNum={};
      for (const [k,v] of Object.entries(olcular)) {
        const n=parseFloat(v); if(!isNaN(n)) oNum[k]=n;
      }
      // RAL Boyalı değerini birleştir
      const ozelliklerGonder = {...ozellikler};
      if (ralKod.trim()) {
        // RAL kodu girilmişse her zaman Boyalı
        ozelliklerGonder['RAL'] = `Boyalı - ${ralKod.trim()}`;
      } else if (ozellikler['RAL']==='Boyalı') {
        ozelliklerGonder['RAL'] = 'Boyalı';
      }

      // Servo motor fiyatı
      const motorFiyatlari = {};
      const aksesuarSec = ozelliklerGonder['AKSESUAR_TIPI'];
      const aksesuarList = Array.isArray(aksesuarSec) ? aksesuarSec : (aksesuarSec ? [aksesuarSec] : []);
      const servoVar = aksesuarList.some(a => a && a.includes('Servo Motor'));
      if (servoVar && motorFiyat) {
        const mf = parseFloat(motorFiyat) || 0;
        // Girilen fiyatı TL'ye çevir
        const pb = form.paraBirimi;
        const mfTL = pb==='EUR' ? mf*(kurlar.EUR||42) : pb==='USD' ? mf*(kurlar.USD||38) : mf;
        motorFiyatlari['Servo Motor'] = mfTL;
      }

      const r = await fiyatHesapla(secUrun.kod, {
        olcular:oNum, stringOlcular:strOlcular, ozellikler:ozelliklerGonder,
        motorFiyatlari: Object.keys(motorFiyatlari).length > 0 ? motorFiyatlari : undefined
      });
      const tlFiyat = r.data.toplam;
      // birimFiyat HER ZAMAN TL olarak sakla, gösterimde dövize çevir
      const fiyat = tlFiyat;

      /* Ürün adı oluştur - Swing gibi */
      const olcuEki = (() => {
        const kasa=strOlcular['KASA_WH'], bogaz=strOlcular['BOGAZ_WH'];
        const kasaCap=strOlcular['KASA_CAP'];
        if (kasa&&bogaz) return ` (${kasa}/${bogaz})`;
        if (kasa) return ` (${kasa})`;
        if (kasaCap) return ` (${kasaCap})`;
        const w=olcular['GENISLIK'],h=olcular['YUKSEKLIK'];
        const cap=olcular['CAP']||olcular['BOGAZ_CAP']||strOlcular['NETIC_CAP']||strOlcular['BOGAZ_CAP'];
        const uzun=olcular['UZUNLUK'];
        if (w&&h&&uzun) return ` ${w}x${h} ${uzun} mm`;
        if (w&&h&&cap) return ` ${w}x${h} Ø${cap}`;
        if (w&&h) return ` ${w}x${h}`;
        if (cap&&uzun) return ` Ø${cap} ${uzun} mm`;
        if (cap) return ` Ø${cap}`;
        if (uzun) return ` ${uzun} mm`;
        return '';
      })();

      const ozellikSuffix = Object.entries(ozelliklerGonder)
        .filter(([,v])=>v&&(Array.isArray(v)?v.length>0:v!==''))
        .map(([,v])=>Array.isArray(v)?v.join(', '):v)
        .join(' / ');

      // Menfez tipi ürün adının yanına ekle
      const menfezTipi = ozelliklerGonder['MENFEZ_TIPI'];
      const menfezEki = menfezTipi && !Array.isArray(menfezTipi) ? ` ${menfezTipi}` : '';

      let ad = secUrun.ad + menfezEki + olcuEki;
      if (ozellikSuffix) ad += ' — ' + ozellikSuffix;

      /* Slot prefix */
      if (secUrun.kod.startsWith('SLT') && olcular['SLOT_SAYISI'])
        ad = olcular['SLOT_SAYISI'] + ' Yarıklı ' + ad;

      /* ── Kutu özel isimlendirme ── */
      if (secUrun.kod === 'BOX_WH') {
        // Menfez Kutusu: W+1 x H+1
        const w = parseInt(olcular['GENISLIK']||0);
        const h = parseInt(olcular['YUKSEKLIK']||0);
        if (w && h) ad = `Menfez Kutusu ${w+10}x${h+10}`;
        else ad = 'Menfez Kutusu';
      } else if (secUrun.kod === 'BOX_LS') {
        const slot = parseInt(olcular['SLOT_SAYISI'] || 0);
        const uzun = parseInt(olcular['UZUNLUK'] || 0);
        const yuk  = parseInt(olcular['YUKSEKLIK'] || 0);
        // En değerleri slot sayısına göre (cm)
        const enMap = {1:55, 2:95, 3:125, 4:160, 5:195, 6:230};
        const boy = uzun ? uzun + 10 : 0; // uzunluk + 1 cm (mm)
        const en  = enMap[slot] || '';
        if (slot && uzun && yuk) {
          ad = `${slot} Yarıklı Slot Difüzör Kutusu — Boy: ${boy} mm / En: ${en} cm / Yükseklik: ${yuk} mm`;
        } else if (slot && uzun) {
          ad = `${slot} Yarıklı Slot Difüzör Kutusu — Boy: ${boy} mm${en ? ' / En: ' + en + ' cm' : ''}`;
        } else {
          ad = 'Slot Difüzör Kutusu';
        }
      } else if (secUrun.kod === 'BOX_STR') {
        // Anemostat Kutusu: boğaz, dış (boğaz+150), kutu (boğaz+90)
        const bogaz = strOlcular['KASA_WH'] || '';
        if (bogaz) {
          const [bw, bh] = bogaz.split('x').map(Number);
          if (bw && bh) {
            const dis  = `${bw+150}x${bh+150}`;
            const kutu = `${bw+90}x${bh+90}`;
            ad = `${bogaz} Boğaz, ${dis} Dış ve ${kutu} Ölçülü Anemostat Kutusu`;
          } else ad = 'Anemostat Kutusu';
        } else ad = 'Anemostat Kutusu';
      }

      const yeniKalem = {
        urunKodu: secUrun.kod,
        urunAdi: ad,
        adet: miktar,
        birim: 'Adet',
        birimFiyat: Math.round(fiyat*100)/100,
        iskonto: 0,
      };

      if (kalemDuzenle !== null) {
        // Güncelleme modu
        const arr = [...form.kalemler];
        const km = {...arr[kalemDuzenle], ...yeniKalem};
        km.toplam = (km.birimFiyat||0) * (km.adet||1);
        arr[kalemDuzenle] = km;
        setForm(f => ({...f, kalemler: arr}));
        setKalemDuzenle(null);
      } else {
        // Yeni kalem ekleme
        setForm(f=>({...f, kalemler:[...f.kalemler, yeniKalem]}));
      }
      setDegisti(true);
      /* Sadece ölçüler ve miktar sıfırla, kategori/ürün/özellikler kalsın */
      setOlcular({}); setStrOlcular({}); setMiktar(1);
    } catch(e) {
      setFiyatHata(e.response?.data?.hata||'Fiyat hesaplanamadı.');
    } finally { setHesaplaniyor(false); }
  };

  /* Kur çevirme */
  const tleCevir = (fiyat) => {
    const pb=form.paraBirimi;
    if (pb==='EUR') return fiyat/(kurlar.EUR||42);
    if (pb==='USD') return fiyat/(kurlar.USD||38);
    return fiyat;
  };
  const fmt = (v, sym='') => { const n = (typeof v === 'number' ? v : 0); return n.toLocaleString('tr-TR',{minimumFractionDigits:2,maximumFractionDigits:2}) + (sym ? ' '+sym : ''); };

  /* Kalem işlemleri */
  const set       = (k,v) => { setForm(f=>({...f,[k]:v})); setDegisti(true); };
  const kalemSet  = (i,k,v) => {
    const arr=[...form.kalemler];
    arr[i]={...arr[i],[k]:v};
    const km=arr[i];
    const net=(km.birimFiyat||0)*(km.adet||1);
    arr[i].toplam=net-net*(km.iskonto||0)/100;
    setForm(f=>({...f,kalemler:arr}));
    setDegisti(true);
  };
  const removeKalem = (i) => { setForm(f=>({...f,kalemler:f.kalemler.filter((_,idx)=>idx!==i)})); setDegisti(true); };
  const insertKalem = (i) => {
    // i'den sonraya boş kalem ekle — kalemDuzenle'yi o index'e set et
    const arr = [...form.kalemler];
    arr.splice(i+1, 0, {urunKodu:'',urunAdi:'',adet:1,birim:'Adet',birimFiyat:0,iskonto:0,toplam:0});
    setForm(f=>({...f,kalemler:arr}));
    setDegisti(true);
  };
  const moveKalem = (i, yon) => {
    const arr = [...form.kalemler];
    const j = i + yon;
    if (j < 0 || j >= arr.length) return;
    [arr[i], arr[j]] = [arr[j], arr[i]];
    setForm(f => ({...f, kalemler: arr}));
    setDegisti(true);
  };
  const addManuel   = () => { setForm(f=>({...f,kalemler:[...f.kalemler,{...EMPTY_KALEM}]})); setDegisti(true); };

  /* Toplam hesap - birimFiyat TL, gösterimde kura çevrilir */
  const kalemToplam = (k) => {
    const net = (k.birimFiyat||0) * (k.adet||1);
    return net;
  };
  const kalemToplamGoster = (k) => {
    return tleCevir(kalemToplam(k));
  };
  // TL bazlı toplamlar (kayıt için)
  const araToplam   = form.kalemler.reduce((s,k)=>s+kalemToplam(k),0);
  const iskontoTutari = araToplam * ((form.iskonto||0)/100);
  const iskontoSonrasi = araToplam - iskontoTutari;
  const kdvTutari   = iskontoSonrasi*(form.kdvOrani/100);
  const genelToplam = iskontoSonrasi+kdvTutari;
  // Gösterim için dövize çevrilmiş toplamlar
  const araToplam_g   = tleCevir(araToplam);
  const iskontoTutari_g = tleCevir(iskontoTutari);
  const kdvTutari_g   = tleCevir(kdvTutari);
  const genelToplam_g = tleCevir(genelToplam);

  const handleSubmit = async () => {
    if (!form.isAdi.trim()) { setError('İş adı zorunludur.'); return; }
    setLoading(true); setError('');
    try {
      // Toplamları hesaplayıp forma ekle
      const payload = {
        ...form,
        musteriId: form.musteriId ? parseInt(form.musteriId) : null,
        araToplam: iskontoSonrasi,
        kdvTutari,
        genelToplam,
        kalemler: form.kalemler.map((k,i) => ({
          ...k,
          siraNo: i+1,
          toplam: kalemToplam(k)
        }))
      };
      if (teklif?.id) await updateTeklif(teklif.id, payload);
      else await createTeklif(payload);
      setDegisti(false);
      onSave();
    } catch(e) {
      setError(e.response?.data?.hata || e.message || 'Bir hata oluştu.');
    } finally { setLoading(false); }
  };

  const katUrunler = urunler.filter(u=>u.kategori===secKategori);
  const sym = pbSembol(form.paraBirimi);

  /* Ölçü input render */
  const renderOlcu = (olcu) => {
    const isStr = STRING_OLCULAR.has(olcu);
    const isInt = INT_OLCULAR.has(olcu);
    const isBogaz = olcu==='BOGAZ_WH'||olcu==='BOGAZ_CAP';
    const combo = isBogaz ? getBogazSec(olcu) : secUrun?.izinliOlcuDegerleri?.[olcu]||[];
    const val = isStr?(strOlcular[olcu]||''):(olcular[olcu]||'');
    const dolu = val!=='';
    const bc = dolu?'var(--green)':'var(--border)';

    if (isStr&&combo.length>0) return (
      <select className="select" style={{borderColor:bc,fontSize:12,padding:'6px 8px'}} value={val} onChange={e=>setS(olcu,e.target.value)}>
        <option value="">— Seç —</option>
        {combo.map(s=><option key={s} value={s}>{s}</option>)}
      </select>
    );
    return (
      <input className="input" type="number" step={isInt?'1':'any'} min={isInt?'1':'0'}
        placeholder={isInt?'Sayı':'mm'} value={val} style={{borderColor:bc,fontSize:12,padding:'6px 8px'}}
        onChange={e=>isStr?setS(olcu,e.target.value):setO(olcu,e.target.value)} />
    );
  };

  /* Özellik input render */
  const renderOzellik = (tip, secenekler) => {
    const isMulti = secUrun?.multiSelectOzellikler?.includes(tip);
    const isOps   = tip==='AKSESUAR_TIPI' || (tip==='MENFEZ_TIPI' && secUrun?.kategori==='PANJUR');
    const s = ozellikler[tip];
    const dolu = isMulti?(Array.isArray(s)&&s.length>0):(s&&s!=='');
    const bc = isOps?(dolu?'var(--green)':'var(--border)'):(dolu?'var(--green)':'var(--red)');

    if (isMulti) return (
      <div style={{border:`1px solid ${bc}`,borderRadius:6,padding:'6px 8px',background:'var(--surface)'}}>
        {secenekler.map(sv=>(
          <label key={sv} style={{display:'flex',alignItems:'center',gap:6,cursor:'pointer',
            textTransform:'none',letterSpacing:0,fontWeight:400,color:'var(--text)',fontSize:12,padding:'2px 0'}}>
            <input type="checkbox" checked={Array.isArray(s)&&s.includes(sv)} onChange={()=>setOz(tip,sv,true)}/>
            {sv}
          </label>
        ))}
      </div>
    );
    return (
      <select className="select" style={{borderColor:bc,fontSize:12,padding:'6px 8px'}} value={s||''} onChange={e=>setOz(tip,e.target.value,false)}>
        <option value="">— Seç —</option>
        {secenekler.map(sv=><option key={sv} value={sv}>{sv}</option>)}
      </select>
    );
  };

  /* Özellikleri 3 gruba ayır - Swing gibi */
  const ozellikGruplari = secUrun ? (() => {
    const olcuGrup=[]; const teknikGrup=[]; const ralGrup=[];
    for (const [tip,sec] of Object.entries(secUrun.ozellikler)) {
      if (tip==='RAL'||tip==='AKSESUAR_TIPI') ralGrup.push([tip,sec]);
      else if (tip==='CERCEVE_TIPI'||tip==='DAMPER_TIPI'||tip==='MONTAJ'||tip==='MENFEZ_TIPI') teknikGrup.push([tip,sec]);
      else teknikGrup.push([tip,sec]);
    }
    return { olcuGrup, teknikGrup, ralGrup };
  })() : { olcuGrup:[], teknikGrup:[], ralGrup:[] };

  const colStyle = {
    background:'var(--surface2)', border:'1px solid var(--border)',
    borderRadius:8, padding:'10px 12px', display:'flex', flexDirection:'column', gap:8
  };
  const colHeadStyle = {
    fontSize:10, fontWeight:700, letterSpacing:'.08em',
    textTransform:'uppercase', color:'var(--muted)', marginBottom:4
  };

  const handleClose = () => {
    if (degisti) {
      if (!window.confirm('Kaydedilmemiş değişiklikler var. Çıkmak istediğinize emin misiniz?')) return;
    }
    onClose();
  };

  return (
    <div className="modal-overlay">
      <div style={{
        background:'var(--surface)', border:'1px solid var(--border)',
        borderRadius:14, width:'98%', maxWidth:1200,
        maxHeight:'96vh', display:'flex', flexDirection:'column',
        animation:'slideUp .2s ease'
      }}>
        {/* HEADER */}
        <div className="modal-header">
          <h2 style={{fontFamily:'var(--font-head)'}}>
            {teklif?`Düzenle — ${teklif.teklifNo}`:'Yeni Teklif'}
          </h2>
          <button className="close-btn" onClick={handleClose}>×</button>
        </div>

        <div style={{flex:1, overflowY:'auto', padding:'16px 20px', display:'flex', flexDirection:'column', gap:14}}>

          {/* ── BÖLÜM 1: Teklif bilgileri ── */}
          <div style={{background:'var(--surface2)',borderRadius:8,padding:'12px 16px'}}>
            <div style={colHeadStyle}>Teklif Bilgileri</div>
            <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr 1fr 1fr 0.7fr', gap:10}}>
              <div>
                <label>Teklif No</label>
                <input className="input" value={form.teklifNo} readOnly style={{opacity:.6,fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>İş Adı *</label>
                <input className="input" placeholder="Proje / iş adı" value={form.isAdi}
                  onChange={e=>set('isAdi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>Müşteri</label>
                <select className="select" value={form.musteriId||''} onChange={e=>set('musteriId', e.target.value ? parseInt(e.target.value) : null)} style={{fontSize:12,padding:'6px 8px',marginBottom:4}}>
                  <option value="">— Seç veya manuel gir —</option>
                  {musteriler.map(m=><option key={m.id} value={m.id}>{m.firmaAdi}</option>)}
                </select>
                {!form.musteriId && (
                  <input className="input" placeholder="Manuel müşteri adı..."
                    value={form.musteriAdi||''}
                    onChange={e=>set('musteriAdi',e.target.value)}
                    style={{fontSize:12,padding:'6px 8px'}}/>
                )}
              </div>
              <div>
                <label>Teklifi Veren</label>
                <input className="input" placeholder="Ad Soyad" value={form.teklifiVeren||''}
                  onChange={e=>set('teklifiVeren',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>Para Birimi</label>
                <select className="select" value={form.paraBirimi} onChange={e=>set('paraBirimi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="TL">TL ₺</option>
                  <option value="EUR">EUR €</option>
                  <option value="USD">USD $</option>
                </select>
              </div>
              <div>
                <label>KDV %</label>
                <input className="input" type="number" value={form.kdvOrani}
                  onChange={e=>set('kdvOrani',parseFloat(e.target.value)||0)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
            </div>
            <div style={{display:'grid',gridTemplateColumns:'1fr 1fr 1fr 3fr',gap:10,marginTop:8}}>
              <div>
                <label>Teklif Tarihi *</label>
                <input className="input" type="date" value={form.teklifTarihi}
                  onChange={e=>{
                    const d=new Date(e.target.value); d.setDate(d.getDate()+7);
                    set('teklifTarihi',e.target.value);
                    set('gecerlilikTarihi',d.toISOString().slice(0,10));
                  }} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div>
                <label>Geçerlilik Tarihi</label>
                <input className="input" type="date" value={form.gecerlilikTarihi||''}
                  onChange={e=>set('gecerlilikTarihi',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
              <div></div>
              <div>
                <label>Notlar</label>
                <input className="input" placeholder="İsteğe bağlı notlar..." value={form.notlar||''}
                  onChange={e=>set('notlar',e.target.value)} style={{fontSize:12,padding:'6px 8px'}}/>
              </div>
            </div>
          </div>

          {/* ── BÖLÜM 2: Ürün Seçim (Swing Toolbar gibi) ── */}
          <div style={{background:'var(--surface2)',borderRadius:8,padding:'10px 16px'}}>
            <div style={colHeadStyle}>
              {kalemDuzenle !== null
                ? `✏️ Kalem ${kalemDuzenle + 1} Düzenleniyor — ${form.kalemler[kalemDuzenle]?.urunAdi?.slice(0,40) || ''}`
                : 'Ürün Ekle'}
            </div>

            {/* Toolbar: Kategori → Ürün → Para Birimi göstergesi */}
            <div style={{display:'flex',gap:10,alignItems:'flex-end',flexWrap:'wrap',marginBottom:10}}>
              <div style={{minWidth:200}}>
                <label>Kategori</label>
                <select className="select" value={secKategori} onChange={e=>katDegisti(e.target.value)} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="">— Kategori Seçiniz —</option>
                  {kategoriler.map(k=><option key={k} value={k}>{KATEGORI_LABEL[k]||k}</option>)}
                </select>
              </div>
              <div style={{minWidth:260}}>
                <label>Ürün</label>
                <select className="select" value={secUrun?.kod||''} onChange={e=>urunSec(urunler.find(u=>u.kod===e.target.value)||null)} disabled={!secKategori} style={{fontSize:12,padding:'6px 8px'}}>
                  <option value="">— Ürün Seçiniz —</option>
                  {katUrunler.map(u=><option key={u.kod} value={u.kod}>{u.ad}</option>)}
                </select>
              </div>
              <div style={{display:'flex',alignItems:'center',gap:6,padding:'6px 10px',
                background:'var(--surface)',borderRadius:6,border:'1px solid var(--border)',fontSize:12,color:'var(--muted)'}}>
                Kur: €{(kurlar.EUR||42).toFixed(2)} / ${(kurlar.USD||38).toFixed(2)}
              </div>
            </div>

            {/* 3 Kolon form - Swing gibi: Ölçüler | Teknik | RAL+Aksesuar */}
            {secUrun && (
              <div style={{display:'grid', gridTemplateColumns:'1fr 1fr 1fr', gap:10}}>

                {/* KOLON 1: ÖLÇÜLER + MİKTAR */}
                <div style={colStyle}>
                  <div style={colHeadStyle}>Ölçüler <span style={{color:'var(--red)'}}>*</span></div>
                  {secUrun.zorunluOlcular.map(olcu=>(
                    <div key={olcu}>
                      <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                        {(secUrun?.kod==='BOX_STR' && olcu==='KASA_WH') ? 'Boğaz Ölçüsü' : (OLCU_LABEL[olcu] || olcu)} <span style={{color:'var(--red)'}}>*</span>
                      </label>
                      {renderOlcu(olcu)}
                    </div>
                  ))}
                  <div>
                    <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                      Miktar *
                    </label>
                    <input className="input" type="number" min="1" value={miktar}
                      onChange={e=>setMiktar(parseInt(e.target.value)||1)}
                      style={{fontSize:12,padding:'6px 8px',borderColor:miktar>0?'var(--green)':'var(--border)'}}/>
                  </div>
                </div>

                {/* KOLON 2: TEKNİK (Çerçeve, Damper, Montaj, Tip) */}
                <div style={colStyle}>
                  <div style={colHeadStyle}>Teknik Özellikler</div>
                  {ozellikGruplari.teknikGrup.length===0
                    ? <div style={{color:'var(--muted)',fontSize:12}}>Bu ürün için teknik özellik yok.</div>
                    : ozellikGruplari.teknikGrup.map(([tip,sec])=>(
                    <div key={tip}>
                      <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                        {OZELLIK_LABEL[tip]||tip}
                        {tip!=='AKSESUAR_TIPI'&&!(tip==='MENFEZ_TIPI'&&secUrun?.kategori==='PANJUR')&&<span style={{color:'var(--red)',marginLeft:2}}>*</span>}
                      </label>
                      {renderOzellik(tip,sec)}
                      {/* RAL kodu alanı - her zaman görünür */}
                      {tip==='RAL' && (
                        <div style={{marginTop:6}}>
                          <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:3}}>
                            RAL Kodu <span style={{color:'var(--muted)',fontSize:10}}>(opsiyonel)</span>
                          </label>
                          <input className="input"
                            placeholder="Örn: RAL 9010"
                            style={{fontSize:12,padding:'5px 8px'}}
                            value={ralKod}
                            onChange={e=>{
                              setRalKod(e.target.value);
                              if(e.target.value.trim()) setOzellikler(o=>({...o,RAL:'Boyalı'}));
                            }}/>
                        </div>
                      )}
                    </div>
                  ))}
                </div>

                {/* KOLON 3: RAL + AKSESUAR */}
                <div style={colStyle}>
                  <div style={colHeadStyle}>RAL / Aksesuar</div>
                  {ozellikGruplari.ralGrup.length===0
                    ? <div style={{color:'var(--muted)',fontSize:12}}>Bu ürün için RAL/Aksesuar yok.</div>
                    : ozellikGruplari.ralGrup.map(([tip,sec])=>(
                    <div key={tip}>
                      <label style={{textTransform:'none',letterSpacing:0,fontWeight:500,color:'var(--text)',fontSize:11}}>
                        {OZELLIK_LABEL[tip]||tip}
                        {tip!=='AKSESUAR_TIPI'&&!(tip==='MENFEZ_TIPI'&&secUrun?.kategori==='PANJUR')&&<span style={{color:'var(--red)',marginLeft:2}}>*</span>}
                        {tip==='AKSESUAR_TIPI'&&<span style={{color:'var(--muted)',marginLeft:3,fontSize:10}}>(opsiyonel)</span>}
                      </label>
                      {renderOzellik(tip,sec)}
                      {/* Servo Motor seçilince motor fiyatı alanı */}
                      {tip==='AKSESUAR_TIPI' && (() => {
                        const ak = ozellikler['AKSESUAR_TIPI'];
                        const akList = Array.isArray(ak) ? ak : (ak ? [ak] : []);
                        return akList.some(a => a && a.includes('Servo Motor')) && (
                          <div style={{marginTop:6}}>
                            <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:3}}>
                              Motor Fiyatı ({sym}) <span style={{color:'var(--red)',marginLeft:2}}>*</span>
                            </label>
                            <input className="input" type="number" min="0"
                              placeholder="Motor fiyatı girin..."
                              style={{fontSize:12,padding:'5px 8px'}}
                              value={motorFiyat}
                              onChange={e=>setMotorFiyat(e.target.value)}/>
                          </div>
                        );
                      })()}
                      {/* RAL kodu alanı - her zaman görünür */}
                      {tip==='RAL' && (
                        <div style={{marginTop:6}}>
                          <label style={{fontSize:11,color:'var(--muted)',display:'block',marginBottom:3}}>
                            RAL Kodu <span style={{color:'var(--muted)',fontSize:10}}>(opsiyonel)</span>
                          </label>
                          <input className="input"
                            placeholder="Örn: RAL 9010"
                            style={{fontSize:12,padding:'5px 8px'}}
                            value={ralKod}
                            onChange={e=>{
                              setRalKod(e.target.value);
                              if(e.target.value.trim()) setOzellikler(o=>({...o,RAL:'Boyalı'}));
                            }}/>
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Hata + Ekle butonu */}
            {secUrun && (
              <div style={{display:'flex',alignItems:'center',gap:10,marginTop:10}}>
                <button className="btn btn-primary" onClick={urunEkle} disabled={hesaplaniyor}
                  style={{padding:'8px 20px'}}>
                  {hesaplaniyor ? 'Hesaplanıyor...' : kalemDuzenle !== null ? '✓ Kalemi Güncelle' : '+ Kaleme Ekle'}
                </button>
                {kalemDuzenle !== null && (
                  <button className="btn btn-secondary btn-sm" onClick={() => setKalemDuzenle(null)}>
                    İptal
                  </button>
                )}
                {fiyatHata && (
                  <span style={{color:'var(--red)',fontSize:12}}>⚠️ {fiyatHata}</span>
                )}
              </div>
            )}
          </div>

          {/* ── BÖLÜM 3: Kalem Tablosu (Swing TeklifTablePanel gibi) ── */}
          <div>
            <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:6}}>
              <span style={{fontFamily:'var(--font-head)',fontWeight:700,fontSize:14}}>
                Kalemler ({form.kalemler.length})
              </span>
              <button className="btn btn-secondary btn-sm" onClick={addManuel}>+ Manuel Ekle</button>
            </div>

            <div style={{border:'1px solid var(--border)',borderRadius:8,overflow:'hidden'}}>
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th style={{width:36}}>#</th>
                      <th>Ürün Adı</th>
                      <th style={{width:90}}>Adet</th>
                      <th style={{width:100}}>Birim</th>
                      <th style={{width:110}}>Birim Fiyat ({sym})</th>
                      <th style={{width:110,textAlign:'right'}}>Toplam ({sym})</th>
                      <th style={{width:32}}></th>
                    </tr>
                  </thead>
                  <tbody>
                    {form.kalemler.length===0 && (
                      <tr><td colSpan={8} style={{textAlign:'center',color:'var(--muted)',padding:20,fontSize:13}}>
                        Henüz kalem yok — yukarıdan ürün seçip ekleyin
                      </td></tr>
                    )}
                    {form.kalemler.map((k,i)=>(
                      <React.Fragment key={i}>
                      {i > 0 && (
                        <tr>
                          <td colSpan={6} style={{padding:'0 0 0 36px'}}>
                            <button onClick={()=>insertKalem(i-1)}
                              style={{background:'none',border:'1px dashed var(--border)',borderRadius:4,
                                color:'var(--muted)',cursor:'pointer',fontSize:11,padding:'1px 10px',
                                width:'100%',textAlign:'left',transition:'all .15s'}}
                              onMouseEnter={e=>e.currentTarget.style.borderColor='var(--accent)'}
                              onMouseLeave={e=>e.currentTarget.style.borderColor='var(--border)'}>
                              + Araya Ekle
                            </button>
                          </td>
                        </tr>
                      )}
                      <tr>
                        <td style={{color:'var(--muted)',fontSize:12}}>{i+1}</td>
                        <td>
                          <div style={{display:'flex',alignItems:'center',gap:4}}>
                            <input className="input" style={{padding:'4px 6px',fontSize:12,flex:1}} value={k.urunAdi}
                              onChange={e=>kalemSet(i,'urunAdi',e.target.value)}/>
                            {k.urunKodu && (
                              <button onClick={()=>{ setKalemDuzenle(i); window.scrollTo({top:0,behavior:'smooth'}); }}
                                title="Ürünü düzenle"
                                style={{padding:'3px 8px',fontSize:11,cursor:'pointer',flexShrink:0,
                                  background:'var(--accent)20',border:'1px solid var(--accent)40',
                                  borderRadius:4,color:'var(--accent)'}}>✏️</button>
                            )}
                          </div>
                          <button onClick={()=>setKalemOzellikAc(kalemOzellikAc===i?null:i)}
                            style={{marginTop:3,fontSize:11,padding:'2px 8px',
                              background: kalemOzellikAc===i ? 'var(--accent)' : 'transparent',
                              border:'1px solid var(--border)',borderRadius:4,
                              color: kalemOzellikAc===i ? '#000' : 'var(--muted)',cursor:'pointer'}}>
                            {kalemOzellikAc===i ? '▲ Özellik Kapat' : '▼ Özellik Ekle'}
                          </button>
                          {kalemOzellikAc===i && (
                            <div style={{marginTop:4,padding:'8px 10px',background:'var(--surface2)',
                              borderRadius:6,border:'1px solid var(--border)'}}>
                              <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:6}}>
                                <span style={{fontSize:11,fontWeight:700,color:'var(--muted)'}}>AÇIKLAMA / ÖZELLİK</span>
                                <button onClick={()=>setKalemOzellikAc(null)}
                                  style={{background:'none',border:'none',color:'var(--muted)',cursor:'pointer',fontSize:14}}>✕</button>
                              </div>
                              <textarea className="input" rows={2}
                                placeholder="Özellik, not veya açıklama girin..."
                                style={{fontSize:12,padding:'5px 8px',resize:'vertical',width:'100%'}}
                                value={k.aciklama||''}
                                onChange={e=>kalemSet(i,'aciklama',e.target.value)}/>
                              <div style={{marginTop:8}}>
                                {Object.entries(ozellikGruplari2).map(([tip, secenekler])=>(
                                  <div key={tip} style={{marginBottom:8}}>
                                    <div style={{fontSize:10,fontWeight:700,color:'var(--muted)',
                                      textTransform:'uppercase',letterSpacing:'.06em',marginBottom:4}}>
                                      {tip.replace(/_/g,' ')}
                                      <span style={{fontWeight:400,marginLeft:4,fontSize:9}}>(opsiyonel)</span>
                                    </div>
                                    <div style={{display:'flex',flexWrap:'wrap',gap:4}}>
                                      {secenekler.map(opt=>{
                                        const secili = (k.aciklama||'').split(', ').map(s=>s.trim()).includes(opt);
                                        return (
                                          <button key={opt}
                                            onClick={()=>{
                                              const mevcut = (k.aciklama||'').split(', ').map(s=>s.trim()).filter(Boolean);
                                              const yeni = secili ? mevcut.filter(s=>s!==opt) : [...mevcut, opt];
                                              kalemSet(i,'aciklama',yeni.join(', '));
                                            }}
                                            style={{fontSize:10,padding:'2px 8px',cursor:'pointer',
                                              border: secili ? '1px solid var(--accent)' : '1px solid var(--border)',
                                              borderRadius:4,
                                              background: secili ? 'var(--accent)' : 'var(--surface)',
                                              color: secili ? '#000' : 'var(--text)',
                                              fontWeight: secili ? 700 : 400}}>
                                            {secili ? '✓ ' : ''}{opt}
                                          </button>
                                        );
                                      })}
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </div>
                          )}
                        </td>
                        <td>
                          <input className="input" type="number" min="0.01" step={k.birim==='m²'||k.birim==='m'?'0.01':'1'} style={{padding:'4px 6px',fontSize:12}} value={k.adet}
                            onChange={e=>{const v=k.birim==='m²'||k.birim==='m'?parseFloat(e.target.value)||1:parseInt(e.target.value)||1;kalemSet(i,'adet',v);}}/>
                        </td>
                        <td>
                          <select className="select" style={{padding:'4px 6px',fontSize:12}} value={k.birim}
                            onChange={e=>kalemSet(i,'birim',e.target.value)}>
                            <option>Adet</option><option>m²</option><option>m</option><option>Takım</option>
                          </select>
                        </td>
                        <td>
                          <input className="input" type="number" step="0.01" style={{padding:'4px 6px',fontSize:12}}
                            value={Math.round(tleCevir(k.birimFiyat||0)*100)/100}
                            onChange={e=>{const v=parseFloat(e.target.value)||0;const pb=form.paraBirimi;const tlV=pb==="EUR"?v*(kurlar.EUR||42):pb==="USD"?v*(kurlar.USD||38):v;kalemSet(i,"birimFiyat",tlV);}}/>
                        </td>
                        <td style={{textAlign:'right',fontWeight:500,color:'var(--accent)',whiteSpace:'nowrap',fontSize:13}}>
                          {fmt(kalemToplamGoster(k), sym)}
                        </td>
                        <td>
                          <div style={{display:'flex',flexDirection:'column',gap:1}}>
                            <button onClick={()=>moveKalem(i,-1)} disabled={i===0}
                              style={{background:'none',border:'none',cursor:i===0?'default':'pointer',fontSize:10,padding:'1px 4px',color:i===0?'var(--border)':'var(--muted)',lineHeight:1}}>▲</button>
                            <button onClick={()=>moveKalem(i,1)} disabled={i===form.kalemler.length-1}
                              style={{background:'none',border:'none',cursor:i===form.kalemler.length-1?'default':'pointer',fontSize:10,padding:'1px 4px',color:i===form.kalemler.length-1?'var(--border)':'var(--muted)',lineHeight:1}}>▼</button>
                          </div>
                          <button onClick={()=>removeKalem(i)} style={{background:'none',border:'none',color:'var(--red)',cursor:'pointer',fontSize:16,padding:'2px 4px'}}>×</button>
                        </td>
                      </tr>
                      </React.Fragment>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Toplam satırı - tablo içinde sağa hizalı */}
              {form.kalemler.length>0 && (
                <div style={{display:'flex',justifyContent:'space-between',alignItems:'flex-end',padding:'10px 16px',
                  borderTop:'1px solid var(--border)',background:'var(--surface2)'}}>
                  {/* Genel iskonto */}
                  <div style={{display:'flex',alignItems:'center',gap:8}}>
                    <label style={{margin:0,fontSize:12,color:'var(--muted)'}}>Genel İskonto %</label>
                    <input className="input" type="number" min="0" max="100" step="0.1"
                      style={{width:80,padding:'4px 8px',fontSize:13}}
                      value={form.iskonto||0}
                      onChange={e=>{set('iskonto',parseFloat(e.target.value)||0);}}/>
                  </div>
                  <div style={{display:'flex',flexDirection:'column',gap:4,minWidth:260}}>
                    <div style={{display:'flex',justifyContent:'space-between',fontSize:13}}>
                      <span style={{color:'var(--muted)'}}>Toplam Adet</span>
                      <span>{form.kalemler.reduce((s,k)=>s+(k.adet||1),0)} Adet</span>
                    </div>
                    <div style={{display:'flex',justifyContent:'space-between',fontSize:13}}>
                      <span style={{color:'var(--muted)'}}>Ara Toplam</span>
                      <span>{fmt(araToplam_g)} {sym}</span>
                    </div>
                    {(form.iskonto||0)>0 && (
                      <div style={{display:'flex',justifyContent:'space-between',fontSize:13,color:'var(--green)'}}>
                        <span>İskonto (%{form.iskonto})</span>
                        <span>-{fmt(iskontoTutari_g)} {sym}</span>
                      </div>
                    )}
                    <div style={{display:'flex',justifyContent:'space-between',fontSize:13}}>
                      <span style={{color:'var(--muted)'}}>{`KDV (%${form.kdvOrani})`}</span>
                      <span>{fmt(kdvTutari_g)} {sym}</span>
                    </div>
                    <div style={{display:'flex',justifyContent:'space-between',fontSize:13,fontWeight:700,
                      color:'var(--accent)',borderTop:'1px solid var(--border)',paddingTop:6}}>
                      <span>Genel Toplam</span>
                      <span>{fmt(genelToplam_g)} {sym}</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>

          {/* Kalem Düzenleme artık ürün panelinde yapılıyor */}

          {error && <div style={{color:'var(--red)',fontSize:13}}>{error}</div>}
        </div>

        {/* FOOTER */}
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={handleClose}>İptal</button>
          <button className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
            {loading?'Kaydediliyor...':(teklif?'Güncelle':'Kaydet')}
          </button>
        </div>
      </div>
    </div>
  );
}

/* ── Kalem Düzenleme Modali ─────────────────────────────── */
function KalemDuzenleModal({ kalem, urunler, kurlar, paraBirimi, onKaydet, onKapat }) {
  const urun = urunler.find(u => u.kod === kalem.urunKodu);
  const [olcular, setOlcular] = useState({});
  const [strOlcular, setStrOlcular] = useState({});
  const [ozellikler, setOzellikler] = useState({});
  const [ralKod, setRalKod] = useState('');
  const [miktar, setMiktar] = useState(kalem.adet || 1);
  const [motorFiyat, setMotorFiyat] = useState('');
  const [hesaplaniyor, setHesaplaniyor] = useState(false);
  const [fiyatHata, setFiyatHata] = useState('');
  const [yeniFiyat, setYeniFiyat] = useState(null);
  const [manuelAd, setManuelAd] = useState(kalem.urunAdi || '');
  const sym = paraBirimi === 'EUR' ? '€' : paraBirimi === 'USD' ? '$' : '₺';

  const tleCevir = (v) => {
    if (paraBirimi === 'EUR') return v / (kurlar.EUR || 42);
    if (paraBirimi === 'USD') return v / (kurlar.USD || 38);
    return v;
  };

  // Mevcut kalemden ölçü ve özellikleri parse et
  useEffect(() => {
    if (!urun) return;
    const ad = kalem.urunAdi || '';
    // WxH parse
    const mWH = ad.match(/(\d+)[xX×](\d+)/);
    if (mWH) { setOlcular(o => ({...o, GENISLIK: mWH[1], YUKSEKLIK: mWH[2]})); }
    // Çap parse
    const mCap = ad.match(/Ø\s*(\d+)/);
    if (mCap) { setOlcular(o => ({...o, CAP: mCap[1]})); }
    // Uzunluk parse
    const mL = ad.match(/(\d+)\s*mm/);
    if (mL && !mWH) { setOlcular(o => ({...o, UZUNLUK: mL[1]})); }
    // Özellikler parse
    if (ad.includes(' — ')) {
      const ops = ad.split(' — ')[1].split(' / ');
      const o = {};
      ops.forEach(op => {
        op = op.trim();
        if (op.includes('mm')) o['CERCEVE_TIPI'] = op;
        else if (op.includes('Damper') || op.includes('damper')) o['DAMPER_TIPI'] = op;
        else if (op.includes('Boyal') || op.includes('Boyasız') || op.includes('Eloksal')) o['RAL'] = op;
        else if (op.includes('Vidalı') || op.includes('Klipsli') || op.includes('Montaj')) o['MONTAJ'] = op;
      });
      setOzellikler(o);
      if (o['RAL'] && o['RAL'].includes('Boyalı - ')) {
        setRalKod(o['RAL'].replace('Boyalı - ', ''));
        setOzellikler(prev => ({...prev, RAL: 'Boyalı'}));
      }
    }
  }, []);

  const hesaplaFiyat = async () => {
    if (!urun) return;
    setHesaplaniyor(true); setFiyatHata('');
    try {
      const ozelliklerGonder = {...ozellikler};
      if (ozellikler['RAL'] === 'Boyalı' && ralKod.trim())
        ozelliklerGonder['RAL'] = `Boyalı - ${ralKod.trim()}`;

      const oNum = {};
      Object.entries(olcular).forEach(([k,v]) => { const n = parseFloat(v); if (!isNaN(n)) oNum[k] = n; });

      const res = await fiyatHesapla(urun.kod, {
        olcular: oNum, stringOlcular: strOlcular,
        ozellikler: ozelliklerGonder, motorFiyati: motorFiyat ? parseFloat(motorFiyat) : null
      });
      setYeniFiyat(res.data.toplam);
      // Ürün adını da güncelle
      if (res.data.urunAdi) setManuelAd(urun.ad);
    } catch(e) { setFiyatHata(e.response?.data?.hata || 'Hesaplanamadı'); }
    finally { setHesaplaniyor(false); }
  };

  const handleKaydet = () => {
    if (!urun) { onKapat(); return; }
    const ozelliklerGonder = {...ozellikler};
    if (ozellikler['RAL'] === 'Boyalı' && ralKod.trim())
      ozelliklerGonder['RAL'] = `Boyalı - ${ralKod.trim()}`;

    // Ürün adını yeniden oluştur
    const w = olcular['GENISLIK'], h = olcular['YUKSEKLIK'];
    const cap = olcular['CAP'] || strOlcular['NETIC_CAP'];
    const uzun = olcular['UZUNLUK'];
    let olcuEki = '';
    if (w && h && cap) olcuEki = ` ${w}x${h} Ø${cap}`;
    else if (w && h) olcuEki = ` ${w}x${h}`;
    else if (cap) olcuEki = ` Ø${cap}`;
    else if (uzun) olcuEki = ` ${uzun} mm`;

    const menfezTipi = ozelliklerGonder['MENFEZ_TIPI'];
    const menfezEki = menfezTipi ? ` ${menfezTipi}` : '';
    const suffix = Object.entries(ozelliklerGonder)
      .filter(([k,v]) => k === 'AKSESUAR_TIPI' && v && (Array.isArray(v) ? v.length > 0 : v !== ''))
      .map(([,v]) => Array.isArray(v) ? v.join(', ') : v).join(' / ');

    let ad = urun.ad + menfezEki + olcuEki;
    if (suffix) ad += ' — ' + suffix;

    const cerceve = ozelliklerGonder['CERCEVE_TIPI'];
    const damper = ozelliklerGonder['DAMPER_TIPI'];
    const ral = ozelliklerGonder['RAL'];
    const montaj = ozelliklerGonder['MONTAJ'];
    const ozellikParcalar = [cerceve, damper, ral, montaj].filter(Boolean);
    if (ozellikParcalar.length) ad += (suffix ? ' / ' : ' — ') + ozellikParcalar.join(' / ');

    // Eğer kullanıcı manuel ad girdiyse onu kullan, yoksa otomatik oluşturulanı
    const finalAd = manuelAd.trim() && manuelAd !== kalem.urunAdi ? manuelAd.trim() : ad;

    onKaydet({
      ...kalem,
      urunAdi: finalAd,
      birimFiyat: yeniFiyat !== null ? yeniFiyat : kalem.birimFiyat,
      adet: miktar,
    });
  };

  return (
    <div style={{position:'fixed',inset:0,background:'rgba(0,0,0,.7)',display:'flex',
      alignItems:'center',justifyContent:'center',zIndex:2000,padding:20}}>
      <div style={{background:'var(--surface)',border:'1px solid var(--border)',borderRadius:14,
        width:'100%',maxWidth:700,maxHeight:'90vh',overflowY:'auto',padding:24}}>
        <div style={{display:'flex',justifyContent:'space-between',alignItems:'center',marginBottom:16}}>
          <h3 style={{fontFamily:'var(--font-head)',fontSize:16,margin:0}}>
            {urun ? urun.ad : kalem.urunAdi} — Düzenle
          </h3>
          <button onClick={onKapat} style={{background:'none',border:'none',color:'var(--muted)',cursor:'pointer',fontSize:20}}>×</button>
        </div>

        {!urun ? (
          <div style={{color:'var(--muted)',fontSize:13}}>Bu kalem manuel eklenmiş, düzenleme yapılamaz.</div>
        ) : (
          <>
            {/* Ölçüler */}
            <div style={{marginBottom:12}}>
              <div style={{fontSize:11,fontWeight:700,color:'var(--muted)',textTransform:'uppercase',marginBottom:8}}>Ölçüler</div>
              <div style={{display:'grid',gridTemplateColumns:'1fr 1fr 1fr',gap:8}}>
                {urun.zorunluOlcular && urun.zorunluOlcular.map(olcu => (
                  <div key={olcu}>
                    <label style={{fontSize:11}}>{olcu.replace(/_/g,' ')}</label>
                    <input className="input" type="number" style={{fontSize:12,padding:'5px 8px'}}
                      value={olcular[olcu] || ''}
                      onChange={e => setOlcular(o => ({...o,[olcu]:e.target.value}))}/>
                  </div>
                ))}
              </div>
            </div>

            {/* Özellikler */}
            {urun.ozellikler && Object.keys(urun.ozellikler).length > 0 && (
              <div style={{marginBottom:12}}>
                <div style={{fontSize:11,fontWeight:700,color:'var(--muted)',textTransform:'uppercase',marginBottom:8}}>Özellikler</div>
                <div style={{display:'grid',gridTemplateColumns:'1fr 1fr',gap:8}}>
                  {Object.entries(urun.ozellikler).map(([tip, secenekler]) => (
                    <div key={tip}>
                      <label style={{fontSize:11}}>{tip.replace(/_/g,' ')}</label>
                      <select className="select" style={{fontSize:12,padding:'5px 8px'}}
                        value={ozellikler[tip] || ''}
                        onChange={e => setOzellikler(o => ({...o,[tip]:e.target.value}))}>
                        <option value="">Seç...</option>
                        {secenekler.map(s => <option key={s} value={s}>{s}</option>)}
                      </select>
                      {tip === 'RAL' && ozellikler['RAL'] === 'Boyalı' && (
                        <input className="input" placeholder="RAL kodu" style={{fontSize:12,padding:'4px 8px',marginTop:4}}
                          value={ralKod} onChange={e => setRalKod(e.target.value)}/>
                      )}
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Ürün Adı */}
            <div style={{marginBottom:12}}>
              <div style={{fontSize:11,fontWeight:700,color:'var(--muted)',textTransform:'uppercase',marginBottom:6}}>Ürün Adı</div>
              <input className="input" style={{fontSize:12,padding:'6px 8px'}}
                value={manuelAd}
                onChange={e => setManuelAd(e.target.value)}
                placeholder="Ürün adını düzenle..."/>
              <div style={{fontSize:10,color:'var(--muted)',marginTop:3}}>
                Fiyat Hesapla butonuna basınca otomatik güncellenir. Manuel değiştirmek için düzenle.
              </div>
            </div>

            {/* Miktar */}
            <div style={{marginBottom:12,display:'flex',alignItems:'center',gap:8}}>
              <label style={{fontSize:11,margin:0}}>Adet</label>
              <input className="input" type="number" min="1" style={{width:80,fontSize:12,padding:'5px 8px'}}
                value={miktar} onChange={e => setMiktar(parseInt(e.target.value)||1)}/>
            </div>

            {fiyatHata && <div style={{color:'var(--red)',fontSize:12,marginBottom:8}}>{fiyatHata}</div>}

            {yeniFiyat !== null && (
              <div style={{color:'var(--green)',fontSize:13,marginBottom:8,fontWeight:600}}>
                Yeni Birim Fiyat: {(tleCevir(yeniFiyat)).toLocaleString('tr-TR',{minimumFractionDigits:2})} {sym}
              </div>
            )}

            <div style={{display:'flex',gap:8,justifyContent:'flex-end',marginTop:16}}>
              <button className="btn btn-secondary" onClick={onKapat}>İptal</button>
              <button className="btn btn-secondary" onClick={hesaplaFiyat} disabled={hesaplaniyor}>
                {hesaplaniyor ? 'Hesaplanıyor...' : '🔄 Fiyat Hesapla'}
              </button>
              <button className="btn btn-primary" onClick={handleKaydet}>Kaydet</button>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
