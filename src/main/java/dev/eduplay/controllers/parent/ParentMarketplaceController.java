package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Product;
import dev.eduplay.services.ProductService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class ParentMarketplaceController {

    @FXML private TextField searchField;
    @FXML private Label resultCountLabel;
    @FXML private FlowPane productsGrid;
    @FXML private javafx.scene.control.ComboBox<String> sortBox;

    private final ProductService productService = new ProductService();
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        allProducts = productService.afficher();
        searchField.textProperty().addListener((o,ov,nv) -> filterAndDisplay());
        // Setup sort box options
        if (sortBox != null) {
            sortBox.getItems().addAll("Aucun", "Prix: bas → haut", "Prix: haut → bas");
            sortBox.getSelectionModel().selectFirst();
            sortBox.valueProperty().addListener((obs, oldV, newV) -> filterAndDisplay());
        }
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<Product> filtered = allProducts.stream().filter(p -> p.getName()!=null && p.getName().toLowerCase().contains(q)).collect(Collectors.toList());
        // Apply sorting
        String sort = sortBox != null ? sortBox.getValue() : "Aucun";
        if ("Prix: bas → haut".equals(sort)) {
            filtered.sort((a,b) -> Double.compare(a.getPrice(), b.getPrice()));
        } else if ("Prix: haut → bas".equals(sort)) {
            filtered.sort((a,b) -> Double.compare(b.getPrice(), a.getPrice()));
        }
        resultCountLabel.setText(filtered.size() + " résultat(s)");
        displayProducts(filtered);
    }

    private void displayProducts(List<Product> list) {
        productsGrid.getChildren().clear();
        if (list.isEmpty()) { productsGrid.getChildren().add(new Label("Aucun produit trouvé")); return; }

        double cardWidth = 300;
        productsGrid.setPrefWrapLength(cardWidth * 3 + 40);

        for (Product p : list) productsGrid.getChildren().add(createProductCard(p));
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(8);
        card.setPrefWidth(300);
        card.getStyleClass().add("product-card");

        ImageView img = new ImageView();
        img.setFitWidth(300); img.setFitHeight(150); img.setPreserveRatio(true);
        img.getStyleClass().add("product-image");
        boolean loaded = false;
        if (p.getPicture() != null && !p.getPicture().isBlank()) {
            File f = new File(p.getPicture());
            if (f.exists()) { try { img.setImage(new Image(f.toURI().toString())); loaded = true;} catch(Exception ignored){} }
        }
        if (!loaded) { Label ph = new Label("🛍️"); ph.setStyle("-fx-font-size:48px;"); card.getChildren().add(ph);} else { card.getChildren().add(img);}        

        Label name = new Label(p.getName());
        name.getStyleClass().add("product-title");

        String descText = p.getDescription() != null ? p.getDescription() : "";
        if (descText.length() > 120) descText = descText.substring(0,120) + "...";
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.getStyleClass().add("product-desc");

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label("€ " + p.getPrice());
        price.getStyleClass().add("product-price");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnOrder = new Button("Commander");
        btnOrder.setStyle("-fx-background-color:#8b5cf6; -fx-text-fill:white; -fx-background-radius:12;");
        btnOrder.setOnAction(e -> Router.reload("parent_order_form", p));

        Button btnAdd = new Button("Ajouter au panier");
        btnAdd.setStyle("-fx-background-color:#10b981; -fx-text-fill:white; -fx-background-radius:12;");
        btnAdd.setOnAction(e -> {
            try {
                int qty = 1;
                dev.eduplay.entities.CartItem ci = new dev.eduplay.entities.CartItem(p, qty);
                dev.eduplay.core.AppContext.addToCart(ci);
                double total = dev.eduplay.core.AppContext.getCartTotal();
                int count = dev.eduplay.core.AppContext.getCart().size();
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, "Produit ajouté au panier.\nArticles dans le panier: " + count + " · Total: € " + String.format(java.util.Locale.US, "%.2f", total), javafx.scene.control.ButtonType.OK);
                a.setHeaderText("Ajouté"); a.showAndWait();
            } catch (Exception ex) {
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Impossible d'ajouter au panier: " + ex.getMessage(), javafx.scene.control.ButtonType.OK);
                a.showAndWait();
            }
        });

        footer.getChildren().add(price);
        footer.getChildren().add(spacer);
        // order button on the right
        HBox rightBox = new HBox(8);
        rightBox.getStyleClass().add("product-actions");
        rightBox.getChildren().addAll(btnAdd, btnOrder);
        footer.getChildren().add(rightBox);

        card.getChildren().addAll(name, desc, footer);
        VBox.setMargin(name, new javafx.geometry.Insets(6,0,0,0));
        return card;
    }
}

