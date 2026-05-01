package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.entities.Subscription;
import dev.eduplay.entities.User;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import dev.eduplay.services.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ParentCourseDetailController {

    @FXML private Button backButton;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label descriptionLabel;
    @FXML private FlowPane seancesPane;
    @FXML private Label subscriptionHint;
    @FXML private VBox kidsCheckboxes;
    @FXML private Button saveSubscriptionsButton;

    private final Map<Integer, CheckBox> kidCheckboxById = new HashMap<>();

    private CourseService courseService;
    private SeanceService seanceService;
    private SubscriptionService subscriptionService;
    private UserService userService;

    private int courseId;
    private Course course;
    private int parentId;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setOnAction(e -> {
                AppContext.clearParentBrowsingCourseId();
                Router.go("parent_courses");
            });
        }
        if (saveSubscriptionsButton != null) {
            saveSubscriptionsButton.setOnAction(e -> saveSubscriptions());
        }

        if (!AppContext.isParent() || AppContext.getCurrentUser() == null) {
            if (subscriptionHint != null) {
                subscriptionHint.setText("Vous devez être connecté en tant que parent.");
            }
            return;
        }
        parentId = AppContext.getCurrentUser().getId();

        try {
            courseService = new CourseService();
            seanceService = new SeanceService();
            subscriptionService = new SubscriptionService();
            userService = new UserService();
        } catch (SQLException e) {
            if (subscriptionHint != null) subscriptionHint.setText(e.getMessage());
            return;
        }

        Integer browsingId = AppContext.getParentBrowsingCourseId();
        if (browsingId == null || browsingId <= 0) {
            if (titleLabel != null) titleLabel.setText("Aucun cours sélectionné");
            if (subscriptionHint != null) {
                subscriptionHint.setText("Retournez au catalogue et choisissez un cours.");
            }
            return;
        }
        courseId = browsingId;
        loadAll();
    }

    private void loadAll() {
        kidCheckboxById.clear();
        if (kidsCheckboxes != null) {
            kidsCheckboxes.getChildren().clear();
        }
        if (seancesPane != null) {
            seancesPane.getChildren().clear();
        }

        try {
            Optional<Course> opt = courseService.trouverParId(courseId);
            if (opt.isEmpty()) {
                if (titleLabel != null) titleLabel.setText("Cours introuvable");
                return;
            }
            course = opt.get();
            if (!"published".equalsIgnoreCase(safe(course.getStatus(), ""))) {
                if (titleLabel != null) titleLabel.setText("Ce cours n'est pas disponible.");
                if (subscriptionHint != null) {
                    subscriptionHint.setText("Seuls les cours publiés sont accessibles.");
                }
                return;
            }

            if (titleLabel != null) titleLabel.setText(course.getTitle());
            User teacher = userService.getById(course.getTeacherId());
            String tName = teacher != null ? teacher.getFullName() : "Enseignant #" + course.getTeacherId();
            if (metaLabel != null) {
                String dur = course.getDurationTraining() != null ? course.getDurationTraining() + " min" : "Durée —";
                metaLabel.setText("Niveau : " + safe(course.getLevel(), "—") + "  •  " + dur + "  •  " + tName);
            }
            if (descriptionLabel != null) {
                descriptionLabel.setText(safe(course.getDescription(), ""));
            }

            List<Seance> seances = seanceService.parCourseId(courseId);
            if (seancesPane != null) {
                for (Seance s : seances) {
                    seancesPane.getChildren().add(makeSeanceCard(s));
                }
                if (seances.isEmpty()) {
                    Label empty = new Label("Aucune séance planifiée pour le moment.");
                    empty.setStyle("-fx-text-fill: #9999BB; -fx-font-size: 13px;");
                    seancesPane.getChildren().add(empty);
                }
            }

            List<User> kids = userService.getChildrenByParentId(parentId);
            if (kids.isEmpty()) {
                if (subscriptionHint != null) {
                    subscriptionHint.setText(
                            "Aucun enfant lié à votre compte. L'administrateur doit renseigner la colonne parent_id " +
                                    "sur les comptes « enfant » avec votre identifiant utilisateur (" + parentId + ").");
                }
                if (saveSubscriptionsButton != null) saveSubscriptionsButton.setDisable(true);
            } else {
                if (subscriptionHint != null) {
                    subscriptionHint.setText(
                            "L'inscription au cours s'applique à l'ensemble du cours (toutes les séances). " +
                                    "Cochez un ou plusieurs enfants puis enregistrez.");
                }
                Map<Integer, Subscription> latestByKid = new HashMap<>();
                for (Subscription sub : subscriptionService.listParParentEtCours(parentId, courseId)) {
                    latestByKid.put(sub.getKidId(), sub);
                }
                for (User kid : kids) {
                    CheckBox cb = new CheckBox(kid.getFullName());
                    Subscription sub = latestByKid.get(kid.getId());
                    cb.setSelected(sub != null && sub.isActive());
                    kidCheckboxById.put(kid.getId(), cb);
                    if (kidsCheckboxes != null) {
                        kidsCheckboxes.getChildren().add(cb);
                    }
                }
                if (saveSubscriptionsButton != null) saveSubscriptionsButton.setDisable(false);
            }
        } catch (SQLException e) {
            if (subscriptionHint != null) subscriptionHint.setText(e.getMessage());
        }
    }

    private Region makeSeanceCard(Seance s) {
        VBox card = new VBox(8);
        card.setPrefWidth(260);
        card.setPadding(new Insets(12));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 10;
                -fx-border-color: #E9EAF2;
                -fx-border-radius: 10;
                -fx-border-width: 1;
                """);

        Label t = new Label(safe(s.getTitle(), "Séance"));
        t.setWrapText(true);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #22223A;");

        String when = formatSeanceWhen(s);
        Label w = new Label(when);
        w.setStyle("-fx-font-size: 11px; -fx-text-fill: #777799;");

        Label loc = new Label("Lieu : " + safe(s.getLocation(), "—"));
        loc.setWrapText(true);
        loc.setStyle("-fx-font-size: 11px; -fx-text-fill: #9999BB;");

        card.getChildren().addAll(t, w, loc);
        return card;
    }

    private static String formatSeanceWhen(Seance s) {
        DateTimeFormatter d = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (s.getStartTime() != null && s.getEndTime() != null) {
            return s.getStartTime().format(dt) + " → " + s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (s.getDate() != null) {
            return s.getDate().format(d);
        }
        return "—";
    }

    private void saveSubscriptions() {
        try {
            for (Map.Entry<Integer, CheckBox> e : kidCheckboxById.entrySet()) {
                int kidId = e.getKey();
                boolean want = e.getValue().isSelected();
                Optional<Subscription> opt = subscriptionService.trouverParParentEnfantEtCours(parentId, kidId, courseId);

                if (want) {
                    if (opt.isEmpty()) {
                        Subscription s = new Subscription();
                        s.setParentId(parentId);
                        s.setKidId(kidId);
                        s.setCourseId(courseId);
                        s.setSubscribedAt(LocalDateTime.now());
                        s.setActive(true);
                        subscriptionService.ajouter(s);
                    } else {
                        Subscription s = opt.get();
                        if (!s.isActive()) {
                            s.setActive(true);
                            s.setSubscribedAt(LocalDateTime.now());
                            subscriptionService.modifier(s);
                        }
                    }
                } else {
                    if (opt.isPresent() && opt.get().isActive()) {
                        Subscription s = opt.get();
                        s.setActive(false);
                        subscriptionService.modifier(s);
                    }
                }
            }
            new Alert(Alert.AlertType.INFORMATION, "Les inscriptions ont été mises à jour.", ButtonType.OK).showAndWait();
            loadAll();
        } catch (SQLException ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }
}
