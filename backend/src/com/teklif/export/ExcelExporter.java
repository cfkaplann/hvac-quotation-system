package com.teklif.export;

import com.teklif.model.ParaBirimi;
import com.teklif.model.takip.Teklif;
import com.teklif.model.takip.TeklifKalem;
import com.teklif.pricing.KurService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.*;
import java.util.*;

public class ExcelExporter {

    private static final String TEMPLATE_PATH  = "/teklif_sablon.xlsx";
    private static final String SHEET_NAME     = "Teklif";
    private static final int    FIRST_DATA_ROW = 25; // 0-indexed → Excel satır 26

    // Kolon indexleri (0-indexed, POI) — YENİ ŞABLON
    private static final int COL_SIRA      = 1;  // B
    private static final int COL_URUN_KOD  = 2;  // C
    private static final int COL_URUN_ADI  = 3;  // D (geniş — ürün adı + ölçüler + özellikler)
    private static final int COL_CERCEVE   = 5;  // F
    private static final int COL_DAMPER    = 6;  // G
    private static final int COL_RAL       = 7;  // H
    private static final int COL_MONTAJ    = 8;  // I
    private static final int COL_MIKTAR    = 9;  // J
    private static final int COL_BIRIM     = 10; // K
    private static final int COL_BIRIM_FYT = 11; // L
    private static final int COL_TOPLAM    = 12; // M

    private static final int[] COL_MAP = {
        COL_SIRA, COL_URUN_KOD, COL_URUN_ADI,
        COL_CERCEVE, COL_DAMPER, COL_RAL, COL_MONTAJ,
        COL_MIKTAR, COL_BIRIM, COL_BIRIM_FYT, COL_TOPLAM
    };

