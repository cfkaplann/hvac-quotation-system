package com.teklif.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class UrunTanimi {

    private String prefix;
    private UrunKategori kategori;
    private OlcuTipi olcuTipi;

    private List<OzellikTipi> zorunluOzellikler;
    private List<OzellikTipi> opsiyonelOzellikler;

    private boolean aksesuarMultiSelect;

    // 🔥 YENİ ALAN
    private Map<OzellikTipi, Set<String>> muafOzellikler;

    public UrunTanimi(String prefix,
                      UrunKategori kategori,
                      OlcuTipi olcuTipi,
                      List<OzellikTipi> zorunlu,
                      List<OzellikTipi> opsiyonel,
                      boolean aksesuarMultiSelect,
                      Map<OzellikTipi, Set<String>> muafOzellikler){

        this.prefix = prefix;
        this.kategori = kategori;
        this.olcuTipi = olcuTipi;
        this.zorunluOzellikler = zorunlu;
        this.opsiyonelOzellikler = opsiyonel;
        this.aksesuarMultiSelect = aksesuarMultiSelect;
        this.muafOzellikler = muafOzellikler;
    }

    public OlcuTipi getOlcuTipi(){ return olcuTipi; }
    public List<OzellikTipi> getZorunlu(){ return zorunluOzellikler; }
    public List<OzellikTipi> getOpsiyonel(){ return opsiyonelOzellikler; }
    public boolean isAksesuarMultiSelect(){ return aksesuarMultiSelect; }

    // 🔥 YENİ GETTER
    public Map<OzellikTipi, Set<String>> getMuafOzellikler(){
        return muafOzellikler;
    }

    // ✅ SADECE EKLENDİ (PricingService için gerekli)
    public UrunKategori getKategori(){
        return kategori;
    }
}
