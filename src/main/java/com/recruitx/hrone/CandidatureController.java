package com.recruitx.hrone;

import com.recruitx.hrone.utils.CError;
import com.recruitx.hrone.utils.DBHelper;
import com.recruitx.hrone.utils.LogType;

import java.sql.ResultSet;
import java.util.List;

public class CandidatureController {

    static public boolean Ajouter(Candidature entity) {
        try {
            String sql =
                    "INSERT INTO CONDIDATURE (" +
                            "ID_CONDIDAT, ID_OFFRE, LETTRE_MOTIVATION, PORTFOLIO, LETTRE_RECOMENDATION, CODE_TYPE_STATUS" +
                            ") VALUES (" +
                            entity.getID_Candidat() + ", " +
                            entity.getID_Offre() + ", '" +
                            entity.getLettre_Motivation() + "', '" +
                            entity.getPortfolio() + "', '" +
                            entity.getLettre_Recomendation() + "', '" +
                            entity.getCode_Type_Status() + "')";

            return DBHelper.ExecuteQuery(sql) == 1;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Ajouter Candidature", ex);
            return false;
        }
    }

    static public boolean Modifier(Candidature entity) {
        try {
            String sql =
                    "UPDATE CONDIDATURE SET " +
                            "ID_CONDIDAT = " + entity.getID_Candidat() + ", " +
                            "LETTRE_MOTIVATION = '" + entity.getLettre_Motivation() + "', " +
                            "PORTFOLIO = '" + entity.getPortfolio() + "', " +
                            "LETTRE_RECOMENDATION = '" + entity.getLettre_Recomendation() + "', " +
                            "CODE_TYPE_STATUS = '" + entity.getCode_Type_Status() + "' " +
                            "WHERE ID_CONDIDATURE = " + entity.getID_Candidature();

            return DBHelper.ExecuteQuery(sql) > 0;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Modifier Candidature", ex);
            return false;
        }
    }

    static public boolean Supprimer(int ID_Condidature) {
        try {
            String sql =
                    "DELETE FROM CONDIDATURE WHERE ID_CONDIDATURE = " + ID_Condidature;

            return DBHelper.ExecuteQuery(sql) >= 0;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Supprimer Candidature " + ID_Condidature, ex);
            return false;
        }
    }


    static public List AvoirListe() {
        List<Candidature> list = new java.util.ArrayList<>();

        try {
            String sql = "SELECT * FROM CONDIDATURE";
            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if (rs != null) {
                while (rs.next()) {

                    Candidature c = new Candidature(
                            rs.getInt("ID_CONDIDATURE"),
                            rs.getInt("ID_CONDIDAT"),
                            rs.getInt("ID_OFFRE"),
                            rs.getString("LETTRE_MOTIVATION"),
                            rs.getString("PORTFOLIO"),
                            rs.getString("LETTRE_RECOMENDATION"),
                            rs.getInt("CODE_TYPE_STATUS")
                    );

                    list.add(c);
                }
                rs.getStatement().close();
                rs.close();

                return list;
            }

            return null;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur AvoirListe Candidature", ex);
            return null;
        }
    }

    static public Candidature AvoirEntite(int ID_Condidature) {
        try {
            String sql =
                    "SELECT * FROM CONDIDATURE WHERE ID_CONDIDATURE = " + ID_Condidature;

            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if (rs.next()) {

                Candidature c = new Candidature(
                        ID_Condidature,
                        rs.getInt("ID_CONDIDAT"),
                        rs.getInt("ID_OFFRE")
                );

                c.setLettre_Motivation(rs.getString("LETTRE_MOTIVATION"));
                c.setPortfolio(rs.getString("PORTFOLIO"));
                c.setLettre_Recomendation(rs.getString("LETTRE_RECOMENDATION"));
                c.setCode_Type_Status(rs.getInt("CODE_TYPE_STATUS"));

                rs.getStatement().close();
                rs.close();
                return c;
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Avoir Candidature " + ID_Condidature, ex);
            return null;
        }

        CError.log(LogType.ERROR, "Erreur Avoir Candidature " + ID_Condidature);
        return null;
    }
}
