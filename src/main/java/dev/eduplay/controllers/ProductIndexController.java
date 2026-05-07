package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Product;
import dev.eduplay.services.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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
        String q = txtSearchName.getText() == null ? "" : txtSearchName.getText().toLowerCase();
        List<Product> filtered = allProducts.stream().filter(p -> {
            boolean matchName = p.getName() != null && p.getName().toLowerCase().contains(q);
            return matchName;
        }).collect(Collectors.toList());

        lblProductCount.setText(filtered.size() + " résultats");
        gridProducts.getChildren().clear();
        for (Product p : filtered) gridProducts.getChildren().add(createProductCard(p));
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E2E8F0; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 12;");
        card.setPrefWidth(300);

        HBox header = new HBox(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label price = new Label("€ " + p.getPrice());
        header.getChildren().addAll(name, spacer, price);

        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
        desc.setWrapText(true);

        HBox actions = new HBox(8);
        Button btnEdit = new Button("Éditer");
        btnEdit.setOnAction(e -> Router.reload("admin_product_form", p));
        Button btnDel = new Button("Supprimer");
        btnDel.setOnAction(e -> { productService.supprimer(p.getId()); loadProducts(); });
        actions.getChildren().addAll(btnEdit, btnDel);

        card.getChildren().addAll(header, desc, actions);
        return card;
    }

    @FXML
    private void addProduct() {
        Router.reload("admin_product_form", null);
    }
}

