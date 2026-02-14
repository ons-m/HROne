package com.recruitx.hrone.controllers;

import com.recruitx.hrone.dao.TypeReactionDAO;
import com.recruitx.hrone.models.TypeReaction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class TypeReactionController {

    @FXML private TableView<TypeReaction> tableView;
    @FXML private TableColumn<TypeReaction, String> colCode;
    @FXML private TableColumn<TypeReaction, String> colDescription;

    @FXML private TextField txtCode;
    @FXML private TextField txtDescription;

    private TypeReactionDAO dao;
    private ObservableList<TypeReaction> list;

    @FXML
    public void initialize() {
        dao = new TypeReactionDAO();
        list = FXCollections.observableArrayList();

        colCode.setCellValueFactory(new PropertyValueFactory<>("codeTypeReaction"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("descriptionReaction"));

        tableView.setItems(list);
        loadAll();

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDetails(newValue)
        );
    }

    @FXML
    private void handleAjouter() {
        if (validate()) {
            TypeReaction type = new TypeReaction(
                    txtCode.getText().toUpperCase(),
                    txtDescription.getText()
            );

            if (dao.create(type)) {
                showAlert("Succès", "Type de réaction ajouté !");
                clear();
                loadAll();
            }
        }
    }

    @FXML
    private void handleModifier() {
        TypeReaction selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && validate()) {
            selected.setDescriptionReaction(txtDescription.getText());

            if (dao.update(selected)) {
                showAlert("Succès", "Type de réaction modifié !");
                loadAll();
            }
        } else {
            showAlert("Attention", "Sélectionnez un type !");
        }
    }

    @FXML
    private void handleSupprimer() {
        TypeReaction selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le type de réaction");
            alert.setContentText("Êtes-vous sûr ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (dao.delete(selected.getCodeTypeReaction())) {
                    showAlert("Succès", "Type de réaction supprimé !");
                    clear();
                    loadAll();
                }
            }
        } else {
            showAlert("Attention", "Sélectionnez un type !");
        }
    }

    @FXML
    private void handleEffacer() {
        clear();
    }

    private void loadAll() {
        list.setAll(dao.readAll());
    }

    private void showDetails(TypeReaction type) {
        if (type != null) {
            txtCode.setText(type.getCodeTypeReaction());
            txtDescription.setText(type.getDescriptionReaction());
        }
    }

    private boolean validate() {
        if (txtCode.getText().isEmpty() || txtDescription.getText().isEmpty()) {
            showAlert("Erreur", "Remplissez tous les champs !");
            return false;
        }

        // Vérifier si le code existe déjà (pour l'ajout)
        if (!tableView.getSelectionModel().isEmpty() &&
                dao.exists(txtCode.getText().toUpperCase())) {
            showAlert("Attention", "Ce code existe déjà !");
            return false;
        }

        return true;
    }

    private void clear() {
        txtCode.clear();
        txtDescription.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}