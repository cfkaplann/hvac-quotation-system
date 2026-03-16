package com.teklif.model;

public enum ParaBirimi {

    TL(" TL"),
    EUR(" EUR"),
    USD(" USD");

    private final String symbol;

    ParaBirimi(String symbol){
        this.symbol = symbol;
    }

    public String getSymbol(){
        return symbol;
    }
}