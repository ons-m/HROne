package com.recruitx.hrone;

import com.recruitx.hrone.utils.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;

public class HelloController {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        try {
            Connection connection = DBConnection.getInstance();

            if (connection != null && !connection.isClosed()) {
                welcomeText.setText("✅ Connected to MySQL successfully");
            } else {
                welcomeText.setText("⚠️ Connection is null or closed");
            }
        } catch (Exception e) {
            welcomeText.setText("❌ Database connection failed");
            e.printStackTrace(); // visible in terminal for debugging
        }
    }

    // ========== MÉTHODES POUR OUVRIR LES AUTRES CRUD ==========

    @FXML
    protected void onGestionPostsClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/recruitx/hrone/post-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Gestion des Posts");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    @FXML
    protected void onGestionCommentairesClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/recruitx/hrone/commentaire-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Gestion des Commentaires");
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    @FXML
    protected void onGestionReactionsClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/recruitx/hrone/reaction-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Gestion des Réactions");
        stage.setScene(new Scene(root, 900, 600));
        stage.show();
    }

    @FXML
    protected void onGestionFormationsClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/recruitx/hrone/formation-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Gestion des Formations");
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    @FXML
    protected void onGestionTypesReactionClick() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/com/recruitx/hrone/typereaction-view.fxml"));
        Stage stage = new Stage();
        stage.setTitle("Gestion des Types de Réaction");
        stage.setScene(new Scene(root, 800, 500));
        stage.show();
    }
}