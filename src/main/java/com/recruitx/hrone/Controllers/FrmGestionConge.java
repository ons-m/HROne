package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Utilisateur;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class FrmGestionConge {

    @FXML
    private Label lblCurrentUser;

    @FXML
    public void initialize() {
        loadSessionData();
    }

    private void loadSessionData() {
        Utilisateur user = Session.getCurrentUser();

        if (user != null) {
            lblCurrentUser.setText("Connected: " + user.getNomUtilisateur());
        } else {
            lblCurrentUser.setText("Not connected");
        }
    }
}