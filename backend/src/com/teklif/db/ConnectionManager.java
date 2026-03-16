package com.teklif.db;

import java.sql.Connection;
import java.sql.DriverManager;

import com.teklif.system.AppPathManager;

public class ConnectionManager {

    private static final boolean DEBUG = false;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws Exception {

        String dbPath = AppPathManager.getDatabaseFile().getAbsolutePath();

        if (DEBUG) {
            System.out.println("DB PATH = " + dbPath);
        }

        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }
}