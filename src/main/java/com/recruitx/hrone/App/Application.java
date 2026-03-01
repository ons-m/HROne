package com.recruitx.hrone.App;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) throws IOException {
        URL FrmMainUrl = Application.class.getResource("../View/FrmMain.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(FrmMainUrl);
        Scene scene = new Scene(fxmlLoader.load(),1024,600);
        String css = Application.class.getResource("../Css/FrmMain.fx.css").toExternalForm();
        scene.getStylesheets().add(css);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

    }
}
