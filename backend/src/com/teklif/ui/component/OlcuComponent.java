package com.teklif.ui.component;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;

import com.teklif.model.OlcuAlanTipi;

public class OlcuComponent extends JPanel {

    private OlcuAlanTipi tip;
    private JLabel lbl;

    private JTextField txt;
    private JComboBox<String> combo;

    public OlcuComponent(OlcuAlanTipi tip){

        this.tip = tip;

        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2,2,2,2);
        gbc.anchor = GridBagConstraints.WEST;

        // =====================================================
        // LABEL
        // =====================================================

        lbl = new JLabel(labelBul(tip));
        lbl.setPreferredSize(new Dimension(160,22));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        add(lbl, gbc);

        // =====================================================
        // INPUT
        // =====================================================

        gbc.gridx = 1;

        // ⭐⭐⭐ ASIL FIX BURASI ⭐⭐⭐
        gbc.weightx = 0;                  // grow KAPALI
        gbc.fill = GridBagConstraints.NONE; // stretch KAPALI

        if(tip == OlcuAlanTipi.KASA_WH ||
        		   tip == OlcuAlanTipi.BOGAZ_WH ||
        		   tip == OlcuAlanTipi.KASA_CAP){


            combo = new JComboBox<>();
            combo.setPreferredSize(new Dimension(75,22));
            combo.setMinimumSize(new Dimension(75,22));
            combo.setMaximumSize(new Dimension(75,22));
            add(combo, gbc);
        }
        else{

            txt = new JTextField();
            txt.setPreferredSize(new Dimension(75,22));
            txt.setMinimumSize(new Dimension(75,22));
            txt.setMaximumSize(new Dimension(75,22));

            txt.setDocument(new javax.swing.text.PlainDocument(){
                @Override
                public void insertString(int offs, String str,
                        javax.swing.text.AttributeSet a)
                        throws javax.swing.text.BadLocationException {

                    if(str==null) return;

                    String yeni = getText(0,getLength()) + str;

                    // =========================================
                    // SLOT SAYISI → SADECE 1-6
                    // =========================================
                    if(tip == OlcuAlanTipi.SLOT_SAYISI){

                        if(!yeni.matches("[1-6]?")) return;

                        super.insertString(offs,str,a);
                        return;
                    }

                    // =========================================
                    // NORMAL SAYISAL ALAN
                    // =========================================
                    if(yeni.matches("[0-9.]*")){
                        super.insertString(offs,str,a);
                    }
                }
            });


            add(txt, gbc);
        }
    }

    public OlcuAlanTipi getTip(){
        return tip;
    }

    public String getValue(){

        if(combo!=null){
            Object val = combo.getSelectedItem();

            if(val == null) return "";

            String s = val.toString().trim();

            // 🔥 LIST FORMAT TEMİZLE
            if(s.startsWith("[") && s.endsWith("]")){
                s = s.substring(1, s.length()-1);
            }

            return s;
        }


        if(txt!=null){
            return txt.getText();
        }

        return "";
    }
    public void setValue(String value){

        if(combo != null){
            combo.setSelectedItem(value);
            return;
        }

        if(txt != null){
            txt.setText(value);
        }
    }


    public double getDoubleValue(){

        try{
            String v = getValue();
            if(v==null || v.isBlank()) return 0;
            return Double.parseDouble(v);
        }catch(Exception e){
            return 0;
        }
    }

    public void addChangeListener(Runnable listener){

        if(txt!=null){

            txt.getDocument().addDocumentListener(new DocumentListener(){
                public void insertUpdate(DocumentEvent e){ listener.run(); }
                public void removeUpdate(DocumentEvent e){ listener.run(); }
                public void changedUpdate(DocumentEvent e){ listener.run(); }
            });
        }

        if(combo!=null){
            combo.addActionListener(e -> listener.run());
        }
    }

    public void setAllowedValues(List<String> values){

        if(combo==null) return;

        Object old = combo.getSelectedItem();

        combo.removeAllItems();

        if(values == null) values = List.of();

        for(String v : values){
            combo.addItem(v);
        }

        // ⭐ eski seçim varsa koru
        if(old!=null && values.contains(old.toString())){
            combo.setSelectedItem(old);
        }
        // ⭐ YOKSA OTOMATİK SEÇ
        else if(!values.isEmpty()){
            combo.setSelectedIndex(0);   // 🔥 EN ÖNEMLİ SATIR
        }
    }



    private String labelBul(OlcuAlanTipi tip){

        switch(tip){
            case GENISLIK: return "Genişlik";
            case YUKSEKLIK: return "Yükseklik";
            case UZUNLUK: return "Uzunluk";
            case CAP: return "Çap Ø";
            case SLOT_SAYISI: return "Slot Sayısı";
            case KASA_WH: return "Dış Ölçü WxH";
            case BOGAZ_WH: return "Boğaz WxH";
            case BOGAZ_CAP: return "Boğaz Çapı Ø";
            case NETIC_CAP: return "Anma Çapı Ø";
            default: return tip.name();
        }
    }
 // ⭐ UI label override (arka plan değişmez)
    public void setLabelText(String text){
        if(text == null || text.isBlank()) return;
        lbl.setText(text);
    }
}
