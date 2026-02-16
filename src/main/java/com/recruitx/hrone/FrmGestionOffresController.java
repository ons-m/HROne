package com.recruitx.hrone;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import com.recruitx.hrone.utils.*;

public class FrmGestionOffresController {

    @FXML private TextField offerTitleInput;
    @FXML private ComboBox<TypeItem> offerContractSelect;
    @FXML private ComboBox<String> offerWorkTypeSelect;
    @FXML private TextField offerExperienceInput;
    @FXML private ComboBox<TypeItem> offerEducationSelect;
    @FXML private TextField salaryMinInput;
    @FXML private TextField salaryMaxInput;
    @FXML private DatePicker offerExpiryInput;
    @FXML private TextArea offerDescriptionInput;

    @FXML private TextField skillsSearch;
    @FXML private ListView<TypeItem> skillsList;

    @FXML private TextField languagesSearch;
    @FXML private ListView<TypeItem> languagesList;

    @FXML private TextField backgroundSearch;
    @FXML private ListView<TypeItem> backgroundList;

    @FXML private TextField offerSearch;
    @FXML private VBox offerList;

    private final ObservableList<Offer> offers = FXCollections.observableArrayList();

    private long editingOfferId = -1;

    @FXML
    private void initialize() {

        List<Offer> data = OfferController.AvoirListe();
        if (data != null) {
            offers.setAll(data);
        }

        offerContractSelect.setItems(
                FXCollections.observableArrayList(
                        OfferController.loadTypesContratFromDB()
                )
        );

        offerWorkTypeSelect.getItems().setAll("En ligne", "Sur site", "Hybride");
        //offerExperienceSelect.getItems().setAll("0-2 ans", "3-5 ans", "6-10 ans", "10+ ans");
        offerEducationSelect.setItems(
                FXCollections.observableArrayList(
                        OfferController.loadNiveauxEtudeFromDB()
                )
        );

        setupListFilter(
                skillsSearch,
                skillsList,
                OfferController.loadCompetencesFromDB()
        );

        setupListFilter(
                languagesSearch,
                languagesList,
                OfferController.loadLanguesFromDB()
        );

        setupListFilter(
                backgroundSearch,
                backgroundList,
                OfferController.loadBackgroundsFromDB()
        );
        offerSearch.textProperty().addListener((obs, o, n) -> renderOffers());
        renderOffers();
    }

    @FXML
    private void onPublish() {

    /* =========================
       VALIDATION
       ========================= */

        List<String> errors = new ArrayList<>();

        // ---- Title ----
        if (isBlank(offerTitleInput.getText())) {
            errors.add("Ajoutez un titre de poste.");
        }

        // ---- Contract type ----
        if (offerContractSelect.getValue() == null) {
            errors.add("Selectionnez un type de contrat.");
        }

        // ---- Education level ----
        if (offerEducationSelect.getValue() == null) {
            errors.add("Selectionnez un niveau d'etude.");
        }

        // ---- Competences / Backgrounds ----
        if (skillsList.getSelectionModel().getSelectedItems().size() < 2) {
            errors.add("Selectionnez au moins 2 competences.");
        }

        if (backgroundList.getSelectionModel().getSelectedItems().size() < 1) {
            errors.add("Selectionnez au moins 1 background.");
        }

        // ---- Salary ----
        double minSalaire;
        double maxSalaire;

        try {
            minSalaire = Double.parseDouble(salaryMinInput.getText().trim());
            if (minSalaire < 0) {
                errors.add("Le salaire minimum doit etre superieur ou egal a 0.");
            }
        } catch (Exception ex) {
            errors.add("Salaire minimum invalide.");
            minSalaire = -1;
        }

        try {
            maxSalaire = Double.parseDouble(salaryMaxInput.getText().trim());
            if (maxSalaire < 0) {
                errors.add("Le salaire maximum doit etre superieur ou egal a 0.");
            }
        } catch (Exception ex) {
            errors.add("Salaire maximum invalide.");
            maxSalaire = -1;
        }

        if (minSalaire >= 0 && maxSalaire >= 0 && minSalaire > maxSalaire) {
            errors.add("Le salaire minimum ne peut pas etre superieur au salaire maximum.");
        }

        // ---- Experience ----
        int experienceMin;

        try {
            experienceMin = Integer.parseInt(offerExperienceInput.getText().trim());
            if (experienceMin < 0) {
                errors.add("L'experience doit etre superieure ou egale a 0.");
            }
            if (experienceMin > 40) {
                errors.add("L'experience ne peut pas depasser 40 ans.");
            }
        } catch (Exception ex) {
            errors.add("Experience invalide (nombre requis).");
            experienceMin = -1;
        }

        // ---- Dates ----
        int numCreation = (int) COrdre.GetNumOrdreNow();
        int numExpiration;

        if (offerExpiryInput.getValue() != null) {
            numExpiration = (int) COrdre.GetNumOrdreFromDate(
                    offerExpiryInput.getValue()
                            .atStartOfDay(ZoneOffset.UTC)
                            .toLocalDateTime()
            );

            if (numExpiration <= numCreation) {
                errors.add("La date d'expiration doit etre posterieure a aujourd'hui.");
            }
        } else {
            numExpiration = (int) (numCreation + COrdre.MONTH_DURATION);
        }

        // ---- Final validation result ----
        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setHeaderText("Erreurs de validation");
            alert.setContentText(String.join("\n", errors));
            alert.showAndWait();
            return;
        }

