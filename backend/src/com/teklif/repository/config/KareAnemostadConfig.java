package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class KareAnemostadConfig {

    public static List<UrunKart> get() {
    	
    	Map<String,List<String>> kasaBogazMap = Map.of(
    		    "295x295", List.of("150x150"),
    		    "370x370", List.of("225x225","150x150"),
    		    "445x445", List.of("300x300","225x225","150x150"),
    		    "520x520", List.of("375x375","300x300","225x225","150x150"),
    		    "595x595", List.of("450x450","375x375","300x300","225x225","150x150")
    		);
    	
    	Map<String,List<String>> kasaBogazMapEgrisel = Map.of(
    		    "265x265", List.of("150x150"),
    		    "340x340", List.of("225x225","150x150"),
    		    "415x415", List.of("300x300","225x225","150x150"),
    		    "490x490", List.of("375x375","300x300","225x225","150x150"),
    		    "565x565", List.of("450x450","375x375","300x300","225x225","150x150")
    		);

        return List.of(

            // =====================================================
            // ⭐ DÜZ KANATLI KARE ANEMOSTAD
            // =====================================================
            new UrunKart(
                "KANM_DUZKNT",
                UrunKategori.KARE_ANEMOSTAD,
                "Düz Kanatlı Kare Anemostad",

                // ÖZELLİKLER
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.DAMPER_TIPI,
                    OzellikTipi.MONTAJ
                ),

                // ÖZELLİK SEÇİMLERİ
                Map.of(
                    OzellikTipi.RAL,
                    List.of("Boyasız","Eloksal","Boyalı"),

                    OzellikTipi.MONTAJ,
                    List.of("Vidalı","Clip-in","Klipsli","Lay-in","Karolaj"),

                    OzellikTipi.DAMPER_TIPI,
                    List.of("Dampersiz","Paralel Damperli","Zıt Damperli")
                ),

                // ZORUNLU ÖLÇÜLER
                List.of(
                    OlcuAlanTipi.KASA_WH,
                    OlcuAlanTipi.BOGAZ_WH
                ),

                // ⭐⭐⭐ YENİ — ÖLÇÜ COMBO DEĞERLERİ ⭐⭐⭐
                Map.of(
                    OlcuAlanTipi.KASA_WH,
                    List.of(
                        "295x295",
                        "370x370",
                        "445x445",
                        "520x520",
                        "595x595"
                    ),

                    OlcuAlanTipi.BOGAZ_WH,
                    List.of(
                        "150x150",
                        "225x225",
                        "300x300",
                        "375x375",
                        "450x450"
                    )
                ),

                false,
                kasaBogazMap
            ),

            // =====================================================
            // ⭐ EĞRİSEL KANATLI KARE ANEMOSTAD
            // =====================================================
            new UrunKart(
                "KANM_EGRKNT",
                UrunKategori.KARE_ANEMOSTAD,
                "Eğrisel Kanatlı Kare Anemostad",

                List.of(
                    OzellikTipi.MENFEZ_TIPI,
                    OzellikTipi.RAL,
                    OzellikTipi.DAMPER_TIPI,
                    OzellikTipi.MONTAJ
                ),

                Map.of(
                    OzellikTipi.RAL,
                    List.of("Boyasız","Eloksal","Boyalı"),

                    OzellikTipi.MONTAJ,
                    List.of("Vidalı","Clip-in","Klipsli","Lay-in","Karolaj"),

                    OzellikTipi.DAMPER_TIPI,
                    List.of("Dampersiz","Paralel Damperli","Zıt Damperli"),

                    OzellikTipi.MENFEZ_TIPI,
                    List.of("Sabit İç Gövdeli","Sökülebilir İç Gövdeli")
                ),

                List.of(
                    OlcuAlanTipi.KASA_WH,
                    OlcuAlanTipi.BOGAZ_WH
                ),

                // ⭐ ÖLÇÜ COMBO
                Map.of(
                    OlcuAlanTipi.KASA_WH,
                    List.of(
                        "265x265",
                        "340x340",
                        "415x415",
                        "490x490",
                        "565x565"
                    ),

                    OlcuAlanTipi.BOGAZ_WH,
                    List.of(
                        "150x150",
                        "225x225",
                        "300x300",
                        "375x375",
                        "450x450"
                    )
                ),

                false,
                kasaBogazMapEgrisel
            )
        );
    }
}
