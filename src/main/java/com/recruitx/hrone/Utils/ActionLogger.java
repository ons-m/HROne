package com.recruitx.hrone.Utils;

import com.recruitx.hrone.Controllers.Session;
import com.recruitx.hrone.Models.Utilisateur;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public final class ActionLogger {

    private static volatile boolean schemaChecked = false;

    private ActionLogger() {
    }

    public static void log(String actionName, String commentaire) {
        Utilisateur currentUser = Session.getCurrentUser();
        if (currentUser == null || actionName == null || actionName.isBlank()) {
            return;
        }

        try {
            Connection connection = DBConnection.getInstance();
            String actionTypeCode = ensureActionType(connection, actionName.trim());

            long baseNumOrdre = COrdre.GetNumOrdreNow();
            for (int i = 0; i < 5; i++) {
                long numOrdre = baseNumOrdre + i;
                ensureOrdreExists(connection, numOrdre);

                if (insertAction(connection, currentUser.getIdUtilisateur(), actionTypeCode, numOrdre, commentaire)) {
                    return;
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String ensureActionType(Connection connection, String actionName) throws SQLException {
        String selectSql = "SELECT Code_Type_Action FROM Type_Action WHERE Description_Action = ? LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, actionName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Code_Type_Action");
                }
            }
        }

        String insertSql = "INSERT INTO Type_Action (Description_Action) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(insertSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, actionName);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setString(1, actionName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Code_Type_Action");
                }
            }
        }

        throw new SQLException("Action type not found and could not be created: " + actionName);
    }

    private static void ensureOrdreExists(Connection connection, long numOrdre) throws SQLException {
        LocalDateTime dt = COrdre.GetDateFromNumOrdre(numOrdre);

        String sql = """
                INSERT INTO Ordre (Num_Ordre, AAAA, MM, JJ, HH, MN, SS)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE Num_Ordre = Num_Ordre
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, numOrdre);
            stmt.setInt(2, dt.getYear());
            stmt.setInt(3, dt.getMonthValue());
            stmt.setInt(4, dt.getDayOfMonth());
            stmt.setInt(5, dt.getHour());
            stmt.setInt(6, dt.getMinute());
            stmt.setInt(7, dt.getSecond());
            stmt.executeUpdate();
        }
    }

    private static boolean insertAction(Connection connection, int userId, String actionTypeCode, long numOrdre, String commentaire) throws SQLException {
        String sql = "INSERT INTO Action_utilisateur (ID_UTILISATEUR, Code_Type_Action, Num_Ordre, Commentaire) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, actionTypeCode);
            stmt.setLong(3, numOrdre);
            stmt.setString(4, commentaire == null ? "" : commentaire);
            stmt.executeUpdate();
            return true;
        } catch (SQLException ex) {
            String msg = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (msg.contains("duplicate")) {
                return false;
            }
            throw ex;
        }
    }
}
