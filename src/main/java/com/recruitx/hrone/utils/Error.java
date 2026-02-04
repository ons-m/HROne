package Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

public final class Error {

    private static final String LOG_DIR =
            System.getProperty("user.dir") + File.separator + "logs";

    private static final String LOG_FILE =
            LOG_DIR + File.separator + "Echsar.log";

    private Error() {
        // Prevent instantiation
    }

    /* ===============================
       Public logging API
       =============================== */

    public static void log(LogType errorType,String message) {
        write(errorType, message, null);
    }

    public static void log(LogType errorType,String message, Exception ex) {
        write(errorType, message, ex);
    }

    public static void log(LogType errorType,Exception ex) {
        write(errorType, ex.getMessage(), ex);
    }

    /* ===============================
       Internal write logic
       =============================== */

    private static synchronized void write(
            LogType errorType,
            String message,
            Exception ex) {

        try {
            createLogDirIfNeeded();

            try (PrintWriter out = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(LOG_FILE, true)))) {
                String level = switch (errorType) {
                    case ERROR -> "ERROR";
                    case INFO -> "INFO";
                    case DEBUG -> "DEBUG";
                };

                out.println("[" + level + "] "
                        + Global.DATETIME_FORMAT.format(LocalDateTime.now()));
                out.println("Message: " + message);

                if (ex != null) {
                    out.println("Exception:");
                    ex.printStackTrace(out);
                }

                out.println("--------------------------------------------------");
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
}
