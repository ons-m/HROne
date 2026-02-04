package com.recruitx.hrone.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    // 🔒 Single instance of the connection
    private static Connection connection;

    // 🔧 TEMPORARY hardcoded configuration (will be externalized later)
    private static final String URL =
            "jdbc:mysql://localhost:3306/hr_one"
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC";

    private static final String USER = "root";     // XAMPP default
    private static final String PASSWORD = "";     // XAMPP default (usually empty)

    // 🚫 Prevent instantiation
    private DBConnection() {
    }

    /**
     * Returns a singleton JDBC connection.
     */
    public static Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }
}
