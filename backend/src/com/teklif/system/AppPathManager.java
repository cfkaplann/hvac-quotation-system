package com.teklif.system;

import java.io.File;

public class AppPathManager {

    public static File getAppDataDir() {
        File dir = new File(System.getProperty("user.dir"));
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public static File getDatabaseFile() {
        return new File(getAppDataDir(), "teklif.db");
    }
}
