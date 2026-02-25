package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Repository.FormationRepository;
import com.recruitx.hrone.Models.Formation;
import com.recruitx.hrone.Utils.COrdre;
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
import java.io.File;

public class FrmFormation {

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

    private FormationRepository formationDAO;
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
        formationDAO = new FormationRepository();
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
     * Récupère le nom de l'entreprise par son ID
     */
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

    /**
     * Récupère la référence de l'entreprise par son ID
     */
    private String getEntrepriseRefById(int idEntreprise) {
        try {
            String refEntreprise = formationDAO.getReferenceById(idEntreprise);
            if (refEntreprise != null && !refEntreprise.isEmpty()) {
                return refEntreprise;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération de l'entreprise: " + e.getMessage());
        }
        return "";
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

    /**
     * Affiche les détails de la formation dans une dialog personnalisée
     */
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

        // Badge entreprise
        String nomEntreprise = getEntrepriseNameById(formation.getIdEntreprise());
        String refEntreprise = getEntrepriseRefById(formation.getIdEntreprise());
        String entrepriseText = refEntreprise.isEmpty() ?
                "🏢 " + nomEntreprise :
                "🏢 " + nomEntreprise + " (" + refEntreprise + ")";
        Label badgeEntreprise = new Label(entrepriseText);
        badgeEntreprise.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");

        badges.getChildren().addAll(badgeDate, badgeEntreprise);

        headerInfo.getChildren().addAll(title, badges);
        header.getChildren().addAll(imageContainer, headerInfo);

        // === SÉPARATEUR ===
        Separator separator1 = new Separator();

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
        Separator separator2 = new Separator();

        // === MODULES STRUCTURÉS ===
        VBox modulesSection = createModulesSection(formation);

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
        scrollPane.setPrefViewportWidth(750);
        scrollPane.setPrefViewportHeight(650);
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
     * Crée la section des modules de façon structurée (style Coursera/Udemy)
     */
    private VBox createModulesSection(Formation formation) {

        VBox modulesSection = new VBox(20);
        modulesSection.setPadding(new Insets(20));
        modulesSection.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10;");

        Label modulesTitle = new Label("Programme de la formation");
        modulesTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        String modulesText = extractModulesFromDescription(formation.getDescription());

        if (modulesText == null || modulesText.isBlank()) {
            Label noModules = new Label("Aucun module disponible.");
            noModules.setStyle("-fx-text-fill: #6b7280;");
            modulesSection.getChildren().addAll(modulesTitle, noModules);
            return modulesSection;
        }

        String[] moduleLines = modulesText.split("\n");

        int totalModules = 0;
        int totalMinutes = 0;

        for (String line : moduleLines) {
            line = line.trim();
            if (line.matches("^\\d+\\..*")) {
                totalModules++;
                totalMinutes += extractDurationInMinutes(line);
            }
        }

        // ===== Summary Header =====
        HBox summary = new HBox(30);
        summary.setPadding(new Insets(15));
        summary.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e5e7eb; -fx-border-radius: 8;");

        Label countLabel = new Label(totalModules + " modules");
        countLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label durationLabel = new Label(formatDuration(totalMinutes));
        durationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        summary.getChildren().addAll(countLabel, durationLabel);

        // ===== Accordion (Collapsible Modules like Coursera) =====
        Accordion accordion = new Accordion();

        int moduleNumber = 0;
        for (String line : moduleLines) {
            line = line.trim();
            if (!line.isEmpty() && line.matches("^\\d+\\..*")) {
                moduleNumber++;
                TitledPane pane = createModuleCard(line, moduleNumber);
                accordion.getPanes().add(pane);
            }
        }

        if (!accordion.getPanes().isEmpty()) {
            accordion.setExpandedPane(accordion.getPanes().get(0));
        }

        modulesSection.getChildren().addAll(modulesTitle, summary, accordion);

        return modulesSection;
    }

    /**
     * Extrait la durée en minutes depuis une ligne de module
     */
    private int extractDurationInMinutes(String line) {
        int minutes = 0;

        try {
            if (line.contains("jour")) {
                String[] parts = line.split("jour");
                if (parts.length > 0) {
                    String durePart = parts[0];
                    // Chercher le dernier nombre avant "jour"
                    String[] numbers = durePart.split("[^0-9.,]+");
                    for (int i = numbers.length - 1; i >= 0; i--) {
                        if (!numbers[i].isEmpty()) {
                            double jours = Double.parseDouble(numbers[i].replace(",", "."));
                            minutes = (int) (jours * 8 * 60); // 8 heures par jour
                            break;
                        }
                    }
                }
            } else if (line.contains("heure")) {
                String[] parts = line.split("heure");
                if (parts.length > 0) {
                    String durePart = parts[0];
                    String[] numbers = durePart.split("[^0-9.,]+");
                    for (int i = numbers.length - 1; i >= 0; i--) {
                        if (!numbers[i].isEmpty()) {
                            double heures = Double.parseDouble(numbers[i].replace(",", "."));
                            minutes = (int) (heures * 60);
                            break;
                        }
                    }
                }
            } else if (line.contains("minute")) {
                String[] parts = line.split("minute");
                if (parts.length > 0) {
                    String durePart = parts[0];
                    String[] numbers = durePart.split("[^0-9]+");
                    for (int i = numbers.length - 1; i >= 0; i--) {
                        if (!numbers[i].isEmpty()) {
                            minutes = Integer.parseInt(numbers[i]);
                            break;
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.err.println("Erreur d'extraction de durée: " + e.getMessage());
        }

        return minutes;
    }

    /**
     * Formate la durée totale
     */
    private String formatDuration(int totalMinutes) {
        if (totalMinutes == 0) {
            return "";
        }

        if (totalMinutes >= 60) {
            int heures = totalMinutes / 60;
            int minutes = totalMinutes % 60;
            if (minutes > 0) {
                return "⏱ Total " + heures + "h " + minutes + "min";
            } else {
                return "⏱ Total " + heures + " heure" + (heures > 1 ? "s" : "");
            }
        } else {
            return "⏱ Total " + totalMinutes + " minutes";
        }
    }

    /**
     * Crée une carte pour un module individuel
     */
    private TitledPane createModuleCard(String moduleLine, int moduleNumber) {

        String moduleText = moduleLine.replaceFirst("^\\d+\\.\\s*", "");
        String titre = moduleText;
        String duree = "";

        if (moduleText.contains("(") && moduleText.contains(")")) {
            int start = moduleText.lastIndexOf("(");
            int end = moduleText.lastIndexOf(")");
            if (start < end) {
                duree = moduleText.substring(start + 1, end);
                titre = moduleText.substring(0, start).trim();
            }
        }

        // ===== Header Text =====
        String headerText = "Module " + moduleNumber + " • " + titre;
        if (!duree.isEmpty()) {
            headerText += "  (" + duree + ")";
        }

        // ===== Content (inside accordion) =====
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setStyle("-fx-background-color: white;");

        Label videos = new Label("• Vidéos pédagogiques");
        Label exercises = new Label("• Exercices pratiques");
        Label resources = new Label("• Ressources téléchargeables");

        videos.setStyle("-fx-text-fill: #374151;");
        exercises.setStyle("-fx-text-fill: #374151;");
        resources.setStyle("-fx-text-fill: #374151;");

        content.getChildren().addAll(videos, exercises, resources);

        TitledPane pane = new TitledPane(headerText, content);

        pane.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-color: white;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        return pane;
    }

    /**
     * Charge une image depuis une URL réseau ou un fichier local
     */
    private Image loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            // URLs réseau (http ou https)
            if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
                System.out.println("✅ Chargement de l'image depuis l'URL réseau: " + imagePath);
                return new Image(imagePath, true);
            }
            // Fichiers locaux avec protocole file://
            else if (imagePath.startsWith("file:/")) {
                System.out.println("✅ Chargement de l'image depuis fichier local (file://): " + imagePath);
                return new Image(imagePath, true);
            }
            // Chemin absolu sans protocole
            else {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    System.out.println("✅ Chargement de l'image depuis chemin local: " + imagePath);
                    return new Image(imageFile.toURI().toString());
                } else {
                    // Tentative de chargement depuis les ressources
                    System.out.println("⚠️ Tentative de chargement depuis les ressources: " + imagePath);
                    String resourcePath = imagePath.startsWith("/") ? imagePath : "/" + imagePath;
                    var stream = getClass().getResourceAsStream(resourcePath);
                    if (stream != null) {
                        System.out.println("✅ Image chargée depuis les ressources");
                        return new Image(stream);
                    } else {
                        System.err.println("❌ Fichier image non trouvé: " + imagePath);
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement de l'image '" + imagePath + "': " + e.getMessage());
            return null;
        }
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
        // TODO: Implémenter le filtrage par mode présentiel
        displayFormations();
    }

    @FXML
    private void handleFilterEnLigne() {
        // TODO: Implémenter le filtrage par mode en ligne
        displayFormations();
    }
}