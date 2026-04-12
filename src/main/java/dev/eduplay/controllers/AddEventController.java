package dev.eduplay.controllers;

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
    @FXML private TextField imagePathField;
    @FXML private Button browseImageBtn;
    @FXML private Button submitBtn;
    @FXML private Button cancelBtn;
    @FXML private Label messageLabel;
    @FXML private StackPane imagePreviewPane;
    @FXML private Label noImageLabel;

    // ==================== ATTRIBUTS ====================
    private SchoolEventService service;
    private File selectedImageFile;
    private static final String UPLOAD_DIR = "uploads/events/";

    // ==================== INITIALISATION ====================
    @FXML
    public void initialize() {
        // Initialisation du service
        service = new SchoolEventService();

        // Remplir les ComboBox des heures et minutes
        initHourMinuteCombos();

        // Configurer la validation des dates
        setupDateValidation();

        // Configurer les actions des boutons
        setupActions();

        // Configurer la validation en temps réel
        setupLiveValidation();

        // Valeurs par défaut
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now());
        startHourCombo.setValue(9);
        startMinuteCombo.setValue(0);
        endHourCombo.setValue(17);
        endMinuteCombo.setValue(0);
    }

    // Remplir les ComboBox (heures 0-23, minutes 0-59)
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

    // Validation des dates (empêcher les dates passées)
    private void setupDateValidation() {
        // Désactiver les dates passées pour DatePicker début
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

        // Désactiver les dates passées pour DatePicker fin
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

        // Si la date de début change, ajuster la date de fin si nécessaire
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && endDatePicker.getValue() != null && endDatePicker.getValue().isBefore(newVal)) {
                endDatePicker.setValue(newVal);
            }
        });
    }

    // Configuration des actions des boutons
    private void setupActions() {
        submitBtn.setOnAction(e -> ajouterEvent());
        cancelBtn.setOnAction(e -> fermerFormulaire());
        browseImageBtn.setOnAction(e -> parcourirImage());
    }

    // Validation en temps réel (bordure rouge si invalide)
    private void setupLiveValidation() {
        titleField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !isValidTitle(newVal)) {
                titleField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
            } else {
                titleField.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
            }
        });

        descriptionArea.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !isValidDescription(newVal)) {
                descriptionArea.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
            } else {
                descriptionArea.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
            }
        });

        locationField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !isValidLocation(newVal)) {
                locationField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 5;");
            } else {
                locationField.setStyle("-fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
            }
        });
    }

    // ==================== VALIDATION DES CHAMPS ====================

    // Vérifier si le texte contient des lettres (pas que des chiffres)
    private boolean containsLetter(String text) {
        return text.matches(".*[a-zA-ZàâäéèêëîïôöùûüçÀÂÄÉÈÊËÎÏÔÖÙÛÜÇ].*");
    }

    // Valider le titre
    private boolean isValidTitle(String title) {
        if (title == null) return false;
        title = title.trim();
        return title.length() >= 3 && containsLetter(title);
    }

    // Valider la description
    private boolean isValidDescription(String description) {
        if (description == null) return false;
        return description.trim().length() >= 10;
    }

    // Valider le lieu
    private boolean isValidLocation(String location) {
        if (location == null) return false;
        location = location.trim();
        return location.length() >= 3 && containsLetter(location);
    }

    // ==================== PARCOURIR IMAGE ====================
    private void parcourirImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image pour l'événement");

        // Filtrer les fichiers image
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter(
                "Fichiers image", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"
        );
        fileChooser.getExtensionFilters().add(imageFilter);

        Stage stage = (Stage) browseImageBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imagePathField.setText(file.getAbsolutePath());
            afficherApercuImage(file);
        }
    }

    // Afficher l'aperçu de l'image
    private void afficherApercuImage(File imageFile) {
        try {
            Image image = new Image(imageFile.toURI().toString(), 200, 120, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(200);
            imageView.setFitHeight(120);

            imagePreviewPane.getChildren().clear();
            imagePreviewPane.getChildren().add(imageView);
            noImageLabel.setVisible(false);
        } catch (Exception e) {
            System.err.println("Erreur chargement image: " + e.getMessage());
        }
    }

    // Copier l'image dans le dossier uploads
    private String copyImageToUploads() {
        if (selectedImageFile == null) {
            return null;
        }

        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String timestamp = String.valueOf(System.currentTimeMillis());
            String extension = "";
            if (selectedImageFile.getName().contains(".")) {
                extension = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf("."));
            }
            String newFileName = "event_" + timestamp + extension;
            File destFile = new File(uploadDir, newFileName);

            Files.copy(selectedImageFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return UPLOAD_DIR + newFileName;

        } catch (IOException e) {
            e.printStackTrace();
            showError("Erreur lors de la copie de l'image: " + e.getMessage());
            return null;
        }
    }

    // ==================== CONVERSION DATE ====================
    private LocalDateTime getDateTimeFromPicker(DatePicker datePicker, ComboBox<Integer> hourCombo, ComboBox<Integer> minuteCombo) {
        if (datePicker.getValue() == null || hourCombo.getValue() == null || minuteCombo.getValue() == null) {
            return null;
        }
        return LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourCombo.getValue(), minuteCombo.getValue()));
    }

    // ==================== AJOUTER ÉVÉNEMENT ====================
    private void ajouterEvent() {
        // Désactiver le bouton pendant l'ajout
        submitBtn.setDisable(true);
        submitBtn.setText("Ajout en cours...");

        try {
            // ========== RÉCUPÉRATION DES VALEURS ==========
            String title = titleField.getText();
            String description = descriptionArea.getText();
            String location = locationField.getText();

            // ========== VALIDATION DU TITRE ==========
            if (!isValidTitle(title)) {
                showError("Le titre doit contenir au moins 3 caractères (dont des lettres)");
                titleField.requestFocus();
                resetButton();
                return;
            }

            // ========== VALIDATION DE LA DESCRIPTION ==========
            if (!isValidDescription(description)) {
                showError("La description doit contenir au moins 10 caractères");
                descriptionArea.requestFocus();
                resetButton();
                return;
            }

            // ========== VALIDATION DU LIEU ==========
            if (!isValidLocation(location)) {
                showError("Le lieu doit contenir au moins 3 caractères (dont des lettres)");
                locationField.requestFocus();
                resetButton();
                return;
            }

            // ========== VALIDATION DES DATES ==========
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

            // ========== CRÉATION DE L'OBJET SCHOOL EVENT ==========
            SchoolEvent event = new SchoolEvent();
            event.setTitle(title.trim());
            event.setDescription(description.trim());
            event.setStartDate(startDateTime);
            event.setEndDate(endDateTime);
            event.setLocation(location.trim());
            event.setLatitude(null);
            event.setLongitude(null);

            // Copier l'image si sélectionnée
            String imagePath = copyImageToUploads();
            event.setImagePath(imagePath);

            // ========== INSERTION DANS LA BASE DE DONNÉES ==========
            service.ajouter(event);

            // ========== SUCCÈS ==========
            showSuccess("✅ Événement créé avec succès !");

            // Fermer la fenêtre après 1.5 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(this::fermerFormulaire);
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

    // Réactiver le bouton après erreur
    private void resetButton() {
        submitBtn.setDisable(false);
        submitBtn.setText("Créer l'événement");
    }

    // ==================== FERMETURE ====================
    private void fermerFormulaire() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    // ==================== MESSAGES ====================
    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
    }
}