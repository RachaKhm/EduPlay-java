package dev.eduplay.controllers;

import dev.eduplay.core.Router;
import dev.eduplay.entities.User;
import dev.eduplay.services.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserListController
 * ─────────────────────────────────────────────────────────────
 * Liste paginée des utilisateurs avec recherche et filtres.
 * Styles des badges : définis dans app.css, appliqués via
 * getStyleClass().add("badge-admin") etc
 * ─────────────────────────────────────────────────────────────
 */
public class UserListController {

    /* ── FXML bindings ─────────────────────────────────────── */

    @FXML private TableView<User>             userTable;
    @FXML private TableColumn<User, Integer>  colId;
    @FXML private TableColumn<User, String>   colFirstName;
    @FXML private TableColumn<User, String>   colLastName;
    @FXML private TableColumn<User, String>   colEmail;
    @FXML private TableColumn<User, String>   colType;
    @FXML private TableColumn<User, Boolean>  colActive;
    @FXML private TableColumn<User, Void>     colActions;

    @FXML private TextField       searchField;
    @FXML private ComboBox<String> typeFilter;
    @FXML private Label           statusLabel;

    /* ── Services et état ───────────────────────────────────── */

    private final UserService              userService = new UserService();
    private final ObservableList<User>     allUsers    = FXCollections.observableArrayList();

    /* ── Initialisation ────────────────────────────────────── */

    @FXML
    public void initialize() {
        setupColumns();
        setupActionColumn();

        typeFilter.getItems().addAll("Tous", "admin", "enseignant", "parent", "enfant");
        typeFilter.setValue("Tous");
        typeFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        loadUsers();
    }

    /* ── Configuration des colonnes ────────────────────────── */

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Colonne Type : badge coloré (style CSS)
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) { setGraphic(null); return; }
                Label badge = new Label(type.toUpperCase());
                badge.getStyleClass().add(resolveBadgeClass(type));
                setGraphic(badge);
                setText(null);
            }
        });

        // Colonne Statut : badge actif/inactif (style CSS)
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));
        colActive.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean active, boolean empty) {
                super.updateItem(active, empty);
                if (empty || active == null) { setGraphic(null); return; }
                Label badge = new Label(active ? "Actif" : "Inactif");
                badge.getStyleClass().add(active ? "badge-actif" : "badge-inactif");
                setGraphic(badge);
                setText(null);
            }
        });
    }

    /* ── Colonne Actions ───────────────────────────────────── */

    private void setupActionColumn() {
        colActions.setCellFactory(new Callback<>() {
            @Override
            public TableCell<User, Void> call(TableColumn<User, Void> col) {
                return new TableCell<>() {
                    private final Button btnEdit   = new Button("Modifier");
                    private final Button btnDelete = new Button("Suppr.");
                    private final HBox   box       = new HBox(6, btnEdit, btnDelete);

                    {
                        // Styles via CSS classes (app.css)
                        btnEdit.getStyleClass().add("btn-icon-edit");
                        btnDelete.getStyleClass().add("btn-icon-delete");
                        box.setStyle("-fx-alignment: CENTER;");

                        btnEdit.setOnAction(e -> {
                            User u = getTableView().getItems().get(getIndex());
                            openForm(u);
                        });

                        btnDelete.setOnAction(e -> {
                            User u = getTableView().getItems().get(getIndex());
                            confirmAndDelete(u);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : box);
                    }
                };
            }
        });
    }

    /* ── Chargement des données ────────────────────────────── */

    private void loadUsers() {
        allUsers.setAll(userService.recuperer());
        userTable.setItems(allUsers);
        updateStatus();
    }

    /* ── Filtres et recherche ──────────────────────────────── */

    @FXML
    public void search() {
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText().toLowerCase().trim();
        String type    = typeFilter.getValue();

        List<User> filtered = allUsers.stream()
                .filter(u -> "Tous".equals(type) || type.equals(u.getType()))
                .filter(u -> keyword.isEmpty()
                        || containsIgnoreCase(u.getFirstName(), keyword)
                        || containsIgnoreCase(u.getLastName(),  keyword)
                        || containsIgnoreCase(u.getEmail(),     keyword))
                .collect(Collectors.toList());

        userTable.setItems(FXCollections.observableArrayList(filtered));
        updateStatus();
    }

    /* ── Actions du tableau ────────────────────────────────── */

    @FXML
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez un utilisateur.");
            return;
        }
        confirmAndDelete(selected);
    }

    private void confirmAndDelete(User user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Supprimer " + user.getFullName() + " définitivement ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.supprimer(user);
            allUsers.remove(user);
            applyFilters();
            showAlert(Alert.AlertType.INFORMATION, "Utilisateur supprimé.");
        }
    }

    @FXML
    public void toggleActiveUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez un utilisateur.");
            return;
        }
        selected.setActive(!selected.isActive());
        userService.modifier(selected);
        userTable.refresh();
    }

    @FXML
    public void refreshList() {
        searchField.clear();
        typeFilter.setValue("Tous");
        loadUsers();
    }

    /* ── Ouverture du formulaire ────────────────────────────── */

    @FXML
    public void openAddUserForm() {
        openForm(null);
    }

    @FXML
    public void editSelectedUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Sélectionnez un utilisateur à modifier.");
            return;
        }
        openForm(selected);
    }

    /**
     * Ouvre le formulaire utilisateur en fenêtre modale.
     *
     * @param user null = mode création, non-null = mode édition
     */
    private void openForm(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/child/UserFormView.fxml"));
            Parent root = loader.load();

            UserFormController ctrl = loader.getController();
            if (user != null) ctrl.setUser(user);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(user == null
                    ? "Nouvel utilisateur"
                    : "Modifier — " + user.getFullName());
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> loadUsers()); // rafraîchir après fermeture
            stage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    /* ── Utilitaires ───────────────────────────────────────── */

    private void updateStatus() {
        int count = userTable.getItems().size();
        if (statusLabel != null) {
            statusLabel.setText(count + " utilisateur"
                    + (count > 1 ? "s" : "")
                    + " affiché"
                    + (count > 1 ? "s" : ""));
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        new Alert(type, message, ButtonType.OK).showAndWait();
    }

    /** Classe CSS du badge selon le type utilisateur. */
    private String resolveBadgeClass(String type) {
        return switch (type) {
            case "admin"      -> "badge-admin";
            case "enseignant" -> "badge-enseignant";
            case "parent"     -> "badge-parent";
            case "enfant"     -> "badge-enfant";
            default           -> "badge-default";
        };
    }

    private boolean containsIgnoreCase(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }
}