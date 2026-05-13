package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Product;
import dev.eduplay.services.ProductService;
import javafx.fxml.FXML;
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
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ProductIndexController {

    @FXML private FlowPane gridProducts;
    @FXML private Label lblProductCount;
    @FXML private TextField txtSearchName;

    private final ProductService productService = new ProductService();
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        loadProducts();
        txtSearchName.textProperty().addListener((o, ov, nv) -> filterProducts());
    }

    private void loadProducts() {
        allProducts = productService.afficher();
        filterProducts();
    }

    private void filterProducts() {
        String q = (txtSearchName.getText() == null) ? "" : txtSearchName.getText().toLowerCase();
        List<Product> filtered = allProducts.stream().filter(p -> {
            return p.getName() != null && p.getName().toLowerCase().contains(q);
        }).collect(Collectors.toList());

        lblProductCount.setText(filtered.size() + " résultats");
        gridProducts.getChildren().clear();
        for (Product p : filtered) {
            gridProducts.getChildren().add(createProductCard(p));
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
            // Placeholder icon or text
            Label placeholder = new Label("📦");
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

        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "Aucune description");
        desc.getStyleClass().add("product-card-desc");
        desc.setWrapText(true);
        desc.setMinHeight(40);
        desc.setMaxHeight(40);

        Label badge = new Label(p.isAvailability() ? "EN STOCK" : "RUPTURE");
        badge.getStyleClass().add("product-card-badge");
        if (!p.isAvailability()) {
            badge.getStyleClass().add("product-card-badge-unavailable");
        }

        info.getChildren().addAll(titlePrice, desc, badge);

        // --- ACTIONS ---
        HBox actions = new HBox(8);
        actions.setStyle("-fx-padding: 8 0 0 0;");
        
        Button btnEdit = new Button("Modifier");
        btnEdit.getStyleClass().add("admin-btn-action-blue");
        btnEdit.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnEdit, Priority.ALWAYS);
        btnEdit.setOnAction(e -> Router.reload("admin_product_form", p));

        Button btnDel = new Button("🗑");
        btnDel.getStyleClass().add("btn-icon-delete");
        btnDel.setPrefWidth(40);
        btnDel.setOnAction(e -> {
            productService.supprimer(p.getId());
            loadProducts();
        });

        actions.getChildren().addAll(btnEdit, btnDel);

        card.getChildren().addAll(imgContainer, info, actions);
        return card;
    }

    @FXML
    private void addProduct() {
        Router.reload("admin_product_form", null);
    }
}
