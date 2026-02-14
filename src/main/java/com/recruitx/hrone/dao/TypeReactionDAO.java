package com.recruitx.hrone.dao;

import com.recruitx.hrone.models.TypeReaction;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeReactionDAO {
    private Connection connection;

    public TypeReactionDAO() {
        this.connection = DBConnection.getConnection();
    }

    // CREATE
    public boolean create(TypeReaction type) {
        String sql = "INSERT INTO type_reaction (Code_Type_Reaction, Description_Reaction) VALUES (?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type.getCodeTypeReaction());
            stmt.setString(2, type.getDescriptionReaction());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur création type réaction: " + e.getMessage());
        }
        return false;
    }

    // READ ALL
    public List<TypeReaction> readAll() {
        List<TypeReaction> types = new ArrayList<>();
        String sql = "SELECT * FROM type_reaction ORDER BY Code_Type_Reaction";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TypeReaction type = new TypeReaction();
                type.setCodeTypeReaction(rs.getString("Code_Type_Reaction"));
                type.setDescriptionReaction(rs.getString("Description_Reaction"));
                types.add(type);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture types réaction: " + e.getMessage());
        }
        return types;
    }

    // READ BY CODE
    public TypeReaction read(String code) {
        String sql = "SELECT * FROM type_reaction WHERE Code_Type_Reaction = ?";
        TypeReaction type = null;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                type = new TypeReaction();
                type.setCodeTypeReaction(rs.getString("Code_Type_Reaction"));
                type.setDescriptionReaction(rs.getString("Description_Reaction"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lecture type réaction: " + e.getMessage());
        }
        return type;
    }

    // UPDATE
    public boolean update(TypeReaction type) {
        String sql = "UPDATE type_reaction SET Description_Reaction = ? WHERE Code_Type_Reaction = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, type.getDescriptionReaction());
            stmt.setString(2, type.getCodeTypeReaction());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur update type réaction: " + e.getMessage());
        }
        return false;
    }

    // DELETE
    public boolean delete(String code) {
        String sql = "DELETE FROM type_reaction WHERE Code_Type_Reaction = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("❌ Erreur delete type réaction: " + e.getMessage());
        }
        return false;
    }

    // VÉRIFIER SI CODE EXISTE
    public boolean exists(String code) {
        String sql = "SELECT COUNT(*) FROM type_reaction WHERE Code_Type_Reaction = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur vérification type réaction: " + e.getMessage());
        }
        return false;
    }
}