package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class KapakConfig {

    public static List<UrunKart> get() {

        return List.of(

            // =====================
            // KAPAK
            // =====================

            new UrunKart(
                "KPK_KONT",
                UrunKategori.KAPAK,
                "Kontrol Kapağı",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Boğazdan Vidalı")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true // ⭐ KAPAK → AKSESUAR SINGLE
            ),

            new UrunKart(
                "KPK_KONTIZO",
                UrunKategori.KAPAK,
                "Kontrol Kapağı - İzolasyonlu",
                List.of(
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Boğazdan Vidalı")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true
            ),

            new UrunKart(
                "KPK_KAPETMNZKONT",
                UrunKategori.KAPAK,
                "Kare Petek Menfez Kontrol Kapağı",
                List.of(
                    OzellikTipi.AKSESUAR_TIPI,
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Boğazdan Vidalı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Filtreli", "Poliüretan Filtreli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true // ⭐ SINGLE AKSESUAR
            ),

            new UrunKart(
                "KPK_LINMNZKONT",
                UrunKategori.KAPAK,
                "Lineer Menfez Kontrol Kapağı",
                List.of(
                    OzellikTipi.MENFEZ_TIPI,
                    OzellikTipi.AKSESUAR_TIPI,
                    OzellikTipi.RAL,
                    OzellikTipi.MONTAJ
                ),
                Map.of(
                    OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),
                    OzellikTipi.MONTAJ, List.of("Vidalı", "Boğazdan Vidalı"),
                    OzellikTipi.MENFEZ_TIPI, List.of("17° Açılı", "30° Açılı"),
                    OzellikTipi.AKSESUAR_TIPI, List.of("Filtreli", "Poliüretan Filtreli")
                ),
                List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
                true // ⭐ SINGLE AKSESUAR
            )

        );
    }
}
