package dev.eduplay.controllers.admin;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AdminSeancesController {

    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> courseFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> sortBy;
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private Button addButton;

    private final ObservableList<Seance> allSeances = FXCollections.observableArrayList();
    private final Map<Integer, String> courseTitleById = new HashMap<>();
    private final Map<String, Integer> courseChoiceToId = new LinkedHashMap<>();
    private FilteredList<Seance> filtered;
    private SortedList<Seance> sorted;

    private SeanceService seanceService;
    private CourseService courseService;

    @FXML
    public void initialize() {
        if (!AppContext.isAdmin()) {
            if (pageSubtitle != null) pageSubtitle.setText("Accès réservé aux administrateurs.");
            return;
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText("Planifiez et gérez les séances de tous les cours.");
        }

        try {
            seanceService = new SeanceService();
            courseService = new CourseService();
        } catch (SQLException e) {
            showError("Base de données", "Impossible d'initialiser les services", e.getMessage());
            return;
        }

        initCourseFilterChoices();
        initFiltersAndSort();
        initFilteringPipeline();
        reload();

        if (addButton != null) {
            addButton.setOnAction(e -> openSeanceForm(null));
        }
    }

    private void initCourseFilterChoices() {
        courseChoiceToId.clear();
        courseChoiceToId.put("Tous les cours", null);
        try {
            for (Course c : courseService.afficherTous()) {
                String label = c.getId() + " — " + (c.getTitle() != null ? c.getTitle() : "(sans titre)");
                courseChoiceToId.put(label, c.getId());
            }
        } catch (SQLException e) {
            showError("Base de données", "Impossible de charger les cours", e.getMessage());
        }
        if (courseFilter != null) {
            courseFilter.setItems(FXCollections.observableArrayList(courseChoiceToId.keySet()));
            courseFilter.getSelectionModel().selectFirst();
        }
    }

    private void initFiltersAndSort() {
        if (statusFilter != null) statusFilter.setItems(FXCollections.observableArrayList("Tous"));
        if (sortBy != null) {
            sortBy.setItems(FXCollections.observableArrayList(
                    "Plus récentes",
                    "Date (prochaine → lointaine)",
                    "Date (lointaine → prochaine)",
                    "Titre (A → Z)"
            ));
            sortBy.getSelectionModel().selectFirst();
        }
    }

    private void initFilteringPipeline() {
        filtered = new FilteredList<>(allSeances, s -> true);
        sorted = new SortedList<>(filtered);

        Runnable refilter = this::applyFilters;
        if (searchField != null) searchField.textProperty().addListener((o, a, b) -> refilter.run());
        if (courseFilter != null) courseFilter.valueProperty().addListener((o, a, b) -> refilter.run());
        if (statusFilter != null) statusFilter.valueProperty().addListener((o, a, b) -> refilter.run());
        if (sortBy != null) sortBy.valueProperty().addListener((o, a, b) -> applySorting());

        applyFilters();
        applySorting();

        if (countLabel != null) {
            countLabel.textProperty().bind(Bindings.size(sorted).asString().concat(" séances"));
        }

        sorted.addListener((javafx.collections.ListChangeListener<? super Seance>) c -> renderCards());
    }

    private void applyFilters() {
        final String q = normalize(searchField != null ? searchField.getText() : "");
        final Integer courseId = courseFilter != null
                ? courseChoiceToId.get(courseFilter.getValue())
                : null;
        final String status = statusFilter != null ? statusFilter.getValue() : "Tous";

        filtered.setPredicate(seance -> {
            if (seance == null) return false;

            String courseTitle = courseTitleById.getOrDefault(seance.getCourseId(), "");

            boolean matchesSearch = q.isBlank()
                    || normalize(seance.getTitle()).contains(q)
                    || normalize(seance.getLocation()).contains(q)
                    || normalize(seance.getDescription()).contains(q)
                    || normalize(seance.getStatus()).contains(q)
                    || normalize(courseTitle).contains(q)
                    || String.valueOf(seance.getCourseId()).contains(q);

            boolean matchesCourse = courseId == null || seance.getCourseId() == courseId;

            boolean matchesStatus = status == null || "Tous".equals(status)
                    || Objects.equals(status, seance.getStatus());

            return matchesSearch && matchesCourse && matchesStatus;
        });

        renderCards();
    }

    private void applySorting() {
        String s = sortBy != null ? sortBy.getValue() : "Plus récentes";
        Comparator<Seance> comparator = switch (s) {
            case "Titre (A → Z)" -> Comparator.comparing(x -> safeLower(x.getTitle()));
            case "Date (prochaine → lointaine)" -> Comparator.comparing(AdminSeancesController::sortDateTime);
            case "Date (lointaine → prochaine)" -> Comparator.comparing(AdminSeancesController::sortDateTime).reversed();
            case "Plus récentes" -> Comparator.comparingInt(Seance::getId).reversed();
            default -> Comparator.comparingInt(Seance::getId);
        };
        sorted.setComparator(comparator);
        renderCards();
    }

    private static LocalDateTime sortDateTime(Seance s) {
        if (s.getStartTime() != null) return s.getStartTime();
        if (s.getDate() != null) return s.getDate().atStartOfDay();
        return LocalDateTime.MAX;
    }

    private void reload() {
        try {
            courseTitleById.clear();
            for (Course c : courseService.afficherTous()) {
                courseTitleById.put(c.getId(), c.getTitle() != null ? c.getTitle() : "");
            }
            allSeances.setAll(seanceService.afficherTous());
            initCourseFilterChoices();
            refreshStatusFilterOptions();
            applyFilters();
        } catch (SQLException e) {
            showError("Base de données", "Impossible de charger les séances", e.getMessage());
        }
    }

    private void refreshStatusFilterOptions() {
        Set<String> statuses = new LinkedHashSet<>();
        for (Seance s : allSeances) {
            if (s != null && s.getStatus() != null && !s.getStatus().isBlank()) {
                statuses.add(s.getStatus());
            }
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
        for (Seance s : sorted) {
            cardsPane.getChildren().add(makeCard(s));
        }
    }

    private Region makeCard(Seance s) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setPadding(new Insets(14));
        card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 12;
                -fx-border-color: #E9EAF2;
                -fx-border-radius: 12;
                -fx-border-width: 1;
                """);

        Label title = new Label(safe(s.getTitle(), "Sans titre"));
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #22223A;");

        String cTitle = courseTitleById.getOrDefault(s.getCourseId(), "Cours #" + s.getCourseId());
        Label courseLbl = new Label("Cours : " + cTitle);
        courseLbl.setWrapText(true);
        courseLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #666688;");

        String when = formatWhen(s);
        Label whenLbl = new Label(when);
        whenLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #777799;");

        HBox meta = new HBox(8,
                pill("Lieu", safe(s.getLocation(), "—"), "#F0F5FF", "#4A90D9"),
                pill("Statut", safe(s.getStatus(), "—"), "#FFF5F7", "#E94560")
        );
        meta.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button edit = new Button("Modifier");
        edit.setStyle(buttonStyle("#4A90D9"));
        edit.setOnAction(e -> openSeanceForm(s));

        Button del = new Button("Supprimer");
        del.setStyle(buttonStyle("#E94560"));
        del.setOnAction(e -> deleteSeance(s));

        HBox actions = new HBox(10, edit, del);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(title, courseLbl, whenLbl, meta, spacer, actions);
        return card;
    }

    private void deleteSeance(Seance s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer cette séance ?\n\n" + safe(s.getTitle(), ""),
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    seanceService.supprimer(s.getId());
                    reload();
                } catch (SQLException e) {
                    showError("Base de données", "Suppression impossible", e.getMessage());
                }
            }
        });
    }

    private void openSeanceForm(Seance existing) {
        try {
            List<Course> courses = courseService.afficherTous();
            if (courses.isEmpty()) {
                showError("Données", "Aucun cours", "Créez au moins un cours avant d'ajouter une séance.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/AdminSeanceFormView.fxml"));
            Parent root = loader.load();
            AdminSeanceFormController ctrl = loader.getController();
            ctrl.init(seanceService, courses, existing, saved -> reload());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "Ajouter une séance" : "Modifier la séance");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException e) {
            showError("UI", "Impossible d'ouvrir le formulaire", e.getMessage());
        } catch (SQLException e) {
            showError("Base de données", "Impossible de charger les cours", e.getMessage());
        }
    }

    private static String formatWhen(Seance s) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (s.getStartTime() != null && s.getEndTime() != null) {
            return s.getStartTime().format(dtf) + " → " + s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (s.getDate() != null) {
            return s.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        return "—";
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
