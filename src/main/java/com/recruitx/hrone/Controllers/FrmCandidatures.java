package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.*;
import com.recruitx.hrone.Repository.*;
import com.recruitx.hrone.Utils.CError;
import com.recruitx.hrone.Utils.COrdre;
import com.recruitx.hrone.Utils.DBHelper;
import com.recruitx.hrone.Utils.LogType;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FrmCandidatures {
    private static final DateTimeFormatter INTERVIEW_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

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
    private final Map<Integer, CandidateProfile> candidateProfilesById = new HashMap<>();

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
        List<Offer> offerList = OfferRepository.AvoirListe();
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
        loadCandidateProfiles();

        List<Candidature> list = CandidatureRepository.AvoirListe();
        if (list == null) {
            list = new ArrayList<>();
        }

        candidates.clear();
        for (Candidature candidature : list) {
            Offer offer = offersById.get(candidature.getID_Offre());
            String offerTitle = offer != null
                    ? offer.getTitre()
                    : "Offre #" + candidature.getID_Offre();

            CandidateProfile profile = candidateProfilesById.get(candidature.getID_Candidat());
            String name = (profile != null && !profile.name.isBlank())
                ? profile.name
                : "Candidat #" + candidature.getID_Candidat();
            String email = profile != null ? safe(profile.email) : "";
            String experience = profile != null ? buildProfileSummary(profile) : "";
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
                            skills,
                            profile
                    )
            );
        }
    }

    private void loadCandidateProfiles() {
        candidateProfilesById.clear();

        String sql = "SELECT c.ID_Condidat, c.CV, u.Nom_Utilisateur, u.Email, u.Num_Tel, u.Adresse, u.CIN, u.Date_Naissance " +
                "FROM Condidat c " +
                "LEFT JOIN Utilisateur u ON u.ID_Utilisateur = c.ID_Utilisateur";

        ResultSet rs = null;
        try {
            rs = DBHelper.ExecuteDataReader(sql);
            while (rs.next()) {
                int candidateId = rs.getInt("ID_Condidat");
                candidateProfilesById.put(
                        candidateId,
                        new CandidateProfile(
                                candidateId,
                                safe(rs.getString("Nom_Utilisateur")),
                                safe(rs.getString("Email")),
                                safe(rs.getString("Num_Tel")),
                                safe(rs.getString("Adresse")),
                                safe(rs.getString("CIN")),
                                rs.getDate("Date_Naissance"),
                                safe(rs.getString("CV"))
                        )
                );
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement profils condidat", ex);
        } finally {
            if (rs != null) {
                try {
                    if (rs.getStatement() != null) {
                        rs.getStatement().close();
                    }
                    rs.close();
                } catch (Exception ignored) {
                }
            }
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

        Button detailsBtn = new Button("Voir candidature");
        detailsBtn.getStyleClass().addAll("btn", "btn-ghost");
        detailsBtn.setOnAction(event -> showCandidatureDetails(candidate));

        Button planInterviewBtn = new Button("Planifier entretien");
        planInterviewBtn.getStyleClass().addAll("btn", "btn-ghost");
        planInterviewBtn.setDisable(!"Acceptee".equals(candidate.status));
        planInterviewBtn.setOnAction(event -> planInterview(candidate));

        actions.getChildren().addAll(status, detailsBtn, planInterviewBtn, acceptBtn, rejectBtn);
        item.getChildren().addAll(info, spacer, actions);
        return item;
    }

    private void planInterview(Candidate candidate) {
        if (!"Acceptee".equals(candidate.status)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Statut requis");
            alert.setContentText("Vous devez accepter la candidature avant de planifier un entretien.");
            alert.showAndWait();
            return;
        }

        TextInputDialog dateDialog = new TextInputDialog();
        dateDialog.setTitle("Planifier entretien");
        dateDialog.setHeaderText("Date et heure d'entretien pour " + candidate.name);
        dateDialog.setContentText("Format: yyyy-MM-dd HH:mm");
        Optional<String> dateInput = dateDialog.showAndWait();
        if (dateInput.isEmpty()) {
            return;
        }

        LocalDateTime interviewDateTime;
        try {
            interviewDateTime = LocalDateTime.parse(dateInput.get().trim(), INTERVIEW_INPUT_FORMAT);
        } catch (DateTimeParseException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Format invalide");
            alert.setContentText("Utilisez le format: yyyy-MM-dd HH:mm");
            alert.showAndWait();
            return;
        }

        TextInputDialog locationDialog = new TextInputDialog();
        locationDialog.setTitle("Planifier entretien");
        locationDialog.setHeaderText("Localisation de l'entretien");
        locationDialog.setContentText("Ex: Salle A / Meet / Teams");
        Optional<String> locationInput = locationDialog.showAndWait();
        if (locationInput.isEmpty() || locationInput.get().isBlank()) {
            return;
        }

        TextInputDialog evaluationDialog = new TextInputDialog();
        evaluationDialog.setTitle("Planifier entretien");
        evaluationDialog.setHeaderText("Note / evaluation (optionnel)");
        evaluationDialog.setContentText("Laissez vide si non applicable");
        Optional<String> evaluationInput = evaluationDialog.showAndWait();
        if (evaluationInput.isEmpty()) {
            return;
        }

        Entretien entretien = new Entretien();
        entretien.setIdCandidat(candidate.candidature.getID_Candidat());
        entretien.setIdRh(getCurrentRhId());
        entretien.setNumOrdreEntretien((int) COrdre.GetNumOrdreFromDate(interviewDateTime));
        entretien.setLocalisation(locationInput.get().trim());
        entretien.setStatusEntretien(0);
        entretien.setEvaluation(evaluationInput.get().trim());

        boolean success = EntretienRepository.Ajouter(entretien);
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de planifier l'entretien.");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Entretien planifie");
        alert.setContentText("Entretien planifie le " + interviewDateTime.format(INTERVIEW_INPUT_FORMAT) + " a " + locationInput.get().trim());
        alert.showAndWait();
    }

    private int getCurrentRhId() {
        return 1;
    }

    private void showCandidatureDetails(Candidate candidate) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details candidature");
        alert.setHeaderText(candidate.name + " - " + candidate.offerTitle);

        String details = buildDetailsText(candidate);
        TextArea detailsArea = new TextArea(details);
        detailsArea.setWrapText(true);
        detailsArea.setEditable(false);
        detailsArea.setPrefRowCount(18);

        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setContent(detailsArea);

        alert.showAndWait();
    }

    private String buildDetailsText(Candidate candidate) {
        StringBuilder builder = new StringBuilder();
        CandidateProfile profile = candidate.profile;

        builder.append("Statut: ").append(candidate.status).append("\n");
        builder.append("Offre: ").append(candidate.offerTitle).append("\n\n");

        if (profile != null) {
            builder.append("Nom: ").append(orDefault(profile.name, "N/A")).append("\n");
            builder.append("Email: ").append(orDefault(profile.email, "N/A")).append("\n");
            builder.append("Telephone: ").append(orDefault(profile.phone, "N/A")).append("\n");
            builder.append("Adresse: ").append(orDefault(profile.address, "N/A")).append("\n");
            builder.append("CIN: ").append(orDefault(profile.cin, "N/A")).append("\n");
            builder.append("Date naissance: ").append(formatDate(profile.birthDate)).append("\n");
            builder.append("CV: ").append(orDefault(profile.cv, "N/A")).append("\n\n");
        }

        builder.append("Lettre de motivation:\n")
                .append(orDefault(candidate.candidature.getLettre_Motivation(), "N/A"))
                .append("\n\n");

        builder.append("Portfolio:\n")
                .append(orDefault(candidate.candidature.getPortfolio(), "N/A"))
                .append("\n\n");

        builder.append("Lettre de recommandation:\n")
                .append(orDefault(candidate.candidature.getLettre_Recomendation(), "N/A"));

        return builder.toString();
    }

    private String buildProfileSummary(CandidateProfile profile) {
        List<String> parts = new ArrayList<>();
        if (!profile.email.isBlank()) {
            parts.add(profile.email);
        }
        if (!profile.phone.isBlank()) {
            parts.add(profile.phone);
        }
        if (!profile.address.isBlank()) {
            parts.add(profile.address);
        }
        return String.join(" • ", parts);
    }

    private void updateStatus(Candidate candidate, int statusCode, String statusLabel) {
        candidate.status = statusLabel;
        candidate.candidature.setCode_Type_Status(statusCode);

        boolean success = CandidatureRepository.Modifier(candidate.candidature);
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

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String orDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value;
    }

    private String formatDate(Date value) {
        if (value == null) {
            return "N/A";
        }
        return new SimpleDateFormat("yyyy-MM-dd").format(value);
    }

    private static final class CandidateProfile {
        private final int candidateId;
        private final String name;
        private final String email;
        private final String phone;
        private final String address;
        private final String cin;
        private final Date birthDate;
        private final String cv;

        private CandidateProfile(
                int candidateId,
                String name,
                String email,
                String phone,
                String address,
                String cin,
                Date birthDate,
                String cv
        ) {
            this.candidateId = candidateId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.cin = cin;
            this.birthDate = birthDate;
            this.cv = cv;
        }
    }

    private static final class Candidate {
        private final Candidature candidature;
        private final String offerTitle;
        private final String name;
        private final String email;
        private final String experience;
        private String status;
        private final String skills;
        private final CandidateProfile profile;

        private Candidate(
                Candidature candidature,
                String offerTitle,
                String name,
                String email,
                String experience,
                String status,
                String skills,
                CandidateProfile profile
        ) {
            this.candidature = candidature;
            this.offerTitle = offerTitle;
            this.name = name;
            this.email = email;
            this.experience = experience;
            this.status = status;
            this.skills = skills;
            this.profile = profile;
        }

        private String searchText() {
            return offerTitle + " " + name + " " + email + " " + skills;
        }
    }
}
