package interva.sambikopi.controller;

import interva.sambikopi.App;
import interva.sambikopi.model.OrderItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    @FXML private Button orderPanelButton;
    @FXML private VBox orderPopupPane;
    @FXML private Button closeOrderPanelButton;
    @FXML private Button popupCompleteButton;
    @FXML private Button popupCancelButton;
    @FXML private Label popupOrderIdLabel;
    @FXML private Label popupCustomerLabel;
    @FXML private Label popupProductLabel;
    @FXML private Label popupDateLabel;
    @FXML private Label popupStatusLabel;

    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> orderIdColumn;
    @FXML private TableColumn<OrderItem, String> customerColumn;
    @FXML private TableColumn<OrderItem, String> productColumn;
    @FXML private TableColumn<OrderItem, String> creationDateColumn;
    @FXML private TableColumn<OrderItem, String> orderStatusColumn;

    private final ObservableList<OrderItem> masterOrderData = FXCollections.observableArrayList();
    private FilteredList<OrderItem> filteredOrderData;
    private boolean activeOnlyFilter = false;
    private OrderItem popupSelectedOrder;

    @FXML
    private void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customer"));
        productColumn.setCellValueFactory(new PropertyValueFactory<>("product"));
        creationDateColumn.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        orderStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        masterOrderData.addAll(
                new OrderItem("00978-AB", "Thomas Tejo", "Espresso Con Panna", "16 January 2028", "Complete"),
                new OrderItem("94741-AB", "Miks Yermolay", "Milkshake Vanilla", "16 January 2028", "Assigned"),
                new OrderItem("09213-AB", "Neon Sylvon", "Mineral Water", "16 January 2028", "Waiting"),
                new OrderItem("81220-AC", "Yamada Yoru", "Kopi Milk Gula Aren", "17 January 2028", "Assigned"),
                new OrderItem("73014-BC", "Iso Yang", "Matcha Latte", "17 January 2028", "Waiting")
        );

        filteredOrderData = new FilteredList<>(masterOrderData, order -> true);
        orderTable.setItems(filteredOrderData);

        orderTable.getSelectionModel().selectedItemProperty().addListener((obs, oldOrder, selectedOrder) -> {
            if (selectedOrder != null) {
                setStatus("Selected order: " + selectedOrder.getOrderId() + " - " + selectedOrder.getCustomer(), false);
                if (orderPopupPane != null && orderPopupPane.isVisible()) {
                    showOrderInPopup(selectedOrder);
                }
            }
        });

        setStatus("Orders page ready.", false);
    }


    @FXML
    private void handleShowOrderPanel() {
        OrderItem selected = orderTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            selected = findFirstActiveOrder();
        }
        if (selected == null && !masterOrderData.isEmpty()) {
            selected = masterOrderData.get(0);
        }

        if (selected == null) {
            setStatus("No order data available.", true);
            return;
        }

        orderTable.getSelectionModel().select(selected);
        showOrderInPopup(selected);
        orderPopupPane.setManaged(true);
        orderPopupPane.setVisible(true);
        setStatus("Order panel opened: " + selected.getOrderId(), false);
    }

    @FXML
    private void handleHideOrderPanel() {
        orderPopupPane.setVisible(false);
        orderPopupPane.setManaged(false);
        setStatus("Order panel closed.", false);
    }

    @FXML
    private void handlePopupComplete() {
        if (popupSelectedOrder == null) {
            popupStatusLabel.setText("No order selected.");
            return;
        }

        popupSelectedOrder.setStatus("Complete");
        orderTable.refresh();
        popupStatusLabel.setText("Completed: " + popupSelectedOrder.getOrderId());
        setStatus("Order completed: " + popupSelectedOrder.getOrderId(), false);
    }

    @FXML
    private void handlePopupCancel() {
        if (popupSelectedOrder == null) {
            popupStatusLabel.setText("No order selected.");
            return;
        }

        popupSelectedOrder.setStatus("Cancelled");
        orderTable.refresh();
        popupStatusLabel.setText("Cancelled: " + popupSelectedOrder.getOrderId());
        setStatus("Order cancelled: " + popupSelectedOrder.getOrderId(), true);
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
    private void handleOpenInventory() throws IOException {
        App.setRoot("inventory.fxml");
    }

    @FXML
    private void handleOpenOrders() throws IOException {
        App.setRoot("orders.fxml");
    }


    private OrderItem findFirstActiveOrder() {
        for (OrderItem order : masterOrderData) {
            if (!order.getStatus().equalsIgnoreCase("Complete")
                    && !order.getStatus().equalsIgnoreCase("Cancelled")) {
                return order;
            }
        }
        return null;
    }

    private void showOrderInPopup(OrderItem order) {
        popupSelectedOrder = order;
        popupOrderIdLabel.setText(order.getOrderId());
        popupCustomerLabel.setText(order.getCustomer());
        popupProductLabel.setText(order.getProduct());
        popupDateLabel.setText(order.getCreationDate());
        popupStatusLabel.setText("Current status: " + order.getStatus());
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
