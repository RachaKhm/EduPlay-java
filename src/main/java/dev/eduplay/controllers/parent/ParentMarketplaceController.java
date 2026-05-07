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

    private final ProductService productService = new ProductService();
    private List<Product> allProducts;

    @FXML
    public void initialize() {
        allProducts = productService.afficher();
        searchField.textProperty().addListener((o,ov,nv) -> filterAndDisplay());
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();
        List<Product> filtered = allProducts.stream().filter(p -> p.getName()!=null && p.getName().toLowerCase().contains(q)).collect(Collectors.toList());
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
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 4);");

        ImageView img = new ImageView();
        img.setFitWidth(300); img.setFitHeight(150); img.setPreserveRatio(true);
        boolean loaded = false;
        if (p.getPicture() != null && !p.getPicture().isBlank()) {
            File f = new File(p.getPicture());
            if (f.exists()) { try { img.setImage(new Image(f.toURI().toString())); loaded = true;} catch(Exception ignored){} }
        }
        if (!loaded) { Label ph = new Label("🛍️"); ph.setStyle("-fx-font-size:48px;"); card.getChildren().add(ph);} else { card.getChildren().add(img);}        

        Label name = new Label(p.getName());
        name.setStyle("-fx-font-size:14px; -fx-font-weight:bold; -fx-text-fill:#1e293b;");

        String descText = p.getDescription() != null ? p.getDescription() : "";
        if (descText.length() > 120) descText = descText.substring(0,120) + "...";
        Label desc = new Label(descText);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill:#64748b; -fx-font-size:12px;");

        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label("€ " + p.getPrice());
        price.setStyle("-fx-font-weight:bold; -fx-text-fill:#10b981;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button btnOrder = new Button("Commander");
        btnOrder.setStyle("-fx-background-color:#8b5cf6; -fx-text-fill:white; -fx-background-radius:12;");
        btnOrder.setOnAction(e -> Router.reload("parent_order_form", p));

        footer.getChildren().add(price);
        footer.getChildren().add(spacer);
        footer.getChildren().add(btnOrder);

        card.getChildren().addAll(name, desc, footer);
        VBox.setMargin(name, new javafx.geometry.Insets(6,0,0,0));
        return card;
    }
}

