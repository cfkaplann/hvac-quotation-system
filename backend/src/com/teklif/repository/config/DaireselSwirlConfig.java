package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class DaireselSwirlConfig {
	
	private static Map<OlcuAlanTipi,String> anmaCapLabel(){
	    return Map.of(OlcuAlanTipi.KASA_CAP,"Anma Çapı (Ø)");
	}

	private static Map<OlcuAlanTipi,String> bogazCapLabel(){
	    return Map.of(OlcuAlanTipi.KASA_CAP,"Boğaz Ölçüsü (Ø)");
	}

    public static List<UrunKart> get() {

        return List.of(

            // =====================
            // DAİRESEL SWİRL DİFÜZÖR
            // =====================

        		new UrunKart(
        			    "DASWRDIF_TELS",
        			    UrunKategori.DAIRESEL_SWIRL,
        			    "Teleskobik Difüzör",

        			    List.of(
        			        OzellikTipi.RAL,
        			        OzellikTipi.MONTAJ
        			    ),

        			    Map.of(
        			        OzellikTipi.RAL, List.of("Boyasız","Eloksal","Boyalı"),
        			        OzellikTipi.MONTAJ, List.of("Vidalı")
        			    ),

        			    List.of(OlcuAlanTipi.KASA_CAP),

        			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
        			    Map.of(
        			        OlcuAlanTipi.KASA_CAP,
        			        List.of(
        			            "Ø200",
        			            "Ø250",
        			            "Ø300",
        			            "Ø400"
        			            
        			        )
        			    ),

        			    true,
        			    null,
        			    bogazCapLabel()
        			),


            new UrunKart(
                "DASWRDIF_KONF",
                UrunKategori.DAIRESEL_SWIRL,
                "Konfor Difüzörü",
                List.of(
                    OzellikTipi.RAL
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			            "Ø300",
			            "Ø400",
			            "Ø500",
			            "Ø630",
			            "Ø800"
			            
			        )
			    ),

			    true,
			    null,
			    anmaCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_YERDOS",
                UrunKategori.DAIRESEL_SWIRL,
                "Yer Döşeme Difüzörü",
                List.of(
                    OzellikTipi.AKSESUAR_TIPI,
                    OzellikTipi.RAL
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Filtresiz", "Poliüretan Filtreli")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			        		"Ø150",
			            "Ø200"
			            
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_DAIPAN",
                UrunKategori.DAIRESEL_SWIRL,
                "Dairesel Panel Difüzör",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			            "Ø100",
			            "Ø125",
			            "Ø160",
			            "Ø200",
			            "Ø250",
			            "Ø315",
			            "Ø400"
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_TURBLS",
                UrunKategori.DAIRESEL_SWIRL,
                "Türbülanslı Swirl Difüzör",
                List.of(
                    OzellikTipi.MENFEZ_TIPI,
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı"),
                    OzellikTipi.MENFEZ_TIPI, List.of("Sabit Kanatlı", "Ayarlanabilir Kanatlı")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			            "Ø250",
			            "Ø315",
			            "Ø355",
			            "Ø400",
			            "Ø500",
			            "Ø630",
			            "Ø800"
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_JETNOZ",
                UrunKategori.DAIRESEL_SWIRL,
                "Jet Nozul (Çift Cidarlı)",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Gizli Vidalı")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			            "Ø80",
			            "Ø120",
			            "Ø160",
			            "Ø200",
			            "Ø250",
			            "Ø315",
			            "Ø400"
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_SBTKNT",
                UrunKategori.DAIRESEL_SWIRL,
                "Dairesel Sabit Kanatlı Difüzör",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Köprülü")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			            "Ø280",
			            "Ø380",
			            "Ø480",
			            "Ø595"
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_HIZISTDKNT",
                UrunKategori.DAIRESEL_SWIRL,
                "Dairesel Hizalı İstridye Kanatlı Swirl Difüzör",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Köprülü")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			        		"Ø380",
				            "Ø480",
				            "Ø595"
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            ),

            new UrunKart(
                "DASWRDIF_AYRBLKNT",
                UrunKategori.DAIRESEL_SWIRL,
                "Ayarlanabilir Kanatlı Swirl Difüzör",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Köprülü")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			        		"Ø280",
				            "Ø380",
				            "Ø480",
				            "Ø595",
				            "Ø780"
				            
			            
			        )
			    ),

			    true,
			    null,
			    bogazCapLabel()
            )
        );
    }
}
