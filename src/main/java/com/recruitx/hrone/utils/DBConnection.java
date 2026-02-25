package com.recruitx.hrone.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    private static Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL chargé");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver MySQL non trouvé !");
        }
    }

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Nouvelle connexion MySQL établie");
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            return null;
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Test connexion échoué: " + e.getMessage());
            return false;
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("🔌 Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur fermeture: " + e.getMessage());
            }
        }
    }

    public static void createDatabaseIfNotExists() {
        String createDbUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(createDbUrl, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS blog_db";
            stmt.executeUpdate(sql);
            System.out.println("✅ Base de données 'blog_db' vérifiée/créée");

        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la création de la base: " + e.getMessage());
        }
    }
}