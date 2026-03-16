package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class PanjurConfig {

    public static List<UrunKart> get() {

        return List.of(

            // =====================
            // PANJUR
            // =====================

            new UrunKart(
                "PNJ_AKSTK",
                UrunKategori.PANJUR,
                "Akustik Panjur",
                List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.MENFEZ_TIPI, List.of("Tekli Tip", "İkiz Tip"),
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Civatalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true   // ⭐ PANJUR = AKSESUAR TEKLİ
            ),

            new UrunKart(
                "PNJ_ALTIKUTU",
                UrunKategori.PANJUR,
                "Altıgen Kutulu Panjur",
                List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.KASA_CAP),

			    // ⭐⭐⭐ ÖLÇÜ DEĞERLERİ BURAYA
			    Map.of(
			        OlcuAlanTipi.KASA_CAP,
			        List.of(
			        		"Ø315",
				            "Ø630"
				            
			            
			        )
			    ),

			    true,
			    null
            ),

            new UrunKart(
                "PNJ_EGRIKNT",
                UrunKategori.PANJUR,
                "Eğrisel Kanatlı Panjur",
                List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.MENFEZ_TIPI, List.of("Yok", "Kol Kumandalı"),
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Civatalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            ),

            new UrunKart(
                "PNJ_GNSKNT",
                UrunKategori.PANJUR,
                "Geniş Kanatlı Panjur",
                List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Civatalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            ),

            new UrunKart(
                "PNJ_KUMTUT",
                UrunKategori.PANJUR,
                "Kum Tutucu Panjur",
                List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.MENFEZ_TIPI, List.of("Çekmeceli Tip", "Alttan Boşaltmalı Tip"),
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Boğazdan Vidalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            ),

            new UrunKart(
                "PNJ_SBTDARKNT",
                UrunKategori.PANJUR,
                "Sabit Dar Kanatlı Panjur",
                List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.MENFEZ_TIPI, List.of("Yok", "Kol Kumandalı"),
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Civatalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            ),

            new UrunKart(
                "PNJ_SRBSTKNT",
                UrunKategori.PANJUR,
                "Serbest Kanatlı Panjur",
                List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Civatalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            ),

            new UrunKart(
                "PNJ_SIVAUST",
                UrunKategori.PANJUR,
                "Sıva Üstü Panjur",
                List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.MONTAJ),
                Map.of(
                    OzellikTipi.MENFEZ_TIPI, List.of("Yok", "Kol Kumandalı"),
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Civatalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli", "Alüminyum Telli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            )

        );
    }
}
