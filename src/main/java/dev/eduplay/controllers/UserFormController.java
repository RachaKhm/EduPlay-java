package dev.eduplay.controllers;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class UserFormController {

    // Champs communs (tous les types)
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private CheckBox activeCheck;
    @FXML private Label feedbackLabel;

    // Champs conditionnels
    @FXML private HBox specialiteBox;   // visible uniquement pour enseignant
    @FXML private TextField specialiteField;
    @FXML private HBox niveauBox;       // visible uniquement pour enfant
    @FXML private TextField niveauField;
    @FXML private TextField usernameField;

    private final UserService userService = new UserService();
    private User editingUser = null; // null = mode création, sinon = édition

    public void initialize() {
        typeCombo.getItems().addAll("admin", "enseignant", "parent", "enfant");
        typeCombo.valueProperty().addListener((obs, old, newType) -> updateFieldVisibility(newType));

        // Masquer les champs conditionnels par défaut
        specialiteBox.setVisible(false);
        specialiteBox.setManaged(false);
        niveauBox.setVisible(false);
        niveauBox.setManaged(false);
    }

    // Appelé depuis UserListController pour en mode édition
    public void setUser(User user) {
        this.editingUser = user;
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        telephoneField.setText(user.getTelephone());
        adresseField.setText(user.getAdresse());
        typeCombo.setValue(user.getType());
        activeCheck.setSelected(user.isActive());
        specialiteField.setText(user.getSpecialite());
        niveauField.setText(user.getNiveau());
        passwordField.setPromptText("Laisser vide pour ne pas changer");
    }

    @FXML
    public void saveUser() {
        if (!validate()) return;

        User u = (editingUser != null) ? editingUser : new User();
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setTelephone(telephoneField.getText().trim());
        u.setAdresse(adresseField.getText().trim());
        u.setType(typeCombo.getValue());
        u.setActive(activeCheck.isSelected());

        // Password : ne changer que si saisi
        String pwd = passwordField.getText();
        if (!pwd.isBlank()) {
            // En prod : BCrypt.hashpw(pwd, BCrypt.gensalt())
            u.setPassword(pwd);
        }

        if ("enseignant".equals(u.getType())) {
            u.setSpecialite(specialiteField.getText().trim());
        }
        if ("enfant".equals(u.getType())) {
            u.setNiveau(niveauField.getText().trim());
        }

        if (editingUser != null) {
            userService.modifier(u);
            feedbackLabel.setText("Utilisateur modifié avec succès.");
        } else {
            userService.ajouter(u);
            feedbackLabel.setText("Utilisateur créé avec succès.");
            clearForm();
        }
    }

    private boolean validate() {
        if (firstNameField.getText().isBlank()) {
            feedbackLabel.setText("Le prénom est obligatoire.");
            return false;
        }
        if (lastNameField.getText().isBlank()) {
            feedbackLabel.setText("Le nom est obligatoire.");
            return false;
        }
        if (typeCombo.getValue() == null) {
            feedbackLabel.setText("Sélectionnez un type.");
            return false;
        }
        if (editingUser == null && passwordField.getText().isBlank()) {
            feedbackLabel.setText("Le mot de passe est obligatoire à la création.");
            return false;
        }
        return true;
    }

    private void updateFieldVisibility(String type) {
        boolean isTeacher = "enseignant".equals(type);
        boolean isChild = "enfant".equals(type);
        specialiteBox.setVisible(isTeacher);
        specialiteBox.setManaged(isTeacher);
        niveauBox.setVisible(isChild);
        niveauBox.setManaged(isChild);
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        telephoneField.clear();
        adresseField.clear();
        passwordField.clear();
        typeCombo.setValue(null);
        feedbackLabel.setText("");
    }
}