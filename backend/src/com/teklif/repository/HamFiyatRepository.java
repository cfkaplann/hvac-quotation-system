package com.teklif.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.teklif.db.ConnectionManager;

public class HamFiyatRepository {

	public double hamFiyatBul(String sheetName, double w, double h) {

		try (Connection conn = ConnectionManager.getConnection()) {

			PreparedStatement psTable = conn.prepareStatement("SELECT id FROM price_table WHERE sheet_name=?");

			psTable.setString(1, sheetName);

			ResultSet rsTable = psTable.executeQuery();

			if (!rsTable.next()) {
				throw new RuntimeException("Sheet bulunamadı: " + sheetName);
			}

			int tableId = rsTable.getInt("id");

			double rowAxis = axisBul(conn, tableId, "ROW", w);
			double colAxis = axisBul(conn, tableId, "COL", h);

			PreparedStatement psPrice = conn
					.prepareStatement("SELECT price FROM price_cell WHERE table_id=? AND row_value=? AND col_value=?");

			psPrice.setInt(1, tableId);
			psPrice.setDouble(2, rowAxis);
			psPrice.setDouble(3, colAxis);

			ResultSet rsPrice = psPrice.executeQuery();

			if (!rsPrice.next()) {
				throw new RuntimeException("Fiyat bulunamadı.");
			}

			return rsPrice.getDouble("price");

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Ham fiyat çekilemedi.");
		}
	}

	private double axisBul(Connection conn, int tableId, String axis, double value) throws Exception {

		PreparedStatement ps = conn.prepareStatement("SELECT value_num FROM price_axis "
				+ "WHERE table_id=? AND axis=? AND value_num>=? " + "ORDER BY value_num ASC LIMIT 1");

		ps.setInt(1, tableId);
		ps.setString(2, axis);
		ps.setDouble(3, value);

		ResultSet rs = ps.executeQuery();

		if (!rs.next()) {
			throw new RuntimeException("Axis bulunamadı.");
		}

		return rs.getDouble("value_num");
	}

	// =====================================================
	// NUMERIC STRATEJİLER
	// =====================================================

	public int tableIdBul(Connection conn, String sheetName) throws Exception {
		PreparedStatement ps = conn.prepareStatement("SELECT id FROM price_table WHERE sheet_name=?");
		ps.setString(1, sheetName);
		ResultSet rs = ps.executeQuery();
		if (!rs.next())
			throw new RuntimeException("Sheet bulunamadı: " + sheetName);
		return rs.getInt("id");
	}

	public double ceilingAxis(Connection conn, int tableId, String axis, double value) throws Exception {
		PreparedStatement ps = conn.prepareStatement(
				"SELECT value_num FROM price_axis WHERE table_id=? AND axis=? AND value_num>=? ORDER BY value_num ASC LIMIT 1");
		ps.setInt(1, tableId);
		ps.setString(2, axis);
		ps.setDouble(3, value);
		ResultSet rs = ps.executeQuery();
		if (!rs.next())
			throw new RuntimeException("Axis bulunamadı: " + axis + " value=" + value);
		return rs.getDouble("value_num");
	}

	public double maxAxis(Connection conn, int tableId, String axis) throws Exception {
		PreparedStatement ps = conn
				.prepareStatement("SELECT MAX(value_num) AS mx FROM price_axis WHERE table_id=? AND axis=?");
		ps.setInt(1, tableId);
		ps.setString(2, axis);
		ResultSet rs = ps.executeQuery();
		if (!rs.next())
			throw new RuntimeException("Max axis bulunamadı: " + axis);
		return rs.getDouble("mx");
	}

	public double cellPrice(Connection conn, int tableId, double rowVal, double colVal) throws Exception {
		PreparedStatement ps = conn
				.prepareStatement("SELECT price FROM price_cell WHERE table_id=? AND row_value=? AND col_value=?");
		ps.setInt(1, tableId);
		ps.setDouble(2, rowVal);
		ps.setDouble(3, colVal);
		ResultSet rs = ps.executeQuery();
		if (!rs.next())
			throw new RuntimeException("Fiyat bulunamadı (row=" + rowVal + ", col=" + colVal + ")");
		return rs.getDouble("price");
	}

	// =====================================================
	// ⭐ STRING SIZE STRATEGY İÇİN EKLENENLER
	// =====================================================

