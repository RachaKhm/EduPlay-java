package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class EventDetailController {

    @FXML private Button backBtn;
    @FXML private Button editBtn;
    @FXML private Button deleteBtn;
    @FXML private Button resourcesBtn;
    @FXML private Label eventTitleLabel;
    @FXML private Label titleValue;
    @FXML private Label locationValue;
    @FXML private Label startDateValue;
    @FXML private Label endDateValue;
    @FXML private Label createdAtValue;
    @FXML private Label latitudeValue;
    @FXML private Label longitudeValue;
    @FXML private TextArea descriptionValue;
    @FXML private ImageView eventImageView;
    @FXML private Label noImageLabel;
    @FXML private StackPane imageContainer;
    @FXML private VBox locationBox;

    private SchoolEventService service;
    private SchoolEvent currentEvent;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("=== EventDetailController initialisé ===");
        service = new SchoolEventService();
        setupActions();
    }

    public void setEventId(int eventId) {
        System.out.println("=== setEventId appelé avec ID: " + eventId);
        try {
            // ✅ Force le rechargement des données
            SchoolEvent event = service.recupererParId(eventId);
            if (event != null) {
                setEvent(event);
            } else {
                System.out.println("❌ Événement non trouvé pour l'ID: " + eventId);
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setEvent(SchoolEvent event) {
        this.currentEvent = event;
        System.out.println("=== Affichage des détails ===");
        System.out.println("Titre: " + event.getTitle());
        displayEventDetails();
    }

    private void setupActions() {
        backBtn.setOnAction(e -> {
            System.out.println("Bouton Retour cliqué");
            goBack();
        });
        editBtn.setOnAction(e -> {
            System.out.println("Bouton Modifier cliqué");
            goToEdit();
        });
        deleteBtn.setOnAction(e -> {
            System.out.println("Bouton Supprimer cliqué");
            deleteEvent();
        });
        resourcesBtn.setOnAction(e -> {
            System.out.println("Bouton Ressources cliqué");
            goToResources();
        });
    }

    private void displayEventDetails() {
        if (currentEvent == null) {
            System.out.println("❌ currentEvent est null");
            return;
        }

        eventTitleLabel.setText("📌 " + currentEvent.getTitle());
        titleValue.setText(currentEvent.getTitle());
        locationValue.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "Non spécifié");

        if (currentEvent.getStartDate() != null) {
            startDateValue.setText(currentEvent.getStartDate().format(dateFormatter));
        }
        if (currentEvent.getEndDate() != null) {
            endDateValue.setText(currentEvent.getEndDate().format(dateFormatter));
        }
        if (currentEvent.getCreatedAt() != null) {
            createdAtValue.setText(currentEvent.getCreatedAt().format(dateFormatter));
        }

        descriptionValue.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "Aucune description");

        if (currentEvent.getLatitude() != null && currentEvent.getLongitude() != null &&
                !currentEvent.getLatitude().isEmpty() && !currentEvent.getLongitude().isEmpty()) {
            latitudeValue.setText(currentEvent.getLatitude());
            longitudeValue.setText(currentEvent.getLongitude());
            locationBox.setVisible(true);
            locationBox.setManaged(true);
        }

        loadImage();
    }

    private void loadImage() {
        String imagePath = currentEvent.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File("uploads/events/" + imagePath);
            if (imageFile.exists()) {
                try {
                    Image image = new Image(imageFile.toURI().toString());
                    eventImageView.setImage(image);
                    eventImageView.setVisible(true);
                    noImageLabel.setVisible(false);
                    System.out.println("✅ Image chargée!");
                } catch (Exception e) {
                    showNoImage();
                }
            } else {
                showNoImage();
            }
        } else {
            showNoImage();
        }
    }

    private void showNoImage() {
        eventImageView.setImage(null);
        eventImageView.setVisible(false);
        noImageLabel.setText("🖼️ Aucune image disponible");
        noImageLabel.setVisible(true);
    }

    private void goBack() {
        Router.go("event_list");
    }

    private void goToEdit() {
        if (currentEvent != null) {
            Router.go("edit_event", currentEvent);
        }
    }

    private void deleteEvent() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + currentEvent.getTitle() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimerAvecRessources(currentEvent);
                showAlert("Succès", "Événement supprimé avec succès");
                goBack();
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer l'événement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void goToResources() {
        if (currentEvent != null) {
            Router.go("event_resource", currentEvent.getId(), currentEvent.getTitle());
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