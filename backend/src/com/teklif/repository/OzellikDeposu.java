package com.teklif.repository;

import com.teklif.db.ConnectionManager;
import com.teklif.model.OzellikOran;
import com.teklif.model.OzellikTipi;

import java.sql.*;
import java.util.*;

public class OzellikDeposu {

    // cache: kategori → tip → seçenek → oran
    // kategori = null ise varsayılan (tüm ürünler)
    private static volatile Map<String, Map<OzellikTipi, Map<String, OzellikOran>>> cache = null;

    public static synchronized void invalidate() { cache = null; }

    // Mevcut API — geriye dönük uyumluluk için
    public static Map<String, OzellikOran> oranlariGetir(OzellikTipi tip) {
        return oranlariGetir(tip, null);
    }

    // Yeni API — kategoriye özgü oran, yoksa varsayılan
    public static Map<String, OzellikOran> oranlariGetir(OzellikTipi tip, String kategori) {
        if (cache == null) yukle();

        // Önce kategoriye özgü oranları al
        if (kategori != null && cache.containsKey(kategori)) {
            Map<String, OzellikOran> katOranlar = cache.get(kategori).get(tip);
            if (katOranlar != null && !katOranlar.isEmpty()) {
                // Varsayılan oranlarla birleştir (kategoride olmayan seçenekler varsayılandan gelsin)
                Map<String, OzellikOran> varsayilan = cache.getOrDefault("__DEFAULT__", Map.of())
                                                           .getOrDefault(tip, Map.of());
                Map<String, OzellikOran> sonuc = new LinkedHashMap<>(varsayilan);
                sonuc.putAll(katOranlar); // kategori oranları varsayılanın üzerine yazar
                return sonuc;
            }
        }

        // Varsayılan
        return cache.getOrDefault("__DEFAULT__", Map.of()).getOrDefault(tip, Map.of());
    }

    private static synchronized void yukle() {
        if (cache != null) return;
        Map<String, Map<OzellikTipi, Map<String, OzellikOran>>> yeni = new HashMap<>();

        try (Connection conn = ConnectionManager.getConnection()) {
            // feature_ratio_v2 varsa onu oku, yoksa feature_ratio'yu oku
            boolean v2Var = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, null, "feature_ratio_v2", null)) {
                v2Var = rs.next();
            } catch (Exception ignored) {}

            String sql = v2Var
                ? "SELECT feature_type, option_name, urun_kategori, ratio, is_sabit FROM feature_ratio_v2"
                : "SELECT feature_type, option_name, NULL as urun_kategori, ratio, is_sabit FROM feature_ratio";

            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String tipStr   = rs.getString("feature_type");
                    String secim    = rs.getString("option_name");
                    String kat      = rs.getString("urun_kategori"); // null = varsayılan
                    double ratio    = rs.getDouble("ratio");
                    boolean sabit   = rs.getInt("is_sabit") == 1;

                    OzellikTipi tip;
                    try { tip = OzellikTipi.valueOf(tipStr); } catch (Exception e) { continue; }

                    String cacheKey = (kat != null) ? kat : "__DEFAULT__";
                    yeni.computeIfAbsent(cacheKey, k -> new HashMap<>())
                        .computeIfAbsent(tip, k -> new LinkedHashMap<>())
                        .put(secim, new OzellikOran(ratio, sabit));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        cache = yeni;
    }
}
