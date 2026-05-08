package dev.eduplay.controllers;

import dev.eduplay.entities.Product;
import dev.eduplay.services.ProductService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatWidgetController {

    @FXML private Button btnBubble;
    @FXML private VBox chatPane;
    @FXML private Button btnClose;
    @FXML private TextField inputField;
    @FXML private Button btnSend;
    @FXML private VBox messagesBox;
    @FXML private ScrollPane scrollMessages;

    // lazy init to avoid DB access during FXML load (prevents FXMLLoader failures if DB not ready)
    private ProductService productService;

    @FXML
    public void initialize() {
        btnBubble.setOnAction(e -> openChat());
        btnClose.setOnAction(e -> closeChat());
        btnSend.setOnAction(e -> onSend());
        inputField.setOnAction(e -> onSend());
    }

    private void openChat() {
        chatPane.setVisible(true);
        chatPane.setManaged(true);
        inputField.requestFocus();
    }

    private void closeChat() {
        chatPane.setVisible(false);
        chatPane.setManaged(false);
    }

    private void onSend() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty()) return;
        appendUserMessage(text);
        inputField.clear();

        // handle the message in background
        new Thread(() -> handleMessage(text)).start();
    }

    private void handleMessage(String text) {
        // Try to detect price request like "sous 100" or "under 100" or "moins de 100"
        Integer priceLimit = extractPrice(text);
        if (priceLimit != null) {
            try {
                if (productService == null) productService = new ProductService();
                List<Product> products = productService.afficher().stream()
                        .filter(p -> p.getPrice() <= priceLimit)
                        .sorted((a,b) -> Double.compare(a.getPrice(), b.getPrice()))
                        .collect(Collectors.toList());

                if (products.isEmpty()) {
                    appendBotMessage("Désolé — aucun produit trouvé sous " + priceLimit + " TND.");
                    return;
                }

                // Take best 3 (cheapest) as a simple heuristic
                List<Product> top = products.stream().limit(3).collect(Collectors.toList());

                StringBuilder reply = new StringBuilder();
                reply.append("Voici les meilleurs ").append(Math.min(3, top.size())).append(" produits sous ").append(priceLimit).append(" TND:\n\n");
                for (Product p : top) {
                    reply.append(String.format("%s — %.2f TND\n", p.getName(), p.getPrice()));
                    if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                        String desc = p.getDescription();
                        if (desc.length() > 120) desc = desc.substring(0,120) + "...";
                        reply.append("  ").append(desc).append("\n");
                    }
                    reply.append("\n");
                }
                reply.append("Explication : ces produits sont sélectionnés par prix (les moins chers) comme demandé. Si tu veux d'autres critères (meilleure qualité, disponibilité, catégorie), précise ta demande.");

                appendBotMessage(reply.toString());
                return;
            } catch (Exception ex) {
                appendBotMessage("Erreur interne lors de la recherche des produits: " + ex.getMessage());
                return;
            }
        }

        // fallback: simple intent — ask for clarification or try a keyword search
        try {
            if (productService == null) productService = new ProductService();
            List<Product> found = productService.afficher().stream()
                    .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(text.toLowerCase()))
                            || (p.getDescription() != null && p.getDescription().toLowerCase().contains(text.toLowerCase())))
                    .limit(5)
                    .collect(Collectors.toList());

            if (!found.isEmpty()) {
                StringBuilder reply = new StringBuilder();
                reply.append("J'ai trouvé ces produits correspondant :\n\n");
                for (Product p : found) reply.append(String.format("%s — %.2f TND\n", p.getName(), p.getPrice()));
                appendBotMessage(reply.toString());
            } else {
                appendBotMessage("Je n'ai pas compris. Tu peux par exemple écrire : 'Je veux un produit sous 100 TND' ou 'Montre-moi des sacs'.");
            }
        } catch (Exception ex) {
            appendBotMessage("Erreur interne lors de la recherche: " + ex.getMessage());
        }
    }

    private Integer extractPrice(String text) {
        // regex find numbers like 100 or 100.50 optionally preceded by 'sous', 'moins de', 'under'
        Pattern p = Pattern.compile("(?:sous|moins de|under|<)\\s*(\\d{1,6})(?:[.,](\\d{1,2}))?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            try {
                String intPart = m.group(1);
                if (intPart == null) return null;
                return Integer.parseInt(intPart);
            } catch (Exception ignored) { }
        }

        // also accept patterns like '100 TND' when preceded by 'sous' may be missing
        p = Pattern.compile("(\\d{1,6})\\s*(tnd|dt|dinar)?", Pattern.CASE_INSENSITIVE);
        m = p.matcher(text);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) { }
        }
        return null;
    }

    private void appendUserMessage(String text) {
        Platform.runLater(() -> {
            Label lbl = new Label(text);
            lbl.getStyleClass().add("msg-bubble");
            HBox container = new HBox(lbl);
            container.getStyleClass().add("message-user");
            messagesBox.getChildren().add(container);
            scrollToBottom();
        });
    }

    private void appendBotMessage(String text) {
        Platform.runLater(() -> {
            Label lbl = new Label(text);
            lbl.setWrapText(true);
            lbl.getStyleClass().add("msg-bubble");
            HBox container = new HBox(lbl);
            container.getStyleClass().add("message-bot");
            messagesBox.getChildren().add(container);
            scrollToBottom();
        });
    }

    private void scrollToBottom() {
        Platform.runLater(() -> scrollMessages.setVvalue(1.0));
    }
}

