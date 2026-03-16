package com.teklif.pricing.strategy;

import java.sql.Connection;
import java.util.List;

import com.teklif.db.ConnectionManager;
import com.teklif.model.dto.PricingRequest;
import com.teklif.repository.HamFiyatRepository;

public class SingleStringSizeStrategy implements PricingStrategy {

    private HamFiyatRepository repo = new HamFiyatRepository();

    @Override
    public double execute(PricingRequest req) {

        try(Connection conn = ConnectionManager.getConnection()){

        	String olcu = null;

        	// ⭐ KASA_CAP
        	if(req.getKasaCap()!=null && !req.getKasaCap().isBlank()){
        	    olcu = req.getKasaCap();
        	}
        	// ⭐ DIŞ ÖLÇÜ
        	else if(req.getDisOlcu()!=null && !req.getDisOlcu().isBlank()){
        	    olcu = req.getDisOlcu();
        	}
        	// ⭐ BOĞAZ ÖLÇÜ
        	else if(req.getBogazOlcu()!=null && !req.getBogazOlcu().isBlank()){
        	    olcu = req.getBogazOlcu();
        	}
        	else{
        	    throw new RuntimeException("Ölçü seçilmedi");
        	}


            int tableId = repo.tableIdBul(conn, req.getSheetName());

            // ⭐ DIRECT STRING MATCH
            String match = olcu;

            System.out.println("REQ OLÇU = " + olcu);

            return repo.cellPriceSingleString(conn, tableId, match);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

}
