package com.teklif.model;


import com.teklif.repository.UrunKataloguDeposu;

import java.util.List;

public class UrunTanimiDeposu {

    public static UrunTanimi getir(String urunKodu){

        UrunKart kart = UrunKataloguDeposu.bul(urunKodu);

        if(kart == null)
            throw new RuntimeException("Urun bulunamadı : " + urunKodu);

        return new UrunTanimi(
            kart.getKod(),
            kart.getKategori(),     // ⭐ ARTIK ENUM DIREKT
            null,                   // şimdilik ölçü tipi yok
            kart.getOzellikler(),  // zorunlu özellikler
            List.of(),              // opsiyonel boş
            true,                   // aksesuar multi select
            null                    // muafiyet yok
        );
    }
}
