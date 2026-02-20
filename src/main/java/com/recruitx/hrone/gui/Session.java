package com.recruitx.hrone.gui;

import com.recruitx.hrone.entities.Utilisateur;

public class Session {
    private static Utilisateur loggedUser;

    public static Utilisateur getLoggedUser() {
        return loggedUser;
    }

    public static void setLoggedUser(Utilisateur user) {
        loggedUser = user;
    }
}
