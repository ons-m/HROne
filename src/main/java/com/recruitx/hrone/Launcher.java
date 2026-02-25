package com.recruitx.hrone;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("🚀 Démarrage de HR One - Forum collaboratif");
        System.out.println("✅ Lancement de l'application...");

        try {
            Application.launch(HelloApplication.class, args);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}