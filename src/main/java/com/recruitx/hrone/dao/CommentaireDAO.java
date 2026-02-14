package com.recruitx.hrone.dao;

import com.recruitx.hrone.models.Commentaire;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireDAO {
    private Connection connection;

    public CommentaireDAO() {
        this.connection = DBConnection.getConnection();
    }

    // AJOUTER
    public boolean create(Commentaire commentaire) {
        String sql = "INSERT INTO commentaire (ID_UTILISATEUR, ID_Post, Est_Reponse, ID_Parent, Num_Ordre_Commentaire, contenu) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, commentaire.getIdUtilisateur());
            stmt.setInt(2, commentaire.getIdPost());
            stmt.setBoolean(3, commentaire.isEstReponse());

            if (commentaire.getIdParent() != null) {
                stmt.setInt(4, commentaire.getIdParent());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, commentaire.getNumOrdreCommentaire());
            stmt.setString(6, commentaire.getContenu());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    commentaire.setIdCommentaire(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur création commentaire: " + e.getMessage());
        }
        return false;
    }

    // LIRE TOUS
    public List<Commentaire> readAll() {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire ORDER BY ID_Post, Num_Ordre_Commentaire";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setIdCommentaire(rs.getInt("ID_Commentaire"));
                c.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                c.setIdPost(rs.getInt("ID_Post"));
                c.setEstReponse(rs.getBoolean("Est_Reponse"));

                int idParent = rs.getInt("ID_Parent");
                if (!rs.wasNull()) {
                    c.setIdParent(idParent);
                }

                c.setNumOrdreCommentaire(rs.getInt("Num_Ordre_Commentaire"));
                c.setContenu(rs.getString("contenu"));
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture commentaires: " + e.getMessage());
        }
        return commentaires;
    }

    // LIRE PAR POST
    public List<Commentaire> readByPost(int idPost) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE ID_Post = ? ORDER BY Num_Ordre_Commentaire";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPost);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setIdCommentaire(rs.getInt("ID_Commentaire"));
                c.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                c.setIdPost(rs.getInt("ID_Post"));
                c.setEstReponse(rs.getBoolean("Est_Reponse"));

                int idParent = rs.getInt("ID_Parent");
                if (!rs.wasNull()) {
                    c.setIdParent(idParent);
                }

                c.setNumOrdreCommentaire(rs.getInt("Num_Ordre_Commentaire"));
                c.setContenu(rs.getString("contenu"));
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lecture par post: " + e.getMessage());
        }
        return commentaires;
    }

    // MODIFIER
    public boolean update(Commentaire commentaire) {
        String sql = "UPDATE commentaire SET ID_UTILISATEUR = ?, ID_Post = ?, Est_Reponse = ?, ID_Parent = ?, Num_Ordre_Commentaire = ?, contenu = ? WHERE ID_Commentaire = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, commentaire.getIdUtilisateur());
            stmt.setInt(2, commentaire.getIdPost());
            stmt.setBoolean(3, commentaire.isEstReponse());

            if (commentaire.getIdParent() != null) {
                stmt.setInt(4, commentaire.getIdParent());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, commentaire.getNumOrdreCommentaire());
            stmt.setString(6, commentaire.getContenu());
            stmt.setInt(7, commentaire.getIdCommentaire());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur modification commentaire: " + e.getMessage());
        }
        return false;
    }

    // SUPPRIMER
    public boolean delete(int id) {
        String sql = "DELETE FROM commentaire WHERE ID_Commentaire = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Erreur suppression commentaire: " + e.getMessage());
        }
        return false;
    }

    // RECHERCHER
    public List<Commentaire> search(String keyword) {
        List<Commentaire> commentaires = new ArrayList<>();
        String sql = "SELECT * FROM commentaire WHERE contenu LIKE ? ORDER BY Num_Ordre_Commentaire";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Commentaire c = new Commentaire();
                c.setIdCommentaire(rs.getInt("ID_Commentaire"));
                c.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                c.setIdPost(rs.getInt("ID_Post"));
                c.setEstReponse(rs.getBoolean("Est_Reponse"));

                int idParent = rs.getInt("ID_Parent");
                if (!rs.wasNull()) {
                    c.setIdParent(idParent);
                }

                c.setNumOrdreCommentaire(rs.getInt("Num_Ordre_Commentaire"));
                c.setContenu(rs.getString("contenu"));
                commentaires.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Erreur recherche: " + e.getMessage());
        }
        return commentaires;
    }

    // PROCHAIN NUMERO ORDRE
    public int getNextOrderNumber(int idPost) {
        String sql = "SELECT MAX(Num_Ordre_Commentaire) FROM commentaire WHERE ID_Post = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, idPost);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (SQLException e) {
            System.err.println("Erreur prochain numéro: " + e.getMessage());
        }
        return 1;
    }
}