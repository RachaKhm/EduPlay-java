package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.mindrot.jbcrypt.BCrypt;

public class UserFormController {

    // Champs communs
    @FXML private Label formTitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private DatePicker birthDateField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private CheckBox activeCheck;

    // Champs conditionnels
    @FXML private HBox specialiteBox;
    @FXML private TextField specialiteField;
    @FXML private HBox niveauBox;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private TextField usernameField;

    // Labels d'erreur sous chaque champ
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label typeError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label specialiteError;
    @FXML private Label niveauError;
    @FXML private Label usernameError;
    @FXML private Label successLabel;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    private final UserService userService = new UserService();
    private User editingUser = null;

    public void initialize() {
        typeCombo.getItems().addAll("admin", "enseignant", "parent", "enfant");
        typeCombo.valueProperty().addListener((obs, old, newType) -> updateFieldVisibility(newType));

        if (niveauCombo != null) {
            niveauCombo.getItems().addAll("Maternelle", "1ère année", "2ème année", "3ème année", "4ème année", "5ème année", "6ème année");
        }

        specialiteBox.setVisible(false);
        specialiteBox.setManaged(false);
        niveauBox.setVisible(false);
        niveauBox.setManaged(false);

        // Vérifier si on est en mode édition
        User userToEdit = UserListController.getUserToEdit();
        if (userToEdit != null) {
            UserListController.clearUserToEdit();
            setUser(userToEdit);
        }
    }

    public void setUser(User user) {
        this.editingUser = user;
        if (formTitle != null) formTitle.setText("Modifier — " + user.getFullName());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        emailField.setText(user.getEmail());
        telephoneField.setText(user.getTelephone());
        adresseField.setText(user.getAdresse());
        birthDateField.setValue(user.getBirthDate());
        typeCombo.setValue(user.getType());
        typeCombo.setDisable(true);
        activeCheck.setSelected(user.isActive());
        if (specialiteField != null) specialiteField.setText(user.getSpecialite());
        if (niveauCombo != null) niveauCombo.setValue(user.getNiveau());
        if (usernameField != null) usernameField.setText(user.getUsername());
        passwordField.setPromptText("Laisser vide pour ne pas changer");
        confirmPasswordField.setPromptText("Laisser vide pour ne pas changer");
    }

    @FXML
    public void saveUser() {
        clearErrors();
        if (!validate()) return;

        User u = (editingUser != null) ? editingUser : new User();
        u.setFirstName(firstNameField.getText().trim());
        u.setLastName(lastNameField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setTelephone(telephoneField.getText().trim());
        u.setAdresse(adresseField.getText().trim());
        u.setBirthDate(birthDateField.getValue());
        u.setType(typeCombo.getValue());
        u.setActive(activeCheck.isSelected());

        String pwd = passwordField.getText();
        if (pwd != null && !pwd.isBlank()) {
            u.setPassword(BCrypt.hashpw(pwd, BCrypt.gensalt()));
        }

        if ("enseignant".equals(u.getType())) {
            u.setSpecialite(specialiteField != null ? specialiteField.getText().trim() : "");
        }
        if ("enfant".equals(u.getType())) {
            u.setNiveau(niveauCombo != null && niveauCombo.getValue() != null ? niveauCombo.getValue() : "");
            u.setUsername(usernameField != null ? usernameField.getText().trim() : "");
        }

        if (editingUser != null) {
            userService.modifier(u);
            successLabel.setText("Utilisateur modifié avec succès.");
        } else {
            userService.ajouter(u);
            successLabel.setText("Utilisateur créé avec succès.");
        }

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
        pause.setOnFinished(e -> Router.reload("users"));
        pause.play();
    }

    @FXML
    public void closeForm() {
        UserListController.clearUserToEdit();
        Router.go("users");
    }

    private boolean validate() {
        boolean valid = true;

        // Prénom
        if (firstNameField.getText().isBlank()) {
            firstNameError.setText("Le prénom est obligatoire.");
            valid = false;
        }

        // Nom
        if (lastNameField.getText().isBlank()) {
            lastNameError.setText("Le nom est obligatoire.");
            valid = false;
        }

        // Type
        String type = typeCombo.getValue();
        if (type == null) {
            typeError.setText("Sélectionnez un type.");
            valid = false;
        }

        // Email obligatoire pour admin/enseignant/parent
        if (type != null && !"enfant".equals(type)) {
            if (emailField.getText() == null || emailField.getText().isBlank()) {
                emailError.setText("L'email est obligatoire pour ce type.");
                valid = false;
            }
        }
        if (emailField.getText() != null && !emailField.getText().isBlank()
                && !emailField.getText().matches(EMAIL_REGEX)) {
            emailError.setText("Veuillez saisir un email valide.");
            valid = false;
        }

        // Téléphone
        if (telephoneField.getText() != null && !telephoneField.getText().isBlank()
                && !telephoneField.getText().matches("\\+?\\d{8,15}")) {
            telephoneError.setText("Numéro invalide (8 à 15 chiffres).");
            valid = false;
        }

        // Spécialité obligatoire pour enseignant
        if ("enseignant".equals(type) && (specialiteField == null || specialiteField.getText().isBlank())) {
            if (specialiteError != null) specialiteError.setText("La spécialité est obligatoire.");
            valid = false;
        }

        // Enfant : username + niveau
        if ("enfant".equals(type)) {
            if (usernameField == null || usernameField.getText() == null || usernameField.getText().isBlank()) {
                if (usernameError != null) usernameError.setText("L'identifiant est obligatoire.");
                valid = false;
            } else if (usernameField.getText().trim().length() < 3) {
                if (usernameError != null) usernameError.setText("Minimum 3 caractères.");
                valid = false;
            } else if (!usernameField.getText().trim().matches("^[a-zA-Z0-9_]+$")) {
                if (usernameError != null) usernameError.setText("Lettres, chiffres et _ uniquement.");
                valid = false;
            }

            if (niveauCombo != null && niveauCombo.getValue() == null) {
                if (niveauError != null) niveauError.setText("Sélectionnez un niveau.");
                valid = false;
            }
        }

        // Mot de passe
        String pw = passwordField.getText();
        String confirmPw = confirmPasswordField.getText();

        if (editingUser == null) {
            // Création : obligatoire
            if (pw == null || pw.isBlank() || pw.length() < 6) {
                passwordError.setText("Minimum 6 caractères requis.");
                valid = false;
            }
            if (pw != null && !pw.equals(confirmPw)) {
                confirmPasswordError.setText("Les mots de passe ne correspondent pas.");
                valid = false;
            }
        } else if (pw != null && !pw.isBlank()) {
            // Édition : valider seulement si rempli
            if (pw.length() < 6) {
                passwordError.setText("Minimum 6 caractères requis.");
                valid = false;
            }
            if (!pw.equals(confirmPw)) {
                confirmPasswordError.setText("Les mots de passe ne correspondent pas.");
                valid = false;
            }
        }

        return valid;
    }

    private void clearErrors() {
        firstNameError.setText("");
        lastNameError.setText("");
        emailError.setText("");
        telephoneError.setText("");
        typeError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        successLabel.setText("");
        if (specialiteError != null) specialiteError.setText("");
        if (niveauError != null) niveauError.setText("");
        if (usernameError != null) usernameError.setText("");
    }

    private void updateFieldVisibility(String type) {
        boolean isTeacher = "enseignant".equals(type);
        boolean isChild = "enfant".equals(type);
        specialiteBox.setVisible(isTeacher);
        specialiteBox.setManaged(isTeacher);
        niveauBox.setVisible(isChild);
        niveauBox.setManaged(isChild);
    }
}