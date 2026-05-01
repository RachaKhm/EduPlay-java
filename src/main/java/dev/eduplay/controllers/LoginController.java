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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.Set;

public class LoginController {

    // ── Comptes de test : bypass OTP ──────────────────────────────────────
    private static final Set<String> TEST_ACCOUNTS = Set.of(
            "admin@gmail.com", "parent@gmail.com", "teacher@gmail.com",
            "child", "admin", "parent", "teacher"
    );

    // ── Vue login normale ─────────────────────────────────────────────────
    @FXML private HBox loginBox;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    // ── Vue OTP (superposée) ──────────────────────────────────────────────
    @FXML private HBox otpBox;
    @FXML private TextField otpField;
    @FXML private Label otpInfoLabel;
    @FXML private Label otpErrorLabel;

    private final UserService userService = new UserService();
    private User pendingUser;

    @FXML
    public void initialize() {
        // OTP caché au départ
        otpBox.setVisible(false);
        otpBox.setManaged(false);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────

    @FXML
    private void handleLogin() {
        String identifier = emailField.getText().trim();
        String password   = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            showLoginError("Veuillez remplir tous les champs.");
            return;
        }

        if (userService.isAccountLocked(identifier)) {
            showLoginError("Compte temporairement bloqué. Réessayez dans 15 minutes.");
            return;
        }

        User user = userService.authenticate(identifier, password);
        if (user == null) {
            userService.recordFailedAttempt(identifier);
            showLoginError("Identifiants incorrects.");
            return;
        }

        pendingUser = user;
        userService.resetFailedAttempts(user.getId());

        // Bypass OTP pour comptes de test ou si email non configuré
        if (isTestAccount(identifier) || !dev.eduplay.services.EmailService.isConfigured()) {
            finalizeLogin(user);
            return;
        }

        // Feedback utilisateur pendant l'envoi de l'OTP
        errorLabel.setStyle("-fx-text-fill: #667eea;");
        errorLabel.setText("Envoi du code de vérification en cours...");
        emailField.setDisable(true);
        passwordField.setDisable(true);

        // Envoi OTP
        new Thread(() -> {
            boolean sent = userService.sendOtp(user);
            javafx.application.Platform.runLater(() -> {
                emailField.setDisable(false);
                passwordField.setDisable(false);
                if (sent) {
                    showOtpStep();
                } else {
                    // Email échoué → login direct
                    finalizeLogin(user);
                }
            });
        }).start();
    }

    // ─── OTP ──────────────────────────────────────────────────────────────

    @FXML
    private void handleVerifyOtp() {
        String otp = otpField.getText().trim();
        if (otp.isEmpty()) {
            otpErrorLabel.setText("Entrez le code reçu.");
            return;
        }

        boolean valid = userService.verifyOtp(pendingUser.getId(), otp);
        if (!valid) {
            otpErrorLabel.setStyle("-fx-text-fill: #E94560; -fx-font-size: 12px;");
            otpErrorLabel.setText("Code invalide ou expiré.");
            return;
        }

        finalizeLogin(pendingUser);
    }

    @FXML
    private void backToLogin() {
        otpBox.setVisible(false);
        otpBox.setManaged(false);
        loginBox.setVisible(true);
        loginBox.setManaged(true);
        otpField.clear();
        otpErrorLabel.setText("");
        emailField.setDisable(false);
        passwordField.setDisable(false);
        pendingUser = null;
        errorLabel.setText("");
    }

    private void showOtpStep() {
        loginBox.setVisible(false);
        loginBox.setManaged(false);
        otpBox.setVisible(true);
        otpBox.setManaged(true);
        if (otpInfoLabel != null) {
            otpInfoLabel.setText("Code envoyé à " + pendingUser.getEmail());
        }
        otpField.requestFocus();
    }

    // ─── WEBCAM ───────────────────────────────────────────────────────────

    @FXML
    private void goToFaceLogin() {
        navigateTo("/views/face-login.fxml", "/views/auth/face-login.fxml");
    }

    // ─── FINALISATION LOGIN ───────────────────────────────────────────────

    private void finalizeLogin(User user) {
        String token = userService.createSession(user.getId());
        SessionManager.getInstance().login(user, token);
        AppContext.setCurrentUser(user);

        String userType = user.getType() != null ? user.getType().toLowerCase().trim() : "unknown";
        String route = switch (userType) {
            case "admin"      -> "admin_dashboard";
            case "enseignant" -> "teacher_dashboard";
            case "parent"     -> "parent_dashboard";
            case "enfant"     -> "child_dashboard";
            default           -> "admin_dashboard";
        };

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/shared/MainView.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);

            try {
                var css = getClass().getResource("/styles/app.css");
                if (css == null) css = getClass().getResource("/styles/main.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}

            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            Router.go(route);

        } catch (Exception e) {
            showLoginError("Erreur lors du chargement : " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── NAVIGATION ───────────────────────────────────────────────────────

    @FXML
    private void goToSignup() {
        navigateTo("/views/auth/ParentSignup.fxml");
    }

    @FXML
    private void goToForgotPassword() {
        navigateTo("/views/forgot-password.fxml", "/views/auth/forgot-password.fxml");
    }

    /**
     * Essaie les chemins dans l'ordre jusqu'à en trouver un valide.
     */
    private void navigateTo(String... paths) {
        for (String path : paths) {
            var url = getClass().getResource(path);
            if (url != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setScene(new Scene(root, 860, 540));
                    return;
                } catch (Exception e) {
                    System.err.println("[LoginController] Erreur " + path + " : " + e.getMessage());
                }
            }
        }
        showLoginError("Vue introuvable. Vérifiez la console.");
    }

    // ─── UTILS ────────────────────────────────────────────────────────────

    private boolean isTestAccount(String identifier) {
        if (TEST_ACCOUNTS.contains(identifier.toLowerCase())) return true;
        if (pendingUser != null) {
            String email    = pendingUser.getEmail()    != null ? pendingUser.getEmail().toLowerCase()    : "";
            String username = pendingUser.getUsername() != null ? pendingUser.getUsername().toLowerCase() : "";
            return TEST_ACCOUNTS.contains(email) || TEST_ACCOUNTS.contains(username);
        }
        return false;
    }

    private void showLoginError(String msg) {
        if (errorLabel != null) {
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #E94560; -fx-padding: 8 0 0 0;");
            errorLabel.setText(msg);
        }
    }
}