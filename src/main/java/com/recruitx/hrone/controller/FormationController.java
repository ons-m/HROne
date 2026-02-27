package com.recruitx.hrone.controller;
import com.recruitx.hrone.dao.FormationDAO;
import com.recruitx.hrone.dao.ParticipationFormationDAO;
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
import java.io.File;

public class FormationController {

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
        ObservableList<Formation> list = FXCollections.observableArrayList(formations);
        formationSelect.setItems(list);

        // ✅ Afficher places dans le ComboBox
        formationSelect.setCellFactory(lv -> new ListCell<Formation>() {
            @Override
            protected void updateItem(Formation f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText(null);
                } else {
                    int places = f.getPlacesRestantes();
                    String statut = places <= 0 ? " 🔴 Complet" : " 🟢 " + places + " places";
                    String mode = "en_ligne".equals(f.getMode()) ? " 🌐" : " 🏢";
                    setText(f.getTitre() + mode + statut);
                }
            }
        });

        formationSelect.setButtonCell(new ListCell<Formation>() {
            @Override
            protected void updateItem(Formation f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText("Sélectionner");
                } else {
                    int places = f.getPlacesRestantes();
                    String statut = places <= 0 ? " 🔴 Complet" : " 🟢 " + places + " places";
                    String mode = "en_ligne".equals(f.getMode()) ? " 🌐" : " 🏢";
                    setText(f.getTitre() + mode + statut);
                }
            }
        });
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

        // ── Titre ──
        Label title = new Label(formation.getTitre());
        title.getStyleClass().add("card-title");

        // ── Badges ──
        HBox badgeBox = new HBox(8);
        badgeBox.setStyle("-fx-padding: 4 0;");

        String mode = formation.getMode() != null ? formation.getMode() : "presentiel";
        int places = formation.getPlacesRestantes();
        int total = formation.getNombrePlaces();

        // Badge Mode
        Label modeBadge = new Label(
                "en_ligne".equals(mode) ? "🌐 En ligne" : "🏢 Présentiel"
        );
        modeBadge.setStyle(
                "en_ligne".equals(mode)
                        ? "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"
                        : "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"
        );

        // Badge Places
        Label placesLabel;
        if ("en_ligne".equals(mode)) {
            // En ligne → afficher seulement X places (pas illimité)
            placesLabel = new Label("🪑 " + places + "/" + total + " places");
        } else {
            // Présentiel → afficher X/Y places
            placesLabel = new Label("🪑 " + places + "/" + total + " places");
        }
        placesLabel.setStyle(
                "-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; " +
                        "-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px;"
        );

        // Badge Disponible / Complet
        Label statusBadge;
        if (places <= 0) {
            statusBadge = new Label("🔴 Complet");
            statusBadge.setStyle(
                    "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"
            );
        } else {
            statusBadge = new Label("🟢 Disponible");
            statusBadge.setStyle(
                    "-fx-background-color: #27ae60; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;"
            );
        }

        // ✅ Ajouter UNE SEULE fois chaque badge
        badgeBox.getChildren().addAll(modeBadge, placesLabel, statusBadge);

        // ── Description ──
        String descriptionOnly = getDescriptionOnly(formation.getDescription());
        String formattedDescription = formatTextWithLineBreaks(descriptionOnly, WORDS_PER_LINE);
        Label description = new Label(formattedDescription);
        description.getStyleClass().add("muted");
        description.setWrapText(true);

        // ── Date ──
        HBox details = new HBox();
        details.getStyleClass().add("formation-details");
        String dateCreation = getDateFromNumOrdre(formation.getNumOrdreCreation());
        Label dateLabel = new Label("📅 " + dateCreation);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        details.getChildren().add(dateLabel);

        // ── Bouton ──
        Button detailsBtn = new Button("Voir détails");
        detailsBtn.getStyleClass().add("ghost");
        detailsBtn.setOnAction(e -> showFormationDetails(formation));

        card.getChildren().addAll(title, badgeBox, description, details, detailsBtn);
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
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la formation");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: white;");

        // === EN-TÊTE AVEC IMAGE ===
        HBox header = new HBox(20);
        header.setStyle("-fx-alignment: center-left;");

        // Image
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

        // ── Ligne 1 de badges : Date + Entreprise ──
        HBox badges = new HBox(10);
        badges.setStyle("-fx-flex-wrap: wrap;");

        String dateCreation = getDateFromNumOrdre(formation.getNumOrdreCreation());
        Label badgeDate = new Label("📅 " + dateCreation);
        badgeDate.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 5;");

        String nomEntreprise = getEntrepriseNameById(formation.getIdEntreprise());
        String refEntreprise = getEntrepriseRefById(formation.getIdEntreprise());
        String entrepriseText = refEntreprise.isEmpty()
                ? "🏢 " + nomEntreprise
                : "🏢 " + nomEntreprise + " (" + refEntreprise + ")";
        Label badgeEntreprise = new Label(entrepriseText);
        badgeEntreprise.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 5;");

        // ✅ Badge Mode
        String modeText = "en_ligne".equals(formation.getMode()) ? "🌐 En ligne" : "🏢 Présentiel";
        String modeCouleur = "en_ligne".equals(formation.getMode()) ? "#2980b9" : "#27ae60";
        Label badgeMode = new Label(modeText);
        badgeMode.setStyle("-fx-background-color: " + modeCouleur + "; -fx-text-fill: white; " +
                "-fx-padding: 5 10; -fx-background-radius: 5;");

        badges.getChildren().addAll(badgeDate, badgeEntreprise, badgeMode);

        // ── Ligne 2 de badges : Durée + Places + Statut ──
        HBox badges2 = new HBox(10);

        // ✅ Badge Durée
        if (formation.getDateDebut() != 0 && formation.getDateFin() != 0) {
            java.time.LocalDateTime debut = COrdre.GetDateFromNumOrdre(formation.getDateDebut());
            java.time.LocalDateTime fin = COrdre.GetDateFromNumOrdre(formation.getDateFin());
            long jours = java.time.temporal.ChronoUnit.DAYS.between(debut, fin);

            String dureeText;
            if (jours <= 1) {
                dureeText = "⏱ 1 jour";
            } else if (jours < 30) {
                dureeText = "⏱ " + jours + " jours";
            } else if (jours < 365) {
                long mois = jours / 30;
                dureeText = "⏱ " + mois + " mois";
            } else {
                long ans = jours / 365;
                dureeText = "⏱ " + ans + " an" + (ans > 1 ? "s" : "");
            }

            Label badgeDuree = new Label(dureeText);
            badgeDuree.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; " +
                    "-fx-padding: 5 10; -fx-background-radius: 5;");
            badges2.getChildren().add(badgeDuree);
        }

        // ✅ Badge Places
        int places = formation.getPlacesRestantes();
        int total = formation.getNombrePlaces();
        if (total > 0) {
            Label badgePlaces = new Label("🪑 " + places + "/" + total + " places");
            badgePlaces.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; " +
                    "-fx-padding: 5 10; -fx-background-radius: 5;");
            badges2.getChildren().add(badgePlaces);
        }

        // ✅ Badge Disponible / Complet
        Label badgeStatut = places > 0
                ? new Label("🟢 Disponible")
                : new Label("🔴 Complet");
        badgeStatut.setStyle(places > 0
                ? "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;"
                : "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 5;");
        badges2.getChildren().add(badgeStatut);

        headerInfo.getChildren().addAll(title, badges, badges2);
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

        // === MODULES ===
        VBox modulesSection = createModulesSection(formation);

        // === ASSEMBLAGE ===
        mainContent.getChildren().addAll(
                header,
                separator1,
                descriptionSection,
                separator2,
                modulesSection
        );

        ScrollPane scrollPane = new ScrollPane(mainContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportWidth(750);
        scrollPane.setPrefViewportHeight(650);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);

        ButtonType closeButton = new ButtonType("Fermer", ButtonBar.ButtonData.OK_DONE);
        ButtonType participerButton = new ButtonType("Participer", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().addAll(participerButton, closeButton);

        Button participerBtn = (Button) dialog.getDialogPane().lookupButton(participerButton);
        participerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-padding: 10 20;");

        Button closeBtn = (Button) dialog.getDialogPane().lookupButton(closeButton);
        closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 10 20;");

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
// ✅ Ajouter en haut de la classe
    private ParticipationFormationDAO participationDAO = new ParticipationFormationDAO();

    @FXML
    private void handleParticiper() {
        String nom = nomField.getText().trim();
        String email = emailField.getText().trim();
        String tel = telephoneField.getText().trim();
        Formation selected = formationSelect.getValue();

        // ── 1. Validation champs ──
        if (nom.isEmpty() || email.isEmpty() || tel.isEmpty() || selected == null) {
            showAlert(Alert.AlertType.WARNING, "Erreur",
                    "Veuillez remplir tous les champs.");
            return;
        }

        int idFormation = selected.getIdFormation();

        // ── 2. Vérifier places disponibles ──
        int placesRestantes = participationDAO.getPlacesRestantes(idFormation);
        if (placesRestantes <= 0) {
            showAlert(Alert.AlertType.WARNING, "Formation saturée",
                    "⚠️ La formation \"" + selected.getTitre() + "\" est complète.\n" +
                            "Aucune place disponible pour le moment.");
            return;
        }

        // ── 3. Trouver l'ID du participant ──
        int idParticipant = participationDAO.getIdParticipantByEmail(email);
        if (idParticipant == -1) {
            showAlert(Alert.AlertType.WARNING, "Email introuvable",
                    "Aucun compte trouvé avec l'email : " + email);
            return;
        }

        // ── 4. Vérifier si déjà inscrit ──
        if (participationDAO.estDejaInscrit(idFormation, idParticipant)) {
            showAlert(Alert.AlertType.WARNING, "Déjà inscrit",
                    "Vous êtes déjà inscrit à la formation \"" + selected.getTitre() + "\".");
            return;
        }

        // ── 5. Insérer participation ──
        boolean succes = participationDAO.insererParticipation(idFormation, idParticipant);
        if (!succes) {
            showAlert(Alert.AlertType.ERROR, "Erreur",
                    "Une erreur est survenue. Réessayez.");
            return;
        }

        // ── 6. Rafraîchir la liste ──
        loadFormations();
        displayFormations();

        // ── 7. Message succès ──
        int placesApres = participationDAO.getPlacesRestantes(idFormation);
        String modeLabel = "en_ligne".equals(selected.getMode()) ? "🌐 En ligne" : "🏢 Présentiel";
        showAlert(Alert.AlertType.INFORMATION, "Inscription réussie",
                "✅ Inscription confirmée !\n\n" +
                        "Formation : " + selected.getTitre() + "\n" +
                        "Mode : " + modeLabel + "\n" +
                        "Places restantes : " + placesApres);

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
        formations = formationDAO.readAll(); // ✅ recharge toutes
        displayFormations();
        updateActiveFilter("all");
    }

    @FXML
    private void handleFilterPresentiel() {
        formations = formationDAO.readByMode("presentiel"); // ✅ filtre présentiel
        displayFormations();
        updateActiveFilter("presentiel");
    }

    @FXML
    private void handleFilterEnLigne() {
        formations = formationDAO.readByMode("en_ligne"); // ✅ filtre en ligne
        displayFormations();
        updateActiveFilter("en_ligne");
    }

    // ✅ Met à jour le style du bouton actif
    private void updateActiveFilter(String active) {
        // Récupérer les boutons depuis le FXML
        // On utilise lookup pour trouver les boutons
        formationGrid.getScene().getRoot().lookupAll(".filter").forEach(node -> {
            node.getStyleClass().remove("active");
        });

        String selector = switch (active) {
            case "presentiel" -> "Presentiel";
            case "en_ligne" -> "En ligne";
            default -> "Toutes";
        };

        formationGrid.getScene().getRoot().lookupAll(".filter").forEach(node -> {
            if (node instanceof Button btn && btn.getText().equals(selector)) {
                btn.getStyleClass().add("active");
            }
        });
    }
}