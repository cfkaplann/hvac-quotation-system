package com.teklif.ui.table;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.teklif.export.ExcelExporter;
import com.teklif.model.ParaBirimi;
import com.teklif.pricing.KurService;

import java.awt.*;

public class TeklifTablePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private ParaBirimi paraBirimi = ParaBirimi.TL;

    private JLabel lblGenel;
    private JLabel lblKdv;
    private JLabel lblKdvDahil;

    public TeklifTablePanel() {

        setLayout(new BorderLayout());

        String[] kolonlar = {
                "<html>SIRA<br>NO</html>",
                "<html>ÜRÜN KODU</html>",
                "<html>ÜRÜN ADI</html>",
                "<html>GENİŞLİK<br>W(mm)</html>",
                "<html>YÜKSEKLİK<br>H(mm)</html>",
                "<html>UZUNLUK<br>L(mm)</html>",
                "<html>ÇAP<br>Ø(mm)</html>",
                "<html>ÇERÇEVE</html>",
                "<html>DAMPER</html>",
                "<html>RAL</html>",
                "<html>MONTAJ</html>",
                "MİKTAR",
                "BİRİM",
                "BİRİM FİYAT",
                "TOPLAM FİYAT"
        };

        model = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setPreferredSize(new Dimension(0, 60));

        // Fiyat renderer (panelde seçili para birimine göre göster)
        DefaultTableCellRenderer priceRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {

                if (value == null) {
                    setText("");
                    return;
                }

                double v;

                if (value instanceof Number) {
                    v = ((Number) value).doubleValue();
                } else {
                    try {
                        v = Double.parseDouble(value.toString());
                    } catch (Exception ex) {
                        super.setValue(value);
                        return;
                    }
                }

                double shown = KurService.cevir(v, paraBirimi);

                setHorizontalAlignment(SwingConstants.RIGHT);
                setText(String.format("%.2f%s", shown, paraBirimi.getSymbol()));
            }
        };

        table.getColumnModel().getColumn(13).setCellRenderer(priceRenderer);
        table.getColumnModel().getColumn(14).setCellRenderer(priceRenderer);

        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        // Alt panel
        JPanel southPanel = new JPanel(new BorderLayout());

        JPanel totalPanel = new JPanel();
        totalPanel.setLayout(new BoxLayout(totalPanel, BoxLayout.Y_AXIS));
        totalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 20));

        lblGenel = new JLabel("0.00");
        lblKdv = new JLabel("0.00");
        lblKdvDahil = new JLabel("0.00");

        totalPanel.add(createRow(new JLabel("GENEL TOPLAM"), lblGenel));
        totalPanel.add(createRow(new JLabel("KDV %20"), lblKdv));
        totalPanel.add(createRow(new JLabel("KDV DAHİL GENEL"), lblKdvDahil));

        JPanel rightWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightWrapper.add(totalPanel);

        JButton btnExcel = new JButton("Excel'e Aktar");

        btnExcel.addActionListener(e -> {
            try {
                // ✅ kritik: seçili para birimini exporter'a gönder
                ExcelExporter.export(table, paraBirimi);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Excel aktarım hatası: " + ex.getMessage(),
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(btnExcel);

        southPanel.add(rightWrapper, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(southPanel, BorderLayout.SOUTH);
    }

    public void addRow(Object[] data) {

        int sıraNo = model.getRowCount() + 1;

        Object[] row = new Object[data.length + 1];
        row[0] = sıraNo;

        for (int i = 0; i < data.length; i++) {
            row[i + 1] = data[i];
        }

        model.addRow(row);
        hesaplaToplamPanel();
    }

    public void removeSelected() {

        int r = table.getSelectedRow();
        if (r == -1) return;

        model.removeRow(r);
        refreshRowNumbers();
        hesaplaToplamPanel();
    }

    private void refreshRowNumbers() {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0);
        }
    }

    public void setParaBirimi(ParaBirimi pb) {
        this.paraBirimi = (pb == null) ? ParaBirimi.TL : pb;
        model.fireTableDataChanged();
        hesaplaToplamPanel();
    }

    private void hesaplaToplamPanel() {

        double toplam = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object val = model.getValueAt(i, 14);

            if (val instanceof Number) {
                toplam += ((Number) val).doubleValue();
            }
        }

        double kdv = toplam * 0.20;
        double dahil = toplam + kdv;

        lblGenel.setText(String.format("%.2f%s",
                KurService.cevir(toplam, paraBirimi),
                paraBirimi.getSymbol()));

        lblKdv.setText(String.format("%.2f%s",
                KurService.cevir(kdv, paraBirimi),
                paraBirimi.getSymbol()));

        lblKdvDahil.setText(String.format("%.2f%s",
                KurService.cevir(dahil, paraBirimi),
                paraBirimi.getSymbol()));
    }

    private JPanel createRow(JLabel left, JLabel right) {

        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(260, 25));

        left.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        row.add(left, BorderLayout.WEST);
        row.add(right, BorderLayout.EAST);

        return row;
    }

    public JTable getTable() {
        return table;
    }
}