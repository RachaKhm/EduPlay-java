package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Commande;
import dev.eduplay.entities.Product;
import dev.eduplay.services.CommandeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ParentOrderFormController {

    @FXML private Label lblTitle;
    @FXML private Label lblPrice;
    @FXML private TextField txtQuantity;

    private Product product;
    private final CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        Object data = Router.getTransitData();
        if (data instanceof Product) {
            product = (Product) data;
            lblTitle.setText(product.getName());
            lblPrice.setText("Prix unitaire: € " + product.getPrice());
            txtQuantity.setText("1");
        }
    }

    @FXML
    private void handlePlaceOrder() {
        if (product == null) { showAlert("Erreur", "Aucun produit sélectionné."); return; }

        int qty = 1;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showAlert("Erreur", "Quantité invalide.");
            return;
        }

        // address removed from form handling

        Commande c = new Commande();
        c.setProductId(product.getId());
        c.setParentId(AppContext.getUserId());
        c.setQuantity(qty);
        c.setTotalPrice(product.getPrice() * qty);

        // Run DB insertion off the JavaFX thread to avoid UI freeze
        if (commandeService == null) {
            showAlert("Erreur", "Service de commande indisponible.");
            return;
        }

        // disable UI quickly if there's a button in the scene (best-effort)
        if (txtQuantity != null) txtQuantity.setDisable(true);

        Thread t = new Thread(() -> {
            try {
                int newId = commandeService.ajouter(c);
                Platform.runLater(() -> {
                    showAlert("Succès", "Commande passée avec succès ! (id=" + newId + ")");
                    Router.go("parent_marketplace");
                });
            } catch (Exception e) {
                e.printStackTrace();
                String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                Platform.runLater(() -> showAlert("Erreur", "Impossible de passer la commande: " + msg));
            } finally {
                Platform.runLater(() -> { if (txtQuantity != null) txtQuantity.setDisable(false); });
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML private void handleCancel() { Router.go("parent_marketplace"); }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

