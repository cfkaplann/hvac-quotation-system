package com.teklif.model.takip;

import java.util.ArrayList;
import java.util.List;

public class Teklif {

    public enum Durum { BEKLIYOR, ONAYLANDI, REDDEDILDI, REVIZE, IPTAL }

    private Integer id;
    private String teklifNo;
    private int revizeNo;
    private String isAdi;
    private Integer musteriId;
    private Musteri musteri;
    private String teklifTarihi;
    private String gecerlilikTarihi;
    private String teklifiVeren;
    private String paraBirimi = "TL";
    private double kdvOrani   = 20.0;
    private double araToplam;
    private double kdvTutari;
    private double genelToplam;
    private Durum  durum      = Durum.BEKLIYOR;
    private String notlar;
    private String olusturmaTarihi;
    private String guncellemeTarihi;
    private List<TeklifKalem> kalemler = new ArrayList<>();

    public void hesaplaToplam() {
        this.araToplam   = kalemler.stream().mapToDouble(TeklifKalem::getToplam).sum();
        this.kdvTutari   = araToplam * (kdvOrani / 100.0);
        this.genelToplam = araToplam + kdvTutari;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTeklifNo() { return teklifNo; }
    public void setTeklifNo(String teklifNo) { this.teklifNo = teklifNo; }
    public int getRevizeNo() { return revizeNo; }
    public void setRevizeNo(int revizeNo) { this.revizeNo = revizeNo; }
    public String getIsAdi() { return isAdi; }
    public void setIsAdi(String isAdi) { this.isAdi = isAdi; }
    public Integer getMusteriId() { return musteriId; }
    public void setMusteriId(Integer musteriId) { this.musteriId = musteriId; }
    public Musteri getMusteri() { return musteri; }
    public void setMusteri(Musteri musteri) { this.musteri = musteri; }
    public String getTeklifTarihi() { return teklifTarihi; }
    public void setTeklifTarihi(String teklifTarihi) { this.teklifTarihi = teklifTarihi; }
    public String getGecerlilikTarihi() { return gecerlilikTarihi; }
    public void setGecerlilikTarihi(String gecerlilikTarihi) { this.gecerlilikTarihi = gecerlilikTarihi; }
    public String getTeklifiVeren() { return teklifiVeren; }
    public void setTeklifiVeren(String teklifiVeren) { this.teklifiVeren = teklifiVeren; }
    public String getParaBirimi() { return paraBirimi; }
    public void setParaBirimi(String paraBirimi) { this.paraBirimi = paraBirimi; }
    public double getKdvOrani() { return kdvOrani; }
    public void setKdvOrani(double kdvOrani) { this.kdvOrani = kdvOrani; }
    public double getAraToplam() { return araToplam; }
    public void setAraToplam(double araToplam) { this.araToplam = araToplam; }
    public double getKdvTutari() { return kdvTutari; }
    public void setKdvTutari(double kdvTutari) { this.kdvTutari = kdvTutari; }
    public double getGenelToplam() { return genelToplam; }
    public void setGenelToplam(double genelToplam) { this.genelToplam = genelToplam; }
    public Durum getDurum() { return durum; }
    public void setDurum(Durum durum) { this.durum = durum; }
    public String getNotlar() { return notlar; }
    public void setNotlar(String notlar) { this.notlar = notlar; }
    public String getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(String v) { this.olusturmaTarihi = v; }
    public String getGuncellemeTarihi() { return guncellemeTarihi; }
    public void setGuncellemeTarihi(String v) { this.guncellemeTarihi = v; }
    public List<TeklifKalem> getKalemler() { return kalemler; }
    public void setKalemler(List<TeklifKalem> kalemler) { this.kalemler = kalemler; }
}
