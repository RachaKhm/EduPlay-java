package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.BookRequest;
import dev.eduplay.services.BookRequestService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookRequestIndexController {

    @FXML private Label lblPendingCount;
    @FXML private TableView<BookRequest> tablePending;
    @FXML private TableColumn<BookRequest, String> colBookTitle;
    @FXML private TableColumn<BookRequest, String> colEnfant;
    @FXML private TableColumn<BookRequest, String> colRole;
    @FXML private TableColumn<BookRequest, String> colDate;
    @FXML private TableColumn<BookRequest, Void> colAction;

    @FXML private Label lblAllCount;
    @FXML private TableView<BookRequest> tableAll;
    @FXML private TableColumn<BookRequest, String> colAllBookTitle;
    @FXML private TableColumn<BookRequest, String> colAllEnfant;
    @FXML private TableColumn<BookRequest, String> colAllDate;
    @FXML private TableColumn<BookRequest, String> colAllStatus;

    private final BookRequestService service = new BookRequestService();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        // --- Table Pending ---
        colBookTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        colBookTitle.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label icon = new Label("📚");
                    Label title = new Label(item);
                    title.setStyle("-fx-font-weight: bold; -fx-text-fill: #1E293B;");
                    box.getChildren().addAll(icon, title);
                    setGraphic(box);
                }
            }
        });

        colEnfant.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnfantName()));
        colEnfant.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label icon = new Label("👤");
                    Label name = new Label(item);
                    name.setStyle("-fx-text-fill: #475569;");
                    box.getChildren().addAll(icon, name);
                    setGraphic(box);
                }
            }
        });

        colRole.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnfantRole()));
        colRole.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.setStyle("-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 10px; -fx-font-weight: bold;");
                    setGraphic(badge);
                }
            }
        });

        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRequestedAt().format(formatter)));

        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("+ Ajouter ce livre");
            {
                btn.setStyle("-fx-background-color: #F97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6 12;");
                btn.setOnAction(event -> {
                    BookRequest req = getTableView().getItems().get(getIndex());
                    Router.reload("admin_resource_form", req.getBookTitle());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });

        // --- Table All History ---
        colAllBookTitle.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBookTitle()));
        colAllEnfant.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEnfantName()));
        colAllDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRequestedAt().format(formatter)));
        
        colAllStatus.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    BookRequest req = tableAll.getItems().get(getIndex());
                    Label badge = new Label();
                    badge.setStyle("-fx-padding: 4 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
                    if (req.isAvailable()) {
                        badge.setText("✅ Disponible");
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #D1FAE5; -fx-text-fill: #047857;");
                    } else {
                        badge.setText("⏳ En attente");
                        badge.setStyle(badge.getStyle() + "-fx-background-color: #FFEDD5; -fx-text-fill: #C2410C;");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    private void loadData() {
        List<BookRequest> pending = service.afficherEnAttente();
        List<BookRequest> all = service.afficherTous();
        
        lblPendingCount.setText(String.valueOf(pending.size()));
        lblAllCount.setText(String.valueOf(all.size()));
        
        tablePending.setItems(FXCollections.observableArrayList(pending));
        tableAll.setItems(FXCollections.observableArrayList(all));
    }

    @FXML
    private void goBackToResources() {
        Router.go("admin_resource_index");
    }
}
