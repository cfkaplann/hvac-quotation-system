package com.teklif.ui.resolver;

import java.util.HashMap;
import java.util.Map;

public class GosterimKodResolver {

    // ⭐ Prefix → UI Kod Map
    private static final Map<String,String> map = new HashMap<>();

    static {

        // =========================
        // ⭐ DAİRESEL ANEMOSTAD
        // =========================
        map.put("DANM_TAVN", "Rou RC");
        map.put("DANM_GEMI", "Rou RS");
        
        // =========================
        // ⭐ KARE ANEMOSTAD
        // =========================
        map.put("KANM_DUZKNT", "Qua L");
        map.put("KANM_EGRKNT", "Qua C");

        // =========================
        // ⭐ MENFEZ
        // =========================
        map.put("MNZ_TEKSIRA", "Glea SD");
        map.put("MNZ_CIFTSIRA", "Glea DD");
        map.put("MNZ_DAITEKSIRA", "Glea R-A");
        map.put("MNZ_DAICIFTSIRA", "Glea R-B");
        map.put("MNZ_KARPET", "Glea SEC");
        map.put("MNZ_KAPTRA", "Glea T");
        map.put("MNZ_LINE", "Glea L");
        map.put("MNZ_LIFTUT", "Glea FF");
        map.put("MNZ_PERF", "Glea P");
        map.put("MNZ_YERLINE", "Glea FL");

        // =========================
        // ⭐ SLOT
        // =========================
        map.put("SLT_TOP", "Venty A");
        map.put("SLT_DAG", "Venty B");
        map.put("SLT_GIZTOP", "Venty H");
        map.put("SLT_GIZDAG", "Venty H");
        map.put("SLT_MAKA", "Venty Roller");

        // =========================
        // ⭐ PANJUR
        // =========================
        map.put("PNJ_AKSTK", "Louvrex ACU");
        map.put("PNJ_ALTIKUTU", "Louvrex Hexa");
        map.put("PNJ_EGRIKNT", "Louvrex C");
        map.put("PNJ_GNSKNT", "Louvrex W");
        map.put("PNJ_KUMTUT", "Louvrex ST");
        map.put("PNJ_SBTDARKNT", "Louvrex N");
        map.put("PNJ_SRBSTKNT", "Louvrex F");
        map.put("PNJ_SIVAUST", "Louvrex SM");

        // =========================
        // ⭐ DİKDÖRTGEN DAMPER
        // =========================
        map.put("DMP_HAVA", "Aero");
        map.put("DMP_BLADRA", "Reflux");
        map.put("DMP_RELI", "Ventra");
        map.put("DMP_YAN", "Aegis F-R");
        map.put("DMP_YANBEL", "Aegis F-EN-R");
        map.put("DMP_DUMTAH", "Aegis S");
        map.put("DMP_DUMTAHBEL", "Aegis S-EN");
      
        
        // =========================
        // ⭐ DAİRESEL DAMPER
        // =========================
        map.put("DAIDMP_HAVA", "Aero C");
        map.put("DAIDMP_GALKLAYAN", "Aegis F-C");
        map.put("DAIDMP_YAN", "Aegis F-EN-C");
        map.put("DAIDMP_YANITH", "Aegis F-EN-C (İTHAL)");
        map.put("DAIDMP_BLADRA", "Reflux C");

        // =========================
        // ⭐KARE SWIRL DİFÜZÖR
        // =========================
        map.put("KASWRDIF_SABKNT", "Wirl S-FD");
        map.put("KASWRDIF_ISTD", "Wirl O");
        map.put("KASWRDIF_AYRKNT", "Wirl AD");
        map.put("KASWRDIF_4YON", "Wirl 4W");
        map.put("KASWRDIF_DRUM", "Beat");
      
        
        // =========================
        // ⭐DAİRESEL SWIRL DİFÜZÖR
        // =========================
        map.put("DASWRDIF_TELS", "Extend");
        map.put("DASWRDIF_KONF", "Confy");
        map.put("DASWRDIF_YERDOS", "Loca");
        map.put("DASWRDIF_DAIPAN", "Rou RRC");
        map.put("DASWRDIF_TURBLS", "Draft");
        map.put("DASWRDIF_JETNOZ", "Beta");
        map.put("DASWRDIF_SBTKNT", "Wirl R-FD");
        map.put("DASWRDIF_HIZISTDKNT", "Wirl ROR-D");
        map.put("DASWRDIF_AYRBLKNT", "Wirl OR-S");
        
        // =========================
        // ⭐ KAPAK
        // =========================
        map.put("KPK_KONT", "Inspector");
        map.put("KPK_KONTIZO", "Inspector ISO");
        map.put("KPK_KAPETMNZKONT", "Inspector EC");
        map.put("KPK_LINMNZKONT", "Inspector L");
        
        // =========================
        // ⭐ KUTU
        // =========================
        map.put("BOX_WH", "");
        map.put("BOX_LS", "");
        map.put("BOX_STR", "");
    
      
    }

    // =====================================================
    // ⭐ ANA METHOD
    // =====================================================
    public static String resolve(String urunKodu){

        if(urunKodu == null) return "";

        for(Map.Entry<String,String> e : map.entrySet()){

            if(urunKodu.startsWith(e.getKey())){
                return e.getValue();
            }
        }

        // fallback
        return urunKodu;
    }
}