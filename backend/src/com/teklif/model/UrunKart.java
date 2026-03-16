package com.teklif.model;

import java.util.List;


import java.util.Map;

public class UrunKart {

    private String kod;
    private UrunKategori kategori;
    private String ad;

 // ⭐ YENİ — Panel label override
    private Map<OlcuAlanTipi, String> olcuLabelMap;

    private List<OzellikTipi> ozellikler;
    private Map<OzellikTipi, List<String>> izinliSecimler;
    private List<OlcuAlanTipi> zorunluOlculer;
    

 // ⭐ KASA → BOĞAZ filtre haritası
    private Map<String, java.util.List<String>> bogazFiltreMap;


    // ⭐ YENİ — ÖLÇÜ COMBO DEĞERLERİ
    private Map<OlcuAlanTipi, List<String>> izinliOlcuDegerleri;

    // ⭐ AKSESUAR MODU
    private boolean aksesuarTekliMi;

    // =====================================================
    // ESKİ CONSTRUCTOR (SİLMEDİM)
    // =====================================================
    public UrunKart(String kod,
                    UrunKategori kategori,
                    String ad,
                    List<OzellikTipi> ozellikler,
                    Map<OzellikTipi, List<String>> izinliSecimler,
                    List<OlcuAlanTipi> zorunluOlculer) {

    	this(kod,kategori,ad,ozellikler,izinliSecimler,
    		     zorunluOlculer,null,false,null);
    }

    // =====================================================
    // ORTA SEVİYE CONSTRUCTOR
    // =====================================================
    public UrunKart(String kod,
                    UrunKategori kategori,
                    String ad,
                    List<OzellikTipi> ozellikler,
                    Map<OzellikTipi, List<String>> izinliSecimler,
                    List<OlcuAlanTipi> zorunluOlculer,
                    boolean aksesuarTekliMi) {

    	this(kod,kategori,ad,ozellikler,izinliSecimler,
    		     zorunluOlculer,null,aksesuarTekliMi,null);
    }

    // =====================================================
    // ⭐ FULL CONSTRUCTOR (YENİ)
    // =====================================================
    
    
    public UrunKart(String kod,
            UrunKategori kategori,
            String ad,
            List<OzellikTipi> ozellikler,
            Map<OzellikTipi, List<String>> izinliSecimler,
            List<OlcuAlanTipi> zorunluOlculer,
            Map<OlcuAlanTipi, List<String>> izinliOlcuDegerleri,
            boolean aksesuarTekliMi,
            Map<String, java.util.List<String>> bogazFiltreMap)
    {

        this.kod = kod;
        this.kategori = kategori;
        this.ad = ad;
        this.ozellikler = ozellikler;
        this.izinliSecimler = izinliSecimler;
        this.zorunluOlculer = zorunluOlculer;
        this.izinliOlcuDegerleri = izinliOlcuDegerleri;
        this.aksesuarTekliMi = aksesuarTekliMi;
        this.bogazFiltreMap = bogazFiltreMap;
    }

    public UrunKart(String kod,
            UrunKategori kategori,
            String ad,
            List<OzellikTipi> ozellikler,
            Map<OzellikTipi, List<String>> izinliSecimler,
            List<OlcuAlanTipi> zorunluOlculer,
            Map<OlcuAlanTipi, List<String>> izinliOlcuDegerleri,
            boolean aksesuarTekliMi,
            Map<String, java.util.List<String>> bogazFiltreMap,
            Map<OlcuAlanTipi, String> olcuLabelMap)
    {
        this(kod,kategori,ad,ozellikler,izinliSecimler,
             zorunluOlculer,izinliOlcuDegerleri,aksesuarTekliMi,bogazFiltreMap);

        this.olcuLabelMap = olcuLabelMap;
    }
    
    public String getKod(){ return kod; }
    public UrunKategori getKategori(){ return kategori; }
    public String getAd(){ return ad; }
    public List<OzellikTipi> getOzellikler(){ return ozellikler; }
    public Map<OzellikTipi,List<String>> getIzinliSecimler(){ return izinliSecimler; }
    public List<OlcuAlanTipi> getZorunluOlculer(){ return zorunluOlculer; }

    // ⭐ YENİ GETTER
    public Map<OlcuAlanTipi,List<String>> getIzinliOlcuDegerleri(){
        return izinliOlcuDegerleri;
    }

    public boolean isAksesuarTekliMi(){
        return aksesuarTekliMi;
    }

    @Override
    public String toString(){
        return ad;
    }
    
    public Map<String, java.util.List<String>> getBogazFiltreMap(){
        return bogazFiltreMap;
    }
    public Map<OlcuAlanTipi, String> getOlcuLabelMap(){
        return olcuLabelMap;
    }

}
