package com.teklif.db;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaInitializer {

    public static void init() {
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement()) {

            // Mevcut ham fiyat tablolari
            st.execute("CREATE TABLE IF NOT EXISTS price_table (id INTEGER PRIMARY KEY AUTOINCREMENT, sheet_name TEXT, prefix TEXT, strategy TEXT);");
            st.execute("CREATE TABLE IF NOT EXISTS price_axis (id INTEGER PRIMARY KEY AUTOINCREMENT, table_id INTEGER, axis TEXT, value_num REAL, value_str TEXT);");
            st.execute("CREATE TABLE IF NOT EXISTS price_cell (table_id INTEGER, row_value REAL, col_value REAL, row_value_str TEXT, col_value_str TEXT, price REAL);");
            st.execute("CREATE TABLE IF NOT EXISTS feature_ratio (feature_type TEXT, option_name TEXT, ratio REAL);");

            // Yeni teklif takip tablolari
            st.execute("""
                CREATE TABLE IF NOT EXISTS musteri (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    firma_adi        TEXT NOT NULL,
                    yetkili          TEXT,
                    telefon          TEXT,
                    eposta           TEXT,
                    adres            TEXT,
                    notlar           TEXT,
                    olusturma_tarihi TEXT DEFAULT (datetime('now','localtime'))
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS teklif (
                    id                INTEGER PRIMARY KEY AUTOINCREMENT,
                    teklif_no         TEXT NOT NULL,
                    revize_no         INTEGER NOT NULL DEFAULT 0,
                    is_adi            TEXT NOT NULL,
                    musteri_id        INTEGER REFERENCES musteri(id),
                    teklif_tarihi     TEXT NOT NULL,
                    gecerlilik_tarihi TEXT,
                    teklifi_veren     TEXT,
                    para_birimi       TEXT NOT NULL DEFAULT 'TL',
                    kdv_orani         REAL NOT NULL DEFAULT 20.0,
                    ara_toplam        REAL NOT NULL DEFAULT 0.0,
                    kdv_tutari        REAL NOT NULL DEFAULT 0.0,
                    genel_toplam      REAL NOT NULL DEFAULT 0.0,
                    durum             TEXT NOT NULL DEFAULT 'BEKLIYOR',
                    notlar            TEXT,
                    olusturma_tarihi  TEXT DEFAULT (datetime('now','localtime')),
                    guncelleme_tarihi TEXT DEFAULT (datetime('now','localtime'))
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS teklif_kalem (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    teklif_id   INTEGER NOT NULL REFERENCES teklif(id) ON DELETE CASCADE,
                    sira_no     INTEGER NOT NULL DEFAULT 1,
                    urun_kodu   TEXT,
                    urun_adi    TEXT NOT NULL,
                    adet        INTEGER NOT NULL DEFAULT 1,
                    birim       TEXT DEFAULT 'Adet',
                    birim_fiyat REAL NOT NULL DEFAULT 0.0,
                    iskonto     REAL DEFAULT 0.0,
                    toplam      REAL NOT NULL DEFAULT 0.0,
                    aciklama     TEXT,
                    genislik     TEXT,
                    yukseklik    TEXT,
                    uzunluk      TEXT,
                    cap          TEXT,
                    cerceve_tipi TEXT,
                    damper_tipi  TEXT,
                    ral_kodu     TEXT,
                    montaj       TEXT
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS teklif_no_sequence (
                    yil  INTEGER PRIMARY KEY,
                    son  INTEGER NOT NULL DEFAULT 0
                );""");

            
            // Mevcut DB icin migration (sütun zaten varsa hata vermez)
            String[] alterlar = {
                "ALTER TABLE teklif_kalem ADD COLUMN genislik TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN yukseklik TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN uzunluk TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN cap TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN cerceve_tipi TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN damper_tipi TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN ral_kodu TEXT",
                "ALTER TABLE teklif_kalem ADD COLUMN montaj TEXT"
            };
            for (String alter : alterlar) {
                try { st.execute(alter); } catch (Exception ignored) {}
            }
            System.out.println("Tablolar hazir.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
