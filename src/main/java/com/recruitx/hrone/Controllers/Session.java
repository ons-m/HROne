package com.recruitx.hrone.Controllers;

import com.recruitx.hrone.Models.Entreprise;
import com.recruitx.hrone.Models.Utilisateur;

public final class Session {

    private static Utilisateur currentUser;
    private static Entreprise currentEntreprise;

    private Session() {
        // Prevent instantiation
    }

    /* ===============================
       Setters
       =============================== */

    public static void setCurrentUser(Utilisateur user) {
        currentUser = user;
    }

    public static void setCurrentEntreprise(Entreprise entreprise) {
        currentEntreprise = entreprise;
    }

    /* ===============================
       Getters
       =============================== */

    public static Utilisateur getCurrentUser() {
        return currentUser;
    }

    public static Entreprise getCurrentEntreprise() {
        return currentEntreprise;
    }

    /* ===============================
       Helpers
       =============================== */

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static void clear() {
        currentUser = null;
        currentEntreprise = null;
    }
}
