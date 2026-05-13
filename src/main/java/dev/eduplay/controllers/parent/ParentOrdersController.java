package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Commande;
import dev.eduplay.entities.Product;
import dev.eduplay.services.CommandeService;
import dev.eduplay.services.ProductService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParentOrdersController {

    @FXML private VBox ordersContainer;
    @FXML private TextField searchField;
    @FXML private Label subtitleLabel;

    private final CommandeService commandeService = new CommandeService();
    private final ProductService productService = new ProductService();
    private Map<Integer, Product> productsMap;
    private List<Commande> allOrders;

    @FXML
    public void initialize() {
        // Load products for display and price calculation
        productsMap = productService.afficher().stream()
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));

        searchField.textProperty().addListener((o, ov, nv) -> filterAndDisplay());
        handleRefresh();
    }

    @FXML
    private void handleRefresh() {
        int parentId = AppContext.getUserId();
        allOrders = commandeService.afficherParParent(parentId);
        subtitleLabel.setText(allOrders.size() + " commande(s) au total");
        filterAndDisplay();
    }

    private void filterAndDisplay() {
        ordersContainer.getChildren().clear();
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase();

        List<Commande> filtered = allOrders.stream().filter(c -> {
            Product p = productsMap.get(c.getProductId());
            String pName = (p != null ? p.getName() : "").toLowerCase();
            String idStr = String.valueOf(c.getId());
            return pName.contains(q) || idStr.contains(q);
        }).collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label empty = new Label("Aucune commande trouvée.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-padding: 20;");
            ordersContainer.getChildren().add(empty);
            return;
        }

        for (Commande c : filtered) {
            ordersContainer.getChildren().add(createOrderCard(c));
        }
    }

    private VBox createOrderCard(Commande item) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-padding: 18; -fx-background-radius: 16; -fx-border-color: #E2E8F0; -fx-border-radius: 16; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 10, 0, 0, 3);");
        
        // --- HEADER ---
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Product p = productsMap.get(item.getProductId());
        String pName = (p != null ? p.getName() : "Produit #" + item.getProductId());
        
        VBox titles = new VBox(2);
        Label nameLabel = new Label(pName);
        nameLabel.setStyle("-fx-font-weight: 800; -fx-font-size: 16px; -fx-text-fill: #1E293B;");
        Label idLabel = new Label("Commande #" + item.getId());
        idLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px;");
        titles.getChildren().addAll(nameLabel, idLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label priceLabel = new Label(String.format("%.3f DT", item.getTotalPrice()));
        priceLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: 900; -fx-font-size: 18px;");
        
        header.getChildren().addAll(titles, spacer, priceLabel);
        
        // --- INFO ---
        HBox infoRow = new HBox(12);
        infoRow.setAlignment(Pos.CENTER_LEFT);
        
        Label qtyLabel = new Label("Quantité: " + item.getQuantity());
        qtyLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px; -fx-font-weight: 600;");
        
        Label statusLabel = new Label(item.getStatus().toUpperCase());
        statusLabel.setStyle("-fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: 900;");
        
        if ("pending".equalsIgnoreCase(item.getStatus())) {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;");
        } else if ("confirmed".equalsIgnoreCase(item.getStatus()) || "delivered".equalsIgnoreCase(item.getStatus()) || "paid".equalsIgnoreCase(item.getStatus())) {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;");
        } else {
            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #F1F5F9; -fx-text-fill: #64748B;");
        }
        
        infoRow.getChildren().addAll(qtyLabel, new Region(), statusLabel);
        HBox.setHgrow(infoRow.getChildren().get(1), Priority.ALWAYS);

        // --- ACTIONS ---
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setStyle("-fx-padding: 8 0 0 0; -fx-border-color: #F1F5F9; -fx-border-width: 1 0 0 0;");

        if ("pending".equalsIgnoreCase(item.getStatus())) {
            Button btnPay = new Button("💳 Payer");
            btnPay.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            btnPay.setOnAction(e -> handlePayOrder(item));

            Button btnEdit = new Button("✏️");
            btnEdit.setStyle("-fx-background-color: #F1F5F9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            btnEdit.setOnAction(e -> handleModifyOrder(item));

            Button btnDelete = new Button("🗑");
            btnDelete.setStyle("-fx-background-color: #FEF2F2; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            btnDelete.setOnAction(e -> handleDeleteOrder(item));

            actions.getChildren().addAll(btnPay, btnEdit, btnDelete);
        } else {
            Label infoMsg = new Label("Commande traitée");
            infoMsg.setStyle("-fx-text-fill: #94A3B8; -fx-font-size: 11px; -fx-font-style: italic;");
            actions.getChildren().add(infoMsg);
        }

        card.getChildren().addAll(header, infoRow, actions);
        return card;
    }

    private void handlePayOrder(Commande c) {
        Router.reload("parent_stripe_payment", c);
    }

    private void handleModifyOrder(Commande c) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(c.getQuantity()));
        dialog.setTitle("Modifier la commande");
        dialog.setHeaderText("Modifier la quantité");
        dialog.setContentText("Nouvelle quantité :");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(qtyStr -> {
            try {
                int newQty = Integer.parseInt(qtyStr.trim());
                if (newQty <= 0) throw new NumberFormatException();
                
                Product p = productsMap.get(c.getProductId());
                if (p != null) {
                    c.setQuantity(newQty);
                    c.setTotalPrice(p.getPrice() * newQty);
                    commandeService.modifier(c);
                    handleRefresh();
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Quantité invalide.");
            }
        });
    }

    private void handleDeleteOrder(Commande c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Suppression");
        confirm.setHeaderText("Annuler la commande");
        confirm.setContentText("Voulez-vous vraiment supprimer cette commande ?");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    commandeService.supprimer(c.getId());
                    handleRefresh();
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression.");
                }
            }
        });
    }

    @FXML
    private void openChat() {
        Router.go("parent_chat");
    }

    @FXML
    private void handleBack() {
        Router.go("parent_marketplace");
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}
