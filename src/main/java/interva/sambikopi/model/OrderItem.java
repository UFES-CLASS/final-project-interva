package interva.sambikopi.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class OrderItem {
    private final StringProperty orderId;
    private final StringProperty customer;
    private final StringProperty product;
    private final StringProperty creationDate;
    private final StringProperty status;
    private final StringProperty paymentMethod;
    private final IntegerProperty totalAmount;

    public OrderItem(String orderId, String customer, String product, String creationDate, String status) {
        this(orderId, customer, product, creationDate, status, "Card", 0);
    }

    public OrderItem(String orderId, String customer, String product, String creationDate, String status, String paymentMethod, int totalAmount) {
        this.orderId = new SimpleStringProperty(orderId);
        this.customer = new SimpleStringProperty(customer);
        this.product = new SimpleStringProperty(product);
        this.creationDate = new SimpleStringProperty(creationDate);
        this.status = new SimpleStringProperty(status);
        this.paymentMethod = new SimpleStringProperty(paymentMethod);
        this.totalAmount = new SimpleIntegerProperty(totalAmount);
    }

    public String getOrderId() { return orderId.get(); }
    public void setOrderId(String value) { orderId.set(value); }
    public StringProperty orderIdProperty() { return orderId; }

    public String getCustomer() { return customer.get(); }
    public void setCustomer(String value) { customer.set(value); }
    public StringProperty customerProperty() { return customer; }

    public String getProduct() { return product.get(); }
    public void setProduct(String value) { product.set(value); }
    public StringProperty productProperty() { return product; }

    public String getCreationDate() { return creationDate.get(); }
    public void setCreationDate(String value) { creationDate.set(value); }
    public StringProperty creationDateProperty() { return creationDate; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String value) { paymentMethod.set(value); }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    public int getTotalAmount() { return totalAmount.get(); }
    public void setTotalAmount(int value) { totalAmount.set(value); }
    public IntegerProperty totalAmountProperty() { return totalAmount; }
}
