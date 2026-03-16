package com.teklif.repository;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;
import com.teklif.repository.config.DaireselAnemostadConfig;
import com.teklif.repository.config.DaireselDamperConfig;
import com.teklif.repository.config.DaireselSwirlConfig;
import com.teklif.repository.config.DikdortgenDamperConfig;
import com.teklif.repository.config.KapakConfig;
import com.teklif.repository.config.KareAnemostadConfig;
import com.teklif.repository.config.KareSwirlConfig;
import com.teklif.repository.config.KutuConfig;
import com.teklif.repository.config.MenfezConfig;
import com.teklif.repository.config.PanjurConfig;
import com.teklif.repository.config.SlotConfig;

public class UrunKataloguDeposu {

    private static final List<UrunKart> URUNLER =
        Stream.of(
            MenfezConfig.get(),
            SlotConfig.get(),
            DikdortgenDamperConfig.get(),
            DaireselDamperConfig.get(),
            KareAnemostadConfig.get(),
            DaireselAnemostadConfig.get(),
            KareSwirlConfig.get(),
            DaireselSwirlConfig.get(),
            PanjurConfig.get(),
            KapakConfig.get(),
            KutuConfig.get()
        )
        .flatMap(List::stream)
        .collect(Collectors.toList());

    public static List<UrunKart> tumUrunler() {
        return URUNLER;
    }

    public static List<UrunKategori> tumKategoriler() {
        return URUNLER.stream()
            .map(UrunKart::getKategori)
            .distinct()
            .collect(Collectors.toList());
    }

    public static UrunKart bul(String kod) {
        return URUNLER.stream()
            .filter(u -> u.getKod().equals(kod))
            .findFirst()
            .orElse(null);
    }
}
