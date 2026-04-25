package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AddEventController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Integer> startHourCombo;
    @FXML private ComboBox<Integer> startMinuteCombo;
    @FXML private ComboBox<Integer> endHourCombo;
    @FXML private ComboBox<Integer> endMinuteCombo;
    @FXML private TextField locationField;
    @FXML private Button browseImageBtn;
    @FXML private Button submitBtn;
    @FXML private Button cancelBtn;
    @FXML private Label messageLabel;
    @FXML private Label fileNameLabel;
    @FXML private StackPane imagePreviewPane;
    @FXML private TextField capacityField;

    private SchoolEventService service;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "uploads/events/";
    private Integer eventIdToModify = null;
    private boolean isModification = false;

    @FXML
    public void initialize() {
        System.out.println("AddEventController initialisé");
        service = new SchoolEventService();
        initHourMinuteCombos();
        setupDateValidation();
        setupActions();
        setupLiveValidation();

        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        startHourCombo.setValue(9);
        startMinuteCombo.setValue(0);
        endHourCombo.setValue(17);
        endMinuteCombo.setValue(0);
        capacityField.setText("50");

        isModification = false;
        submitBtn.setText("Créer l'événement");
    }

    public void setEventToModify(int eventId) {
        this.eventIdToModify = eventId;
        this.isModification = true;
        submitBtn.setText("Modifier l'événement");

        System.out.println("Mode MODIFICATION - ID: " + eventId);

        try {
            SchoolEvent event = service.recupererParId(eventId);
            if (event != null) {
                titleField.setText(event.getTitle());
                descriptionArea.setText(event.getDescription());
                locationField.setText(event.getLocation());
                capacityField.setText(String.valueOf(event.getMaxCapacity()));

                if (event.getStartDate() != null) {
                    startDatePicker.setValue(event.getStartDate().toLocalDate());
                    startHourCombo.setValue(event.getStartDate().getHour());
                    startMinuteCombo.setValue(event.getStartDate().getMinute());
                }

                if (event.getEndDate() != null) {
                    endDatePicker.setValue(event.getEndDate().toLocalDate());
                    endHourCombo.setValue(event.getEndDate().getHour());
                    endMinuteCombo.setValue(event.getEndDate().getMinute());
                }

                if (event.getImagePath() != null) {
                    fileNameLabel.setText(event.getImagePath());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initHourMinuteCombos() {
        for (int i = 0; i < 24; i++) {
            startHourCombo.getItems().add(i);
            endHourCombo.getItems().add(i);
        }
        for (int i = 0; i < 60; i++) {
            startMinuteCombo.getItems().add(i);
            endMinuteCombo.getItems().add(i);
        }
    }

    private void setupDateValidation() {
        startDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        endDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcccc;");
                }
            }
        });

        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && endDatePicker.getValue() != null && endDatePicker.getValue().isBefore(newVal)) {
                endDatePicker.setValue(newVal);
            }
        });
    }

    private void setupActions() {
        submitBtn.setOnAction(e -> ajouterEvent());
        cancelBtn.setOnAction(e -> fermerFormulaire());
        browseImageBtn.setOnAction(e -> parcourirImage());
    }

    private void setupLiveValidation() {
        titleField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !isValidTitle(newVal)) {
                titleField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                titleField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !isValidDescription(newVal)) {
                descriptionArea.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                descriptionArea.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });

        locationField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !isValidLocation(newVal)) {
                locationField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                locationField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });

        capacityField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                capacityField.setText(old);
            }
        });
    }

    private boolean containsLetter(String text) {
        return text.matches(".*[a-zA-ZàâäéèêëîïôöùûüçÀÂÄÉÈÊËÎÏÔÖÙÛÜÇ].*");
    }

    private boolean isValidTitle(String title) {
        if (title == null) return false;
        title = title.trim();
        return title.length() >= 3 && containsLetter(title);
    }

    private boolean isValidDescription(String description) {
        if (description == null) return false;
        return description.trim().length() >= 10;
    }

    private boolean isValidLocation(String location) {
        if (location == null) return false;
        location = location.trim();
        return location.length() >= 3 && containsLetter(location);
    }

    private void parcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");

        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        Stage stage = (Stage) browseImageBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            if (fileNameLabel != null) {
                fileNameLabel.setText(file.getName());
            }
            afficherApercuImage(file);
        }
    }

    private void afficherApercuImage(File imageFile) {
        try {
            Image image = new Image(imageFile.toURI().toString(), 200, 120, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);
            imageView.setFitHeight(120);

            imagePreviewPane.getChildren().clear();
            imagePreviewPane.getChildren().add(imageView);
            imagePreviewPane.setVisible(true);
            imagePreviewPane.setManaged(true);
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }
    }

    private String copyImageToUploads() {
        if (selectedImageFile == null) return null;

        try {
            File uploadDir = new File("uploads/events/");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String originalName = selectedImageFile.getName();
            File destFile = new File(uploadDir, originalName);

            Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Image copiée: " + destFile.getAbsolutePath());
            return originalName;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private LocalDateTime getDateTimeFromPicker(DatePicker datePicker, ComboBox<Integer> hourCombo, ComboBox<Integer> minuteCombo) {
        if (datePicker.getValue() == null || hourCombo.getValue() == null || minuteCombo.getValue() == null) {
            return null;
        }
        return LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourCombo.getValue(), minuteCombo.getValue()));
    }

    private void ajouterEvent() {
        submitBtn.setDisable(true);
        submitBtn.setText(isModification ? "Modification..." : "Enregistrement...");

        try {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            String location = locationField.getText();

            if (!isValidTitle(title)) {
                showError("Le titre doit contenir au moins 3 caractères (dont des lettres)");
                titleField.requestFocus();
                resetButton();
                return;
            }

            if (!isValidDescription(description)) {
                showError("La description doit contenir au moins 10 caractères");
                descriptionArea.requestFocus();
                resetButton();
                return;
            }

            if (!isValidLocation(location)) {
                showError("Le lieu doit contenir au moins 3 caractères (dont des lettres)");
                locationField.requestFocus();
                resetButton();
                return;
            }

            // ✅ Validation de la capacité
            int maxCapacity = 50;
            try {
                maxCapacity = Integer.parseInt(capacityField.getText().trim());
                if (maxCapacity < 1) {
                    showError("La capacité doit être au moins 1");
                    capacityField.requestFocus();
                    resetButton();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Veuillez saisir un nombre valide pour la capacité");
                capacityField.requestFocus();
                resetButton();
                return;
            }

            if (startDatePicker.getValue() == null) {
                showError("La date de début est obligatoire");
                resetButton();
                return;
            }

            LocalDateTime startDateTime = getDateTimeFromPicker(startDatePicker, startHourCombo, startMinuteCombo);
            if (startDateTime != null && startDateTime.isBefore(LocalDateTime.now())) {
                showError("La date de début ne peut pas être dans le passé");
                resetButton();
                return;
            }

            if (endDatePicker.getValue() == null) {
                showError("La date de fin est obligatoire");
                resetButton();
                return;
            }

            LocalDateTime endDateTime = getDateTimeFromPicker(endDatePicker, endHourCombo, endMinuteCombo);

            if (startDateTime == null || endDateTime == null) {
                showError("Veuillez sélectionner des dates et heures valides");
                resetButton();
                return;
            }

            if (endDateTime.isBefore(startDateTime)) {
                showError("La date de fin doit être après la date de début");
                resetButton();
                return;
            }

            String imagePath = copyImageToUploads();

            if (isModification && eventIdToModify != null) {
                SchoolEvent event = service.recupererParId(eventIdToModify);
                if (event != null) {
                    event.setTitle(title.trim());
                    event.setDescription(description.trim());
                    event.setStartDate(startDateTime);
                    event.setEndDate(endDateTime);
                    event.setLocation(location.trim());
                    if (imagePath != null) event.setImagePath(imagePath);
                    event.setMaxCapacity(maxCapacity);
                    service.modifier(event);
                    showSuccess("✅ Événement modifié avec succès !");
                } else {
                    showError("Événement non trouvé");
                    resetButton();
                    return;
                }
            } else {
                SchoolEvent event = new SchoolEvent();
                event.setTitle(title.trim());
                event.setDescription(description.trim());
                event.setStartDate(startDateTime);
                event.setEndDate(endDateTime);
                event.setLocation(location.trim());
                event.setImagePath(imagePath);
                event.setMaxCapacity(maxCapacity);
                event.setCurrentRegistrations(0);
                service.ajouter(event);
                System.out.println("✅ Événement créé avec capacité: " + maxCapacity);
                showSuccess("✅ Événement créé avec succès !");
            }

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        Router.go("event_list");
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showError("Erreur base de données: " + e.getMessage());
            e.printStackTrace();
            resetButton();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
            e.printStackTrace();
            resetButton();
        }
    }

    private void resetButton() {
        submitBtn.setDisable(false);
        submitBtn.setText(isModification ? "Modifier" : "Créer l'événement");
    }

    private void fermerFormulaire() {
        Router.go("event_list");
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}