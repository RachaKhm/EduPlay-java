package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.BookRequest;
import dev.eduplay.services.BookRequestService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SidebarController {

    @FXML private Label labelUserName;
    @FXML private Label labelUserRole;
    @FXML private Label labelUserInitials;

    /* ── COMMON ──────────────────────────────── */
    @FXML private Node btnDashboard;
    @FXML private Node btnLogout;

    /* ── ADMIN ──────────────────────────────── */
    @FXML private Node sectionAdminBox;
    @FXML private Node sectionContentBox;
    @FXML private Node btnUsers;
    @FXML private Node btnTeachers;
    @FXML private Node btnParents;
    @FXML private Node btnAdminCourses;
    @FXML private Node btnLibrary;
    @FXML private Node btnProducts;
    @FXML private Node btnGamesAdmin;
    @FXML private Node btnBookRequests;
    @FXML private Node btnResource;
    
    @FXML private Node sectionEventsBox;
    @FXML private Node btnEventList;
    @FXML private Node btnAddEvent;
    @FXML private Node btnRegistrationList;
    @FXML private Node btnScanner;
    @FXML private Node btnStatistics;
    @FXML private Node btnCalendar;

    /* ── TEACHER ───────────────────────────── */
    @FXML private Node sectionTeacherBox;
    @FXML private Node btnCourses;
    @FXML private Node btnLevel;
    @FXML private Node btnGamesTeacher;
    @FXML private Node btnSeances;

    /* ── PARENT ─────────────────────────────── */
    @FXML private Node sectionParentBox;
    @FXML private Node btnChildren;
    @FXML private Node btnParentChat;
    @FXML private Node btnParentOrders;
    @FXML private Node btnMarketplaceParent;
    @FXML private Node btnParentCart;
    
    @FXML private Node sectionParentEventsBox;
    @FXML private Node btnParentEventList;
    @FXML private Node btnParentSeances;
    @FXML private Node btnParentRegistrations;

    /* ── CHILD ──────────────────────────────── */
    @FXML private Node sectionChildBox;
    @FXML private Node sectionChildLibraryBox;
    @FXML private Node btnMyCoursesChild;
    @FXML private Node btnGames;
    @FXML private Node btnChildLibrary;
    @FXML private Node btnEvents;

    private List<Node> allNavButtons;

    @FXML
    public void initialize() {
        // Initialize the list of all buttons for highlighting logic
        allNavButtons = Arrays.asList(
                btnDashboard,
                btnUsers, btnTeachers, btnParents, btnAdminCourses, btnLibrary, btnProducts, btnBookRequests, btnResource, btnEventList, btnAddEvent, btnRegistrationList, 
                btnScanner, btnStatistics, btnCalendar,
                btnCourses, btnLevel, btnGamesTeacher, btnSeances,
                btnChildren, btnParentEventList, btnParentSeances, btnParentRegistrations,
                btnMarketplaceParent, btnParentCart, btnParentOrders, btnParentChat,
                btnGamesAdmin,
                btnMyCoursesChild, btnGames, btnChildLibrary, btnEvents, btnLogout
        ).stream().filter(Objects::nonNull).collect(Collectors.toList());

        // Set user info
        String fullName = AppContext.getFullName();
        String role = AppContext.getRole();

        setIfNotNull(labelUserName, fullName);
        setIfNotNull(labelUserRole, role);
        setIfNotNull(labelUserInitials, buildInitials(fullName));

        // Manage visibility
        hideAllRoleSections();
        showSectionsForRole(role);

        // Setup routing sync
        Router.setOnRouteChange(this::syncActiveButton);
        syncActiveButton(Router.getCurrentRoute());

        // Book notification check for children
        if ("enfant".equalsIgnoreCase(role)) {
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
            try {
                Stage stage = (Stage) btnDashboard.getScene().getWindow();
                StackPane root = (StackPane) stage.getScene().getRoot();

                VBox toast = new VBox(5);
                toast.setStyle("-fx-background-color: #1E293B; -fx-padding: 15; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");
                toast.setMaxWidth(300);
                toast.setMaxHeight(Region.USE_PREF_SIZE);
                
                Label title = new Label("📚 Livre Disponible !");
                title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                
                Label desc = new Label("Votre demande pour '" + req.getBookTitle() + "' a été acceptée.");
                desc.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 12px;");
                desc.setWrapText(true);

                toast.getChildren().addAll(title, desc);
                
                StackPane.setAlignment(toast, Pos.TOP_RIGHT);
                StackPane.setMargin(toast, new Insets(20));
                
                root.getChildren().add(toast);

                // Animation
                toast.setOpacity(0);
                toast.setTranslateY(-20);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(500), toast);
                fadeIn.setToValue(1);
                
                TranslateTransition slideDown = new TranslateTransition(Duration.millis(500), toast);
                slideDown.setToY(0);

                SequentialTransition show = new SequentialTransition(new ParallelTransition(fadeIn, slideDown));
                
                PauseTransition delay = new PauseTransition(Duration.seconds(5));
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toast);
                fadeOut.setToValue(0);
                
                show.setOnFinished(ev -> {
                    delay.play();
                    delay.setOnFinished(ev2 -> {
                        fadeOut.play();
                        fadeOut.setOnFinished(ev3 -> root.getChildren().remove(toast));
                    });
                });
                
                show.play();

            } catch (Exception e) {
                System.err.println("Failed to show notification: " + e.getMessage());
            }
        });
    }

    /* ───────────────── NAVIGATION ───────────────── */

    @FXML private void showDashboard() { Router.go(AppContext.getDefaultRoute()); }
    
    // ADMIN
    @FXML private void showUsers() { Router.go("users"); }
    @FXML private void showTeachers() { Router.go("teachers"); }
    @FXML private void showParents() { Router.go("parents"); }
    @FXML private void showAdminCourses() { Router.go("library"); }
    @FXML private void showLibrary() { Router.go("library_index"); }
    @FXML private void showProducts() { Router.go("admin_product_index"); }
    @FXML private void showBookRequests() { Router.go("book_requests_index"); }
    @FXML private void showResource() { Router.go("resource"); }
    @FXML private void showEventList() { Router.go("event_list"); }
    @FXML private void showAddEvent() { Router.go("add_event"); }
    @FXML private void showRegistrationList() { Router.go("registration_list"); }
    @FXML private void showScanner() { Router.go("scanner"); }
    @FXML private void showStatistics() { Router.go("statistics"); }
    @FXML private void showCalendar() { Router.go("admin_calendar"); }

    // TEACHER
    @FXML private void showCourses() { Router.go("teacher_courses"); }
    @FXML private void showLevels() { Router.go("levels_list"); }
    @FXML private void showGamesTeacher() { Router.go("games_list"); }
    @FXML private void showTeacherSeances() { Router.go("teacher_seances"); }

    // PARENT
    @FXML private void showChildren() { Router.go("parent_children"); }
    @FXML private void showParentEventList() { Router.go("parent_event_list"); }
    @FXML private void showParentSeances() { Router.go("parent_seances"); }
    @FXML private void showParentRegistrations() { Router.go("parent_registrations"); }
    @FXML private void showParentMarketplace() { Router.go("parent_marketplace"); }
    @FXML private void showParentOrders() { Router.go("parent_orders"); }
    @FXML private void showParentCart() { Router.go("parent_cart"); }
    @FXML private void showParentChat() { Router.go("parent_chat"); }

    // ADMIN - games
    @FXML private void showGamesAdmin() { Router.go("games_list"); }

    // CHILD
    @FXML private void showMyCoursesChild() { Router.go("child_courses"); }
    @FXML private void showGames() { Router.go("child_games"); }
    @FXML private void showChildLibrary() { Router.go("child_library"); }
    @FXML private void showEvents() { Router.go("child_seances"); }

    /* ───────────────── LOGOUT ───────────────── */

    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("EduPlay - Déconnexion");
        confirm.setHeaderText("Déconnexion");
        confirm.setContentText("Voulez-vous vraiment vous déconnecter ?");

        // Custom Buttons
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        // Styling the Dialog
        DialogPane dialogPane = confirm.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        dialogPane.getStyleClass().add("modern-dialog");

        // Set Stage icon/title if needed, but Alert handles most
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.setAlwaysOnTop(true);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                AppContext.clear();
                Router.clearCache();
                navigateToLogin();
            }
        });
    }

    /* ───────────────── ROUTE SYNC ───────────────── */

    public void syncActiveButton(String route) {
        if (allNavButtons == null) return;

        allNavButtons.forEach(b -> b.getStyleClass().remove("nav-btn-active"));

        Node active = switch (route) {
            case "admin_dashboard", "teacher_dashboard", "parent_dashboard", "child_dashboard" -> btnDashboard;
            case "users" -> btnUsers;
            case "teachers" -> btnTeachers;
            case "parents" -> btnParents;
            case "library" -> btnAdminCourses;
            case "library_index" -> btnLibrary;
            case "admin_product_index" -> btnProducts;
            case "book_requests_index" -> btnBookRequests;
            case "resource" -> btnResource;
            case "event_list" -> btnEventList;
            case "add_event" -> btnAddEvent;
            case "registration_list" -> btnRegistrationList;
            case "scanner" -> btnScanner;
            case "statistics" -> btnStatistics;
            case "admin_calendar" -> btnCalendar;
            case "teacher_courses" -> btnCourses;
            case "levels_list" -> btnLevel;
            case "games_list" -> (btnGamesAdmin != null ? btnGamesAdmin : btnGamesTeacher);
            case "teacher_seances" -> btnSeances;
            case "parent_children" -> btnChildren;
            case "parent_event_list" -> btnParentEventList;
            case "parent_seances" -> btnParentSeances;
            case "parent_registrations" -> btnParentRegistrations;
            case "parent_marketplace" -> btnMarketplaceParent;
            case "parent_cart" -> btnParentCart;
            case "parent_chat" -> btnParentChat;
            case "child_courses" -> btnMyCoursesChild;
            case "child_games" -> btnGames;
            case "child_library" -> btnChildLibrary;
            case "child_seances" -> btnEvents;
            default -> null;
        };

        if (active != null && active.isVisible()) {
            active.getStyleClass().add("nav-btn-active");
        }
    }

    /* ───────────────── ROLE VISIBILITY ───────────────── */

    private void hideAllRoleSections() {
        setVisible(sectionAdminBox, false);
        setVisible(sectionContentBox, false);
        setVisible(btnUsers, false);
        setVisible(btnTeachers, false);
        setVisible(btnParents, false);
        setVisible(btnAdminCourses, false);
        setVisible(btnLibrary, false);
        setVisible(btnProducts, false);
        setVisible(btnBookRequests, false);
        setVisible(btnResource, false);
        setVisible(sectionEventsBox, false);
        setVisible(btnEventList, false);
        setVisible(btnAddEvent, false);
        setVisible(btnRegistrationList, false);
        setVisible(btnScanner, false);
        setVisible(btnStatistics, false);
        setVisible(btnCalendar, false);

        setVisible(sectionTeacherBox, false);
        setVisible(btnCourses, false);
        setVisible(btnLevel, false);
        setVisible(btnGamesTeacher, false);
        setVisible(btnSeances, false);

        setVisible(sectionParentBox, false);
        setVisible(btnChildren, false);
        setVisible(sectionParentEventsBox, false);
        setVisible(btnParentEventList, false);
        setVisible(btnParentSeances, false);
        setVisible(btnParentRegistrations, false);
        
        setVisible(btnMarketplaceParent, false);
        setVisible(btnParentCart, false);
        setVisible(btnParentOrders, false);
        setVisible(btnParentChat, false);

        setVisible(sectionChildBox, false);
        setVisible(sectionChildLibraryBox, false);
        setVisible(btnMyCoursesChild, false);
        setVisible(btnGames, false);
        setVisible(btnChildLibrary, false);
        setVisible(btnEvents, false);
    }

    private void showSectionsForRole(String role) {
        if (role == null) return;
        switch (role.toLowerCase()) {
            case "admin" -> {
                setVisible(sectionAdminBox, true);
                setVisible(sectionContentBox, true);
                setVisible(btnUsers, true);
                setVisible(btnTeachers, true);
                setVisible(btnParents, true);
                setVisible(btnAdminCourses, true);
                setVisible(btnLibrary, true);
                setVisible(btnProducts, true);
                setVisible(btnGamesAdmin, true);
                setVisible(btnBookRequests, true);
                setVisible(btnResource, true);
                setVisible(sectionEventsBox, true);
                setVisible(btnEventList, true);
                setVisible(btnAddEvent, true);
                setVisible(btnRegistrationList, true);
                setVisible(btnScanner, true);
                setVisible(btnStatistics, true);
                setVisible(btnCalendar, true);
            }
            case "enseignant" -> {
                setVisible(sectionTeacherBox, true);
                setVisible(btnCourses, true);
                setVisible(btnLevel, true);
                setVisible(btnGamesTeacher, true);
                setVisible(btnSeances, true);
            }
            case "parent" -> {
                setVisible(sectionParentBox, true);
                setVisible(btnChildren, true);
                setVisible(sectionParentEventsBox, true);
                setVisible(btnParentEventList, true);
                setVisible(btnParentSeances, true);
                setVisible(btnParentRegistrations, true);
                setVisible(btnMarketplaceParent, true);
                setVisible(btnParentCart, true);
                setVisible(btnParentChat, true);
                setVisible(btnParentOrders, true);
            }
            case "enfant" -> {
                setVisible(sectionChildBox, true);
                setVisible(sectionChildLibraryBox, true);
                setVisible(btnMyCoursesChild, true);
                setVisible(btnGames, true);
                setVisible(btnChildLibrary, true);
                setVisible(btnEvents, true);
            }
        }
    }

    /* ───────────────── LOGIN ───────────────── */

    private void navigateToLogin() {
        try {
            Parent root = new FXMLLoader(getClass().getResource("/views/auth/LoginView.fxml")).load();
            Stage stage = (Stage) btnDashboard.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
            stage.centerOnScreen();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur navigation: " + e.getMessage()).show();
        }
    }

    /* ───────────────── UTILS ───────────────── */

    private void setVisible(Node node, boolean value) {
        if (node != null) {
            node.setVisible(value);
            node.setManaged(value);
        }
    }

    private void setIfNotNull(Label label, String text) {
        if (label != null && text != null) label.setText(text);
    }

    private String buildInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] p = name.trim().split("\\s+");
        if (p.length == 1) return p[0].substring(0, 1).toUpperCase();
        return (p[0].charAt(0) + "" + p[p.length - 1].charAt(0)).toUpperCase();
    }
}