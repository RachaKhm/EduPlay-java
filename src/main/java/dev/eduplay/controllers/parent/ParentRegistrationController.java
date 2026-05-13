package dev.eduplay.controllers.parent;

import dev.eduplay.core.AppContext;
import dev.eduplay.core.Router;
import dev.eduplay.entities.EventRegistration;
import dev.eduplay.entities.SchoolEvent;
import dev.eduplay.entities.User;
import dev.eduplay.services.EventRegistrationService;
import dev.eduplay.services.SchoolEventService;
import dev.eduplay.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class ParentRegistrationController {

    @FXML private Button backBtn;
    @FXML private Button submitBtn;
    @FXML private Button addChildBtn;
    @FXML private Label eventTitleLabel;
    @FXML private ComboBox<User> childCombo;
    @FXML private TextField parentPhoneField;
    @FXML private TextField classLevelField;
    @FXML private TextArea medicalNotesArea;
    @FXML private TextField emergencyNameField;
    @FXML private TextField emergencyPhoneField;
    @FXML private TextArea notesArea;
    @FXML private Label messageLabel;
    @FXML private VBox newChildForm;
    @FXML private TextField newChildFirstName;
    @FXML private TextField newChildLastName;
    @FXML private DatePicker newChildBirthDate;
    @FXML private Button saveNewChildBtn;
    @FXML private Button cancelNewChildBtn;
    @FXML private Label selectedChildrenLabel;
    @FXML private ListView<User> selectedChildrenList;

    private EventRegistrationService registrationService;
    private SchoolEventService eventService;
    private UserService userService;
    private int eventId;
    private ObservableList<User> availableChildrenList;
    private ObservableList<User> selectedChildrenListData;

    @FXML
    public void initialize() {
        System.out.println("ParentRegistrationController initialisé");
        registrationService = new EventRegistrationService();
        eventService = new SchoolEventService();
        userService = new UserService();
        availableChildrenList = FXCollections.observableArrayList();
        selectedChildrenListData = FXCollections.observableArrayList();

        backBtn.setOnAction(e -> Router.go("parent_event_detail", eventId));
        submitBtn.setOnAction(e -> submitRegistrations());
        addChildBtn.setOnAction(e -> showAddChildForm());

        setupPhoneValidation();
        chargerEnfants();

        if (newChildForm != null) {
            newChildForm.setVisible(false);
            newChildForm.setManaged(false);
        }

        if (selectedChildrenList != null) {
            selectedChildrenList.setItems(selectedChildrenListData);
            selectedChildrenList.setCellFactory(lv -> new ListCell<User>() {
                @Override
                protected void updateItem(User enfant, boolean empty) {
                    super.updateItem(enfant, empty);
                    if (empty || enfant == null) {
                        setText(null);
                    } else {
                        setText(enfant.getFirstName() + " " + enfant.getLastName());
                        setStyle("-fx-padding: 5;");
                    }
                }
            });
        }

        if (saveNewChildBtn != null) {
            saveNewChildBtn.setOnAction(e -> saveNewChild());
        }
        if (cancelNewChildBtn != null) {
            cancelNewChildBtn.setOnAction(e -> cancelNewChild());
        }
    }

    private void setupPhoneValidation() {
        parentPhoneField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !isValidPhone(newVal)) {
                parentPhoneField.setStyle("-fx-border-color: #e74c3c; -fx-border-width: 2;");
            } else {
                parentPhoneField.setStyle("");
            }
        });
    }

    private boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String trimmed = phone.trim();
        if (!trimmed.matches("\\d{8}")) return false;
        char firstChar = trimmed.charAt(0);
        return firstChar == '2' || firstChar == '4' || firstChar == '5' || firstChar == '9';
    }

    private void chargerEnfants() {
        User currentUser = AppContext.getCurrentUser();
        if (currentUser == null) {
            showError("Utilisateur non connecté");
            return;
        }

        // ✅ Utilisation de la méthode existante getChildrenByParentId
        List<User> enfants = userService.getChildrenByParentId(currentUser.getId());

        availableChildrenList.clear();
        availableChildrenList.addAll(enfants);

        childCombo.setItems(availableChildrenList);
        childCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User enfant, boolean empty) {
                super.updateItem(enfant, empty);
                if (empty || enfant == null) {
                    setText(null);
                } else {
                    setText(enfant.getFirstName() + " " + enfant.getLastName());
                }
            }
        });

        childCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User enfant, boolean empty) {
                super.updateItem(enfant, empty);
                if (empty || enfant == null) {
                    setText("Sélectionnez un enfant");
                } else {
                    setText(enfant.getFirstName() + " " + enfant.getLastName());
                }
            }
        });

        updateSelectedChildrenLabel();

        if (enfants.isEmpty()) {
            showError("Aucun enfant trouvé. Cliquez sur '+' pour en ajouter un.");
        }
    }

    @FXML
    private void addChildToSelection() {
        User selectedChild = childCombo.getValue();
        if (selectedChild == null) {
            showError("Veuillez sélectionner un enfant");
            return;
        }

        if (selectedChildrenListData.contains(selectedChild)) {
            showError("Cet enfant est déjà dans la liste");
            return;
        }

        selectedChildrenListData.add(selectedChild);
        updateSelectedChildrenLabel();
        showSuccess("Enfant ajouté à la liste");
    }

    @FXML
    private void removeChildFromSelection() {
        User selectedChild = selectedChildrenList.getSelectionModel().getSelectedItem();
        if (selectedChild == null) {
            showError("Veuillez sélectionner un enfant à retirer");
            return;
        }
        selectedChildrenListData.remove(selectedChild);
        updateSelectedChildrenLabel();
        showSuccess("Enfant retiré de la liste");
    }

    private void updateSelectedChildrenLabel() {
        if (selectedChildrenLabel != null) {
            int count = selectedChildrenListData.size();
            selectedChildrenLabel.setText(count + " enfant(s) sélectionné(s)");
        }
    }

    private void showAddChildForm() {
        if (newChildForm != null) {
            newChildForm.setVisible(true);
            newChildForm.setManaged(true);
            if (newChildFirstName != null) newChildFirstName.clear();
            if (newChildLastName != null) newChildLastName.clear();
            if (newChildBirthDate != null) newChildBirthDate.setValue(null);
        }
    }

    private void saveNewChild() {
        if (newChildFirstName == null || newChildLastName == null) {
            showError("Erreur: formulaire non disponible");
            return;
        }

        String firstName = newChildFirstName.getText().trim();
        String lastName = newChildLastName.getText().trim();

        if (firstName.isEmpty()) {
            showError("Veuillez saisir le prénom de l'enfant");
            newChildFirstName.requestFocus();
            return;
        }

        if (lastName.isEmpty()) {
            showError("Veuillez saisir le nom de l'enfant");
            newChildLastName.requestFocus();
            return;
        }

        try {
            User currentUser = AppContext.getCurrentUser();
            if (currentUser == null) {
                showError("Utilisateur non connecté");
                return;
            }

            User newChild = new User();
            newChild.setFirstName(firstName);
            newChild.setLastName(lastName);
            if (newChildBirthDate != null && newChildBirthDate.getValue() != null) {
                newChild.setBirthDate(newChildBirthDate.getValue());
            }
            newChild.setType("enfant");
            newChild.setParentId(currentUser.getId());
            newChild.setActive(true);
            newChild.setCreatedAt(LocalDateTime.now());

            userService.ajouter(newChild);

            showSuccess("✅ Enfant ajouté avec succès !");

            if (newChildForm != null) {
                newChildForm.setVisible(false);
                newChildForm.setManaged(false);
            }

            chargerEnfants();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Erreur lors de l'ajout: " + e.getMessage());
        }
    }

    private void cancelNewChild() {
        if (newChildForm != null) {
            newChildForm.setVisible(false);
            newChildForm.setManaged(false);
        }
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
        try {
            SchoolEvent event = eventService.recupererParId(eventId);
            if (event != null) {
                eventTitleLabel.setText("🎉 " + event.getTitle());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setEventTitle(String eventTitle) {
        eventTitleLabel.setText("🎉 " + eventTitle);
    }

    public void refreshEnfants() {
        chargerEnfants();
    }

    // ✅ Vérification si un enfant est déjà inscrit à un événement
    private boolean isChildAlreadyRegistered(int parentId, int eventId, String childFullName) throws SQLException {
        EventRegistrationService regService = new EventRegistrationService();
        List<EventRegistration> registrations = regService.recupererParParentId(parentId);

        for (EventRegistration reg : registrations) {
            if (reg.getEvent() != null && reg.getEvent().getId() == eventId
                    && reg.getChildFullName() != null && reg.getChildFullName().equals(childFullName)) {
                return true;
            }
        }
        return false;
    }

    private void submitRegistrations() {
        String parentPhone = parentPhoneField.getText().trim();

        if (selectedChildrenListData.isEmpty()) {
            showError("Veuillez ajouter au moins un enfant à la liste");
            return;
        }

        if (parentPhone.isEmpty()) {
            showError("Veuillez saisir le téléphone parent");
            parentPhoneField.requestFocus();
            return;
        }

        if (!isValidPhone(parentPhone)) {
            showError("Le téléphone doit contenir 8 chiffres et commencer par 2, 4, 5 ou 9");
            parentPhoneField.requestFocus();
            return;
        }

        try {
            SchoolEvent event = eventService.recupererParId(eventId);
            if (event == null) {
                showError("Événement non trouvé");
                return;
            }

            int remaining = event.getMaxCapacity() - event.getCurrentRegistrations();

            if (selectedChildrenListData.size() > remaining) {
                showError("Vous avez sélectionné " + selectedChildrenListData.size() + " enfants, mais il ne reste que " + remaining + " place(s).");
                return;
            }

            User currentUser = AppContext.getCurrentUser();
            if (currentUser == null) {
                showError("Utilisateur non connecté");
                return;
            }

            int successCount = 0;
            for (User child : selectedChildrenListData) {
                String childFullName = child.getFirstName() + " " + child.getLastName();

                // ✅ Vérification sans modifier UserService
                if (isChildAlreadyRegistered(currentUser.getId(), eventId, childFullName)) {
                    showError("L'enfant " + childFullName + " est déjà inscrit à cet événement");
                    continue;
                }

                EventRegistration registration = new EventRegistration();
                registration.setEvent(event);
                registration.setParent(currentUser);
                registration.setRegisteredAt(LocalDateTime.now());
                registration.setChildFullName(childFullName);
                registration.setParentPhone(parentPhone);
                registration.setChildClassLevel(classLevelField.getText().trim().isEmpty() ? null : classLevelField.getText().trim());
                registration.setMedicalNotes(medicalNotesArea.getText().trim().isEmpty() ? null : medicalNotesArea.getText().trim());
                registration.setEmergencyContactName(emergencyNameField.getText().trim().isEmpty() ? null : emergencyNameField.getText().trim());
                registration.setEmergencyContactPhone(emergencyPhoneField.getText().trim().isEmpty() ? null : emergencyPhoneField.getText().trim());
                registration.setNotes(notesArea.getText().trim().isEmpty() ? null : notesArea.getText().trim());
                registration.setScannedAt(null);
                registration.setReminderSent(false);

                registrationService.ajouter(registration);
                successCount++;
            }

            if (successCount > 0) {
                showSuccess("✅ Inscription réussie pour " + successCount + " enfant(s) !");

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        javafx.application.Platform.runLater(() -> {
                            Router.go("parent_registrations");
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            showError("Erreur lors de l'inscription: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        messageLabel.setText("❌ " + message);
        messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        messageLabel.setVisible(true);

        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> messageLabel.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showSuccess(String message) {
        messageLabel.setText("✅ " + message);
        messageLabel.setStyle("-fx-text-fill: #27ae60;");
        messageLabel.setVisible(true);
    }
}