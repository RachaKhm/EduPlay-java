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

    // Labels pour les informations
    @FXML private Label titleValue;
    @FXML private Label locationValue;
    @FXML private Label startDateValue;
    @FXML private Label endDateValue;
    @FXML private Label createdAtValue;
    @FXML private Label latitudeValue;
    @FXML private Label longitudeValue;
    @FXML private Label descriptionValue;
    @FXML private Label capacityValue;
    @FXML private Label registrationsCountValue;

    // Image
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
        System.out.println("Capacité max: " + event.getMaxCapacity());
        System.out.println("Inscrits: " + event.getCurrentRegistrations());
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

        System.out.println("=== Début displayEventDetails ===");

        // Titre
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

        // ✅ Affichage de la capacité
        displayCapacity();

        // Localisation
        if (currentEvent.getLatitude() != null && currentEvent.getLongitude() != null &&
                !currentEvent.getLatitude().isEmpty() && !currentEvent.getLongitude().isEmpty() &&
                !currentEvent.getLatitude().equals("null") && !currentEvent.getLongitude().equals("null")) {
            latitudeValue.setText(currentEvent.getLatitude());
            longitudeValue.setText(currentEvent.getLongitude());
            locationBox.setVisible(true);
            locationBox.setManaged(true);
        } else {
            locationBox.setVisible(false);
            locationBox.setManaged(false);
        }

        loadImage();

        System.out.println("=== Fin displayEventDetails ===");
    }

    private void displayCapacity() {
        int maxCapacity = currentEvent.getMaxCapacity();
        int currentRegs = currentEvent.getCurrentRegistrations();
        int remaining = maxCapacity - currentRegs;

        System.out.println("Affichage capacité - Max: " + maxCapacity + ", Inscrits: " + currentRegs);

        if (capacityValue == null) {
            System.err.println("capacityValue est null !");
            return;
        }
        if (registrationsCountValue == null) {
            System.err.println("registrationsCountValue est null !");
            return;
        }

        capacityValue.setText("🎟️ Capacité: " + maxCapacity + " places");
        registrationsCountValue.setText("📊 Inscrits: " + currentRegs + " / " + maxCapacity);

        capacityValue.setVisible(true);
        registrationsCountValue.setVisible(true);

        if (remaining <= 0) {
            String style = "-fx-text-fill: #ef4444; -fx-font-weight: bold; -fx-font-size: 14px;";
            capacityValue.setStyle(style);
            registrationsCountValue.setStyle(style);
        } else if (remaining <= 10) {
            String style = "-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-font-size: 14px;";
            capacityValue.setStyle(style);
            registrationsCountValue.setStyle(style);
        } else {
            capacityValue.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold; -fx-font-size: 14px;");
            registrationsCountValue.setStyle("-fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-font-size: 14px;");
        }
    }

    private void loadImage() {
        String imagePath = currentEvent.getImagePath();
        System.out.println("Chargement image: " + imagePath);

        if (imagePath != null && !imagePath.isEmpty() && !imagePath.equals("null")) {
            String fileName = imagePath;
            if (imagePath.contains("/")) {
                fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            }
            if (imagePath.contains("\\")) {
                fileName = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
            }

            File imageFile = new File("uploads/events/" + fileName);
            System.out.println("Chemin image: " + imageFile.getAbsolutePath());

            if (imageFile.exists()) {
                try {
                    Image image = new Image(imageFile.toURI().toString());
                    eventImageView.setImage(image);
                    eventImageView.setVisible(true);
                    noImageLabel.setVisible(false);
                    System.out.println("✅ Image chargée!");
                    return;
                } catch (Exception e) {
                    System.err.println("Erreur chargement image: " + e.getMessage());
                }
            }
        }

        showNoImage();
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