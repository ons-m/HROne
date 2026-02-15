package com.recruitx.hrone.dao;

import com.recruitx.hrone.models.Formation;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormationDAO {
    private Connection connection;



    public FormationDAO() {
        try {
            this.connection = DBConnection.getInstance();  // ✅ UTILISEZ getInstance()
        } catch (SQLException e) {
            System.err.println("❌ Erreur connexion DB: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // CREATE
    public boolean create(Formation formation) {
        String sql = "INSERT INTO formation (Titre, Description, Num_Ordre_Creation, ID_Entreprise, Image) VALUES (?, ?, ?, ?, ?)";
        System.out.println("--------------------------------------------");

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, formation.getTitre());
            stmt.setString(2, formation.getDescription());
            stmt.setInt(3, formation.getNumOrdreCreation());
            stmt.setInt(4, formation.getIdEntreprise());
            stmt.setString(5, formation.getImage());
            System.out.println("--------------------------------------------"+ formation);
            int rows = stmt.executeUpdate();
            System.out.println("--------------------------------------------");

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                System.out.println("--------------------------------------------");

                if (rs.next()) {
                    System.out.println("--------------------------------------------");

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
        String sql = "UPDATE formation SET Titre = ?, Description = ?, Num_Ordre_Creation = ?, ID_Entreprise = ?, Image = ? WHERE ID_Formation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, formation.getTitre());
            stmt.setString(2, formation.getDescription());
            stmt.setInt(3, formation.getNumOrdreCreation());
            stmt.setInt(4, formation.getIdEntreprise());
            stmt.setString(5, formation.getImage());
            stmt.setInt(6, formation.getIdFormation());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update formation: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM formation WHERE ID_Formation = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete formation: " + e.getMessage());
        }
        return false;
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
    private Formation mapResultSetToFormation(ResultSet rs) throws SQLException {
        Formation formation = new Formation();
        formation.setIdFormation(rs.getInt("ID_Formation"));
        formation.setTitre(rs.getString("Titre"));
        formation.setDescription(rs.getString("Description"));
        formation.setNumOrdreCreation(rs.getInt("Num_Ordre_Creation"));
        formation.setIdEntreprise(rs.getInt("ID_Entreprise"));
        formation.setImage(rs.getString("Image"));
        return formation;
    }
}