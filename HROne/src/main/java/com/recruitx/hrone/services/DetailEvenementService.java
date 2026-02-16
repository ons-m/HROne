package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.DetailEvenement;
import com.recruitx.hrone.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetailEvenementService implements IService<DetailEvenement> {

    private Connection cnx;

    public DetailEvenementService(){
        try{
            cnx = DBConnection.getInstance();
        }catch (Exception e){

        }
    }

    @Override
    public void add(DetailEvenement de) {
        String req = "INSERT INTO `detail_evenement`(`ID_Evenement`, `ID_Activite`, `Num_Ordre_Debut_Activite`, `Num_Ordre_Fin_Activite`) VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, de.getIdEvenement());
            ps.setInt(2, de.getIdActivite());
            ps.setInt(3, de.getNumOrdreDebutActivite());
            ps.setInt(4, de.getNumOrdreFinActivite());
            ps.executeUpdate();
            System.out.println("Détail Evénement Ajouté !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Utiliser delete(idEvenement, idActivite, ...)");
    }

    public void delete(int idEvenement, int idActivite, int numDebut, int numFin) {
        String req = "DELETE FROM `detail_evenement` WHERE ID_Evenement=? AND ID_Activite=? AND Num_Ordre_Debut_Activite=? AND Num_Ordre_Fin_Activite=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, idEvenement);
            ps.setInt(2, idActivite);
            ps.setInt(3, numDebut);
            ps.setInt(4, numFin);
            ps.executeUpdate();
            System.out.println("Détail Evénement Supprimé !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void update(DetailEvenement de) {
        throw new UnsupportedOperationException("Impossible de modifier une clé primaire complète.");
    }

    @Override
    public List<DetailEvenement> getAll() {
        List<DetailEvenement> list = new ArrayList<>();
        String req = "SELECT * FROM `detail_evenement`";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                DetailEvenement de = new DetailEvenement(
                        rs.getInt("ID_Evenement"),
                        rs.getInt("ID_Activite"),
                        rs.getInt("Num_Ordre_Debut_Activite"),
                        rs.getInt("Num_Ordre_Fin_Activite"));
                list.add(de);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public DetailEvenement getOne(int id) {
        throw new UnsupportedOperationException("Utiliser getOne(idEvenement, idActivite, ...)");
    }
}
