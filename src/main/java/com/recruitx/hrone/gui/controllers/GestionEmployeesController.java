package com.recruitx.hrone.gui.controllers;

import com.recruitx.hrone.entities.Utilisateur;
import com.recruitx.hrone.services.UtilisateurCRUD;
import com.recruitx.hrone.utils.COrdre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class GestionEmployeesController {

    // Form fields
    @FXML private TextField empEmail;
    @FXML private TextField empName;
    @FXML private TextField empPrenom;
    @FXML private TextField empAddress;
    @FXML private TextField empPhone;
    @FXML private TextField empCIN;
    @FXML private DatePicker empDOB;
    @FXML private ComboBox<String> empGender;
    @FXML private Label empFormStatus;

    // Table
    @FXML private TableView<Utilisateur> employeeTable;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colEmail;
    @FXML private TableColumn<Utilisateur, String> colTel;
    @FXML private TableColumn<Utilisateur, String> colCIN;
    @FXML private TableColumn<Utilisateur, Date> colDOB;
    @FXML private TableColumn<Utilisateur, Character> colGender;
    @FXML private TableColumn<Utilisateur, Void> colActions;

    private final UtilisateurCRUD userService = new UtilisateurCRUD();
    private int hrCompanyId;

    private Utilisateur selectedUser = null; // for editing

    @FXML
    private void initialize() {
        empGender.setItems(FXCollections.observableArrayList("H", "F", "A"));

        // Setup table columns
        colId.setCellValueFactory(new PropertyValueFactory<>("idUtilisateur"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomUtilisateur"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("numTel"));
        colCIN.setCellValueFactory(new PropertyValueFactory<>("cin"));
        colDOB.setCellValueFactory(new PropertyValueFactory<>("dateNaissance"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));

        setupActionsColumn();
    }

    public void setHrCompanyId(int companyId) {
        this.hrCompanyId = companyId;
        refreshEmployeeTable();
    }

    @FXML
    private void handleAddOrUpdateEmployee() {
        if(empEmail.getText().isEmpty() || empName.getText().isEmpty() || empPrenom.getText().isEmpty() ||
                empAddress.getText().isEmpty() || empPhone.getText().isEmpty() || empCIN.getText().isEmpty() ||
                empDOB.getValue() == null || empGender.getValue() == null) {
            empFormStatus.setText("Veuillez remplir tous les champs !");
            return;
        }

        try {
            if(selectedUser == null) { // Add new
                Utilisateur user = new Utilisateur();
                fillUserFromForm(user);
                user.setIdProfil(3); // Employee
                user.setIdEntreprise(hrCompanyId);
                user.setMotPasse("password");
                user.setNumOrdreSignIn((int) COrdre.GetNumOrdreNow());
                userService.create(user);

                empFormStatus.setText("Employé ajouté avec succès !");
            } else { // Update existing
                fillUserFromForm(selectedUser);
                userService.update(selectedUser);
                empFormStatus.setText("Employé mis à jour !");
                selectedUser = null;
            }

            clearForm();
            refreshEmployeeTable();

        } catch(Exception e) {
            empFormStatus.setText("Erreur lors de l'opération !");
            e.printStackTrace();
        }
    }

    private void fillUserFromForm(Utilisateur user) {
        user.setEmail(empEmail.getText());
        user.setNomUtilisateur(empName.getText() + " " + empPrenom.getText());
        user.setAdresse(empAddress.getText());
        user.setNumTel(empPhone.getText());
        user.setCin(empCIN.getText());
        Date dob = Date.from(empDOB.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
        user.setDateNaissance(dob);
        user.setGender(empGender.getValue().charAt(0));
    }

    private void clearForm() {
        empEmail.clear();
        empName.clear();
        empPrenom.clear();
        empAddress.clear();
        empPhone.clear();
        empCIN.clear();
        empDOB.setValue(null);
        empGender.setValue(null);
        empFormStatus.setText("");
        selectedUser = null;
    }

    private void refreshEmployeeTable() {
        List<Utilisateur> users = userService.getAll();
        ObservableList<Utilisateur> employees = FXCollections.observableArrayList();
        for(Utilisateur u : users) {
            if(u.getIdProfil() == 3 && u.getIdEntreprise() == hrCompanyId) {
                employees.add(u);
            }
        }
        employeeTable.setItems(employees);
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final HBox pane = new HBox(10, editBtn, deleteBtn);

            {
                editBtn.setOnAction(e -> {
                    selectedUser = getTableView().getItems().get(getIndex());
                    fillFormForEdit(selectedUser);
                });
                deleteBtn.setOnAction(e -> {
                    Utilisateur user = getTableView().getItems().get(getIndex());
                    userService.delete(user.getIdUtilisateur());
                    refreshEmployeeTable();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if(empty) setGraphic(null);
                else setGraphic(pane);
            }
        });
    }

    private void fillFormForEdit(Utilisateur user) {
        empEmail.setText(user.getEmail());
        String[] names = user.getNomUtilisateur().split(" ", 2);
        empName.setText(names.length > 0 ? names[0] : "");
        empPrenom.setText(names.length > 1 ? names[1] : "");
        empAddress.setText(user.getAdresse());
        empPhone.setText(user.getNumTel());
        empCIN.setText(user.getCin());
        empDOB.setValue(user.getDateNaissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        empGender.setValue(String.valueOf(user.getGender()));
        empFormStatus.setText("Modification de l'employé...");
    }
}
