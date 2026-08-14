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

            boolean wDisi = w > maxW;
            boolean hDisi = h > maxH;

            // ── İkisi de tablo içinde → direkt değer ──────────
            if (!wDisi && !hDisi) {
                double wCeil = repo.ceilingAxis(conn, tableId, "ROW", w);
                double hCeil = repo.ceilingAxis(conn, tableId, "COL", h);
                return repo.cellPrice(conn, tableId, wCeil, hCeil);
            }

            // ── İkisi de tablo dışında → alan oranı ──────────
            if (wDisi && hDisi) {
                double fiyatRef = repo.cellPrice(conn, tableId, maxW, maxH);
                double alanOrani = (w * h) / (maxW * maxH);
                return fiyatRef * alanOrani;
            }

            // ── Sadece biri tablo dışında → eski bölme yöntemi ──
            double toplamFiyat = 0;

            int wFull = (int)(w / maxW);
            double wRemain = w % maxW;
            int hFull = (int)(h / maxH);
            double hRemain = h % maxH;

            if (wFull > 0 && hFull > 0) {
                double fiyat = repo.cellPrice(conn, tableId, maxW, maxH);
                toplamFiyat += wFull * hFull * fiyat;
            }
            if (wFull > 0 && hRemain > 0) {
                double hC = repo.ceilingAxis(conn, tableId, "COL", hRemain);
                double fiyat = repo.cellPrice(conn, tableId, maxW, hC);
                toplamFiyat += wFull * fiyat;
            }
            if (wRemain > 0 && hFull > 0) {
                double wC = repo.ceilingAxis(conn, tableId, "ROW", wRemain);
                double fiyat = repo.cellPrice(conn, tableId, wC, maxH);
                toplamFiyat += hFull * fiyat;
            }
            if (wRemain > 0 && hRemain > 0) {
                double wC = repo.ceilingAxis(conn, tableId, "ROW", wRemain);
                double hC = repo.ceilingAxis(conn, tableId, "COL", hRemain);
                double fiyat = repo.cellPrice(conn, tableId, wC, hC);
                toplamFiyat += fiyat;
            }
            // Sadece bir eksen tam, diğeri 0 kalan
            if (wFull > 0 && hRemain == 0 && hFull == 0) {
                double fiyat = repo.cellPrice(conn, tableId, maxW, repo.ceilingAxis(conn, tableId, "COL", h));
                toplamFiyat += wFull * fiyat;
            }
            if (hFull > 0 && wRemain == 0 && wFull == 0) {
                double fiyat = repo.cellPrice(conn, tableId, repo.ceilingAxis(conn, tableId, "ROW", w), maxH);
                toplamFiyat += hFull * fiyat;
            }

            return toplamFiyat;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
