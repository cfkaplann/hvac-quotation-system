import React, { useState, useEffect } from 'react';
import { getUrunler, fiyatHesapla } from '../services/api';

const OLCU_LABEL = {
  GENISLIK:'Genişlik W (mm)',YUKSEKLIK:'Yükseklik H (mm)',UZUNLUK:'Uzunluk L (mm)',
  CAP:'Çap Ø (mm)',KASA_WH:'Kasa Ölçüsü',BOGAZ_WH:'Boğaz Ölçüsü',
  BOGAZ_CAP:'Boğaz Çap (mm)',NETIC_CAP:'Netice Çap (mm)',KASA_CAP:'Kasa Çap',SLOT_SAYISI:'Slot Sayısı',
};
const STRING_OLCULAR = new Set(['KASA_WH','BOGAZ_WH','KASA_CAP','BOGAZ_CAP']);
const INT_OLCULAR    = new Set(['SLOT_SAYISI']);
const OZELLIK_LABEL  = {
  CERCEVE_TIPI:'Çerçeve Tipi',MENFEZ_TIPI:'Tip',DAMPER_TIPI:'Damper',
  RAL:'RAL / Renk',MONTAJ:'Montaj',AKSESUAR_TIPI:'Aksesuar',
};
const KATEGORI_LABEL = {
  MENFEZ:'Menfez',SLOT:'Slot / Lineer',DIKDORTGEN_DAMPER:'Dikdörtgen Damper',
  DAIRESEL_DAMPER:'Dairesel Damper',KARE_ANEMOSTAD:'Kare Anemostad',
  DAIRESEL_ANEMOSTAD:'Dairesel Anemostad',KARE_SWIRL:'Kare Swirl',
  DAIRESEL_SWIRL:'Dairesel Swirl',PANJUR:'Panjur',KAPAK:'Kapak',KUTU:'Kutu / Box',
};

