package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SidebarController {

    /* ── Labels utilisateur ────────────────────────────────── */

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    /* ── Boutons communs ───────────────────────────────────── */

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;

    /* ── Boutons Admin (cachés pour parent) ────────────────── */

    @FXML private Label  sectionAdmin;
    @FXML private Button btnUsers;
    @FXML private Button btnTeachers;
    @FXML private Button btnParents;
    @FXML private Button btnEventList;
    @FXML private Button btnRegistrationList;

    /* ── Boutons Enseignant (cachés pour parent) ───────────── */

    @FXML private Label  sectionTeacher;
    @FXML private Button btnCourses;
    @FXML private Button btnStudents;

    /* ── Boutons Parent (visibles pour parent) ─────────────── */

    @FXML private Label  sectionEvents;
    @FXML private Button btnParentEventList;
    @FXML private Button btnParentRegistrations;
    @FXML private Label  sectionFamily;
    @FXML private Button btnChildren;

    /* ── Boutons Enfant (cachés pour parent) ───────────────── */

    @FXML private Label  sectionChild;
    @FXML private Button btnMyCoursesChild;
    @FXML private Button btnGames;

    /* ── Tous les boutons de navigation ────────────────────── */

    private List<Button> allNavButtons;

    @FXML
    public void initialize() {
        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnTeachers, btnParents,
                btnCourses, btnStudents,
                btnChildren,
                btnMyCoursesChild, btnGames,
                btnProfile,
                btnEventList, btnRegistrationList,
                btnParentEventList, btnParentRegistrations
        );

        String fullName = AppContext.getFullName();
        String role = AppContext.getRole();

        setIfNotNull(labelUserName, fullName);
        setIfNotNull(labelUserRole, capitalize(role));
        setIfNotNull(labelUserInitials, buildInitials(fullName));

        hideAllRoleSections();
        showSectionsForRole(role);

        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());
    }

    /* ── Actions de navigation ─────────────────────────────── */

    @FXML private void showDashboard() { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile() { Router.go("profile"); }

    // Admin
    @FXML private void showUsers() { Router.go("users"); }
    @FXML private void showTeachers() { Router.go("teachers"); }
    @FXML private void showParents() { Router.go("parents"); }
    @FXML private void showEventList() { Router.go("event_list"); }
    @FXML private void showRegistrationList() { Router.go("registration_list"); }

    // Enseignant
    @FXML private void showCourses() { Router.go("teacher_courses"); }
    @FXML private void showStudents() { Router.go("teacher_students"); }

    // Parent
    @FXML private void showParentEventList() { Router.go("parent_event_list"); }
    @FXML private void showParentRegistrations() { Router.go("parent_registrations"); }
    @FXML private void showChildren() { Router.go("parent_children"); }

    // Enfant
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showGames() { Router.go("child_games"); }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Se déconnecter ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AppContext.clear();
                Router.clearCache();
                navigateToLogin();
            }
        });
    }

    public void syncActiveButton(String route) {
        allNavButtons.stream()
                .filter(b -> b != null && b.isVisible())
                .forEach(b -> {
                    b.getStyleClass().remove("nav-btn-active");
                    if (!b.getStyleClass().contains("nav-btn")) {
                        b.getStyleClass().add("nav-btn");
                    }
                });

        Button active = switch (route) {
            case "admin_dashboard", "teacher_dashboard", "parent_dashboard", "child_dashboard" -> btnDashboard;
            case "users", "teachers", "parents" -> btnUsers;
            case "teacher_courses" -> btnCourses;
            case "teacher_students" -> btnStudents;
            case "parent_children" -> btnChildren;
            case "child_courses" -> btnMyCoursesChild;
            case "child_games" -> btnGames;
            case "profile" -> btnProfile;
            case "event_list" -> btnEventList;
            case "registration_list" -> btnRegistrationList;
            case "parent_event_list" -> btnParentEventList;
            case "parent_registrations" -> btnParentRegistrations;
            default -> btnDashboard;
        };

        if (active != null && active.isVisible()) {
            active.getStyleClass().remove("nav-btn");
            if (!active.getStyleClass().contains("nav-btn-active")) {
                active.getStyleClass().add("nav-btn-active");
            }
        }
    }

    private void hideAllRoleSections() {
        // Admin
        setVisible(sectionAdmin, false);
        setVisible(btnUsers, false);
        setVisible(btnTeachers, false);
        setVisible(btnParents, false);
        setVisible(btnEventList, false);
        setVisible(btnRegistrationList, false);

        // Enseignant
        setVisible(sectionTeacher, false);
        setVisible(btnCourses, false);
        setVisible(btnStudents, false);

        // Parent
        setVisible(sectionEvents, false);
        setVisible(btnParentEventList, false);
        setVisible(btnParentRegistrations, false);
        setVisible(sectionFamily, false);
        setVisible(btnChildren, false);

        // Enfant
        setVisible(sectionChild, false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames, false);
    }

    private void showSectionsForRole(String role) {
        switch (role) {
            case "admin" -> {
                setVisible(sectionAdmin, true);
                setVisible(btnUsers, true);
                setVisible(btnTeachers, true);
                setVisible(btnParents, true);
                setVisible(btnEventList, true);
                setVisible(btnRegistrationList, true);
            }
            case "enseignant" -> {
                setVisible(sectionTeacher, true);
                setVisible(btnCourses, true);
                setVisible(btnStudents, true);
            }
            case "parent" -> {
                setVisible(sectionEvents, true);
                setVisible(btnParentEventList, true);
                setVisible(btnParentRegistrations, true);
                setVisible(sectionFamily, true);
                setVisible(btnChildren, true);
            }
            case "enfant" -> {
                setVisible(sectionChild, true);
                setVisible(btnMyCoursesChild, true);
                setVisible(btnGames, true);
            }
        }
    }

    private void navigateToLogin() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/auth/LoginView.fxml")).load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.setTitle("EduPlay — Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Erreur navigation : " + e.getMessage()).showAndWait();
        }
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void setIfNotNull(Label label, String text) {
        if (label != null) label.setText(text);
    }

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}