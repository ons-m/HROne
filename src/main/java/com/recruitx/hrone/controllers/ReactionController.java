package com.recruitx.hrone.controllers;

import com.recruitx.hrone.dao.ReactionDAO;
import com.recruitx.hrone.dao.TypeReactionDAO;
import com.recruitx.hrone.models.Reaction;
import com.recruitx.hrone.models.TypeReaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class ReactionController {

    @FXML private TableView<Reaction> tableViewReactions;
    @FXML private TableColumn<Reaction, Integer> colId;
    @FXML private TableColumn<Reaction, String> colType;
    @FXML private TableColumn<Reaction, Integer> colCommentaireId;

    @FXML private TextField txtId;
    @FXML private TextField txtCommentaireId;
    @FXML private ComboBox<String> comboType;
    @FXML private TextField txtUserId;
    @FXML private TextField txtOrdre;
    @FXML private TextField txtSearch;

    private ReactionDAO reactionDAO;
    private TypeReactionDAO typeReactionDAO;
    private ObservableList<Reaction> reactionList;
    private ObservableList<String> typeList;

    @FXML
    public void initialize() {
        reactionDAO = new ReactionDAO();
        typeReactionDAO = new TypeReactionDAO();
        reactionList = FXCollections.observableArrayList();
        typeList = FXCollections.observableArrayList();

        // Configurer les colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("idReaction"));
        colType.setCellValueFactory(new PropertyValueFactory<>("codeTypeReaction"));
        colCommentaireId.setCellValueFactory(new PropertyValueFactory<>("idCommentaire"));

        tableViewReactions.setItems(reactionList);

        // Charger les types de réaction
        loadTypes();

        // Charger toutes les réactions
        loadAllReactions();

        // Écouter la sélection
        tableViewReactions.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showReactionDetails(newValue)
        );

        // Auto-ordre quand commentaire change
        txtCommentaireId.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                try {
                    int commentaireId = Integer.parseInt(newValue);
                    txtOrdre.setText(String.valueOf(reactionDAO.getNextOrderNumber(commentaireId)));
                } catch (NumberFormatException e) {
                    // Ignorer
                }
            }
        });
    }

    @FXML
    private void handleAjouter() {
        if (validateFields()) {
            Reaction reaction = new Reaction();
            reaction.setIdCommentaire(Integer.parseInt(txtCommentaireId.getText()));
            reaction.setCodeTypeReaction(comboType.getValue());
            reaction.setIdUtilisateur(Integer.parseInt(txtUserId.getText()));
            reaction.setNumOrdreReaction(Integer.parseInt(txtOrdre.getText()));

            // Vérifier si l'utilisateur a déjà réagi
            if (reactionDAO.hasUserReacted(reaction.getIdCommentaire(), reaction.getIdUtilisateur())) {
                showAlert("Attention", "Cet utilisateur a déjà réagi à ce commentaire !");
                return;
            }

            if (reactionDAO.create(reaction)) {
                showAlert("Succès", "Réaction ajoutée !");
                clearFields();
                loadAllReactions();
            }
        }
    }

    @FXML
    private void handleModifier() {
        Reaction selected = tableViewReactions.getSelectionModel().getSelectedItem();
        if (selected != null && validateFields()) {
            selected.setIdCommentaire(Integer.parseInt(txtCommentaireId.getText()));
            selected.setCodeTypeReaction(comboType.getValue());
            selected.setIdUtilisateur(Integer.parseInt(txtUserId.getText()));
            selected.setNumOrdreReaction(Integer.parseInt(txtOrdre.getText()));

            if (reactionDAO.update(selected)) {
                showAlert("Succès", "Réaction modifiée !");
                loadAllReactions();
            }
        } else {
            showAlert("Attention", "Sélectionnez une réaction !");
        }
    }

    @FXML
    private void handleSupprimer() {
        Reaction selected = tableViewReactions.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer la réaction");
            alert.setContentText("Êtes-vous sûr ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (reactionDAO.delete(selected.getIdReaction())) {
                    showAlert("Succès", "Réaction supprimée !");
                    clearFields();
                    loadAllReactions();
                }
            }
        } else {
            showAlert("Attention", "Sélectionnez une réaction !");
        }
    }

    @FXML
    private void handleAutoOrdre() {
        if (!txtCommentaireId.getText().isEmpty()) {
            try {
                int commentaireId = Integer.parseInt(txtCommentaireId.getText());
                int nextOrder = reactionDAO.getNextOrderNumber(commentaireId);
                txtOrdre.setText(String.valueOf(nextOrder));
            } catch (NumberFormatException e) {
                showAlert("Erreur", "ID Commentaire invalide !");
            }
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtSearch.getText().trim();
        // Recherche simplifiée - vous pouvez améliorer
        if (!keyword.isEmpty()) {
            showAlert("Info", "La recherche par texte n'est pas disponible pour les réactions. Filtrez par ID.");
        } else {
            loadAllReactions();
        }
    }

    @FXML
    private void handleEffacer() {
        clearFields();
    }

    private void loadAllReactions() {
        reactionList.setAll(reactionDAO.readAll());
    }

    private void loadTypes() {
        List<TypeReaction> types = typeReactionDAO.readAll();
        for (TypeReaction type : types) {
            typeList.add(type.getCodeTypeReaction());
        }
        comboType.setItems(typeList);
    }

    private void showReactionDetails(Reaction reaction) {
        if (reaction != null) {
            txtId.setText(String.valueOf(reaction.getIdReaction()));
            txtCommentaireId.setText(String.valueOf(reaction.getIdCommentaire()));
            comboType.setValue(reaction.getCodeTypeReaction());
            txtUserId.setText(String.valueOf(reaction.getIdUtilisateur()));
            txtOrdre.setText(String.valueOf(reaction.getNumOrdreReaction()));
        }
    }

    private boolean validateFields() {
        if (txtCommentaireId.getText().isEmpty() || comboType.getValue() == null ||
                txtUserId.getText().isEmpty() || txtOrdre.getText().isEmpty()) {
            showAlert("Erreur", "Remplissez tous les champs !");
            return false;
        }

        try {
            Integer.parseInt(txtCommentaireId.getText());
            Integer.parseInt(txtUserId.getText());
            Integer.parseInt(txtOrdre.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Les IDs et l'ordre doivent être des nombres !");
            return false;
        }
    }

    private void clearFields() {
        txtId.clear();
        txtCommentaireId.clear();
        comboType.setValue(null);
        txtUserId.clear();
        txtOrdre.clear();
        txtSearch.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}