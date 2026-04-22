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

        // Initialisation minimale du Router (sans contentArea pour l'instant)
        // pour permettre la navigation via remplacement de scène si besoin.
        dev.eduplay.core.Router.init(null);

        // Charger la vue de connexion par défaut
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/views/auth/LoginView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 860, 540);

        // Charger le CSS global
        try {
            scene.getStylesheets().add(
                    Objects.requireNonNull(
                            getClass().getResource("/styles/app.css")
                    ).toExternalForm());
        } catch (Exception e) {
            System.out.println("app.css non trouvé.");
        }

        primaryStage.setTitle("EduPlay");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(860);
        primaryStage.setMinHeight(540);
        primaryStage.centerOnScreen();

        // Gestion des Deep Links passés en argument (ex: eduplay://reset-password?token=...)
        Parameters params = getParameters();
        if (!params.getRaw().isEmpty()) {
            String arg = params.getRaw().get(0);
            if (arg.startsWith("eduplay://")) {
                dev.eduplay.core.Router.handleDeepLink(arg);
            }
        }

        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
