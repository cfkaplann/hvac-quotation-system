package com.teklif.api;

import com.teklif.db.ConnectionManager;
import com.teklif.model.takip.TeklifNot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/teklifler/{teklifId}/notlar")
public class TeklifNotController {

    // GET /api/teklifler/{id}/notlar
    @GetMapping
    public ResponseEntity<?> listele(@PathVariable int teklifId) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT * FROM teklif_not WHERE teklif_id=? ORDER BY tarih DESC")) {
            ps.setInt(1, teklifId);
            List<TeklifNot> liste = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeklifNot n = new TeklifNot();
                    n.setId(rs.getInt("id"));
                    n.setTeklifId(rs.getInt("teklif_id"));
                    n.setIcerik(rs.getString("icerik"));
                    n.setTarih(rs.getString("tarih"));
                    n.setYazan(rs.getString("yazan"));
                    liste.add(n);
                }
            }
            return ResponseEntity.ok(liste);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }

    // POST /api/teklifler/{id}/notlar
    @PostMapping
    public ResponseEntity<?> ekle(@PathVariable int teklifId, @RequestBody TeklifNot not) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO teklif_not (teklif_id, icerik, yazan) VALUES (?,?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, teklifId);
            ps.setString(2, not.getIcerik());
            ps.setString(3, not.getYazan());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) not.setId(rs.getInt(1));
            }
            not.setTeklifId(teklifId);
            return ResponseEntity.ok(not);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }

    // DELETE /api/teklifler/{id}/notlar/{notId}
    @DeleteMapping("/{notId}")
    public ResponseEntity<?> sil(@PathVariable int teklifId, @PathVariable int notId) {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM teklif_not WHERE id=? AND teklif_id=?")) {
            ps.setInt(1, notId);
            ps.setInt(2, teklifId);
            ps.executeUpdate();
            return ResponseEntity.ok(Map.of("silindi", true));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }
}
