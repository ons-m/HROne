package com.recruitx.hrone;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FrmFicheEmployeeController {

    private enum Mode{
        ADD,
        EDIT
    }

    private Mode currentMode = Mode.ADD;
    private int editingId = 0;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField soldField;
    @FXML private TextField hoursField;
    @FXML private TextField macField;
    @FXML private TextField salaryField;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private FrmGestionEmployeeController FrmParent;

    public void setEditMode(int employeeId) {
        this.currentMode = Mode.EDIT;
        this.editingId = employeeId;

        confirmButton.setText("Valider");
        cancelButton.setText("Annuler");

        Employe e = EmployeController.AvoirEntite(editingId);

        if (e != null) {

            // ---- STATIC utilisateur fields ----
            nameField.setText("User1");
            emailField.setText("email@static.com");
            maleRadio.setSelected(true); // or false depending on your static choice
            birthDatePicker.setValue(java.time.LocalDate.of(1990,1,1));

            // ---- Real EMPLOYEE fields ----
            soldField.setText(String.valueOf(e.getSolde_Conger()));
            hoursField.setText(String.valueOf(e.getNbr_Heure_De_Travail()));
            macField.setText(e.getMac_Machine());
            salaryField.setText(String.valueOf(e.getSaliare()));
        }

    }
    public void setAddMode() {
        currentMode = Mode.ADD;
        editingId = 0;

        confirmButton.setText("Valider");
        cancelButton.setText("Annuler");

        clearForm();
    }
    private void clearForm() {
        nameField.clear();
        emailField.clear();
        maleRadio.setSelected(false);
        femaleRadio.setSelected(false);
        birthDatePicker.setValue(null);
        soldField.clear();
        hoursField.clear();
        macField.clear();
        salaryField.clear();
    }
    public void setFrmParent(FrmGestionEmployeeController parent){
        this.FrmParent = parent;
    }

    @FXML private void initialize(){

        ToggleGroup genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);


    }

    @FXML private void handleConfirm() {

        String validationError = validateForm();

        if (validationError != null) {
            showError(validationError);
            return;
        }

        int solde = Integer.parseInt(soldField.getText().trim());
        int hours = Integer.parseInt(hoursField.getText().trim());
        int salaire = Integer.parseInt(salaryField.getText().trim());
        String mac = macField.getText().trim();

        if (currentMode == Mode.ADD) {

            Employe e = new Employe(
                    0,              // ID_EMPLOYE (auto)
                    1,              // ID_USER (temporary static)
                    solde,
                    hours,
                    mac,
                    salaire
            );

            boolean success = EmployeController.Ajouter(e);

            if (success) {
                FrmParent.hideModal();
            } else {
                showError("Erreur lors de l'enregistrement.");
            }
        } else {

            Employe e = new Employe(
                    editingId,
                    1,
                    solde,
                    hours,
                    mac,
                    salaire
            );

            boolean success = EmployeController.Modifier(e);
            if (success) {
                FrmParent.hideModal();
            } else {
                showError("Erreur lors de l'enregistrement.");
            }
        }
    }
    @FXML private void handleCancle(){
        FrmParent.hideModal();
    }

    private String validateForm() {

        // NAME
        String name = nameField.getText().trim();
        if (name.length() < 3) {
            return "Le nom doit contenir au moins 3 caractères.";
        }

        // EMAIL
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            return "L'email est obligatoire.";
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Format d'email invalide.";
        }

        // GENDER
        if (!maleRadio.isSelected() && !femaleRadio.isSelected()) {
            return "Veuillez sélectionner un genre.";
        }

        // BIRTH DATE
        if (birthDatePicker.getValue() == null) {
            return "La date de naissance est obligatoire.";
        }
        if (birthDatePicker.getValue().isAfter(java.time.LocalDate.now())) {
            return "La date de naissance ne peut pas être dans le futur.";
        }

        // SOLDE
        if (!isPositiveInteger(soldField.getText())) {
            return "Le solde de congé doit être un nombre entier supérieur à 0.";
        }

        // HOURS
        if (!isPositiveInteger(hoursField.getText())) {
            return "Le nombre d'heures doit être un entier supérieur à 0.";
        }

        // SALAIRE
        if (!isPositiveInteger(salaryField.getText())) {
            return "Le salaire doit être un entier supérieur à 0.";
        }

        // MAC
        String mac = macField.getText().trim();
        if (mac.isEmpty()) {
            return "L'adresse MAC est obligatoire.";
        }
        if (!mac.matches("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$")) {
            return "Format MAC invalide. Exemple : AA:BB:CC:DD:EE:FF";
        }

        return null; // valid
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText("Données invalides");
        alert.setContentText(message);
        alert.showAndWait();
    }
    private boolean isPositiveInteger(String value) {
        try {
            int number = Integer.parseInt(value.trim());
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
