package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.time.LocalDate;

public class AddChildController {

    @FXML private TextField lastNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField usernameField;
    @FXML private DatePicker birthDateField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label lastNameError;
    @FXML private Label firstNameError;
    @FXML private Label usernameError;
    @FXML private Label birthDateError;
    @FXML private Label niveauError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label successLabel;

    @FXML private Label formTitleLabel;
    @FXML private Button submitButton;

    private final UserService userService = new UserService();
    private User editingChild = null;

    @FXML
    public void initialize() {
        niveauCombo.getItems().addAll(
                "Maternelle", "1ère année", "2ème année", "3ème année",
                "4ème année", "5ème année", "6ème année"
        );

        User childToEdit = ChildrenListController.getChildToEdit();
        if (childToEdit != null) {
            editingChild = childToEdit;
            ChildrenListController.clearChildToEdit();
            populateForm(editingChild);

            if (formTitleLabel != null) formTitleLabel.setText("Modifier l'enfant");
            if (submitButton != null)   submitButton.setText("Enregistrer les modifications");

            passwordField.setPromptText("Laisser vide pour ne pas changer");
            confirmPasswordField.setPromptText("Laisser vide pour ne pas changer");
        }
    }

    private void populateForm(User child) {
        lastNameField.setText(child.getLastName()   != null ? child.getLastName()   : "");
        firstNameField.setText(child.getFirstName() != null ? child.getFirstName()  : "");
        usernameField.setText(child.getUsername()   != null ? child.getUsername()   : "");
        birthDateField.setValue(child.getBirthDate());
        niveauCombo.setValue(child.getNiveau());
    }

    @FXML
    public void handleAddChild() {
        clearErrors();
        if (!validateInput()) return;

        if (editingChild != null) {
            // Mode modification
            editingChild.setLastName(lastNameField.getText().trim());
            editingChild.setFirstName(firstNameField.getText().trim());
            editingChild.setUsername(usernameField.getText().trim());
            editingChild.setBirthDate(birthDateField.getValue());
            editingChild.setNiveau(niveauCombo.getValue());

            String pw = passwordField.getText();
            if (pw != null && !pw.isBlank()) {
                editingChild.setPassword(BCrypt.hashpw(pw, BCrypt.gensalt()));
            }

            userService.modifier(editingChild);
            successLabel.setText("Enfant modifié avec succès !");

        } else {
            // Mode création
            User child = new User();
            child.setLastName(lastNameField.getText().trim());
            child.setFirstName(firstNameField.getText().trim());
            child.setUsername(usernameField.getText().trim());
            child.setBirthDate(birthDateField.getValue());
            child.setNiveau(niveauCombo.getValue());
            child.setType("enfant");
            child.setActive(true);
            child.setPassword(BCrypt.hashpw(passwordField.getText(), BCrypt.gensalt()));

            if (AppContext.getCurrentUser() != null) {
                child.setParentId(AppContext.getCurrentUser().getId());
            }

            userService.ajouter(child);
            successLabel.setText("Compte enfant ajouté avec succès !");
            clearForm();
        }

        // Retourner à la liste après 1 seconde
        javafx.animation.PauseTransition pause =
                new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> Router.reload("parent_children"));
        pause.play();
    }

    private boolean validateInput() {
        boolean valid = true;

        if (lastNameField.getText().trim().isEmpty()) {
            lastNameError.setText("Le nom est obligatoire.");
            valid = false;
        }
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameError.setText("Le prénom est obligatoire.");
            valid = false;
        }

        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            usernameError.setText("L'identifiant est obligatoire.");
            valid = false;
        } else if (username.length() < 3) {
            usernameError.setText("L'identifiant doit contenir au moins 3 caractères.");
            valid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            usernameError.setText("Lettres, chiffres et underscores uniquement.");
            valid = false;
        } else {
            User existing = userService.findByLogin(username);
            if (existing != null && (editingChild == null || existing.getId() != editingChild.getId())) {
                usernameError.setText("Cet identifiant est déjà utilisé.");
                valid = false;
            }
        }

        if (birthDateField.getValue() == null) {
            birthDateError.setText("La date de naissance est obligatoire.");
            valid = false;
        } else if (birthDateField.getValue().isAfter(LocalDate.now())) {
            birthDateError.setText("La date ne peut pas être dans le futur.");
            valid = false;
        }

        if (niveauCombo.getValue() == null) {
            niveauError.setText("Veuillez sélectionner un niveau scolaire.");
            valid = false;
        }

        String pw      = passwordField.getText();
        String confirmPw = confirmPasswordField.getText();

        if (editingChild == null) {
            if (pw == null || pw.length() < 6) {
                passwordError.setText("Le mot de passe doit contenir au moins 6 caractères.");
                valid = false;
            }
            if (pw != null && !pw.equals(confirmPw)) {
                confirmPasswordError.setText("Les mots de passe ne correspondent pas.");
                valid = false;
            }
        } else {
            if (pw != null && !pw.isBlank()) {
                if (pw.length() < 6) {
                    passwordError.setText("Le mot de passe doit contenir au moins 6 caractères.");
                    valid = false;
                }
                if (!pw.equals(confirmPw)) {
                    confirmPasswordError.setText("Les mots de passe ne correspondent pas.");
                    valid = false;
                }
            }
        }

        return valid;
    }

    private void clearErrors() {
        lastNameError.setText("");
        firstNameError.setText("");
        usernameError.setText("");
        birthDateError.setText("");
        niveauError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        successLabel.setText("");
    }

    private void clearForm() {
        lastNameField.clear();
        firstNameField.clear();
        usernameField.clear();
        birthDateField.setValue(null);
        niveauCombo.setValue(null);
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    public void handleCancel() {
        ChildrenListController.clearChildToEdit();
        Router.go("parent_children");
    }
}