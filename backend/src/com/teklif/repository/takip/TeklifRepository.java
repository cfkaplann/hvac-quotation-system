package com.teklif.repository.takip;

import com.teklif.db.ConnectionManager;
import com.teklif.model.takip.Musteri;
import com.teklif.model.takip.Teklif;
import com.teklif.model.takip.Teklif.Durum;
import com.teklif.model.takip.TeklifKalem;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
public class TeklifRepository {

    public synchronized String yeniTeklifNo() throws Exception {
        int yil = LocalDate.now().getYear();
        try (Connection c = ConnectionManager.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO teklif_no_sequence (yil,son) VALUES (?,0) ON CONFLICT(yil) DO NOTHING")) {
                ps.setInt(1, yil); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE teklif_no_sequence SET son=son+1 WHERE yil=?")) {
                ps.setInt(1, yil); ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT son FROM teklif_no_sequence WHERE yil=?")) {
                ps.setInt(1, yil);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return String.format("TKL-%d-%04d", yil, rs.getInt("son"));
                }
            }
        }
        throw new RuntimeException("Teklif no uretilemedi!");
    }

    public List<Teklif> hepsiniGetir() throws Exception {
        List<Teklif> liste = new ArrayList<>();
        String sql = "SELECT t.*, m.firma_adi, m.yetkili, m.telefon, m.eposta FROM teklif t LEFT JOIN musteri m ON t.musteri_id=m.id ORDER BY t.olusturma_tarihi DESC";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) liste.add(mapRow(rs));
        }
        return liste;
    }

    public List<Teklif> durumIleGetir(Durum durum) throws Exception {
        List<Teklif> liste = new ArrayList<>();
        String sql = "SELECT t.*, m.firma_adi, m.yetkili, m.telefon, m.eposta FROM teklif t LEFT JOIN musteri m ON t.musteri_id=m.id WHERE t.durum=? ORDER BY t.olusturma_tarihi DESC";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, durum.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) liste.add(mapRow(rs));
            }
        }
        return liste;
    }

    public Optional<Teklif> idIleGetir(int id) throws Exception {
        String sql = "SELECT t.*, m.firma_adi, m.yetkili, m.telefon, m.eposta FROM teklif t LEFT JOIN musteri m ON t.musteri_id=m.id WHERE t.id=?";
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Teklif t = mapRow(rs);
                    t.setKalemler(kalemleriGetir(id));
                    return Optional.of(t);
                }
            }
        }
        return Optional.empty();
    }

    public Teklif kaydet(Teklif t) throws Exception {
        if (t.getTeklifNo() == null || t.getTeklifNo().isEmpty()) t.setTeklifNo(yeniTeklifNo());
        t.hesaplaToplam();
        String sql = "INSERT INTO teklif (teklif_no,revize_no,is_adi,musteri_id,teklif_tarihi,gecerlilik_tarihi,teklifi_veren,para_birimi,kdv_orani,ara_toplam,kdv_tutari,genel_toplam,durum,notlar) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = ConnectionManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getTeklifNo());   ps.setInt(2, t.getRevizeNo());
                ps.setString(3, t.getIsAdi());      ps.setObject(4, t.getMusteriId());
                ps.setString(5, t.getTeklifTarihi()); ps.setString(6, t.getGecerlilikTarihi());
                ps.setString(7, t.getTeklifiVeren()); ps.setString(8, t.getParaBirimi());
                ps.setDouble(9, t.getKdvOrani());   ps.setDouble(10, t.getAraToplam());
                ps.setDouble(11, t.getKdvTutari()); ps.setDouble(12, t.getGenelToplam());
                ps.setString(13, t.getDurum().name()); ps.setString(14, t.getNotlar());
                ps.executeUpdate();
                try (ResultSet k = ps.getGeneratedKeys()) {
                    if (k.next()) t.setId(k.getInt(1));
                }
                if (t.getId() == null) {
                    try (PreparedStatement ps2 = c.prepareStatement("SELECT last_insert_rowid()");
                         ResultSet rs2 = ps2.executeQuery()) {
                        if (rs2.next()) t.setId(rs2.getInt(1));
                    }
                }
            }
            if (t.getKalemler() != null) kalemleriKaydet(c, t.getId(), t.getKalemler());
            c.commit();
        }
        return t;
    }

    public boolean guncelle(Teklif t) throws Exception {
        t.hesaplaToplam();
        String sql = "UPDATE teklif SET is_adi=?,musteri_id=?,teklif_tarihi=?,gecerlilik_tarihi=?,teklifi_veren=?,para_birimi=?,kdv_orani=?,ara_toplam=?,kdv_tutari=?,genel_toplam=?,durum=?,notlar=?,guncelleme_tarihi=datetime('now','localtime') WHERE id=?";
        try (Connection c = ConnectionManager.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, t.getIsAdi());      ps.setObject(2, t.getMusteriId());
                ps.setString(3, t.getTeklifTarihi()); ps.setString(4, t.getGecerlilikTarihi());
                ps.setString(5, t.getTeklifiVeren()); ps.setString(6, t.getParaBirimi());
                ps.setDouble(7, t.getKdvOrani());   ps.setDouble(8, t.getAraToplam());
                ps.setDouble(9, t.getKdvTutari());  ps.setDouble(10, t.getGenelToplam());
                ps.setString(11, t.getDurum().name()); ps.setString(12, t.getNotlar());
                ps.setInt(13, t.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement d = c.prepareStatement("DELETE FROM teklif_kalem WHERE teklif_id=?")) {
                d.setInt(1, t.getId()); d.executeUpdate();
            }
            if (t.getKalemler() != null) kalemleriKaydet(c, t.getId(), t.getKalemler());
            c.commit();
        }
        return true;
    }

    public boolean durumGuncelle(int id, Durum durum) throws Exception {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE teklif SET durum=?,guncelleme_tarihi=datetime('now','localtime') WHERE id=?")) {
            ps.setString(1, durum.name()); ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Teklif revizeOlustur(int teklifId) throws Exception {
        Teklif eski = idIleGetir(teklifId).orElseThrow(() -> new RuntimeException("Teklif bulunamadi: " + teklifId));
        Teklif yeni = new Teklif();
        yeni.setTeklifNo(eski.getTeklifNo());
        yeni.setRevizeNo(eski.getRevizeNo() + 1);
        yeni.setIsAdi(eski.getIsAdi());
        yeni.setMusteriId(eski.getMusteriId());
        yeni.setTeklifTarihi(LocalDate.now().toString());
        yeni.setGecerlilikTarihi(eski.getGecerlilikTarihi());
        yeni.setTeklifiVeren(eski.getTeklifiVeren());
        yeni.setParaBirimi(eski.getParaBirimi());
        yeni.setKdvOrani(eski.getKdvOrani());
        yeni.setDurum(Durum.BEKLIYOR);
        yeni.setNotlar("Revize: " + eski.getTeklifNo() + "-R" + eski.getRevizeNo());
        yeni.setKalemler(new ArrayList<>(eski.getKalemler()));
        return kaydet(yeni);
    }

    public boolean sil(int id) throws Exception {
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM teklif WHERE id=?")) {
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        }
    }

    private void kalemleriKaydet(Connection c, int teklifId, List<TeklifKalem> kalemler) throws SQLException {
        String sql = "INSERT INTO teklif_kalem (teklif_id,sira_no,urun_kodu,urun_adi,adet,birim,birim_fiyat,iskonto,toplam,aciklama,genislik,yukseklik,uzunluk,cap,cerceve_tipi,damper_tipi,ral_kodu,montaj) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            int sira = 1;
            for (TeklifKalem k : kalemler) {
                k.hesaplaToplam();
                ps.setInt(1, teklifId); ps.setInt(2, sira++);
                ps.setString(3, k.getUrunKodu()); ps.setString(4, k.getUrunAdi());
                ps.setInt(5, k.getAdet()); ps.setString(6, k.getBirim());
                ps.setDouble(7, k.getBirimFiyat()); ps.setDouble(8, k.getIskonto());
                ps.setDouble(9, k.getToplam()); ps.setString(10, k.getAciklama());
                ps.setString(11, k.getGenislik()); ps.setString(12, k.getYukseklik());
                ps.setString(13, k.getUzunluk());  ps.setString(14, k.getCap());
                ps.setString(15, k.getCerceveTipi()); ps.setString(16, k.getDamperTipi());
                ps.setString(17, k.getRalKodu()); ps.setString(18, k.getMontaj());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private List<TeklifKalem> kalemleriGetir(int teklifId) throws Exception {
        List<TeklifKalem> liste = new ArrayList<>();
        try (Connection c = ConnectionManager.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM teklif_kalem WHERE teklif_id=? ORDER BY sira_no")) {
            ps.setInt(1, teklifId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TeklifKalem k = new TeklifKalem();
                    k.setId(rs.getInt("id")); k.setTeklifId(rs.getInt("teklif_id"));
                    k.setSiraNo(rs.getInt("sira_no")); k.setUrunKodu(rs.getString("urun_kodu"));
                    k.setUrunAdi(rs.getString("urun_adi")); k.setAdet(rs.getInt("adet"));
                    k.setBirim(rs.getString("birim")); k.setBirimFiyat(rs.getDouble("birim_fiyat"));
                    k.setIskonto(rs.getDouble("iskonto")); k.setToplam(rs.getDouble("toplam"));
                    k.setAciklama(rs.getString("aciklama"));
                    k.setGenislik(rs.getString("genislik")); k.setYukseklik(rs.getString("yukseklik"));
                    k.setUzunluk(rs.getString("uzunluk")); k.setCap(rs.getString("cap"));
                    k.setCerceveTipi(rs.getString("cerceve_tipi")); k.setDamperTipi(rs.getString("damper_tipi"));
                    k.setRalKodu(rs.getString("ral_kodu")); k.setMontaj(rs.getString("montaj"));
                    liste.add(k);
                }
            }
        }
        return liste;
    }

    private Teklif mapRow(ResultSet rs) throws SQLException {
        Teklif t = new Teklif();
        t.setId(rs.getInt("id")); t.setTeklifNo(rs.getString("teklif_no"));
        t.setRevizeNo(rs.getInt("revize_no")); t.setIsAdi(rs.getString("is_adi"));
        t.setMusteriId((Integer) rs.getObject("musteri_id"));
        t.setTeklifTarihi(rs.getString("teklif_tarihi")); t.setGecerlilikTarihi(rs.getString("gecerlilik_tarihi"));
        t.setTeklifiVeren(rs.getString("teklifi_veren")); t.setParaBirimi(rs.getString("para_birimi"));
        t.setKdvOrani(rs.getDouble("kdv_orani")); t.setAraToplam(rs.getDouble("ara_toplam"));
        t.setKdvTutari(rs.getDouble("kdv_tutari")); t.setGenelToplam(rs.getDouble("genel_toplam"));
        t.setDurum(Durum.valueOf(rs.getString("durum")));
        t.setNotlar(rs.getString("notlar")); t.setOlusturmaTarihi(rs.getString("olusturma_tarihi"));
        t.setGuncellemeTarihi(rs.getString("guncelleme_tarihi"));
        try {
            String firma = rs.getString("firma_adi");
            if (firma != null) {
                Musteri m = new Musteri(); m.setId(t.getMusteriId()); m.setFirmaAdi(firma);
                m.setYetkili(rs.getString("yetkili")); m.setTelefon(rs.getString("telefon"));
                m.setEposta(rs.getString("eposta")); t.setMusteri(m);
            }
        } catch (Exception ignored) {}
        return t;
    }
}
