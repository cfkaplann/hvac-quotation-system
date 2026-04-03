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
import java.util.List;

public class ExcelExporter {

    private static final String TEMPLATE_PATH  = "/teklif_sablon.xlsx";
    private static final String SHEET_NAME     = "Teklif";
    private static final int    FIRST_DATA_ROW = 25;
    private static final int[]  COL_MAP        = { 1, 2, 3, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18 };
    private static final int    COL_TOPLAM     = 18;

    private static final java.util.Map<String,String> KOD_MAP = new java.util.HashMap<>();
    static {
        KOD_MAP.put("DANM_TAVN","Rou RC"); KOD_MAP.put("DANM_GEMI","Rou RS");
        KOD_MAP.put("KANM_DUZKNT","Qua L"); KOD_MAP.put("KANM_EGRKNT","Qua C");
        KOD_MAP.put("MNZ_TEKSIRA","Glea SD"); KOD_MAP.put("MNZ_CIFTSIRA","Glea DD");
        KOD_MAP.put("MNZ_DAITEKSIRA","Glea R-A"); KOD_MAP.put("MNZ_DAICIFTSIRA","Glea R-B");
        KOD_MAP.put("MNZ_KARPET","Glea SEC"); KOD_MAP.put("MNZ_KAPTRA","Glea T");
        KOD_MAP.put("MNZ_LINE","Glea L"); KOD_MAP.put("MNZ_LIFTUT","Glea FF");
        KOD_MAP.put("MNZ_PERF","Glea P"); KOD_MAP.put("MNZ_YERLINE","Glea FL");
        KOD_MAP.put("SLT_TOP","Venty A"); KOD_MAP.put("SLT_DAG","Venty B");
        KOD_MAP.put("SLT_GIZTOP","Venty H"); KOD_MAP.put("SLT_GIZDAG","Venty H");
        KOD_MAP.put("SLT_MAKA","Venty Roller");
        KOD_MAP.put("PNJ_AKSTK","Louvrex ACU"); KOD_MAP.put("PNJ_ALTIKUTU","Louvrex Hexa");
        KOD_MAP.put("PNJ_EGRIKNT","Louvrex C"); KOD_MAP.put("PNJ_GNSKNT","Louvrex W");
        KOD_MAP.put("PNJ_KUMTUT","Louvrex ST"); KOD_MAP.put("PNJ_SBTDARKNT","Louvrex N");
        KOD_MAP.put("PNJ_SRBSTKNT","Louvrex F"); KOD_MAP.put("PNJ_SIVAUST","Louvrex SM");
        KOD_MAP.put("DMP_HAVA","Aero"); KOD_MAP.put("DMP_BLADRA","Reflux");
        KOD_MAP.put("DMP_RELI","Ventra"); KOD_MAP.put("DMP_YAN","Aegis F-R");
        KOD_MAP.put("DMP_YANBEL","Aegis F-EN-R"); KOD_MAP.put("DMP_DUMTAH","Aegis S");
        KOD_MAP.put("DMP_DUMTAHBEL","Aegis S-EN");
        KOD_MAP.put("DAIDMP_HAVA","Aero C"); KOD_MAP.put("DAIDMP_GALKLAYAN","Aegis F-C");
        KOD_MAP.put("DAIDMP_YAN","Aegis F-EN-C"); KOD_MAP.put("DAIDMP_YANITH","Aegis F-EN-C (İTHAL)");
        KOD_MAP.put("DAIDMP_BLADRA","Reflux C");
        KOD_MAP.put("KASWRDIF_SABKNT","Wirl S-FD"); KOD_MAP.put("KASWRDIF_ISTD","Wirl O");
        KOD_MAP.put("KASWRDIF_AYRKNT","Wirl AD"); KOD_MAP.put("KASWRDIF_4YON","Wirl 4W");
        KOD_MAP.put("KASWRDIF_DRUM","Beat");
        KOD_MAP.put("DASWRDIF_TELS","Extend"); KOD_MAP.put("DASWRDIF_KONF","Confy");
        KOD_MAP.put("DASWRDIF_YERDOS","Loca"); KOD_MAP.put("DASWRDIF_DAIPAN","Rou RRC");
        KOD_MAP.put("DASWRDIF_TURBLS","Draft"); KOD_MAP.put("DASWRDIF_JETNOZ","Beta");
        KOD_MAP.put("DASWRDIF_SBTKNT","Wirl R-FD"); KOD_MAP.put("DASWRDIF_HIZISTDKNT","Wirl ROR-D");
        KOD_MAP.put("DASWRDIF_AYRBLKNT","Wirl OR-S");
        KOD_MAP.put("KPK_KONT","Inspector"); KOD_MAP.put("KPK_KONTIZO","Inspector ISO");
        KOD_MAP.put("KPK_KAPETMNZKONT","Inspector EC"); KOD_MAP.put("KPK_LINMNZKONT","Inspector L");
        KOD_MAP.put("BOX_WH",""); KOD_MAP.put("BOX_LS",""); KOD_MAP.put("BOX_STR","");
    }

