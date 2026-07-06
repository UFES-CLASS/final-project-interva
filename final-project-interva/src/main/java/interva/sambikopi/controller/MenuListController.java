package interva.sambikopi.controller;

import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class MenuListController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private TableView<CafeMenuItem> menuTable;
    @FXML private TableColumn<CafeMenuItem, String> colMenuName;
    @FXML private TableColumn<CafeMenuItem, String> colCategory;
    @FXML private TableColumn<CafeMenuItem, String> colPrice;
    @FXML private TableColumn<CafeMenuItem, String> colIngredients;
    @FXML private TableColumn<CafeMenuItem, String> colStatus;

    private FilteredList<CafeMenuItem> filteredMenuData;

    @FXML
    private void initialize() {
        colMenuName.setCellValueFactory(new PropertyValueFactory<>("menuName"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colIngredients.setCellValueFactory(new PropertyValueFactory<>("ingredients"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredMenuData = new FilteredList<>(SambiKopiDataStore.getMenuItems(), item -> true);
        menuTable.setItems(filteredMenuData);
        setStatus("Menu List ready.", false);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase();
        filteredMenuData.setPredicate(item -> keyword.isEmpty()
                || item.getMenuName().toLowerCase().contains(keyword)
                || item.getCategory().toLowerCase().contains(keyword)
                || item.getPrice().toLowerCase().contains(keyword)
                || item.getIngredients().toLowerCase().contains(keyword)
                || item.getStatus().toLowerCase().contains(keyword));
        setStatus(keyword.isEmpty() ? "Showing all menu items." : "Search result for: " + keyword, false);
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        filteredMenuData.setPredicate(item -> true);
        menuTable.getSelectionModel().clearSelection();
        setStatus("Menu List refreshed.", false);
    }

    @FXML
    private void handleDeleteSelected() {
        CafeMenuItem selected = menuTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            setStatus("Select a menu item first before deleting.", true);
            return;
        }

        SambiKopiDataStore.getMenuItems().remove(selected);
        menuTable.getSelectionModel().clearSelection();
        setStatus("Deleted menu item: " + selected.getMenuName(), false);
    }

    @FXML
    private void handleDeleteAll() {
        if (SambiKopiDataStore.getMenuItems().isEmpty()) {
            setStatus("There are no menu items to delete.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete all menu items from the Menu List?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete All Menu Items");
        confirm.setHeaderText(null);

        confirm.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                SambiKopiDataStore.getMenuItems().clear();
                menuTable.getSelectionModel().clearSelection();
                setStatus("All menu items have been deleted.", false);
            }
        });
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
