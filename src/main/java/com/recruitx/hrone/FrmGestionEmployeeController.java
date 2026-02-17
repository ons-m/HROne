package com.recruitx.hrone;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.HBox;

import java.util.List;

public class FrmGestionEmployeeController {

    @FXML private TableView<EmployeeRow> employeesTable;
    @FXML private TableColumn<EmployeeRow, Boolean> selectColumn;
    @FXML private TableColumn<EmployeeRow, String> nameColumn;
    @FXML private TableColumn<EmployeeRow, String> emailColumn;
    @FXML private TableColumn<EmployeeRow, String> birthColumn;
    @FXML private TableColumn<EmployeeRow, String> soldColumn;
    @FXML private TableColumn<EmployeeRow, String> remainingColumn;
    @FXML private TableColumn<EmployeeRow, String> hoursColumn;

    @FXML private TableColumn<EmployeeRow, String> actionsColumn;
    @FXML private Node FrmFicheEmployee;
    @FXML private FrmFicheEmployeeController FrmFicheEmployeeController;

    @FXML private HBox mainContent;


    @FXML private void initialize() {

        FrmFicheEmployeeController.setFrmParent(this);

        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        birthColumn.setCellValueFactory(data -> data.getValue().birthProperty());
        soldColumn.setCellValueFactory(data -> data.getValue().soldProperty());
        remainingColumn.setCellValueFactory(data -> data.getValue().remainingProperty());
        hoursColumn.setCellValueFactory(data -> data.getValue().hoursProperty());
        actionsColumn.setCellFactory(column -> createActionsCell());

        RefreshTable();

    }

    private void RefreshTable() {

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
        FrmFicheEmployeeController.setEditMode(row.getId());
        showModal();
    }
    @FXML private void onAdd() {
        FrmFicheEmployeeController.setAddMode();
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
                RefreshTable();
            }
        });
    }

    private void showModal() {
        FrmFicheEmployee.setVisible(true);
        FrmFicheEmployee.setManaged(true);

        mainContent.setEffect(new javafx.scene.effect.GaussianBlur(10));
    }
    public void hideModal() {
        FrmFicheEmployee.setVisible(false);
        FrmFicheEmployee.setManaged(false);

        mainContent.setEffect(null);
        RefreshTable();
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
    public static class EmployeeRow {
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty birth = new SimpleStringProperty();
        private final StringProperty sold = new SimpleStringProperty();
        private final StringProperty remaining = new SimpleStringProperty();
        private final StringProperty hours = new SimpleStringProperty();
        private final int id;

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
