package interva.sambikopi.controller;

import interva.sambikopi.model.OrderItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OrdersController {

    @FXML private TextField orderSearchField;
    @FXML private Label orderStatusLabel;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> colOrderId;
    @FXML private TableColumn<OrderItem, String> colCustomer;
    @FXML private TableColumn<OrderItem, String> colProduct;
    @FXML private TableColumn<OrderItem, String> colCreationDate;
    @FXML private TableColumn<OrderItem, String> colPayment;
    @FXML private TableColumn<OrderItem, String> colStatus;

    private FilteredList<OrderItem> filteredOrderData;
    private boolean activeOnlyFilter;

    @FXML
    private void initialize() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCreationDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));

        filteredOrderData = new FilteredList<>(SambiKopiDataStore.getOrders(), order -> true);
        orderTable.setItems(filteredOrderData);

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, selectedOrder) -> {
            if (selectedOrder != null) {
                setStatus("Selected order: " + selectedOrder.getOrderId() + " - " + selectedOrder.getCustomer(), false);
            }
        });

        int newOrders = SambiKopiDataStore.consumeBaristaNotificationCount();
        if (newOrders > 0) {
            setStatus("New cashier order received: " + newOrders + " order(s) waiting.", false);
        } else {
            setStatus("Barista Orders ready.", false);
        }
    }

    @FXML
    private void handleSearchOrders() {
        applyFilters();
        String keyword = orderSearchField.getText().trim();
        setStatus(keyword.isEmpty() ? "Showing all orders." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleFilterOrders() {
        activeOnlyFilter = !activeOnlyFilter;
        applyFilters();
        setStatus(activeOnlyFilter ? "Showing active orders only." : "Filter cleared.", false);
    }

    @FXML
    private void handleRefreshOrders() {
        orderSearchField.clear();
        activeOnlyFilter = false;
        filteredOrderData.setPredicate(order -> true);
        orderTable.refresh();
        setStatus("Orders refreshed.", false);
    }

    @FXML
    private void handleCompleteOrder() {
        updateSelectedOrderStatus("Complete", "Order completed: ");
    }

    @FXML
    private void handleAssignOrder() {
        updateSelectedOrderStatus("Assigned", "Order assigned: ");
    }

    @FXML
    private void handleCancelOrder() {
        updateSelectedOrderStatus("Cancelled", "Order cancelled: ");
    }

    private void updateSelectedOrderStatus(String status, String successMessage) {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an order first.", true);
            return;
        }
        selected.setStatus(status);
        orderTable.refresh();
        setStatus(successMessage + selected.getOrderId(), status.equals("Cancelled"));
    }

    private void applyFilters() {
        String keyword = orderSearchField.getText().trim().toLowerCase();
        filteredOrderData.setPredicate(order -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || order.getOrderId().toLowerCase().contains(keyword)
                    || order.getCustomer().toLowerCase().contains(keyword)
                    || order.getProduct().toLowerCase().contains(keyword)
                    || order.getCreationDate().toLowerCase().contains(keyword)
                    || order.getPaymentMethod().toLowerCase().contains(keyword)
                    || order.getStatus().toLowerCase().contains(keyword);

            boolean matchesActiveFilter = !activeOnlyFilter
                    || (!order.getStatus().equalsIgnoreCase("Complete")
                    && !order.getStatus().equalsIgnoreCase("Cancelled"));

            return matchesKeyword && matchesActiveFilter;
        });
    }

    private void setStatus(String message, boolean error) {
        orderStatusLabel.setText(message);
        orderStatusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
