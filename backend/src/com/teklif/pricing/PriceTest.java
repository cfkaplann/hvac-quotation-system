package com.teklif.pricing;

import java.util.*;

import com.teklif.model.OzellikTipi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.dto.PricingResult;

public class PriceTest {

    public static void main(String[] args) {

        PricingService service = new PricingService();

        Map<OzellikTipi, List<String>> ozellikler = new HashMap<>();
        ozellikler.put(OzellikTipi.RAL, List.of("Boyalı"));
        ozellikler.put(OzellikTipi.DAMPER_TIPI, List.of("Zıt Damperli"));

        // =========================
        // MENFEZ TEST
        // =========================

        PricingRequest req = PricingRequest
                .builder("MNZ_TEKSIRA")
                .wh(4500, 180)
                .build();

        PricingResult result = service.fiyatHesapla(req, ozellikler);

        System.out.println("HAM = " + result.getHamFiyat());
        System.out.println("ORAN = " + result.getOranEkleri());
        System.out.println("TOPLAM = " + result.getToplam());

        // =========================
        // Ø DAMPER TEST
        // =========================

        PricingRequest dReq = PricingRequest
                .builder("DAIDMP_YAN")
                .diameter(235)
                .build();

        PricingResult dResult = service.fiyatHesapla(dReq, ozellikler);

        System.out.println("Ø HAM = " + dResult.getHamFiyat());
        System.out.println("Ø TOPLAM = " + dResult.getToplam());
    }
}
