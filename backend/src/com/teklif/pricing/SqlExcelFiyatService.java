package com.teklif.pricing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.teklif.model.OlcuTipi;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.factory.StrategyFactory;
import com.teklif.pricing.strategy.PricingStrategy;
import com.teklif.repository.config.OlcuTipiResolver;

public class SqlExcelFiyatService {

    // ✅ basit, hızlı cache
    private static final Map<String, Double> HAM_FIYAT_CACHE = new ConcurrentHashMap<>();

    public double hamFiyatGetir(PricingRequest req){

        String key = req.buildCacheKey();

        Double cached = HAM_FIYAT_CACHE.get(key);
        if (cached != null) {
            return cached;
        }

        String sheet = req.getSheetName();

        OlcuTipi tip = OlcuTipiResolver.resolve(sheet);
        PricingStrategy strategy = StrategyFactory.getStrategy(tip);

        double price = strategy.execute(req);

        HAM_FIYAT_CACHE.put(key, price);
        return price;
    }

    // İstersen cache temizleme (ürün fiyat tabloları değişirse)
    public static void clearCache(){
        HAM_FIYAT_CACHE.clear();
    }
}