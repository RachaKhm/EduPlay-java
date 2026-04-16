package dev.eduplay.controllers.child;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Course;
import dev.eduplay.services.CourseService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Locale;

public class ChildMyCoursesController {

    @FXML private Label pageSubtitle;
    @FXML private TextField searchField;
    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;

    private final ObservableList<Course> courses = FXCollections.observableArrayList();
    private FilteredList<Course> filtered;

    private CourseService courseService;

    @FXML
    public void initialize() {
        if (!AppContext.isChild() || AppContext.getCurrentUser() == null) {
            if (pageSubtitle != null) pageSubtitle.setText("Accès réservé aux élèves.");
            return;
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText("Cours auxquels tes parents t’ont inscrit(e). Touche une carte pour voir les séances et le PDF.");
        }

        try {
            courseService = new CourseService();
        } catch (SQLException e) {
            showError(e.getMessage());
            return;
        }

        int kidId = AppContext.getCurrentUser().getId();
        filtered = new FilteredList<>(courses, c -> true);
        if (searchField != null) {
            searchField.textProperty().addListener((o, a, b) -> applySearch());
        }
        if (countLabel != null) {
            countLabel.textProperty().bind(Bindings.size(filtered).asString().concat(" cours"));
        }
        filtered.addListener((javafx.collections.ListChangeListener<? super Course>) c -> renderCards());

        try {
            courses.setAll(courseService.afficherPourEnfantAbonne(kidId));
            applySearch();
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    private void applySearch() {
        String q = normalize(searchField != null ? searchField.getText() : "");
        filtered.setPredicate(c -> c != null && (q.isBlank()
                || normalize(c.getTitle()).contains(q)
                || normalize(c.getDescription()).contains(q)
                || normalize(c.getLevel()).contains(q)));
        renderCards();
    }

    private void renderCards() {
        if (cardsPane == null) return;
        cardsPane.getChildren().clear();
        for (Course c : filtered) {
            cardsPane.getChildren().add(makeCard(c));
        }
        if (filtered.isEmpty()) {
            Label empty = new Label("Aucun cours pour l’instant. Demande à tes parents de t’inscrire depuis leur espace « Cours ».");
            empty.setWrapText(true);
            empty.setMaxWidth(520);
            empty.setStyle("-fx-text-fill: #9999BB; -fx-font-size: 13px;");
            cardsPane.getChildren().add(empty);
        }
    }

    private VBox makeCard(Course c) {
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
        card.setOnMouseClicked(e -> openDetail(c));

        Label title = new Label(safe(c.getTitle(), "Sans titre"));
        title.setWrapText(true);
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #22223A;");

        Label desc = new Label(ellipsis(safe(c.getDescription(), ""), 140));
        desc.setWrapText(true);
        desc.setStyle("-fx-font-size: 12px; -fx-text-fill: #777799;");

        HBox meta = new HBox(8, pill("Niveau", safe(c.getLevel(), "—"), "#E0FFE8", "#106030"));
        meta.setAlignment(Pos.CENTER_LEFT);

        Label hint = new Label("Séances & PDF →");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #4A90D9;");

        card.getChildren().addAll(title, desc, meta, hint);
        return card;
    }

    private void openDetail(Course c) {
        AppContext.setChildBrowsingCourseId(c.getId());
        Router.reload("child_course_detail");
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

    private static String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}
