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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SidebarController {

    /* ── Labels utilisateur ────────────────────────────────── */

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    /* ── Boutons communs ───────────────────────────────────── */

    @FXML private HBox btnDashboard;
    @FXML private HBox btnProfile;

    /* ── Boutons Admin ─────────────────────────────────────── */

    @FXML private HBox sectionAdminBox;
    @FXML private Label sectionAdmin;
    @FXML private HBox btnUsers;
    @FXML private HBox btnLibrary;
    @FXML private HBox btnResource;

    @FXML private Button btnEventList;
    @FXML private Button btnRegistrationList;

    /* ── Boutons Enseignant ────────────────────────────────── */

    @FXML private HBox sectionTeacherBox;
    @FXML private Label sectionTeacher;
    @FXML private HBox btnCourses;
    @FXML private HBox btnStudents;

    /* ── Boutons Parent ────────────────────────────────────── */

    @FXML private HBox sectionParentBox;
    @FXML private Label sectionParent;
    @FXML private HBox btnChildren;
    @FXML private HBox btnEvents;

    /* ── Boutons Enfant ────────────────────────────────────── */

    @FXML private HBox sectionChildBox;
    @FXML private Label sectionChild;
    @FXML private HBox btnMyCoursesChild;
    @FXML private HBox btnGames;
    @FXML private HBox btnChildLibrary;

    /* ── Tous les boutons de navigation (pour reset actif) ─── */

    private List<HBox> allNavButtons;

    /* ── Initialisation ────────────────────────────────────── */

    @FXML
    public void initialize() {
        // Collecte de tous les boutons nav
        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnLibrary, btnResource,
                btnCourses, btnStudents,
                btnChildren, btnEvents,
                btnMyCoursesChild, btnGames, btnChildLibrary,
                btnProfile
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());

        // Affichage utilisateur connecté
        String fullName = AppContext.getFullName();
        String role     = AppContext.getRole();

        setIfNotNull(labelUserName, fullName);
        setIfNotNull(labelUserRole, capitalize(role));
        setIfNotNull(labelUserInitials, buildInitials(fullName));

        // Masquer tout, puis afficher uniquement les sections du rôle
        hideAllRoleSections();
        showSectionsForRole(role);

        // Synchroniser le bouton actif avec la route courante
        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());
    }

    /* ── Actions de navigation ─────────────────────────────── */

    @FXML private void showDashboard()      { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile()        { Router.go("profile"); }
    @FXML private void showUsers()          { Router.go("users"); }
    @FXML private void showLibrary()        { Router.go("library"); }
    @FXML private void showResource()       { Router.go("resource"); }
    @FXML private void showChildLibrary()   { Router.go("child_library"); }

    @FXML private void showCourses()        { Router.go("teacher_courses"); }
    @FXML private void showStudents()       { Router.go("teacher_students"); }
    @FXML private void showChildren()       { Router.go("parent_children"); }
    @FXML private void showEvents()         { Router.go("parent_events"); }
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showGames()          { Router.go("child_games"); }
    @FXML private void showEventList() {Router.go("event_list");}
    @FXML private void showRegistrationList() {Router.go("registration_list");}

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

    /* ── Synchronisation bouton actif ──────────────────────── */

    public void syncActiveButton(String route) {
        // Reset tous les boutons visibles → état inactif
        allNavButtons.stream()
                .filter(b -> b != null && b.isVisible())
                .forEach(b -> {
                    b.getStyleClass().remove("modern-nav-btn-active");
                    if (!b.getStyleClass().contains("modern-nav-btn")) {
                        b.getStyleClass().add("modern-nav-btn");
                    }
                });

        // Identifier le bouton de la route active
        HBox active = switch (route) {
            case "admin_dashboard",
                 "teacher_dashboard",
                 "parent_dashboard",
                 "child_dashboard"   -> btnDashboard;
            case "users"             -> btnUsers;
            case "library"           -> btnLibrary;
            case "resource"          -> btnResource;
            case "teacher_courses"   -> btnCourses;
            case "teacher_students"  -> btnStudents;
            case "parent_children"   -> btnChildren;
            case "parent_events"     -> btnEvents;
            case "child_courses"     -> btnMyCoursesChild;
            case "child_games"       -> btnGames;
            case "child_library"     -> btnChildLibrary;
            case "profile"           -> btnProfile;
            case "event_list"        -> btnEventList;
            case "registration_list" -> btnRegistrationList;
            default                  -> btnDashboard;
        };

        // Appliquer la classe active
        if (active != null && active.isVisible()) {
            active.getStyleClass().remove("modern-nav-btn");
            if (!active.getStyleClass().contains("modern-nav-btn-active")) {
                active.getStyleClass().add("modern-nav-btn-active");
            }
        }
    }

    /* ── Visibilité sections par rôle ──────────────────────── */

    private void hideAllRoleSections() {
        // Utils admin
        setVisible(sectionAdminBox, false);
        setVisible(btnUsers,     false);
        setVisible(btnLibrary,   false);
        setVisible(btnResource,  false);

        // Sections enseignant
        setVisible(sectionTeacherBox, false);
        setVisible(btnCourses,     false);
        setVisible(btnStudents,    false);

        // Sections parent
        setVisible(sectionParentBox, false);
        setVisible(btnChildren,   false);
        setVisible(btnEvents,     false);

        // Sections enfant
        setVisible(sectionChildBox,   false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames,          false);
        setVisible(btnChildLibrary,   false);
    }

    private void showSectionsForRole(String role) {
        switch (role) {
            case "admin" -> {
                setVisible(sectionAdminBox, true);
                setVisible(btnUsers,    true);
                setVisible(btnLibrary,  true);
                setVisible(btnResource, true);
            }
            case "enseignant" -> {
                setVisible(sectionTeacherBox, true);
                setVisible(btnCourses,     true);
                setVisible(btnStudents,    true);
            }
            case "parent" -> {
                setVisible(sectionParentBox, true);
                setVisible(btnChildren,   true);
                setVisible(btnEvents,     true);
            }
            case "enfant" -> {
                setVisible(sectionChildBox,   true);
                setVisible(btnMyCoursesChild, true);
                setVisible(btnGames,          true);
                setVisible(btnChildLibrary,   true);
            }
        }
    }

    /* ── Navigation Login ──────────────────────────────────── */

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

    /* ── Utilitaires ───────────────────────────────────────── */

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