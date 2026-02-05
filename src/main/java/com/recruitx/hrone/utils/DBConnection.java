package com.recruitx.hrone.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    // 🔒 Single instance of the connection
    private static Connection connection;

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

            //Collect Parameters from config.ini
            String DatabaseName = Global.LireParamParNom("DatabaseName");
            String MysqlPort = Global.LireParamParNom("MysqlPort");
            String DatabaseServer = Global.LireParamParNom("DatabaseServer");

            //Check if all the configuration parameters are present
            if(DatabaseServer == null || DatabaseServer.isEmpty()
            || MysqlPort == null || MysqlPort.isEmpty()
            || DatabaseName == null || DatabaseName.isEmpty()) {
                throw new SQLException("Database configuration parameters are missing.");
            }

            //Build the connection URL
            String URL = "jdbc:mysql://" + DatabaseServer + ":" + MysqlPort + "/" + DatabaseName
                    + "?useSSL=false"
                    + "&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC";

            //Connect to the database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }

        // Return the singleton connection
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace(); // Log error during close
            }
        }
    }

}
