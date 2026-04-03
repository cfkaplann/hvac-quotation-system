package com.teklif.api;

import com.teklif.db.ConnectionManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // ── Giriş ────────────────────────────────────────────
    @PostMapping("/giris")
    public ResponseEntity<?> giris(@RequestBody Map<String,String> body) {
        String kullaniciAdi = body.getOrDefault("kullaniciAdi","").trim();
        String sifre        = body.getOrDefault("sifre","").trim();
        if (kullaniciAdi.isEmpty() || sifre.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("hata","Kullanıcı adı ve şifre zorunludur."));
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id, kullanici_adi, ad_soyad, rol, aktif FROM kullanici WHERE kullanici_adi=? AND sifre_hash=?")) {
            ps.setString(1, kullaniciAdi);
            ps.setString(2, sifre);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return ResponseEntity.status(401).body(Map.of("hata","Kullanıcı adı veya şifre hatalı."));
                if (rs.getInt("aktif") == 0)
                    return ResponseEntity.status(401).body(Map.of("hata","Hesabınız devre dışı bırakılmış."));
                Map<String,Object> kullanici = new LinkedHashMap<>();
                kullanici.put("id",           rs.getInt("id"));
                kullanici.put("kullaniciAdi", rs.getString("kullanici_adi"));
                kullanici.put("adSoyad",      rs.getString("ad_soyad"));
                kullanici.put("rol",          rs.getString("rol"));
                // Token: basit id+kullaniciAdi kombinasyonu
                String token = "tok-" + rs.getInt("id") + "-" + kullaniciAdi;
                kullanici.put("token", token);
                return ResponseEntity.ok(kullanici);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }

    // ── Token doğrula ─────────────────────────────────────
    @GetMapping("/dogrula")
    public ResponseEntity<?> dogrula(@RequestHeader(value="X-Token",defaultValue="") String token) {
        Integer userId = tokendenId(token);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("hata","Geçersiz token."));
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                "SELECT id, kullanici_adi, ad_soyad, rol FROM kullanici WHERE id=? AND aktif=1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return ResponseEntity.status(401).body(Map.of("hata","Kullanıcı bulunamadı."));
                Map<String,Object> k = new LinkedHashMap<>();
                k.put("id",           rs.getInt("id"));
                k.put("kullaniciAdi", rs.getString("kullanici_adi"));
                k.put("adSoyad",      rs.getString("ad_soyad"));
                k.put("rol",          rs.getString("rol"));
                k.put("token",        token);
                return ResponseEntity.ok(k);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }

    public static Integer tokendenId(String token) {
        if (token == null || !token.startsWith("tok-")) return null;
        try { return Integer.parseInt(token.split("-")[1]); }
        catch (Exception e) { return null; }
    }
}
