package dev.eduplay.services;

import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatisticsService {

    private Connection cn;

    public StatisticsService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    // ==================== STATISTIQUES GLOBALES ====================

    /**
     * Nombre total d'événements
     */
    public int getTotalEvents() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM school_event";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    /**
     * Nombre total d'inscriptions
     */
    public int getTotalRegistrations() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM event_registration";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    /**
     * Nombre d'événements à venir
     */
    public int getUpcomingEvents() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM school_event WHERE start_date > NOW()";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    /**
     * Nombre d'événements passés
     */
    public int getPastEvents() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM school_event WHERE end_date < NOW()";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }


    /**
     * Nombre d'événements en cours
     */
    public int getOngoingEvents() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM school_event WHERE start_date <= NOW() AND end_date >= NOW()";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    /**
     * Nombre total de participants (inscriptions cumulées)
     */
    public int getTotalParticipants() throws SQLException {
        String sql = "SELECT SUM(current_registrations) as total FROM school_event";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    /**
     * Capacité totale disponible
     */
    public int getTotalCapacity() throws SQLException {
        String sql = "SELECT SUM(max_capacity) as total FROM school_event";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        if (rs.next()) {
            return rs.getInt("total");
        }
        return 0;
    }

    /**
     * Taux de remplissage global (%)
     */
    public double getFillRate() throws SQLException {
        int totalParticipants = getTotalParticipants();
        int totalCapacity = getTotalCapacity();
        if (totalCapacity == 0) return 0;
        return (double) totalParticipants / totalCapacity * 100;
    }

    // ==================== STATISTIQUES PAR PÉRIODE ====================

    /**
     * Inscriptions par mois (6 derniers mois)
     */
    public Map<String, Integer> getRegistrationsByMonth() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();

        String sql = """
            SELECT 
                DATE_FORMAT(registered_at, '%Y-%m') as month,
                COUNT(*) as count
            FROM event_registration
            WHERE registered_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
            GROUP BY DATE_FORMAT(registered_at, '%Y-%m')
            ORDER BY month ASC
            """;

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        while (rs.next()) {
            String monthStr = rs.getString("month");
            LocalDate date = LocalDate.parse(monthStr + "-01");
            String monthLabel = date.format(formatter);
            data.put(monthLabel, rs.getInt("count"));
        }

        return data;
    }

    /**
     * Événements par mois (6 derniers mois)
     */
    public Map<String, Integer> getEventsByMonth() throws SQLException {
        Map<String, Integer> data = new LinkedHashMap<>();

        String sql = """
            SELECT 
                DATE_FORMAT(created_at, '%Y-%m') as month,
                COUNT(*) as count
            FROM school_event
            WHERE created_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)
            GROUP BY DATE_FORMAT(created_at, '%Y-%m')
            ORDER BY month ASC
            """;

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        while (rs.next()) {
            String monthStr = rs.getString("month");
            LocalDate date = LocalDate.parse(monthStr + "-01");
            String monthLabel = date.format(formatter);
            data.put(monthLabel, rs.getInt("count"));
        }

        return data;
    }

    // ==================== STATISTIQUES PAR ÉVÉNEMENT ====================

    /**
     * Top 5 des événements les plus populaires
     */
    public List<EventStats> getTopEvents() throws SQLException {
        List<EventStats> topEvents = new ArrayList<>();

        String sql = """
            SELECT 
                se.id,
                se.title,
                se.max_capacity,
                se.current_registrations,
                ROUND(se.current_registrations * 100.0 / se.max_capacity, 1) as fill_rate,
                se.start_date
            FROM school_event se
            WHERE se.max_capacity > 0
            ORDER BY se.current_registrations DESC
            LIMIT 5
            """;

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            EventStats stats = new EventStats();
            stats.id = rs.getInt("id");
            stats.title = rs.getString("title");
            stats.maxCapacity = rs.getInt("max_capacity");
            stats.currentRegistrations = rs.getInt("current_registrations");
            stats.fillRate = rs.getDouble("fill_rate");
            stats.startDate = rs.getTimestamp("start_date") != null ?
                    rs.getTimestamp("start_date").toLocalDateTime() : null;
            topEvents.add(stats);
        }

        return topEvents;
    }

    /**
     * Taux de remplissage par événement
     */
    public List<EventStats> getAllEventsFillRate() throws SQLException {
        List<EventStats> events = new ArrayList<>();

        String sql = """
            SELECT 
                se.id,
                se.title,
                se.max_capacity,
                se.current_registrations,
                ROUND(se.current_registrations * 100.0 / se.max_capacity, 1) as fill_rate,
                se.start_date
            FROM school_event se
            WHERE se.max_capacity > 0
            ORDER BY se.start_date DESC
            """;

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            EventStats stats = new EventStats();
            stats.id = rs.getInt("id");
            stats.title = rs.getString("title");
            stats.maxCapacity = rs.getInt("max_capacity");
            stats.currentRegistrations = rs.getInt("current_registrations");
            stats.fillRate = rs.getDouble("fill_rate");
            stats.startDate = rs.getTimestamp("start_date") != null ?
                    rs.getTimestamp("start_date").toLocalDateTime() : null;
            events.add(stats);
        }

        return events;
    }

    // ==================== STATISTIQUES PAR PARENT ====================

    /**
     * Top 5 des parents les plus actifs
     */
    public List<ParentStats> getTopParents() throws SQLException {
        List<ParentStats> topParents = new ArrayList<>();

        String sql = """
            SELECT 
                u.id,
                CONCAT(u.first_name, ' ', u.last_name) as full_name,
                u.email,
                COUNT(er.id) as registrations_count
            FROM user u
            JOIN event_registration er ON u.id = er.parent_id
            WHERE u.type = 'parent'
            GROUP BY u.id, u.first_name, u.last_name, u.email
            ORDER BY registrations_count DESC
            LIMIT 5
            """;

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            ParentStats stats = new ParentStats();
            stats.id = rs.getInt("id");
            stats.fullName = rs.getString("full_name");
            stats.email = rs.getString("email");
            stats.registrationsCount = rs.getInt("registrations_count");
            topParents.add(stats);
        }

        return topParents;
    }

    // ==================== CLASSES INTERNES ====================

    public static class EventStats {
        public int id;
        public String title;
        public int maxCapacity;
        public int currentRegistrations;
        public double fillRate;
        public LocalDateTime startDate;

        public String getFormattedDate() {
            if (startDate == null) return "Date non définie";
            return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }
    }

    public static class ParentStats {
        public int id;
        public String fullName;
        public String email;
        public int registrationsCount;
    }
}