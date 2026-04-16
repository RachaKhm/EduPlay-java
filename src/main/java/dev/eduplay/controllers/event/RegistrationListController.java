package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.services.EventRegistrationService;
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
import java.util.List;
import java.util.stream.Collectors;

public class RegistrationListController {

    @FXML private TableView<EventRegistration> registrationTable;
    @FXML private TableColumn<EventRegistration, Integer> colId;
    @FXML private TableColumn<EventRegistration, String> colEventTitle;
    @FXML private TableColumn<EventRegistration, String> colChildName;
    @FXML private TableColumn<EventRegistration, String> colParentPhone;
    @FXML private TableColumn<EventRegistration, String> colStatus;
    @FXML private TableColumn<EventRegistration, String> colRegisteredAt;
    @FXML private TableColumn<EventRegistration, Void> colActions;
    @FXML private ComboBox<String> eventFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private TextField searchField;
    @FXML private Label countLabel;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Label pageInfo;
    @FXML private Button refreshBtn;

    private EventRegistrationService service;
    private SchoolEventService eventService;
    private ObservableList<EventRegistration> allRegistrations;
    private ObservableList<EventRegistration> filteredRegistrations;
    private ObservableList<EventRegistration> currentPageRegistrations;
    private int currentPage = 1;
    private int itemsPerPage = 10;
    private int totalPages = 1;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        System.out.println("RegistrationListController initialisé");

        service = new EventRegistrationService();
        eventService = new SchoolEventService();
        allRegistrations = FXCollections.observableArrayList();
        filteredRegistrations = FXCollections.observableArrayList();
        currentPageRegistrations = FXCollections.observableArrayList();

        setupTableColumns();
        setupActions();
        setupFilters();

        statusFilterCombo.getItems().clear();
        statusFilterCombo.getItems().addAll("Tous", "PENDING", "APPROVED", "REJECTED");
        statusFilterCombo.setValue("Tous");

