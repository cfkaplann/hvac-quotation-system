package com.teklif.api;

import com.teklif.model.*;
import com.teklif.model.dto.PricingRequest;
import com.teklif.pricing.PricingService;
import com.teklif.pricing.dto.PricingResult;
import com.teklif.pricing.builder.PricingRequestMapper;
import com.teklif.repository.UrunKataloguDeposu;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/urunler")
public class UrunlerRestController {

    private final PricingService pricingService = new PricingService();

    @GetMapping
    public ResponseEntity<?> listele() {
        List<Map<String, Object>> result = UrunKataloguDeposu.tumUrunler()
            .stream().map(this::urunToMap).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{kod}")
    public ResponseEntity<?> getir(@PathVariable String kod) {
        UrunKart u = UrunKataloguDeposu.bul(kod);
        if (u == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(urunToMap(u));
    }

    @PostMapping("/{kod}/fiyat")
    public ResponseEntity<?> fiyatHesapla(
            @PathVariable String kod,
            @RequestBody Map<String, Object> body) {
        try {
            UrunKart urun = UrunKataloguDeposu.bul(kod);
            if (urun == null) return ResponseEntity.notFound().build();

            // Ölçüleri parse et
            Map<OlcuAlanTipi, Double> olcular = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> olcularRaw = (Map<String, Object>) body.getOrDefault("olcular", Map.of());
            for (Map.Entry<String, Object> e : olcularRaw.entrySet()) {
                try {
                    OlcuAlanTipi tip = OlcuAlanTipi.valueOf(e.getKey());
                    olcular.put(tip, Double.parseDouble(e.getValue().toString()));
                } catch (Exception ignored) {}
            }

            // String ölçüler (KASA_WH, BOGAZ_WH vb) için
            @SuppressWarnings("unchecked")
            Map<String, Object> strOlcularRaw = (Map<String, Object>) body.getOrDefault("stringOlcular", Map.of());

            PricingRequest.Builder builder = PricingRequest.builder(urun.getKod());
            PricingRequestMapper.apply(builder, olcular);

            // KASA_WH / BOGAZ_WH string olarak gönderilirse
            String kasaWh  = (String) strOlcularRaw.get("KASA_WH");
            String bogazWh = (String) strOlcularRaw.get("BOGAZ_WH");
            String kasaCap = (String) strOlcularRaw.get("KASA_CAP");
            String bogazCap = (String) strOlcularRaw.get("BOGAZ_CAP");

            if (kasaWh != null || bogazWh != null) {
                builder.disBogaz(kasaWh != null ? kasaWh : "", bogazWh != null ? bogazWh : "");
            }
            if (kasaCap != null) builder.kasaCap(kasaCap);
            if (bogazCap != null) {
                try { builder.diameter(Double.parseDouble(bogazCap.replace("Ø","").trim())); } catch (Exception ignored) {}
            }

            PricingRequest req = builder.build();

            // Özellikleri parse et
            Map<OzellikTipi, List<String>> secimler = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<String, Object> ozelliklerRaw = (Map<String, Object>) body.getOrDefault("ozellikler", Map.of());
            for (Map.Entry<String, Object> e : ozelliklerRaw.entrySet()) {
                try {
                    OzellikTipi tip = OzellikTipi.valueOf(e.getKey());
                    Object val = e.getValue();
                    if (val instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<String> liste = (List<String>) val;
                        if (!liste.isEmpty()) secimler.put(tip, liste);
                    } else if (val != null && !val.toString().isEmpty()) {
                        secimler.put(tip, List.of(val.toString()));
                    }
                } catch (Exception ignored) {}
            }

            PricingResult result = pricingService.fiyatHesapla(req, secimler);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("hamFiyat",   result.getHamFiyat());
            resp.put("oranEkleri", result.getOranEkleri());
            resp.put("motorEk",    result.getMotorEk());
            resp.put("toplam",     result.getToplam());
            resp.put("urunAdi",    urun.getAd());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("hata", e.getMessage() != null ? e.getMessage() : "Fiyat hesaplanamadı"));
        }
    }

    private Map<String, Object> urunToMap(UrunKart u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("kod",      u.getKod());
        m.put("ad",       u.getAd());
        m.put("kategori", u.getKategori().name());

        // Zorunlu ölçüler
        List<String> zorunluOlcular = u.getZorunluOlculer() != null
            ? u.getZorunluOlculer().stream().map(Enum::name).collect(Collectors.toList())
            : List.of();
        m.put("zorunluOlcular", zorunluOlcular);

        // İzinli ölçü değerleri (combo için)
        Map<String, List<String>> izinliOlcuDegerleri = new LinkedHashMap<>();
        if (u.getIzinliOlcuDegerleri() != null) {
            for (Map.Entry<OlcuAlanTipi, List<String>> e : u.getIzinliOlcuDegerleri().entrySet()) {
                izinliOlcuDegerleri.put(e.getKey().name(), e.getValue());
            }
        }
        m.put("izinliOlcuDegerleri", izinliOlcuDegerleri);

        // Boğaz filtre map (kasa -> izinli boğazlar)
        Map<String, List<String>> bogazFiltreMap = new LinkedHashMap<>();
        if (u.getBogazFiltreMap() != null) {
            bogazFiltreMap.putAll(u.getBogazFiltreMap());
        }
        m.put("bogazFiltreMap", bogazFiltreMap);

        // Özellikler
        Map<String, List<String>> ozellikler = new LinkedHashMap<>();
        if (u.getOzellikler() != null) {
            for (OzellikTipi tip : u.getOzellikler()) {
                ozellikler.put(tip.name(), u.getIzinliSecimler() != null
                    ? u.getIzinliSecimler().getOrDefault(tip, List.of()) : List.of());
            }
        }
        m.put("ozellikler", ozellikler);
        m.put("multiSelectOzellikler",
            u.getOzellikler() != null
                ? u.getOzellikler().stream().filter(OzellikTipi::isMultiSelect).map(Enum::name).collect(Collectors.toList())
                : List.of());

        return m;
    }
}
