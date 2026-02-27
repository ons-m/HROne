package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Entreprise;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Repository.EntrepriseRepository;
import com.recruitx.hrone.Repository.UtilisateurRepository;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.mindrot.jbcrypt.BCrypt;

public class FrmLogin implements NavigationAware{

    @FXML private TextField loginEmail;
    @FXML private PasswordField loginPassword;

    private final UtilisateurRepository utilisateurCRUD = new UtilisateurRepository();

    private FrmMain mainController;

    @Override
    public void setMainController(FrmMain mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
    }

    @FXML
    private void handleLogin() {
        String email = loginEmail.getText().trim();
        String password = loginPassword.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur loggedUser = utilisateurCRUD.findByEmail(email);

        if (loggedUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect !");
            return;
        }

        // Vérifier le mot de passe hashé
        if (!BCrypt.checkpw(password, loggedUser.getMotPasse())) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Email ou mot de passe incorrect !");
            return;
        }

        mainController.showSidebar();
        mainController.setCurrentUser(loggedUser,GetUserEntreprise(loggedUser));
        mainController.setVisisiblityByRole();
        switch(loggedUser.getIdProfil()){
            case 1 -> mainController.loadView(FrmMain.ViewType.OFFRES);
            case 2, 3 -> mainController.loadView(FrmMain.ViewType.COMMUNAUTE);
        }


    }

    private Entreprise GetUserEntreprise(Utilisateur user){
        Entreprise currEntreprise;

        EntrepriseRepository entrepriseRepository = new EntrepriseRepository();
        currEntreprise = entrepriseRepository.getById(user.getIdEntreprise());

        if(currEntreprise == null){
           showAlert(Alert.AlertType.ERROR, "Erreur", "Entreprise introuvable pour l'utilisateur !");
        }

        return currEntreprise;
    }

    @FXML
    private void goToCandidateSignup() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.SIGNUPCANDIDAT);
        }
    }

    @FXML
    private void goToRhSignup() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.SIGNUPENTREPRISE);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
