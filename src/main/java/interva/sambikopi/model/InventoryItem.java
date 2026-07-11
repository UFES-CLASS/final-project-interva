package interva.sambikopi.model;

public class InventoryItem extends CafeItem {

    private String product;
    private int stock;
    private String expDate;
    private String status;
    private String notifyStatus;

    public InventoryItem(String product, int stock, String expDate, String status, String notifyStatus) {
        super(product, status);
        this.product = product;
        this.stock = stock;
        this.expDate = expDate;
        this.status = status;
        this.notifyStatus = notifyStatus;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
        setItemName(product);
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
        super.setStatus(status);
    }

    public String getNotifyStatus() {
        return notifyStatus;
    }

    public void setNotifyStatus(String notifyStatus) {
        this.notifyStatus = notifyStatus;
    }

    @Override
    public String getItemType() {
        return "Inventory Item";
    }
}
