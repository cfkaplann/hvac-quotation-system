package com.teklif.pricing.factory;

import com.teklif.model.OlcuTipi;
import com.teklif.pricing.strategy.*;

public class StrategyFactory {

    public static PricingStrategy getStrategy(OlcuTipi olcuTipi) {

        switch (olcuTipi) {

            case MATRIX_WH:
                return new MatrixWHStrategy();

            case MATRIX_L_SLOT:
                return new MatrixLSlotStrategy();

            case DIAMETER:
                return new DiameterStrategy();
            case STRING_SIZE_SINGLE:
                return new SingleStringSizeStrategy();


            case STRING_SIZE:
                return new StringSizeStrategy();

            default:
                throw new IllegalArgumentException("Bilinmeyen ölçü tipi: " + olcuTipi);
        }
    }
}
