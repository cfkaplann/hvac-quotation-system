package com.teklif.importer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.poi.ss.usermodel.*;

import com.teklif.db.ConnectionManager;

public class UniversalExcelImporter {

	public static void importSheet(Connection conn, Sheet sheet, String sheetName) {

	    try{

	        String strategy = detectStrategy(sheetName);

	        int tableId = ensurePriceTable(conn, sheetName, strategy);

	        switch(strategy){

	            case "MATRIX_WH":
	                importMatrixWH(conn, sheet, tableId);
	                break;

	            case "MATRIX_L_SLOT":
	                importMatrixWH(conn, sheet, tableId);
	                break;

	            case "DIAMETER":
	                importDiameter(conn, sheet, tableId);
	                break;

	            case "STRING_SIZE":
	                importStringSize(conn, sheet, tableId);
	                break;

	            default:
	                throw new RuntimeException("Strategy bulunamadı: " + sheetName);
	        }

	        System.out.println("IMPORT OK -> " + sheetName);

	    }catch(Exception e){
	        throw new RuntimeException(e);
	    }
	}



    // =====================================================
    // STRATEGY TESPİT
    // =====================================================

    private static String detectStrategy(String sheetName){

        if(sheetName.startsWith("SLT")) return "MATRIX_L_SLOT";

        // DIAMETER (numeric)
        if(sheetName.startsWith("DAIDMP") || sheetName.startsWith("DANM"))
            return "DIAMETER";

        // STRING_SIZE (Ø315 gibi string veya tek kolon ölçü)
        if(sheetName.startsWith("KANM")
                || sheetName.startsWith("KASWRDIF")
                || sheetName.startsWith("DASWRDIF")     // ✅ Swirl fix
                || sheetName.startsWith("PNJ_ALTIKUTU") // ✅ Altıgen kutu fix
        )
            return "STRING_SIZE";

        return "MATRIX_WH";
    }



    // =====================================================
    // MATRIX WH IMPORT
    // =====================================================

    private static void importMatrixWH(Connection conn, Sheet sheet, int tableId)throws Exception{

        Row header = sheet.getRow(0);

        for(int c=1;c<header.getLastCellNum();c++){
            Double colVal = parseNumeric(header.getCell(c));
            if(colVal!=null)
                ensureAxisNum(conn,tableId,"COL",colVal);
        }

        for(int r=1;r<=sheet.getLastRowNum();r++){

            Row row = sheet.getRow(r);
            if(row==null) continue;

            Double rowVal = parseNumeric(row.getCell(0));
            if(rowVal==null) continue;

            ensureAxisNum(conn,tableId,"ROW",rowVal);

            for(int c=1;c<row.getLastCellNum();c++){

                Double price = parseNumeric(row.getCell(c));
                Double colVal = parseNumeric(header.getCell(c));

                if(price==null || colVal==null) continue;

                insertCellNum(conn,tableId,rowVal,colVal,price);
            }
        }
    }

    // =====================================================
    // DIAMETER IMPORT
    // =====================================================

    private static void importDiameter(Connection conn,Sheet sheet,int tableId)throws Exception{

        ensureAxisNum(conn,tableId,"COL",0);

        for(int r=0;r<=sheet.getLastRowNum();r++){

            Row row = sheet.getRow(r);
            if(row==null) continue;

            Double d = parseNumeric(row.getCell(0));
            Double price = parseNumeric(row.getCell(1));

            if(d==null || price==null) continue;

            ensureAxisNum(conn,tableId,"ROW",d);

            insertCellNum(conn,tableId,d,0,price);
        }
    }

    // =====================================================
    // STRING IMPORT
    // =====================================================

    private static void importStringSize(Connection conn,Sheet sheet,int tableId)throws Exception{

        Row header = sheet.getRow(0);

        for(int c=1;c<header.getLastCellNum();c++){
        	String colVal = parseString(header.getCell(c));
            ensureAxisStr(conn,tableId,"COL",colVal);
        }

        for(int r=1;r<=sheet.getLastRowNum();r++){

            Row row = sheet.getRow(r);
            if(row==null) continue;

            String rowVal = parseString(row.getCell(0));


            ensureAxisStr(conn,tableId,"ROW",rowVal);

            for(int c=1;c<row.getLastCellNum();c++){

                Double price = parseNumeric(row.getCell(c));
                String colVal = parseString(header.getCell(c));


                if(price==null) continue;

                insertCellStr(conn,tableId,rowVal,colVal,price);
            }
        }
    }

    // =====================================================
    // DB HELPERS
    // =====================================================

    private static int ensurePriceTable(Connection conn,String sheetName,String strategy)throws Exception{

        PreparedStatement ps = conn.prepareStatement(
            "SELECT id FROM price_table WHERE sheet_name=?"
        );
        ps.setString(1,sheetName);

        ResultSet rs = ps.executeQuery();
        if(rs.next()) return rs.getInt("id");

        String prefix = sheetName.split("_")[0];

        PreparedStatement ins = conn.prepareStatement(
            "INSERT INTO price_table(sheet_name,prefix,strategy) VALUES(?,?,?)"
        );

        ins.setString(1,sheetName);
        ins.setString(2,prefix);
        ins.setString(3,strategy);
        ins.executeUpdate();

        return ensurePriceTable(conn,sheetName,strategy);
    }

    private static void ensureAxisNum(Connection conn,int tableId,String axis,double val)throws Exception{

        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO price_axis(table_id,axis,value_num) VALUES(?,?,?)"
        );

        ps.setInt(1,tableId);
        ps.setString(2,axis);
        ps.setDouble(3,val);
        ps.executeUpdate();
    }

    private static void ensureAxisStr(Connection conn,int tableId,String axis,String val)throws Exception{

        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO price_axis(table_id,axis,value_str) VALUES(?,?,?)"
        );

        ps.setInt(1,tableId);
        ps.setString(2,axis);
        ps.setString(3,val);
        ps.executeUpdate();
    }

    private static void insertCellNum(Connection conn,int tableId,double r,double c,double price)throws Exception{

        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO price_cell(table_id,row_value,col_value,price) VALUES(?,?,?,?)"
        );

        ps.setInt(1,tableId);
        ps.setDouble(2,r);
        ps.setDouble(3,c);
        ps.setDouble(4,price);
        ps.executeUpdate();
    }

    private static void insertCellStr(Connection conn,int tableId,String r,String c,double price)throws Exception{

        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO price_cell(table_id,row_value_str,col_value_str,price) VALUES(?,?,?,?)"
        );

        ps.setInt(1,tableId);
        ps.setString(2,r);
        ps.setString(3,c);
        ps.setDouble(4,price);
        ps.executeUpdate();
    }

    private static Double parseNumeric(Cell cell){

        if(cell==null) return null;

        try{
            if(cell.getCellType()==CellType.NUMERIC)
                return cell.getNumericCellValue();

            if(cell.getCellType()==CellType.STRING){
                String s = cell.getStringCellValue().replace(",",".").trim();
                if(s.isEmpty()) return null;
                return Double.parseDouble(s);
            }
        }catch(Exception e){}

        return null;
    }
    
    private static String parseString(Cell cell){

        if(cell == null) return null;

        if(cell.getCellType() == CellType.STRING){
            return cell.getStringCellValue().trim();
        }

        if(cell.getCellType() == CellType.NUMERIC){
            double v = cell.getNumericCellValue();

            if(v == (long)v)
                return String.valueOf((long)v);

            return String.valueOf(v);
        }

        return null;
    }


}
