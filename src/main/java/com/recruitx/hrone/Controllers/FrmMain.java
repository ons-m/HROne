package com.recruitx.hrone.Controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

import java.util.HashMap;
import java.util.Map;

public class FrmMain {

    @FXML private StackPane contentPane;

    @FXML private Button btnUsers;
    @FXML private Button btnEmployee;
    @FXML private Button btnEntretiens;
    @FXML private Button btnEvenements;
    @FXML private Button btnFormations;
    @FXML private Button btnOffres;
    @FXML private Button btnTraining;
    @FXML private Button btnCommunaute;
    @FXML private Button btnEvents;
    @FXML private Button btnGererMesOffres;

    private final Map<ViewType, Parent> viewCache = new HashMap<>();
    private Button currentActiveButton;

    public enum ViewType {
        GESTION_EMPLOYEE,
        GESTION_USERS,
        ENTRETIENS,
        EVENEMENTS,
        FORMATIONS,
        OFFRES,
        MES_CANDIDATURES,
        GESTIONOFFRES,
        TRAINING,
        COMMUNAUTE,
        EVENTS
    }

    /* ================================
       Navigation Handlers
       ================================ */
    @FXML private void openGestionOffres() {loadView(ViewType.GESTIONOFFRES,btnCommunaute);}

    @FXML private void openGestionEmployee() {
        loadView(ViewType.GESTION_EMPLOYEE, btnEmployee);
    }

    @FXML private void openGestionUsers() {
        loadView(ViewType.GESTION_USERS, btnUsers);
    }

    @FXML private void openEntretiens() {
        loadView(ViewType.ENTRETIENS, btnEntretiens);
    }

    @FXML private void openEvenements() {
        loadView(ViewType.EVENEMENTS, btnEvenements);
    }

    @FXML private void openFormations() {
        loadView(ViewType.FORMATIONS, btnFormations);
    }

    @FXML private void openOffres() {
        loadView(ViewType.OFFRES, btnOffres);
    }

    @FXML private void openTraining() {
        loadView(ViewType.TRAINING, btnTraining);
    }

    @FXML private void openCommunaute() {
        loadView(ViewType.COMMUNAUTE, btnCommunaute);
    }

    @FXML private void openEvents() {
        loadView(ViewType.EVENTS, btnEvents);
    }

    /* ================================
       Core Loader (With Cache + Highlight)
       ================================ */

    public void loadView(ViewType type) {
        loadView(type, null);
    }

    private void loadView(ViewType type, Button clickedButton) {
        try {
            Parent view;

            if (viewCache.containsKey(type)) {
                view = viewCache.get(type);
            } else {
                String path = resolvePath(type);
                FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
                view = loader.load();

                Object controller = loader.getController();
                if (controller instanceof NavigationAware navAware) {
                    navAware.setMainController(this);
                }

                viewCache.put(type, view);
            }

            contentPane.getChildren().setAll(view);

            if (clickedButton != null) {
                setActiveButton(clickedButton);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resolvePath(ViewType type) {
        return switch (type) {
            case GESTION_EMPLOYEE ->
                    "/com/recruitx/hrone/View/FrmGestionEmployee.fxml";
            case GESTION_USERS ->
                    "/com/recruitx/hrone/View/FrmGestionUsers.fxml";
            //Todo : Ajouter Interface Entretien
            case ENTRETIENS ->
                    "/com/recruitx/hrone/View/FrmGestionOffres.fxml";
            case EVENEMENTS ->
                    "/com/recruitx/hrone/View/FrmEvenements.fxml";
            case FORMATIONS ->
                    "/com/recruitx/hrone/View/FrmFormations.fxml";
            case MES_CANDIDATURES ->
                    "/com/recruitx/hrone/View/FrmMesCandidatures.fxml";
            case OFFRES ->
                    "/com/recruitx/hrone/View/FrmCandidat.fxml";
            case TRAINING ->
                    "/com/recruitx/hrone/View/FrmTraining.fxml";
            case COMMUNAUTE ->
                    "/com/recruitx/hrone/View/FrmCommunaute.fxml";
            case EVENTS ->
                    "/com/recruitx/hrone/View/FrmEvents.fxml";
            case GESTIONOFFRES ->
                    "/com/recruitx/hrone/View/FrmGestionOffres.fxml";
        };
    }

    /* ================================
       Active Button Styling
       ================================ */

    private void setActiveButton(Button button) {

        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-link-active");
        }

        button.getStyleClass().add("nav-link-active");
        currentActiveButton = button;
    }
}