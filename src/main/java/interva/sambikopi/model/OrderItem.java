package interva.sambikopi.model;

public class OrderItem {

    private String orderId;
    private String customer;
    private String product;
    private String creationDate;
    private String status;

    public OrderItem(String orderId, String customer, String product, String creationDate, String status) {
        this.orderId = orderId;
        this.customer = customer;
        this.product = product;
        this.creationDate = creationDate;
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
