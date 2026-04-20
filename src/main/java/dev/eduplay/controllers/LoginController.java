package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.core.SessionManager;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private VBox otpBox;
    @FXML private TextField otpField;

    private final UserService userService = new UserService();
    private User pendingUser;

    @FXML
    public void initialize() {
        if (otpBox != null) {
            otpBox.setVisible(false);
            otpBox.setManaged(false);
        }
    }

    @FXML
    private void handleLogin() {
        String identifier = emailField.getText().trim();
        String password   = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        // 1. Compte verrouillé ?
        if (userService.isAccountLocked(identifier)) {
            showError("Compte temporairement bloqué. Réessayez dans 15 minutes.");
            return;
        }

        // 2. Vérifier les identifiants
        User user = userService.authenticate(identifier, password);
        if (user == null) {
            userService.recordFailedAttempt(identifier);
            showError("Identifiants incorrects.");
            return;
        }

        // 3. Identifiants OK
        pendingUser = user;
        userService.resetFailedAttempts(user.getId());

        // 🚨 Bypass OTP pour admin de test
        if ("admin@gmail.com".equalsIgnoreCase(user.getEmail())) {
            finalizeLogin(user);
            return;
        }
        // Essayer d'envoyer OTP
        new Thread(() -> {
            boolean sent = userService.sendOtp(user);
            javafx.application.Platform.runLater(() -> {
                if (sent) {
                    showOtpStep();
                } else {
                    // Email non configuré → login direct sans OTP
                    finalizeLogin(user);
                }
            });
        }).start();
    }

    @FXML
    private void handleVerifyOtp() {
        if (otpField == null || otpField.getText().trim().isEmpty()) {
            showError("Entrez le code reçu.");
            return;
        }

        boolean valid = userService.verifyOtp(pendingUser.getId(), otpField.getText().trim());
        if (!valid) {
            showError("Code invalide ou expiré.");
            return;
        }

        finalizeLogin(pendingUser);
    }

    private void finalizeLogin(User user) {
        // Créer la session
        String token = userService.createSession(user.getId());
        SessionManager.getInstance().login(user, token);
        AppContext.setCurrentUser(user);

        // Déterminer le dashboard selon le rôle
        String route = switch (user.getType().toLowerCase()) {
            case "admin"      -> "admin_dashboard";
            case "enseignant" -> "teacher_dashboard";
            case "parent"     -> "parent_dashboard";
            case "enfant"     -> "child_dashboard";
            default           -> "admin_dashboard";
        };

        // Charger MainView.fxml (qui initialise le Router)
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/MainView.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);

            // CSS optionnel
            try {
                var css = getClass().getResource("/styles/main.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();

            // Naviguer vers le bon dashboard (Router est maintenant initialisé)
            Router.go(route);

        } catch (Exception e) {
            showError("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showOtpStep() {
        emailField.setDisable(true);
        passwordField.setDisable(true);
        if (otpBox != null) {
            otpBox.setVisible(true);
            otpBox.setManaged(true);
        }
        showSuccess("Code envoyé à " + pendingUser.getEmail());
    }

    private void showError(String msg) {
        if (errorLabel != null) {
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #E94560; -fx-padding: 10 0 0 0;");
            errorLabel.setText(msg);
        }
    }

    private void showSuccess(String msg) {
        if (errorLabel != null) {
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #2E9E6E; -fx-padding: 10 0 0 0;");
            errorLabel.setText(msg);
        }
    }

    @FXML
    private void goToSignup() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/auth/SignupView.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
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
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}