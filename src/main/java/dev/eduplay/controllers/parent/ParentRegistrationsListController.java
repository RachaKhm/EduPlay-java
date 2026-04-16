package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.User;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParentRegistrationsListController {

    @FXML private Button backToEventsBtn;
    @FXML private VBox registrationsContainer;
    @FXML private Label emptyLabel;

    private EventRegistrationService service;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        service = new EventRegistrationService();
        backToEventsBtn.setOnAction(e -> Router.go("parent_event_list"));
        loadRegistrations();
    }

    private void loadRegistrations() {
        registrationsContainer.getChildren().clear();

        try {
            User currentUser = AppContext.getCurrentUser();
            if (currentUser == null) return;

            List<EventRegistration> registrations = service.recupererParParentId(currentUser.getId());

            if (registrations.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                return;
            }

            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);

            for (EventRegistration r : registrations) {
                registrationsContainer.getChildren().add(createRegistrationCard(r));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createRegistrationCard(EventRegistration r) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 8, 0, 0, 2);");
        card.setPadding(new Insets(16));

        Label eventTitle = new Label(r.getEvent() != null ? r.getEvent().getTitle() : "Événement");
        eventTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label childName = new Label("👤 Enfant : " + r.getChildFullName());
        childName.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label dates = new Label("📅 " + (r.getEvent() != null && r.getEvent().getStartDate() != null ?
                r.getEvent().getStartDate().format(dateFormatter) : "N/A"));
        dates.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        Label statusLabel = new Label(r.getStatus());
        statusLabel.setStyle(getStatusStyle(r.getStatus()));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> cancelRegistration(r));

        HBox bottomRow = new HBox(16);
        bottomRow.getChildren().addAll(statusLabel, cancelBtn);
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        card.getChildren().addAll(eventTitle, childName, dates, bottomRow);
        return card;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "APPROVED": return "-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #d1fae5; -fx-background-radius: 20;";
            case "PENDING": return "-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #fed7aa; -fx-background-radius: 20;";
            case "REJECTED": return "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-color: #fee2e2; -fx-background-radius: 20;";
            default: return "-fx-text-fill: #6b7280; -fx-font-weight: bold;";
        }
    }

    private void cancelRegistration(EventRegistration registration) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Annuler l'inscription");
        confirm.setContentText("Êtes-vous sûr de vouloir annuler cette inscription ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(registration);
                loadRegistrations();
                showAlert("Succès", "Inscription annulée avec succès");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible d'annuler l'inscription");
                e.printStackTrace();
            }
        }
    }

    public void refreshList() {
        loadRegistrations();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}