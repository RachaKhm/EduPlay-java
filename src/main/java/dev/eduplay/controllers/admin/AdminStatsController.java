package dev.eduplay.controllers.admin;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.entities.Subscription;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AdminStatsController {

    @FXML private Label subtitleLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalSeancesLabel;
    @FXML private Label totalSubscriptionsLabel;
    @FXML private Label activeSubscriptionsLabel;

    @FXML private PieChart courseStatusPie;
    @FXML private PieChart subscriptionStatePie;
    @FXML private BarChart<String, Number> courseLevelBar;
    @FXML private BarChart<String, Number> subscriptionsByCourseBar;
    @FXML private LineChart<String, Number> seancesTimelineLine;

    private CourseService courseService;
    private SeanceService seanceService;
    private SubscriptionService subscriptionService;

    @FXML
    public void initialize() {
        if (!AppContext.isAdmin()) {
            if (subtitleLabel != null) subtitleLabel.setText("Accès réservé aux administrateurs.");
            return;
        }

        if (subtitleLabel != null) {
            subtitleLabel.setText("Visualise les tendances des cours, séances et inscriptions.");
        }

        try {
            courseService = new CourseService();
            seanceService = new SeanceService();
            subscriptionService = new SubscriptionService();
            loadStats();
        } catch (SQLException e) {
            showError("Base de données", "Impossible de charger les statistiques", e.getMessage());
        }
    }

    private void loadStats() throws SQLException {
        List<Course> courses = courseService.afficherTous();
        List<Seance> seances = seanceService.afficherTous();
        List<Subscription> subscriptions = subscriptionService.afficherTous();

        if (totalCoursesLabel != null) totalCoursesLabel.setText(String.valueOf(courses.size()));
        if (totalSeancesLabel != null) totalSeancesLabel.setText(String.valueOf(seances.size()));
        if (totalSubscriptionsLabel != null) totalSubscriptionsLabel.setText(String.valueOf(subscriptions.size()));
        if (activeSubscriptionsLabel != null) {
            long activeCount = subscriptions.stream().filter(Subscription::isActive).count();
            activeSubscriptionsLabel.setText(String.valueOf(activeCount));
        }

        buildCourseStatusPie(courses);
        buildSubscriptionStatePie(subscriptions);
        buildCourseLevelBar(courses);
        buildSubscriptionsByCourseBar(courses, subscriptions);
        buildSeancesTimelineLine(seances);
    }

    private void buildCourseStatusPie(List<Course> courses) {
        if (courseStatusPie == null) return;
        Map<String, Long> byStatus = courses.stream()
                .collect(Collectors.groupingBy(c -> safe(c.getStatus(), "unknown"), TreeMap::new, Collectors.counting()));
        courseStatusPie.setData(FXCollections.observableArrayList(
                byStatus.entrySet().stream()
                        .map(e -> new PieChart.Data(e.getKey(), e.getValue()))
                        .toList()
        ));
    }

    private void buildSubscriptionStatePie(List<Subscription> subscriptions) {
        if (subscriptionStatePie == null) return;
        long active = subscriptions.stream().filter(Subscription::isActive).count();
        long inactive = subscriptions.size() - active;
        subscriptionStatePie.setData(FXCollections.observableArrayList(
                new PieChart.Data("Actives", active),
                new PieChart.Data("Inactives", inactive)
        ));
    }

    private void buildCourseLevelBar(List<Course> courses) {
        if (courseLevelBar == null) return;
        Map<String, Long> byLevel = courses.stream()
                .collect(Collectors.groupingBy(c -> safe(c.getLevel(), "non précisé"), TreeMap::new, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Cours");
        byLevel.forEach((level, count) -> series.getData().add(new XYChart.Data<>(level, count)));

        courseLevelBar.getData().clear();
        courseLevelBar.getData().add(series);
    }

    private void buildSubscriptionsByCourseBar(List<Course> courses, List<Subscription> subscriptions) {
        if (subscriptionsByCourseBar == null) return;

        Map<Integer, String> courseTitleById = courses.stream()
                .collect(Collectors.toMap(Course::getId, c -> safe(c.getTitle(), "Cours #" + c.getId()), (a, b) -> a));
        Map<Integer, Long> counts = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getCourseId, Collectors.counting()));

        List<Map.Entry<Integer, Long>> top = counts.entrySet().stream()
                .sorted(Map.Entry.<Integer, Long>comparingByValue().reversed())
                .limit(8)
                .toList();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Inscriptions");
        for (Map.Entry<Integer, Long> e : top) {
            String title = courseTitleById.getOrDefault(e.getKey(), "Cours #" + e.getKey());
            series.getData().add(new XYChart.Data<>(ellipsis(title, 20), e.getValue()));
        }

        subscriptionsByCourseBar.getData().clear();
        subscriptionsByCourseBar.getData().add(series);
    }

    private void buildSeancesTimelineLine(List<Seance> seances) {
        if (seancesTimelineLine == null) return;
        DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MM/yyyy");
        LocalDate start = LocalDate.now().minusMonths(5).withDayOfMonth(1);
        LocalDate end = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        Map<YearMonth, Long> byMonth = seances.stream()
                .map(AdminStatsController::resolveDate)
                .filter(Objects::nonNull)
                .map(YearMonth::from)
                .collect(Collectors.groupingBy(x -> x, TreeMap::new, Collectors.counting()));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Séances");
        for (LocalDate cur = start; !cur.isAfter(end); cur = cur.plusMonths(1)) {
            YearMonth ym = YearMonth.from(cur);
            long value = byMonth.getOrDefault(ym, 0L);
            series.getData().add(new XYChart.Data<>(cur.format(monthFmt), value));
        }

        seancesTimelineLine.getData().clear();
        seancesTimelineLine.getData().add(series);
    }

    private static LocalDate resolveDate(Seance s) {
        if (s == null) return null;
        if (s.getDate() != null) return s.getDate();
        if (s.getStartTime() != null) return s.getStartTime().toLocalDate();
        return null;
    }

    private static String safe(String value, String fallback) {
        return (value == null || value.isBlank()) ? fallback : value.trim();
    }

    private static String ellipsis(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }

    private void showError(String title, String header, String details) {
        Alert a = new Alert(Alert.AlertType.ERROR, details == null ? "" : details, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(header);
        a.showAndWait();
    }
}
