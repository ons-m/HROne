package com.recruitx.hrone.Controllers;

import java.awt.Desktop;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.io.File;
import java.net.URI;

import com.recruitx.hrone.API.ArbeitnowJobService;
import com.recruitx.hrone.API.ArbeitnowJobService.ExternalJobOffer;
import com.recruitx.hrone.API.ArbeitnowJobService.SearchResult;
import com.recruitx.hrone.Models.*;
import com.recruitx.hrone.Repository.*;
import com.recruitx.hrone.Utils.ActionLogger;
import com.recruitx.hrone.Utils.DBHelper;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;


public class FrmCandidat implements NavigationAware {

     private FrmMain mainController;

    @FXML private TextField offerSearch;
    @FXML private ComboBox<String> offerLocation;
    @FXML private ComboBox<String> offerContractType;
    @FXML private TextField salaryMin;
    @FXML private TextField salaryMax;
    @FXML private CheckBox showFavoritesOnly;
    @FXML private VBox offersGrid;
    @FXML private Label offerCount;
    @FXML private Label externalOffersStatus;
    @FXML private ComboBox<Offer> selectedOffer;

    @FXML private TextField fullName;
    @FXML private TextField email;
    @FXML private TextField portfolio;
    @FXML private TextField cv;
    @FXML private TextArea lettreMotivation;
    @FXML private TextField lettreRecommandation;

    List<Offer> offers = OfferRepository.AvoirListe();
    private Map<String, String> competenceLabelsByCode = new HashMap<>();
    private Map<String, String> langueLabelsByCode = new HashMap<>();
    private Map<String, String> backgroundLabelsByCode = new HashMap<>();
    private Map<String, String> contratLabelsByCode = new HashMap<>();
    private Map<String, String> niveauLabelsByCode = new HashMap<>();
    private final Set<Integer> favoriteOfferIds = new HashSet<>();
    private List<ExternalJobOffer> externalOffers = new ArrayList<>();

