package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.SQLException;

public class EditRegistrationController {

    @FXML private Button cancelBtn;
    @FXML private Button submitBtn;
    @FXML private Label subtitleLabel;
    @FXML private Label childNameLabel;
    @FXML private Label parentPhoneLabel;
    @FXML private Label eventTitleLabel;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;

    private EventRegistrationService service;
    private int eventId;
    private String eventTitle;
    private EventRegistration currentRegistration;

    @FXML
    public void initialize() {
        System.out.println("EditRegistrationController initialisé");
        service = new EventRegistrationService();
        setupActions();
    }

    // ✅ Méthode appelée par Router avec l'ID
    public void setRegistrationId(int registrationId) {
        System.out.println("=== setRegistrationId appelé avec ID: " + registrationId);
        try {
            EventRegistration registration = service.recupererParId(registrationId);
            if (registration != null) {
                setRegistration(registration);
            } else {
                System.out.println("❌ Inscription non trouvée");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ✅ Méthode appelée par Router avec l'objet
    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        displayRegistrationInfo();
    }

    private void displayRegistrationInfo() {
        if (currentRegistration == null) {
            System.out.println("❌ currentRegistration est null");
            return;
        }

        System.out.println("=== Affichage des infos de l'inscription ===");
        System.out.println("ID: " + currentRegistration.getId());
        System.out.println("Enfant: " + currentRegistration.getChildFullName());
        System.out.println("Téléphone: " + currentRegistration.getParentPhone());

        childNameLabel.setText(currentRegistration.getChildFullName());
        parentPhoneLabel.setText(currentRegistration.getParentPhone() != null ? currentRegistration.getParentPhone() : "Non spécifié");

        if (currentRegistration.getEvent() != null) {
            eventTitleLabel.setText(currentRegistration.getEvent().getTitle());
            this.eventId = currentRegistration.getEvent().getId();
            this.eventTitle = currentRegistration.getEvent().getTitle();
        }

        notesArea.setText(currentRegistration.getNotes() != null ? currentRegistration.getNotes() : "");
        subtitleLabel.setText("Modification pour : " + currentRegistration.getChildFullName());
    }

    private void setupActions() {
        cancelBtn.setOnAction(e -> goBack());
        submitBtn.setOnAction(e -> saveChanges());
    }

    private void saveChanges() {
        String newNotes = notesArea.getText().trim();

        System.out.println("=== Sauvegarde des modifications ===");
        System.out.println("Nouvelles notes: " + newNotes);

        try {
            currentRegistration.setNotes(newNotes.isEmpty() ? null : newNotes);
            service.modifier(currentRegistration);

            System.out.println("✅ Modification réussie !");
            showSuccess("✅ Inscription modifiée avec succès !");

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        Router.go("registration_list");
                    });
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }).start();

        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur base de données: " + e.getMessage());
        }
    }

    private void goBack() {
        Router.go("registration_list");
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