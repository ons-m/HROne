package com.recruitx.hrone.Controllers;

import com.mysql.cj.exceptions.CJConnectionFeatureNotAvailableException;
import com.recruitx.hrone.Models.Evenement;
import com.recruitx.hrone.Models.ParticipationEvenement;
import com.recruitx.hrone.Models.ListeAttente;
import com.recruitx.hrone.Models.Activite;
import com.recruitx.hrone.Repository.ActiviteRepository;
import com.recruitx.hrone.Repository.EvenementRepository;
import com.recruitx.hrone.Repository.ParticipationEvenementRepository;
import com.recruitx.hrone.Repository.ListeAttenteRepository;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class FrmEvenements implements NavigationAware {

    @FXML
    private VBox eventsGrid;
    @FXML
    private ComboBox<Evenement> eventSelect;
    @FXML
    private ComboBox<Activite> activiteSelect;
    @FXML
    private TextField nomField;
    @FXML
    private TextField emailField;
    @FXML
    private TextArea messageArea;
    @FXML
    private CheckBox consentUnknown;
    @FXML
    private Label statusLabel;

    @FXML
    private VBox paymentSection;
    @FXML
    private Label priceLabel;
    @FXML
    private VBox paymentOptions;
    @FXML
    private RadioButton radioSurPlace;
    @FXML
    private RadioButton radioCarte;
    @FXML
    private TextField cardNumField;
    private ToggleGroup paymentGroup;

    private final EvenementRepository es = new EvenementRepository();
    private final ActiviteRepository as = new ActiviteRepository();
    private final ParticipationEvenementRepository pes = new ParticipationEvenementRepository();
    private final ListeAttenteRepository las = new ListeAttenteRepository();

    private FrmMain mainController;

    @Override
    public void setMainController(FrmMain mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        refreshEvents();
        setupComboBox();
        setupPaymentLogic();
    }

    private void setupPaymentLogic() {
        paymentGroup = new ToggleGroup();
        if (radioSurPlace != null)
            radioSurPlace.setToggleGroup(paymentGroup);
        if (radioCarte != null) {
            radioCarte.setToggleGroup(paymentGroup);
            radioCarte.selectedProperty().addListener((obs, oldV, newV) -> {
                if (cardNumField != null) {
                    cardNumField.setVisible(newV);
                    cardNumField.setManaged(newV);
                }
            });
        }
    }

    private void setupComboBox() {
        if (eventSelect != null) {
            eventSelect.setCellFactory(param -> new ListCell<Evenement>() {
                @Override
                protected void updateItem(Evenement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
                }
            });
            eventSelect.setButtonCell(new ListCell<Evenement>() {
                @Override
                protected void updateItem(Evenement item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
                }
            });
            eventSelect.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    filterActivitesByEvent(newVal.getIdEvenement());
                    updatePaymentDisplay(newVal);
                }
            });
        }
        if (activiteSelect != null) {
            activiteSelect.setCellFactory(param -> new ListCell<Activite>() {
            @Override
                protected void updateItem(Activite item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
            }
            });
            activiteSelect.setButtonCell(new ListCell<Activite>() {
            @Override
                protected void updateItem(Activite item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item.getTitre());
            }
        });
    }
    }

    private void updatePaymentDisplay(Evenement e) {
        if (priceLabel == null)
            return;
        if (e.isEstPayant()) {
            priceLabel.setText("Prix : " + e.getPrix() + " DT");
            paymentOptions.setVisible(true);
            paymentOptions.setManaged(true);
        } else {
            priceLabel.setText("Prix : Gratuit");
            paymentOptions.setVisible(false);
            paymentOptions.setManaged(false);
        }
    }

    private void filterActivitesByEvent(int idEvenement) {
        if (activiteSelect == null)
            return;
        activiteSelect.getItems().clear();
        List<Activite> all = as.getAll();
        for (Activite a : all) {
            if (a.getIdEvenement() == idEvenement) {
                activiteSelect.getItems().add(a);
            }
        }
        }

    private void refreshEvents() {
        if (eventSelect != null) {
            List<Evenement> list = es.getAll();
            System.out.println("[EvenementsController] Chargement de " + list.size()
                    + " événements dans la ComboBox et la Grille.");
            eventSelect.getItems().setAll(list);

            if (eventsGrid != null) {
                eventsGrid.getChildren().clear();
                for (Evenement ev : list) {
                    eventsGrid.getChildren().add(createEventCard(ev));
                }
            }
        } else {
            System.err.println("[EvenementsController] eventSelect est NULL !");
        }
    }

    private Node createEventCard(Evenement ev) {
        VBox card = new VBox();
        card.getStyleClass().add("event-card");

        Label title = new Label(ev.getTitre());
        title.getStyleClass().add("card-title");

        Label desc = new Label(ev.getDescription());
        desc.setWrapText(true);
        desc.getStyleClass().add("muted");

        HBox footer = new HBox();
        footer.setSpacing(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label loc = new Label("📍 " + ev.getLocalisation());
        loc.getStyleClass().add("kicker");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSelect = new Button("Sélectionner");
        btnSelect.getStyleClass().add("ghost");
        btnSelect.setOnAction(e -> {
            eventSelect.setValue(ev);
            statusLabel.setText("Événement '" + ev.getTitre() + "' sélectionné.");
        });

        footer.getChildren().addAll(loc, spacer, btnSelect);
        card.getChildren().addAll(title, desc, footer);

        return card;
    }

    @FXML
    private void handleParticiper() {
        try {
            // 1. Validations
            if (consentUnknown != null && !consentUnknown.isSelected()) {
                showError("Veuillez accepter les conditions.");
                return;
            }
            Evenement selectedEv = eventSelect.getValue();
            if (selectedEv == null) {
                showError("Veuillez sélectionner un événement.");
                return;
            }
            Activite selectedAct = activiteSelect.getValue();
            if (selectedAct == null) {
                showError("Veuillez sélectionner une activité.");
                return;
            }
            String nom = nomField != null ? nomField.getText().trim() : "";
            String email = emailField != null ? emailField.getText().trim() : "";
            if (nom.isEmpty() || email.isEmpty()) {
                showError("Veuillez renseigner votre nom et email.");
                return;
            }

            int idEvent = selectedEv.getIdEvenement();
            int idActivite = selectedAct.getIdActivite();

            if (pes.existsDuplicate(email, idEvent, idActivite)) {
                showError("Vous êtes déjà inscrit à cette activité.");
                return;
            }

            String modePaiement = "Gratuit";
            if (selectedEv.isEstPayant()) {
                if (radioCarte != null && radioCarte.isSelected()) {
                    String cardNum = cardNumField != null ? cardNumField.getText().trim() : "";
                    if (!cardNum.matches("\\d{16}")) {
                        showError("Numéro de carte invalide (16 chiffres).");
                        return;
                    }
                    modePaiement = "En ligne (Carte)";
                } else {
                    modePaiement = "Sur place";
                }
            }

            // 2. Inscription
            int currentParticipants = pes.getCountForEvent(idEvent);
            if (currentParticipants >= selectedEv.getNbMax()) {
                las.addToWaitlist(new ListeAttente(idEvent, idActivite, nom, email));
                showSuccess("Événement complet ! Ajouté à la liste d'attente.");
            } else {
                ParticipationEvenement pe = new ParticipationEvenement();
                pe.setIdEvenement(idEvent);
                pe.setIdActivite(idActivite);
                // Utilisation de l'ID 1 (Administrateur/Défaut) pour satisfaire la contrainte
                // utilisateur
                pe.setIdParticipant(Session.getCurrentUser().getIdUtilisateur());
                pe.setNomComplet(nom);
                pe.setEmail(email);
                pe.setDescription(messageArea != null ? messageArea.getText().trim() : "");
                pe.setModePaiement(modePaiement);
                pe.setNumOrdreParticipation(1);

                pes.add(pe);
                showSuccess("✅ Inscription réussie !");
            }
            clearFields();
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    @FXML
    private void goToParticipations() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.PARTICIPATIONS);
        }
    }

    @FXML
    private void openChatbot(javafx.event.ActionEvent event) {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.CHATBOT);
        }
    }

    private void clearFields() {
        if (nomField != null)
            nomField.clear();
        if (emailField != null)
            emailField.clear();
        if (messageArea != null)
            messageArea.clear();
    }

    private void showError(String msg) {
        if (statusLabel != null) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
            statusLabel.setStyle("-fx-text-fill: #e11d48;");
        }
    }

    private void showSuccess(String msg) {
        if (statusLabel != null) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
            statusLabel.setStyle("-fx-text-fill: #059669;");
        }
    }
}
