package dev.eduplay.controllers.child;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.SeanceService;
import dev.eduplay.services.SubscriptionService;
import dev.eduplay.services.UserService;
import dev.eduplay.entities.User;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ChildCourseDetailController {

    @FXML private Button backButton;
    @FXML private Label titleLabel;
    @FXML private Label metaLabel;
    @FXML private Label descriptionLabel;
    @FXML private Button downloadPdfButton;
    @FXML private Button openPdfButton;
    @FXML private Label pdfHint;
    @FXML private FlowPane seancesPane;

    private CourseService courseService;
    private SeanceService seanceService;
    private SubscriptionService subscriptionService;
    private UserService userService;

    private int kidId;
    private int courseId;
    private Course course;

    @FXML
    public void initialize() {
        if (backButton != null) {
            backButton.setOnAction(e -> {
                AppContext.clearChildBrowsingCourseId();
                Router.reload("child_courses");
            });
        }
        if (downloadPdfButton != null) {
            downloadPdfButton.setOnAction(e -> downloadPdf());
        }
        if (openPdfButton != null) {
            openPdfButton.setOnAction(e -> openPdf());
        }

        if (!AppContext.isChild() || AppContext.getCurrentUser() == null) {
            if (titleLabel != null) titleLabel.setText("Accès refusé");
            return;
        }
        kidId = AppContext.getCurrentUser().getId();

        Integer browsing = AppContext.getChildBrowsingCourseId();
        if (browsing == null || browsing <= 0) {
            if (titleLabel != null) titleLabel.setText("Aucun cours sélectionné");
            return;
        }
        courseId = browsing;

        try {
            courseService = new CourseService();
            seanceService = new SeanceService();
            subscriptionService = new SubscriptionService();
            userService = new UserService();
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
            return;
        }

        load();
    }

    private void load() {
        if (seancesPane != null) seancesPane.getChildren().clear();

        try {
            if (!subscriptionService.estAbonneActif(kidId, courseId)) {
                if (titleLabel != null) titleLabel.setText("Tu n’es pas inscrit(e) à ce cours.");
                if (metaLabel != null) metaLabel.setText("Retourne à « Mes cours ».");
                setPdfButtonsDisabled(true);
                return;
            }

            var opt = courseService.trouverParId(courseId);
            if (opt.isEmpty()) {
                if (titleLabel != null) titleLabel.setText("Cours introuvable");
                return;
            }
            course = opt.get();
            if ("archived".equalsIgnoreCase(safe(course.getStatus(), ""))) {
                if (titleLabel != null) titleLabel.setText("Ce cours n’est plus disponible (archivé).");
                setPdfButtonsDisabled(true);
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

            File pdf = resolvePdfFile(course.getPdfFile());
            boolean hasPdf = pdf != null && pdf.isFile();
            setPdfButtonsDisabled(!hasPdf);
            if (pdfHint != null) {
                pdfHint.setText(hasPdf
                        ? "Fichier : " + pdf.getName()
                        : (course.getPdfFile() == null || course.getPdfFile().isBlank()
                        ? "Aucun document PDF pour ce cours."
                        : "Le fichier PDF est introuvable sur cet ordinateur : " + course.getPdfFile()));
            }

            for (Seance s : seanceService.parCourseId(courseId)) {
                if (seancesPane != null) {
                    seancesPane.getChildren().add(makeSeanceCard(s));
                }
            }
            if (seancesPane != null && seancesPane.getChildren().isEmpty()) {
                Label empty = new Label("Aucune séance planifiée pour le moment.");
                empty.setStyle("-fx-text-fill: #9999BB;");
                seancesPane.getChildren().add(empty);
            }
        } catch (SQLException e) {
            alert(Alert.AlertType.ERROR, e.getMessage());
        }
    }

    private void setPdfButtonsDisabled(boolean disabled) {
        if (downloadPdfButton != null) downloadPdfButton.setDisable(disabled);
        if (openPdfButton != null) openPdfButton.setDisable(disabled);
    }

    private static File resolvePdfFile(String path) {
        if (path == null || path.isBlank()) return null;
        File f = new File(path.trim());
        return f;
    }

    private void downloadPdf() {
        if (course == null) return;
        File src = resolvePdfFile(course.getPdfFile());
        if (src == null || !src.isFile()) {
            alert(Alert.AlertType.WARNING, "Fichier PDF introuvable.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le PDF");
        chooser.setInitialFileName(src.getName().toLowerCase(Locale.ROOT).endsWith(".pdf") ? src.getName() : src.getName() + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        Stage stage = (Stage) downloadPdfButton.getScene().getWindow();
        File dest = chooser.showSaveDialog(stage);
        if (dest == null) return;
        try {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            new Alert(Alert.AlertType.INFORMATION, "Fichier enregistré :\n" + dest.getAbsolutePath(), ButtonType.OK).showAndWait();
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Impossible d’enregistrer le fichier : " + e.getMessage());
        }
    }

    private void openPdf() {
        if (course == null) return;
        File src = resolvePdfFile(course.getPdfFile());
        if (src == null || !src.isFile()) {
            alert(Alert.AlertType.WARNING, "Fichier PDF introuvable.");
            return;
        }
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            alert(Alert.AlertType.INFORMATION, "Ouverture automatique non disponible. Utilise « Télécharger le PDF ».");
            return;
        }
        try {
            Desktop.getDesktop().open(src);
        } catch (IOException e) {
            alert(Alert.AlertType.ERROR, "Impossible d’ouvrir le fichier : " + e.getMessage());
        }
    }

    private VBox makeSeanceCard(Seance s) {
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

        Label w = new Label(formatSeanceWhen(s));
        w.setStyle("-fx-font-size: 11px; -fx-text-fill: #777799;");

        Label loc = new Label("Lieu : " + safe(s.getLocation(), "—"));
        loc.setWrapText(true);
        loc.setStyle("-fx-font-size: 11px; -fx-text-fill: #9999BB;");

        card.getChildren().addAll(t, w, loc);
        return card;
    }

    private static String formatSeanceWhen(Seance s) {
        DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter d = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        if (s.getStartTime() != null && s.getEndTime() != null) {
            return s.getStartTime().format(dt) + " → " + s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        if (s.getDate() != null) {
            return s.getDate().format(d);
        }
        return "—";
    }

    private static String safe(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private void alert(Alert.AlertType type, String msg) {
        new Alert(type, msg, ButtonType.OK).showAndWait();
    }
}
