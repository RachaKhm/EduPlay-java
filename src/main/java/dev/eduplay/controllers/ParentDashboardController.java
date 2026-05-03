package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.entities.Subscription;
import dev.eduplay.entities.User;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.GroqRecommendationService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import dev.eduplay.services.UserService;
import javafx.application.Platform;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Notification;
import dev.eduplay.services.NotificationService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.sql.SQLException;
import java.util.List;

/**
 * Dashboard Parent — affiche les séances des enfants + recommandations Groq IA.
 */
public class ParentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label subtitleLabel;
    @FXML private Label totalChildrenLabel;
    @FXML private Label totalSeancesLabel;
    @FXML private Label totalCoursesLabel;

    @FXML private VBox kidsSeancesContainer;

    @FXML private TextArea aiPromptField;
    @FXML private Button aiAskBtn;
    @FXML private VBox aiResponseBox;
    @FXML private Label aiResponseLabel;
    @FXML private Label aiLoadingLabel;
    @FXML private Label aiErrorLabel;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("EEE dd MMM · HH:mm", Locale.FRENCH);

    @FXML private VBox notificationsContainer;
    @FXML private Button clearAllBtn;
    @FXML private Label notificationBadge;
    @FXML private ScrollPane notificationsScrollPane;

    private NotificationService notificationService;
    private final UserService userService = new UserService();
    private int currentParentId;

    @FXML
    public void initialize() {
        String name = AppContext.getFullName();
        if (welcomeLabel != null) welcomeLabel.setText("Bonjour, " + name + " 👋");
        if (subtitleLabel != null) subtitleLabel.setText("Voici le tableau de bord de votre espace parent.");

        loadDashboard();
        System.out.println("ParentDashboardController initialisé");

        notificationService = new NotificationService();

        // Récupérer l'utilisateur connecté
        if (AppContext.getCurrentUser() != null) {
            currentParentId = AppContext.getCurrentUser().getId();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Bienvenue, " + AppContext.getFullName());
            }
        } else {
            System.err.println("⚠️ Aucun utilisateur connecté");
            currentParentId = -1;
        }

        loadNotifications();

        if (clearAllBtn != null) {
            clearAllBtn.setOnAction(e -> clearAllNotifications());
        }
        if (aiAskBtn != null) aiAskBtn.setOnAction(e -> askGroq());
    }

    // ─── DASHBOARD DATA ───────────────────────────────────────────────────────

    private void loadDashboard() {
        if (AppContext.getCurrentUser() == null) return;
        int parentId = AppContext.getCurrentUser().getId();

        // Rafraîchir les notifications toutes les 30 secondes
        PauseTransition refreshTimer = new PauseTransition(Duration.seconds(30));
        refreshTimer.setOnFinished(e -> {
            loadNotifications();
            refreshTimer.playFromStart();
        });
        refreshTimer.play();
    }

    private void loadNotifications() {
        if (currentParentId == -1) return;

        try {
            // 1. Children
            List<User> children = userService.getChildrenByParentId(currentParentId);
            if (totalChildrenLabel != null) totalChildrenLabel.setText(String.valueOf(children.size()));

            // 2. Subscriptions → courses → séances
            SubscriptionService subService = new SubscriptionService();
            CourseService courseService   = new CourseService();
            SeanceService seanceService   = new SeanceService();

            List<Subscription> allSubs = subService.afficherTous().stream()
                    .filter(s -> children.stream().anyMatch(c -> c.getId() == s.getKidId()))
                    .collect(Collectors.toList());

            // Course names map
            List<Course> allCourses = courseService.afficherTous();
            Map<Integer, String> courseName = new HashMap<>();
            for (Course c : allCourses) courseName.put(c.getId(), c.getTitle());

            // Child name map
            Map<Integer, String> childName = new HashMap<>();
            for (User c : children) childName.put(c.getId(), c.getFirstName() + " " + c.getLastName());

            // Enrolled course IDs (per kid)
            Set<Integer> enrolledCourseIds = allSubs.stream()
                    .map(Subscription::getCourseId)
                    .collect(Collectors.toSet());

            if (totalCoursesLabel != null) totalCoursesLabel.setText(String.valueOf(enrolledCourseIds.size()));

            // All séances for enrolled courses
            List<Seance> allSeances = seanceService.afficherTous().stream()
                    .filter(s -> enrolledCourseIds.contains(s.getCourseId()))
                    .sorted(Comparator.comparing(s ->
                            s.getStartTime() != null ? s.getStartTime() : java.time.LocalDateTime.MAX))
                    .collect(Collectors.toList());

            if (totalSeancesLabel != null) totalSeancesLabel.setText(String.valueOf(allSeances.size()));

            // 3. Render kids-séances cards
            renderKidsSeances(allSeances, allSubs, courseName, childName);

            List<Notification> notifications = notificationService.getAllNotifications(currentParentId, 50);
            int unreadCount = notificationService.countUnreadNotifications(currentParentId);

            updateNotificationBadge(unreadCount);
            displayNotifications(notifications);

        } catch (Exception e) {
            System.err.println("Erreur chargement notifications: " + e.getMessage());
            // Si erreur, afficher au moins quelque chose
            if (notificationsContainer != null) {
                notificationsContainer.getChildren().clear();
                Label errorLabel = new Label("⚠️ Impossible de charger les notifications");
                errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
                notificationsContainer.getChildren().add(errorLabel);
            }
            if (kidsSeancesContainer != null) {
                Label err = new Label("⚠ Erreur de chargement: " + e.getMessage());
                err.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px;");
                kidsSeancesContainer.getChildren().setAll(err);
            }
        }
    }

    private void renderKidsSeances(List<Seance> seances, List<Subscription> subs,
                                   Map<Integer, String> courseNames, Map<Integer, String> childNames) {
        if (kidsSeancesContainer == null) return;
        kidsSeancesContainer.getChildren().clear();

        if (seances.isEmpty()) {
            Label empty = new Label("Aucune séance inscrite. Allez dans 'Séances' pour inscrire vos enfants.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8; -fx-padding: 10 0;");
            empty.setWrapText(true);
            kidsSeancesContainer.getChildren().add(empty);
            return;
        }

        for (Seance s : seances) {
            // Which children are in this course?
            List<String> enrolled = subs.stream()
                    .filter(sub -> sub.getCourseId() == s.getCourseId())
                    .map(sub -> childNames.getOrDefault(sub.getKidId(), "Enfant #" + sub.getKidId()))
                    .distinct()
                    .collect(Collectors.toList());

            kidsSeancesContainer.getChildren().add(buildSeanceCard(s, courseNames, enrolled));
        }
    }

    private VBox buildSeanceCard(Seance s, Map<Integer, String> courseNames, List<String> enrolledChildren) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 6, 0, 0, 3);");

        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String cTitle = courseNames.getOrDefault(s.getCourseId(), "Cours #" + s.getCourseId());
        Label courseLbl = new Label("📚 " + cTitle);
        courseLbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #3B82F6; " +
                "-fx-background-color: #EFF6FF; -fx-padding: 2 8; -fx-background-radius: 999;");

        String statusColor = "scheduled".equalsIgnoreCase(s.getStatus()) ? "#10B981" : "#F59E0B";
        Label statusLbl = new Label(s.getStatus() != null ? s.getStatus().toUpperCase() : "—");
        statusLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: white; -fx-background-color: " +
                statusColor + "; -fx-padding: 2 7; -fx-background-radius: 999;");

        topRow.getChildren().addAll(courseLbl, statusLbl);

        Label titleLbl = new Label(s.getTitle() != null ? s.getTitle() : "Séance sans titre");
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        titleLbl.setWrapText(true);

        String dateStr = s.getStartTime() != null ? s.getStartTime().format(FMT)
                : (s.getDate() != null ? s.getDate().toString() : "—");
        Label dateLbl = new Label("🗓 " + dateStr);
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        // Children enrolled tags
        HBox childTags = new HBox(6);
        childTags.setAlignment(Pos.CENTER_LEFT);
        for (String child : enrolledChildren) {
            Label tag = new Label("👤 " + child);
            tag.setStyle("-fx-font-size: 11px; -fx-text-fill: #6D28D9; -fx-background-color: #EDE9FE; " +
                    "-fx-padding: 2 8; -fx-background-radius: 999;");
            childTags.getChildren().add(tag);
        }

        card.getChildren().addAll(topRow, titleLbl, dateLbl);
        if (!childTags.getChildren().isEmpty()) card.getChildren().add(childTags);
        return card;
    }

    // ─── GROQ AI ─────────────────────────────────────────────────────────────

    private void askGroq() {
        String question = aiPromptField != null ? aiPromptField.getText().trim() : "";
        if (question.isBlank()) {
            setVisible(aiErrorLabel, true);
            if (aiErrorLabel != null) aiErrorLabel.setText("⚠ Veuillez écrire une question avant de consulter l'IA.");
            return;
        }

        // Build context from available séances
        String seanceContext = buildSeanceContextForAI();
        String fullPrompt = "Voici les séances disponibles :\n" + seanceContext +
                "\n\nQuestion du parent : " + question +
                "\n\nRéponds en français, de façon concise et utile pour un parent.";

        // Show loading state
        setVisible(aiLoadingLabel, true);
        setVisible(aiResponseBox, false);
        setVisible(aiErrorLabel, false);
        if (aiAskBtn != null) aiAskBtn.setDisable(true);

        // Call Groq on background thread
        Thread thread = new Thread(() -> {
            try {
                GroqRecommendationService groq = new GroqRecommendationService(new File("config/groq.properties"));
                String response = groq.recommendSeances(fullPrompt);
                Platform.runLater(() -> {
                    if (aiResponseLabel != null) aiResponseLabel.setText(response);
                    setVisible(aiResponseBox, true);
                    setVisible(aiLoadingLabel, false);
                    if (aiAskBtn != null) aiAskBtn.setDisable(false);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    setVisible(aiLoadingLabel, false);
                    setVisible(aiErrorLabel, true);
                    if (aiErrorLabel != null)
                        aiErrorLabel.setText("⚠ Erreur IA : " + ex.getMessage());
                    if (aiAskBtn != null) aiAskBtn.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String buildSeanceContextForAI() {
        try {
            SeanceService seanceService = new SeanceService();
            CourseService courseService = new CourseService();

            List<Course> courses = courseService.afficherTous();
            Map<Integer, Course> courseMap = new HashMap<>();
            for (Course c : courses) courseMap.put(c.getId(), c);

            List<Seance> seances = seanceService.afficherTous();
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(seances.size(), 10); // send max 10 to AI
            for (int i = 0; i < limit; i++) {
                Seance s = seances.get(i);
                Course c = courseMap.get(s.getCourseId());
                String level = c != null ? c.getLevel() : "?";
                String cTitle = c != null ? c.getTitle() : "Cours #" + s.getCourseId();
                String date = s.getStartTime() != null
                        ? s.getStartTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : (s.getDate() != null ? s.getDate().toString() : "date inconnue");
                sb.append("- Séance : ").append(s.getTitle())
                        .append(" | Cours : ").append(cTitle)
                        .append(" | Niveau : ").append(level)
                        .append(" | Date : ").append(date)
                        .append(" | Lieu : ").append(s.getLocation() != null ? s.getLocation() : "—")
                        .append("\n");
            }
            return sb.isEmpty() ? "Aucune séance disponible." : sb.toString();
        } catch (Exception e) {
            return "Impossible de charger les séances.";
        }
    }

    private void setVisible(javafx.scene.Node node, boolean v) {
        if (node != null) { node.setVisible(v); node.setManaged(v); }
    }

    private void displayNotifications(List<Notification> notifications) {
        if (notificationsContainer == null) return;

        notificationsContainer.getChildren().clear();

        if (notifications == null || notifications.isEmpty()) {
            Label emptyLabel = new Label("📭 Aucune notification");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px;");
            emptyLabel.setPadding(new Insets(30));
            notificationsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Notification notif : notifications) {
            notificationsContainer.getChildren().add(createNotificationCard(notif));
        }
    }

    private VBox createNotificationCard(Notification notification) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.setStyle("-fx-background-color: " + (notification.isRead() ? "#ffffff" : "#f0fdf4") +
                "; -fx-background-radius: 12; -fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        String icon = getIconForType(notification.getType());
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 20px;");

        Label titleLabel = new Label(notification.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14px;");

        Label dateLabel = new Label(notification.getFormattedDate());
        dateLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(iconLabel, titleLabel, spacer, dateLabel);

        // Message
        Label messageLabel = new Label(notification.getMessage());
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-text-fill: #475569; -fx-font-size: 13px;");

        // Boutons d'action
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        if (!notification.isRead()) {
            Button markReadBtn = new Button("✓ Marquer comme lue");
            markReadBtn.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 15; -fx-cursor: hand;");
            markReadBtn.setOnAction(e -> {
                if (notificationService != null && currentParentId != -1) {
                    notificationService.markAsRead(currentParentId, notification.getId());
                    loadNotifications();
                }
            });
            actions.getChildren().add(markReadBtn);
        }

        Button viewBtn = new Button("👁️ Voir");
        viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 12; -fx-background-radius: 15; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> handleNotificationClick(notification));
        actions.getChildren().add(viewBtn);

        card.getChildren().addAll(header, messageLabel, actions);

        return card;
    }

    private String getIconForType(String type) {
        if (type == null) return "📌";
        switch (type) {
            case "EVENT_NEW": return "🎉";
            case "EVENT_MODIFIED": return "📢";
            case "REMINDER": return "⏰";
            case "REGISTRATION_CONFIRMED": return "✅";
            default: return "📌";
        }
    }

    private void clearAllNotifications() {
        if (notificationService != null && currentParentId != -1) {
            notificationService.markAllAsRead(currentParentId);
            loadNotifications();
        }
    }

    private void handleNotificationClick(Notification notification) {
        if (notification == null) return;

        if (!notification.isRead() && notificationService != null && currentParentId != -1) {
            notificationService.markAsRead(currentParentId, notification.getId());
        }

        // Navigation selon le type
        String type = notification.getType();
        int referenceId = notification.getReferenceId();

        if (type != null) {
            switch (type) {
                case "EVENT_NEW":
                case "EVENT_MODIFIED":
                    if (referenceId > 0) {
                        Router.go("parent_event_detail", referenceId);
                    }
                    break;
                case "REMINDER":
                case "REGISTRATION_CONFIRMED":
                    Router.go("parent_registrations");
                    break;
                default:
                    break;
            }
        }

        loadNotifications(); // Recharger pour mettre à jour le statut de lecture
    }

    private void updateNotificationBadge(int count) {
        if (notificationBadge != null) {
            if (count > 0) {
                notificationBadge.setText(String.valueOf(count));
                notificationBadge.setVisible(true);
                notificationBadge.setManaged(true);
            } else {
                notificationBadge.setVisible(false);
                notificationBadge.setManaged(false);
            }
        }
    }

    @FXML
    private void showEvents() {
        Router.go("parent_event_list");
    }

    @FXML
    private void showRegistrations() {
        Router.go("parent_registrations");
    }

    @FXML
    private void showChildren() {
        Router.go("parent_children");
    }

    @FXML
    private void showProfile() {
        Router.go("profile");
    }
}