    public static void export(JTable table, ParaBirimi paraBirimi) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("teklif.xlsx"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try (InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
             Workbook workbook = new XSSFWorkbook(is);
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = workbook.getSheet(SHEET_NAME);
            TableModel model = table.getModel();
            int productCount = model.getRowCount();
            if (productCount == 0) return;
            String symbol = paraBirimi.getSymbol();
            for (int r = 0; r < productCount; r++) {
                Row row = sheet.getRow(FIRST_DATA_ROW + r);
                if (row == null) row = sheet.createRow(FIRST_DATA_ROW + r);
                clearMappedCells(row);
                for (int jCol = 0; jCol < COL_MAP.length; jCol++) {
                    int xCol = COL_MAP[jCol];
                    Cell cell = row.getCell(xCol);
                    if (cell == null) cell = row.createCell(xCol);
                    Object value = model.getValueAt(r, jCol);
                    if (value == null) continue;
                    if (jCol == 0) { cell.setCellValue(Double.parseDouble(value.toString())); continue; }
                    if (jCol == 9 || jCol == 10) {
                        try {
                            double converted = KurService.cevir(Double.parseDouble(value.toString()), paraBirimi);
                            cell.setCellValue(converted);
                        } catch (Exception ex) { cell.setCellValue(0); }
                        continue;
                    }
                    String text = value.toString().replaceAll("(?i)<br\\s*/?>", " ").replaceAll("(?i)<[^>]+>", "");
                    cell.setCellValue(text.trim());
                }
            }
            int firstRow = FIRST_DATA_ROW + 1; int lastRow = FIRST_DATA_ROW + productCount;
            setFormula(sheet, workbook, 28 + productCount - 1, COL_TOPLAM, "SUM(M" + firstRow + ":M" + lastRow + ")", symbol);
            setFormula(sheet, workbook, 29 + productCount - 1, COL_TOPLAM, "M" + (29 + productCount) + "*0.20", symbol);
            setFormula(sheet, workbook, 30 + productCount - 1, COL_TOPLAM, "M" + (29 + productCount) + "+M" + (30 + productCount), symbol);
            PrintSetup ps = sheet.getPrintSetup(); ps.setLandscape(false); ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
            sheet.setFitToPage(true); ps.setFitWidth((short) 1); ps.setFitHeight((short) 0);
            workbook.write(fos);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static byte[] exportToBytes(Teklif teklif, ParaBirimi pb) throws Exception {
        return exportToBytes(teklif, pb, new HashMap<>());
    }

    public static byte[] exportToBytes(Teklif teklif, ParaBirimi pb, Map<String,String> ticariKodlar) throws Exception {
        try (InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
             Workbook wb = new XSSFWorkbook(is);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.getSheet(SHEET_NAME);
            String sym = pb.getSymbol();
            int count = teklif.getKalemler().size();

            // Teklif bilgileri
            String musteriAdi = "";
            if (teklif.getMusteri() != null && teklif.getMusteri().getFirmaAdi() != null)
                musteriAdi = teklif.getMusteri().getFirmaAdi();
            else if (teklif.getMusteriAdi() != null && !teklif.getMusteriAdi().isBlank())
                musteriAdi = teklif.getMusteriAdi();
            setCellStr(sheet, 9, 3, musteriAdi);

            if (teklif.getMusteri() != null)
                setCellStr(sheet, 10, 3, nvl(teklif.getMusteri().getYetkili()));

            setCellStr(sheet, 11, 3, nvl(teklif.getIsAdi()));
            setCellStr(sheet, 11, 12, formatTarih(teklif.getTeklifTarihi()));
            setCellStr(sheet, 12, 12, nvl(teklif.getTeklifNo()));

            // Telefon: C13 (row=12, col=2) — B13 etiketi şablonda mevcut
            if (teklif.getMusteri() != null)
                setCellStr(sheet, 12, 2, nvl(teklif.getMusteri().getTelefon()));

            satirlariHazirla(sheet, count);

            int rowIndex = FIRST_DATA_ROW;
            for (TeklifKalem k : teklif.getKalemler()) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) row = sheet.createRow(rowIndex);
                clearMappedCells(row);

                String tamAd = nvl(k.getUrunAdi());
                String ticariKod = ticariKodlar.getOrDefault(nvl(k.getUrunKodu()), "");

                String urunAdExcel = tamAd;
                if (tamAd.contains(" \u2014 ")) {
                    String[] bolum = tamAd.split(" \u2014 ", 2);
                    urunAdExcel = bolum[0].trim();
                    String ozellikStr = bolum[1].trim();
                    List<String> kalanlar = new ArrayList<>();
                    for (String op : ozellikStr.split(" / ")) {
                        String o = op.trim();
                        if (o.isEmpty()) continue;
                        if (o.matches(".*\\d+\\s*mm.*"))
                            setCellStr(row, COL_CERCEVE, o);
                        else if (o.contains("Damper") || o.contains("damper"))
                            setCellStr(row, COL_DAMPER, o);
                        else if (o.contains("Boyal") || o.contains("Boyasız") || o.contains("Eloksal") || o.contains("AISI") || o.contains("Paslanmaz"))
                            setCellStr(row, COL_RAL, o);
                        else if (o.equals("Vidalı")||o.equals("Klipsli")||o.equals("Sustalı")||o.equals("Clip-in")||o.equals("Lay-in")||o.equals("Karolaj")||o.contains("Montaj"))
                            setCellStr(row, COL_MONTAJ, o);
                        else
                            kalanlar.add(o);
                    }
                    if (!kalanlar.isEmpty())
                        urunAdExcel += " \u2014 " + String.join(" / ", kalanlar);
                }

                setNumCell(row, COL_SIRA, (double)(rowIndex - FIRST_DATA_ROW + 1));
                setCellStr(row, COL_URUN_KOD, !ticariKod.isBlank() ? ticariKod : nvl(k.getUrunKodu()));
                setCellStr(row, COL_URUN_ADI, urunAdExcel);
                setNumCell(row, COL_MIKTAR, (double) k.getAdet());
                setCellStr(row, COL_BIRIM, nvl(k.getBirim()));
                setPriceDbl(row, COL_BIRIM_FYT, KurService.cevir(k.getBirimFiyat(), pb), sym, wb);
                setPriceDbl(row, COL_TOPLAM,    KurService.cevir(k.getToplam(),     pb), sym, wb);
                rowIndex++;
            }

            int kaydirma  = count - 1;
            int toplamIdx = 28 + kaydirma;
            int kdvIdx    = 29 + kaydirma;
            int genelIdx  = 30 + kaydirma;

            setPriceDbl(sheet, toplamIdx, COL_TOPLAM, KurService.cevir(teklif.getAraToplam(),   pb), sym, wb);
            setPriceDbl(sheet, kdvIdx,    COL_TOPLAM, KurService.cevir(teklif.getKdvTutari(),   pb), sym, wb);
            setPriceDbl(sheet, genelIdx,  COL_TOPLAM, KurService.cevir(teklif.getGenelToplam(), pb), sym, wb);

            yazGecerlilikTarihi(sheet, count, formatTarih(teklif.getGecerlilikTarihi()));

            wb.write(bos);
            return bos.toByteArray();
        }
    }

