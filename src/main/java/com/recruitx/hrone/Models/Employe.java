package com.recruitx.hrone.Models;

public class Employe {
    private int ID_Employe;
    private Utilisateur user;
    private int Solde_Conger;
    private int Nbr_Heure_De_Travail;
    private String Mac_Machine;
    private int Salaire;

    public Employe(int ID_Employe, Utilisateur user, int Solde_Conger, int Nbr_Heure_De_Travail, String Mac_Machine,int salaire) {
        this.ID_Employe = ID_Employe;
        this.user = user;
        setSolde_Conger(Solde_Conger);
        setNbr_Heure_De_Travail(Nbr_Heure_De_Travail);
        this.Mac_Machine = Mac_Machine;
        this.Salaire=salaire;
    }
    public Employe(int ID_Employe, Utilisateur user) {
        this.ID_Employe = ID_Employe;
        this.user = user;
    }
    public Employe(){}

    public int getID_Employe() {
        return ID_Employe;
    }
    public Utilisateur getUser() {
        return user;
    }
    public int getSolde_Conger() {
        return Solde_Conger;
    }
    public int getNbr_Heure_De_Travail() {
        return Nbr_Heure_De_Travail;
    }
    public String getMac_Machine() {
        return Mac_Machine;
    }
    public int getSaliare() {return Salaire;}

    public void setID_Employe(int ID_Employe) { this.ID_Employe = ID_Employe; }
    public void setSolde_Conger(int solde_Conger) {
        if(solde_Conger < 0) {
            solde_Conger = 0;
            throw new IllegalArgumentException("Solde de congé ne peut pas être négatif.");
        }
        this.Solde_Conger = solde_Conger;
    }
    public void setNbr_Heure_De_Travail(int nbr_Heure_De_Travail) {
        if(nbr_Heure_De_Travail < 0) {
            nbr_Heure_De_Travail = 0;
            throw new IllegalArgumentException("Nombre d'heures de travail ne peut pas être négatif.");
        }
        this.Nbr_Heure_De_Travail = nbr_Heure_De_Travail;
    }
    public void setMac_Machine(String mac_Machine) {
        this.Mac_Machine = mac_Machine;
    }
    public void setUser(Utilisateur user) {this.user = user;}
    public void setSalaire(int salaire) {this.Salaire = salaire; }


    @Override
    public String toString() {
        return "Employe : " +
                "ID_Employe : " + ID_Employe +
                "Conge : " + Solde_Conger +
                "Heure : " + Nbr_Heure_De_Travail +
                "MAC : " + Mac_Machine;
    }

}
