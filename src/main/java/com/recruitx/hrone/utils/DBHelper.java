package com.recruitx.hrone.utils;

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

    public static Object getSingleValue(String sql, Object... params) {
        DBHelper db = new DBHelper();
        try {
            return db.getSingleResult(sql, params);
        } finally {
            db.close();
        }
    }

    public static int execute(String sql, Object... params) {
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
}