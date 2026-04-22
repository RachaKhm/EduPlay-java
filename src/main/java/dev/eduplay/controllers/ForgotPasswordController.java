package dev.eduplay.controllers;

import dev.eduplay.core.TokenHolder;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private Label messageLabel;
    @FXML private Button sendButton;

    private final UserService userService = new UserService();

    // Chemins possibles — on teste dans l'ordre jusqu'à trouver
    private static final String[] RESET_PATHS = {
            "/views/reset-password.fxml",
            "/views/auth/reset-password.fxml",
            "/views/auth/ResetPasswordView.fxml",
            "/views/shared/reset-password.fxml",
            "/reset-password.fxml",
    };

    private static final String[] LOGIN_PATHS = {
            "/views/auth/LoginView.fxml",
            "/views/LoginView.fxml",
            "/views/auth/login.fxml",
    };

    @FXML
    private void handleSend() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre email.");
            return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Format d'email invalide.");
            return;
        }

        sendButton.setDisable(true);
        showInfo("Envoi en cours...");

        new Thread(() -> {
            boolean sent = userService.generateResetToken(email);
            javafx.application.Platform.runLater(() -> {
                if (sent) {
                    showSuccess("Code envoyé ! Vérifiez votre boîte mail.");
                    new Thread(() -> {
                        try { Thread.sleep(2500); } catch (InterruptedException ignored) {}
                        javafx.application.Platform.runLater(this::goToResetPassword);
                    }).start();
                } else {
                    showError("Aucun compte trouvé avec cet email.");
                    sendButton.setDisable(false);
                }
            });
        }).start();
    }

    void goToResetPassword() {
        for (String path : RESET_PATHS) {
            var url = getClass().getResource(path);
            if (url != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(new Scene(root, 860, 540));
                    System.out.println("[ForgotPwd] OK → " + path);
                    return;
                } catch (Exception e) {
                    System.err.println("[ForgotPwd] Echec " + path + " : " + e.getMessage());
                }
            } else {
                System.err.println("[ForgotPwd] Introuvable : " + path);
            }
        }
        showError("Fichier reset-password.fxml introuvable. Voir console pour détails.");
        sendButton.setDisable(false);
    }

    @FXML
    private void goToLogin() {
        for (String path : LOGIN_PATHS) {
            var url = getClass().getResource(path);
            if (url != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(new Scene(root, 860, 540));
                    return;
                } catch (Exception e) {
                    System.err.println("[ForgotPwd] Echec login " + path);
                }
            }
        }
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #E94560; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }
    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2E9E6E; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }
    private void showInfo(String msg) {
        messageLabel.setStyle("-fx-text-fill: #555577; -fx-font-size: 12px;");
        messageLabel.setText(msg);
    }
}