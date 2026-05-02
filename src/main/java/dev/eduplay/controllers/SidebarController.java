package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.core.SessionManager;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SidebarController {

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    @FXML private HBox btnDashboard;
    @FXML private HBox btnProfile;

    /* ── Sections ── */
    @FXML private HBox sectionAdminBox;
    @FXML private Label sectionAdmin;
    @FXML private HBox btnUsers;
    @FXML private HBox btnLibrary;
    @FXML private HBox btnResource;

    @FXML private HBox sectionTeacherBox;
    @FXML private Label sectionTeacher;
    @FXML private HBox btnCourses;
    @FXML private HBox btnLevel;
    @FXML private HBox btnGames;

    @FXML private HBox sectionParentBox;
    @FXML private Label sectionParent;
    @FXML private HBox btnChildren;
    @FXML private HBox btnParentSeances;
    @FXML private HBox btnEvents;

    @FXML private HBox sectionChildBox;
    @FXML private Label sectionChild;
    @FXML private HBox btnMyCoursesChild;
    @FXML private HBox btnChildLibrary;

    // Admin extras
    @FXML private HBox btnCalendar;
    @FXML private HBox btnStats;

    // Teacher extras
    @FXML private HBox btnSeances;

    private final UserService userService = new UserService();
    private List<HBox> allNavButtons;

    @FXML
    public void initialize() {

        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnLibrary, btnResource, btnCalendar, btnStats,
                btnCourses, btnLevel, btnGames, btnSeances,
                btnChildren, btnParentSeances, btnEvents,
                btnMyCoursesChild, btnChildLibrary,
                btnProfile
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());

        String fullName = AppContext.getFullName();
        String role = AppContext.getRole();

        setIfNotNull(labelUserName, fullName);
        setIfNotNull(labelUserRole, role);
        setIfNotNull(labelUserInitials, buildInitials(fullName));

        hideAllRoleSections();
        showSectionsForRole(role);

        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());
    }

    /* NAVIGATION */

    @FXML private void showDashboard()       { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile()         { Router.go("profile"); }
    @FXML private void showUsers()           { Router.go("users"); }
    @FXML private void showLibrary()         { Router.go("library"); }
    @FXML private void showResource()        { Router.go("resource"); }
    @FXML private void showCalendar()        { Router.go("admin_calendar"); }
    @FXML private void showStats()           { Router.go("admin_stats"); }
    @FXML private void showChildLibrary()    { Router.go("child_library"); }
    @FXML private void showTeacherSeances()  { Router.go("teacher_seances"); }
    @FXML private void showParentSeances()   { Router.go("parent_seances"); }

    @FXML private void showCourses()     { Router.go("teacher_courses"); }
    @FXML private void showStudents()    { Router.go("teacher_students"); }
    @FXML private void showChildren()    { Router.go("parent_children"); }
    @FXML private void showEvents()      { Router.go("child_seances"); }
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showLevels()      { Router.go("levels_list"); }
    @FXML private void showGames() {
        // Teachers go to game management list; children go to game play list
        if (AppContext.isTeacher() || AppContext.isAdmin()) {
            Router.go("games_list");
        } else {
            Router.go("child_games");
        }
    }
    @FXML private void showGamesFront() { Router.go("child_games"); }

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Voulez-vous vraiment vous déconnecter ?", ButtonType.YES, ButtonType.NO);
        
        // Supprimer la barre du haut (titre, croix de fermeture)
        confirm.initStyle(javafx.stage.StageStyle.UNDECORATED);
        // Supprimer l'icône (?)
        confirm.setGraphic(null);
        
        confirm.setHeaderText("Confirmation de déconnexion");

        try {
            javafx.scene.control.DialogPane dialogPane = confirm.getDialogPane();
            // Assure la suppression complète de l'icône dans l'en-tête
            dialogPane.setGraphic(null);
            
            dialogPane.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
            dialogPane.getStyleClass().add("modern-dialog");
            
            // Personnalisation des boutons
            ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.YES)).setText("Oui, me déconnecter");
            ((javafx.scene.control.Button) dialogPane.lookupButton(ButtonType.NO)).setText("Annuler");
        } catch (Exception e) {
            System.err.println("Impossible de charger les styles pour le popup de déconnexion.");
        }

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AppContext.clear();
                Router.clearCache();
                navigateToLogin();
            }
        });
    }

    /* ACTIVE BUTTON */

    public void syncActiveButton(String route) {

        allNavButtons.stream()
                .filter(b -> b != null && b.isVisible())
                .forEach(b -> {
                    b.getStyleClass().remove("modern-nav-btn-active");
                    if (!b.getStyleClass().contains("modern-nav-btn")) {
                        b.getStyleClass().add("modern-nav-btn");
                    }
                });

        HBox active = switch (route) {
            case "admin_dashboard",
                 "teacher_dashboard",
                 "parent_dashboard",
                 "child_dashboard" -> btnDashboard;

            case "users" -> btnUsers;
            case "library" -> btnLibrary;
            case "resource" -> btnResource;
            case "admin_calendar" -> btnCalendar;
            case "admin_stats" -> btnStats;

            case "teacher_courses" -> btnCourses;
            case "teacher_seances" -> btnSeances;

            case "parent_children" -> btnChildren;
            case "parent_seances" -> btnParentSeances;

            case "child_courses" -> btnMyCoursesChild;
            case "child_games" -> btnGames;
            case "child_library" -> btnChildLibrary;
            case "child_seances" -> btnEvents;

            case "profile" -> btnProfile;

            default -> btnDashboard;
        };

        if (active != null && active.isVisible()) {
            active.getStyleClass().add("modern-nav-btn-active");
        }
    }

    /* ROLE VISIBILITY */

    private void hideAllRoleSections() {
        setVisible(sectionAdminBox, false);
        setVisible(sectionAdmin, false);
        setVisible(btnUsers, false);
        setVisible(btnLibrary, false);
        setVisible(btnResource, false);
        setVisible(btnCalendar, false);
        setVisible(btnStats, false);

        setVisible(sectionTeacherBox, false);
        setVisible(sectionTeacher, false);
        setVisible(btnCourses, false);
        setVisible(btnLevel, false);
        setVisible(btnGames, false);
        setVisible(btnSeances, false);

        setVisible(sectionParentBox, false);
        setVisible(sectionParent, false);
        setVisible(btnChildren, false);
        setVisible(btnParentSeances, false);
        setVisible(btnEvents, false);

        setVisible(sectionChildBox, false);
        setVisible(sectionChild, false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnChildLibrary, false);
    }

    private void showSectionsForRole(String role) {
        if (role == null) return;
        switch (role) {
            case "admin" -> {
                setVisible(sectionAdminBox, true);
                setVisible(sectionAdmin, true);
                setVisible(btnUsers, true);
                setVisible(btnLibrary, true);
                setVisible(btnResource, true);
                setVisible(btnCalendar, true);
                setVisible(btnStats, true);
            }
            case "enseignant" -> {
                setVisible(sectionTeacherBox, true);
                setVisible(sectionTeacher, true);
                setVisible(btnCourses, true);
                setVisible(btnGames, true);
                setVisible(btnLevel, true);
                setVisible(btnSeances, true);
            }
            case "parent" -> {
                setVisible(sectionParentBox, true);
                setVisible(sectionParent, true);
                setVisible(btnChildren, true);
                setVisible(btnParentSeances, true);
            }
            case "enfant" -> {
                setVisible(sectionChildBox, true);
                setVisible(sectionChild, true);
                setVisible(btnMyCoursesChild, true);
                setVisible(btnGames, true);
                setVisible(btnChildLibrary, true);
                setVisible(btnEvents, true);
            }
        }
    }

    /* LOGIN NAV */

    private void navigateToLogin() {
        try {
            Parent root = new FXMLLoader(
                    getClass().getResource("/views/auth/LoginView.fxml")).load();

            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));

        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).show();
        }
    }

    /* UTILS */

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
        String[] parts = fullName.split(" ");
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }


}