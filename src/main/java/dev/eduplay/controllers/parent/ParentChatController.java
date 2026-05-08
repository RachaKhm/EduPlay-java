package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.services.ChatService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParentChatController {

    @FXML private ListView<String> messagesList;
    @FXML private TextField inputField;

    private final ChatService chatService = new ChatService();
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    @FXML
    public void initialize() {
        messagesList.getItems().add("Assistant: Bonjour ! Je peux vous aider avec vos commandes, paiements et questions générales.");
    }

    @FXML
    public void handleSend() {
        String q = inputField.getText();
        if (q == null || q.isBlank()) return;
        messagesList.getItems().add("Vous: " + q);
        inputField.clear();

        // async
        exec.submit(() -> {
            try {
                String reply = chatService.ask(q);
                Platform.runLater(() -> messagesList.getItems().add("Assistant: " + reply));
            } catch (Exception e) {
                Platform.runLater(() -> messagesList.getItems().add("Assistant: Désolé, erreur: " + e.getMessage()));
            }
        });
    }

    @FXML
    public void handleBack() { Router.go("parent_dashboard"); }
}

