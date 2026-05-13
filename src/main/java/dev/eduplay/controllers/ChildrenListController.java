package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Contrôleur de la vue "Mes enfants" du parent.
 * Affiche la liste des enfants rattachés et permet d'en ajouter, modifier, supprimer.
 */
public class ChildrenListController {

    @FXML private VBox childrenRows;
    @FXML private Label emptyLabel;
    @FXML private Label subtitleLabel;

    private final UserService userService = new UserService();

    // Champ statique pour passer l'enfant à éditer vers AddChildController
    private static User childToEdit = null;

    public static User getChildToEdit() { return childToEdit; }
    public static void setChildToEdit(User child) { childToEdit = child; }
    public static void clearChildToEdit() { childToEdit = null; }

    @FXML
    public void initialize() {
        loadChildren();
    }

    private void loadChildren() {
        childrenRows.getChildren().clear();

        if (AppContext.getCurrentUser() == null) return;

        int parentId = AppContext.getCurrentUser().getId();
        List<User> allEnfants = userService.getByType("enfant");

        List<User> mesEnfants = allEnfants.stream()
                .filter(e -> e.getParentId() == parentId)
                .toList();

        if (mesEnfants.isEmpty()) {
            emptyLabel.setVisible(true);
            emptyLabel.setManaged(true);
        } else {
            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);

            for (User enfant : mesEnfants) {
                childrenRows.getChildren().add(buildChildRow(enfant));
            }
        }

        if (subtitleLabel != null) {
            subtitleLabel.setText(mesEnfants.size() + " enfant(s) rattaché(s)");
        }
    }

    private HBox buildChildRow(User enfant) {
        HBox row = new HBox();
        row.setSpacing(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12 20; -fx-border-color: #F0F0F8; -fx-border-width: 0 0 1 0;");

        Label prenom = new Label(enfant.getFirstName() != null ? enfant.getFirstName() : "—");
        prenom.setPrefWidth(100);
        prenom.setStyle("-fx-font-size: 13px; -fx-text-fill: #333355;");

        Label nom = new Label(enfant.getLastName() != null ? enfant.getLastName() : "—");
        nom.setPrefWidth(100);
        nom.setStyle("-fx-font-size: 13px; -fx-text-fill: #333355;");

        Label username = new Label(enfant.getUsername() != null ? enfant.getUsername() : "—");
        username.setPrefWidth(110);
        username.setStyle("-fx-font-size: 13px; -fx-text-fill: #555577;");

        Label niveau = new Label(enfant.getNiveau() != null ? enfant.getNiveau() : "—");
        niveau.setPrefWidth(110);
        niveau.setStyle("-fx-font-size: 13px; -fx-text-fill: #555577;");

        Label birthDate = new Label(enfant.getBirthDate() != null ? enfant.getBirthDate().toString() : "—");
        birthDate.setPrefWidth(110);
        birthDate.setStyle("-fx-font-size: 13px; -fx-text-fill: #555577;");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Bouton Modifier
        Button btnEdit = new Button("✏ Modifier");
        btnEdit.setStyle("-fx-background-color: #F0F5FF; -fx-text-fill: #4A90D9; " +
                "-fx-font-size: 11px; -fx-padding: 5 12; -fx-background-radius: 5; " +
                "-fx-border-color: #C0D0F8; -fx-border-width: 1; -fx-border-radius: 5; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> editChild(enfant));

        // Bouton Supprimer
        Button btnDelete = new Button("🗑 Supprimer");
        btnDelete.setStyle("-fx-background-color: #FFF0F0; -fx-text-fill: #E94560; " +
                "-fx-font-size: 11px; -fx-padding: 5 12; -fx-background-radius: 5; " +
                "-fx-border-color: #F0C0C0; -fx-border-width: 1; -fx-border-radius: 5; -fx-cursor: hand;");
        btnDelete.setOnAction(e -> deleteChild(enfant));

        HBox actions = new HBox(8, btnEdit, btnDelete);
        actions.setAlignment(Pos.CENTER_RIGHT);

        row.getChildren().addAll(prenom, nom, username, niveau, birthDate, spacer, actions);
        return row;
    }

    private void editChild(User enfant) {
        setChildToEdit(enfant);
        Router.reload("parent_add_child");
    }

    private void deleteChild(User enfant) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le compte de " + enfant.getFullName() + " ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer cet enfant");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                userService.supprimer(enfant);
                Router.reload("parent_children");
            }
        });
    }

    @FXML
    public void openAddChild() {
        clearChildToEdit();
        Router.reload("parent_add_child");
    }
}
