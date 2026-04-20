package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ResetPasswordController {

    @FXML private TextField tokenField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleReset() {
        String token    = tokenField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        if (token.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Tous les champs sont obligatoires.");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("Les mots de passe ne correspondent pas.");
            return;
        }
        if (password.length() < 8) {
            messageLabel.setText("Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }

        boolean success = userService.resetPassword(token, password);
        if (success) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Mot de passe modifié ! Redirection...");
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                javafx.application.Platform.runLater(() -> Router.go("login"));
            }).start();
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Code invalide ou expiré.");
        }
    }
}