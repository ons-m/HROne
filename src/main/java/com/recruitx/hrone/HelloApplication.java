package com.recruitx.hrone;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("GUI/FrmGestionEmployee.fxml"));
        Scene scene = new Scene(fxmlLoader.load(),1024,600);
        String css = HelloApplication.class.getResource("CSS/FrmGestionEmployee.fx.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
