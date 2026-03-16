package com.teklif.ui.util;

import javax.swing.*;
import java.awt.*;

public class PlaceholderComboRenderer extends DefaultListCellRenderer {

    private final String placeholder;

    public PlaceholderComboRenderer(String placeholder){
        this.placeholder = placeholder;
    }

    @Override
    public Component getListCellRendererComponent(
            JList<?> list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus){

        super.getListCellRendererComponent(
                list,value,index,isSelected,cellHasFocus);

        // ⭐ sadece seçili alan için ghost göster
        if(index == -1 && value == null){

            setText(placeholder);
            setForeground(Color.GRAY);
        }
        else{
            setForeground(Color.BLACK);
        }

        return this;
    }
}
