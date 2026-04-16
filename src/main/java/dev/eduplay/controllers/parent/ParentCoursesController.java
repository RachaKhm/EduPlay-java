package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Course;
import dev.eduplay.services.CourseService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ParentCoursesController {

    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> sortBy;
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;

    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private FilteredList<Course> filtered;
    private SortedList<Course> sorted;

    private CourseService courseService;

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
        } catch (SQLException e) {
            showError("Base de données", e.getMessage());
            return;
        }

        initFilters();
        initFilteringPipeline();
        reload();
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
