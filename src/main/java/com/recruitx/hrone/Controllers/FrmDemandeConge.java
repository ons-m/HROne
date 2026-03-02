package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.DemandeConge;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Repository.DemandeCongeRepository;
import com.recruitx.hrone.Utils.ActionLogger;
import com.recruitx.hrone.Utils.COrdre;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class FrmDemandeConge {

    @FXML
    private Label lblCurrentUser;
    @FXML
    private DatePicker dpDateDebut;
    @FXML
    private DatePicker dpDateFin;
    @FXML
    private TextArea txtCommentaire;
    @FXML
    private Label lblNbrJours;
    @FXML
    private Label lblStatus;

    private int currentUserId = -1;

    @FXML
    public void initialize() {
        loadSessionData();
        bindDateListeners();
        updateDaysLabel();
    }

    private void loadSessionData() {
        Utilisateur user = Session.getCurrentUser();

        if (user != null) {
            lblCurrentUser.setText("Connected: " + user.getNomUtilisateur());
            currentUserId = user.getIdUtilisateur();
        } else {
            lblCurrentUser.setText("Not connected");
            currentUserId = -1;
        }
    }

    private void bindDateListeners() {
        dpDateDebut.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysLabel());
        dpDateFin.valueProperty().addListener((obs, oldVal, newVal) -> updateDaysLabel());
    }

    private void updateDaysLabel() {
        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();

        if (debut == null || fin == null || fin.isBefore(debut)) {
            lblNbrJours.setText("0 jour");
            return;
        }

        long jours = ChronoUnit.DAYS.between(debut, fin) + 1;
        lblNbrJours.setText(jours + (jours > 1 ? " jours" : " jour"));
    }

    @FXML
    private void handleSubmit() {
        if (currentUserId <= 0) {
            setStatus("Utilisateur non connecté.", false);
            return;
        }

        LocalDate debut = dpDateDebut.getValue();
        LocalDate fin = dpDateFin.getValue();

        if (debut == null || fin == null) {
            setStatus("Veuillez sélectionner la date de début et la date de fin.", false);
            return;
        }

        if (fin.isBefore(debut)) {
            setStatus("La date de fin doit être supérieure ou égale à la date de début.", false);
            return;
        }

        if (debut.isBefore(LocalDate.now())) {
            setStatus("La date de début ne peut pas être dans le passé.", false);
            return;
        }

        int idEmploye = DemandeCongeRepository.getEmployeIdByUserId(currentUserId);
        if (idEmploye <= 0) {
            setStatus("Aucun employé lié à l'utilisateur connecté.", false);
            return;
        }

        int nbrJours = (int) ChronoUnit.DAYS.between(debut, fin) + 1;

        LocalDateTime startDateTime = debut.atStartOfDay();
        LocalDateTime endDateTime = fin.atTime(23, 59, 59);

        DemandeConge demande = new DemandeConge();
        demande.setIdEmploye(idEmploye);
        demande.setNbrJourDemande(nbrJours);
        demande.setNumOrdreDebutConge((int) COrdre.GetNumOrdreFromDate(startDateTime));
        demande.setNumOrdreFinConge((int) COrdre.GetNumOrdreFromDate(endDateTime));
        demande.setStatus(0);

        boolean success = DemandeCongeRepository.ajouter(demande);
        if (!success) {
            setStatus("Impossible d'enregistrer la demande de congé.", false);
            return;
        }

        String commentaire = txtCommentaire.getText() == null ? "" : txtCommentaire.getText().trim();
        ActionLogger.log("Demande Congé", "Demande congé " + nbrJours + " jour(s). " + commentaire);

        setStatus("Demande de congé envoyée avec succès.", true);
        handleReset();
    }

    @FXML
    private void handleReset() {
        dpDateDebut.setValue(null);
        dpDateFin.setValue(null);
        txtCommentaire.clear();
        updateDaysLabel();
    }

    private void setStatus(String message, boolean success) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("status-success", "status-error");
        lblStatus.getStyleClass().add(success ? "status-success" : "status-error");
    }
}