        cleanExpiredRegistrations();
        loadEvents();
        loadRegistrations();
    }


    private void cleanExpiredRegistrations() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<EventRegistration> allRegs = service.recuperer();
            List<EventRegistration> expiredRegs = new ArrayList<>();
            List<String> deletedEventTitles = new ArrayList<>();

            for (EventRegistration reg : allRegs) {
                if (reg.getEvent() != null && reg.getEvent().getEndDate() != null) {
                    // Vérifier si la date de fin est passée
                    if (reg.getEvent().getEndDate().isBefore(now)) {
                        expiredRegs.add(reg);
                        deletedEventTitles.add(reg.getEvent().getTitle() + " - " + reg.getChildFullName());
                    }
                }
            }

            if (!expiredRegs.isEmpty()) {
                for (EventRegistration reg : expiredRegs) {
                    service.supprimer(reg);
                    System.out.println("🗑️ Inscription supprimée pour événement expiré: " +
                            reg.getEvent().getTitle() + " - " + reg.getChildFullName());
                }

                // Afficher une alerte avec le détail
                StringBuilder message = new StringBuilder();
                message.append(expiredRegs.size()).append(" inscription(s) supprimée(s) :\n\n");
                for (String title : deletedEventTitles) {
                    message.append("• ").append(title).append("\n");
                }
                showAlert("Nettoyage automatique", message.toString());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors du nettoyage: " + e.getMessage());
        }
    }


    @FXML
    private void forceCleanExpired() {
        cleanExpiredRegistrations();
        loadRegistrations();
        showAlert("Nettoyage", "Les inscriptions expirées ont été supprimées");
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colChildName.setCellValueFactory(new PropertyValueFactory<>("childFullName"));
        colParentPhone.setCellValueFactory(new PropertyValueFactory<>("parentPhone"));

        colRegisteredAt.setCellValueFactory(cellData -> {
            if (cellData.getValue().getRegisteredAt() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRegisteredAt().format(dateFormatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        // Colonne Événement - Version corrigée
        colEventTitle.setCellValueFactory(cellData -> {
            EventRegistration registration = cellData.getValue();
            if (registration.getEvent() != null && registration.getEvent().getTitle() != null) {
                return new javafx.beans.property.SimpleStringProperty(registration.getEvent().getTitle());
            }
            return new javafx.beans.property.SimpleStringProperty("Chargement...");
        });

        // Statut avec couleur
        colStatus.setCellFactory(col -> new TableCell<EventRegistration, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "APPROVED":
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "PENDING":
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "REJECTED":
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                    }
                }
            }
        });
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Colonne des actions
        colActions.setCellFactory(col -> new TableCell<EventRegistration, Void>() {
            private final Button voirBtn = new Button("👁️ Voir");
            private final Button modifierBtn = new Button("✏️ Modifier");
            private final HBox container = new HBox(8, voirBtn, modifierBtn);

            {
                String btnStyle = "-fx-padding: 6 12; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;";
                voirBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;" + btnStyle);
                modifierBtn.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white;" + btnStyle);
                container.setAlignment(javafx.geometry.Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    EventRegistration registration = getTableView().getItems().get(getIndex());
                    voirBtn.setOnAction(e -> {
                        System.out.println("Voir inscription ID: " + registration.getId());
                        voirInscription(registration);
                    });
                    modifierBtn.setOnAction(e -> {
                        System.out.println("Modifier inscription ID: " + registration.getId());
                        modifierInscription(registration);
                    });
                    setGraphic(container);
                }
            }
        });
    }

    private void setupActions() {
        prevBtn.setOnAction(e -> pagePrecedente());
        nextBtn.setOnAction(e -> pageSuivante());
        refreshBtn.setOnAction(e -> refreshManually());
        eventFilterCombo.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterRegistrations();
        });

        statusFilterCombo.valueProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterRegistrations();
        });

        searchField.textProperty().addListener((obs, old, newVal) -> {
            currentPage = 1;
            filterRegistrations();
        });
    }

    @FXML
    private void refreshManually() {
        System.out.println("🔄 Rafraîchissement manuel de la liste des inscriptions...");
        cleanExpiredRegistrations();
        loadRegistrations();
        showAlert("Rafraîchissement", "✅ La liste des inscriptions a été actualisée");
    }

    private void setupFilters() {
        eventFilterCombo.getItems().clear();
        eventFilterCombo.getItems().add("Tous les événements");
    }

    private void loadEvents() {
        try {
            List<SchoolEvent> events = eventService.recuperer();
            for (SchoolEvent event : events) {
                eventFilterCombo.getItems().add(event.getTitle());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRegistrations() {
        try {
            // ✅ Nettoie avant de charger
            cleanExpiredRegistrations();

            List<EventRegistration> registrations = service.recuperer();
            allRegistrations.setAll(registrations);
            filterRegistrations();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les inscriptions: " + e.getMessage());
        }
    }

    private void filterRegistrations() {
        String searchText = searchField.getText().toLowerCase();
        String selectedEvent = eventFilterCombo.getValue();
        String selectedStatus = statusFilterCombo.getValue();

        List<EventRegistration> filtered = allRegistrations.stream()
                .filter(r -> searchText.isEmpty() ||
                        r.getChildFullName().toLowerCase().contains(searchText) ||
                        (r.getParentPhone() != null && r.getParentPhone().contains(searchText)))
                .filter(r -> selectedEvent == null ||
                        "Tous les événements".equals(selectedEvent) ||
                        (r.getEvent() != null && r.getEvent().getTitle() != null &&
                                r.getEvent().getTitle().equals(selectedEvent)))
                .filter(r -> selectedStatus == null ||
                        "Tous".equals(selectedStatus) ||
                        r.getStatus().equals(selectedStatus))
                .collect(Collectors.toList());

        filteredRegistrations.setAll(filtered);
        countLabel.setText(filtered.size() + " inscription(s)");
        updatePagination();
    }

    private void updatePagination() {
        if (filteredRegistrations.isEmpty()) {
            registrationTable.setItems(FXCollections.observableArrayList());
            pageInfo.setText("Aucune inscription");
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            return;
        }

        totalPages = (int) Math.ceil((double) filteredRegistrations.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, filteredRegistrations.size());

        currentPageRegistrations.setAll(filteredRegistrations.subList(start, end));
        registrationTable.setItems(currentPageRegistrations);

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

    private void voirInscription(EventRegistration registration) {
        Router.go("registration_detail", registration);  // Passe l'objet, pas l'ID
    }

    private void modifierInscription(EventRegistration registration) {
        Router.go("edit_registration", registration.getId());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



}