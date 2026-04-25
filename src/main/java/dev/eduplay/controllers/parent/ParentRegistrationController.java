package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.entities.User;
import dev.eduplay.services.EventRegistrationService;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class ParentRegistrationController {

    @FXML private Button backBtn;
    @FXML private Button submitBtn;
    @FXML private Label eventTitleLabel;
    @FXML private TextField childNameField;
    @FXML private TextField parentPhoneField;
    @FXML private TextField classLevelField;
    @FXML private TextArea medicalNotesArea;
    @FXML private TextField emergencyNameField;
    @FXML private TextField emergencyPhoneField;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;

    private EventRegistrationService registrationService;
    private SchoolEventService eventService;
    private int eventId;

    @FXML
    public void initialize() {
        System.out.println("ParentRegistrationController initialisé");
        registrationService = new EventRegistrationService();
        eventService = new SchoolEventService();
        backBtn.setOnAction(e -> Router.go("parent_event_detail", eventId));
        submitBtn.setOnAction(e -> submitRegistration());

        setupPhoneValidation();
    }

    private void setupPhoneValidation() {
        parentPhoneField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !isValidPhone(newVal)) {
                parentPhoneField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                parentPhoneField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });

        childNameField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.trim().isEmpty()) {
                childNameField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2; -fx-border-radius: 8;");
            } else {
                childNameField.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8;");
            }
        });
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        if (!trimmed.matches("\\d{8}")) return false;
        char firstChar = trimmed.charAt(0);
        return firstChar == '2' || firstChar == '4' || firstChar == '5' || firstChar == '9';
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
        try {
            SchoolEvent event = eventService.recupererParId(eventId);
            if (event != null) {
                eventTitleLabel.setText("Événement : " + event.getTitle());
                // ✅ Vérifier la capacité avant de permettre l'inscription
                int remaining = event.getRemainingSpaces();
                if (remaining <= 0) {
                    submitBtn.setDisable(true);
                    showError("Désolé, cet événement est complet. Plus aucune place disponible.");
                } else {
                    submitBtn.setDisable(false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void submitRegistration() {
        String childName = childNameField.getText().trim();
        String parentPhone = parentPhoneField.getText().trim();

        if (childName.isEmpty()) {
            showError("Veuillez saisir le nom de l'enfant");
            childNameField.requestFocus();
            return;
        }

        if (childName.length() < 3) {
            showError("Le nom doit contenir au moins 3 caractères");
            childNameField.requestFocus();
            return;
        }

        if (parentPhone.isEmpty()) {
            showError("Veuillez saisir le téléphone parent");
            parentPhoneField.requestFocus();
            return;
        }

        if (!isValidPhone(parentPhone)) {
            showError("Le téléphone doit contenir 8 chiffres et commencer par 2, 4, 5 ou 9");
            parentPhoneField.requestFocus();
            return;
        }

        try {
            SchoolEvent event = eventService.recupererParId(eventId);
            if (event == null) {
                showError("Événement non trouvé");
                return;
            }

            // ✅ Vérifier la capacité avant l'insertion
            if (!event.hasAvailableSpaces()) {
                showError("Désolé, cet événement a atteint sa capacité maximale");
                return;
            }

            User currentUser = AppContext.getCurrentUser();
            if (currentUser == null) {
                showError("Utilisateur non connecté");
                return;
            }

            EventRegistration registration = new EventRegistration();
            registration.setEvent(event);
            registration.setParent(currentUser);
            // ❌ PLUS DE STATUS - L'inscription est directement validée
            registration.setRegisteredAt(LocalDateTime.now());
            registration.setChildFullName(childName);
            registration.setParentPhone(parentPhone);
            registration.setChildClassLevel(classLevelField.getText().trim().isEmpty() ? null : classLevelField.getText().trim());
            registration.setMedicalNotes(medicalNotesArea.getText().trim().isEmpty() ? null : medicalNotesArea.getText().trim());
            registration.setEmergencyContactName(emergencyNameField.getText().trim().isEmpty() ? null : emergencyNameField.getText().trim());
            registration.setEmergencyContactPhone(emergencyPhoneField.getText().trim().isEmpty() ? null : emergencyPhoneField.getText().trim());
            registration.setNotes(notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim());

            registration.setTicketQrCode(null);
            registration.setQrCodePath(null);
            registration.setScannedAt(null);
            registration.setReminderSent(false);
            registration.setReminderSentAt(null);

            registrationService.ajouter(registration);

            showSuccess("✅ Inscription envoyée avec succès !");

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        Router.go("parent_registrations");
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            showError("Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}