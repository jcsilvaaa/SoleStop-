package com.mobdeve.s17.MC02;

public class Product {
    private String name;
    private String price;
    private int imageResId;
    private String imageUrl;
    private String brand;
    private String description;
    private String firestoreId;

    public Product() {}

    public Product(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    public Product(String name, String price, int imageResId, String brand) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.brand = brand;
    }

    // getters & setters
    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
    public String getBrand() { return brand; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public String getFirestoreId() { return firestoreId; }

    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
}