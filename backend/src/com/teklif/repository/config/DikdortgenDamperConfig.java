package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class DikdortgenDamperConfig {
	public static List<UrunKart> get() {

		return List.of(
				// =====================
				// DİKDÖRTGEN H-Y-D DAMPERİ
				// =====================

				new UrunKart("DMP_HAVA", UrunKategori.DIKDORTGEN_DAMPER, "Hava Damperi",
						List.of(OzellikTipi.MENFEZ_TIPI,

								OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MENFEZ_TIPI, List.of("Bronz Yataklı", "PVC Makaralı "),

								OzellikTipi.MONTAJ, List.of("Hava Kanalına Montaj"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Contalı", "Manuel Kollu", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("DMP_BLADRA", UrunKategori.DIKDORTGEN_DAMPER, "Black Draft Damper",
						List.of(OzellikTipi.MENFEZ_TIPI,

								OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MENFEZ_TIPI, List.of("Dörtgen Kesitli", "Aksiyal Fan Tipi "),

								OzellikTipi.MONTAJ, List.of("Hava Kanalına Montaj"),

								OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("DMP_RELI", UrunKategori.DIKDORTGEN_DAMPER, "Relief Damper", List.of(

						OzellikTipi.RAL, OzellikTipi.MENFEZ_TIPI,

						OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MENFEZ_TIPI, List.of("Dörtgen Kesitli", "Şaft Tipi "),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Hava Kanalına Montaj"),

								OzellikTipi.AKSESUAR_TIPI, List.of("Galvaniz Telli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK),
						true   // ⭐ PANJUR = AKSESUAR TEKLİ
						),

				new UrunKart("DMP_YAN", UrunKategori.DIKDORTGEN_DAMPER, "Yangın Damperi", List.of(

						OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MONTAJ, List.of("Hava Kanalına Montaj", "Duvar Geçiş Parçası İle"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Sigortalı", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("DMP_YANBEL", UrunKategori.DIKDORTGEN_DAMPER, "Yangın Damperi (EN-1366/2 Belgeli)",
						List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ), Map.of(

								OzellikTipi.MONTAJ, List.of("Hava Kanalına Montaj", "Duvar Geçiş Parçası İle"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Sigortalı", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("DMP_DUMTAH", UrunKategori.DIKDORTGEN_DAMPER, "Duman Tahliye Damperi",
						List.of(OzellikTipi.MENFEZ_TIPI,

								OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MENFEZ_TIPI, List.of("Kanal Tipi", "Şaft", "Koridor Şaft"),

								OzellikTipi.MONTAJ, List.of("Hava Kanalına Montaj", "Şaft Duvarı Üzerine Vidalı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Sigortalı", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("DMP_DUMTAHBEL", UrunKategori.DIKDORTGEN_DAMPER,
						"Duman Tahliye Damperi (EN-1366/10 Belgeli)", List.of(OzellikTipi.MENFEZ_TIPI,

								OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.MENFEZ_TIPI, List.of("Kanal Tipi", "Şaft", "Koridor Şaft"),

								OzellikTipi.MONTAJ, List.of("Hava Kanalına Montaj", "Şaft Duvarı Üzerine Vidalı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Sigortalı", "Servo Motor 24V", "Servo Motor 230V",
										"Limit Switch")), // 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK))

		);
	}
}
