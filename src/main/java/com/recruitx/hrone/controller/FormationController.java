package com.recruitx.hrone.controller;

import com.recruitx.hrone.dao.FormationDAO;
import com.recruitx.hrone.models.Formation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class FormationController {

    @FXML
    private ComboBox<String> formationSelect;


    @FXML private TableView<Formation> tableViewFormations;
    @FXML private TableColumn<Formation, Integer> colId;
    @FXML private TableColumn<Formation, String> colTitre;
    @FXML private TableColumn<Formation, Integer> colOrdre;

    @FXML private TextField txtId;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtOrdre;
    @FXML private TextField txtEntrepriseId;
    @FXML private TextField txtImage;
    @FXML private TextField txtSearch;

    private FormationDAO formationDAO;
    private ObservableList<Formation> formationList;

    @FXML
    public void initialize() {
        formationDAO = new FormationDAO();
        formationList = FXCollections.observableArrayList();

        // Configurer les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idFormation"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("numOrdreCreation"));

        tableViewFormations.setItems(formationList);

        // Charger toutes les formations
        loadAllFormations();

        // Écouter la sélection
        tableViewFormations.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showFormationDetails(newValue)
        );

        // Auto-ordre
        updateAutoOrder();
    }

    @FXML
    private void handleAjouter() {
        if (validateFields()) {
            Formation formation = new Formation();
            formation.setTitre(txtTitre.getText());
            formation.setDescription(txtDescription.getText());
            formation.setNumOrdreCreation(Integer.parseInt(txtOrdre.getText()));
            formation.setIdEntreprise(Integer.parseInt(txtEntrepriseId.getText()));
            formation.setImage(txtImage.getText());

            if (formationDAO.create(formation)) {
                showAlert("Succès", "Formation ajoutée !");
                clearFields();
                loadAllFormations();
                updateAutoOrder();
            }
        }
    }

    @FXML
    private void handleModifier() {
        Formation selected = tableViewFormations.getSelectionModel().getSelectedItem();
        if (selected != null && validateFields()) {
            selected.setTitre(txtTitre.getText());
            selected.setDescription(txtDescription.getText());
            selected.setNumOrdreCreation(Integer.parseInt(txtOrdre.getText()));
            selected.setIdEntreprise(Integer.parseInt(txtEntrepriseId.getText()));
            selected.setImage(txtImage.getText());

            if (formationDAO.update(selected)) {
                showAlert("Succès", "Formation modifiée !");
                loadAllFormations();
            }
        } else {
            showAlert("Attention", "Sélectionnez une formation !");
        }
    }

    @FXML
    private void handleSupprimer() {
        Formation selected = tableViewFormations.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la formation");
            alert.setContentText("Êtes-vous sûr ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (formationDAO.delete(selected.getIdFormation())) {
                    showAlert("Succès", "Formation supprimée !");
                    clearFields();
                    loadAllFormations();
                    updateAutoOrder();
                }
            }
        } else {
            showAlert("Attention", "Sélectionnez une formation !");
        }
    }

    @FXML
    private void handleAutoOrdre() {
        updateAutoOrder();
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtSearch.getText().trim();
        if (!keyword.isEmpty()) {
            formationList.setAll(formationDAO.search(keyword));
        } else {
            loadAllFormations();
        }
    }

    @FXML
    private void handleFiltrerParEntreprise() {
        if (!txtEntrepriseId.getText().isEmpty()) {
            try {
                int entrepriseId = Integer.parseInt(txtEntrepriseId.getText());
                List<Formation> formations = formationDAO.readByEntreprise(entrepriseId);
                formationList.setAll(formations);
            } catch (NumberFormatException e) {
                showAlert("Erreur", "ID Entreprise invalide !");
            }
        }
    }

    @FXML
    private void handleAfficherTous() {
        loadAllFormations();
    }

    @FXML
    private void handleEffacer() {
        clearFields();
    }

    private void loadAllFormations() {
        formationList.setAll(formationDAO.readAll());
    }

    private void showFormationDetails(Formation formation) {
        if (formation != null) {
            txtId.setText(String.valueOf(formation.getIdFormation()));
            txtTitre.setText(formation.getTitre());
            txtDescription.setText(formation.getDescription());
            txtOrdre.setText(String.valueOf(formation.getNumOrdreCreation()));
            txtEntrepriseId.setText(String.valueOf(formation.getIdEntreprise()));
            txtImage.setText(formation.getImage());
        }
    }

    private void updateAutoOrder() {
        int nextOrder = formationDAO.getNextOrderNumber();
        txtOrdre.setText(String.valueOf(nextOrder));
    }

    private boolean validateFields() {
        if (txtTitre.getText().isEmpty() || txtOrdre.getText().isEmpty() ||
                txtEntrepriseId.getText().isEmpty()) {
            showAlert("Erreur", "Remplissez les champs obligatoires (Titre, Ordre, ID Entreprise) !");
            return false;
        }

        try {
            Integer.parseInt(txtOrdre.getText());
            Integer.parseInt(txtEntrepriseId.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Ordre et ID Entreprise doivent être des nombres !");
            return false;
        }
    }

    private void clearFields() {
        txtId.clear();
        txtTitre.clear();
        txtDescription.clear();
        txtEntrepriseId.clear();
        txtImage.clear();
        txtSearch.clear();
        updateAutoOrder();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}