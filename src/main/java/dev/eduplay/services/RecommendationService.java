package dev.eduplay.services;

import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.tools.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {

    private Connection cn;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public RecommendationService() {
        cn = MyDataBase.getInstance().getCnx();
    }

    /**
     * Récupère tous les événements avec un score de recommandation
     * Les événements déjà inscrits sont exclus
     */
    public List<EventWithScore> getAllEventsWithScore(int parentId) throws SQLException {
        Set<Integer> registeredEventIds = getRegisteredEventIds(parentId);
        Map<String, Integer> preferences = getCategoryPreferences(parentId);

        List<EventWithScore> events = new ArrayList<>();

        String sql = """
            SELECT 
                se.id,
                se.title,
                se.description,
                se.start_date,
                se.end_date,
                se.location,
                se.max_capacity,
                se.current_registrations,
                (SELECT COUNT(*) FROM event_registration WHERE event_id = se.id) as total_registrations
            FROM school_event se
            WHERE se.end_date > NOW()
            ORDER BY se.start_date ASC
            """;

        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            int eventId = rs.getInt("id");

            // Exclure les événements déjà inscrits
            if (registeredEventIds.contains(eventId)) continue;

            EventWithScore event = new EventWithScore();
            event.id = eventId;
            event.title = rs.getString("title");
            event.description = rs.getString("description");
            event.startDate = rs.getTimestamp("start_date") != null ?
                    rs.getTimestamp("start_date").toLocalDateTime() : null;
            event.endDate = rs.getTimestamp("end_date") != null ?
                    rs.getTimestamp("end_date").toLocalDateTime() : null;
            event.location = rs.getString("location");
            event.maxCapacity = rs.getInt("max_capacity");
            event.currentRegistrations = rs.getInt("current_registrations");
            event.totalRegistrations = rs.getInt("total_registrations");
            event.fillRate = event.maxCapacity > 0 ?
                    (event.currentRegistrations * 100.0 / event.maxCapacity) : 0;

            // Calculer le score de recommandation
            event.recommendationScore = calculateRecommendationScore(event, preferences);

            events.add(event);
        }

        // Trier par score (les meilleurs en premier)
        events.sort((a, b) -> Double.compare(b.recommendationScore, a.recommendationScore));

        return events;
    }

    /**
     * Récupère les IDs des événements déjà inscrits
     */
    private Set<Integer> getRegisteredEventIds(int parentId) throws SQLException {
        Set<Integer> ids = new HashSet<>();
        String sql = "SELECT DISTINCT event_id FROM event_registration WHERE parent_id = ?";
        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, parentId);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt("event_id"));
        }
        return ids;
    }

    /**
     * Analyse les préférences basées sur les titres des événements déjà inscrits
     */
    private Map<String, Integer> getCategoryPreferences(int parentId) throws SQLException {
        Map<String, Integer> preferences = new HashMap<>();

        String sql = """
            SELECT se.title
            FROM event_registration er
            JOIN school_event se ON er.event_id = se.id
            WHERE er.parent_id = ?
            """;

        PreparedStatement pst = cn.prepareStatement(sql);
        pst.setInt(1, parentId);
        ResultSet rs = pst.executeQuery();

        // Mots-clés à rechercher dans les titres
        String[] keywords = {"atelier", "mosaïste", "art", "sport", "musique",
                "danse", "théâtre", "peinture", "bricolage", "cuisine",
                "nature", "science", "robotique", "langue", "anglais"};

        while (rs.next()) {
            String title = rs.getString("title").toLowerCase();
            for (String keyword : keywords) {
                if (title.contains(keyword)) {
                    preferences.merge(keyword, 1, Integer::sum);
                }
            }
        }

        return preferences;
    }

    /**
     * Calcule le score de recommandation (0 à 100)
     */
    private double calculateRecommendationScore(EventWithScore event, Map<String, Integer> preferences) {
        double score = 0;

        // 1. Popularité (0-30 points) - plus il y a d'inscrits, mieux c'est
        int maxRegistrations = 50; // Valeur de référence
        double popularityScore = Math.min(event.totalRegistrations * 100.0 / maxRegistrations, 30);
        score += popularityScore;

        // 2. Places disponibles (0-20 points) - plus il reste de places, mieux c'est
        if (event.maxCapacity > 0) {
            double availableScore = (event.maxCapacity - event.currentRegistrations) * 100.0 / event.maxCapacity;
            score += Math.min(availableScore / 5, 20);
        }

        // 3. Proximité de la date (0-25 points)
        if (event.startDate != null) {
            long daysUntil = java.time.Duration.between(LocalDateTime.now(), event.startDate).toDays();
            if (daysUntil <= 3) {
                score += 25; // Très bientôt
            } else if (daysUntil <= 7) {
                score += 20; // Cette semaine
            } else if (daysUntil <= 14) {
                score += 15; // Dans 2 semaines
            } else if (daysUntil <= 30) {
                score += 10; // Dans le mois
            } else {
                score += 5;  // Plus tard
            }
        }

        // 4. Correspondance avec les préférences (0-25 points)
        if (!preferences.isEmpty() && event.title != null) {
            String titleLower = event.title.toLowerCase();
            double matchScore = 0;
            for (Map.Entry<String, Integer> pref : preferences.entrySet()) {
                if (titleLower.contains(pref.getKey())) {
                    matchScore += Math.min(pref.getValue() * 5, 25);
                }
            }
            score += Math.min(matchScore, 25);
        }

        return Math.min(score, 100);
    }

    /**
     * Vérifie si un événement est recommandé (score > 50)
     */
    public boolean isRecommended(int parentId, int eventId) throws SQLException {
        List<EventWithScore> events = getAllEventsWithScore(parentId);
        for (EventWithScore event : events) {
            if (event.id == eventId) {
                return event.recommendationScore > 50;
            }
        }
        return false;
    }

    /**
     * Récupère le score de recommandation d'un événement spécifique
     */
    public double getRecommendationScore(int parentId, int eventId) throws SQLException {
        List<EventWithScore> events = getAllEventsWithScore(parentId);
        for (EventWithScore event : events) {
            if (event.id == eventId) {
                return event.recommendationScore;
            }
        }
        return 0;
    }

    // Classe interne pour les événements avec score
    public static class EventWithScore {
        public int id;
        public String title;
        public String description;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public String location;
        public int maxCapacity;
        public int currentRegistrations;
        public int totalRegistrations;
        public double fillRate;
        public double recommendationScore;

        public String getFormattedDate() {
            if (startDate == null) return "Date à définir";
            return startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        }

        public String getRecommendationLabel() {
            if (recommendationScore >= 70) return "🔥 Très populaire";
            if (recommendationScore >= 50) return "⭐ Recommandé";
            if (recommendationScore >= 30) return "👍 Intéressant";
            return "";
        }

        public String getRecommendationColor() {
            if (recommendationScore >= 70) return "#ef4444"; // Rouge
            if (recommendationScore >= 50) return "#f59e0b"; // Orange
            if (recommendationScore >= 30) return "#10b981"; // Vert
            return "#9ca3af"; // Gris
        }

        public boolean isStronglyRecommended() {
            return recommendationScore >= 50;
        }
    }
}