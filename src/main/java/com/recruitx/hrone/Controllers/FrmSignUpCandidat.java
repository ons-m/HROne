package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Entreprise;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Repository.EntrepriseRepository;
import com.recruitx.hrone.Repository.UtilisateurRepository;
import com.recruitx.hrone.Utils.COrdre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class FrmSignUpCandidat implements NavigationAware{

    @FXML private TextField candUsername;
    @FXML private PasswordField candPassword;
    @FXML private TextField candEmail;
    @FXML private TextField candAddress;
    @FXML private TextField candPhone;
    @FXML private TextField candCIN;
    @FXML private DatePicker candBirth;
    @FXML private ComboBox<String> candGender;

    private final UtilisateurRepository utilisateurCRUD = new UtilisateurRepository();
    private ObservableList<Entreprise> entrepriseList;

    private static final int DEFAULT_ENTREPRISE_ID = 1;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private FrmMain mainController;

    @Override
    public void setMainController(FrmMain mainController) {
        this.mainController = mainController;
    }

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {
        candGender.setItems(FXCollections.observableArrayList("Homme", "Femme"));
    }

    // ================= REGISTER CANDIDATE =================
    @FXML
    private void handleRegisterCandidate() {

        String username = candUsername.getText().trim();
        String password = candPassword.getText().trim();
        String email = candEmail.getText().trim();
        String address = candAddress.getText().trim();
        String phone = candPhone.getText().trim();
        String cin = candCIN.getText().trim();
        LocalDate birth = candBirth.getValue();
        String gender = candGender.getValue();

        // ===== VALIDATION =====
        if (username.isEmpty() || password.isEmpty() || email.isEmpty()
                || address.isEmpty() || phone.isEmpty() || cin.isEmpty()
                || birth == null || gender == null) {

            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Veuillez remplir tous les champs.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email invalide !");
            return;
        }

        if (password.length() < 8) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le mot de passe doit contenir au moins 8 caractères !");
            return;
        }

        if (!phone.matches("\\d{8}") || !cin.matches("\\d{8}")) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Le numéro de téléphone et le CIN doivent contenir exactement 8 chiffres !");
            return;
        }

        // ===== CHECK UNIQUENESS =====
        if (utilisateurCRUD.emailExists(email)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Cet Email est déjà utilisé !");
            return;
        }

        if (utilisateurCRUD.cinExists(cin)) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Ce CIN est déjà utilisé !");
            return;
        }

        try {
            // ===== CREATE CANDIDATE =====
            Utilisateur u = new Utilisateur();
            u.setNomUtilisateur(username);

            // 🔐 PASSWORD HASHING
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            u.setMotPasse(hashedPassword);

            u.setEmail(email);
            u.setAdresse(address);
            u.setNumTel(phone);
            u.setCin(cin);
            u.setGender(gender.charAt(0));
            u.setDateNaissance(java.sql.Date.valueOf(birth));
            u.setIdProfil(1); // Candidat
            u.setIdEntreprise(DEFAULT_ENTREPRISE_ID);
            u.setNumOrdreSignIn((int) COrdre.GetNumOrdreNow());
            u.setFirstLogin(0);

            utilisateurCRUD.create(u);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès",
                    "Candidat inscrit avec succès !");

            // Retour à l'écran de login
            if (mainController != null) {
                mainController.loadView(FrmMain.ViewType.LOGIN);
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue : " + e.getMessage());
        }
    }


    @FXML
    private void goToLogin() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.LOGIN);
        }
    }

    private void showAlert(Alert.AlertType type,
                           String title,
                           String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
