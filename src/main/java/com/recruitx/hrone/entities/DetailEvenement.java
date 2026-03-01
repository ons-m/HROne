package com.recruitx.hrone.entities;

public class DetailEvenement {
    private int idEvenement;
    private int idActivite;
    private int numOrdreDebutActivite;
    private int numOrdreFinActivite;

    public DetailEvenement() {
    }

    public DetailEvenement(int idEvenement, int idActivite, int numOrdreDebutActivite, int numOrdreFinActivite) {
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.numOrdreDebutActivite = numOrdreDebutActivite;
        this.numOrdreFinActivite = numOrdreFinActivite;
    }

    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public int getNumOrdreDebutActivite() {
        return numOrdreDebutActivite;
    }

    public void setNumOrdreDebutActivite(int numOrdreDebutActivite) {
        this.numOrdreDebutActivite = numOrdreDebutActivite;
    }

    public int getNumOrdreFinActivite() {
        return numOrdreFinActivite;
    }

    public void setNumOrdreFinActivite(int numOrdreFinActivite) {
        this.numOrdreFinActivite = numOrdreFinActivite;
    }

    @Override
    public String toString() {
        return "DetailEvenement{" +
                "idEvenement=" + idEvenement +
                ", idActivite=" + idActivite +
                ", numOrdreDebutActivite=" + numOrdreDebutActivite +
                ", numOrdreFinActivite=" + numOrdreFinActivite +
                '}';
    }
}
