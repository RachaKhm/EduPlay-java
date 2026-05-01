package dev.eduplay.controllers;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import dev.eduplay.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ParentSignupController {

    @FXML private TextField     firstNameField;
    @FXML private TextField     lastNameField;
    @FXML private TextField     emailField;
    @FXML private TextField     phoneField;
    @FXML private TextField     addressField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label successLabel;

    private final UserService userService = new UserService();

    @FXML
    private void handleSignup() {
        clearErrors();
        boolean valid = true;

        String firstName = firstNameField.getText().trim();
        String lastName  = lastNameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();
        String address   = addressField.getText().trim();
        String password  = passwordField.getText();
        String confirm   = confirmPasswordField.getText();

        if (firstName.isBlank()) {
            setError(firstNameError, "Le prénom est requis.");
            valid = false;
        }
        if (lastName.isBlank()) {
            setError(lastNameError, "Le nom est requis.");
            valid = false;
        }
        if (email.isBlank() || !email.contains("@")) {
            setError(emailError, "Entrez un email valide.");
            valid = false;
        }
        if (password.length() < 6) {
            setError(passwordError, "Le mot de passe doit faire au moins 6 caractères.");
            valid = false;
        }
        if (!password.equals(confirm)) {
            setError(confirmPasswordError, "Les mots de passe ne correspondent pas.");
            valid = false;
        }

        if (!valid) return;

        // Vérifier si l'email est déjà utilisé
        if (userService.findByLogin(email) != null) {
            setError(emailError, "Cet email est déjà utilisé.");
            return;
        }

        User parent = new User();
        parent.setFirstName(firstName);
        parent.setLastName(lastName);
        parent.setEmail(email);
        parent.setTelephone(phone);
        parent.setAdresse(address);
        parent.setType("parent");
        parent.setActive(true);
        parent.setPassword(PasswordUtils.hashPassword(password));

        userService.ajouter(parent);

        if (successLabel != null) {
            successLabel.setStyle("-fx-text-fill: #2E9E6E; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 12 0 0 0;");
            successLabel.setText("✓ Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
        }

        // Retour à l'écran de connexion après 2 secondes
        new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(this::goToLogin);
        }).start();
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/auth/LoginView.fxml")).load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
        } catch (Exception e) {
            System.err.println("[ParentSignupController] Erreur retour login : " + e.getMessage());
        }
    }

    // ── Utils ──────────────────────────────────────────────────

    private void clearErrors() {
        Label[] labels = {firstNameError, lastNameError, emailError,
                phoneError, passwordError, confirmPasswordError};
        for (Label l : labels) {
            if (l != null) l.setText("");
        }
        if (successLabel != null) successLabel.setText("");
    }

    private void setError(Label label, String msg) {
        if (label != null) {
            label.setStyle("-fx-text-fill: #E94560; -fx-font-size: 11px;");
            label.setText(msg);
        }
    }
}
