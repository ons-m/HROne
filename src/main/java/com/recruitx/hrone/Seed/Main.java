package com.recruitx.hrone.Seed;

import com.recruitx.hrone.utils.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        insertCompetences("src\\main\\java\\com\\recruitx\\hrone\\Seed\\Competence.txt");
        insertLangues("src\\main\\java\\com\\recruitx\\hrone\\Seed\\langues.txt");
        insertBackgrounds("src\\main\\java\\com\\recruitx\\hrone\\Seed\\Backround.txt");
        insertNiveauxEtude("src\\main\\java\\com\\recruitx\\hrone\\Seed\\niveauEtd.txt");
        //insertTypesContrat();
        //seedTypeStatusCandidature();

        System.out.println("=== Enum data insertion finished ===");
    }

    private static void seedTypeStatusCandidature() {
        try {
            insertIfNotExists("SUBMITTED");
            insertIfNotExists("REVIEW");
            insertIfNotExists("ACCEPTED");
            insertIfNotExists("REJECTED");

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur seed TYPE_STATUS_CONDIDATURE", ex);
        }
    }

    private static void insertIfNotExists(String description) throws SQLException {

        String sql =
                "INSERT INTO TYPE_STATUS_CONDIDATURE (DESCRIPTION_STATUS_CONDIDATURE) " +
                        "SELECT '" + description + "' " +
                        "WHERE NOT EXISTS (" +
                        "  SELECT 1 FROM TYPE_STATUS_CONDIDATURE " +
                        "  WHERE DESCRIPTION_STATUS_CONDIDATURE = '" + description + "'" +
                        ")";

        DBHelper.ExecuteQuery(sql);
    }



    private static void insertTypesContrat() {
        try {
            String[] contrats = {
                    "CDI",
                    "CDD",
                    "Freelance",
                    "Stage"
            };

            for (String label : contrats) {
                String sql =
                        "INSERT INTO TYPE_CONTRAT (DESCRIPTION_CONTRAT) VALUES ('" +
                                escape(label) + "')";

                DBHelper.ExecuteQuery(sql);
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur insertion types contrat", ex);
        }
    }

    /* =========================
       COMPETENCES
       ========================= */

    private static void insertCompetences(String fileName) {
        try {
            List<String> lines = readLines(fileName);

            for (String label : lines) {
                String sql =
                        "INSERT INTO TYPE_COMPETENCE (Description_Competence) VALUES ('" +
                                escape(label) + "')";

                DBHelper.ExecuteQuery(sql);
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur insertion competences", ex);
        }
    }

    /* =========================
       LANGUES
       ========================= */

    private static void insertLangues(String fileName) {
        try {
            List<String> lines = readLines(fileName);

            for (String label : lines) {
                String sql =
                        "INSERT INTO TYPE_LANGUE (DESCRIPTION_LANGUE) VALUES ('" +
                                escape(label) + "')";

                DBHelper.ExecuteQuery(sql);
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur insertion langues", ex);
        }
    }

    /* =========================
       BACKGROUND ETUDE
       ========================= */

    private static void insertBackgrounds(String fileName) {
        try {
            List<String> lines = readLines(fileName);

            for (String label : lines) {
                String sql =
                        "INSERT INTO TYPE_BACKGROUND_ETUDE (DESCRIPTION_TYPE_BACKGROUND_ETUDE) VALUES ('" +
                                escape(label) + "')";

                DBHelper.ExecuteQuery(sql);
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur insertion backgrounds", ex);
        }
    }

    /* =========================
       HELPERS
       ========================= */

    private static List<String> readLines(String fileName) throws Exception {
        Path path = Path.of(fileName);

        if (!Files.exists(path)) {
            throw new Exception("Fichier introuvable: " + fileName);
        }

        return Files.readAllLines(path).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .toList();
    }

    /**
     * Escapes single quotes for SQL.
     * Example: L'ingénierie -> L''ingénierie
     */
    private static String escape(String value) {
        return value.replace("'", "''");
    }

    private static void insertNiveauxEtude(String fileName) {
        try {
            List<String> lines = readLines(fileName);

            for (String label : lines) {
                String sql =
                        "INSERT INTO TYPE_NIVEAU_ETUDE (DESCRIPTION_TYPE_ETUDE) VALUES ('" +
                                escape(label) + "')";

                DBHelper.ExecuteQuery(sql);
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur insertion niveaux etude", ex);
        }
    }
}
