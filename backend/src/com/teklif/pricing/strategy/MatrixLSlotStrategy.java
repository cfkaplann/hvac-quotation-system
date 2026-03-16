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

        double l = req.getL();
        double slot = req.getSlot();

        try (Connection conn = ConnectionManager.getConnection()) {

            int tableId = repo.tableIdBul(conn, req.getSheetName());

            double maxL = repo.maxAxis(conn, tableId, "ROW");

            double toplamFiyat = 0;

            // slot ceiling sadece 1 kere
            double slotCeil =
                    repo.ceilingAxis(conn, tableId, "COL", slot);

            // =====================================================
            // FULL PARÇALAR
            // =====================================================

            int fullCount = (int)(l / maxL);

            if(fullCount > 0){

                double lMaxCeil =
                        repo.ceilingAxis(conn, tableId, "ROW", maxL);

                double fiyatMax =
                        repo.cellPrice(conn, tableId, lMaxCeil, slotCeil);

                toplamFiyat += fullCount * fiyatMax;
            }

            // =====================================================
            // KALAN PARÇA
            // =====================================================

            double remain = l % maxL;

            if(remain > 0){

                double lRemainCeil =
                        repo.ceilingAxis(conn, tableId, "ROW", remain);

                double fiyatRemain =
                        repo.cellPrice(conn, tableId, lRemainCeil, slotCeil);

                toplamFiyat += fiyatRemain;
            }

            return toplamFiyat;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
