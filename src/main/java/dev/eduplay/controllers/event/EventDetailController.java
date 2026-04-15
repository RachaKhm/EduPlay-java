package dev.eduplay.controllers.event;

import dev.eduplay.controllers.event.MainController;
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
    private MainController mainController;
    private SchoolEvent currentEvent;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        service = new SchoolEventService();
        setupActions();
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setEvent(SchoolEvent event) {
        this.currentEvent = event;
        System.out.println("=== Chargement de l'événement ===");
        System.out.println("Titre: " + event.getTitle());
        System.out.println("ImagePath: " + event.getImagePath());
        displayEventDetails();
    }

    private void setupActions() {
        backBtn.setOnAction(e -> goBack());
        editBtn.setOnAction(e -> goToEdit());
        deleteBtn.setOnAction(e -> deleteEvent());
        resourcesBtn.setOnAction(e -> goToResources());
    }

    private void displayEventDetails() {
        if (currentEvent == null) return;

        // Titre
        eventTitleLabel.setText("📌 " + currentEvent.getTitle());
        titleValue.setText(currentEvent.getTitle());

        // Lieu
        locationValue.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "Non spécifié");

        // Dates
        if (currentEvent.getStartDate() != null) {
            startDateValue.setText(currentEvent.getStartDate().format(dateFormatter));
        }
        if (currentEvent.getEndDate() != null) {
            endDateValue.setText(currentEvent.getEndDate().format(dateFormatter));
        }
        if (currentEvent.getCreatedAt() != null) {
            createdAtValue.setText(currentEvent.getCreatedAt().format(dateFormatter));
        }

        // Description
        descriptionValue.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "Aucune description");

        // Latitude/Longitude
        if (currentEvent.getLatitude() != null && currentEvent.getLongitude() != null &&
                !currentEvent.getLatitude().isEmpty() && !currentEvent.getLongitude().isEmpty()) {
            latitudeValue.setText(currentEvent.getLatitude());
            longitudeValue.setText(currentEvent.getLongitude());
            locationBox.setVisible(true);
            locationBox.setManaged(true);
        }

        // Image
        loadImage();
    }

    private void loadImage() {
        String imageName = currentEvent.getImagePath();
        System.out.println("Nom de l'image: " + imageName);

        if (imageName != null && !imageName.isEmpty()) {
            // Chemin fixe vers le dossier des images
            File imageFile = new File("uploads/events/" + imageName);
            System.out.println("Chemin complet: " + imageFile.getAbsolutePath());

            if (imageFile.exists()) {
                try {
                    Image image = new Image(imageFile.toURI().toString());
                    eventImageView.setImage(image);
                    eventImageView.setVisible(true);
                    noImageLabel.setVisible(false);
                    System.out.println("✅ Image chargée!");
                } catch (Exception e) {
                    System.err.println("Erreur: " + e.getMessage());
                    showNoImage();
                }
            } else {
                System.out.println("❌ Fichier non trouvé");
                showNoImage();
            }
        } else {
            showNoImage();
        }
    }

    public void updateImage(String newImagePath) {
        if (newImagePath != null && !newImagePath.isEmpty()) {
            currentEvent.setImagePath(newImagePath);
            loadImage();
        }
    }

    private void showNoImage() {
        eventImageView.setImage(null);
        eventImageView.setVisible(false);
        noImageLabel.setText("🖼️ Aucune image disponible");
        noImageLabel.setVisible(true);
    }

    private void goBack() {
        if (mainController != null) {
            mainController.goToEventList();
        }
    }

    private void goToEdit() {
        if (mainController != null && currentEvent != null) {
            mainController.goToEditEvent(currentEvent.getId());
        }
    }

    private void deleteEvent() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + currentEvent.getTitle() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(currentEvent);
                showAlert("Succès", "Événement supprimé avec succès");
                goBack();
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer l'événement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void goToResources() {
        if (mainController != null && currentEvent != null) {
            mainController.goToEventResources(currentEvent.getId(), currentEvent.getTitle());
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