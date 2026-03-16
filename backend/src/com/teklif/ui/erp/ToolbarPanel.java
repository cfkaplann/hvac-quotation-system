package com.teklif.ui.erp;

import javax.swing.*;
import java.awt.*;

import com.teklif.model.ParaBirimi;
import com.teklif.model.UrunKart;
import com.teklif.model.UrunKategori;

public class ToolbarPanel extends JPanel {

    private JComboBox<UrunKategori> cmbKategori;
    private JComboBox<UrunKart> cmbUrun;
    private JComboBox<ParaBirimi> cmbParaBirimi;

    public ToolbarPanel(){

        setLayout(new FlowLayout(FlowLayout.LEFT));

        cmbKategori = new JComboBox<>();
        cmbUrun     = new JComboBox<>();

        // ✅ Placeholder için null ekle
        cmbKategori.addItem(null);
        cmbUrun.addItem(null);

        // ✅ Enum + Object için renderer (null gelirse placeholder yaz)
        cmbKategori.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Kategori Seçiniz" : value.toString());
                return this;
            }
        });

        cmbUrun.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value == null ? "Ürün Seçiniz" : value.toString());
                return this;
            }
        });

        add(new JLabel("Kategori"));
        add(cmbKategori);

        add(new JLabel("Ürün"));
        add(cmbUrun);

        cmbParaBirimi = new JComboBox<>(ParaBirimi.values());
        cmbParaBirimi.setSelectedItem(ParaBirimi.TL);

        add(new JLabel("Para Birimi"));
        add(cmbParaBirimi);
    }

    // ✅ Getter tipleri düzeltildi
    public JComboBox<UrunKategori> getCmbKategori(){ return cmbKategori; }
    public JComboBox<UrunKart> getCmbUrun(){ return cmbUrun; }
    public JComboBox<ParaBirimi> getCmbParaBirimi() { return cmbParaBirimi; }
}