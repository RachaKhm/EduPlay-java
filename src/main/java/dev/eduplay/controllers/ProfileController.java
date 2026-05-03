package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.User;
import dev.eduplay.services.CloudinaryService;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;

public class ProfileController {

    @FXML private StackPane avatarContainer;
    @FXML private ImageView profileImageView;
    @FXML private Label initialsLabel;
    @FXML private Label roleBadge;
    @FXML private Label fullNameLabel;
    @FXML private Label uploadStatusLabel;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private DatePicker birthDateField;

    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // Conditionnel Enfant
    @FXML private HBox enfantBox;
    @FXML private TextField usernameField;
    @FXML private ComboBox<String> niveauCombo;

    // Conditionnel Enseignant
    @FXML private HBox enseignantBox;
    @FXML private TextField specialiteField;

    // Erreurs
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;
    @FXML private Label emailError;
    @FXML private Label telephoneError;
    @FXML private Label oldPasswordError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label specialiteError;
    @FXML private Label usernameError;
    @FXML private Label successLabel;

    private final UserService userService = new UserService();
    private User currentUser;

    @FXML
    public void initialize() {
        if (niveauCombo != null) {
            niveauCombo.getItems().addAll(
                    "Maternelle", "1ère année", "2ème année", "3ème année",
                    "4ème année", "5ème année", "6ème année"
            );
        }

        enfantBox.setVisible(false);
        enfantBox.setManaged(false);
        enseignantBox.setVisible(false);
        enseignantBox.setManaged(false);

        if (profileImageView != null) {
            profileImageView.setVisible(false);
            profileImageView.setManaged(false);
        }

        currentUser = AppContext.getCurrentUser();
        if (currentUser != null) {
            loadUserData();
        }
    }

    private void loadUserData() {
        // En-tête : initiales
        String first = currentUser.getFirstName() != null ? currentUser.getFirstName() : "";
        String last  = currentUser.getLastName()  != null ? currentUser.getLastName()  : "";

        String initials = "";
        if (!first.isEmpty()) initials += first.substring(0, 1).toUpperCase();
        if (!last.isEmpty())  initials += last.substring(0, 1).toUpperCase();
        if (initialsLabel != null) initialsLabel.setText(initials);

        if (fullNameLabel != null) fullNameLabel.setText(currentUser.getFullName());

        String type = currentUser.getType() != null ? currentUser.getType() : "user";
        if (roleBadge != null) {
            roleBadge.setText(type.toUpperCase());
            roleBadge.setStyle(
                    "-fx-background-color: " + getRoleColor(type) + ";" +
                            "-fx-text-fill: white; -fx-padding: 4 10;" +
                            "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;"
            );
        }

        // Champs communs
        firstNameField.setText(first);
        lastNameField.setText(last);
        emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        telephoneField.setText(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        adresseField.setText(currentUser.getAdresse() != null ? currentUser.getAdresse() : "");
        birthDateField.setValue(currentUser.getBirthDate());

        // Champs conditionnels
        if ("enfant".equals(type)) {
            enfantBox.setVisible(true);
            enfantBox.setManaged(true);
            if (usernameField != null) usernameField.setText(currentUser.getUsername());
            if (niveauCombo != null)   niveauCombo.setValue(currentUser.getNiveau());
            emailField.setDisable(true);
        } else if ("enseignant".equals(type)) {
            enseignantBox.setVisible(true);
            enseignantBox.setManaged(true);
            if (specialiteField != null) specialiteField.setText(currentUser.getSpecialite());
        }

        // Photo de profil Cloudinary
        String photoUrl = userService.getProfilePicture(currentUser.getId());
        if (photoUrl != null && !photoUrl.isBlank()) {
            showProfileImage(photoUrl);
        } else {
            showInitials();
        }
    }

    // ─── PHOTO DE PROFIL ──────────────────────────────────────────────────────

    @FXML
    private void handleChangePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une photo de profil");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );

        File file = fileChooser.showOpenDialog(firstNameField.getScene().getWindow());
        if (file == null) return;

        setUploadStatus("Upload en cours...", "gray");

        new Thread(() -> {
            try {
                String url = CloudinaryService.uploadProfilePicture(file);
                userService.updateProfilePicture(currentUser.getId(), url);
                currentUser.setProfilePicture(url);
                AppContext.setCurrentUser(currentUser);

                javafx.application.Platform.runLater(() -> {
                    showProfileImage(url);
                    setUploadStatus("Photo mise à jour ✓", "#2E9E6E");
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        setUploadStatus("Erreur lors de l'upload.", "#E94560")
                );
                e.printStackTrace();
            }
        }).start();
    }

    private void showProfileImage(String url) {
        if (profileImageView == null) return;
        Circle clip = new Circle(40, 40, 40);
        profileImageView.setClip(clip);
        profileImageView.setImage(new Image(url, true));
        profileImageView.setVisible(true);
        profileImageView.setManaged(true);
        if (initialsLabel != null) initialsLabel.setVisible(false);
    }

    private void showInitials() {
        if (profileImageView != null) {
            profileImageView.setVisible(false);
            profileImageView.setManaged(false);
        }
        if (initialsLabel != null) initialsLabel.setVisible(true);
    }

    private void setUploadStatus(String msg, String color) {
        if (uploadStatusLabel != null) {
            uploadStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + color + ";");
            uploadStatusLabel.setText(msg);
        }
    }

