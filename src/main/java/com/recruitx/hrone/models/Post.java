package com.recruitx.hrone.models;

public class Post {
    private int idPost;
    private int idUtilisateur;
    private String titre;
    private String description;
    private String image;
    private int numOrdrePost;

    public Post() {}

    public Post(int idUtilisateur, String titre, String description, String image, int numOrdrePost) {
        this.idUtilisateur = idUtilisateur;
        this.titre = titre;
        this.description = description;
        this.image = image;
        this.numOrdrePost = numOrdrePost;
    }

    public int getIdPost() { return idPost; }
    public void setIdPost(int idPost) { this.idPost = idPost; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getNumOrdrePost() { return numOrdrePost; }
    public void setNumOrdrePost(int numOrdrePost) { this.numOrdrePost = numOrdrePost; }

    @Override
    public String toString() {
        return titre;
    }
}