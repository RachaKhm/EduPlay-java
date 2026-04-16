package dev.eduplay.controllers.admin;

import dev.eduplay.entities.Course;
import dev.eduplay.entities.User;
import dev.eduplay.services.CourseService;
import dev.eduplay.validation.FormInputChecks;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AdminCourseFormController {

    @FXML private Label formTitle;
    @FXML private ComboBox<User> teacherField;
    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private TextField durationField;
    @FXML private ComboBox<String> levelField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextField pdfField;
    @FXML private Button browsePdfButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private CourseService courseService;
    private Course editing;
    private Consumer<Course> onSaved;

    public void init(CourseService courseService, List<User> teachers, Course existing, Consumer<Course> onSaved) {
        this.courseService = Objects.requireNonNull(courseService);
        this.editing = existing;
        this.onSaved = onSaved;

        setupTeacherCombo(teachers);
        initCombos();
        fillForm(existing);
    }

    private void setupTeacherCombo(List<User> teachers) {
        if (teacherField == null) return;
        teacherField.setItems(FXCollections.observableArrayList(teachers));
        teacherField.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) {
                return u == null ? "" : u.getFullName();
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });
    }

    @FXML
    public void initialize() {
        if (cancelButton != null) cancelButton.setOnAction(e -> close());
        if (saveButton != null) saveButton.setOnAction(e -> save());
        if (browsePdfButton != null) browsePdfButton.setOnAction(e -> browsePdf());
    }

    private void initCombos() {
        if (levelField != null) {
            levelField.getItems().setAll("Débutant", "Intermédiaire", "Avancé");
            if (levelField.getValue() == null) levelField.getSelectionModel().selectFirst();
        }
        if (statusField != null) {
            statusField.getItems().setAll("draft", "published", "archived");
            if (statusField.getValue() == null) statusField.getSelectionModel().selectFirst();
        }
    }

    private void browsePdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir un fichier PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"));
        Stage owner = (Stage) (browsePdfButton != null ? browsePdfButton.getScene().getWindow() : null);
        File f = chooser.showOpenDialog(owner);
        if (f != null && pdfField != null) {
            pdfField.setText(f.getAbsolutePath());
        }
    }

    private void fillForm(Course c) {
        boolean isEdit = c != null && c.getId() > 0;
        if (formTitle != null) formTitle.setText(isEdit ? "Modifier le cours" : "Nouveau cours");
        if (saveButton != null) saveButton.setText(isEdit ? "Enregistrer" : "Créer");

        if (teacherField != null && !teacherField.getItems().isEmpty()) {
            teacherField.getSelectionModel().selectFirst();
        }

        if (c == null) return;

        if (titleField != null) titleField.setText(nvl(c.getTitle()));
        if (descriptionField != null) descriptionField.setText(nvl(c.getDescription()));
        if (durationField != null) {
            durationField.setText(c.getDurationTraining() != null ? String.valueOf(c.getDurationTraining()) : "");
        }
        if (levelField != null && c.getLevel() != null) levelField.setValue(c.getLevel());
        if (statusField != null && c.getStatus() != null) statusField.setValue(c.getStatus());
        if (pdfField != null) pdfField.setText(nvl(c.getPdfFile()));

        if (teacherField != null) {
            for (User u : teacherField.getItems()) {
                if (u.getId() == c.getTeacherId()) {
                    teacherField.getSelectionModel().select(u);
                    break;
                }
            }
        }
    }

    private void save() {
        User teacher = teacherField != null ? teacherField.getValue() : null;
        String title = nvl(titleField != null ? titleField.getText() : "").trim();
        String rawDur = nvl(durationField != null ? durationField.getText() : "").trim();
        String level = levelField != null ? levelField.getValue() : null;
        String status = statusField != null ? statusField.getValue() : "draft";
        String desc = nvl(descriptionField != null ? descriptionField.getText() : "").trim();
        String pdf = nvl(pdfField != null ? pdfField.getText() : "").trim();

        String err = FormInputChecks.checkCourseTeacher(teacher);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkCourseTitle(title);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkCourseLevel(level);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkCourseDurationOptional(rawDur);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkCourseDescriptionOptional(desc);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkCoursePdfPathOptional(pdf);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkCourseStatus(status);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }

        Integer duration = null;
        if (!rawDur.isBlank()) {
            duration = Integer.parseInt(rawDur.trim());
        }

        Course c = (editing != null) ? editing : new Course();
        c.setTitle(title);
        c.setDurationTraining(duration);
        c.setLevel(level);
        c.setStatus(status);
        c.setDescription(desc.isBlank() ? null : desc);
        c.setPdfFile(pdf.isBlank() ? null : pdf);
        c.setTeacherId(teacher.getId());
        if (c.getCreatedAt() == null) c.setCreatedAt(LocalDateTime.now());

        try {
            if (editing == null || editing.getId() <= 0) {
                int id = courseService.ajouter(c);
                c.setId(id);
            } else {
                courseService.modifier(c);
            }
            if (onSaved != null) onSaved.accept(c);
            close();
        } catch (SQLException e) {
            warn("Base de données", "Enregistrement impossible", e.getMessage());
        }
    }

    private void close() {
        Stage stage = (Stage) (cancelButton != null ? cancelButton.getScene().getWindow()
                : saveButton != null ? saveButton.getScene().getWindow() : null);
        if (stage != null) stage.close();
    }

    private void warn(String title, String header, String content) {
        Alert a = new Alert(Alert.AlertType.WARNING, content, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(header);
        a.showAndWait();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}
