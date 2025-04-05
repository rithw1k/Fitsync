package com.example.fitsync;

import java.util.List;

public class Order {
    private int orderId;
    private float totalPrice;
    private String addedAt;
    private String status;
    private String location;
    private List<OrderItem> items;

    public Order(int orderId, float totalPrice, String addedAt, String status,String location, List<OrderItem> items) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.addedAt = addedAt;
        this.status = status;
        this.location=location;
        this.items = items;
    }

    public int getOrderId() { return orderId; }
    public float getTotalPrice() { return totalPrice; }
    public String getAddedAt() { return addedAt; }
    public String getStatus() { return status; }
    public String getLocation() {return location;}
    public List<OrderItem> getItems() { return items; }
}