package com.recruitx.hrone.entities;

public class Activite {
    private int idActivite;
    private int idEvenement;
    private String titre;
    private String description;

    public Activite() {
    }

    public Activite(int idActivite, int idEvenement, String titre, String description) {
        this.idActivite = idActivite;
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
    }

    public Activite(int idEvenement, String titre, String description) {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
    }

    public int getIdEvenement() {
        return idEvenement;
    }

    public void setIdEvenement(int idEvenement) {
        this.idEvenement = idEvenement;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Activite{" +
                "idActivite=" + idActivite +
                ", idEvenement=" + idEvenement +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
