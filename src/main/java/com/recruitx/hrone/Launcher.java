package com.recruitx.hrone;
import com.recruitx.hrone.utils.*;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Ensure configuration file exists (Config.ini) Before Launching the Application
        try{
            Global.ensureConfigExists();
        }catch(Exception e){
            CError.log(LogType.ERROR, "Coudn't Start the app Because the config file is missing", e);
            return;
        }

        try{
            DBConnection.getInstance();
        }catch(Exception e){
            CError.log(LogType.ERROR, "Coudn't Connect to database", e);
            return;
        }

        Application.launch(HelloApplication.class, args);
    }
}
