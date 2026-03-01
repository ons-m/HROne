package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Repository.ActiviteRepository;
import com.recruitx.hrone.Models.Evenement;
import com.recruitx.hrone.Models.Activite;
import com.recruitx.hrone.Repository.EvenementRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FrmActivite implements NavigationAware{

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label statusLabel;
    @FXML
    private ComboBox<Evenement> eventSelect;
    @FXML
    private VBox activiteList;

    private final ActiviteRepository as = new ActiviteRepository();
    private final EvenementRepository es = new EvenementRepository();
    private Activite activiteToEdit = null;
    
    private FrmMain mainController;

    @Override
    public void setMainController(FrmMain mainController) {
        this.mainController = mainController;
    }

	@FXML
    public void initialize() {
        setupEventComboBox();
        refreshList();
    }

    private void setupEventComboBox() {
        if (eventSelect != null) {
            List<Evenement> events = es.getAll();
            eventSelect.getItems().addAll(events);

            eventSelect.setConverter(new javafx.util.StringConverter<Evenement>() {
                @Override
                public String toString(Evenement object) {
                    return object != null ? object.getTitre() : "";
                }

                @Override
                public Evenement fromString(String string) {
                    return eventSelect.getItems().stream()
                            .filter(ev -> ev.getTitre().equals(string))
                            .findFirst().orElse(null);
                }
            });
        }
    }
    
    @FXML
    private void handlePublier() {
        try {
            String titre = titreField.getText();
            String description = descriptionArea.getText();

            if (eventSelect.getValue() == null) {
                showError("Veuillez sélectionner un événement.");
                return;
            }

            int idEvenement = eventSelect.getValue().getIdEvenement();

            if (activiteToEdit == null) {
                // Mode Création
                Activite a = new Activite(idEvenement, titre, description);
                as.add(a);
                showSuccess("Activité ajoutée avec succès !");
            } else {
                // Mode Modification
                activiteToEdit.setIdEvenement(idEvenement);
                activiteToEdit.setTitre(titre);
                activiteToEdit.setDescription(description);

                as.update(activiteToEdit);
                showSuccess("Activité modifiée avec succès !");
                activiteToEdit = null;
            }

            clearFields();
            refreshList();

        } catch (Exception e) {
            showError("Erreur système : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        clearFields();
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.EVENTS);
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionArea.clear();
        if (eventSelect != null)
            eventSelect.setValue(null);
        activiteToEdit = null;
    }

    private void showError(String msg) {
        statusLabel.setText("Erreur : " + msg);
        statusLabel.setStyle("-fx-text-fill: #dc2626;");
    }

    private void showSuccess(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill: #16a34a;");
    }

    private void refreshList() {
        if (activiteList == null)
            return;
        activiteList.getChildren().clear();
        List<Activite> activites = as.getAll();

        if (activites.isEmpty()) {
            Label placeholder = new Label("Aucune activité planifiée.");
            placeholder.setStyle("-fx-text-fill: #64748b; -fx-padding: 10;");
            activiteList.getChildren().add(placeholder);
            return;
        }

        for (Activite a : activites) {
            VBox card = createActiviteCard(a);
            activiteList.getChildren().add(card);
        }
    }

    private VBox createActiviteCard(Activite a) {
        VBox card = new VBox();
        card.getStyleClass().add("resource-item");

        Label lTitre = new Label(a.getTitre());
        lTitre.getStyleClass().add("resource-title");

        Evenement ev = es.getOne(a.getIdEvenement());
        String evName = (ev != null) ? ev.getTitre() : "Événement inconnu";
        Label lEv = new Label("Lié à : " + evName);
        lEv.getStyleClass().add("resource-meta");

        Label lDesc = new Label(a.getDescription());
        lDesc.setWrapText(true);
        lDesc.setStyle("-fx-text-fill: #334155;");

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().addAll("btn-danger", "btn-small");
        btnSupprimer.setOnAction(evAction -> confirmDelete(a));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-padding: 5 10;");
        btnModifier.setOnAction(evAction -> loadActiviteForEdit(a));

        ToolBar actions = new ToolBar();
        actions.setStyle("-fx-background-color: transparent; -fx-padding: 5 0 0 0;");
        actions.getItems().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(lTitre, lEv, lDesc, actions);
        return card;
    }

    private void loadActiviteForEdit(Activite a) {
        activiteToEdit = a;
        titreField.setText(a.getTitre());
        descriptionArea.setText(a.getDescription());

        // Retrouver l'événement lié
        Evenement ev = es.getOne(a.getIdEvenement());
        if (ev != null)
            eventSelect.setValue(ev);

        showSuccess("Mode édition : " + a.getTitre());
    }

    private void confirmDelete(Activite a) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer l'activité '" + a.getTitre() + "' ?");
        alert.setContentText("Cette action est irréversible.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            as.delete(a.getIdActivite());
            refreshList();
            showSuccess("Activité supprimée.");
            if (activiteToEdit != null && activiteToEdit.getIdActivite() == a.getIdActivite()) {
                clearFields();
            }
        }
    }
}
