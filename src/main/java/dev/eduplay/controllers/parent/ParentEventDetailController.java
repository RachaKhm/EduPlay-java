package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventResource;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.EventResourceService;
import dev.eduplay.services.SchoolEventService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ParentEventDetailController {

    @FXML private Button backBtn;
    @FXML private Button registerBtn;
    @FXML private ImageView eventImageView;
    @FXML private Label titleLabel;
    @FXML private Label startDateLabel;
    @FXML private Label endDateLabel;
    @FXML private Label locationLabel;
    @FXML private Label descriptionLabel;
    @FXML private VBox resourcesContainer;
    @FXML private VBox checklistContainer;
    @FXML private VBox planningContainer;

    private SchoolEventService eventService;
    private EventResourceService resourceService;
    private SchoolEvent currentEvent;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("ParentEventDetailController initialisé");
        eventService = new SchoolEventService();
        resourceService = new EventResourceService();

        backBtn.setOnAction(e -> Router.go("parent_event_list"));
        registerBtn.setOnAction(e -> Router.go("parent_registration_form", currentEvent.getId()));
    }

    public void setEventId(int eventId) {
        System.out.println("setEventId appelé avec ID: " + eventId);
        try {
            currentEvent = eventService.recupererParId(eventId);
            if (currentEvent != null) {
                System.out.println("Événement trouvé: " + currentEvent.getTitle());
                displayEventDetails();
                loadResources();
            } else {
                System.out.println("Événement non trouvé pour l'ID: " + eventId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayEventDetails() {
        titleLabel.setText(currentEvent.getTitle());
        descriptionLabel.setText(currentEvent.getDescription() != null ? currentEvent.getDescription() : "Aucune description");
        locationLabel.setText("📍 " + (currentEvent.getLocation() != null ? currentEvent.getLocation() : "Lieu non spécifié"));

        if (currentEvent.getStartDate() != null) {
            startDateLabel.setText("📅 Début: " + currentEvent.getStartDate().format(dateFormatter));
        }
        if (currentEvent.getEndDate() != null) {
            endDateLabel.setText("🏁 Fin: " + currentEvent.getEndDate().format(dateFormatter));
        }

        // Charger l'image
        String imagePath = currentEvent.getImagePath();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imageFile = new File("uploads/events/" + imagePath);
            if (imageFile.exists()) {
                try {
                    eventImageView.setImage(new Image(imageFile.toURI().toString()));
                } catch (Exception e) {
                    System.err.println("Erreur chargement image: " + e.getMessage());
                }
            }
        }
    }

    private void loadResources() {
        try {
            List<EventResource> resources = resourceService.recupererParEventId(currentEvent.getId());
            System.out.println("Nombre de ressources chargées: " + resources.size());

            // Filtrer par type
            List<EventResource> mainResources = resources.stream()
                    .filter(r -> r.getType().equals("DOCUMENT") || r.getType().equals("LIEN") || r.getType().equals("VIDEO"))
                    .toList();

            EventResource checklist = resources.stream()
                    .filter(r -> r.getType().equals("CHECKLIST"))
                    .findFirst().orElse(null);

            EventResource planning = resources.stream()
                    .filter(r -> r.getType().equals("PLANNING"))
                    .findFirst().orElse(null);

            displayMainResources(mainResources);
            displayChecklist(checklist);
            displayPlanning(planning);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayMainResources(List<EventResource> resources) {
        resourcesContainer.getChildren().clear();
        if (resources.isEmpty()) {
            Label emptyLabel = new Label("Aucune ressource disponible");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            resourcesContainer.getChildren().add(emptyLabel);
            return;
        }

        for (EventResource r : resources) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 12;");

            Label title = new Label(r.getTitle());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            if (r.getContext() != null && !r.getContext().isEmpty()) {
                Label context = new Label(r.getContext());
                context.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
                context.setWrapText(true);
                card.getChildren().add(context);
            }

            Button openBtn = new Button("Ouvrir");
            openBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20; -fx-cursor: hand;");
            openBtn.setOnAction(e -> {
                if (r.getUrl() != null && !r.getUrl().isEmpty()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(r.getUrl()));
                    } catch (Exception ex) {
                        System.err.println("Erreur ouverture URL: " + ex.getMessage());
                    }
                }
            });

            card.getChildren().addAll(title, openBtn);
            resourcesContainer.getChildren().add(card);
        }
    }

    private void displayChecklist(EventResource checklist) {
        checklistContainer.getChildren().clear();
        if (checklist == null || checklist.getContext() == null) {
            Label emptyLabel = new Label("Aucune checklist disponible");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            checklistContainer.getChildren().add(emptyLabel);
            return;
        }

        String[] lines = checklist.getContext().split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                CheckBox checkBox = new CheckBox(trimmed);
                checkBox.setStyle("-fx-font-size: 13px;");
                checklistContainer.getChildren().add(checkBox);
            }
        }
    }

    private void displayPlanning(EventResource planning) {
        planningContainer.getChildren().clear();
        if (planning == null || planning.getContext() == null) {
            Label emptyLabel = new Label("Aucun planning disponible");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 13px;");
            planningContainer.getChildren().add(emptyLabel);
            return;
        }

        String[] lines = planning.getContext().split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                Label timeLabel = new Label(trimmed);
                timeLabel.setStyle("-fx-font-size: 13px; -fx-padding: 4 0;");
                planningContainer.getChildren().add(timeLabel);
            }
        }
    }
}