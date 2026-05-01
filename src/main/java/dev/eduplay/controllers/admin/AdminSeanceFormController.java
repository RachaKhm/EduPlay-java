package dev.eduplay.controllers.admin;

import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.services.SeanceService;
import dev.eduplay.validation.FormInputChecks;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class AdminSeanceFormController {

    @FXML private Label formTitle;
    @FXML private ComboBox<Course> courseField;
    @FXML private TextField titleField;
    @FXML private DatePicker datePicker;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private TextField locationField;
    @FXML private ComboBox<String> statusField;
    @FXML private TextArea descriptionField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private SeanceService seanceService;
    private Seance editing;
    private Consumer<Seance> onSaved;

    public void init(SeanceService seanceService, List<Course> courses, Seance existing, Consumer<Seance> onSaved) {
        this.seanceService = Objects.requireNonNull(seanceService);
        this.editing = existing;
        this.onSaved = onSaved;

        setupCourseCombo(courses);
        initStatus();
        fillForm(existing);
    }

    private void setupCourseCombo(List<Course> courses) {
        if (courseField == null) return;
        courseField.setItems(FXCollections.observableArrayList(courses));
        courseField.setConverter(new StringConverter<>() {
            @Override
            public String toString(Course c) {
                if (c == null) return "";
                return c.getId() + " — " + (c.getTitle() != null ? c.getTitle() : "(sans titre)");
            }

            @Override
            public Course fromString(String s) {
                return null;
            }
        });
    }

    private void initStatus() {
        if (statusField != null) {
            statusField.getItems().setAll("scheduled", "cancelled", "completed");
            if (statusField.getValue() == null) statusField.getSelectionModel().selectFirst();
        }
    }

    @FXML
    public void initialize() {
        if (cancelButton != null) cancelButton.setOnAction(e -> close());
        if (saveButton != null) saveButton.setOnAction(e -> save());
    }

    private void fillForm(Seance s) {
        boolean isEdit = s != null && s.getId() > 0;
        if (formTitle != null) formTitle.setText(isEdit ? "Modifier la séance" : "Nouvelle séance");
        if (saveButton != null) saveButton.setText(isEdit ? "Enregistrer" : "Créer");

        if (courseField != null && !courseField.getItems().isEmpty()) {
            courseField.getSelectionModel().selectFirst();
        }

        if (s == null) {
            if (datePicker != null) datePicker.setValue(LocalDate.now());
            if (startTimeField != null) startTimeField.setText("09:00");
            if (endTimeField != null) endTimeField.setText("10:30");
            return;
        }

        if (titleField != null) titleField.setText(nvl(s.getTitle()));
        if (locationField != null) locationField.setText(nvl(s.getLocation()));
        if (descriptionField != null) descriptionField.setText(nvl(s.getDescription()));
        if (statusField != null && s.getStatus() != null) statusField.setValue(s.getStatus());

        LocalDate date = s.getDate();
        if (date == null && s.getStartTime() != null) {
            date = s.getStartTime().toLocalDate();
        }
        if (datePicker != null) datePicker.setValue(date != null ? date : LocalDate.now());

        if (startTimeField != null) {
            startTimeField.setText(s.getStartTime() != null
                    ? s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "09:00");
        }
        if (endTimeField != null) {
            endTimeField.setText(s.getEndTime() != null
                    ? s.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    : "10:30");
        }

        if (courseField != null) {
            for (Course c : courseField.getItems()) {
                if (c.getId() == s.getCourseId()) {
                    courseField.getSelectionModel().select(c);
                    break;
                }
            }
        }
    }

    private void save() {
        Course course = courseField != null ? courseField.getValue() : null;
        if (course == null) {
            warn("Contrôle de saisie", "Sélectionnez un cours.", "");
            return;
        }

        String title = nvl(titleField != null ? titleField.getText() : "").trim();
        String startRaw = nvl(startTimeField != null ? startTimeField.getText() : "");
        String endRaw = nvl(endTimeField != null ? endTimeField.getText() : "");
        String location = nvl(locationField != null ? locationField.getText() : "").trim();
        String desc = nvl(descriptionField != null ? descriptionField.getText() : "").trim();
        String status = statusField != null ? statusField.getValue() : "scheduled";

        String err = FormInputChecks.checkSeanceTitle(title);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkSeanceLocationOptional(location);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkSeanceDescriptionOptional(desc);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkSeanceStatus(status);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkSeanceTimeFormat("début", startRaw);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }
        err = FormInputChecks.checkSeanceTimeFormat("fin", endRaw);
        if (err != null) {
            warn("Contrôle de saisie", err, "");
            return;
        }

        LocalDate date = datePicker != null ? datePicker.getValue() : null;
        if (date == null) {
            warn("Contrôle de saisie", "Choisis une date pour la séance.", "");
            return;
        }

        LocalTime startT;
        LocalTime endT;
        try {
            startT = parseFlexibleTime(startRaw);
            endT = parseFlexibleTime(endRaw);
        } catch (DateTimeParseException ex) {
            warn("Contrôle de saisie", "Les heures ne sont pas valides (format HH:mm ou H:mm).", "");
            return;
        }

        LocalDateTime startDt = LocalDateTime.of(date, startT);
        LocalDateTime endDt = LocalDateTime.of(date, endT);
        if (!endDt.isAfter(startDt)) {
            warn("Contrôle de saisie", "L'heure de fin doit être après l'heure de début.", "");
            return;
        }

        Seance s = (editing != null) ? editing : new Seance();
        s.setCourseId(course.getId());
        s.setTitle(title);
        s.setDate(date);
        s.setStartTime(startDt);
        s.setEndTime(endDt);
        s.setLocation(location.isBlank() ? null : location);
        s.setDescription(desc.isBlank() ? null : desc);
        s.setStatus(status);

        try {
            if (editing == null || editing.getId() <= 0) {
                int id = seanceService.ajouter(s);
                s.setId(id);
            } else {
                seanceService.modifier(s);
            }
            if (onSaved != null) onSaved.accept(s);
            close();
        } catch (SQLException e) {
            warn("Base de données", "Enregistrement impossible", e.getMessage());
        }
    }

    private static LocalTime parseFlexibleTime(String text) {
        String t = text.trim();
        if (t.isBlank()) throw new DateTimeParseException("empty", t, 0);
        try {
            return LocalTime.parse(t, DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException ignored) {
            return LocalTime.parse(t, DateTimeFormatter.ofPattern("H:mm"));
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