    /* =========================
       BUILD OFFER ENTITY
       ========================= */

        Offer offerData = new Offer();

        offerData.setID_Entreprise(1);

        if (editingOfferId > 0) {
            offerData.setID_Offre((int) editingOfferId);
        }

        offerData.setTitre(offerTitleInput.getText().trim());

        offerData.setWork_Type(
                offerWorkTypeSelect.getValue() == null
                        ? "En ligne"
                        : offerWorkTypeSelect.getValue()
        );

        if (offerContractSelect.getValue() != null) {
            offerData.setCode_Type_Contrat(
                    offerContractSelect.getValue().getCode()
            );
        }

        offerData.setDescription(
                offerDescriptionInput.getText() == null
                        ? ""
                        : offerDescriptionInput.getText().trim()
        );

    /* =========================
       DATES → NUMORDRE
       ========================= */

        offerData.setNum_Ordre_Creation(
                (int) COrdre.GetNumOrdreNow()
        );

        if (offerExpiryInput.getValue() != null) {
            offerData.setNum_Ordre_Expiration(
                    (int) COrdre.GetNumOrdreFromDate(
                            offerExpiryInput.getValue()
                                    .atStartOfDay(ZoneOffset.UTC)
                                    .toLocalDateTime()
                    )
            );
        } else {
            offerData.setNum_Ordre_Expiration(
                    (int) (COrdre.GetNumOrdreNow() + COrdre.MONTH_DURATION)
            );
        }

    /* =========================
       EXTRACT SELECTED CODES
       ========================= */

        List<String> codesCompetences = skillsList.getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(TypeItem::getCode)
                .toList();

        List<String> codesLangues = languagesList.getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(TypeItem::getCode)
                .toList();

        List<String> codesBackgrounds = backgroundList.getSelectionModel()
                .getSelectedItems()
                .stream()
                .map(TypeItem::getCode)
                .toList();

        if (offerEducationSelect.getValue() != null) {
            offerData.setCode_Type_Niveau_Etude(
                    offerEducationSelect.getValue().getCode()
            );
        }

        offerData.setCodes_Competences(codesCompetences);
        offerData.setCodes_Langues(codesLangues);
        offerData.setCodes_Backgrounds(codesBackgrounds);

    /* =========================
       PERSIST (DB)
       ========================= */

        boolean success;

        if (editingOfferId > 0) {
            success = OfferController.Modifier(offerData);
            editingOfferId = -1;
        } else {
            success = OfferController.Ajouter(offerData);
        }

