package com.recruitx.hrone.Repository;

import com.recruitx.hrone.Models.Formation;
import com.recruitx.hrone.Utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationRepository {
    private Connection connection;



    public FormationRepository() {
        try {
            this.connection = DBConnection.getInstance();  // ✅ UTILISEZ getInstance()
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // CREATE
    public boolean create(Formation formation) {
        String sql = "INSERT INTO formation " +
                "(Titre, Description, Num_Ordre_Creation, ID_Entreprise, Image, " +
                "Mode, NombrePlaces, PlacesRestantes, Date_Debut, Date_Fin, Niveau) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, formation.getTitre());
            stmt.setString(2, formation.getDescription());
            stmt.setInt(3, formation.getNumOrdreCreation());
            stmt.setInt(4, formation.getIdEntreprise());
            stmt.setString(5, formation.getImage());
            stmt.setString(6, formation.getMode() != null ? formation.getMode() : "presentiel"); // ✅
            stmt.setInt(7, formation.getNombrePlaces());     // ✅
            stmt.setInt(8, formation.getPlacesRestantes());  // ✅
            stmt.setLong(9, formation.getDateDebut());       // ✅
            stmt.setLong(10, formation.getDateFin());
            stmt.setString(11, formation.getNiveau() != null ? formation.getNiveau() : "Débutant");


            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    formation.setIdFormation(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création formation: " + e.getMessage());
        }
        return false;
    }
    // READ ALL
    public List<Formation> readAll() {
        List<Formation> formations = new ArrayList<>();
        String sql = "SELECT * FROM formation ORDER BY Num_Ordre_Creation, Titre";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                formations.add(mapResultSetToFormation(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture formations: " + e.getMessage());
        }
        return formations;
    }

    // READ BY ID
    public Formation read(int id) {
        String sql = "SELECT * FROM formation WHERE ID_Formation = ?";
        Formation formation = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                formation = mapResultSetToFormation(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture formation: " + e.getMessage());
        }
        return formation;
    }

    // READ BY ENTREPRISE
    public List<Formation> readByEntreprise(int idEntreprise) {
        List<Formation> formations = new ArrayList<>();
        String sql = "SELECT * FROM formation WHERE ID_Entreprise = ? ORDER BY Num_Ordre_Creation";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEntreprise);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                formations.add(mapResultSetToFormation(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture formations par entreprise: " + e.getMessage());
        }
        return formations;
    }

    // UPDATE
    public boolean update(Formation formation) {
        String sql = "UPDATE formation SET " +
                "Titre = ?, Description = ?, Num_Ordre_Creation = ?, " +
                "ID_Entreprise = ?, Image = ?, " +
                "Mode = ?, NombrePlaces = ?, PlacesRestantes = ?, " +
                "Date_Debut = ?, Date_Fin = ?, Niveau = ? " +
                "WHERE ID_Formation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, formation.getTitre());
            stmt.setString(2, formation.getDescription());
            stmt.setInt(3, formation.getNumOrdreCreation());
            stmt.setInt(4, formation.getIdEntreprise());
            stmt.setString(5, formation.getImage());
            stmt.setString(6, formation.getMode() != null ? formation.getMode() : "presentiel"); // ✅
            stmt.setInt(7, formation.getNombrePlaces());     // ✅
            stmt.setInt(8, formation.getPlacesRestantes());  // ✅
            stmt.setLong(9, formation.getDateDebut());       // ✅
            stmt.setLong(10, formation.getDateFin());        // ✅
            stmt.setInt(11, formation.getIdFormation());
            stmt.setString(11, formation.getNiveau() != null ? formation.getNiveau() : "Débutant");
            stmt.setInt(12, formation.getIdFormation());


            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update formation: " + e.getMessage());
        }
        return false;
    }
    // DELETE
// ✅ DELETE avec suppression cascade des participations
    public boolean delete(int id) {
        String sqlParticipations = "DELETE FROM participation_formation WHERE ID_Formation = ?";
        String sqlFormation = "DELETE FROM formation WHERE ID_Formation = ?";

        try {
            // ✅ Étape 1 — Supprimer participations liées
            PreparedStatement ps1 = connection.prepareStatement(sqlParticipations);
            ps1.setInt(1, id);
            int nbParticipations = ps1.executeUpdate();
            System.out.println("✅ " + nbParticipations + " participation(s) supprimée(s)");

            // ✅ Étape 2 — Supprimer la formation
            PreparedStatement ps2 = connection.prepareStatement(sqlFormation);
            ps2.setInt(1, id);
            boolean ok = ps2.executeUpdate() > 0;

            if (ok) System.out.println("✅ Formation " + id + " supprimée");
            return ok;

        } catch (SQLException e) {
            System.err.println("❌ Erreur suppression: " + e.getMessage());
            return false;
        }
    }

    // ✅ Compter participants d'une formation
    public int getNombreParticipants(int idFormation) {
        String sql = "SELECT COUNT(*) FROM participation_formation WHERE ID_Formation = ?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage: " + e.getMessage());
        }
        return 0;
    }
    // RECHERCHE
    public List<Formation> search(String keyword) {
        List<Formation> formations = new ArrayList<>();
        String sql = "SELECT * FROM formation WHERE Titre LIKE ? OR Description LIKE ? ORDER BY Num_Ordre_Creation";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                formations.add(mapResultSetToFormation(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche formations: " + e.getMessage());
        }
        return formations;
    }

    // PROCHAIN NUMERO ORDRE
    public int getNextOrderNumber() {
        String sql = "SELECT MAX(Num_Ordre_Creation) FROM formation";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println(rs.getInt(1));
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur prochain numéro ordre: " + e.getMessage());
        }
        return 1;
    }

    // COMPTER FORMATIONS PAR ENTREPRISE
    public int countByEntreprise(int idEntreprise) {
        String sql = "SELECT COUNT(*) FROM formation WHERE ID_Entreprise = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idEntreprise);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage formations: " + e.getMessage());
        }
        return 0;
    }

    // Méthode utilitaire pour mapper ResultSet → Formation
    // Mettre à jour mapResultSetToFormation
    private Formation mapResultSetToFormation(ResultSet rs) throws SQLException {
        Formation formation = new Formation();
        formation.setIdFormation(rs.getInt("ID_Formation"));
        formation.setTitre(rs.getString("Titre"));
        formation.setDescription(rs.getString("Description"));
        formation.setNumOrdreCreation(rs.getInt("Num_Ordre_Creation"));
        formation.setIdEntreprise(rs.getInt("ID_Entreprise"));
        formation.setImage(rs.getString("Image"));
        formation.setNiveau(rs.getString("Niveau"));
        // Nouveaux champs
        try {
            formation.setMode(rs.getString("Mode"));
            formation.setNombrePlaces(rs.getInt("NombrePlaces"));
            formation.setPlacesRestantes(rs.getInt("PlacesRestantes"));
            formation.setDateDebut(rs.getLong("Date_Debut"));
            formation.setDateFin(rs.getLong("Date_Fin"));
        } catch (SQLException e) {
            // Colonnes pas encore ajoutées
        }
        return formation;
    }

    // Ajouter cette méthode pour filtrer par mode
    public List<Formation> readByMode(String mode) {
        List<Formation> formations = new ArrayList<>();
        String sql = "SELECT * FROM formation WHERE Mode = ? ORDER BY Num_Ordre_Creation";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mode);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                formations.add(mapResultSetToFormation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture par mode: " + e.getMessage());
        }
        return formations;
    }


    public String getReferenceById(int idEntreprise) {
        String sql = "SELECT Reference FROM Entreprise WHERE ID_Entreprise = ?";
        String reference = null;

        try (
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idEntreprise);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                reference = rs.getString("Reference");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la référence: " + e.getMessage());
            e.printStackTrace();
        }

        return reference;
    }

    public String getNameEntrepriseById(int idEntreprise) {
        String sql = "SELECT Nom_Entreprise FROM Entreprise WHERE ID_Entreprise = ?";
        String nomEntreprise = null;

        try (
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, idEntreprise);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nomEntreprise = rs.getString("Nom_Entreprise");
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'entreprise: " + e.getMessage());
            e.printStackTrace();
        }

        return nomEntreprise;
    }

}