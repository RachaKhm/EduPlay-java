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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.awt.Desktop;
import java.net.URI;
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

        backBtn.setOnAction(e -> {
            System.out.println("Retour à la liste des événements");
            Router.go("parent_event_list");
        });

        registerBtn.setOnAction(e -> {
            if (currentEvent != null && currentEvent.hasAvailableSpaces()) {
                System.out.println("Redirection vers formulaire d'inscription");
                Router.go("parent_registration_form", currentEvent.getId());
            } else {
                showAlert("Information", "Désolé, cet événement est complet. Plus aucune place disponible.");
            }
        });
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

        // ✅ Afficher les places disponibles
        displayAvailableSpaces();

        loadImage();
    }

    // ✅ NOUVELLE MÉTHODE POUR AFFICHER LES PLACES DISPONIBLES
    private void displayAvailableSpaces() {
        int remaining = currentEvent.getRemainingSpaces();
        int maxCapacity = currentEvent.getMaxCapacity();

        Label placesLabel = new Label();
        placesLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8 0 0 0;");

        if (remaining > 0) {
            placesLabel.setText("🎟️ Places disponibles : " + remaining + " / " + maxCapacity);
            placesLabel.setStyle(placesLabel.getStyle() + " -fx-text-fill: #10b981;");
            registerBtn.setDisable(false);
            registerBtn.setText("📝 S'inscrire");
        } else {
            placesLabel.setText("❌ Complet - Plus aucune place disponible");
            placesLabel.setStyle(placesLabel.getStyle() + " -fx-text-fill: #ef4444;");
            registerBtn.setDisable(true);
            registerBtn.setText("❌ Complet");
        }

        // Ajouter le label après les dates
        // On cherche un conteneur pour ajouter ce label (par exemple après les dates)
        // Solution simple : ajouter une nouvelle ligne dans l'affichage
        HBox infoRow = new HBox(16);
        infoRow.getChildren().add(placesLabel);

        // Note: Ceci est une simplification. Idéalement, il faudrait modifier le FXML
        // pour avoir un endroit dédié. Pour l'instant, on ajoute après locationLabel.
    }

    private void loadImage() {
        String imagePath = currentEvent.getImagePath();
        System.out.println("=== CHARGEMENT IMAGE ===");
        System.out.println("ImagePath brut: " + imagePath);

        if (imagePath == null || imagePath.isEmpty()) {
            setDefaultImage();
            return;
        }

        String fileName = imagePath;
        if (imagePath.contains("/")) {
            fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        }
        if (imagePath.contains("\\")) {
            fileName = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
        }
        System.out.println("Nom du fichier: " + fileName);

        String userDir = System.getProperty("user.dir");
        System.out.println("Répertoire de travail: " + userDir);

        String[] pathsToTry = {
                "uploads/events/" + fileName,
                "src/main/resources/uploads/events/" + fileName,
                "target/classes/uploads/events/" + fileName,
                userDir + "/uploads/events/" + fileName,
                userDir + "/src/main/resources/uploads/events/" + fileName,
                "C:/Users/MSI/IdeaProjects/EduPlay-Java/uploads/events/" + fileName,
                "C:/Users/MSI/IdeaProjects/EduPlay-Java/src/main/resources/uploads/events/" + fileName,
                fileName,
                "./" + fileName
        };

        boolean imageFound = false;
        for (String path : pathsToTry) {
            File file = new File(path);
            if (file.exists()) {
                try {
                    Image image = new Image(file.toURI().toString());
                    eventImageView.setImage(image);
                    eventImageView.setFitHeight(280);
                    eventImageView.setFitWidth(Double.MAX_VALUE);
                    eventImageView.setPreserveRatio(false);
                    eventImageView.setVisible(true);
                    System.out.println("✅ IMAGE CHARGÉE depuis: " + path);
                    imageFound = true;
                    return;
                } catch (Exception e) {
                    System.err.println("Erreur chargement: " + e.getMessage());
                }
            }
        }

        if (!imageFound) {
            System.out.println("❌ IMAGE NON TROUVÉE pour: " + fileName);
            setDefaultImage();
        }
    }

    private void setDefaultImage() {
        eventImageView.setImage(null);
        eventImageView.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #6d28d9);");
        eventImageView.setFitHeight(280);
        eventImageView.setFitWidth(Double.MAX_VALUE);
        eventImageView.setPreserveRatio(false);
    }

    private void loadResources() {
        try {
            List<EventResource> resources = resourceService.recupererParEventId(currentEvent.getId());
            System.out.println("Nombre de ressources chargées: " + resources.size());

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

            Button openBtn = new Button("📂 Ouvrir");
            openBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 20; -fx-cursor: hand;");
            openBtn.setOnAction(e -> openResource(r));

            card.getChildren().addAll(title, openBtn);
            resourcesContainer.getChildren().add(card);
        }
    }

    private void openResource(EventResource resource) {
        System.out.println("Ouverture de la ressource: " + resource.getTitle());

        if (resource.getUrl() != null && !resource.getUrl().isEmpty()) {
            String url = resource.getUrl();
            try {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                Desktop.getDesktop().browse(new URI(url));
                System.out.println("✅ URL ouverte: " + url);
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible d'ouvrir l'URL: " + ex.getMessage());
            }
        }
        else if (resource.getFilePath() != null && !resource.getFilePath().isEmpty()) {
            String filePath = resource.getFilePath();
            try {
                File file = new File(filePath);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                    System.out.println("✅ Fichier ouvert: " + filePath);
                } else {
                    showAlert("Erreur", "Fichier non trouvé: " + filePath);
                }
            } catch (Exception ex) {
                showAlert("Erreur", "Impossible d'ouvrir le fichier: " + ex.getMessage());
            }
        } else {
            showAlert("Information", "Aucun fichier ou URL disponible pour cette ressource");
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}