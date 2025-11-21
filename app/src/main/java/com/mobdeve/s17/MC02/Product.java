package com.mobdeve.s17.MC02;

public class Product {
    private String name;
    private String price;
    private int imageResId;
    private String brand;
    private String firestoreId;

    // REQUIRED no-arg constructor
    public Product() {}

    // 3-parameter constructor
    public Product(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    // 4-parameter constructor
    public Product(String name, String price, int imageResId, String brand) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.brand = brand;
    }

    // Getters
    public String getName() { return name; }
    public String getPrice() { return price; }
    public int getImageResId() { return imageResId; }
    public String getBrand() { return brand; }
    public String getFirestoreId() { return firestoreId; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }
}