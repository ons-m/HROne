package com.recruitx.hrone.gui.controllers;

import com.recruitx.hrone.entities.Entreprise;
import com.recruitx.hrone.entities.Utilisateur;
import com.recruitx.hrone.services.EntrepriseCRUD;
import com.recruitx.hrone.services.UtilisateurCRUD;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class UsersDashboardController {

    // --- Menus ---
    @FXML private VBox recruitmentSubmenu;
    @FXML private VBox adminSubmenu;

    @FXML
    private void toggleRecruitmentMenu() {
        boolean visible = recruitmentSubmenu.isVisible();
        recruitmentSubmenu.setVisible(!visible);
        recruitmentSubmenu.setManaged(!visible);
    }

    @FXML
    private void toggleAdminMenu() {
        boolean visible = adminSubmenu.isVisible();
        adminSubmenu.setVisible(!visible);
        adminSubmenu.setManaged(!visible);
    }

    // --- User Form ---
    @FXML private TextField userEmail;
    @FXML private TextField userAddress;
    @FXML private TextField userPhone;
    @FXML private TextField userCIN;
    @FXML private DatePicker userDOB;
    @FXML private ComboBox<String> genderSelect;
    @FXML private ComboBox<Entreprise> companySelect;
    @FXML private Label userFormStatus;
    @FXML private VBox userList;

    // --- Company Form ---
    @FXML private TextField companyNameField;
    @FXML private TextField companyRefField;
    @FXML private Label companyFormStatus;
    @FXML private VBox companyList;

    // --- Services ---
    private final UtilisateurCRUD userService = new UtilisateurCRUD();
    private final EntrepriseCRUD companyService = new EntrepriseCRUD();

    @FXML
    private void initialize() {
        // Gender options
        genderSelect.setItems(FXCollections.observableArrayList("H", "F", "A"));

        // Configure company ComboBox display
        companySelect.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Entreprise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNomEntreprise());
            }
        });
        companySelect.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Entreprise item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getNomEntreprise());
            }
        });

        // Load initial data
        refreshCompanyList();
        refreshUserList();
    }

    // --- User Handlers ---
    @FXML
    private void handleAddUser() {
        if (userEmail.getText().isEmpty() || userAddress.getText().isEmpty() ||
                userPhone.getText().isEmpty() || userCIN.getText().isEmpty() ||
                userDOB.getValue() == null || genderSelect.getValue() == null ||
                companySelect.getValue() == null) {

            userFormStatus.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            Utilisateur user = new Utilisateur();
            user.setEmail(userEmail.getText());
            user.setAdresse(userAddress.getText());
            user.setNumTel(userPhone.getText());
            user.setCin(userCIN.getText());
            LocalDate dob = userDOB.getValue();
            user.setDateNaissance(Date.from(dob.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            user.setGender(genderSelect.getValue().charAt(0));
            user.setIdEntreprise(companySelect.getValue().getIdEntreprise());
            user.setIdProfil(2); // default RH profile
            user.setNomUtilisateur(userEmail.getText());
            user.setMotPasse("password"); // default password
            user.setNumOrdreSignIn(0);

            userService.create(user);
            userFormStatus.setText("Utilisateur ajouté avec succès !");
            clearUserForm();
            refreshUserList();

        } catch (Exception e) {
            userFormStatus.setText("Erreur lors de l'ajout !");
            e.printStackTrace();
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

    private void refreshUserList() {
        userList.getChildren().clear();
        ObservableList<Utilisateur> users = FXCollections.observableArrayList(userService.getAll());
        for (Utilisateur u : users) {
            Entreprise c = companyService.getById(u.getIdEntreprise());
            String companyName = (c != null) ? c.getNomEntreprise() : "N/A";
            Label lbl = new Label(u.getEmail() + " - " + companyName);
            userList.getChildren().add(lbl);
        }
    }

    // --- Company Handlers ---
    @FXML
    private void handleAddCompany() {
        if (companyNameField.getText().isEmpty() || companyRefField.getText().isEmpty()) {
            companyFormStatus.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            Entreprise company = new Entreprise();
            company.setNomEntreprise(companyNameField.getText());
            company.setReference(companyRefField.getText());

            companyService.create(company);
            companyFormStatus.setText("Entreprise ajoutée avec succès !");
            clearCompanyForm();
            refreshCompanyList();
        } catch (Exception e) {
            companyFormStatus.setText("Erreur lors de l'ajout !");
            e.printStackTrace();
        }
    }

    private void clearCompanyForm() {
        companyNameField.clear();
        companyRefField.clear();
    }

    private void refreshCompanyList() {
        companyList.getChildren().clear();
        ObservableList<Entreprise> companies = FXCollections.observableArrayList(companyService.getAll());
        companySelect.setItems(companies);
        for (Entreprise c : companies) {
            Label lbl = new Label(c.getNomEntreprise() + " - " + c.getReference());
            companyList.getChildren().add(lbl);
        }
    }
}
