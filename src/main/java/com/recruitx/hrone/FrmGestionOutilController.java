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
import javafx.scene.layout.VBox;

import java.util.List;

public class FrmGestionOutilController {

    private enum Mode {
        ADD,
        UPDATE
    }


    @FXML private TableView<OutilRow> outilsTable;
    @FXML private TableColumn<OutilRow, Boolean> selectColumn;
    @FXML private TableColumn<OutilRow, String> nameColumn;
    @FXML private TableColumn<OutilRow, String> identifiantColumn;
    @FXML private TableColumn<OutilRow, String> hashColumn;
    @FXML private TableColumn<OutilRow, String> actionsColumn;
    @FXML private TextField nomField;
    @FXML private TextField identifiantField;
    @FXML private TextField hashField;

    @FXML private StackPane modalOverlay;
    @FXML private HBox mainContent;

    @FXML private Button confirmButton;
    @FXML private Button cancelButton;

    private int editingId = 0;
    private Mode currentMode = Mode.ADD;

    @FXML private void initialize() {
        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        identifiantColumn.setCellValueFactory(data -> data.getValue().identifiantProperty());
        hashColumn.setCellValueFactory(data -> data.getValue().hashProperty());

        actionsColumn.setCellFactory(column -> createActionsCell());

        loadData();
    }

    @FXML private void handleConfirm(){

        String validationError = validateForm();

        if (validationError != null) {
            showError(validationError);
            return;
        }

        String nom = nomField.getText();
        String identifiant = identifiantField.getText();
        String hash = hashField.getText();

        if (currentMode == Mode.ADD) {

            Outil outil = new Outil(nom, identifiant, hash);
            boolean success = OutilController.Ajouter(outil);

            if (success) {
                clearForm();
                loadData();
                hideModal();
            }

        } else {

            Outil outil = new Outil(editingId, nom, identifiant, hash);
            boolean success = OutilController.Modifier(outil);

            if (success) {
                clearForm();
                loadData();
                hideModal();
            }
        }

    }

    @FXML private void handleOpenAddModal() {
        setAddMode();
        clearForm();
        showModal();
    }

    @FXML private void handleCloseModal() {
        hideModal();
    }

    private TableCell<OutilRow, String> createActionsCell() {

        return new TableCell<>() {

            private final MenuItem edit = new MenuItem("Modifier");
            private final MenuItem delete = new MenuItem("Supprimer");
            private final MenuButton menuButton = new MenuButton("...", null, edit, delete);

            {
                menuButton.getStyleClass().add("icon-btn");

                delete.setOnAction(event -> {
                    OutilRow row = getTableView().getItems().get(getIndex());
                    onDelete(row.getId());
                });

                edit.setOnAction(event -> {
                    OutilRow row = getTableView().getItems().get(getIndex());
                    onEdit(row);
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

    private void onDelete(int id) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'outil");
        confirm.setContentText("Voulez-vous vraiment supprimer cet outil ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                boolean success = OutilController.Supprimer(id);

                if (success) {
                    loadData(); // safest option
                } else {
                    System.out.println("Delete failed.");
                }
            }
        });
    }

    private void onEdit(OutilRow row) {

        nomField.setText(row.nameProperty().get());
        identifiantField.setText(row.identifiantProperty().get());
        hashField.setText(row.hashProperty().get());

        setUpdateMode(row.getId());

        showModal();
    }

    private void loadData() {
        List<Outil> outils = OutilController.AvoirListe();
        ObservableList<OutilRow> rows = FXCollections.observableArrayList();

        if (outils != null) {
            for (Outil o : outils) {
                rows.add(new OutilRow(o));
            }
        }

        outilsTable.setItems(rows);
    }

    private void setAddMode() {
        currentMode = Mode.ADD;
        editingId = 0;

        confirmButton.setText("Valider");
        cancelButton.setText("Annuler");
    }

    private void setUpdateMode(int id) {
        currentMode = Mode.UPDATE;
        editingId = id;

        confirmButton.setText("Valider");
        cancelButton.setText("Annuler");
    }

    private void clearForm() {
        nomField.clear();
        identifiantField.clear();
        hashField.clear();
    }

    public static class OutilRow {
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty identifiant = new SimpleStringProperty();
        private final StringProperty hash = new SimpleStringProperty();
        private final int id;

        public OutilRow(String name, String identifiant, String hash) {
            this.name.set(name);
            this.identifiant.set(identifiant);
            this.hash.set(hash);
            this.id = 0;
        }

        public OutilRow(int id ,String name, String identifiant, String hash) {
            this.name.set(name);
            this.identifiant.set(identifiant);
            this.hash.set(hash);
            this.id = id;
        }

        public OutilRow(Outil o){
            this.id = o.getID_Outil();
            this.hash.set(o.getHash_App());
            this.identifiant.set(o.getIdentifiant_Universelle());
            this.name.set(o.getNom_Outil());
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }
        public StringProperty nameProperty() {
            return name;
        }
        public StringProperty identifiantProperty() {
            return identifiant;
        }
        public StringProperty hashProperty() {
            return hash;
        }
        public int getId() {return id;}
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
    }

    private String validateForm() {

        String nom = nomField.getText().trim();
        String identifiant = identifiantField.getText().trim();
        String hash = hashField.getText().trim();

        if (nom.isEmpty()) {
            return "Le nom de l'outil ne peut pas être vide.";
        }

        if (identifiant.isEmpty()) {
            return "L'identifiant de l'application ne peut pas être vide.";
        }

        if (!identifiant.toLowerCase().endsWith(".exe")) {
            return "L'identifiant doit se terminer par .exe";
        }

        if (hash.isEmpty()) {
            return "Le hash ne peut pas être vide.";
        }

        if (hash.length() != 64) {
            return "Le hash doit contenir exactement 64 caractères.";
        }

        if (!hash.matches("[0-9a-f]{64}")) {
            return "Le hash doit contenir uniquement des caractères hexadécimaux en minuscules (0-9, a-f).";
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

}
