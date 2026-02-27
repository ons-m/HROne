package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Entreprise;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Repository.EntrepriseRepository;
import com.recruitx.hrone.Repository.UtilisateurRepository;
import com.recruitx.hrone.Utils.COrdre;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class FrmUserDashboard {

    @FXML private TextField userEmail;
    @FXML private TextField userAddress;
    @FXML private TextField userPhone;
    @FXML private TextField userCIN;
    @FXML private DatePicker userDOB;
    @FXML private ComboBox<String> genderSelect;
    @FXML private ComboBox<Entreprise> companySelect;
    @FXML private Label userFormStatus;

    @FXML private TextField companyNameField;
    @FXML private TextField companyRefField;
    @FXML private Label companyFormStatus;

    @FXML private VBox companyList;
    @FXML private VBox userList;

    private final UtilisateurRepository utilisateurRepository = new UtilisateurRepository();
    private final EntrepriseRepository entrepriseRepository = new EntrepriseRepository();

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[A-Za-z0-9]{6,20}$");

    @FXML
    private void initialize() {
        genderSelect.setItems(FXCollections.observableArrayList("Homme", "Femme"));

        companySelect.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Entreprise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomEntreprise() + " (" + item.getReference() + ")");
            }
        });

        companySelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Entreprise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomEntreprise() + " (" + item.getReference() + ")");
            }
        });

        refreshCompanies();
        refreshUsers();
    }

    @FXML
    private void handleAddUser() {
        String email = userEmail.getText().trim();
        String address = userAddress.getText().trim();
        String phone = userPhone.getText().trim();
        String cin = userCIN.getText().trim();
        LocalDate dob = userDOB.getValue();
        String gender = genderSelect.getValue();
        Entreprise selectedCompany = companySelect.getValue();

        if (email.isEmpty() || address.isEmpty() || phone.isEmpty() || cin.isEmpty() || dob == null || gender == null || selectedCompany == null) {
            setStatus(userFormStatus, "Veuillez remplir tous les champs utilisateur.", false);
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            setStatus(userFormStatus, "Email invalide.", false);
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            setStatus(userFormStatus, "Téléphone invalide (8 à 15 chiffres).", false);
            return;
        }

        if (!CIN_PATTERN.matcher(cin).matches()) {
            setStatus(userFormStatus, "CIN invalide (6 à 20 caractères alphanumériques).", false);
            return;
        }

        if (utilisateurRepository.emailExists(email)) {
            setStatus(userFormStatus, "Cet email est déjà utilisé.", false);
            return;
        }

        if (utilisateurRepository.cinExists(cin)) {
            setStatus(userFormStatus, "Ce CIN est déjà utilisé.", false);
            return;
        }

        try {
            Utilisateur user = new Utilisateur();
            user.setEmail(email);
            user.setAdresse(address);
            user.setNumTel(phone);
            user.setCin(cin);
            user.setDateNaissance(java.sql.Date.valueOf(dob));
            user.setGender(gender.charAt(0));
            user.setIdEntreprise(selectedCompany.getIdEntreprise());
            user.setIdProfil(2);
            user.setNomUtilisateur(email.split("@")[0]);
            user.setMotPasse(BCrypt.hashpw(cin, BCrypt.gensalt(12)));
            user.setNumOrdreSignIn((int) COrdre.GetNumOrdreNow());
            user.setFirstLogin(1);

            utilisateurRepository.create(user);

            clearUserForm();
            refreshUsers();
            setStatus(userFormStatus, "Utilisateur ajouté avec succès.", true);
        } catch (Exception ex) {
            setStatus(userFormStatus, "Erreur lors de l'ajout utilisateur: " + ex.getMessage(), false);
        }
    }

    @FXML
    private void handleAddCompany() {
        String name = companyNameField.getText().trim();
        String reference = companyRefField.getText().trim();

        if (name.isEmpty() || reference.isEmpty()) {
            setStatus(companyFormStatus, "Veuillez remplir le nom et la référence de l'entreprise.", false);
            return;
        }

        if (entrepriseRepository.referenceExists(reference)) {
            setStatus(companyFormStatus, "Cette référence entreprise existe déjà.", false);
            return;
        }

        try {
            Entreprise entreprise = new Entreprise();
            entreprise.setNomEntreprise(name);
            entreprise.setReference(reference);

            entrepriseRepository.create(entreprise);

            clearCompanyForm();
            refreshCompanies();
            setStatus(companyFormStatus, "Entreprise ajoutée avec succès.", true);
        } catch (Exception ex) {
            setStatus(companyFormStatus, "Erreur lors de l'ajout entreprise: " + ex.getMessage(), false);
        }
    }

    @FXML
    private void handleResetUserForm() {
        clearUserForm();
        userFormStatus.setText("");
    }

    @FXML
    private void handleResetCompanyForm() {
        clearCompanyForm();
        companyFormStatus.setText("");
    }

    private void refreshCompanies() {
        List<Entreprise> entreprises = entrepriseRepository.getAll();
        companySelect.setItems(FXCollections.observableArrayList(entreprises));

        companyList.getChildren().clear();
        for (Entreprise entreprise : entreprises) {
            VBox row = new VBox();
            row.getStyleClass().add("resource-item");

            Label title = new Label(entreprise.getNomEntreprise());
            title.getStyleClass().add("resource-title");

            Label meta = new Label("Ref: " + entreprise.getReference() + " • ID: " + entreprise.getIdEntreprise());
            meta.getStyleClass().add("resource-meta");

            HBox actions = new HBox(8);
            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().addAll("btn", "btn-small", "btn-primary");
            editBtn.setOnAction(e -> handleEditCompany(entreprise));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.getStyleClass().addAll("btn", "btn-small", "btn-danger");
            deleteBtn.setOnAction(e -> handleDeleteCompany(entreprise));
            actions.getChildren().addAll(editBtn, deleteBtn);

            row.getChildren().addAll(title, meta, actions);
            companyList.getChildren().add(row);
        }
    }

    private void refreshUsers() {
        List<Utilisateur> users = utilisateurRepository.getAll();
        List<Entreprise> entreprises = entrepriseRepository.getAll();

        userList.getChildren().clear();
        for (Utilisateur user : users) {
            String companyName = entreprises.stream()
                    .filter(e -> e.getIdEntreprise() == user.getIdEntreprise())
                    .map(Entreprise::getNomEntreprise)
                    .findFirst()
                    .orElse("Entreprise inconnue");

            VBox row = new VBox();
            row.getStyleClass().add("resource-item");

            Label title = new Label(user.getEmail());
            title.getStyleClass().add("resource-title");

            Label meta = new Label("CIN: " + user.getCin() + " • Entreprise: " + companyName + " • Rôle ID: " + user.getIdProfil());
            meta.getStyleClass().add("resource-meta");

            HBox actions = new HBox(8);
            Button editBtn = new Button("Modifier");
            editBtn.getStyleClass().addAll("btn", "btn-small", "btn-primary");
            editBtn.setOnAction(e -> handleEditUser(user));

            Button deleteBtn = new Button("Supprimer");
            deleteBtn.getStyleClass().addAll("btn", "btn-small", "btn-danger");
            deleteBtn.setOnAction(e -> handleDeleteUser(user));
            actions.getChildren().addAll(editBtn, deleteBtn);

            row.getChildren().addAll(title, meta, actions);
            userList.getChildren().add(row);
        }
    }

    private void handleDeleteUser(Utilisateur user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur");
        confirm.setContentText("Voulez-vous supprimer " + user.getEmail() + " ?");

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                utilisateurRepository.delete(user.getIdUtilisateur());
                refreshUsers();
                setStatus(userFormStatus, "Utilisateur supprimé avec succès.", true);
            } catch (Exception ex) {
                setStatus(userFormStatus, "Erreur suppression utilisateur: " + ex.getMessage(), false);
            }
        }
    }

    private void handleDeleteCompany(Entreprise entreprise) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'entreprise");
        confirm.setContentText("Voulez-vous supprimer " + entreprise.getNomEntreprise() + " ?");

        Optional<ButtonType> choice = confirm.showAndWait();
        if (choice.isPresent() && choice.get() == ButtonType.OK) {
            try {
                entrepriseRepository.delete(entreprise.getIdEntreprise());
                refreshCompanies();
                refreshUsers();
                setStatus(companyFormStatus, "Entreprise supprimée avec succès.", true);
            } catch (Exception ex) {
                setStatus(companyFormStatus, "Suppression impossible (entreprise liée à des utilisateurs): " + ex.getMessage(), false);
            }
        }
    }

    private void handleEditCompany(Entreprise original) {
        Dialog<Entreprise> dialog = new Dialog<>();
        dialog.setTitle("Modifier entreprise");
        dialog.setHeaderText("Mettre à jour les informations");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField nameField = new TextField(original.getNomEntreprise());
        TextField refField = new TextField(original.getReference());

        grid.add(new Label("Nom"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Référence"), 0, 1);
        grid.add(refField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveBtn) {
                Entreprise updated = new Entreprise();
                updated.setIdEntreprise(original.getIdEntreprise());
                updated.setNomEntreprise(nameField.getText().trim());
                updated.setReference(refField.getText().trim());
                return updated;
            }
            return null;
        });

        Optional<Entreprise> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        Entreprise updated = result.get();
        if (updated.getNomEntreprise().isEmpty() || updated.getReference().isEmpty()) {
            setStatus(companyFormStatus, "Nom et référence sont obligatoires.", false);
            return;
        }

        if (!updated.getReference().equalsIgnoreCase(original.getReference())
                && entrepriseRepository.referenceExists(updated.getReference())) {
            setStatus(companyFormStatus, "Cette référence entreprise existe déjà.", false);
            return;
        }

        try {
            entrepriseRepository.update(updated);
            refreshCompanies();
            refreshUsers();
            setStatus(companyFormStatus, "Entreprise modifiée avec succès.", true);
        } catch (Exception ex) {
            setStatus(companyFormStatus, "Erreur modification entreprise: " + ex.getMessage(), false);
        }
    }

    private void handleEditUser(Utilisateur original) {
        List<Entreprise> entreprises = entrepriseRepository.getAll();
        if (entreprises.isEmpty()) {
            setStatus(userFormStatus, "Aucune entreprise disponible pour modifier l'utilisateur.", false);
            return;
        }

        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Modifier utilisateur");
        dialog.setHeaderText("Mettre à jour les informations");

        ButtonType saveBtn = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField emailField = new TextField(original.getEmail());
        TextField addressField = new TextField(original.getAdresse());
        TextField phoneField = new TextField(original.getNumTel());
        TextField cinField = new TextField(original.getCin());
        DatePicker dobPicker = new DatePicker(original.getDateNaissance() == null
                ? null
                : new java.sql.Date(original.getDateNaissance().getTime()).toLocalDate());

        ComboBox<String> genderBox = new ComboBox<>(FXCollections.observableArrayList("Homme", "Femme"));
        genderBox.setValue((original.getGender() == 'F' || original.getGender() == 'f') ? "Femme" : "Homme");

        ComboBox<Entreprise> companyBox = new ComboBox<>(FXCollections.observableArrayList(entreprises));
        companyBox.setCellFactory(cb -> new ListCell<>() {
            @Override
            protected void updateItem(Entreprise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomEntreprise() + " (" + item.getReference() + ")");
            }
        });
        companyBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Entreprise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomEntreprise() + " (" + item.getReference() + ")");
            }
        });
        companyBox.setValue(entreprises.stream()
                .filter(e -> e.getIdEntreprise() == original.getIdEntreprise())
                .findFirst()
                .orElse(entreprises.get(0)));

        grid.add(new Label("Email"), 0, 0);
        grid.add(emailField, 1, 0);
        grid.add(new Label("Adresse"), 0, 1);
        grid.add(addressField, 1, 1);
        grid.add(new Label("Téléphone"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(new Label("CIN"), 0, 3);
        grid.add(cinField, 1, 3);
        grid.add(new Label("Date naissance"), 0, 4);
        grid.add(dobPicker, 1, 4);
        grid.add(new Label("Genre"), 0, 5);
        grid.add(genderBox, 1, 5);
        grid.add(new Label("Entreprise"), 0, 6);
        grid.add(companyBox, 1, 6);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == saveBtn) {
                Utilisateur updated = new Utilisateur();
                updated.setIdUtilisateur(original.getIdUtilisateur());
                updated.setEmail(emailField.getText().trim());
                updated.setAdresse(addressField.getText().trim());
                updated.setNumTel(phoneField.getText().trim());
                updated.setCin(cinField.getText().trim());
                updated.setDateNaissance(dobPicker.getValue() == null ? null : java.sql.Date.valueOf(dobPicker.getValue()));
                updated.setGender(genderBox.getValue() == null ? original.getGender() : genderBox.getValue().charAt(0));
                updated.setIdEntreprise(companyBox.getValue() == null ? original.getIdEntreprise() : companyBox.getValue().getIdEntreprise());
                updated.setIdProfil(original.getIdProfil());
                updated.setNomUtilisateur(original.getNomUtilisateur());
                updated.setMotPasse(original.getMotPasse());
                updated.setNumOrdreSignIn(original.getNumOrdreSignIn());
                updated.setFirstLogin(original.getFirstLogin());
                return updated;
            }
            return null;
        });

        Optional<Utilisateur> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        Utilisateur updated = result.get();
        if (updated.getEmail().isEmpty() || updated.getAdresse().isEmpty() || updated.getNumTel().isEmpty() || updated.getCin().isEmpty()) {
            setStatus(userFormStatus, "Email, adresse, téléphone et CIN sont obligatoires.", false);
            return;
        }

        if (!EMAIL_PATTERN.matcher(updated.getEmail()).matches()) {
            setStatus(userFormStatus, "Email invalide.", false);
            return;
        }

        if (!PHONE_PATTERN.matcher(updated.getNumTel()).matches()) {
            setStatus(userFormStatus, "Téléphone invalide (8 à 15 chiffres).", false);
            return;
        }

        if (!CIN_PATTERN.matcher(updated.getCin()).matches()) {
            setStatus(userFormStatus, "CIN invalide (6 à 20 caractères alphanumériques).", false);
            return;
        }

        Utilisateur userWithSameEmail = utilisateurRepository.findByEmail(updated.getEmail());
        if (userWithSameEmail != null && userWithSameEmail.getIdUtilisateur() != original.getIdUtilisateur()) {
            setStatus(userFormStatus, "Cet email est déjà utilisé.", false);
            return;
        }

        if (!updated.getCin().equalsIgnoreCase(original.getCin()) && utilisateurRepository.cinExists(updated.getCin())) {
            setStatus(userFormStatus, "Ce CIN est déjà utilisé.", false);
            return;
        }

        try {
            utilisateurRepository.update(updated);
            refreshUsers();
            setStatus(userFormStatus, "Utilisateur modifié avec succès.", true);
        } catch (Exception ex) {
            setStatus(userFormStatus, "Erreur modification utilisateur: " + ex.getMessage(), false);
        }
    }

    private void clearUserForm() {
        userEmail.clear();
        userAddress.clear();
        userPhone.clear();
        userCIN.clear();
        userDOB.setValue(null);
        genderSelect.setValue(null);
        companySelect.setValue(null);
    }

    private void clearCompanyForm() {
        companyNameField.clear();
        companyRefField.clear();
    }

    private void setStatus(Label label, String message, boolean success) {
        label.setText(message);
        label.getStyleClass().removeAll("status-success", "status-error");
        label.getStyleClass().add(success ? "status-success" : "status-error");
    }
}


