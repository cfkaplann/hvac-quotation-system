package com.teklif.dependency;

import java.util.*;

import com.teklif.model.OlcuAlanTipi;

public class DependencyEngine {

    // urunKodu -> (kasa -> allowed bogaz listesi)
    private static final Map<String, Map<String,List<String>>> rules = new HashMap<>();

    static {

        // ⭐ KARE ANEMOSTAD ÖRNEĞİ
        Map<String,List<String>> kareRules = new HashMap<>();

        kareRules.put("295x295", List.of("150x150"));
        kareRules.put("370x370", List.of("150x150","225x225"));
        kareRules.put("445x445", List.of("150x150","225x225","300x300"));

        rules.put("KANM", kareRules); // prefix bazlı
    }

    public static List<String> allowedValues(
            String urunKodu,
            OlcuAlanTipi source,
            String value,
            OlcuAlanTipi target){

        if(source != OlcuAlanTipi.KASA_WH) return null;
        if(target != OlcuAlanTipi.BOGAZ_WH) return null;

        String prefix = urunKodu.split("_")[0];

        Map<String,List<String>> map = rules.get(prefix);

        if(map == null) return null;

        return map.get(value);
    }
}
