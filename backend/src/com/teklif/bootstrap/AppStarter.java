package com.teklif.bootstrap;

import com.teklif.db.SchemaInitializer;
import com.teklif.importer.MasterExcelImporter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Component
public class AppStarter {

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        try {
            System.out.println("Veritabani kontrol ediliyor...");
            SchemaInitializer.init();

            System.out.println("Excel fiyat tablolari yukleniyor...");
            String excelPath = extractExcel();
            new MasterExcelImporter().importAll(excelPath);

            System.out.println("Hazir! http://localhost:8080");
        } catch (Exception e) {
            System.err.println("Baslama hatasi: " + e.getMessage());
            e.printStackTrace();
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
