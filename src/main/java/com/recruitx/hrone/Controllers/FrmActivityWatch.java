package com.recruitx.hrone.Controllers;

import javafx.concurrent.Task;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.Desktop;
import java.net.URI;

public class FrmActivityWatch {

    private static final String ACTIVITY_WATCH_URL = "http://localhost:5600";

    @FXML
    private StackPane contentPane;

    private WebView webView;
    private WebEngine webEngine;

    @FXML
    public void initialize() {
        checkServerAndLoad();
    }

    private boolean isServerRunning() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(3))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ACTIVITY_WATCH_URL))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();

            HttpResponse<Void> response =
                    client.send(request, HttpResponse.BodyHandlers.discarding());

            return response.statusCode() == 200;

        } catch (Exception e) {
            return false;
        }
    }

    private void checkServerAndLoad() {

        Task<Boolean> healthCheckTask = new Task<>() {
            @Override
            protected Boolean call() {
                return isServerRunning();
            }
        };

        healthCheckTask.setOnSucceeded(event -> {
            if (healthCheckTask.getValue()) {
                loadActivityWatch();
            } else {
                showError("ActivityWatch server is not running on port 5600.");
            }
        });

        healthCheckTask.setOnFailed(event ->
                showError("Unable to verify ActivityWatch server.")
        );

        new Thread(healthCheckTask).start();
    }

    private void loadActivityWatch() {

        webView = new WebView();
        webEngine = webView.getEngine();

        webEngine.load(ACTIVITY_WATCH_URL);

        webEngine.getLoadWorker().exceptionProperty().addListener((obs, oldEx, newEx) -> {
            if (newEx != null) {
                showError("ActivityWatch server is not running.");
            }
        });

        contentPane.getChildren().setAll(webView);
    }

    @FXML
    private void handleRefresh() {
        if (webEngine != null) {
            webEngine.reload();
        }
    }

    @FXML
    private void handleOpenExternal() {
        try {
            Desktop.getDesktop().browse(new URI(ACTIVITY_WATCH_URL));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        contentPane.getChildren().setAll(errorLabel);
    }
}