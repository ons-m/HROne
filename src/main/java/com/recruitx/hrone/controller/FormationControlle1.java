package com.recruitx.hrone.controller;

import com.recruitx.hrone.dao.FormationDAO;
import com.recruitx.hrone.models.Formation;
import com.recruitx.hrone.utils.COrdre;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import java.io.File;

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

    // Format de date pour l'affichage
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");

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
     * Convertit numOrdreCreation en date formatée
     */
    private String getDateFromNumOrdre(int numOrdre) {
        try {
            LocalDateTime dateTime = COrdre.GetDateFromNumOrdre(numOrdre);
            return dateTime.format(DATE_FORMATTER);
        } catch (Exception e) {
            System.err.println("Erreur de conversion de date: " + e.getMessage());
            return "Date inconnue";
        }
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
            return fullDescription.substring(0, markerIndex).trim();
        }

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

        // Détails avec date de création
        HBox details = new HBox();
        details.getStyleClass().add("formation-details");

        String dateCreation = getDateFromNumOrdre(formation.getNumOrdreCreation());
        Label dateLabel = new Label("📅 " + dateCreation);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        details.getChildren().add(dateLabel);

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

        // Mettre à jour la description
        VBox heroCardBody = (VBox) heroCard.lookup(".hero-card-body");
        if (heroCardBody != null && heroCardBody.getChildren().size() > 1) {
            Label desc = (Label) heroCardBody.getChildren().get(1);
            String descriptionOnly = getDescriptionOnly(featured.getDescription());
            String formattedDesc = formatTextWithLineBreaks(descriptionOnly, WORDS_PER_LINE);
            desc.setText(formattedDesc);
        }

        // Mettre à jour les détails avec la date de création
        HBox detailGrid = (HBox) heroCard.lookup(".detail-grid");
        if (detailGrid != null) {
            String dateCreation = getDateFromNumOrdre(featured.getNumOrdreCreation());
            updateDetailItem(detailGrid, 0, dateCreation);
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
        // Créer une dialog personnalisée
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la formation");

        // Conteneur principal
        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: white;");

        // === EN-TÊTE AVEC IMAGE ===
        HBox header = new HBox(20);
        header.setStyle("-fx-alignment: center-left;");

        // Image de la formation
        VBox imageContainer = new VBox();
        imageContainer.setStyle("-fx-alignment: center;");
        if (formation.getImage() != null && !formation.getImage().isEmpty()) {
            try {
                ImageView imageView = new ImageView();
                Image image = loadImage(formation.getImage());

                if (image != null) {
                    imageView.setImage(image);
                    imageView.setFitWidth(200);
                    imageView.setFitHeight(150);
                    imageView.setPreserveRatio(true);
                    imageView.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
                    imageContainer.getChildren().add(imageView);
                } else {
                    Label noImage = new Label("📷 Image non disponible");
                    noImage.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
                    imageContainer.getChildren().add(noImage);
                }
            } catch (Exception e) {
                Label noImage = new Label("📷 Erreur de chargement");
                noImage.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
                imageContainer.getChildren().add(noImage);
                System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            }
        } else {
            Label noImage = new Label("📷 Aucune image");
            noImage.setStyle("-fx-font-size: 14px; -fx-text-fill: #999;");
            imageContainer.getChildren().add(noImage);
        }

        // Informations générales
        VBox headerInfo = new VBox(10);
        headerInfo.setStyle("-fx-alignment: center-left;");
        HBox.setHgrow(headerInfo, javafx.scene.layout.Priority.ALWAYS);

        Label title = new Label(formation.getTitre());
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        title.setWrapText(true);

        HBox badges = new HBox(10);

        // Badge de date
        String dateCreation = getDateFromNumOrdre(formation.getNumOrdreCreation());
        Label badgeDate = new Label("📅 " + dateCreation);
        badgeDate.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");

        // Badge entreprise avec nom + référence
        String nomEntreprise = getEntrepriseNameById(formation.getIdEntreprise());
        String refEntreprise = getEntrepriseRefById(formation.getIdEntreprise());
        Label badgeEntreprise = new Label("🏢 " + nomEntreprise + " (" + refEntreprise + ")");
        badgeEntreprise.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");

        badges.getChildren().addAll(badgeDate, badgeEntreprise);

        headerInfo.getChildren().addAll(title, badges);
        header.getChildren().addAll(imageContainer, headerInfo);

        // === SÉPARATEUR ===
        javafx.scene.control.Separator separator1 = new javafx.scene.control.Separator();

        // === DESCRIPTION ===
        VBox descriptionSection = new VBox(10);

        Label descTitle = new Label("📋 Description");
        descTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        String descriptionOnly = getDescriptionOnly(formation.getDescription());
        String formattedDescription = formatTextWithLineBreaks(descriptionOnly, WORDS_PER_LINE);

        Label descContent = new Label(formattedDescription);
        descContent.setWrapText(true);
        descContent.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e; -fx-line-spacing: 3px;");
        descContent.setMaxWidth(650);

        descriptionSection.getChildren().addAll(descTitle, descContent);

        // === SÉPARATEUR ===
        javafx.scene.control.Separator separator2 = new javafx.scene.control.Separator();

        // === MODULES ===
        VBox modulesSection = new VBox(15);

        Label modulesTitle = new Label("📚 Programme - Modules");
        modulesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        String modulesText = extractModulesFromDescription(formation.getDescription());

        if (modulesText != null && !modulesText.isEmpty()) {
            String[] modules = modulesText.split("\n");

            VBox modulesList = new VBox(8);

            for (String module : modules) {
                module = module.trim();
                if (!module.isEmpty()) {
                    HBox moduleItem = new HBox(10);
                    moduleItem.setStyle("-fx-alignment: center-left; -fx-padding: 10; -fx-background-color: #f8f9fa; -fx-background-radius: 5;");

                    Label bullet = new Label("▸");
                    bullet.setStyle("-fx-font-size: 16px; -fx-text-fill: #3498db; -fx-font-weight: bold;");

                    Label moduleLabel = new Label(module);
                    moduleLabel.setWrapText(true);
                    moduleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c3e50;");
                    moduleLabel.setMaxWidth(600);
                    HBox.setHgrow(moduleLabel, javafx.scene.layout.Priority.ALWAYS);

                    moduleItem.getChildren().addAll(bullet, moduleLabel);
                    modulesList.getChildren().add(moduleItem);
                }
            }

            modulesSection.getChildren().addAll(modulesTitle, modulesList);
        } else {
            Label noModules = new Label("Aucun module disponible");
            noModules.setStyle("-fx-font-size: 13px; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
            modulesSection.getChildren().addAll(modulesTitle, noModules);
        }

        // === AJOUTER TOUS LES ÉLÉMENTS ===
        mainContent.getChildren().addAll(
                header,
                separator1,
                descriptionSection,
                separator2,
                modulesSection
        );

        // Mettre dans un ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(700);
        scrollPane.setPrefViewportHeight(600);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);

        // Boutons
        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        ButtonType participerButton = new ButtonType("Participer", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(participerButton, closeButton);

        // Style des boutons
        Button participerBtn = (Button) dialog.getDialogPane().lookupButton(participerButton);
        participerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20;");

        // Action sur le bouton Participer
        participerBtn.setOnAction(e -> {
            dialog.close();
            formationSelect.setValue(formation);
            showAlert(Alert.AlertType.INFORMATION,
                    "Formation sélectionnée",
                    "Veuillez remplir le formulaire de participation ci-dessous.");
        });

        dialog.showAndWait();
    }
    /**
     * Charge une image depuis un chemin local ou une URL web
     */
    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                System.out.println("Chargement de l'image depuis l'URL: " + imagePath);
                return new Image(imagePath, true);
            } else {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    System.out.println("Chargement de l'image locale: " + imagePath);
                    return new Image(imageFile.toURI().toString());
                } else {
                    System.out.println("Tentative de chargement depuis les ressources: " + imagePath);
                    String resourcePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
                    var stream = getClass().getResourceAsStream(resourcePath);
                    if (stream != null) {
                        return new Image(stream);
                    } else {
                        System.err.println("Image non trouvée: " + imagePath);
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de l'image '" + imagePath + "': " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrait les modules depuis la description (après [MODULES])
     */
    private String extractModulesFromDescription(String fullDescription) {
        if (fullDescription == null || fullDescription.isEmpty()) {
            return "";
        }

        int markerIndex = fullDescription.indexOf(MODULES_MARKER);
        if (markerIndex != -1 && markerIndex + MODULES_MARKER.length() < fullDescription.length()) {
            return fullDescription.substring(markerIndex + MODULES_MARKER.length()).trim();
        }

        return "";
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



    private String getEntrepriseNameById(int idEntreprise) {
        try {
            String nomEntreprise = formationDAO.getNameEntrepriseById(idEntreprise);
            if (nomEntreprise != null && !nomEntreprise.isEmpty()) {
                return nomEntreprise;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'entreprise: " + e.getMessage());
        }
        return "Entreprise inconnue";
    }

    private String getEntrepriseRefById(int idEntreprise) {
        try {
            String refEntreprise = formationDAO.getReferenceById(idEntreprise);
            if (refEntreprise != null && !refEntreprise.isEmpty()) {
                return refEntreprise;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'entreprise: " + e.getMessage());
        }
        return "Entreprise inconnue";
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