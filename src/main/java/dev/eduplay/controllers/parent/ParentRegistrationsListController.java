package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.User;
import dev.eduplay.services.EventRegistrationService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParentRegistrationsListController {

    @FXML private Button refreshBtn;
    @FXML private Button backToEventsBtn;
    @FXML private ComboBox<String> filterCombo;  // ✅ AJOUTÉ
    @FXML private VBox registrationsContainer;
    @FXML private Label emptyLabel;

    private EventRegistrationService service;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        System.out.println("ParentRegistrationsListController initialisé");
        service = new EventRegistrationService();
        backToEventsBtn.setOnAction(e -> Router.go("parent_event_list"));
        refreshBtn.setOnAction(e -> refreshList());

        // ✅ Initialiser le filtre
        filterCombo.getItems().clear();
        filterCombo.getItems().addAll("Toutes", "À venir", "Passées");
        filterCombo.setValue("Toutes");
        filterCombo.valueProperty().addListener((obs, old, newVal) -> loadRegistrations());

        // ✅ Rafraîchissement automatique toutes les 30 secondes
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            System.out.println("🔄 Rafraîchissement automatique des inscriptions");
            loadRegistrations();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        loadRegistrations();
    }

    // ✅ Méthode pour arrêter le rafraîchissement (appeler lors de la fermeture)
    public void shutdown() {
        if (autoRefresh != null) {
            autoRefresh.stop();
        }
    }

    private void refreshList() {
        System.out.println("🔄 Rafraîchissement manuel des inscriptions...");
        loadRegistrations();
        showAlert("Rafraîchissement", "✅ La liste a été actualisée");
    }

    private void loadRegistrations() {
        registrationsContainer.getChildren().clear();

        try {
            User currentUser = AppContext.getCurrentUser();
            if (currentUser == null) return;

            List<EventRegistration> registrations = service.recupererParParentId(currentUser.getId());

            // ✅ Filtrer selon la sélection
            String filter = filterCombo.getValue();
            LocalDateTime now = LocalDateTime.now();

            List<EventRegistration> filtered = registrations.stream()
                    .filter(r -> {
                        if ("À venir".equals(filter)) {
                            return r.getEvent() != null && r.getEvent().getEndDate() != null &&
                                    r.getEvent().getEndDate().isAfter(now);
                        } else if ("Passées".equals(filter)) {
                            return r.getEvent() != null && r.getEvent().getEndDate() != null &&
                                    r.getEvent().getEndDate().isBefore(now);
                        }
                        return true; // "Toutes"
                    })
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                emptyLabel.setVisible(true);
                emptyLabel.setManaged(true);
                return;
            }

            emptyLabel.setVisible(false);
            emptyLabel.setManaged(false);

            for (EventRegistration r : filtered) {
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

        String dateStr = r.getEvent() != null && r.getEvent().getStartDate() != null ?
                r.getEvent().getStartDate().format(dateFormatter) : "N/A";
        Label dates = new Label("📅 " + dateStr);
        dates.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        boolean canCancel = r.getEvent() != null && r.getEvent().getEndDate() != null &&
                r.getEvent().getEndDate().isAfter(LocalDateTime.now());

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> cancelRegistration(r));
        cancelBtn.setDisable(!canCancel);

        Button voirBtn = new Button("👁️ Voir détails");
        voirBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20; -fx-cursor: hand;");
        voirBtn.setOnAction(e -> Router.go("parent_registration_detail", r.getId()));

        HBox bottomRow = new HBox(16);
        bottomRow.getChildren().addAll(voirBtn, cancelBtn);
        bottomRow.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

        card.getChildren().addAll(eventTitle, childName, dates, bottomRow);
        return card;
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
                showAlert("Erreur", "Impossible d'annuler l'inscription: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}