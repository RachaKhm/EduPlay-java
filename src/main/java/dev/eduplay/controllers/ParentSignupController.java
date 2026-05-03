package dev.eduplay.controllers;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.regex.Pattern;

public class ParentSignupController {

    @FXML private TextField lastNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // Un label d'erreur sous chaque champ
    @FXML private Label lastNameError;
    @FXML private Label firstNameError;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label successLabel;

    private final UserService userService = new UserService();

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    @FXML
    public void handleSignup() {
        clearErrors();

        if (!validateInput()) {
            return;
        }

        User parent = new User();
        parent.setLastName(lastNameField.getText().trim());
        parent.setFirstName(firstNameField.getText().trim());
        parent.setEmail(emailField.getText().trim());
        parent.setTelephone(phoneField.getText().trim());
        parent.setAdresse(addressField.getText().trim());
        parent.setType("parent");
        parent.setActive(true);

        String hashedPw = BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt());
        parent.setPassword(hashedPw);

        userService.ajouter(parent);

        successLabel.setText("Compte parent créé avec succès ! Vous pouvez vous connecter.");
        clearForm();
    }

    private boolean validateInput() {
        boolean valid = true;

        // Nom
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Le nom est obligatoire.");
            valid = false;
        }

        // Prénom
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("Le prénom est obligatoire.");
            valid = false;
        }

        // Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("L'email est obligatoire.");
            valid = false;
        } else if (!Pattern.matches(EMAIL_REGEX, email)) {
            emailError.setText("Veuillez saisir une adresse email valide.");
            valid = false;
        } else if (userService.findByEmail(email) != null) {
            emailError.setText("Cette adresse email est déjà utilisée.");
            valid = false;
        }

        // Téléphone (optionnel mais vérifié si rempli)
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\+?\\d{8,15}")) {
            phoneError.setText("Numéro de téléphone invalide (8 à 15 chiffres).");
            valid = false;
        }

        // Mot de passe (min 8 caractères)
        String pw = passwordField.getText();
        if (pw == null || pw.length() < 8) {
            passwordError.setText("Le mot de passe doit contenir au moins 8 caractères.");
            valid = false;
        }

        // Confirmation
        String confirmPw = confirmPasswordField.getText();
        if (pw != null && !pw.equals(confirmPw)) {
            confirmPasswordError.setText("Les mots de passe ne correspondent pas.");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        lastNameError.setText("");
        firstNameError.setText("");
        emailError.setText("");
        phoneError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        successLabel.setText("");
    }

    private void clearForm() {
        lastNameField.clear();
        firstNameField.clear();
        emailField.clear();
        phoneField.clear();
        addressField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    public void goToLogin() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/auth/LoginView.fxml")).load();
            Stage stage = (Stage) lastNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 900, 600));
            stage.setTitle("EduPlay — Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
