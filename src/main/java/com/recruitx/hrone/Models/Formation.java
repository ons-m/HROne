package com.recruitx.hrone.Models;

public class Formation {
    private int idFormation;
    private String titre;
    private String description;
    private int numOrdreCreation;
    private int idEntreprise;
    private String image;
    private String mode;           // "presentiel" ou "en_ligne"
    private int nombrePlaces;
    private int placesRestantes;
    private long dateDebut;        // NumOrdre COrdre
    private long dateFin;          // NumOrdre COrdre

    public Formation() {}

    public Formation(String titre, String description, int numOrdreCreation,
                     int idEntreprise, String image) {
        this.titre = titre;
        this.description = description;
        this.numOrdreCreation = numOrdreCreation;
        this.idEntreprise = idEntreprise;
        this.image = image;
    }

    // Getters et Setters existants
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

    // Nouveaux getters/setters
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public int getNombrePlaces() { return nombrePlaces; }
    public void setNombrePlaces(int nombrePlaces) { this.nombrePlaces = nombrePlaces; }

    public int getPlacesRestantes() { return placesRestantes; }
    public void setPlacesRestantes(int placesRestantes) { this.placesRestantes = placesRestantes; }

    public long getDateDebut() { return dateDebut; }
    public void setDateDebut(long dateDebut) { this.dateDebut = dateDebut; }

    public long getDateFin() { return dateFin; }
    public void setDateFin(long dateFin) { this.dateFin = dateFin; }

    public boolean isEnLigne() { return "en_ligne".equals(mode); }
    public boolean isPresentiel() { return "presentiel".equals(mode); }

    @Override
    public String toString() {
        return titre + " (Ordre: " + numOrdreCreation + ")";
    }
}