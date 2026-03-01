package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.ListeAttente;
import com.recruitx.hrone.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ListeAttenteService {

    public ListeAttenteService() {
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance();
    }

    public void addToWaitlist(ListeAttente la) {
        String req = "INSERT INTO `liste_attente`(`ID_Evenement`, `ID_Activite`, `nom_complet`, `email`) VALUES (?,?,?,?)";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, la.getIdEvenement());
            ps.setInt(2, la.getIdActivite());
            ps.setString(3, la.getNomComplet());
            ps.setString(4, la.getEmail());
            ps.executeUpdate();
            System.out.println("Ajouté à la liste d'attente !");
        } catch (SQLException e) {
            System.err.println("Erreur waitlist add : " + e.getMessage());
        }
    }

    public List<ListeAttente> getWaitlistByEvent(int idEvenement) {
        List<ListeAttente> list = new ArrayList<>();
        String req = "SELECT * FROM `liste_attente` WHERE ID_Evenement = ? ORDER BY date_demande ASC";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new ListeAttente(
                        rs.getInt("ID_Attente"),
                        rs.getInt("ID_Evenement"),
                        rs.getInt("ID_Activite"),
                        rs.getString("nom_complet"),
                        rs.getString("email"),
                        rs.getTimestamp("date_demande")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur waitlist get : " + e.getMessage());
        }
        return list;
    }

    public void removeFromWaitlist(int idAttente) {
        String req = "DELETE FROM `liste_attente` WHERE ID_Attente = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idAttente);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur waitlist delete : " + e.getMessage());
        }
    }

    public ListeAttente getFirstInLine(int idEvenement) {
        String req = "SELECT * FROM `liste_attente` WHERE ID_Evenement = ? ORDER BY date_demande ASC LIMIT 1";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ListeAttente(
                        rs.getInt("ID_Attente"),
                        rs.getInt("ID_Evenement"),
                        rs.getInt("ID_Activite"),
                        rs.getString("nom_complet"),
                        rs.getString("email"),
                        rs.getTimestamp("date_demande"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur waitlist first : " + e.getMessage());
        }
        return null;
    }
}
