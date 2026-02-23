package com.recruitx.hrone.models;

public class Commentaire {
    private int idCommentaire;
    private int idUtilisateur;
    private int idPost;
    private boolean estReponse;
    private Integer idParent;
    private int numOrdreCommentaire;
    private String contenu;

    public Commentaire() {}

    public Commentaire(int idUtilisateur, int idPost, boolean estReponse,
                       Integer idParent, int numOrdreCommentaire, String contenu) {
        this.idUtilisateur = idUtilisateur;
        this.idPost = idPost;
        this.estReponse = estReponse;
        this.idParent = idParent;
        this.numOrdreCommentaire = numOrdreCommentaire;
        this.contenu = contenu;
    }

    public int getIdCommentaire() { return idCommentaire; }
    public void setIdCommentaire(int idCommentaire) { this.idCommentaire = idCommentaire; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getIdPost() { return idPost; }
    public void setIdPost(int idPost) { this.idPost = idPost; }

    public boolean isEstReponse() { return estReponse; }
    public void setEstReponse(boolean estReponse) { this.estReponse = estReponse; }

    public Integer getIdParent() { return idParent; }
    public void setIdParent(Integer idParent) { this.idParent = idParent; }

    public int getNumOrdreCommentaire() { return numOrdreCommentaire; }
    public void setNumOrdreCommentaire(int numOrdreCommentaire) { this.numOrdreCommentaire = numOrdreCommentaire; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    @Override
    public String toString() {
        return "Commentaire #" + idCommentaire;
    }
}