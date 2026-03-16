package com.teklif.ui.util;

import javax.swing.*;
import java.awt.*;

public class ComboPlaceholderRenderer extends DefaultListCellRenderer {

    private final String placeholder;

    public ComboPlaceholderRenderer(String placeholder){
        this.placeholder = placeholder;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus){

        JLabel lbl = (JLabel) super.getListCellRendererComponent(
                list,value,index,isSelected,cellHasFocus);

        // ⭐ Placeholder görünümü
        if(value != null && value.toString().equals(placeholder)){

            lbl.setForeground(Color.GRAY);
            lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
        }

        return lbl;
    }
}
