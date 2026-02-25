package com.recruitx.hrone;

import com.recruitx.hrone.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.sql.Connection;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    private Button helloButton;

    @FXML
    protected void onHelloButtonClick() {
        try {
            Connection connection = DBConnection.getConnection();

            if (connection != null && !connection.isClosed()) {
                welcomeText.setText("✅ Connecté à MySQL avec succès");
                welcomeText.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                goToBlog();
            } else {
                welcomeText.setText("⚠️ Connexion échouée");
                welcomeText.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            }

        } catch (Exception e) {
            welcomeText.setText("❌ Erreur: " + e.getMessage());
            welcomeText.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            e.printStackTrace();
        }
    }

    private void goToBlog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/blog.fxml"));
            Parent blogRoot = loader.load();

            Stage stage = (Stage) helloButton.getScene().getWindow();
            Scene scene = new Scene(blogRoot, 1200, 800);
            stage.setScene(scene);
            stage.setTitle("HR One - Employee Forum");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}