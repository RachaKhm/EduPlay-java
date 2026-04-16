package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.BookRequest;
import dev.eduplay.services.BookRequestService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookRequestIndexController {

    @FXML private Label lblPendingCount;
    @FXML private TableView<BookRequest> tablePending;
    @FXML private TableColumn<BookRequest, String> colBookTitle;
    @FXML private TableColumn<BookRequest, String> colDate;
    @FXML private TableColumn<BookRequest, Void> colAction;

    @FXML private Label lblAllCount;
    @FXML private TableView<BookRequest> tableAll;
    @FXML private TableColumn<BookRequest, String> colAllBookTitle;
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
        // Pending
        colBookTitle.setCellValueFactory(cellData -> new SimpleStringProperty("📚 " + cellData.getValue().getBookTitle()));
        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRequestedAt().format(formatter)));
        
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("+ Ajouter ce livre");
            {
                btn.setStyle("-fx-background-color: #F97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                btn.setOnAction(event -> {
                    BookRequest req = getTableView().getItems().get(getIndex());
                    // Pass the title to be pre-filled via Router Transit Data
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

        // All History
        colAllBookTitle.setCellValueFactory(cellData -> new SimpleStringProperty("📚 " + cellData.getValue().getBookTitle()));
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
                    if (req.isNotified()) {
                        badge.setText("✅ Notifié");
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
