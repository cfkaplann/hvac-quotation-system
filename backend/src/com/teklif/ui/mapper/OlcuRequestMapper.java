package com.teklif.ui.mapper;

import java.util.List;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.ui.component.OlcuComponent;

public class OlcuRequestMapper {
	
	private static final boolean DEBUG = false;

	public static void map(PricingRequest.Builder builder, List<OlcuComponent> comps) {

		Double w = null, h = null, l = null, diameter = null;
		Integer slot = null;

		String dis = null, bogaz = null;

		for (OlcuComponent c : comps) {

			OlcuAlanTipi tip = c.getTip();

			switch (tip) {

			case GENISLIK:
				w = c.getDoubleValue();
				break;

			case YUKSEKLIK:
				h = c.getDoubleValue();
				break;

			case UZUNLUK:
				l = c.getDoubleValue();
				break;

			case SLOT_SAYISI:
				slot = (int) c.getDoubleValue();
				break;

			case CAP:
			case BOGAZ_CAP:
			case NETIC_CAP:
				diameter = c.getDoubleValue();
				break;

			case KASA_WH:
				String rawDis = c.getValue();

				if (rawDis != null) {
					rawDis = rawDis.replace("[", "").replace("]", "");
				}

				dis = rawDis;
				break;

			case KASA_CAP:

			    String cap = c.getValue();

			    if(cap != null){
			        cap = cap.replace("[","").replace("]","");
			    }

			    // ⭐⭐⭐ STRATEGY disOlcu okuyor
			    dis = cap;

			    // ayrıca builder'a da yaz
			    builder.kasaCap(cap);

			    break;


			case BOGAZ_WH:
				String raw = c.getValue();

				if (raw != null) {
					raw = raw.replace("[", "").replace("]", "");
				}

				bogaz = raw;
				break;
			}

			if (DEBUG) {
			    System.out.println("TIP = " + tip + " VALUE = [" + c.getValue() + "]");
			}

		}
		// =====================================
		// 0'DAN BÜYÜK VALIDATION
		// =====================================

		if (w != null && w <= 0)
			throw new RuntimeException("Genişlik 0'dan büyük olmalı");
		if (h != null && h <= 0)
			throw new RuntimeException("Yükseklik 0'dan büyük olmalı");
		if (l != null && l <= 0)
			throw new RuntimeException("Uzunluk 0'dan büyük olmalı");
		if (diameter != null && diameter <= 0)
			throw new RuntimeException("Çap 0'dan büyük olmalı");

		// =====================
		// BUILDER SET
		// =====================

		if (w != null && h != null)
			builder.wh(w, h);

		if (l != null && slot != null)
			builder.lSlot(l, slot);

		if (diameter != null)
			builder.diameter(diameter);

		// STRING ÖLÇÜLER HER ZAMAN GÖNDER
		if (dis != null || bogaz != null) {
			builder.disBogaz(dis, bogaz);
		}

	}
}
