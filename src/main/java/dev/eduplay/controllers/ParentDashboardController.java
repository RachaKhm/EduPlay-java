package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Dashboard Parent
 * Remplacer par le vrai controller du module Événements/Enfants
 */

public class ParentDashboardController {
    @FXML private Label subtitleLabel;
    @FXML private Label totalChildrenLabel;
    @FXML private Label totalEventsLabel;

    @FXML
    public void initialize() {
        if (subtitleLabel != null)
            subtitleLabel.setText("Bienvenue, " + AppContext.getFullName());
        // TODO: charger depuis ChildService / EventService
        if (totalChildrenLabel != null) totalChildrenLabel.setText("0");
        if (totalEventsLabel   != null) totalEventsLabel.setText("0");
    }
}
