package com.teklif.api;

import com.teklif.db.ConnectionManager;
import com.teklif.export.ExcelExporter;
import com.teklif.export.PdfExporter;
import com.teklif.model.ParaBirimi;
import com.teklif.model.takip.Teklif;
import com.teklif.repository.takip.TeklifRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/teklifler")
public class TeklifExportController {

    @Autowired
    TeklifRepository repo;

    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> pdf(@PathVariable int id) {
        try {
            Teklif t = repo.idIleGetir(id)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı: " + id));
            ParaBirimi pb;
            try { pb = ParaBirimi.valueOf(t.getParaBirimi()); }
            catch (Exception e) { pb = ParaBirimi.TL; }
            byte[] bytes = PdfExporter.exportToBytes(t, pb);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + t.getTeklifNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }

    @GetMapping("/{id}/excel")
    public ResponseEntity<?> excel(@PathVariable int id) {
        try {
            Teklif t = repo.idIleGetir(id)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı: " + id));

            ParaBirimi pb;
            String pbStr = t.getParaBirimi() != null ? t.getParaBirimi().toUpperCase() : "TL";
            if ("TRY".equals(pbStr) || "TL".equals(pbStr)) pb = ParaBirimi.TL;
            else { try { pb = ParaBirimi.valueOf(pbStr); } catch (Exception e) { pb = ParaBirimi.TL; } }

            // Ticari kodları DB'den yükle
            Map<String, String> ticariKodlar = ticariKodlariGetir();

            byte[] bytes = ExcelExporter.exportToBytes(t, pb, ticariKodlar);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + t.getTeklifNo() + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage()));
        }
    }

    private Map<String, String> ticariKodlariGetir() {
        Map<String, String> map = new HashMap<>();
        try (Connection c = ConnectionManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT urun_kod, ticari_kod FROM ticari_kod_map")) {
            while (rs.next()) {
                String kod      = rs.getString("urun_kod");
                String ticariKod = rs.getString("ticari_kod");
                if (kod != null && ticariKod != null && !ticariKod.isBlank())
                    map.put(kod, ticariKod);
            }
        } catch (Exception ignored) {}
        return map;
    }
}
