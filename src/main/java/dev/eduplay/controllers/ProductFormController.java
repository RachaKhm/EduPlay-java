package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Product;
import dev.eduplay.services.ProductService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;

public class ProductFormController {

    @FXML private Label lblHeaderTitle;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextArea txtDescription;
    @FXML private CheckBox chkAvailability;
    @FXML private Label lblImageName;

    private String selectedImagePath;
    private final ProductService productService = new ProductService();
    private Product currentProduct;
    private java.util.function.Consumer<Boolean> onSaved;

    @FXML
    public void initialize() {
        // If transit data was set via Router, keep compatibility
        Object data = Router.getTransitData();
        if (data instanceof Product) {
            currentProduct = (Product) data;
            lblHeaderTitle.setText("✏️ Modifier le produit");
            fillForm();
        } else if (currentProduct == null) {
            currentProduct = new Product();
            lblHeaderTitle.setText("➕ Nouveau produit");
            chkAvailability.setSelected(true);
        }
    }

    /**
     * Initialize controller when used inside a modal dialog.
     * @param product Product to edit, or null to create new
     * @param onSaved callback executed after successful save (can be null)
     */
    public void init(Product product, java.util.function.Consumer<Boolean> onSaved) {
        this.onSaved = onSaved;
        if (product != null) {
            this.currentProduct = product;
            lblHeaderTitle.setText("✏️ Modifier le produit");
            fillForm();
        } else {
            this.currentProduct = new Product();
            lblHeaderTitle.setText("➕ Nouveau produit");
            chkAvailability.setSelected(true);
        }
    }

    private void fillForm() {
        txtName.setText(currentProduct.getName());
        txtPrice.setText(String.valueOf(currentProduct.getPrice()));
        txtDescription.setText(currentProduct.getDescription());
        chkAvailability.setSelected(currentProduct.isAvailability());
        selectedImagePath = currentProduct.getPicture();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            lblImageName.setText(new File(selectedImagePath).getName());
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner l'image du produit");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif"));
        Window window = txtName.getScene().getWindow();
        File file = fc.showOpenDialog(window);
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImageName.setText(file.getName());
        }
    }

    @FXML
    private void handleSave() {
        String name = txtName.getText() == null ? "" : txtName.getText().trim();
        if (name.isEmpty()) { showAlert(Alert.AlertType.ERROR, "Validation", "Le nom est requis."); return; }
        double price = 0;
        try { price = Double.parseDouble(txtPrice.getText().trim()); } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Validation", "Prix invalide."); return; }

        currentProduct.setName(name);
        currentProduct.setPrice(price);
        currentProduct.setDescription(txtDescription.getText() == null ? "" : txtDescription.getText().trim());
        currentProduct.setAvailability(chkAvailability.isSelected());
        if (selectedImagePath != null) currentProduct.setPicture(selectedImagePath);

        try {
            if (currentProduct.getId() == 0) {
                productService.ajouter(currentProduct);
            } else {
                productService.modifier(currentProduct);
            }

            // If opened as modal with a callback, use it and close the window
            if (onSaved != null) {
                onSaved.accept(true);
                try {
                    javafx.stage.Window w = lblHeaderTitle.getScene().getWindow();
                    if (w instanceof javafx.stage.Stage) ((javafx.stage.Stage) w).close();
                } catch (Exception ignored) {}
            } else {
                Router.reload("admin_product_index");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        if (onSaved != null) {
            try { javafx.stage.Window w = lblHeaderTitle.getScene().getWindow(); if (w instanceof javafx.stage.Stage) ((javafx.stage.Stage) w).close(); } catch (Exception ignored) {}
        } else {
            Router.go("admin_product_index");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}

