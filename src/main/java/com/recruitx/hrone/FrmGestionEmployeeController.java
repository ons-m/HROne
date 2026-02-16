package com.recruitx.hrone;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;

import java.util.List;

public class FrmGestionEmployeeController {

    private enum Mode {
        ADD,
        UPDATE
    }

    @FXML private TableView<EmployeeRow> employeesTable;

    @FXML private TableColumn<EmployeeRow, Boolean> selectColumn;

    @FXML private TableColumn<EmployeeRow, String> nameColumn;

    @FXML private TableColumn<EmployeeRow, String> emailColumn;

    @FXML private TableColumn<EmployeeRow, String> birthColumn;

    @FXML private TableColumn<EmployeeRow, String> soldColumn;

    @FXML private TableColumn<EmployeeRow, String> remainingColumn;

    @FXML private TableColumn<EmployeeRow, String> hoursColumn;

    @FXML private TableColumn<EmployeeRow, String> actionsColumn;

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private RadioButton maleRadio;
    @FXML private RadioButton femaleRadio;
    @FXML private DatePicker birthDatePicker;
    @FXML private TextField soldField;
    @FXML private TextField hoursField;
    @FXML private TextField macField;
    @FXML private TextField salaryField;

    @FXML private StackPane modalOverlay;
    @FXML private HBox mainContent;

    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private Mode currentMode = Mode.ADD;
    private int editingId = 0;

    @FXML private void initialize() {
        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        birthColumn.setCellValueFactory(data -> data.getValue().birthProperty());
        soldColumn.setCellValueFactory(data -> data.getValue().soldProperty());
        remainingColumn.setCellValueFactory(data -> data.getValue().remainingProperty());
        hoursColumn.setCellValueFactory(data -> data.getValue().hoursProperty());
        actionsColumn.setCellFactory(column -> createActionsCell());

        loadData();

        ToggleGroup genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);
    }

    @FXML private void handleOpenAddModal() {
        setAddMode();
        showModal();
    }

    @FXML private void handleCloseModal(){
        hideModal();
    }

    @FXML
    private void handleConfirm() {

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
                loadData();
                hideModal();
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
                loadData();
                hideModal();
            } else {
                showError("Erreur lors de l'enregistrement.");
            }
        }

        hideModal();
    }

    private void loadData() {

        ObservableList<EmployeeRow> rows = FXCollections.observableArrayList();

        List<Employe> list = EmployeController.AvoirListe();
        System.out.println("DB returned: " + (list == null ? "null" : list.size() + " rows"));
        if (list != null) {
            for (Employe e : list) {

                rows.add(new EmployeeRow(e));
            }
        }

        employeesTable.setItems(rows);
    }


    private void onEdit(EmployeeRow row) {

        editingId = row.getId();
        setUpdateMode(editingId);

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

        showModal();
    }



    private void onDelete(int id) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'employé");
        confirm.setContentText("Voulez-vous vraiment supprimer cet employé ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                System.out.println("Delete employee with id: " + id);

                EmployeController.Supprimer(id);
                loadData();
            }
        });
    }

    private void setAddMode() {
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


    private void setUpdateMode(int id) {
        currentMode = Mode.UPDATE;
        editingId = id;

        confirmButton.setText("Valider");
        cancelButton.setText("Annuler");
    }

    private void showModal() {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);

        mainContent.setEffect(new javafx.scene.effect.GaussianBlur(10));
    }

    private void hideModal() {
        clearForm();
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        mainContent.setEffect(null);

        editingId = 0;
        currentMode = Mode.ADD;
    }

    private TableCell<EmployeeRow, String> createActionsCell() {

        return new TableCell<>() {

            private final MenuItem edit = new MenuItem("Modifier");
            private final MenuItem delete = new MenuItem("Supprimer");
            private final MenuButton menuButton = new MenuButton("...", null, edit, delete);

            {
                menuButton.getStyleClass().add("icon-btn");

                edit.setOnAction(event -> {
                    EmployeeRow row = getTableView().getItems().get(getIndex());
                    onEdit(row);
                });

                delete.setOnAction(event -> {
                    EmployeeRow row = getTableView().getItems().get(getIndex());
                    onDelete(row.getId());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(menuButton);
                }
            }
        };
    }

    private String validateForm() {

        // NAME
        String name = nameField.getText().trim();
        if (name.isEmpty() || name.length() < 3) {
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

    private boolean isPositiveInteger(String value) {
        try {
            int number = Integer.parseInt(value.trim());
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur de validation");
        alert.setHeaderText("Données invalides");
        alert.setContentText(message);
        alert.showAndWait();
    }


    public static class EmployeeRow {
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty birth = new SimpleStringProperty();
        private final StringProperty sold = new SimpleStringProperty();
        private final StringProperty remaining = new SimpleStringProperty();
        private final StringProperty hours = new SimpleStringProperty();
        private final int id;

        public EmployeeRow(String name, String email, String birth, String sold, String remaining, String hours) {
            this.name.set(name);
            this.email.set(email);
            this.birth.set(birth);
            this.sold.set(sold);
            this.remaining.set(remaining);
            this.hours.set(hours);
            this.id = 0;
        }

        public EmployeeRow(int id ,String name, String email, String birth, String sold, String remaining, String hours) {
            this.name.set(name);
            this.email.set(email);
            this.birth.set(birth);
            this.sold.set(sold);
            this.remaining.set(remaining);
            this.hours.set(hours);
            this.id = id;
        }

        public EmployeeRow(Employe e){
            this.name.set("User1");
            this.email.set("email@static.com");
            this.birth.set("-");
            this.sold.set(String.valueOf(e.getSolde_Conger()));
            this.remaining.set(String.valueOf(e.getSolde_Conger()));
            this.hours.set(String.valueOf(e.getNbr_Heure_De_Travail()));
            this.id = e.getID_Employe();
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty emailProperty() {
            return email;
        }

        public StringProperty birthProperty() {
            return birth;
        }

        public StringProperty soldProperty() {
            return sold;
        }

        public StringProperty remainingProperty() {
            return remaining;
        }

        public StringProperty hoursProperty() {
            return hours;
        }

        public int getId () {return id;}
    }
}
