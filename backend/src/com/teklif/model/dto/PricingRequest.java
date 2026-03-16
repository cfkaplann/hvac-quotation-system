package com.teklif.model.dto;

public class PricingRequest {

    private final String sheetName;

    private final Double w;
    private final Double h;
    private String kasaCap;

    private final Double l;
    private final Integer slot;

    private final Double diameter;

    // QUA / KSD gibi string ölçüler için (şimdilik 2D)
    private final String disOlcu;   // "295x295"
    private final String bogazOlcu; // "150x150" veya "Ø250"

    // ✅ YENİ EKLENDİ (Motor manuel fiyatı)
    private final Double motorFiyati;

    private PricingRequest(Builder b) {
        this.sheetName = b.sheetName;
        this.w = b.w; this.h = b.h;
        this.l = b.l; this.slot = b.slot;
        this.diameter = b.diameter;
        this.disOlcu = b.disOlcu;
        this.bogazOlcu = b.bogazOlcu;
        this.kasaCap =b.kasaCap;

        // ✅ YENİ
        this.motorFiyati = b.motorFiyati;
    }

    public String getSheetName() { return sheetName; }
    public Double getW() { return w; }
    public Double getH() { return h; }
    public Double getL() { return l; }
    public Integer getSlot() { return slot; }
    public Double getDiameter() { return diameter; }
    public String getDisOlcu() { return disOlcu; }
    public String getBogazOlcu() { return bogazOlcu; }

    // ✅ YENİ GETTER
    public Double getMotorFiyati(){ return motorFiyati; }

    public static Builder builder(String sheetName) { return new Builder(sheetName); }

    public static class Builder {
        private final String sheetName;
        private Double w, h, l, diameter;
        private Integer slot;
        private String disOlcu, bogazOlcu,kasaCap;

        // ✅ YENİ
        private Double motorFiyati;

        public Builder(String sheetName) { this.sheetName = sheetName; }

        public Builder wh(double w, double h) { this.w = w; this.h = h; return this; }
        public Builder lSlot(double l, int slot) { this.l = l; this.slot = slot; return this; }
        public Builder diameter(double d) { this.diameter = d; return this; }
        public Builder disBogaz(String dis, String bogaz) { this.disOlcu = dis; this.bogazOlcu = bogaz; return this; }
        public Builder kasaCap(String val){
            this.kasaCap = val;
            return this;
        }
        // ✅ YENİ BUILDER METHOD
        public Builder motorFiyati(double f){
            this.motorFiyati = f;
            return this;
        }

        public PricingRequest build() { return new PricingRequest(this); }
    }
    public String getKasaCap(){
        return kasaCap;
    }
    public String buildCacheKey() {

        // null-safe key
        return String.valueOf(sheetName) + "|" +
                "w=" + String.valueOf(w) + "|" +
                "h=" + String.valueOf(h) + "|" +
                "l=" + String.valueOf(l) + "|" +
                "slot=" + String.valueOf(slot) + "|" +
                "d=" + String.valueOf(diameter) + "|" +
                "dis=" + String.valueOf(disOlcu) + "|" +
                "bogaz=" + String.valueOf(bogazOlcu) + "|" +
                "kasaCap=" + String.valueOf(kasaCap);
    }
}
