package dev.eduplay.controllers.child;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ChildSeancesController {

    @FXML private VBox seancesContainer;
    @FXML private Label titleLabel;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE dd MMM yyyy · HH:mm", Locale.FRENCH);

    @FXML
    public void initialize() {
        loadUpcomingSeances();
    }

    private void loadUpcomingSeances() {
        seancesContainer.getChildren().clear();
        int kidId = AppContext.getCurrentUser().getId();

        try {
            SubscriptionService subService = new SubscriptionService();
            CourseService courseService = new CourseService();
            SeanceService seanceService = new SeanceService();

            // Get courses the kid is subscribed to
            List<Course> myCourses = courseService.afficherPourEnfantAbonne(kidId);
            Set<Integer> myCourseIds = myCourses.stream()
                    .map(Course::getId)
                    .collect(Collectors.toSet());

            Map<Integer, String> courseNames = new HashMap<>();
            for (Course c : myCourses) {
                courseNames.put(c.getId(), c.getTitle());
            }

            // Get all séances, filter to subscribed courses, sort by upcoming
            List<Seance> allSeances = seanceService.afficherTous();
            LocalDateTime now = LocalDateTime.now();

            List<Seance> upcoming = allSeances.stream()
                    .filter(s -> myCourseIds.contains(s.getCourseId()))
                    .filter(s -> s.getStartTime() == null || s.getStartTime().isAfter(now.minusHours(1)))
                    .sorted(Comparator.comparing(s -> s.getStartTime() != null ? s.getStartTime() : LocalDateTime.MAX))
                    .collect(Collectors.toList());

            if (titleLabel != null)
                titleLabel.setText("Mes Séances (" + upcoming.size() + " à venir)");

            if (upcoming.isEmpty()) {
                Label empty = new Label("Aucune séance à venir. Demande à tes parents de t'inscrire !");
                empty.setStyle("-fx-font-size: 15px; -fx-text-fill: #94A3B8; -fx-padding: 30;");
                seancesContainer.getChildren().add(empty);
                return;
            }

            for (Seance s : upcoming) {
                seancesContainer.getChildren().add(buildSeanceCard(s, courseNames));
            }

        } catch (SQLException e) {
            Label err = new Label("Erreur de chargement : " + e.getMessage());
            err.setStyle("-fx-text-fill: #EF4444;");
            seancesContainer.getChildren().add(err);
        }
    }

    private VBox buildSeanceCard(Seance s, Map<Integer, String> courseNames) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 14; " +
                "-fx-border-color: #E2E8F0; -fx-border-width: 1; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 4);");

        String courseTitle = courseNames.getOrDefault(s.getCourseId(), "Cours #" + s.getCourseId());
        Label courseLbl = new Label("📚 " + courseTitle);
        courseLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #3B82F6; -fx-font-weight: bold; " +
                "-fx-background-color: #EFF6FF; -fx-padding: 3 8; -fx-background-radius: 999;");

        Label titleLbl = new Label(s.getTitle() != null ? s.getTitle() : "Séance sans titre");
        titleLbl.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        titleLbl.setWrapText(true);

        String dateStr = s.getStartTime() != null ? s.getStartTime().format(FMT)
                : (s.getDate() != null ? s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
        Label dateLbl = new Label("🗓 " + dateStr);
        dateLbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748B;");

        HBox meta = new HBox(12);
        if (s.getLocation() != null && !s.getLocation().isBlank()) {
            Label loc = new Label("📍 " + s.getLocation());
            loc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            meta.getChildren().add(loc);
        }

        String statusColor = "scheduled".equals(s.getStatus()) ? "#10B981" : "#F59E0B";
        Label statusLbl = new Label(s.getStatus() != null ? s.getStatus().toUpperCase() : "");
        statusLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: white; -fx-background-color: " + statusColor +
                "; -fx-padding: 2 8; -fx-background-radius: 999;");
        meta.getChildren().add(statusLbl);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(courseLbl, titleLbl, dateLbl, meta);
        return card;
    }
}
