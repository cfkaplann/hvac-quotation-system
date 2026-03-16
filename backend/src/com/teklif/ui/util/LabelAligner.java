package com.teklif.ui.util;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelAligner {

    // =====================================================
    // ⭐ PUBLIC ALIGN METHOD
    // =====================================================
    public static void align(JPanel... panels){

        List<JLabel> labels = new ArrayList<>();

        // 🔥 recursive label toplama
        for(JPanel p : panels){
            collectLabels(p, labels);
        }

        if(labels.isEmpty()) return;

        // 🔥 en geniş label hesapla
        int maxWidth = 0;

        for(JLabel l : labels){
            int w = l.getPreferredSize().width;
            if(w > maxWidth){
                maxWidth = w;
            }
        }

        Dimension newSize = new Dimension(maxWidth,25);

        // 🔥 tüm label'lara aynı width ver
        for(JLabel l : labels){
            l.setPreferredSize(newSize);
            l.setMinimumSize(newSize);
        }

        // 🔥 UI refresh
        for(JPanel p : panels){
            p.revalidate();
            p.repaint();
        }
    }

    // =====================================================
    // ⭐ RECURSIVE LABEL FINDER
    // =====================================================
    private static void collectLabels(Container container,
                                      List<JLabel> list){

        for(Component c : container.getComponents()){

            if(c instanceof JLabel){
                list.add((JLabel)c);
            }

            // ⭐ nested panel destek
            if(c instanceof Container){
                collectLabels((Container)c, list);
            }
        }
    }
}
