package com.recruitx.hrone.entities;

public class Entreprise {

    private int idEntreprise;
    private String nomEntreprise;
    private String reference;

    // Constructor without ID (for creating new records)
    public Entreprise(String nomEntreprise, String reference) {
        this.nomEntreprise = nomEntreprise;
        this.reference = reference;
    }

    // Constructor with ID (for reading from DB)
    public Entreprise(int idEntreprise, String nomEntreprise, String reference) {
        this.idEntreprise = idEntreprise;
        this.nomEntreprise = nomEntreprise;
        this.reference = reference;
    }

    public Entreprise() {

    }

    // Getters and Setters
    public int getIdEntreprise() {
        return idEntreprise;
    }

    public void setIdEntreprise(int idEntreprise) {
        this.idEntreprise = idEntreprise;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public void setNomEntreprise(String nomEntreprise) {
        this.nomEntreprise = nomEntreprise;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    // Optional: toString() for debugging
    @Override
    public String toString() {
        return "Entreprise{" +
                "idEntreprise=" + idEntreprise +
                ", nomEntreprise='" + nomEntreprise + '\'' +
                ", reference='" + reference + '\'' +
                '}';
    }
}
