package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Commande;
import dev.eduplay.entities.Product;
import dev.eduplay.services.CommandeService;
import dev.eduplay.services.ProductService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ParentOrderFormController {

    @FXML private Label lblTitle;
    @FXML private Label lblPrice;
    @FXML private TextField txtQuantity;
    @FXML private javafx.scene.control.Button btnPlace;

    private Product product;
    private final CommandeService commandeService = new CommandeService();
    private final ProductService productService = new ProductService();
    private Commande editingCommande = null;
    private boolean editMode = false;

    @FXML
    public void initialize() {
        Object data = Router.getTransitData();
        if (data instanceof Product) {
            product = (Product) data;
            lblTitle.setText(product.getName());
            lblPrice.setText("Prix unitaire: € " + product.getPrice());
            txtQuantity.setText("1");
            if (btnPlace != null) btnPlace.setText("Commander");
        } else if (data instanceof Commande) {
            // Edit existing commande
            editingCommande = (Commande) data;
            editMode = true;
            // try to load product by id
            int pid = editingCommande.getProductId();
            Product found = null;
            try {
                for (Product p : productService.afficher()) {
                    if (p.getId() == pid) { found = p; break; }
                }
            } catch (Exception ex) { /* ignore */ }
            product = found;
            if (product != null) {
                lblTitle.setText(product.getName());
                lblPrice.setText("Prix unitaire: € " + product.getPrice());
            } else {
                lblTitle.setText("Produit #" + pid);
                // if product unknown, compute unit price from commande
                double unit = editingCommande.getQuantity() > 0 ? editingCommande.getTotalPrice() / editingCommande.getQuantity() : editingCommande.getTotalPrice();
                lblPrice.setText("Prix unitaire: € " + unit);
            }
            txtQuantity.setText(String.valueOf(editingCommande.getQuantity()));
            if (btnPlace != null) btnPlace.setText("Enregistrer");
        }
    }

    @FXML
    private void handlePlaceOrder() {
        // In create mode product is required; in edit mode we can proceed even if product object is missing
        if (!editMode && product == null) { showAlert("Erreur", "Aucun produit sélectionné."); return; }

        int qty = 1;
        try {
            qty = Integer.parseInt(txtQuantity.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (Exception e) {
            showAlert("Erreur", "Quantité invalide.");
            return;
        }

        // address removed from form handling

        if (editMode && editingCommande != null) {
            // modify existing commande
            editingCommande.setQuantity(qty);
            double unitPrice = product != null ? product.getPrice() : (editingCommande.getQuantity() > 0 ? editingCommande.getTotalPrice() / editingCommande.getQuantity() : editingCommande.getTotalPrice());
            editingCommande.setTotalPrice(unitPrice * qty);

            Thread t = new Thread(() -> {
                try {
                    commandeService.modifier(editingCommande);
                    Platform.runLater(() -> {
                        showAlert("Succès", "Commande mise à jour ! (id=" + editingCommande.getId() + ")");
                        // Force reload so the orders list refreshes
                        Router.reload("parent_orders");
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    String msg = e.getMessage() != null ? e.getMessage() : e.toString();
                    Platform.runLater(() -> showAlert("Erreur", "Impossible de modifier la commande: " + msg));
                } finally {
                    Platform.runLater(() -> { if (txtQuantity != null) txtQuantity.setDisable(false); });
                }
            });
            t.setDaemon(true);
            t.start();
            return;
        }

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

