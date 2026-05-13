package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.RecommendationService;
import dev.eduplay.services.RecommendationService.EventWithScore;
import dev.eduplay.services.SchoolEventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ParentEventListController {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ToggleButton orderToggle;
    @FXML private Button resetBtn;
    @FXML private Label resultCountLabel;
    @FXML private FlowPane eventsGrid;
    @FXML private HBox paginationBox;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private SchoolEventService service;
    private RecommendationService recommendationService;
    private ObservableList<SchoolEvent> allEvents;
    private ObservableList<SchoolEvent> currentPageEvents;
    private Map<Integer, EventWithScore> recommendationScores = new HashMap<>();
    private int currentPage = 1;
    private int itemsPerPage = 6;
    private int totalPages = 1;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("ParentEventListController initialisé");

        service = new SchoolEventService();
        recommendationService = new RecommendationService();
        allEvents = FXCollections.observableArrayList();
        currentPageEvents = FXCollections.observableArrayList();

        sortCombo.getItems().clear();
        sortCombo.getItems().addAll("Recommandés", "startDate", "title", "createdAt");
        sortCombo.setValue("Recommandés");
        orderToggle.setText("⬇️ Desc");
        orderToggle.setSelected(false);

        setupListeners();
        loadEvents();
    }

    private void setupListeners() {
        searchField.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterAndDisplay();
        });
        sortCombo.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterAndDisplay();
        });
        orderToggle.selectedProperty().addListener((obs, old, newVal) -> {
            orderToggle.setText(newVal ? "⬆️ Asc" : "⬇️ Desc");
            currentPage = 1;
            filterAndDisplay();
        });
        resetBtn.setOnAction(e -> resetFilters());
        prevBtn.setOnAction(e -> previousPage());
        nextBtn.setOnAction(e -> nextPage());
    }

    private void resetFilters() {
        searchField.clear();
        sortCombo.setValue("Recommandés");
        orderToggle.setSelected(false);
        currentPage = 1;
        filterAndDisplay();
    }

    private void loadEvents() {
        try {
            List<SchoolEvent> events = service.recuperer();
            System.out.println("Nombre d'événements chargés: " + events.size());

            // Récupérer les scores de recommandation pour le parent connecté
            int parentId = AppContext.getCurrentUser().getId();
            List<EventWithScore> scoredEvents = recommendationService.getAllEventsWithScore(parentId);

            // Créer un map pour un accès rapide
            recommendationScores.clear();
            for (EventWithScore scored : scoredEvents) {
                recommendationScores.put(scored.id, scored);
            }

            allEvents.setAll(events);
            filterAndDisplay();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
        }
    }

    private void filterAndDisplay() {
        String searchText = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        String sortBy = sortCombo.getValue();
        boolean ascending = orderToggle.isSelected();

        List<SchoolEvent> filtered = allEvents.stream()
                .filter(e -> searchText.isEmpty() ||
                        e.getTitle().toLowerCase().contains(searchText) ||
                        (e.getLocation() != null && e.getLocation().toLowerCase().contains(searchText)))
                .collect(Collectors.toList());

        System.out.println("Événements filtrés: " + filtered.size());

        // Trier selon le critère choisi
        Comparator<SchoolEvent> comparator = getComparator(sortBy);
        if (comparator != null) {
            if ("Recommandés".equals(sortBy)) {
                // Tri personnalisé pour les recommandations
                filtered.sort((e1, e2) -> {
                    EventWithScore score1 = recommendationScores.get(e1.getId());
                    EventWithScore score2 = recommendationScores.get(e2.getId());

                    double s1 = score1 != null ? score1.recommendationScore : 0;
                    double s2 = score2 != null ? score2.recommendationScore : 0;

                    if (ascending) {
                        return Double.compare(s1, s2);
                    } else {
                        return Double.compare(s2, s1);
                    }
                });
            } else {
                filtered.sort(ascending ? comparator : comparator.reversed());
            }
        }

        totalPages = (int) Math.ceil((double) filtered.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filtered.size());

        currentPageEvents.clear();
        if (start < filtered.size()) {
            currentPageEvents.addAll(filtered.subList(start, end));
        }

        displayEventsGrid();
        updatePaginationControls(filtered.size());
    }

    private Comparator<SchoolEvent> getComparator(String sortBy) {
        if (sortBy == null || "Recommandés".equals(sortBy)) {
            return Comparator.comparing(e -> 0); // Géré séparément
        }

        switch (sortBy) {
            case "title":
                return Comparator.comparing(SchoolEvent::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "createdAt":
                return Comparator.comparing(SchoolEvent::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            case "startDate":
            default:
                return Comparator.comparing(SchoolEvent::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }
    }

    private void displayEventsGrid() {
        eventsGrid.getChildren().clear();

        if (currentPageEvents.isEmpty()) {
            Label emptyLabel = new Label("Aucun événement trouvé");
            emptyLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 14px; -fx-padding: 40;");
            eventsGrid.getChildren().add(emptyLabel);
            return;
        }

        double cardWidth = 300;
        eventsGrid.setPrefWrapLength(cardWidth * 3 + 40);

        for (SchoolEvent event : currentPageEvents) {
            eventsGrid.getChildren().add(createEventCard(event));
        }
    }

    private VBox createEventCard(SchoolEvent event) {
        VBox card = new VBox(10);
        card.setPrefWidth(300);
        card.setMaxWidth(300);

        // Appliquer un style différent si l'événement est recommandé
        EventWithScore scoreInfo = recommendationScores.get(event.getId());
        boolean isRecommended = scoreInfo != null && scoreInfo.isStronglyRecommended();

        if (isRecommended) {
            card.setStyle("-fx-background-color: #fefce8; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2); -fx-border-color: #fde047; -fx-border-radius: 16; -fx-border-width: 2;");
        } else {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0, 0, 2);");
        }

        card.setPadding(new Insets(0, 0, 12, 0));
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setOnMouseClicked(e -> Router.go("parent_event_detail", event.getId()));

        // Image
        ImageView imageView = new ImageView();
        imageView.setFitHeight(160);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(false);

        String imagePath = event.getImagePath();
        boolean imageLoaded = false;

        if (imagePath != null && !imagePath.isEmpty()) {
            String fileName = imagePath;
            if (imagePath.contains("/")) {
                fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            }
            if (imagePath.contains("\\")) {
                fileName = imagePath.substring(imagePath.lastIndexOf("\\") + 1);
            }

            String[] pathsToTry = {
                    "uploads/events/" + fileName,
                    System.getProperty("user.dir") + "/uploads/events/" + fileName,
                    "C:/Users/MSI/IdeaProjects/EduPlay-Java/uploads/events/" + fileName
            };

            for (String path : pathsToTry) {
                File file = new File(path);
                if (file.exists()) {
                    try {
                        imageView.setImage(new Image(file.toURI().toString()));
                        imageLoaded = true;
                        break;
                    } catch (Exception ex) {}
                }
            }
        }

        if (!imageLoaded) {
            imageView.setStyle("-fx-background-color: linear-gradient(to right, #8b5cf6, #6d28d9);");
        }

        card.getChildren().add(imageView);

        // Contenu
        VBox content = new VBox(8);
        content.setPadding(new Insets(12, 16, 16, 16));

        // En-tête avec titre et badge de recommandation
        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);

        // Badge de recommandation
        if (isRecommended && scoreInfo != null) {
            Label recommendationBadge = new Label(scoreInfo.getRecommendationLabel());
            String badgeColor = scoreInfo.getRecommendationColor();
            recommendationBadge.setStyle(
                    "-fx-background-color: " + badgeColor + ";" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-font-size: 10px;" +
                            "-fx-padding: 2 8;" +
                            "-fx-background-radius: 12;"
            );
            Tooltip.install(recommendationBadge, new Tooltip("Cet événement est recommandé pour vous !\nScore: " + (int)scoreInfo.recommendationScore + "/100"));
            headerBox.getChildren().add(recommendationBadge);
        }

        headerBox.getChildren().add(titleLabel);

        String descText = event.getDescription() != null ? event.getDescription() : "";
        if (descText.length() > 80) {
            descText = descText.substring(0, 80) + "...";
        }
        Label descLabel = new Label(descText);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");
        descLabel.setWrapText(true);

        HBox infoRow = new HBox(16);
        String dateStr = event.getStartDate() != null ?
                event.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Date non définie";
        Label dateLabel = new Label("📅 " + dateStr);
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b5cf6; -fx-font-weight: bold;");

        String locationStr = event.getLocation() != null ? event.getLocation() : "";
        if (locationStr.length() > 15) {
            locationStr = locationStr.substring(0, 15) + "...";
        }
        Label locationLabel = new Label("📍 " + locationStr);
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #8b5cf6; -fx-font-weight: bold;");
        infoRow.getChildren().addAll(dateLabel, locationLabel);

        // Places disponibles
        int placesLeft = event.getMaxCapacity() - event.getCurrentRegistrations();
        String placesText = placesLeft > 0 ? placesLeft + " places restantes" : "Complet";
        Label placesLabel = new Label("🎫 " + placesText);
        placesLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (placesLeft > 0 ? "#10b981" : "#ef4444") + "; -fx-font-weight: bold;");

        Button detailsBtn = new Button(isRecommended ? "⭐ Voir les détails →" : "Voir les détails →");
        detailsBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 25; -fx-cursor: hand;");
        detailsBtn.setOnAction(e -> Router.go("parent_event_detail", event.getId()));

        content.getChildren().addAll(headerBox, descLabel, infoRow, placesLabel, detailsBtn);
        card.getChildren().add(content);

        return card;
    }

    private void updatePaginationControls(int totalItems) {
        resultCountLabel.setText(totalItems + " résultat(s)");
        if (totalPages > 1) {
            paginationBox.setVisible(true);
            paginationBox.setManaged(true);
            pageInfo.setText("Page " + currentPage + " sur " + totalPages);
            prevBtn.setDisable(currentPage == 1);
            nextBtn.setDisable(currentPage == totalPages);
        } else {
            paginationBox.setVisible(false);
            paginationBox.setManaged(false);
        }
    }

    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            filterAndDisplay();
        }
    }

    private void nextPage() {
        if (currentPage < totalPages) {
            currentPage++;
            filterAndDisplay();
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