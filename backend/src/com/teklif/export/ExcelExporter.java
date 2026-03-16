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

public class ExcelExporter {

    private static final String TEMPLATE_PATH      = "/teklif_sablon.xlsx";
    private static final String SHEET_NAME         = "Teklif";
    private static final int    FIRST_DATA_ROW     = 25; // 0-tabanlı = Excel satır 26

    // ── Swing tablo kolon sırası → Excel sütun (0-tabanlı POI) ──
    // jCol:  0     1     2     3     4     5     6     7     8     9    10    11    12    13    14
    // Alan:  Sıra  Kod   Ad    W     H     L     Ø     Çrçv  Damp  RAL  Mont  Mikt  Birim BFiy  Top
    private static final int[] COL_MAP = { 1, 2, 3, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };

    // Müşteri / teklif bilgisi hücreleri (0-tabanlı satır, 0-tabanlı sütun)
    // B10(1,9)=FİRMA ADI etiketi → D10(3,9) değer
    // B11(1,10)=YETKİLİ       → D11(3,10)
    // B12(1,11)=PROJE ADI     → D12(3,11)
    // R12(17,11)=TARİH etiket → S12(18,11) değer
    // R13(17,12)=TEKLİF NO    → S13(18,12) değer

    // ── Swing masaüstü (korundu) ─────────────────────────
    public static void export(JTable table, ParaBirimi paraBirimi) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("teklif.xlsx"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        try (InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
             Workbook wb    = new XSSFWorkbook(is);
             FileOutputStream fos = new FileOutputStream(file)) {

            Sheet  sheet  = wb.getSheet(SHEET_NAME);
            TableModel model  = table.getModel();
            int    count  = model.getRowCount();
            if (count == 0) return;
            String symbol = paraBirimi.getSymbol();

            for (int r = 0; r < count; r++) {
                Row row = sheet.getRow(FIRST_DATA_ROW + r);
                if (row == null) continue;
                clearRow(row);

                for (int jCol = 0; jCol < COL_MAP.length; jCol++) {
                    int xCol  = COL_MAP[jCol];
                    Object val = model.getValueAt(r, jCol);
                    if (val == null) continue;

                    Cell cell = getOrCreate(row, xCol);

                    // Sıra no
                    if (jCol == 0) { cell.setCellValue(r + 1.0); continue; }

                    // Ürün adı — HTML temizle
                    if (jCol == 2) { cell.setCellValue(stripHtml(val.toString())); continue; }

                    // Sayısal ölçüler: W, H, L, Ø, Miktar
                    if (jCol == 3 || jCol == 4 || jCol == 5 || jCol == 6 || jCol == 11) {
                        tryNum(cell, val.toString()); continue;
                    }

                    // Fiyatlar: Birim fiyat, Toplam
                    if (jCol == 13 || jCol == 14) {
                        try {
                            double conv = KurService.cevir(Double.parseDouble(val.toString()), paraBirimi);
                            setPriceDbl(cell, conv, symbol, wb);
                        } catch (Exception e) { cell.setCellValue(0); }
                        continue;
                    }

                    // Metin
                    String s = val.toString().trim();
                    if (!s.isEmpty()) cell.setCellValue(s);
                }
            }

            yazFormulller(sheet, wb, count, symbol);
            printAyarla(sheet);
            wb.write(fos);

        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Spring Boot API ───────────────────────────────────
    public static byte[] exportToBytes(Teklif teklif, ParaBirimi pb) throws Exception {

        try (InputStream is  = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
             Workbook    wb  = new XSSFWorkbook(is);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet  sheet  = wb.getSheet(SHEET_NAME);
            String symbol = pb.getSymbol();

            // ── Müşteri / teklif başlık bilgileri ──
            // Firma adı → D10 (0-tabanlı: satır=9, sütun=3)
            // Proje adı → D12 (satır=11, sütun=3)
            // Tarih     → S12 (satır=11, sütun=18)
            // Teklif no → S13 (satır=12, sütun=18)
            if (teklif.getMusteri() != null && teklif.getMusteri().getFirmaAdi() != null)
                setCellStr(sheet, 9, 3, teklif.getMusteri().getFirmaAdi());
            setCellStr(sheet, 11, 3, nvl(teklif.getIsAdi()));
            setCellStr(sheet, 11, 18, nvl(teklif.getTeklifTarihi()));
            setCellStr(sheet, 12, 18, nvl(teklif.getTeklifNo()));
            if (teklif.getTeklifiVeren() != null)
                setCellStr(sheet, 10, 3, teklif.getTeklifiVeren());

            // ── Kalemler ──
            int rowIndex = FIRST_DATA_ROW;
            for (TeklifKalem k : teklif.getKalemler()) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) row = sheet.createRow(rowIndex);
                clearRow(row);

                // jCol 0: Sıra
                getOrCreate(row, COL_MAP[0]).setCellValue(rowIndex - FIRST_DATA_ROW + 1.0);
                // jCol 1: Ürün Kodu
                setStr(row, COL_MAP[1], nvl(k.getUrunKodu()));
                // jCol 2: Ürün Adı (HTML temiz)
                setStr(row, COL_MAP[2], stripHtml(nvl(k.getUrunAdi())));
                // jCol 3-6: Ölçüler
                tryNumRow(row, COL_MAP[3], nvl(k.getGenislik()));
                tryNumRow(row, COL_MAP[4], nvl(k.getYukseklik()));
                tryNumRow(row, COL_MAP[5], nvl(k.getUzunluk()));
                tryNumRow(row, COL_MAP[6], nvl(k.getCap()));
                // jCol 7-10: Özellikler
                setStr(row, COL_MAP[7],  nvl(k.getCerceveTipi()));
                setStr(row, COL_MAP[8],  nvl(k.getDamperTipi()));
                setStr(row, COL_MAP[9],  nvl(k.getRalKodu()));
                setStr(row, COL_MAP[10], nvl(k.getMontaj()));
                // jCol 11-12: Miktar, Birim
                getOrCreate(row, COL_MAP[11]).setCellValue((double) k.getAdet());
                setStr(row, COL_MAP[12], nvl(k.getBirim()));
                // jCol 13-14: Fiyatlar (kura çevrilmiş)
                setPriceDbl(getOrCreate(row, COL_MAP[13]), KurService.cevir(k.getBirimFiyat(), pb), symbol, wb);
                setPriceDbl(getOrCreate(row, COL_MAP[14]), KurService.cevir(k.getToplam(),     pb), symbol, wb);

                rowIndex++;
            }

            yazFormulller(sheet, wb, teklif.getKalemler().size(), symbol);
            printAyarla(sheet);
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    // ── Formüller ─────────────────────────────────────────
    private static void yazFormulller(Sheet sheet, Workbook wb, int count, String sym) {
        // Excel satır numaraları (1-tabanlı)
        int first = FIRST_DATA_ROW + 1;
        int last  = FIRST_DATA_ROW + count;
        // Toplam satırları: Excel 127=POI 126, 128=POI 127, 129=POI 128
        setFormula(sheet, wb, 126, 18, "SUM(S" + first + ":S" + last + ")", sym);
        setFormula(sheet, wb, 127, 18, "S127*0.20", sym);
        setFormula(sheet, wb, 128, 18, "S127+S128", sym);
    }

    // ── Yardımcılar ───────────────────────────────────────
    private static String stripHtml(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)<br\\s*/?>", " ")
                .replaceAll("(?i)<[^>]+>", "")
                .replaceAll("\\s+", " ").trim();
    }

    private static String nvl(String s) { return s == null ? "" : s; }

    private static void setCellStr(Sheet sheet, int row0, int col0, String val) {
        Row row = sheet.getRow(row0);
        if (row == null) row = sheet.createRow(row0);
        Cell cell = getOrCreate(row, col0);
        if (val != null && !val.isBlank()) cell.setCellValue(val);
    }

    private static void setStr(Row row, int col, String val) {
        Cell cell = getOrCreate(row, col);
        if (val != null && !val.isBlank()) cell.setCellValue(val);
        else cell.setBlank();
    }

    private static void tryNum(Cell cell, String val) {
        if (val == null || val.isBlank()) { cell.setBlank(); return; }
        try {
            double d = Double.parseDouble(val.replace(",", "."));
            if (d == 0) cell.setBlank(); else cell.setCellValue(d);
        } catch (Exception e) {
            if (!val.isBlank()) cell.setCellValue(val); else cell.setBlank();
        }
    }

    private static void tryNumRow(Row row, int col, String val) {
        tryNum(getOrCreate(row, col), val);
    }

    private static void setPriceDbl(Cell cell, double val, String sym, Workbook wb) {
        cell.setCellValue(val);
        CellStyle s = wb.createCellStyle();
        s.cloneStyleFrom(cell.getCellStyle());
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"" + sym + "\""));
        cell.setCellStyle(s);
    }

    private static void setFormula(Sheet sheet, Workbook wb, int rowIdx, int col, String formula, String sym) {
        Row row = sheet.getRow(rowIdx);
        if (row == null) return;
        Cell cell = getOrCreate(row, col);
        cell.setCellFormula(formula);
        CellStyle s = wb.createCellStyle();
        s.cloneStyleFrom(cell.getCellStyle());
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"" + sym + "\""));
        cell.setCellStyle(s);
    }

    private static void clearRow(Row row) {
        for (int col : COL_MAP) {
            Cell c = row.getCell(col);
            if (c != null) c.setBlank();
        }
    }

    private static Cell getOrCreate(Row row, int col) {
        Cell c = row.getCell(col);
        return c != null ? c : row.createCell(col);
    }

    private static void printAyarla(Sheet sheet) {
        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(false);
        ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true);
        ps.setFitWidth((short) 1);
        ps.setFitHeight((short) 0);
    }
}
