package com.teklif.pricing.strategy;

import java.sql.Connection;

import com.teklif.db.ConnectionManager;
import com.teklif.model.dto.PricingRequest;
import com.teklif.repository.HamFiyatRepository;

public class MatrixWHStrategy implements PricingStrategy {

    private final HamFiyatRepository repo = new HamFiyatRepository();

    @Override
    public double execute(PricingRequest req) {

        if (req.getW() == null || req.getH() == null)
            throw new IllegalArgumentException("WH gerekli: " + req.getSheetName());

        double w = req.getW();
        double h = req.getH();

        try (Connection conn = ConnectionManager.getConnection()) {

            int tableId = repo.tableIdBul(conn, req.getSheetName());

            double maxW = repo.maxAxis(conn, tableId, "ROW");
            double maxH = repo.maxAxis(conn, tableId, "COL");

            double toplamFiyat = 0;

            // ===============================
            // W FULL + REMAIN
            // ===============================

            int wFull = (int)(w / maxW);
            double wRemain = w % maxW;

            // ===============================
            // H FULL + REMAIN
            // ===============================

            int hFull = (int)(h / maxH);
            double hRemain = h % maxH;

            // ===============================
            // FULL GRID
            // ===============================

            if(wFull > 0 && hFull > 0){

                double wMax = repo.ceilingAxis(conn, tableId, "ROW", maxW);
                double hMax = repo.ceilingAxis(conn, tableId, "COL", maxH);

                double fiyat = repo.cellPrice(conn, tableId, wMax, hMax);

                toplamFiyat += wFull * hFull * fiyat;
            }

            // ===============================
            // W FULL × H REMAIN
            // ===============================

            if(wFull > 0 && hRemain > 0){

                double wMax = repo.ceilingAxis(conn, tableId, "ROW", maxW);
                double hC = repo.ceilingAxis(conn, tableId, "COL", hRemain);

                double fiyat = repo.cellPrice(conn, tableId, wMax, hC);

                toplamFiyat += wFull * fiyat;
            }

            // ===============================
            // W REMAIN × H FULL
            // ===============================

            if(wRemain > 0 && hFull > 0){

                double wC = repo.ceilingAxis(conn, tableId, "ROW", wRemain);
                double hMax = repo.ceilingAxis(conn, tableId, "COL", maxH);

                double fiyat = repo.cellPrice(conn, tableId, wC, hMax);

                toplamFiyat += hFull * fiyat;
            }

            // ===============================
            // REMAIN × REMAIN
            // ===============================

            if(wRemain > 0 && hRemain > 0){

                double wC = repo.ceilingAxis(conn, tableId, "ROW", wRemain);
                double hC = repo.ceilingAxis(conn, tableId, "COL", hRemain);

                double fiyat = repo.cellPrice(conn, tableId, wC, hC);

                toplamFiyat += fiyat;
            }

            return toplamFiyat;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
