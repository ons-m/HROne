package com.recruitx.hrone.entities;

public class Evenement {
    private int idEvenement;
    private String titre;
    private String description;
    private int numOrdreCreation;
    private int numOrdreDebutEvenement;
    private int numOrdreFinEvenement;
    private String localisation;
    private String image;

    public Evenement() {
    }

    public Evenement(int idEvenement, String titre, String description, int numOrdreCreation,
            int numOrdreDebutEvenement, int numOrdreFinEvenement, String localisation, String image) {
        this.idEvenement = idEvenement;
        this.titre = titre;
        this.description = description;
        this.numOrdreCreation = numOrdreCreation;
        this.numOrdreDebutEvenement = numOrdreDebutEvenement;
        this.numOrdreFinEvenement = numOrdreFinEvenement;
        this.localisation = localisation;
        this.image = image;
    }

    public Evenement(String titre, String description, int numOrdreCreation, int numOrdreDebutEvenement,
            int numOrdreFinEvenement, String localisation, String image) {
        this.titre = titre;
        this.description = description;
        this.numOrdreCreation = numOrdreCreation;
        this.numOrdreDebutEvenement = numOrdreDebutEvenement;
        this.numOrdreFinEvenement = numOrdreFinEvenement;
        this.localisation = localisation;
        this.image = image;
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

    public int getNumOrdreCreation() {
        return numOrdreCreation;
    }

    public void setNumOrdreCreation(int numOrdreCreation) {
        this.numOrdreCreation = numOrdreCreation;
    }

    public int getNumOrdreDebutEvenement() {
        return numOrdreDebutEvenement;
    }

    public void setNumOrdreDebutEvenement(int numOrdreDebutEvenement) {
        this.numOrdreDebutEvenement = numOrdreDebutEvenement;
    }

    public int getNumOrdreFinEvenement() {
        return numOrdreFinEvenement;
    }

    public void setNumOrdreFinEvenement(int numOrdreFinEvenement) {
        this.numOrdreFinEvenement = numOrdreFinEvenement;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Evenement{" +
                "idEvenement=" + idEvenement +
                ", titre='" + titre + '\'' +
                ", description='" + description + '\'' +
                ", numOrdreCreation=" + numOrdreCreation +
                ", numOrdreDebutEvenement=" + numOrdreDebutEvenement +
                ", numOrdreFinEvenement=" + numOrdreFinEvenement +
                ", localisation='" + localisation + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
