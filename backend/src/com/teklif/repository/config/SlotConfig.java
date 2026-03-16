package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class SlotConfig {
	public static List<UrunKart> get() {

		return List.of(

				// =====================
				// SLOT
				// =====================

				new UrunKart("SLT_TOP", UrunKategori.SLOT, "Toplayıcı Slot Difüzör", List.of(

						OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.DAMPER_TIPI, List.of("Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Köprülü")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.UZUNLUK, OlcuAlanTipi.SLOT_SAYISI)),
				new UrunKart("SLT_DAG", UrunKategori.SLOT, "Dağıtıcı Slot Difüzör", List.of(

						OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.DAMPER_TIPI, List.of("Damperli, Yönlendiricili Kanatlı"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Köprülü")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.UZUNLUK, OlcuAlanTipi.SLOT_SAYISI)),

				new UrunKart("SLT_GIZTOP", UrunKategori.SLOT, "Toplayıcı Gizli Slot Difüzör",
						List.of(OzellikTipi.MENFEZ_TIPI,

								OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.DAMPER_TIPI, List.of("Damperli, Yönlendiricili Kanatlı"),

								OzellikTipi.MENFEZ_TIPI, List.of("Alçıpana Montaj", "Ahşaba Montaj"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.UZUNLUK, OlcuAlanTipi.SLOT_SAYISI)),

				new UrunKart("SLT_GIZDAG", UrunKategori.SLOT, "Dağıtıcı Gizli Slot Difüzör",
						List.of(OzellikTipi.MENFEZ_TIPI,

								OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.DAMPER_TIPI, List.of("Damperli, Yönlendiricili Kanatlı"),

								OzellikTipi.MENFEZ_TIPI, List.of("Alçıpana Montaj", "Ahşaba Montaj"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.UZUNLUK, OlcuAlanTipi.SLOT_SAYISI)),
				new UrunKart("SLT_MAKA", UrunKategori.SLOT, "Makaralı Slot Difüzör",
						List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL,
								OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.DAMPER_TIPI, List.of("Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Köprülü")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.UZUNLUK, OlcuAlanTipi.SLOT_SAYISI))

		);
	}
}
