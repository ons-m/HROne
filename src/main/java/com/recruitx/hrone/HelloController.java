package com.recruitx.hrone;

import com.recruitx.hrone.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
<<<<<<< HEAD
=======

>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
import java.sql.Connection;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        try {
<<<<<<< HEAD
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
=======
            // CORRECTION ICI : getConnection() au lieu de getInstance()
            Connection connection = DBConnection.getConnection();

            if (connection != null && !connection.isClosed()) {
                welcomeText.setText("✅ Connected to MySQL successfully");
            } else {
                welcomeText.setText("⚠️ Connection is null or closed");
            }

        } catch (Exception e) {
            welcomeText.setText("❌ Database connection failed");
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
            e.printStackTrace();
        }
    }
}