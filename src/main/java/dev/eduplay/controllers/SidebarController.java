package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.BookRequest;
import dev.eduplay.services.BookRequestService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SidebarController {

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    @FXML private Button btnDashboard;
    @FXML private Button btnProfile;

    @FXML private Label  sectionAdmin;
    @FXML private Button btnUsers;
    @FXML private Button btnTeachers;
    @FXML private Button btnParents;
    @FXML private Button btnLibrary;
    @FXML private Button btnResource;
    @FXML private Button btnStatistics;

    @FXML private Button btnEventList;
    @FXML private Button btnRegistrationList;

    @FXML private Label  sectionTeacher;
    @FXML private Button btnCourses;
    @FXML private Button btnStudents;

    @FXML private Label  sectionParent;
    @FXML private Button btnChildren;
    @FXML private Button btnEvents;

    @FXML private Label  sectionChild;
    @FXML private Button btnMyCoursesChild;
    @FXML private Button btnGames;
    @FXML private Button btnChildLibrary;

    private List<Button> allNavButtons;

    @FXML
    public void initialize() {
        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnTeachers, btnParents, btnLibrary, btnResource, btnStatistics,
                btnCourses, btnStudents,
                btnChildren, btnEvents,
                btnMyCoursesChild, btnGames, btnChildLibrary,
                btnProfile
        );

        String fullName = AppContext.getFullName();
        String role     = AppContext.getRole();

        setIfNotNull(labelUserName, fullName);
        setIfNotNull(labelUserRole, capitalize(role));
        setIfNotNull(labelUserInitials, buildInitials(fullName));

        hideAllRoleSections();
        showSectionsForRole(role);

        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());

        if ("enfant".equals(role)) {
            startNotificationCheck();
        }
    }

    private void startNotificationCheck() {
        BookRequestService brService = new BookRequestService();
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.seconds(30), e -> {
                int childId = AppContext.getUserId();
                List<BookRequest> unread = brService.getNotificationsNonLues(childId);
                for (BookRequest req : unread) {
                    showSystemNotification(req);
                    brService.marquerCommeNotifie(req.getId());
                }
            })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void showSystemNotification(BookRequest req) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bonne nouvelle ! 📚");
            alert.setHeaderText("Un livre que tu as demandé est disponible !");
            alert.setContentText("Le livre « " + req.getBookTitle() + " » a été ajouté à la bibliothèque.\nTu peux maintenant aller le lire ! ✨");
            
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.setStyle("-fx-background-color: #F0F9FF; -fx-font-family: 'Segoe UI';");
            
            alert.show();
        });
    }

    @FXML private void showDashboard()      { Router.go(AppContext.getDefaultRoute()); }
    @FXML private void showProfile()        { Router.go("profile"); }
    @FXML private void showUsers()          { Router.go("users"); }
    @FXML private void showTeachers()       { Router.go("teachers"); }
    @FXML private void showParents()        { Router.go("parents"); }
    @FXML private void showLibrary()        { Router.go("library_index"); }
    @FXML private void showResource()       { Router.go("admin_resource_index"); }
    @FXML private void showStatistics()     { Router.go("statistics_index"); }
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

    public void syncActiveButton(String route) {
        String inactiveStyle = "-fx-background-color: transparent; -fx-text-fill: #374151; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 12 16; -fx-background-radius: 12; -fx-cursor: hand;";

        allNavButtons.stream()
                .filter(b -> b != null && b.isVisible())
                .forEach(b -> b.setStyle(inactiveStyle));

        Button active = btnDashboard;
        if ("users".equals(route) || "teachers".equals(route) || "parents".equals(route)) active = btnUsers;
        else if ("teacher_courses".equals(route)) active = btnCourses;
        else if ("teacher_students".equals(route)) active = btnStudents;
        else if ("parent_children".equals(route)) active = btnChildren;
        else if ("parent_events".equals(route)) active = btnEvents;
        else if ("child_courses".equals(route)) active = btnMyCoursesChild;
        else if ("child_games".equals(route)) active = btnGames;
        else if ("library_index".equals(route)) active = btnLibrary;
        else if ("admin_resource_index".equals(route)) active = btnResource;
        else if ("statistics_index".equals(route)) active = btnStatistics;
        else if ("child_library".equals(route)) active = btnChildLibrary;
        else if ("profile".equals(route)) active = btnProfile;
        else if ("event_list".equals(route)) active = btnEventList;
        else if ("registration_list".equals(route)) active = btnRegistrationList;
        else if (route.contains("dashboard")) active = btnDashboard;

        if (active != null && active.isVisible()) {
            active.getStyleClass().remove("nav-btn");
            if (!active.getStyleClass().contains("nav-btn-active")) {
                active.getStyleClass().add("nav-btn-active");
            }
        }
    }

    private void hideAllRoleSections() {
        setVisible(sectionAdmin, false);
        setVisible(btnUsers,     false);
        setVisible(btnTeachers,  false);
        setVisible(btnParents,   false);
        setVisible(btnLibrary,   false);
        setVisible(btnResource,  false);
        setVisible(btnStatistics, false);
        setVisible(sectionTeacher, false);
        setVisible(btnCourses,     false);
        setVisible(btnStudents,    false);
        setVisible(sectionParent, false);
        setVisible(btnChildren,   false);
        setVisible(btnEvents,     false);
        setVisible(sectionChild,      false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames,          false);
        setVisible(btnChildLibrary,   false);
    }

    private void showSectionsForRole(String role) {
        switch (role) {
            case "admin" -> {
                setVisible(sectionAdmin, true);
                setVisible(btnUsers,    true);
                setVisible(btnTeachers, true);
                setVisible(btnParents,  true);
                setVisible(btnLibrary,    true);
                setVisible(btnResource,   true);
                setVisible(btnStatistics, true);
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

    private void navigateToLogin() {
        try {
            Parent root = new FXMLLoader(getClass().getResource("/views/auth/LoginView.fxml")).load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.setTitle("EduPlay — Connexion");
            stage.centerOnScreen();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur navigation : " + e.getMessage()).showAndWait();
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