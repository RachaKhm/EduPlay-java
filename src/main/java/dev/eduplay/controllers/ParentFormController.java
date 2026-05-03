package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.List;

public class ParentFormController {

    @FXML private Label formTitle;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adresseField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox activeCheck;

    @FXML private VBox childrenContainer;
    @FXML private HBox childRow1;
    @FXML private Label feedbackLabel;

    private final UserService userService = new UserService();
    private final List<HBox> childRows = new ArrayList<>();

    @FXML
    public void initialize() {
        if (childRow1 != null) {
            childRows.add(childRow1);
            // Add action to the delete button of the static row if found
            Button delBtn = (Button) childRow1.getChildren().get(childRow1.getChildren().size() - 1);
            delBtn.setOnAction(e -> {
                childrenContainer.getChildren().remove(childRow1);
                childRows.remove(childRow1);
            });
        }
    }

    @FXML
    public void addChildRow() {
        HBox row = new HBox(8);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        TextField firstName = new TextField();
        firstName.setPromptText("Prénom");
        firstName.setPrefWidth(120);
        firstName.getStyleClass().add("parent-input");

        TextField lastName = new TextField();
        lastName.setPromptText("Nom");
        lastName.setPrefWidth(120);
        lastName.getStyleClass().add("parent-input");

        ComboBox<String> niveau = new ComboBox<>();
        niveau.getItems().addAll("Maternelle", "1ère année", "2ème année", "3ème année", "4ème année", "5ème année", "6ème année");
        niveau.setPrefWidth(130);

        TextField username = new TextField();
        username.setPromptText("username");
        username.setPrefWidth(110);
        username.getStyleClass().add("parent-input");

        Button deleteBtn = new Button("✕");
        deleteBtn.setPrefWidth(32);
        deleteBtn.getStyleClass().add("parent-row-delete-btn");
        deleteBtn.setOnAction(e -> {
            childrenContainer.getChildren().remove(row);
            childRows.remove(row);
        });

        row.getChildren().addAll(firstName, lastName, niveau, username, deleteBtn);
        childrenContainer.getChildren().add(row);
        childRows.add(row);
    }

    @FXML
    public void saveParent() {
        if (firstNameField.getText().isBlank() || lastNameField.getText().isBlank() || emailField.getText().isBlank()) {
            feedbackLabel.setText("Veuillez remplir les champs obligatoires du parent.");
            feedbackLabel.getStyleClass().add("child-error-label");
            return;
        }

        // Créer le parent
        User parent = new User();
        parent.setFirstName(firstNameField.getText().trim());
        parent.setLastName(lastNameField.getText().trim());
        parent.setEmail(emailField.getText().trim());
        parent.setTelephone(telephoneField.getText().trim());
        parent.setAdresse(adresseField.getText().trim());
        parent.setType("parent");
        parent.setActive(activeCheck.isSelected());
        
        String pwd = passwordField.getText();
        if (pwd != null && !pwd.isBlank()) {
            parent.setPassword(BCrypt.hashpw(pwd, BCrypt.gensalt()));
        } else {
            parent.setPassword(BCrypt.hashpw("eduplay123", BCrypt.gensalt())); // Default password
        }

        userService.ajouter(parent);
        User savedParent = userService.findByLogin(parent.getEmail()); // Re-fetch to get ID

        if (savedParent == null) {
             feedbackLabel.setText("Erreur lors de la création du parent.");
             return;
        }

        // Créer les enfants
        for (HBox row : childRows) {
            TextField fn = (TextField) row.getChildren().get(0);
            TextField ln = (TextField) row.getChildren().get(1);
            ComboBox<String> niv = (ComboBox<String>) row.getChildren().get(2);
            TextField un = (TextField) row.getChildren().get(3);

            if (!fn.getText().isBlank() && !un.getText().isBlank()) {
                User child = new User();
                child.setFirstName(fn.getText().trim());
                child.setLastName(ln.getText().trim());
                child.setNiveau(niv.getValue());
                child.setUsername(un.getText().trim());
                child.setType("enfant");
                child.setParentId(savedParent.getId());
                child.setActive(true);
                child.setPassword(BCrypt.hashpw("eduplay123", BCrypt.gensalt()));
                userService.ajouter(child);
            }
        }

        feedbackLabel.setText("Parent et enfants enregistrés avec succès !");
        feedbackLabel.getStyleClass().remove("child-error-label");
        feedbackLabel.getStyleClass().add("child-success-label");

        javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1.5));
        pause.setOnFinished(e -> Router.go("users"));
        pause.play();
    }

    @FXML
    public void closeForm() {
        Router.go("users");
    }
}
