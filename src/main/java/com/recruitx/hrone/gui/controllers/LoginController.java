package com.recruitx.hrone.gui.controllers;

import com.recruitx.hrone.entities.Utilisateur;
import com.recruitx.hrone.gui.MainApp;
import com.recruitx.hrone.gui.Session;
import com.recruitx.hrone.services.UtilisateurCRUD;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

public class LoginController {

    @FXML private ImageView appLogo;          // logo ajouté
    @FXML private ComboBox<String> loginRole;
    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;

    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();

    @FXML
    public void initialize() {
        // Charger le logo
        Image logo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/recruitx/hrone/images/logo.png"))); // place ton logo dans src/main/resources/images/logo.png
        appLogo.setImage(logo);

        loginRole.setItems(FXCollections.observableArrayList("CANDIDAT", "AGENT RH", "EMPLOYEE"));
    }

    @FXML
    private void handleLogin() {
        String role = loginRole.getValue();
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText().trim();

        if (role == null || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur loggedUser = utilisateurCRUD.findByEmail(email);

        if (loggedUser != null) {
            // Vérifier le mot de passe hashé
            if (!BCrypt.checkpw(password, loggedUser.getMotPasse())) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect !");
                return;
            }

            // Vérifier le rôle
            boolean roleMatch =
                    (role.equals("CANDIDAT") && loggedUser.getIdProfil() == 1) ||
                            (role.equals("AGENT RH") && loggedUser.getIdProfil() == 2) ||
                            (role.equals("EMPLOYEE") && loggedUser.getIdProfil() == 3);

            if (!roleMatch) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Rôle incorrect !");
                return;
            }

            // Connecter l'utilisateur
            Session.setLoggedUser(loggedUser);

            // Redirection selon rôle
            switch (role) {
                case "AGENT RH" -> MainApp.showGestionEmployee();
            }

        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect !");
        }
    }

    @FXML
    private void goToCandidateSignup() {
        MainApp.showCandidateSignup();
    }

    @FXML
    private void goToRhSignup() {
        MainApp.showRhSignup();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
