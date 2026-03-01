package com.recruitx.hrone.controller;

import com.recruitx.hrone.api.ChatbotAPI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ChatbotController {

    @FXML
    private VBox chatContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private TextField messageField;

    private final ChatbotAPI chatService = new ChatbotAPI();

    @FXML
    public void initialize() {
        // Message d'accueil
        addMessage("Bonjour ! Je suis l'assistant RH. Comment puis-je vous aider ?", false);

        // Auto-scroll
        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }

    @FXML
    private void sendMessage() {
        String message = messageField.getText().trim();
        if (message.isEmpty())
            return;

        addMessage(message, true);
        messageField.clear();

        // Ajout d'un indicateur de chargement
        HBox loadingBox = addMessage("Gardez la ligne, je recherche les informations...", false);

        chatService.getResponse(message).thenAccept(response -> {
            Platform.runLater(() -> {
                chatContainer.getChildren().remove(loadingBox);
                addMessage(response, false);
            });
        });
    }

    private HBox addMessage(String text, boolean isUser) {
        HBox box = new HBox();
        box.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        Label label = new Label(text);
        label.setWrapText(true);
        label.setMaxWidth(280);

        String style = isUser
                ? "-fx-background-color: #16a34a; -fx-text-fill: white; -fx-padding: 8 12; -fx-background-radius: 15 15 2 15;"
                : "-fx-background-color: #f1f5f9; -fx-text-fill: #1e293b; -fx-padding: 8 12; -fx-background-radius: 15 15 15 2;";

        label.setStyle(style);
        box.getChildren().add(label);
        chatContainer.getChildren().add(box);
        return box;
    }

    @FXML
    private void closeChat() {
        Stage stage = (Stage) chatContainer.getScene().getWindow();
        stage.close();
    }
}
