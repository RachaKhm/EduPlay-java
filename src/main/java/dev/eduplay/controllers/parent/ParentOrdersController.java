package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.Commande;
import dev.eduplay.entities.Product;
import dev.eduplay.services.CommandeService;
import dev.eduplay.services.ProductService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dev.eduplay.services.StripeService;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressIndicator;

public class ParentOrdersController {

	@FXML private VBox ordersContainer;
	@FXML private Label subtitleLabel;
	@FXML private TextField searchField;

	private final CommandeService commandeService = new CommandeService();
	private final ProductService productService = new ProductService();

	private List<Commande> currentOrders;
	private final Map<Integer, Product> productById = new HashMap<>();

	@FXML
	public void initialize() {
		if (searchField != null) {
			searchField.textProperty().addListener((obs, oldV, newV) -> renderOrders());
		}
		loadProducts();
		loadOrders();
	}

	@FXML
	public void openChat() {
		try {
			Router.go("parent_chat");
		} catch (Exception e) {
			new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Impossible d'ouvrir le chat: " + e.getMessage()).showAndWait();
		}
	}

	private void loadProducts() {
		productById.clear();
		List<Product> products = productService.afficher();
		for (Product p : products) productById.put(p.getId(), p);
	}

	private void loadOrders() {
		int parentId = AppContext.getUserId();
		try {
			currentOrders = commandeService.afficherParParent(parentId);
		} catch (Exception e) {
			currentOrders = List.of();
		}
		Platform.runLater(this::renderOrders);
	}

	private void renderOrders() {
		ordersContainer.getChildren().clear();
		if (currentOrders == null || currentOrders.isEmpty()) {
			Label empty = new Label("Aucune commande pour le moment.");
			empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-padding: 30;");
			ordersContainer.getChildren().add(empty);
			if (subtitleLabel != null) subtitleLabel.setText("0 commande(s)");
			return;
		}

		String q = searchField != null ? searchField.getText().trim().toLowerCase() : "";
		int count = 0;
		for (Commande cmd : currentOrders) {
			String productName = "Produit #" + cmd.getProductId();
			Product p = productById.get(cmd.getProductId());
			if (p != null) productName = p.getName();
			// Apply search filter
			if (!q.isEmpty() && !(productName.toLowerCase().contains(q) || String.valueOf(cmd.getId()).contains(q))) continue;
			ordersContainer.getChildren().add(buildCard(cmd, productName));
			count++;
		}
		if (subtitleLabel != null) subtitleLabel.setText(count + " commande(s)");
	}

	private VBox buildCard(Commande cmd, String productName) {
		VBox card = new VBox(8);
		card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #E9EAF2; -fx-border-radius: 12; -fx-border-width: 1; -fx-padding: 14;");

		Label title = new Label("#" + cmd.getId() + " — " + productName);
		title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

		Label meta = new Label("Quantité: " + cmd.getQuantity() + " · Total: €" + cmd.getTotalPrice());
		meta.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569;");

		HBox actions = new HBox(8);
		Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
		Button btnDelete = new Button("Supprimer");
		btnDelete.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;");
		btnDelete.setOnAction(e -> {
			Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer la commande #"+cmd.getId()+" ?", ButtonType.YES, ButtonType.NO);
			confirm.setHeaderText("Supprimer commande");
			confirm.showAndWait().ifPresent(b -> {
				if (b == ButtonType.YES) {
					try {
						commandeService.supprimer(cmd.getId());
						loadOrders();
					} catch (Exception ex) {
						new Alert(Alert.AlertType.ERROR, "Impossible de supprimer la commande: " + ex.getMessage(), ButtonType.OK).showAndWait();
					}
				}
			});
		});

		Button btnEdit = new Button("Modifier");
		btnEdit.setStyle("-fx-background-color: #06B6D4; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;");
		btnEdit.setOnAction(e -> {
			// Navigate to order edit form if available, otherwise show info
			try {
				Router.reload("parent_order_form", cmd);
			} catch (Exception ex) {
				new Alert(Alert.AlertType.INFORMATION, "Modification non disponible.", ButtonType.OK).showAndWait();
			}
		});

		// Payer button (Stripe Checkout)
		Button btnPay = new Button("Payer");
		btnPay.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 12;");
		btnPay.setOnAction(e -> {
			StripeService stripe = new StripeService();
			if (!stripe.isConfigured()) {
				new Alert(Alert.AlertType.WARNING, "Stripe non configuré. Définissez STRIPE_SECRET_KEY dans votre .env ou variable d'environnement.", ButtonType.OK).showAndWait();
				return;
			}

			ProgressIndicator pi = new ProgressIndicator();
			pi.setPrefSize(24,24);
			actions.getChildren().add(pi);

			Task<Void> task = new Task<>() {
				@Override
				protected Void call() throws Exception {
					try {
						stripe.createCheckoutAndOpen(productName, cmd.getTotalPrice(), Math.max(1, cmd.getQuantity()));
					} catch (Exception ex) {
						throw ex;
					}
					return null;
				}
			};
			task.setOnSucceeded(evt -> Platform.runLater(() -> actions.getChildren().remove(pi)));
			task.setOnFailed(evt -> {
				Platform.runLater(() -> {
					actions.getChildren().remove(pi);
					new Alert(Alert.AlertType.ERROR, "Impossible de démarrer le paiement: " + task.getException().getMessage(), ButtonType.OK).showAndWait();
				});
			});
			new Thread(task).start();
		});

		actions.getChildren().addAll(spacer, btnPay, btnEdit, btnDelete);

		card.getChildren().addAll(title, meta, actions);
		return card;
	}

	@FXML
	private void handleRefresh() { loadProducts(); loadOrders(); }

	@FXML
	private void handleBack() { Router.go("parent_dashboard"); }

}


