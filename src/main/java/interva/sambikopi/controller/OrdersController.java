package interva.sambikopi.controller;

import interva.sambikopi.model.OrderItem;
import interva.sambikopi.model.SambiKopiDataStore;
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
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredOrderData = new FilteredList<>(SambiKopiDataStore.getOrders(), order -> true);
        orderTable.setItems(filteredOrderData);

        selectQueueFront();

        int newOrders = SambiKopiDataStore.consumeBaristaNotificationCount();
        if (newOrders > 0) {
            setStatus("New cashier order received: " + newOrders + " order(s) waiting. Queue mode: process the front order first.", false);
        } else {
            showQueueFrontStatus("Queue mode enabled. Process the front order first.");
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
        setStatus(activeOnlyFilter ? "Showing active orders only. Queue order is still enforced." : "Filter cleared. Queue order is still enforced.", false);
        selectQueueFront();
    }

    @FXML
    private void handleRefreshOrders() {
        orderSearchField.clear();
        activeOnlyFilter = false;
        filteredOrderData.setPredicate(order -> true);
        orderTable.refresh();
        selectQueueFront();
        showQueueFrontStatus("Orders refreshed.");
    }

    @FXML
    private void handleCompleteOrder() {
        OrderItem selected = getSelectedQueueFrontOrder();
        if (selected == null) {
            return;
        }

        SambiKopiDataStore.StockDeductionResult result = SambiKopiDataStore.completeOrderAndDeductStock(selected);
        orderTable.refresh();
        if (result.isSuccess()) {
            selectQueueFront();
            setStatus(result.getMessage() + " " + selected.getOrderId() + ". Next order is now at the front of the queue.", false);
        } else {
            setStatus(result.getMessage(), true);
        }
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
        OrderItem selected = getSelectedQueueFrontOrder();
        if (selected == null) {
            return;
        }
        SambiKopiDataStore.updateOrderStatus(selected, status);
        orderTable.refresh();
        selectQueueFront();
        if (status.equals("Cancelled")) {
            setStatus(successMessage + selected.getOrderId() + ". Next order is now at the front of the queue.", true);
        } else {
            setStatus(successMessage + selected.getOrderId() + ". This order remains at the front until completed or cancelled.", false);
        }
    }


    private OrderItem getSelectedQueueFrontOrder() {
        OrderItem queueFront = SambiKopiDataStore.peekNextOrderInQueue();
        if (queueFront == null) {
            setStatus("All orders have already been completed or cancelled.", false);
            return null;
        }

        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            orderTable.getSelectionModel().select(queueFront);
            return queueFront;
        }

        if (selected != queueFront) {
            orderTable.getSelectionModel().select(queueFront);
            setStatus("Queue rule: process " + queueFront.getOrderId() + " first before handling " + selected.getOrderId() + ".", true);
            return null;
        }

        return selected;
    }

    private void selectQueueFront() {
        OrderItem queueFront = SambiKopiDataStore.peekNextOrderInQueue();
        if (queueFront != null) {
            orderTable.getSelectionModel().select(queueFront);
        } else {
            orderTable.getSelectionModel().clearSelection();
        }
    }

    private void showQueueFrontStatus(String prefix) {
        OrderItem queueFront = SambiKopiDataStore.peekNextOrderInQueue();
        if (queueFront == null) {
            setStatus(prefix + " All orders are already complete or cancelled.", false);
        } else {
            setStatus(prefix + " Front of queue: " + queueFront.getOrderId() + " - " + queueFront.getCustomer() + ".", false);
        }
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
