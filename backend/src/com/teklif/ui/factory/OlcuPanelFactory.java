package com.teklif.ui.factory;

import javax.swing.*;

import com.teklif.model.OlcuTipi;
import com.teklif.model.OlcuAlanTipi;
import com.teklif.ui.component.OlcuComponent;   // ⭐ YENİ IMPORT

public class OlcuPanelFactory {

    // =====================================================
    // 🔴 ESKİ MİMARİ (SİLMEDİM)
    // =====================================================
    public static JPanel createPanel(OlcuTipi tip){

        JPanel p = new JPanel();

        switch(tip){

            case MATRIX_WH:
                p.add(new JLabel("W:"));
                p.add(new JTextField(5));
                p.add(new JLabel("H:"));
                p.add(new JTextField(5));
                break;

            case MATRIX_L_SLOT:
                p.add(new JLabel("L:"));
                p.add(new JTextField(5));
                p.add(new JLabel("Slot:"));
                p.add(new JTextField(5));
                break;

            case DIAMETER:
                p.add(new JLabel("Ø:"));
                p.add(new JTextField(5));
                break;

            case STRING_SIZE:
                p.add(new JLabel("Dış Ölçü:"));
                p.add(new JTextField(8));
                p.add(new JLabel("Boğaz:"));
                p.add(new JTextField(8));
                break;
        }

        return p;
    }

    // =====================================================
    // 🟢 YENİ MİMARİ (ESKİYİ BOZMADAN EKLENDİ)
    // =====================================================
    public static OlcuComponent createComponent(OlcuAlanTipi tip){
        return new OlcuComponent(tip);
    }

    // =====================================================
    // 🟡 PANEL ÜRETEN ARA METHOD (DURSUN)
    // =====================================================
    public static JPanel createField(OlcuAlanTipi tip){

        JPanel p = new JPanel();

        JLabel lbl = new JLabel(labelBul(tip));
        JTextField txt = new JTextField(8);

        p.add(lbl);
        p.add(txt);

        return p;
    }

    private static String labelBul(OlcuAlanTipi tip){

        switch(tip){
            case GENISLIK: return "Genişlik";
            case YUKSEKLIK: return "Yükseklik";
            case UZUNLUK: return "Uzunluk";
            case CAP: return "Çap Ø";
            case SLOT_SAYISI: return "Slot Sayısı";

            // ⭐ SENİN İSTEDİĞİN DÜZELTME
            case KASA_WH: return "Dış Ölçü (WxH)";
            case BOGAZ_WH: return "Boğaz Ölçü (WxH)";

            case BOGAZ_CAP: return "Boğaz Çapı Ø(mm)";
            case NETIC_CAP: return "Anma Çapı Ø(mm)";
            default: return tip.name();
        }
    }

}
