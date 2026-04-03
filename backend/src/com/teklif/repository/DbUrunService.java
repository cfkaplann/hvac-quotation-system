package com.teklif.repository;

import com.teklif.db.ConnectionManager;
import com.teklif.model.*;

import java.sql.*;
import java.util.*;

/**
 * Admin panelinden DB'ye eklenen ürünleri yönetir.
 * UrunKataloguDeposu ile entegre çalışır.
 */
public class DbUrunService {

    private static final String OLCU_SEP  = ",";
    private static final String OZ_SEP    = ",";

    // ── DB'deki tüm ürünleri UrunKart olarak döndür ──
    public static List<UrunKart> dbUrunleri() {
        List<UrunKart> liste = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT * FROM urun_tanim WHERE aktif=1")) {
            while (rs.next()) {
                UrunKart k = satiraKart(rs);
                if (k != null) liste.add(k);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }

    // ── Yeni ürün ekle ──
    public static void ekle(String kod, String ad, String kategori,
                             List<String> zorunluOlcular,
                             List<String> ozellikTipleri,
                             String fiyatStratejisi) throws Exception {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO urun_tanim(kod,ad,kategori,zorunlu_olcular,ozellik_tipleri,fiyat_stratejisi) VALUES(?,?,?,?,?,?)")) {
            ps.setString(1, kod);
            ps.setString(2, ad);
            ps.setString(3, kategori);
            ps.setString(4, String.join(OLCU_SEP, zorunluOlcular));
            ps.setString(5, String.join(OZ_SEP, ozellikTipleri));
            ps.setString(6, fiyatStratejisi);
            ps.executeUpdate();
        }
    }

    // ── Ürün sil ──
    public static void sil(String kod) throws Exception {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE urun_tanim SET aktif=0 WHERE kod=?")) {
            ps.setString(1, kod);
            ps.executeUpdate();
        }
    }

    // ── Tüm DB ürünlerini listele (admin için) ──
    public static List<Map<String,Object>> dbUrunListesi() {
        List<Map<String,Object>> liste = new ArrayList<>();
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT * FROM urun_tanim WHERE aktif=1 ORDER BY kategori,ad")) {
            while (rs.next()) {
                Map<String,Object> row = new LinkedHashMap<>();
                row.put("id",              rs.getInt("id"));
                row.put("kod",             rs.getString("kod"));
                row.put("ad",              rs.getString("ad"));
                row.put("kategori",        rs.getString("kategori"));
                row.put("zorunluOlcular",  splitList(rs.getString("zorunlu_olcular")));
                row.put("ozellikTipleri",  splitList(rs.getString("ozellik_tipleri")));
                row.put("fiyatStratejisi", rs.getString("fiyat_stratejisi"));
                liste.add(row);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return liste;
    }

    // ── Yardımcılar ──
    private static UrunKart satiraKart(ResultSet rs) {
        try {
            String kod      = rs.getString("kod");
            String ad       = rs.getString("ad");
            String katStr   = rs.getString("kategori");
            UrunKategori kat;
            try { kat = UrunKategori.valueOf(katStr); }
            catch (Exception e) { return null; }

            List<OlcuAlanTipi> olcular = new ArrayList<>();
            for (String s : splitList(rs.getString("zorunlu_olcular"))) {
                try { olcular.add(OlcuAlanTipi.valueOf(s)); } catch (Exception ignored) {}
            }

            List<OzellikTipi> ozellikler = new ArrayList<>();
            for (String s : splitList(rs.getString("ozellik_tipleri"))) {
                try { ozellikler.add(OzellikTipi.valueOf(s)); } catch (Exception ignored) {}
            }

            // İzinli seçimler feature_ratio tablosundan alınır
            Map<OzellikTipi, List<String>> izinliSecimler = new HashMap<>();
            for (OzellikTipi tip : ozellikler) {
                Map<String, OzellikOran> oranMap = OzellikDeposu.oranlariGetir(tip);
                izinliSecimler.put(tip, new ArrayList<>(oranMap.keySet()));
            }

            return new UrunKart(kod, kat, ad, ozellikler, izinliSecimler, olcular);
        } catch (Exception e) {
            e.printStackTrace(); return null;
        }
    }

    private static List<String> splitList(String s) {
        if (s == null || s.trim().isEmpty()) return new ArrayList<>();
        return Arrays.asList(s.split(","));
    }
}
