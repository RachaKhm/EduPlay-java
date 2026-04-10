package dev.eduplay.mains;

import dev.eduplay.entities.SchoolEvent;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class MainFX extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AddEvent.fxml"));

    }
}
