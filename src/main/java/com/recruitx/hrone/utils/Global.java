package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class Global {

        /* ===============================
       Application constants
       =============================== */

    public static final String APP_NAME = "YourApp";
    public static final String APP_VERSION = "1.0.0";
    public static final String CONFIG_FILE = "config.ini";

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

    public static void LireParamParNom(
            AtomicReference<String> param,
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
                    param.set(value);
                    return;
                }
            }

        } catch (IOException ex) {
            Error.log(LogType.ERROR,"Failed to read config param: " + nomParam, ex);
        }
    }

    /* ===============================
       Integer parameter
       =============================== */

    public static void LireParamParNom(
            AtomicInteger param,
            String nomParam) {

        try (BufferedReader br = new BufferedReader(
                new FileReader(CONFIG_FILE))) {

            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();

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
                    param.set(Integer.parseInt(value));
                    return;
                }
            }

        } catch (NumberFormatException ex) {
            Error.log(LogType.ERROR,"Invalid integer value for param: " + nomParam, ex);
        } catch (IOException ex) {
            Error.log(LogType.ERROR,"Failed to read config param: " + nomParam, ex);
        }
    }

    public static void LireParam(){
        System.out.println("Lecture des parametres de configuration...");
    }

}
