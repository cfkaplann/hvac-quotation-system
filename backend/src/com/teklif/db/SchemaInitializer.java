package com.teklif.db;

import java.sql.Connection;
import java.sql.Statement;

public class SchemaInitializer {

    public static void init() {
        try (Connection conn = ConnectionManager.getConnection();
             Statement st = conn.createStatement()) {

            st.execute("CREATE TABLE IF NOT EXISTS price_table (id INTEGER PRIMARY KEY AUTOINCREMENT, sheet_name TEXT, prefix TEXT, strategy TEXT);");
            st.execute("CREATE TABLE IF NOT EXISTS price_axis (id INTEGER PRIMARY KEY AUTOINCREMENT, table_id INTEGER, axis TEXT, value_num REAL, value_str TEXT);");
            st.execute("CREATE TABLE IF NOT EXISTS price_cell (table_id INTEGER, row_value REAL, col_value REAL, row_value_str TEXT, col_value_str TEXT, price REAL);");
            st.execute("CREATE TABLE IF NOT EXISTS feature_ratio (feature_type TEXT, option_name TEXT, ratio REAL, is_sabit INTEGER DEFAULT 0, PRIMARY KEY(feature_type, option_name));");

            try { st.execute("ALTER TABLE feature_ratio ADD COLUMN is_sabit INTEGER DEFAULT 0;"); } catch (Exception ignored) {}
            try { st.execute("ALTER TABLE feature_ratio ADD COLUMN urun_kategori TEXT DEFAULT NULL;"); } catch (Exception ignored) {}

            st.execute("""
                CREATE TABLE IF NOT EXISTS feature_ratio_v2 (
                    feature_type   TEXT NOT NULL,
                    option_name    TEXT NOT NULL,
                    urun_kategori  TEXT,
                    ratio          REAL NOT NULL DEFAULT 0.0,
                    is_sabit       INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(feature_type, option_name, urun_kategori)
                );""");

            try { st.execute("ALTER TABLE feature_ratio_v2 ADD COLUMN urun_kodu TEXT DEFAULT NULL;"); } catch (Exception ignored) {}

            st.execute("""
                CREATE TABLE IF NOT EXISTS feature_ratio_v3 (
                    feature_type   TEXT NOT NULL,
                    option_name    TEXT NOT NULL,
                    urun_kategori  TEXT,
                    urun_kodu      TEXT,
                    ratio          REAL NOT NULL DEFAULT 0.0,
                    is_sabit       INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(feature_type, option_name, urun_kategori, urun_kodu)
                );""");

            try {
                st.execute("""
                    INSERT OR IGNORE INTO feature_ratio_v2(feature_type,option_name,urun_kategori,ratio,is_sabit)
                    SELECT fr.feature_type, fr.option_name, NULL, fr.ratio, fr.is_sabit
                    FROM feature_ratio fr
                    WHERE NOT EXISTS (
                        SELECT 1 FROM feature_ratio_v2 v2
                        WHERE v2.feature_type=fr.feature_type
                          AND v2.option_name=fr.option_name
                          AND v2.urun_kategori IS NULL
                    )
                """);
            } catch (Exception ignored) {}

            try {
                st.execute("""
                    INSERT OR IGNORE INTO feature_ratio_v3(feature_type,option_name,urun_kategori,urun_kodu,ratio,is_sabit)
                    SELECT feature_type,option_name,urun_kategori,NULL,ratio,is_sabit
                    FROM feature_ratio_v2
                """);
            } catch (Exception ignored) {}

            try {
                st.execute("""
                    DELETE FROM feature_ratio_v2
                    WHERE rowid NOT IN (
                        SELECT MIN(rowid)
                        FROM feature_ratio_v2
                        GROUP BY feature_type, option_name, COALESCE(urun_kategori,'__NULL__')
                    )
                """);
            } catch (Exception ignored) {}

            try (java.sql.ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM feature_ratio")) {
                if (rs.next() && rs.getInt(1) == 0) seedFeatureRatios(conn);
            }

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
                    musteri_adi       TEXT,
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
                    id           INTEGER PRIMARY KEY AUTOINCREMENT,
                    teklif_id    INTEGER NOT NULL REFERENCES teklif(id) ON DELETE CASCADE,
                    sira_no      INTEGER NOT NULL DEFAULT 1,
                    urun_kodu    TEXT,
                    urun_adi     TEXT NOT NULL,
                    adet         INTEGER NOT NULL DEFAULT 1,
                    birim        TEXT DEFAULT 'Adet',
                    birim_fiyat  REAL NOT NULL DEFAULT 0.0,
                    iskonto      REAL DEFAULT 0.0,
                    toplam       REAL NOT NULL DEFAULT 0.0,
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

            st.execute("""
                CREATE TABLE IF NOT EXISTS teklif_not (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    teklif_id   INTEGER NOT NULL REFERENCES teklif(id) ON DELETE CASCADE,
                    icerik      TEXT NOT NULL,
                    tarih       TEXT NOT NULL DEFAULT (datetime('now','localtime')),
                    yazan       TEXT
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS kullanici (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    kullanici_adi    TEXT NOT NULL UNIQUE,
                    sifre_hash       TEXT NOT NULL,
                    ad_soyad         TEXT,
                    rol              TEXT NOT NULL DEFAULT 'KULLANICI',
                    aktif            INTEGER NOT NULL DEFAULT 1,
                    olusturma_tarihi TEXT DEFAULT (datetime('now','localtime'))
                );""");

            st.execute("INSERT OR IGNORE INTO kullanici (kullanici_adi, sifre_hash, ad_soyad, rol) VALUES ('admin', 'admin123', 'Sistem Yöneticisi', 'ADMIN')");

            try { st.execute("ALTER TABLE teklif ADD COLUMN musteri_adi TEXT"); } catch (Exception ignored) {}
            try { st.execute("ALTER TABLE urun_tanim ADD COLUMN ticari_kod TEXT DEFAULT NULL"); } catch (Exception ignored) {}
            st.execute("CREATE TABLE IF NOT EXISTS ticari_kod_map (urun_kod TEXT PRIMARY KEY, ticari_kod TEXT NOT NULL);");

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

            st.execute("""
                CREATE TABLE IF NOT EXISTS urun_ozellik_override (
                    urun_kod     TEXT NOT NULL,
                    ozellik_tip  TEXT NOT NULL,
                    secenekler   TEXT NOT NULL DEFAULT '[]',
                    PRIMARY KEY(urun_kod, ozellik_tip)
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS urun_tanim (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    kod              TEXT NOT NULL UNIQUE,
                    ad               TEXT NOT NULL,
                    kategori         TEXT NOT NULL,
                    zorunlu_olcular  TEXT NOT NULL DEFAULT '[]',
                    ozellik_tipleri  TEXT NOT NULL DEFAULT '[]',
                    fiyat_stratejisi TEXT NOT NULL DEFAULT 'WH',
                    ticari_kod       TEXT,
                    aktif            INTEGER NOT NULL DEFAULT 1
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS siparis (
                    id               INTEGER PRIMARY KEY AUTOINCREMENT,
                    teklif_id        INTEGER NOT NULL REFERENCES teklif(id),
                    teklif_no        TEXT NOT NULL,
                    musteri_adi      TEXT,
                    is_adi           TEXT,
                    siparis_tarihi   TEXT DEFAULT (datetime('now','localtime')),
                    uretim_durumu    TEXT NOT NULL DEFAULT 'URETIM_BEKLIYOR',
                    not_lar          TEXT,
                    son_guncelleme   TEXT DEFAULT (datetime('now','localtime')),
                    termin_tarihi    TEXT
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS siparis_gecmis (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    siparis_id  INTEGER NOT NULL REFERENCES siparis(id) ON DELETE CASCADE,
                    durum       TEXT NOT NULL,
                    aciklama    TEXT,
                    tarih       TEXT DEFAULT (datetime('now','localtime')),
                    yapan       TEXT
                );""");

            st.execute("""
                CREATE TABLE IF NOT EXISTS sistem_ayar (
                    anahtar  TEXT PRIMARY KEY,
                    deger    TEXT NOT NULL,
                    aciklama TEXT
                );""");
            st.execute("INSERT OR IGNORE INTO sistem_ayar(anahtar,deger,aciklama) VALUES('yay_fiyati','0','Spot Yaylı için yay fiyatı (TL)')");

            try {
                int yil = java.time.LocalDate.now().getYear();
                java.sql.ResultSet rsSeq = st.executeQuery(
                    "SELECT MAX(CAST(SUBSTR(teklif_no, -4) AS INTEGER)) as maks FROM teklif WHERE teklif_no LIKE 'TKL-" + yil + "-%'");
                int maks = 0;
                if (rsSeq.next()) maks = rsSeq.getInt("maks");
                try (java.sql.PreparedStatement psSeq = conn.prepareStatement(
                        "INSERT INTO teklif_no_sequence(yil,son) VALUES(?,?) ON CONFLICT(yil) DO UPDATE SET son=MAX(son,?)")) {
                    psSeq.setInt(1, yil); psSeq.setInt(2, maks); psSeq.setInt(3, maks);
                    psSeq.executeUpdate();
                }
                System.out.println("Sequence guncellendi: " + maks);
            } catch (Exception e) { System.out.println("Sequence hatasi: " + e.getMessage()); }

            System.out.println("Tablolar hazir.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void seedFeatureRatios(java.sql.Connection conn) throws Exception {
        String sql = "INSERT OR IGNORE INTO feature_ratio(feature_type,option_name,ratio,is_sabit) VALUES(?,?,?,?)";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            Object[][] data = {
                {"CERCEVE_TIPI","32 mm",0.15,0},{"CERCEVE_TIPI","28 mm",0.10,0},
                {"CERCEVE_TIPI","22 mm",0.05,0},{"CERCEVE_TIPI","17 mm",0.00,1},
                {"DAMPER_TIPI","Dampersiz",0.00,1},{"DAMPER_TIPI","Paralel Damperli",0.40,0},
                {"DAMPER_TIPI","Zıt Damperli",0.50,0},
                {"RAL","Boyalı",0.15,0},{"RAL","Eloksal",0.15,0},
                {"RAL","Boyasız",0.00,1},{"RAL","AISI 304 Paslanmaz Çelik",0.00,1},
                {"MONTAJ","Vidalı",0.00,1},{"MONTAJ","Klipsli",0.05,0},
                {"MONTAJ","Köprülü",0.05,0},{"MONTAJ","Clip-in",0.00,1},
                {"MONTAJ","Sustalı",0.05,0},{"MONTAJ","Lay-in",0.00,1},
                {"MONTAJ","Karolaj",0.00,1},{"MONTAJ","Gizli Vidalı",0.00,1},
                {"MONTAJ","Boğazdan Vidalı",0.00,1},{"MONTAJ","Hava Kanalına Montaj",0.00,1},
                {"MONTAJ","Duvar Geçiş Parçası İle Montaj",0.00,1},
                {"MONTAJ","Şaft Duvarı Üzerine Vidalı Montaj",0.00,1},{"MONTAJ","Civatalı",0.00,1},
                {"MONTAJ","Göbekten Vidalı",0.00,1},
                {"AKSESUAR_TIPI","Galvaniz Telli",0.15,0},{"AKSESUAR_TIPI","Contalı",0.02,0},
                {"AKSESUAR_TIPI","Manuel Kollu",0.00,1},{"AKSESUAR_TIPI","Servo Motor 24V",0.00,1},
                {"AKSESUAR_TIPI","Servo Motor 230V",0.00,1},{"AKSESUAR_TIPI","Limit Switch",0.00,1},
                {"AKSESUAR_TIPI","Klapeli",0.00,1},{"AKSESUAR_TIPI","Sigortalı",0.00,1},
                {"AKSESUAR_TIPI","Filtresiz",0.00,1},{"AKSESUAR_TIPI","Poliüretan Filtreli",0.25,0},
                {"AKSESUAR_TIPI","Alüminyum Telli",0.20,0},{"AKSESUAR_TIPI","G2 Elyaf Filtreli",0.15,0},
                {"AKSESUAR_TIPI","Dış İzoleli",0.15,0},{"AKSESUAR_TIPI","İç İzoleli",0.15,0},
                {"MENFEZ_TIPI","17° Açılı",0.00,1},{"MENFEZ_TIPI","30° Açılı",0.05,0},
                {"MENFEZ_TIPI","Damla Kanatlı",0.15,0},{"MENFEZ_TIPI","İnoks Telli",0.00,1},
                {"MENFEZ_TIPI","Yuvarlak Delikli",0.10,0},{"MENFEZ_TIPI","Kare Delikli",0.15,0},
                {"MENFEZ_TIPI","Ağır Tip",0.10,0},{"MENFEZ_TIPI","Bronz Yataklı",0.15,0},
                {"MENFEZ_TIPI","Şaft Tipi",0.05,0},{"MENFEZ_TIPI","Kol Kumandalı",0.02,0},
                {"MENFEZ_TIPI","İkiz Tip",0.30,0},{"MENFEZ_TIPI","Ahşaba Montaj",0.05,0},
                {"MENFEZ_TIPI","Sabit Kanatlı",0.00,1},{"MENFEZ_TIPI","PVC Makaralı",0.00,1},
                {"MENFEZ_TIPI","Dörtgen Kesitli",0.00,1},{"MENFEZ_TIPI","Alçıpana Montaj",0.00,1},
                {"MENFEZ_TIPI","Kanal Tipi",0.00,1},{"MENFEZ_TIPI","Şaft",0.00,1},
                {"MENFEZ_TIPI","Koridor Şaft",0.00,1},{"MENFEZ_TIPI","Tekli Tip",0.00,1},
            };
            for (Object[] row : data) {
                ps.setString(1,(String)row[0]); ps.setString(2,(String)row[1]);
                ps.setDouble(3,(Double)row[2]); ps.setInt(4,(Integer)row[3]);
                ps.executeUpdate();
            }
        }
    }
}
