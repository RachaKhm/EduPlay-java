package dev.eduplay.controllers;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.services.EventRegistrationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ScannerController {

    @FXML private StackPane videoContainer;
    @FXML private VBox resultBox;
    @FXML private Label resultTitle;
    @FXML private Label resultMessage;
    @FXML private Label childNameResult;
    @FXML private Label eventNameResult;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    @FXML private Button backBtn;
    @FXML private Button closeResultBtn;
    @FXML private Label statusLabel;

    private Webcam webcam;
    private ImageView imageView;
    private ExecutorService executor;
    private boolean scanning = false;
    private EventRegistrationService registrationService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Rectangle scanOverlay;
    private Timeline pulseAnimation;
    private Timeline frameCapture;
    private boolean continuousMode = false;

    @FXML
    public void initialize() {
        registrationService = new EventRegistrationService();

        startBtn.setOnAction(e -> startScanner());
        stopBtn.setOnAction(e -> stopScanner());
        backBtn.setOnAction(e -> goBack());
        closeResultBtn.setOnAction(e -> {
            resultBox.setVisible(false);
            resultBox.setManaged(false);
            resultBox.getStyleClass().removeAll();
        });

        stopBtn.setDisable(true);

        imageView = new ImageView();
        imageView.setFitWidth(720);
        imageView.setFitHeight(540);
        imageView.setPreserveRatio(true);
    }

    private void startScanner() {
        if (webcam == null) {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                statusLabel.setText("❌ Aucune caméra détectée !");
                showError("Erreur caméra", "Aucune webcam n'est détectée sur votre système.");
                return;
            }
            webcam.setViewSize(WebcamResolution.VGA.getSize());
        }

        if (!webcam.isOpen()) {
            webcam.open();
        }

        Platform.runLater(() -> {
            videoContainer.getChildren().clear();
            videoContainer.getChildren().add(imageView);
            addScanOverlay();
        });

        scanning = true;
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
        statusLabel.setText("🟢 Scan en cours...");

        frameCapture = new Timeline(
                new KeyFrame(Duration.millis(100), e -> captureAndProcessFrame())
        );
        frameCapture.setCycleCount(Timeline.INDEFINITE);
        frameCapture.play();
    }

    private void captureAndProcessFrame() {
        if (!scanning || webcam == null || !webcam.isOpen()) {
            return;
        }

        try {
            BufferedImage bufferedImage = webcam.getImage();
            if (bufferedImage != null) {
                Image fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
                Platform.runLater(() -> imageView.setImage(fxImage));

                String result = decodeQRCode(bufferedImage);
                if (result != null) {
                    final String finalResult = result;
                    Platform.runLater(() -> {
                        processQRCodeResult(finalResult);
                        if (!continuousMode) {
                            stopScanner();
                        }
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur capture frame: " + e.getMessage());
        }
    }

    private void stopScanner() {
        scanning = false;

        if (frameCapture != null) {
            frameCapture.stop();
        }

        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }

        if (pulseAnimation != null) {
            pulseAnimation.stop();
        }

        Platform.runLater(() -> {
            videoContainer.getChildren().clear();
            startBtn.setDisable(false);
            stopBtn.setDisable(true);
            statusLabel.setText("⏹ Scan arrêté");
            imageView.setImage(null);
        });
    }

    private void addScanOverlay() {
        scanOverlay = new Rectangle(400, 300);
        scanOverlay.setFill(Color.TRANSPARENT);
        scanOverlay.setStroke(Color.LIME);
        scanOverlay.setStrokeWidth(3);
        scanOverlay.setArcWidth(20);
        scanOverlay.setArcHeight(20);

        scanOverlay.translateXProperty().bind(videoContainer.widthProperty().subtract(scanOverlay.widthProperty()).divide(2));
        scanOverlay.translateYProperty().bind(videoContainer.heightProperty().subtract(scanOverlay.heightProperty()).divide(2));

        pulseAnimation = new Timeline(
                new KeyFrame(Duration.seconds(0), e -> scanOverlay.setStroke(Color.LIME)),
                new KeyFrame(Duration.seconds(0.5), e -> scanOverlay.setStroke(Color.rgb(0, 255, 0, 0.3))),
                new KeyFrame(Duration.seconds(1), e -> scanOverlay.setStroke(Color.LIME))
        );
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.play();

        videoContainer.getChildren().add(scanOverlay);

        Text instructionText = new Text("Positionnez le QR code ici");
        instructionText.setFill(Color.WHITE);
        instructionText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        instructionText.translateXProperty().bind(videoContainer.widthProperty().subtract(instructionText.layoutBoundsProperty().getValue().getWidth()).divide(2));
        instructionText.translateYProperty().bind(scanOverlay.translateYProperty().subtract(10));

        videoContainer.getChildren().add(instructionText);
    }

    private String decodeQRCode(BufferedImage image) {
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);
            return result.getText();
        } catch (NotFoundException e) {
            return null;
        } catch (Exception e) {
            System.err.println("Erreur décodage QR: " + e.getMessage());
            return null;
        }
    }

    private void processQRCodeResult(String qrData) {
        System.out.println("QR Code détecté: " + qrData);

        int registrationId = extractRegistrationId(qrData);

        if (registrationId == -1) {
            showError("QR Code invalide", "Ce QR code n'est pas reconnu par le système.\nFormat attendu: REG_ID:123");
            return;
        }

        try {
            EventRegistration registration = registrationService.recupererParId(registrationId);

            if (registration == null) {
                showError("Inscription non trouvée", "Aucune inscription correspondante n'a été trouvée avec l'ID: " + registrationId);
                return;
            }

            // Vérifier si le QR code a déjà été scanné
            if (registration.getScannedAt() != null) {
                System.out.println("❌ QR code déjà scanné le: " + registration.getScannedAt());
                showError("Déjà scanné",
                        "Ce ticket a déjà été utilisé le " + registration.getScannedAt().format(dateFormatter));
                return;
            }

            // Valider l'inscription
            registration.setScannedAt(LocalDateTime.now());
            registrationService.modifier(registration);

            System.out.println("✅ Scan validé pour l'inscription ID: " + registrationId + " à " + LocalDateTime.now());
            showSuccess(registration);
            playSuccessSound();

        } catch (SQLException e) {
            showError("Erreur base de données", "Erreur lors de la validation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extrait l'ID d'inscription du QR code
     * Format attendu: REG_ID:123 (exemple: REG_ID:30)
     */
    private int extractRegistrationId(String qrData) {
        try {
            if (qrData != null) {
                // Nouveau format: REG_ID:123
                if (qrData.startsWith("REG_ID:")) {
                    String idStr = qrData.substring(7); // Prend tout après "REG_ID:"
                    // Gérer le cas où il y aurait encore un underscore (ancien format)
                    if (idStr.contains("_")) {
                        idStr = idStr.split("_")[0];
                    }
                    return Integer.parseInt(idStr);
                }
                // Format direct numérique
                else if (qrData.matches("\\d+")) {
                    return Integer.parseInt(qrData);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur extraction ID: " + e.getMessage());
        }
        return -1;
    }

    private void showSuccess(EventRegistration registration) {
        resultTitle.setText("✅ ENTRÉE VALIDÉE");
        resultTitle.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        resultMessage.setText("L'entrée a été validée avec succès !");
        childNameResult.setText("👶 Enfant : " + registration.getChildFullName());
        if (registration.getEvent() != null) {
            eventNameResult.setText("📅 Événement : " + registration.getEvent().getTitle());
        }
        resultBox.setVisible(true);
        resultBox.setManaged(true);
        resultBox.setStyle("-fx-background-color: #f0fdf4; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: #86efac; -fx-border-radius: 16;");
        statusLabel.setText("✅ Scan validé - " + registration.getChildFullName());

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                Platform.runLater(() -> {
                    if (continuousMode) {
                        resultBox.setVisible(false);
                        resultBox.setManaged(false);
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void showError(String title, String message) {
        resultTitle.setText("❌ " + title);
        resultTitle.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        resultMessage.setText(message);
        childNameResult.setText("");
        eventNameResult.setText("");
        resultBox.setVisible(true);
        resultBox.setManaged(true);
        resultBox.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 16; -fx-padding: 20; -fx-border-color: #fca5a5; -fx-border-radius: 16;");
        statusLabel.setText("❌ " + title);

        if ("Déjà scanné".equals(title)) {
            System.out.println("⏹ Arrêt du scanner car le QR code a déjà été utilisé");
            stopScanner();
        }
    }

    private void playSuccessSound() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    private void goBack() {
        cleanup();
        Router.go("admin_dashboard");
    }

    @FXML
    public void cleanup() {
        stopScanner();
        if (webcam != null) {
            webcam.close();
            webcam = null;
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
        scanning = false;
    }

    public void setContinuousMode(boolean continuous) {
        this.continuousMode = continuous;
    }

    public boolean isContinuousMode() {
        return continuousMode;
    }
}