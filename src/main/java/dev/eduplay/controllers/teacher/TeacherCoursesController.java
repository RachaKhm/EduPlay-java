package dev.eduplay.controllers.teacher;

import dev.eduplay.core.AppContext;
import dev.eduplay.services.CourseService;
import dev.eduplay.entities.Course;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class TeacherCoursesController {

    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> levelFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> sortBy;
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private Button addButton;

    private final ObservableList<Course> allCourses = FXCollections.observableArrayList();
    private FilteredList<Course> filtered;
    private SortedList<Course> sorted;

    private CourseService courseService;

    @FXML
    public void initialize() {
        if (pageSubtitle != null) {
            pageSubtitle.setText("Gérez vos cours, créez et mettez à jour le contenu.");
        }

        try {
            courseService = new CourseService();
        } catch (SQLException e) {
            showError("Base de données", "Impossible d'initialiser CourseService", e.getMessage());
            return;
        }

        initFiltersAndSort();
        initFilteringPipeline();
        reload();

        if (addButton != null) {
            addButton.setOnAction(e -> openCourseForm(null));
        }
    }

    private void initFiltersAndSort() {
        if (levelFilter != null) levelFilter.setItems(FXCollections.observableArrayList("Tous"));
        if (statusFilter != null) statusFilter.setItems(FXCollections.observableArrayList("Tous"));
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
        if (statusFilter != null) statusFilter.valueProperty().addListener((o, a, b) -> refilter.run());
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
        final String status = statusFilter != null ? statusFilter.getValue() : "Tous";

        filtered.setPredicate(course -> {
            if (course == null) return false;

            boolean matchesSearch = q.isBlank()
                    || normalize(course.getTitle()).contains(q)
                    || normalize(course.getDescription()).contains(q)
                    || normalize(course.getLevel()).contains(q)
                    || normalize(course.getStatus()).contains(q);

            boolean matchesLevel = level == null || "Tous".equals(level)
                    || Objects.equals(level, course.getLevel());

            boolean matchesStatus = status == null || "Tous".equals(status)
                    || Objects.equals(status, course.getStatus());

            return matchesSearch && matchesLevel && matchesStatus;
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
            Integer teacherId = AppContext.getCurrentUser() != null ? AppContext.getCurrentUser().getId() : null;
            if (teacherId == null) {
                showError("Session", "Aucun enseignant connecté", "Veuillez vous reconnecter.");
                return;
            }
            allCourses.setAll(courseService.afficherParTeacher(teacherId));
            refreshFilterOptions();
            applyFilters();
        } catch (SQLException e) {
            showError("Base de données", "Impossible de charger les cours", e.getMessage());
        }
    }

    private void refreshFilterOptions() {
        Set<String> levels = new LinkedHashSet<>();
        Set<String> statuses = new LinkedHashSet<>();
        for (Course c : allCourses) {
            if (c == null) continue;
            if (c.getLevel() != null && !c.getLevel().isBlank()) levels.add(c.getLevel());
            if (c.getStatus() != null && !c.getStatus().isBlank()) statuses.add(c.getStatus());
        }

        if (levelFilter != null) {
            String previous = levelFilter.getValue();
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("Tous");
            items.addAll(levels);
            levelFilter.setItems(items);
            levelFilter.setValue(previous != null && items.contains(previous) ? previous : "Tous");
        }
        if (statusFilter != null) {
            String previous = statusFilter.getValue();
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("Tous");
            items.addAll(statuses);
            statusFilter.setItems(items);
            statusFilter.setValue(previous != null && items.contains(previous) ? previous : "Tous");
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
                """);

        Label title = new Label(safe(c.getTitle(), "Sans titre"));
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #22223A;");

        Label desc = new Label(ellipsis(safe(c.getDescription(), ""), 140));
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #777799;");

        HBox meta = new HBox(8,
                pill("Niveau", safe(c.getLevel(), "—"), "#F0F5FF", "#4A90D9"),
                pill("Statut", safe(c.getStatus(), "—"), "#FFF5F7", "#E94560")
        );
        meta.setAlignment(Pos.CENTER_LEFT);

        Label small = new Label(formatSmall(c));
        small.setStyle("-fx-font-size: 11px; -fx-text-fill: #9999BB;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button edit = new Button("Modifier");
        edit.setStyle(buttonStyle("#4A90D9"));
        edit.setOnAction(e -> openCourseForm(c));

        Button del = new Button("Supprimer");
        del.setStyle(buttonStyle("#E94560"));
        del.setOnAction(e -> deleteCourse(c));

        HBox actions = new HBox(10, edit, del);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, desc, meta, small, spacer, actions);
        return card;
    }

    private void deleteCourse(Course c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer ce cours ?\n\n" + safe(c.getTitle(), ""),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    courseService.supprimer(c.getId());
                    reload();
                } catch (SQLException e) {
                    showError("Base de données", "Suppression impossible", e.getMessage());
                }
            }
        });
    }

    private void openCourseForm(Course existing) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/teacher/CourseFormView.fxml"));
            Parent root = loader.load();
            CourseFormController ctrl = loader.getController();

            Integer teacherId = AppContext.getCurrentUser() != null ? AppContext.getCurrentUser().getId() : null;
            if (teacherId == null) {
                showError("Session", "Aucun enseignant connecté", "Veuillez vous reconnecter.");
                return;
            }

            ctrl.init(courseService, teacherId, existing, saved -> reload());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "Ajouter un cours" : "Modifier le cours");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            showError("UI", "Impossible d'ouvrir le formulaire", e.getMessage());
        }
    }

    private static Label pill(String label, String value, String bg, String fg) {
        Label l = new Label(label + ": " + value);
        l.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" +
                "-fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 999;");
        return l;
    }

    private static String buttonStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 12;" +
                "-fx-cursor: hand;";
    }

    private static String formatSmall(Course c) {
        String dur = c.getDurationTraining() != null ? (c.getDurationTraining() + " min") : "Durée —";
        String created = c.getCreatedAt() != null
                ? c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                : "—";
        return dur + "  •  Créé: " + created;
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

    private void showError(String title, String header, String details) {
        Alert a = new Alert(Alert.AlertType.ERROR, details == null ? "" : details, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(header);
        a.showAndWait();
    }
}

