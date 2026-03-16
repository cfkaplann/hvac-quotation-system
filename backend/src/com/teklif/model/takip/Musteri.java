package com.teklif.model.takip;

public class Musteri {
    private Integer id;
    private String firmaAdi;
    private String yetkili;
    private String telefon;
    private String eposta;
    private String adres;
    private String notlar;
    private String olusturmaTarihi;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getFirmaAdi() { return firmaAdi; }
    public void setFirmaAdi(String firmaAdi) { this.firmaAdi = firmaAdi; }
    public String getYetkili() { return yetkili; }
    public void setYetkili(String yetkili) { this.yetkili = yetkili; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
    public String getEposta() { return eposta; }
    public void setEposta(String eposta) { this.eposta = eposta; }
    public String getAdres() { return adres; }
    public void setAdres(String adres) { this.adres = adres; }
    public String getNotlar() { return notlar; }
    public void setNotlar(String notlar) { this.notlar = notlar; }
    public String getOlusturmaTarihi() { return olusturmaTarihi; }
    public void setOlusturmaTarihi(String v) { this.olusturmaTarihi = v; }
}
