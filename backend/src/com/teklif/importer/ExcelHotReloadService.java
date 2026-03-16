package com.teklif.importer;

import java.io.File;

public class ExcelHotReloadService {

    private static long lastModified = 0;

    public static void checkAndReload() {

        try {

            String path = "HAM_FIYATLAR.xlsx";

            File file = new File(path);

            if(!file.exists())
                return;

            long modified = file.lastModified();

            if(modified != lastModified){

                System.out.println("♻ Excel değişti → fiyatlar yeniden yükleniyor...");

                MasterExcelImporter importer = new MasterExcelImporter();
                importer.importAll(path);   // ⭐ DOĞRU METOD

                lastModified = modified;

                System.out.println("✅ Fiyatlar güncellendi");

            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}