export default function UrunSecimModal({ onEkle, onClose, paraBirimi = 'TL', kurlar = { EUR: 42, USD: 38 } }) {
  const [urunler,setUrunler]=useState([]);
  const [kategoriler,setKategoriler]=useState([]);
  const [aktifKat,setAktifKat]=useState('');
  const [seciliUrun,setSeciliUrun]=useState(null);
  const [olcular,setOlcular]=useState({});
  const [strOlcular,setStrOlcular]=useState({});
  const [ozellikler,setOzellikler]=useState({});
  const [sonuc,setSonuc]=useState(null);
  const [loading,setLoading]=useState(false);
  const [hatalar,setHatalar]=useState([]);
  const [adet,setAdet]=useState(1);
  const [aramaText,setAramaText]=useState('');

  useEffect(()=>{
    getUrunler().then(r=>{
      setUrunler(r.data);
      const katlar=[...new Set(r.data.map(u=>u.kategori))];
      setKategoriler(katlar);
      setAktifKat(katlar[0]||'');
    });
  },[]);

  const filtreliUrunler=urunler.filter(u=>
    (!aktifKat||u.kategori===aktifKat)&&
    (!aramaText||u.ad.toLowerCase().includes(aramaText.toLowerCase()))
  );

  const urunSec=(u)=>{setSeciliUrun(u);setOlcular({});setStrOlcular({});setOzellikler({});setSonuc(null);setHatalar([]);setAdet(1);};
  const setO=(tip,val)=>{setOlcular(o=>({...o,[tip]:val}));setSonuc(null);};
  const setS=(tip,val)=>{
    if(tip==='KASA_WH'||tip==='KASA_CAP') setStrOlcular(o=>({...o,[tip]:val,BOGAZ_WH:'',BOGAZ_CAP:''}));
    else setStrOlcular(o=>({...o,[tip]:val}));
    setSonuc(null);
  };
  const setOz=(tip,val,multi)=>{
    if(multi){setOzellikler(o=>{const m=Array.isArray(o[tip])?o[tip]:[];return{...o,[tip]:m.includes(val)?m.filter(x=>x!==val):[...m,val]};});}
    else{setOzellikler(o=>({...o,[tip]:val}));}
    setSonuc(null);
  };

  const validate=()=>{
    const errs=[];
    if(!seciliUrun) return['Ürün seçiniz.'];
    for(const olcu of seciliUrun.zorunluOlcular){
      if(STRING_OLCULAR.has(olcu)){
        if(!strOlcular[olcu]||strOlcular[olcu].trim()==='') errs.push(`"${OLCU_LABEL[olcu]||olcu}" seçilmesi zorunludur.`);
      } else {
        const v=olcular[olcu];
        if(v===undefined||v===''||isNaN(parseFloat(v))) errs.push(`"${OLCU_LABEL[olcu]||olcu}" girilmesi zorunludur.`);
        else if(parseFloat(v)<=0) errs.push(`"${OLCU_LABEL[olcu]||olcu}" 0'dan büyük olmalıdır.`);
      }
    }
    for(const [tip,secenekler] of Object.entries(seciliUrun.ozellikler)){
      if(tip==='AKSESUAR_TIPI'||secenekler.length===0) continue;
      const s=ozellikler[tip];
      if(!s||(Array.isArray(s)?s.length===0:s==='')) errs.push(`"${OZELLIK_LABEL[tip]||tip}" seçilmesi zorunludur.`);
    }
    return errs;
  };

  const hesapla=async()=>{
    const errs=validate();
    if(errs.length>0){setHatalar(errs);return;}
    setLoading(true);setHatalar([]);setSonuc(null);
    try{
      const oNum={};
      for(const[k,v] of Object.entries(olcular)){const n=parseFloat(v);if(!isNaN(n))oNum[k]=n;}
      const r=await fiyatHesapla(seciliUrun.kod,{olcular:oNum,stringOlcular:strOlcular,ozellikler});
      setSonuc(r.data);
    }catch(e){setHatalar([e.response?.data?.hata||'Fiyat hesaplanamadı. Değerleri kontrol edin.']);}
    finally{setLoading(false);}
  };

  const kalemeEkle=()=>{
    if(!sonuc||!seciliUrun) return;
    const p=[];
    for(const olcu of seciliUrun.zorunluOlcular){
      if(STRING_OLCULAR.has(olcu)&&strOlcular[olcu]) p.push(strOlcular[olcu]);
      else if(olcular[olcu]) p.push(`${(OLCU_LABEL[olcu]||olcu).split(' ')[0]}:${olcular[olcu]}`);
    }
    for(const[tip,val] of Object.entries(ozellikler)){
      if(!val||(Array.isArray(val)&&val.length===0)) continue;
      p.push(Array.isArray(val)?val.join('+'):val);
    }
    const tamAd=seciliUrun.ad+(p.length>0?' | '+p.join(' | '):'');
    const tlFiyat = sonuc.toplam;
    let cevrilen = tlFiyat;
    if (paraBirimi === 'EUR') cevrilen = tlFiyat / (kurlar.EUR || 42);
    else if (paraBirimi === 'USD') cevrilen = tlFiyat / (kurlar.USD || 38);
    onEkle({urunKodu:seciliUrun.kod,urunAdi:tamAd,adet,birim:'Adet',birimFiyat:Math.round(cevrilen*100)/100,iskonto:0});
  };

  const getBogazSecenekleri=(olcuTip)=>{
    if(!seciliUrun) return[];
    const kasa=strOlcular['KASA_WH']||strOlcular['KASA_CAP'];
    const fm=seciliUrun.bogazFiltreMap;
    if(fm&&kasa&&fm[kasa]) return fm[kasa];
    return seciliUrun.izinliOlcuDegerleri?.[olcuTip]||[];
  };

  const renderOlcu=(olcu)=>{
    const isStr=STRING_OLCULAR.has(olcu);
    const isInt=INT_OLCULAR.has(olcu);
    const isBogaz=olcu==='BOGAZ_WH'||olcu==='BOGAZ_CAP';
    const combo=isBogaz?getBogazSecenekleri(olcu):seciliUrun.izinliOlcuDegerleri?.[olcu]||[];
    const val=isStr?(strOlcular[olcu]||''):(olcular[olcu]||'');
    const bc=val===''?'var(--border)':'var(--green)';
    if(isStr&&combo.length>0){
      return(
        <select className="select" style={{borderColor:bc}} value={val} onChange={e=>setS(olcu,e.target.value)}>
          <option value="">— Seç —</option>
          {combo.map(s=><option key={s} value={s}>{s}</option>)}
        </select>
      );
    }
    return(
      <input className="input" type="number" step={isInt?'1':'any'} min={isInt?'1':'0'}
        placeholder={isInt?'Sayı':'mm'} value={val} style={{borderColor:bc}}
        onChange={e=>isStr?setS(olcu,e.target.value):setO(olcu,e.target.value)}/>
    );
  };

  const renderOzellik=(tip,secenekler)=>{
    const isMulti=seciliUrun.multiSelectOzellikler.includes(tip);
    const isOps=tip==='AKSESUAR_TIPI';
    const s=ozellikler[tip];
    const doldu=isMulti?(Array.isArray(s)&&s.length>0):(s&&s!=='');
    const bc=isOps?(doldu?'var(--green)':'var(--border)'):(doldu?'var(--green)':'var(--red)');
    if(isMulti){
      return(
        <div style={{border:`1px solid ${bc}`,borderRadius:8,padding:'8px 10px',background:'var(--surface2)'}}>
          {secenekler.map(sv=>(
            <label key={sv} style={{display:'flex',alignItems:'center',gap:8,cursor:'pointer',textTransform:'none',letterSpacing:0,fontWeight:400,color:'var(--text)',fontSize:13,padding:'3px 0'}}>
              <input type="checkbox" checked={Array.isArray(s)&&s.includes(sv)} onChange={()=>setOz(tip,sv,true)}/>
              {sv}
            </label>
          ))}
        </div>
      );
    }
    return(
      <select className="select" style={{borderColor:bc}} value={s||''} onChange={e=>setOz(tip,e.target.value,false)}>
        <option value="">— Seç —</option>
        {secenekler.map(sv=><option key={sv} value={sv}>{sv}</option>)}
      </select>
    );
  };

  return(
    <div className="modal-overlay" onClick={e=>e.target===e.currentTarget&&onClose()}>
      <div style={{background:'var(--surface)',border:'1px solid var(--border)',borderRadius:14,width:'95%',maxWidth:1060,maxHeight:'92vh',display:'flex',flexDirection:'column',animation:'slideUp .2s ease'}}>
        <div className="modal-header">
          <h2 style={{fontFamily:'var(--font-head)'}}>Ürün Seç & Fiyat Hesapla</h2>
          <button className="close-btn" onClick={onClose}>×</button>
        </div>
        <div style={{display:'flex',flex:1,overflow:'hidden'}}>
          {/* SOL */}
          <div style={{width:270,flexShrink:0,borderRight:'1px solid var(--border)',display:'flex',flexDirection:'column',overflow:'hidden'}}>
            <div style={{padding:'10px 12px',borderBottom:'1px solid var(--border)'}}>
              <input className="input" placeholder="🔍 Ara..." value={aramaText}
                onChange={e=>{setAramaText(e.target.value);setAktifKat('');}} style={{fontSize:13}}/>
            </div>
            <div style={{overflowY:'auto',flex:1}}>
              {kategoriler.map(k=>(
                <div key={k}>
                  <div onClick={()=>setAktifKat(aktifKat===k?'':k)} style={{padding:'7px 14px',cursor:'pointer',fontSize:11,fontWeight:700,letterSpacing:'.06em',textTransform:'uppercase',color:'var(--muted)',background:'var(--surface)',borderBottom:'1px solid var(--border)',display:'flex',justifyContent:'space-between'}}>
                    <span>{KATEGORI_LABEL[k]||k}</span><span>{aktifKat===k?'▲':'▼'}</span>
                  </div>
                  {(aktifKat===k||aramaText)&&filtreliUrunler.filter(u=>u.kategori===k).map(u=>(
                    <div key={u.kod} onClick={()=>urunSec(u)} style={{padding:'9px 18px',cursor:'pointer',fontSize:13,background:seciliUrun?.kod===u.kod?'var(--surface2)':'transparent',borderLeft:seciliUrun?.kod===u.kod?'2px solid var(--accent)':'2px solid transparent',color:seciliUrun?.kod===u.kod?'var(--text)':'var(--muted)',borderBottom:'1px solid var(--border)'}}
                      onMouseEnter={e=>{if(seciliUrun?.kod!==u.kod)e.currentTarget.style.background='var(--surface2)';}}
                      onMouseLeave={e=>{if(seciliUrun?.kod!==u.kod)e.currentTarget.style.background='transparent';}}>
                      {u.ad}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </div>
          {/* SAĞ */}
          <div style={{flex:1,overflow:'auto',padding:24}}>
            {!seciliUrun?(
              <div style={{textAlign:'center',color:'var(--muted)',marginTop:60}}>← Soldan bir ürün seçin</div>
            ):(
              <>
                <div style={{marginBottom:20}}>
                  <div style={{fontFamily:'var(--font-head)',fontSize:18,fontWeight:700}}>{seciliUrun.ad}</div>
                  <div style={{fontSize:12,color:'var(--muted)',marginTop:3}}>{seciliUrun.kod}</div>
                </div>

                {seciliUrun.zorunluOlcular.length>0&&(
                  <div style={{marginBottom:20}}>
                    <div style={{fontSize:11,color:'var(--muted)',letterSpacing:'.08em',textTransform:'uppercase',marginBottom:10,fontWeight:700}}>
                      Ölçüler <span style={{color:'var(--red)'}}>* zorunlu</span>
                    </div>
                    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(170px,1fr))',gap:12}}>
                      {seciliUrun.zorunluOlcular.map(olcu=>(
                        <div key={olcu}>
                          <label style={{color:'var(--text)'}}>{OLCU_LABEL[olcu]||olcu} <span style={{color:'var(--red)'}}>*</span></label>
                          {renderOlcu(olcu)}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {Object.keys(seciliUrun.ozellikler).length>0&&(
                  <div style={{marginBottom:20}}>
                    <div style={{fontSize:11,color:'var(--muted)',letterSpacing:'.08em',textTransform:'uppercase',marginBottom:10,fontWeight:700}}>Özellikler</div>
                    <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(200px,1fr))',gap:16}}>
                      {Object.entries(seciliUrun.ozellikler).map(([tip,secenekler])=>(
                        <div key={tip}>
                          <label style={{color:'var(--text)'}}>
                            {OZELLIK_LABEL[tip]||tip}
                            {tip!=='AKSESUAR_TIPI'&&<span style={{color:'var(--red)',marginLeft:3}}>*</span>}
                            {tip==='AKSESUAR_TIPI'&&<span style={{color:'var(--muted)',marginLeft:4,fontSize:10}}>(opsiyonel)</span>}
                          </label>
                          {renderOzellik(tip,secenekler)}
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {hatalar.length>0&&(
                  <div style={{background:'#ef444415',border:'1px solid var(--red)',borderRadius:8,padding:'10px 14px',marginBottom:16}}>
                    {hatalar.map((h,i)=>(
                      <div key={i} style={{color:'var(--red)',fontSize:13}}>⚠️ {h}</div>
                    ))}
                  </div>
                )}

                <button className="btn btn-secondary" onClick={hesapla} disabled={loading} style={{marginBottom:16}}>
                  {loading?'Hesaplanıyor...':'🧮 Fiyat Hesapla'}
                </button>

                {sonuc&&(
                  <div style={{background:'var(--surface2)',borderRadius:10,padding:20,border:'1px solid var(--border)'}}>
                    <div style={{fontFamily:'var(--font-head)',fontWeight:700,marginBottom:14,fontSize:15}}>Hesaplama Sonucu</div>
                    {[['Ham Fiyat',sonuc.hamFiyat],['Oran Ekleri',sonuc.oranEkleri],['Motor Ek',sonuc.motorEk]]
                      .filter(([,v])=>v>0).map(([lbl,val])=>(
                      <div key={lbl} style={{display:'flex',justifyContent:'space-between',padding:'4px 0',fontSize:13,color:'var(--muted)'}}>
                        <span>{lbl}</span><span>{val.toLocaleString('tr-TR',{minimumFractionDigits:2})} ₺</span>
                      </div>
                    ))}
                    <div style={{display:'flex',justifyContent:'space-between',padding:'10px 0 6px',borderTop:'1px solid var(--border)',fontWeight:700,color:'var(--accent)',fontSize:16,marginTop:6}}>
                      <span>Birim Fiyat (TL)</span>
                      <span>{sonuc.toplam.toLocaleString('tr-TR',{minimumFractionDigits:2})} ₺</span>
                    </div>
                    <div style={{display:'flex',gap:12,marginTop:16,alignItems:'flex-end'}}>
                      <div style={{width:120}}>
                        <label>Adet</label>
                        <input className="input" type="number" min="1" value={adet} onChange={e=>setAdet(parseInt(e.target.value)||1)}/>
                      </div>
                      <button className="btn btn-primary" style={{flex:1,justifyContent:'center',padding:10}} onClick={kalemeEkle}>
                        + Kaleme Ekle &nbsp;({(() => {
                        const tl = sonuc.toplam * adet;
                        if (paraBirimi === 'EUR') return (tl / (kurlar.EUR||42)).toLocaleString('tr-TR',{minimumFractionDigits:2}) + ' €';
                        if (paraBirimi === 'USD') return (tl / (kurlar.USD||38)).toLocaleString('tr-TR',{minimumFractionDigits:2}) + ' $';
                        return tl.toLocaleString('tr-TR',{minimumFractionDigits:2}) + ' ₺';
                      })()})
                      </button>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
