package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Dashboard / Formulaire Enseignant
 * Ce controller est utilisé pour la vue dashboard et le formulaire "Nouvel enseignant".
 * Il fournit la méthode `saveTeacher` attendue par `TeacherFormView.fxml`.
 */
public class TeacherDashboardController {

    // Dashboard fields (peuvent être null lorsqu'on affiche le formulaire)
    @FXML private Label subtitleLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalStudentsLabel;

    // Form fields (présents dans TeacherFormView.fxml)
    @FXML private Label formTitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private ComboBox<String> specialiteCombo;
    @FXML private ComboBox<String> niveauEnseignementCombo;
    @FXML private TextArea bioField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox activeCheck;
    @FXML private Label feedbackLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        // Dashboard initialisation
        if (subtitleLabel != null)
            subtitleLabel.setText("Bienvenue, " + AppContext.getFullName());
        if (totalCoursesLabel  != null) totalCoursesLabel.setText("0");
        if (totalStudentsLabel != null) totalStudentsLabel.setText("0");

        // Form initialisation: remplir les combo boxes si elles existent
        if (specialiteCombo != null) {
            specialiteCombo.getItems().clear();
            specialiteCombo.getItems().addAll("Mathématiques", "Français", "Sciences", "Anglais", "Histoire", "Géographie");
        }
        if (niveauEnseignementCombo != null) {
            niveauEnseignementCombo.getItems().clear();
            niveauEnseignementCombo.getItems().addAll("Maternelle", "Primaire", "Collège", "Lycée");
        }
        if (activeCheck != null) activeCheck.setSelected(true);
    }

    @FXML
    public void saveTeacher() {
        if (firstNameField == null || lastNameField == null || emailField == null || feedbackLabel == null) return;

        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank() || emailField.getText().isBlank()) {
            feedbackLabel.setText("Veuillez remplir les champs obligatoires (Prénom, Nom, Email).");
            feedbackLabel.getStyleClass().add("child-error-label");
            return;
        }

        User teacher = new User();
        teacher.setFirstName(firstNameField.getText().trim());
        teacher.setLastName(lastNameField.getText().trim());
        teacher.setEmail(emailField.getText().trim());
        teacher.setTelephone(telephoneField != null ? telephoneField.getText().trim() : "");
        teacher.setType("enseignant");
        teacher.setActive(activeCheck != null && activeCheck.isSelected());
        teacher.setSpecialite(specialiteCombo != null ? specialiteCombo.getValue() : null);
        teacher.setNiveau(niveauEnseignementCombo != null ? niveauEnseignementCombo.getValue() : null);
        teacher.setAdresse(bioField != null ? bioField.getText().trim() : null);

        String pwd = passwordField != null ? passwordField.getText() : null;
        if (pwd != null && !pwd.isBlank()) {
            teacher.setPassword(BCrypt.hashpw(pwd, BCrypt.gensalt()));
        } else {
            teacher.setPassword(BCrypt.hashpw("eduplay123", BCrypt.gensalt()));
        }

        try {
            userService.ajouter(teacher);
            feedbackLabel.getStyleClass().remove("child-error-label");
            feedbackLabel.getStyleClass().add("child-success-label");
            feedbackLabel.setText("Enseignant créé avec succès !");

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.2));
            pause.setOnFinished(e -> Router.go("teachers"));
            pause.play();

        } catch (Exception e) {
            feedbackLabel.getStyleClass().remove("child-success-label");
            feedbackLabel.getStyleClass().add("child-error-label");
            feedbackLabel.setText("Erreur lors de la création de l'enseignant : " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void closeForm() {
        Router.go("teachers");
    }
}
