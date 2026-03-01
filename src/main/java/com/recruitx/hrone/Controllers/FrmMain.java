package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Entreprise;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Controllers.Session;
import com.recruitx.hrone.Utils.ActionLogger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class FrmMain {

    @FXML private StackPane contentPane;
    @FXML private VBox sidebar;

    @FXML private Button btnUsers;
    @FXML private Button btnEmployee;
    @FXML private Button btnEntretiens;
    @FXML private Button btnEvenements;
    @FXML private Button btnFormations;
    @FXML private Button btnOffres;
    @FXML private Button btnTraining;
    @FXML private Button btnCommunaute;
    @FXML private Button GestionOffres;
    @FXML private Button btnEvents;
    @FXML private Button btnOutils;
    @FXML private Button btntracabilite;
    @FXML private Button btnGestionConge;
    @FXML private Button btnDemandeConge;
    @FXML private Button btnParticipation;
    @FXML private Button btnActivityWatch;


    private final Map<ViewType, Parent> viewCache = new HashMap<>();
    private Button currentActiveButton;

    public enum ViewType {
        GESTION_EMPLOYEE,
        GESTION_USERS,
        ENTRETIENS,
        EVENEMENTS,
        FORMATIONS,
        OFFRES,
        OUTILS,
        MES_CANDIDATURES,
        GESTIONOFFRES,
        TRAINING,
        COMMUNAUTE,
        EVENTS,
        ACTIVITES,
        LOGIN,
        SIGNUPCANDIDAT,
        SIGNUPENTREPRISE,
        TRACABILITE,
        GESTION_CONGE,
        DEMANDE_CONGE,
        CHATBOT,
        PARTICIPATIONS,
        ACTIVITY_WATCH
    }


    @FXML
    public void initialize() {
        hideSidebar();
        loadView(ViewType.LOGIN);
    }


    /* ================================
       Navigation Handlers
       ================================ */
    @FXML private void openGestionOffres() {loadView(ViewType.GESTIONOFFRES,GestionOffres);}

    @FXML private void openGestionEmployee() {
        loadView(ViewType.GESTION_EMPLOYEE, btnEmployee);
    }

    @FXML private void openGestionOutils() { loadView(ViewType.OUTILS, btnOutils); }

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

    @FXML private void openTracabilite() {loadView(ViewType.TRACABILITE, btntracabilite);}

    @FXML private void opengGestionConge() {loadView(ViewType.GESTION_CONGE, btnGestionConge);}

    @FXML private void openDemangeConge() {loadView(ViewType.DEMANDE_CONGE, btnDemandeConge);}

    @FXML private void openParticipations() {loadView(ViewType.PARTICIPATIONS);}

    @FXML private void openActivityWatch() {loadView(ViewType.ACTIVITY_WATCH);}
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

            if (Session.isLoggedIn() && type != ViewType.LOGIN) {
                ActionLogger.log("Navigation", "Ouverture page: " + type.name());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String resolvePath(ViewType type) {
        return switch (type) {
            //Interface Gestion Employee Cote HR
            case GESTION_EMPLOYEE ->
                    "/com/recruitx/hrone/View/FrmGestionEmployee.fxml";
            //Interface Gestion Outils Cote HR
            case OUTILS ->
                    "/com/recruitx/hrone/View/FrmGestionOutil.fxml";
            //Interface Gestion Users Cote Admin
            case GESTION_USERS ->
                    "/com/recruitx/hrone/View/FrmUserDashboard.fxml";
            //interface Entretiens Cote HR
            case ENTRETIENS ->
                    "/com/recruitx/hrone/View/FrmCandidatures.fxml";
            //interaface Evenement Cote Employee
            case EVENEMENTS ->
                    "/com/recruitx/hrone/View/FrmEvenement.fxml";
            //Interface Formation Cote HR
            case FORMATIONS ->
                    "/com/recruitx/hrone/View/FrmFormationDashboard.fxml";
            //Interface Candidature Cote Candidat
            case MES_CANDIDATURES ->
                    "/com/recruitx/hrone/View/FrmMesCandidatures.fxml";
            //Interface Liste Offre Cote Candidat
            case OFFRES ->
                    "/com/recruitx/hrone/View/FrmCandidat.fxml";
            //Interface Formation Cote Employee
            case TRAINING ->
                    "/com/recruitx/hrone/View/FrmFormation.fxml";
            //Inteface Communauté Cote Employee
            case COMMUNAUTE ->
                    "/com/recruitx/hrone/View/FrmBlog.fxml";
            //Interface Evenement Cote HR
            case EVENTS ->
                    "/com/recruitx/hrone/View/FrmEventsDashboard.fxml";
            //Interface Offre Cote HR
            case GESTIONOFFRES ->
                    "/com/recruitx/hrone/View/FrmGestionOffres.fxml";
            //Interface Activites Cote HR
            case ACTIVITES ->
                    "/com/recruitx/hrone/View/FrmActivites.fxml";
            //Interface SignUp Candidat
            case SIGNUPCANDIDAT ->
                    "/com/recruitx/hrone/View/FrmSignUpCandiat.fxml";
            //Interface SignUp Entreprise
            case SIGNUPENTREPRISE ->
                    "/com/recruitx/hrone/View/FrmSignUpEntreprise.fxml";
            //Interface Login
            case LOGIN ->
                    "/com/recruitx/hrone/View/FrmLogin.fxml";
            //Interface Tracabilite Cote Admin
            case TRACABILITE ->
                    "/com/recruitx/hrone/View/FrmTracabilite.fxml";
            //Interface Gestion Conge Cote RH
            case GESTION_CONGE ->
                    "/com/recruitx/hrone/View/FrmGestionConge.fxml";
            //Interface Demande Conge Cote Employee
            case DEMANDE_CONGE ->
                    "/com/recruitx/hrone/View/FrmDemandeConge.fxml";
            //Interface ChatBot Cote Employee
            case CHATBOT ->
                "/com/recruitx/hrone/View/FrmChatbot.fxml";
            //Interface Participations Evenement Cote Employee
            case PARTICIPATIONS ->
                "/com/recruitx/hrone/View/FrmParticipations.fxml";
            //Inteface Activity Watch Cote Employee
            case ACTIVITY_WATCH ->
                "/com/recruitx/hrone/View/FrmActivityWatch.fxml";
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

    public void showSidebar() {
        sidebar.setVisible(true);
        sidebar.setManaged(true);
    }

    public void hideSidebar() {
        sidebar.setVisible(false);
        sidebar.setManaged(false);
    }

    public void setCurrentUser(Utilisateur currentUser, Entreprise currEntreprise) {
        if(currentUser == null || currEntreprise == null){

        }

        Session.setCurrentUser(currentUser);
        Session.setCurrentEntreprise(currEntreprise);
    }

    public void setVisisiblityByRole(){
        int role = Session.getCurrentUser().getIdProfil();

        // First: hide everything
        hideAllSidebarButtons();

        switch (role) {

            // =========================
            // CANDIDAT
            // =========================
            case 1 -> {
                showSideBarButton(btnOffres);
            }

            // =========================
            // RH
            // =========================
            case 2 -> {
                showSideBarButton(btnEmployee);
                showSideBarButton(btnOutils);
                showSideBarButton(btnEntretiens);
                showSideBarButton(btnFormations);
                showSideBarButton(GestionOffres);
                showSideBarButton(btnEvenements);
                showSideBarButton(btnGestionConge);
                showSideBarButton(btnCommunaute);
                showSideBarButton(btntracabilite);
            }

            // =========================
            // EMPLOYEE
            // =========================
            case 3 -> {
                showSideBarButton(btnTraining);
                showSideBarButton(btnEvents);
                showSideBarButton(btnDemandeConge);
                showSideBarButton(btnCommunaute);
                showSideBarButton(btnParticipation);
                showSideBarButton(btnActivityWatch);
            }

        }

    }

    private void hideSideBarButton(Button button) {
        button.setVisible(false);
        button.setManaged(false);
    }

    private void showSideBarButton(Button button) {
        button.setVisible(true);
        button.setManaged(true);
    }

    private void hideAllSidebarButtons() {

        hideSideBarButton(btnUsers);
        hideSideBarButton(btnEmployee);
        hideSideBarButton(btnEntretiens);
        hideSideBarButton(btnEvenements);
        hideSideBarButton(btnFormations);
        hideSideBarButton(btnOffres);
        hideSideBarButton(btnTraining);
        hideSideBarButton(btnCommunaute);
        hideSideBarButton(GestionOffres);
        hideSideBarButton(btnEvents);
        //hideSideBarButton(btnGererMesOffres);
        hideSideBarButton(btnOutils);
        hideSideBarButton(btntracabilite);
        hideSideBarButton(btnGestionConge);
        hideSideBarButton(btnDemandeConge);
        hideSideBarButton(btnParticipation);
        hideSideBarButton(btnActivityWatch);
    }
}