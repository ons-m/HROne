package com.recruitx.hrone.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public final class CError {

    private static final String LOG_DIR =
            System.getProperty("user.dir") + File.separator + "logs";

    private static final String LOG_FILE =
            LOG_DIR + File.separator + "Echsar.log";

    private static final String SQL_LOG_FILE =
            LOG_DIR + File.separator + "Echsar_Sql.log";

    private CError() {
        // Prevent instantiation
    }

    /* ===============================
       Public logging API (General)
       =============================== */

    public static void log(LogType errorType, String message) {
        write(errorType, message, null);
    }

    public static void log(LogType errorType, String message, Exception ex) {
        write(errorType, message, ex);
    }

    public static void log(LogType errorType, Exception ex) {
        write(errorType, ex.getMessage(), ex);
    }

    /* ===============================
       Public logging API (SQL)
       =============================== */

    public static synchronized void Log_Sql(
            String sql,
            long executionTimeMs,
            Exception ex) {

        try {
            createLogDirIfNeeded();

            long numOrdre = COrdre.GetNumOrdreNow();

            try (PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(SQL_LOG_FILE, true)))) {

                String header =
                        "[SQL] " +
                                Global.DATETIME_FORMAT.format(LocalDateTime.now()) +
                                " (numOrdre=" + numOrdre + ")" +
                                " execTime=" + executionTimeMs +
                                " sql=" + sanitize(sql) +
                                " exception=" + (ex == null ? "none" : ex.toString());

                out.println(header);

                if (ex != null) {
                    ex.printStackTrace(out);
                }
            }

        } catch (IOException ignored) {
            // Never let SQL logging crash the app
        }
    }

    /* ===============================
       Internal write logic (General)
       =============================== */

    private static synchronized void write(
            LogType errorType,
            String message,
            Exception ex) {

        try {
            createLogDirIfNeeded();

            long numOrdre = COrdre.GetNumOrdreNow();

            try (PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(LOG_FILE, true)))) {

                String header =
                        "[" + errorType + "] " +
                                Global.DATETIME_FORMAT.format(LocalDateTime.now()) +
                                " (numOrdre=" + numOrdre + ")" +
                                " message=" + sanitize(message) +
                                " exception=" + (ex == null ? "none" : ex.toString());

                out.println(header);

                if (ex != null) {
                    ex.printStackTrace(out);
                }
            }

        } catch (IOException ignored) {
            // Never let logging crash the app
        }
    }

    private static void createLogDirIfNeeded() {
        File dir = new File(LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static String sanitize(String value) {
        if (value == null) {
            return "null";
        }
        return value
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();
    }
}
