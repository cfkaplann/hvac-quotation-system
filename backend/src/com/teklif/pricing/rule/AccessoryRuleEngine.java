package com.teklif.pricing.rule;

import java.util.List;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class AccessoryRuleEngine {

    public static boolean motorFiyatiGoster(
            UrunKart kart,
            List<String> aksesuarlar){

        if(kart == null || aksesuarlar == null)
            return false;

        boolean damperMi =
            kart.getKategori() == UrunKategori.DIKDORTGEN_DAMPER ||
            kart.getKategori() == UrunKategori.DAIRESEL_DAMPER;

        if(!damperMi)
            return false;

        return aksesuarlar.contains("Servo Motor 24V") ||
               aksesuarlar.contains("Servo Motor230V") ||
               aksesuarlar.contains("Limit Switch");
    }
}
