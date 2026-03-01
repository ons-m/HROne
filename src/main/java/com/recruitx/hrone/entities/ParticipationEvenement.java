package com.recruitx.hrone.entities;

public class ParticipationEvenement {
    private int idEvenement;
    private int idActivite;
    private int idParticipant;
    private int numOrdreParticipation;
    private String nomComplet;
    private String email;
    private String description;
    private String modePaiement;

    public ParticipationEvenement() {
    }

    public ParticipationEvenement(int idEvenement, int idActivite, int idParticipant, int numOrdreParticipation,
            String nomComplet, String email, String description, String modePaiement) {
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.idParticipant = idParticipant;
        this.numOrdreParticipation = numOrdreParticipation;
        this.nomComplet = nomComplet;
        this.email = email;
        this.description = description;
        this.modePaiement = modePaiement;
    }

    // Constructor without payment method (for backward compatibility before user
    // explicitly chooses)
    public ParticipationEvenement(int idEvenement, int idActivite, int idParticipant, int numOrdreParticipation,
            String nomComplet, String email, String description) {
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.idParticipant = idParticipant;
        this.numOrdreParticipation = numOrdreParticipation;
        this.nomComplet = nomComplet;
        this.email = email;
        this.description = description;
        this.modePaiement = "Gratuit";
    }

    public ParticipationEvenement(int idEvenement, int idActivite, int idParticipant, int numOrdreParticipation) {
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.idParticipant = idParticipant;
        this.numOrdreParticipation = numOrdreParticipation;
    }

    public String getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(String modePaiement) {
        this.modePaiement = modePaiement;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public void setNomComplet(String nomComplet) {
        this.nomComplet = nomComplet;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
                ", nomComplet='" + nomComplet + '\'' +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                ", modePaiement='" + modePaiement + '\'' +
                '}';
    }
}
