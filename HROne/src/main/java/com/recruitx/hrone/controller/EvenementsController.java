package com.recruitx.hrone.controller;

import com.recruitx.hrone.entities.Evenement;
import com.recruitx.hrone.entities.ParticipationEvenement;
import com.recruitx.hrone.services.EvenementService;
import com.recruitx.hrone.services.ParticipationEvenementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.util.List;

public class EvenementsController {

    @FXML
    private VBox eventsGrid;
    @FXML
    private ComboBox<Evenement> eventSelect;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextArea messageArea;
    @FXML
    private CheckBox consentUnknown;
    @FXML
    private Label statusLabel; // Ensure this is in FXML or handle null

    private final EvenementService es = new EvenementService();
    // private final ParticipationEvenementService pes = new
    // ParticipationEvenementService(); // Uncomment when ready

    @FXML
    public void initialize() {
        refreshEvents();
        setupComboBox();
    }

    private void setupComboBox() {
        if (eventSelect == null)
            return;
        List<Evenement> events = es.getAll();
        eventSelect.getItems().addAll(events);

        eventSelect.setConverter(new StringConverter<Evenement>() {
            @Override
            public String toString(Evenement object) {
                return object != null ? object.getTitre() : "";
            }

            @Override
            public Evenement fromString(String string) {
                return eventSelect.getItems().stream()
                        .filter(ap -> ap.getTitre().equals(string))
                        .findFirst().orElse(null);
            }
        });
    }

    private void refreshEvents() {
        if (eventsGrid == null)
            return;
        eventsGrid.getChildren().clear();
        List<Evenement> events = es.getAll();

        if (events.isEmpty()) {
            Label placeholder = new Label("Aucun événement disponible pour le moment.");
            placeholder.setStyle("-fx-text-fill: #64748b; -fx-padding: 20;");
            eventsGrid.getChildren().add(placeholder);
        }

        for (Evenement e : events) {
            VBox card = createEventCard(e);
            eventsGrid.getChildren().add(card);
        }
    }

    private VBox createEventCard(Evenement e) {
        VBox card = new VBox();
        card.getStyleClass().add("event-card");

        Label lTitre = new Label(e.getTitre());
        lTitre.getStyleClass().add("card-title");

        Label lDesc = new Label(e.getDescription());
        lDesc.getStyleClass().add("muted");
        lDesc.setWrapText(true);

        HBox details = new HBox(15);
        details.getStyleClass().add("event-details");

        // Convert Num_Ordre to Date for display
        LocalDate dateDebut = LocalDate.ofEpochDay(e.getNumOrdreDebutEvenement());
        Label lDate = new Label(dateDebut.toString());
        Label lLieu = new Label(e.getLocalisation());
        // Label lType = new Label("Présentiel"); // Static for now

        details.getChildren().addAll(lDate, lLieu);

        Button btnParticiper = new Button("Participer");
        btnParticiper.getStyleClass().add("ghost");
        btnParticiper.setOnAction(ev -> {
            if (eventSelect != null) {
                eventSelect.setValue(e);
                if (nomField != null)
                    nomField.requestFocus();
            }
        });

        card.getChildren().addAll(lTitre, lDesc, details, btnParticiper);
        return card;
    }

    @FXML
    private void handleParticiper() {
        if (statusLabel != null)
            statusLabel.setVisible(true);

        try {
            if (consentUnknown != null && !consentUnknown.isSelected()) {
                showError("Veuillez accepter les conditions.");
                return;
            }
            if (eventSelect != null && eventSelect.getValue() == null) {
                showError("Veuillez sélectionner un événement.");
                return;
            }
            if (nomField != null && nomField.getText().isEmpty()) {
                showError("Nom obligatoire.");
                return;
            }

            // --- Logic to create Participation ---
            // int idEvent = eventSelect.getValue().getIdEvenement();
            // ParticipationEvenement pe = new ParticipationEvenement(...)
            // pes.add(pe);

            showSuccess("Participation enregistrée (Simulation) !");

        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    private void showError(String msg) {
        if (statusLabel == null)
            return;
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-success");
        statusLabel.getStyleClass().add("status-error");
        statusLabel.setVisible(true);
    }

    private void showSuccess(String msg) {
        if (statusLabel == null)
            return;
        statusLabel.setText(msg);
        statusLabel.getStyleClass().removeAll("status-error");
        statusLabel.getStyleClass().add("status-success");
        statusLabel.setVisible(true);
    }
}
