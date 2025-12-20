package com.example.foodvan.models;

import java.io.Serializable;
import java.util.List;

/**
 * FoodVan model class representing a food vendor's van
 */
public class FoodVan implements Serializable {
    private String vanId;
    private String vendorId;
    private String name;
    private String description;
    private String imageUrl;
    private String cuisineType;
    private double latitude;
    private double longitude;
    private String address;
    private boolean isOnline;
    private boolean isOpen;
    private String openingTime;
    private String closingTime;
    private double rating;
    private int totalRatings;
    private int totalOrders;
    private double distance; // Distance from user's location
    private String phoneNumber;
    private String email;
    private List<String> specialties;
    private double averagePrice;
    private int estimatedDeliveryTime; // in minutes
    private boolean acceptsOnlinePayment;
    private boolean acceptsCashPayment;
    private long createdAt;
    private long lastUpdated;

    public FoodVan() {
        // Default constructor required for Firebase
    }

    public FoodVan(String vanId, String vendorId, String name, String description) {
        this.vanId = vanId;
        this.vendorId = vendorId;
        this.name = name;
        this.description = description;
        this.isOnline = false;
        this.isOpen = false;
        this.rating = 0.0;
        this.totalRatings = 0;
        this.totalOrders = 0;
        this.acceptsOnlinePayment = true;
        this.acceptsCashPayment = true;
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getVanId() {
        return vanId;
    }

    public void setVanId(String vanId) {
        this.vanId = vanId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
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

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(String openingTime) {
        this.openingTime = openingTime;
    }

    public String getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(String closingTime) {
        this.closingTime = closingTime;
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

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties;
    }

    public double getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(double averagePrice) {
        this.averagePrice = averagePrice;
    }

    public int getEstimatedDeliveryTime() {
        return estimatedDeliveryTime;
    }

    public void setEstimatedDeliveryTime(int estimatedDeliveryTime) {
        this.estimatedDeliveryTime = estimatedDeliveryTime;
    }

    public boolean isAcceptsOnlinePayment() {
        return acceptsOnlinePayment;
    }

    public void setAcceptsOnlinePayment(boolean acceptsOnlinePayment) {
        this.acceptsOnlinePayment = acceptsOnlinePayment;
    }

    public boolean isAcceptsCashPayment() {
        return acceptsCashPayment;
    }

    public void setAcceptsCashPayment(boolean acceptsCashPayment) {
        this.acceptsCashPayment = acceptsCashPayment;
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

    // Utility methods
    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }

    public String getFormattedDistance() {
        return String.format("%.1f km", distance);
    }

    public boolean isAvailable() {
        return isOnline && isOpen;
    }

    public void updateRating(double newRating) {
        double totalRatingPoints = this.rating * this.totalRatings;
        this.totalRatings++;
        this.rating = (totalRatingPoints + newRating) / this.totalRatings;
    }

    // Additional methods for CustomerMapActivity
    public String getId() {
        return vanId;
    }

    public String getVanName() {
        return name;
    }

    public String getOwnerName() {
        // If vendorId contains owner name, return it, otherwise return a default
        return vendorId != null ? vendorId : "Owner";
    }
}
