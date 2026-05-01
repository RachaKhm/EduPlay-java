package dev.eduplay.controllers.admin;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.Course;
import dev.eduplay.entities.Seance;
import dev.eduplay.services.CourseService;
import dev.eduplay.services.GoogleCalendarService;
import dev.eduplay.services.SeanceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class AdminCalendarController {

    @FXML private Label pageSubtitle;
    @FXML private Button syncGoogleButton;
    @FXML private Button prevMonthButton;
    @FXML private Button nextMonthButton;
    @FXML private Button clearDayFilterButton;
    @FXML private Label calendarMonthLabel;
    @FXML private Label selectedDaySummary;
    @FXML private GridPane calendarGrid;
    @FXML private VBox selectedDaySeancesBox;

    private final ObservableList<Seance> allSeances = FXCollections.observableArrayList();
    private final Map<Integer, String> courseTitleById = new HashMap<>();

    private YearMonth displayedMonth = YearMonth.now();
    private LocalDate selectedCalendarDate;

    private SeanceService seanceService;
    private CourseService courseService;

    @FXML
    public void initialize() {
        if (!AppContext.isAdmin()) {
            if (pageSubtitle != null) pageSubtitle.setText("Accès réservé aux administrateurs.");
            return;
        }
        if (pageSubtitle != null) {
            pageSubtitle.setText("Visualise les séances dans un calendrier puis synchronise vers Google Calendar.");
        }

        try {
            seanceService = new SeanceService();
            courseService = new CourseService();
        } catch (SQLException e) {
            showError("Base de données", "Impossible d'initialiser les services", e.getMessage());
            return;
        }

        if (syncGoogleButton != null) syncGoogleButton.setOnAction(e -> syncWithGoogleCalendar());
        if (prevMonthButton != null) {
            prevMonthButton.setOnAction(e -> {
                displayedMonth = displayedMonth.minusMonths(1);
                renderCalendar();
            });
        }
        if (nextMonthButton != null) {
            nextMonthButton.setOnAction(e -> {
                displayedMonth = displayedMonth.plusMonths(1);
                renderCalendar();
            });
        }
        if (clearDayFilterButton != null) {
            clearDayFilterButton.setOnAction(e -> {
                selectedCalendarDate = null;
                renderCalendar();
            });
        }

        reload();
    }

    private void reload() {
        try {
            courseTitleById.clear();
            for (Course c : courseService.afficherTous()) {
                courseTitleById.put(c.getId(), c.getTitle() != null ? c.getTitle() : "");
            }
            allSeances.setAll(seanceService.afficherTous());
            renderCalendar();
        } catch (SQLException e) {
            showError("Base de données", "Impossible de charger les séances", e.getMessage());
        }
    }

    private void syncWithGoogleCalendar() {
        File credentialsPath = new File("config/google-calendar-credentials.json");
        GoogleCalendarService calendarService = new GoogleCalendarService(credentialsPath);
        try {
            GoogleCalendarService.SyncResult result = calendarService.syncSeances(new ArrayList<>(allSeances), courseTitleById);
            Alert info = new Alert(
                    Alert.AlertType.INFORMATION,
                    "Synchronisation terminée.\n\n"
                            + "Créés: " + result.createdCount() + "\n"
                            + "Mises à jour: " + result.updatedCount() + "\n"
                            + "Ignorés (incomplets): " + result.skippedCount(),
                    ButtonType.OK
            );
            info.setTitle("Google Calendar");
            info.setHeaderText("Les séances ont été synchronisées avec votre calendrier Google.");
            info.showAndWait();
        } catch (Exception e) {
            String hint = """
                    Vérifie les points suivants :
                    - Le fichier existe : config/google-calendar-credentials.json
                    - Dans Google Cloud, ajoute ce redirect URI : http://localhost:8888/Callback
                    - La Google Calendar API est activée sur ton projet
                    """;
            showError("Google Calendar", "Synchronisation impossible", e.getMessage() + "\n\n" + hint);
        }
    }

    private void renderCalendar() {
        if (calendarGrid == null) return;

        if (calendarMonthLabel != null) {
            DateTimeFormatter headerFormat = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
            calendarMonthLabel.setText(capitalize(displayedMonth.atDay(1).format(headerFormat)));
        }

        calendarGrid.getChildren().clear();
        Map<LocalDate, List<Seance>> seancesByDate = buildSeancesByDate();

        List<String> weekDays = List.of("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim");
        for (int col = 0; col < weekDays.size(); col++) {
            Label day = new Label(weekDays.get(col));
            day.setMaxWidth(Double.MAX_VALUE);
            day.setAlignment(Pos.CENTER);
            day.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #8A8FB4;");
            calendarGrid.add(day, col, 0);
        }

        LocalDate firstDay = displayedMonth.atDay(1);
        int firstCol = toMondayFirstColumn(firstDay.getDayOfWeek());
        int daysInMonth = displayedMonth.lengthOfMonth();
        int row = 1;
        int col = firstCol;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = displayedMonth.atDay(day);
            int count = seancesByDate.getOrDefault(date, List.of()).size();
            VBox dayCell = buildCalendarCell(date, count);
            calendarGrid.add(dayCell, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        renderSelectedDayPanel(seancesByDate);
    }

    private VBox buildCalendarCell(LocalDate date, int seanceCount) {
        Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
        dayNumber.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label marker = new Label(seanceCount > 0 ? seanceCount + " séance" + (seanceCount > 1 ? "s" : "") : "");
        marker.setStyle("-fx-font-size: 10px;");

        VBox box = new VBox(4, dayNumber, marker);
        box.setPadding(new Insets(8));
        box.setMinHeight(56);
        box.setPrefHeight(56);
        box.setMaxWidth(Double.MAX_VALUE);
        box.setStyle(dayCellStyle(date, seanceCount));
        box.setOnMouseClicked(e -> {
            selectedCalendarDate = date;
            renderCalendar();
        });
        return box;
    }

    private String dayCellStyle(LocalDate date, int seanceCount) {
        boolean selected = Objects.equals(date, selectedCalendarDate);
        boolean today = Objects.equals(date, LocalDate.now());
        String bg = selected ? "#EAF3FF" : seanceCount > 0 ? "#F4F8FF" : "#FAFAFD";
        String border = selected ? "#4A90D9" : today ? "#9E7CF4" : "#E6E8F2";
        return "-fx-background-color: " + bg + ";"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-width: " + (selected ? "2" : "1") + ";"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
                + "-fx-cursor: hand;";
    }

    private void renderSelectedDayPanel(Map<LocalDate, List<Seance>> seancesByDate) {
        if (selectedDaySummary != null) {
            if (selectedCalendarDate == null) {
                selectedDaySummary.setText("Clique une date pour voir les séances du jour.");
            } else {
                int count = seancesByDate.getOrDefault(selectedCalendarDate, List.of()).size();
                DateTimeFormatter f = DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", Locale.FRENCH);
                selectedDaySummary.setText(
                        capitalize(selectedCalendarDate.format(f)) + " : " + count + " séance" + (count > 1 ? "s" : "")
                );
            }
        }

        if (selectedDaySeancesBox == null) return;
        selectedDaySeancesBox.getChildren().clear();

        if (selectedCalendarDate == null) {
            Label placeholder = new Label("Aucune date sélectionnée.");
            placeholder.setStyle("-fx-text-fill:#777799;");
            selectedDaySeancesBox.getChildren().add(placeholder);
            return;
        }

        List<Seance> seances = new ArrayList<>(seancesByDate.getOrDefault(selectedCalendarDate, List.of()));
        seances.sort((a, b) -> {
            if (a.getStartTime() == null && b.getStartTime() == null) return 0;
            if (a.getStartTime() == null) return 1;
            if (b.getStartTime() == null) return -1;
            return a.getStartTime().compareTo(b.getStartTime());
        });

        if (seances.isEmpty()) {
            Label empty = new Label("Aucune séance ce jour.");
            empty.setStyle("-fx-text-fill:#777799;");
            selectedDaySeancesBox.getChildren().add(empty);
            return;
        }

        for (Seance seance : seances) {
            selectedDaySeancesBox.getChildren().add(buildSeanceRow(seance));
        }
    }

    private Region buildSeanceRow(Seance seance) {
        String courseTitle = courseTitleById.getOrDefault(seance.getCourseId(), "Cours #" + seance.getCourseId());
        String title = (seance.getTitle() == null || seance.getTitle().isBlank()) ? "Sans titre" : seance.getTitle();

        Label titleLbl = new Label(title + " - " + courseTitle);
        titleLbl.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:#2F3352;");

        String hourText = "Heure non précisée";
        if (seance.getStartTime() != null && seance.getEndTime() != null) {
            hourText = seance.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    + " -> "
                    + seance.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        Label metaLbl = new Label(hourText + " | " + safe(seance.getLocation(), "Sans lieu"));
        metaLbl.setStyle("-fx-font-size:11px;-fx-text-fill:#6F7393;");

        VBox row = new VBox(3, titleLbl, metaLbl);
        row.setPadding(new Insets(8, 10, 8, 10));
        row.setStyle("-fx-background-color:#F8F9FF;-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:#E5E8F5;");
        return row;
    }

    private Map<LocalDate, List<Seance>> buildSeancesByDate() {
        Map<LocalDate, List<Seance>> map = new HashMap<>();
        for (Seance seance : allSeances) {
            LocalDate d = resolveDate(seance);
            if (d != null) {
                map.computeIfAbsent(d, k -> new ArrayList<>()).add(seance);
            }
        }
        return map;
    }

    private static int toMondayFirstColumn(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> 0;
            case TUESDAY -> 1;
            case WEDNESDAY -> 2;
            case THURSDAY -> 3;
            case FRIDAY -> 4;
            case SATURDAY -> 5;
            case SUNDAY -> 6;
        };
    }

    private static LocalDate resolveDate(Seance seance) {
        if (seance == null) return null;
        if (seance.getDate() != null) return seance.getDate();
        if (seance.getStartTime() != null) return seance.getStartTime().toLocalDate();
        return null;
    }

    private static String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
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
