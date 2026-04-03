package com.teklif.repository.config;

import com.teklif.model.OlcuTipi;
import com.teklif.db.ConnectionManager;

import java.sql.*;

public class OlcuTipiResolver {

    public static OlcuTipi resolve(String urunKodu) {

        // Önce mevcut config'den bak (hızlı yol)
        OlcuTipi config = resolveFromConfig(urunKodu);
        if (config != null) return config;

        // Config'de yoksa price_table'dan oku (DB'den eklenen ürünler)
        OlcuTipi db = resolveFromDb(urunKodu);
        if (db != null) return db;

        throw new RuntimeException("Olcu tipi bulunamadı: " + urunKodu);
    }

    private static OlcuTipi resolveFromConfig(String kod) {
        if (kod.startsWith("DAIDMP"))      return OlcuTipi.DIAMETER;
        if (kod.startsWith("MNZ"))         return OlcuTipi.MATRIX_WH;
        if (kod.startsWith("SLT"))         return OlcuTipi.MATRIX_L_SLOT;
        if (kod.startsWith("DMP"))         return OlcuTipi.MATRIX_WH;
        if (kod.startsWith("KANM"))        return OlcuTipi.STRING_SIZE;
        if (kod.startsWith("DANM"))        return OlcuTipi.DIAMETER;
        if (kod.startsWith("KASWRDIF"))    return OlcuTipi.STRING_SIZE_SINGLE;
        if (kod.startsWith("DASWRDIF"))    return OlcuTipi.STRING_SIZE_SINGLE;
        if (kod.startsWith("PNJ_ALTIKUTU"))return OlcuTipi.STRING_SIZE_SINGLE;
        if (kod.startsWith("PNJ"))         return OlcuTipi.MATRIX_WH;
        if (kod.startsWith("KPK"))         return OlcuTipi.MATRIX_WH;
        if (kod.startsWith("BOX_WH"))      return OlcuTipi.MATRIX_WH;
        if (kod.startsWith("BOX_LS"))      return OlcuTipi.MATRIX_L_SLOT;
        if (kod.startsWith("BOX_STR"))     return OlcuTipi.STRING_SIZE_SINGLE;
        return null;
    }

    private static OlcuTipi resolveFromDb(String urunKodu) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT strategy FROM price_table WHERE sheet_name=? OR prefix=? LIMIT 1")) {
            ps.setString(1, urunKodu);
            ps.setString(2, urunKodu);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String strateji = rs.getString("strategy");
                if (strateji == null) return null;
                return switch (strateji.toUpperCase()) {
                    case "MATRIX_WH"          -> OlcuTipi.MATRIX_WH;
                    case "MATRIX_L_SLOT"      -> OlcuTipi.MATRIX_L_SLOT;
                    case "DIAMETER"           -> OlcuTipi.DIAMETER;
                    case "STRING_SIZE"        -> OlcuTipi.STRING_SIZE;
                    case "STRING_SIZE_SINGLE" -> OlcuTipi.STRING_SIZE_SINGLE;
                    default -> null;
                };
            }
        } catch (Exception e) {
            return null;
        }
    }
}
