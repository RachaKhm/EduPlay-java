package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.services.EventRegistrationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class RegistrationDetailController {

    @FXML private Button backBtn;
    @FXML private Label childNameValue;
    @FXML private Label statusValue;
    @FXML private Label registeredAtValue;
    @FXML private Label eventValue;
    @FXML private Label phoneValue;

    private EventRegistration currentRegistration;

    @FXML
    public void initialize() {
        backBtn.setOnAction(e -> goBack());
    }


    public void setRegistrationId(int registrationId) {
        try {
            EventRegistrationService service = new EventRegistrationService();
            EventRegistration registration = service.recupererParId(registrationId);
            if (registration != null) {
                setRegistration(registration);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setRegistration(EventRegistration registration) {
        this.currentRegistration = registration;
        displayRegistrationInfo();
    }

    private void displayRegistrationInfo() {
        if (currentRegistration == null) return;

        childNameValue.setText("👶 Enfant: " + currentRegistration.getChildFullName());
        statusValue.setText("🏷️ Statut: " + currentRegistration.getStatus());
        registeredAtValue.setText("📅 Inscrit le: " + currentRegistration.getRegisteredAt());

        if (currentRegistration.getEvent() != null) {
            eventValue.setText("📌 Événement: " + currentRegistration.getEvent().getTitle());
        }

        if (currentRegistration.getParentPhone() != null) {
            phoneValue.setText("📞 Téléphone: " + currentRegistration.getParentPhone());
        }
    }

    private void goBack() {
        Router.go("registration_list");
    }
}