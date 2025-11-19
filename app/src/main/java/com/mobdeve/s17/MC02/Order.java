package com.mobdeve.s17.MC02;

import java.util.List;

public class Order {
    private List<Product> items;
    private String shippingAddress;
    private String paymentMethod;
    private int totalAmount;

    public Order() {} // needed for Firestore

    public Order(List<Product> items, String shippingAddress, String paymentMethod, int totalAmount) {
        this.items = items;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
    }

    // Getters and setters
    public List<Product> getItems() { return items; }
    public void setItems(List<Product> items) { this.items = items; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public int getTotalAmount() { return totalAmount; }
    public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
}
