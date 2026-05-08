package dev.eduplay.controllers.parent;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Commande;
import dev.eduplay.services.CommandeService;
import dev.eduplay.services.StripeService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.sql.SQLException;

public class StripePaymentController {

    @FXML private Label lblAmount;
    @FXML private Label lblProduct;
    @FXML private TextField txtCardNumber;
    @FXML private TextField txtExpiry;
    @FXML private TextField txtCvc;
    @FXML private TextField txtHolder;
    @FXML private Button btnPay;

    private Commande currentCommande;
    private final CommandeService commandeService = new CommandeService();
    private final StripeService stripeService = StripeService.getInstance();

    @FXML
    public void initialize() {
        Object data = Router.getTransitData();
        if (data instanceof Commande) {
            currentCommande = (Commande) data;
            lblAmount.setText(String.format("%.3f DT", currentCommande.getTotalPrice()));
            lblProduct.setText("Commande #" + currentCommande.getId());
        }
    }

    @FXML
    private void handlePayment() {
        if (currentCommande == null) return;

        // Basic mock validation
        // Basic mock validation
        if (txtCardNumber.getText() == null || txtCardNumber.getText().trim().length() < 10) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Numéro de carte invalide.");
            return;
        }

        btnPay.setDisable(true);
        btnPay.setText("Traitement en cours...");

        // Simulate network delay
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait 2s
                commandeService.updatePaymentStatus(currentCommande.getId(), true, "paid");
                
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION, "Paiement réussi", "Merci pour votre achat !");
                    Router.go("parent_orders");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Le paiement a échoué : " + e.getMessage());
                    btnPay.setDisable(false);
                    btnPay.setText("Payer maintenant");
                });
            }
        }).start();
    }

    @FXML
    private void handleCancel() {
        Router.go("parent_orders");
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
