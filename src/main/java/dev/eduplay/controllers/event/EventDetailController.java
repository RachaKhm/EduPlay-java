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

    // ✅ Méthode appelée par Router
    public void setEventId(int eventId) {
        System.out.println("=== setEventId appelé avec ID: " + eventId);
        try {
            SchoolEvent event = service.recupererParId(eventId);
            if (event != null) {
                System.out.println("✅ Événement trouvé: " + event.getTitle());
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
        System.out.println("Lieu: " + event.getLocation());
        System.out.println("Description: " + event.getDescription());
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

        System.out.println("=== Affichage des détails de l'événement ===");

        // Titre
        eventTitleLabel.setText("📌 " + currentEvent.getTitle());
        titleValue.setText(currentEvent.getTitle());

        // Lieu
        locationValue.setText(currentEvent.getLocation() != null ? currentEvent.getLocation() : "Non spécifié");

        // Dates
        if (currentEvent.getStartDate() != null) {
            startDateValue.setText(currentEvent.getStartDate().format(dateFormatter));
        } else {
            startDateValue.setText("Non spécifiée");
        }

        if (currentEvent.getEndDate() != null) {
            endDateValue.setText(currentEvent.getEndDate().format(dateFormatter));
        } else {
            endDateValue.setText("Non spécifiée");
        }

        if (currentEvent.getCreatedAt() != null) {
            createdAtValue.setText(currentEvent.getCreatedAt().format(dateFormatter));
        } else {
            createdAtValue.setText("Non spécifiée");
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
        } else {
            locationBox.setVisible(false);
            locationBox.setManaged(false);
        }

        // Image
        loadImage();
    }

    private void loadImage() {
        String imagePath = currentEvent.getImagePath();
        System.out.println("=== CHARGEMENT IMAGE ===");
        System.out.println("ImagePath brut: " + imagePath);

        if (imagePath == null || imagePath.isEmpty()) {
            System.out.println("❌ Aucun chemin d'image");
            showNoImage();
            return;
        }

        // Essayer différents chemins possibles
        String[] pathsToTry = {
                imagePath,  // Chemin direct de la BD
                "uploads/events/" + imagePath,  // Dossier uploads
                "src/main/resources/uploads/events/" + imagePath,  // Dans resources
                System.getProperty("user.dir") + "/uploads/events/" + imagePath,  // Depuis racine projet
                "C:/Users/MSI/IdeaProjects/EduPlay-Java/uploads/events/" + imagePath  // Chemin absolu
        };

        for (String path : pathsToTry) {
            File file = new File(path);
            System.out.println("Test: " + file.getAbsolutePath() + " - Existe: " + file.exists());

            if (file.exists()) {
                try {
                    Image image = new Image(file.toURI().toString());
                    eventImageView.setImage(image);
                    eventImageView.setVisible(true);
                    noImageLabel.setVisible(false);
                    System.out.println("✅ IMAGE CHARGÉE depuis: " + path);
                    return;
                } catch (Exception e) {
                    System.err.println("Erreur chargement: " + e.getMessage());
                }
            }
        }

        System.out.println("❌ IMAGE NON TROUVÉE après tous les essais");
        showNoImage();
    }

    private void showNoImage() {
        eventImageView.setImage(null);
        eventImageView.setVisible(false);
        noImageLabel.setText("🖼️ Aucune image disponible");
        noImageLabel.setVisible(true);
    }

    private void goBack() {
        System.out.println("goBack() - Retour à la liste");
        Router.go("event_list");
    }

    private void goToEdit() {
        System.out.println("goToEdit() - ID: " + currentEvent.getId());
        if (currentEvent != null) {
            Router.go("add_event", currentEvent.getId());
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
        System.out.println("goToResources() - ID: " + currentEvent.getId() + ", Titre: " + currentEvent.getTitle());
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