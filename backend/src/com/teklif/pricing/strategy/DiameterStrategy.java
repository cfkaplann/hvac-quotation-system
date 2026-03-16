package com.teklif.pricing.strategy;

import java.sql.Connection;

import com.teklif.db.ConnectionManager;
import com.teklif.model.dto.PricingRequest;
import com.teklif.repository.HamFiyatRepository;

public class DiameterStrategy implements PricingStrategy {

    private final HamFiyatRepository repo = new HamFiyatRepository();

    @Override
    public double execute(PricingRequest req) {

        if (req.getDiameter() == null)
            throw new IllegalArgumentException("Çap gerekli: " + req.getSheetName());

        double d = req.getDiameter();

        try (Connection conn = ConnectionManager.getConnection()) {

            int tableId = repo.tableIdBul(conn, req.getSheetName());

            double dCeil = repo.ceilingAxis(conn, tableId, "ROW", d);

            return repo.cellPrice(conn, tableId, dCeil, 0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
