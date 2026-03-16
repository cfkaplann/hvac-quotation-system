package com.teklif.pricing.strategy;

import com.teklif.model.dto.PricingRequest;

public interface PricingStrategy {

    double execute(PricingRequest req);
}
