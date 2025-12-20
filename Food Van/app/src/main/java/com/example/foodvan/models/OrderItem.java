package com.example.foodvan.models;

import java.io.Serializable;

/**
 * OrderItem model class representing individual items in an order
 */
public class OrderItem implements Serializable {
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String itemImageUrl;
    private double itemPrice;
    private int quantity;
    private double totalPrice;
    private String customizations;
    private String category;
    private boolean isVegetarian;
    private boolean isVegan;
    private boolean isSpicy;

    public OrderItem() {
        // Default constructor required for Firebase
    }

    public OrderItem(String itemId, String itemName, double itemPrice, int quantity) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.totalPrice = itemPrice * quantity;
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemImageUrl() {
        return itemImageUrl;
    }

    public void setItemImageUrl(String itemImageUrl) {
        this.itemImageUrl = itemImageUrl;
    }

    public double getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(double itemPrice) {
        this.itemPrice = itemPrice;
        this.totalPrice = itemPrice * quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.totalPrice = itemPrice * quantity;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCustomizations() {
        return customizations;
    }

    public void setCustomizations(String customizations) {
        this.customizations = customizations;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        isVegetarian = vegetarian;
    }

    public boolean isVegan() {
        return isVegan;
    }

    public void setVegan(boolean vegan) {
        isVegan = vegan;
    }

    public boolean isSpicy() {
        return isSpicy;
    }

    public void setSpicy(boolean spicy) {
        isSpicy = spicy;
    }

    // Utility methods
    public String getFormattedPrice() {
        return String.format("₹%.2f", itemPrice);
    }

    public String getFormattedTotalPrice() {
        return String.format("₹%.2f", totalPrice);
    }

    public String getQuantityText() {
        return quantity + "x";
    }
}
