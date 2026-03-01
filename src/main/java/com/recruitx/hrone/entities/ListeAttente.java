package com.recruitx.hrone.entities;

import java.sql.Timestamp;

public class ListeAttente {
    private int idAttente;
    private int idEvenement;
    private int idActivite;
    private String nomComplet;
    private String email;
    private Timestamp dateDemande;

    public ListeAttente() {
    }

    public ListeAttente(int idAttente, int idEvenement, int idActivite, String nomComplet, String email,
            Timestamp dateDemande) {
        this.idAttente = idAttente;
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.nomComplet = nomComplet;
        this.email = email;
        this.dateDemande = dateDemande;
    }

    public ListeAttente(int idEvenement, int idActivite, String nomComplet, String email) {
        this.idEvenement = idEvenement;
        this.idActivite = idActivite;
        this.nomComplet = nomComplet;
        this.email = email;
    }

    public int getIdAttente() {
        return idAttente;
    }

    public void setIdAttente(int idAttente) {
        this.idAttente = idAttente;
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

    public Timestamp getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Timestamp dateDemande) {
        this.dateDemande = dateDemande;
    }

    @Override
    public String toString() {
        return "ListeAttente{" +
                "idAttente=" + idAttente +
                ", idEvenement=" + idEvenement +
                ", idActivite=" + idActivite +
                ", nomComplet='" + nomComplet + '\'' +
                ", email='" + email + '\'' +
                ", dateDemande=" + dateDemande +
                '}';
    }
}
