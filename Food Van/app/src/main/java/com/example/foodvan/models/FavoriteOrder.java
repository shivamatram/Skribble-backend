package com.example.foodvan.models;

import java.io.Serializable;

/**
 * FavoriteOrder model class for managing user's favorite food items and vendors
 */
public class FavoriteOrder implements Serializable {
    
    private String favoriteId;
    private String userId;
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String vendorId;
    private String vendorName;
    private String imageUrl;
    private double price;
    private float rating;
    private int reviewsCount;
    private String type; // "FOOD" or "VENDOR"
    private long addedDate;
    private boolean isAvailable;
    private String category;
    private String cuisine;
    
    // Default constructor for Firebase
    public FavoriteOrder() {}
    
    // Constructor
    public FavoriteOrder(String favoriteId, String userId, String itemId, String itemName, 
                        String itemDescription, String vendorId, String vendorName, 
                        String imageUrl, double price, float rating, int reviewsCount, 
                        String type, long addedDate, boolean isAvailable, 
                        String category, String cuisine) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.reviewsCount = reviewsCount;
        this.type = type;
        this.addedDate = addedDate;
        this.isAvailable = isAvailable;
        this.category = category;
        this.cuisine = cuisine;
    }
    
    // Getters and Setters
    public String getFavoriteId() { return favoriteId; }
    public void setFavoriteId(String favoriteId) { this.favoriteId = favoriteId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }
    
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }
    
    public int getReviewsCount() { return reviewsCount; }
    public void setReviewsCount(int reviewsCount) { this.reviewsCount = reviewsCount; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public long getAddedDate() { return addedDate; }
    public void setAddedDate(long addedDate) { this.addedDate = addedDate; }
    
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getCuisine() { return cuisine; }
    public void setCuisine(String cuisine) { this.cuisine = cuisine; }
    
    // Helper methods
    public String getFormattedPrice() {
        return "₹" + String.format("%.0f", price);
    }
    
    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }
    
    public String getFormattedReviews() {
        if (reviewsCount > 1000) {
            return "(" + (reviewsCount / 1000) + "k+ reviews)";
        }
        return "(" + reviewsCount + " reviews)";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FavoriteOrder that = (FavoriteOrder) obj;
        return favoriteId != null ? favoriteId.equals(that.favoriteId) : that.favoriteId == null;
    }
    
    @Override
    public int hashCode() {
        return favoriteId != null ? favoriteId.hashCode() : 0;
    }
}
