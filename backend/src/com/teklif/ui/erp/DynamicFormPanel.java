package com.teklif.ui.erp;

import javax.swing.*;
import java.awt.*;

public class DynamicFormPanel extends JPanel {

    private JPanel pnlOlculer;
    private JPanel pnlTeknik;
    private JPanel pnlRalAksesuar;

    public DynamicFormPanel(){

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // =========================
        // 3 KOLON → GridBag ile oranlı
        // 1.kolon küçük, 2 orta, 3 büyük
        // =========================
        JPanel grid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0,0,0,12);
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;

        pnlOlculer = new JPanel();
        pnlOlculer.setLayout(new BoxLayout(pnlOlculer, BoxLayout.Y_AXIS));

        pnlTeknik = new JPanel();
        pnlTeknik.setLayout(new BoxLayout(pnlTeknik, BoxLayout.Y_AXIS));

        pnlRalAksesuar = new JPanel();
        pnlRalAksesuar.setLayout(new BoxLayout(pnlRalAksesuar, BoxLayout.Y_AXIS));
        pnlRalAksesuar.setAlignmentX(Component.LEFT_ALIGNMENT);


        JScrollPane sp1 = new JScrollPane(wrapTop(pnlOlculer));
        JScrollPane sp2 = new JScrollPane(wrapTop(pnlTeknik));
        JScrollPane sp3 = new JScrollPane(wrapTop(pnlRalAksesuar));

        // scrollbar davranışı
        sp1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        // --- 1. kolon (küçük) ---
        gbc.gridx = 0;
        gbc.weightx = 0.28;          // küçük
        grid.add(sp1, gbc);

        // --- 2. kolon (orta) ---
        gbc.gridx = 1;
        gbc.weightx = 0.30;          // orta
        grid.add(sp2, gbc);

        // --- 3. kolon (büyük) ---
        gbc.gridx = 2;
        gbc.weightx = 0.42;          // büyük
        gbc.insets = new Insets(0,0,0,0); // son kolonda sağ boşluk yok
        grid.add(sp3, gbc);

        add(grid, BorderLayout.CENTER);
    }

    private JPanel wrapTop(JPanel p){

        JPanel w = new JPanel(new BorderLayout());

        p.setAlignmentX(Component.LEFT_ALIGNMENT); // ⭐ EKLE

        w.add(p, BorderLayout.WEST); // ⭐ NORTH → WEST
        return w;
    }


    public JPanel getPnlOlculer(){ return pnlOlculer; }
    public JPanel getPnlTeknik(){ return pnlTeknik; }
    public JPanel getPnlRalAksesuar(){ return pnlRalAksesuar; }
}
