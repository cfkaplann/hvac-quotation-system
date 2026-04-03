package com.teklif.repository;

import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.teklif.repository.config.*;

public class UrunKataloguDeposu {

    // Config'den gelen sabit ürünler
    private static final List<UrunKart> CONFIG_URUNLER =
        Stream.of(
            MenfezConfig.get(), SlotConfig.get(),
            DikdortgenDamperConfig.get(), DaireselDamperConfig.get(),
            KareAnemostadConfig.get(), DaireselAnemostadConfig.get(),
            KareSwirlConfig.get(), DaireselSwirlConfig.get(),
            PanjurConfig.get(), KapakConfig.get(), KutuConfig.get()
        ).flatMap(List::stream).collect(Collectors.toList());

    // DB'den gelen ürünler — invalidate ile yenilenir
    private static volatile List<UrunKart> dbUrunler = null;

    public static synchronized void invalidate() { dbUrunler = null; }

    private static List<UrunKart> dbUrunler() {
        if (dbUrunler == null) dbUrunler = DbUrunService.dbUrunleri();
        return dbUrunler;
    }

    public static List<UrunKart> tumUrunler() {
        List<UrunKart> tum = new ArrayList<>(CONFIG_URUNLER);
        tum.addAll(dbUrunler());
        return tum;
    }

    public static List<UrunKategori> tumKategoriler() {
        return tumUrunler().stream()
            .map(UrunKart::getKategori).distinct().collect(Collectors.toList());
    }

    public static UrunKart bul(String kod) {
        return tumUrunler().stream()
            .filter(u -> u.getKod().equals(kod))
            .findFirst().orElse(null);
    }
}
