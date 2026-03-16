package com.teklif.model;

public enum UrunKategori {

    MENFEZ("Menfez"),
    SLOT("Slot Difüzör"),

    DIKDORTGEN_DAMPER("Dikdörtgen H-Y-D Damperi"),
    DAIRESEL_DAMPER("Dairesel H-Y-D Damperi"),

    KARE_ANEMOSTAD("Kare Anemostad"),
    DAIRESEL_ANEMOSTAD("Dairesel Anemostad"),

    KARE_SWIRL("Kare Swirl Difüzör"),
    DAIRESEL_SWIRL("Dairesel Swirl Difüzör"),

    PANJUR("Panjur"),
    KAPAK("Kapak"),
    
    KUTU("Kutu");

    private final String displayName;

    UrunKategori(String displayName){
        this.displayName = displayName;
    }

    public String getDisplayName(){
        return displayName;
    }

    @Override
    public String toString(){
        return displayName; // 🔥 JComboBox otomatik güzel isim gösterir
    }
}
