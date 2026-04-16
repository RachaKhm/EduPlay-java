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

    // Error Labels
    @FXML private Label lblErrorTitle;
    @FXML private Label lblErrorAuthor;
    @FXML private Label lblErrorSummary;
    @FXML private Label lblErrorLibrary;
    @FXML private Label lblErrorType;
    @FXML private Label lblErrorLang;
    @FXML private Label lblErrorMinAge;
    @FXML private Label lblErrorMaxAge;
    @FXML private Label lblErrorImage;
    @FXML private Label lblErrorPdf;

    @FXML
    public void initialize() {
        comboType.getItems().addAll("Livre", "Magazine", "Journal", "Manuel");
        comboLang.getItems().addAll("Français", "Anglais", "Arabe", "Espagnol");
        
        setupLibraryCombo();

        // Listeners for real-time error hiding
        txtTitle.textProperty().addListener((o, ov, nv) -> hideError(lblErrorTitle));
        txtAuthor.textProperty().addListener((o, ov, nv) -> hideError(lblErrorAuthor));
        txtSummary.textProperty().addListener((o, ov, nv) -> hideError(lblErrorSummary));
        txtMinAge.textProperty().addListener((o, ov, nv) -> hideError(lblErrorMinAge));
        txtMaxAge.textProperty().addListener((o, ov, nv) -> hideError(lblErrorMaxAge));
        comboLibrary.valueProperty().addListener((o, ov, nv) -> hideError(lblErrorLibrary));
        comboType.valueProperty().addListener((o, ov, nv) -> hideError(lblErrorType));
        comboLang.valueProperty().addListener((o, ov, nv) -> hideError(lblErrorLang));

        Object data = Router.getTransitData();
        if (data instanceof Resource) {
            currentResource = (Resource) data;
            lblHeaderTitle.setText("✏️ Modifier la ressource");
            lblHeaderDesc.setText("Modifiez les informations de « " + currentResource.getTitle() + " »");
            fillForm();
        } else if (data instanceof String) {
            String prefilledTitle = (String) data;
            currentResource = new Resource();
            txtTitle.setText(prefilledTitle);
            lblHeaderTitle.setText("📄 Nouvelle ressource");
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
                return null;
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
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.webp", "*.gif"));
        Window window = comboLibrary.getScene().getWindow();
        File file = fc.showOpenDialog(window);
        if (file != null) {
            selectedImagePath = file.getAbsolutePath();
            lblImageName.setText(file.getName());
            hideError(lblErrorImage);
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
            hideError(lblErrorPdf);
        }
    }

    private void showError(Label label, String msg) {
        if (label != null) {
            label.setText("⚠️ " + msg);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    private void hideError(Label label) {
        if (label != null) {
            label.setVisible(false);
            label.setManaged(false);
        }
    }

    private boolean validateForm() {
        boolean valid = true;
        boolean isEdit = (currentResource.getId() != 0);

        String regex = "^[a-zA-Z0-9À-ÿ\\s\\-_.,'!?():;\\\"+&]+$";

        String title = txtTitle.getText() != null ? txtTitle.getText().trim() : "";
        if (title.isEmpty()) {
            showError(lblErrorTitle, "Le titre est obligatoire."); valid = false;
        } else {
            if (title.length() < 3) {
                showError(lblErrorTitle, "Minimum 3 caractères."); valid = false;
            } else if (title.length() > 100) {
                showError(lblErrorTitle, "Maximum 100 caractères."); valid = false;
            } else if (!title.matches(regex)) {
                showError(lblErrorTitle, "Caractères spéciaux non autorisés."); valid = false;
            } else if (currentResource.getId() == 0 && resourceService.existsByTitle(title)) {
                showError(lblErrorTitle, "Une ressource avec ce titre existe déjà."); valid = false;
            }
        }

        String author = txtAuthor.getText() != null ? txtAuthor.getText().trim() : "";
        if (author.isEmpty()) {
            showError(lblErrorAuthor, "L'auteur est obligatoire."); valid = false;
        } else {
            if (author.length() < 3) {
                showError(lblErrorAuthor, "Minimum 3 caractères."); valid = false;
            } else if (author.length() > 100) {
                showError(lblErrorAuthor, "Maximum 100 caractères."); valid = false;
            } else if (!author.matches(regex)) {
                showError(lblErrorAuthor, "Caractères spéciaux non autorisés."); valid = false;
            }
        }

        String summary = txtSummary.getText() != null ? txtSummary.getText().trim() : "";
        if (summary.isEmpty()) {
            showError(lblErrorSummary, "Le résumé est obligatoire."); valid = false;
        } else {
            if (summary.length() < 10) {
                showError(lblErrorSummary, "Le résumé doit contenir au moins 10 caractères."); valid = false;
            } else if (!summary.matches(regex)) {
                showError(lblErrorSummary, "Caractères spéciaux non autorisés."); valid = false;
            }
        }

        if (comboLibrary.getValue() == null) {
            showError(lblErrorLibrary, "Veuillez sélectionner une bibliothèque."); valid = false;
        }

        if (comboType.getValue() == null || comboType.getValue().trim().isEmpty()) {
            showError(lblErrorType, "Un type est requis."); valid = false;
        }

        if (comboLang.getValue() == null || comboLang.getValue().trim().isEmpty()) {
            showError(lblErrorLang, "Une langue est requise."); valid = false;
        }

        int minAge = -1;
        try {
            minAge = Integer.parseInt(txtMinAge.getText().trim());
            if (minAge < 0) { showError(lblErrorMinAge, "Doit être >= 0."); valid = false; }
        } catch (Exception e) {
            showError(lblErrorMinAge, "Nombre entier requis."); valid = false;
        }

        int maxAge = -1;
        try {
            maxAge = Integer.parseInt(txtMaxAge.getText().trim());
            if (maxAge < 0) { showError(lblErrorMaxAge, "Doit être >= 0."); valid = false; }
        } catch (Exception e) {
            showError(lblErrorMaxAge, "Nombre entier requis."); valid = false;
        }

        if (minAge >= 0 && maxAge >= 0 && minAge > maxAge) {
            showError(lblErrorMaxAge, "L'âge max doit être >= à l'âge min."); valid = false;
        }

        // Files validation
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            String ext = selectedImagePath.toLowerCase();
            if (!ext.endsWith(".png") && !ext.endsWith(".jpg") && !ext.endsWith(".jpeg")) {
                showError(lblErrorImage, "Format accepté : PNG, JPG, JPEG."); valid = false;
            }
        } else if (!isEdit) {
            showError(lblErrorImage, "L'image de couverture est requise."); valid = false;
        }

        if (!isEdit && (selectedPdfPath == null || selectedPdfPath.isEmpty())) {
            showError(lblErrorPdf, "Le document PDF est requis."); valid = false;
        }

        return valid;
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
        if (!validateForm()) return;

        currentResource.setTitle(txtTitle.getText().trim());
        currentResource.setAuthor(txtAuthor.getText().trim());
        currentResource.setSummary(txtSummary.getText() != null ? txtSummary.getText().trim() : "");
        currentResource.setType(comboType.getValue());
        currentResource.setLanguage(comboLang.getValue());
        currentResource.setLibraryId(comboLibrary.getValue().getId());
        
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            currentResource.setCoverImage(selectedImagePath);
        }
        if (selectedPdfPath != null && !selectedPdfPath.isEmpty()) {
            currentResource.setPdfFile(selectedPdfPath);
        }
        
        currentResource.setMinAge(Integer.parseInt(txtMinAge.getText().trim()));
        currentResource.setMaxAge(Integer.parseInt(txtMaxAge.getText().trim()));

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
