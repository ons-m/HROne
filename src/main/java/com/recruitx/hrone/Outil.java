package com.recruitx.hrone;

public class Outil {
    private int ID_Outil;
    private String Nom_Outil;
    private String Identifiant_Universelle;
    private String Hash_App;

    public Outil(){}
    public Outil(int ID_Outil, String Nom_Outil, String Identifiant_Universelle,String Hash_App) {
        this.ID_Outil = ID_Outil;
        this.Nom_Outil = Nom_Outil;
        this.Hash_App = Hash_App;
        this.Identifiant_Universelle = Identifiant_Universelle;
    }
    public Outil(String Nom_Outil,String Identifiant_Universelle, String Hash_App) {
        this.Nom_Outil = Nom_Outil;
        this.Hash_App = Hash_App;
        this.Identifiant_Universelle = Identifiant_Universelle;
    }
    public Outil(int ID_Outil){
       this.ID_Outil = ID_Outil;
       this.Nom_Outil = "";
       this.Hash_App = "";
       this.Identifiant_Universelle = "";
    }

    public int getID_Outil() {
        return ID_Outil;
    }
    public String getNom_Outil() {
        return Nom_Outil;
    }
    public String getHash_App() {
        return  Hash_App;
    }
    public String getIdentifiant_Universelle() {
        return  Identifiant_Universelle;
    }

    public void setNom_Outil(String nom_Outil) {
        this.Nom_Outil = nom_Outil;
    }
    public void setHash_App(String hash_App) {
        this.Hash_App = hash_App;
    }
    public void setIdentifiant_Universelle(String identifiant_Universelle) {
        this.Identifiant_Universelle = identifiant_Universelle;
    }

}
