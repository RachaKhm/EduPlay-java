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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import dev.eduplay.utils.GoogleAuthHelper;
import com.google.api.services.oauth2.model.Userinfo;
import java.util.Set;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

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
    
    // ── Vue Confirmation Google (Overlay) ─────────────────────────────────
    @FXML private VBox googleConfirmBox;
    @FXML private Label googleEmailLabel;
    @FXML private TextField googleFirstNameField;
    @FXML private TextField googleLastNameField;

    private final UserService userService = new UserService();
    private User pendingUser;
    private Userinfo pendingGoogleUser;

    @FXML
    public void initialize() {
        // Cache les overlays au départ
        otpBox.setVisible(false);
        otpBox.setManaged(false);
        
        if (googleConfirmBox != null) {
            googleConfirmBox.setVisible(false);
            googleConfirmBox.setManaged(false);
        }
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

        // Bypass OTP pour comptes de test
        if (isTestAccount(identifier)) {
            finalizeLogin(user);
            return;
        }

        // Envoi OTP
        new Thread(() -> {
            boolean sent = userService.sendOtp(user);
            javafx.application.Platform.runLater(() -> {
                if (sent) {
                    showOtpStep();
                } else {
                    // Email non configuré → login direct
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

    @FXML
    private void handleGoogleLogin() {
        showLoginError("Ouverture de la page de connexion Google...");

        new Thread(() -> {
            try {
                Userinfo userinfo = GoogleAuthHelper.getUserInfo();
                String email = userinfo.getEmail();

                if (email == null || email.isEmpty()) {
                    javafx.application.Platform.runLater(() -> 
                        showLoginError("Impossible de récupérer l'email Google.")
                    );
                    return;
                }

                javafx.application.Platform.runLater(() -> {
                    User googleUser = userService.findByEmail(email);

                    if (googleUser != null) {
                        finalizeLogin(googleUser);
                    } else {
                        // Afficher la boîte de confirmation et pré-remplir les champs
                        pendingGoogleUser = userinfo;
                        googleEmailLabel.setText(email);
                        
                        // Pré-remplissage intelligent
                        String firstName = userinfo.getGivenName();
                        String lastName = userinfo.getFamilyName();
                        
                        if (firstName == null || firstName.isEmpty()) {
                            firstName = userinfo.getName() != null ? userinfo.getName().split(" ")[0] : "";
                        }
                        if (lastName == null || lastName.isEmpty()) {
                             String full = userinfo.getName();
                             if (full != null && full.contains(" ")) {
                                 lastName = full.substring(full.indexOf(" ") + 1);
                             } else {
                                 lastName = "";
                             }
                        }
                        
                        googleFirstNameField.setText(firstName);
                        googleLastNameField.setText(lastName);
                        
                        googleConfirmBox.setVisible(true);
                        googleConfirmBox.setManaged(true);
                        loginBox.setDisable(true);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> 
                    showLoginError("Erreur d'authentification Google : " + e.getMessage())
                );
            }
        }).start();
    }

    @FXML
    private void confirmGoogleSignup() {
        if (pendingGoogleUser == null) return;
        
        String email = pendingGoogleUser.getEmail();
        
        // Vérification de sécurité supplémentaire : l'email ne doit pas déjà exister
        if (userService.findByEmail(email) != null) {
            showLoginError("Ce compte Google est déjà associé à un utilisateur EduPlay.");
            cancelGoogleSignup();
            return;
        }

        User newUser = new User();
        newUser.setEmail(email);
        
        // Utiliser les données saisies/modifiées par l'utilisateur
        String firstName = googleFirstNameField.getText().trim();
        String lastName = googleLastNameField.getText().trim();
        
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showLoginError("Veuillez remplir votre nom et prénom.");
            return;
        }

        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setType("parent");
        newUser.setUsername(email.split("@")[0] + "_" + (int)(Math.random() * 1000));
        newUser.setActive(true);
        
        // Générer un mot de passe aléatoire sécurisé (car requis par la DB)
        String randomPassword = UUID.randomUUID().toString();
        newUser.setPassword(BCrypt.hashpw(randomPassword, BCrypt.gensalt())); 

        userService.ajouter(newUser);
        
        User createdUser = userService.findByEmail(email);
        if (createdUser != null) {
            googleConfirmBox.setVisible(false);
            googleConfirmBox.setManaged(false);
            loginBox.setDisable(false);
            finalizeLogin(createdUser);
        } else {
            showLoginError("Erreur lors de la création du compte.");
        }
    }

    @FXML
    private void cancelGoogleSignup() {
        googleConfirmBox.setVisible(false);
        googleConfirmBox.setManaged(false);
        loginBox.setDisable(false);
        pendingGoogleUser = null;
    }

    // ─── FINALISATION LOGIN ───────────────────────────────────────────────

    private void finalizeLogin(User user) {
        String token = userService.createSession(user.getId());
        SessionManager.getInstance().login(user, token);
        AppContext.setCurrentUser(user);

        String route = switch (user.getType().toLowerCase()) {
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
        navigateTo("/views/auth/ParentSignup.fxml", "/views/auth/SignupView.fxml", "/views/SignupView.fxml");
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