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
import dev.eduplay.services.CourseReviewService;
import dev.eduplay.entities.CourseReview;
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
    @FXML private TextField searchField;

    private List<Seance> allSeances = new ArrayList<>();
    private Map<Integer, String> courseTitleById = new HashMap<>();
    private List<User> children = new ArrayList<>();
    private SubscriptionService subService;
    private CourseReviewService reviewService;
    private int parentId;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("EEE dd MMM yyyy · HH:mm", Locale.FRENCH);

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldV, newV) -> filterAndRender());
        }
        loadSeances();
    }

    private void loadSeances() {
        seancesContainer.getChildren().clear();
        try {
            SeanceService seanceService = new SeanceService();
            CourseService courseService = new CourseService();
            UserService userService = new UserService();
            subService = new SubscriptionService();
            reviewService = new CourseReviewService();

            parentId = AppContext.getCurrentUser().getId();
            children = userService.getChildrenByParentId(parentId);

            List<Course> allCourses = courseService.afficherTous();
            courseTitleById.clear();
            for (Course c : allCourses) courseTitleById.put(c.getId(), c.getTitle());

            allSeances = seanceService.afficherTous();
            allSeances.sort((a, b) -> {
                if (a.getStartTime() == null) return 1;
                if (b.getStartTime() == null) return -1;
                return a.getStartTime().compareTo(b.getStartTime());
            });

            filterAndRender();

        } catch (SQLException e) {
            Label err = new Label("Erreur : " + e.getMessage());
            err.setStyle("-fx-text-fill: #EF4444;");
            seancesContainer.getChildren().add(err);
        }
    }

    private void filterAndRender() {
        seancesContainer.getChildren().clear();

        if (allSeances.isEmpty()) {
            Label empty = new Label("Aucune séance disponible actuellement.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-padding: 30;");
            seancesContainer.getChildren().add(empty);
            return;
        }

        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";

        List<Seance> filtered = allSeances;
        if (!query.isEmpty()) {
            filtered = allSeances.stream()
                    .filter(s -> s.getTitle() != null && s.getTitle().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        if (subtitleLabel != null)
            subtitleLabel.setText(filtered.size() + " séance(s) disponible(s) · " + children.size() + " enfant(s)");

        if (filtered.isEmpty()) {
            Label empty = new Label("Aucune séance ne correspond à votre recherche.");
            empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-padding: 30;");
            seancesContainer.getChildren().add(empty);
            return;
        }

        for (Seance s : filtered) {
            seancesContainer.getChildren().add(buildCard(s, courseTitleById, children, subService, parentId));
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

        double avgRating = 0.0;
        try {
            avgRating = reviewService.getAverageRating(s.getCourseId());
        } catch (SQLException e) {
            // Ignorer l'erreur d'affichage de note
        }
        
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().add(courseLbl);
        
        if (avgRating > 0) {
            Label ratingLbl = new Label(String.format(Locale.US, "%.1f ⭐", avgRating));
            ratingLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #F59E0B; -fx-font-weight: bold; " +
                    "-fx-background-color: #FEF3C7; -fx-padding: 2 8; -fx-background-radius: 999;");
            topRow.getChildren().add(ratingLbl);
        }

        HBox meta = new HBox(10);
        if (s.getLocation() != null && !s.getLocation().isBlank()) {
            Label loc = new Label("📍 " + s.getLocation());
            loc.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
            meta.getChildren().add(loc);
        }

        // "Inscrire mon enfant" button
        if (!children.isEmpty()) {
            Button inscrireBtn = new Button("✅ Inscrire");
            inscrireBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;");
            inscrireBtn.setOnAction(e -> showRegisterDialog(s, children, subService, parentId, courseTitleById));

            Button rateBtn = new Button("⭐ Évaluer");
            rateBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 7 16; -fx-cursor: hand;");
            rateBtn.setOnAction(e -> showRatingDialog(s.getCourseId(), courseTitle));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            meta.getChildren().addAll(spacer, rateBtn, inscrireBtn);
        }

        card.getChildren().addAll(topRow, titleLbl, dateLbl, meta);
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

    private void showRatingDialog(int courseId, String courseTitle) {
        try {
            CourseReview existing = reviewService.getReviewByUserAndCourse(parentId, courseId);
            int initialRating = existing != null ? existing.getRating() : 0;
            String initialComment = existing != null ? existing.getComment() : "";

            Dialog<CourseReview> dialog = new Dialog<>();
            dialog.setTitle("Évaluer le cours");
            dialog.setHeaderText("Donnez votre avis sur le cours : " + courseTitle);

            ButtonType submitType = new ButtonType(existing != null ? "Mettre à jour" : "Soumettre", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(submitType, ButtonType.CANCEL);

            HBox starsBox = new HBox(5);
            starsBox.setAlignment(Pos.CENTER);
            Label[] stars = new Label[5];
            final int[] selectedRating = {initialRating};

            for (int i = 0; i < 5; i++) {
                int ratingValue = i + 1;
                Label star = new Label(ratingValue <= initialRating ? "⭐" : "☆");
                star.setStyle("-fx-font-size: 32px; -fx-cursor: hand; -fx-text-fill: #F59E0B;");
                
                star.setOnMouseClicked(e -> {
                    selectedRating[0] = ratingValue;
                    for (int j = 0; j < 5; j++) {
                        stars[j].setText(j < ratingValue ? "⭐" : "☆");
                    }
                });
                
                stars[i] = star;
                starsBox.getChildren().add(star);
            }

            TextArea commentArea = new TextArea(initialComment != null ? initialComment : "");
            commentArea.setPromptText("Laissez un commentaire (optionnel)...");
            commentArea.setPrefRowCount(3);
            commentArea.setWrapText(true);
            commentArea.setStyle("-fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-background-radius: 8;");

            VBox content = new VBox(15, new Label("Votre note :"), starsBox, new Label("Votre commentaire :"), commentArea);
            content.setPadding(new Insets(20));
            content.setPrefWidth(350);
            dialog.getDialogPane().setContent(content);

            dialog.setResultConverter(btn -> {
                if (btn == submitType && selectedRating[0] > 0) {
                    CourseReview review = new CourseReview();
                    if (existing != null) review.setId(existing.getId());
                    review.setCourseId(courseId);
                    review.setUserId(parentId);
                    review.setRating(selectedRating[0]);
                    review.setComment(commentArea.getText().trim());
                    return review;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(review -> {
                try {
                    if (review.getId() > 0) {
                        reviewService.modifier(review);
                        new Alert(Alert.AlertType.INFORMATION, "✅ Avis mis à jour !", ButtonType.OK).showAndWait();
                    } else {
                        reviewService.ajouter(review);
                        new Alert(Alert.AlertType.INFORMATION, "✅ Merci pour votre avis !", ButtonType.OK).showAndWait();
                    }
                } catch (SQLException ex) {
                    new Alert(Alert.AlertType.ERROR, "Erreur lors de l'enregistrement : " + ex.getMessage(), ButtonType.OK).showAndWait();
                }
            });

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Erreur base de données : " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}
