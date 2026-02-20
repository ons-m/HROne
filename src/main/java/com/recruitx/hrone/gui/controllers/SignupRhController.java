package com.recruitx.hrone.gui.controllers;

import com.recruitx.hrone.entities.Entreprise;
import com.recruitx.hrone.entities.Utilisateur;
import com.recruitx.hrone.gui.MainApp;
import com.recruitx.hrone.services.EntrepriseCRUD;
import com.recruitx.hrone.services.UtilisateurCRUD;
import com.recruitx.hrone.utils.COrdre;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class SignupRhController {

    // ---------------- RH Infos ----------------
    @FXML private TextField rhUsername;
    @FXML private PasswordField rhPassword;
    @FXML private TextField rhEmail;
    @FXML private TextField rhAddress;
    @FXML private TextField rhPhone;
    @FXML private TextField rhCIN;
    @FXML private DatePicker rhBirth;
    @FXML private ComboBox<String> rhGender;

    // ---------------- Entreprise Infos ----------------
    @FXML private TextField companyName;
    @FXML private TextField companyReference;

    private final UtilisateurCRUD utilisateurCRUD = new UtilisateurCRUD();
    private final EntrepriseCRUD entrepriseCRUD = new EntrepriseCRUD();

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    @FXML
    public void initialize() {
        rhGender.setItems(FXCollections.observableArrayList("Homme", "Femme"));
    }

    @FXML
    private void handleSignupRh() {
        String username = rhUsername.getText().trim();
        String password = rhPassword.getText().trim();
        String email = rhEmail.getText().trim();
        String address = rhAddress.getText().trim();
        String phone = rhPhone.getText().trim();
        String cin = rhCIN.getText().trim();
        LocalDate birth = rhBirth.getValue();
        String gender = rhGender.getValue();

        String compName = companyName.getText().trim();
        String compRef = companyReference.getText().trim();

        // ================= VALIDATIONS =================
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() ||
                address.isEmpty() || phone.isEmpty() || cin.isEmpty() ||
                birth == null || gender == null ||
                compName.isEmpty() || compRef.isEmpty()) {

            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Veuillez remplir tous les champs.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email invalide !");
            return;
        }

        if (password.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Le mot de passe doit contenir au moins 8 caractères !");
            return;
        }

        if (!phone.matches("\\d{8}") || !cin.matches("\\d{8}")) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Téléphone et CIN doivent contenir exactement 8 chiffres !");
            return;
        }

        // ================= CHECK UNIQUENESS =================
        if (utilisateurCRUD.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Cet email est déjà utilisé !");
            return;
        }

        if (utilisateurCRUD.cinExists(cin)) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Ce CIN est déjà utilisé !");
            return;
        }

        if (entrepriseCRUD.referenceExists(compRef)) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Cette référence entreprise est déjà utilisée !");
            return;
        }

        try {
            // ================= CREATE ENTREPRISE =================
            Entreprise entreprise = new Entreprise();
            entreprise.setNomEntreprise(compName);
            entreprise.setReference(compRef);
            entrepriseCRUD.create(entreprise);

            int entrepriseId = entreprise.getIdEntreprise();

            // ================= HASH PASSWORD =================
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

            // ================= CREATE USER =================
            Utilisateur rh = new Utilisateur();
            rh.setNomUtilisateur(username);
            rh.setMotPasse(hashedPassword);
            rh.setEmail(email);
            rh.setAdresse(address);
            rh.setNumTel(phone);
            rh.setCin(cin);
            rh.setGender(gender.charAt(0));
            rh.setDateNaissance(java.sql.Date.valueOf(birth));
            rh.setIdProfil(2); // RH
            rh.setIdEntreprise(entrepriseId);
            rh.setNumOrdreSignIn((int) COrdre.GetNumOrdreNow());
            rh.setFirstLogin(0);

            utilisateurCRUD.create(rh);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Compte RH et Entreprise créés avec succès !");

            MainApp.showLoginScreen();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue : " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        MainApp.showLoginScreen();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
