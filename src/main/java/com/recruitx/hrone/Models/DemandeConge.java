package com.recruitx.hrone.Models;

public class DemandeConge {

    private int idDemande;
    private int idEmploye;
    private int nbrJourDemande;
    private int numOrdreDebutConge;
    private int numOrdreFinConge;
    private int status;

    public DemandeConge() {
    }

    public DemandeConge(int idEmploye, int nbrJourDemande, int numOrdreDebutConge, int numOrdreFinConge, int status) {
        this.idEmploye = idEmploye;
        this.nbrJourDemande = nbrJourDemande;
        this.numOrdreDebutConge = numOrdreDebutConge;
        this.numOrdreFinConge = numOrdreFinConge;
        this.status = status;
    }

    public int getIdDemande() {
        return idDemande;
    }

    public void setIdDemande(int idDemande) {
        this.idDemande = idDemande;
    }

    public int getIdEmploye() {
        return idEmploye;
    }

    public void setIdEmploye(int idEmploye) {
        this.idEmploye = idEmploye;
    }

    public int getNbrJourDemande() {
        return nbrJourDemande;
    }

    public void setNbrJourDemande(int nbrJourDemande) {
        this.nbrJourDemande = nbrJourDemande;
    }

    public int getNumOrdreDebutConge() {
        return numOrdreDebutConge;
    }

    public void setNumOrdreDebutConge(int numOrdreDebutConge) {
        this.numOrdreDebutConge = numOrdreDebutConge;
    }

    public int getNumOrdreFinConge() {
        return numOrdreFinConge;
    }

    public void setNumOrdreFinConge(int numOrdreFinConge) {
        this.numOrdreFinConge = numOrdreFinConge;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
