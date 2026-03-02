package com.recruitx.hrone.Repository;

import com.recruitx.hrone.Utils.CError;
import com.recruitx.hrone.Utils.DBHelper;
import com.recruitx.hrone.Utils.LogType;

public class CandidatRepository {

    public static boolean Ajouter(int ID_User) {
        try{
            String sql = "INSERT INTO CONDIDAT(ID_UTILISATEUR,CV) VALUES (" + ID_User + ", '')";

            return  DBHelper.ExecuteQuery(sql) == 1;
        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't Insert Candidat ",e );
            return false;
        }
    }

    public static int GetIDCandidatFromIDUser(int ID_User){
        try{
            String sql = "SELECT ID_CONDIDAT FROM CONDIDAT WHERE ID_UTILISATEUR = " + ID_User;

            Object result = DBHelper.ExecuteScalar(sql);
            int intResult = (result != null) ? Integer.parseInt(result.toString()) : -1;
            return intResult;
        }catch(Exception e){
            CError.log(LogType.ERROR,"Coudn't Get Candidat with UserID=" + ID_User,e );
            return -1;
        }
    }
}
