package com.recruitx.hrone.controllers;

import com.recruitx.hrone.dao.PostDAO;
import com.recruitx.hrone.models.Post;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class PostController {

    @FXML private TableView<Post> tableViewPosts;
    @FXML private TableColumn<Post, Integer> colId;
    @FXML private TableColumn<Post, String> colTitre;
    @FXML private TableColumn<Post, Integer> colOrdre;

    @FXML private TextField txtId;
    @FXML private TextField txtUserId;
    @FXML private TextField txtTitre;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtImage;
    @FXML private TextField txtOrdre;
    @FXML private TextField txtSearch;

    private PostDAO postDAO;
    private ObservableList<Post> postList;

    @FXML
    public void initialize() {
        postDAO = new PostDAO();
        postList = FXCollections.observableArrayList();

        colId.setCellValueFactory(new PropertyValueFactory<>("idPost"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("numOrdrePost"));

        tableViewPosts.setItems(postList);
        loadPosts();

        tableViewPosts.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showPostDetails(newValue)
        );
    }

    @FXML
    private void handleAjouter() {
        if (validateFields()) {
            Post post = new Post(
                    Integer.parseInt(txtUserId.getText()),
                    txtTitre.getText(),
                    txtDescription.getText(),
                    txtImage.getText(),
                    Integer.parseInt(txtOrdre.getText())
            );

            if (postDAO.create(post)) {
                showAlert("Succès", "Post ajouté!");
                clearFields();
                loadPosts();
            }
        }
    }

    @FXML
    private void handleModifier() {
        Post selectedPost = tableViewPosts.getSelectionModel().getSelectedItem();
        if (selectedPost != null && validateFields()) {
            selectedPost.setIdUtilisateur(Integer.parseInt(txtUserId.getText()));
            selectedPost.setTitre(txtTitre.getText());
            selectedPost.setDescription(txtDescription.getText());
            selectedPost.setImage(txtImage.getText());
            selectedPost.setNumOrdrePost(Integer.parseInt(txtOrdre.getText()));

            if (postDAO.update(selectedPost)) {
                showAlert("Succès", "Post modifié!");
                loadPosts();
            }
        } else {
            showAlert("Attention", "Sélectionnez un post!");
        }
    }

    @FXML
    private void handleSupprimer() {
        Post selectedPost = tableViewPosts.getSelectionModel().getSelectedItem();
        if (selectedPost != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation");
            alert.setHeaderText("Supprimer le post");
            alert.setContentText("Êtes-vous sûr?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                if (postDAO.delete(selectedPost.getIdPost())) {
                    showAlert("Succès", "Post supprimé!");
                    clearFields();
                    loadPosts();
                }
            }
        } else {
            showAlert("Attention", "Sélectionnez un post!");
        }
    }

    @FXML
    private void handleRechercher() {
        String keyword = txtSearch.getText().trim();
        if (!keyword.isEmpty()) {
            postList.setAll(postDAO.search(keyword));
        } else {
            loadPosts();
        }
    }

    @FXML
    private void handleEffacer() {
        clearFields();
    }

    private void loadPosts() {
        postList.setAll(postDAO.readAll());
    }

    private void showPostDetails(Post post) {
        if (post != null) {
            txtId.setText(String.valueOf(post.getIdPost()));
            txtUserId.setText(String.valueOf(post.getIdUtilisateur()));
            txtTitre.setText(post.getTitre());
            txtDescription.setText(post.getDescription());
            txtImage.setText(post.getImage());
            txtOrdre.setText(String.valueOf(post.getNumOrdrePost()));
        }
    }

    private boolean validateFields() {
        if (txtUserId.getText().isEmpty() || txtTitre.getText().isEmpty() ||
                txtOrdre.getText().isEmpty()) {
            showAlert("Erreur", "Remplissez tous les champs!");
            return false;
        }

        try {
            Integer.parseInt(txtUserId.getText());
            Integer.parseInt(txtOrdre.getText());
            return true;
        } catch (NumberFormatException e) {
            showAlert("Erreur", "ID et Ordre doivent être des nombres!");
            return false;
        }
    }

    private void clearFields() {
        txtId.clear();
        txtUserId.clear();
        txtTitre.clear();
        txtDescription.clear();
        txtImage.clear();
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