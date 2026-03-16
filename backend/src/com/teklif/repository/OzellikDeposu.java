package com.teklif.repository;

import java.util.HashMap;
import java.util.Map;

import com.teklif.model.OzellikTipi;
import com.teklif.model.OzellikOran;

public class OzellikDeposu {

	private static final Map<OzellikTipi, Map<String, OzellikOran>> oranlar = new HashMap<>();

	static {

		// ÇERÇEVE TİPİ
		Map<String, OzellikOran> cerceve = new HashMap<>();
		cerceve.put("32 mm", new OzellikOran(0.15, false));
		cerceve.put("28 mm", new OzellikOran(0.10, false));
		cerceve.put("22 mm", new OzellikOran(0.05, false));
		cerceve.put("17 mm", new OzellikOran(0.00, true)); // SABİT
		oranlar.put(OzellikTipi.CERCEVE_TIPI, cerceve);

		// DAMPER
		Map<String, OzellikOran> damper = new HashMap<>();
		damper.put("Dampersiz", new OzellikOran(0.0, true)); // SABİT
		damper.put("Paralel Damperli", new OzellikOran(0.40, false));
		damper.put("Zıt Damperli", new OzellikOran(0.50, false));
		oranlar.put(OzellikTipi.DAMPER_TIPI, damper);

		// RAL
		Map<String, OzellikOran> ral = new HashMap<>();
		ral.put("Boyalı", new OzellikOran(0.15, false));
		ral.put("Eloksal", new OzellikOran(0.15, false));
		ral.put("Boyasız", new OzellikOran(0.0, true)); // SABİT
		ral.put("AISI 304 Paslanmaz Çelik", new OzellikOran(0.0, true)); 
		oranlar.put(OzellikTipi.RAL, ral);

		// MONTAJ
		Map<String, OzellikOran> montaj = new HashMap<>();
		montaj.put("Vidalı", new OzellikOran(0.0, true)); // SABİT
		montaj.put("Klipsli", new OzellikOran(0.05, false));
		montaj.put("Köprülü", new OzellikOran(0.05, false));
		montaj.put("Clip-in", new OzellikOran(0.0, true));
		montaj.put("Sustalı", new OzellikOran(0.05, false));
		montaj.put("Lay-in", new OzellikOran(0.0, true));
		montaj.put("Karolaj", new OzellikOran(0.0, true));
		montaj.put("Gizli Vidalı", new OzellikOran(0.0, true));
		montaj.put("Boğazdan Vidalı", new OzellikOran(0.0, true));
		montaj.put("Hava Kanalına Montaj", new OzellikOran(0.0, true));
		montaj.put("Duvar Geçiş Parçası İle Montaj", new OzellikOran(0.0, true));
		montaj.put("Şaft Duvarı Üzerine Vidalı Montaj", new OzellikOran(0.0, true));
		montaj.put("Civatalı", new OzellikOran(0.0, true));

		oranlar.put(OzellikTipi.MONTAJ, montaj);

		// AKSESUAR TİPİ
		Map<String, OzellikOran> aksesuar = new HashMap<>();
		aksesuar.put("Galvaniz Telli", new OzellikOran(0.15, false)); //
		aksesuar.put("Contalı", new OzellikOran(0.02, false));
		aksesuar.put("Manuel Kollu", new OzellikOran(0.00, true));
		aksesuar.put("Servo Motor 24V", new OzellikOran(0.00, true));
		aksesuar.put("Servo Motor 230V", new OzellikOran(0.00, true));
		aksesuar.put("Limit Switch", new OzellikOran(0.00, true));
		aksesuar.put("Klapeli", new OzellikOran(0.00, true));
		aksesuar.put("Sigortalı", new OzellikOran(0.00, true));
		aksesuar.put("Yok", new OzellikOran(0.00, true));
		aksesuar.put("Filtreli", new OzellikOran(0.00, true));
		aksesuar.put("Filtresiz", new OzellikOran(0.00, true));
		aksesuar.put("Poliüretan Filtreli", new OzellikOran(0.25, false));
		aksesuar.put("Alüminyum Telli", new OzellikOran(0.20, false));
		aksesuar.put("G2 Elyaf Filtreli", new OzellikOran(0.15, false));
		aksesuar.put("Dış İzoleli", new OzellikOran(0.15, false));
		aksesuar.put("İç İzoleli", new OzellikOran(0.15, false));
		oranlar.put(OzellikTipi.AKSESUAR_TIPI, aksesuar);

		// MENFEZ TİPİ
		Map<String, OzellikOran> menfez = new HashMap<>();
		menfez.put("17° Açılı", new OzellikOran(0.0, true));
		menfez.put("30° Açılı", new OzellikOran(0.05, false));
		menfez.put("Damla Kanatlı", new OzellikOran(0.15, false));
		menfez.put("İnoks Telli", new OzellikOran(0.0, true));
		menfez.put("Yuvarlak Delikli", new OzellikOran(0.1, false));
		menfez.put("Kare Delikli", new OzellikOran(0.15, false));
		menfez.put("25 mm Çerçeveli", new OzellikOran(0.0, true));
		menfez.put("50 mm Çerçeveli", new OzellikOran(0.05, false));
		menfez.put("Ağır Tip", new OzellikOran(0.10, false));
		menfez.put("Dairesel Kanatlı, Kare Ön Tablalı", new OzellikOran(0.0, true));
		menfez.put("Kare Kanatlı, Kare Ön Tablalı", new OzellikOran(0.0, true));
		menfez.put("Dörtgen Hizalı, Kare Ön Tablalı", new OzellikOran(0.0, true));
		menfez.put("Dairesel Hizalı, Kare Ön Tablalı", new OzellikOran(0.0, true));
		menfez.put("Merkezden Kaçık, Dairesel Kanatlı, Kare Ön Tablalı", new OzellikOran(0.0, true));
		menfez.put("Sabit Kanatlı", new OzellikOran(0.0, true));
		menfez.put("Ayarlanabilir Kanatlıı", new OzellikOran(0.0, true));
		menfez.put("PVC Makaralı", new OzellikOran(0.0, true));
		menfez.put("Bronz Yataklı", new OzellikOran(0.15, false));
		menfez.put("Dörtgen Kesitli", new OzellikOran(0.0, true));
		menfez.put("Şaft Tipi", new OzellikOran(0.05, false));
		menfez.put("Aksiyel Fan Tipi", new OzellikOran(0.15, false));
		menfez.put("Kol Kumandalı", new OzellikOran(0.02, false));
		menfez.put("Tekli Tip", new OzellikOran(0.0, true));
		menfez.put("İkiz Tip", new OzellikOran(0.30, false));
		menfez.put("Alçıpana Montaj", new OzellikOran(0.0, true));
		menfez.put("Ahşaba Montaj", new OzellikOran(0.05, false));
		menfez.put("Kanal Tipi", new OzellikOran(0.0, true));
		menfez.put("Şaft", new OzellikOran(0.0, true));
		menfez.put("Koridor Şaft", new OzellikOran(0.0, true));

		oranlar.put(OzellikTipi.MENFEZ_TIPI, menfez);
	}

	public static Map<String, OzellikOran> oranlariGetir(OzellikTipi tip) {
		return oranlar.get(tip);
	}
}
