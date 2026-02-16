package com.recruitx.hrone;

public class Candidature {

    private int ID_Candidature;
    private int ID_Candidat;
    private int ID_Offre;
    private String Lettre_Motivation;
    private String Portfolio;
    private String Lettre_Recomendation;
    private int Code_Type_Status;

    public Candidature() {}

    public Candidature(int ID_Condidature, int ID_Condidat, int ID_Offre) {
        this.ID_Candidature = ID_Condidature;
        this.ID_Candidat = ID_Condidat;
        this.ID_Offre = ID_Offre;
    }

    public Candidature(
            int ID_Condidature,
            int ID_Condidat,
            int ID_Offre,
            String Lettre_Motivation,
            String Portfolio,
            String Lettre_Recomendation,
            int Code_Type_Status
    ) {
        this.ID_Candidature = ID_Condidature;
        this.ID_Candidat = ID_Condidat;
        this.ID_Offre = ID_Offre;
        this.Lettre_Motivation = Lettre_Motivation;
        this.Portfolio = Portfolio;
        this.Lettre_Recomendation = Lettre_Recomendation;
        this.Code_Type_Status = Code_Type_Status;
    }

    public int getID_Candidature() { return ID_Candidature; }
    public int getID_Candidat() { return ID_Candidat; }
    public int getID_Offre() { return ID_Offre; }
    public String getLettre_Motivation() { return Lettre_Motivation; }
    public String getPortfolio() { return Portfolio; }
    public String getLettre_Recomendation() { return Lettre_Recomendation; }
    public int getCode_Type_Status() { return Code_Type_Status; }

    public void setID_Condidature(int ID_Candidature) { this.ID_Candidature = ID_Candidature; }
    public void setID_Candidat(int ID_Candidat) { this.ID_Candidat = ID_Candidat; }
    public void setID_Offre(int ID_Offre) { this.ID_Offre = ID_Offre; }
    public void setLettre_Motivation(String Lettre_Motivation) { this.Lettre_Motivation = Lettre_Motivation; }
    public void setPortfolio(String Portfolio) { this.Portfolio = Portfolio; }
    public void setLettre_Recomendation(String Lettre_Recomendation) { this.Lettre_Recomendation = Lettre_Recomendation; }
    public void setCode_Type_Status(int Code_Type_Status) { this.Code_Type_Status = Code_Type_Status; }
}
