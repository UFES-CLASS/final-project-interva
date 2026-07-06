package interva.sambikopi.controller;

import interva.sambikopi.App;
import interva.sambikopi.model.OrderItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class OrdersController {

    @FXML private Button dashboardButton;
    @FXML private Button menuButton;
    @FXML private Button menuListButton;
    @FXML private Button inventoryButton;
    @FXML private Button ordersButton;

    @FXML private TextField orderSearchField;
    @FXML private Button searchOrderButton;
    @FXML private Button filterButton;
    @FXML private Button refreshOrderButton;
    @FXML private Button completeOrderButton;
    @FXML private Button assignOrderButton;
    @FXML private Button cancelOrderButton;
    @FXML private Label orderStatusLabel;

    @FXML private Button addOrderButton;
    @FXML private VBox incomingOrderPane;
    @FXML private Button closeIncomingOrderButton;
    @FXML private Button acceptIncomingOrderButton;
    @FXML private Button rejectIncomingOrderButton;
    @FXML private Label incomingOrderIdLabel;
    @FXML private Label incomingCustomerLabel;
    @FXML private Label incomingProductLabel;
    @FXML private Label incomingDateLabel;
    @FXML private Label incomingStatusLabel;

    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> customerColumn;
    @FXML private TableColumn<OrderItem, String> productColumn;
    @FXML private TableColumn<OrderItem, String> creationDateColumn;
    @FXML private TableColumn<OrderItem, String> orderStatusColumn;

    private FilteredList<OrderItem> filteredOrderData;
    private boolean activeOnlyFilter = false;
    private OrderItem incomingOrder;

    @FXML
    private void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("product"));
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredOrderData = new FilteredList<>(SambiKopiDataStore.getOrders(), order -> true);
        orderTable.setItems(filteredOrderData);

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, selectedOrder) -> {
            if (selectedOrder != null) {
                setStatus("Selected order: " + selectedOrder.getOrderId() + " - " + selectedOrder.getCustomer(), false);
            }
        });

        setStatus("Orders page ready.", false);
    }

    @FXML
    private void handleShowAddOrderPanel() {
        incomingOrder = SambiKopiDataStore.createIncomingOrder();
        showIncomingOrder(incomingOrder);
        incomingOrderPane.setManaged(true);
        incomingOrderPane.setVisible(true);
        setStatus("New incoming order is ready to be accepted.", false);
    }

    @FXML
    private void handleHideIncomingOrderPanel() {
        incomingOrderPane.setVisible(false);
        incomingOrderPane.setManaged(false);
        incomingOrder = null;
        setStatus("Incoming order panel closed.", false);
    }

    @FXML
    private void handleAcceptIncomingOrder() {
        if (incomingOrder == null) {
            incomingStatusLabel.setText("No incoming order to accept.");
            return;
        }

        SambiKopiDataStore.getOrders().add(incomingOrder);
        orderTable.getSelectionModel().select(incomingOrder);
        incomingStatusLabel.setText("Accepted: " + incomingOrder.getOrderId());
        setStatus("Incoming order accepted: " + incomingOrder.getOrderId(), false);
        incomingOrder = null;
        incomingOrderPane.setVisible(false);
        incomingOrderPane.setManaged(false);
    }

    @FXML
    private void handleRejectIncomingOrder() {
        if (incomingOrder == null) {
            incomingStatusLabel.setText("No incoming order to reject.");
            return;
        }

        String rejectedId = incomingOrder.getOrderId();
        incomingOrder = null;
        incomingOrderPane.setVisible(false);
        incomingOrderPane.setManaged(false);
        setStatus("Incoming order rejected: " + rejectedId, true);
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
        filterButton.setText(activeOnlyFilter ? "Active Only" : "Filters");
        applyFilters();
        setStatus(activeOnlyFilter ? "Showing active orders only." : "Filter cleared.", false);
    }

    @FXML
    private void handleRefreshOrders() {
        orderSearchField.clear();
        activeOnlyFilter = false;
        filterButton.setText("Filters");
        filteredOrderData.setPredicate(order -> true);
        orderTable.getSelectionModel().clearSelection();
        setStatus("Orders refreshed.", false);
    }

    @FXML
    private void handleCompleteOrder() {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an order first.", true);
            return;
        }

        selected.setStatus("Complete");
        orderTable.refresh();
        setStatus("Order completed: " + selected.getOrderId(), false);
    }

    @FXML
    private void handleAssignOrder() {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an order first.", true);
            return;
        }

        selected.setStatus("Assigned");
        orderTable.refresh();
        setStatus("Order assigned: " + selected.getOrderId(), false);
    }

    @FXML
    private void handleCancelOrder() {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select an order first.", true);
            return;
        }

        selected.setStatus("Cancelled");
        orderTable.refresh();
        setStatus("Order cancelled: " + selected.getOrderId(), true);
    }

    @FXML
    private void handleOpenDashboard() {
        showInfo("Dashboard", "Dashboard page is still under development.");
    }

    @FXML
    private void handleOpenMenu() throws IOException {
        App.setRoot("menu.fxml");
    }

    @FXML
    private void handleOpenMenuList() throws IOException {
        App.setRoot("menu_list.fxml");
    }

    @FXML
    private void handleOpenInventory() throws IOException {
        App.setRoot("inventory.fxml");
    }

    @FXML
    private void handleOpenOrders() throws IOException {
        App.setRoot("orders.fxml");
    }

    private void showIncomingOrder(OrderItem order) {
        incomingOrderIdLabel.setText(order.getOrderId());
        incomingCustomerLabel.setText(order.getCustomer());
        incomingProductLabel.setText(order.getProduct());
        incomingDateLabel.setText(order.getCreationDate());
        incomingStatusLabel.setText("Incoming order. Accept to add it to the order list.");
    }

    private void applyFilters() {
        String keyword = orderSearchField.getText().trim().toLowerCase();

        filteredOrderData.setPredicate(order -> {
            boolean matchesKeyword = keyword.isEmpty()
                    || order.getOrderId().toLowerCase().contains(keyword)
                    || order.getCustomer().toLowerCase().contains(keyword)
                    || order.getProduct().toLowerCase().contains(keyword)
                    || order.getCreationDate().toLowerCase().contains(keyword)
                    || order.getStatus().toLowerCase().contains(keyword);

            boolean matchesActiveFilter = !activeOnlyFilter
                    || !order.getStatus().equalsIgnoreCase("Complete")
                    && !order.getStatus().equalsIgnoreCase("Cancelled");

            return matchesKeyword && matchesActiveFilter;
        });
    }

    private void setStatus(String message, boolean error) {
        orderStatusLabel.setText(message);
        orderStatusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
