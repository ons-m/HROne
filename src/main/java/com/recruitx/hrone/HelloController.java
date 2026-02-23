package com.recruitx.hrone;

import com.recruitx.hrone.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.sql.Connection;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        try {
            // Utiliser DBConnection.getConnection() directement (static)
            Connection connection = DBConnection.getConnection();

            if (connection != null && !connection.isClosed()) {
                welcomeText.setText("✅ Connecté à MySQL avec succès");
                welcomeText.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
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
}