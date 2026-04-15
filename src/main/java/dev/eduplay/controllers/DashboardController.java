package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DashboardController
 * ─────────────────────────────────────────────────────────────
 * Tableau de bord administrateur (DashboardView.fxml).

 * Affiche :
 *   • 4 cartes statistiques (total, enseignants, parents, enfants)
 *   • Tableau des 5 derniers utilisateurs inscrits

 * Toute navigation est déléguée à Router-go("route").
 * Ce controller ne crée JAMAIS de Stage ni de Scene.
 * ─────────────────────────────────────────────────────────────
 */
public class DashboardController {

    /* ── FXML bindings ─────────────────────────────────────── */

    @FXML private Label totalUsersLabel;
    @FXML private Label totalEnseignantsLabel;
    @FXML private Label totalParentsLabel;
    @FXML private Label totalEnfantsLabel;

    @FXML private TableView<User>             recentUsersTable;
    @FXML private TableColumn<User, String>   recentColName;
    @FXML private TableColumn<User, String>   recentColEmail;
    @FXML private TableColumn<User, String>   recentColType;
    @FXML private TableColumn<User, String>   recentColDate;
    @FXML private TableColumn<User, Boolean>  recentColActive;

    /* ── Services ──────────────────────────────────────────── */

    private final UserService userService = new UserService();

    /* ── Constantes ────────────────────────────────────────── */

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int RECENT_USERS_LIMIT = 5;

    /* ── Initialisation ────────────────────────────────────── */

    @FXML
    public void initialize() {
        setupRecentTable();
        refreshStats();
    }

    /* ── Statistiques ──────────────────────────────────────── */

    @FXML
    public void refreshStats() {
        List<User> all = userService.recuperer();

        setText(totalUsersLabel,       String.valueOf(all.size()));
        setText(totalEnseignantsLabel, String.valueOf(countByType(all, "enseignant")));
        setText(totalParentsLabel,     String.valueOf(countByType(all, "parent")));
        setText(totalEnfantsLabel,     String.valueOf(countByType(all, "enfant")));

        // Remplir le tableau des utilisateurs récents
        List<User> recent = all.stream()
                .filter(u -> u.getCreatedAt() != null)
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(RECENT_USERS_LIMIT)
                .collect(Collectors.toList());

        if (recentUsersTable != null) {
            recentUsersTable.setItems(FXCollections.observableArrayList(recent));
        }
    }

    /* ── Configuration du tableau ──────────────────────────── */

    private void setupRecentTable() {
        if (recentColName != null) {
            recentColName.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getFullName()));
        }

        if (recentColEmail != null) {
            recentColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        }

        if (recentColType != null) {
            recentColType.setCellValueFactory(new PropertyValueFactory<>("type"));
            recentColType.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(String type, boolean empty) {
                    super.updateItem(type, empty);
                    if (empty || type == null) { setGraphic(null); return; }
                    Label badge = new Label(type.toUpperCase());
                    badge.getStyleClass().add(resolveBadgeClass(type));
                    setGraphic(badge);
                    setText(null);
                }
            });
        }

        if (recentColDate != null) {
            recentColDate.setCellValueFactory(data -> {
                User u = data.getValue();
                String d = (u.getCreatedAt() != null)
                        ? u.getCreatedAt().format(DATE_FMT) : "—";
                return new SimpleStringProperty(d);
            });
        }

        if (recentColActive != null) {
            recentColActive.setCellValueFactory(new PropertyValueFactory<>("active"));
            recentColActive.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Boolean active, boolean empty) {
                    super.updateItem(active, empty);
                    if (empty || active == null) { setGraphic(null); return; }
                    Label badge = new Label(active ? "● Actif" : "● Inactif");
                    badge.getStyleClass().add(active ? "badge-actif" : "badge-inactif");
                    setGraphic(badge);
                    setText(null);
                }
            });
        }
    }

    /* ── Navigation (boutons FXML) ─────────────────────────── */

    @FXML
    public void showDashboard(ActionEvent event) {
        Router.go("admin_dashboard");
    }

    @FXML
    public void showUsers(ActionEvent event) {
        Router.go("users");
    }

    @FXML
    public void showTeachers(ActionEvent event) {
        Router.go("teachers");
    }

    @FXML
    public void showParents(ActionEvent event) {
        Router.go("parents");
    }

    @FXML
    public void showChildren(ActionEvent event) {
        Router.go("parent_children");
    }

    /* ── Utilitaires ───────────────────────────────────────── */

    /** Retourne la classe CSS du badge selon le type utilisateur. */
    private String resolveBadgeClass(String type) {
        return switch (type) {
            case "admin"      -> "badge-admin";
            case "enseignant" -> "badge-enseignant";
            case "parent"     -> "badge-parent";
            case "enfant"     -> "badge-enfant";
            default           -> "badge-default";
        };
    }

    private long countByType(List<User> users, String type) {
        return users.stream()
                .filter(u -> type.equalsIgnoreCase(u.getType()))
                .count();
    }

    private void setText(Label label, String text) {
        if (label != null) label.setText(text);
    }
}