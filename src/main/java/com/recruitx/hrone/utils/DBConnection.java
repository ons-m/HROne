package com.recruitx.hrone.utils;

import java.sql.Connection;
import java.sql.DriverManager;
<<<<<<< HEAD
import java.sql.SQLException;
=======
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857

public class DBConnection {
    private static Connection connection;

<<<<<<< HEAD
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

=======
    // Configuration pour votre base hr_one
    private static final String URL = "jdbc:mysql://localhost:3306/hr_one";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    /**
     * Retourne la connexion unique à la base de données
     * @return Connection object ou null en cas d'erreur
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // 1. Charger le driver MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");

                // 2. Établir la connexion
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                // 3. Vérifier que la connexion est active
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ Connexion MySQL établie avec succès !");
                    System.out.println("📊 Base de données : hr_one");
                } else {
                    System.err.println("⚠️ Connexion établie mais fermée !");
                }

            } catch (ClassNotFoundException e) {
                System.err.println("❌ ERREUR : Driver MySQL non trouvé !");
                System.err.println("➡️ Vérifiez que mysql-connector-j est dans pom.xml");
                e.printStackTrace();

            } catch (Exception e) {
                System.err.println("❌ ERREUR : Impossible de se connecter à MySQL !");
                System.err.println("➡️ URL: " + URL);
                System.err.println("➡️ User: " + USER);
                System.err.println("➡️ Message: " + e.getMessage());

                // Suggestions de dépannage
                System.err.println("\n🔧 VÉRIFIEZ :");
                System.err.println("1. XAMPP est-il démarré ?");
                System.err.println("2. MySQL tourne-t-il sur le port 3306 ?");
                System.err.println("3. La base 'hr_one' existe-t-elle ?");
                System.err.println("4. L'utilisateur 'root' a-t-il les droits ?");

                e.printStackTrace();
            }
        }
        return connection;
    }

    /**
     * Méthode alternative pour compatibilité
     * @return Connection object
     */
    public static Connection getInstance() {
        return getConnection();
    }

    /**
     * Ferme la connexion à la base de données
     */
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
<<<<<<< HEAD
                System.out.println("🔌 Connexion fermée");
            } catch (SQLException e) {
                System.err.println("❌ Erreur fermeture: " + e.getMessage());
            }
        }
    }
=======
                System.out.println("🔌 Connexion MySQL fermée");
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la fermeture : " + e.getMessage());
            }
        }
    }

    /**
     * Vérifie si la connexion est active
     * @return true si la connexion est ouverte
     */
    public static boolean isConnected() {
        if (connection == null) {
            return false;
        }
        try {
            return !connection.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Teste la connexion et affiche les informations
     */
    public static void testConnection() {
        System.out.println("\n🔍 TEST DE CONNEXION MySQL");
        System.out.println("=========================");

        Connection conn = getConnection();
        if (conn != null) {
            try {
                System.out.println("✅ CONNEXION RÉUSSIE !");
                System.out.println("📋 Informations :");
                System.out.println("   - Base : " + conn.getCatalog());
                System.out.println("   - Auto-commit : " + conn.getAutoCommit());
                System.out.println("   - Transaction isolation : " + conn.getTransactionIsolation());
                System.out.println("   - Base 'hr_one' accessible ✓");

            } catch (Exception e) {
                System.err.println("⚠️ Connexion OK mais erreur sur getCatalog()");
            }
        } else {
            System.err.println("❌ CONNEXION ÉCHOUÉE !");
        }
    }
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
}