    // ─── SAUVEGARDE PROFIL ────────────────────────────────────────────────────

    @FXML
    public void saveProfile() {
        clearErrors();
        if (!validate()) return;

        currentUser.setFirstName(firstNameField.getText().trim());
        currentUser.setLastName(lastNameField.getText().trim());
        if (!"enfant".equals(currentUser.getType())) {
            currentUser.setEmail(emailField.getText().trim());
        }
        currentUser.setTelephone(telephoneField.getText().trim());
        currentUser.setAdresse(adresseField.getText().trim());
        currentUser.setBirthDate(birthDateField.getValue());

        if ("enfant".equals(currentUser.getType())) {
            currentUser.setUsername(usernameField.getText().trim());
            currentUser.setNiveau(niveauCombo.getValue());
        } else if ("enseignant".equals(currentUser.getType())) {
            currentUser.setSpecialite(specialiteField.getText().trim());
        }

        String newPw = passwordField.getText();
        if (newPw != null && !newPw.isBlank()) {
            currentUser.setPassword(BCrypt.hashpw(newPw, BCrypt.gensalt()));
        }

        userService.modifier(currentUser);
        AppContext.setCurrentUser(currentUser);
        loadUserData();

        successLabel.setText("Votre profil a été mis à jour avec succès !");
        oldPasswordField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    // ─── VALIDATION ───────────────────────────────────────────────────────────

    private boolean validate() {
        boolean valid = true;
        String type = currentUser.getType();

        if (firstNameField.getText().isBlank()) {
            firstNameError.setText("Prénom obligatoire.");
            valid = false;
        }
        if (lastNameField.getText().isBlank()) {
            lastNameError.setText("Nom obligatoire.");
            valid = false;
        }

        if (!"enfant".equals(type)) {
            if (emailField.getText() == null || emailField.getText().isBlank()) {
                emailError.setText("Email obligatoire.");
                valid = false;
            } else if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailError.setText("Email invalide.");
                valid = false;
            }
        }

        if (telephoneField.getText() != null && !telephoneField.getText().isBlank()
                && !telephoneField.getText().matches("\\+?\\d{8,15}")) {
            telephoneError.setText("Téléphone invalide.");
            valid = false;
        }

        if ("enseignant".equals(type) && specialiteField.getText().isBlank()) {
            specialiteError.setText("Spécialité obligatoire.");
            valid = false;
        }

        if ("enfant".equals(type)) {
            if (usernameField.getText().isBlank()) {
                usernameError.setText("Identifiant obligatoire.");
                valid = false;
            } else if (usernameField.getText().trim().length() < 3) {
                usernameError.setText("Minimum 3 caractères.");
                valid = false;
            } else {
                User existing = userService.findByLogin(usernameField.getText().trim());
                if (existing != null && existing.getId() != currentUser.getId()) {
                    usernameError.setText("Identifiant déjà utilisé.");
                    valid = false;
                }
            }
        }

        // Mot de passe
        String oldPw  = oldPasswordField.getText();
        String newPw  = passwordField.getText();
        String confPw = confirmPasswordField.getText();

        if ((newPw != null && !newPw.isBlank()) || (oldPw != null && !oldPw.isBlank())) {
            if (oldPw == null || oldPw.isBlank()) {
                oldPasswordError.setText("Veuillez saisir votre mot de passe actuel.");
                valid = false;
            } else if (!BCrypt.checkpw(oldPw, currentUser.getPassword())) {
                oldPasswordError.setText("Mot de passe actuel incorrect.");
                valid = false;
            }
            if (newPw == null || newPw.length() < 6) {
                passwordError.setText("Minimum 6 caractères.");
                valid = false;
            }
            if (newPw != null && !newPw.equals(confPw)) {
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
        oldPasswordError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        specialiteError.setText("");
        usernameError.setText("");
        successLabel.setText("");
        if (uploadStatusLabel != null) uploadStatusLabel.setText("");
    }

    private String getRoleColor(String type) {
        return switch (type) {
            case "admin"     -> "#E94560";
            case "enseignant"-> "#4A90E2";
            case "parent"    -> "#F39C12";
            case "enfant"    -> "#2ECC71";
            default          -> "#95A5A6";
        };
    }
}