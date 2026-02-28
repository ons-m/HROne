package com.recruitx.hrone.Repository;

import com.recruitx.hrone.Models.DemandeConge;
import com.recruitx.hrone.Utils.CError;
import com.recruitx.hrone.Utils.COrdre;
import com.recruitx.hrone.Utils.DBHelper;
import com.recruitx.hrone.Utils.LogType;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DemandeCongeRepository {

    public static class DemandeCongeView {
        private final int idDemande;
        private final int idEmploye;
        private final String nomEmploye;
        private final int nbrJours;
        private final String dateDebut;
        private final String dateFin;
        private final int status;

        public DemandeCongeView(int idDemande, int idEmploye, String nomEmploye, int nbrJours, String dateDebut, String dateFin, int status) {
            this.idDemande = idDemande;
            this.idEmploye = idEmploye;
            this.nomEmploye = nomEmploye;
            this.nbrJours = nbrJours;
            this.dateDebut = dateDebut;
            this.dateFin = dateFin;
            this.status = status;
        }

        public int getIdDemande() {
            return idDemande;
        }

        public int getIdEmploye() {
            return idEmploye;
        }

        public String getNomEmploye() {
            return nomEmploye;
        }

        public int getNbrJours() {
            return nbrJours;
        }

        public String getDateDebut() {
            return dateDebut;
        }

        public String getDateFin() {
            return dateFin;
        }

        public int getStatus() {
            return status;
        }
    }

    private DemandeCongeRepository() {
    }

    public static int getEmployeIdByUserId(int userId) {
        try {
            String sql = "SELECT ID_EMPLOYE FROM EMPLOYEE WHERE ID_UTILISATEUR = " + userId + " LIMIT 1";
            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if (rs != null && rs.next()) {
                int id = rs.getInt("ID_EMPLOYE");
                rs.getStatement().close();
                rs.close();
                return id;
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur getEmployeIdByUserId", ex);
        }

        return -1;
    }

    public static boolean ajouter(DemandeConge entity) {
        try {
            ensureOrdreExists(entity.getNumOrdreDebutConge());
            ensureOrdreExists(entity.getNumOrdreFinConge());

            String sql = "INSERT INTO DEMANDE_CONGE (ID_EMPLOYE, NBR_JOUR_DEMANDE, NUM_ORDRE_DEBUT_CONGE, NUM_ORDRE_FIN_CONGE, STATUS) VALUES ("
                    + entity.getIdEmploye() + ", "
                    + entity.getNbrJourDemande() + ", "
                    + entity.getNumOrdreDebutConge() + ", "
                    + entity.getNumOrdreFinConge() + ", "
                    + entity.getStatus() + ")";

            return DBHelper.ExecuteQuery(sql) == 1;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Ajouter Demande Conge", ex);
            return false;
        }
    }

    public static List<DemandeCongeView> avoirListeDemandes() {
        List<DemandeCongeView> list = new ArrayList<>();

        try {
            String sql = """
                    SELECT
                        d.ID_DEMENDE,
                        d.ID_EMPLOYE,
                        COALESCE(u.NOM_UTILISATEUR, CONCAT('Employe #', d.ID_EMPLOYE)) AS NOM_EMPLOYE,
                        d.NBR_JOUR_DEMANDE,
                        CONCAT(LPAD(od.JJ, 2, '0'), '/', LPAD(od.MM, 2, '0'), '/', od.AAAA) AS DATE_DEBUT,
                        CONCAT(LPAD(ofn.JJ, 2, '0'), '/', LPAD(ofn.MM, 2, '0'), '/', ofn.AAAA) AS DATE_FIN,
                        d.STATUS
                    FROM DEMANDE_CONGE d
                    LEFT JOIN EMPLOYEE e ON e.ID_EMPLOYE = d.ID_EMPLOYE
                    LEFT JOIN UTILISATEUR u ON u.ID_UTILISATEUR = e.ID_UTILISATEUR
                    LEFT JOIN ORDRE od ON od.NUM_ORDRE = d.NUM_ORDRE_DEBUT_CONGE
                    LEFT JOIN ORDRE ofn ON ofn.NUM_ORDRE = d.NUM_ORDRE_FIN_CONGE
                    ORDER BY d.ID_DEMENDE DESC
                    """;

            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            if (rs != null) {
                while (rs.next()) {
                    list.add(new DemandeCongeView(
                            rs.getInt("ID_DEMENDE"),
                            rs.getInt("ID_EMPLOYE"),
                            rs.getString("NOM_EMPLOYE"),
                            rs.getInt("NBR_JOUR_DEMANDE"),
                            rs.getString("DATE_DEBUT"),
                            rs.getString("DATE_FIN"),
                            rs.getInt("STATUS")
                    ));
                }
                rs.getStatement().close();
                rs.close();
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur avoirListeDemandes", ex);
        }

        return list;
    }

    public static boolean modifierStatus(int idDemande, int status) {
        try {
            String sql = "UPDATE DEMANDE_CONGE SET STATUS = " + status + " WHERE ID_DEMENDE = " + idDemande;
            return DBHelper.ExecuteQuery(sql) > 0;
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur modifierStatus DemandeConge", ex);
            return false;
        }
    }

    private static void ensureOrdreExists(int numOrdre) {
        try {
            LocalDateTime dt = COrdre.GetDateFromNumOrdre(numOrdre);
            String sql = "INSERT INTO ORDRE (NUM_ORDRE, AAAA, MM, JJ, HH, MN, SS) VALUES ("
                    + numOrdre + ", "
                    + dt.getYear() + ", "
                    + dt.getMonthValue() + ", "
                    + dt.getDayOfMonth() + ", "
                    + dt.getHour() + ", "
                    + dt.getMinute() + ", "
                    + dt.getSecond() + ") ON DUPLICATE KEY UPDATE NUM_ORDRE = NUM_ORDRE";

            DBHelper.ExecuteQuery(sql);
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur ensureOrdreExists", ex);
        }
    }
}
