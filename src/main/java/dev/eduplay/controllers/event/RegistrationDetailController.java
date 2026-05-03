package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class RegistrationDetailController {

    @FXML private Button backBtn;
    @FXML private Button editBtn;
    @FXML private Label eventValue;
    @FXML private Label childNameValue;
    @FXML private Label phoneValue;
    @FXML private Label registeredAtValue;
    @FXML private Label scannedAtValue;
    @FXML private Label qrCodeMessage;
    @FXML private ImageView qrCodeImageView;
    @FXML private VBox qrBox;
    @FXML private VBox scannedBox;

    private EventRegistrationService service;
    private EventRegistration currentRegistration;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("RegistrationDetailController initialisé");
        service = new EventRegistrationService();

        backBtn.setOnAction(e -> {
            System.out.println("Retour à la liste");
            Router.go("registration_list");
        });

        editBtn.setOnAction(e -> {
            if (currentRegistration != null) {
                System.out.println("Modifier inscription ID: " + currentRegistration.getId());
                Router.go("edit_registration", currentRegistration.getId());
            }
        });
    }

    // ✅ Méthode appelée par Router quand on passe un ID
    public void setRegistrationId(int registrationId) {
        System.out.println("=== setRegistrationId appelé avec ID: " + registrationId);
        try {
            EventRegistration registration = service.recupererParId(registrationId);
            if (registration != null) {
                this.currentRegistration = registration;
                displayDetails();
            } else {
                System.out.println("❌ Inscription non trouvée avec ID: " + registrationId);
                showErrorMessage("Inscription non trouvée");
            }
        } catch (SQLException e) {
            System.err.println("Erreur chargement inscription: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur lors du chargement: " + e.getMessage());
        }
    }

    // ✅ Méthode appelée par Router quand on passe l'objet complet
    public void setRegistration(EventRegistration registration) {
        System.out.println("=== setRegistration appelé avec ID: " + registration.getId());
        this.currentRegistration = registration;
        displayDetails();
    }

    private void displayDetails() {
        if (currentRegistration == null) {
            System.out.println("❌ currentRegistration est null");
            return;
        }

        System.out.println("Affichage détails inscription ID: " + currentRegistration.getId());

        try {
            if (currentRegistration.getEvent() != null) {
                eventValue.setText(currentRegistration.getEvent().getTitle() != null ?
                        currentRegistration.getEvent().getTitle() : "Titre non disponible");
            } else {
                eventValue.setText("Événement non disponible");
            }
        } catch (Exception e) {
            System.err.println("Erreur affichage événement: " + e.getMessage());
            eventValue.setText("Erreur chargement");
        }

        try {
            childNameValue.setText(currentRegistration.getChildFullName() != null ?
                    currentRegistration.getChildFullName() : "Non spécifié");
        } catch (Exception e) {
            childNameValue.setText("Erreur");
        }

        try {
            phoneValue.setText(currentRegistration.getParentPhone() != null ?
                    currentRegistration.getParentPhone() : "Non spécifié");
        } catch (Exception e) {
            phoneValue.setText("Erreur");
        }

        try {
            if (currentRegistration.getRegisteredAt() != null) {
                registeredAtValue.setText(currentRegistration.getRegisteredAt().format(dateFormatter));
            } else {
                registeredAtValue.setText("Date non disponible");
            }
        } catch (Exception e) {
            registeredAtValue.setText("Erreur");
        }

        try {
            if (currentRegistration.getScannedAt() != null) {
                scannedAtValue.setText(currentRegistration.getScannedAt().format(dateFormatter));
                scannedBox.setVisible(true);
                scannedBox.setManaged(true);
            } else {
                scannedBox.setVisible(false);
                scannedBox.setManaged(false);
            }
        } catch (Exception e) {
            scannedBox.setVisible(false);
        }

        displayQRCode();
    }

    private void displayQRCode() {
        String qrPath = null;
        try {
            qrPath = currentRegistration.getQrCodePath();
            System.out.println("QR Path: " + qrPath);
        } catch (Exception e) {
            System.err.println("Erreur récupération QR path: " + e.getMessage());
        }

        if (qrPath != null && !qrPath.isEmpty()) {
            File qrFile = new File(qrPath);
            if (qrFile.exists()) {
                try {
                    Image qrImage = new Image(qrFile.toURI().toString());
                    qrCodeImageView.setImage(qrImage);
                    qrCodeImageView.setVisible(true);
                    qrBox.setVisible(true);
                    qrBox.setManaged(true);
                    qrCodeMessage.setText("✅ QR Code prêt");
                    System.out.println("✅ QR Code affiché");
                } catch (Exception e) {
                    System.err.println("Erreur chargement QR code: " + e.getMessage());
                    qrCodeMessage.setText("❌ Erreur chargement");
                }
            } else {
                qrCodeMessage.setText("⏳ QR Code non trouvé");
                System.out.println("Fichier QR non trouvé: " + qrFile.getAbsolutePath());
            }
        } else {
            qrCodeMessage.setText("⏳ Aucun QR Code généré");
        }
    }

    private void showErrorMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Router.go("registration_list");
    }
}