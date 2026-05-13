package dev.eduplay.services;

import dev.eduplay.entities.Notification;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class NotificationService {

    // Stockage en mémoire (Map: userId -> Liste de notifications)
    private static final Map<Integer, List<Notification>> notificationsCache = new ConcurrentHashMap<>();
    private static int nextId = 1;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Connection cn;

    public NotificationService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    // ==================== RÉCUPÉRATION DES PARENTS ====================

    private List<ParentInfo> getParents() throws SQLException {
        String sql = "SELECT id, first_name, last_name, email FROM user WHERE type = 'parent' AND active = 1";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        List<ParentInfo> parents = new ArrayList<>();
        while (rs.next()) {
            ParentInfo parent = new ParentInfo();
            parent.id = rs.getInt("id");
            parent.firstName = rs.getString("first_name");
            parent.lastName = rs.getString("last_name");
            parent.email = rs.getString("email");
            parents.add(parent);
        }
        return parents;
    }

    private static class ParentInfo {
        int id;
        String firstName;
        String lastName;
        String email;
        String getFullName() { return firstName + " " + lastName; }
    }

    // ==================== GESTION DES NOTIFICATIONS ====================

    /**
     * Ajoute une notification pour un utilisateur
     */
    public void addNotification(int userId, String title, String message, String type, int referenceId) {
        Notification notif = new Notification(userId, title, message, type, referenceId);
        notif.setId(nextId++);

        notificationsCache.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, notif);

        // Limiter à 100 notifications par utilisateur
        List<Notification> userNotifs = notificationsCache.get(userId);
        if (userNotifs.size() > 100) {
            userNotifs.remove(userNotifs.size() - 1);
        }

        System.out.println("✅ Notification ajoutée pour l'utilisateur " + userId + ": " + title);
    }

    /**
     * Notifie tous les parents d'un nouvel événement
     */
    public void notifyNewEvent(SchoolEvent event) throws SQLException {
        List<ParentInfo> parents = getParents();

        if (parents.isEmpty()) {
            System.out.println("⚠️ Aucun parent trouvé");
            return;
        }

        String eventDate = event.getStartDate() != null ? event.getStartDate().format(formatter) : "Date à venir";
        String title = "🎉 " + event.getTitle();
        String message = "Nouvel événement : " + event.getTitle() + " le " + eventDate + " à " + event.getLocation();

        for (ParentInfo parent : parents) {
            addNotification(parent.id, title, message, "EVENT_NEW", event.getId());
        }

        System.out.println("✅ " + parents.size() + " notifications envoyées pour le nouvel événement");
    }

    /**
     * ✅ Notifie un parent spécifique d'une modification d'événement
     */
    public void notifyEventModification(int parentId, SchoolEvent event, String oldDate, String newDate,
                                        String oldLocation, String newLocation) {
        String title = "📢 " + event.getTitle();

        StringBuilder changes = new StringBuilder();
        if (oldDate != null && newDate != null && !oldDate.equals(newDate)) {
            changes.append("Date: ").append(oldDate).append(" → ").append(newDate);
        }
        if (oldLocation != null && newLocation != null && !oldLocation.equals(newLocation)) {
            if (changes.length() > 0) changes.append(", ");
            changes.append("Lieu: ").append(oldLocation).append(" → ").append(newLocation);
        }

        String message = "L'événement \"" + event.getTitle() + "\" a été modifié. " + changes.toString();
        addNotification(parentId, title, message, "EVENT_MODIFIED", event.getId());
    }

    /**
     * Notifie un parent d'un rappel d'événement (24h avant)
     */
    public void notifyReminder(int parentId, String childName, SchoolEvent event) {
        String eventTime = event.getStartDate() != null ?
                event.getStartDate().format(DateTimeFormatter.ofPattern("HH:mm")) : "l'heure prévue";

        String title = "⏰ " + event.getTitle();
        String message = "N'oubliez pas, " + childName + " participe à " + event.getTitle() +
                " demain à " + eventTime;

        addNotification(parentId, title, message, "REMINDER", event.getId());
    }

    /**
     * Notifie un parent d'une confirmation d'inscription
     */
    public void notifyRegistrationConfirmed(int parentId, String childName, SchoolEvent event, int registrationId) {
        String title = "✅ " + event.getTitle();
        String message = "Votre enfant " + childName + " est bien inscrit à " + event.getTitle() +
                ". Votre QR code est disponible.";

        addNotification(parentId, title, message, "REGISTRATION_CONFIRMED", registrationId);
    }

    // ==================== CONSULTATION NOTIFICATIONS ====================

    /**
     * Récupère les notifications non lues d'un utilisateur
     */
    public List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifs = notificationsCache.getOrDefault(userId, new ArrayList<>());
        return notifs.stream()
                .filter(n -> !n.isRead())
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les notifications d'un utilisateur
     */
    public List<Notification> getAllNotifications(int userId, int limit) {
        List<Notification> notifs = notificationsCache.getOrDefault(userId, new ArrayList<>());
        return notifs.stream().limit(limit).collect(Collectors.toList());
    }

    /**
     * Marque une notification comme lue
     */
    public void markAsRead(int userId, int notificationId) {
        List<Notification> notifs = notificationsCache.get(userId);
        if (notifs != null) {
            for (Notification n : notifs) {
                if (n.getId() == notificationId) {
                    n.setRead(true);
                    break;
                }
            }
        }
    }

    /**
     * Marque toutes les notifications d'un utilisateur comme lues
     */
    public void markAllAsRead(int userId) {
        List<Notification> notifs = notificationsCache.get(userId);
        if (notifs != null) {
            notifs.forEach(n -> n.setRead(true));
        }
    }

    /**
     * Compte les notifications non lues
     */
    public int countUnreadNotifications(int userId) {
        List<Notification> notifs = notificationsCache.getOrDefault(userId, new ArrayList<>());
        return (int) notifs.stream().filter(n -> !n.isRead()).count();
    }

    /**
     * Vérifie si des notifications non lues existent
     */
    public boolean hasUnreadNotifications(int userId) {
        return countUnreadNotifications(userId) > 0;
    }

    /**
     * Supprime une notification (optionnel)
     */
    public void deleteNotification(int userId, int notificationId) {
        List<Notification> notifs = notificationsCache.get(userId);
        if (notifs != null) {
            notifs.removeIf(n -> n.getId() == notificationId);
        }
    }
}