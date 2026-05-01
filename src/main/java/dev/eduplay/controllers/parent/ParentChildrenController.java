package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;

import java.util.List;
import java.util.stream.Collectors;

public class ParentChildrenController {

    @FXML private VBox childrenContainer;
    @FXML private Label noChildrenLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        loadChildren();
    }

    private void loadChildren() {
        childrenContainer.getChildren().clear();
        User parent = AppContext.getCurrentUser();
        if (parent == null) return;

        // Fetch all users and filter by parent_id
        // In a real app, we'd have a specific service method, but I'll filter for now
        List<User> allUsers = userService.recuperer();
        List<User> children = allUsers.stream()
                .filter(u -> u.getParentId() != null && u.getParentId() == parent.getId())
                .collect(Collectors.toList());

        if (children.isEmpty()) {
            noChildrenLabel.setVisible(true);
            noChildrenLabel.setManaged(true);
        } else {
            noChildrenLabel.setVisible(false);
            noChildrenLabel.setManaged(false);
            for (User child : children) {
                childrenContainer.getChildren().add(createChildCard(child));
            }
        }
    }

    private HBox createChildCard(User child) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 5, 0, 0, 5);");

        VBox info = new VBox(5);
        Label name = new Label(child.getFirstName() + " " + child.getLastName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #3B82F6;");
        
        Label detail = new Label("Username: " + child.getUsername() + " | Niveau: " + (child.getNiveau() != null ? child.getNiveau() : "N/A"));
        detail.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");
        
        info.getChildren().addAll(name, detail);
        
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-background-radius: 5; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer le compte de " + child.getFirstName() + " ?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    userService.supprimer(child);
                    loadChildren();
                }
            });
        });

        card.getChildren().addAll(info, spacer, deleteBtn);
        return card;
    }

    @FXML
    private void handleAddChild() {
        // Simple dialog for now to create a child account
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un enfant");
        dialog.setHeaderText("Créer un compte pour votre enfant");

        ButtonType saveButtonType = new ButtonType("Créer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox grid = new VBox(10);
        TextField firstName = new TextField(); firstName.setPromptText("Prénom");
        TextField lastName = new TextField(); lastName.setPromptText("Nom");
        TextField username = new TextField(); username.setPromptText("Username");
        PasswordField password = new PasswordField(); password.setPromptText("Mot de passe");
        ComboBox<String> level = new ComboBox<>(FXCollections.observableArrayList("Primaire", "Collège", "Lycée"));
        level.setPromptText("Niveau");

        grid.getChildren().addAll(
            new Label("Prénom:"), firstName,
            new Label("Nom:"), lastName,
            new Label("Username:"), username,
            new Label("Mot de passe:"), password,
            new Label("Niveau:"), level
        );
        grid.setStyle("-fx-padding: 20;");
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User u = new User();
                u.setFirstName(firstName.getText());
                u.setLastName(lastName.getText());
                u.setUsername(username.getText());
                u.setPassword(password.getText());
                u.setType("enfant");
                u.setNiveau(level.getValue());
                u.setParentId(AppContext.getCurrentUser().getId());
                u.setActive(true);
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(child -> {
            userService.ajouter(child);
            loadChildren();
        });
    }
}
