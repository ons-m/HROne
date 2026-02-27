package com.recruitx.hrone.dao;

import com.recruitx.hrone.utils.COrdre;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationFormationDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance();
    }

    // ✅ Insérer participation + décrémenter places
    public boolean insererParticipation(int idFormation, int idParticipant) {
        String sql = "INSERT INTO participation_formation " +
                "(ID_Formation, ID_Participant, Num_Ordre_Participation, Statut) " +
                "VALUES (?, ?, ?, 'inscrit')";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            ps.setLong(3, COrdre.GetNumOrdreNow());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) decrementerPlaces(idFormation);
            return ok;
        } catch (SQLException e) {
            System.err.println("Erreur insertion: " + e.getMessage());
            return false;
        }
    }

    // ✅ Décrémenter places
    private void decrementerPlaces(int idFormation) {
        String sql = "UPDATE formation SET PlacesRestantes = PlacesRestantes - 1 " +
                "WHERE ID_Formation = ? AND PlacesRestantes > 0";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur décrémentation: " + e.getMessage());
        }
    }

    // ✅ Places restantes
    public int getPlacesRestantes(int idFormation) {
        String sql = "SELECT PlacesRestantes FROM formation WHERE ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("PlacesRestantes");
        } catch (SQLException e) {
            System.err.println("Erreur places: " + e.getMessage());
        }
        return 0;
    }

    // ✅ Déjà inscrit ?
    public boolean estDejaInscrit(int idFormation, int idParticipant) {
        String sql = "SELECT 1 FROM participation_formation " +
                "WHERE ID_Formation = ? AND ID_Participant = ? AND Statut = 'inscrit'";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("Erreur vérification: " + e.getMessage());
        }
        return false;
    }

    // ✅ Chercher par email — colonne Email + ID_UTILISATEUR
    public int getIdParticipantByEmail(String email) {
        String sql = "SELECT ID_UTILISATEUR FROM utilisateur WHERE Email = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("ID_UTILISATEUR");
        } catch (SQLException e) {
            System.err.println("Erreur recherche email: " + e.getMessage());
        }
        return -1;
    }

    // ✅ Marquer comme achevée
    public boolean marquerAcheve(int idFormation, int idParticipant) {
        String sql = "UPDATE participation_formation SET Statut = 'acheve' " +
                "WHERE ID_Formation = ? AND ID_Participant = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur achèvement: " + e.getMessage());
            return false;
        }

    }
    public List<String[]> getParticipantsByFormation(int idFormation) {
        List<String[]> participants = new ArrayList<>();
        String sql = "SELECT pf.ID_Participant, u.Nom_Utilisateur, pf.Statut " +
                "FROM participation_formation pf " +
                "LEFT JOIN utilisateur u ON u.ID_UTILISATEUR = pf.ID_Participant " +
                "WHERE pf.ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                participants.add(new String[]{
                        String.valueOf(rs.getInt("ID_Participant")),
                        rs.getString("Nom_Utilisateur"),
                        rs.getString("Statut")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur participants: " + e.getMessage());
        }
        return participants;
    }
    private String getNomCandidat(int idParticipant) {
        String sql = "SELECT Nom_Utilisateur FROM utilisateur WHERE ID_UTILISATEUR = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idParticipant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("Nom_Utilisateur");
        } catch (SQLException e) {
            System.err.println("Erreur nom candidat: " + e.getMessage());
        }
        return "Candidat";
    }
}