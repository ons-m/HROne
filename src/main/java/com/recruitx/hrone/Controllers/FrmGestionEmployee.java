package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Repository.EmployeRepository;
import com.recruitx.hrone.Models.Employe;
import com.recruitx.hrone.Utils.ActionLogger;
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
import javafx.scene.layout.VBox;

import java.util.List;

public class FrmGestionEmployee {

    @FXML private TableView<EmployeeRow> employeesTable;
    @FXML private TableColumn<EmployeeRow, Boolean> selectColumn;
    @FXML private TableColumn<EmployeeRow, String> nameColumn;
    @FXML private TableColumn<EmployeeRow, String> emailColumn;
    @FXML private TableColumn<EmployeeRow, String> birthColumn;
    @FXML private TableColumn<EmployeeRow, String> soldColumn;
    @FXML private TableColumn<EmployeeRow, String> remainingColumn;
    @FXML private TableColumn<EmployeeRow, String> hoursColumn;
    @FXML private TableColumn<EmployeeRow, String> sailaireColumn;

    @FXML private TableColumn<EmployeeRow, String> actionsColumn;
    @FXML private Node FrmFicheEmployee;
    //@FXML private Node FrmAssignToolsDialog;
    @FXML private FrmFicheEmployee FrmFicheEmployeeController;
    //@FXML private FrmAssignTools FrmAssignToolsController;

    @FXML private VBox mainContent;


    @FXML private void initialize() {

        FrmFicheEmployeeController.setFrmParent(this);
        //FrmAssignToolsController.setFrmParent(this);

        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        birthColumn.setCellValueFactory(data -> data.getValue().birthProperty());
        soldColumn.setCellValueFactory(data -> data.getValue().soldProperty());
        remainingColumn.setCellValueFactory(data -> data.getValue().remainingProperty());
        hoursColumn.setCellValueFactory(data -> data.getValue().hoursProperty());
        sailaireColumn.setCellValueFactory(data -> data.getValue().salaireProperty());
        actionsColumn.setCellFactory(column -> createActionsCell());

        RefreshTable();

    }

    private void RefreshTable() {

        ObservableList<EmployeeRow> rows = FXCollections.observableArrayList();

        List<Employe> list = EmployeRepository.AvoirListe();
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

    private void openAssignToolsDialog(int ID_Employee){
        //.setModal(ID_Employee);
        //showAssignToolsModal();
    }

    private void onDelete(int id) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'employé");
        confirm.setContentText("Voulez-vous vraiment supprimer cet employé ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                System.out.println("Delete employee with id: " + id);

                EmployeRepository.Supprimer(id);
                ActionLogger.log("Supprimer Employe", "Suppression employé ID=" + id);
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

//    private void showAssignToolsModal(){
//        FrmAssignToolsDialog.setVisible(true);
//        FrmAssignToolsDialog.setManaged(true);
//
//        mainContent.setEffect(new javafx.scene.effect.GaussianBlur(10));
//    }
//
//    public void hideAssignToolsModal(){
//        FrmAssignToolsDialog.setVisible(false);
//        FrmAssignToolsDialog.setManaged(false);
//
//        mainContent.setEffect(null);
//    }

    private TableCell<EmployeeRow, String> createActionsCell() {

        return new TableCell<>() {

            private final MenuItem edit = new MenuItem("Modifier");
            private final MenuItem delete = new MenuItem("Supprimer");
            private final MenuItem assignTools = new MenuItem("Affecter Outils");

            private final MenuButton menuButton =
                    new MenuButton("...", null, edit, delete, assignTools);

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

                assignTools.setOnAction(event -> {
                    EmployeeRow row = getTableView().getItems().get(getIndex());
                    openAssignToolsDialog(row.getId());
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : menuButton);
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
        private final StringProperty salaire = new SimpleStringProperty();
        private final int id;

        public EmployeeRow(Employe e){
            this.name.set(e.getUser().getNomUtilisateur());
            this.email.set(e.getUser().getEmail());
            this.birth.set(
                    e.getUser().getDateNaissance() != null
                            ? e.getUser().getDateNaissance().toString()
                            : "-"
            );
            this.sold.set(String.valueOf(e.getSolde_Conger()));
            this.remaining.set(String.valueOf(e.getSolde_Conger()));
            this.hours.set(String.valueOf(e.getNbr_Heure_De_Travail()));
            this.id = e.getID_Employe();
            this.salaire.set(String.valueOf(e.getSaliare()));
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

        public StringProperty salaireProperty() { return salaire ;}

        public int getId () {return id;}
    }
}
