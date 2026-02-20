package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.Utilisateur;
import com.recruitx.hrone.utils.DBConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurCRUD implements InterfaceCRUD<Utilisateur> {

    @Override
    public void create(Utilisateur utilisateur) {
        String checkSql = "SELECT 1 FROM utilisateur WHERE Email = ? OR CIN = ?";
        String insertSql = "INSERT INTO utilisateur " +
                "(ID_Entreprise, ID_Profil, Nom_Utilisateur, Mot_Passe, Email, Adresse, Num_Tel, CIN, Num_Ordre_Sign_In, Date_Naissance, Gender, firstLogin) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = DBConnection.getInstance();

            // Check for unique Email and CIN
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, utilisateur.getEmail());
            checkStmt.setString(2, utilisateur.getCin());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                System.out.println("Erreur : Email ou CIN déjà utilisé !");
                return;
            }

            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setInt(1, utilisateur.getIdEntreprise());
            insertStmt.setInt(2, utilisateur.getIdProfil());
            insertStmt.setString(3, utilisateur.getNomUtilisateur());
            insertStmt.setString(4, utilisateur.getMotPasse());
            insertStmt.setString(5, utilisateur.getEmail());
            insertStmt.setString(6, utilisateur.getAdresse());
            insertStmt.setString(7, utilisateur.getNumTel());
            insertStmt.setString(8, utilisateur.getCin());
            insertStmt.setInt(9, utilisateur.getNumOrdreSignIn());
            if (utilisateur.getDateNaissance() != null) {
                insertStmt.setDate(10, new java.sql.Date(utilisateur.getDateNaissance().getTime()));
            } else {
                insertStmt.setNull(10, Types.DATE);
            }
            insertStmt.setString(11, String.valueOf(utilisateur.getGender()));
            insertStmt.setInt(12, utilisateur.getFirstLogin()); // <-- firstLogin added

            insertStmt.executeUpdate();

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                utilisateur.setIdUtilisateur(generatedKeys.getInt(1));
            }

            System.out.println("Utilisateur créé : " + utilisateur);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Utilisateur> getAll() {
        String sql = "SELECT * FROM utilisateur";
        List<Utilisateur> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Utilisateur u = new Utilisateur(
                        rs.getInt("ID_Utilisateur"),
                        rs.getInt("ID_Entreprise"),
                        rs.getInt("ID_Profil"),
                        rs.getString("Nom_Utilisateur"),
                        rs.getString("Mot_Passe"),
                        rs.getString("Email"),
                        rs.getString("Adresse"),
                        rs.getString("Num_Tel"),
                        rs.getString("CIN"),
                        rs.getInt("Num_Ordre_Sign_In"),
                        rs.getDate("Date_Naissance"),
                        rs.getString("Gender").charAt(0),
                        rs.getInt("firstLogin") // <-- firstLogin as int
                );
                list.add(u);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    @Override
    public Utilisateur getById(int id) {
        String sql = "SELECT * FROM utilisateur WHERE ID_Utilisateur = ?";
        Utilisateur u = null;
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                u = new Utilisateur(
                        rs.getInt("ID_Utilisateur"),
                        rs.getInt("ID_Entreprise"),
                        rs.getInt("ID_Profil"),
                        rs.getString("Nom_Utilisateur"),
                        rs.getString("Mot_Passe"),
                        rs.getString("Email"),
                        rs.getString("Adresse"),
                        rs.getString("Num_Tel"),
                        rs.getString("CIN"),
                        rs.getInt("Num_Ordre_Sign_In"),
                        rs.getDate("Date_Naissance"),
                        rs.getString("Gender").charAt(0),
                        rs.getInt("firstLogin") // <-- firstLogin as int
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return u;
    }

    public Utilisateur findByEmail(String email) {
        String sql = "SELECT * FROM utilisateur WHERE Email = ?";
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Utilisateur(
                        rs.getInt("ID_Utilisateur"),
                        rs.getInt("ID_Entreprise"),
                        rs.getInt("ID_Profil"),
                        rs.getString("Nom_Utilisateur"),
                        rs.getString("Mot_Passe"),
                        rs.getString("Email"),
                        rs.getString("Adresse"),
                        rs.getString("Num_Tel"),
                        rs.getString("CIN"),
                        rs.getInt("Num_Ordre_Sign_In"),
                        rs.getDate("Date_Naissance"),
                        rs.getString("Gender").charAt(0),
                        rs.getInt("firstLogin")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Utilisateur login(String email, String password) {

        String sql = "SELECT * FROM utilisateur WHERE Email = ?";

        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String storedHashedPassword = rs.getString("Mot_Passe");

                // 🔐 Compare password with BCrypt
                if (BCrypt.checkpw(password, storedHashedPassword)) {

                    return new Utilisateur(
                            rs.getInt("ID_Utilisateur"),
                            rs.getInt("ID_Entreprise"),
                            rs.getInt("ID_Profil"),
                            rs.getString("Nom_Utilisateur"),
                            rs.getString("Mot_Passe"),
                            rs.getString("Email"),
                            rs.getString("Adresse"),
                            rs.getString("Num_Tel"),
                            rs.getString("CIN"),
                            rs.getInt("Num_Ordre_Sign_In"),
                            rs.getDate("Date_Naissance"),
                            rs.getString("Gender").charAt(0),
                            rs.getInt("firstLogin")
                    );

                } else {
                    return null; // password incorrect
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null; // email not found
    }




    @Override
    public void update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateur SET ID_Entreprise = ?, ID_Profil = ?, Nom_Utilisateur = ?, Mot_Passe = ?, Email = ?, Adresse = ?, Num_Tel = ?, CIN = ?, Num_Ordre_Sign_In = ?, Date_Naissance = ?, Gender = ?, firstLogin = ? WHERE ID_Utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setInt(1, utilisateur.getIdEntreprise());
            stmt.setInt(2, utilisateur.getIdProfil());
            stmt.setString(3, utilisateur.getNomUtilisateur());
            stmt.setString(4, utilisateur.getMotPasse());
            stmt.setString(5, utilisateur.getEmail());
            stmt.setString(6, utilisateur.getAdresse());
            stmt.setString(7, utilisateur.getNumTel());
            stmt.setString(8, utilisateur.getCin());
            stmt.setInt(9, utilisateur.getNumOrdreSignIn());
            if (utilisateur.getDateNaissance() != null) {
                stmt.setDate(10, new java.sql.Date(utilisateur.getDateNaissance().getTime()));
            } else {
                stmt.setNull(10, Types.DATE);
            }
            stmt.setString(11, String.valueOf(utilisateur.getGender()));
            stmt.setInt(12, utilisateur.getFirstLogin()); // <-- firstLogin updated
            stmt.setInt(13, utilisateur.getIdUtilisateur());

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Utilisateur updated: " + utilisateur);
            } else {
                System.out.println("No utilisateur found with ID: " + utilisateur.getIdUtilisateur());
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM utilisateur WHERE ID_Utilisateur = ?";
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Utilisateur deleted. ID: " + id);
            } else {
                System.out.println("No utilisateur found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Vérifie si l'email existe
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM utilisateur WHERE Email = ?";
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true si déjà utilisé
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Vérifie si le CIN existe
    public boolean cinExists(String cin) {
        String sql = "SELECT 1 FROM utilisateur WHERE CIN = ?";
        try {
            Connection conn = DBConnection.getInstance();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
