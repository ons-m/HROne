package com.recruitx.hrone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
<<<<<<< Updated upstream
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("FrmMesCandidatures.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),1024,768);
        String css = HelloApplication.class.getResource("FrmMesCandidatures.fx.css").toExternalForm();
=======
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("FrmCandidat.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),1024,768);
        String css = HelloApplication.class.getResource("FrmCandidat.fx.css").toExternalForm();
>>>>>>> Stashed changes
        scene.getStylesheets().add(css);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
