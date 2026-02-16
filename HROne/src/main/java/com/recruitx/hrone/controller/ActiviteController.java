package com.recruitx.hrone.controller;

import com.recruitx.hrone.entities.Activite;
import com.recruitx.hrone.services.ActiviteService;
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

public class ActiviteController {

    @FXML
    private TextField titreField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label statusLabel;
    @FXML
    private VBox activiteList;

    private final ActiviteService as = new ActiviteService();
    private Activite activiteToEdit = null;

    @FXML
    public void initialize() {
        refreshList();
    }

    @FXML
    private void handlePublier() {
        try {
            String titre = titreField.getText();
            String description = descriptionArea.getText();

            if (titre == null || titre.trim().isEmpty()) {
                showError("Le titre est obligatoire.");
                return;
            }

            if (activiteToEdit == null) {
                // Mode Création
                Activite a = new Activite(titre, description);
                as.add(a);
                showSuccess("Activité ajoutée avec succès !");
            } else {
                // Mode Modification
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recruitx/hrone/events-dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            showError("Erreur lors du retour : " + e.getMessage());
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionArea.clear();
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

        Label lDesc = new Label(a.getDescription());
        lDesc.setWrapText(true);
        lDesc.setStyle("-fx-text-fill: #334155;");

        Button btnSupprimer = new Button("Supprimer");
        btnSupprimer.getStyleClass().addAll("btn-danger", "btn-small");
        btnSupprimer.setOnAction(ev -> confirmDelete(a));

        Button btnModifier = new Button("Modifier");
        btnModifier.setStyle(
                "-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-background-radius: 8; -fx-padding: 5 10;");
        btnModifier.setOnAction(ev -> loadActiviteForEdit(a));

        ToolBar actions = new ToolBar();
        actions.setStyle("-fx-background-color: transparent; -fx-padding: 5 0 0 0;");
        actions.getItems().addAll(btnModifier, btnSupprimer);

        card.getChildren().addAll(lTitre, lDesc, actions);
        return card;
    }

    private void loadActiviteForEdit(Activite a) {
        activiteToEdit = a;
        titreField.setText(a.getTitre());
        descriptionArea.setText(a.getDescription());
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
