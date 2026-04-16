package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SidebarController {

    @FXML private Label  labelUserName;
    @FXML private Label  labelUserRole;
    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;

    // Admin
    @FXML private Button btnUsers;
    @FXML private Button btnTeachers;
    @FXML private Button btnParents;
    @FXML private Button btnAdminCourses;
    @FXML private Button btnAdminSeances;

    // Enseignant
    @FXML private Button btnCourses;
    @FXML private Button btnStudents;

    // Parent
    @FXML private Button btnChildren;
    @FXML private Button btnParentCourses;
    @FXML private Button btnEvents;

    // Enfant
    @FXML private Button btnMyCoursesChild;
    @FXML private Button btnGames;

    private final List<Button> navButtons = new ArrayList<>();

    @FXML
    public void initialize() {
        labelUserName.setText(AppContext.getFullName());
        labelUserRole.setText(capitalize(AppContext.getRole()));

        collectNavButtons();
        hideAll();
        showForRole(AppContext.getRole());

        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());
    }

    // ── Actions navigation ────────────────────
    @FXML private void showDashboard()      { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile()        { Router.go("profile"); }
    @FXML private void showUsers()          { Router.go("users"); }
    @FXML private void showTeachers()       { Router.go("teachers"); }
    @FXML private void showParents()        { Router.go("parents"); }
    @FXML private void showAdminCourses()   { Router.go("admin_courses"); }
    @FXML private void showAdminSeances()   { Router.go("admin_seances"); }
    @FXML private void showTeacherCourses() { Router.go("teacher_courses"); }
    @FXML private void showStudents()       { Router.go("teacher_students"); }
    @FXML private void showChildren()       { Router.go("parent_children"); }
    @FXML private void showParentCourses()  { Router.go("parent_courses"); }
    @FXML private void showEvents()         { Router.go("parent_events"); }
    @FXML private void showMyCoursesChild() {
        // Recharger la vue pour exécuter à nouveau initialize() (cache Router sinon liste figée).
        Router.reload("child_courses");
    }
    @FXML private void showGames()          { Router.go("child_games"); }

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

    // ── Affichage des boutons selon le rôle ───
    private void hideAll() {
        for (Button b : new Button[]{
                btnUsers, btnTeachers, btnParents, btnAdminCourses, btnAdminSeances,
                btnCourses, btnStudents,
                btnChildren, btnParentCourses, btnEvents,
                btnMyCoursesChild, btnGames
        }) hide(b);
    }

    private void showForRole(String role) {
        switch (role) {
            case "admin"      -> {
                show(btnUsers);
                show(btnTeachers);
                show(btnParents);
                show(btnAdminCourses);
                show(btnAdminSeances);
            }
            case "enseignant" -> { show(btnCourses); show(btnStudents); }
            case "parent"     -> { show(btnChildren); show(btnParentCourses); show(btnEvents); }
            case "enfant"     -> { show(btnMyCoursesChild); show(btnGames); }
        }
    }

    // ── Bouton actif synchronisé avec Router ──
    public void syncActiveButton(String route) {
        navButtons.forEach(b -> { if (b.isVisible()) setInactive(b); });

        Button active = switch (route) {
            case "admin_dashboard",
                 "teacher_dashboard",
                 "parent_dashboard",
                 "child_dashboard"  -> btnDashboard;
            case "users", "teachers", "parents" -> btnUsers;
            case "admin_courses"    -> btnAdminCourses;
            case "admin_seances"    -> btnAdminSeances;
            case "teacher_courses"  -> btnCourses;
            case "teacher_students" -> btnStudents;
            case "parent_children"  -> btnChildren;
            case "parent_courses",
                 "parent_course_detail" -> btnParentCourses;
            case "parent_events"    -> btnEvents;
            case "child_courses",
                 "child_course_detail" -> btnMyCoursesChild;
            case "child_games"      -> btnGames;
            case "profile"          -> btnProfile;
            default                 -> btnDashboard;
        };

        if (active != null && active.isVisible()) setActive(active);
    }

    // ── Helpers ───────────────────────────────
    private void collectNavButtons() {
        navButtons.clear();
        for (Button b : new Button[]{
                btnDashboard, btnUsers, btnTeachers, btnParents, btnAdminCourses, btnAdminSeances,
                btnCourses, btnStudents, btnChildren, btnParentCourses, btnEvents,
                btnMyCoursesChild, btnGames, btnProfile
        }) if (b != null) navButtons.add(b);
    }

    private void show(Button b) { if (b != null) { b.setVisible(true);  b.setManaged(true);  } }
    private void hide(Button b) { if (b != null) { b.setVisible(false); b.setManaged(false); } }

    private void setActive(Button b) {
        b.setStyle("-fx-background-color:#E94560;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:8;-fx-alignment:CENTER_LEFT;-fx-padding:10 16;" +
                "-fx-border-width:0;-fx-cursor:hand;");
    }

    private void setInactive(Button b) {
        b.setStyle("-fx-background-color:transparent;-fx-text-fill:#AAAACC;" +
                "-fx-background-radius:8;-fx-alignment:CENTER_LEFT;-fx-padding:10 16;" +
                "-fx-border-width:0;-fx-cursor:hand;");
    }

    private void navigateToLogin() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/auth/LoginView.fxml")).load();
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.setTitle("EduPlay — Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur : " + e.getMessage()).showAndWait();
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}