package com.mobdeve.s17.MC02;

public class Product {
    private String name;
    private String price;
    private int imageResId;

    // Firestore ID (auto-generated when saved)
    private String firestoreId;

    // ✅ REQUIRED by Firestore for automatic object mapping
    public Product() {
    }

    public Product(String name, String price, int imageResId) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getFirestoreId() {
        return firestoreId;
    }

    public void setFirestoreId(String firestoreId) {
        this.firestoreId = firestoreId;
    }

    // Optional setters (needed for Firestore too)
    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }
}
