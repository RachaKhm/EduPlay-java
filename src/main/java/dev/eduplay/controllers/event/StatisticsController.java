package dev.eduplay.controllers.event;

import dev.eduplay.core.Router;
import dev.eduplay.services.StatisticsService;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.Map;

public class StatisticsController {

    @FXML private Label totalEventsLabel;
    @FXML private Label totalRegistrationsLabel;
    @FXML private Label upcomingEventsLabel;
    @FXML private Label pastEventsLabel;
    @FXML private Label totalParticipantsLabel;
    @FXML private Label fillRateLabel;

    @FXML private BarChart<String, Number> registrationsChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<StatisticsService.EventStats> topEventsTable;
    @FXML private TableColumn<StatisticsService.EventStats, String> colEventTitle;
    @FXML private TableColumn<StatisticsService.EventStats, Integer> colRegistrations;
    @FXML private TableColumn<StatisticsService.EventStats, Integer> colMaxCapacity;
    @FXML private TableColumn<StatisticsService.EventStats, Double> colFillRate;
    @FXML private TableColumn<StatisticsService.EventStats, String> colEventDate;

    private StatisticsService statisticsService;

    @FXML
    public void initialize() {
        System.out.println("StatisticsController initialisé");
        statisticsService = new StatisticsService();

        setupTableColumns();
        loadStatistics();
    }

    private void setupTableColumns() {
        colEventTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colRegistrations.setCellValueFactory(new PropertyValueFactory<>("currentRegistrations"));
        colMaxCapacity.setCellValueFactory(new PropertyValueFactory<>("maxCapacity"));
        colFillRate.setCellValueFactory(new PropertyValueFactory<>("fillRate"));
        colEventDate.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
    }

    private void loadStatistics() {
        try {
            // Statistiques globales
            if (totalEventsLabel != null) {
                totalEventsLabel.setText(String.valueOf(statisticsService.getTotalEvents()));
            }
            if (totalRegistrationsLabel != null) {
                totalRegistrationsLabel.setText(String.valueOf(statisticsService.getTotalRegistrations()));
            }
            if (upcomingEventsLabel != null) {
                upcomingEventsLabel.setText(String.valueOf(statisticsService.getUpcomingEvents()));
            }
            if (pastEventsLabel != null) {
                pastEventsLabel.setText(String.valueOf(statisticsService.getPastEvents()));
            }
            if (totalParticipantsLabel != null) {
                totalParticipantsLabel.setText(String.valueOf(statisticsService.getTotalParticipants()));
            }

            double fillRate = statisticsService.getFillRate();
            if (fillRateLabel != null) {
                fillRateLabel.setText(String.format("%.1f%%", fillRate));
                fillRateLabel.setStyle(fillRate > 70 ? "-fx-text-fill: #10b981;" : "-fx-text-fill: #f59e0b;");
            }

            // Graphique des inscriptions par mois
            loadRegistrationsChart();

            // Top événements
            if (topEventsTable != null) {
                topEventsTable.setItems(javafx.collections.FXCollections.observableArrayList(
                        statisticsService.getTopEvents()
                ));
            }

        } catch (SQLException e) {
            System.err.println("Erreur chargement statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRegistrationsChart() {
        if (registrationsChart == null) return;

        try {
            registrationsChart.getData().clear();

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Inscriptions");

            Map<String, Integer> data = statisticsService.getRegistrationsByMonth();

            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            registrationsChart.getData().add(series);

        } catch (SQLException e) {
            System.err.println("Erreur chargement graphique: " + e.getMessage());
        }
    }

    @FXML
    private void refreshStats() {
        loadStatistics();
    }

    @FXML
    private void goBack() {
        Router.go("admin_dashboard");
    }

    @FXML
    private void showAllEvents() {
        Router.go("event_list");
    }

    @FXML
    private void showAllRegistrations() {
        Router.go("registration_list");
    }
}