package com.recruitx.hrone.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Global {

        /* ===============================
       Application constants
       =============================== */

    public static final String APP_NAME = "YourApp";
    public static final String APP_VERSION = "0.0.1";
    public static final String CONFIG_FILE =
            getAppDirectory().resolve("config.ini").toString();


    public static Path getAppDirectory() {
        try {
            return Paths.get(
                    Global.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI()
            ).getParent();
        } catch (Exception e) {
            return Paths.get(System.getProperty("user.dir"));
        }
    }

    public static void ensureConfigExists() {
        Path target = getAppDirectory().resolve("config.ini");

        try (InputStream in =
                     Global.class.getClassLoader().getResourceAsStream("config.ini")) {

            if (in == null) {
                CError.log(LogType.ERROR, "Default config.ini not found in resources");
                return;
            }

            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Failed to create config.ini", ex);
        }
    }
    /* ===============================
       Date / Time formats
       =============================== */

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static final DateTimeFormatter DATETIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Global() {
        // Prevent instantiation
    }

    /* ===============================
     String parameter
     =============================== */

    public static String LireParamParNom(
            String nomParam) {

        try (BufferedReader br = new BufferedReader(
                new FileReader(CONFIG_FILE))) {

            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();

                // Ignore empty lines & comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0].trim();
                String value = parts[1].trim();

                if (key.equalsIgnoreCase(nomParam)) {
                    return value;
                }
            }

        } catch (IOException ex) {
            CError.log(LogType.ERROR,"Failed to read config param: " + nomParam, ex);
        }
        return "";
    }

    public static void LireParam(){
        System.out.println("Lecture des parametres de configuration...");
    }

}
