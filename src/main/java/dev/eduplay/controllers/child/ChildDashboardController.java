package dev.eduplay.controllers.child;

import dev.eduplay.core.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ChildDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalGamesLabel;

    @FXML
    public void initialize() {
        String name = AppContext.getFullName();
        if (welcomeLabel != null) {
            welcomeLabel.setText("👋 Bonjour, " + (name != null && !name.isBlank() ? name : "Champion") + " !");
        }
        // Stats can be enriched later
        if (totalCoursesLabel != null) totalCoursesLabel.setText("—");
        if (totalGamesLabel != null) totalGamesLabel.setText("—");
    }
}
