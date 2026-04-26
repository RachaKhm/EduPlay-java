package dev.eduplay.services;

import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SchoolEventService implements IGeneralService<SchoolEvent> {

    Connection cn;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public SchoolEventService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    @Override
    public void ajouter(SchoolEvent event) throws SQLException {
        String sql = "INSERT INTO school_event(title, description, start_date, end_date, location, image_path, created_at, latitude, longitude, max_capacity, current_registrations) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
        ps.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
        ps.setString(5, event.getLocation());
        ps.setString(6, event.getImagePath());
        ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(8, event.getLatitude());
        ps.setString(9, event.getLongitude());
        ps.setInt(10, event.getMaxCapacity());
        ps.setInt(11, 0);

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            event.setId(rs.getInt(1));
            System.out.println("✅ Événement ajouté avec ID: " + event.getId());

            // ✅ NOTIFICATION : informer tous les parents du nouvel événement
            try {
                NotificationService notificationService = new NotificationService();
                notificationService.notifyNewEvent(event);
            } catch (SQLException e) {
                System.err.println("⚠️ Erreur lors de l'envoi des notifications: " + e.getMessage());
            }
        }
    }

    @Override
    public void supprimer(SchoolEvent event) throws SQLException {
        String sqlq = "DELETE FROM school_event WHERE id = ?";
        PreparedStatement ps = cn.prepareStatement(sqlq);
        ps.setInt(1, event.getId());
        ps.executeUpdate();
    }

    public void supprimerAvecRessources(SchoolEvent event) throws SQLException {
        cn.setAutoCommit(false);
        try {
            String sqlResources = "DELETE FROM event_resource WHERE event_id = ?";
            PreparedStatement psResources = cn.prepareStatement(sqlResources);
            psResources.setInt(1, event.getId());
            psResources.executeUpdate();

            String sqlEvent = "DELETE FROM school_event WHERE id = ?";
            PreparedStatement psEvent = cn.prepareStatement(sqlEvent);
            psEvent.setInt(1, event.getId());
            psEvent.executeUpdate();

            cn.commit();
        } catch (SQLException e) {
            cn.rollback();
            throw e;
        } finally {
            cn.setAutoCommit(true);
        }
    }

    @Override
    public int chercher(SchoolEvent event) throws SQLException {
        String sql = "SELECT 1 FROM school_event WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, event.getId());
        ResultSet rs = pst.executeQuery();
        return rs.next() ? event.getId() : -1;
    }

    @Override
    public void modifier(SchoolEvent event) throws SQLException {
        // Récupérer l'ancienne version avant modification
        SchoolEvent oldEvent = recupererParId(event.getId());

        String sql = "UPDATE school_event SET title = ?, description = ?, start_date = ?, end_date = ?, location = ?, image_path = ?, latitude = ?, longitude = ?, max_capacity = ? WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setString(1, event.getTitle());
        pst.setString(2, event.getDescription());
        pst.setTimestamp(3, Timestamp.valueOf(event.getStartDate()));
        pst.setTimestamp(4, Timestamp.valueOf(event.getEndDate()));
        pst.setString(5, event.getLocation());
        pst.setString(6, event.getImagePath());
        pst.setString(7, event.getLatitude());
        pst.setString(8, event.getLongitude());
        pst.setInt(9, event.getMaxCapacity());
        pst.setInt(10, event.getId());

        pst.executeUpdate();

        // Vérifier si des modifications importantes ont été faites (date ou lieu)
        boolean hasImportantChanges = hasImportantChanges(oldEvent, event);

        // Notifier les parents si nécessaire
        if (hasImportantChanges) {
            notifyParentsOfEventChange(oldEvent, event);
        }
    }

    /**
     * Vérifie si les modifications sont importantes (date ou lieu)
     */
    private boolean hasImportantChanges(SchoolEvent oldEvent, SchoolEvent newEvent) {
        if (oldEvent == null || newEvent == null) return false;

        boolean dateChanged = false;
        boolean locationChanged = false;

        if (oldEvent.getStartDate() != null && newEvent.getStartDate() != null) {
            dateChanged = !oldEvent.getStartDate().equals(newEvent.getStartDate());
        }

        if (oldEvent.getLocation() != null && newEvent.getLocation() != null) {
            locationChanged = !oldEvent.getLocation().equals(newEvent.getLocation());
        }

        return dateChanged || locationChanged;
    }

    /**
     * Notifie tous les parents inscrits à un événement modifié
     */
    private void notifyParentsOfEventChange(SchoolEvent oldEvent, SchoolEvent newEvent) throws SQLException {
        EventRegistrationService registrationService = new EventRegistrationService();
        EmailService emailService = new EmailService();
        NotificationService notificationService = new NotificationService();

        List<EventRegistration> registrations = registrationService.recupererParEventId(newEvent.getId());

        String oldDate = oldEvent.getStartDate() != null ? oldEvent.getStartDate().format(formatter) : null;
        String newDate = newEvent.getStartDate() != null ? newEvent.getStartDate().format(formatter) : null;
        String oldLocation = oldEvent.getLocation();
        String newLocation = newEvent.getLocation();

        System.out.println("📧 Envoi de notifications à " + registrations.size() + " parents pour modification de l'événement");

        for (EventRegistration registration : registrations) {
            // ✅ Notification in-app
            try {
                notificationService.notifyEventModification(
                        registration.getParent().getId(),
                        newEvent,
                        oldDate, newDate,
                        oldLocation, newLocation
                );
            } catch (Exception e) {
                System.err.println("Erreur notification in-app: " + e.getMessage());
            }

            // ✅ Email de notification
            if (registration.getParent() != null && registration.getParent().getEmail() != null) {
                emailService.sendEventModificationNotification(
                        registration.getParent().getEmail(),
                        registration.getParent().getFullName(),
                        registration.getChildFullName(),
                        newEvent.getTitle(),
                        oldDate, newDate,
                        oldLocation, newLocation
                );

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public List<SchoolEvent> recuperer() throws SQLException {
        String sql = "SELECT * FROM school_event ORDER BY start_date DESC";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<SchoolEvent> events = new ArrayList<>();
        while(rs.next()){
            SchoolEvent event = new SchoolEvent(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getTimestamp("start_date").toLocalDateTime(),
                    rs.getTimestamp("end_date").toLocalDateTime(),
                    rs.getString("location"),
                    rs.getString("image_path"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getString("latitude"),
                    rs.getString("longitude"));
            event.setMaxCapacity(rs.getInt("max_capacity"));
            event.setCurrentRegistrations(rs.getInt("current_registrations"));
            events.add(event);
        }
        return events;
    }

    public SchoolEvent recupererParId(int id) throws SQLException {
        String sql = "SELECT * FROM school_event WHERE id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, id);
        ResultSet rs = pst.executeQuery();

        if (rs.next()) {
            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            event.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            event.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            event.setLocation(rs.getString("location"));
            event.setImagePath(rs.getString("image_path"));
            event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            event.setLatitude(rs.getString("latitude"));
            event.setLongitude(rs.getString("longitude"));
            event.setMaxCapacity(rs.getInt("max_capacity"));
            event.setCurrentRegistrations(rs.getInt("current_registrations"));
            return event;
        }
        return null;
    }

    /**
     * Récupère les événements qui commencent entre deux dates
     */
    public List<SchoolEvent> getEventsStartingBetween(LocalDateTime start, LocalDateTime end) throws SQLException {
        String sql = "SELECT * FROM school_event WHERE start_date BETWEEN ? AND ? ORDER BY start_date ASC";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setTimestamp(1, Timestamp.valueOf(start));
        pst.setTimestamp(2, Timestamp.valueOf(end));
        ResultSet rs = pst.executeQuery();

        List<SchoolEvent> events = new ArrayList<>();
        while (rs.next()) {
            SchoolEvent event = new SchoolEvent();
            event.setId(rs.getInt("id"));
            event.setTitle(rs.getString("title"));
            event.setDescription(rs.getString("description"));
            event.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
            event.setEndDate(rs.getTimestamp("end_date").toLocalDateTime());
            event.setLocation(rs.getString("location"));
            event.setImagePath(rs.getString("image_path"));
            event.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            event.setLatitude(rs.getString("latitude"));
            event.setLongitude(rs.getString("longitude"));
            event.setMaxCapacity(rs.getInt("max_capacity"));
            event.setCurrentRegistrations(rs.getInt("current_registrations"));
            events.add(event);
        }
        return events;
    }
}