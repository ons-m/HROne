package com.recruitx.hrone.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    private static final String URL = "jdbc:mysql://localhost:3306/blog_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&autoReconnect=true";
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
            // Tester si la connexion existe et est valide
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Nouvelle connexion MySQL établie");
            } else {
                // Tester si la connexion fonctionne vraiment
                try {
                    connection.createStatement().execute("SELECT 1");
                } catch (SQLException e) {
                    // Connexion morte, on en crée une nouvelle
                    System.out.println("⚠️ Connexion morte, reconnexion...");
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                }
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("❌ Erreur de connexion: " + e.getMessage());
            return null;
        }
    }

    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() &&
                    connection.createStatement().execute("SELECT 1");
        } catch (SQLException e) {
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
}