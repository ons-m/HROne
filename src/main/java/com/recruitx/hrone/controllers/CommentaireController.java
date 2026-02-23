package com.recruitx.hrone.controllers;

import com.recruitx.hrone.dao.CommentaireDAO;
import com.recruitx.hrone.dao.PostDAO;
import com.recruitx.hrone.models.Commentaire;
import com.recruitx.hrone.models.Post;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class CommentaireController {

    @FXML private TableView<Commentaire> tableView;
    @FXML private TableColumn<Commentaire, Integer> colId;
    @FXML private TableColumn<Commentaire, String> colContenu;
    @FXML private TableColumn<Commentaire, Integer> colPostId;

    @FXML private TextField txtId;
    @FXML private TextField txtUserId;
    @FXML private TextField txtPostId;
    @FXML private CheckBox chkReponse;
    @FXML private TextField txtParentId;
    @FXML private TextField txtOrdre;
    @FXML private TextArea txtContenu;
    @FXML private TextField txtSearch;

    private CommentaireDAO dao;
    private ObservableList<Commentaire> list;

    @FXML
    public void initialize() {
        dao = new CommentaireDAO();
        list = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("idCommentaire"));
        colContenu.setCellValueFactory(new PropertyValueFactory<>("contenu"));
        colPostId.setCellValueFactory(new PropertyValueFactory<>("idPost"));

        tableView.setItems(list);
        loadAll();

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showDetails(newValue)
        );
    }

    @FXML
    private void handleAjouter() {
        if (validate()) {
            Commentaire c = new Commentaire();
            c.setIdUtilisateur(Integer.parseInt(txtUserId.getText()));
            c.setIdPost(Integer.parseInt(txtPostId.getText()));
            c.setEstReponse(chkReponse.isSelected());

            if (!txtParentId.getText().isEmpty()) {
                c.setIdParent(Integer.parseInt(txtParentId.getText()));
            }

            c.setNumOrdreCommentaire(Integer.parseInt(txtOrdre.getText()));
            c.setContenu(txtContenu.getText());

            if (dao.create(c)) {
                showAlert("Succès", "Commentaire ajouté !");
                clear();
                loadAll();
            }
        }
    }

    @FXML
    private void handleModifier() {
        Commentaire selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null && validate()) {
            selected.setIdUtilisateur(Integer.parseInt(txtUserId.getText()));
            selected.setIdPost(Integer.parseInt(txtPostId.getText()));
            selected.setEstReponse(chkReponse.isSelected());

            if (!txtParentId.getText().isEmpty()) {
                selected.setIdParent(Integer.parseInt(txtParentId.getText()));
            } else {
                selected.setIdParent(null);
            }

            selected.setNumOrdreCommentaire(Integer.parseInt(txtOrdre.getText()));
            selected.setContenu(txtContenu.getText());

            if (dao.update(selected)) {
                showAlert("Succès", "Commentaire modifié !");
                loadAll();
            }
        } else {
            showAlert("Attention", "Sélectionnez un commentaire !");
        }
    }

    @FXML
    private void handleSupprimer() {
        Commentaire selected = tableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le commentaire");
            alert.setContentText("Êtes-vous sûr ?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (dao.delete(selected.getIdCommentaire())) {
                    showAlert("Succès", "Commentaire supprimé !");
                    clear();
                    loadAll();
                }
            }
        } else {
            showAlert("Attention", "Sélectionnez un commentaire !");
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtSearch.getText().trim();
        if (!keyword.isEmpty()) {
            list.setAll(dao.search(keyword));
        } else {
            loadAll();
        }
    }

    @FXML
    private void handleAutoOrdre() {
        if (!txtPostId.getText().isEmpty()) {
            try {
                int postId = Integer.parseInt(txtPostId.getText());
                int next = dao.getNextOrderNumber(postId);
                txtOrdre.setText(String.valueOf(next));
            } catch (Exception e) {
                showAlert("Erreur", "ID Post invalide !");
            }
        }
    }

    @FXML
    private void handleEffacer() {
        clear();
    }

    private void loadAll() {
        list.setAll(dao.readAll());
    }

    private void showDetails(Commentaire c) {
        if (c != null) {
            txtId.setText(String.valueOf(c.getIdCommentaire()));
            txtUserId.setText(String.valueOf(c.getIdUtilisateur()));
            txtPostId.setText(String.valueOf(c.getIdPost()));
            chkReponse.setSelected(c.isEstReponse());

            if (c.getIdParent() != null) {
                txtParentId.setText(String.valueOf(c.getIdParent()));
            } else {
                txtParentId.clear();
            }

            txtOrdre.setText(String.valueOf(c.getNumOrdreCommentaire()));
            txtContenu.setText(c.getContenu());
        }
    }

    private boolean validate() {
        if (txtUserId.getText().isEmpty() || txtPostId.getText().isEmpty() ||
                txtOrdre.getText().isEmpty() || txtContenu.getText().isEmpty()) {
            showAlert("Erreur", "Remplissez tous les champs !");
            return false;
        }

        try {
            Integer.parseInt(txtUserId.getText());
            Integer.parseInt(txtPostId.getText());
            Integer.parseInt(txtOrdre.getText());

            if (!txtParentId.getText().isEmpty()) {
                Integer.parseInt(txtParentId.getText());
            }
            return true;
        } catch (Exception e) {
            showAlert("Erreur", "Les IDs doivent être des nombres !");
            return false;
        }
    }

    private void clear() {
        txtId.clear();
        txtUserId.clear();
        txtPostId.clear();
        chkReponse.setSelected(false);
        txtParentId.clear();
        txtOrdre.clear();
        txtContenu.clear();
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