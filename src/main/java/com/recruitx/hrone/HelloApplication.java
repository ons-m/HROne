package com.recruitx.hrone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

<<<<<<< HEAD
import java.net.URL;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("📂 Chargement du fichier FXML...");

            // Charger blog.fxml depuis resources
            URL fxmlUrl = getClass().getResource("/blog.fxml");
            if (fxmlUrl == null) {
                System.err.println("❌ ERREUR: blog.fxml introuvable dans resources!");
                System.err.println("📁 Vérifiez que le fichier est dans: src/main/resources/blog.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(loader.load(), 1200, 800);

            // Charger le CSS si disponible
            try {
                String css = getClass().getResource("/style.css").toExternalForm();
                scene.getStylesheets().add(css);
                System.out.println("✅ CSS chargé");
            } catch (Exception e) {
                System.out.println("ℹ️ style.css non trouvé");
            }

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

    @Override
    public void stop() {
        System.out.println("👋 Arrêt de HR One");
    }

    @Override
    public void init() {
        System.out.println("🔄 Initialisation...");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
=======
import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        String css = HelloApplication.class.getResource("style.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
