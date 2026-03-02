package com.recruitx.hrone.Repository;

import com.recruitx.hrone.Models.ParticipationEvenement;
import com.recruitx.hrone.Models.ListeAttente;
import com.recruitx.hrone.Utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationEvenementRepository {

    private static final List<ParticipationEvenement> memoryList = new ArrayList<>();

    public ParticipationEvenementRepository() {
        }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance();
    }

    public void add(ParticipationEvenement pe) {
        String req = "INSERT INTO `participation_evenement`(`ID_Evenement`, `ID_Activite`, `ID_Participant`, `Num_Ordre_Participation`, `nom_complet`, `email`, `description`, `mode_paiement`) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, pe.getIdEvenement());
            ps.setInt(2, pe.getIdActivite());
            ps.setInt(3, pe.getIdParticipant());
            ps.setInt(4, pe.getNumOrdreParticipation());
            ps.setString(5, pe.getNomComplet());
            ps.setString(6, pe.getEmail());
            ps.setString(7, pe.getDescription());
            ps.setString(8, pe.getModePaiement());
            ps.executeUpdate();
            System.out.println("Participation Ajoutée !");
        } catch (SQLException e) {
            System.err.println("Database add failed, storing in memory for UI presentation: " + e.getMessage());
            memoryList.add(pe);
        }
    }

    public void delete(int id) {
        throw new UnsupportedOperationException("Utiliser delete(idEvenement, idActivite, idParticipant)");
    }

    public ParticipationEvenement delete(int idEvenement, int idActivite, int idParticipant) {
        String req = "DELETE FROM `participation_evenement` WHERE ID_Evenement=? AND ID_Activite=? AND ID_Participant=?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvenement);
            ps.setInt(2, idActivite);
            ps.setInt(3, idParticipant);
            ps.executeUpdate();
            System.out.println("Participation Supprimée !");
        } catch (SQLException e) {
            System.err.println("Database delete failed: " + e.getMessage());
        }

        ParticipationEvenement promoted = promoteNextInLine(idEvenement);

        memoryList.removeIf(p -> p.getIdEvenement() == idEvenement && p.getIdActivite() == idActivite
                && p.getIdParticipant() == idParticipant);

        return promoted;
    }


    public void update(ParticipationEvenement pe) {
        String req = "UPDATE `participation_evenement` SET `Num_Ordre_Participation`=?, `nom_complet`=?, `email`=?, `description`=?, `mode_paiement`=? WHERE ID_Evenement=? AND ID_Activite=? AND ID_Participant=?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, pe.getNumOrdreParticipation());
            ps.setString(2, pe.getNomComplet());
            ps.setString(3, pe.getEmail());
            ps.setString(4, pe.getDescription());
            ps.setString(5, pe.getModePaiement());
            ps.setInt(6, pe.getIdEvenement());
            ps.setInt(7, pe.getIdActivite());
            ps.setInt(8, pe.getIdParticipant());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database update failed: " + e.getMessage());
        }
    }


    public List<ParticipationEvenement> getAll() {
        List<ParticipationEvenement> list = new ArrayList<>();
        String req = "SELECT * FROM `participation_evenement`";
        try (Connection cnx = getConnection();
            Statement st = cnx.createStatement();
                ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                ParticipationEvenement pe = new ParticipationEvenement(
                        rs.getInt("ID_Evenement"),
                        rs.getInt("ID_Activite"),
                        rs.getInt("ID_Participant"),
                        rs.getInt("Num_Ordre_Participation"),
                        rs.getString("nom_complet"),
                        rs.getString("email"),
                        rs.getString("description"),
                        rs.getString("mode_paiement"));
                list.add(pe);
            }
        } catch (SQLException e) {
            System.err.println("Database getAll failed: " + e.getMessage());
        }
        for (ParticipationEvenement memItem : memoryList) {
            boolean exists = list.stream().anyMatch(dbItem -> dbItem.getIdEvenement() == memItem.getIdEvenement() &&
                    dbItem.getIdActivite() == memItem.getIdActivite() &&
                    dbItem.getIdParticipant() == memItem.getIdParticipant());
            if (!exists)
                list.add(memItem);
        }
        return list;
    }


    public ParticipationEvenement getOne(int id) {
        throw new UnsupportedOperationException("Utiliser getOne(idEvenement, idActivite, idParticipant)");
    }

    public int getCountForEvent(int idEvenement) {
        int count = 0;
        String req = "SELECT COUNT(*) FROM `participation_evenement` WHERE ID_Evenement = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, idEvenement);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                count = rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count DB: " + e.getMessage());
        }
        for (ParticipationEvenement pe : memoryList) {
            if (pe.getIdEvenement() == idEvenement)
                count++;
        }
        return count;
    }

    /**
     * ÉTAPE 1 — Vérifie si un doublon exact existe (même email + événement +
     * activité).
     */
    public boolean existsDuplicate(String email, int idEvenement, int idActivite) {
        String req = "SELECT COUNT(*) FROM `participation_evenement` WHERE email = ? AND ID_Evenement = ? AND ID_Activite = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.setInt(2, idEvenement);
            ps.setInt(3, idActivite);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                return true;
        } catch (SQLException e) {
            System.err.println("Erreur existsDuplicate: " + e.getMessage());
        }
        // Vérifier aussi en mémoire
        for (ParticipationEvenement pe : memoryList) {
            if (pe.getEmail().equalsIgnoreCase(email)
                    && pe.getIdEvenement() == idEvenement
                    && pe.getIdActivite() == idActivite)
                return true;
        }
        return false;
    }

    /**
     * ÉTAPE 2 — Vérifie si l'utilisateur a déjà une participation (et donc a déjà
     * payé)
     * pour cet événement, quelle que soit l'activité.
     */
    public boolean hasAlreadyPaidForEvent(String email, int idEvenement) {
        String req = "SELECT COUNT(*) FROM `participation_evenement` WHERE email = ? AND ID_Evenement = ?";
        try (Connection cnx = getConnection(); PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.setInt(2, idEvenement);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0)
                return true;
        } catch (SQLException e) {
            System.err.println("Erreur hasAlreadyPaidForEvent: " + e.getMessage());
        }
        // Vérifier aussi en mémoire
        for (ParticipationEvenement pe : memoryList) {
            if (pe.getEmail().equalsIgnoreCase(email)
                    && pe.getIdEvenement() == idEvenement)
                return true;
        }
        return false;
    }

    private ParticipationEvenement promoteNextInLine(int idEvenement) {
        ListeAttenteRepository las = new ListeAttenteRepository();
        ListeAttente next = las.getFirstInLine(idEvenement);
        if (next != null) {
            ParticipationEvenement pe = new ParticipationEvenement(
                    next.getIdEvenement(),
                    next.getIdActivite(),
                    (int) (System.currentTimeMillis() % 100000000),
                    (int) (System.currentTimeMillis() / 1000),
                    next.getNomComplet(),
                    next.getEmail(),
                    "Inscrit automatiquement depuis la liste d'attente",
                    "Attente Paiement");
            this.add(pe);
            las.removeFromWaitlist(next.getIdAttente());
            return pe;
        }
        return null;
    }
}