    @Override
    public void setMainController(FrmMain mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void onShowMyApplications() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.MES_CANDIDATURES);
        }
    }

    @FXML
    private void onApply() {

        List<String> errors = new ArrayList<>();
        final int currentCandidateId = Session.getCurrentUser().getIdUtilisateur();

        if (selectedOffer.getValue() == null) {
            errors.add("Veuillez selectionner une offre.");
        }

        if (lettreMotivation.getText() == null || lettreMotivation.getText().isBlank()) {
            errors.add("La lettre de motivation est obligatoire.");
        }

        if (cv.getText() == null || cv.getText().isBlank()) {
            errors.add("Veuillez selectionner un fichier CV depuis votre ordinateur.");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Candidature incomplete");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return;
        }

        boolean cvSaved = saveCandidateCvPath(currentCandidateId, cv.getText().trim());
        if (!cvSaved) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur CV");
            alert.setContentText("Impossible d'enregistrer le chemin du CV.");
            alert.showAndWait();
            return;
        }

        int cID = CandidatRepository.GetIDCandidatFromIDUser(currentCandidateId);

        Candidature c = new Candidature();
        c.setID_Candidat(cID);
        c.setID_Offre(selectedOffer.getValue().getID_Offre());
        c.setLettre_Motivation(lettreMotivation.getText().trim());
        c.setPortfolio(portfolio.getText());
        c.setLettre_Recomendation(lettreRecommandation.getText());
        c.setCode_Type_Status(1); // default

        boolean success = CandidatureRepository.Ajouter(c);

        if (success) {
            ActionLogger.log("Appliquer à Offre", "Candidature envoyée pour l'offre ID=" + c.getID_Offre());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Candidature envoyee");
            alert.setContentText("Votre candidature a ete envoyee avec succes.");
            alert.showAndWait();
            clearApplyForm();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible d'envoyer la candidature.");
            alert.showAndWait();
        }
    }

    @FXML
    private void initialize() {
        if (offers == null) {
            offers = new ArrayList<>();
        }

        cv.setEditable(false);

        competenceLabelsByCode = buildLookupMap(OfferRepository.loadCompetencesFromDB());
        langueLabelsByCode = buildLookupMap(OfferRepository.loadLanguesFromDB());
        backgroundLabelsByCode = buildLookupMap(OfferRepository.loadBackgroundsFromDB());
        contratLabelsByCode = buildLookupMap(OfferRepository.loadTypesContratFromDB());
        niveauLabelsByCode = buildLookupMap(OfferRepository.loadNiveauxEtudeFromDB());

        offerLocation.getItems().setAll(
                "Toutes les villes", "Casablanca", "Rabat", "Tanger", "Remote"
        );
        offerLocation.getSelectionModel().selectFirst();

        offerContractType.getItems().setAll(
            "Tous les contrats", "Freelancer", "CDI", "CDD", "Stage"
        );
        offerContractType.getSelectionModel().selectFirst();

        List<String> offerTitles = new ArrayList<>();
        for (Offer offer : offers) {
            offerTitles.add(offer.getTitre());
        }
        selectedOffer.getItems().setAll(offers);

        offerSearch.textProperty().addListener((obs, o, n) -> filterOffers());
        offerLocation.valueProperty().addListener((obs, o, n) -> filterOffers());
        offerContractType.valueProperty().addListener((obs, o, n) -> filterOffers());
        salaryMin.textProperty().addListener((obs, o, n) -> filterOffers());
        salaryMax.textProperty().addListener((obs, o, n) -> filterOffers());
        showFavoritesOnly.selectedProperty().addListener((obs, o, n) -> filterOffers());

        filterOffers();
        loadExternalOffersAsync();
    }

    @FXML
    private void onBrowseCvFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selectionner un fichier CV");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers CV", "*.pdf", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selected = chooser.showOpenDialog(cv.getScene() != null ? cv.getScene().getWindow() : null);
        if (selected != null) {
            cv.setText(selected.getAbsolutePath());
        }
    }

    @FXML
    private void onSearch() {
        filterOffers();
        loadExternalOffersAsync();
    }

    private void loadExternalOffersAsync() {
        String query = offerSearch == null ? "" : offerSearch.getText();
        setExternalStatus("Chargement des offres Europe (Arbeitnow)...");

        CompletableFuture
                .supplyAsync(() -> ArbeitnowJobService.fetchOffers(query))
                .thenAccept(result -> Platform.runLater(() -> {
                    SearchResult safeResult = result == null
                            ? SearchResult.failure(new ArrayList<>(), "Reponse vide")
                            : result;

                    externalOffers = safeResult.getOffers();

                    if (!safeResult.isSuccess()) {
                        setExternalStatus("Offres externes indisponibles: " + safeResult.getMessage());
                    } else if (externalOffers.isEmpty()) {
                        setExternalStatus("Aucune offre externe trouvee.");
                    } else {
                        setExternalStatus("Offres externes chargees: " + externalOffers.size());
                    }

                    filterOffers();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        externalOffers = new ArrayList<>();
                        setExternalStatus("Erreur externe: " + ex.getMessage());
                        filterOffers();
                    });
                    return null;
                });
    }

    private void filterOffers() {
        String query = normalize(offerSearch.getText());
        String location = offerLocation.getValue() == null ? "" : offerLocation.getValue();
        String contractType = offerContractType.getValue() == null ? "" : offerContractType.getValue();
        Double salaryMinValue = parseSalaryValue(salaryMin.getText());
        Double salaryMaxValue = parseSalaryValue(salaryMax.getText());
        boolean favoritesOnly = showFavoritesOnly != null && showFavoritesOnly.isSelected();

        boolean allLocations =
                location.isBlank() || location.equalsIgnoreCase("Toutes les villes");
        boolean allContracts =
            contractType.isBlank() || contractType.equalsIgnoreCase("Tous les contrats");

        List<Offer> filtered = new ArrayList<>();
        for (Offer offer : offers) {

            boolean matchesQuery =
                    query.isBlank() || normalize(buildSearchText(offer)).contains(query);

            boolean matchesLocation =
                    allLocations || offer.getWork_Type().equalsIgnoreCase(location);

            boolean matchesContract = allContracts || matchesContractType(offer, contractType);

            boolean matchesSalary = matchesSalaryRange(offer, salaryMinValue, salaryMaxValue);
            boolean matchesFavorite = !favoritesOnly || favoriteOfferIds.contains(offer.getID_Offre());

            if (matchesQuery && matchesLocation && matchesContract && matchesSalary && matchesFavorite) {
                filtered.add(offer);
            }
        }

        List<ExternalJobOffer> visibleExternal = favoritesOnly ? new ArrayList<>() : externalOffers;

        renderOffers(filtered, visibleExternal);
        int total = filtered.size() + visibleExternal.size();
        offerCount.setText(total + " offre" + (total > 1 ? "s" : ""));
    }

    private void setExternalStatus(String message) {
        if (externalOffersStatus == null) {
            return;
        }

        String text = message == null ? "" : message.trim();
        externalOffersStatus.setText(text);
        boolean hasText = !text.isBlank();
        externalOffersStatus.setManaged(hasText);
        externalOffersStatus.setVisible(hasText);
    }

    private String buildSearchText(Offer offer) {
        return offer.getTitre() + " " +
                offer.getWork_Type() + " " +
                offer.getCode_Type_Contrat() + " " +
                offer.getDescription();
    }

    private boolean matchesContractType(Offer offer, String selectedType) {
        String offerContractCode = normalizeCode(offer.getCode_Type_Contrat());
        String translatedContract = normalize(translateCode(offer.getCode_Type_Contrat(), contratLabelsByCode));
        String selectedNormalized = normalize(selectedType);

        return translatedContract.contains(selectedNormalized)
                || normalize(offerContractCode).contains(selectedNormalized);
    }

    private boolean matchesSalaryRange(Offer offer, Double salaryMinFilter, Double salaryMaxFilter) {
        if (salaryMinFilter == null && salaryMaxFilter == null) {
            return true;
        }

        double offerMin = offer.getMin_Salaire();
        double offerMax = offer.getMax_Salaire();

        if (offerMin <= 0 && offerMax <= 0) {
            return false;
        }

        if (offerMin <= 0) {
            offerMin = offerMax;
        }
        if (offerMax <= 0) {
            offerMax = offerMin;
        }

        if (salaryMinFilter != null && offerMax < salaryMinFilter) {
            return false;
        }
        if (salaryMaxFilter != null && offerMin > salaryMaxFilter) {
            return false;
        }

        return true;
    }

    private Double parseSalaryValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void renderOffers(List<Offer> list, List<ExternalJobOffer> externalList) {
        offersGrid.getChildren().clear();

        for (Offer offer : list) {
            offersGrid.getChildren().add(buildOfferCard(offer));
        }

        if (externalList == null || externalList.isEmpty()) {
            return;
        }

        for (ExternalJobOffer offer : externalList) {
            offersGrid.getChildren().add(buildExternalOfferCard(offer));
        }
    }

    private VBox buildExternalOfferCard(ExternalJobOffer offer) {
        VBox card = new VBox();
        card.getStyleClass().add("offer-card");

        HBox header = new HBox();
        header.getStyleClass().add("offer-card-header");

        Label title = new Label(orDefault(offer.getTitle()));
        title.getStyleClass().add("offer-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label tag = new Label("Externe EU");
        tag.getStyleClass().add("tag");

        header.getChildren().addAll(title, spacer, tag);

        Label meta = new Label(
                orDefault(offer.getCompany()) + " • " +
                        orDefault(offer.getLocation()) + " • " +
                        orDefault(offer.getEmploymentType())
        );
        meta.getStyleClass().add("offer-meta");

        Label desc = new Label(compactText(offer.getDescription(), 260));
        desc.getStyleClass().add("offer-desc");
        desc.setWrapText(true);

        HBox footer = new HBox();
        footer.getStyleClass().add("offer-footer");

        Button detailsBtn = new Button("Voir details");
        detailsBtn.getStyleClass().addAll("btn", "btn-ghost");
        detailsBtn.setOnAction(e -> showExternalOfferDetails(offer));

        Button applyBtn = new Button("Postuler sur site");
        applyBtn.getStyleClass().addAll("btn", "btn-primary");
        applyBtn.setOnAction(e -> openExternalUrl(offer.getApplyUrl()));

        footer.getChildren().addAll(detailsBtn, applyBtn);

        card.getChildren().addAll(header, meta, desc, footer);
        return card;
    }

    private VBox buildOfferCard(Offer offer) {
        VBox card = new VBox();
        card.getStyleClass().add("offer-card");

        HBox header = new HBox();
        header.getStyleClass().add("offer-card-header");

        Label title = new Label(offer.getTitre());
        title.getStyleClass().add("offer-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Label tag = new Label(offer.getCode_Type_Contrat());
        tag.getStyleClass().add("tag");

        header.getChildren().addAll(title, spacer, tag);

        Label meta = new Label(
                offer.getWork_Type() + " • " +
                        offer.getNbr_Annee_Experience() + " ans"
        );
        meta.getStyleClass().add("offer-meta");

        Label desc = new Label(offer.getDescription());
        desc.getStyleClass().add("offer-desc");
        desc.setWrapText(true);

        HBox footer = new HBox();
        footer.getStyleClass().add("offer-footer");

        Button detailsBtn = new Button("Voir details");
        detailsBtn.getStyleClass().addAll("btn", "btn-ghost");
        detailsBtn.setOnAction(e -> showOfferDetails(offer));

        Button favoriteBtn = new Button();
        favoriteBtn.getStyleClass().addAll("btn", "btn-favorite");
        syncFavoriteButton(favoriteBtn, offer);
        favoriteBtn.setOnAction(e -> toggleFavorite(offer));

        Button applyBtn = new Button("Postuler");
        applyBtn.getStyleClass().addAll("btn", "btn-primary");
        applyBtn.setOnAction(e ->
                selectedOffer.getSelectionModel().select(offer)
        );

        footer.getChildren().addAll(detailsBtn, favoriteBtn, applyBtn);

        card.getChildren().addAll(header, meta, desc, footer);
        return card;
    }

    private void showOfferDetails(Offer offer) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Details de l'offre");
        details.setHeaderText(offer.getTitre());
        details.initModality(Modality.APPLICATION_MODAL);

        TextArea content = new TextArea(buildOfferDetailsText(offer));
        content.setWrapText(true);
        content.setEditable(false);
        content.setPrefRowCount(18);
        details.getDialogPane().setContent(content);

        ButtonType applyNow = new ButtonType("Appliquer maintenant", ButtonBar.ButtonData.OK_DONE);
        details.getButtonTypes().setAll(applyNow, ButtonType.CLOSE);

        details.showAndWait().ifPresent(type -> {
            if (type == applyNow) {
                selectedOffer.getSelectionModel().select(offer);
                lettreMotivation.requestFocus();
            }
        });
    }

    private void showExternalOfferDetails(ExternalJobOffer offer) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Details de l'offre externe");
        details.setHeaderText(orDefault(offer.getTitle()));
        details.initModality(Modality.APPLICATION_MODAL);

        StringBuilder builder = new StringBuilder();
        builder.append("Societe: ").append(orDefault(offer.getCompany())).append("\n");
        builder.append("Localisation: ").append(orDefault(offer.getLocation())).append("\n");
        builder.append("Type: ").append(orDefault(offer.getEmploymentType())).append("\n");
        builder.append("Source: ").append(orDefault(offer.getSourceWebsite())).append("\n\n");
        builder.append("Description:\n").append(orDefault(offer.getDescription())).append("\n\n");
        builder.append("Lien de candidature:\n").append(orDefault(offer.getApplyUrl()));

        TextArea content = new TextArea(builder.toString());
        content.setWrapText(true);
        content.setEditable(false);
        content.setPrefRowCount(18);
        details.getDialogPane().setContent(content);

        ButtonType applyNow = new ButtonType("Postuler sur le site", ButtonBar.ButtonData.OK_DONE);
        details.getButtonTypes().setAll(applyNow, ButtonType.CLOSE);

        details.showAndWait().ifPresent(type -> {
            if (type == applyNow) {
                openExternalUrl(offer.getApplyUrl());
            }
        });
    }

    private void openExternalUrl(String url) {
        if (url == null || url.isBlank()) {
            showError("Lien indisponible", "Aucun lien de candidature n'est disponible pour cette offre.");
            return;
        }

        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
                return;
            }
        } catch (Exception ex) {
            showError("Impossible d'ouvrir le lien", "Le navigateur n'a pas pu ouvrir ce lien: " + url);
            return;
        }

        showError("Desktop non supporte", "Ouverture du navigateur non supportee sur cet environnement.");
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private String compactText(String value, int maxLength) {
        String normalized = orDefault(value)
                .replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.length() <= maxLength) {
            return normalized;
        }

        return normalized.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private void toggleFavorite(Offer offer) {
        int offerId = offer.getID_Offre();
        if (favoriteOfferIds.contains(offerId)) {
            favoriteOfferIds.remove(offerId);
            filterOffers();
            return;
        }
        favoriteOfferIds.add(offerId);
        filterOffers();
    }

    private void syncFavoriteButton(Button button, Offer offer) {
        boolean favorite = favoriteOfferIds.contains(offer.getID_Offre());
        button.getStyleClass().remove("btn-favorite-active");
        if (favorite) {
            button.setText("Retirer favori");
            button.getStyleClass().add("btn-favorite-active");
            return;
        }
        button.setText("Ajouter favoris");
    }

    private String buildOfferDetailsText(Offer offer) {
        StringBuilder builder = new StringBuilder();
        builder.append("Contrat: ")
                .append(translateCode(offer.getCode_Type_Contrat(), contratLabelsByCode))
                .append("\n");
        builder.append("Localisation / Work type: ").append(orDefault(offer.getWork_Type())).append("\n");
        builder.append("Experience requise: ").append(offer.getNbr_Annee_Experience()).append(" an(s)\n");
        builder.append("Niveau d'etude: ")
                .append(translateCode(offer.getCode_Type_Niveau_Etude(), niveauLabelsByCode))
                .append("\n");
        builder.append("Salaire: ").append(formatSalary(offer.getMin_Salaire(), offer.getMax_Salaire())).append("\n");
        builder.append("\nDescription:\n").append(orDefault(offer.getDescription())).append("\n\n");
        builder.append("Competences: ")
                .append(joinTranslatedValues(offer.getCodes_Competences(), competenceLabelsByCode))
                .append("\n");
        builder.append("Langues: ")
                .append(joinTranslatedValues(offer.getCodes_Langues(), langueLabelsByCode))
                .append("\n");
        builder.append("Backgrounds: ")
                .append(joinTranslatedValues(offer.getCodes_Backgrounds(), backgroundLabelsByCode));
        return builder.toString();
    }

    private String joinTranslatedValues(List<String> values, Map<String, String> labelsByCode) {
        if (values == null || values.isEmpty()) {
            return "N/A";
        }
        String result = values.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> translateCode(v, labelsByCode))
                .collect(Collectors.joining(", "));
        return result.isBlank() ? "N/A" : result;
    }

    private String translateCode(String code, Map<String, String> labelsByCode) {
        String normalized = normalizeCode(code);
        if (normalized.isBlank()) {
            return "N/A";
        }
        String label = labelsByCode.get(normalized);
        return (label == null || label.isBlank()) ? normalized : label;
    }

    private String normalizeCode(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private Map<String, String> buildLookupMap(List<TypeItem> items) {
        Map<String, String> map = new HashMap<>();
        if (items == null) {
            return map;
        }

        for (TypeItem item : items) {
            if (item == null) {
                continue;
            }
            String code = normalizeCode(item.getCode());
            String label = normalizeCode(item.getLabel());
            if (!code.isBlank() && !label.isBlank()) {
                map.put(code, label);
            }
        }
        return map;
    }

    private String orDefault(String value) {
        if (value == null || value.isBlank()) {
            return "N/A";
        }
        return value;
    }

    private String formatSalary(double min, double max) {
        if (min <= 0 && max <= 0) {
            return "N/A";
        }
        if (min > 0 && max > 0) {
            return String.format(Locale.ROOT, "%.0f - %.0f MAD", min, max);
        }
        if (min > 0) {
            return String.format(Locale.ROOT, "A partir de %.0f MAD", min);
        }
        return String.format(Locale.ROOT, "Jusqu'a %.0f MAD", max);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).trim();
    }

    private boolean saveCandidateCvPath(int candidateId, String cvPath) {
        try {
            String safePath = cvPath.replace("'", "''");
            String sql =
                    "UPDATE CONDIDAT SET CV = '" + safePath + "' WHERE ID_CONDIDAT = " + candidateId;
            return DBHelper.ExecuteQuery(sql) >= 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private void clearApplyForm() {
        fullName.clear();
        email.clear();
        selectedOffer.getSelectionModel().clearSelection();
        lettreMotivation.clear();
        portfolio.clear();
        cv.clear();
        lettreRecommandation.clear();
    }
}
