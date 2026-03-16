package com.teklif.api;

import com.teklif.model.takip.Teklif;
import com.teklif.model.takip.Teklif.Durum;
import com.teklif.repository.takip.TeklifRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/teklifler")
public class TeklifRestController {

    @Autowired TeklifRepository repo;

    @GetMapping
    public ResponseEntity<?> listele(@RequestParam(required=false) String durum) {
        try { return ResponseEntity.ok(durum != null ? repo.durumIleGetir(Durum.valueOf(durum.toUpperCase())) : repo.hepsiniGetir()); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getir(@PathVariable int id) {
        try { return repo.idIleGetir(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @GetMapping("/yeni-no")
    public ResponseEntity<?> yeniNo() {
        try { return ResponseEntity.ok(Map.of("teklifNo", repo.yeniTeklifNo())); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<?> olustur(@RequestBody Teklif t) {
        try { return ResponseEntity.ok(repo.kaydet(t)); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> guncelle(@PathVariable int id, @RequestBody Teklif t) {
        try { t.setId(id); return repo.guncelle(t) ? ResponseEntity.ok(t) : ResponseEntity.notFound().build(); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PatchMapping("/{id}/durum")
    public ResponseEntity<?> durumDegistir(@PathVariable int id, @RequestBody Map<String,String> body) {
        try {
            Durum d = Durum.valueOf(body.get("durum").toUpperCase());
            return repo.durumGuncelle(id, d) ? ResponseEntity.ok(Map.of("durum", d)) : ResponseEntity.notFound().build();
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping("/{id}/revize")
    public ResponseEntity<?> revize(@PathVariable int id) {
        try { return ResponseEntity.ok(repo.revizeOlustur(id)); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> sil(@PathVariable int id) {
        try { return repo.sil(id) ? ResponseEntity.ok(Map.of("mesaj","Silindi")) : ResponseEntity.notFound().build(); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }
}
