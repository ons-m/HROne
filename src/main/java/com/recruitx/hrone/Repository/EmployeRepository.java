package com.recruitx.hrone.Repository;

import com.recruitx.hrone.Controllers.Session;
import com.recruitx.hrone.Models.Employe;
import com.recruitx.hrone.Models.Utilisateur;
import com.recruitx.hrone.Utils.*;

import java.sql.ResultSet;
import java.util.List;

public class EmployeRepository {

    static public boolean Ajouter(Employe entity) {
        try {
            Utilisateur user = entity.getUser();
            UtilisateurRepository repository = new UtilisateurRepository();
            repository.create(user);

            String sql =
                    "INSERT INTO EMPLOYEE (ID_UTILISATEUR,SOLDE_CONGE, NBR_HEURE_DE_TRAVAIL, MAC_MACHINE,SALAIRE) VALUES (" +
                            user.getIdUtilisateur() + ", " +
                            entity.getSolde_Conger() + ", " +
                            entity.getNbr_Heure_De_Travail() + ", '" +
                            entity.getMac_Machine() + "'," +
                            entity.getSaliare() + ")";

            return DBHelper.ExecuteQuery(sql) == 1;

        } catch (Exception ex) {
            CError.log(LogType.ERROR, "Erreur Ajouter Employe", ex);
            return false;
        }
    }


    static public boolean Modifier(Employe entity) {
        try{
            Utilisateur user = entity.getUser();
            UtilisateurRepository repository = new UtilisateurRepository();
            repository.update(user);

            String sql =
                    "UPDATE EMPLOYEE SET " +
                            "SOLDE_CONGE = " + entity.getSolde_Conger() + ", " +
                            "NBR_HEURE_DE_TRAVAIL = " + entity.getNbr_Heure_De_Travail() + ", " +
                            "MAC_MACHINE = '" + entity.getMac_Machine() + "', " +
                            "SALAIRE = " + entity.getSaliare() + " " +
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
            Employe temp = AvoirEntite(id);


            String sql = "DELETE FROM EMPLOYEE WHERE ID_EMPLOYE = " + id;
            int result = DBHelper.ExecuteQuery(sql);

            String sqlUser = "DELETE FROM UTILISATEUR WHERE ID_UTILISATEUR = " + temp.getUser().getIdUtilisateur();
            DBHelper.ExecuteQuery(sqlUser);

            return  result >= 0;
        }catch (Exception ex){
            CError.log(LogType.ERROR, "Erreur Supprimer Employe" + id, ex);
            return false;
        }
    }


    static public List AvoirListe() {
        List<Employe> employes = new java.util.ArrayList<>();
        try{
            String sql = """
                SELECT e.*, u.*
                FROM EMPLOYEE e
                JOIN UTILISATEUR u ON e.ID_UTILISATEUR = u.ID_UTILISATEUR
                """;
            ResultSet rs = DBHelper.ExecuteDataReader(sql);

            if(rs != null){
                while (rs.next()) {

                    // --- USER ---
                    Utilisateur user = new Utilisateur();
                    user.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                    user.setNomUtilisateur(rs.getString("Nom_Utilisateur"));
                    user.setEmail(rs.getString("Email"));
                    user.setDateNaissance(rs.getDate("Date_Naissance"));
                    user.setAdresse(rs.getString("Adresse"));
                    user.setNumTel(rs.getString("Num_Tel"));
                    user.setCin(rs.getString("CIN"));
                    user.setGender(rs.getString("Gender").charAt(0));
                    user.setIdEntreprise(rs.getInt("ID_Entreprise"));
                    user.setIdProfil(rs.getInt("ID_Profil"));

                    // --- EMPLOYEE ---
                    Employe e = new Employe(
                            rs.getInt("ID_EMPLOYE"),
                            user,
                            rs.getInt("SOLDE_CONGE"),
                            rs.getInt("NBR_HEURE_DE_TRAVAIL"),
                            rs.getString("MAC_MACHINE"),
                            rs.getInt("SALAIRE")
                    );

                    employes.add(e);
                }
                rs.getStatement().close();
                rs.close();

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
            String sql = "SELECT * FROM EMPLOYEE JOIN UTILISATEUR ON EMPLOYEE.ID_UTILISATEUR = UTILISATEUR.ID_UTILISATEUR WHERE ID_EMPLOYE = " + ID_Employe;
            ResultSet rs = DBHelper.ExecuteDataReader(sql);
            if(rs.next()){
                // --------- UTILISATEUR ----------
                Utilisateur user = new Utilisateur();
                user.setIdUtilisateur(rs.getInt("ID_UTILISATEUR"));
                user.setIdEntreprise(rs.getInt("ID_Entreprise"));
                user.setIdProfil(rs.getInt("ID_Profil"));
                user.setNomUtilisateur(rs.getString("Nom_Utilisateur"));
                user.setMotPasse(rs.getString("Mot_Passe"));
                user.setEmail(rs.getString("Email"));
                user.setAdresse(rs.getString("Adresse"));
                user.setNumTel(rs.getString("Num_Tel"));
                user.setCin(rs.getString("CIN"));
                user.setNumOrdreSignIn(rs.getInt("Num_Ordre_Sign_In"));
                user.setDateNaissance(rs.getDate("Date_Naissance"));
                user.setGender(rs.getString("Gender").charAt(0));

                // --------- EMPLOYEE ----------
                return new Employe(
                        rs.getInt("ID_EMPLOYE"),
                        user,
                        rs.getInt("SOLDE_CONGE"),
                        rs.getInt("NBR_HEURE_DE_TRAVAIL"),
                        rs.getString("MAC_MACHINE"),
                        rs.getInt("SALAIRE")
                );
            }

        }catch (Exception ex){
            CError.log(LogType.ERROR, "Erreur Avoir Employe " + ID_Employe, ex);
            return null;
        }
        CError.log(LogType.ERROR, "Erreur Avoir Employe " + ID_Employe);
        return null;
    }
}
