package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class DaireselDamperConfig {
	public static List<UrunKart> get() {

		return List.of(
				// =====================
				// DAİRESEL H-Y-D DAMPERİ
				// =====================

				new UrunKart("DAIDMP_HAVA", UrunKategori.DAIRESEL_DAMPER, "Dairesel Hava Damperi", List.of(

						OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MONTAJ, List.of("Vidalı", "Spot Yaylı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Contalı", "Manuel Kollu", "Servo Motor 24V", "Servo Motor 230V", "Limit Switch",
										"Klapeli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.CAP)),

				new UrunKart("DAIDMP_GALKLAYAN", UrunKategori.DAIRESEL_DAMPER,
						"Dairesel Galvaniz Klapeli Yangın Damperi", List.of(

								OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MONTAJ, List.of("Vidalı", "Spot Yaylı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Contalı", "Manuel Kollu", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.CAP)),

				new UrunKart("DAIDMP_YAN", UrunKategori.DAIRESEL_DAMPER,
						"Dairesel Yangın Damperi (EN-1366/2 Belgeli, CA-SI Klapeli, Intumescent Contalı)",
						List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ), Map.of(

								OzellikTipi.MONTAJ, List.of("Vidalı", "Spot Yaylı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Contalı", "Manuel Kollu", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.CAP)),

				new UrunKart("DAIDMP_YANITH", UrunKategori.DAIRESEL_DAMPER,
						"Dairesel Yangın Damperi (LINDAB)(EN-1366/2 Belgeli, CA-SI Klapeli, Intumescent Contalı)",
						List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ), Map.of(

								OzellikTipi.MONTAJ, List.of("Vidalı", "Spot Yaylı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Contalı", "Manuel Kollu", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.CAP)),

				new UrunKart("DAIDMP_BLADRA", UrunKategori.DAIRESEL_DAMPER, "Black Draft Damper",
						List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ), Map.of(

								OzellikTipi.MONTAJ, List.of("Vidalı", "Spot Yaylı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Contalı", "Manuel Kollu", "Servo Motor 24V", "Servo Motor 230V", "Limit Switch",
										"Klapeli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.CAP))

		);
	}
}
