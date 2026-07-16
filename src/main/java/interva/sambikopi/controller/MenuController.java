package interva.sambikopi.controller;

import interva.sambikopi.model.CafeMenuItem;
import interva.sambikopi.model.InventoryItem;
import interva.sambikopi.model.MenuIngredient;
import interva.sambikopi.model.SambiKopiDataStore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MenuController {

    @FXML private TextField menuNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private TextField priceField;
    @FXML private ComboBox<String> ingredientStockComboBox;
    @FXML private TextField ingredientQuantityField;
    @FXML private ComboBox<String> ingredientUnitComboBox;
    @FXML private ListView<String> ingredientsListView;
    @FXML private Button chooseImageButton;
    @FXML private Label selectedImageLabel;
    @FXML private Label statusLabel;

    private final ObservableList<MenuIngredient> selectedIngredients = FXCollections.observableArrayList();
    private String selectedImagePath = SambiKopiDataStore.DEFAULT_MENU_IMAGE;

    @FXML
    private void initialize() {
        categoryComboBox.getItems().setAll("Coffee", "Non-Coffee", "Pastry");
        categoryComboBox.getSelectionModel().select("Coffee");
        ingredientUnitComboBox.getItems().setAll("portion", "gram", "ml", "pcs", "scoop", "bottle");
        ingredientUnitComboBox.getSelectionModel().select("portion");
        loadStockOptions();
        updateIngredientsList();
        selectedImageLabel.setText("No photo selected.");
        setStatus("Fill the menu form, choose ingredients from Stock, then submit it for Owner review.", false);
    }

    @FXML
    private void handleAddIngredient() {
        String product = ingredientStockComboBox.getValue();
        String unit = ingredientUnitComboBox.getValue();
        String quantityText = ingredientQuantityField.getText().trim();

        if (product == null || product.isBlank()) {
            setStatus("Choose a stock ingredient first.", true);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityText);
        } catch (NumberFormatException e) {
            setStatus("Ingredient quantity must be a number.", true);
            return;
        }

        if (quantity <= 0) {
            setStatus("Ingredient quantity must be greater than 0.", true);
            return;
        }

        for (MenuIngredient ingredient : selectedIngredients) {
            if (ingredient.getStockProduct().equalsIgnoreCase(product) && ingredient.getUnit().equalsIgnoreCase(unit)) {
                ingredient.setQuantity(ingredient.getQuantity() + quantity);
                updateIngredientsList();
                ingredientQuantityField.clear();
                setStatus("Updated ingredient quantity: " + product, false);
                return;
            }
        }

        selectedIngredients.add(new MenuIngredient("", product, quantity, unit));
        updateIngredientsList();
        ingredientQuantityField.clear();
        setStatus("Added ingredient: " + product + " - " + quantity + " " + unit, false);
    }

    @FXML
    private void handleRemoveIngredient() {
        int index = ingredientsListView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= selectedIngredients.size()) {
            setStatus("Select an ingredient from the list first.", true);
            return;
        }
        MenuIngredient removed = selectedIngredients.remove(index);
        updateIngredientsList();
        setStatus("Removed ingredient: " + removed.getStockProduct(), false);
    }

    @FXML
    private void handleChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose menu photo");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp"));
        File selectedFile = chooser.showOpenDialog(chooseImageButton.getScene().getWindow());

        if (selectedFile == null) {
            return;
        }

        try {
            Path assetDir = Path.of("assets", "menu");
            Files.createDirectories(assetDir);

            String safeName = sanitizeFileName(selectedFile.getName());
            String extension = "png";
            int dotIndex = safeName.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < safeName.length() - 1) {
                extension = safeName.substring(dotIndex + 1).toLowerCase();
                safeName = safeName.substring(0, dotIndex);
            }

            String outputName = safeName + "_" + System.currentTimeMillis() + "." + extension;
            Path target = assetDir.resolve(outputName);
            Files.copy(selectedFile.toPath(), target, StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = target.toString().replace('\\', '/');
            selectedImageLabel.setText("Photo selected.");
            setStatus("Photo selected and saved to assets folder.", false);
        } catch (IOException e) {
            setStatus("Failed to copy image: " + e.getMessage(), true);
        }
    }

    @FXML
    private void handleSaveMenu() {
        String name = SambiKopiDataStore.formatTitleCase(menuNameField.getText());
        String category = categoryComboBox.getValue();
        String priceRaw = priceField.getText().trim();

        if (name.isEmpty() || category == null || priceRaw.isEmpty()) {
            setStatus("Please complete menu name, category, and price before saving.", true);
            return;
        }

        if (selectedIngredients.isEmpty()) {
            setStatus("Add at least one ingredient from Stock before saving.", true);
            return;
        }

        int price = SambiKopiDataStore.parseIdr(priceRaw);
        if (price <= 0) {
            setStatus("Price must be a valid number, for example 18000 or Rp 18.000.", true);
            return;
        }

        List<MenuIngredient> ingredientsToSave = new ArrayList<>();
        for (MenuIngredient ingredient : selectedIngredients) {
            ingredientsToSave.add(new MenuIngredient(name, ingredient.getStockProduct(), ingredient.getQuantity(), ingredient.getUnit()));
        }

        String ingredientDisplay = SambiKopiDataStore.buildIngredientDisplay(ingredientsToSave);
        CafeMenuItem item = new CafeMenuItem(name, category, SambiKopiDataStore.formatIdr(price), ingredientDisplay, "Pending Approval", selectedImagePath);
        SambiKopiDataStore.addMenuForReview(item, ingredientsToSave);
        setStatus("Saved to Menu List and sent to Owner Review: " + name, false);
        clearFormOnly();
    }

    @FXML
    private void handleClearForm() {
        clearFormOnly();
        setStatus("Form cleared.", false);
    }

    private void loadStockOptions() {
        ingredientStockComboBox.getItems().clear();
        for (InventoryItem item : SambiKopiDataStore.getInventoryItems()) {
            ingredientStockComboBox.getItems().add(item.getProduct());
        }
        if (!ingredientStockComboBox.getItems().isEmpty()) {
            ingredientStockComboBox.getSelectionModel().selectFirst();
        }
    }

    private void updateIngredientsList() {
        ObservableList<String> displayItems = FXCollections.observableArrayList();
        for (MenuIngredient ingredient : selectedIngredients) {
            displayItems.add(ingredient.toDisplayText());
        }
        ingredientsListView.setItems(displayItems);
    }

    private void clearFormOnly() {
        menuNameField.clear();
        priceField.clear();
        ingredientQuantityField.clear();
        selectedIngredients.clear();
        updateIngredientsList();
        selectedImagePath = SambiKopiDataStore.DEFAULT_MENU_IMAGE;
        selectedImageLabel.setText("No photo selected.");
        categoryComboBox.getSelectionModel().select("Coffee");
        ingredientUnitComboBox.getItems().setAll("portion", "gram", "ml", "pcs", "scoop", "bottle");
        ingredientUnitComboBox.getSelectionModel().select("portion");
        loadStockOptions();
    }

    private String sanitizeFileName(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "menu_photo";
        }
        return rawName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.setStyle(error
                ? "-fx-text-fill:#B04A34; -fx-font-weight:bold;"
                : "-fx-text-fill:#4F6448; -fx-font-weight:bold;");
    }
}
