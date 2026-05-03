package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.AIDescriptionService;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AddEventController {

    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker startDatePicker;
    @FXML private TextField startTimeField;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField endTimeField;
    @FXML private TextField locationField;
    @FXML private TextField maxCapacityField;
    @FXML private Label imagePathLabel;
    @FXML private Button cancelBtn;
    @FXML private Button submitBtn;
    @FXML private Button generateDescBtn;
    @FXML private ComboBox<String> targetPublicCombo;
    @FXML private ComboBox<String> descriptionStyleCombo;
    @FXML private Label messageLabel;

    private SchoolEventService service;
    private String selectedImagePath = null;
    private AIDescriptionService aiService;

    @FXML
    public void initialize() {
        System.out.println("AddEventController initialisé");
        service = new SchoolEventService();
        aiService = new AIDescriptionService();

        // Valeurs par défaut
        startTimeField.setText("10:00");
        endTimeField.setText("12:00");

        // ✅ EMPÊCHER LA SÉLECTION DE DATES PASSÉES
        startDatePicker.setDayCellFactory(getDateCellFactory());
        endDatePicker.setDayCellFactory(getDateCellFactory());

        // ✅ Désactiver la saisie manuelle dans les DatePicker
        startDatePicker.setEditable(false);
        endDatePicker.setEditable(false);

        // Initialiser les combo boxes
        targetPublicCombo.getItems().addAll("Enfants (3-6 ans)", "Enfants (7-12 ans)", "Adolescents", "Tout public", "Familles", "Parents");
        targetPublicCombo.setValue("Enfants (3-6 ans)");

        descriptionStyleCombo.getItems().addAll("Classique", "Fun", "Éducatif", "Premium");
        descriptionStyleCombo.setValue("Classique");

        setupActions();
        setupValidation();
    }

    /**
     * ✅ Crée un factory pour désactiver les dates passées
     */
    private Callback<DatePicker, DateCell> getDateCellFactory() {
        return new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);
                        // Désactiver les dates antérieures à aujourd'hui
                        if (item.isBefore(LocalDate.now())) {
                            setDisable(true);
                            setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #cccccc;");
                        }
                    }
                };
            }
        };
    }

    private void setupActions() {
        cancelBtn.setOnAction(e -> Router.go("event_list"));
        submitBtn.setOnAction(e -> addEvent());
        generateDescBtn.setOnAction(e -> generateDescription());

        // ✅ Vérifier la cohérence date début / date fin quand date début change
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && endDatePicker.getValue() != null) {
                if (endDatePicker.getValue().isBefore(newVal)) {
                    endDatePicker.setValue(newVal);
                }
            }
        });

        // ✅ Vérifier la cohérence date début / date fin quand date fin change
        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && startDatePicker.getValue() != null) {
                if (newVal.isBefore(startDatePicker.getValue())) {
                    endDatePicker.setValue(startDatePicker.getValue());
                    showTemporaryMessage("⚠️ La date de fin ne peut pas être avant la date de début", "error");
                }
            }
        });
    }

    private void setupValidation() {
        // ✅ Titre : pas de caractères spéciaux interdits, longueur limitée
        titleField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                titleField.setText(old);
                showTemporaryMessage("Le titre ne peut pas dépasser 100 caractères", "error");
            }
        });

        // ✅ Description : longueur limitée
        descriptionArea.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.length() > 2000) {
                descriptionArea.setText(old);
                showTemporaryMessage("La description ne peut pas dépasser 2000 caractères", "error");
            }
        });

        // ✅ Capacité : seulement des chiffres, 1-1000
        maxCapacityField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                if (!newVal.matches("\\d*")) {
                    maxCapacityField.setText(old);
                    showTemporaryMessage("La capacité doit être un nombre", "error");
                } else if (!newVal.isEmpty() && Integer.parseInt(newVal) > 1000) {
                    maxCapacityField.setText(old);
                    showTemporaryMessage("La capacité ne peut pas dépasser 1000", "error");
                }
            }
        });

        // ✅ Heure début - format HH:MM
        startTimeField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("^([0-1]?[0-9]|2[0-3]):?[0-5]?[0-9]?$")) {
                startTimeField.setText(old);
            }
        });

        // ✅ Heure fin - format HH:MM
        endTimeField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("^([0-1]?[0-9]|2[0-3]):?[0-5]?[0-9]?$")) {
                endTimeField.setText(old);
            }
        });
    }

    /**
     * Génère une description automatique via IA
     */
    @FXML
    private void generateDescription() {
        String title = titleField.getText();
        String location = locationField.getText();
        String eventDate = getFormattedEventDate();
        String targetPublic = targetPublicCombo.getValue();
        String style = descriptionStyleCombo.getValue();

        if (title == null || title.trim().isEmpty()) {
            showTemporaryMessage("Veuillez d'abord saisir un titre !", "error");
            titleField.requestFocus();
            return;
        }

        if (location == null || location.trim().isEmpty()) {
            showTemporaryMessage("Veuillez d'abord saisir un lieu !", "error");
            locationField.requestFocus();
            return;
        }

        showTemporaryMessage("🤖 Génération de la description en cours...", "info");

        generateDescBtn.setDisable(true);

        new Thread(() -> {
            try {
                Thread.sleep(500);

                String generatedDescription;

                if ("Classique".equals(style)) {
                    generatedDescription = aiService.generateDescription(title, location, eventDate, targetPublic);
                } else {
                    generatedDescription = aiService.generateVariation(title, location, eventDate,
                            style.equals("Fun") ? "fun" : style.equals("Éducatif") ? "educatif" : "premium");
                }

                final String finalDescription = generatedDescription;
                javafx.application.Platform.runLater(() -> {
                    descriptionArea.setText(finalDescription);
                    generateDescBtn.setDisable(false);
                    showTemporaryMessage("✅ Description générée avec succès !", "success");
                });

            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> {
                    generateDescBtn.setDisable(false);
                    showTemporaryMessage("❌ Erreur lors de la génération", "error");
                });
            }
        }).start();
    }

    private String getFormattedEventDate() {
        if (startDatePicker.getValue() == null) {
            return "la date prévue";
        }
        LocalDate date = startDatePicker.getValue();
        String timeStr = startTimeField.getText();
        if (timeStr != null && !timeStr.trim().isEmpty() && isValidTime(timeStr)) {
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " à " + timeStr;
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private boolean isValidTime(String timeStr) {
        return timeStr != null && timeStr.matches("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$");
    }

    private void showTemporaryMessage(String message, String type) {
        javafx.application.Platform.runLater(() -> {
            messageLabel.setText(message);
            if ("error".equals(type)) {
                messageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            } else if ("success".equals(type)) {
                messageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 12px;");
            } else {
                messageLabel.setStyle("-fx-text-fill: #8b5cf6; -fx-font-size: 12px;");
            }
            messageLabel.setVisible(true);

            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
                } catch (InterruptedException e) {}
            }).start();
        });
    }

    private void addEvent() {
        // ==================== VALIDATION DES CHAMPS ====================

        // 1. Titre - non vide, sans caractères interdits
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showAlert("Erreur de saisie", "Le titre est obligatoire");
            titleField.requestFocus();
            return;
        }
        if (title.length() > 100) {
            showAlert("Erreur de saisie", "Le titre ne peut pas dépasser 100 caractères");
            titleField.requestFocus();
            return;
        }

        // 2. Description - non vide
        String description = descriptionArea.getText();
        if (description == null || description.trim().isEmpty()) {
            showAlert("Erreur de saisie", "La description est obligatoire");
            descriptionArea.requestFocus();
            return;
        }
        if (description.length() > 2000) {
            showAlert("Erreur de saisie", "La description ne peut pas dépasser 2000 caractères");
            descriptionArea.requestFocus();
            return;
        }

        // 3. Lieu - non vide
        String location = locationField.getText();
        if (location == null || location.trim().isEmpty()) {
            showAlert("Erreur de saisie", "Le lieu est obligatoire");
            locationField.requestFocus();
            return;
        }

        // 4. Date de début - non vide et pas dans le passé
        LocalDate startDate = startDatePicker.getValue();
        if (startDate == null) {
            showAlert("Erreur de saisie", "La date de début est obligatoire");
            startDatePicker.requestFocus();
            return;
        }
        if (startDate.isBefore(LocalDate.now())) {
            showAlert("Erreur de saisie", "La date de début ne peut pas être dans le passé");
            startDatePicker.requestFocus();
            return;
        }

        // 5. Heure de début
        String startTime = startTimeField.getText();
        if (startTime == null || startTime.trim().isEmpty()) {
            showAlert("Erreur de saisie", "L'heure de début est obligatoire");
            startTimeField.requestFocus();
            return;
        }
        if (!isValidTime(startTime)) {
            showAlert("Erreur de saisie", "Format d'heure invalide. Utilisez HH:MM (ex: 14:30)");
            startTimeField.requestFocus();
            return;
        }

        // 6. Date de fin - non vide et pas dans le passé
        LocalDate endDate = endDatePicker.getValue();
        if (endDate == null) {
            showAlert("Erreur de saisie", "La date de fin est obligatoire");
            endDatePicker.requestFocus();
            return;
        }
        if (endDate.isBefore(LocalDate.now())) {
            showAlert("Erreur de saisie", "La date de fin ne peut pas être dans le passé");
            endDatePicker.requestFocus();
            return;
        }

        // 7. Heure de fin
        String endTime = endTimeField.getText();
        if (endTime == null || endTime.trim().isEmpty()) {
            showAlert("Erreur de saisie", "L'heure de fin est obligatoire");
            endTimeField.requestFocus();
            return;
        }
        if (!isValidTime(endTime)) {
            showAlert("Erreur de saisie", "Format d'heure invalide. Utilisez HH:MM (ex: 16:30)");
            endTimeField.requestFocus();
            return;
        }

        // 8. Capacité - nombre valide
        String capacityText = maxCapacityField.getText();
        if (capacityText == null || capacityText.trim().isEmpty()) {
            showAlert("Erreur de saisie", "La capacité maximale est obligatoire");
            maxCapacityField.requestFocus();
            return;
        }

        int maxCapacity;
        try {
            maxCapacity = Integer.parseInt(capacityText.trim());
            if (maxCapacity <= 0) {
                showAlert("Erreur de saisie", "La capacité doit être un nombre positif (minimum 1)");
                maxCapacityField.requestFocus();
                return;
            }
            if (maxCapacity > 1000) {
                showAlert("Erreur de saisie", "La capacité ne peut pas dépasser 1000 personnes");
                maxCapacityField.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur de saisie", "La capacité doit être un nombre valide");
            maxCapacityField.requestFocus();
            return;
        }

        // ==================== CONVERSION DES DATES ====================

        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        try {
            startDateTime = LocalDateTime.of(startDate, LocalTime.parse(startTime));
            endDateTime = LocalDateTime.of(endDate, LocalTime.parse(endTime));
        } catch (DateTimeParseException e) {
            showAlert("Erreur de saisie", "Format de date/heure invalide");
            return;
        }

        // 9. Vérification que la date de fin est après la date de début
        if (endDateTime.isBefore(startDateTime)) {
            showAlert("Erreur de saisie", "La date de fin doit être postérieure à la date de début");
            endDatePicker.requestFocus();
            return;
        }

        // ==================== CRÉATION DE L'ÉVÉNEMENT ====================

        try {
            SchoolEvent event = new SchoolEvent();
            event.setTitle(title.trim());
            event.setDescription(description.trim());
            event.setStartDate(startDateTime);
            event.setEndDate(endDateTime);
            event.setLocation(location.trim());
            event.setMaxCapacity(maxCapacity);

            // Gestion de l'image (optionnelle)
            if (selectedImagePath != null) {
                event.setImagePath(selectedImagePath);
            }

            service.ajouter(event);
            showAlert("Succès", "✅ Événement ajouté avec succès !");
            Router.go("event_list");

        } catch (SQLException e) {
            e.printStackTrace();
            if (e.getMessage().contains("Duplicate entry")) {
                showAlert("Erreur", "Un événement avec ce titre existe déjà");
            } else {
                showAlert("Erreur", "Impossible d'ajouter l'événement: " + e.getMessage());
            }
        }
    }

    @FXML
    private void chooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Vérifier la taille du fichier (max 5MB)
                if (selectedFile.length() > 5 * 1024 * 1024) {
                    showAlert("Erreur", "L'image ne doit pas dépasser 5 Mo");
                    return;
                }

                String uploadDir = "uploads/events/";
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = uploadPath.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                selectedImagePath = "uploads/events/" + fileName;
                imagePathLabel.setText(selectedFile.getName());
                imagePathLabel.setStyle("-fx-text-fill: #10b981;");

                showTemporaryMessage("✅ Image chargée avec succès", "success");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de copier l'image: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}