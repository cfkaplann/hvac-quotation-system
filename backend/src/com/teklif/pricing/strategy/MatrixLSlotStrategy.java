package com.teklif.pricing.strategy;

import java.sql.Connection;

import com.teklif.db.ConnectionManager;
import com.teklif.model.dto.PricingRequest;
import com.teklif.repository.HamFiyatRepository;

public class MatrixLSlotStrategy implements PricingStrategy {

    private final HamFiyatRepository repo = new HamFiyatRepository();

    @Override
    public double execute(PricingRequest req) {

        if (req.getL() == null || req.getSlot() == null)
            throw new IllegalArgumentException("L ve Slot gerekli: " + req.getSheetName());

        double l    = req.getL();
        double slot = req.getSlot();

        // Yükseklik çarpanı — sadece BOX_LS için
        double yukseklikCarpan = 1.0;
        if ("BOX_LS".equals(req.getSheetName()) && req.getH() != null) {
            double h = req.getH();
            if (h >= 500) {
                yukseklikCarpan = 1.5;
            } else if (h >= 400) {
                yukseklikCarpan = 1.25;
            } else {
                yukseklikCarpan = 1.0;
            }
        }

        try (Connection conn = ConnectionManager.getConnection()) {

            int tableId = repo.tableIdBul(conn, req.getSheetName());

            double maxL = repo.maxAxis(conn, tableId, "ROW");

            double toplamFiyat = 0;

            double slotCeil = repo.ceilingAxis(conn, tableId, "COL", slot);

            // FULL PARÇALAR
            int fullCount = (int)(l / maxL);
            if (fullCount > 0) {
                double lMaxCeil = repo.ceilingAxis(conn, tableId, "ROW", maxL);
                double fiyatMax = repo.cellPrice(conn, tableId, lMaxCeil, slotCeil);
                toplamFiyat += fullCount * fiyatMax;
            }

            // KALAN PARÇA
            double remain = l % maxL;
            if (remain > 0) {
                double lRemainCeil = repo.ceilingAxis(conn, tableId, "ROW", remain);
                double fiyatRemain = repo.cellPrice(conn, tableId, lRemainCeil, slotCeil);
                toplamFiyat += fiyatRemain;
            }

            // Yükseklik çarpanını uygula
            return toplamFiyat * yukseklikCarpan;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
