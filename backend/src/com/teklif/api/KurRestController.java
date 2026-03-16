package com.teklif.api;

import com.teklif.pricing.KurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kur")
public class KurRestController {

    @GetMapping
    public ResponseEntity<?> kurlar() {
        return ResponseEntity.ok(KurService.kurBilgisi());
    }
}