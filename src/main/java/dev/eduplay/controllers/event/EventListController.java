package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.SchoolEventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

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
    @FXML private Button addBtn;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

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

        cleanExpiredEvents();
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

                    voirBtn.setOnAction(e -> {
                        System.out.println("Voir événement ID: " + event.getId());
                        Router.go("event_detail", event.getId());
                    });

                    ressourcesBtn.setOnAction(e -> {
                        System.out.println("Ressources événement ID: " + event.getId());
                        Router.go("event_resource", event.getId(), event.getTitle());
                    });

                    modifierBtn.setOnAction(e -> {
                        System.out.println("Modifier événement ID: " + event.getId());
                        Router.go("add_event", event.getId());
                    });

                    supprimerBtn.setOnAction(e -> supprimerEvent(event));

                    setGraphic(container);
                }
            }
        });
    }

    private void setupActions() {
        addBtn.setOnAction(e -> {
            System.out.println("Nouvel événement");
            Router.go("add_event");
        });
        prevBtn.setOnAction(e -> pagePrecedente());
        nextBtn.setOnAction(e -> pageSuivante());
    }

    private void cleanExpiredEvents() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<SchoolEvent> allEvents = service.recuperer();
            List<SchoolEvent> expiredEvents = new ArrayList<>();

            for (SchoolEvent event : allEvents) {
                if (event.getEndDate() != null && event.getEndDate().isBefore(now)) {
                    expiredEvents.add(event);
                }
            }

            if (!expiredEvents.isEmpty()) {
                for (SchoolEvent event : expiredEvents) {
                    service.supprimerAvecRessources(event);
                    System.out.println("🗑️ Événement expiré supprimé: " + event.getTitle());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du nettoyage: " + e.getMessage());
        }
    }

    private void loadEvents() {
        try {
            cleanExpiredEvents();
            List<SchoolEvent> events = service.recuperer();
            originalEvents.setAll(events);
            applyFiltersAndSort();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les événements: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyFiltersAndSort() {
        String searchLower = currentSearchText.toLowerCase().trim();

        List<SchoolEvent> filtered = originalEvents.stream()
                .filter(e -> searchLower.isEmpty() ||
                        e.getTitle().toLowerCase().contains(searchLower) ||
                        e.getLocation().toLowerCase().contains(searchLower))
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
            case "title":
                return Comparator.comparing(SchoolEvent::getTitle, Comparator.nullsLast(String::compareTo));
            case "location":
                return Comparator.comparing(SchoolEvent::getLocation, Comparator.nullsLast(String::compareTo));
            case "startDate":
                return Comparator.comparing(SchoolEvent::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "endDate":
                return Comparator.comparing(SchoolEvent::getEndDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "createdAt":
                return Comparator.comparing(SchoolEvent::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
            default:
                return Comparator.comparing(SchoolEvent::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()));
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
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'événement \"" + event.getTitle() + "\" ?\n\nToutes les ressources associées seront également supprimées.");

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