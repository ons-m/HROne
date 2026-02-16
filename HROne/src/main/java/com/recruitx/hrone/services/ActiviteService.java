package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.Activite;
import com.recruitx.hrone.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActiviteService implements com.recruitx.hrone.services.IService<Activite> {

    private Connection cnx;

    public ActiviteService() {
        try {
            cnx = DBConnection.getInstance();
        } catch (Exception e) {

        }
    }

    @Override
    public void add(Activite a) {
        if (a.getTitre() == null || a.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'activité ne peut pas être vide.");
        }

        String req = "INSERT INTO `activite`(`Titre`, `Description`) VALUES (?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.executeUpdate();
            System.out.println("Activité Ajoutée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM `activite` WHERE ID_Activite = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
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

        String req = "UPDATE `activite` SET `Titre`=?,`Description`=? WHERE ID_Activite = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, a.getTitre());
            ps.setString(2, a.getDescription());
            ps.setInt(3, a.getIdActivite());
            ps.executeUpdate();
            System.out.println("Activité Modifiée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<Activite> getAll() {
        List<Activite> list = new ArrayList<>();
        String req = "SELECT * FROM `activite`";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Activite a = new Activite(
                        rs.getInt("ID_Activite"),
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
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                a = new Activite(
                        rs.getInt("ID_Activite"),
                        rs.getString("Titre"),
                        rs.getString("Description"));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return a;
    }
}
