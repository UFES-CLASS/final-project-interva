package com.fad.LibrarySystem.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class InventoryItem {

    private final StringProperty productName;
    private final IntegerProperty stock;
    private final StringProperty expDate;
    private final StringProperty status;

    public InventoryItem(String productName, int stock, String expDate, String status) {
        this.productName = new SimpleStringProperty(productName);
        this.stock = new SimpleIntegerProperty(stock);
        this.expDate = new SimpleStringProperty(expDate);
        this.status = new SimpleStringProperty(status);
    }

    public String getProductName() {
        return productName.get();
    }

    public void setProductName(String productName) {
        this.productName.set(productName);
    }

    public StringProperty productNameProperty() {
        return productName;
    }

    public int getStock() {
        return stock.get();
    }

    public void setStock(int stock) {
        this.stock.set(stock);
    }

    public IntegerProperty stockProperty() {
        return stock;
    }

    public String getExpDate() {
        return expDate.get();
    }

    public void setExpDate(String expDate) {
        this.expDate.set(expDate);
    }

    public StringProperty expDateProperty() {
        return expDate;
    }

    public String getStatus() {
        return status.get();
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public StringProperty statusProperty() {
        return status;
    }
}