        if (!success) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText("Erreur");
            alert.setContentText("Une erreur est survenue lors de l'enregistrement.");
            alert.showAndWait();
            return;
        }

    /* =========================
       RELOAD & UI RESET
       ========================= */

        List<Offer> data = OfferController.AvoirListe();
        if (data != null) {
            offers.setAll(data);
        }

        clearForm();
        renderOffers();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Offre prête à publier");
        alert.setContentText("L'offre a été enregistrée avec succès.");
        alert.showAndWait();
    }

    private void renderOffers() {
        System.out.println(offerList.getChildren());
        offerList.getChildren().clear();
        String query = normalize(offerSearch.getText());

        for (Offer offer : offers) {
            if (!query.isBlank() && !normalize(buildSearchText(offer)).contains(query))
                continue;

            offerList.getChildren().add(buildOfferItem(offer));
        }
    }

    private HBox buildOfferItem(Offer offer) {

        HBox item = new HBox();
        item.getStyleClass().add("offer-item");

        VBox info = new VBox();

        Label title = new Label(offer.getTitre());
        title.getStyleClass().add("offer-title");

        Label meta = new Label(
                safe(offer.getWork_Type()) + " • " +
                        safe(offer.getCode_Type_Contrat()) + " • " +
                        offer.getNbr_Annee_Experience() + " ans"
        );

        meta.getStyleClass().add("offer-meta");

        info.getChildren().addAll(title, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Modifier");
        editBtn.getStyleClass().add("btn");
        editBtn.setOnAction(e -> fillForm(offer));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.getStyleClass().addAll("btn", "btn-danger");
        deleteBtn.setOnAction(e -> deleteOffer(offer.getID_Offre()));

        HBox actions = new HBox(editBtn, deleteBtn);
        actions.getStyleClass().add("list-item-actions");

        item.getChildren().addAll(info, spacer, actions);
        return item;
    }

    private void fillForm(Offer offer) {

        editingOfferId = offer.getID_Offre();

    /* =========================
       SIMPLE FIELDS
       ========================= */

        offerTitleInput.setText(offer.getTitre());

        offerWorkTypeSelect.setValue(
                offer.getWork_Type() == null ? "En ligne" : offer.getWork_Type()
        );

        offerExperienceInput.setText(
                String.valueOf(offer.getNbr_Annee_Experience())
        );

        offerDescriptionInput.setText(
                offer.getDescription() == null ? "" : offer.getDescription()
        );

        salaryMinInput.setText(
                String.valueOf(offer.getMin_Salaire())
        );

        salaryMaxInput.setText(
                String.valueOf(offer.getMax_Salaire())
        );

        if (offer.getNum_Ordre_Expiration() > 0) {
            offerExpiryInput.setValue(
                    COrdre.GetDateFromNumOrdre(
                            offer.getNum_Ordre_Expiration()
                    ).toLocalDate()
            );
        } else {
            offerExpiryInput.setValue(null);
        }

    /* =========================
       SINGLE-CHOICE TYPES
       ========================= */

        // TYPE_CONTRAT
        offerContractSelect.getItems().stream()
                .filter(t -> t.getCode().equals(offer.getCode_Type_Contrat()))
                .findFirst()
                .ifPresent(offerContractSelect::setValue);

        // TYPE_NIVEAU_ETUDE
        if (offer.getCode_Type_Niveau_Etude() != null) {
            offerEducationSelect.getItems().stream()
                    .filter(t -> t.getCode().equals(offer.getCode_Type_Niveau_Etude()))
                    .findFirst()
                    .ifPresent(offerEducationSelect::setValue);
        } else {
            offerEducationSelect.getSelectionModel().clearSelection();
        }

    /* =========================
       MULTI-CHOICE LISTS
       ========================= */

        restoreMultiSelection(skillsList, offer.getCodes_Competences());
        restoreMultiSelection(languagesList, offer.getCodes_Langues());
        restoreMultiSelection(backgroundList, offer.getCodes_Backgrounds());

        offerTitleInput.requestFocus();
    }

    private void restoreMultiSelection(
            ListView<TypeItem> listView,
            List<String> codes
    ) {
        listView.getSelectionModel().clearSelection();

        if (codes == null || codes.isEmpty()) return;

        for (TypeItem item : listView.getItems()) {
            if (codes.contains(item.getCode())) {
                listView.getSelectionModel().select(item);
            }
        }
    }

    private void deleteOffer(int offerId) {
        Alert confirm = new Alert(AlertType.CONFIRMATION);
        confirm.setHeaderText("Confirmer la suppression");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cette offre ?");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            if (response == ButtonType.OK) {
                boolean success = OfferController.Supprimer(offerId);
                if (success) {
                    offers.removeIf(o -> o.getID_Offre() == offerId);
                    renderOffers();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setHeaderText("Erreur");
                    alert.setContentText("Une erreur est survenue lors de la suppression.");
                    alert.showAndWait();
                }
            }
        });
    }

    private String buildSearchText(Offer offer) {
        return safe(offer.getTitre()) + " " +
                safe(offer.getWork_Type()) + " " +
                safe(offer.getCode_Type_Contrat());
    }

    private String safe(String v) {
        return v == null ? "" : v;
    }

    private void clearForm() {
        offerTitleInput.clear();
        offerContractSelect.getSelectionModel().clearSelection();
        offerWorkTypeSelect.getSelectionModel().clearSelection();
        offerExperienceInput.clear();
        offerEducationSelect.getSelectionModel().clearSelection();
        salaryMinInput.clear();
        salaryMaxInput.clear();
        offerExpiryInput.setValue(null);
        offerDescriptionInput.clear();
        skillsList.getSelectionModel().clearSelection();
        languagesList.getSelectionModel().clearSelection();
        backgroundList.getSelectionModel().clearSelection();
    }

    private <T> void setupListFilter(
            TextField searchField,
            ListView<T> listView,
            List<T> items
    ) {
        ObservableList<T> baseItems = FXCollections.observableArrayList(items);
        FilteredList<T> filtered = new FilteredList<>(baseItems, item -> true);
        listView.setItems(filtered);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        searchField.textProperty().addListener((obs, o, n) -> {
            String query = normalize(n);
            filtered.setPredicate(item ->
                    normalize(item.toString()).contains(query)
            );
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