    private static void satirlariHazirla(Sheet sheet, int count) {
        if (count <= 1) return;
        int eklenecek = count - 1;
        int kaynakBas = FIRST_DATA_ROW + 1;
        int kaynakSon = sheet.getLastRowNum();

        for (int r = kaynakSon; r >= kaynakBas; r--) {
            kopyraSatir(sheet, r, r + eklenecek);
            Row kaynak = sheet.getRow(r);
            if (kaynak != null) sheet.removeRow(kaynak);
        }

        List<org.apache.poi.ss.util.CellRangeAddress> tasinanlar = new ArrayList<>();
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            org.apache.poi.ss.util.CellRangeAddress mr = sheet.getMergedRegion(i);
            if (mr.getFirstRow() >= kaynakBas) { tasinanlar.add(mr); sheet.removeMergedRegion(i); }
        }
        for (org.apache.poi.ss.util.CellRangeAddress mr : tasinanlar)
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                mr.getFirstRow()+eklenecek, mr.getLastRow()+eklenecek, mr.getFirstColumn(), mr.getLastColumn()));

        List<org.apache.poi.ss.util.CellRangeAddress> sablonMerges = new ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            org.apache.poi.ss.util.CellRangeAddress mr = sheet.getMergedRegion(i);
            if (mr.getFirstRow() == FIRST_DATA_ROW) sablonMerges.add(mr);
        }
        Row sablon = sheet.getRow(FIRST_DATA_ROW);
        for (int i = 1; i < count; i++) {
            int idx = FIRST_DATA_ROW + i;
            Row yeni = sheet.getRow(idx); if (yeni == null) yeni = sheet.createRow(idx);
            if (sablon != null) {
                yeni.setHeight(sablon.getHeight());
                for (int col = 0; col < 14; col++) {
                    Cell src = sablon.getCell(col); Cell dest = yeni.getCell(col);
                    if (dest == null) dest = yeni.createCell(col);
                    if (src != null) dest.setCellStyle(src.getCellStyle());
                }
            }
            for (org.apache.poi.ss.util.CellRangeAddress mr : sablonMerges)
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                    idx, idx, mr.getFirstColumn(), mr.getLastColumn()));
        }
    }

    private static void kopyraSatir(Sheet sheet, int src, int dst) {
        Row kaynak = sheet.getRow(src);
        Row hedef = sheet.getRow(dst); if (hedef != null) sheet.removeRow(hedef);
        hedef = sheet.createRow(dst);
        if (kaynak == null) return;
        hedef.setHeight(kaynak.getHeight());
        for (Cell srcCell : kaynak) {
            Cell dstCell = hedef.createCell(srcCell.getColumnIndex(), srcCell.getCellType());
            dstCell.setCellStyle(srcCell.getCellStyle());
            switch (srcCell.getCellType()) {
                case STRING:  dstCell.setCellValue(srcCell.getStringCellValue()); break;
                case NUMERIC: dstCell.setCellValue(srcCell.getNumericCellValue()); break;
                case BOOLEAN: dstCell.setCellValue(srcCell.getBooleanCellValue()); break;
                case FORMULA: dstCell.setCellFormula(srcCell.getCellFormula()); break;
                default: break;
            }
        }
    }

    private static void yazGecerlilikTarihi(Sheet sheet, int count, String tarih) {
        int idx = 39 + (count - 1);
        Row row = sheet.getRow(idx); if (row == null) row = sheet.createRow(idx);
        Cell cell = row.getCell(3); if (cell == null) cell = row.createCell(3);
        cell.setCellValue("TEKLİFİMİZ "+tarih + " TARİHİNE KADAR GEÇERLİDİR.");
    }

    private static String formatTarih(String t) {
        if (t != null && t.contains("-")) {
            String[] p = t.split("-");
            if (p.length == 3) return p[2] + "." + p[1] + "." + p[0];
        }
        return nvl(t);
    }

    private static void setCellStr(Sheet sheet, int rowIdx, int col, String val) {
        Row row = sheet.getRow(rowIdx); if (row == null) row = sheet.createRow(rowIdx);
        Cell cell = row.getCell(col); if (cell == null) cell = row.createCell(col);
        cell.setCellValue(val == null ? "" : val);
    }

    private static void setCellStr(Row row, int col, String val) {
        Cell cell = row.getCell(col); if (cell == null) cell = row.createCell(col);
        cell.setCellValue(val == null ? "" : val);
    }

    private static void setNumCell(Row row, int col, double val) {
        Cell cell = row.getCell(col); if (cell == null) cell = row.createCell(col);
        cell.setCellValue(val);
    }

    private static void setPriceDbl(Row row, int col, double val, String sym, Workbook wb) {
        Cell cell = row.getCell(col); if (cell == null) cell = row.createCell(col);
        cell.setCellValue(val);
        CellStyle s = wb.createCellStyle(); s.cloneStyleFrom(cell.getCellStyle());
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"" + sym + "\""));
        cell.setCellStyle(s);
    }

    private static void setPriceDbl(Sheet sheet, int rowIdx, int col, double val, String sym, Workbook wb) {
        Row row = sheet.getRow(rowIdx); if (row == null) row = sheet.createRow(rowIdx);
        setPriceDbl(row, col, val, sym, wb);
    }

    private static void setFormula(Sheet sheet, Workbook wb, int rowIdx, int col, String formula, String sym) {
        Row row = sheet.getRow(rowIdx); if (row == null) row = sheet.createRow(rowIdx);
        Cell cell = row.getCell(col); if (cell == null) cell = row.createCell(col);
        cell.setCellFormula(formula);
        CellStyle s = wb.createCellStyle(); s.cloneStyleFrom(cell.getCellStyle());
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"" + sym + "\""));
        cell.setCellStyle(s);
    }

    private static void clearMappedCells(Row row) {
        for (int xCol : COL_MAP) {
            Cell cell = row.getCell(xCol); if (cell != null) cell.setBlank();
        }
    }

    private static String nvl(String s) { return s == null ? "" : s; }
}
