package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Resource;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class ResourceShowController {

    @FXML private Label lblTitle;
    @FXML private Label lblAuthor;
    @FXML private Label lblType;
    @FXML private Label lblLang;
    @FXML private Label lblAge;
    @FXML private Label lblSummary;
    @FXML private VBox imgContainer;
    @FXML private Label imgPlaceholder;
    @FXML private Button btnDownloadPdf;

    private Resource resource;

    @FXML
    public void initialize() {
        Object data = Router.getTransitData();
        if (data instanceof Resource) {
            resource = (Resource) data;
            fillData();
        }
    }

    private void fillData() {
        lblTitle.setText(resource.getTitle());
        lblAuthor.setText("par " + resource.getAuthor());
        
        String type = resource.getType() != null ? resource.getType().toUpperCase() : "DOC";
        lblType.setText(type);
        
        String lang = resource.getLanguage() != null ? resource.getLanguage() : "FR";
        lblLang.setText("🌍 " + lang.substring(0, Math.min(2, lang.length())).toUpperCase());
        
        lblAge.setText(resource.getMinAge() + " - " + resource.getMaxAge() + " ans");
        
        lblSummary.setText(resource.getSummary() != null && !resource.getSummary().isEmpty() ? resource.getSummary() : "Aucun résumé disponible.");
        
        // Manage Cover Image
        boolean hasImage = false;
        if (resource.getCoverImage() != null && !resource.getCoverImage().trim().isEmpty()) {
            try {
                javafx.scene.image.Image img = dev.eduplay.tools.ImageLoader.load(resource.getCoverImage());
                if (img != null && !img.isError()) {
                    javafx.scene.image.ImageView imgView = new javafx.scene.image.ImageView(img);
                    imgView.setFitWidth(180);
                    imgView.setFitHeight(250);
                    imgView.setPreserveRatio(true);
                    imgContainer.getChildren().clear(); // remove placeholder
                    imgContainer.getChildren().add(imgView);
                    hasImage = true;
                }
            } catch (Exception e) {
                System.out.println("Erreur de chargement d'image: " + e.getMessage());
            }
        }
        
        if (!hasImage) {
            if (type.equals("LIVRE")) imgPlaceholder.setText("📘");
            else if (type.equals("MAGAZINE")) imgPlaceholder.setText("📰");
            else imgPlaceholder.setText("📄");
        }
        
        if (resource.getPdfFile() == null || resource.getPdfFile().trim().isEmpty()) {
            btnDownloadPdf.setDisable(true);
            btnDownloadPdf.setText("Aucun PDF disponible");
        } else {
            btnDownloadPdf.setDisable(false);
            btnDownloadPdf.setText("Télécharger le PDF");
        }
    }

    @FXML
    private void downloadPdf() {
        if (resource.getPdfFile() != null && !resource.getPdfFile().trim().isEmpty()) {
            java.io.File pdfFile = new java.io.File(resource.getPdfFile());
            if (pdfFile.exists()) {
                try {
                    java.awt.Desktop.getDesktop().open(pdfFile);
                } catch (Exception e) {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Impossible d'ouvrir le PDF : " + e.getMessage());
                    alert.showAndWait();
                }
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                alert.setTitle("Fichier introuvable");
                alert.setHeaderText(null);
                alert.setContentText("Le fichier PDF n'existe plus sur le disque à l'emplacement indiqué.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void editResource() {
        Router.reload("admin_resource_form", resource);
    }

    @FXML
    private void goBack() {
        Router.go("admin_resource_index");
    }
}
