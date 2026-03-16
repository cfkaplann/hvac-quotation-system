package com.teklif.api;

import com.teklif.model.takip.Musteri;
import com.teklif.repository.takip.MusteriRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/musteriler")
public class MusteriRestController {

    @Autowired MusteriRepository repo;

    @GetMapping
    public ResponseEntity<?> listele(@RequestParam(required=false) String ara) {
        try { return ResponseEntity.ok(ara != null ? repo.ara(ara) : repo.hepsiniGetir()); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getir(@PathVariable int id) {
        try { return repo.idIleGetir(id).<ResponseEntity<?>>map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build()); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping
    public ResponseEntity<?> kaydet(@RequestBody Musteri m) {
        try { return ResponseEntity.ok(repo.kaydet(m)); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> guncelle(@PathVariable int id, @RequestBody Musteri m) {
        try { m.setId(id); return repo.guncelle(m) ? ResponseEntity.ok(m) : ResponseEntity.notFound().build(); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> sil(@PathVariable int id) {
        try { return repo.sil(id) ? ResponseEntity.ok(Map.of("mesaj","Silindi")) : ResponseEntity.notFound().build(); }
        catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }
}
