package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.API.EmailService;
import com.recruitx.hrone.Models.*;
import com.recruitx.hrone.Repository.*;
import com.recruitx.hrone.Utils.CError;
import com.recruitx.hrone.Utils.ActionLogger;
import com.recruitx.hrone.Utils.COrdre;
import com.recruitx.hrone.Utils.DBHelper;
import com.recruitx.hrone.Utils.LogType;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FrmCandidatures {
    private static final DateTimeFormatter INTERVIEW_INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter INTERVIEW_TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    @FXML
    private ComboBox<String> offerFilter;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private ComboBox<String> experienceFilter;

    @FXML
    private TextField skillsFilter;

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

    @FXML
    private Label statTotalOffers;

    @FXML
    private Label statTotalApplications;

    @FXML
    private Label statAccepted;

    @FXML
    private Label statRejected;

    @FXML
    private Label statMostAppliedJob;

    @FXML
    private Label statAvgInterviewScore;

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

        experienceFilter.getItems().setAll(
            "Toutes les experiences",
            "0-1 ans",
            "2-4 ans",
            "5+ ans"
        );
        experienceFilter.getSelectionModel().selectFirst();

        offerFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderCandidates());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderCandidates());
        experienceFilter.valueProperty().addListener((obs, oldValue, newValue) -> renderCandidates());
        skillsFilter.textProperty().addListener((obs, oldValue, newValue) -> renderCandidates());

        renderCandidates();
        refreshStatistics();
    }

    @FXML
    private void onResetFilters() {
        offerFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        experienceFilter.getSelectionModel().selectFirst();
        skillsFilter.clear();
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
                int requiredExperienceYears = offer != null ? offer.getNbr_Annee_Experience() : -1;

            candidates.add(
                    new Candidate(
                            candidature,
                            offerTitle,
                            name,
                            email,
                            experience,
                        requiredExperienceYears,
                            status,
                            skills,
                            profile
                    )
            );
        }
    }

    private void refreshStatistics() {
        int totalOffers = offersById.size();
        int totalApplications = candidates.size();

        int accepted = 0;
        int rejected = 0;
        Map<String, Integer> applicationsPerOffer = new HashMap<>();

        for (Candidate candidate : candidates) {
            if ("Acceptee".equals(candidate.status)) {
                accepted++;
            }
            if ("Rejetee".equals(candidate.status)) {
                rejected++;
            }

            applicationsPerOffer.put(
                    candidate.offerTitle,
                    applicationsPerOffer.getOrDefault(candidate.offerTitle, 0) + 1
            );
        }

        String mostAppliedJob = "-";
        if (!applicationsPerOffer.isEmpty()) {
            mostAppliedJob = applicationsPerOffer.entrySet().stream()
                    .max(
                            Comparator
                                    .comparingInt((Map.Entry<String, Integer> e) -> e.getValue())
                                    .thenComparing(Map.Entry::getKey)
                    )
                    .map(Map.Entry::getKey)
                    .orElse("-");
        }

        String avgScore = computeAverageInterviewScore();

        if (statTotalOffers != null) {
            statTotalOffers.setText(String.valueOf(totalOffers));
        }
        if (statTotalApplications != null) {
            statTotalApplications.setText(String.valueOf(totalApplications));
        }
        if (statAccepted != null) {
            statAccepted.setText(String.valueOf(accepted));
        }
        if (statRejected != null) {
            statRejected.setText(String.valueOf(rejected));
        }
        if (statMostAppliedJob != null) {
            statMostAppliedJob.setText(mostAppliedJob);
        }
        if (statAvgInterviewScore != null) {
            statAvgInterviewScore.setText(avgScore);
        }
    }

    private String computeAverageInterviewScore() {
        String sql = "SELECT EVALUATION FROM ENTRETIEN";
        ResultSet rs = null;

        double sum = 0;
        int count = 0;

        try {
            rs = DBHelper.ExecuteDataReader(sql);
            while (rs.next()) {
                Double score = parseNumericScore(rs.getString("EVALUATION"));
                if (score != null) {
                    sum += score;
                    count++;
                }
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur calcul score moyen entretien", ex);
            return "N/A";
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

        if (count == 0) {
            return "N/A";
        }

        DecimalFormat format = new DecimalFormat("0.00");
        return format.format(sum / count);
    }

    private Double parseNumericScore(String evaluation) {
        if (evaluation == null || evaluation.isBlank()) {
            return null;
        }

        String normalized = evaluation.trim().replace(',', '.');
        StringBuilder numeric = new StringBuilder();
        boolean dotSeen = false;

        for (int index = 0; index < normalized.length(); index++) {
            char ch = normalized.charAt(index);
            if (Character.isDigit(ch)) {
                numeric.append(ch);
                continue;
            }
            if (ch == '.' && !dotSeen) {
                numeric.append(ch);
                dotSeen = true;
                continue;
            }
            if (numeric.length() > 0) {
                break;
            }
        }

        if (numeric.length() == 0 || ".".contentEquals(numeric)) {
            return null;
        }

        try {
            return Double.parseDouble(numeric.toString());
        } catch (NumberFormatException ex) {
            return null;
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
        String selectedExperience = experienceFilter.getValue();
        String skillsQuery = normalize(skillsFilter.getText());

        int activeFilters = 0;
        if (selectedOffer != null && !selectedOffer.equals("Toutes les offres")) {
            activeFilters++;
        }
        if (selectedStatus != null && !selectedStatus.equals("Tous les statuts")) {
            activeFilters++;
        }
        if (selectedExperience != null && !selectedExperience.equals("Toutes les experiences")) {
            activeFilters++;
        }
        if (!skillsQuery.isBlank()) {
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
            boolean matchesExperience = matchesExperienceFilter(candidate.requiredExperienceYears, selectedExperience);
            boolean matchesSkills = skillsQuery.isBlank() || normalize(candidate.skills).contains(skillsQuery);

            if (matchesOffer && matchesStatus && matchesExperience && matchesSkills) {
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

        Optional<InterviewPlanInput> input = showInterviewPlannerDialog(candidate);
        if (input.isEmpty()) {
            return;
        }

        InterviewPlanInput interviewInput = input.get();
        LocalDateTime interviewDateTime = LocalDateTime.of(interviewInput.date, interviewInput.time);

        Entretien entretien = new Entretien();
        entretien.setIdCandidat(candidate.candidature.getID_Candidat());
        entretien.setIdRh(getCurrentRhId());
        entretien.setNumOrdreEntretien((int) COrdre.GetNumOrdreFromDate(interviewDateTime));
        entretien.setLocalisation(interviewInput.location);
        entretien.setStatusEntretien(0);
        entretien.setEvaluation(interviewInput.evaluation);

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
        alert.setContentText("Entretien planifie le " + interviewDateTime.format(INTERVIEW_INPUT_FORMAT) + " a " + interviewInput.location);
        alert.showAndWait();

        ActionLogger.log("Planifier Entretien", "Entretien candidat ID=" + candidate.candidature.getID_Candidat());

        refreshStatistics();
    }

    private Optional<InterviewPlanInput> showInterviewPlannerDialog(Candidate candidate) {
        Dialog<InterviewPlanInput> dialog = new Dialog<>();
        dialog.setTitle("Planifier entretien");

        DialogPane pane = dialog.getDialogPane();
        pane.getStyleClass().add("interview-dialog");

        if (getClass().getResource("/com/recruitx/hrone/Css/FrmCandidatures.fx.css") != null) {
            pane.getStylesheets().add(
                    getClass().getResource("/com/recruitx/hrone/Css/FrmCandidatures.fx.css").toExternalForm()
            );
        }

        ButtonType planType = new ButtonType("Planifier", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().addAll(ButtonType.CANCEL, planType);

        Label title = new Label("Planifier un entretien");
        title.getStyleClass().add("dialog-title");

        Label subtitle = new Label("Candidat: " + candidate.name + " • Offre: " + candidate.offerTitle);
        subtitle.getStyleClass().add("muted");

        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        TextField timeField = new TextField("09:30");
        timeField.setPromptText("HH:mm");

        TextField locationField = new TextField();
        locationField.setPromptText("Salle A / Meet / Teams");

        TextArea evaluationArea = new TextArea();
        evaluationArea.setPromptText("Notes de preparation (optionnel)");
        evaluationArea.setPrefRowCount(3);

        Label errorLabel = new Label();
        errorLabel.getStyleClass().add("field-error");
        errorLabel.setManaged(false);
        errorLabel.setVisible(false);

        GridPane form = new GridPane();
        form.getStyleClass().add("interview-form");
        form.setHgap(12);
        form.setVgap(10);

        form.add(buildDialogField("Date", datePicker), 0, 0);
        form.add(buildDialogField("Heure", timeField), 1, 0);
        form.add(buildDialogField("Localisation", locationField), 0, 1, 2, 1);
        form.add(buildDialogField("Evaluation", evaluationArea), 0, 2, 2, 1);

        VBox container = new VBox(10, title, subtitle, form, errorLabel);
        container.getStyleClass().add("interview-dialog-content");
        pane.setContent(container);

        Button planBtn = (Button) pane.lookupButton(planType);
        planBtn.getStyleClass().addAll("btn", "btn-primary");

        Button cancelBtn = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelBtn.getStyleClass().addAll("btn", "btn-ghost");

        final InterviewPlanInput[] result = new InterviewPlanInput[1];
        planBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String error = null;

            LocalDate date = datePicker.getValue();
            if (date == null) {
                error = "Selectionnez une date d'entretien.";
            }

            LocalTime time = null;
            if (error == null) {
                try {
                    time = LocalTime.parse(timeField.getText().trim(), INTERVIEW_TIME_FORMAT);
                } catch (DateTimeParseException ex) {
                    error = "Heure invalide. Utilisez HH:mm (ex: 14:30).";
                }
            }

            String location = locationField.getText() == null ? "" : locationField.getText().trim();
            if (error == null && location.isBlank()) {
                error = "La localisation est obligatoire.";
            }

            if (error != null) {
                errorLabel.setText(error);
                errorLabel.setManaged(true);
                errorLabel.setVisible(true);
                event.consume();
                return;
            }

            String evaluation = evaluationArea.getText() == null ? "" : evaluationArea.getText().trim();
            result[0] = new InterviewPlanInput(date, time, location, evaluation);
            errorLabel.setManaged(false);
            errorLabel.setVisible(false);
        });

        dialog.setResultConverter(buttonType -> buttonType == planType ? result[0] : null);
        return dialog.showAndWait();
    }

    private VBox buildDialogField(String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText);
        VBox field = new VBox(6, label, control);
        field.getStyleClass().add("field");
        return field;
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

        if (statusCode == 2) {
            ActionLogger.log("Accepter Candidat", "Candidature ID=" + candidate.candidature.getID_Candidature());
            if (candidate.email != null && !candidate.email.isBlank()) {
                EmailService.sendJobApprovalEmail(
                        candidate.email,
                        candidate.name,
                        candidate.offerTitle
                );
            }
        } else if (statusCode == 3) {
            ActionLogger.log("Rejeter Candidat", "Candidature ID=" + candidate.candidature.getID_Candidature());
        } else {
            ActionLogger.log("Modifier Candidature", "Mise à jour statut candidature ID=" + candidate.candidature.getID_Candidature());
        }

        renderCandidates();
        refreshStatistics();
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

    private boolean matchesExperienceFilter(int years, String selectedExperience) {
        if (selectedExperience == null || selectedExperience.equals("Toutes les experiences")) {
            return true;
        }

        if (years < 0) {
            return false;
        }

        if (selectedExperience.equals("0-1 ans")) {
            return years <= 1;
        }
        if (selectedExperience.equals("2-4 ans")) {
            return years >= 2 && years <= 4;
        }
        if (selectedExperience.equals("5+ ans")) {
            return years >= 5;
        }

        return true;
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
        private final int requiredExperienceYears;
        private String status;
        private final String skills;
        private final CandidateProfile profile;

        private Candidate(
                Candidature candidature,
                String offerTitle,
                String name,
                String email,
                String experience,
                int requiredExperienceYears,
                String status,
                String skills,
                CandidateProfile profile
        ) {
            this.candidature = candidature;
            this.offerTitle = offerTitle;
            this.name = name;
            this.email = email;
            this.experience = experience;
            this.requiredExperienceYears = requiredExperienceYears;
            this.status = status;
            this.skills = skills;
            this.profile = profile;
        }

        private String searchText() {
            return offerTitle + " " + name + " " + email + " " + skills;
        }
    }

    private static final class InterviewPlanInput {
        private final LocalDate date;
        private final LocalTime time;
        private final String location;
        private final String evaluation;

        private InterviewPlanInput(LocalDate date, LocalTime time, String location, String evaluation) {
            this.date = date;
            this.time = time;
            this.location = location;
            this.evaluation = evaluation;
        }
    }
}
