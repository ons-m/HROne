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

        employeesTable.setItems(sampleRows());
    }

    @FXML private void handleOpenAddModal() {
        setAddMode();
        showModal();
    }

    @FXML private void handleCloseModal(){
        hideModal();
    }

    @FXML private void handleConfirm() {
        if (currentMode == Mode.ADD) {
            System.out.println("Add Employee logic here");
        } else {
            System.out.println("Update Employee logic here");
        }
    }

    private void onEdit(EmployeeRow row) {

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

                // Later: EmployeeController.Supprimer(id);
                // loadData();
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

    private void clearForm(){

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

    private ObservableList<EmployeeRow> sampleRows() {
        return FXCollections.observableArrayList(
            new EmployeeRow("Sarah El Haddadi","sarah@hrone.ma",  "12/05/1995", "24 j", "18 j", "160 h"),
            new EmployeeRow("Mehdi Bensalem", "mehdi@hrone.ma", "03/11/1992", "20 j", "12 j", "172 h")
        );
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

//        public EmployeeRow(Employe e){
//
//        }

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
