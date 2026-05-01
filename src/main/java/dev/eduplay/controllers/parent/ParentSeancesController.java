package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.entities.Subscription;
import dev.eduplay.entities.User;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import dev.eduplay.services.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ParentSeancesController {

    @FXML private VBox seancesContainer;
    @FXML private Label subtitleLabel;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE dd MMM yyyy · HH:mm", Locale.FRENCH);

    @FXML
    public void initialize() {
        loadSeances();
    }

    private void loadSeances() {
        seancesContainer.getChildren().clear();
        try {
            SeanceService seanceService = new SeanceService();
            CourseService courseService = new CourseService();
            UserService userService = new UserService();
            SubscriptionService subService = new SubscriptionService();

            int parentId = AppContext.getCurrentUser().getId();
            List<User> children = userService.getChildrenByParentId(parentId);

            // Build a map of all course IDs
            List<Course> allCourses = courseService.afficherTous();
            Map<Integer, String> courseTitleById = new HashMap<>();
            for (Course c : allCourses) courseTitleById.put(c.getId(), c.getTitle());

            List<Seance> allSeances = seanceService.afficherTous();

            if (allSeances.isEmpty()) {
                Label empty = new Label("Aucune séance disponible actuellement.");
                empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-padding: 30;");
                seancesContainer.getChildren().add(empty);
                return;
            }

            if (subtitleLabel != null)
                subtitleLabel.setText(allSeances.size() + " séance(s) disponible(s) · " + children.size() + " enfant(s)");

            // Sort: upcoming first
            allSeances.sort((a, b) -> {
                if (a.getStartTime() == null) return 1;
                if (b.getStartTime() == null) return -1;
                return a.getStartTime().compareTo(b.getStartTime());
            });

            for (Seance s : allSeances) {
                seancesContainer.getChildren().add(buildCard(s, courseTitleById, children, subService, parentId));
            }

        } catch (SQLException e) {
            Label err = new Label("Erreur : " + e.getMessage());
            err.setStyle("-fx-text-fill: #EF4444;");
            seancesContainer.getChildren().add(err);
        }
    }

    private VBox buildCard(Seance s, Map<Integer, String> courseTitleById,
                           List<User> children, SubscriptionService subService, int parentId) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #E9EAF2; -fx-border-radius: 12; -fx-border-width: 1;");

        String courseTitle = courseTitleById.getOrDefault(s.getCourseId(), "Cours #" + s.getCourseId());
        Label courseLbl = new Label("📚 " + courseTitle);
        courseLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #3B82F6; -fx-font-weight: bold; " +
                "-fx-background-color: #EFF6FF; -fx-padding: 2 8; -fx-background-radius: 999;");

        Label titleLbl = new Label(s.getTitle() != null ? s.getTitle() : "Séance sans titre");
        titleLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");
        titleLbl.setWrapText(true);

        String dateStr = s.getStartTime() != null ? s.getStartTime().format(FMT)
                : (s.getDate() != null ? s.getDate().toString() : "—");
        Label dateLbl = new Label("🗓 " + dateStr);
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        HBox meta = new HBox(10);
        if (s.getLocation() != null && !s.getLocation().isBlank()) {
            Label loc = new Label("📍 " + s.getLocation());
            loc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            meta.getChildren().add(loc);
        }

        // "Inscrire mon enfant" button
        if (!children.isEmpty()) {
            Button inscrireBtn = new Button("✅ Inscrire mon enfant");
            inscrireBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;");
            inscrireBtn.setOnAction(e -> showRegisterDialog(s, children, subService, parentId, courseTitleById));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            meta.getChildren().addAll(spacer, inscrireBtn);
        }

        card.getChildren().addAll(courseLbl, titleLbl, dateLbl, meta);
        return card;
    }

    private void showRegisterDialog(Seance seance, List<User> children,
                                    SubscriptionService subService, int parentId,
                                    Map<Integer, String> courseTitleById) {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Inscrire un enfant");
        dialog.setHeaderText("Inscrire votre enfant à : " + seance.getTitle());

        ButtonType inscrireType = new ButtonType("Inscrire", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(inscrireType, ButtonType.CANCEL);

        ComboBox<User> childCombo = new ComboBox<>(FXCollections.observableArrayList(children));
        childCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(User u) { return u != null ? u.getFirstName() + " " + u.getLastName() : ""; }
            @Override public User fromString(String s) { return null; }
        });
        childCombo.getSelectionModel().selectFirst();
        childCombo.setMaxWidth(Double.MAX_VALUE);

        VBox content = new VBox(10,
                new Label("Choisissez l'enfant à inscrire :"),
                childCombo
        );
        content.setPadding(new Insets(20));
        content.setPrefWidth(350);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(btn -> btn == inscrireType ? childCombo.getValue() : null);

        dialog.showAndWait().ifPresent(child -> {
            try {
                // Check if already subscribed to the course
                boolean alreadySub = subService.estAbonneActif(child.getId(), seance.getCourseId());
                if (alreadySub) {
                    new Alert(Alert.AlertType.INFORMATION,
                            child.getFirstName() + " est déjà inscrit(e) à ce cours !", ButtonType.OK).showAndWait();
                    return;
                }

                Subscription sub = new Subscription();
                sub.setParentId(parentId);
                sub.setKidId(child.getId());
                sub.setCourseId(seance.getCourseId());
                sub.setSubscribedAt(LocalDateTime.now());
                sub.setActive(true);
                subService.ajouter(sub);

                new Alert(Alert.AlertType.INFORMATION,
                        "✅ " + child.getFirstName() + " a été inscrit(e) avec succès !", ButtonType.OK).showAndWait();
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur : " + ex.getMessage(), ButtonType.OK).showAndWait();
            }
        });
    }
}
