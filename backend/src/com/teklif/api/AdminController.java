package com.teklif.api;

import com.teklif.db.ConnectionManager;
import com.teklif.importer.MasterExcelImporter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/urunler")
    public ResponseEntity<?> urunler(@RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!ok(token)) return no();
        try (Connection c = ConnectionManager.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT id,sheet_name,strategy FROM price_table ORDER BY sheet_name")) {
            List<Map<String,Object>> liste = new ArrayList<>();
            while (rs.next()) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("id",        rs.getInt("id"));
                m.put("sheetName", rs.getString("sheet_name"));
                m.put("strategy",  rs.getString("strategy"));
                liste.add(m);
            }
            return ResponseEntity.ok(liste);
        } catch (Exception e) { return err(e); }
    }

    @GetMapping("/fiyatlar/{tableId}")
    public ResponseEntity<?> fiyatlar(@PathVariable int tableId,
            @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!ok(token)) return no();
        try (Connection c = ConnectionManager.getConnection()) {

            String strategy;
            try (PreparedStatement ps = c.prepareStatement("SELECT strategy FROM price_table WHERE id=?")) {
                ps.setInt(1, tableId);
                try (ResultSet rs = ps.executeQuery()) { strategy = rs.next() ? rs.getString(1) : ""; }
            }

            // ROW değerleri (sayısal veya string)
            List<Object> rows = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT DISTINCT value_num, value_str FROM price_axis WHERE table_id=? AND axis='ROW' ORDER BY value_num, value_str")) {
                ps.setInt(1, tableId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double num = rs.getDouble("value_num");
                        String str = rs.getString("value_str");
                        rows.add(str != null ? str : num);
                    }
                }
            }

            // COL değerleri (sayısal veya string)
            List<Object> cols = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT DISTINCT value_num, value_str FROM price_axis WHERE table_id=? AND axis='COL' ORDER BY value_num, value_str")) {
                ps.setInt(1, tableId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double num = rs.getDouble("value_num");
                        String str = rs.getString("value_str");
                        cols.add(str != null ? str : num);
                    }
                }
            }

            // Hücreler: "rowKey|colKey" → fiyat
            Map<String,Double> hücreler = new LinkedHashMap<>();
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT row_value, col_value, row_value_str, col_value_str, price FROM price_cell WHERE table_id=? GROUP BY row_value, col_value, row_value_str, col_value_str")) {
                ps.setInt(1, tableId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String rk = rs.getString("row_value_str") != null
                                ? rs.getString("row_value_str")
                                : String.valueOf(rs.getDouble("row_value"));
                        String ck = rs.getString("col_value_str") != null
                                ? rs.getString("col_value_str")
                                : String.valueOf(rs.getDouble("col_value"));
                        // double 100.0 → "100.0" normalize
                        if (rk.endsWith(".0")) rk = rk.substring(0, rk.length()-2);
                        if (ck.endsWith(".0")) ck = ck.substring(0, ck.length()-2);
                        hücreler.put(rk + "|" + ck, rs.getDouble("price"));
                    }
                }
            }

            // row/col değerlerini de normalize et
            rows.replaceAll(v -> {
                String s = v.toString();
                return s.endsWith(".0") ? s.substring(0, s.length()-2) : v;
            });
            cols.replaceAll(v -> {
                String s = v.toString();
                return s.endsWith(".0") ? s.substring(0, s.length()-2) : v;
            });

            Map<String,Object> result = new LinkedHashMap<>();
            result.put("rows",     rows);
            result.put("cols",     cols);
            result.put("hücreler", hücreler);
            result.put("strategy", strategy);
            return ResponseEntity.ok(result);
        } catch (Exception e) { return err(e); }
    }

    @PutMapping("/fiyatlar/{tableId}/hucre")
    public ResponseEntity<?> hucreGuncelle(@PathVariable int tableId,
            @RequestBody Map<String,Object> body,
            @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!ok(token)) return no();
        try (Connection c = ConnectionManager.getConnection()) {
            double fiyat  = Double.parseDouble(body.get("fiyat").toString());
            String rowVal = body.get("rowVal").toString();
            String colVal = body.get("colVal").toString();

            // Sayısal mı string mi?
            boolean rowIsNum = isNum(rowVal);
            boolean colIsNum = isNum(colVal);

            PreparedStatement ps;
            if (!rowIsNum || !colIsNum) {
                // String key
                ps = c.prepareStatement(
                    "UPDATE price_cell SET price=? WHERE table_id=? AND row_value_str=? AND col_value_str=?");
                ps.setDouble(1, fiyat); ps.setInt(2, tableId);
                ps.setString(3, rowIsNum ? null : rowVal);
                ps.setString(4, colIsNum ? null : colVal);
            } else {
                ps = c.prepareStatement(
                    "UPDATE price_cell SET price=? WHERE table_id=? AND row_value=? AND col_value=?");
                ps.setDouble(1, fiyat); ps.setInt(2, tableId);
                ps.setDouble(3, Double.parseDouble(rowVal));
                ps.setDouble(4, Double.parseDouble(colVal));
            }
            int updated = ps.executeUpdate(); ps.close();
            return ResponseEntity.ok(Map.of("guncellendi", updated));
        } catch (Exception e) { return err(e); }
    }

    @PostMapping("/fiyatlar/{tableId}/toplu-guncelle")
    public ResponseEntity<?> topluGuncelle(@PathVariable int tableId,
            @RequestBody Map<String,Object> body,
            @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!ok(token)) return no();
        try (Connection c = ConnectionManager.getConnection()) {
            double oran = Double.parseDouble(body.get("oran").toString());
            PreparedStatement ps = c.prepareStatement(
                "UPDATE price_cell SET price = ROUND(price * (1 + ? / 100.0), 2) WHERE table_id=?");
            ps.setDouble(1, oran); ps.setInt(2, tableId);
            int updated = ps.executeUpdate(); ps.close();
            return ResponseEntity.ok(Map.of("guncellendi", updated));
        } catch (Exception e) { return err(e); }
    }

    // ── Excel'den sıfırla (tüm fiyatları sil ve Excel'den yeniden yükle) ──
    // ── Özellik Oranları ─────────────────────────────────
    @GetMapping("/ozellik-oranlari")
    public ResponseEntity<?> ozellikOranlariGetir(
            @RequestHeader(value="X-Token", defaultValue="") String token) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery(
                 "SELECT feature_type, option_name, ratio, is_sabit FROM feature_ratio_v2 WHERE urun_kategori IS NULL ORDER BY feature_type, option_name")) {
            java.util.List<Map<String,Object>> list = new java.util.ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new java.util.LinkedHashMap<>();
                row.put("featureType", rs.getString("feature_type"));
                row.put("optionName", rs.getString("option_name"));
                row.put("ratio", rs.getDouble("ratio"));
                row.put("isSabit", rs.getInt("is_sabit") == 1);
                list.add(row);
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping("/ozellik-oranlari")
    public ResponseEntity<?> ozellikOraniEkle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR REPLACE INTO feature_ratio_v2(feature_type,option_name,urun_kategori,ratio,is_sabit) VALUES(?,?,NULL,?,?)")) {
            ps.setString(1, (String) body.get("featureType"));
            ps.setString(2, (String) body.get("optionName"));
            ps.setDouble(3, Double.parseDouble(body.get("ratio").toString()));
            ps.setInt(4, Boolean.TRUE.equals(body.get("isSabit")) ? 1 : 0);
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/ozellik-oranlari")
    public ResponseEntity<?> ozellikOraniSil(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM feature_ratio_v2 WHERE feature_type=? AND option_name=? AND urun_kategori IS NULL")) {
            ps.setString(1, (String) body.get("featureType"));
            ps.setString(2, (String) body.get("optionName"));
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PutMapping("/ozellik-oranlari")
    public ResponseEntity<?> ozellikOraniGuncelle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "UPDATE feature_ratio_v2 SET ratio=?, is_sabit=? WHERE feature_type=? AND option_name=? AND urun_kategori IS NULL")) {
            ps.setDouble(1, Double.parseDouble(body.get("ratio").toString()));
            ps.setInt(2, Boolean.TRUE.equals(body.get("isSabit")) ? 1 : 0);
            ps.setString(3, (String) body.get("featureType"));
            ps.setString(4, (String) body.get("optionName"));
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate(); // cache'i temizle
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Ürün Özellik Yönetimi ────────────────────────────
    @GetMapping("/urun-ozellikler/{urunKod}")
    public ResponseEntity<?> urunOzelliklerGetir(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String urunKod) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try {
            // Önce ürünün tüm özellik tiplerini al
            com.teklif.model.UrunKart urun = com.teklif.repository.UrunKataloguDeposu.bul(urunKod);
            if (urun == null) return ResponseEntity.notFound().build();

            // Override'ları al
            java.util.Map<String,java.util.List<String>> overrides = new java.util.LinkedHashMap<>();
            try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(
                     "SELECT ozellik_tip, secenekler FROM urun_ozellik_override WHERE urun_kod=?")) {
                ps.setString(1, urunKod);
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        overrides.put(rs.getString("ozellik_tip"),
                            parseJsonArray(rs.getString("secenekler")));
                    }
                }
            }

            // Config'deki özellik tipleri + override'da ekstra eklenmiş tipler
            java.util.Set<String> tipSeti = new java.util.LinkedHashSet<>();
            if (urun.getOzellikler() != null) {
                urun.getOzellikler().forEach(t -> tipSeti.add(t.name()));
            }
            // Override'da config'de olmayan ekstra tipler de olabilir
            overrides.keySet().forEach(tipSeti::add);

            java.util.List<Map<String,Object>> result = new java.util.ArrayList<>();
            for (String tipName : tipSeti) {
                com.teklif.model.OzellikTipi tip = null;
                try { tip = com.teklif.model.OzellikTipi.valueOf(tipName); } catch (Exception ignored) {}

                Map<String,Object> row = new java.util.LinkedHashMap<>();
                row.put("ozellikTip", tipName);

                // Varsayılan: config'den al
                java.util.List<String> varsayilan = (tip != null && urun.getIzinliSecimler() != null)
                    ? new java.util.ArrayList<>(urun.getIzinliSecimler().getOrDefault(tip, java.util.List.of()))
                    : new java.util.ArrayList<>();

                java.util.List<String> aktif = overrides.containsKey(tipName)
                    ? overrides.get(tipName) : varsayilan;

                row.put("secenekler", aktif);
                row.put("varsayilan", varsayilan);
                row.put("overrideVar", overrides.containsKey(tipName));
                row.put("ekstra", tip == null || (urun.getOzellikler() == null || !urun.getOzellikler().contains(tip)));
                result.add(row);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PutMapping("/urun-ozellikler/{urunKod}")
    public ResponseEntity<?> urunOzelliklerGuncelle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String urunKod,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR REPLACE INTO urun_ozellik_override(urun_kod,ozellik_tip,secenekler) VALUES(?,?,?)")) {
            String ozellikTip = (String) body.get("ozellikTip");
            @SuppressWarnings("unchecked")
            java.util.List<String> secenekler = (java.util.List<String>) body.get("secenekler");
            ps.setString(1, urunKod);
            ps.setString(2, ozellikTip);
            ps.setString(3, toJsonArray(secenekler));
            ps.executeUpdate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/urun-ozellikler/{urunKod}/{ozellikTip}")
    public ResponseEntity<?> urunOzellikOverrideSil(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String urunKod, @PathVariable String ozellikTip) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM urun_ozellik_override WHERE urun_kod=? AND ozellik_tip=?")) {
            ps.setString(1, urunKod); ps.setString(2, ozellikTip);
            ps.executeUpdate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    private java.util.List<String> parseJsonArray(String json) {
        java.util.List<String> list = new java.util.ArrayList<>();
        if (json == null || json.trim().equals("[]")) return list;
        String inner = json.trim().replaceAll("^\\[|\\]$", "");
        for (String part : inner.split(",")) {
            String s = part.trim().replaceAll("^\"|\"$", "");
            if (!s.isEmpty()) list.add(s);
        }
        return list;
    }

    private String toJsonArray(java.util.List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── Ürün Yönetimi ─────────────────────────────────────
    @GetMapping("/urun-tanim")
    public ResponseEntity<?> urunTanimListesi(
            @RequestHeader(value="X-Token", defaultValue="") String token) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        return ResponseEntity.ok(com.teklif.repository.DbUrunService.dbUrunListesi());
    }

    @PostMapping("/urun-tanim")
    public ResponseEntity<?> urunTanimEkle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try {
            String kod       = (String) body.get("kod");
            String ad        = (String) body.get("ad");
            String kategori  = (String) body.get("kategori");
            String strateji  = (String) body.getOrDefault("fiyatStratejisi", "WH");
            @SuppressWarnings("unchecked")
            java.util.List<String> olcular  = (java.util.List<String>) body.getOrDefault("zorunluOlcular", java.util.List.of());
            @SuppressWarnings("unchecked")
            java.util.List<String> ozellikler = (java.util.List<String>) body.getOrDefault("ozellikTipleri", java.util.List.of());

            if (kod == null || kod.isBlank()) return ResponseEntity.badRequest().body(Map.of("hata","Kod zorunludur"));
            if (ad  == null || ad.isBlank())  return ResponseEntity.badRequest().body(Map.of("hata","Ad zorunludur"));

            com.teklif.repository.DbUrunService.ekle(kod, ad, kategori, olcular, ozellikler, strateji);
            com.teklif.repository.UrunKataloguDeposu.invalidate();

            // price_table kaydı oluştur (fiyat matrisi için)
            int tableId = -1;
            try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection()) {
                // Strateji → DB strategy string
                String dbStrateji = switch(strateji) {
                    case "CAP"    -> "DIAMETER";
                    case "L"      -> "MATRIX_L_SLOT";
                    case "WH_STR" -> "STRING_SIZE";
                    default       -> "MATRIX_WH";
                };
                // prefix = ilk ürün kodu prefix (örn: MNZ_YENI → MNZ_YENI)
                try (java.sql.PreparedStatement ps2 = conn.prepareStatement(
                        "INSERT OR IGNORE INTO price_table(sheet_name,prefix,strategy) VALUES(?,?,?)",
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    ps2.setString(1, kod);
                    ps2.setString(2, kod);
                    ps2.setString(3, dbStrateji);
                    ps2.executeUpdate();
                    try (java.sql.ResultSet gk = ps2.getGeneratedKeys()) {
                        if (gk.next()) tableId = gk.getInt(1);
                    }
                }
                // Zaten varsa id'yi bul
                if (tableId == -1) {
                    try (java.sql.PreparedStatement ps3 = conn.prepareStatement(
                            "SELECT id FROM price_table WHERE sheet_name=?")) {
                        ps3.setString(1, kod);
                        try (java.sql.ResultSet rs2 = ps3.executeQuery()) {
                            if (rs2.next()) tableId = rs2.getInt(1);
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("ok", true, "tableId", tableId));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/urun-tanim/{kod}")
    public ResponseEntity<?> urunTanimSil(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String kod) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try {
            com.teklif.repository.DbUrunService.sil(kod);
            com.teklif.repository.UrunKataloguDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Yeni ürün fiyat matrisi oluştur ─────────────────
    @PostMapping("/fiyatlar/{tableId}/matris-olustur")
    public ResponseEntity<?> matrisOlustur(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable int tableId,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection()) {
            // Önce eski axis/cell kayıtlarını temizle
            try (java.sql.PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM price_axis WHERE table_id=?")) {
                del.setInt(1, tableId); del.executeUpdate();
            }
            try (java.sql.PreparedStatement del = conn.prepareStatement(
                    "DELETE FROM price_cell WHERE table_id=?")) {
                del.setInt(1, tableId); del.executeUpdate();
            }

            // Satır değerleri (ROW axis = W veya çap veya uzunluk)
            @SuppressWarnings("unchecked")
            java.util.List<Object> rowVals = (java.util.List<Object>) body.get("rowValues");
            // Sütun değerleri (COL axis = H)
            @SuppressWarnings("unchecked")
            java.util.List<Object> colVals = (java.util.List<Object>) body.getOrDefault("colValues", java.util.List.of());
            // Fiyatlar: [[f00,f01,...],[f10,f11,...]] (rowxcol)
            @SuppressWarnings("unchecked")
            java.util.List<java.util.List<Object>> fiyatlar = (java.util.List<java.util.List<Object>>) body.get("fiyatlar");
            boolean stringAxis = Boolean.TRUE.equals(body.get("stringAxis"));

            // ROW axis ekle
            try (java.sql.PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO price_axis(table_id,axis,value_num,value_str) VALUES(?,?,?,?)")) {
                for (Object rv : rowVals) {
                    ps.setInt(1, tableId); ps.setString(2, "ROW");
                    if (stringAxis) { ps.setNull(3, java.sql.Types.REAL); ps.setString(4, rv.toString()); }
                    else { ps.setDouble(3, Double.parseDouble(rv.toString())); ps.setNull(4, java.sql.Types.VARCHAR); }
                    ps.executeUpdate();
                }
            }

            // COL axis ekle (WH matris için)
            if (!colVals.isEmpty()) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO price_axis(table_id,axis,value_num,value_str) VALUES(?,?,?,?)")) {
                    for (Object cv : colVals) {
                        ps.setInt(1, tableId); ps.setString(2, "COL");
                        if (stringAxis) { ps.setNull(3, java.sql.Types.REAL); ps.setString(4, cv.toString()); }
                        else { ps.setDouble(3, Double.parseDouble(cv.toString())); ps.setNull(4, java.sql.Types.VARCHAR); }
                        ps.executeUpdate();
                    }
                }
            }

            // price_cell ekle
            if (fiyatlar != null) {
                try (java.sql.PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO price_cell(table_id,row_value,col_value,row_value_str,col_value_str,price) VALUES(?,?,?,?,?,?)")) {
                    for (int ri = 0; ri < rowVals.size(); ri++) {
                        java.util.List<Object> satir = fiyatlar.size() > ri ? fiyatlar.get(ri) : java.util.List.of();
                        if (colVals.isEmpty()) {
                            // Tek eksenli (çap/uzunluk)
                            double fiyat = satir.isEmpty() ? 0 : Double.parseDouble(satir.get(0).toString());
                            ps.setInt(1, tableId);
                            if (stringAxis) { ps.setNull(2,java.sql.Types.REAL); ps.setString(4,rowVals.get(ri).toString()); }
                            else { ps.setDouble(2, Double.parseDouble(rowVals.get(ri).toString())); ps.setNull(4,java.sql.Types.VARCHAR); }
                            ps.setNull(3, java.sql.Types.REAL); ps.setNull(5, java.sql.Types.VARCHAR);
                            ps.setDouble(6, fiyat);
                            ps.executeUpdate();
                        } else {
                            for (int ci = 0; ci < colVals.size(); ci++) {
                                double fiyat = (satir.size() > ci) ? Double.parseDouble(satir.get(ci).toString()) : 0;
                                ps.setInt(1, tableId);
                                if (stringAxis) {
                                    ps.setNull(2,java.sql.Types.REAL); ps.setString(4,rowVals.get(ri).toString());
                                    ps.setNull(3,java.sql.Types.REAL); ps.setString(5,colVals.get(ci).toString());
                                } else {
                                    ps.setDouble(2, Double.parseDouble(rowVals.get(ri).toString())); ps.setNull(4,java.sql.Types.VARCHAR);
                                    ps.setDouble(3, Double.parseDouble(colVals.get(ci).toString())); ps.setNull(5,java.sql.Types.VARCHAR);
                                }
                                ps.setDouble(6, fiyat);
                                ps.executeUpdate();
                            }
                        }
                    }
                }
            }
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Kategori bazlı oranlar ───────────────────────────
    @GetMapping("/kategori-oranlari")
    public ResponseEntity<?> kategoriOranlariGetir(
            @RequestHeader(value="X-Token", defaultValue="") String token) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery(
                 "SELECT feature_type, option_name, urun_kategori, ratio, is_sabit FROM feature_ratio_v2 WHERE urun_kategori IS NOT NULL ORDER BY urun_kategori, feature_type, option_name")) {
            java.util.List<Map<String,Object>> list = new java.util.ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new java.util.LinkedHashMap<>();
                row.put("featureType",   rs.getString("feature_type"));
                row.put("optionName",    rs.getString("option_name"));
                row.put("urunKategori",  rs.getString("urun_kategori"));
                row.put("ratio",         rs.getDouble("ratio"));
                row.put("isSabit",       rs.getInt("is_sabit") == 1);
                list.add(row);
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping("/kategori-oranlari")
    public ResponseEntity<?> kategoriOraniEkle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR REPLACE INTO feature_ratio_v2(feature_type,option_name,urun_kategori,ratio,is_sabit) VALUES(?,?,?,?,?)")) {
            ps.setString(1, (String) body.get("featureType"));
            ps.setString(2, (String) body.get("optionName"));
            ps.setString(3, (String) body.get("urunKategori"));
            ps.setDouble(4, Double.parseDouble(body.get("ratio").toString()));
            ps.setInt(5,    Boolean.TRUE.equals(body.get("isSabit")) ? 1 : 0);
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/kategori-oranlari")
    public ResponseEntity<?> kategoriOraniSil(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM feature_ratio_v2 WHERE feature_type=? AND option_name=? AND urun_kategori=?")) {
            ps.setString(1, (String) body.get("featureType"));
            ps.setString(2, (String) body.get("optionName"));
            ps.setString(3, (String) body.get("urunKategori"));
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Sistem Ayarları ──────────────────────────────────
    @GetMapping("/ayarlar")
    public ResponseEntity<?> ayarlariGetir(
            @RequestHeader(value="X-Token", defaultValue="") String token) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.Statement st = conn.createStatement();
             java.sql.ResultSet rs = st.executeQuery("SELECT anahtar,deger,aciklama FROM sistem_ayar ORDER BY anahtar")) {
            java.util.List<Map<String,Object>> list = new java.util.ArrayList<>();
            while (rs.next()) {
                Map<String,Object> row = new java.util.LinkedHashMap<>();
                row.put("anahtar",  rs.getString("anahtar"));
                row.put("deger",    rs.getString("deger"));
                row.put("aciklama", rs.getString("aciklama"));
                list.add(row);
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PutMapping("/ayarlar/{anahtar}")
    public ResponseEntity<?> ayarGuncelle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String anahtar,
            @RequestBody Map<String,String> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try {
            com.teklif.repository.SistemAyarService.set(anahtar, body.get("deger"));
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Ürün Bazlı Oran Override ─────────────────────────
    @GetMapping("/urun-oranlari/{urunKodu}")
    public ResponseEntity<?> urunOranlariGetir(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String urunKodu) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "SELECT feature_type, option_name, ratio, is_sabit FROM feature_ratio_v3 WHERE urun_kodu=? ORDER BY feature_type, option_name")) {
            ps.setString(1, urunKodu);
            java.util.List<Map<String,Object>> list = new java.util.ArrayList<>();
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,Object> row = new java.util.LinkedHashMap<>();
                    row.put("featureType", rs.getString("feature_type"));
                    row.put("optionName",  rs.getString("option_name"));
                    row.put("ratio",       rs.getDouble("ratio"));
                    row.put("isSabit",     rs.getInt("is_sabit") == 1);
                    list.add(row);
                }
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping("/urun-oranlari/{urunKodu}")
    public ResponseEntity<?> urunOraniEkle(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String urunKodu,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "INSERT OR REPLACE INTO feature_ratio_v3(feature_type,option_name,urun_kategori,urun_kodu,ratio,is_sabit) VALUES(?,?,NULL,?,?,?)")) {
            ps.setString(1, (String) body.get("featureType"));
            ps.setString(2, (String) body.get("optionName"));
            ps.setString(3, urunKodu);
            ps.setDouble(4, Double.parseDouble(body.get("ratio").toString()));
            ps.setInt(5,    Boolean.TRUE.equals(body.get("isSabit")) ? 1 : 0);
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @DeleteMapping("/urun-oranlari/{urunKodu}")
    public ResponseEntity<?> urunOraniSil(
            @RequestHeader(value="X-Token", defaultValue="") String token,
            @PathVariable String urunKodu,
            @RequestBody Map<String,Object> body) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM feature_ratio_v3 WHERE feature_type=? AND option_name=? AND urun_kodu=?")) {
            ps.setString(1, (String) body.get("featureType"));
            ps.setString(2, (String) body.get("optionName"));
            ps.setString(3, urunKodu);
            ps.executeUpdate();
            com.teklif.repository.OzellikDeposu.invalidate();
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    // ── Sequence düzelt ──────────────────────────────────
    @PostMapping("/sequence-duzelt")
    public ResponseEntity<?> sequenceDuzelt(
            @RequestHeader(value="X-Token", defaultValue="") String token) {
        if (!ok(token)) return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz"));
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection()) {
            int yil = java.time.LocalDate.now().getYear();
            // Mevcut maksimum teklif sıra numarasını bul
            java.sql.ResultSet rs = conn.createStatement().executeQuery(
                "SELECT MAX(CAST(SUBSTR(teklif_no, -4) AS INTEGER)) as maks FROM teklif WHERE teklif_no LIKE 'TKL-" + yil + "-%'");
            int maks = 0;
            if (rs.next()) maks = rs.getInt("maks");
            // Sequence'i maks değere ayarla
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO teklif_no_sequence(yil,son) VALUES(?,?) ON CONFLICT(yil) DO UPDATE SET son=?");
            ps.setInt(1, yil); ps.setInt(2, maks); ps.setInt(3, maks);
            ps.executeUpdate();
            return ResponseEntity.ok(Map.of("ok", true, "sonDeger", maks, "mesaj", "Sequence " + maks + " olarak ayarlandı"));
        } catch (Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata", e.getMessage())); }
    }

    @PostMapping("/excel-sifirla")
    public ResponseEntity<?> excelSifirla(
            @RequestHeader(value="X-Token",defaultValue="") String token) {
        if (!ok(token)) return no();
        try (Connection c = ConnectionManager.getConnection()) {
            // Tüm fiyat verilerini temizle
            c.createStatement().execute("DELETE FROM price_cell");
            c.createStatement().execute("DELETE FROM price_axis");
            c.createStatement().execute("DELETE FROM price_table");
            c.createStatement().execute("DELETE FROM feature_ratio");
        } catch (Exception e) { return err(e); }
        // Excel'den yeniden import et
        try {
            java.io.InputStream is = getClass().getResourceAsStream("/HAM_FIYATLAR.xlsx");
            if (is == null) return ResponseEntity.internalServerError().body(Map.of("hata","HAM_FIYATLAR.xlsx bulunamadi!"));
            java.io.File temp = java.io.File.createTempFile("HAM_FIYATLAR", ".xlsx");
            temp.deleteOnExit();
            java.nio.file.Files.copy(is, temp.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            new MasterExcelImporter().importAll(temp.getAbsolutePath());
            return ResponseEntity.ok(Map.of("ok", true, "mesaj", "Fiyatlar Excel'den yeniden yüklendi."));
        } catch (Exception e) { return err(e); }
    }

    private boolean isNum(String s) {
        try { Double.parseDouble(s); return true; } catch (Exception e) { return false; }
    }
    private boolean ok(String t) {
        Integer userId = AuthController.tokendenId(t);
        if (userId == null) return false;
        try (java.sql.Connection conn = com.teklif.db.ConnectionManager.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(
                "SELECT rol FROM kullanici WHERE id=? AND aktif=1")) {
            ps.setInt(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() && "ADMIN".equals(rs.getString("rol"));
            }
        } catch (Exception e) { return false; }
    }
    private ResponseEntity<?> no()        { return ResponseEntity.status(401).body(Map.of("hata","Yetkisiz.")); }
    private ResponseEntity<?> err(Exception e) { return ResponseEntity.internalServerError().body(Map.of("hata",e.getMessage())); }
}
