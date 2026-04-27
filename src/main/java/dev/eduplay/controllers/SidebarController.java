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

    /* ── Boutons communs ───────────────────────────────────── */

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;

    /* ── Boutons Admin ─────────────────────────────────────── */

    @FXML private Label  sectionAdmin;
    @FXML private Button btnUsers;
    @FXML private Button btnTeachers;
    @FXML private Button btnParents;

    /* ── Boutons Enseignant ────────────────────────────────── */

    @FXML private Label  sectionTeacher;
    @FXML private Button btnCourses;
    @FXML private Button btnStudents;
    @FXML private Button btnLevel;
    @FXML private Button BtnGames;

    /* ── Boutons Parent ────────────────────────────────────── */

    @FXML private Label  sectionParent;
    @FXML private Button btnChildren;
    @FXML private Button btnEvents;
    @FXML private HBox btnDashboard;
    @FXML private HBox btnProfile;

    @FXML private HBox sectionAdminBox;
    @FXML private Label sectionAdmin;
    @FXML private HBox btnUsers;
    @FXML private HBox btnLibrary;
    @FXML private HBox btnResource;

    @FXML private Label  sectionChild;
    @FXML private Button btnMyCoursesChild;
    @FXML private Button btnGames;
    @FXML private Button Games_children;
    @FXML private HBox sectionTeacherBox;
    @FXML private Label sectionTeacher;
    @FXML private HBox btnCourses;
    @FXML private HBox btnStudents;

    @FXML private HBox sectionParentBox;
    @FXML private Label sectionParent;
    @FXML private HBox btnChildren;
    @FXML private HBox btnEvents;

    @FXML private HBox sectionChildBox;
    @FXML private Label sectionChild;
    @FXML private HBox btnMyCoursesChild;
    @FXML private HBox btnGames;
    @FXML private HBox btnChildLibrary;

    private final UserService userService = new UserService();
    private List<HBox> allNavButtons;

    @FXML
    public void initialize() {

        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnLibrary, btnResource,
                btnCourses, btnStudents,
                btnChildren, btnEvents,
                btnMyCoursesChild, btnGames, btnChildLibrary,
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

    @FXML private void showDashboard() { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile() { Router.go("profile"); }
    @FXML private void showUsers() { Router.go("users"); }
    @FXML private void showLibrary() { Router.go("library"); }
    @FXML private void showResource() { Router.go("resource"); }
    @FXML private void showChildLibrary() { Router.go("child_library"); }

    @FXML private void showCourses() { Router.go("teacher_courses"); }
    @FXML private void showStudents() { Router.go("teacher_students"); }
    @FXML private void showChildren() { Router.go("parent_children"); }
    @FXML private void showEvents() { Router.go("parent_events"); }
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showLevels()          { Router.go("levels_list"); }
    @FXML private void showGames() { Router.go("games_list"); }
    @FXML private void showGamesFront() { Router.go("child_games"); }

    @FXML private void showGames() { Router.go("child_games"); }

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

            case "teacher_courses" -> btnCourses;
            case "teacher_students" -> btnStudents;

            case "parent_children" -> btnChildren;
            case "parent_events" -> btnEvents;

            case "child_courses" -> btnMyCoursesChild;
            case "child_games" -> btnGames;
            case "child_library" -> btnChildLibrary;

            case "profile" -> btnProfile;

            default -> btnDashboard;
        };

        if (active != null && active.isVisible()) {
            active.getStyleClass().add("modern-nav-btn-active");
        }
    }

    /* ROLE VISIBILITY */

    private void hideAllRoleSections() {
        // Sections admin
        setVisible(sectionAdmin, false);
        setVisible(btnUsers,     false);
        setVisible(btnTeachers,  false);
        setVisible(btnParents,   false);

        // Sections enseignant
        setVisible(sectionTeacher, false);
        setVisible(btnCourses,     false);
        setVisible(btnStudents,    false);
        setVisible(btnLevel,    false);
        setVisible(btnGames,    false);


        // Sections parent
        setVisible(sectionParent, false);
        setVisible(btnChildren,   false);
        setVisible(btnEvents,     false);

        // Sections enfant
        setVisible(sectionChild,      false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames,          false);
        setVisible(Games_children,          false);
        setVisible(sectionAdminBox, false);
        setVisible(btnUsers, false);
        setVisible(btnLibrary, false);
        setVisible(btnResource, false);

        setVisible(sectionTeacherBox, false);
        setVisible(btnCourses, false);
        setVisible(btnStudents, false);

        setVisible(sectionParentBox, false);
        setVisible(btnChildren, false);
        setVisible(btnEvents, false);

        setVisible(sectionChildBox, false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames, false);
        setVisible(btnChildLibrary, false);
    }

    private void showSectionsForRole(String role) {
        switch (role) {
            case "admin" -> {
                setVisible(sectionAdminBox, true);
                setVisible(btnUsers, true);
                setVisible(btnLibrary, true);
                setVisible(btnResource, true);
            }
            case "enseignant" -> {
                setVisible(sectionTeacher, true);
                setVisible(btnCourses,     true);
                setVisible(btnStudents,    true);
                setVisible(btnGames,          true);
                setVisible(btnLevel,    true);

            }
            case "parent" -> {
                setVisible(sectionParent, true);
                setVisible(btnChildren,   true);
                setVisible(btnEvents,     true);

            }
            case "enfant" -> {
                setVisible(sectionChild,      true);
                setVisible(Games_children,          true);

                setVisible(sectionTeacherBox, true);
                setVisible(btnCourses, true);
                setVisible(btnStudents, true);
            }
            case "parent" -> {
                setVisible(sectionParentBox, true);
                setVisible(btnChildren, true);
                setVisible(btnEvents, true);
            }
            case "enfant" -> {
                setVisible(sectionChildBox, true);
                setVisible(btnMyCoursesChild, true);
                setVisible(btnGames, true);
                setVisible(btnChildLibrary, true);
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