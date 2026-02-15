package com.recruitx.hrone;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;

public class FrmGestionOutilController {
    @FXML
    private TableView<OutilRow> outilsTable;

    @FXML
    private TableColumn<OutilRow, Boolean> selectColumn;

    @FXML
    private TableColumn<OutilRow, String> nameColumn;

    @FXML
    private TableColumn<OutilRow, String> identifiantColumn;

    @FXML
    private TableColumn<OutilRow, String> hashColumn;

    @FXML
    private TableColumn<OutilRow, String> actionsColumn;

    @FXML
    private void initialize() {
        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        identifiantColumn.setCellValueFactory(data -> data.getValue().identifiantProperty());
        hashColumn.setCellValueFactory(data -> data.getValue().hashProperty());

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final MenuButton menuButton = buildMenuButton();

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                } else {
                    setGraphic(menuButton);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                }
            }
        });

        outilsTable.setItems(sampleRows());
    }

    private MenuButton buildMenuButton() {
        MenuItem edit = new MenuItem("Modifier");
        MenuItem delete = new MenuItem("Supprimer");
        MenuButton button = new MenuButton("...", null, edit, delete);
        button.getStyleClass().add("icon-btn");
        return button;
    }

    private ObservableList<OutilRow> sampleRows() {
        return FXCollections.observableArrayList(
            new OutilRow("Outil RH Suite", "RH-UNI-4821", "7f2c9a8b11"),
            new OutilRow("Badge Access Pro", "ACC-UNI-9910", "2dd9f1c34a")
        );
    }

    public static class OutilRow {
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty identifiant = new SimpleStringProperty();
        private final StringProperty hash = new SimpleStringProperty();

        public OutilRow(String name, String identifiant, String hash) {
            this.name.set(name);
            this.identifiant.set(identifiant);
            this.hash.set(hash);
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
    }
}
