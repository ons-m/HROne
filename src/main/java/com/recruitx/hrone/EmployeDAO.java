package com.recruitx.hrone;

import com.recruitx.hrone.utils.CError;
import com.recruitx.hrone.utils.DBHelper;
import com.recruitx.hrone.utils.LogType;

import java.sql.ResultSet;
import java.util.List;

public class EmployeDAO {

    static public boolean Ajouter(Employe entity) {
        try {
            String sql =
                    "INSERT INTO EMPLOYEE (ID_USER,SOLDE_CONGE, NBR_HEURE_DE_TRAVAIL, MAC_MACHINE) VALUES (" +
                            "0, " +
                            entity.getSolde_Conger() + ", " +
                            entity.getNbr_Heure_De_Travail() + ", '" +
                            entity.getMac_Machine() + "')";

            return DBHelper.ExecuteQuery(sql) == 1;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Ajouter Employe", ex);
            return false;
        }
    }


    static public boolean Modifier(Employe entity) {
        try{
            String sql =
                    "UPDATE EMPLOYEE SET " +
                            "SOLDE_CONGE = " + entity.getSolde_Conger() + ", " +
                            "NBR_HEURE_DE_TRAVAIL = " + entity.getNbr_Heure_De_Travail() + ", " +
                            "MAC_MACHINE = '" + entity.getMac_Machine() + "' " +
                            "WHERE ID_EMPLOYE = " + entity.getID_Employe();

            int result = DBHelper.ExecuteQuery(sql);
            return  result > 0;
        }catch (Exception ex){
            CError.log(LogType.ERROR, "Erreur Modifier Employe", ex);
            return false;
        }
    }


    static public boolean Supprimer(int id) {
        try{
            String sql = "DELETE FROM EMPLOYEE WHERE ID_EMPLOYE = " + id;
            int result = DBHelper.ExecuteQuery(sql);
            return  result >= 0;
        }catch (Exception ex){
            CError.log(LogType.ERROR, "Erreur Supprimer Employe" + id, ex);
            return false;
        }
    }


    static public List AvoirListe() {
        List<Employe> employes = new java.util.ArrayList<>();
        try{
            String sql = "SELECT * FROM EMPLOYEE";
            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if(rs != null){
                while (rs.next()) {
                    int ID_Employe = rs.getInt("ID_EMPLOYE");
                    int ID_User = rs.getInt("ID_USER");
                    int Solde_Conger = rs.getInt("SOLDE_CONGE");
                    int Nbr_Heure_De_Travail = rs.getInt("NBR_HEURE_DE_TRAVAIL");
                    String Mac_Machine = rs.getString("MAC_MACHINE");

                    Employe e = new Employe(ID_Employe,ID_User, Solde_Conger, Nbr_Heure_De_Travail, Mac_Machine);
                    employes.add(e);
                }
                rs.close();
                rs.getStatement().close();
            }else{
                return  null;
            }

            return  employes;
        }catch (Exception ex){
            CError.log(LogType.ERROR, "Erreur AvoirListe Employe", ex);
            return null;
        }
    }


    static public Employe AvoirEntite(int ID_Employe) {
        try{
            String sql = "SELECT * FROM EMPLOYEE WHERE ID_EMPLOYE = " + ID_Employe;
            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            if(rs.next()){
                int ID_User = rs.getInt("ID_USER");
                Employe e = new Employe(ID_Employe,ID_User);
                e.setSolde_Conger(rs.getInt("SOLDE_CONGE"));
                e.setNbr_Heure_De_Travail(rs.getInt("NBR_HEURE_DE_TRAVAIL"));
                e.setMac_Machine(rs.getString("MAC_MACHINE"));
                rs.getStatement().close();

                return e;
            }

        }catch (Exception ex){
            CError.log(LogType.ERROR, "Erreur Avoir Employe " + ID_Employe, ex);
            return null;
        }
        CError.log(LogType.ERROR, "Erreur Avoir Employe " + ID_Employe);
        return null;
    }

}
