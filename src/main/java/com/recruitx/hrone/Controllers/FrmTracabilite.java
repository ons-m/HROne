package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Controllers.Session;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Utils.DBConnection;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FrmTracabilite {

    @FXML
    private Label lblCurrentUser;
    @FXML
    private Label lblTotalActions;
    @FXML
    private Label lblStatus;

    @FXML
    private TableView<ActionRow> actionsTable;
    @FXML
    private TableColumn<ActionRow, String> dateColumn;
    @FXML
    private TableColumn<ActionRow, String> userColumn;
    @FXML
    private TableColumn<ActionRow, String> actionColumn;
    @FXML
    private TableColumn<ActionRow, String> commentColumn;

    @FXML
    public void initialize() {
        loadSessionData();
        initializeTable();
        loadActionHistory();
    }

    private void loadSessionData() {
        Utilisateur user = Session.getCurrentUser();

        if (user != null) {
            lblCurrentUser.setText("Connected: " + user.getNomUtilisateur());
        } else {
            lblCurrentUser.setText("Not connected");
        }
    }

    private void initializeTable() {
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDateHeure()));
        userColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUtilisateur()));
        actionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAction()));
        commentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCommentaire()));
    }

    private void loadActionHistory() {
        ObservableList<ActionRow> rows = FXCollections.observableArrayList();

        String sql = """
                SELECT
                    CONCAT(
                        LPAD(o.JJ, 2, '0'), '/',
                        LPAD(o.MM, 2, '0'), '/',
                        o.AAAA, ' ',
                        LPAD(o.HH, 2, '0'), ':',
                        LPAD(o.MN, 2, '0'), ':',
                        LPAD(o.SS, 2, '0')
                    ) AS action_date,
                    u.Nom_Utilisateur AS user_name,
                    ta.Description_Action AS action_name,
                    COALESCE(au.Commentaire, '') AS action_comment
                FROM Action_utilisateur au
                INNER JOIN UTILISATEUR u ON u.ID_UTILISATEUR = au.ID_UTILISATEUR
                INNER JOIN Type_Action ta ON ta.Code_Type_Action = au.Code_Type_Action
                INNER JOIN Ordre o ON o.Num_Ordre = au.Num_Ordre
                ORDER BY o.AAAA DESC, o.MM DESC, o.JJ DESC, o.HH DESC, o.MN DESC, o.SS DESC
                """;

        try {
            Connection connection = DBConnection.getInstance();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {
                    rows.add(new ActionRow(
                            resultSet.getString("action_date"),
                            resultSet.getString("user_name"),
                            resultSet.getString("action_name"),
                            resultSet.getString("action_comment")
                    ));
                }
            }

            actionsTable.setItems(rows);
            lblTotalActions.setText(rows.size() + " actions");
            lblStatus.setText(rows.isEmpty() ? "Aucune action enregistrée." : "");

        } catch (SQLException e) {
            actionsTable.setItems(rows);
            lblTotalActions.setText("0 actions");
            lblStatus.setText("Impossible de charger l'historique des actions.");
            e.printStackTrace();
        }
    }

    public static class ActionRow {
        private final String dateHeure;
        private final String utilisateur;
        private final String action;
        private final String commentaire;

        public ActionRow(String dateHeure, String utilisateur, String action, String commentaire) {
            this.dateHeure = dateHeure;
            this.utilisateur = utilisateur;
            this.action = action;
            this.commentaire = commentaire;
        }

        public String getDateHeure() {
            return dateHeure;
        }

        public String getUtilisateur() {
            return utilisateur;
        }

        public String getAction() {
            return action;
        }

        public String getCommentaire() {
            return commentaire;
        }
    }
}