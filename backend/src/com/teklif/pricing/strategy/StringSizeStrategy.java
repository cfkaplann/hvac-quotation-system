package com.teklif.pricing.strategy;

import java.sql.Connection;
import java.util.List;

import com.teklif.db.ConnectionManager;
import com.teklif.model.dto.PricingRequest;
import com.teklif.repository.HamFiyatRepository;

public class StringSizeStrategy implements PricingStrategy {

    private HamFiyatRepository repo = new HamFiyatRepository();

    @Override
    public double execute(PricingRequest req) {

        try(Connection conn = ConnectionManager.getConnection()){

            // =====================================================
            // ⭐ INPUT VALIDATION (EN ÖNCE)
            // =====================================================

        	boolean disVar   = req.getDisOlcu()!=null && !req.getDisOlcu().isBlank();
        	boolean bogazVar = req.getBogazOlcu()!=null && !req.getBogazOlcu().isBlank();

        	if(!disVar && !bogazVar)
        	    throw new RuntimeException("Ölçü seçilmedi");



            // =====================================================
            // TABLE BUL
            // =====================================================

            int tableId = repo.tableIdBul(conn, req.getSheetName());


            // =====================================================
            // AXIS LISTELERI
            // =====================================================

            List<String> rowAxis = repo.axisStringList(conn, tableId, "ROW");
            List<String> colAxis = repo.axisStringList(conn, tableId, "COL");


            // =====================================================
            // CEILING MATCH
            // =====================================================

            String rowMatch = null;
            String colMatch = null;

            if(disVar){
                rowMatch = repo.ceilingString(rowAxis, req.getDisOlcu());
            }

            if(bogazVar){
                colMatch = repo.ceilingString(colAxis, req.getBogazOlcu());
            }


            // =====================================================
            // PRICE
            // =====================================================

            System.out.println("ROW MATCH = " + rowMatch);
            System.out.println("COL MATCH = " + colMatch);
            System.out.println("SHEET = " + req.getSheetName());

            
         // =====================================================
         // PRICE
         // =====================================================

         if(disVar && bogazVar){
             return repo.cellPriceString(conn, tableId, rowMatch, colMatch);
         }

         if(disVar){ // ⭐ sadece kasa ile fiyatlanan ürünler
             return repo.cellPriceString(conn, tableId, rowMatch, null);
         }

         if(bogazVar){ // ⭐ sadece boğaz ile fiyatlanan ürünler
             return repo.cellPriceString(conn, tableId, null, colMatch);
         }

         throw new RuntimeException("Fiyat bulunamadı");


        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}