	public List<String> axisStringList(Connection conn, int tableId, String axis) throws Exception {

		List<String> list = new ArrayList<>();

		PreparedStatement ps = conn.prepareStatement("SELECT value_str FROM price_axis WHERE table_id=? AND axis=?");

		ps.setInt(1, tableId);
		ps.setString(2, axis);

		ResultSet rs = ps.executeQuery();

		while (rs.next()) {
			list.add(rs.getString("value_str"));
		}

		return list;
	}

	public String ceilingString(List<String> axisList, String input){

	    // ⭐ çap mı?
	    boolean diameterMode = !input.contains("x");

	    // =================================================
	    // DIAMETER MODE
	    // =================================================
	    if(diameterMode){

	        double hedef =
	                Double.parseDouble(input.replace("Ø","").trim());

	        String enUygun = null;
	        double min = Double.MAX_VALUE;

	        for(String s : axisList){

	            if(s == null || s.isBlank()) continue;

	            double val =
	                    Double.parseDouble(s.replace("Ø","").trim());

	            if(val >= hedef && val < min){
	                min = val;
	                enUygun = s;
	            }
	        }

	        if(enUygun == null)
	            throw new RuntimeException("Ceiling string bulunamadı : " + input);

	        return enUygun;
	    }

	    // =================================================
	    // WxH MODE (ESKİ SİSTEM)
	    // =================================================
	    double[] hedef = parseWH(input);

	    String enUygun = null;
	    double minAlan = Double.MAX_VALUE;

	    for(String s : axisList){

	        double[] p = parseWH(s);

	        if(p[0] >= hedef[0] && p[1] >= hedef[1]){

	            double alan = p[0] * p[1];

	            if(alan < minAlan){
	                minAlan = alan;
	                enUygun = s;
	            }
	        }
	    }

	    if(enUygun == null)
	        throw new RuntimeException("Ceiling string bulunamadı : " + input);

	    return enUygun;
	}


	public double cellPriceString(Connection conn, int tableId, String rowVal, String colVal) throws Exception {

		PreparedStatement ps;

		if (colVal == null) {

			ps = conn.prepareStatement(
					"SELECT price FROM price_cell " + "WHERE table_id=? AND row_value_str=? AND col_value_str IS NULL");

			ps.setInt(1, tableId);
			ps.setString(2, rowVal);

		} else {

			ps = conn.prepareStatement(
					"SELECT price FROM price_cell " + "WHERE table_id=? AND row_value_str=? AND col_value_str=?");

			ps.setInt(1, tableId);
			ps.setString(2, rowVal);
			ps.setString(3, colVal);
		}

		ResultSet rs = ps.executeQuery();

		if (!rs.next())
			throw new RuntimeException("String fiyat bulunamadı");

		return rs.getDouble("price");
	}

	private double[] parseWH(String val){

	    String temiz = val.replace("Ø","").trim();

	    String[] parca = temiz.split("x");

	    // ⭐⭐⭐ TEK ÇAP GELİRSE → KARE GİBİ DAVRAN ⭐⭐⭐
	    if(parca.length == 1){

	        double d = Double.parseDouble(parca[0]);

	        // çapı WxH gibi düşün
	        return new double[]{ d, d };
	    }

	    return new double[]{
	        Double.parseDouble(parca[0]),
	        Double.parseDouble(parca[1])
	    };
	}


	// HamFiyatRepository içine ekle
	public List<String> bogazSecenekleri(Connection conn, int tableId, String kasaRow) throws Exception {

		List<String> list = new ArrayList<>();

		PreparedStatement ps = conn.prepareStatement("SELECT DISTINCT col_value_str " + "FROM price_cell "
				+ "WHERE table_id=? AND row_value_str=? AND price IS NOT NULL " + "ORDER BY col_value_str");

		ps.setInt(1, tableId);
		ps.setString(2, kasaRow);

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			list.add(rs.getString("col_value_str"));
		}

		return list;
	}
	// ⭐ TEK KOLON STRING TABLO İÇİN
	public double cellPriceSingleString(Connection conn,
	                                     int tableId,
	                                     String rowVal) throws Exception {

	    PreparedStatement ps = conn.prepareStatement(
	        "SELECT price FROM price_cell " +
	        "WHERE table_id=? AND row_value_str=?"
	    );

	    ps.setInt(1, tableId);
	    ps.setString(2, rowVal);

	    ResultSet rs = ps.executeQuery();

	    if(!rs.next())
	        throw new RuntimeException("Single string fiyat bulunamadı");

	    return rs.getDouble("price");
	}

}
