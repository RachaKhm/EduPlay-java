package dev.eduplay.core;

import com.sun.net.httpserver.HttpServer;
import dev.eduplay.controllers.ResetPasswordController;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Petit serveur HTTP local sur le port 8765.
 * Quand l'utilisateur clique sur le bouton dans l'email,
 * son navigateur ouvre http://localhost:8765/reset?token=xxx
 * Ce serveur intercepte le token et ouvre ResetPasswordView dans l'app.
 *
 * Démarrer avec : ResetTokenServer.start(primaryStage)
 * Arrêter avec  : ResetTokenServer.stop()
 */
public class ResetTokenServer {

    private static HttpServer server;
    private static Stage appStage;

    private static final String[] RESET_PATHS = {
            "/views/reset-password.fxml",
            "/views/auth/reset-password.fxml",
            "/views/auth/ResetPasswordView.fxml",
            "/views/shared/reset-password.fxml",
            "/reset-password.fxml",
    };

    public static void start(Stage stage) {
        appStage = stage;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 8765), 0);

            server.createContext("/reset", exchange -> {
                String query = exchange.getRequestURI().getQuery(); // token=xxxx
                String token = null;
                if (query != null && query.startsWith("token=")) {
                    token = query.substring(6);
                }

                // Réponse HTML : page de succès + fermeture auto du navigateur
                String html = buildSuccessPage();
                exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, html.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(html.getBytes());
                }

                // Ouvrir ResetPasswordView dans l'app (thread JavaFX)
                final String finalToken = token;
                Platform.runLater(() -> openResetView(finalToken));
            });

            server.setExecutor(null);
            server.start();
            System.out.println("[ResetTokenServer] Démarré sur http://localhost:8765");

        } catch (IOException e) {
            System.err.println("[ResetTokenServer] Impossible de démarrer : " + e.getMessage());
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[ResetTokenServer] Arrêté.");
        }
    }

    private static void openResetView(String token) {
        if (token != null && !token.isBlank()) {
            TokenHolder.set(token);
        }

        for (String path : RESET_PATHS) {
            var url = ResetTokenServer.class.getResource(path);
            if (url != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();

                    // Utiliser la fenêtre existante ou en créer une
                    Stage target = appStage != null ? appStage : new Stage();
                    target.setScene(new Scene(root, 860, 540));
                    target.setTitle("EduPlay — Réinitialisation");
                    target.toFront();
                    target.show();

                    System.out.println("[ResetTokenServer] ResetPasswordView ouverte, token : "
                            + (token != null ? token.substring(0, 8) + "..." : "null"));
                    return;
                } catch (Exception e) {
                    System.err.println("[ResetTokenServer] Erreur " + path + " : " + e.getMessage());
                }
            }
        }
        System.err.println("[ResetTokenServer] reset-password.fxml introuvable.");
    }

    private static String buildSuccessPage() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>EduPlay — Redirection</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        display: flex; align-items: center; justify-content: center;
                        height: 100vh; margin: 0;
                        background: #1A1A2E; color: white;
                    }
                    .box {
                        text-align: center; padding: 40px;
                        background: #12122A; border-radius: 16px;
                        max-width: 400px;
                    }
                    h1 { color: #E94560; }
                    p  { color: #9999BB; }
                    .check { font-size: 48px; }
                </style>
                <script>
                    // Fermer l'onglet automatiquement après 2 secondes
                    setTimeout(() => window.close(), 2000);
                </script>
            </head>
            <body>
                <div class="box">
                    <div class="check">✓</div>
                    <h1>EduPlay</h1>
                    <p>L'application va s'ouvrir.<br>Vous pouvez fermer cet onglet.</p>
                </div>
            </body>
            </html>
        """;
    }
}