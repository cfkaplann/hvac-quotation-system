package com.teklif.api;

import com.teklif.db.ConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/siparisler")
public class SiparisController {

    // Üretim durum sırası
    private static final List<String> DURUM_SIRASI = List.of(
        "URETIM_BEKLIYOR", "URETIM_ALINDI", "URETIMDE",
        "HAZIR", "SEVK_EDILDI", "TESLIM_EDILDI"
    );

    // ── Onaylanan teklifleri sipariş olarak listele ──────
    @GetMapping
    public ResponseEntity<?> listele(
            @RequestHeader(value="X-Token", defaultValue="") String token) {
        if (!tokenGecerli(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (Connection conn = ConnectionManager.getConnection()) {
            terminMigration(conn);
            // Önce onaylanan ama henüz sipariş oluşturulmamış teklifleri otomatik ekle
            otomatikSiparisOlustur(conn);

            String sql = """
                SELECT s.id, s.teklif_id, s.teklif_no, s.musteri_adi, s.is_adi,
                       s.siparis_tarihi, s.uretim_durumu, s.not_lar, s.son_guncelleme, s.termin_tarihi,
                       t.genel_toplam, t.para_birimi
                FROM siparis s
                LEFT JOIN teklif t ON s.teklif_id = t.id
                ORDER BY
                  CASE s.uretim_durumu
                    WHEN 'TESLIM_EDILDI' THEN 6
                    WHEN 'IPTAL' THEN 7
                    ELSE 0 END,
                  CASE WHEN s.termin_tarihi IS NULL THEN 1 ELSE 0 END,
                  s.termin_tarihi ASC
                """;
            List<Map<String,Object>> list = new ArrayList<>();
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("id",            rs.getInt("id"));
                    row.put("teklifId",      rs.getInt("teklif_id"));
                    row.put("teklifNo",      rs.getString("teklif_no"));
                    row.put("musteriAdi",    rs.getString("musteri_adi"));
                    row.put("isAdi",         rs.getString("is_adi"));
                    row.put("siparisTarihi", rs.getString("siparis_tarihi"));
                    row.put("uretimDurumu",  rs.getString("uretim_durumu"));
                    row.put("notlar",        rs.getString("not_lar"));
                    row.put("sonGuncelleme", rs.getString("son_guncelleme"));
                    row.put("terminTarihi",  rs.getString("termin_tarihi"));
                    row.put("genelToplam",   rs.getDouble("genel_toplam"));
                    row.put("paraBirimi",    rs.getString("para_birimi"));
                    list.add(row);
                }
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Durum güncelle ──────────────────────────────────
    @PutMapping("/{id}/durum")
    public ResponseEntity<?> durumGuncelle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable int id,
            @RequestBody Map<String,String> body) {
        if (!tokenGecerli(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        String yeniDurum = body.get("durum");
        String aciklama  = body.getOrDefault("aciklama", "");
        String yapan     = kullaniciAdi(token);
        if (yeniDurum == null) return ResponseEntity.badRequest().body(Map.of("hata","Durum zorunludur"));

        try (Connection conn = ConnectionManager.getConnection()) {
            // Siparişi güncelle
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE siparis SET uretim_durumu=?, son_guncelleme=datetime('now','localtime') WHERE id=?")) {
                ps.setString(1, yeniDurum); ps.setInt(2, id); ps.executeUpdate();
            }
            // Geçmişe ekle
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO siparis_gecmis(siparis_id,durum,aciklama,yapan) VALUES(?,?,?,?)")) {
                ps.setInt(1, id); ps.setString(2, yeniDurum);
                ps.setString(3, aciklama); ps.setString(4, yapan);
                ps.executeUpdate();
            }
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Not ekle ────────────────────────────────────────
    @PutMapping("/{id}/not")
    public ResponseEntity<?> notGuncelle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable int id,
            @RequestBody Map<String,String> body) {
        if (!tokenGecerli(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE siparis SET not_lar=? WHERE id=?")) {
            ps.setString(1, body.get("notlar")); ps.setInt(2, id);
            ps.executeUpdate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Termin tarihi güncelle ──────────────────────────
    @PutMapping("/{id}/termin")
    public ResponseEntity<?> terminGuncelle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable int id,
            @RequestBody Map<String,String> body) {
        if (!tokenGecerli(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE siparis SET termin_tarihi=? WHERE id=?")) {
            ps.setString(1, body.get("terminTarihi")); ps.setInt(2, id);
            ps.executeUpdate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Geçmiş ──────────────────────────────────────────
    @GetMapping("/{id}/gecmis")
    public ResponseEntity<?> gecmis(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable int id) {
        if (!tokenGecerli(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT * FROM siparis_gecmis WHERE siparis_id=? ORDER BY tarih DESC")) {
            ps.setInt(1, id);
            List<Map<String,Object>> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new LinkedHashMap<>();
                    row.put("durum",    rs.getString("durum"));
                    row.put("aciklama", rs.getString("aciklama"));
                    row.put("tarih",    rs.getString("tarih"));
                    row.put("yapan",    rs.getString("yapan"));
                    list.add(row);
                }
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Yardımcılar ─────────────────────────────────────
    private void terminMigration(Connection conn) {
        try { conn.createStatement().execute("ALTER TABLE siparis ADD COLUMN termin_tarihi TEXT"); } catch (Exception ignored) {}
    }

    private void otomatikSiparisOlustur(Connection conn) throws Exception {

        String sql = """
            INSERT OR IGNORE INTO siparis(teklif_id, teklif_no, musteri_adi, is_adi)
            SELECT t.id, t.teklif_no,
                   COALESCE(m.firma_adi,'—'),
                   t.is_adi
            FROM teklif t
            LEFT JOIN musteri m ON t.musteri_id = m.id
            WHERE t.durum = 'ONAYLANDI'
            AND t.id NOT IN (SELECT teklif_id FROM siparis)
            """;
        conn.createStatement().execute(sql);
    }

    private boolean tokenGecerli(String t) {
        return AuthController.tokendenId(t) != null;
    }

    private String kullaniciAdi(String t) {
        Integer id = AuthController.tokendenId(t);
        if (id == null) return "?";
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT ad_soyad, kullanici_adi FROM kullanici WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String ad = rs.getString("ad_soyad");
                    return (ad != null && !ad.isBlank()) ? ad : rs.getString("kullanici_adi");
                }
            }
        } catch (Exception ignored) {}
        return "?";
    }
}
