package interva.sambikopi.model;

public class MenuIngredient {
    private String menuName;
    private String stockProduct;
    private int quantity;

    public MenuIngredient(String menuName, String stockProduct, int quantity) {
        this.menuName = menuName;
        this.stockProduct = stockProduct;
        this.quantity = quantity;
    }

    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getStockProduct() { return stockProduct; }
    public void setStockProduct(String stockProduct) { this.stockProduct = stockProduct; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String toDisplayText() {
        return stockProduct + " x" + quantity;
    }
}
