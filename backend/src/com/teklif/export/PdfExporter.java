package com.teklif.export;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.teklif.model.takip.Teklif;
import com.teklif.model.takip.TeklifKalem;

import java.io.ByteArrayOutputStream;

public class PdfExporter {

    // Renkler
    private static final BaseColor BG_HEADER  = new BaseColor(14, 17, 23);
    private static final BaseColor ACCENT     = new BaseColor(245, 158, 11);
    private static final BaseColor ROW_ALT    = new BaseColor(30, 37, 53);
    private static final BaseColor TEXT_MUTED = new BaseColor(100, 116, 139);
    private static final BaseColor WHITE      = BaseColor.WHITE;

    // Fontlar
    private static Font fontTitle()   { return new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD,   ACCENT); }
    private static Font fontSub()     { return new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, TEXT_MUTED); }
    private static Font fontLabel()   { return new Font(Font.FontFamily.HELVETICA, 8,  Font.BOLD,   TEXT_MUTED); }
    private static Font fontValue()   { return new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, WHITE); }
    private static Font fontBold()    { return new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD,   WHITE); }
    private static Font fontSmall()   { return new Font(Font.FontFamily.HELVETICA, 8,  Font.NORMAL, TEXT_MUTED); }
    private static Font fontTotal()   { return new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD,   ACCENT); }
    private static Font fontThHead()  { return new Font(Font.FontFamily.HELVETICA, 8,  Font.BOLD,   TEXT_MUTED); }
    private static Font fontTd()      { return new Font(Font.FontFamily.HELVETICA, 9,  Font.NORMAL, WHITE); }

    public static byte[] exportToBytes(Teklif t) throws Exception {

        Document doc = new Document(PageSize.A4, 36, 36, 40, 40);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        PdfWriter writer = PdfWriter.getInstance(doc, bos);
        doc.open();

        PdfContentByte cb = writer.getDirectContentUnder();
        cb.setColorFill(BG_HEADER);
        cb.rectangle(0, 0, PageSize.A4.getWidth(), PageSize.A4.getHeight());
        cb.fill();

        // ── HEADER ───────────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1f, 1f});
        header.setSpacingAfter(20);

        // Sol: logo / firma
        PdfPCell left = new PdfPCell();
        left.setBorder(0);
        left.setPadding(0);
        Paragraph title = new Paragraph("TEKLİF", fontTitle());
        title.setSpacingAfter(2);
        left.addElement(title);
        left.addElement(new Paragraph("TAKİP SİSTEMİ", fontSub()));
        header.addCell(left);

        // Sağ: teklif no + tarih
        PdfPCell right = new PdfPCell();
        right.setBorder(0); right.setPadding(0);
        right.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph noP = new Paragraph(t.getTeklifNo() + (t.getRevizeNo() > 0 ? "  R" + t.getRevizeNo() : ""), fontBold());
        noP.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(noP);
        Paragraph dateP = new Paragraph(t.getTeklifTarihi(), fontSub());
        dateP.setAlignment(Element.ALIGN_RIGHT);
        right.addElement(dateP);
        header.addCell(right);
        doc.add(header);

        // ── AYIRICI ──────────────────────────────────────────────
        LineSeparator ls = new LineSeparator(1f, 100f, ACCENT, Element.ALIGN_LEFT, 0);
        doc.add(new Chunk(ls));
        doc.add(Chunk.NEWLINE);

        // ── BİLGİ SATIRLARI ──────────────────────────────────────
        PdfPTable info = new PdfPTable(4);
        info.setWidthPercentage(100);
        info.setWidths(new float[]{1f, 1.5f, 1f, 1.5f});
        info.setSpacingBefore(8);
        info.setSpacingAfter(20);

        addInfoCell(info, "İŞ ADI",         t.getIsAdi());
        addInfoCell(info, "MÜŞTERİ",        t.getMusteri() != null ? t.getMusteri().getFirmaAdi() : "—");
        addInfoCell(info, "TEKLİFİ VEREN",  nvl(t.getTeklifiVeren()));
        addInfoCell(info, "GEÇERLİLİK",     nvl(t.getGecerlilikTarihi()));
        addInfoCell(info, "DURUM",           t.getDurum().name());
        addInfoCell(info, "PARA BİRİMİ",    t.getParaBirimi());
        addInfoCell(info, "KDV ORANI",      "%" + (int) t.getKdvOrani());
        addInfoCell(info, "YETKİLİ",        t.getMusteri() != null ? nvl(t.getMusteri().getYetkili()) : "—");
        doc.add(info);

        // ── KALEMLER TABLOSU ────────────────────────────────────
        String[] cols     = {"#", "ÜRÜN ADI", "ADET", "BİRİM", "BİRİM FİYAT", "İSK.%", "TOPLAM"};
        float[]  colWidths = {0.4f, 3.5f, 0.6f, 0.7f, 1.2f, 0.6f, 1.2f};

        PdfPTable table = new PdfPTable(cols.length);
        table.setWidthPercentage(100);
        table.setWidths(colWidths);
        table.setSpacingAfter(16);

        // Başlık satırı
        for (String c : cols) {
            PdfPCell th = new PdfPCell(new Phrase(c, fontThHead()));
            th.setBackgroundColor(new BaseColor(22, 27, 38));
            th.setBorderColor(new BaseColor(42, 51, 71));
            th.setBorderWidth(0.5f);
            th.setPadding(8);
            th.setHorizontalAlignment(Element.ALIGN_LEFT);
            table.addCell(th);
        }

        // Kalem satırları
        boolean alt = false;
        int sira = 1;
        for (TeklifKalem k : t.getKalemler()) {
            BaseColor bg = alt ? ROW_ALT : BG_HEADER;
            addTd(table, String.valueOf(sira++),                                      bg, Element.ALIGN_CENTER);
            addTd(table, k.getUrunAdi(),                                              bg, Element.ALIGN_LEFT);
            addTd(table, String.valueOf(k.getAdet()),                                 bg, Element.ALIGN_CENTER);
            addTd(table, nvl(k.getBirim()),                                           bg, Element.ALIGN_CENTER);
            addTd(table, fmt(k.getBirimFiyat()) + " " + t.getParaBirimi(),            bg, Element.ALIGN_RIGHT);
            addTd(table, k.getIskonto() > 0 ? fmt(k.getIskonto()) + "%" : "—",       bg, Element.ALIGN_CENTER);
            addTd(table, fmt(k.getToplam()) + " " + t.getParaBirimi(),                bg, Element.ALIGN_RIGHT);
            alt = !alt;
        }
        doc.add(table);

        // ── TOPLAMLAR ────────────────────────────────────────────
        PdfPTable totals = new PdfPTable(2);
        totals.setWidthPercentage(40);
        totals.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totals.setWidths(new float[]{1.5f, 1f});

        addTotalRow(totals, "Ara Toplam",                        fmt(t.getAraToplam())   + " " + t.getParaBirimi(), false);
        addTotalRow(totals, "KDV (%" + (int)t.getKdvOrani()+")",fmt(t.getKdvTutari())   + " " + t.getParaBirimi(), false);
        addTotalRow(totals, "GENEL TOPLAM",                      fmt(t.getGenelToplam()) + " " + t.getParaBirimi(), true);
        doc.add(totals);

        // ── NOTLAR ───────────────────────────────────────────────
        if (t.getNotlar() != null && !t.getNotlar().isBlank()) {
            doc.add(Chunk.NEWLINE);
            Paragraph notP = new Paragraph("Notlar: " + t.getNotlar(), fontSmall());
            notP.setSpacingBefore(8);
            doc.add(notP);
        }

        // ── FOOTER ───────────────────────────────────────────────
        doc.add(Chunk.NEWLINE);
        LineSeparator ls2 = new LineSeparator(0.5f, 100f, TEXT_MUTED, Element.ALIGN_LEFT, 0);
        doc.add(new Chunk(ls2));
        Paragraph footer = new Paragraph("Bu teklif " + t.getTeklifTarihi() + " tarihinde düzenlenmiştir.", fontSmall());
        footer.setSpacingBefore(6);
        doc.add(footer);

        doc.close();
        return bos.toByteArray();
    }

    private static void addInfoCell(PdfPTable t, String label, String value) {
        PdfPCell c = new PdfPCell();
        c.setBorder(0); c.setPaddingBottom(10); c.setPaddingRight(12);
        c.addElement(new Paragraph(label, fontLabel()));
        c.addElement(new Paragraph(nvl(value), fontValue()));
        t.addCell(c);
    }

    private static void addTd(PdfPTable t, String val, BaseColor bg, int align) {
        PdfPCell c = new PdfPCell(new Phrase(val, fontTd()));
        c.setBackgroundColor(bg);
        c.setBorderColor(new BaseColor(42, 51, 71));
        c.setBorderWidth(0.5f);
        c.setPadding(7);
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }

    private static void addTotalRow(PdfPTable t, String label, String val, boolean highlight) {
        PdfPCell lc = new PdfPCell(new Phrase(label, highlight ? fontTotal() : fontLabel()));
        PdfPCell vc = new PdfPCell(new Phrase(val,   highlight ? fontTotal() : fontValue()));
        lc.setBorder(highlight ? Rectangle.TOP : 0);
        vc.setBorder(highlight ? Rectangle.TOP : 0);
        if (highlight) { lc.setBorderColor(ACCENT); vc.setBorderColor(ACCENT); }
        lc.setPadding(6); vc.setPadding(6);
        lc.setBackgroundColor(highlight ? new BaseColor(22, 27, 38) : BG_HEADER);
        vc.setBackgroundColor(highlight ? new BaseColor(22, 27, 38) : BG_HEADER);
        vc.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(lc); t.addCell(vc);
    }

    private static String fmt(double v) {
        return String.format("%,.2f", v).replace(',', '.');
    }

    private static String nvl(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}
