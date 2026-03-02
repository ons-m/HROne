package com.recruitx.hrone.Repository;

import com.recruitx.hrone.Models.Entretien;
import com.recruitx.hrone.Utils.CError;
import com.recruitx.hrone.Utils.DBHelper;
import com.recruitx.hrone.Utils.LogType;

public class EntretienRepository {

    public static boolean Ajouter(Entretien entity) {
        try {
            String localisation = safe(entity.getLocalisation());
            String evaluation = safe(entity.getEvaluation());

            String sql =
                    "INSERT INTO ENTRETIEN (ID_CONDIDAT, ID_RH, NUM_ORDRE_ENTRETIEN, LOCALISATION, STATUS_ENTRETIEN, EVALUATION) VALUES (" +
                            entity.getIdCandidat() + ", " +
                            entity.getIdRh() + ", " +
                            entity.getNumOrdreEntretien() + ", '" +
                            localisation + "', " +
                            entity.getStatusEntretien() + ", '" +
                            evaluation + "')";

            return DBHelper.ExecuteQuery(sql) == 1;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Ajouter Entretien", ex);
            return false;
        }
    }

    private static String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''").trim();
    }
}
