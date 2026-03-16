package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class KareSwirlConfig {
	private static Map<OlcuAlanTipi,String> kasaLabel(){
	    return Map.of(OlcuAlanTipi.KASA_WH,"Kasa Ölçüsü (WxH)");
	}

	private static Map<OlcuAlanTipi,String> bogazLabel(){
	    return Map.of(OlcuAlanTipi.BOGAZ_WH,"Boğaz Ölçüsü (WxH)");
	}

    public static List<UrunKart> get() {

      

        return List.of(

        // =====================================================
        // SABİT KANATLI
        // =====================================================
        new UrunKart(
            "KASWRDIF_SABKNT",
            UrunKategori.KARE_SWIRL,
            "Kare Sabit Kanatlı Swirl Difüzör",

            List.of(OzellikTipi.RAL, OzellikTipi.MONTAJ),

            Map.of(
                OzellikTipi.RAL,
                List.of("Boyasız","Eloksal","Boyalı"),

                OzellikTipi.MONTAJ,
                List.of("Vidalı","Köprülü")
            ),

            List.of(OlcuAlanTipi.KASA_WH),

            Map.of(
            	    OlcuAlanTipi.KASA_WH,
            	    List.of(
            	        "250x250",
            	        "300x300",
            	        "350x350",
            	        "400x400",
            	        "450x450",
            	        "500x500",
            	        "595x595"
            	    )
            	),

            false,
            null,
            kasaLabel()
        ),

        // =====================================================
        // İSTRİDYE
        // =====================================================
        new UrunKart(
            "KASWRDIF_ISTD",
            UrunKategori.KARE_SWIRL,
            "İstridye Swirl Difüzör",

            List.of(
                OzellikTipi.RAL,
                OzellikTipi.MENFEZ_TIPI,
                OzellikTipi.MONTAJ
            ),

            Map.of(
                OzellikTipi.RAL,
                List.of("Boyasız","Eloksal","Boyalı"),

                OzellikTipi.MONTAJ,
                List.of("Vidalı","Köprülü","Clip-in"),

                OzellikTipi.MENFEZ_TIPI,
                List.of(
                    "Dairesel Hizalı, Kare Ön Tablalı",
                    "Dörtgen Hizalı, Kare Ön Tablalı"
                )
            ),

            List.of(OlcuAlanTipi.KASA_WH),

            Map.of(
            	    OlcuAlanTipi.KASA_WH,
            	    List.of(
            	    		"400x400",
                	        "500x500",
                	        "595x595"
            	    )
            	),

            false,
            null,
            kasaLabel()
        ),

        // =====================================================
        // AYARLANABİLİR
        // =====================================================
        new UrunKart(
            "KASWRDIF_AYRKNT",
            UrunKategori.KARE_SWIRL,
            "Ayarlanabilir Kanatlı Swirl Difüzör",

            List.of(
                OzellikTipi.RAL,
                OzellikTipi.MENFEZ_TIPI,
                OzellikTipi.MONTAJ
            ),

            Map.of(
                OzellikTipi.RAL,
                List.of("Boyasız","Eloksal","Boyalı"),

                OzellikTipi.MONTAJ,
                List.of("Vidalı","Köprülü","Clip-in"),

                OzellikTipi.MENFEZ_TIPI,
                List.of(
                    "Dairesel Kanatlı, Kare Ön Tablalı",
                    "Kare Kanatlı, Kare Ön Tablalı",
                    "Merkezden Kaçık, Dairesel Kanatlı, Kare Ön Tablalı"
                )
            ),

            List.of(OlcuAlanTipi.KASA_WH),

            Map.of(
            	    OlcuAlanTipi.KASA_WH,
            	    List.of(
            	    		   "300x300",
                   	        "400x400",
                   	        "500x500",
                   	        "595x595",
                   	        "800x800"
            	    )
            	),

            false,
            null,
            kasaLabel()
        ),

        // =====================================================
        // 4 YÖNLÜ
        // =====================================================
        new UrunKart(
            "KASWRDIF_4YON",
            UrunKategori.KARE_SWIRL,
            "Dört Yönlü Swirl Difüzör",

            List.of(
                OzellikTipi.RAL,
                OzellikTipi.DAMPER_TIPI,
                OzellikTipi.MENFEZ_TIPI,
                OzellikTipi.MONTAJ
            ),

            Map.of(
                OzellikTipi.RAL,
                List.of("Boyasız","Eloksal","Boyalı"),

                OzellikTipi.MONTAJ,
                List.of("Vidalı","Köprülü","Clip-in"),

                OzellikTipi.MENFEZ_TIPI,
                List.of("Sabit Kanatlı","Ayarlanabilir Kanatlı"),

                OzellikTipi.DAMPER_TIPI,
                List.of("Dampersiz","Paralel Damperli","Zıt Damperli")
            ),

            List.of(OlcuAlanTipi.BOGAZ_WH),

            Map.of(
            	    OlcuAlanTipi.BOGAZ_WH,
            	    List.of(
            	    	      "420x420",
                     	        "595x595",
                     	        "720x720",
                     	        "1040x1040"
            	    )
            	),
            false,
            null,
            bogazLabel()
        ),

        // =====================================================
        // DRUM
        // =====================================================
        new UrunKart(
            "KASWRDIF_DRUM",
            UrunKategori.KARE_SWIRL,
            "Drum Difüzör",

            List.of(
                OzellikTipi.RAL,
                OzellikTipi.DAMPER_TIPI,
                OzellikTipi.MONTAJ
            ),

            Map.of(
                OzellikTipi.RAL,
                List.of("Boyasız","Eloksal","Boyalı"),

                OzellikTipi.MONTAJ,
                List.of("Vidalı"),

                OzellikTipi.DAMPER_TIPI,
                List.of("Dampersiz","Paralel Damperli","Zıt Damperli")
            ),

            List.of(OlcuAlanTipi.BOGAZ_WH),

            Map.of(
            	    OlcuAlanTipi.BOGAZ_WH,
            	    List.of(
            	        "255x156",
            	        "300x156",
            	        "330x156",
            	        "375x156",
            	        "485x156",
            	        "635x156",
            	        "535x260",
            	        "600x260",
            	        "660x260",
            	        "750x260",
            	        "900x260",
            	        "1050x260"
            	    )
            	),

            false,
            null,
            bogazLabel()
        )
        );
    }
}
