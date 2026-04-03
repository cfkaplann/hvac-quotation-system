package com.teklif.api;

import com.teklif.db.ConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/kullanicilar")
public class KullaniciController {

    // ── Liste ─────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<?> liste(@RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!adminMi(token)) return yetkisiz();
        try (Connection c = ConnectionManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                "SELECT id, kullanici_adi, ad_soyad, rol, aktif, olusturma_tarihi FROM kullanici ORDER BY id")) {
            List<Map<String,Object>> liste = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id",            rs.getInt("id"));
                m.put("kullaniciAdi",  rs.getString("kullanici_adi"));
                m.put("adSoyad",       rs.getString("ad_soyad"));
                m.put("rol",           rs.getString("rol"));
                m.put("aktif",         rs.getInt("aktif") == 1);
                m.put("olusturmaTarihi", rs.getString("olusturma_tarihi"));
                liste.add(m);
            }
            return ResponseEntity.ok(liste);
        } catch (Exception e) { return hata(e); }
    }

    // ── Ekle ─────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> ekle(@RequestBody Map<String,Object> body,
                                   @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!adminMi(token)) return yetkisiz();
        String kullaniciAdi = s(body,"kullaniciAdi");
        String sifre        = s(body,"sifre");
        String adSoyad      = s(body,"adSoyad");
        String rol          = s(body,"rol","KULLANICI");
        if (kullaniciAdi.isEmpty() || sifre.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("hata","Kullanıcı adı ve şifre zorunludur."));
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "INSERT INTO kullanici (kullanici_adi, sifre_hash, ad_soyad, rol) VALUES (?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, kullaniciAdi); ps.setString(2, sifre);
            ps.setString(3, adSoyad);      ps.setString(4, rol);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) body.put("id", rs.getInt(1));
            }
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            if (e.getMessage().contains("UNIQUE"))
                return ResponseEntity.badRequest().body(Map.of("hata","Bu kullanıcı adı zaten mevcut."));
            return hata(e);
        }
    }

    // ── Güncelle ─────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> guncelle(@PathVariable int id,
                                       @RequestBody Map<String,Object> body,
                                       @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!adminMi(token)) return yetkisiz();
        try (Connection c = ConnectionManager.getConnection()) {
            String sifre   = s(body,"sifre");
            String adSoyad = s(body,"adSoyad");
            String rol     = s(body,"rol","KULLANICI");
            boolean aktif  = body.containsKey("aktif") ? (Boolean)body.get("aktif") : true;

            if (!sifre.isEmpty()) {
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE kullanici SET ad_soyad=?, rol=?, aktif=?, sifre_hash=? WHERE id=?");
                ps.setString(1,adSoyad); ps.setString(2,rol);
                ps.setInt(3,aktif?1:0); ps.setString(4,sifre); ps.setInt(5,id);
                ps.executeUpdate();
            } else {
                PreparedStatement ps = c.prepareStatement(
                    "UPDATE kullanici SET ad_soyad=?, rol=?, aktif=? WHERE id=?");
                ps.setString(1,adSoyad); ps.setString(2,rol);
                ps.setInt(3,aktif?1:0); ps.setInt(4,id);
                ps.executeUpdate();
            }
            return ResponseEntity.ok(Map.of("ok",true));
        } catch (Exception e) { return hata(e); }
    }

    // ── Sil ──────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> sil(@PathVariable int id,
                                  @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!adminMi(token)) return yetkisiz();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM kullanici WHERE id=?")) {
            ps.setInt(1, id); ps.executeUpdate();
            return ResponseEntity.ok(Map.of("silindi",true));
        } catch (Exception e) { return hata(e); }
    }

    private boolean adminMi(String token) {
        Integer userId = AuthController.tokendenId(token);
        if (userId == null) return false;
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT rol FROM kullanici WHERE id=? AND aktif=1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && "ADMIN".equals(rs.getString("rol"));
            }
        } catch (Exception e) { return false; }
    }

    private String s(Map<String,Object> m, String k) { return s(m,k,""); }
    private String s(Map<String,Object> m, String k, String def) {
        Object v = m.get(k); return v==null ? def : v.toString().trim();
    }
    private ResponseEntity<?> yetkisiz() { return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz.")); }
    private ResponseEntity<?> hata(Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata",e.getMessage())); }
}
