package com.recruitx.hrone;

import java.util.ArrayList;
import java.util.List;

public class Offer {

    private int ID_Offre;
    private String Titre;
    private String Description;
    private int ID_Entreprise;
    private String Work_Type;
    private String Code_Type_Contrat;
    private int Nbr_Annee_Experience;
    private String Code_Type_Niveau_Etude;
    private double Min_Salaire;
    private double Max_Salaire;
    private int Num_Ordre_Creation;
    private int Num_Ordre_Expiration;

    private List<String> Codes_Competences = new ArrayList<>();
    private List<String> Codes_Langues = new ArrayList<>();
    private List<String> Codes_Backgrounds = new ArrayList<>();

    public Offer() {}

    public Offer(int ID_Offre, String Titre) {
        this.ID_Offre = ID_Offre;
        this.Titre = Titre;
    }

    public Offer(
            int ID_Offre,
            String Titre,
            String Description,
            int ID_Entreprise,
            String Work_Type,
            String Code_Type_Contrat,
            int Nbr_Annee_Experience,
            String Code_Type_Niveau_Etude,
            double Min_Salaire,
            double Max_Salaire,
            int Num_Ordre_Creation,
            int Num_Ordre_Expiration
    ) {
        this.ID_Offre = ID_Offre;
        this.Titre = Titre;
        this.Description = Description;
        this.ID_Entreprise = ID_Entreprise;
        this.Work_Type = Work_Type;
        this.Code_Type_Contrat = Code_Type_Contrat;
        this.Nbr_Annee_Experience = Nbr_Annee_Experience;
        this.Code_Type_Niveau_Etude = Code_Type_Niveau_Etude;
        this.Min_Salaire = Min_Salaire;
        this.Max_Salaire = Max_Salaire;
        this.Num_Ordre_Creation = Num_Ordre_Creation;
        this.Num_Ordre_Expiration = Num_Ordre_Expiration;
    }

    // Getters
    public int getID_Offre() { return ID_Offre; }
    public String getTitre() { return Titre; }
    public String getDescription() { return Description; }
    public int getID_Entreprise() { return ID_Entreprise; }
    public String getWork_Type() { return Work_Type; }
    public String getCode_Type_Contrat() { return Code_Type_Contrat; }
    public int getNbr_Annee_Experience() { return Nbr_Annee_Experience; }
    public String getCode_Type_Niveau_Etude() { return Code_Type_Niveau_Etude; }
    public double getMin_Salaire() { return Min_Salaire; }
    public double getMax_Salaire() { return Max_Salaire; }
    public int getNum_Ordre_Creation() { return Num_Ordre_Creation; }
    public int getNum_Ordre_Expiration() { return Num_Ordre_Expiration; }
    public List<String> getCodes_Competences() { return Codes_Competences; }
    public List<String> getCodes_Langues() { return Codes_Langues; }
    public List<String> getCodes_Backgrounds() { return Codes_Backgrounds; }

    // Setters
    public void setID_Offre(int ID_Offre) { this.ID_Offre = ID_Offre; }
    public void setTitre(String titre) { Titre = titre; }
    public void setDescription(String description) { Description = description; }
    public void setID_Entreprise(int ID_Entreprise) { this.ID_Entreprise = ID_Entreprise; }
    public void setWork_Type(String work_Type) { Work_Type = work_Type; }
    public void setCode_Type_Contrat(String code_Type_Contrat) { Code_Type_Contrat = code_Type_Contrat; }
    public void setNbr_Annee_Experience(int nbr_Annee_Experience) { Nbr_Annee_Experience = nbr_Annee_Experience; }
    public void setCode_Type_Niveau_Etude(String code_Type_Niveau_Etude) { Code_Type_Niveau_Etude = code_Type_Niveau_Etude; }
    public void setMin_Salaire(double min_Salaire) { Min_Salaire = min_Salaire; }
    public void setMax_Salaire(double max_Salaire) { Max_Salaire = max_Salaire; }
    public void setNum_Ordre_Creation(int num_Ordre_Creation) { Num_Ordre_Creation = num_Ordre_Creation; }
    public void setNum_Ordre_Expiration(int num_Ordre_Expiration) { Num_Ordre_Expiration = num_Ordre_Expiration; }
    public void setCodes_Competences(List<String> list) { this.Codes_Competences = list; }
    public void setCodes_Langues(List<String> list) { this.Codes_Langues = list; }
    public void setCodes_Backgrounds(List<String> list) { this.Codes_Backgrounds = list; }

    @Override
    public String toString() {return getTitre();}
}
