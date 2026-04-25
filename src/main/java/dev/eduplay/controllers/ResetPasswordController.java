package dev.eduplay.controllers;

import dev.eduplay.core.TokenHolder;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ResetPasswordController {

    @FXML private TextField tokenField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label messageLabel;
    @FXML private Button resetButton;
    @FXML private Label tokenHintLabel; // optionnel — affiche hint si token pré-rempli

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Vérifier si un token a été passé via TokenHolder (depuis l'email deep link)
        String pendingToken = TokenHolder.getAndClear();
        if (pendingToken != null && !pendingToken.isBlank()) {
            tokenField.setText(pendingToken);
            tokenField.setDisable(true); // token auto-rempli, pas modifiable
            if (tokenHintLabel != null) {
                tokenHintLabel.setText("Token reçu par email.");
                tokenHintLabel.setStyle("-fx-text-fill: #2E9E6E; -fx-font-size: 11px;");
            }
        }
    }

    @FXML
    private void handleReset() {
        String token    = tokenField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        // Validations
        if (token.isEmpty()) {
            showError("Veuillez entrer le code reçu par email.");
            return;
        }
        if (password.isEmpty()) {
            showError("Veuillez entrer un nouveau mot de passe.");
            return;
        }
        if (password.length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        resetButton.setDisable(true);
        showInfo("Vérification en cours...");

        new Thread(() -> {
            boolean success = userService.resetPassword(token, password);
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    showSuccess("Mot de passe modifié avec succès ! Redirection...");
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                        javafx.application.Platform.runLater(this::goToLogin);
                    }).start();
                } else {
                    showError("Code invalide ou expiré. Recommencez depuis 'Mot de passe oublié'.");
                    resetButton.setDisable(false);
                }
            });
        }).start();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/auth/LoginView.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) tokenField.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void goToForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/forgot-password.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) tokenField.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
        } catch (Exception e) {
            e.printStackTrace();
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