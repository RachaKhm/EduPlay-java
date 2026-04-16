package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Dashboard Enseignant
 * Remplacer par le vrai controller du module Cours, Game, Level (Nadine w houdhaifa hottou controllekom hna)
 */

public class TeacherDashboardController {
    @FXML private Label subtitleLabel;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalStudentsLabel;

    @FXML
    public void initialize() {
        if (subtitleLabel != null)
            subtitleLabel.setText("Bienvenue, " + AppContext.getFullName());
        // TODO: charger les vraies stats depuis CourseService
        if (totalCoursesLabel  != null) totalCoursesLabel.setText("0");
        if (totalStudentsLabel != null) totalStudentsLabel.setText("0");
    }
}
