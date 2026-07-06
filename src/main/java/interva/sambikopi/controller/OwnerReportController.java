package interva.sambikopi.controller;

import interva.sambikopi.model.OrderItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Map;
import java.util.stream.Collectors;

public class OwnerReportController {

    @FXML private Label totalOrdersLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label pendingMenuLabel;
    @FXML private Label notifiedStockLabel;
    @FXML private BarChart<String, Number> orderStatusChart;
    @FXML private PieChart paymentChart;

    @FXML
    private void initialize() {
        refreshReport();
    }

    @FXML
    private void handleRefreshReport() {
        refreshReport();
    }

    private void refreshReport() {
        int totalOrders = SambiKopiDataStore.getOrders().size();
        int totalSales = SambiKopiDataStore.getOrders().stream()
                .filter(order -> !order.getStatus().equalsIgnoreCase("Cancelled"))
                .mapToInt(OrderItem::getTotalAmount)
                .sum();

        totalOrdersLabel.setText(String.valueOf(totalOrders));
        totalSalesLabel.setText(SambiKopiDataStore.formatIdr(totalSales));
        pendingMenuLabel.setText(String.valueOf(SambiKopiDataStore.getMenuReviewItems().size()));
        notifiedStockLabel.setText(String.valueOf(SambiKopiDataStore.getStockReviewItems().size()));

        orderStatusChart.getData().clear();
        XYChart.Series<String, Number> orderSeries = new XYChart.Series<>();
        orderSeries.setName("Orders");

        Map<String, Long> statusCounts = SambiKopiDataStore.getOrders().stream()
                .collect(Collectors.groupingBy(OrderItem::getStatus, Collectors.counting()));

        orderSeries.getData().add(new XYChart.Data<>("Waiting", statusCounts.getOrDefault("Waiting", 0L)));
        orderSeries.getData().add(new XYChart.Data<>("Assigned", statusCounts.getOrDefault("Assigned", 0L)));
        orderSeries.getData().add(new XYChart.Data<>("Complete", statusCounts.getOrDefault("Complete", 0L)));
        orderSeries.getData().add(new XYChart.Data<>("Cancelled", statusCounts.getOrDefault("Cancelled", 0L)));
        orderStatusChart.getData().add(orderSeries);

        Map<String, Long> paymentCounts = SambiKopiDataStore.getOrders().stream()
                .collect(Collectors.groupingBy(OrderItem::getPaymentMethod, Collectors.counting()));

        paymentChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Cash", paymentCounts.getOrDefault("Cash", 0L)),
                new PieChart.Data("Card", paymentCounts.getOrDefault("Card", 0L)),
                new PieChart.Data("QRIS", paymentCounts.getOrDefault("QRIS", 0L))
        ));
    }
}
