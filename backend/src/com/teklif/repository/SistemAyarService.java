package com.teklif.repository;

import com.teklif.db.ConnectionManager;
import java.sql.*;

public class SistemAyarService {

    public static double getDouble(String anahtar, double varsayilan) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT deger FROM sistem_ayar WHERE anahtar=?")) {
            ps.setString(1, anahtar);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Double.parseDouble(rs.getString("deger"));
            }
        } catch (Exception ignored) {}
        return varsayilan;
    }

    public static void set(String anahtar, String deger) throws Exception {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR REPLACE INTO sistem_ayar(anahtar,deger) VALUES(?,?)")) {
            ps.setString(1, anahtar); ps.setString(2, deger);
            ps.executeUpdate();
        }
    }
}
