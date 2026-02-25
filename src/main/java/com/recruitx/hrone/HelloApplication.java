package com.recruitx.hrone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("📂 Chargement du fichier FXML...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);

            primaryStage.setTitle("HR One - Employee Forum");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            System.out.println("✅ Application démarrée !");

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}