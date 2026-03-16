package com.teklif.repository;

import com.teklif.model.UrunKart;
import com.teklif.model.OzellikTipi;

public class UrunKartTest {

    public static void main(String[] args) {

        UrunKart kart = UrunKataloguDeposu.bul("MNZ_TEKSIRA");

        System.out.println("Ürün Adı : " + kart.getAd());
        System.out.println("Kategori : " + kart.getKategori());
        System.out.println("Özellikler : " + kart.getOzellikler());

        System.out.println("\n--- İzinli Seçimler ---");

        kart.getIzinliSecimler().forEach((tip, liste) -> {
            System.out.println(tip + " -> " + liste);
        });
    }
}
