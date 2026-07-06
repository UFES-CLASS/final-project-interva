package interva.sambikopi.controller;

import interva.sambikopi.model.InventoryItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class StockController {

    @FXML private TextField stockSearchField;
    @FXML private Label stockStatusLabel;
    @FXML private TableView<InventoryItem> stockTable;
    @FXML private TableColumn<InventoryItem, String> colProduct;
    @FXML private TableColumn<InventoryItem, Number> colStock;
    @FXML private TableColumn<InventoryItem, String> colExpDate;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, String> colNotifyStatus;

    private FilteredList<InventoryItem> filteredStockData;

    @FXML
    private void initialize() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colExpDate.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colNotifyStatus.setCellValueFactory(new PropertyValueFactory<>("notifyStatus"));

        filteredStockData = new FilteredList<>(SambiKopiDataStore.getInventoryItems(), item -> true);
        stockTable.setItems(filteredStockData);
        setStatus("Stock page ready. Select low stock items, then notify owner if needed.", false);
    }

    @FXML
    private void handleSearchStock() {
        String keyword = stockSearchField.getText().trim().toLowerCase();
        filteredStockData.setPredicate(item -> keyword.isEmpty()
                || item.getProduct().toLowerCase().contains(keyword)
                || item.getExpDate().toLowerCase().contains(keyword)
                || item.getStatus().toLowerCase().contains(keyword)
                || item.getNotifyStatus().toLowerCase().contains(keyword));
        setStatus(keyword.isEmpty() ? "Showing all stock items." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefreshStock() {
        stockSearchField.clear();
        filteredStockData.setPredicate(item -> true);
        stockTable.getSelectionModel().clearSelection();
        stockTable.refresh();
        setStatus("Stock data refreshed.", false);
    }

    @FXML
    private void handleCheckStock() {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a stock item first.", true);
            return;
        }

        if (selected.getStock() <= 3 || selected.getStatus().equalsIgnoreCase("Critical")) {
            setStatus(selected.getProduct() + " is critical. Notify owner immediately.", true);
        } else if (selected.getStock() <= 8 || selected.getStatus().equalsIgnoreCase("Low Stock")) {
            setStatus(selected.getProduct() + " is low stock. Owner notification is recommended.", false);
        } else {
            setStatus(selected.getProduct() + " stock is still available.", false);
        }
    }

    @FXML
    private void handleNotifyOwner() {
        InventoryItem selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a stock item before notifying owner.", true);
            return;
        }
        SambiKopiDataStore.notifyOwnerStock(selected);
        stockTable.refresh();
        setStatus("Owner notified about stock item: " + selected.getProduct(), false);
    }

    private void setStatus(String message, boolean error) {
        stockStatusLabel.setText(message);
        stockStatusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
