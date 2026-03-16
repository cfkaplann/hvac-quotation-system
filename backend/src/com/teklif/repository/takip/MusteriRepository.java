package com.teklif.repository.takip;

import com.teklif.db.ConnectionManager;
import com.teklif.model.takip.Musteri;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.*;

@Repository
public class MusteriRepository {

    public List<Musteri> hepsiniGetir() throws Exception {
        List<Musteri> liste = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM musteri ORDER BY firma_adi");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    public Optional<Musteri> idIleGetir(int id) throws Exception {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM musteri WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public List<Musteri> ara(String kelime) throws Exception {
        List<Musteri> liste = new ArrayList<>();
        String sql = "SELECT * FROM musteri WHERE firma_adi LIKE ? OR yetkili LIKE ? ORDER BY firma_adi";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String p = "%" + kelime + "%";
            ps.setString(1, p); ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(map(rs));
            }
        }
        return liste;
    }

    public Musteri kaydet(Musteri m) throws Exception {
        String sql = "INSERT INTO musteri (firma_adi,yetkili,telefon,eposta,adres,notlar) VALUES (?,?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getFirmaAdi()); ps.setString(2, m.getYetkili());
            ps.setString(3, m.getTelefon());  ps.setString(4, m.getEposta());
            ps.setString(5, m.getAdres());    ps.setString(6, m.getNotlar());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) m.setId(k.getInt(1)); }
        }
        return m;
    }

    public boolean guncelle(Musteri m) throws Exception {
        String sql = "UPDATE musteri SET firma_adi=?,yetkili=?,telefon=?,eposta=?,adres=?,notlar=? WHERE id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getFirmaAdi()); ps.setString(2, m.getYetkili());
            ps.setString(3, m.getTelefon());  ps.setString(4, m.getEposta());
            ps.setString(5, m.getAdres());    ps.setString(6, m.getNotlar());
            ps.setInt(7, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean sil(int id) throws Exception {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM musteri WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Musteri map(ResultSet rs) throws SQLException {
        Musteri m = new Musteri();
        m.setId(rs.getInt("id"));
        m.setFirmaAdi(rs.getString("firma_adi"));
        m.setYetkili(rs.getString("yetkili"));
        m.setTelefon(rs.getString("telefon"));
        m.setEposta(rs.getString("eposta"));
        m.setAdres(rs.getString("adres"));
        m.setNotlar(rs.getString("notlar"));
        m.setOlusturmaTarihi(rs.getString("olusturma_tarihi"));
        return m;
    }
}
