package dev.eduplay.controllers.event;

import dev.eduplay.controllers.event.MainController;
import dev.eduplay.entities.EventRegistration;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RegistrationDetailController {

    @FXML private Button backBtn;
    @FXML private Label childNameValue;
    @FXML private Label statusValue;
    @FXML private Label registeredAtValue;
    @FXML private Label eventValue;
    @FXML private Label phoneValue;

    private MainController mainController;
    private EventRegistration currentRegistration;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        childNameValue.setText("👶 Enfant: " + registration.getChildFullName());
        statusValue.setText("🏷️ Statut: " + registration.getStatus());
        registeredAtValue.setText("📅 Inscrit le: " + registration.getRegisteredAt());

        if (registration.getEvent() != null) {
            eventValue.setText("📌 Événement: " + registration.getEvent().getTitle());
        }

        if (registration.getParentPhone() != null) {
            phoneValue.setText("📞 Téléphone: " + registration.getParentPhone());
        }
    }

    private void goBack() {
        if (mainController != null) {
            mainController.goToRegistrationList();
        }
    }
}