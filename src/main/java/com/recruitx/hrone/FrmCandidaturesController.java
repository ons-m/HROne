package com.recruitx.hrone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FrmCandidaturesController {
    @FXML
    private ComboBox<String> offerFilter;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private TextField candidateSearch;

    @FXML
    private VBox candidateList;

    @FXML
    private Label candidateCount;

    @FXML
    private Label candidateEmpty;

    @FXML
    private Label filterSummary;

    @FXML
    private Button resetFilters;

    private final List<String> offers = new ArrayList<>();
    private final List<Candidate> candidates = new ArrayList<>();
    private final Map<Integer, Offer> offersById = new HashMap<>();

    @FXML
    private void initialize() {
        loadOffers();
        loadCandidatures();

        offerFilter.getItems().add("Toutes les offres");
        offerFilter.getItems().addAll(offers);
        offerFilter.getSelectionModel().selectFirst();

        statusFilter.getItems().setAll("Tous les statuts", "En attente", "Acceptee", "Rejetee");
        statusFilter.getSelectionModel().selectFirst();

        offerFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderCandidates());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderCandidates());
        candidateSearch.textProperty().addListener((obs, oldValue, newValue) -> renderCandidates());

        renderCandidates();
    }

    @FXML
    private void onResetFilters() {
        offerFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        candidateSearch.clear();
        renderCandidates();
    }

    private void loadOffers() {
        List<Offer> offerList = OfferController.AvoirListe();
        if (offerList == null) {
            offerList = new ArrayList<>();
        }

        offers.clear();
        offersById.clear();

        for (Offer offer : offerList) {
            offers.add(offer.getTitre());
            offersById.put(offer.getID_Offre(), offer);
        }
    }

    private void loadCandidatures() {
        List<Candidature> list = CandidatureController.AvoirListe();
        if (list == null) {
            list = new ArrayList<>();
        }

        candidates.clear();
        for (Candidature candidature : list) {
            Offer offer = offersById.get(candidature.getID_Offre());
            String offerTitle = offer != null
                    ? offer.getTitre()
                    : "Offre #" + candidature.getID_Offre();

            String name = "Candidat #" + candidature.getID_Candidat();
            String email = "";
            String experience = "";
            String status = statusLabelFromCode(candidature.getCode_Type_Status());
            String skills = buildSkills(candidature);

            candidates.add(
                    new Candidate(
                            candidature,
                            offerTitle,
                            name,
                            email,
                            experience,
                            status,
                            skills
                    )
            );
        }
    }

    private void renderCandidates() {
        String selectedOffer = offerFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        String query = normalize(candidateSearch.getText());

        int activeFilters = 0;
        if (selectedOffer != null && !selectedOffer.equals("Toutes les offres")) {
            activeFilters++;
        }
        if (selectedStatus != null && !selectedStatus.equals("Tous les statuts")) {
            activeFilters++;
        }
        if (!query.isBlank()) {
            activeFilters++;
        }
        filterSummary.setText("Filtres actifs: " + activeFilters);

        List<Candidate> filtered = new ArrayList<>();
        for (Candidate candidate : candidates) {
            boolean matchesOffer = selectedOffer == null
                || selectedOffer.equals("Toutes les offres")
                || candidate.offerTitle.equals(selectedOffer);
            boolean matchesStatus = selectedStatus == null
                || selectedStatus.equals("Tous les statuts")
                || candidate.status.equals(selectedStatus);
            boolean matchesQuery = query.isBlank() || normalize(candidate.searchText()).contains(query);

            if (matchesOffer && matchesStatus && matchesQuery) {
                filtered.add(candidate);
            }
        }

        candidateList.getChildren().clear();
        for (Candidate candidate : filtered) {
            candidateList.getChildren().add(buildCandidateItem(candidate));
        }

        candidateCount.setText(filtered.size() + " candidature" + (filtered.size() > 1 ? "s" : ""));
        boolean empty = filtered.isEmpty();
        candidateEmpty.setVisible(empty);
        candidateEmpty.setManaged(empty);
    }

    private HBox buildCandidateItem(Candidate candidate) {
        HBox item = new HBox();
        item.getStyleClass().add("resource-item");

        VBox info = new VBox();
        Label title = new Label(candidate.name);
        title.getStyleClass().add("resource-title");
        Label meta = new Label(candidate.offerTitle + " • " + candidate.email + " • " + candidate.experience);
        meta.getStyleClass().add("resource-meta");
        info.getChildren().addAll(title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox();
        actions.getStyleClass().add("list-item-actions");

        Label status = new Label(candidate.status);
        status.getStyleClass().addAll("status", statusClass(candidate.status));

        Button acceptBtn = new Button("Accepter");
        acceptBtn.getStyleClass().add("btn");
        acceptBtn.setOnAction(event -> updateStatus(candidate, 2, "Acceptee"));

        Button rejectBtn = new Button("Rejeter");
        rejectBtn.getStyleClass().addAll("btn", "btn-danger");
        rejectBtn.setOnAction(event -> updateStatus(candidate, 3, "Rejetee"));

        actions.getChildren().addAll(status, acceptBtn, rejectBtn);
        item.getChildren().addAll(info, spacer, actions);
        return item;
    }

    private void updateStatus(Candidate candidate, int statusCode, String statusLabel) {
        candidate.status = statusLabel;
        candidate.candidature.setCode_Type_Status(statusCode);

        boolean success = CandidatureController.Modifier(candidate.candidature);
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de mettre a jour le statut.");
            alert.showAndWait();
            return;
        }

        renderCandidates();
    }

    private String statusLabelFromCode(int code) {
        if (code == 2) {
            return "Acceptee";
        }
        if (code == 3) {
            return "Rejetee";
        }
        return "En attente";
    }

    private String buildSkills(Candidature candidature) {
        List<String> parts = new ArrayList<>();
        if (candidature.getLettre_Motivation() != null) {
            parts.add(candidature.getLettre_Motivation());
        }
        if (candidature.getPortfolio() != null) {
            parts.add(candidature.getPortfolio());
        }
        if (candidature.getLettre_Recomendation() != null) {
            parts.add(candidature.getLettre_Recomendation());
        }
        return String.join(" ", parts);
    }

    private String statusClass(String status) {
        if ("Acceptee".equals(status)) {
            return "status-live";
        }
        if ("Rejetee".equals(status)) {
            return "status-draft";
        }
        return "status-review";
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase(Locale.ROOT).trim();
    }

    private static final class Candidate {
        private final Candidature candidature;
        private final String offerTitle;
        private final String name;
        private final String email;
        private final String experience;
        private String status;
        private final String skills;

        private Candidate(
                Candidature candidature,
                String offerTitle,
                String name,
                String email,
                String experience,
                String status,
                String skills
        ) {
            this.candidature = candidature;
            this.offerTitle = offerTitle;
            this.name = name;
            this.email = email;
            this.experience = experience;
            this.status = status;
            this.skills = skills;
        }

        private String searchText() {
            return offerTitle + " " + name + " " + email + " " + skills;
        }
    }
}
