package com.teklif.model.takip;

public class TeklifKalem {
    private Integer id;
    private Integer teklifId;
    private Integer siraNo;
    private String  urunKodu;
    private String  urunAdi;
    private int     adet   = 1;
    private String  birim  = "Adet";
    private double  birimFiyat;
    private double  iskonto;
    private double  toplam;
    private String  aciklama;

    // Yeni ölçü/özellik alanları
    private String genislik;
    private String yukseklik;
    private String uzunluk;
    private String cap;
    private String cerceveTipi;
    private String damperTipi;
    private String ralKodu;
    private String montaj;

    public void hesaplaToplam() {
        double net = birimFiyat * adet;
        this.toplam = net - (net * iskonto / 100.0);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getTeklifId() { return teklifId; }
    public void setTeklifId(Integer teklifId) { this.teklifId = teklifId; }
    public Integer getSiraNo() { return siraNo; }
    public void setSiraNo(Integer siraNo) { this.siraNo = siraNo; }
    public String getUrunKodu() { return urunKodu; }
    public void setUrunKodu(String urunKodu) { this.urunKodu = urunKodu; }
    public String getUrunAdi() { return urunAdi; }
    public void setUrunAdi(String urunAdi) { this.urunAdi = urunAdi; }
    public int getAdet() { return adet; }
    public void setAdet(int adet) { this.adet = adet; }
    public String getBirim() { return birim; }
    public void setBirim(String birim) { this.birim = birim; }
    public double getBirimFiyat() { return birimFiyat; }
    public void setBirimFiyat(double birimFiyat) { this.birimFiyat = birimFiyat; }
    public double getIskonto() { return iskonto; }
    public void setIskonto(double iskonto) { this.iskonto = iskonto; }
    public double getToplam() { return toplam; }
    public void setToplam(double toplam) { this.toplam = toplam; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }

    public String getGenislik() { return genislik; }
    public void setGenislik(String genislik) { this.genislik = genislik; }
    public String getYukseklik() { return yukseklik; }
    public void setYukseklik(String yukseklik) { this.yukseklik = yukseklik; }
    public String getUzunluk() { return uzunluk; }
    public void setUzunluk(String uzunluk) { this.uzunluk = uzunluk; }
    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }
    public String getCerceveTipi() { return cerceveTipi; }
    public void setCerceveTipi(String cerceveTipi) { this.cerceveTipi = cerceveTipi; }
    public String getDamperTipi() { return damperTipi; }
    public void setDamperTipi(String damperTipi) { this.damperTipi = damperTipi; }
    public String getRalKodu() { return ralKodu; }
    public void setRalKodu(String ralKodu) { this.ralKodu = ralKodu; }
    public String getMontaj() { return montaj; }
    public void setMontaj(String montaj) { this.montaj = montaj; }
}
