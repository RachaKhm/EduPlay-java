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
    @FXML private Button generateQrBtn;
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

        generateQrBtn.setOnAction(e -> generateQRCode());
    }

    // ✅ Méthode appelée par Router
    public void setRegistration(EventRegistration registration) {
        System.out.println("=== setRegistration appelé ===");
        System.out.println("Registration ID: " + registration.getId());
        System.out.println("Enfant: " + registration.getChildFullName());
        this.currentRegistration = registration;
        displayDetails();
    }

    private void displayDetails() {
        if (currentRegistration == null) {
            System.out.println("❌ currentRegistration est null");
            return;
        }

        System.out.println("Affichage détails inscription ID: " + currentRegistration.getId());

        if (currentRegistration.getEvent() != null) {
            eventValue.setText(currentRegistration.getEvent().getTitle());
            System.out.println("Événement: " + currentRegistration.getEvent().getTitle());
        } else {
            eventValue.setText("Événement non trouvé");
        }

        childNameValue.setText(currentRegistration.getChildFullName() != null ? currentRegistration.getChildFullName() : "Non spécifié");
        phoneValue.setText(currentRegistration.getParentPhone() != null ? currentRegistration.getParentPhone() : "Non spécifié");

        if (currentRegistration.getRegisteredAt() != null) {
            registeredAtValue.setText(currentRegistration.getRegisteredAt().format(dateFormatter));
        } else {
            registeredAtValue.setText("Date non spécifiée");
        }

        if (currentRegistration.getScannedAt() != null) {
            scannedAtValue.setText(currentRegistration.getScannedAt().format(dateFormatter));
            scannedBox.setVisible(true);
            scannedBox.setManaged(true);
        } else {
            scannedBox.setVisible(false);
            scannedBox.setManaged(false);
        }

        displayQRCode();
    }

    private void displayQRCode() {
        String qrPath = currentRegistration.getQrCodePath();
        System.out.println("QR Path: " + qrPath);

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

    private void generateQRCode() {
        if (currentRegistration == null) {
            showAlert("Erreur", "Aucune inscription sélectionnée");
            return;
        }

        try {
            String qrCodePath = dev.eduplay.utils.QRCodeGenerator.generateForRegistration(currentRegistration.getId());
            String qrCodeValueData = "REGISTRATION_ID:" + currentRegistration.getId();

            service.updateQRCode(currentRegistration.getId(), qrCodePath, qrCodeValueData);

            currentRegistration = service.recupererParId(currentRegistration.getId());
            displayQRCode();

            showAlert("Succès", "✅ QR Code généré avec succès !");

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors de la génération: " + e.getMessage());
            e.printStackTrace();
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