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

/**
 * SidebarController
 * ─────────────────────────────────────────────────────────────
 * Gère la sidebar dynamique commune à tous les rôles.
 *
 * Logique de rendu :
 *   1. Affiche les initiales, nom et rôle de l'utilisateur
 *   2. Masque les boutons/sections non applicables au rôle
 *   3. Synchronise le bouton actif avec Router.setOnRouteChange()
 *
 * Styles actif/inactif : définis dans app.css
 *   .nav-btn        → état par défaut
 *   .nav-btn-active → bouton de la route courante
 * ─────────────────────────────────────────────────────────────
 */
public class SidebarController {

    /* ── Labels utilisateur ────────────────────────────────── */

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    /* ── Boutons communs ───────────────────────────────────── */

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;

    /* ── Boutons Admin ─────────────────────────────────────── */

    @FXML private Label  sectionAdmin;
    @FXML private Button btnUsers;
    @FXML private Button btnTeachers;
    @FXML private Button btnParents;
    @FXML private Button btnLibrary;
    @FXML private Button btnResource;

    /* ── Boutons Enseignant ────────────────────────────────── */

    @FXML private Label  sectionTeacher;
    @FXML private Button btnCourses;
    @FXML private Button btnStudents;

    /* ── Boutons Parent ────────────────────────────────────── */

    @FXML private Label  sectionParent;
    @FXML private Button btnChildren;
    @FXML private Button btnEvents;

    /* ── Boutons Enfant ────────────────────────────────────── */

    @FXML private Label  sectionChild;
    @FXML private Button btnMyCoursesChild;
    @FXML private Button btnGames;
    @FXML private Button btnChildLibrary;

    /* ── Tous les boutons de navigation (pour reset actif) ─── */

    private List<Button> allNavButtons;

    /* ── Initialisation ────────────────────────────────────── */

    @FXML
    public void initialize() {
        // Collecte de tous les boutons nav
        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnTeachers, btnParents, btnLibrary, btnResource,
                btnCourses, btnStudents,
                btnChildren, btnEvents,
                btnMyCoursesChild, btnGames, btnChildLibrary,
                btnProfile
        );

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
    @FXML private void showTeachers()       { Router.go("teachers"); }
    @FXML private void showParents()        { Router.go("parents"); }
    @FXML private void showLibrary()        { Router.go("library_index"); }
    @FXML private void showResource()       { Router.go("admin_resource_index"); }
    @FXML private void showCourses()        { Router.go("teacher_courses"); }
    @FXML private void showStudents()       { Router.go("teacher_students"); }
    @FXML private void showChildren()       { Router.go("parent_children"); }
    @FXML private void showEvents()         { Router.go("parent_events"); }
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showGames()          { Router.go("child_games"); }
    @FXML private void showChildLibrary()   { Router.go("child_library"); }

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

    /**
     * Retire la classe active de tous les boutons,
     * puis l'applique au bouton correspondant à la route.
     * Utilise les classes CSS de app.css (.nav-btn / .nav-btn-active).
     */
    public void syncActiveButton(String route) {
        String activeStyle = "-fx-background-color: #EFF6FF; -fx-border-color: #DBEAFE; -fx-border-width: 1; -fx-border-radius: 12; -fx-background-radius: 12; -fx-text-fill: #2563EB; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-cursor: hand;";
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-background-radius: 12; -fx-cursor: hand;";

        allNavButtons.stream()
                .filter(b -> b != null && b.isVisible())
                .forEach(b -> b.setStyle(inactiveStyle));

        // Identifier le bouton de la route active
        Button active = switch (route) {
            case "admin_dashboard",
                 "teacher_dashboard",
                 "parent_dashboard",
                 "child_dashboard"   -> btnDashboard;
            case "users",
                 "teachers",
                 "parents"           -> btnUsers;
            case "teacher_courses"   -> btnCourses;
            case "teacher_students"  -> btnStudents;
            case "parent_children"   -> btnChildren;
            case "parent_events"     -> btnEvents;
            case "child_courses"     -> btnMyCoursesChild;
            case "child_games"       -> btnGames;
            case "library_index"     -> btnLibrary;
            case "admin_resource_index" -> btnResource;
            case "child_library"     -> btnChildLibrary;
            case "profile"           -> btnProfile;
            default                  -> btnDashboard;
        };

        // Appliquer la classe active
        if (active != null && active.isVisible()) {
            active.setStyle(activeStyle);
        }
    }

    /* ── Visibilité sections par rôle ──────────────────────── */

    /**
     * Masque toutes les sections et boutons spécifiques aux rôles.
     * Appelé en premier dans initialize(), avant showSectionsForRole().
     */
    private void hideAllRoleSections() {
        // Sections admin
        setVisible(sectionAdmin, false);
        setVisible(btnUsers,     false);
        setVisible(btnTeachers,  false);
        setVisible(btnParents,   false);
        setVisible(btnLibrary,   false);
        setVisible(btnResource,  false);

        // Sections enseignant
        setVisible(sectionTeacher, false);
        setVisible(btnCourses,     false);
        setVisible(btnStudents,    false);

        // Sections parent
        setVisible(sectionParent, false);
        setVisible(btnChildren,   false);
        setVisible(btnEvents,     false);

        // Sections enfant
        setVisible(sectionChild,      false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames,          false);
        setVisible(btnChildLibrary,   false);
    }

    /**
     * Affiche uniquement les sections correspondant au rôle.
     *
     * @param role admin | enseignant | parent | enfant
     */
    private void showSectionsForRole(String role) {
        switch (role) {
            case "admin" -> {
                setVisible(sectionAdmin, true);
                setVisible(btnUsers,    true);
                setVisible(btnTeachers, true);
                setVisible(btnParents,  true);
                setVisible(btnLibrary,  true);
                setVisible(btnResource, true);
            }
            case "enseignant" -> {
                setVisible(sectionTeacher, true);
                setVisible(btnCourses,     true);
                setVisible(btnStudents,    true);
            }
            case "parent" -> {
                setVisible(sectionParent, true);
                setVisible(btnChildren,   true);
                setVisible(btnEvents,     true);
            }
            case "enfant" -> {
                setVisible(sectionChild,      true);
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

    /**
     * Affiche ou masque un nœud en gérant aussi managed
     * (pour ne pas occuper d'espace quand invisible).
     */
    private void setVisible(javafx.scene.Node node, boolean visible) {
        if (node != null) {
            node.setVisible(visible);
            node.setManaged(visible);
        }
    }

    private void setIfNotNull(Label label, String text) {
        if (label != null) label.setText(text);
    }

    /** Génère les initiales depuis le nom complet (ex: "John Doe" → "JD"). */
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