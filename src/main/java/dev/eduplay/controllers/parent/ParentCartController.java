package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.CartItem;
import dev.eduplay.entities.Commande;
import dev.eduplay.services.CommandeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class ParentCartController {

    @FXML private VBox cartContainer;
    @FXML private Label subtitleLabel;

    private final CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        renderCart();
    }

    private void renderCart() {
        cartContainer.getChildren().clear();
        List<CartItem> items = new ArrayList<>(AppContext.getCart());
        if (items.isEmpty()) {
            Label empty = new Label("Votre panier est vide.");
            empty.setStyle("-fx-font-size:14px; -fx-text-fill:#94A3B8; -fx-padding:20;");
            cartContainer.getChildren().add(empty);
            if (subtitleLabel != null) subtitleLabel.setText("0 article(s)");
            return;
        }

        int count = 0;
        for (CartItem it : items) {
            cartContainer.getChildren().add(buildRow(it));
            count += it.getQuantity();
        }
        if (subtitleLabel != null) subtitleLabel.setText(count + " article(s) · Total: € " + String.format(java.util.Locale.US, "%.2f", AppContext.getCartTotal()));
    }

    private HBox buildRow(CartItem it) {
        HBox row = new HBox(10);
        row.setStyle("-fx-background-color:white; -fx-padding:12; -fx-background-radius:10; -fx-border-color:#E9EAF2; -fx-border-radius:10;");

        Label name = new Label(it.getProduct().getName());
        name.setStyle("-fx-font-weight:bold; -fx-font-size:13px;");

        Label price = new Label("€ " + it.getProduct().getPrice());
        price.setStyle("-fx-text-fill:#10b981; -fx-font-weight:bold;");

        Spinner<Integer> qty = new Spinner<>();
        qty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, it.getQuantity()));
        qty.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            it.setQuantity(newV);
            // update subtitle / total
            if (subtitleLabel != null) subtitleLabel.setText(getCount() + " article(s) · Total: € " + String.format(java.util.Locale.US, "%.2f", AppContext.getCartTotal()));
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        javafx.scene.control.Button btnRemove = new javafx.scene.control.Button("Supprimer");
        btnRemove.setOnAction(e -> {
            AppContext.removeFromCart(it.getProduct().getId());
            renderCart();
        });

        row.getChildren().addAll(name, price, spacer, qty, btnRemove);
        return row;
    }

    private int getCount() {
        int c = 0; for (CartItem it : AppContext.getCart()) c += it.getQuantity(); return c;
    }

    @FXML
    private void handleClearCart() {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Vider le panier ?", ButtonType.YES, ButtonType.NO);
        a.setHeaderText("Vider le panier");
        a.showAndWait().ifPresent(b -> { if (b == ButtonType.YES) { AppContext.clearCart(); renderCart(); } });
    }

    @FXML
    private void handleCheckout() {
        List<CartItem> items = new ArrayList<>(AppContext.getCart());
        if (items.isEmpty()) { new Alert(Alert.AlertType.INFORMATION, "Panier vide.", ButtonType.OK).showAndWait(); return; }

        // Create commandes for each cart item
        Thread t = new Thread(() -> {
            try {
                int parentId = AppContext.getUserId();
                for (CartItem it : items) {
                    Commande c = new Commande();
                    c.setProductId(it.getProduct().getId());
                    c.setParentId(parentId);
                    c.setQuantity(it.getQuantity());
                    c.setTotalPrice(it.getProduct().getPrice() * it.getQuantity());
                    commandeService.ajouter(c);
                }
                AppContext.clearCart();
                Platform.runLater(() -> {
                    new Alert(Alert.AlertType.INFORMATION, "Commande(s) passée(s) avec succès !", ButtonType.OK).showAndWait();
                    Router.go("parent_orders");
                });
            } catch (Exception e) {
                Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Erreur lors du passage en commande: " + e.getMessage(), ButtonType.OK).showAndWait());
            }
        });
        t.setDaemon(true);
        t.start();
    }
}

