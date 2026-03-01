package com.recruitx.hrone.controller;

import com.recruitx.hrone.entities.Evenement;
import com.recruitx.hrone.entities.ListeAttente;
import com.recruitx.hrone.services.EvenementService;
import com.recruitx.hrone.services.ListeAttenteService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EventsDashboardController {

    @FXML
    private TextField titreField;
    @FXML
    private TextField localisationField;
    @FXML
    private DatePicker dateDebutPicker;
    @FXML
    private DatePicker dateFinPicker;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField imageField;
    @FXML
    private TextField nbMaxField;
    @FXML
    private Label statusLabel;
    @FXML
    private Label formTitleLabel;
    @FXML
    private VBox eventList;
    @FXML
    private VBox waitlistContainer;

    private CheckBox payantCheck;
    private TextField prixField;

    private final EvenementService es = new EvenementService();
    private final ListeAttenteService las = new ListeAttenteService();
    private Evenement eventToEdit = null;

    @FXML
    public void initialize() {
        injectPaymentFields();
        refreshList();
        refreshWaitlist();
    }

    private void injectPaymentFields() {
        if (imageField != null && imageField.getParent() != null) {
            VBox parent = (VBox) imageField.getParent().getParent();
            payantCheck = new CheckBox("Événement Payant ?");
            payantCheck.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0;");
            prixField = new TextField();
            prixField.setPromptText("Prix en DT");
            prixField.setVisible(false);
            prixField.setManaged(false);
            payantCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
                prixField.setVisible(newVal);
                prixField.setManaged(newVal);
            });
            parent.getChildren().add(parent.getChildren().indexOf(imageField.getParent()) + 1, payantCheck);
            parent.getChildren().add(parent.getChildren().indexOf(payantCheck) + 1, prixField);
        }
    }

    @FXML
    private void handlePublier() {
        try {
            String titre = titreField.getText().trim();
            String localisation = localisationField.getText().trim();
            LocalDate debut = dateDebutPicker.getValue();
            LocalDate fin = dateFinPicker.getValue();

            if (titre.isEmpty() || localisation.isEmpty() || debut == null || fin == null) {
                showError("Veuillez remplir les champs obligatoires.");
                return;
            }

            double prix = 0.0;
            if (payantCheck != null && payantCheck.isSelected()) {
                try {
                    prix = Double.parseDouble(prixField.getText().trim());
                } catch (Exception ex) {
                    showError("Prix invalide.");
                    return;
                }
            }

            int nbMax = 50;
            try {
                if (nbMaxField != null && !nbMaxField.getText().trim().isEmpty()) {
                    nbMax = Integer.parseInt(nbMaxField.getText().trim());
                }
            } catch (Exception ex) {
            }

            if (eventToEdit == null) {
                Evenement ev = new Evenement(0, titre, descriptionArea.getText().trim(),
                        (int) (System.currentTimeMillis() / 1000), (int) debut.toEpochDay(), (int) fin.toEpochDay(),
                        localisation, imageField.getText().trim(), payantCheck != null && payantCheck.isSelected(),
                        prix, nbMax);
                es.add(ev);
                showSuccess("Événement créé !");
            } else {
                eventToEdit.setTitre(titre);
                eventToEdit.setDescription(descriptionArea != null ? descriptionArea.getText().trim() : "");
                eventToEdit.setLocalisation(localisation);
                eventToEdit.setNumOrdreDebutEvenement((int) debut.toEpochDay());
                eventToEdit.setNumOrdreFinEvenement((int) fin.toEpochDay());
                eventToEdit.setImage(imageField != null ? imageField.getText().trim() : "");
                eventToEdit.setNbMax(nbMax);
                eventToEdit.setEstPayant(payantCheck != null && payantCheck.isSelected());
                eventToEdit.setPrix(prix);
                es.update(eventToEdit);
                showSuccess("Événement mis à jour !");
                eventToEdit = null;
                if (formTitleLabel != null)
                    formTitleLabel.setText("Créer un événement");
            }
            clearFields();
            refreshList();
            refreshWaitlist();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void refreshList() {
        if (eventList == null)
            return;
        eventList.getChildren().clear();
        List<Evenement> list = es.getAll();
        for (Evenement e : list) {
            VBox card = new VBox(5);
            card.getStyleClass().add("resource-item");
            Label title = new Label(e.getTitre() + " (" + e.getLocalisation() + ")");
            title.getStyleClass().add("resource-title");
            HBox actions = new HBox(10);
            Button btnEdit = new Button("Modifier");
            btnEdit.setOnAction(ev -> loadEventForEdit(e));
            Button btnDelete = new Button("Supprimer");
            btnDelete.setOnAction(ev -> handleDelete(e));
            actions.getChildren().addAll(btnEdit, btnDelete);
            card.getChildren().addAll(title, actions);
            eventList.getChildren().add(card);
        }
    }

    private void refreshWaitlist() {
        if (waitlistContainer == null)
            return;
        waitlistContainer.getChildren().clear();
        List<Evenement> events = es.getAll();
        for (Evenement ev : events) {
            List<ListeAttente> wl = las.getWaitlistByEvent(ev.getIdEvenement());
            for (ListeAttente item : wl) {
                VBox card = new VBox(2);
                card.getStyleClass().add("resource-item");
                card.setStyle("-fx-border-color: #f59e0b; -fx-border-width: 0 0 0 4;");
                Label info = new Label(ev.getTitre() + " : " + item.getNomComplet());
                card.getChildren().add(info);
                waitlistContainer.getChildren().add(card);
            }
        }
    }

    private void loadEventForEdit(Evenement e) {
        eventToEdit = e;
        titreField.setText(e.getTitre());
        localisationField.setText(e.getLocalisation());
        if (nbMaxField != null)
            nbMaxField.setText(String.valueOf(e.getNbMax()));
        if (descriptionArea != null)
            descriptionArea.setText(e.getDescription() != null ? e.getDescription() : "");
        if (imageField != null)
            imageField.setText(e.getImage() != null ? e.getImage() : "");
        // Restore dates (stored as epoch days)
        if (dateDebutPicker != null && e.getNumOrdreDebutEvenement() > 0)
            dateDebutPicker.setValue(java.time.LocalDate.ofEpochDay(e.getNumOrdreDebutEvenement()));
        if (dateFinPicker != null && e.getNumOrdreFinEvenement() > 0)
            dateFinPicker.setValue(java.time.LocalDate.ofEpochDay(e.getNumOrdreFinEvenement()));
        // Restore payment fields
        if (payantCheck != null) {
            payantCheck.setSelected(e.isEstPayant());
            if (prixField != null && e.isEstPayant())
                prixField.setText(String.valueOf(e.getPrix()));
        }
        // Update form title to signal edit mode
        if (formTitleLabel != null)
            formTitleLabel.setText("Modifier l'événement : " + e.getTitre());
        showSuccess("Mode édition : " + e.getTitre());
        // Scroll form into view by requesting focus
        titreField.requestFocus();
    }

    private void handleDelete(Evenement e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(res -> {
            if (res == ButtonType.YES) {
                es.delete(e.getIdEvenement());
                refreshList();
                refreshWaitlist();
            }
        });
    }

    @FXML
    private void handleReset() {
        clearFields();
    }

    private void clearFields() {
        titreField.clear();
        localisationField.clear();
        if (nbMaxField != null)
            nbMaxField.clear();
        if (descriptionArea != null)
            descriptionArea.clear();
        if (imageField != null)
            imageField.clear();
        if (dateDebutPicker != null)
            dateDebutPicker.setValue(null);
        if (dateFinPicker != null)
            dateFinPicker.setValue(null);
        if (payantCheck != null) {
            payantCheck.setSelected(false);
        }
        if (prixField != null) {
            prixField.clear();
        }
        if (formTitleLabel != null)
            formTitleLabel.setText("Créer un événement");
        eventToEdit = null;
    }

    private void showError(String msg) {
        if (statusLabel != null)
            statusLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        if (statusLabel != null)
            statusLabel.setText(msg);
    }

    @FXML
    private void handleManageActivites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recruitx/hrone/activites.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
