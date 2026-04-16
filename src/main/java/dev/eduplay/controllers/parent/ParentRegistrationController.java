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
        registrationService = new EventRegistrationService();
        eventService = new SchoolEventService();
        backBtn.setOnAction(e -> Router.go("parent_event_detail", eventId));
        submitBtn.setOnAction(e -> submitRegistration());
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
        try {
            SchoolEvent event = eventService.recupererParId(eventId);
            if (event != null) {
                eventTitleLabel.setText("Événement : " + event.getTitle());
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
            return;
        }
        if (parentPhone.isEmpty()) {
            showError("Veuillez saisir le téléphone parent");
            return;
        }

        try {
            SchoolEvent event = eventService.recupererParId(eventId);
            if (event == null) {
                showError("Événement non trouvé");
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
            registration.setStatus("PENDING");
            registration.setRegisteredAt(LocalDateTime.now());
            registration.setChildFullName(childName);
            registration.setParentPhone(parentPhone);
            registration.setChildClassLevel(classLevelField.getText().trim().isEmpty() ? null : classLevelField.getText().trim());
            registration.setMedicalNotes(medicalNotesArea.getText().trim().isEmpty() ? null : medicalNotesArea.getText().trim());
            registration.setEmergencyContactName(emergencyNameField.getText().trim().isEmpty() ? null : emergencyNameField.getText().trim());
            registration.setEmergencyContactPhone(emergencyPhoneField.getText().trim().isEmpty() ? null : emergencyPhoneField.getText().trim());
            registration.setNotes(notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim());

            // ✅ INITIALISER LES VALEURS PAR DÉFAUT (évite les null)
            registration.setTicketQrCode(null);
            registration.setQrCodePath(null);
            registration.setScannedAt(null);
            registration.setReminderSent(false);  // ← Important !
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
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}