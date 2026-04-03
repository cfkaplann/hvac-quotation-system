package com.teklif.model.takip;

public class TeklifNot {
    private Integer id;
    private Integer teklifId;
    private String  icerik;
    private String  tarih;
    private String  yazan;

    public Integer getId()               { return id; }
    public void setId(Integer id)        { this.id = id; }
    public Integer getTeklifId()                  { return teklifId; }
    public void setTeklifId(Integer teklifId)     { this.teklifId = teklifId; }
    public String getIcerik()            { return icerik; }
    public void setIcerik(String icerik) { this.icerik = icerik; }
    public String getTarih()             { return tarih; }
    public void setTarih(String tarih)   { this.tarih = tarih; }
    public String getYazan()             { return yazan; }
    public void setYazan(String yazan)   { this.yazan = yazan; }
}
