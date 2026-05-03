package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class EditRegistrationController {

    @FXML private Button cancelBtn;
    @FXML private Button submitBtn;
    @FXML private Label subtitleLabel;
    @FXML private Label childNameLabel;
    @FXML private Label parentPhoneLabel;
    @FXML private Label eventTitleLabel;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;
    @FXML private Label scanStatusLabel;
    @FXML private Button resetScanBtn;
    @FXML private HBox scanActionsBox;

    private EventRegistrationService service;
    private EventRegistration currentRegistration;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("EditRegistrationController initialisé");
        service = new EventRegistrationService();
        setupActions();
    }

    public void setRegistrationId(int registrationId) {
        System.out.println("=== setRegistrationId appelé avec ID: " + registrationId);
        try {
            EventRegistration registration = service.recupererParId(registrationId);
            if (registration != null) {
                setRegistration(registration);
            } else {
                System.out.println("❌ Inscription non trouvée");
                showError("Inscription non trouvée");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur chargement: " + e.getMessage());
        }
    }

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
        System.out.println("ScannedAt: " + currentRegistration.getScannedAt());

        childNameLabel.setText(currentRegistration.getChildFullName());
        parentPhoneLabel.setText(currentRegistration.getParentPhone() != null ? currentRegistration.getParentPhone() : "Non spécifié");

        if (currentRegistration.getEvent() != null) {
            eventTitleLabel.setText(currentRegistration.getEvent().getTitle());
        }

        notesArea.setText(currentRegistration.getNotes() != null ? currentRegistration.getNotes() : "");
        subtitleLabel.setText("Modification pour : " + currentRegistration.getChildFullName());

        // ✅ Afficher le statut du scan
        displayScanStatus();
    }

    private void displayScanStatus() {
        if (currentRegistration.getScannedAt() != null) {
            // QR code déjà scanné
            scanStatusLabel.setText("✅ Déjà scanné le " + currentRegistration.getScannedAt().format(dateFormatter));
            scanStatusLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
            resetScanBtn.setVisible(true);
            resetScanBtn.setManaged(true);
        } else {
            // QR code non scanné
            scanStatusLabel.setText("⏳ Non scanné - Ticket valide");
            scanStatusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-padding: 10 0 5 0;");
            resetScanBtn.setVisible(false);
            resetScanBtn.setManaged(false);
        }
    }

    private void setupActions() {
        cancelBtn.setOnAction(e -> goBack());
        submitBtn.setOnAction(e -> saveChanges());
        resetScanBtn.setOnAction(e -> resetScan());
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

    /**
     * ✅ NOUVEAU : Annuler le scan du ticket
     * Réinitialise scannedAt à null
     */
    private void resetScan() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler le scan du ticket");
        confirm.setContentText("Êtes-vous sûr de vouloir annuler le scan de ce ticket ?\n\n" +
                "L'enfant pourra à nouveau scanner son QR code à l'entrée.");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                currentRegistration.setScannedAt(null);
                service.modifier(currentRegistration);

                System.out.println("✅ Scan annulé pour l'inscription ID: " + currentRegistration.getId());
                showSuccess("✅ Scan annulé avec succès ! Le ticket est à nouveau valide.");

                // Mettre à jour l'affichage
                displayScanStatus();

            } catch (SQLException e) {
                System.err.println("Erreur lors de l'annulation du scan: " + e.getMessage());
                e.printStackTrace();
                showError("Erreur lors de l'annulation: " + e.getMessage());
            }
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