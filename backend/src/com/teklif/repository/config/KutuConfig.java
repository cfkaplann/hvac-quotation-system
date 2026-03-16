package com.teklif.repository.config;

import java.util.List;
import java.util.Map;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.OzellikTipi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class KutuConfig {

	private static Map<OlcuAlanTipi, String> kasaLabel() {
		return Map.of(OlcuAlanTipi.KASA_WH, "AxA");
	}

	public static List<UrunKart> get() {

		return List.of(

				// =====================
				// KUTU
				// =====================

				new UrunKart("BOX_WH", UrunKategori.KUTU, "Menfez Kutusu", List.of(OzellikTipi.AKSESUAR_TIPI), Map.of(

						OzellikTipi.AKSESUAR_TIPI, List.of("İç İzoleli", "Dış İzoleli")),
						List.of(OlcuAlanTipi.GENISLIK, OlcuAlanTipi.YUKSEKLIK), false),

				new UrunKart("BOX_LS", UrunKategori.KUTU, "Slot Difüzör Kutusu", List.of(OzellikTipi.AKSESUAR_TIPI),
						Map.of(

								OzellikTipi.AKSESUAR_TIPI, List.of("İç İzoleli", "Dış İzoleli")),
						List.of(OlcuAlanTipi.UZUNLUK, OlcuAlanTipi.SLOT_SAYISI), false),

				new UrunKart("BOX_STR", UrunKategori.KUTU, "Kare Amemostad Planum Box",
						List.of(OzellikTipi.AKSESUAR_TIPI), Map.of(

								OzellikTipi.AKSESUAR_TIPI, List.of("İç İzoleli", "Dış İzoleli")),
						List.of(OlcuAlanTipi.KASA_WH),

						Map.of(OlcuAlanTipi.KASA_WH, List.of("150x150", "225x225", "300x300", "375x375",

								"450x450", "525x525", "600x600")),

						false, null, kasaLabel())

		);
	}

}
