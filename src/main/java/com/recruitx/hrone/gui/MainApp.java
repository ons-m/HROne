package com.recruitx.hrone.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage primaryStage;
    private static Scene mainScene;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        // Show the login screen at startup
        showLoginScreen();

        // Minimum window size
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
    }

    /**
     * Load and show a FXML screen
     */
    private static void loadScreen(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource(fxmlPath));
            Parent root = loader.load();
            if (mainScene == null) {
                mainScene = new Scene(root);
                primaryStage.setScene(mainScene);
            } else {
                mainScene.setRoot(root);
            }
            primaryStage.setTitle(title);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showLoginScreen() {
        loadScreen("/com/recruitx/hrone/pages/login/login.fxml", "HR One - Connexion");
    }

    public static void showCandidateSignup() {
        loadScreen("/com/recruitx/hrone/pages/signupCandidate/signupCandidate.fxml", "HR One - Inscription Candidat");
    }

    public static void showRhSignup() {
        loadScreen("/com/recruitx/hrone/pages/signupRh/signupRh.fxml", "HR One - Inscription RH");
    }

    public static void showGestionEmployee() {
        loadScreen("/com/recruitx/hrone/pages/gestion-employees/gestion-employees.fxml",
                "HR One - Gestion Employee");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
