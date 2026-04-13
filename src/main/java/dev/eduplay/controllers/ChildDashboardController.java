package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Dashboard Enfant
 * Remplacer par le vrai controller du module Jeux/Cours
 */
public class ChildDashboardController {
    @FXML private Label subtitleLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalGamesLabel;

    @FXML
    public void initialize() {
        if (subtitleLabel != null)
            subtitleLabel.setText("Bonjour " + AppContext.getFullName() + " !");
        // TODO: charger depuis CourseService / GameService
        if (totalCoursesLabel != null) totalCoursesLabel.setText("0");
        if (totalGamesLabel   != null) totalGamesLabel.setText("0");
    }
}