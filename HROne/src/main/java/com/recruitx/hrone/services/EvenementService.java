package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.Evenement;
import com.recruitx.hrone.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvenementService implements IService<Evenement> {

    private Connection cnx;

    public EvenementService(){
        try{
            cnx = DBConnection.getInstance();
        }catch(Exception e){

        }
    }

    @Override
    public void add(Evenement p) {
        if (p.getTitre() == null || p.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide.");
        }
        if (p.getNumOrdreCreation() <= 0 || p.getNumOrdreDebutEvenement() <= 0 || p.getNumOrdreFinEvenement() <= 0) {
            throw new IllegalArgumentException("Les numéros d'ordre doivent être positifs et valides.");
        }

        String req = "INSERT INTO `evenement`(`Titre`, `Description`, `Num_Ordre_Creation`, `Num_Ordre_Debut_Evenement`, `Num_Ordre_Fin_Evenement`, `Localisation`, `Image`) VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());
            ps.setInt(3, p.getNumOrdreCreation());
            ps.setInt(4, p.getNumOrdreDebutEvenement());
            ps.setInt(5, p.getNumOrdreFinEvenement());
            ps.setString(6, p.getLocalisation());
            ps.setString(7, p.getImage());
            ps.executeUpdate();
            System.out.println("Evenement Ajouté !");
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de l'ajout : " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String req = "DELETE FROM `evenement` WHERE ID_Evenement = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Evenement Supprimé !");
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public void update(Evenement p) {
        if (p.getTitre() == null || p.getTitre().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre ne peut pas être vide.");
        }

        String req = "UPDATE `evenement` SET `Titre`=?,`Description`=?,`Num_Ordre_Creation`=?,`Num_Ordre_Debut_Evenement`=?,`Num_Ordre_Fin_Evenement`=?,`Localisation`=?,`Image`=? WHERE ID_Evenement = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, p.getTitre());
            ps.setString(2, p.getDescription());
            ps.setInt(3, p.getNumOrdreCreation());
            ps.setInt(4, p.getNumOrdreDebutEvenement());
            ps.setInt(5, p.getNumOrdreFinEvenement());
            ps.setString(6, p.getLocalisation());
            ps.setString(7, p.getImage());
            ps.setInt(8, p.getIdEvenement());
            ps.executeUpdate();
            System.out.println("Evenement Modifié !");
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public List<Evenement> getAll() {
        List<Evenement> list = new ArrayList<>();
        String req = "SELECT * FROM `evenement`";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Evenement p = new Evenement(
                        rs.getInt("ID_Evenement"),
                        rs.getString("Titre"),
                        rs.getString("Description"),
                        rs.getInt("Num_Ordre_Creation"),
                        rs.getInt("Num_Ordre_Debut_Evenement"),
                        rs.getInt("Num_Ordre_Fin_Evenement"),
                        rs.getString("Localisation"),
                        rs.getString("Image"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération : " + e.getMessage());
        }
        return list;
    }

    @Override
    public Evenement getOne(int id) {
        Evenement p = null;
        String req = "SELECT * FROM `evenement` WHERE ID_Evenement = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                p = new Evenement(
                        rs.getInt("ID_Evenement"),
                        rs.getString("Titre"),
                        rs.getString("Description"),
                        rs.getInt("Num_Ordre_Creation"),
                        rs.getInt("Num_Ordre_Debut_Evenement"),
                        rs.getInt("Num_Ordre_Fin_Evenement"),
                        rs.getString("Localisation"),
                        rs.getString("Image"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL lors de la récupération par ID : " + e.getMessage());
        }
        return p;
    }
}
