package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Repository.DemandeCongeRepository;
import com.recruitx.hrone.Utils.ActionLogger;
import com.recruitx.hrone.Models.Utilisateur;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.List;

public class FrmGestionConge {

    @FXML
    private Label lblCurrentUser;
    @FXML
    private Label lblStatus;

    @FXML
    private TableView<DemandeRow> demandesTable;
    @FXML
    private TableColumn<DemandeRow, String> idColumn;
    @FXML
    private TableColumn<DemandeRow, String> employeColumn;
    @FXML
    private TableColumn<DemandeRow, String> joursColumn;
    @FXML
    private TableColumn<DemandeRow, String> debutColumn;
    @FXML
    private TableColumn<DemandeRow, String> finColumn;
    @FXML
    private TableColumn<DemandeRow, String> statusColumn;
    @FXML
    private TableColumn<DemandeRow, String> actionsColumn;

    @FXML
    public void initialize() {
        loadSessionData();
        initializeTable();
        loadDemandes();
    }

    private void loadSessionData() {
        Utilisateur user = Session.getCurrentUser();

        if (user != null) {
            lblCurrentUser.setText("Connected: " + user.getNomUtilisateur());
        } else {
            lblCurrentUser.setText("Not connected");
        }
    }

    private void initializeTable() {
        idColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().idDemande)));
        employeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().nomEmploye));
        joursColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().nbrJours)));
        debutColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().dateDebut));
        finColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().dateFin));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(mapStatus(data.getValue().status)));
        actionsColumn.setCellFactory(column -> createActionsCell());
    }

    private void loadDemandes() {
        List<DemandeCongeRepository.DemandeCongeView> demandes = DemandeCongeRepository.avoirListeDemandes();
        ObservableList<DemandeRow> rows = FXCollections.observableArrayList();

        for (DemandeCongeRepository.DemandeCongeView d : demandes) {
            rows.add(new DemandeRow(
                    d.getIdDemande(),
                    d.getIdEmploye(),
                    d.getNomEmploye(),
                    d.getNbrJours(),
                    d.getDateDebut(),
                    d.getDateFin(),
                    d.getStatus()
            ));
        }

        demandesTable.setItems(rows);
        setStatus(rows.isEmpty() ? "Aucune demande de congé trouvée." : "", true);
    }

    @FXML
    private void handleRefresh() {
        loadDemandes();
    }

    private TableCell<DemandeRow, String> createActionsCell() {
        return new TableCell<>() {

            private final Button approveButton = new Button("Approuver");
            private final Button rejectButton = new Button("Refuser");
            private final HBox box = new HBox(8, approveButton, rejectButton);

            {
                approveButton.getStyleClass().add("btn-approve");
                rejectButton.getStyleClass().add("btn-reject");

                approveButton.setOnAction(event -> {
                    DemandeRow row = getTableView().getItems().get(getIndex());
                    updateDemandeStatus(row, 1);
                });

                rejectButton.setOnAction(event -> {
                    DemandeRow row = getTableView().getItems().get(getIndex());
                    updateDemandeStatus(row, 2);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    return;
                }

                DemandeRow row = getTableView().getItems().get(getIndex());
                boolean pending = row.status == 0;
                approveButton.setDisable(!pending);
                rejectButton.setDisable(!pending);

                setGraphic(box);
            }
        };
    }

    private void updateDemandeStatus(DemandeRow row, int newStatus) {
        boolean success = DemandeCongeRepository.modifierStatus(row.idDemande, newStatus);
        if (!success) {
            setStatus("Impossible de mettre à jour la demande #" + row.idDemande, false);
            return;
        }

        row.status = newStatus;
        demandesTable.refresh();

        if (newStatus == 1) {
            ActionLogger.log("Approuver Congé", "Demande #" + row.idDemande + " approuvée pour " + row.nomEmploye);
            setStatus("Demande #" + row.idDemande + " approuvée.", true);
        } else {
            ActionLogger.log("Refuser Congé", "Demande #" + row.idDemande + " refusée pour " + row.nomEmploye);
            setStatus("Demande #" + row.idDemande + " refusée.", true);
        }
    }

    private String mapStatus(int status) {
        return switch (status) {
            case 1 -> "Approuvée";
            case 2 -> "Refusée";
            default -> "En attente";
        };
    }

    private void setStatus(String message, boolean success) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
        lblStatus.getStyleClass().add(success ? "status-success" : "status-error");
    }

    public static class DemandeRow {
        private final int idDemande;
        private final int idEmploye;
        private final String nomEmploye;
        private final int nbrJours;
        private final String dateDebut;
        private final String dateFin;
        private int status;

        public DemandeRow(int idDemande, int idEmploye, String nomEmploye, int nbrJours, String dateDebut, String dateFin, int status) {
            this.idDemande = idDemande;
            this.idEmploye = idEmploye;
            this.nomEmploye = nomEmploye;
            this.nbrJours = nbrJours;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
            this.status = status;
        }
    }
}