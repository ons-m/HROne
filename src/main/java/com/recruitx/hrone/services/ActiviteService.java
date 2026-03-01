package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.Activite;
import com.recruitx.hrone.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService implements com.recruitx.hrone.services.IService<Activite> {

    public ActiviteService() {
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance();
    }

    @Override
    public void add(Activite a) {
        if (a.getTitre() == null || a.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'activité ne peut pas être vide.");
        }

        String req = "INSERT INTO `activite`(`Titre`, `Description`, `ID_Evenement`) VALUES (?,?,?)";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setInt(3, a.getIdEvenement());
            ps.executeUpdate();
            System.out.println("Activité Ajoutée !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'ajout de l'activité : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM `activite` WHERE ID_Activite = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Activité Supprimée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void update(Activite a) {
        if (a.getTitre() == null || a.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'activité ne peut pas être vide.");
        }

        String req = "UPDATE `activite` SET `Titre`=?,`Description`=?, `ID_Evenement`=? WHERE ID_Activite = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setInt(3, a.getIdEvenement());
            ps.setInt(4, a.getIdActivite());
            ps.executeUpdate();
            System.out.println("Activité Modifiée !");
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de la modification de l'activité : " + e.getMessage(), e);
        }
    }

    @Override
    public List<Activite> getAll() {
        List<Activite> list = new ArrayList<>();
        String req = "SELECT * FROM `activite` ORDER BY ID_Activite DESC";
        try (Connection cnx = getConnection();
                Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                int idEv = 0;
                try {
                    idEv = rs.getInt("ID_Evenement");
                } catch (SQLException ignore) {
                    // La colonne n'existe pas encore
                }
                Activite a = new Activite(
                        rs.getInt("ID_Activite"),
                        idEv,
                        rs.getString("Titre"),
                        rs.getString("Description"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public Activite getOne(int id) {
        Activite a = null;
        String req = "SELECT * FROM `activite` WHERE ID_Activite = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int idEv = 0;
                try {
                    idEv = rs.getInt("ID_Evenement");
                } catch (SQLException ignore) {
                }
                a = new Activite(
                        rs.getInt("ID_Activite"),
                        idEv,
                        rs.getString("Titre"),
                        rs.getString("Description"));
            }
        } catch (SQLException e) {
            System.err.println("ERREUR SQL (getOne) : " + e.getMessage());
            e.printStackTrace();
        }
        return a;
    }

    public List<Activite> getByEvenement(int idEvenement) {
        List<Activite> list = new ArrayList<>();
        String req = "SELECT * FROM `activite` WHERE ID_Evenement = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int idEv = 0;
                try {
                    idEv = rs.getInt("ID_Evenement");
                } catch (SQLException ignore) {
                }
                Activite a = new Activite(
                        rs.getInt("ID_Activite"),
                        idEv,
                        rs.getString("Titre"),
                        rs.getString("Description"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }
}
