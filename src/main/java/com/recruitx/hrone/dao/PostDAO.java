package com.recruitx.hrone.dao;

import com.recruitx.hrone.models.Post;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {
    private Connection connection;

    // CONSTRUCTEUR CORRIGÉ
    public PostDAO() {
        this.connection = DBConnection.getConnection();  // ✅ Pas de try-catch inutile
    }

    // CREATE
    public boolean create(Post post) {
        String sql = "INSERT INTO post (ID_UTILISATEUR, Titre, Description, Image, Num_Ordre_Post) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, post.getIdUtilisateur());
            stmt.setString(2, post.getTitre());
            stmt.setString(3, post.getDescription());
            stmt.setString(4, post.getImage());
            stmt.setInt(5, post.getNumOrdrePost());

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    post.setIdPost(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur création post: " + e.getMessage());
        }
        return false;
    }

    // READ ALL
    public List<Post> readAll() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post ORDER BY Num_Ordre_Post";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Post post = new Post();
                post.setIdPost(rs.getInt("ID_Post"));
                post.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                post.setTitre(rs.getString("Titre"));
                post.setDescription(rs.getString("Description"));
                post.setImage(rs.getString("Image"));
                post.setNumOrdrePost(rs.getInt("Num_Ordre_Post"));
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture posts: " + e.getMessage());
        }
        return posts;
    }

    // UPDATE
    public boolean update(Post post) {
        String sql = "UPDATE post SET Titre = ?, Description = ?, Image = ?, Num_Ordre_Post = ?, ID_UTILISATEUR = ? WHERE ID_Post = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, post.getTitre());
            stmt.setString(2, post.getDescription());
            stmt.setString(3, post.getImage());
            stmt.setInt(4, post.getNumOrdrePost());
            stmt.setInt(5, post.getIdUtilisateur());
            stmt.setInt(6, post.getIdPost());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update post: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean delete(int id) {
        String sql = "DELETE FROM post WHERE ID_Post = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete post: " + e.getMessage());
        }
        return false;
    }

    // RECHERCHE
    public List<Post> search(String keyword) {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM post WHERE Titre LIKE ? OR Description LIKE ? ORDER BY Num_Ordre_Post";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Post post = new Post();
                post.setIdPost(rs.getInt("ID_Post"));
                post.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                post.setTitre(rs.getString("Titre"));
                post.setDescription(rs.getString("Description"));
                post.setImage(rs.getString("Image"));
                post.setNumOrdrePost(rs.getInt("Num_Ordre_Post"));
                posts.add(post);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur recherche: " + e.getMessage());
        }
        return posts;
    }
}