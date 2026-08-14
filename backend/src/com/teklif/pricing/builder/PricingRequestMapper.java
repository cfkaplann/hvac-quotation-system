package com.teklif.pricing.builder;

import java.util.*;

import com.teklif.model.OlcuAlanTipi;
import com.teklif.model.dto.PricingRequest;

public class PricingRequestMapper {

    public static void apply(
            PricingRequest.Builder builder,
            Map<OlcuAlanTipi, Double> values){

        Double w = values.get(OlcuAlanTipi.GENISLIK);
        Double h = values.get(OlcuAlanTipi.YUKSEKLIK);

        if (w != null && h != null) {
            builder.wh(w, h);
        } else if (w != null) {
            builder.wh(w, 0.0);
        } else if (h != null) {
            // YUKSEKLIK var ama GENISLIK yok (BOX_LS gibi)
            builder.wh(0.0, h);
        }

        Double l    = values.get(OlcuAlanTipi.UZUNLUK);
        Double slot = values.get(OlcuAlanTipi.SLOT_SAYISI);

        if (l != null && slot != null) {
            builder.lSlot(l, slot.intValue());
        }

        Double cap = firstNotNull(
                values.get(OlcuAlanTipi.CAP),
                values.get(OlcuAlanTipi.BOGAZ_CAP),
                values.get(OlcuAlanTipi.NETIC_CAP));

        if (cap != null) {
            builder.diameter(cap);
        }
    }

    private static Double firstNotNull(Double... vals) {
        for (Double v : vals) {
            if (v != null) return v;
        }
        return null;
    }
}
