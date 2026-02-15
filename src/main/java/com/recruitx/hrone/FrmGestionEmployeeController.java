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

public class FrmGestionEmployeeController {
    @FXML
    private TableView<EmployeeRow> employeesTable;

    @FXML
    private TableColumn<EmployeeRow, Boolean> selectColumn;

    @FXML
    private TableColumn<EmployeeRow, String> nameColumn;

    @FXML
    private TableColumn<EmployeeRow, String> phoneColumn;

    @FXML
    private TableColumn<EmployeeRow, String> emailColumn;

    @FXML
    private TableColumn<EmployeeRow, String> cinColumn;

    @FXML
    private TableColumn<EmployeeRow, String> birthColumn;

    @FXML
    private TableColumn<EmployeeRow, String> soldColumn;

    @FXML
    private TableColumn<EmployeeRow, String> remainingColumn;

    @FXML
    private TableColumn<EmployeeRow, String> hoursColumn;

    @FXML
    private TableColumn<EmployeeRow, String> actionsColumn;

    @FXML
    private void initialize() {
        selectColumn.setCellValueFactory(data -> data.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        phoneColumn.setCellValueFactory(data -> data.getValue().phoneProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        cinColumn.setCellValueFactory(data -> data.getValue().cinProperty());
        birthColumn.setCellValueFactory(data -> data.getValue().birthProperty());
        soldColumn.setCellValueFactory(data -> data.getValue().soldProperty());
        remainingColumn.setCellValueFactory(data -> data.getValue().remainingProperty());
        hoursColumn.setCellValueFactory(data -> data.getValue().hoursProperty());

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

        employeesTable.setItems(sampleRows());
    }

    private MenuButton buildMenuButton() {
        MenuItem edit = new MenuItem("Modifier");
        MenuItem delete = new MenuItem("Supprimer");
        MenuButton button = new MenuButton("...", null, edit, delete);
        button.getStyleClass().add("icon-btn");
        return button;
    }

    private ObservableList<EmployeeRow> sampleRows() {
        return FXCollections.observableArrayList(
            new EmployeeRow("Sarah El Haddadi", "06 12 34 56 78", "sarah@hrone.ma", "AA123456", "12/05/1995", "24 j", "18 j", "160 h"),
            new EmployeeRow("Mehdi Bensalem", "06 98 76 54 32", "mehdi@hrone.ma", "BB654321", "03/11/1992", "20 j", "12 j", "172 h")
        );
    }

    public static class EmployeeRow {
        private final BooleanProperty selected = new SimpleBooleanProperty(false);
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty phone = new SimpleStringProperty();
        private final StringProperty email = new SimpleStringProperty();
        private final StringProperty cin = new SimpleStringProperty();
        private final StringProperty birth = new SimpleStringProperty();
        private final StringProperty sold = new SimpleStringProperty();
        private final StringProperty remaining = new SimpleStringProperty();
        private final StringProperty hours = new SimpleStringProperty();

        public EmployeeRow(String name, String phone, String email, String cin, String birth, String sold, String remaining, String hours) {
            this.name.set(name);
            this.phone.set(phone);
            this.email.set(email);
            this.cin.set(cin);
            this.birth.set(birth);
            this.sold.set(sold);
            this.remaining.set(remaining);
            this.hours.set(hours);
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public StringProperty nameProperty() {
            return name;
        }

        public StringProperty phoneProperty() {
            return phone;
        }

        public StringProperty emailProperty() {
            return email;
        }

        public StringProperty cinProperty() {
            return cin;
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
    }
}
