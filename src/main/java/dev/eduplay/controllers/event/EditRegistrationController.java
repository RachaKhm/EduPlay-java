package dev.eduplay.controllers.event;

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
    @FXML private ComboBox<String> statusCombo;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;

    private EventRegistrationService service;
    private MainController mainController;
    private EventRegistration currentRegistration;

    @FXML
    public void initialize() {
        service = new EventRegistrationService();

        // Initialiser le ComboBox
        statusCombo.getItems().clear();
        statusCombo.getItems().addAll("PENDING", "APPROVED", "REJECTED");

        setupActions();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        displayRegistrationInfo();
    }

    private void displayRegistrationInfo() {
        if (currentRegistration == null) return;

        childNameLabel.setText(currentRegistration.getChildFullName());
        parentPhoneLabel.setText(currentRegistration.getParentPhone() != null ? currentRegistration.getParentPhone() : "Non spécifié");

        if (currentRegistration.getEvent() != null) {
            eventTitleLabel.setText(currentRegistration.getEvent().getTitle());
        }

        statusCombo.setValue(currentRegistration.getStatus());
        notesArea.setText(currentRegistration.getNotes() != null ? currentRegistration.getNotes() : "");
        subtitleLabel.setText("Modification pour : " + currentRegistration.getChildFullName());

        // Log pour déboguer
        System.out.println("=== Modification inscription ===");
        System.out.println("ID: " + currentRegistration.getId());
        System.out.println("Statut actuel: " + currentRegistration.getStatus());
        System.out.println("Notes actuelles: " + currentRegistration.getNotes());
    }

    private void setupActions() {
        cancelBtn.setOnAction(e -> goBack());
        submitBtn.setOnAction(e -> saveChanges());
    }

    private void saveChanges() {
        String newStatus = statusCombo.getValue();
        String newNotes = notesArea.getText().trim();

        System.out.println("=== Sauvegarde des modifications ===");
        System.out.println("Nouveau statut: " + newStatus);
        System.out.println("Nouvelles notes: " + newNotes);

        if (newStatus == null || newStatus.isEmpty()) {
            showError("Veuillez sélectionner un statut");
            return;
        }

        try {
            // Mettre à jour l'objet
            currentRegistration.setStatus(newStatus);
            currentRegistration.setNotes(newNotes.isEmpty() ? null : newNotes);

            // ✅ Appel à la nouvelle méthode modifierBack()
            service.modifierBack(currentRegistration);

            System.out.println("✅ Modification réussie !");
            showSuccess("✅ Inscription modifiée avec succès !");

            // Retour à la liste après succès
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> {
                        if (mainController != null) {
                            mainController.goToRegistrationList();
                        }
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
        if (mainController != null) {
            mainController.goToRegistrationList();
        }
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);

        // Faire disparaître le message après 3 secondes
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