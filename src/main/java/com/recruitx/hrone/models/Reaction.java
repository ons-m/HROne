package com.recruitx.hrone.models;

public class Reaction {
    private int idReaction;
    private int idCommentaire;
    private String codeTypeReaction;
    private int idUtilisateur;
    private int numOrdreReaction;

    // Constructeurs
    public Reaction() {}

    public Reaction(int idCommentaire, String codeTypeReaction, int idUtilisateur, int numOrdreReaction) {
        this.idCommentaire = idCommentaire;
        this.codeTypeReaction = codeTypeReaction;
        this.idUtilisateur = idUtilisateur;
        this.numOrdreReaction = numOrdreReaction;
    }

    // Getters et Setters
    public int getIdReaction() { return idReaction; }
    public void setIdReaction(int idReaction) { this.idReaction = idReaction; }

    public int getIdCommentaire() { return idCommentaire; }
    public void setIdCommentaire(int idCommentaire) { this.idCommentaire = idCommentaire; }

    public String getCodeTypeReaction() { return codeTypeReaction; }
    public void setCodeTypeReaction(String codeTypeReaction) { this.codeTypeReaction = codeTypeReaction; }

    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getNumOrdreReaction() { return numOrdreReaction; }
    public void setNumOrdreReaction(int numOrdreReaction) { this.numOrdreReaction = numOrdreReaction; }

    @Override
    public String toString() {
        return "Réaction #" + idReaction + " (Type: " + codeTypeReaction + ")";
    }
}
