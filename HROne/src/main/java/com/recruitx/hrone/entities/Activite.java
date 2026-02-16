package com.recruitx.hrone.entities;

public class Activite {
    private int idActivite;
    private String titre;
    private String description;

    public Activite() {
    }

    public Activite(int idActivite, String titre, String description) {
        this.idActivite = idActivite;
        this.titre = titre;
        this.description = description;
    }

    public Activite(String titre, String description) {
        this.titre = titre;
        this.description = description;
    }

    public int getIdActivite() {
        return idActivite;
    }

    public void setIdActivite(int idActivite) {
        this.idActivite = idActivite;
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
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
