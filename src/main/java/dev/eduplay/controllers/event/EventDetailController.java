package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import dev.eduplay.tools.ImageLoader;  // ← AJOUTER CET IMPORT
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class EventDetailController {

    @FXML private Button backBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button resourcesBtn;
    @FXML private Label titleValue;
    @FXML private Label startDateValue;
    @FXML private Label endDateValue;
    @FXML private Label locationValue;
    @FXML private Label descriptionValue;
    @FXML private Label capacityValue;
    @FXML private Label registrationsCountValue;
    @FXML private Label createdAtValue;
    @FXML private Label latitudeValue;
    @FXML private Label longitudeValue;
    @FXML private VBox locationBox;
    @FXML private ImageView eventImageView;

    private SchoolEventService eventService;
    private SchoolEvent currentEvent;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        eventService = new SchoolEventService();

        backBtn.setOnAction(e -> Router.go("event_list"));
        editBtn.setOnAction(e -> Router.go("edit_event", currentEvent));
        deleteBtn.setOnAction(e -> deleteEvent());
        resourcesBtn.setOnAction(e -> Router.go("event_resource", currentEvent.getId(), currentEvent.getTitle()));
    }

    public void setEventId(int eventId) {
        try {
            currentEvent = eventService.recupererParId(eventId);
            if (currentEvent != null) {
                displayEventDetails();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setEvent(SchoolEvent event) {
        this.currentEvent = event;
        displayEventDetails();
    }

    private void displayEventDetails() {
        if (currentEvent == null) return;

        titleValue.setText(currentEvent.getTitle());
        descriptionValue.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "Aucune description");
        locationValue.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "Lieu non spécifié");

        if (currentEvent.getStartDate() != null) {
            startDateValue.setText(currentEvent.getStartDate().format(formatter));
        }
        if (currentEvent.getEndDate() != null) {
            endDateValue.setText(currentEvent.getEndDate().format(formatter));
        }
        if (currentEvent.getCreatedAt() != null) {
            createdAtValue.setText(currentEvent.getCreatedAt().format(formatter));
        }

        int remaining = currentEvent.getMaxCapacity() - currentEvent.getCurrentRegistrations();
        registrationsCountValue.setText(currentEvent.getCurrentRegistrations() + " / " + currentEvent.getMaxCapacity());
        capacityValue.setText("Capacité: " + currentEvent.getMaxCapacity() + " places");

        // Afficher les coordonnées si disponibles
        if ((currentEvent.getLatitude() != null && !currentEvent.getLatitude().isEmpty()) ||
                (currentEvent.getLongitude() != null && !currentEvent.getLongitude().isEmpty())) {
            latitudeValue.setText(currentEvent.getLatitude() != null ? currentEvent.getLatitude() : "-");
            longitudeValue.setText(currentEvent.getLongitude() != null ? currentEvent.getLongitude() : "-");
            locationBox.setVisible(true);
            locationBox.setManaged(true);
        }

        // ✅ CHARGER L'IMAGE AVEC ImageLoader (comme dans ChildLibraryController)
        loadImage();
    }

    // ✅ NOUVELLE MÉTHODE : Utilise ImageLoader comme dans tes autres contrôleurs
    private void loadImage() {
        String imagePath = currentEvent.getImagePath();
        System.out.println("Chargement image: " + imagePath);

        if (imagePath != null && !imagePath.isEmpty()) {
            // Utiliser ImageLoader.load() comme dans ChildLibraryController
            javafx.scene.image.Image image = ImageLoader.load(imagePath);
            if (image != null) {
                eventImageView.setImage(image);
                eventImageView.setFitHeight(320);
                eventImageView.setFitWidth(1000);
                eventImageView.setPreserveRatio(false);
                System.out.println("✅ Image chargée avec succès");
                return;
            } else {
                System.out.println("❌ ImageLoader n'a pas pu charger l'image: " + imagePath);
            }
        }

        // Image par défaut (dégradé violet)
        setDefaultImage();
    }

    private void setDefaultImage() {
        eventImageView.setImage(null);
        eventImageView.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #6d28d9);");
    }

    private void deleteEvent() {
        // Logique de suppression
        javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer cet événement ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    eventService.supprimer(currentEvent);
                    Router.go("event_list");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}