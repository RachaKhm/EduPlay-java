package dev.eduplay.controllers;

import dev.eduplay.core.AppContext;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

/**
 * Dashboard Parent
 * Affiche le nombre réel d'enfants rattachés et d'événements inscrits.
 */
public class ParentDashboardController {
    @FXML private Label subtitleLabel;
    @FXML private Label totalChildrenLabel;
    @FXML private Label totalEventsLabel;

    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        if (subtitleLabel != null)
            subtitleLabel.setText("Bienvenue, " + AppContext.getFullName());

        // Compter les enfants rattachés à ce parent
        int childrenCount = 0;
        if (AppContext.getCurrentUser() != null) {
            int parentId = AppContext.getCurrentUser().getId();
            List<User> enfants = userService.getByType("enfant");
            childrenCount = (int) enfants.stream()
                    .filter(e -> e.getParentId() == parentId)
                    .count();
        }
        if (totalChildrenLabel != null) totalChildrenLabel.setText(String.valueOf(childrenCount));

        // Compter les événements inscrits
        int eventsCount = 0;
        try {
            if (AppContext.getCurrentUser() != null) {
                EventRegistrationService ers = new EventRegistrationService();
                int currentId = AppContext.getCurrentUser().getId();
                eventsCount = (int) ers.recuperer().stream()
                        .filter(r -> r.getParent() != null && r.getParent().getId() == currentId)
                        .count();
            }
        } catch (Exception e) {
            // EventRegistrationService peut ne pas être prêt
            eventsCount = 0;
        }
        if (totalEventsLabel != null) totalEventsLabel.setText(String.valueOf(eventsCount));
    }
}
