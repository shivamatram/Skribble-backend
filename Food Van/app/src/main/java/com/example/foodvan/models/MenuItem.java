package com.example.foodvan.models;

import java.io.Serializable;

/**
 * MenuItem model class representing food items in a van's menu
 */
public class MenuItem implements Serializable {
    private String itemId;
    private String vanId;
    private String name;
    private String description;
    private String imageUrl;
    private double price;
    private String category;
    private boolean isVegetarian;
    private boolean isVegan;
    private boolean isGlutenFree;
    private boolean isSpicy;
    private boolean isAvailable;
    private int preparationTime; // in minutes
    private double rating;
    private int totalRatings;
    private String ingredients;
    private int calories;
    private double discount; // percentage
    private boolean isBestSeller;
    private boolean isNew;
    private int cartQuantity; // For cart management
    private long createdAt;
    private int orderCount; // Number of times this item has been ordered
    private long lastUpdated;
    private String vendorId; // ID of the vendor who owns this item
    private String imageUri; // Temporary URI for image before upload

    public MenuItem() {
        // Default constructor required for Firebase
    }

    public MenuItem(String itemId, String vanId, String name, String description, double price) {
        this.itemId = itemId;
        this.vanId = vanId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isAvailable = true;
        this.isVegetarian = false;
        this.isVegan = false;
        this.isGlutenFree = false;
        this.isSpicy = false;
        this.preparationTime = 15;
        this.rating = 0.0;
        this.totalRatings = 0;
        this.discount = 0.0;
        this.isBestSeller = false;
        this.isNew = false;
        this.cartQuantity = 0;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    // Convenience methods for ID (alias for itemId)
    public String getId() {
        return itemId;
    }

    public void setId(String id) {
        this.itemId = id;
    }

    public String getVanId() {
        return vanId;
    }

    public void setVanId(String vanId) {
        this.vanId = vanId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public boolean isGlutenFree() {
        return isGlutenFree;
    }

    public void setGlutenFree(boolean glutenFree) {
        isGlutenFree = glutenFree;
    }

    public boolean isSpicy() {
        return isSpicy;
    }

    public void setSpicy(boolean spicy) {
        isSpicy = spicy;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isBestSeller() {
        return isBestSeller;
    }

    public void setBestSeller(boolean bestSeller) {
        isBestSeller = bestSeller;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getCartQuantity() {
        return cartQuantity;
    }

    public void setCartQuantity(int cartQuantity) {
        this.cartQuantity = cartQuantity;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    // Utility methods
    public double getDiscountedPrice() {
        if (discount > 0) {
            return price - (price * discount / 100);
        }
        return price;
    }

    public String getFormattedPrice() {
        return String.format("₹%.2f", price);
    }

    public String getFormattedDiscountedPrice() {
        return String.format("₹%.2f", getDiscountedPrice());
    }

    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }

    public String getFormattedPreparationTime() {
        return preparationTime + " mins";
    }

    public void updateRating(double newRating) {
        double totalRatingPoints = this.rating * this.totalRatings;
        this.totalRatings++;
        this.rating = (totalRatingPoints + newRating) / this.totalRatings;
    }

    public boolean hasDiscount() {
        return discount > 0;
    }

    public String getDietaryInfo() {
        StringBuilder info = new StringBuilder();
        if (isVegetarian) info.append("Veg ");
        if (isVegan) info.append("Vegan ");
        if (isGlutenFree) info.append("Gluten-Free ");
        if (isSpicy) info.append("Spicy ");
        return info.toString().trim();
    }
}
