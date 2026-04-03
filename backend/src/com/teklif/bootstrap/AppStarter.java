package com.teklif.bootstrap;

import com.teklif.db.ConnectionManager;
import com.teklif.db.SchemaInitializer;
import com.teklif.importer.MasterExcelImporter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;

@Component
public class AppStarter {

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        try {
            System.out.println("Veritabani kontrol ediliyor...");
            SchemaInitializer.init();

            // Fiyat tablosu boşsa ilk kurulum — Excel'den yükle
            if (fiyatTablosuBos()) {
                System.out.println("İlk kurulum: Excel fiyat tablolari yukleniyor...");
                String excelPath = extractExcel();
                new MasterExcelImporter().importAll(excelPath);
                System.out.println("İlk kurulum tamamlandi.");
            } else {
                System.out.println("Fiyat tablolari mevcut. Admin panelinden yonetilebilir.");
            }

            System.out.println("Hazir! http://localhost:8080");
        } catch (Exception e) {
            System.err.println("Baslama hatasi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean fiyatTablosuBos() {
        try (Connection c = ConnectionManager.getConnection();
             ResultSet rs = c.createStatement().executeQuery("SELECT COUNT(*) FROM price_table")) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (Exception e) {
            return true;
        }
    }

    private String extractExcel() throws Exception {
        InputStream is = getClass().getResourceAsStream("/HAM_FIYATLAR.xlsx");
        if (is == null) throw new RuntimeException("HAM_FIYATLAR.xlsx bulunamadi!");
        File temp = File.createTempFile("HAM_FIYATLAR", ".xlsx");
        temp.deleteOnExit();
        Files.copy(is, temp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return temp.getAbsolutePath();
    }
}
