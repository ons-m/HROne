package com.recruitx.hrone.dao;

import com.recruitx.hrone.models.Formation;
import com.recruitx.hrone.utils.COrdre;
import com.recruitx.hrone.utils.CertificatGenerator;
import com.recruitx.hrone.utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipationFormationDAO {

    private Connection getConn() throws SQLException {
        return DBConnection.getInstance();
    }

    // ✅ Insérer participation + décrémenter places
    public boolean insererParticipation(int idFormation, int idParticipant) {
        String sql = "INSERT INTO participation_formation " +
                "(ID_Formation, ID_Participant, Num_Ordre_Participation, Statut) " +
                "VALUES (?, ?, ?, 'inscrit')";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            ps.setLong(3, COrdre.GetNumOrdreNow());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) decrementerPlaces(idFormation);
            return ok;
        } catch (SQLException e) {
            System.err.println("Erreur insertion: " + e.getMessage());
            return false;
        }
    }

    // ✅ Décrémenter places
    private void decrementerPlaces(int idFormation) {
        String sql = "UPDATE formation SET PlacesRestantes = PlacesRestantes - 1 " +
                "WHERE ID_Formation = ? AND PlacesRestantes > 0";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur décrémentation: " + e.getMessage());
        }
    }

    // ✅ Places restantes
    public int getPlacesRestantes(int idFormation) {
        String sql = "SELECT PlacesRestantes FROM formation WHERE ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("PlacesRestantes");
        } catch (SQLException e) {
            System.err.println("Erreur places: " + e.getMessage());
        }
        return 0;
    }

    // ✅ Déjà inscrit ?
