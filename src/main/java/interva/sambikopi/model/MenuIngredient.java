package interva.sambikopi.model;

public class MenuIngredient {
    private String menuName;
    private String stockProduct;
    private int quantity;
    private String unit;

    public MenuIngredient(String menuName, String stockProduct, int quantity) {
        this(menuName, stockProduct, quantity, "portion");
    }

    public MenuIngredient(String menuName, String stockProduct, int quantity, String unit) {
        this.menuName = menuName;
        this.stockProduct = stockProduct;
        this.quantity = quantity;
        this.unit = (unit == null || unit.isBlank()) ? "portion" : unit.trim();
    }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getStockProduct() { return stockProduct; }
    public void setStockProduct(String stockProduct) { this.stockProduct = stockProduct; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = (unit == null || unit.isBlank()) ? "portion" : unit.trim(); }

    public String toDisplayText() {
        return stockProduct + " - " + quantity + " " + unit;
    }
}
