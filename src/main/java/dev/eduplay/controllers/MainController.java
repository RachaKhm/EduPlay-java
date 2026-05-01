package dev.eduplay.controllers;

import com.github.sarxos.webcam.Webcam;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController {

    @FXML private StackPane contentArea;
    @FXML private Label     pillInitials;
    @FXML private Label     pillName;
    @FXML private Label     pillRole;
    @FXML private HBox      btnProfile;
    @FXML private StackPane faceEnrollBtn;

    private final UserService userService = new UserService();

    private static final Map<String, String> ROUTE_TITLES = Map.ofEntries(
            Map.entry("admin_dashboard",   "Tableau de bord"),
            Map.entry("users",             "Gestion des utilisateurs"),
            Map.entry("teacher_dashboard", "Tableau de bord"),
            Map.entry("teacher_courses",   "Mes cours"),
            Map.entry("teacher_students",  "Mes élèves"),
            Map.entry("parent_dashboard",  "Tableau de bord"),
            Map.entry("parent_children",   "Mes enfants"),
            Map.entry("parent_events",     "Événements"),
            Map.entry("child_dashboard",   "Tableau de bord"),
            Map.entry("child_courses",     "Mes cours"),
            Map.entry("child_games",       "Jeux"),
            Map.entry("profile",           "Mon profil")
    );

    @FXML
    public void initialize() {
        String fullName = AppContext.getFullName();
        if (pillName     != null) pillName.setText(capitalize(fullName));
        if (pillRole     != null) pillRole.setText(capitalize(AppContext.getRole()));
        if (pillInitials != null) pillInitials.setText(buildInitials(fullName));
        Router.init(contentArea);
        Router.setOnRouteChange(route -> {});
        Router.go(AppContext.getDefaultRoute());
    }

    // ─── Popup enrollment facial (webcam live) ────────────────

    @FXML
    private void handleFaceEnroll() {
        User user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("EduPlay — Enregistrer mon visage");
        popup.setResizable(false);

        // ── Composants ──────────────────────────────────────────
        Label title = new Label("📷  Enregistrer mon visage");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #22223A;");

        String existing = userService.getFacialEmbedding(user.getId());
        Label subtitle = new Label(existing != null
                ? "✓ Visage déjà enregistré — vous pouvez le mettre à jour."
                : "Aucun visage enregistré. Activez la caméra pour vous enregistrer.");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(380);
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                + (existing != null ? "#2E9E6E;" : "#E94560;"));

        // Preview webcam
        ImageView preview = new ImageView();
        preview.setFitWidth(240);
        preview.setFitHeight(180);
        preview.setPreserveRatio(true);

        StackPane previewPane = new StackPane();
        previewPane.setStyle("-fx-background-color: #1A1A2E; -fx-background-radius: 12;");
        previewPane.setMinSize(320, 240);
        previewPane.setMaxSize(320, 240);

        Label camHint = new Label("📷  Caméra non démarrée");
        camHint.setStyle("-fx-text-fill: #555577; -fx-font-size: 13px;");
        previewPane.getChildren().addAll(preview, camHint);

        Label statusLabel = new Label("");
        statusLabel.setWrapText(true);
        statusLabel.setMaxWidth(380);

        // Boutons
        Button startBtn   = new Button("▶  Démarrer la caméra");
        Button captureBtn = new Button("📸  Capturer et enregistrer");
        Button stopBtn    = new Button("⏹  Arrêter");
        Button cancelBtn  = new Button("Fermer");

        styleBtn(startBtn,   "#1A1A2E", "white");
        styleBtn(captureBtn, "#E94560", "white");
        styleBtn(stopBtn,    "#E5E7EB", "#374151");
        cancelBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #9999BB; " +
                "-fx-font-size: 12px; -fx-border-width: 0; -fx-cursor: hand;");

        captureBtn.setDisable(true);
        stopBtn.setDisable(true);

        HBox camBtns = new HBox(8, startBtn, stopBtn);
        camBtns.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(startBtn, Priority.ALWAYS);
        HBox.setHgrow(stopBtn,  Priority.ALWAYS);
        startBtn.setMaxWidth(Double.MAX_VALUE);
        stopBtn.setMaxWidth(Double.MAX_VALUE);

        // ── Webcam state ─────────────────────────────────────────
        final Webcam[] camHolder    = {null};
        final Thread[] threadHolder = {null};
        final AtomicBoolean streaming = new AtomicBoolean(false);
        final BufferedImage[] lastFrame = {null};

        startBtn.setOnAction(e -> {
            startBtn.setDisable(true);
            setStatus(statusLabel, "Démarrage...", "gray");
            new Thread(() -> {
                try {
                    Webcam cam = Webcam.getDefault();
                    if (cam == null) {
                        Platform.runLater(() -> {
                            setStatus(statusLabel, "Aucune caméra détectée.", "red");
                            startBtn.setDisable(false);
                        });
                        return;
                    }
                    cam.setViewSize(new Dimension(320, 240));
                    cam.open();
                    camHolder[0] = cam;
                    streaming.set(true);

                    Platform.runLater(() -> {
                        camHint.setVisible(false);
                        stopBtn.setDisable(false);
                        captureBtn.setDisable(false);
                        setStatus(statusLabel, "Caméra active. Placez votre visage et cliquez Capturer.", "green");
                    });

                    threadHolder[0] = new Thread(() -> {
                        while (streaming.get() && cam.isOpen()) {
                            BufferedImage frame = cam.getImage();
                            if (frame != null) {
                                lastFrame[0] = frame;
                                Image fx = SwingFXUtils.toFXImage(frame, null);
                                Platform.runLater(() -> preview.setImage(fx));
                            }
                            try { Thread.sleep(66); } catch (InterruptedException ex) { break; }
                        }
                    });
                    threadHolder[0].setDaemon(true);
                    threadHolder[0].start();

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        setStatus(statusLabel, "Erreur : " + ex.getMessage(), "red");
                        startBtn.setDisable(false);
                    });
                }
            }).start();
        });

        stopBtn.setOnAction(e -> {
            streaming.set(false);
            if (threadHolder[0] != null) threadHolder[0].interrupt();
            if (camHolder[0] != null && camHolder[0].isOpen()) camHolder[0].close();
            camHint.setVisible(true);
            preview.setImage(null);
            captureBtn.setDisable(true);
            stopBtn.setDisable(true);
            startBtn.setDisable(false);
            setStatus(statusLabel, "Caméra arrêtée.", "gray");
        });

        captureBtn.setOnAction(e -> {
            if (lastFrame[0] == null) return;
            BufferedImage snapshot = lastFrame[0];
            captureBtn.setDisable(true);
            setStatus(statusLabel, "Analyse en cours...", "gray");

            new Thread(() -> {
                try {
                    Image fxImg  = SwingFXUtils.toFXImage(snapshot, null);
                    String b64   = WebcamCapture.imageToBase64(fxImg);
                    String emb   = FaceService.extractEmbedding(b64);
                    userService.saveFacialEmbedding(user.getId(), emb);

                    Platform.runLater(() -> {
                        setStatus(statusLabel, "✓ Visage enregistré avec succès !", "green");
                        subtitle.setText("✓ Connexion faciale activée.");
                        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #2E9E6E;");
                        // Arrêter la caméra et fermer après 2s
                        streaming.set(false);
                        if (threadHolder[0] != null) threadHolder[0].interrupt();
                        if (camHolder[0] != null && camHolder[0].isOpen()) camHolder[0].close();
                        new Thread(() -> {
                            try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
                            Platform.runLater(popup::close);
                        }).start();
                    });

                } catch (Exception ex) {
                    Platform.runLater(() -> {
                        String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                        if (msg.contains("Connection refused") || msg.contains("ConnectException")) {
                            setStatus(statusLabel,
                                "⚠️ Serveur facial non démarré.\n" +
                                "Ouvrez un terminal et lancez :\n" +
                                "python face-service/app.py", "red");
                        } else if (msg.contains("Face could not be detected") || msg.contains("No face")) {
                            setStatus(statusLabel, "Aucun visage détecté. Rapprochez-vous de la caméra.", "red");
                        } else {
                            setStatus(statusLabel, "Erreur : " + msg, "red");
                        }
                        captureBtn.setDisable(false);
                    });
                }
            }).start();
        });

        cancelBtn.setOnAction(e -> {
            streaming.set(false);
            if (threadHolder[0] != null) threadHolder[0].interrupt();
            if (camHolder[0]   != null && camHolder[0].isOpen()) camHolder[0].close();
            popup.close();
        });

        popup.setOnCloseRequest(e -> {
            streaming.set(false);
            if (threadHolder[0] != null) threadHolder[0].interrupt();
            if (camHolder[0]   != null && camHolder[0].isOpen()) camHolder[0].close();
        });

        // ── Layout ───────────────────────────────────────────────
        VBox layout = new VBox(14,
                title, subtitle, previewPane, camBtns,
                statusLabel, captureBtn, cancelBtn
        );
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(32));
        layout.setStyle("-fx-background-color: #F8F9FA;");
        layout.setPrefWidth(460);

        popup.setScene(new Scene(layout));
        popup.show();
    }

    @FXML private void showProfile() { Router.go("profile"); }

    // ─── Utils ───────────────────────────────────────────────

    private String buildInitials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "U";
        String[] parts = fullName.split(" ");
        String init = "";
        if (parts.length > 0 && !parts[0].isEmpty()) init += parts[0].charAt(0);
        if (parts.length > 1 && !parts[1].isEmpty()) init += parts[1].charAt(0);
        return init.toUpperCase();
    }

    private String capitalize(String s) {
        if (s == null || s.isBlank()) return "";
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private void setStatus(Label label, String msg, String color) {
        String hex = switch (color) {
            case "green" -> "#2E9E6E";
            case "red"   -> "#E94560";
            default      -> "#555577";
        };
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + hex + ";");
        label.setText(msg);
    }

    private void styleBtn(Button btn, String bg, String fg) {
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; " +
                "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 10; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;");
    }
}