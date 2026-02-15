package com.recruitx.hrone.controller;

import com.recruitx.hrone.dao.FormationDAO;
import com.recruitx.hrone.models.Formation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.util.List;

public class FormationControlle1 {

    @FXML
    private ComboBox<Formation> formationSelect;

    @FXML
    private TextField nomField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField telephoneField;

    @FXML
    private VBox formationGrid;

    @FXML
    private Label sessionsOuvertesCount;

    @FXML
    private VBox heroCard;

    private FormationDAO formationDAO;
    private List<Formation> formations;

    // Marqueur pour séparer la description des modules
    private static final String MODULES_MARKER = "[MODULES]";

    // Nombre de mots avant d'ajouter un saut de ligne
    private static final int WORDS_PER_LINE = 15;

    @FXML
    public void initialize() {
        formationDAO = new FormationDAO();
        loadFormations();
        displayFormations();
        updateSessionCount();
        displayFeaturedFormation();
    }

    private void loadFormations() {
        formations = formationDAO.readAll();
        ObservableList<Formation> list =
                FXCollections.observableArrayList(formations);
        formationSelect.setItems(list);
    }

    /**
     * Extrait seulement la description avant le marqueur [MODULES]
     */
    private String getDescriptionOnly(String fullDescription) {
        if (fullDescription == null || fullDescription.isEmpty()) {
            return "";
        }

        int markerIndex = fullDescription.indexOf(MODULES_MARKER);
        if (markerIndex != -1) {
            // Retourner seulement la partie avant [MODULES]
            return fullDescription.substring(0, markerIndex).trim();
        }

        // Si pas de marqueur, retourner toute la description
        return fullDescription.trim();
    }

    /**
     * Ajoute des sauts de ligne après un certain nombre de mots
     */
    private String formatTextWithLineBreaks(String text, int wordsPerLine) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String[] words = text.split("\\s+");
        StringBuilder formatted = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            formatted.append(words[i]);

            if ((i + 1) % wordsPerLine == 0 && i < words.length - 1) {
                formatted.append("\n");
            } else if (i < words.length - 1) {
                formatted.append(" ");
            }
        }

        return formatted.toString();
    }

    private void displayFormations() {
        formationGrid.getChildren().clear();

        for (Formation formation : formations) {
            VBox card = createFormationCard(formation);
            formationGrid.getChildren().add(card);
        }
    }

    private VBox createFormationCard(Formation formation) {
        VBox card = new VBox();
        card.getStyleClass().add("formation-card");

        // Titre
        Label title = new Label(formation.getTitre());
        title.getStyleClass().add("card-title");

        // Description - SEULEMENT avant [MODULES]
        String descriptionOnly = getDescriptionOnly(formation.getDescription());
        String formattedDescription = formatTextWithLineBreaks(descriptionOnly, WORDS_PER_LINE);

        Label description = new Label(formattedDescription);
        description.getStyleClass().add("muted");
        description.setWrapText(true);

        // Détails
        HBox details = new HBox();
        details.getStyleClass().add("formation-details");

        //Label ordre = new Label("Ordre: " + formation.getNumOrdreCreation());
        //Label entreprise = new Label("ID: " + formation.getIdEntreprise());

        //details.getChildren().addAll(ordre, entreprise);

        // Bouton
        Button detailsBtn = new Button("Voir details");
        detailsBtn.getStyleClass().add("ghost");
        detailsBtn.setOnAction(e -> showFormationDetails(formation));

        card.getChildren().addAll(title, description, details, detailsBtn);

        return card;
    }

    private void updateSessionCount() {
        if (sessionsOuvertesCount != null) {
            sessionsOuvertesCount.setText(String.valueOf(formations.size()));
        }
    }

    private void displayFeaturedFormation() {
        if (formations.isEmpty() || heroCard == null) return;

        Formation featured = formations.stream()
                .min((f1, f2) -> Integer.compare(f1.getNumOrdreCreation(), f2.getNumOrdreCreation()))
                .orElse(formations.get(0));

        // Mettre à jour le titre
        Label featuredTitle = (Label) heroCard.lookup(".hero-card-title");
        if (featuredTitle != null) {
            featuredTitle.setText(featured.getTitre());
        }

        // Mettre à jour la description - SEULEMENT avant [MODULES]
        VBox heroCardBody = (VBox) heroCard.lookup(".hero-card-body");
        if (heroCardBody != null && heroCardBody.getChildren().size() > 1) {
            Label desc = (Label) heroCardBody.getChildren().get(1);
            String descriptionOnly = getDescriptionOnly(featured.getDescription());
            String formattedDesc = formatTextWithLineBreaks(descriptionOnly, WORDS_PER_LINE);
            desc.setText(formattedDesc);
        }

        // Mettre à jour les détails
        HBox detailGrid = (HBox) heroCard.lookup(".detail-grid");
        if (detailGrid != null) {
            updateDetailItem(detailGrid, 0, "18 fevrier 2026");
            updateDetailItem(detailGrid, 1, "15 jours");
            updateDetailItem(detailGrid, 2, "En ligne");
            updateDetailItem(detailGrid, 3, "Avancé");
        }
    }

    private void updateDetailItem(HBox grid, int index, String value) {
        if (grid.getChildren().size() > index) {
            VBox item = (VBox) grid.getChildren().get(index);
            if (item.getChildren().size() > 1) {
                Label valueLabel = (Label) item.getChildren().get(1);
                valueLabel.setText(value);
            }
        }
    }

    private void showFormationDetails(Formation formation) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la formation");
        alert.setHeaderText(formation.getTitre());

        // Description complète (avec modules si présents)
        String fullDescription = formation.getDescription();
        if (fullDescription != null) {
            fullDescription = fullDescription.replace("[MODULES]", "\n\nMODULES:\n");
        }

        String content = String.format(
                "%s\n\nOrdre de création: %d\nID Entreprise: %d\nImage: %s",
                fullDescription,
                formation.getNumOrdreCreation(),
                formation.getIdEntreprise(),
                formation.getImage() != null ? formation.getImage() : "Non disponible"
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleParticiper() {
        String nom = nomField.getText();
        String email = emailField.getText();
        String tel = telephoneField.getText();
        Formation selected = formationSelect.getValue();

        if (nom.isEmpty() || email.isEmpty() || tel.isEmpty() || selected == null) {
            showAlert(Alert.AlertType.WARNING,
                    "Erreur",
                    "Veuillez remplir tous les champs.");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION,
                "Inscription réussie",
                "Participation envoyée pour la formation : "
                        + selected.getTitre());

        clearFields();
    }

    private void clearFields() {
        nomField.clear();
        emailField.clear();
        telephoneField.clear();
        formationSelect.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleFilterAll() {
        displayFormations();
    }

    @FXML
    private void handleFilterPresentiel() {
        displayFormations();
    }

    @FXML
    private void handleFilterEnLigne() {
        displayFormations();
    }
}