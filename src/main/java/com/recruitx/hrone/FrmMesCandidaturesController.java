package com.recruitx.hrone;

import com.recruitx.hrone.utils.CError;
import com.recruitx.hrone.utils.DBHelper;
import com.recruitx.hrone.utils.LogType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class FrmMesCandidaturesController {
    @FXML
    private TextField searchField;

    @FXML
    private Label totalCount;

    @FXML
    private Label inProgressCount;

    @FXML
    private VBox cardList;

    @FXML
    private Label emptyState;

    private final List<ApplicationItem> applications = new ArrayList<>();
    private final Map<Integer, Offer> offersById = new HashMap<>();
    private final Map<Integer, String> enterprisesById = new HashMap<>();
    private final Map<Integer, String> statusByCode = new HashMap<>();
    private final Map<Integer, CandidateInfo> candidatesById = new HashMap<>();

    private static final int CURRENT_USER_ID = 1;

    @FXML
    private void initialize() {
        CError.log(LogType.INFO, "FrmMesCandidaturesController init start");
        loadOffers();
        loadEnterprises();
        loadStatusTypes();
        loadCandidates();
        loadApplications();
        CError.log(LogType.INFO, "FrmMesCandidaturesController init done. applications=" + applications.size());

        searchField.textProperty().addListener((obs, oldValue, newValue) -> render());
        render();
    }

    private void loadOffers() {
        List<Offer> offers = OfferController.AvoirListe();
        if (offers == null) {
            CError.log(LogType.ERROR, "loadOffers: OfferController.AvoirListe returned null");
            offers = new ArrayList<>();
        }
        CError.log(LogType.INFO, "loadOffers: offers=" + offers.size());
        offersById.clear();
        for (Offer offer : offers) {
            offersById.put(offer.getID_Offre(), offer);
        }
    }

    private void loadEnterprises() {
        enterprisesById.clear();
        String sql = "SELECT ID_Entreprise, Nom_Entreprise FROM Entreprise";

        try {
            var rs = DBHelper.ExecuteDataReader(sql);
            if (rs != null) {
                while (rs.next()) {
                    enterprisesById.put(
                            rs.getInt("ID_Entreprise"),
                            rs.getString("Nom_Entreprise")
                    );
                }
                rs.getStatement().close();
                rs.close();
                CError.log(LogType.INFO, "loadEnterprises: entreprises=" + enterprisesById.size());
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement entreprises", ex);
        }
    }

    private void loadStatusTypes() {
        statusByCode.clear();
        String sql =
                "SELECT Code_Type_Status_Condidature, Description_Status_Condidature " +
                "FROM Type_Status_Condidature";

        try {
            var rs = DBHelper.ExecuteDataReader(sql);
            if (rs != null) {
                while (rs.next()) {
                    statusByCode.put(
                            rs.getInt("Code_Type_Status_Condidature"),
                            rs.getString("Description_Status_Condidature")
                    );
                }
                rs.getStatement().close();
                rs.close();
                CError.log(LogType.INFO, "loadStatusTypes: status=" + statusByCode.size());
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement status candidatures", ex);
        }
    }

    private void loadApplications() {
        List<Candidature> candidatures = CandidatureController.AvoirListe();
        if (candidatures == null) {
            CError.log(LogType.ERROR, "loadApplications: CandidatureController.AvoirListe returned null");
            candidatures = new ArrayList<>();
        }

        CError.log(LogType.INFO, "loadApplications: candidatures=" + candidatures.size());

        applications.clear();
        for (Candidature candidature : candidatures) {
            CandidateInfo candidateInfo = candidatesById.get(candidature.getID_Candidat());
            if (candidateInfo == null || candidateInfo.userId != CURRENT_USER_ID) {
                CError.log(
                        LogType.INFO,
                        "loadApplications: skip candidature=" + candidature.getID_Candidature() +
                                " candidatId=" + candidature.getID_Candidat() +
                                " userId=" + (candidateInfo == null ? "null" : candidateInfo.userId)
                );
                continue;
            }

            Offer offer = offersById.get(candidature.getID_Offre());
            String title = offer != null ? offer.getTitre() : "Offre #" + candidature.getID_Offre();
            String company = offer != null
                    ? enterprisesById.getOrDefault(
                            offer.getID_Entreprise(),
                            "Entreprise #" + offer.getID_Entreprise()
                    )
                    : "Entreprise";
            String city = candidateInfo.city;
            String contract = offer != null ? safe(offer.getCode_Type_Contrat()) : "";
            String mode = offer != null ? safe(offer.getWork_Type()) : "";
            String domain = "";
            String status = statusLabelFromCode(candidature.getCode_Type_Status());
            String date = "N/A";
            String reference = "CAND-" + candidature.getID_Candidature();

            applications.add(
                    new ApplicationItem(
                            candidature,
                            title,
                            company,
                            city,
                            contract,
                            mode,
                            domain,
                            status,
                            date,
                            reference
                    )
            );
        }

        CError.log(LogType.INFO, "loadApplications: loaded applications=" + applications.size());
    }

    private void loadCandidates() {
        candidatesById.clear();
        String sql =
                "SELECT c.ID_Condidat, u.ID_UTILISATEUR, u.Nom_Utilisateur, u.Email, u.Adresse " +
                "FROM Condidat c " +
                "JOIN UTILISATEUR u ON u.ID_UTILISATEUR = c.ID_UTILISATEUR";

        try {
            var rs = DBHelper.ExecuteDataReader(sql);
            if (rs != null) {
                while (rs.next()) {
                    int candidatId = rs.getInt("ID_Condidat");
                    int userId = rs.getInt("ID_UTILISATEUR");
                    String name = rs.getString("Nom_Utilisateur");
                    String email = rs.getString("Email");
                    String city = rs.getString("Adresse");

                    candidatesById.put(
                            candidatId,
                            new CandidateInfo(
                                    userId,
                                    safe(name),
                                    safe(email),
                                    city == null || city.isBlank() ? "-" : city
                            )
                    );
                }
                rs.getStatement().close();
                rs.close();
                CError.log(LogType.INFO, "loadCandidates: candidats=" + candidatesById.size());
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement candidats", ex);
        }
    }

    private void render() {
        String query = normalize(searchField.getText());
        List<ApplicationItem> filtered = new ArrayList<>();
        for (ApplicationItem item : applications) {
            boolean matches = query.isBlank() || normalize(item.searchText()).contains(query);
            if (matches) {
                filtered.add(item);
            }
        }

        cardList.getChildren().clear();
        for (ApplicationItem item : filtered) {
            cardList.getChildren().add(buildCard(item));
        }

        totalCount.setText("Total: " + applications.size());
        int inProgress = 0;
        for (ApplicationItem item : applications) {
            if ("En cours".equals(item.status)) {
                inProgress++;
            }
        }
        inProgressCount.setText("En cours: " + inProgress);

        boolean empty = filtered.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
    }

    private VBox buildCard(ApplicationItem item) {
        VBox card = new VBox();
        card.getStyleClass().add("card-item");

        HBox header = new HBox();
        header.getStyleClass().add("card-item-header");
        VBox titleBox = new VBox();
        Label title = new Label(item.title);
        title.getStyleClass().add("card-title");
        Label company = new Label(item.company + "  " + item.city);
        company.getStyleClass().add("muted");
        titleBox.getChildren().addAll(title, company);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(item.status);
        status.getStyleClass().addAll("status", statusClass(item.status));

        header.getChildren().addAll(titleBox, spacer, status);

        Label meta = new Label("Postulee le " + item.date + "  Ref: " + item.reference);
        meta.getStyleClass().add("muted");

        HBox tags = new HBox();
        tags.getStyleClass().add("tags");
        tags.getChildren().addAll(buildTag(item.contract), buildTag(item.mode), buildTag(item.domain));

        HBox actions = new HBox();
        actions.getStyleClass().add("card-actions");


        Button deleteBtn = new Button("Retirer");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(event -> {
            deleteApplication(item);
        });

        actions.getChildren().addAll(deleteBtn);

        card.getChildren().addAll(header, meta, tags, actions);
        return card;
    }

    private void updateStatus(ApplicationItem item, String statusLabel) {
        item.status = statusLabel;
        item.candidature.setCode_Type_Status(statusCodeForLabel(statusLabel));

        boolean success = CandidatureController.Modifier(item.candidature);
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de mettre a jour le statut.");
            alert.showAndWait();
            return;
        }

        render();
    }

    private void deleteApplication(ApplicationItem item) {
        boolean success = CandidatureController.Supprimer(item.candidature.getID_Candidature());
        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Impossible de supprimer la candidature.");
            alert.showAndWait();
            return;
        }

        applications.remove(item);
        render();
    }

    private String statusLabelFromCode(int code) {
        String description = statusByCode.get(code);
        if (description == null) {
            return "En cours";
        }
        return mapStatusDescription(description);
    }

    private int statusCodeForLabel(String label) {
        for (Map.Entry<Integer, String> entry : statusByCode.entrySet()) {
            if (mapStatusDescription(entry.getValue()).equalsIgnoreCase(label)) {
                return entry.getKey();
            }
        }
        return 2;
    }

    private String mapStatusDescription(String description) {
        if (description == null) {
            return "En cours";
        }

        String normalized = description.trim().toUpperCase(Locale.ROOT);
        if ("SUBMITTED".equals(normalized)) {
            return "Envoyee";
        }
        if ("REVIEW".equals(normalized)) {
            return "En cours";
        }
        if ("ACCEPTED".equals(normalized)) {
            return "Entretien";
        }
        if ("REJECTED".equals(normalized)) {
            return "Refusee";
        }
        return description;
    }

    private Label buildTag(String text) {
        Label tag = new Label(text);
        tag.getStyleClass().add("tag");
        return tag;
    }

    private String statusClass(String status) {
        if ("Envoyee".equals(status)) {
            return "status-sent";
        }
        if ("Entretien".equals(status)) {
            return "status-meet";
        }
        if ("Refusee".equals(status)) {
            return "status-closed";
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
        return value == null ? "" : value;
    }

    private static final class ApplicationItem {
        private final Candidature candidature;
        private final String title;
        private final String company;
        private final String city;
        private final String contract;
        private final String mode;
        private final String domain;
        private String status;
        private final String date;
        private final String reference;

        private ApplicationItem(
                Candidature candidature,
                String title,
                String company,
                String city,
                String contract,
                String mode,
                String domain,
                String status,
                String date,
                String reference) {
            this.candidature = candidature;
            this.title = title;
            this.company = company;
            this.city = city;
            this.contract = contract;
            this.mode = mode;
            this.domain = domain;
            this.status = status;
            this.date = date;
            this.reference = reference;
        }

        private String searchText() {
            return title + " " + company + " " + city + " " + contract + " " + mode + " " + domain + " " + status;
        }
    }

    private static final class CandidateInfo {
        private final int userId;
        private final String name;
        private final String email;
        private final String city;

        private CandidateInfo(int userId, String name, String email, String city) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.city = city;
        }
    }
}
