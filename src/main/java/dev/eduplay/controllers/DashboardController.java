package dev.eduplay.controllers;

import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    // ───── Labels stats ─────
    @FXML private Label totalUsersLabel;
    @FXML private Label totalEnseignantsLabel;
    @FXML private Label totalParentsLabel;
    @FXML private Label totalEnfantsLabel;
    @FXML private Label adminNameLabel;

    // ───── Table utilisateurs récents ─────
    @FXML private TableView<User> recentUsersTable;
    @FXML private TableColumn<User, String> recentColName;
    @FXML private TableColumn<User, String> recentColEmail;
    @FXML private TableColumn<User, String> recentColType;
    @FXML private TableColumn<User, String> recentColDate;
    @FXML private TableColumn<User, Boolean> recentColActive;

    private final UserService userService = new UserService();
    private User currentUser;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────
    @FXML
    public void initialize() {
        setupRecentTable();
        refreshStats();
    }

    // 🔥 appelé après login
    public void setCurrentUser(User user) {
        this.currentUser = user;

        if (user != null && adminNameLabel != null) {
            adminNameLabel.setText(
                    user.getFullName() + "  ·  " + capitalize(user.getType())
            );
        }
    }

    // ───── Stats ─────
    @FXML
    public void refreshStats() {
        List<User> all = userService.getAll();

        if (totalUsersLabel != null)
            totalUsersLabel.setText(String.valueOf(all.size()));

        if (totalEnseignantsLabel != null)
            totalEnseignantsLabel.setText(String.valueOf(countByType(all, "enseignant")));

        if (totalParentsLabel != null)
            totalParentsLabel.setText(String.valueOf(countByType(all, "parent")));

        if (totalEnfantsLabel != null)
            totalEnfantsLabel.setText(String.valueOf(countByType(all, "enfant")));

        // 🔥 utilisateurs récents
        List<User> recent = all.stream()
                .filter(u -> u.getCreatedAt() != null)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());

        if (recentUsersTable != null)
            recentUsersTable.setItems(FXCollections.observableArrayList(recent));
    }

    // ───── Table config ─────
    private void setupRecentTable() {

        if (recentColName != null)
            recentColName.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getFullName()));

        if (recentColEmail != null)
            recentColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        if (recentColType != null) {
            recentColType.setCellValueFactory(new PropertyValueFactory<>("type"));

            recentColType.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String type, boolean empty) {
                    super.updateItem(type, empty);
                    if (empty || type == null) {
                        setGraphic(null);
                        return;
                    }

                    Label badge = new Label(type);
                    badge.setStyle(getTypeStyle(type));

                    setGraphic(badge);
                    setText(null);
                }
            });
        }

        if (recentColDate != null)
            recentColDate.setCellValueFactory(data -> {
                User u = data.getValue();
                String d = (u.getCreatedAt() != null)
                        ? u.getCreatedAt().format(FMT)
                        : "—";
                return new SimpleStringProperty(d);
            });

        if (recentColActive != null) {
            recentColActive.setCellValueFactory(new PropertyValueFactory<>("active"));

            recentColActive.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean active, boolean empty) {
                    super.updateItem(active, empty);
                    if (empty || active == null) {
                        setGraphic(null);
                        return;
                    }

                    Label l = new Label(active ? "● Actif" : "● Inactif");
                    l.setStyle(active
                            ? "-fx-text-fill:#1A7A4A;"
                            : "-fx-text-fill:#C0304A;");

                    setGraphic(l);
                    setText(null);
                }
            });
        }
    }

    // ───── Navigation ─────
    @FXML
    public void showUsers() {
        navigateTo("/dev/eduplay/views/admin/UserListView.fxml", "Utilisateurs");
    }

    @FXML
    public void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Se déconnecter ?", ButtonType.YES, ButtonType.NO);

        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                navigateTo("/dev/eduplay/views/auth/LoginView.fxml", "Login");
            }
        });
    }

    // ───── Navigation helper ─────
    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof DashboardController dc) {
                dc.setCurrentUser(currentUser);
            }

            Stage stage = (Stage) totalUsersLabel.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(new Scene(root, stage.getWidth(), stage.getHeight()));

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erreur navigation: " + e.getMessage()).showAndWait();
        }
    }

    // ───── Utils ─────
    private long countByType(List<User> users, String type) {
        return users.stream()
                .filter(u -> type.equalsIgnoreCase(u.getType()))
                .count();
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String getTypeStyle(String type) {
        return switch (type) {
            case "admin"      -> "-fx-background-color:#F0E8FF; -fx-text-fill:#6030A0; -fx-padding:3 10; -fx-background-radius:12;";
            case "enseignant" -> "-fx-background-color:#FFF0E0; -fx-text-fill:#A05010; -fx-padding:3 10; -fx-background-radius:12;";
            case "parent"     -> "-fx-background-color:#E0F0FF; -fx-text-fill:#105090; -fx-padding:3 10; -fx-background-radius:12;";
            case "enfant"     -> "-fx-background-color:#E0FFE8; -fx-text-fill:#106030; -fx-padding:3 10; -fx-background-radius:12;";
            default           -> "-fx-background-color:#F0F0F0; -fx-text-fill:#606060; -fx-padding:3 10; -fx-background-radius:12;";
        };
    }

    public void showDashboard(ActionEvent actionEvent) {
    }

    public void showChildren(ActionEvent actionEvent) {
    }

    public void showParents(ActionEvent actionEvent) {
    }

    public void showTeachers(ActionEvent actionEvent) {
    }
}