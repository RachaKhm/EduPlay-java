package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class ParentEventDetailController {

    @FXML private Button backBtn;
    @FXML private Button registerBtn;
    @FXML private ImageView eventImageView;
    @FXML private Label titleLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label locationLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label placesLabel;
    @FXML private VBox resourcesContainer;
    @FXML private VBox checklistContainer;
    @FXML private VBox planningContainer;

    private SchoolEventService eventService;
    private SchoolEvent currentEvent;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("ParentEventDetailController initialisé");
        eventService = new SchoolEventService();

        backBtn.setOnAction(e -> Router.go("parent_event_list"));
        registerBtn.setOnAction(e -> {
            System.out.println("=== BOUTON CLICK ===");
            if (currentEvent != null) {
                System.out.println("Event ID: " + currentEvent.getId());
                Router.go("parent_registration_form", currentEvent.getId(), currentEvent.getTitle());
            } else {
                System.out.println("❌ currentEvent est NULL !");
                showAlert("Erreur", "Impossible de charger l'événement. Veuillez réessayer.");
            }
        });
    }

    public void setEventId(int eventId) {
        System.out.println("=== setEventId appelé avec ID: " + eventId);
        try {
            currentEvent = eventService.recupererParId(eventId);
            if (currentEvent != null) {
                System.out.println("✅ Événement chargé: " + currentEvent.getTitle());
                displayDetails();
            } else {
                System.out.println("❌ Événement non trouvé");
                showErrorMessage("Événement non trouvé");
            }
        } catch (SQLException e) {
            System.err.println("Erreur: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Erreur de chargement");
        }
    }

    private void displayDetails() {
        titleLabel.setText(currentEvent.getTitle());
        descriptionLabel.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "Aucune description");
        locationLabel.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "Lieu non spécifié");

        if (currentEvent.getStartDate() != null) {
            startDateLabel.setText(currentEvent.getStartDate().format(dateFormatter));
        }
        if (currentEvent.getEndDate() != null) {
            endDateLabel.setText(currentEvent.getEndDate().format(dateFormatter));
        }

        int placesLeft = currentEvent.getMaxCapacity() - currentEvent.getCurrentRegistrations();
        placesLabel.setText(placesLeft + " places restantes");
        placesLabel.setStyle(placesLeft > 0 ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        registerBtn.setDisable(placesLeft <= 0);

        loadImage();
    }

    private void loadImage() {
        String path = currentEvent.getImagePath();
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    eventImageView.setImage(new Image(file.toURI().toString()));
                } catch (Exception e) {}
            }
        }
    }

    private void showErrorMessage(String msg) {
        titleLabel.setText("Erreur");
        descriptionLabel.setText(msg);
        registerBtn.setDisable(true);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}