package interva.sambikopi.controller;

import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class OwnerReviewMenuController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private TableView<CafeMenuItem> menuReviewTable;
    @FXML private TableColumn<CafeMenuItem, String> colMenuName;
    @FXML private TableColumn<CafeMenuItem, String> colCategory;
    @FXML private TableColumn<CafeMenuItem, String> colPrice;
    @FXML private TableColumn<CafeMenuItem, String> colIngredients;
    @FXML private TableColumn<CafeMenuItem, String> colStatus;

    private FilteredList<CafeMenuItem> filteredData;

    @FXML
    private void initialize() {
        colMenuName.setCellValueFactory(new PropertyValueFactory<>("menuName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colIngredients.setCellValueFactory(new PropertyValueFactory<>("ingredients"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredData = new FilteredList<>(SambiKopiDataStore.getMenuReviewItems(), item -> true);
        menuReviewTable.setItems(filteredData);

        int count = SambiKopiDataStore.consumeOwnerMenuNotificationCount();
        setStatus(count > 0 ? "New menu review request received: " + count + " item(s)." : "Review new menu requests from Barista.", false);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        filteredData.setPredicate(item -> keyword.isEmpty()
                || item.getMenuName().toLowerCase().contains(keyword)
                || item.getCategory().toLowerCase().contains(keyword)
                || item.getPrice().toLowerCase().contains(keyword)
                || item.getIngredients().toLowerCase().contains(keyword)
                || item.getStatus().toLowerCase().contains(keyword));
        setStatus(keyword.isEmpty() ? "Showing all menu review requests." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filteredData.setPredicate(item -> true);
        menuReviewTable.refresh();
        setStatus("Menu review refreshed.", false);
    }

    @FXML
    private void handleApproveMenu() {
        CafeMenuItem selected = menuReviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a menu item first.", true);
            return;
        }
        SambiKopiDataStore.approveMenuItem(selected);
        menuReviewTable.getSelectionModel().clearSelection();
        menuReviewTable.refresh();
        setStatus("Approved menu item: " + selected.getMenuName(), false);
    }

    @FXML
    private void handleRejectMenu() {
        CafeMenuItem selected = menuReviewTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a menu item first.", true);
            return;
        }
        String menuName = selected.getMenuName();
        SambiKopiDataStore.rejectMenuItem(selected);
        menuReviewTable.getSelectionModel().clearSelection();
        menuReviewTable.refresh();
        setStatus("Rejected and removed menu item: " + menuName, true);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
