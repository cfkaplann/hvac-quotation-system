package com.teklif.pricing.dto;

import java.util.Map;

public class PricingResult {

    private final double hamFiyat;
    private final double oranEkleri;
    private final double motorEk;
    private final double toplam;

    // ⭐ YENİ → detaylı kırılım
    private final Map<String, Double> detaylar;

    // ==========================================
    // ESKİ CONSTRUCTOR (SİLME)
    // ==========================================
    public PricingResult(double hamFiyat,
                         double oranEkleri,
                         double motorEk,
                         double toplam){

        this(hamFiyat,oranEkleri,motorEk,toplam,null);
    }

    // ==========================================
    // ⭐ YENİ CONSTRUCTOR
    // ==========================================
    public PricingResult(double hamFiyat,
                         double oranEkleri,
                         double motorEk,
                         double toplam,
                         Map<String,Double> detaylar){

        this.hamFiyat = hamFiyat;
        this.oranEkleri = oranEkleri;
        this.motorEk = motorEk;
        this.toplam = toplam;
        this.detaylar = detaylar;
    }

    public double getHamFiyat(){
        return hamFiyat;
    }

    public double getOranEkleri(){
        return oranEkleri;
    }

    public double getMotorEk(){
        return motorEk;
    }

    public double getToplam(){
        return toplam;
    }

    // ⭐ YENİ GETTER
    public Map<String,Double> getDetaylar(){
        return detaylar;
    }

    @Override
    public String toString(){

        String base =
                "PricingResult{" +
                "hamFiyat=" + hamFiyat +
                ", oranEkleri=" + oranEkleri +
                ", motorEk=" + motorEk +
                ", toplam=" + toplam +
                '}';

        if(detaylar==null || detaylar.isEmpty())
            return base;

        StringBuilder sb = new StringBuilder(base);
        sb.append("\nDETAYLAR:\n");

        for(String k:detaylar.keySet()){
            sb.append(k).append(" -> +").append(detaylar.get(k)).append("\n");
        }

        return sb.toString();
    }
}
