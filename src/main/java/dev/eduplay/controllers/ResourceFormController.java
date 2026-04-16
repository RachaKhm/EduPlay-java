package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.Library;
import dev.eduplay.entities.Resource;
import dev.eduplay.services.LibraryService;
import dev.eduplay.services.ResourceService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.util.List;

public class ResourceFormController {

    @FXML private Label lblHeaderTitle;
    @FXML private Label lblHeaderDesc;
    
    @FXML private TextField txtTitle;
    @FXML private TextField txtAuthor;
    @FXML private TextArea txtSummary;
    @FXML private ComboBox<Library> comboLibrary;
    @FXML private ComboBox<String> comboType;
    @FXML private ComboBox<String> comboLang;
    @FXML private TextField txtMinAge;
    @FXML private TextField txtMaxAge;
    @FXML private Label lblImageName;
    @FXML private Label lblPdfName;

    private String selectedImagePath;
    private String selectedPdfPath;

    private final ResourceService resourceService = new ResourceService();
    private final LibraryService libraryService = new LibraryService();
    private Resource currentResource;

    @FXML
    public void initialize() {
        comboType.getItems().addAll("Livre", "Magazine", "Journal", "Article");
        comboLang.getItems().addAll("Francais", "Anglais", "Arabe");
        
        // Setup Library ComboBox
        setupLibraryCombo();

        Object data = Router.getTransitData();
        if (data instanceof Resource) {
            currentResource = (Resource) data;
            lblHeaderTitle.setText("✏️ Modifier la ressource");
            lblHeaderDesc.setText("Modifiez les informations de « " + currentResource.getTitle() + " »");
            fillForm();
        } else if (data instanceof String) {
            // It could be a book request title! Let's check.
            String prefilledTitle = (String) data;
            currentResource = new Resource();
            txtTitle.setText(prefilledTitle);
            lblHeaderTitle.setText("📄 Nouvelle ressource (depuis une demande)");
        } else {
            currentResource = new Resource();
            lblHeaderTitle.setText("📄 Nouvelle ressource");
        }
    }

    private void setupLibraryCombo() {
        List<Library> libraries = libraryService.afficher();
        comboLibrary.getItems().addAll(libraries);
        
        comboLibrary.setConverter(new StringConverter<Library>() {
            @Override
            public String toString(Library lib) {
                return lib != null ? lib.getName() : "";
            }

            @Override
            public Library fromString(String string) {
                return null; // Not needed
            }
        });
    }

    private void fillForm() {
        txtTitle.setText(currentResource.getTitle());
        txtAuthor.setText(currentResource.getAuthor());
        txtSummary.setText(currentResource.getSummary());
        comboType.setValue(currentResource.getType());
        comboLang.setValue(currentResource.getLanguage());
        txtMinAge.setText(String.valueOf(currentResource.getMinAge()));
        txtMaxAge.setText(String.valueOf(currentResource.getMaxAge()));
        
        selectedImagePath = currentResource.getCoverImage();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            lblImageName.setText(new File(selectedImagePath).getName());
        }
        
        selectedPdfPath = currentResource.getPdfFile();
        if (selectedPdfPath != null && !selectedPdfPath.isEmpty()) {
            lblPdfName.setText(new File(selectedPdfPath).getName());
        }
        
        // Set selected library
        for (Library lib : comboLibrary.getItems()) {
            if (lib.getId() == currentResource.getLibraryId()) {
                comboLibrary.setValue(lib);
                break;
            }
        }
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner l'image de couverture");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers Images", "*.png", "*.jpg", "*.jpeg"));
        Window window = comboLibrary.getScene().getWindow();
        File file = fc.showOpenDialog(window);
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImageName.setText(file.getName());
        }
    }

    @FXML
    private void handleChoosePdf() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sélectionner le document PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Window window = comboLibrary.getScene().getWindow();
        File file = fc.showOpenDialog(window);
        if (file != null) {
            selectedPdfPath = file.getAbsolutePath();
            lblPdfName.setText(file.getName());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleSave() {
        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty() || 
            txtAuthor.getText() == null || txtAuthor.getText().trim().isEmpty() || 
            comboLibrary.getValue() == null) {
            
            showAlert(Alert.AlertType.WARNING, "Champs requis", "Veuillez remplir le titre, l'auteur et sélectionner une bibliothèque !");
            return;
        }

        currentResource.setTitle(txtTitle.getText());
        currentResource.setAuthor(txtAuthor.getText());
        currentResource.setSummary(txtSummary.getText());
        currentResource.setType(comboType.getValue());
        currentResource.setLanguage(comboLang.getValue());
        currentResource.setLibraryId(comboLibrary.getValue().getId());
        currentResource.setCoverImage(selectedImagePath);
        currentResource.setPdfFile(selectedPdfPath);
        
        try {
            if (txtMinAge.getText() != null && !txtMinAge.getText().isEmpty()) {
                currentResource.setMinAge(Integer.parseInt(txtMinAge.getText()));
            } else {
                currentResource.setMinAge(0);
            }
            if (txtMaxAge.getText() != null && !txtMaxAge.getText().isEmpty()) {
                currentResource.setMaxAge(Integer.parseInt(txtMaxAge.getText()));
            } else {
                currentResource.setMaxAge(0);
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Format invalide", "L'âge minimum et maximum doivent être des nombres.");
            return;
        }

        try {
            if (currentResource.getId() == 0) {
                resourceService.ajouter(currentResource);
            } else {
                resourceService.modifier(currentResource);
            }
            Router.reload("admin_resource_index");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur base de données", "Impossible d'enregistrer la ressource : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Router.go("admin_resource_index");
    }
}
