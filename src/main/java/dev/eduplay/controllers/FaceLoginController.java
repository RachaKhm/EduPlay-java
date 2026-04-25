package dev.eduplay.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.core.SessionManager;
import dev.eduplay.entities.User;
import dev.eduplay.services.FaceService;
import dev.eduplay.services.UserService;
import dev.eduplay.utils.WebcamCapture;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

public class FaceLoginController {

    @FXML private ImageView webcamView;
    @FXML private VBox      cameraOffOverlay;
    @FXML private TextField usernameField;
    @FXML private Label     statusLabel;
    @FXML private Button    startCamBtn;
    @FXML private Button    stopCamBtn;
    @FXML private Button    captureBtn;

    private final UserService userService = new UserService();

    private Webcam webcam;
    private Thread previewThread;
    private final AtomicBoolean streaming = new AtomicBoolean(false);
    private volatile BufferedImage lastFrame; // dernière frame capturée

    // ─── Démarrer la caméra ───────────────────────────────────

    @FXML
    private void handleStartCamera() {
        setStatus("Démarrage de la caméra...", "gray");
        startCamBtn.setDisable(true);

        new Thread(() -> {
            try {
                webcam = Webcam.getDefault();
                if (webcam == null) {
                    Platform.runLater(() -> {
                        setStatus("Aucune caméra détectée.", "red");
                        startCamBtn.setDisable(false);
                    });
                    return;
                }

                webcam.setViewSize(new Dimension(320, 240));
                webcam.open();
                streaming.set(true);

                Platform.runLater(() -> {
                    cameraOffOverlay.setVisible(false);
                    cameraOffOverlay.setManaged(false);
                    stopCamBtn.setDisable(false);
                    captureBtn.setDisable(false);
                    setStatus("Caméra active. Placez-vous devant et cliquez sur Capturer.", "green");
                });

                // Thread de preview — met à jour l'ImageView à ~15 FPS
                previewThread = new Thread(() -> {
                    while (streaming.get() && webcam.isOpen()) {
                        BufferedImage frame = webcam.getImage();
                        if (frame != null) {
                            lastFrame = frame;
                            Image fxImg = SwingFXUtils.toFXImage(frame, null);
                            Platform.runLater(() -> webcamView.setImage(fxImg));
                        }
                        try { Thread.sleep(66); } catch (InterruptedException e) { break; }
                    }
                });
                previewThread.setDaemon(true);
                previewThread.start();

            } catch (Exception e) {
                Platform.runLater(() -> {
                    setStatus("Erreur caméra : " + e.getMessage(), "red");
                    startCamBtn.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ─── Arrêter la caméra ────────────────────────────────────

    @FXML
    private void handleStopCamera() {
        stopCamera();
        cameraOffOverlay.setVisible(true);
        cameraOffOverlay.setManaged(true);
        captureBtn.setDisable(true);
        stopCamBtn.setDisable(true);
        startCamBtn.setDisable(false);
        webcamView.setImage(null);
        setStatus("Caméra arrêtée.", "gray");
    }

    private void stopCamera() {
        streaming.set(false);
        if (previewThread != null) previewThread.interrupt();
        if (webcam != null && webcam.isOpen()) webcam.close();
    }

    // ─── Capturer + Vérifier ──────────────────────────────────

    @FXML
    private void handleCaptureAndVerify() {
        String identifier = usernameField.getText().trim();

        if (identifier.isEmpty()) {
            setStatus("Entrez votre email ou identifiant.", "red");
            return;
        }
        if (lastFrame == null) {
            setStatus("Aucune image disponible. Démarrez la caméra.", "red");
            return;
        }

        // Chercher l'utilisateur
        User user = userService.findByLogin(identifier);
        if (user == null) {
            setStatus("Identifiant inconnu.", "red");
            return;
        }

        String storedEmbedding = userService.getFacialEmbedding(user.getId());
        if (storedEmbedding == null || storedEmbedding.isBlank()) {
            setStatus("Aucun visage enregistré pour ce compte.\nConnectez-vous normalement et enregistrez votre visage.", "red");
            return;
        }

        // Figer la frame au moment du clic
        BufferedImage snapshot = lastFrame;

        captureBtn.setDisable(true);
        setStatus("Analyse du visage en cours...", "gray");

        new Thread(() -> {
            try {
                // Convertir BufferedImage → base64
                Image fxImage = SwingFXUtils.toFXImage(snapshot, null);
                String base64 = WebcamCapture.imageToBase64(fxImage);

                boolean match = FaceService.compareFaces(storedEmbedding, base64);

                Platform.runLater(() -> {
                    if (match) {
                        setStatus("✓ Visage reconnu ! Connexion...", "green");
                        stopCamera();
                        finalizeLogin(user);
                    } else {
                        setStatus("Visage non reconnu. Repositionnez-vous et réessayez.", "red");
                        captureBtn.setDisable(false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                        setStatus("Serveur Python non démarré.\nLancez : python face-service/app.py", "red");
                    } else {
                        setStatus("Erreur : " + e.getMessage(), "red");
                    }
                    captureBtn.setDisable(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ─── Finalisation login ───────────────────────────────────

    private void finalizeLogin(User user) {
        String token = userService.createSession(user.getId());
        SessionManager.getInstance().login(user, token);
        AppContext.setCurrentUser(user);

        String route = switch (user.getType().toLowerCase()) {
            case "admin"      -> "admin_dashboard";
            case "enseignant" -> "teacher_dashboard";
            case "parent"     -> "parent_dashboard";
            case "enfant"     -> "child_dashboard";
            default           -> "admin_dashboard";
        };

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/shared/MainView.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root);
            try {
                var css = getClass().getResource("/styles/app.css");
                if (css == null) css = getClass().getResource("/styles/main.css");
                if (css != null) scene.getStylesheets().add(css.toExternalForm());
            } catch (Exception ignored) {}
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
            Router.go(route);
        } catch (Exception e) {
            setStatus("Erreur chargement : " + e.getMessage(), "red");
            e.printStackTrace();
        }
    }

    // ─── Retour login ─────────────────────────────────────────

    @FXML
    private void goToLogin() {
        stopCamera();
        try {
            var url = getClass().getResource("/views/auth/LoginView.fxml");
            if (url == null) url = getClass().getResource("/views/LoginView.fxml");
            if (url == null) return;
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 860, 540));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Nettoyage quand la vue est fermée ────────────────────

    public void cleanup() {
        stopCamera();
    }

    // ─── Utils ───────────────────────────────────────────────

    private void setStatus(String msg, String color) {
        if (statusLabel == null) return;
        String hex = switch (color) {
            case "green" -> "#2E9E6E";
            case "red"   -> "#E94560";
            default      -> "#555577";
        };
        statusLabel.setStyle("-fx-text-fill: " + hex + "; -fx-font-size: 12px;");
        statusLabel.setText(msg);
    }
}