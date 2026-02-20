package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.Entreprise;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EntrepriseCRUD implements InterfaceCRUD<Entreprise> {

    /**
     * Creates a new entreprise in the database.
     * Inserts the name and reference into the 'entreprise' table.
     */
    @Override
    public void create(Entreprise entreprise) {

        String checkSql = "SELECT COUNT(*) FROM entreprise WHERE Reference = ?";
        String insertSql = "INSERT INTO entreprise (Nom_Entreprise, Reference) VALUES (?, ?)";

        try (Connection con = DBConnection.getInstance()) {

            con.setAutoCommit(false); // 🔥 start transaction

            // 1️⃣ Vérification référence
            try (PreparedStatement checkPs = con.prepareStatement(checkSql)) {

                checkPs.setString(1, entreprise.getReference());
                ResultSet rs = checkPs.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("Référence déjà utilisée !");
                }
            }

            // 2️⃣ Insertion
            try (PreparedStatement insertPs =
                         con.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                insertPs.setString(1, entreprise.getNomEntreprise());
                insertPs.setString(2, entreprise.getReference());
                insertPs.executeUpdate();

                try (ResultSet rs = insertPs.getGeneratedKeys()) {
                    if (rs.next()) {
                        entreprise.setIdEntreprise(rs.getInt(1));
                    }
                }
            }

            con.commit(); // ✅ commit si tout va bien

            System.out.println("Entreprise créée avec succès. ID = " + entreprise.getIdEntreprise());

        } catch (SQLException e) {

            // 🔥 Gestion erreur contrainte UNIQUE SQL
            if ("23000".equals(e.getSQLState())) {
                throw new RuntimeException("Référence déjà existante (contrainte base).");
            }

            throw new RuntimeException("Erreur lors de la création : " + e.getMessage());
        }
    }



    /**
     * Retrieves all entreprises from the database.
     * Returns a list of Entreprise objects.
     */
    @Override
    public List<Entreprise> getAll() {
        String sql = "SELECT * FROM entreprise";
        List<Entreprise> entreprises = new ArrayList<>();
        try {
            Connection connection = DBConnection.getInstance();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Entreprise entreprise = new Entreprise(
                        rs.getString("Nom_Entreprise"),
                        rs.getString("Reference")
                );
                entreprise.setIdEntreprise(rs.getInt("ID_Entreprise")); // ← important
                entreprises.add(entreprise);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return entreprises;
    }


    /**
     * Retrieves a single entreprise by its ID.
     * Returns the Entreprise object if found, otherwise null.
     */
    @Override
    public Entreprise getById(int id) {
        String sql = "SELECT * FROM entreprise WHERE ID_Entreprise = ?";
        Entreprise e = null;
        try {
            Connection connection = DBConnection.getInstance();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                e = new Entreprise(
                        rs.getString("Nom_Entreprise"),
                        rs.getString("Reference")
                );
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return e;
    }

    /**
     * Updates an existing entreprise in the database.
     * Updates the name and reference based on the provided Entreprise object.
     * The entreprise ID is used to locate the row in the database.
     */
    @Override
    public void update(Entreprise entreprise) {
        String sql = "UPDATE entreprise SET Nom_Entreprise = ?, Reference = ? WHERE ID_Entreprise = ?";
        try {
            Connection connection = DBConnection.getInstance();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, entreprise.getNomEntreprise());
            ps.setString(2, entreprise.getReference());
            ps.setInt(3, entreprise.getIdEntreprise()); // Make sure your Entreprise class has getIdEntreprise()
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Entreprise updated successfully!");
            } else {
                System.out.println("No entreprise found with ID: " + entreprise.getIdEntreprise());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deletes an entreprise from the database by ID.
     * Prints a message whether deletion was successful or if the entreprise was not found.
     */
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM entreprise WHERE ID_Entreprise = ?";
        try {
            Connection connection = DBConnection.getInstance();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, id);
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Entreprise deleted successfully!");
            } else {
                System.out.println("No entreprise found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Vérifie si la référence existe
    public boolean referenceExists(String reference) {
        String sql = "SELECT 1 FROM entreprise WHERE Reference = ?";
        try (Connection conn = DBConnection.getInstance();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reference);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true si référence déjà utilisée
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
