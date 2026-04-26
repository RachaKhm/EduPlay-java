package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Notification;
import dev.eduplay.services.NotificationService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;

public class ParentDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private VBox notificationsContainer;
    @FXML private Button clearAllBtn;
    @FXML private Label notificationBadge;
    @FXML private ScrollPane notificationsScrollPane;

    private NotificationService notificationService;
    private int currentParentId;

    @FXML
    public void initialize() {
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
        }
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