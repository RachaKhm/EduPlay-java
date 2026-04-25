package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ParentRegistrationDetailController {

    @FXML private Button backBtn;
    @FXML private Label eventTitleLabel;
    @FXML private Label childNameLabel;
    @FXML private Label dateLabel;
    @FXML private Label locationLabel;
    @FXML private ImageView qrCodeImageView;
    @FXML private Label qrMessageLabel;
    @FXML private Label infoLabel;

    private EventRegistrationService service;
    private EventRegistration currentRegistration;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("ParentRegistrationDetailController initialisé");
        service = new EventRegistrationService();
        backBtn.setOnAction(e -> Router.go("parent_registrations"));
    }

    // Méthode appelée quand on passe un Integer (l'ID)
    public void setRegistrationId(int registrationId) {
        System.out.println("setRegistrationId appelé avec ID: " + registrationId);
        try {
            currentRegistration = service.recupererParId(registrationId);
            if (currentRegistration != null) {
                displayDetails();
            } else {
                showError("Inscription non trouvée avec l'ID: " + registrationId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Erreur lors du chargement: " + e.getMessage());
        }
    }

    // Méthode appelée quand on passe directement l'objet EventRegistration
    public void setRegistration(EventRegistration registration) {
        System.out.println("setRegistration appelé avec ID: " + (registration != null ? registration.getId() : "null"));
        if (registration != null) {
            this.currentRegistration = registration;
            displayDetails();
        } else {
            showError("Inscription reçue est null");
        }
    }

    private void displayDetails() {
        if (currentRegistration == null) {
            System.err.println("currentRegistration est null dans displayDetails");
            showError("Impossible d'afficher les détails");
            return;
        }

        System.out.println("Affichage détails inscription ID: " + currentRegistration.getId());

        // Informations de l'événement
        if (currentRegistration.getEvent() != null) {
            System.out.println("Titre événement: " + currentRegistration.getEvent().getTitle());
            eventTitleLabel.setText(currentRegistration.getEvent().getTitle());
            if (currentRegistration.getEvent().getStartDate() != null) {
                dateLabel.setText(currentRegistration.getEvent().getStartDate().format(dateFormatter));
                System.out.println("Date: " + currentRegistration.getEvent().getStartDate().format(dateFormatter));
            }
            if (currentRegistration.getEvent().getLocation() != null) {
                locationLabel.setText(currentRegistration.getEvent().getLocation());
                System.out.println("Lieu: " + currentRegistration.getEvent().getLocation());
            }
        } else {
            System.err.println("L'événement est null pour l'inscription ID: " + currentRegistration.getId());
            eventTitleLabel.setText("Événement non trouvé");
        }

        // Informations de l'enfant
        childNameLabel.setText(currentRegistration.getChildFullName());
        System.out.println("Enfant: " + currentRegistration.getChildFullName());

        // Message d'information
        infoLabel.setText("✅ Inscription confirmée - Présentez ce QR code à l'entrée");
        infoLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        // Afficher le QR code
        displayQRCode();
    }

    private void displayQRCode() {
        String qrPath = currentRegistration.getQrCodePath();
        System.out.println("Recherche QR code à: " + qrPath);

        if (qrPath != null && !qrPath.isEmpty()) {
            File qrFile = new File(qrPath);
            if (qrFile.exists()) {
                try {
                    Image qrImage = new Image(qrFile.toURI().toString());
                    qrCodeImageView.setImage(qrImage);
                    qrCodeImageView.setVisible(true);
                    qrMessageLabel.setText("✅ QR code prêt - À scanner à l'entrée");
                    qrMessageLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    System.out.println("✅ QR code affiché depuis: " + qrFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Erreur chargement QR code: " + e.getMessage());
                    showNoQRCode();
                }
            } else {
                System.out.println("❌ Fichier QR code non trouvé: " + qrFile.getAbsolutePath());
                showNoQRCode();
            }
        } else {
            System.out.println("❌ Aucun chemin QR code dans l'inscription ID: " + currentRegistration.getId());
            showNoQRCode();
        }
    }

    private void showNoQRCode() {
        qrCodeImageView.setImage(null);
        qrCodeImageView.setVisible(false);
        qrMessageLabel.setText("⏳ QR code non disponible");
        qrMessageLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        System.err.println("Erreur: " + message);
        qrMessageLabel.setText("❌ " + message);
        qrMessageLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        // Afficher aussi dans les labels principaux
        if (eventTitleLabel != null) eventTitleLabel.setText("Erreur");
        if (childNameLabel != null) childNameLabel.setText(message);
    }
}