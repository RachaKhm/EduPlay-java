package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleSend() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            messageLabel.setText("Veuillez entrer votre email.");
            return;
        }
        messageLabel.setStyle("-fx-text-fill: gray;");
        messageLabel.setText("Envoi en cours...");

        // Exécution en arrière-plan pour ne pas bloquer l'UI
        new Thread(() -> {
            boolean sent = userService.generateResetToken(email);
            javafx.application.Platform.runLater(() -> {
                if (sent) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Email envoyé ! Vérifiez votre boîte mail.");
                    // Rediriger vers la vue reset après 2 secondes
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                        javafx.application.Platform.runLater(() -> Router.go("reset-password"));
                    }).start();
                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Aucun compte trouvé avec cet email.");
                }
            });
        }).start();
    }

    @FXML
    private void goToLogin() {
        Router.go("login");
    }
}