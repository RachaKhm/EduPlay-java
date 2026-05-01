package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.entities.Subscription;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.GroqRecommendationService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ParentCoursesController {

    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> sortBy;
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextArea aiRecommendationLabel;
    @FXML private Button refreshAiButton;

    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private FilteredList<Course> filtered;
    private SortedList<Course> sorted;

    private CourseService courseService;
    private SeanceService seanceService;
    private SubscriptionService subscriptionService;

    @FXML
    public void initialize() {
        if (!AppContext.isParent()) {
            if (pageSubtitle != null) pageSubtitle.setText("Accès réservé aux parents.");
            return;
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText("Cours publiés — cliquez sur une carte pour voir les séances et inscrire vos enfants.");
        }

        try {
            courseService = new CourseService();
            seanceService = new SeanceService();
            subscriptionService = new SubscriptionService();
        } catch (SQLException e) {
            showError("Base de données", e.getMessage());
            return;
        }

        if (refreshAiButton != null) {
            refreshAiButton.setOnAction(e -> loadAiRecommendationAsync());
        }

        initFilters();
        initFilteringPipeline();
        reload();
        loadAiRecommendationAsync();
    }

    private void initFilters() {
        if (levelFilter != null) levelFilter.setItems(FXCollections.observableArrayList("Tous"));
        if (sortBy != null) {
            sortBy.setItems(FXCollections.observableArrayList(
                    "Plus récents",
                    "Titre (A → Z)",
                    "Titre (Z → A)",
                    "Durée (courte → longue)",
                    "Durée (longue → courte)"
            ));
            sortBy.getSelectionModel().selectFirst();
        }
    }

    private void initFilteringPipeline() {
        filtered = new FilteredList<>(allCourses, c -> true);
        sorted = new SortedList<>(filtered);

        Runnable refilter = this::applyFilters;
        if (searchField != null) searchField.textProperty().addListener((o, a, b) -> refilter.run());
        if (levelFilter != null) levelFilter.valueProperty().addListener((o, a, b) -> refilter.run());
        if (sortBy != null) sortBy.valueProperty().addListener((o, a, b) -> applySorting());

        applyFilters();
        applySorting();

        if (countLabel != null) {
            countLabel.textProperty().bind(Bindings.size(sorted).asString().concat(" cours"));
        }

        sorted.addListener((javafx.collections.ListChangeListener<? super Course>) c -> renderCards());
    }

    private void applyFilters() {
        final String q = normalize(searchField != null ? searchField.getText() : "");
        final String level = levelFilter != null ? levelFilter.getValue() : "Tous";

        filtered.setPredicate(course -> {
            if (course == null) return false;
            boolean matchesSearch = q.isBlank()
                    || normalize(course.getTitle()).contains(q)
                    || normalize(course.getDescription()).contains(q)
                    || normalize(course.getLevel()).contains(q);
            boolean matchesLevel = level == null || "Tous".equals(level)
                    || Objects.equals(level, course.getLevel());
            return matchesSearch && matchesLevel;
        });
        renderCards();
    }

    private void applySorting() {
        String s = sortBy != null ? sortBy.getValue() : "Plus récents";
        Comparator<Course> comparator = switch (s) {
            case "Titre (A → Z)" -> Comparator.comparing(c -> safeLower(c.getTitle()));
            case "Titre (Z → A)" -> Comparator.comparing((Course c) -> safeLower(c.getTitle())).reversed();
            case "Durée (courte → longue)" -> Comparator.comparingInt(c -> c.getDurationTraining() != null ? c.getDurationTraining() : Integer.MAX_VALUE);
            case "Durée (longue → courte)" -> Comparator.<Course>comparingInt(c -> c.getDurationTraining() != null ? c.getDurationTraining() : Integer.MIN_VALUE).reversed();
            case "Plus récents" -> Comparator.comparing((Course c) -> c.getCreatedAt() != null ? c.getCreatedAt() : java.time.LocalDateTime.MIN).reversed();
            default -> Comparator.comparingInt(Course::getId);
        };
        sorted.setComparator(comparator);
        renderCards();
    }

    private void reload() {
        try {
            allCourses.setAll(courseService.afficherPublies());
            refreshLevelFilter();
            applyFilters();
        } catch (SQLException e) {
            showError("Impossible de charger les cours", e.getMessage());
        }
    }

    private void refreshLevelFilter() {
        Set<String> levels = new LinkedHashSet<>();
        for (Course c : allCourses) {
            if (c != null && c.getLevel() != null && !c.getLevel().isBlank()) {
                levels.add(c.getLevel());
            }
        }
        if (levelFilter != null) {
            String previous = levelFilter.getValue();
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("Tous");
            items.addAll(levels);
            levelFilter.setItems(items);
            levelFilter.setValue(previous != null && items.contains(previous) ? previous : "Tous");
        }
    }

    private void renderCards() {
        if (cardsPane == null) return;
        cardsPane.getChildren().clear();
        for (Course c : sorted) {
            cardsPane.getChildren().add(makeCard(c));
        }
    }

    private Region makeCard(Course c) {
        VBox card = new VBox(10);
        card.setPrefWidth(280);
        card.setPadding(new Insets(14));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-color: #E9EAF2;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                -fx-cursor: hand;
                """);
        card.setOnMouseClicked(e -> openCourseDetail(c));

        Label title = new Label(safe(c.getTitle(), "Sans titre"));
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #22223A;");

        Label desc = new Label(ellipsis(safe(c.getDescription(), ""), 140));
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #777799;");

        HBox meta = new HBox(8, pill("Niveau", safe(c.getLevel(), "—"), "#F0F5FF", "#4A90D9"));
        meta.setAlignment(Pos.CENTER_LEFT);

        Label hint = new Label("Voir séances et inscriptions →");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #4A90D9;");

        card.getChildren().addAll(title, desc, meta, hint);
        return card;
    }

    private void openCourseDetail(Course c) {
        AppContext.setParentBrowsingCourseId(c.getId());
        Router.reload("parent_course_detail");
    }

    private void loadAiRecommendationAsync() {
        if (aiRecommendationLabel != null) {
            aiRecommendationLabel.setText("Chargement de la recommandation IA...");
        }
        if (refreshAiButton != null) refreshAiButton.setDisable(true);

        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                Integer parentId = AppContext.getCurrentUser() != null ? AppContext.getCurrentUser().getId() : null;
                if (parentId == null) {
                    return "Impossible de générer une recommandation : parent non connecté.";
                }

                List<Subscription> subscriptions = subscriptionService.afficherTous().stream()
                        .filter(Subscription::isActive)
                        .filter(s -> s.getParentId() == parentId)
                        .toList();

                Set<Integer> subscribedCourseIds = subscriptions.stream()
                        .map(Subscription::getCourseId)
                        .collect(Collectors.toSet());

                Map<Integer, Course> courseById = new HashMap<>();
                for (Course c : allCourses) {
                    if (c != null) courseById.put(c.getId(), c);
                }

                List<Seance> upcoming = seanceService.afficherTous().stream()
                        .filter(s -> s != null && !"cancelled".equalsIgnoreCase(safe(s.getStatus(), "")))
                        .filter(s -> {
                            LocalDate d = s.getDate() != null
                                    ? s.getDate()
                                    : s.getStartTime() != null ? s.getStartTime().toLocalDate() : null;
                            return d != null && !d.isBefore(LocalDate.now());
                        })
                        .sorted(Comparator.comparing(ParentCoursesController::seanceSortDate))
                        .limit(20)
                        .toList();

                List<String> lines = new ArrayList<>();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Seance s : upcoming) {
                    Course c = courseById.get(s.getCourseId());
                    if (c == null) continue;
                    LocalDate d = s.getDate() != null
                            ? s.getDate()
                            : s.getStartTime() != null ? s.getStartTime().toLocalDate() : null;
                    String dateText = d != null ? d.format(df) : "date inconnue";
                    String hourText = s.getStartTime() != null ? s.getStartTime().toLocalTime().toString() : "heure ?";
                    String prefix = subscribedCourseIds.contains(c.getId()) ? "[ABONNEMENT]" : "[CATALOGUE]";
                    lines.add(prefix + " " + c.getTitle() + " | " + safe(s.getTitle(), "Séance") + " | " + dateText + " " + hourText);
                }

                if (lines.isEmpty()) {
                    return """
                            Aucune séance à venir pour le moment.
                            Revenez plus tard ou consultez le catalogue pour voir les nouveaux cours publiés.
                            """.trim();
                }

                String context = """
                        Tu aides un parent sur EduPlay.
                        Rédige en français une recommandation utile en 3-5 lignes maximum.
                        Règles strictes:
                        - Utilise uniquement les séances listées ci-dessous.
                        - Ne demande pas de contacter le support, ne parle pas de problèmes techniques.
                        - Ne mentionne pas les tags [ABONNEMENT] ou [CATALOGUE] dans la réponse.
                        - Priorise les séances [ABONNEMENT].
                        - Donne des actions concrètes (ex: séance à privilégier cette semaine).

                        Séances disponibles:
                        %s
                        """.formatted(String.join("\n", lines));

                GroqRecommendationService groq = new GroqRecommendationService(new File("config/groq.properties"));
                return groq.recommendSeances(context);
            }
        };

        task.setOnSucceeded(e -> {
            if (aiRecommendationLabel != null) aiRecommendationLabel.setText(task.getValue());
            if (refreshAiButton != null) refreshAiButton.setDisable(false);
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex != null ? ex.getMessage() : "Erreur inconnue";
            if (aiRecommendationLabel != null) {
                aiRecommendationLabel.setText("Recommandation IA indisponible pour le moment.\n" + msg);
            }
            if (refreshAiButton != null) refreshAiButton.setDisable(false);
        });

        Thread t = new Thread(task, "parent-ai-recommendation");
        t.setDaemon(true);
        t.start();
    }

    private static LocalDate seanceSortDate(Seance s) {
        if (s.getDate() != null) return s.getDate();
        if (s.getStartTime() != null) return s.getStartTime().toLocalDate();
        return LocalDate.MAX;
    }

    private static Label pill(String label, String value, String bg, String fg) {
        Label l = new Label(label + ": " + value);
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                "-fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 999;");
        return l;
    }

    private static String ellipsis(String s, int max) {
        if (s == null) return "";
        String t = s.strip();
        if (t.length() <= max) return t;
        return t.substring(0, Math.max(0, max - 1)) + "…";
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.toLowerCase(Locale.ROOT).trim();
    }

    private static String safeLower(String s) {
        return normalize(safe(s, ""));
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private void showError(String header, String details) {
        Alert a = new Alert(Alert.AlertType.ERROR, details == null ? "" : details, ButtonType.OK);
        a.setHeaderText(header);
        a.showAndWait();
    }
}
