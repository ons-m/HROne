package com.recruitx.hrone;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FrmCandidatController {

    @FXML private TextField offerSearch;
    @FXML private ComboBox<String> offerLocation;
    @FXML private VBox offersGrid;
    @FXML private Label offerCount;
    @FXML private ComboBox<Offer> selectedOffer;

    @FXML private TextField fullName;
    @FXML private TextField email;
    @FXML private TextField portfolio;
    @FXML private TextField cv;
    @FXML private TextArea lettreMotivation;
    @FXML private TextField lettreRecommandation;

    List<Offer> offers = OfferController.AvoirListe();

    @FXML
    private void onApply() {

        List<String> errors = new ArrayList<>();

        if (selectedOffer.getValue() == null) {
            errors.add("Veuillez selectionner une offre.");
        }

        if (lettreMotivation.getText() == null || lettreMotivation.getText().isBlank()) {
            errors.add("La lettre de motivation est obligatoire.");
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Candidature incomplete");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return;
        }

        Candidature c = new Candidature();
        c.setID_Candidat(1); // TEMP – utilisateur connecte
        c.setID_Offre(selectedOffer.getValue().getID_Offre());
        c.setLettre_Motivation(lettreMotivation.getText().trim());
        c.setPortfolio(portfolio.getText());
        c.setLettre_Recomendation(lettreRecommandation.getText());
        c.setCode_Type_Status(1); // default

        boolean success = CandidatureController.Ajouter(c);

        if (success) {
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

        offerLocation.getItems().setAll(
                "Toutes les villes", "Casablanca", "Rabat", "Tanger", "Remote"
        );
        offerLocation.getSelectionModel().selectFirst();

        List<String> offerTitles = new ArrayList<>();
        for (Offer offer : offers) {
            offerTitles.add(offer.getTitre());
        }
        selectedOffer.getItems().setAll(offers);

        offerSearch.textProperty().addListener((obs, o, n) -> filterOffers());
        offerLocation.valueProperty().addListener((obs, o, n) -> filterOffers());

        filterOffers();
    }

    @FXML
    private void onSearch() {
        filterOffers();
    }

    private void filterOffers() {
        String query = normalize(offerSearch.getText());
        String location = offerLocation.getValue() == null ? "" : offerLocation.getValue();
        boolean allLocations =
                location.isBlank() || location.equalsIgnoreCase("Toutes les villes");

        List<Offer> filtered = new ArrayList<>();
        for (Offer offer : offers) {

            boolean matchesQuery =
                    query.isBlank() || normalize(buildSearchText(offer)).contains(query);

            boolean matchesLocation =
                    allLocations || offer.getWork_Type().equalsIgnoreCase(location);

            if (matchesQuery && matchesLocation) {
                filtered.add(offer);
            }
        }

        renderOffers(filtered);
        offerCount.setText(filtered.size() + " offre" + (filtered.size() > 1 ? "s" : ""));
    }

    private String buildSearchText(Offer offer) {
        return offer.getTitre() + " " +
                offer.getWork_Type() + " " +
                offer.getCode_Type_Contrat() + " " +
                offer.getDescription();
    }

    private void renderOffers(List<Offer> list) {
        offersGrid.getChildren().clear();
        for (Offer offer : list) {
            offersGrid.getChildren().add(buildOfferCard(offer));
        }
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

        Button applyBtn = new Button("Postuler");
        applyBtn.getStyleClass().addAll("btn", "btn-primary");
        applyBtn.setOnAction(e ->
                selectedOffer.getSelectionModel().select(offer)
        );

        footer.getChildren().addAll(detailsBtn, applyBtn);

        card.getChildren().addAll(header, meta, desc, footer);
        return card;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).trim();
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