// ✅ Vérifier TOUS les statuts (inscrit + acheve)
    public boolean estDejaInscrit(int idFormation, int idParticipant) {
        String sql = "SELECT 1 FROM participation_formation " +
                "WHERE ID_Formation = ? AND ID_Participant = ?"; // ✅ sans filtre Statut
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.err.println("Erreur vérification: " + e.getMessage());
        }
        return false;
    }
    // ✅ Chercher par email — colonne Email + ID_UTILISATEUR
    public int getIdParticipantByEmail(String email) {
        String sql = "SELECT ID_UTILISATEUR FROM utilisateur WHERE Email = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("ID_UTILISATEUR");
        } catch (SQLException e) {
            System.err.println("Erreur recherche email: " + e.getMessage());
        }
        return -1;
    }

    private String getNomCandidat(int idParticipant) {
        String sql = "SELECT Nom_Utilisateur FROM utilisateur WHERE ID_UTILISATEUR = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idParticipant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("Nom_Utilisateur");
        } catch (SQLException e) {
            System.err.println("Erreur nom candidat: " + e.getMessage());
        }
        return "Candidat";
    }

    private String getTitreFormation(int idFormation) {
        String sql = "SELECT Titre FROM formation WHERE ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("Titre");
        } catch (SQLException e) {
            System.err.println("Erreur titre formation: " + e.getMessage());
        }
        return "Formation";
    }

    private String getLogoEntreprise(int idFormation) {
        return null;}
    private void sauvegarderCheminCertificat(int idFormation, int idParticipant,
                                             String chemin) {
        // ✅ Générer bytes PDF pour stockage DB
        String niveau = getNiveauFormation(idFormation);
        String nomCandidat = getNomCandidat(idParticipant);
        String titreFormation = getTitreFormation(idFormation);

        byte[] pdfBytes = CertificatGenerator.genererBytes(
                nomCandidat, titreFormation, niveau, null);

        String sql = "INSERT INTO certification " +
                "(ID_Formation, ID_Participant, Description_Certif, Fichier_PDF) " +
                "VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            ps.setString(3, chemin);                           // ✅ chemin fichier
            ps.setBytes(4, pdfBytes);                         // ✅ PDF en binaire DB
            ps.executeUpdate();
            System.out.println("✅ Certificat sauvegardé en DB (binaire)");
        } catch (SQLException e) {
            System.err.println("❌ Erreur sauvegarde certificat: " + e.getMessage());
        }
    }    // ✅ Marquer comme achevée
    public boolean marquerAcheve(int idFormation, int idParticipant) {
        String sql = "UPDATE participation_formation SET Statut = 'acheve' " +
                "WHERE ID_Formation = ? AND ID_Participant = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ps.setInt(2, idParticipant);
            boolean ok = ps.executeUpdate() > 0;

            if (ok) {
                String nomCandidat = getNomCandidat(idParticipant);
                String titreFormation = getTitreFormation(idFormation);
                String logoPath = getLogoEntreprise(idFormation);
                String emailCandidat = getEmailById(idParticipant);

                // ✅ Récupérer niveau depuis DB
                String niveau = getNiveauFormation(idFormation);

                System.out.println("🔄 Génération certificat pour: " + nomCandidat);

                // ✅ 4 arguments avec niveau
                String cheminPdf = CertificatGenerator.generer(
                        nomCandidat, titreFormation, niveau, logoPath);

                if (cheminPdf != null) {
                    System.out.println("✅ Certificat généré : " + cheminPdf);
                    sauvegarderCheminCertificat(idFormation, idParticipant, cheminPdf);

                    // ✅ Envoyer par email en arrière-plan
                    if (emailCandidat != null) {
                        String finalNom = nomCandidat;
                        String finalTitre = titreFormation;
                        String finalNiveau = niveau;
                        String finalChemin = cheminPdf;
                        new Thread(() ->
                                com.recruitx.hrone.utils.EmailService.sendCertificatEmail(
                                        emailCandidat,
                                        finalNom,
                                        finalTitre,
                                        finalNiveau,
                                        finalChemin
                                )
                        ).start();
                        System.out.println("📧 Email certificat envoyé à : " + emailCandidat);
                    }

                    // ✅ Ouvrir automatiquement le PDF
                    try {
                        java.awt.Desktop.getDesktop().open(new java.io.File(cheminPdf));
                    } catch (Exception e) {
                        System.err.println("Impossible d'ouvrir le PDF: " + e.getMessage());
                    }
                } else {
                    System.err.println("❌ Certificat non généré !");
                }
            }
            return ok;
        } catch (SQLException e) {
            System.err.println("Erreur achèvement: " + e.getMessage());
            return false;
        }
    }

    // ✅ Récupérer niveau depuis DB
    private String getNiveauFormation(int idFormation) {
        String sql = "SELECT Niveau FROM formation WHERE ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String niveau = rs.getString("Niveau");
                return niveau != null ? niveau : "Non défini";
            }
        } catch (SQLException e) {
            System.err.println("Erreur niveau: " + e.getMessage());
        }
        return "Non défini";
    }    public List<String[]> getParticipantsByFormation(int idFormation) {
        List<String[]> participants = new ArrayList<>();
        String sql = "SELECT pf.ID_Participant, u.Nom_Utilisateur, pf.Statut " +
                "FROM participation_formation pf " +
                "LEFT JOIN utilisateur u ON u.ID_UTILISATEUR = pf.ID_Participant " +
                "WHERE pf.ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                participants.add(new String[]{
                        String.valueOf(rs.getInt("ID_Participant")),
                        rs.getString("Nom_Utilisateur"),
                        rs.getString("Statut")
                });
            }
        } catch (SQLException e) {
            System.err.println("Erreur participants: " + e.getMessage());
        }
        return participants;
    }
    // Récupérer formations du candidat
    public List<Formation> getFormationsDuCandidat(int idParticipant) {
        List<Formation> result = new ArrayList<>();
        String sql = """
        SELECT f.ID_Formation, f.Titre, f.Date_Debut, f.Date_Fin, f.Mode
        FROM participation_formation pf
        JOIN formation f ON pf.ID_Formation = f.ID_Formation
        WHERE pf.ID_Participant = ?
        AND pf.Statut = 'inscrit'
        AND f.Date_Debut != 0 AND f.Date_Fin != 0
        """;
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idParticipant);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Formation f = new Formation();
                f.setIdFormation(rs.getInt("ID_Formation"));
                f.setTitre(rs.getString("Titre"));
                f.setDateDebut(rs.getLong("Date_Debut"));
                f.setDateFin(rs.getLong("Date_Fin"));
                f.setMode(rs.getString("Mode"));
                result.add(f);
            }
        } catch (SQLException e) {
            System.err.println("Erreur formations candidat: " + e.getMessage());
        }
        return result;
    }

    // Vérifier conflit de dates
    public boolean aConflitDeDates(int idFormation, int idParticipant) {
        String sql = "SELECT Date_Debut, Date_Fin FROM formation WHERE ID_Formation = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idFormation);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                long debut = rs.getLong("Date_Debut");
                long fin = rs.getLong("Date_Fin");
                if (debut == 0 && fin == 0) return false;

                String sqlConflict = """
                SELECT pf.ID_Formation FROM participation_formation pf
                JOIN formation f ON pf.ID_Formation = f.ID_Formation
                WHERE pf.ID_Participant = ?
                AND pf.Statut = 'inscrit'
                AND f.Date_Debut < ? AND f.Date_Fin > ?
                """;
                PreparedStatement ps2 = getConn().prepareStatement(sqlConflict);
                ps2.setInt(1, idParticipant);
                ps2.setLong(2, fin);
                ps2.setLong(3, debut);
                return ps2.executeQuery().next();
            }
        } catch (SQLException e) {
            System.err.println("Erreur conflit: " + e.getMessage());
        }
        return false;
    }
    // ✅ Récupérer email depuis DB
    public String getEmailById(int idParticipant) {
        String sql = "SELECT Email FROM utilisateur WHERE ID_UTILISATEUR = ?";
        try {
            PreparedStatement ps = getConn().prepareStatement(sql);
            ps.setInt(1, idParticipant);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("Email");
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération email: " + e.getMessage());
        }
        return null;
    }


}