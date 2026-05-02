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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SidebarController {

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    @FXML private HBox btnDashboard;
    @FXML private HBox btnProfile;
    @FXML private HBox btnUsers;
    @FXML private HBox btnTeachers;
    @FXML private HBox btnParents;
    @FXML private HBox btnEventList;
    @FXML private HBox btnRegistrationList;
    @FXML private HBox btnCourses;
    @FXML private HBox btnStudents;
    @FXML private HBox btnChildren;
    @FXML private HBox btnEvents;
    @FXML private HBox btnMyCoursesChild;
    @FXML private HBox btnGames;
    @FXML private HBox btnLibrary;        // ← AJOUTÉ
    @FXML private HBox btnResource;       // ← AJOUTÉ
    @FXML private HBox btnChildLibrary;   // ← AJOUTÉ

    @FXML private Label sectionAdmin;
    @FXML private Label sectionTeacher;
    @FXML private Label sectionParent;
    @FXML private Label sectionChild;

    private List<HBox> allNavButtons;

    @FXML
    public void initialize() {
        allNavButtons = Arrays.asList(
                btnDashboard, btnUsers, btnTeachers, btnParents,
                btnEventList, btnRegistrationList,
                btnCourses, btnStudents,
                btnChildren, btnEvents,
                btnMyCoursesChild, btnGames,
                btnLibrary, btnResource, btnChildLibrary,
                btnProfile
        );

        String fullName = AppContext.getFullName();
        String role = AppContext.getRole();

        if (labelUserName != null) labelUserName.setText(fullName);
        if (labelUserRole != null) labelUserRole.setText(capitalize(role));
        if (labelUserInitials != null) labelUserInitials.setText(buildInitials(fullName));

        hideAllRoleSections();
        showSectionsForRole(role);

        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());
    }

    // ⚠️ TOUTES les méthodes de navigation
    @FXML private void showDashboard() { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile() { Router.go("profile"); }
    @FXML private void showUsers() { Router.go("users"); }
    @FXML private void showTeachers() { Router.go("teachers"); }
    @FXML private void showParents() { Router.go("parents"); }
    @FXML private void showEventList() { Router.go("event_list"); }
    @FXML private void showRegistrationList() { Router.go("registration_list"); }
    @FXML private void showCourses() { Router.go("teacher_courses"); }
    @FXML private void showStudents() { Router.go("teacher_students"); }
    @FXML private void showChildren() { Router.go("parent_children"); }
    @FXML private void showEvents() { Router.go("parent_events"); }
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showGames() { Router.go("child_games"); }
    @FXML private void showLibrary() { Router.go("library"); }           // ← AJOUTÉ
    @FXML private void showResource() { Router.go("resource"); }         // ← AJOUTÉ
    @FXML private void showChildLibrary() { Router.go("child_library"); } // ← AJOUTÉ

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Se déconnecter ?", ButtonType.YES, ButtonType.NO);
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
        allNavButtons.stream().filter(b -> b != null && b.isVisible()).forEach(b -> {
            b.getStyleClass().remove("nav-btn-active");
            if (!b.getStyleClass().contains("nav-btn")) b.getStyleClass().add("nav-btn");
        });

        HBox active = switch (route) {
            case "admin_dashboard", "teacher_dashboard", "parent_dashboard", "child_dashboard" -> btnDashboard;
            case "users", "teachers", "parents" -> btnUsers;
            case "event_list" -> btnEventList;
            case "registration_list" -> btnRegistrationList;
            case "teacher_courses" -> btnCourses;
            case "teacher_students" -> btnStudents;
            case "parent_children" -> btnChildren;
            case "parent_events" -> btnEvents;
            case "child_courses" -> btnMyCoursesChild;
            case "child_games" -> btnGames;
            case "library" -> btnLibrary;
            case "resource" -> btnResource;
            case "child_library" -> btnChildLibrary;
            case "profile" -> btnProfile;
            default -> btnDashboard;
        };

        if (active != null && active.isVisible()) {
            active.getStyleClass().remove("nav-btn");
            active.getStyleClass().add("nav-btn-active");
        }
    }

    private void hideAllRoleSections() {
        setVisible(sectionAdmin, false);
        setVisible(sectionTeacher, false);
        setVisible(sectionParent, false);
        setVisible(sectionChild, false);
        setVisible(btnUsers, false);
        setVisible(btnTeachers, false);
        setVisible(btnParents, false);
        setVisible(btnEventList, false);
        setVisible(btnRegistrationList, false);
        setVisible(btnCourses, false);
        setVisible(btnStudents, false);
        setVisible(btnChildren, false);
        setVisible(btnEvents, false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames, false);
        setVisible(btnLibrary, false);
        setVisible(btnResource, false);
        setVisible(btnChildLibrary, false);
    }

    private void showSectionsForRole(String role) {
        switch (role) {
            case "admin":
                setVisible(sectionAdmin, true);
                setVisible(btnUsers, true);
                setVisible(btnTeachers, true);
                setVisible(btnParents, true);
                setVisible(btnEventList, true);
                setVisible(btnRegistrationList, true);
                setVisible(btnLibrary, true);
                setVisible(btnResource, true);
                break;
            case "enseignant":
                setVisible(sectionTeacher, true);
                setVisible(btnCourses, true);
                setVisible(btnStudents, true);
                break;
            case "parent":
                setVisible(sectionParent, true);
                setVisible(btnChildren, true);
                setVisible(btnEvents, true);
                break;
            case "enfant":
                setVisible(sectionChild, true);
                setVisible(btnMyCoursesChild, true);
                setVisible(btnGames, true);
                setVisible(btnChildLibrary, true);
                break;
        }
    }

    private void navigateToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/LoginView.fxml"));
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.setTitle("EduPlay — Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
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