package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ResetPasswordController {

    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label messageLabel;
    @FXML private Button resetBtn;

    private String token;
    private final UserService userService = new UserService();
    private volatile boolean resetInProgress = false;

    /**
     * Injected by the Router. Validates token immediately.
     */
    public void setToken(String token) {
        this.token = token;
        
        // Validation immédiate du token au chargement
        resetBtn.setDisable(true);
        passwordField.setDisable(true);
        confirmField.setDisable(true);
        messageLabel.setStyle("-fx-text-fill: #4A90E2;");
        messageLabel.setText("Vérification du lien...");

        new Thread(() -> {
            boolean isValid = userService.validateResetToken(token);
            javafx.application.Platform.runLater(() -> {
                if (isValid) {
                    resetBtn.setDisable(false);
                    passwordField.setDisable(false);
                    confirmField.setDisable(false);
                    messageLabel.setText("");
                } else {
                    showError("Ce lien est invalide ou a expiré.");
                }
            });
        }).start();
    }

    @FXML
    private void handleReset() {
        if (resetInProgress) return; // Protection contre le spam click

        String password = passwordField.getText();
        String confirm  = confirmField.getText();

        if (token == null || token.isEmpty()) {
            showError("Lien manquant.");
            return;
        }
        if (password.isEmpty() || confirm.isEmpty()) {
            showError("Veuillez remplir les deux champs.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }
        if (password.length() < 8) {
            showError("Minimum 8 caractères requis.");
            return;
        }

        startLoading();

        new Thread(() -> {
            boolean success = userService.resetPassword(token, password);
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    showSuccess("✓ Mot de passe modifié ! Redirection...");
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                        javafx.application.Platform.runLater(() -> Router.go("login"));
                    }).start();
                } else {
                    stopLoading();
                    showError("Échec de la réinitialisation. Le lien n'est plus valide.");
                }
            });
        }).start();
    }

    private void startLoading() {
        resetInProgress = true;
        resetBtn.setDisable(true);
        passwordField.setDisable(true);
        confirmField.setDisable(true);
        messageLabel.setStyle("-fx-text-fill: #4A90E2;");
        messageLabel.setText("Modification en cours...");
    }

    private void stopLoading() {
        resetInProgress = false;
        resetBtn.setDisable(false);
        passwordField.setDisable(false);
        confirmField.setDisable(false);
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #E94560;");
        messageLabel.setText(msg);
    }

    private void showSuccess(String msg) {
        messageLabel.setStyle("-fx-text-fill: #2E9E6E;");
        messageLabel.setText(msg);
    }

    @FXML
    private void goToLogin() {
        Router.go("login");
    }
}
