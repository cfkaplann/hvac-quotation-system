package com.teklif.controller;

import java.util.List;
import java.util.Map;

import com.teklif.model.OzellikTipi;
import com.teklif.model.ParaBirimi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.KurService;
import com.teklif.pricing.PricingService;
import com.teklif.pricing.dto.PricingResult;

public class TeklifController {

    private PricingService pricingService = new PricingService();

    // =====================================================
    // ⭐ CANLI FİYAT
    // =====================================================

    public PricingResult hesapla(PricingRequest request,
                                 Map<OzellikTipi, List<String>> secimler){

        return pricingService.fiyatHesapla(request, secimler);
    }
    
 // =====================================================
 // ⭐ MOTOR FİYATLI HESAPLA (YENİ)
 // =====================================================

 public PricingResult hesapla(PricingRequest request,
                              Map<OzellikTipi, List<String>> secimler,
                              Map<String, Double> motorFiyatlari){

     return pricingService.fiyatHesapla(request, secimler, motorFiyatlari);
 }
 public PricingResult hesapla(
	        PricingRequest request,
	        Map<OzellikTipi, List<String>> secimler,
	        Map<String, Double> motorFiyatlari,
	        ParaBirimi pb){

	 PricingResult tlResult =
		        hesapla(request, secimler, motorFiyatlari);
	 

	    double ham = KurService.cevir(tlResult.getHamFiyat(), pb);
	    double oran = KurService.cevir(tlResult.getOranEkleri(), pb);
	    double motor = KurService.cevir(tlResult.getMotorEk(), pb);
	    double toplam = KurService.cevir(tlResult.getToplam(), pb);

	    return new PricingResult(
	            ham,
	            oran,
	            motor,
	            toplam
	    );
	}
}
