package dev.eduplay.controllers.teacher;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.CourseReviewService;
import dev.eduplay.entities.CourseReview;
import javafx.collections.FXCollections;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class TeacherSeancesController {

    @FXML private VBox seancesContainer;
    @FXML private Label subtitleLabel;

    private CourseService courseService;
    private SeanceService seanceService;
    private CourseReviewService reviewService;
    private Map<Integer, String> courseTitleById = new HashMap<>();

    @FXML
    public void initialize() {
        try {
            courseService = new CourseService();
            seanceService = new SeanceService();
            reviewService = new CourseReviewService();
        } catch (SQLException e) {
            showErr("Connexion DB impossible: " + e.getMessage());
            return;
        }

        loadSeances();
    }

    private void loadSeances() {
        seancesContainer.getChildren().clear();
        int teacherId = AppContext.getCurrentUser().getId();

        try {
            // Load teacher's courses
            List<Course> myCourses = courseService.afficherParTeacher(teacherId);
            courseTitleById.clear();
            for (Course c : myCourses) {
                courseTitleById.put(c.getId(), c.getTitle() != null ? c.getTitle() : "(sans titre)");
            }

            // Load séances for each course
            List<Seance> all = seanceService.afficherTous().stream()
                    .filter(s -> courseTitleById.containsKey(s.getCourseId()))
                    .sorted((a, b) -> {
                        if (a.getStartTime() == null) return 1;
                        if (b.getStartTime() == null) return -1;
                        return a.getStartTime().compareTo(b.getStartTime());
                    })
                    .toList();

            if (subtitleLabel != null)
                subtitleLabel.setText(all.size() + " séance(s) planifiée(s) dans vos cours");

            if (all.isEmpty()) {
                Label empty = new Label("Aucune séance créée pour vos cours.");
                empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-padding: 30;");
                seancesContainer.getChildren().add(empty);
                return;
            }

            for (Seance s : all) seancesContainer.getChildren().add(buildCard(s));

        } catch (SQLException e) {
            showErr("Erreur DB: " + e.getMessage());
        }
    }

    private VBox buildCard(Seance s) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-border-color: #E9EAF2; -fx-border-radius: 12; -fx-border-width: 1;");

        String courseTitle = courseTitleById.getOrDefault(s.getCourseId(), "Cours #" + s.getCourseId());
        Label courseLbl = new Label("📚 " + courseTitle);
        courseLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #3B82F6; -fx-font-weight: bold;");

        Label title = new Label(s.getTitle() != null ? s.getTitle() : "Sans titre");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #22223A;");
        title.setWrapText(true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE dd MMM yyyy · HH:mm", Locale.FRENCH);
        String when = s.getStartTime() != null ? s.getStartTime().format(dtf)
                : (s.getDate() != null ? s.getDate().toString() : "—");
        Label dateLbl = new Label("🗓 " + when);
        dateLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        String loc = s.getLocation() != null && !s.getLocation().isBlank() ? "📍 " + s.getLocation() : "";
        Label locLbl = new Label(loc);
        locLbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Button viewReviews = new Button("⭐ Avis");
        viewReviews.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 7; -fx-padding: 6 14; -fx-cursor: hand;");
        viewReviews.setOnAction(e -> openReviewsDialog(s.getCourseId(), courseTitle));

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 7; -fx-padding: 6 14; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditForm(s));

        Button delBtn = new Button("Supprimer");
        delBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-background-radius: 7; -fx-padding: 6 14; -fx-cursor: hand;");
        delBtn.setOnAction(e -> deleteSeance(s));

        HBox actions = new HBox(8, viewReviews, editBtn, delBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(courseLbl, title, dateLbl, locLbl, actions);
        return card;
    }

    private void openEditForm(Seance s) {
        openForm(s);
    }

    private void openForm(Seance existing) {
        try {
            int teacherId = AppContext.getCurrentUser().getId();
            List<Course> myCourses = courseService.afficherParTeacher(teacherId);
            if (myCourses.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Créez d'abord un cours avant d'ajouter une séance.", ButtonType.OK).showAndWait();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/AdminSeanceFormView.fxml"));
            Parent root = loader.load();
            dev.eduplay.controllers.admin.AdminSeanceFormController ctrl = loader.getController();
            ctrl.init(seanceService, myCourses, existing, saved -> loadSeances());

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(existing == null ? "Nouvelle séance" : "Modifier la séance");
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (IOException | SQLException e) {
            showErr("Impossible d'ouvrir le formulaire: " + e.getMessage());
        }
    }

    private void deleteSeance(Seance s) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la séance « " + s.getTitle() + " » ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try {
                    seanceService.supprimer(s.getId());
                    loadSeances();
                } catch (SQLException e) {
                    showErr("Suppression impossible: " + e.getMessage());
                }
            }
        });
    }

    private void showErr(String msg) {
        Label lbl = new Label("⚠ " + msg);
        lbl.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 13px; -fx-padding: 10;");
        seancesContainer.getChildren().add(lbl);
    }

    private void openReviewsDialog(int courseId, String courseTitle) {
        try {
            java.util.List<CourseReview> reviews = reviewService.getAllReviewsForCourse(courseId);
            
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Avis sur le cours");
            dialog.setHeaderText("Avis pour : " + (courseTitle == null ? "" : courseTitle));
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setPrefWidth(400);

            if (reviews.isEmpty()) {
                Label empty = new Label("Aucun avis pour ce cours.");
                empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px;");
                content.getChildren().add(empty);
            } else {
                double avg = reviewService.getAverageRating(courseId);
                Label avgLbl = new Label(String.format(Locale.US, "Note moyenne : %.1f ⭐", avg));
                avgLbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F59E0B;");
                content.getChildren().add(avgLbl);

                ScrollPane scroll = new ScrollPane();
                scroll.setFitToWidth(true);
                scroll.setPrefHeight(300);
                scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

                VBox list = new VBox(10);
                for (CourseReview r : reviews) {
                    VBox rBox = new VBox(5);
                    rBox.setPadding(new Insets(10));
                    rBox.setStyle("-fx-background-color: #F8FAFC; -fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");
                    
                    String stars = "⭐".repeat(r.getRating()) + "☆".repeat(5 - r.getRating());
                    Label st = new Label(stars);
                    st.setStyle("-fx-text-fill: #F59E0B; -fx-font-size: 14px;");
                    
                    Label cmt = new Label(r.getComment() == null || r.getComment().isBlank() ? "(Aucun commentaire)" : r.getComment());
                    cmt.setWrapText(true);
                    cmt.setStyle("-fx-text-fill: #334155; -fx-font-size: 13px;");

                    String date = r.getCreatedAt() != null ? r.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                    Label dt = new Label(date);
                    dt.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");

                    rBox.getChildren().addAll(st, cmt, dt);
                    list.getChildren().add(rBox);
                }
                scroll.setContent(list);
                content.getChildren().add(scroll);
            }

            dialog.getDialogPane().setContent(content);
            dialog.showAndWait();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
