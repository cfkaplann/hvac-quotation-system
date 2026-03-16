package com.teklif.importer;

import java.sql.Connection;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class AutoExcelImporter {

    public void importAll(Connection conn, Workbook wb) {

        for (int i = 0; i < wb.getNumberOfSheets(); i++) {

            Sheet sheet = wb.getSheetAt(i);
            String currentSheetName = sheet.getSheetName();

            System.out.println("AUTO IMPORTER → " + currentSheetName);

            UniversalExcelImporter.importSheet(conn, sheet, currentSheetName);
        }
    }
}
