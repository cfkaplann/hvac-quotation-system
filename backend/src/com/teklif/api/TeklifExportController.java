package com.teklif.api;

import com.teklif.export.ExcelExporter;
import com.teklif.export.PdfExporter;
import com.teklif.model.ParaBirimi;
import com.teklif.model.takip.Teklif;
import com.teklif.repository.takip.TeklifRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teklifler")
public class TeklifExportController {

    @Autowired
    TeklifRepository repo;

    // ── PDF ──────────────────────────────────────────────────────
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> pdf(@PathVariable int id) {
        try {
            Teklif t = repo.idIleGetir(id)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı: " + id));

            byte[] bytes = PdfExporter.exportToBytes(t);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + t.getTeklifNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("hata", e.getMessage()));
        }
    }

    // ── EXCEL ────────────────────────────────────────────────────
    @GetMapping("/{id}/excel")
    public ResponseEntity<?> excel(
            @PathVariable int id,
            @RequestParam(defaultValue = "TRY") String paraBirimi) {
        try {
            Teklif t = repo.idIleGetir(id)
                .orElseThrow(() -> new RuntimeException("Teklif bulunamadı: " + id));

            // TRY → TL alias desteği
            ParaBirimi pb;
            String pbStr = paraBirimi.toUpperCase();
            if ("TRY".equals(pbStr) || "TL".equals(pbStr)) {
                pb = ParaBirimi.TL;
            } else {
                try { pb = ParaBirimi.valueOf(pbStr); }
                catch (Exception e) { pb = ParaBirimi.TL; }
            }

            byte[] bytes = ExcelExporter.exportToBytes(t, pb);

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + t.getTeklifNo() + ".xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("hata", e.getMessage()));
        }
    }
}
