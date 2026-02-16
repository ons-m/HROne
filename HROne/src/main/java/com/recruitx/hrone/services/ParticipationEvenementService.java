package com.recruitx.hrone.services;

import com.recruitx.hrone.entities.ParticipationEvenement;
import com.recruitx.hrone.utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationEvenementService implements IService<ParticipationEvenement> {

    private Connection cnx;

    public ParticipationEvenementService() {
        try{
            cnx = DBConnection.getInstance();
        }catch(Exception e){

        }

    }

    @Override
    public void add(ParticipationEvenement pe) {
        String req = "INSERT INTO `participation_evenement`(`ID_Evenement`, `ID_Activite`, `ID_Participant`, `Num_Ordre_Participation`) VALUES (?,?,?,?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, pe.getIdEvenement());
            ps.setInt(2, pe.getIdActivite());
            ps.setInt(3, pe.getIdParticipant());
            ps.setInt(4, pe.getNumOrdreParticipation());
            ps.executeUpdate();
            System.out.println("Participation Ajoutée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        throw new UnsupportedOperationException("Utiliser delete(idEvenement, idActivite, idParticipant)");
    }

    public void delete(int idEvenement, int idActivite, int idParticipant) {
        String req = "DELETE FROM `participation_evenement` WHERE ID_Evenement=? AND ID_Activite=? AND ID_Participant=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, idEvenement);
            ps.setInt(2, idActivite);
            ps.setInt(3, idParticipant);
            ps.executeUpdate();
            System.out.println("Participation Supprimée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void update(ParticipationEvenement pe) {
        String req = "UPDATE `participation_evenement` SET `Num_Ordre_Participation`=? WHERE ID_Evenement=? AND ID_Activite=? AND ID_Participant=?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, pe.getNumOrdreParticipation());
            ps.setInt(2, pe.getIdEvenement());
            ps.setInt(3, pe.getIdActivite());
            ps.setInt(4, pe.getIdParticipant());
            ps.executeUpdate();
            System.out.println("Participation Modifiée !");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public List<ParticipationEvenement> getAll() {
        List<ParticipationEvenement> list = new ArrayList<>();
        String req = "SELECT * FROM `participation_evenement`";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                ParticipationEvenement pe = new ParticipationEvenement(
                        rs.getInt("ID_Evenement"),
                        rs.getInt("ID_Activite"),
                        rs.getInt("ID_Participant"),
                        rs.getInt("Num_Ordre_Participation"));
                list.add(pe);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return list;
    }

    @Override
    public ParticipationEvenement getOne(int id) {
        throw new UnsupportedOperationException("Utiliser getOne(idEvenement, idActivite, idParticipant)");
    }
}
