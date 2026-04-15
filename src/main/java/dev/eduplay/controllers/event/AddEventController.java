package dev.eduplay.controllers.event;

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

    // ==================== FXML COMPOSANTS ====================
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

    // ==================== ATTRIBUTS ====================
    private SchoolEventService service;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "uploads/events/";
    private MainController mainController;
    private Integer eventIdToModify = null;

    // ==================== INITIALISATION ====================
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
    }

    public void setMainController(MainController mainController) {
        System.out.println("MainController reçu par AddEventController");
        this.mainController = mainController;
    }

    public void setEventToModify(int eventId) {
        System.out.println("setEventToModify appelé avec ID: " + eventId);
        this.eventIdToModify = eventId;
        submitBtn.setText("Modifier");

        try {
            SchoolEvent event = service.recupererParId(eventId);
            if (event != null) {
                System.out.println("Événement trouvé: " + event.getTitle());
                titleField.setText(event.getTitle());
                descriptionArea.setText(event.getDescription());
                locationField.setText(event.getLocation());

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
            } else {
                System.out.println("Événement non trouvé pour l'ID: " + eventId);
                showError("Événement non trouvé");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement de l'événement");
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
            // Créer le dossier s'il n'existe pas
            File uploadDir = new File("uploads/events/");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Garder le nom original du fichier
            String originalName = selectedImageFile.getName();
            File destFile = new File(uploadDir, originalName);

            // Copier le fichier
            java.nio.file.Files.copy(selectedImageFile.toPath(), destFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.out.println("✅ Image copiée: " + destFile.getAbsolutePath());

            // Retourner juste le nom du fichier (pas le chemin complet)
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
        submitBtn.setText("Enregistrement...");

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

            if (eventIdToModify != null) {
                // MODIFICATION
                System.out.println("Modification de l'événement ID: " + eventIdToModify);
                SchoolEvent event = service.recupererParId(eventIdToModify);
                if (event != null) {
                    event.setTitle(title.trim());
                    event.setDescription(description.trim());
                    event.setStartDate(startDateTime);
                    event.setEndDate(endDateTime);
                    event.setLocation(location.trim());
                    if (imagePath != null) event.setImagePath(imagePath);
                    service.modifier(event);
                    System.out.println("Événement modifié avec succès");
                    showSuccess("✅ Événement modifié avec succès !");
                } else {
                    showError("Événement non trouvé");
                    resetButton();
                    return;
                }
            } else {
                // AJOUT
                System.out.println("Ajout d'un nouvel événement");
                SchoolEvent event = new SchoolEvent();
                event.setTitle(title.trim());
                event.setDescription(description.trim());
                event.setStartDate(startDateTime);
                event.setEndDate(endDateTime);
                event.setLocation(location.trim());
                event.setImagePath(imagePath);
                service.ajouter(event);
                System.out.println("Événement ajouté avec succès");
                showSuccess("✅ Événement créé avec succès !");
            }

            // Retour à la liste après succès
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        if (mainController != null) {
                            System.out.println("Retour à la liste des événements");
                            mainController.goToEventList();
                        } else {
                            System.out.println("mainController est null, impossible de retourner à la liste");
                        }
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
        submitBtn.setText(eventIdToModify != null ? "Modifier" : "Enregistrer");
    }

    private void fermerFormulaire() {
        if (mainController != null) {
            mainController.goToEventList();
        }
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