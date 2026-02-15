package com.recruitx.hrone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestFormationMainfx extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/recruitx/hrone/formation/formations-dashboard.fxml")
            );

            Scene scene = new Scene(loader.load(), 1400, 900);

            // Load CSS
            String css = getClass()
                    .getResource("/com/recruitx/hrone/formation/formations-dashboard.fx.css")
                    .toExternalForm();
            scene.getStylesheets().add(css);

            stage.setTitle("HR One - Gestion des Formations");
            stage.setScene(scene);
            stage.setMinWidth(1200);
            stage.setMinHeight(700);
            stage.show();

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'application:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}