package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class MenfezConfig {

	public static List<UrunKart> get() {

		return List.of(

				// =====================
				// MENFEZ
				// =====================

				new UrunKart("MNZ_TEKSIRA", UrunKategori.MENFEZ, "Tek Sıra Kanatlı Menfez", List.of(

						OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm", "22 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_CIFTSIRA", UrunKategori.MENFEZ, "Çift Sıra Kanatlı Menfez", List.of(

						OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm", "22 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_DAITEKSIRA", UrunKategori.MENFEZ, "Dairesel Tip Tek Sıra Kanatlı Menfez",
						List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL,
								OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm", "22 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Galvaniz Telli", "Alüminyum Telli", "G2 Elyaf Filtreli",
										"Poliüretan Filtreli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK, OlcuAlanTipi.CAP)),

				new UrunKart("MNZ_DAICIFTSIRA", UrunKategori.MENFEZ, "Dairesel Tip Çift Sıra Kanatlı Menfez", List.of(

						OzellikTipi.CERCEVE_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI,
						OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm", "22 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Galvaniz Telli", "Alüminyum Telli", "G2 Elyaf Filtreli",
										"Poliüretan Filtreli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK, OlcuAlanTipi.CAP)),

				new UrunKart("MNZ_KARPET", UrunKategori.MENFEZ, "Kare Petek Menfez",
						List.of(OzellikTipi.AKSESUAR_TIPI, OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL,
								OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı", "Clip-in"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Galvaniz Telli", "Alüminyum Telli", "G2 Elyaf Filtreli",
										"Poliüretan Filtreli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_KAPTRA", UrunKategori.MENFEZ, "Kapı Transfer Menfezi", List.of(

						OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL,

						OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("22 mm"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_LINE", UrunKategori.MENFEZ, "Lineer Menfez",
						List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL,
								OzellikTipi.DAMPER_TIPI, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm", "22 mm", "17 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.MENFEZ_TIPI, List.of("17° Açılı", "30° Açılı", "Damla Kanatlı "),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Galvaniz Telli", "Alüminyum Telli", "G2 Elyaf Filtreli",
										"Poliüretan Filtreli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_LIFTUT", UrunKategori.MENFEZ, "Lif Tutucu Menfez", List.of(

						OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL, OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("28 mm"),

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.RAL, List.of("AISI 304 Paslanmaz Çelik "),

								OzellikTipi.MONTAJ, List.of("Vidalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_PERF", UrunKategori.MENFEZ, "Perfore Menfez", List.of(

						OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL, OzellikTipi.AKSESUAR_TIPI, OzellikTipi.MONTAJ),
						Map.of(OzellikTipi.CERCEVE_TIPI, List.of("32 mm", "28 mm"),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı"),

								OzellikTipi.MONTAJ, List.of("Vidalı", "Klipsli", "Sustalı", "Clip-in"),

								OzellikTipi.AKSESUAR_TIPI,
								List.of("Galvaniz Telli", "Alüminyum Telli", "G2 Elyaf Filtreli",
										"Poliüretan Filtreli")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK)),

				new UrunKart("MNZ_YERLINE", UrunKategori.MENFEZ, "Yer Tipi Lineer Menfez",
						List.of(OzellikTipi.MENFEZ_TIPI, OzellikTipi.CERCEVE_TIPI, OzellikTipi.RAL,
								OzellikTipi.DAMPER_TIPI, OzellikTipi.MONTAJ),
						Map.of(

								OzellikTipi.DAMPER_TIPI, List.of("Dampersiz", "Paralel Damperli", "Zıt Damperli"),

								OzellikTipi.MENFEZ_TIPI, List.of("Ağır Tip", "25 mm Çerçeveli", "50 mm Çerçeveli "),

								OzellikTipi.RAL, List.of("Boyasız", "Eloksal", "Boyalı")),
						// 🔥 ZORUNLU ÖLÇÜLER
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK))

		);
	}
}
