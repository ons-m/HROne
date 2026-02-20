package com.recruitx.hrone.entities;

import java.util.Date;

public class Utilisateur {

    private int idUtilisateur;
    private int idEntreprise;
    private int idProfil;
    private String nomUtilisateur;
    private String motPasse;
    private String email;
    private String adresse;
    private String numTel;
    private String cin;
    private int numOrdreSignIn;
    private Date dateNaissance;
    private char gender;
    private int firstLogin;

    // Default constructor
    public Utilisateur() {
    }

    // Constructor with all fields
    public Utilisateur(int idUtilisateur, int idEntreprise, int idProfil, String nomUtilisateur,
                       String motPasse, String email, String adresse, String numTel,
                       String cin, int numOrdreSignIn, Date dateNaissance, char gender,  int firstLogin) {
        this.idUtilisateur = idUtilisateur;
        this.idEntreprise = idEntreprise;
        this.idProfil = idProfil;
        this.nomUtilisateur = nomUtilisateur;
        this.motPasse = motPasse;
        this.email = email;
        this.adresse = adresse;
        this.numTel = numTel;
        this.cin = cin;
        this.numOrdreSignIn = numOrdreSignIn;
        this.dateNaissance = dateNaissance;
        this.gender = gender;
        this.firstLogin = firstLogin;
    }

    // Getters and Setters
    public int getIdUtilisateur() { return idUtilisateur; }
    public void setIdUtilisateur(int idUtilisateur) { this.idUtilisateur = idUtilisateur; }

    public int getIdEntreprise() { return idEntreprise; }
    public void setIdEntreprise(int idEntreprise) { this.idEntreprise = idEntreprise; }

    public int getIdProfil() { return idProfil; }
    public void setIdProfil(int idProfil) { this.idProfil = idProfil; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }

    public String getMotPasse() { return motPasse; }
    public void setMotPasse(String motPasse) { this.motPasse = motPasse; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }

    public String getCin() { return cin; }
    public void setCin(String cin) { this.cin = cin; }

    public int getNumOrdreSignIn() { return numOrdreSignIn; }
    public void setNumOrdreSignIn(int numOrdreSignIn) { this.numOrdreSignIn = numOrdreSignIn; }

    public Date getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(Date dateNaissance) { this.dateNaissance = dateNaissance; }

    public char getGender() { return gender; }
    public void setGender(char gender) { this.gender = gender; }

    public int getFirstLogin() { return firstLogin; }
    public void setFirstLogin(int firstLogin) { this.firstLogin = firstLogin; }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "idUtilisateur=" + idUtilisateur +
                ", idEntreprise=" + idEntreprise +
                ", idProfil=" + idProfil +
                ", nomUtilisateur='" + nomUtilisateur + '\'' +
                ", motPasse='" + motPasse + '\'' +
                ", email='" + email + '\'' +
                ", adresse='" + adresse + '\'' +
                ", numTel='" + numTel + '\'' +
                ", cin='" + cin + '\'' +
                ", numOrdreSignIn=" + numOrdreSignIn +
                ", dateNaissance=" + dateNaissance +
                ", gender=" + gender +
                ", firstLogin=" + firstLogin +
                '}';
    }
}
