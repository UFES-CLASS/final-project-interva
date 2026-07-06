package interva.sambikopi.controller;

import interva.sambikopi.model.OrderItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class CashierReportController {

    @FXML private Label totalOrdersLabel;
    @FXML private Label waitingOrdersLabel;
    @FXML private Label completedOrdersLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label statusLabel;

    @FXML
    private void initialize() {
        handleRefreshReport();
    }

    @FXML
    private void handleRefreshReport() {
        int totalOrders = SambiKopiDataStore.getOrders().size();
        long waitingOrders = SambiKopiDataStore.getOrders().stream()
                .filter(order -> order.getStatus().equalsIgnoreCase("Waiting"))
                .count();
        long completedOrders = SambiKopiDataStore.getOrders().stream()
                .filter(order -> order.getStatus().equalsIgnoreCase("Complete"))
                .count();
        int totalRevenue = SambiKopiDataStore.getOrders().stream()
                .filter(order -> order.getStatus().equalsIgnoreCase("Complete") || order.getStatus().equalsIgnoreCase("Waiting") || order.getStatus().equalsIgnoreCase("Assigned"))
                .mapToInt(OrderItem::getTotalAmount)
                .sum();

        totalOrdersLabel.setText(String.valueOf(totalOrders));
        waitingOrdersLabel.setText(String.valueOf(waitingOrders));
        completedOrdersLabel.setText(String.valueOf(completedOrders));
        totalRevenueLabel.setText(SambiKopiDataStore.formatIdr(totalRevenue));
        statusLabel.setText("Report updated.");
        statusLabel.setStyle("-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
