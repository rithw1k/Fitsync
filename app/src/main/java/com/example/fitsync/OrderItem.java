package com.example.fitsync;

public class OrderItem {
    private int productId;
    private String name;
    private float price;
    private int quantity;
    private String imageUrl;

    public OrderItem(int productId, String name, float price, int quantity, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public float getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
}