package com.teklif.model;

public class OzellikOran {

    private final double oran;
    private final boolean sabit;

    public OzellikOran(double oran, boolean sabit) {
        this.oran = oran;
        this.sabit = sabit;
    }

    public double getOran() {
        return oran;
    }

    public boolean isSabit() {
        return sabit;
    }
}
