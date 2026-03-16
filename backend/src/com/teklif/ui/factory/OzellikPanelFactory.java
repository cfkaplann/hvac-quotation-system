package com.teklif.ui.factory;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import com.teklif.model.*;
import com.teklif.ui.component.RalCompositeComponent;

public class OzellikPanelFactory {

    private static final int INPUT_HEIGHT = 22;
    private static final Dimension LABEL_SIZE = new Dimension(110, INPUT_HEIGHT);

    public static Map<OzellikTipi, JComponent> createERPColumns(
            JPanel pnlTeknik,
            JPanel pnlRal,
            UrunKart kart,
            Runnable changeListener){

        Map<OzellikTipi,JComponent> map = new HashMap<>();

        for(OzellikTipi tip : kart.getOzellikler()){

            List<String> secimlerRaw = kart.getIzinliSecimler().get(tip);
            if(secimlerRaw == null || secimlerRaw.isEmpty()) continue;

            // UNIQUE + TRIM
            LinkedHashSet<String> uniqSet = new LinkedHashSet<>();
            for(String s : secimlerRaw){
                if(s == null) continue;
                String t = s.trim();
                if(!t.isBlank()) uniqSet.add(t);
            }
            List<String> secimler = new ArrayList<>(uniqSet);

            JPanel target =
                    (tip == OzellikTipi.RAL || tip == OzellikTipi.AKSESUAR_TIPI)
                            ? pnlRal
                            : pnlTeknik;

            // ⭐ TÜM SATIRLAR AYNI LAYOUT
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            // ⭐ LABEL HER ZAMAN VAR
            JLabel lbl = new JLabel(labelBul(tip));
            lbl.setPreferredSize(LABEL_SIZE);
            row.add(lbl);

            // =====================================================
            // ⭐ AKSESUAR -> RADIO GRID
            // =====================================================
            if(tip == OzellikTipi.AKSESUAR_TIPI){

                boolean tekli = kart.isAksesuarTekliMi();

                JPanel grid = new JPanel(new GridLayout(0, 2, 10, 2));
                ButtonGroup group = tekli ? new ButtonGroup() : null;

                for(String s : secimler){
                    JRadioButton rb = new JRadioButton(s);
                    rb.addActionListener(e -> changeListener.run());
                    if(group != null) group.add(rb);
                    grid.add(rb);
                }

                row.add(grid);
                target.add(row);
                map.put(tip, grid);
                continue;
            }

            // =====================================================
            // ⭐ RAL -> COMPOSITE COMPONENT
            // =====================================================
            if(tip == OzellikTipi.RAL){

                RalCompositeComponent ralComp =
                        new RalCompositeComponent(secimler);

                row.add(ralComp);
                target.add(row);

                map.put(tip, ralComp);
                continue;
            }

            // =====================================================
            // ⭐ NORMAL COMBO
            // =====================================================
            JComboBox<String> cmb = new JComboBox<>();

            for(String s : secimler){
                cmb.addItem(s);
            }

            cmb.setRenderer(
                new com.teklif.ui.util.PlaceholderComboRenderer("Seçim Yapınız")
            );

            cmb.addActionListener(e -> changeListener.run());

            row.add(cmb);
            target.add(row);

            map.put(tip, cmb);
        }

        return map;
    }

    private static String labelBul(OzellikTipi tip){
        switch(tip){
            case RAL: return "RAL";
            case MONTAJ: return "Montaj";
            case DAMPER_TIPI: return "Damper Tipi";
            case AKSESUAR_TIPI: return "Aksesuar";
            case CERCEVE_TIPI: return "Çerçeve";
            case MENFEZ_TIPI: return "Menfez Tipi";
            default: return tip.name();
        }
    }
}
