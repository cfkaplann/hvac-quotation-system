package com.teklif.model;

public enum OzellikTipi {

    CERCEVE_TIPI(false),
    MENFEZ_TIPI(false),
    DAMPER_TIPI(false),
    RAL(false),
    MONTAJ(false),

    // ⭐ sadece bu multi select
    AKSESUAR_TIPI(true);

    private final boolean multiSelect;

    OzellikTipi(boolean multiSelect){
        this.multiSelect = multiSelect;
    }

    public boolean isMultiSelect(){
        return multiSelect;
    }
}
