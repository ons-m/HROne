package com.recruitx.hrone.utils;

<<<<<<< HEAD
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBHelper {

    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;

    public DBHelper() {
        this.conn = DBConnection.getConnection();
    }

    public ResultSet executeQuery(String sql, Object... params) {
        try {
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            rs = stmt.executeQuery();
            return rs;
        } catch (SQLException e) {
            System.err.println("❌ Erreur executeQuery: " + e.getMessage());
            return null;
        }
    }

    public int executeUpdate(String sql, Object... params) {
        try {
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("❌ Erreur executeUpdate: " + e.getMessage());
            return -1;
        }
    }

    public Object getSingleResult(String sql, Object... params) {
        try {
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getObject(1);
            }
            return null;
        } catch (SQLException e) {
            System.err.println("❌ Erreur getSingleResult: " + e.getMessage());
            return null;
        }
    }

    public List<Object[]> getResults(String sql, Object... params) {
        List<Object[]> results = new ArrayList<>();
        try {
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            rs = stmt.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                results.add(row);
            }
            return results;
        } catch (SQLException e) {
            System.err.println("❌ Erreur getResults: " + e.getMessage());
            return results;
        }
    }

    public void close() {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Erreur close: " + e.getMessage());
        }
    }

    // Méthodes statiques pour usage rapide
    public static Object getSingleValue(String sql, Object... params) {
        DBHelper db = new DBHelper();
        try {
            return db.getSingleResult(sql, params);
        } finally {
            db.close();
        }
    }

    public static int executeUpdateStatic(String sql, Object... params) {
        DBHelper db = new DBHelper();
        try {
            return db.executeUpdate(sql, params);
        } finally {
            db.close();
        }
    }

    public static boolean userExists(int userId) {
        Object result = getSingleValue("SELECT id FROM users WHERE id = ?", userId);
        return result != null;
    }

    public static String getUserName(int userId) {
        Object result = getSingleValue("SELECT display_name FROM users WHERE id = ?", userId);
        return result != null ? result.toString() : "Utilisateur";
    }
}  // ← Cette accolade était peut-être manquante !
=======
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class DBHelper {

    private DBHelper() {
        // Prevent instantiation
    }

    /* ==========================================
       1. ExecuteScalar
       ========================================== */

    /**
     * Executes a SQL query that returns a single value.
     * Example: SELECT COUNT(*) FROM users
     */
    public static Object ExecuteScalar(String sql) throws SQLException {

        long startTime = System.currentTimeMillis();

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            Object result = rs.next() ? rs.getObject(1) : null;

            CError.Log_Sql(
                    sql,
                    System.currentTimeMillis() - startTime,
                    null
            );

            return result;

        } catch (SQLException ex) {
            CError.Log_Sql(
                    sql,
                    System.currentTimeMillis() - startTime,
                    ex
            );
            throw ex;
        }
    }

    /* ==========================================
       2. ExecuteQuery (INSERT / UPDATE / DELETE)
       ========================================== */

    /**
     * Executes INSERT, UPDATE or DELETE statements.
     * Returns the number of affected rows.
     */
    public static int ExecuteQuery(String sql) throws SQLException {

        long startTime = System.currentTimeMillis();

        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int rows = stmt.executeUpdate();

            CError.Log_Sql(
                    sql,
                    System.currentTimeMillis() - startTime,
                    null
            );

            return rows;

        } catch (SQLException ex) {
            CError.Log_Sql(
                    sql,
                    System.currentTimeMillis() - startTime,
                    ex
            );
            throw ex;
        }
    }

    /* ==========================================
       3. ExecuteDataReader
       ========================================== */

    /**
     * Executes a SELECT query and returns a ResultSet.
     * Caller is responsible for closing:
     *   - ResultSet
     *   - Statement
     *   - Connection
     */
    public static ResultSet ExecuteDataReader(String sql) throws SQLException {

        long startTime = System.currentTimeMillis();

        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            CError.Log_Sql(
                    sql,
                    System.currentTimeMillis() - startTime,
                    null
            );

            return rs;

        } catch (SQLException ex) {
            CError.Log_Sql(
                    sql,
                    System.currentTimeMillis() - startTime,
                    ex
            );
            throw ex;
        }
    }
}
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
