package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Activite;
import com.recruitx.hrone.Models.Evenement;
import com.recruitx.hrone.Models.ParticipationEvenement;
import com.recruitx.hrone.Repository.ActiviteRepository;
import com.recruitx.hrone.Repository.EvenementRepository;
import com.recruitx.hrone.Repository.ParticipationEvenementRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;

public class FrmParticipation implements NavigationAware {

    @FXML
    private VBox participationsList;

    private final ParticipationEvenementRepository service = new ParticipationEvenementRepository();
    private final EvenementRepository evenementService = new EvenementRepository();
    private final ActiviteRepository activiteService = new ActiviteRepository();

    private FrmMain mainController;

    @Override
    public void setMainController(FrmMain mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadData();
    }

    private void loadData() {
        if (participationsList == null) {
            System.err.println("[ParticipationsController] participationsList fx:id est NULL !");
            return;
        }
        participationsList.getChildren().clear();

        List<ParticipationEvenement> list = service.getAll();
        System.out.println("[ParticipationsController] " + list.size() + " participations récupérées de la DB.");

        if (list.isEmpty()) {
            Label placeholder = new Label("Vous n'avez aucune participation pour le moment.");
            placeholder.getStyleClass().add("muted");
            participationsList.getChildren().add(placeholder);
            return;
        }

        for (ParticipationEvenement pe : list) {
            participationsList.getChildren().add(createParticipationCard(pe));
        }
    }

    private VBox createParticipationCard(ParticipationEvenement pe) {
        VBox card = new VBox();
        card.getStyleClass().add("resource-item");

        Evenement ev = evenementService.getOne(pe.getIdEvenement());
        Activite act = activiteService.getOne(pe.getIdActivite());

        String eventName = (ev != null) ? ev.getTitre() : "Événement inconnu";
        String activityName = (act != null) ? act.getTitre() : "Activité inconnue";

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(5);
        Label lTitre = new Label("Événement: " + eventName);
        lTitre.getStyleClass().add("resource-title");

        Label lSub = new Label("Activité: " + activityName);
        lSub.getStyleClass().add("resource-meta");

        String txtNom = (pe.getNomComplet() != null && !pe.getNomComplet().isEmpty()) ? pe.getNomComplet()
                : "Non renseigné";
        String txtEmail = (pe.getEmail() != null && !pe.getEmail().isEmpty()) ? pe.getEmail() : "Non renseigné";
        String txtPaiement = (pe.getModePaiement() != null && !pe.getModePaiement().isEmpty()) ? pe.getModePaiement()
                : "Gratuit";

        Label lInfo = new Label("Nom: " + txtNom + " | Email: " + txtEmail + " | Paiement: " + txtPaiement);
        lInfo.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        Label lMessage = new Label("Message: " + (pe.getDescription() != null ? pe.getDescription() : ""));
        lMessage.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px; -fx-font-style: italic;");
        lMessage.setWrapText(true);

        titleBox.getChildren().addAll(lTitre, lSub, lInfo, lMessage);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDelete = new Button("Supprimer");
        btnDelete.getStyleClass().addAll("btn", "btn-small", "btn-danger");
        btnDelete.setOnAction(e -> handleDelete(pe));

        HBox actions = new HBox(10, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        header.getChildren().addAll(titleBox, spacer, actions);
        card.getChildren().add(header);

        return card;
    }

    private void handleDelete(ParticipationEvenement pe) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Vous êtes sur le point d'annuler cette participation.");
        alert.setContentText("Êtes-vous sûr de vouloir continuer ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ParticipationEvenement promoted = service.delete(pe.getIdEvenement(), pe.getIdActivite(),
                        pe.getIdParticipant());
                loadData();

                if (promoted != null) {
                    Alert promoAlert = new Alert(Alert.AlertType.INFORMATION);
                    promoAlert.setTitle("Place Libérée !");
                    promoAlert.setHeaderText("Promotion automatique effectuée");
                    promoAlert.setContentText("Une place s'est libérée. " + promoted.getNomComplet()
                            + " a été retiré de la liste d'attente et ajouté aux participations !");
                    promoAlert.show();
                }
            }
        });
    }

    @FXML
    private void goBack() {
        if (mainController != null) {
            mainController.loadView(FrmMain.ViewType.EVENTS);
        }
    }
}
