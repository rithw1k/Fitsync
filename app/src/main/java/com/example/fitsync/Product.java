package com.example.fitsync;

public class Product {
    private int id;  // Add this
    private String name;
    private String description;
    private float price;
    private int stock;
    private String imageUrl;

    public Product(int id, String name, String description, float price, int stock, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public float getPrice() { return price; }
    public int getStock() { return stock; }
    public String getImageUrl() { return imageUrl; }
}