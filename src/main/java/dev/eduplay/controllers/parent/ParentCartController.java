package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.CartItem;
import dev.eduplay.services.CartService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class ParentCartController {

    @FXML private ListView<CartItem> cartListView;
    @FXML private Label lblTotal;

    private final CartService cartService = CartService.getInstance();

    @FXML
    public void initialize() {
        setupListView();
        refresh();
    }

    private void setupListView() {
        cartListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(15);
                    container.setAlignment(Pos.CENTER_LEFT);
                    container.setStyle("-fx-padding: 10;");

                    Label icon = new Label("📦");
                    icon.setStyle("-fx-font-size: 24px;");

                    VBox info = new VBox(2);
                    Label name = new Label(item.getProduct().getName());
                    name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    Label price = new Label(String.format("%.3f DT / unité", item.getProduct().getPrice()));
                    price.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");
                    info.getChildren().addAll(name, price);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label qty = new Label("x" + item.getQuantity());
                    qty.setStyle("-fx-font-weight: bold; -fx-text-fill: #3B82F6;");

                    Label subtotal = new Label(String.format("%.3f DT", item.getProduct().getPrice() * item.getQuantity()));
                    subtotal.setStyle("-fx-font-weight: 900; -fx-text-fill: #1E293B; -fx-min-width: 80; -fx-alignment: center-right;");

                    Button btnRemove = new Button("✕");
                    btnRemove.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                    btnRemove.setOnAction(e -> {
                        cartService.remove(item.getProduct().getId());
                        refresh();
                    });

                    container.getChildren().addAll(icon, info, spacer, qty, subtotal, btnRemove);
                    setGraphic(container);
                }
            }
        });
    }

    private void refresh() {
        List<CartItem> items = cartService.list();
        cartListView.getItems().setAll(items);
        lblTotal.setText(String.format("%.3f DT", cartService.getTotal()));
    }

    @FXML
    private void handleCheckout() {
        if (cartService.list().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Panier vide", "Votre panier est vide.");
            return;
        }

        try {
            cartService.checkout();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Votre commande a été passée avec succès !");
            Router.go("parent_orders");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de finaliser la commande : " + e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        cartService.clear();
        refresh();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
