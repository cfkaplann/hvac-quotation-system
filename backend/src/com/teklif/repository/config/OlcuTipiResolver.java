package com.teklif.repository.config;

import com.teklif.model.OlcuTipi;

public class OlcuTipiResolver {

    public static OlcuTipi resolve(String urunKodu){

        if(urunKodu.startsWith("MNZ"))
            return OlcuTipi.MATRIX_WH;

        if(urunKodu.startsWith("SLT"))
            return OlcuTipi.MATRIX_L_SLOT;

        if(urunKodu.startsWith("DMP"))
            return OlcuTipi.MATRIX_WH;

        if(urunKodu.startsWith("DAIDMP"))
            return OlcuTipi.DIAMETER;

        if(urunKodu.startsWith("KANM"))
            return OlcuTipi.STRING_SIZE;

        if(urunKodu.startsWith("DANM"))
            return OlcuTipi.DIAMETER;

        if(urunKodu.startsWith("KASWRDIF"))
            return OlcuTipi.STRING_SIZE_SINGLE;

        if(urunKodu.startsWith("DASWRDIF"))
            return OlcuTipi.STRING_SIZE_SINGLE;
        
        if(urunKodu.startsWith("PNJ_ALTIKUTU"))
            return OlcuTipi.STRING_SIZE_SINGLE;

        if(urunKodu.startsWith("PNJ"))
            return OlcuTipi.MATRIX_WH;

        if(urunKodu.startsWith("KPK"))
            return OlcuTipi.MATRIX_WH;
        
        if(urunKodu.startsWith("BOX_WH"))
            return OlcuTipi.MATRIX_WH;
        
        if(urunKodu.startsWith("BOX_LS"))
            return OlcuTipi.MATRIX_L_SLOT;
        
        if(urunKodu.startsWith("BOX_STR"))
            return OlcuTipi.STRING_SIZE_SINGLE;

        throw new RuntimeException("Olcu tipi bulunamadı : " + urunKodu);
    }
}
