package com.recruitx.hrone;

import com.recruitx.hrone.utils.CError;
import com.recruitx.hrone.utils.DBHelper;
import com.recruitx.hrone.utils.LogType;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OfferController {

    static public boolean Ajouter(Offer entity) {
        try {
            String sql =
                    "INSERT INTO OFFRE (" +
                            "TITRE, DESCRIPTION, ID_ENTREPRISE, WORK_TYPE, CODE_TYPE_CONTRAT, " +
                            "NBR_ANNEE_EXPERIENCE, CODE_TYPE_NIVEAU_ETUDE, MIN_SALAIRE, MAX_SALAIRE, " +
                            "NUM_ORDRE_CREATION, NUM_ORDRE_EXPIRATION) VALUES (" +
                            "'" + entity.getTitre() + "', '" +
                            entity.getDescription() + "', " +
                            entity.getID_Entreprise() + ", '" +
                            entity.getWork_Type() + "', '" +
                            entity.getCode_Type_Contrat() + "', " +
                            entity.getNbr_Annee_Experience() + ", '" +
                            entity.getCode_Type_Niveau_Etude() + "', " +
                            entity.getMin_Salaire() + ", " +
                            entity.getMax_Salaire() + ", " +
                            entity.getNum_Ordre_Creation() + ", " +
                            entity.getNum_Ordre_Expiration() + ")";

            if (DBHelper.ExecuteQuery(sql) != 1)
                return false;

            // 🔹 get generated ID (reference-style assumption)
            ResultSet rs = DBHelper.ExecuteDataReader("SELECT MAX(ID_OFFRE) AS ID FROM OFFRE");
            if (rs.next()) {
                entity.setID_Offre(rs.getInt("ID"));
            }
            rs.getStatement().close();
            rs.close();


            InsererDetails(entity);
            return true;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Ajouter Offre", ex);
            return false;
        }
    }


    static public boolean Modifier(Offer entity) {
        try {
            String sql =
                    "UPDATE OFFRE SET " +
                            "TITRE = '" + entity.getTitre() + "', " +
                            "DESCRIPTION = '" + entity.getDescription() + "', " +
                            "ID_ENTREPRISE = " + entity.getID_Entreprise() + ", " +
                            "WORK_TYPE = '" + entity.getWork_Type() + "', " +
                            "CODE_TYPE_CONTRAT = '" + entity.getCode_Type_Contrat() + "', " +
                            "NBR_ANNEE_EXPERIENCE = " + entity.getNbr_Annee_Experience() + ", " +
                            "CODE_TYPE_NIVEAU_ETUDE = '" + entity.getCode_Type_Niveau_Etude() + "', " +
                            "MIN_SALAIRE = " + entity.getMin_Salaire() + ", " +
                            "MAX_SALAIRE = " + entity.getMax_Salaire() + ", " +
                            "NUM_ORDRE_CREATION = " + entity.getNum_Ordre_Creation() + ", " +
                            "NUM_ORDRE_EXPIRATION = " + entity.getNum_Ordre_Expiration() + " " +
                            "WHERE ID_OFFRE = " + entity.getID_Offre();

            if (DBHelper.ExecuteQuery(sql) <= 0)
                return false;

            SupprimerDetails(entity.getID_Offre());
            InsererDetails(entity);

            return true;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Modifier Offre", ex);
            return false;
        }
    }

    static public boolean Supprimer(int ID_Offre) {
        try {
            SupprimerDetails(ID_Offre);

            String sql = "DELETE FROM OFFRE WHERE ID_OFFRE = " + ID_Offre;
            return DBHelper.ExecuteQuery(sql) >= 0;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Supprimer Offre " + ID_Offre, ex);
            return false;
        }
    }

    static public List AvoirListe() {
        List<Offer> offres = new java.util.ArrayList<>();

        try {
            String sql = "SELECT * FROM OFFRE";
            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if (rs != null) {
                while (rs.next()) {

                    Offer o = ConstruireOffreDepuisRS(rs);

                    // Load detail lists
                    o.setCodes_Competences(ChargerCompetences(o.getID_Offre()));
                    o.setCodes_Langues(ChargerLangues(o.getID_Offre()));
                    o.setCodes_Backgrounds(ChargerBackgrounds(o.getID_Offre()));

                    offres.add(o);
                }
                rs.getStatement().close();
                rs.close();
            } else {
                return null;
            }

            return offres;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur AvoirListe Offre", ex);
            return null;
        }
    }

    static public Offer AvoirEntite(int ID_Offre) {
        try {
            String sql = "SELECT * FROM OFFRE WHERE ID_OFFRE = " + ID_Offre;
            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if (rs.next()) {

                Offer o = ConstruireOffreDepuisRS(rs);

                o.setCodes_Competences(ChargerCompetences(ID_Offre));
                o.setCodes_Langues(ChargerLangues(ID_Offre));
                o.setCodes_Backgrounds(ChargerBackgrounds(ID_Offre));

                rs.getStatement().close();
                return o;
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Avoir Offre " + ID_Offre, ex);
            return null;
        }

        CError.log(LogType.ERROR, "Erreur Avoir Offre " + ID_Offre);
        return null;
    }

    static private List ChargerCompetences(int ID_Offre) {
        List<String> list = new java.util.ArrayList<>();
        try {
            String sql =
                    "SELECT Code_Type_Competence " +
                            "FROM DETAIL_OFFRE_COMPETENCE " +
                            "WHERE ID_OFFRE = " + ID_Offre;

            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            while (rs.next()) {
                list.add(rs.getString("Code_Type_Competence"));
            }
            rs.close();
            rs.getStatement().close();

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Charger Competences Offre " + ID_Offre, ex);
        }
        return list;
    }

    static private List ChargerLangues(int ID_Offre) {
        List<String> list = new java.util.ArrayList<>();
        try {
            String sql =
                    "SELECT CODE_TYPE_LANGUE " +
                            "FROM DETAIL_OFFRE_LANGUE " +
                            "WHERE ID_OFFRE = " + ID_Offre;

            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            while (rs.next()) {
                list.add(rs.getString("CODE_TYPE_LANGUE"));
            }
            rs.close();
            rs.getStatement().close();

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Charger Langues Offre " + ID_Offre, ex);
        }
        return list;
    }

    static private List ChargerBackgrounds(int ID_Offre) {
        List<String> list = new java.util.ArrayList<>();
        try {
            String sql =
                    "SELECT CODE_TYPE_BACKGROUND_ETUDE " +
                            "FROM DETAIL_OFFRE_BACKGROUND " +
                            "WHERE ID_OFFRE = " + ID_Offre;

            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            while (rs.next()) {
                list.add(rs.getString("CODE_TYPE_BACKGROUND_ETUDE"));
            }
            rs.close();
            rs.getStatement().close();

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Charger Backgrounds Offre " + ID_Offre, ex);
        }
        return list;
    }

    static private Offer ConstruireOffreDepuisRS(ResultSet rs) throws Exception {
        Offer o = new Offer(
                rs.getInt("ID_OFFRE"),
                rs.getString("TITRE"),
                rs.getString("DESCRIPTION"),
                rs.getInt("ID_ENTREPRISE"),
                rs.getString("WORK_TYPE"),
                rs.getString("CODE_TYPE_CONTRAT"),
                rs.getInt("NBR_ANNEE_EXPERIENCE"),
                rs.getString("CODE_TYPE_NIVEAU_ETUDE"),
                rs.getDouble("MIN_SALAIRE"),
                rs.getDouble("MAX_SALAIRE"),
                rs.getInt("NUM_ORDRE_CREATION"),
                rs.getInt("NUM_ORDRE_EXPIRATION")
        );

        return o;
    }

    static private void InsererDetails(Offer entity) {
        try {
            int id = entity.getID_Offre();

            for (String code : entity.getCodes_Competences()) {
                DBHelper.ExecuteQuery(
                        "INSERT INTO DETAIL_OFFRE_COMPETENCE (ID_OFFRE, Code_Type_Competence) VALUES (" +
                                id + ", '" + code + "')"
                );
            }

            for (String code : entity.getCodes_Langues()) {
                DBHelper.ExecuteQuery(
                        "INSERT INTO DETAIL_OFFRE_LANGUE (ID_OFFRE, Code_Type_Langue) VALUES (" +
                                id + ", '" + code + "')"
                );
            }

            for (String code : entity.getCodes_Backgrounds()) {
                DBHelper.ExecuteQuery(
                        "INSERT INTO DETAIL_OFFRE_BACKGROUND (ID_OFFRE, Code_Type_Background_Etude ) VALUES (" +
                                id + ", '" + code + "')"
                );
            }

        } catch (Exception ex) {
            CError.log(
                    LogType.ERROR,
                    "Erreur Inserer Details Offre " + entity.getID_Offre(),
                    ex
            );
        }
    }


    static private void SupprimerDetails(int ID_Offre) {
        try {

            DBHelper.ExecuteQuery(
                    "DELETE FROM DETAIL_OFFRE_COMPETENCE WHERE ID_OFFRE = " + ID_Offre
            );

            DBHelper.ExecuteQuery(
                    "DELETE FROM DETAIL_OFFRE_LANGUE WHERE ID_OFFRE = " + ID_Offre
            );

            DBHelper.ExecuteQuery(
                    "DELETE FROM DETAIL_OFFRE_BACKGROUND WHERE ID_OFFRE = " + ID_Offre
            );

        } catch (Exception ex) {
            CError.log(
                    LogType.ERROR,
                    "Erreur Supprimer Details Offre " + ID_Offre,
                    ex
            );
        }
    }

    public static List<TypeItem> loadCompetencesFromDB() {
        List<TypeItem> list = new ArrayList<>();
        try {
            ResultSet rs = DBHelper.ExecuteDataReader(
                    "SELECT Code_Type_Competence, Description_Competence FROM TYPE_COMPETENCE"
            );
            if (rs != null) {
                while (rs.next()) {
                    list.add(new TypeItem(
                            rs.getString("Code_Type_Competence"),
                            rs.getString("Description_Competence")
                    ));
                }
                rs.close();
                rs.getStatement().close();
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement competences", ex);
        }
        return list;
    }

    public static List<TypeItem> loadLanguesFromDB() {
        List<TypeItem> list = new ArrayList<>();
        try {
            ResultSet rs = DBHelper.ExecuteDataReader(
                    "SELECT CODE_TYPE_LANGUE, DESCRIPTION_LANGUE FROM TYPE_LANGUE"
            );
            if (rs != null) {
                while (rs.next()) {
                    list.add(new TypeItem(
                            rs.getString("CODE_TYPE_LANGUE"),
                            rs.getString("DESCRIPTION_LANGUE")
                    ));
                }
                rs.close();
                rs.getStatement().close();
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement langues", ex);
        }
        return list;
    }

    public static List<TypeItem> loadBackgroundsFromDB() {
        List<TypeItem> list = new ArrayList<>();
        try {
            ResultSet rs = DBHelper.ExecuteDataReader(
                    "SELECT CODE_TYPE_BACKGROUND_ETUDE, DESCRIPTION_TYPE_BACKGROUND_ETUDE " +
                            "FROM TYPE_BACKGROUND_ETUDE"
            );
            if (rs != null) {
                while (rs.next()) {
                    list.add(new TypeItem(
                            rs.getString("CODE_TYPE_BACKGROUND_ETUDE"),
                            rs.getString("DESCRIPTION_TYPE_BACKGROUND_ETUDE")
                    ));
                }
                rs.close();
                rs.getStatement().close();
            }
        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement backgrounds", ex);
        }
        return list;
    }

    public static List<TypeItem> loadNiveauxEtudeFromDB() {

        List<TypeItem> list = new ArrayList<>();

        try {
            ResultSet rs = DBHelper.ExecuteDataReader(
                    "SELECT CODE_TYPE_NIVEAU_ETUDE, DESCRIPTION_TYPE_ETUDE " +
                            "FROM TYPE_NIVEAU_ETUDE"
            );

            if (rs != null) {
                while (rs.next()) {
                    list.add(new TypeItem(
                            rs.getString("CODE_TYPE_NIVEAU_ETUDE"),
                            rs.getString("DESCRIPTION_TYPE_ETUDE")
                    ));
                }
                rs.close();
                rs.getStatement().close();
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement niveaux etude", ex);
        }

        return list;
    }

    public static List<TypeItem> loadTypesContratFromDB() {

        List<TypeItem> list = new ArrayList<>();

        try {
            ResultSet rs = DBHelper.ExecuteDataReader(
                    "SELECT CODE_TYPE_CONTRAT, DESCRIPTION_CONTRAT FROM TYPE_CONTRAT"
            );

            if (rs != null) {
                while (rs.next()) {
                    list.add(new TypeItem(
                            rs.getString("CODE_TYPE_CONTRAT"),
                            rs.getString("DESCRIPTION_CONTRAT")
                    ));
                }
                rs.close();
                rs.getStatement().close();
            }

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur chargement types contrat", ex);
        }

        return list;
    }

}
