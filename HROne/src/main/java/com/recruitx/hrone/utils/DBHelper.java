package com.recruitx.hrone.utils;

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
