package com.teklif.pricing;

import com.teklif.model.ParaBirimi;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.*;
import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.concurrent.atomic.*;
import java.util.logging.*;

public class KurService {

    private static final Logger log = Logger.getLogger(KurService.class.getName());
    private static final String TCMB_URL =
        "https://www.tcmb.gov.tr/kurlar/today.xml";

    // Cache — günde bir kez çek
    private static final AtomicReference<double[]> CACHE = new AtomicReference<>(null);
    private static volatile LocalDate cacheDate = null;

    /** [0]=EUR, [1]=USD */
    private static double[] kurlar() {
        LocalDate bugun = LocalDate.now();
        double[] cached = CACHE.get();
        if (cached != null && bugun.equals(cacheDate)) return cached;

        try {
            URLConnection conn = new URL(TCMB_URL).openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);

            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = db.parse(conn.getInputStream());
            NodeList nodes = doc.getElementsByTagName("Currency");

            double eur = 0, usd = 0;
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String kod = el.getAttribute("CurrencyCode");
                String satis = el.getElementsByTagName("ForexSelling").item(0).getTextContent().trim();
                if (satis.isEmpty()) continue;
                double rate = Double.parseDouble(satis.replace(",", "."));
                if ("EUR".equals(kod)) eur = rate;
                if ("USD".equals(kod)) usd = rate;
            }

            if (eur > 0 && usd > 0) {
                double[] fresh = {eur, usd};
                CACHE.set(fresh);
                cacheDate = bugun;
                log.info("TCMB kur güncellendi: EUR=" + eur + " USD=" + usd);
                return fresh;
            }
        } catch (Exception e) {
            log.warning("TCMB kur çekilemedi, sabit kur kullanılıyor: " + e.getMessage());
        }

        // Fallback — sabit kur
        double[] fallback = {42.0, 38.0};
        CACHE.compareAndSet(null, fallback);
        return CACHE.get();
    }

    public static double euroKur()  { return kurlar()[0]; }
    public static double dolarKur() { return kurlar()[1]; }

    public static double cevir(double tl, ParaBirimi pb) {
        return switch (pb) {
            case EUR -> tl / euroKur();
            case USD -> tl / dolarKur();
            default  -> tl;
        };
    }

    /** Güncel kurları döner — API endpoint için */
    public static java.util.Map<String, Object> kurBilgisi() {
        double[] k = kurlar();
        var m = new java.util.LinkedHashMap<String, Object>();
        m.put("EUR", k[0]);
        m.put("USD", k[1]);
        m.put("tarih", LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        return m;
    }
}