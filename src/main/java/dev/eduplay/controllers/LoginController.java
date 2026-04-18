package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import dev.eduplay.utils.PasswordUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;

    private final UserService userService = new UserService();

    @FXML
    public void handleLogin() {

        String login = emailField.getText().trim();
        String password = passwordField.getText();
        errorLabel.setText("");

        if (login.isBlank() || password.isBlank()) {
            showError("Remplissez tous les champs.");
            return;
        }

        User user = userService.findByLogin(login);

        if (user == null) {
            showError("Identifiants incorrects.");
            return;
        }

        if (!user.isActive()) {
            showError("Compte désactivé.");
            return;
        }

        if (!PasswordUtils.checkPassword(password, user.getPassword())) {
            showError("Mot de passe incorrect.");
            return;
        }

        AppContext.setCurrentUser(user);

        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/shared/MainView.fxml")).load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 1100, 700));
            stage.setTitle("EduPlay — " + capitalize(AppContext.getRole()));
            stage.setMinWidth(800); stage.setMinHeight(550);
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setStyle("-fx-text-fill: #E94560; -fx-font-size: 12px;");
    }

    private String capitalize(String s) {
        return (s == null || s.isBlank()) ? "" : s.substring(0,1).toUpperCase() + s.substring(1);
    }

    @FXML
    public void goToSignup() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/auth/ParentSignup.fxml")).load();
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
            stage.setTitle("EduPlay — Inscription Parent");
            stage.centerOnScreen();
        } catch (IOException e) {
            showError("Erreur navigation : " + e.getMessage());
            e.printStackTrace();
        }
    }
}