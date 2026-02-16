package com.recruitx.hrone.entities;

public class ParticipationEvenement {
    private int idEvenement;
    private int idActivite;
    private int idParticipant;
    private int numOrdreParticipation;

    public ParticipationEvenement() {
    }

    public ParticipationEvenement(int idEvenement, int idActivite, int idParticipant, int numOrdreParticipation) {
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.idParticipant = idParticipant;
        this.numOrdreParticipation = numOrdreParticipation;
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

    public int getIdParticipant() {
        return idParticipant;
    }

    public void setIdParticipant(int idParticipant) {
        this.idParticipant = idParticipant;
    }

    public int getNumOrdreParticipation() {
        return numOrdreParticipation;
    }

    public void setNumOrdreParticipation(int numOrdreParticipation) {
        this.numOrdreParticipation = numOrdreParticipation;
    }

    @Override
    public String toString() {
        return "ParticipationEvenement{" +
                "idEvenement=" + idEvenement +
                ", idActivite=" + idActivite +
                ", idParticipant=" + idParticipant +
                ", numOrdreParticipation=" + numOrdreParticipation +
                '}';
    }
}
