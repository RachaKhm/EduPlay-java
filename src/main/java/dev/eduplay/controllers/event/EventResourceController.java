package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventResource;
import dev.eduplay.services.EventResourceService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class EventResourceController {

    // FXML COMPOSANTS
    @FXML private Button backBtn;
    @FXML private Button addResourceBtn;
    @FXML private Label eventTitleLabel;
    @FXML private Label eventSubtitleLabel;
    @FXML private Label resourceCountLabel;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> typeFilterCombo;
    @FXML private TableView<EventResource> resourceTable;
    @FXML private TableColumn<EventResource, String> colType;
    @FXML private TableColumn<EventResource, String> colTitle;
    @FXML private TableColumn<EventResource, String> colCreatedAt;
    @FXML private TableColumn<EventResource, Void> colActions;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;

    private EventResourceService service;
    private int eventId;
    private String eventTitle;
    private ObservableList<EventResource> allResources;
    private ObservableList<EventResource> filteredResources;
    private ObservableList<EventResource> currentPageResources;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("EventResourceController initialisé");

        service = new EventResourceService();
        allResources = FXCollections.observableArrayList();
        filteredResources = FXCollections.observableArrayList();
        currentPageResources = FXCollections.observableArrayList();

        setupTableColumns();
        setupActions();
        setupFilters();

        typeFilterCombo.setValue("Tous");
    }

    public void setEventId(int eventId, String eventTitle) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        eventTitleLabel.setText("📚 " + eventTitle);
        loadResources();
    }

    private void setupTableColumns() {
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTitle.setCellValueFactory(new PropertyValueFactory<>("title"));

        colCreatedAt.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getCreatedAt().format(dateFormatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        colActions.setCellFactory(col -> new TableCell<EventResource, Void>() {
            private final Button voirBtn = new Button("👁️ Voir");
            private final Button modifierBtn = new Button("✏️ Modifier");
            private final Button supprimerBtn = new Button("🗑️ Supprimer");
            private final HBox container = new HBox(8, voirBtn, modifierBtn, supprimerBtn);

            {
                String btnStyle = "-fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
                voirBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #374151;" + btnStyle);
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
                    EventResource resource = getTableView().getItems().get(getIndex());
                    voirBtn.setOnAction(e -> voirRessource(resource));
                    modifierBtn.setOnAction(e -> modifierRessource(resource));
                    supprimerBtn.setOnAction(e -> supprimerRessource(resource));
                    setGraphic(container);
                }
            }
        });
    }

    private void setupActions() {
        backBtn.setOnAction(e -> goBack());
        addResourceBtn.setOnAction(e -> goToAddResource());
        prevBtn.setOnAction(e -> pagePrecedente());
        nextBtn.setOnAction(e -> pageSuivante());
    }

    private void setupFilters() {
        typeFilterCombo.getItems().clear();
        typeFilterCombo.getItems().addAll("Tous", "VIDEO", "DOCUMENT", "LIEN", "CHECKLIST", "PLANNING");
        typeFilterCombo.setValue("Tous");

        searchField.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterResources();
        });

        typeFilterCombo.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterResources();
        });
    }

    private void loadResources() {
        try {
            List<EventResource> resources = service.recupererParEventId(eventId);
            allResources.setAll(resources);
            filterResources();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les ressources: " + e.getMessage());
        }
    }

    private void filterResources() {
        String searchText = searchField.getText().toLowerCase();
        String typeFilter = typeFilterCombo.getValue();

        List<EventResource> filtered = allResources.stream()
                .filter(r -> searchText.isEmpty() || r.getTitle().toLowerCase().contains(searchText))
                .filter(r -> "Tous".equals(typeFilter) || r.getType().equals(typeFilter))
                .collect(Collectors.toList());

        filteredResources.setAll(filtered);
        resourceCountLabel.setText(filtered.size() + " ressource(s)");
        updatePagination();
    }

    private void updatePagination() {
        if (filteredResources.isEmpty()) {
            resourceTable.setItems(FXCollections.observableArrayList());
            pageInfo.setText("Aucune ressource");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            return;
        }

        totalPages = (int) Math.ceil((double) filteredResources.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredResources.size());

        currentPageResources.setAll(filteredResources.subList(start, end));
        resourceTable.setItems(currentPageResources);

        pageInfo.setText("Page " + currentPage + " sur " + totalPages);
        prevBtn.setDisable(currentPage == 1);
        nextBtn.setDisable(currentPage == totalPages);
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

    // ✅ CORRIGÉ : Utilise Router au lieu de mainController
    private void voirRessource(EventResource resource) {
        Router.go("resource_detail", eventId, eventTitle, resource);
    }

    // ✅ CORRIGÉ : Utilise Router au lieu de mainController
    private void modifierRessource(EventResource resource) {
        Router.go("add_resource", eventId, eventTitle, resource);
    }

    private void supprimerRessource(EventResource resource) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la ressource");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer la ressource \"" + resource.getTitle() + "\" ?");

        if (confirm.showAndWait().get() == ButtonType.OK) {
            try {
                service.supprimer(resource);
                loadResources();
                showAlert("Succès", "Ressource supprimée avec succès");
            } catch (SQLException e) {
                showAlert("Erreur", "Impossible de supprimer la ressource: " + e.getMessage());
            }
        }
    }

    // ✅ CORRIGÉ : Utilise Router au lieu de mainController
    private void goBack() {
        Router.go("event_detail", eventId);
    }

    // ✅ CORRIGÉ : Utilise Router au lieu de mainController
    private void goToAddResource() {
        Router.go("add_resource", eventId, eventTitle);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}