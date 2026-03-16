package com.teklif.importer;

import com.teklif.db.ConnectionManager;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.sql.Connection;

public class MasterExcelImporter {

    public void importAll(String excelPath) {

        File file = new File(excelPath);

        if (!file.exists()) {
            throw new RuntimeException("Excel dosyasi bulunamadi: " + file.getAbsolutePath());
        }

        try (Connection conn = ConnectionManager.getConnection()) {

            conn.createStatement().execute("PRAGMA busy_timeout = 5000");
            conn.setAutoCommit(false);

            try {
                Workbook wb = WorkbookFactory.create(file);
                new AutoExcelImporter().importAll(conn, wb);
                conn.commit();
                System.out.println("Excel import tamamlandi.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
