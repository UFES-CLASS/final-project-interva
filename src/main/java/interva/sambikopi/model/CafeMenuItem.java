package interva.sambikopi.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CafeMenuItem {
    private final StringProperty menuName;
    private final StringProperty category;
    private final StringProperty price;
    private final StringProperty ingredients;
    private final StringProperty status;
    private final StringProperty imagePath;

    public CafeMenuItem(String menuName, String category, String price, String ingredients, String status) {
        this(menuName, category, price, ingredients, status, "assets/menu/default.png");
    }

    public CafeMenuItem(String menuName, String category, String price, String ingredients, String status, String imagePath) {
        this.menuName = new SimpleStringProperty(menuName);
        this.category = new SimpleStringProperty(category);
        this.price = new SimpleStringProperty(price);
        this.ingredients = new SimpleStringProperty(ingredients);
        this.status = new SimpleStringProperty(status);
        this.imagePath = new SimpleStringProperty(imagePath == null || imagePath.isBlank() ? "assets/menu/default.png" : imagePath);
    }

    public String getMenuName() { return menuName.get(); }
    public void setMenuName(String value) { menuName.set(value); }
    public StringProperty menuNameProperty() { return menuName; }

    public String getCategory() { return category.get(); }
    public void setCategory(String value) { category.set(value); }
    public StringProperty categoryProperty() { return category; }

    public String getPrice() { return price.get(); }
    public void setPrice(String value) { price.set(value); }
    public StringProperty priceProperty() { return price; }

    public String getIngredients() { return ingredients.get(); }
    public void setIngredients(String value) { ingredients.set(value); }
    public StringProperty ingredientsProperty() { return ingredients; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getImagePath() { return imagePath.get(); }
    public void setImagePath(String value) { imagePath.set(value == null || value.isBlank() ? "assets/menu/default.png" : value); }
    public StringProperty imagePathProperty() { return imagePath; }
}
