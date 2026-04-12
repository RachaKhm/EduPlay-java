package dev.eduplay.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {

        // Charger la vue de connexion
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/auth/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 860, 540);

        // Charger le CSS global (app.css dans resources/styles/)
        try {
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource("/styles/app.css")
                    ).toExternalForm());
        } catch (NullPointerException e) {
            System.out.println("app.css non trouvé — styles inline utilisés.");
        }

        primaryStage.setTitle("EduPlay — Connexion");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(760);
        primaryStage.setMinHeight(480);
        primaryStage.centerOnScreen();

        // Icône application (optionnel)
        try {
            primaryStage.getIcons().add(
                    new Image(Objects.requireNonNull(
                            getClass().getResourceAsStream("/styles/icon.png"))));
        } catch (Exception ignored) {}

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}