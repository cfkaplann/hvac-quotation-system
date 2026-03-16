package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class DaireselAnemostadConfig {

	public static List<UrunKart> get() {

		return List.of(

				// =====================
				// DAİRESEL ANEMOSTAD
				// =====================

				new UrunKart("DANM_TAVN", UrunKategori.DAIRESEL_ANEMOSTAD, "Düz Kanatlı Kare Anemostad",
						List.of(OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"), OzellikTipi.MONTAJ,
								List.of("Vidalı", "Spot Yaylı"), OzellikTipi.DAMPER_TIPI,
								List.of("Dampersiz", "Kelebek Kanatlı")

						),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.NETIC_CAP)),

				new UrunKart("DANM_GEMI", UrunKategori.DAIRESEL_ANEMOSTAD, "Gemici Anemostad",
						List.of(OzellikTipi.RAL, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"), OzellikTipi.MONTAJ,
								List.of("Vidalı", "Spot Yaylı")), // 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.NETIC_CAP))

		);
	}
}
