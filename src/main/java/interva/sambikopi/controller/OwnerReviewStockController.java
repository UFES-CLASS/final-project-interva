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

public class OwnerReviewStockController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private TableView<InventoryItem> stockReviewTable;
    @FXML private TableColumn<InventoryItem, String> colProduct;
    @FXML private TableColumn<InventoryItem, Number> colStock;
    @FXML private TableColumn<InventoryItem, String> colExpDate;
    @FXML private TableColumn<InventoryItem, String> colStatus;
    @FXML private TableColumn<InventoryItem, String> colNotifyStatus;

    private FilteredList<InventoryItem> filteredData;

    @FXML
    private void initialize() {
        colProduct.setCellValueFactory(new PropertyValueFactory<>("product"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colExpDate.setCellValueFactory(new PropertyValueFactory<>("expDate"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colNotifyStatus.setCellValueFactory(new PropertyValueFactory<>("notifyStatus"));

        filteredData = new FilteredList<>(SambiKopiDataStore.getStockReviewItems(), item -> true);
        stockReviewTable.setItems(filteredData);

        int count = SambiKopiDataStore.consumeOwnerStockNotificationCount();
        setStatus(count > 0 ? "New stock notification received: " + count + " item(s)." : "Review stock notifications from Barista.", false);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        filteredData.setPredicate(item -> keyword.isEmpty()
                || item.getProduct().toLowerCase().contains(keyword)
                || item.getStatus().toLowerCase().contains(keyword)
                || item.getNotifyStatus().toLowerCase().contains(keyword));
        setStatus(keyword.isEmpty() ? "Showing all stock notifications." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filteredData.setPredicate(item -> true);
        stockReviewTable.refresh();
        setStatus("Stock review refreshed.", false);
    }

    @FXML
    private void handleMarkReviewed() {
        InventoryItem selected = stockReviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a stock notification first.", true);
            return;
        }
        SambiKopiDataStore.markStockReviewed(selected);
        stockReviewTable.getSelectionModel().clearSelection();
        stockReviewTable.refresh();
        setStatus("Stock notification reviewed: " + selected.getProduct(), false);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
