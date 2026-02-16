package com.recruitx.hrone.controller;

import com.recruitx.hrone.entities.Evenement;
import com.recruitx.hrone.services.EvenementService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private Label statusLabel;
    @FXML
    private VBox eventList;

    private final EvenementService es = new EvenementService();
    // Variable pour stocker l'événement en cours de modification (null si création)
    private Evenement eventToEdit = null;

    @FXML
    public void initialize() {
        refreshList();
    }

    @FXML
    private void handlePublier() {
        try {
            String titre = titreField.getText();
            String description = descriptionArea.getText();
            String localisation = localisationField.getText();
            String image = imageField.getText();
            LocalDate dateDebut = dateDebutPicker.getValue();
            LocalDate dateFin = dateFinPicker.getValue();

            // --- Contrôle de Saisie ---
            if (titre == null || titre.trim().isEmpty()) {
                showError("Le titre est obligatoire.");
                return;
            }
            if (localisation == null || localisation.trim().isEmpty()) {
                showError("La localisation est obligatoire.");
                return;
            }
            if (dateDebut == null || dateFin == null) {
                showError("Les dates sont obligatoires.");
                return;
            }
            if (dateDebut.isAfter(dateFin)) {
                showError("La date de début doit être avant la date de fin.");
                return;
            }
            if (dateDebut.isBefore(LocalDate.now())) {
                showError("La date de début ne peut pas être dans le passé.");
                return;
            }

            // --- Conversion Date -> Int (Epoch Day) ---
            // Note: Si la DB a une table `Ordre` pré-remplie, ces IDs doivent y exister.
            // On suppose ici une correspondance directe ou que l'utilisateur gère `Ordre`.
            int numCreation = (int) LocalDate.now().toEpochDay();
            int numDebut = (int) dateDebut.toEpochDay();
            int numFin = (int) dateFin.toEpochDay();

            if (eventToEdit == null) {
                // Mode Création
                Evenement ev = new Evenement(titre, description, numCreation, numDebut, numFin, localisation, image);
                es.add(ev);
                showSuccess("Événement publié avec succès !");
            } else {
                // Mode Modification
                eventToEdit.setTitre(titre);
                eventToEdit.setDescription(description);
                eventToEdit.setLocalisation(localisation);
                eventToEdit.setImage(image);
                // On garde la date de création originale, ou on update ? Généralement on garde
                // creation mais on update debut/fin.
                eventToEdit.setNumOrdreDebutEvenement(numDebut);
                eventToEdit.setNumOrdreFinEvenement(numFin);

                es.update(eventToEdit);
                showSuccess("Événement modifié avec succès !");
                eventToEdit = null; // Reset mode
            }

            clearFields();
            refreshList();

        } catch (Exception e) {
            showError("Erreur système : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        titreField.clear();
        localisationField.clear();
        descriptionArea.clear();
        imageField.clear();
        dateDebutPicker.setValue(null);
        dateFinPicker.setValue(null);
        eventToEdit = null;
    }

    private void showError(String msg) {
        statusLabel.setText("Erreur : " + msg);
        statusLabel.setStyle("-fx-text-fill: #dc2626;"); // Red 600
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #16a34a;"); // Green 600
    }

    private void refreshList() {
        if (eventList == null)
            return;
        eventList.getChildren().clear();
        List<Evenement> events = es.getAll();

        if (events.isEmpty()) {
            Label placeholder = new Label("Aucun événement planifié.");
            placeholder.setStyle("-fx-text-fill: #64748b; -fx-padding: 10;");
            eventList.getChildren().add(placeholder);
            return;
        }

        for (Evenement e : events) {
            VBox card = createEventCard(e);
            eventList.getChildren().add(card);
        }
    }

    private VBox createEventCard(Evenement e) {
        VBox card = new VBox();
        card.getStyleClass().add("resource-item");

        Label lTitre = new Label(e.getTitre());
        lTitre.getStyleClass().add("resource-title");

        // Conversion Int -> Date pour affichage
        LocalDate dDebut = LocalDate.ofEpochDay(e.getNumOrdreDebutEvenement());
        LocalDate dFin = LocalDate.ofEpochDay(e.getNumOrdreFinEvenement());

        Label lMeta = new Label(dDebut + " - " + dFin + " • " + e.getLocalisation());
        lMeta.getStyleClass().add("resource-meta");

        Label lDesc = new Label(e.getDescription());
        lDesc.setWrapText(true);
        lDesc.setStyle("-fx-text-fill: #334155;"); // Slate 700

        // Boutons Actions
        // HBox actions = new HBox(10); ...

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editItem = new MenuItem("Modifier");
        editItem.setOnAction(ev -> loadEventForEdit(e));

        MenuItem deleteItem = new MenuItem("Supprimer");
        deleteItem.setOnAction(ev -> confirmDelete(e));

        MenuItem activiteItem = new MenuItem("Gestion Activités");
        activiteItem.setOnAction(ev -> handleManageActivites());

        contextMenu.getItems().addAll(editItem, deleteItem, activiteItem);
        card.setOnContextMenuRequested(event -> contextMenu.show(card, event.getScreenX(), event.getScreenY()));

        // Ou simple clic pour edit ? non.
        // Ajoutons un bouton supprimer visible pour simplicité
        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().add("btn-danger");
        btnSupprimer.getStyleClass().add("btn-small");
        btnSupprimer.setOnAction(ev -> confirmDelete(e));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-padding: 5 10;");
        btnModifier.setOnAction(ev -> loadEventForEdit(e));

        ToolBar actions = new ToolBar();
        actions.setStyle("-fx-background-color: transparent; -fx-padding: 5 0 0 0;");
        actions.getItems().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(lTitre, lMeta, lDesc, actions);
        return card;
    }

    @FXML
    private void handleManageActivites() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recruitx/hrone/activites.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur lors de l'ouverture des activités : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadEventForEdit(Evenement e) {
        eventToEdit = e;
        titreField.setText(e.getTitre());
        localisationField.setText(e.getLocalisation());
        descriptionArea.setText(e.getDescription());
        imageField.setText(e.getImage());
        dateDebutPicker.setValue(LocalDate.ofEpochDay(e.getNumOrdreDebutEvenement()));
        dateFinPicker.setValue(LocalDate.ofEpochDay(e.getNumOrdreFinEvenement()));

        showSuccess("Mode édition : " + e.getTitre());
    }

    private void confirmDelete(Evenement e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'événement '" + e.getTitre() + "' ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            es.delete(e.getIdEvenement());
            refreshList();
            showSuccess("Événement supprimé.");
            if (eventToEdit != null && eventToEdit.getIdEvenement() == e.getIdEvenement()) {
                clearFields();
            }
        }
    }
}
