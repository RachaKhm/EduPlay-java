package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import dev.eduplay.utils.CSVExporter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EventListController {

    @FXML private TableView<SchoolEvent> eventTable;
    @FXML private TableColumn<SchoolEvent, String> colTitle;
    @FXML private TableColumn<SchoolEvent, String> colStartDate;
    @FXML private TableColumn<SchoolEvent, String> colEndDate;
    @FXML private TableColumn<SchoolEvent, String> colLocation;
    @FXML private TableColumn<SchoolEvent, Void> colActions;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ToggleButton orderToggle;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Button addBtn;
    @FXML private Button exportBtn;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;
    @FXML private Button refreshBtn;

    private SchoolEventService service;
    private ObservableList<SchoolEvent> originalEvents;
    private ObservableList<SchoolEvent> filteredEvents;
    private ObservableList<SchoolEvent> currentPageEvents;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private String currentSearchText = "";
    private String currentSortBy = "startDate";
    private boolean currentAscending = false;
    private Timeline autoRefresh;

    @FXML
    public void initialize() {
        System.out.println("=== EventListController initialisé ===");

        service = new SchoolEventService();
        originalEvents = FXCollections.observableArrayList();
        filteredEvents = FXCollections.observableArrayList();
        currentPageEvents = FXCollections.observableArrayList();

        setupTableColumns();
        setupActions();

        sortCombo.setValue("startDate");
        orderToggle.setText("⬇️ Desc");
        orderToggle.setSelected(false);

        filterCombo.getItems().clear();
        filterCombo.getItems().addAll("Tous", "À venir", "Passés");
        filterCombo.setValue("Tous");

        // ✅ Rafraîchissement automatique toutes les 30 secondes
        autoRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            System.out.println("🔄 Rafraîchissement automatique des événements");
            loadEvents();
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        loadEvents();

        searchField.textProperty().addListener((obs, old, newVal) -> {
            currentSearchText = newVal == null ? "" : newVal;
            currentPage = 1;
            applyFiltersAndSort();
        });

        sortCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                currentSortBy = newVal;
                currentPage = 1;
                applyFiltersAndSort();
            }
        });

        orderToggle.selectedProperty().addListener((obs, old, newVal) -> {
            currentAscending = newVal;
            orderToggle.setText(currentAscending ? "⬆️ Asc" : "⬇️ Desc");
            currentPage = 1;
            applyFiltersAndSort();
        });

        filterCombo.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            applyFiltersAndSort();
        });
    }

    // ✅ Méthode pour arrêter le rafraîchissement
    public void shutdown() {
        if (autoRefresh != null) {
            autoRefresh.stop();
        }
    }

    private void setupTableColumns() {
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

        colStartDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getStartDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStartDate().format(dateFormatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colEndDate.setCellValueFactory(cellData -> {
            if (cellData.getValue().getEndDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEndDate().format(dateFormatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colActions.setCellFactory(col -> new TableCell<SchoolEvent, Void>() {
            private final Button voirBtn = new Button("Voir");
            private final Button ressourcesBtn = new Button("Ressources");
            private final Button modifierBtn = new Button("Modifier");
            private final Button supprimerBtn = new Button("Supprimer");
            private final HBox container = new HBox(8, voirBtn, ressourcesBtn, modifierBtn, supprimerBtn);

            {
                String btnStyle = "-fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
                voirBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151;" + btnStyle);
                ressourcesBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white;" + btnStyle);
                modifierBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;" + btnStyle);
                supprimerBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white;" + btnStyle);
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    SchoolEvent event = getTableView().getItems().get(getIndex());

                    voirBtn.setOnAction(e -> Router.go("event_detail", event.getId()));
                    ressourcesBtn.setOnAction(e -> Router.go("event_resource", event.getId(), event.getTitle()));
                    modifierBtn.setOnAction(e -> Router.go("edit_event", event));
                    supprimerBtn.setOnAction(e -> supprimerEvent(event));
                    setGraphic(container);
                }
            }
        });
    }

    private void setupActions() {
        addBtn.setOnAction(e -> Router.go("add_event"));
        exportBtn.setOnAction(e -> exporterEvenements());
        refreshBtn.setOnAction(e -> refreshManually());
        prevBtn.setOnAction(e -> pagePrecedente());
        nextBtn.setOnAction(e -> pageSuivante());
    }

    private void loadEvents() {
        try {
            List<SchoolEvent> events = service.recuperer();
            originalEvents.setAll(events);
            applyFiltersAndSort();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void refreshManually() {
        System.out.println("🔄 Rafraîchissement manuel des événements...");
        loadEvents();
        showAlert("Rafraîchissement", "✅ La liste des événements a été actualisée");
    }

    private void applyFiltersAndSort() {
        String searchLower = currentSearchText.toLowerCase().trim();
        String filter = filterCombo.getValue();
        LocalDateTime now = LocalDateTime.now();

        List<SchoolEvent> filtered = originalEvents.stream()
                .filter(e -> searchLower.isEmpty() ||
                        e.getTitle().toLowerCase().contains(searchLower) ||
                        e.getLocation().toLowerCase().contains(searchLower))
                .filter(e -> {
                    if ("À venir".equals(filter)) {
                        return e.getEndDate() != null && e.getEndDate().isAfter(now);
                    } else if ("Passés".equals(filter)) {
                        return e.getEndDate() != null && e.getEndDate().isBefore(now);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Comparator<SchoolEvent> comparator = getComparator(currentSortBy);
        if (comparator != null) {
            if (currentAscending) {
                filtered.sort(comparator);
            } else {
                filtered.sort(comparator.reversed());
            }
        }

        filteredEvents.setAll(filtered);
        updatePagination();
    }

    private Comparator<SchoolEvent> getComparator(String sortBy) {
        if (sortBy == null) return Comparator.comparing(SchoolEvent::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        switch (sortBy) {
            case "title": return Comparator.comparing(SchoolEvent::getTitle, Comparator.nullsLast(String::compareTo));
            case "location": return Comparator.comparing(SchoolEvent::getLocation, Comparator.nullsLast(String::compareTo));
            case "startDate": return Comparator.comparing(SchoolEvent::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "endDate": return Comparator.comparing(SchoolEvent::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdAt": return Comparator.comparing(SchoolEvent::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            default: return Comparator.comparing(SchoolEvent::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
        }
    }

    private void updatePagination() {
        if (filteredEvents.isEmpty()) {
            eventTable.setItems(FXCollections.observableArrayList());
            pageInfo.setText("Aucun événement");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            return;
        }

        totalPages = (int) Math.ceil((double) filteredEvents.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredEvents.size());

        if (start < filteredEvents.size()) {
            currentPageEvents.setAll(filteredEvents.subList(start, end));
            eventTable.setItems(currentPageEvents);
        } else {
            eventTable.setItems(FXCollections.observableArrayList());
        }

        pageInfo.setText("Page " + currentPage + " sur " + totalPages + " (" + filteredEvents.size() + " événements)");
        prevBtn.setDisable(currentPage == 1);
        nextBtn.setDisable(currentPage == totalPages || totalPages == 0);
    }

    private void pagePrecedente() {
        if (currentPage > 1) {
            currentPage--;
            updatePagination();
        }
    }

    private void pageSuivante() {
        if (currentPage < totalPages) {
            currentPage++;
            updatePagination();
        }
    }

    private void supprimerEvent(SchoolEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'événement");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + event.getTitle() + "\" ?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimerAvecRessources(event);
                loadEvents();
                showAlert("Succès", "✅ Événement supprimé avec succès");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer l'événement: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void exporterEvenements() {
        try {
            List<SchoolEvent> eventsToExport = filteredEvents.isEmpty() ? new ArrayList<>(originalEvents) : new ArrayList<>(filteredEvents);
            if (eventsToExport.isEmpty()) {
                showAlert("Export", "❌ Aucun événement à exporter");
                return;
            }
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer le fichier CSV");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            fileChooser.setInitialFileName("evenements_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            Stage stage = (Stage) exportBtn.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                String[] headers = {"ID", "Titre", "Lieu", "Date début", "Date fin", "Description"};
                List<String[]> data = new ArrayList<>();
                for (SchoolEvent event : eventsToExport) {
                    String[] row = {
                            String.valueOf(event.getId()), event.getTitle(),
                            event.getLocation() != null ? event.getLocation() : "",
                            event.getStartDate() != null ? event.getStartDate().format(dateFormatter) : "",
                            event.getEndDate() != null ? event.getEndDate().format(dateFormatter) : "",
                            event.getDescription() != null ? event.getDescription().replace("\n", " ").replace("\r", "") : ""
                    };
                    data.add(row);
                }
                CSVExporter.exporter(data, headers, file.getAbsolutePath());
                showAlert("Export réussi", "✅ " + eventsToExport.size() + " événement(s) exporté(s) vers " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "❌ Erreur lors de l'export: " + e.getMessage());
        }
    }

    public void refreshList() {
        loadEvents();
        searchField.clear();
        currentSearchText = "";
        currentPage = 1;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}