    public static String gosterimKodPublic(String kod) { return gosterimKod(kod); }
    private static String gosterimKod(String kod) {
        if (kod == null) return "";
        for (java.util.Map.Entry<String,String> e : KOD_MAP.entrySet()) {
            if (kod.startsWith(e.getKey())) return e.getValue();
        }
        return kod;
    }

    // ── Swing masaüstü ───────────────────────────────────
    public static void export(JTable table, ParaBirimi paraBirimi) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("teklif.xlsx"));
        if (chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try (InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
             Workbook wb = new XSSFWorkbook(is);
             FileOutputStream fos = new FileOutputStream(file)) {
            Sheet sheet = wb.getSheet(SHEET_NAME);
            TableModel model = table.getModel();
            int count = model.getRowCount();
            if (count == 0) return;
            String sym = paraBirimi.getSymbol();
            satirlariHazirla(sheet, count);
            for (int r = 0; r < count; r++) {
                int ri = FIRST_DATA_ROW + r;
                Row row = sheet.getRow(ri);
                if (row == null) row = sheet.createRow(ri);
                clearRow(row);
                for (int jCol = 0; jCol < COL_MAP.length; jCol++) {
                    Object val = model.getValueAt(r, jCol);
                    Cell cell = getOrCreate(row, COL_MAP[jCol]);
                    if (jCol == 0)  { cell.setCellValue(r + 1.0); continue; }
                    if (jCol == 2)  { cell.setCellValue(stripHtml(s(val))); continue; }
                    if (jCol == 3 || jCol == 4 || jCol == 5 || jCol == 6 || jCol == 11) {
                        tryNum(cell, s(val)); continue;
                    }
                    if (jCol == 13 || jCol == 14) {
                        try { setPriceDbl(cell, KurService.cevir(Double.parseDouble(s(val)), paraBirimi), sym, wb); }
                        catch (Exception e) { cell.setCellValue(0); }
                        continue;
                    }
                    String str = s(val).trim();
                    if (!str.isEmpty()) cell.setCellValue(str);
                }
            }
            yazFormulller(sheet, wb, count, sym, 20.0);
            printAyarla(sheet);
            wb.write(fos);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Spring Boot API ───────────────────────────────────
    public static byte[] exportToBytes(Teklif teklif, ParaBirimi pb) throws Exception {
        try (InputStream is = ExcelExporter.class.getResourceAsStream(TEMPLATE_PATH);
             Workbook wb   = new XSSFWorkbook(is);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            Sheet sheet = wb.getSheet(SHEET_NAME);
            String sym  = pb.getSymbol();

            // Başlık bilgileri
            if (teklif.getMusteri() != null && teklif.getMusteri().getFirmaAdi() != null)
                setCellStr(sheet, 9,  3, teklif.getMusteri().getFirmaAdi());
            if (teklif.getMusteri() != null && teklif.getMusteri().getYetkili() != null)
                setCellStr(sheet, 10, 3, teklif.getMusteri().getYetkili());
            setCellStr(sheet, 11, 3,  nvl(teklif.getIsAdi()));
            setCellStr(sheet, 11, 18, nvl(teklif.getTeklifTarihi()));
            setCellStr(sheet, 12, 18, nvl(teklif.getTeklifNo()));

            // Satırları hazırla (5. hata: buraya eklendi)
            satirlariHazirla(sheet, teklif.getKalemler().size());

            int rowIndex = FIRST_DATA_ROW;
            for (TeklifKalem k : teklif.getKalemler()) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) row = sheet.createRow(rowIndex);
                clearRow(row);
                getOrCreate(row, COL_MAP[0]).setCellValue(rowIndex - FIRST_DATA_ROW + 1.0);
                setStr(row, COL_MAP[1], gosterimKod(k.getUrunKodu()));
                setStr(row, COL_MAP[2], temizAd(k.getUrunAdi()));
                Olcular olc = cikartOlcular(k);
                tryNum(getOrCreate(row, COL_MAP[3]),  olc.genislik);
                tryNum(getOrCreate(row, COL_MAP[4]),  olc.yukseklik);
                tryNum(getOrCreate(row, COL_MAP[5]),  olc.uzunluk);
                tryNum(getOrCreate(row, COL_MAP[6]),  olc.cap);
                setStr(row, COL_MAP[7],  olc.cerceve);
                setStr(row, COL_MAP[8],  olc.damper);
                setStr(row, COL_MAP[9],  olc.ral);
                setStr(row, COL_MAP[10], olc.montaj);
                getOrCreate(row, COL_MAP[11]).setCellValue((double) k.getAdet());
                setStr(row, COL_MAP[12], nvl(k.getBirim()));
                setPriceDbl(getOrCreate(row, COL_MAP[13]), KurService.cevir(k.getBirimFiyat(), pb), sym, wb);
                setPriceDbl(getOrCreate(row, COL_MAP[14]), KurService.cevir(k.getToplam(),     pb), sym, wb);
                rowIndex++;
            }

            yazFormulller(sheet, wb, teklif.getKalemler().size(), sym, teklif.getKdvOrani());
            printAyarla(sheet);
            wb.write(bos);
            return bos.toByteArray();
        }
    }

    // ── Ölçü/özellik çıkarma ─────────────────────────────
    public static class Olcular {
        String genislik="", yukseklik="", uzunluk="", cap="";
        String cerceve="", damper="", ral="", montaj="";
    }

    public static Olcular cikartOlcularPublic(TeklifKalem k) { return cikartOlcular(k); }
    private static Olcular cikartOlcular(TeklifKalem k) {
        Olcular o = new Olcular();
        if (k.getGenislik()    != null && !k.getGenislik().isBlank())    o.genislik  = k.getGenislik();
        if (k.getYukseklik()   != null && !k.getYukseklik().isBlank())   o.yukseklik = k.getYukseklik();
        if (k.getUzunluk()     != null && !k.getUzunluk().isBlank())     o.uzunluk   = k.getUzunluk();
        if (k.getCap()         != null && !k.getCap().isBlank())         o.cap       = k.getCap();
        if (k.getCerceveTipi() != null && !k.getCerceveTipi().isBlank()) o.cerceve   = k.getCerceveTipi();
        if (k.getDamperTipi()  != null && !k.getDamperTipi().isBlank())  o.damper    = k.getDamperTipi();
        if (k.getRalKodu()     != null && !k.getRalKodu().isBlank())     o.ral       = k.getRalKodu();
        if (k.getMontaj()      != null && !k.getMontaj().isBlank())      o.montaj    = k.getMontaj();

        boolean dbBos = o.genislik.isEmpty() && o.cerceve.isEmpty() && o.ral.isEmpty();
        if (dbBos && k.getUrunAdi() != null && k.getUrunAdi().contains("|")) {
            for (String p : k.getUrunAdi().split("\\|")) {
                String t = p.trim();
                if (t.startsWith("Genişlik:"))       o.genislik  = t.replace("Genişlik:", "").trim();
                else if (t.startsWith("Yükseklik:"))  o.yukseklik = t.replace("Yükseklik:", "").trim();
                else if (t.startsWith("Uzunluk:"))    o.uzunluk   = t.replace("Uzunluk:", "").trim();
                else if (t.matches("\\d+\\s*mm"))     { }
                else if (t.toLowerCase().contains("damper")) o.damper = t;
                else if (t.equalsIgnoreCase("Boyalı") || t.equalsIgnoreCase("Boyasız")
                      || t.equalsIgnoreCase("Eloksal") || t.equalsIgnoreCase("Ham")
                      || t.startsWith("Boyalı"))      o.ral = t;
                else if (t.toLowerCase().contains("vidalı") || t.toLowerCase().contains("klipsli")
                      || t.toLowerCase().contains("flanş") || t.toLowerCase().contains("montaj")) o.montaj = t;
                else if (t.matches("\\d+°.*") || t.toLowerCase().contains("kanatlı")
                      || t.toLowerCase().contains("sıva") || t.toLowerCase().contains("bant")) o.cerceve = t;
            }
        }
        if (dbBos && k.getUrunAdi() != null && k.getUrunAdi().contains("\u2014")) {
            java.util.regex.Matcher mWH = java.util.regex.Pattern.compile("\\((\\d+)x(\\d+)").matcher(k.getUrunAdi());
            if (mWH.find()) { o.genislik = mWH.group(1); o.yukseklik = mWH.group(2); }
            java.util.regex.Matcher mCap = java.util.regex.Pattern.compile("[Øø](\\d+)").matcher(k.getUrunAdi());
            if (mCap.find()) o.cap = mCap.group(1);
            String[] split = k.getUrunAdi().split(" \u2014 ", 2);
            if (split.length > 1) {
                for (String p : split[1].split(" / ")) {
                    String t = p.trim();
                    if (t.toLowerCase().contains("damper"))                o.damper = t;
                    else if (t.toLowerCase().contains("tavan") || t.toLowerCase().contains("zemin")
                          || t.toLowerCase().contains("vidalı"))           o.montaj = t;
                    else if (t.toLowerCase().contains("boyalı") || t.equalsIgnoreCase("ham")
                          || t.equalsIgnoreCase("boyasız") || t.startsWith("Boyalı - ")) o.ral = t;
                    else if (o.cerceve.isEmpty())                           o.cerceve = t;
                }
            }
        }
        return o;
    }

    // ── Satır ekleme ─────────────────────────────────────
    private static void satirlariHazirla(Sheet sheet, int count) {
        if (count <= 1) return;
        int eklenecek = count - 1;
        int kaynakBas = FIRST_DATA_ROW + 1;
        int kaynakSon = sheet.getLastRowNum();

        // ADIM 1: Tüm satırları aşağı kopyala
        for (int r = kaynakSon; r >= kaynakBas; r--) {
            kopyraSatir(sheet, r, r + eklenecek);
            Row kaynak = sheet.getRow(r);
            if (kaynak != null) sheet.removeRow(kaynak);
        }

        // ADIM 2: Merged cell'leri aşağı taşı (satır 27+ olanlar)
        List<org.apache.poi.ss.util.CellRangeAddress> tasinanlar = new java.util.ArrayList<>();
        for (int i = sheet.getNumMergedRegions() - 1; i >= 0; i--) {
            org.apache.poi.ss.util.CellRangeAddress mr = sheet.getMergedRegion(i);
            if (mr.getFirstRow() >= kaynakBas) {
                tasinanlar.add(mr);
                sheet.removeMergedRegion(i);
            }
        }
        for (org.apache.poi.ss.util.CellRangeAddress mr : tasinanlar) {
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                mr.getFirstRow() + eklenecek, mr.getLastRow() + eklenecek,
                mr.getFirstColumn(), mr.getLastColumn()));
        }

        // ADIM 3: Şablon satırının (satır 26) merge listesini kaydet
        List<org.apache.poi.ss.util.CellRangeAddress> sablonMerges = new java.util.ArrayList<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            org.apache.poi.ss.util.CellRangeAddress mr = sheet.getMergedRegion(i);
            if (mr.getFirstRow() == FIRST_DATA_ROW) {
                sablonMerges.add(mr);
            }
        }

        // ADIM 4: Veri satırlarının stilini kopyala + merge ekle
        Row sablon = sheet.getRow(FIRST_DATA_ROW);
        for (int i = 1; i < count; i++) {
            int yeniRowIdx = FIRST_DATA_ROW + i;
            Row yeni = sheet.getRow(yeniRowIdx);
            if (yeni == null) yeni = sheet.createRow(yeniRowIdx);
            if (sablon != null) {
                yeni.setHeight(sablon.getHeight());
                for (int col = 0; col < 20; col++) {
                    Cell src  = sablon.getCell(col);
                    Cell dest = yeni.getCell(col);
                    if (dest == null) dest = yeni.createCell(col);
                    if (src != null) dest.setCellStyle(src.getCellStyle());
                }
            }
            // Şablon merge'lerini bu satıra ekle
            for (org.apache.poi.ss.util.CellRangeAddress mr : sablonMerges) {
                sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                    yeniRowIdx, yeniRowIdx, mr.getFirstColumn(), mr.getLastColumn()));
            }
        }
    }

    private static void kopyraSatir(Sheet sheet, int kaynakIdx, int hedefIdx) {
        Row kaynak = sheet.getRow(kaynakIdx);
        Row hedef  = sheet.getRow(hedefIdx);
        if (hedef != null) sheet.removeRow(hedef);
        hedef = sheet.createRow(hedefIdx);
        if (kaynak == null) return;
        hedef.setHeight(kaynak.getHeight());
        for (Cell srcCell : kaynak) {
            Cell destCell = hedef.createCell(srcCell.getColumnIndex(), srcCell.getCellType());
            destCell.setCellStyle(srcCell.getCellStyle());
            switch (srcCell.getCellType()) {
                case STRING:  destCell.setCellValue(srcCell.getStringCellValue()); break;
                case NUMERIC: destCell.setCellValue(srcCell.getNumericCellValue()); break;
                case BOOLEAN: destCell.setCellValue(srcCell.getBooleanCellValue()); break;
                case FORMULA: destCell.setCellFormula(srcCell.getCellFormula()); break;
                default: break;
            }
        }
    }

    // ── Formüller ────────────────────────────────────────
    private static void yazFormulller(Sheet sheet, Workbook wb, int count, String sym, double kdvOrani) {
        int first     = FIRST_DATA_ROW + 1;
        int lastVeri  = FIRST_DATA_ROW + count;
        int kaydirma  = count - 1;
        int toplamIdx = 28 + kaydirma;
        int kdvIdx    = 29 + kaydirma;
        int genelIdx  = 30 + kaydirma;
        int toplamRow = toplamIdx + 1;
        int kdvRow    = kdvIdx + 1;
        setFormula(sheet, wb, toplamIdx, COL_TOPLAM, "SUM(S" + first + ":S" + lastVeri + ")", sym);
        setFormula(sheet, wb, kdvIdx,    COL_TOPLAM, "S" + toplamRow + "*" + String.valueOf(kdvOrani / 100.0).replace(",", "."), sym);
        setFormula(sheet, wb, genelIdx,  COL_TOPLAM, "S" + toplamRow + "+S" + kdvRow, sym);
    }

    // ── Yardımcı metodlar ────────────────────────────────
    private static String nvl(String s)   { return s == null ? "" : s; }
    private static String s(Object o)     { return o == null ? "" : o.toString(); }

    private static String stripHtml(String s) {
        if (s == null) return "";
        return s.replaceAll("(?i)<br\\s*/?>", " ").replaceAll("(?i)<[^>]+>", "")
                .replaceAll("\\s+", " ").trim();
    }

    public static String temizAdPublic(String s) { return temizAd(s); }
    private static String temizAd(String s) {
        if (s == null) return "";
        String t = stripHtml(s);
        if (t.contains("|"))   t = t.substring(0, t.indexOf("|")).trim();
        if (t.contains(" \u2014 ")) t = t.substring(0, t.indexOf(" \u2014 ")).trim();
        // Parantez içi: kasa/boğaz varsa koru, yoksa kaldır
        int pp = t.indexOf(" (");
        if (pp >= 0) {
            int kp = t.indexOf(")", pp);
            if (kp > pp) {
                String ic = t.substring(pp + 2, kp);
                boolean kasaBogaz = ic.contains("/") || ic.contains("x") || ic.contains("X");
                if (!kasaBogaz) t = t.substring(0, pp).trim();
            }
        }
        // Sondaki WxH eki kaldır
        int son = t.length() - 1;
        while (son > 0 && Character.isDigit(t.charAt(son))) son--;
        if (son > 0 && (t.charAt(son) == 'x' || t.charAt(son) == 'X')) {
            int xPos = son;
            while (xPos > 0 && Character.isDigit(t.charAt(xPos - 1))) xPos--;
            if (xPos > 0 && t.charAt(xPos - 1) == ' ') t = t.substring(0, xPos - 1).trim();
        }
        // Ø eki kaldır
        if (t.length() > 1) {
            int capPos = -1;
            for (int i = t.length() - 1; i >= 0; i--) {
                char ch = t.charAt(i);
                if (Character.isDigit(ch)) continue;
                if (ch == '\u00D8' || ch == '\u00F8' || ch == 'O') { capPos = i; break; }
                break;
            }
            if (capPos > 0 && t.charAt(capPos - 1) == ' ') t = t.substring(0, capPos - 1).trim();
        }
        return t;
    }

    private static void setCellStr(Sheet sheet, int r0, int c0, String val) {
        Row row = sheet.getRow(r0);
        if (row == null) row = sheet.createRow(r0);
        Cell cell = getOrCreate(row, c0);
        if (val != null && !val.isBlank()) cell.setCellValue(val);
    }

    private static void setStr(Row row, int col, String val) {
        Cell cell = getOrCreate(row, col);
        if (val != null && !val.isBlank()) cell.setCellValue(val); else cell.setBlank();
    }

    private static void tryNum(Cell cell, String val) {
        if (val == null || val.isBlank()) { cell.setBlank(); return; }
        try {
            double d = Double.parseDouble(val.replace(",", "."));
            if (d == 0) cell.setBlank(); else cell.setCellValue(d);
        } catch (Exception e) { cell.setCellValue(val); }
    }

    private static void setPriceDbl(Cell cell, double val, String sym, Workbook wb) {
        cell.setCellValue(val);
        CellStyle s = wb.createCellStyle();
        s.cloneStyleFrom(cell.getCellStyle());
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"" + sym + "\""));
        cell.setCellStyle(s);
    }

    private static void setFormula(Sheet sheet, Workbook wb, int ri, int col, String formula, String sym) {
        Row row = sheet.getRow(ri);
        if (row == null) row = sheet.createRow(ri);
        Cell cell = getOrCreate(row, col);
        cell.setCellFormula(formula);
        CellStyle s = wb.createCellStyle();
        s.cloneStyleFrom(cell.getCellStyle());
        s.setDataFormat(wb.createDataFormat().getFormat("#,##0.00 \"" + sym + "\""));
        cell.setCellStyle(s);
    }

    private static void clearRow(Row row) {
        for (int col : COL_MAP) { Cell c = row.getCell(col); if (c != null) c.setBlank(); }
    }

    private static Cell getOrCreate(Row row, int col) {
        Cell c = row.getCell(col); return c != null ? c : row.createCell(col);
    }

    private static void printAyarla(Sheet sheet) {
        PrintSetup ps = sheet.getPrintSetup();
        ps.setLandscape(false); ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
        sheet.setFitToPage(true); ps.setFitWidth((short)1); ps.setFitHeight((short)0);
    }
}
