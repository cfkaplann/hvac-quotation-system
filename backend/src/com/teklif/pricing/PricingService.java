package com.teklif.pricing;

import com.teklif.importer.ExcelHotReloadService;

import java.util.*;

import com.teklif.model.OzellikTipi;
import com.teklif.model.ParaBirimi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.dto.PricingResult;
import com.teklif.repository.OzellikDeposu;
import com.teklif.model.OzellikOran;

public class PricingService {
	
	private static final Map<String, Double> AKSESUAR_EURO = Map.of(
		    "Sigortalı", 2.0,
		    "Limit Switch", 6.0
		);

    private SqlExcelFiyatService sqlService = new SqlExcelFiyatService();

    public PricingResult fiyatHesapla(
    		
            PricingRequest req,
            Map<OzellikTipi,List<String>> secimler){
    	
    	// ⭐ Excel değişti mi kontrol et
        ExcelHotReloadService.checkAndReload();

        double hamFiyat = sqlService.hamFiyatGetir(req);

        double oranEkleri = 0;
        double motorEk = 0;

        if (secimler != null) {

            for(OzellikTipi tip : secimler.keySet()){

                Map<String, OzellikOran> oranMap =
                        OzellikDeposu.oranlariGetir(tip);

                if(oranMap == null) continue;

                for(String secim : secimler.get(tip)){

                    OzellikOran oran = oranMap.get(secim);

                    if(oran == null) continue;

                    if(!oran.isSabit()){
                        oranEkleri += hamFiyat * oran.getOran();
                    }
                }
            }
        }

        List<String> aksesuarlar = null;

        if (secimler != null) {
            aksesuarlar =
                    secimler.get(OzellikTipi.AKSESUAR_TIPI);
        }

        if(aksesuarlar != null){

            boolean motorVar =
                    aksesuarlar.contains("Servo Motor 24V") ||
                    aksesuarlar.contains("Servo Motor230V");

            if(motorVar && req.getMotorFiyati()!=null){
                motorEk = req.getMotorFiyati() * 1.1;
            }
        }

        double toplam = hamFiyat + oranEkleri + motorEk;
        
     // ----------------------------------------------------
     // DAMPPER AKSESUAR SABİT EURO EK (TOPLAMA EKLENİR)
     // ----------------------------------------------------

     // ----------------------------------------------------
     // SADECE DAMPERLERDE SABİT AKSESUAR EK
     // ----------------------------------------------------

        String sheet = req.getSheetName();

        if (sheet != null &&
                (sheet.startsWith("DMP")
                || sheet.startsWith("DAIDMP"))) {



         if (aksesuarlar != null) {

             for (String a : aksesuarlar) {

                 double euro =
                         AKSESUAR_EURO.getOrDefault(a, 0.0);

                 if (euro > 0) {
                     toplam +=
                             KurService.cevir(euro, ParaBirimi.EUR);
                 }
             }
         }
     }

        return new PricingResult(
                hamFiyat,
                oranEkleri,
                motorEk,
                toplam
        );
    }
 // =====================================================
 // ⭐ MOTOR MAP DESTEKLİ YENİ HESAP
 // =====================================================
 public PricingResult fiyatHesapla(
         PricingRequest req,
         Map<OzellikTipi,List<String>> secimler,
         Map<String,Double> motorFiyatlari){

     PricingResult base = fiyatHesapla(req, secimler);

     double motorToplam = 0;

     if(motorFiyatlari != null){
         for(Double d : motorFiyatlari.values()){
        	 motorToplam += d * 1.10;   // ⭐ %10 fazlası
         }
     }

     double yeniToplam =
    	        base.getHamFiyat()
    	        + base.getOranEkleri()
    	        + motorToplam;

     return new PricingResult(
             base.getHamFiyat(),
             base.getOranEkleri(),
             motorToplam,
             yeniToplam
     );
 }
}
