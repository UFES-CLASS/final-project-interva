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
import javafx.beans.property.SimpleStringProperty;

public class CashierOrdersController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> colOrderId;
    @FXML private TableColumn<OrderItem, String> colCustomer;
    @FXML private TableColumn<OrderItem, String> colProduct;
    @FXML private TableColumn<OrderItem, String> colCreationDate;
    @FXML private TableColumn<OrderItem, String> colPayment;
    @FXML private TableColumn<OrderItem, String> colTotal;
    @FXML private TableColumn<OrderItem, String> colStatus;

    private FilteredList<OrderItem> filteredOrders;

    @FXML
    private void initialize() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colCustomer.setCellValueFactory(new PropertyValueFactory<>("customer"));
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colCreationDate.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        colPayment.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        colTotal.setCellValueFactory(data -> new SimpleStringProperty(SambiKopiDataStore.formatIdr(data.getValue().getTotalAmount())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredOrders = new FilteredList<>(SambiKopiDataStore.getOrders(), order -> true);
        orderTable.setItems(filteredOrders);
        setStatus("Cashier Orders ready.", false);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        filteredOrders.setPredicate(order -> keyword.isEmpty()
                || order.getOrderId().toLowerCase().contains(keyword)
                || order.getCustomer().toLowerCase().contains(keyword)
                || order.getProduct().toLowerCase().contains(keyword)
                || order.getCreationDate().toLowerCase().contains(keyword)
                || order.getPaymentMethod().toLowerCase().contains(keyword)
                || order.getStatus().toLowerCase().contains(keyword));
        setStatus(keyword.isEmpty() ? "Showing all cashier orders." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filteredOrders.setPredicate(order -> true);
        orderTable.refresh();
        setStatus("Cashier Orders refreshed.", false);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
