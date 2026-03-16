package com.teklif.ui.component;

import javax.swing.*;
import java.awt.*;

public class RalCompositeComponent extends JPanel {

    private JComboBox<String> cmbRal;
    private JTextField txtRalKod;
    private JLabel lblKod;

    public RalCompositeComponent(java.util.List<String> secenekler) {

    	setLayout(new FlowLayout(FlowLayout.LEFT,6,2));

    	
    	

    	cmbRal = new JComboBox<>(secenekler.toArray(new String[0]));
    	cmbRal.setPreferredSize(new Dimension(140,22));

    	JLabel lblKod = new JLabel("RAL Kodu:");
    	txtRalKod = new JTextField(8);

    	lblKod.setVisible(false);
    	txtRalKod.setVisible(false);

    	  // ⭐ label artık burada
    	add(cmbRal);
    	add(lblKod);
    	add(txtRalKod);


        cmbRal.addActionListener(e -> {

            String secim = (String) cmbRal.getSelectedItem();

            boolean boyaliMi =
                    secim != null &&
                    secim.equalsIgnoreCase("Boyalı");

            lblKod.setVisible(boyaliMi);
            txtRalKod.setVisible(boyaliMi);

            revalidate();
            repaint();
        });
    }

    public String getValue(){

        String ral = (String) cmbRal.getSelectedItem();

        if("Boyalı".equalsIgnoreCase(ral)){
            String kod = txtRalKod.getText();
            if(kod!=null && !kod.isBlank())
                return ral + " - " + kod;
        }

        return ral;
    }

    public String getRalSecim(){
        return (String)cmbRal.getSelectedItem();
    }

    public String getRalKod(){
        return txtRalKod.getText();
    }
}
