package com.recruitx.hrone;
<<<<<<< HEAD
=======
import com.recruitx.hrone.utils.*;
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
<<<<<<< HEAD
        System.out.println("🚀 Démarrage de HR One - Forum collaboratif");
        System.out.println("✅ Lancement de l'application...");

        try {
            Application.launch(HelloApplication.class, args);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du lancement : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
=======
        // Ensure configuration file exists (Config.ini) Before Launching the Application
        try{
            Global.ensureConfigExists();
        }catch(Exception e){
            CError.log(LogType.ERROR, "Coudn't Start the app", e);
            return;
        }

        Application.launch(HelloApplication.class, args);
    }
}
>>>>>>> 1dcf51ffc0dfa31816c3027349d427002b209857
