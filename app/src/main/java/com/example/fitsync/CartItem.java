package com.example.fitsync;

public class CartItem {
    private int id;
    private int productId;
    private String name;
    private float price;
    private int quantity;
    private String imageUrl;

    public CartItem(int id, int productId, String name, float price, int quantity, String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public float getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}