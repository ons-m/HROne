package com.recruitx.hrone.Models;

public class Entretien {

    private int idCandidat;
    private int idRh;
    private int numOrdreEntretien;
    private String localisation;
    private int statusEntretien;
    private String evaluation;

    public Entretien() {
    }

    public Entretien(
            int idCandidat,
            int idRh,
            int numOrdreEntretien,
            String localisation,
            int statusEntretien,
            String evaluation
    ) {
        this.idCandidat = idCandidat;
        this.idRh = idRh;
        this.numOrdreEntretien = numOrdreEntretien;
        this.localisation = localisation;
        this.statusEntretien = statusEntretien;
        this.evaluation = evaluation;
    }

    public int getIdCandidat() {
        return idCandidat;
    }

    public void setIdCandidat(int idCandidat) {
        this.idCandidat = idCandidat;
    }

    public int getIdRh() {
        return idRh;
    }

    public void setIdRh(int idRh) {
        this.idRh = idRh;
    }

    public int getNumOrdreEntretien() {
        return numOrdreEntretien;
    }

    public void setNumOrdreEntretien(int numOrdreEntretien) {
        this.numOrdreEntretien = numOrdreEntretien;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public int getStatusEntretien() {
        return statusEntretien;
    }

    public void setStatusEntretien(int statusEntretien) {
        this.statusEntretien = statusEntretien;
    }

    public String getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(String evaluation) {
        this.evaluation = evaluation;
    }
}
