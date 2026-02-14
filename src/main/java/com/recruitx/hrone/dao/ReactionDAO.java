package com.recruitx.hrone.dao;

import com.recruitx.hrone.models.Reaction;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReactionDAO {
    private Connection connection;

    public ReactionDAO() {
        this.connection = DBConnection.getConnection();
    }

    // CREATE
    public boolean create(Reaction reaction) {
        String sql = "INSERT INTO reaction (ID_Commentaire, Code_Type_Reaction, ID_UTILISATEUR, Num_Ordre_Reaction) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reaction.getIdCommentaire());
            stmt.setString(2, reaction.getCodeTypeReaction());
            stmt.setInt(3, reaction.getIdUtilisateur());
            stmt.setInt(4, reaction.getNumOrdreReaction());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    reaction.setIdReaction(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création réaction: " + e.getMessage());
        }
        return false;
    }

    // READ ALL
    public List<Reaction> readAll() {
        List<Reaction> reactions = new ArrayList<>();
        String sql = "SELECT * FROM reaction ORDER BY ID_Commentaire, Num_Ordre_Reaction";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Reaction reaction = new Reaction();
                reaction.setIdReaction(rs.getInt("ID_Reaction"));
                reaction.setIdCommentaire(rs.getInt("ID_Commentaire"));
                reaction.setCodeTypeReaction(rs.getString("Code_Type_Reaction"));
                reaction.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                reaction.setNumOrdreReaction(rs.getInt("Num_Ordre_Reaction"));
                reactions.add(reaction);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture réactions: " + e.getMessage());
        }
        return reactions;
    }

    // READ BY COMMENTAIRE
    public List<Reaction> readByCommentaire(int idCommentaire) {
        List<Reaction> reactions = new ArrayList<>();
        String sql = "SELECT * FROM reaction WHERE ID_Commentaire = ? ORDER BY Num_Ordre_Reaction";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommentaire);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reaction reaction = new Reaction();
                reaction.setIdReaction(rs.getInt("ID_Reaction"));
                reaction.setIdCommentaire(rs.getInt("ID_Commentaire"));
                reaction.setCodeTypeReaction(rs.getString("Code_Type_Reaction"));
                reaction.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                reaction.setNumOrdreReaction(rs.getInt("Num_Ordre_Reaction"));
                reactions.add(reaction);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture réactions par commentaire: " + e.getMessage());
        }
        return reactions;
    }

    // READ BY UTILISATEUR
    public List<Reaction> readByUtilisateur(int idUtilisateur) {
        List<Reaction> reactions = new ArrayList<>();
        String sql = "SELECT * FROM reaction WHERE ID_UTILISATEUR = ? ORDER BY ID_Commentaire, Num_Ordre_Reaction";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Reaction reaction = new Reaction();
                reaction.setIdReaction(rs.getInt("ID_Reaction"));
                reaction.setIdCommentaire(rs.getInt("ID_Commentaire"));
                reaction.setCodeTypeReaction(rs.getString("Code_Type_Reaction"));
                reaction.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                reaction.setNumOrdreReaction(rs.getInt("Num_Ordre_Reaction"));
                reactions.add(reaction);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture réactions par utilisateur: " + e.getMessage());
        }
        return reactions;
    }

    // UPDATE
    public boolean update(Reaction reaction) {
        String sql = "UPDATE reaction SET ID_Commentaire = ?, Code_Type_Reaction = ?, ID_UTILISATEUR = ?, Num_Ordre_Reaction = ? WHERE ID_Reaction = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, reaction.getIdCommentaire());
            stmt.setString(2, reaction.getCodeTypeReaction());
            stmt.setInt(3, reaction.getIdUtilisateur());
            stmt.setInt(4, reaction.getNumOrdreReaction());
            stmt.setInt(5, reaction.getIdReaction());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update réaction: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM reaction WHERE ID_Reaction = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete réaction: " + e.getMessage());
        }
        return false;
    }

    // SUPPRIMER PAR COMMENTAIRE
    public boolean deleteByCommentaire(int idCommentaire) {
        String sql = "DELETE FROM reaction WHERE ID_Commentaire = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommentaire);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete réactions par commentaire: " + e.getMessage());
        }
        return false;
    }

    // COMPTER RÉACTIONS PAR COMMENTAIRE
    public int countByCommentaire(int idCommentaire) {
        String sql = "SELECT COUNT(*) FROM reaction WHERE ID_Commentaire = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommentaire);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur comptage réactions: " + e.getMessage());
        }
        return 0;
    }

    // PROCHAIN NUMERO ORDRE POUR COMMENTAIRE
    public int getNextOrderNumber(int idCommentaire) {
        String sql = "SELECT MAX(Num_Ordre_Reaction) FROM reaction WHERE ID_Commentaire = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommentaire);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur prochain numéro réaction: " + e.getMessage());
        }
        return 1;
    }

    // VÉRIFIER SI UTILISATEUR A DÉJÀ RÉAGI AU COMMENTAIRE
    public boolean hasUserReacted(int idCommentaire, int idUtilisateur) {
        String sql = "SELECT COUNT(*) FROM reaction WHERE ID_Commentaire = ? AND ID_UTILISATEUR = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idCommentaire);
            stmt.setInt(2, idUtilisateur);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification réaction utilisateur: " + e.getMessage());
        }
        return false;
    }
}