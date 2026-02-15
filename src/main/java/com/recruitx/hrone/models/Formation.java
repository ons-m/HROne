package com.recruitx.hrone.models;

public class Formation {
    private int idFormation;
    private String titre;
    private String description;
    private int numOrdreCreation;
    private int idEntreprise;
    private String image;
    // Constructeurs
    public Formation() {}

    public Formation(String titre, String description, int numOrdreCreation,
                     int idEntreprise, String image) {
        this.titre = titre;
        this.description = description;
        this.numOrdreCreation = numOrdreCreation;
        this.idEntreprise = idEntreprise;
        this.image = image;
    }

    // Getters et Setters
    public int getIdFormation() { return idFormation; }
    public void setIdFormation(int idFormation) { this.idFormation = idFormation; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getNumOrdreCreation() { return numOrdreCreation; }
    public void setNumOrdreCreation(int numOrdreCreation) { this.numOrdreCreation = numOrdreCreation; }

    public int getIdEntreprise() { return idEntreprise; }
    public void setIdEntreprise(int idEntreprise) { this.idEntreprise = idEntreprise; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    @Override
    public String toString() {
        return titre + " (Ordre: " + numOrdreCreation + ")";
    }
}