package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.core.SessionManager;
import dev.eduplay.services.FaceService;
import dev.eduplay.services.UserService;
import dev.eduplay.utils.WebcamCapture;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;

public class FaceLoginController {

    @FXML private ImageView cameraView;
    @FXML private TextField usernameField;
    @FXML private Label statusLabel;
    @FXML private Button captureBtn;

    private final UserService userService = new UserService();
    private Image capturedImage;

    // Simule la capture — remplacez par votre lib webcam (Sarxos ou OpenIMAJ)
    @FXML
    private void handleCapture() {
        // TODO: intégrer la capture webcam réelle
        // Pour l'instant on prend le snapshot de cameraView
        capturedImage = cameraView.getImage();
        if (capturedImage == null) {
            setStatus("Aucune image capturée.", false);
            return;
        }
        setStatus("Photo capturée. Cliquez sur Vérifier.", true);
    }

    @FXML
    private void handleVerify() {
        String username = usernameField.getText().trim();
        if (username.isEmpty() || capturedImage == null) {
            setStatus("Entrez votre identifiant et capturez votre visage.", false);
            return;
        }

        String storedEmbedding = userService.getFacialEmbedding(username);
        if (storedEmbedding == null) {
            setStatus("Identifiant inconnu ou visage non enregistré.", false);
            return;
        }

        setStatus("Vérification en cours...", true);
        captureBtn.setDisable(true);

        new Thread(() -> {
            try {
                String base64 = WebcamCapture.imageToBase64(capturedImage);
                boolean match = FaceService.compareFaces(storedEmbedding, base64);

                javafx.application.Platform.runLater(() -> {
                    if (match) {
                        var user = userService.findByLogin(username);
                        String token = userService.createSession(user.getId());
                        SessionManager.getInstance().login(user, token);
                        Router.go("enfant");
                    } else {
                        setStatus("Visage non reconnu. Réessayez.", false);
                        captureBtn.setDisable(false);
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    setStatus("Erreur : " + e.getMessage(), false);
                    captureBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void setStatus(String msg, boolean ok) {
        statusLabel.setStyle(ok ? "-fx-text-fill: green;" : "-fx-text-fill: red;");
        statusLabel.setText(msg);
    }

    @FXML
    private void goToLogin() { Router.go("login"); }
}