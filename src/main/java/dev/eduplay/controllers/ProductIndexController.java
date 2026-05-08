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

    @FXML private FlowPane cardsPane;
    @FXML private Label countLabel;
    @FXML private TextField searchField;
    @FXML private javafx.scene.control.ComboBox<String> sortBy;

    private final ProductService productService = new ProductService();
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        loadProducts();
        searchField.textProperty().addListener((o, ov, nv) -> filterProducts());
        // populate sort options
        try {
            sortBy.getItems().addAll("Nom (A→Z)", "Nom (Z→A)", "Prix (bas→haut)", "Prix (haut→bas)");
            sortBy.getSelectionModel().selectFirst();
            sortBy.valueProperty().addListener((o,ov,nv) -> filterProducts());
        } catch (Exception ignored) {}
    }

    private void loadProducts() {
        allProducts = productService.afficher();
        filterProducts();
    }

    private void filterProducts() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        List<Product> filtered = allProducts.stream().filter(p -> {
            String name = p.getName() != null ? p.getName().toLowerCase() : "";
            String desc = p.getDescription() != null ? p.getDescription().toLowerCase() : "";
            return name.contains(q) || desc.contains(q);
        }).collect(Collectors.toList());

        // Sorting
        String sort = null;
        try { sort = sortBy.getValue(); } catch (Exception ignored) {}
        if (sort != null) {
            switch (sort) {
                case "Nom (A→Z)" -> filtered.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                case "Nom (Z→A)" -> filtered.sort((a, b) -> b.getName().compareToIgnoreCase(a.getName()));
                case "Prix (bas→haut)" -> filtered.sort((a, b) -> Double.compare(a.getPrice(), b.getPrice()));
                case "Prix (haut→bas)" -> filtered.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
            }
        }

        countLabel.setText(filtered.size() + " produits");
        cardsPane.getChildren().clear();
        for (Product p : filtered) cardsPane.getChildren().add(createProductCard(p));
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(320);

        HBox header = new HBox(8);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label name = new Label(p.getName());
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label price = new Label("€ " + p.getPrice());
        price.getStyleClass().add("stat-value");
        header.getChildren().addAll(name, spacer, price);

        Label desc = new Label(p.getDescription() != null ? p.getDescription() : "");
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill:#556; -fx-font-size:12px;");

        HBox actions = new HBox(8);
        Button btnEdit = new Button("✏️ Éditer");
        btnEdit.getStyleClass().add("btn-primary");
        btnEdit.setOnAction(e -> openProductForm(p));

        Button btnDel = new Button("🗑️ Supprimer");
        btnDel.getStyleClass().add("btn-danger");
        btnDel.setOnAction(e -> { productService.supprimer(p.getId()); loadProducts(); });

        Button btnPay = new Button("Payer");
        btnPay.getStyleClass().add("btn-primary");
        // btnPay action can be implemented later to open Stripe

        actions.getChildren().addAll(btnEdit, btnDel, btnPay);

        card.getChildren().addAll(header, desc, actions);
        return card;
    }

    @FXML
    private void addProduct() {
        openProductForm(null);
    }

    private void openProductForm(Product product) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/ProductForm.fxml"));
            javafx.scene.Parent root = loader.load();
            dev.eduplay.controllers.ProductFormController ctrl = loader.getController();
            ctrl.init(product, saved -> { if (saved) loadProducts(); });

            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialog.setTitle(product == null ? "Ajouter un produit" : "Modifier le produit");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.setResizable(false);
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

