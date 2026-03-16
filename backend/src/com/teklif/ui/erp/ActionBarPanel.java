package com.teklif.ui.erp;

import javax.swing.*;
import java.awt.*;

public class ActionBarPanel extends JPanel {

    private JButton btnUrunEkle = new JButton("Ürün Ekle");
    private JButton btnUrunSil  = new JButton("Ürün Sil");

    public ActionBarPanel(){

        setLayout(new FlowLayout(FlowLayout.RIGHT));

        add(btnUrunEkle);
        add(btnUrunSil);
    }

    public JButton getBtnUrunEkle(){
        return btnUrunEkle;
    }

    public JButton getBtnUrunSil(){
        return btnUrunSil;
    }
}
