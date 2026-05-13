package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Product;
import dev.eduplay.services.ProductService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ParentMarketplaceController {

    @FXML private TextField searchField;
    @FXML private Label resultCountLabel;
    @FXML private FlowPane productsGrid;

    private final ProductService productService = new ProductService();
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        allProducts = productService.afficher();
        searchField.textProperty().addListener((o,ov,nv) -> filterAndDisplay());
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String q = (searchField.getText() == null) ? "" : searchField.getText().toLowerCase();
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName() != null && p.getName().toLowerCase().contains(q))
                .collect(Collectors.toList());
        
        resultCountLabel.setText(filtered.size() + " résultat(s)");
        displayProducts(filtered);
    }

    private void displayProducts(List<Product> list) {
        productsGrid.getChildren().clear();
        if (list.isEmpty()) {
            Label empty = new Label("Aucun produit ne correspond à votre recherche.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-padding: 40;");
            productsGrid.getChildren().add(empty);
            return;
        }

        productsGrid.setPrefWrapLength(1000);
        for (Product p : list) {
            productsGrid.getChildren().add(createProductCard(p));
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(12);
        card.getStyleClass().add("product-card");
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        // --- IMAGE AREA ---
        StackPane imgContainer = new StackPane();
        imgContainer.setPrefHeight(150);
        imgContainer.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 12;");
        
        ImageView iv = new ImageView();
        iv.setFitHeight(130);
        iv.setFitWidth(240);
        iv.setPreserveRatio(true);

        boolean imageSet = false;
        if (p.getPicture() != null && !p.getPicture().isEmpty()) {
            try {
                File file = new File(p.getPicture());
                if (file.exists()) {
                    iv.setImage(new Image(file.toURI().toString()));
                    imageSet = true;
                }
            } catch (Exception ignored) {}
        }

        if (!imageSet) {
            Label placeholder = new Label("🛍️");
            placeholder.setStyle("-fx-font-size: 40px;");
            imgContainer.getChildren().add(placeholder);
        } else {
            imgContainer.getChildren().add(iv);
        }

        // --- INFO AREA ---
        VBox info = new VBox(6);
        
        HBox titlePrice = new HBox(8);
        Label title = new Label(p.getName());
        title.getStyleClass().add("product-card-title");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);
        
        Label price = new Label(String.format("%.3f DT", p.getPrice()));
        price.getStyleClass().add("product-card-price");
        
        titlePrice.getChildren().addAll(title, price);

        String descText = p.getDescription() != null ? p.getDescription() : "Aucune description";
        if (descText.length() > 80) descText = descText.substring(0, 80) + "...";
        Label desc = new Label(descText);
        desc.getStyleClass().add("product-card-desc");
        desc.setWrapText(true);
        desc.setMinHeight(40);
        desc.setMaxHeight(40);

        Label badge = new Label(p.isAvailability() ? "DISPONIBLE" : "INDISPONIBLE");
        badge.getStyleClass().add("product-card-badge");
        if (!p.isAvailability()) {
            badge.getStyleClass().add("product-card-badge-unavailable");
        }

        info.getChildren().addAll(titlePrice, desc, badge);

        // --- ACTIONS ---
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER);

        Button btnOrder = new Button("Commander");
        btnOrder.getStyleClass().add("btn-save");
        HBox.setHgrow(btnOrder, Priority.ALWAYS);
        btnOrder.setMaxWidth(Double.MAX_VALUE);
        btnOrder.setDisable(!p.isAvailability());
        btnOrder.setOnAction(e -> Router.reload("parent_order_form", p));

        Button btnAddToCart = new Button("🛒");
        btnAddToCart.getStyleClass().add("btn-save");
        btnAddToCart.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-padding: 8 12;");
        btnAddToCart.setDisable(!p.isAvailability());
        btnAddToCart.setOnAction(e -> {
            dev.eduplay.services.CartService.getInstance().add(p, 1);
            showNotify("Panier", p.getName() + " ajouté au panier !");
        });

        actions.getChildren().addAll(btnOrder, btnAddToCart);
        card.getChildren().addAll(imgContainer, info, actions);
        return card;
    }

    private void showNotify(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.show(); // Non-blocking
    }
}
