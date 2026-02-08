package com.recruitx.hrone;

import com.recruitx.hrone.utils.CError;
import com.recruitx.hrone.utils.DBHelper;
import com.recruitx.hrone.utils.LogType;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OutilDAO {

    public static boolean Ajouter(Outil outil){
        try{
            String sql = "INSERT INTO outils_de_travail(Identifiant_Universelle,Hash_App,Nom_Outil) " +
                    "VALUES ('" + outil.getIdentifiant_Universelle() +
                    "','" + outil.getHash_App() +
                    "','" + outil.getNom_Outil() + "')";

            return  DBHelper.ExecuteQuery(sql) == 1;
        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't Insert tool ",e );
            return false;
        }
    }

    public static boolean Modifier(Outil outil){
        try{
            String sql = "UPDATE outils_de_travail set " +
                    "Identifiant_Universelle = '" + outil.getIdentifiant_Universelle() + "'" +
                    ", Hash_App = '" + outil.getHash_App() + "'" +
                    ", Nom_Outil = '" + outil.getNom_Outil() + "'" +
                    "WHERE ID_OUTIL = " + outil.getID_Outil();

            return DBHelper.ExecuteQuery(sql) > 0;
        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't Update tool with ID=" + outil.getID_Outil(),e );
            return false;
        }
    }

    public static boolean Supprimer(int id){
        try{
            String sql = "DELETE FROM outils_de_travail WHERE ID_OUTIL = " + id;

            return DBHelper.ExecuteQuery(sql) > 0;
        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't Delete tool with ID=" + id,e );
            return false;
        }
    }

    public static List<Outil> AvoirListe(){
        try{
            List<Outil> outils = new ArrayList<Outil>();
            String sql = "SELECT * FROM outils_de_travail";

            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            if(rs != null){
                while(rs.next()){
                    int ID = rs.getInt("ID_OUTIL");
                    Outil outil = new Outil(ID);
                    outil.setIdentifiant_Universelle(rs.getString("Identifiant_Universelle"));
                    outil.setHash_App(rs.getString("Hash_App"));
                    outil.setNom_Outil(rs.getString("Nom_Outil"));
                    outils.add(outil);
                }
                rs.getStatement().close();
                rs.close();
            }else{
                return  null;
            }

            return outils;
        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't select list of tools ",e );
            return null;
        }
    }

    public static Outil AvoirEntite(int id){
        try{
            String sql = "SELECT * FROM outils_de_travail WHERE ID_OUTIL = " + id;

            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if(rs != null){
                rs.next();

                Outil outil = new Outil(id);
                outil.setIdentifiant_Universelle(rs.getString("Identifiant_Universelle"));
                outil.setHash_App(rs.getString("Hash_App"));
                outil.setNom_Outil(rs.getString("Nom_Outil"));

                rs.getStatement().close();
                rs.close();

                return outil;
            }else{
                return  null;
            }

        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't select tool with ID="+ id,e );
            return null;
        }
    }

}
