package com.example.foodvan.models;

import java.util.HashMap;
import java.util.Map;

/**
 * Vendor model class for Firebase integration
 * Represents a food van vendor with all necessary properties
 */
public class Vendor {
    private String id;
    private String name;
    private String email;
    private String phone;
    private String vanName;
    private String description;
    private String avatarUrl;
    private String address;
    private double latitude;
    private double longitude;
    private boolean online;
    private long lastSeen;
    private long createdAt;
    private long lastLocationUpdate;
    private String fcmToken;
    private Map<String, String> operatingHours;
    private double rating;
    private int totalOrders;
    private double totalEarnings;
    private boolean verified;
    private boolean phoneVerified;
    private String licenseNumber;
    private String category;
    
    // Additional fields to fix ClassMapper warnings
    private String openingTime;
    private String closingTime;
    private String businessName;
    private String cuisineType;
    private boolean isActive;
    private long updatedAt;
    private long deactivatedAt;
    private boolean messagesEnabled;
    private boolean reviewsEnabled;
    private boolean profileVisible;
    private boolean contactInfoVisible;
    private Map<String, Object> privacy;
    private Map<String, Object> privacySettings;
    
    // Fields added to match database and avoid warnings
    private long lastActiveTime;
    // Note: isOnline handled by setter/getter compatibility

    // Default constructor required for Firebase
    public Vendor() {
        this.operatingHours = new HashMap<>();
        this.rating = 0.0;
        this.totalOrders = 0;
        this.totalEarnings = 0.0;
        this.verified = false;
        this.online = false;
    }

    // Constructor with basic details
    public Vendor(String id, String name, String email, String vanName) {
        this();
        this.id = id;
        this.name = name;
        this.email = email;
        this.vanName = vanName;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVanName() {
        return vanName;
    }

    public void setVanName(String vanName) {
        this.vanName = vanName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    // Compatibility for "isOnline" field in JSON
    public void setIsOnline(boolean isOnline) {
        this.online = isOnline;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public void setLastActiveTime(long lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastLocationUpdate() {
        return lastLocationUpdate;
    }

    public void setLastLocationUpdate(long lastLocationUpdate) {
        this.lastLocationUpdate = lastLocationUpdate;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public Map<String, String> getOperatingHours() {
        return operatingHours;
    }

    public void setOperatingHours(Map<String, String> operatingHours) {
        this.operatingHours = operatingHours;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }

    // Getters and setters for additional fields
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

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getCuisineType() {
        return cuisineType;
    }

    public void setCuisineType(String cuisineType) {
        this.cuisineType = cuisineType;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    
    // Compatibility for "isActive" field in JSON
    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getDeactivatedAt() {
        return deactivatedAt;
    }

    public void setDeactivatedAt(long deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public boolean isMessagesEnabled() {
        return messagesEnabled;
    }

    public void setMessagesEnabled(boolean messagesEnabled) {
        this.messagesEnabled = messagesEnabled;
    }

    public boolean isReviewsEnabled() {
        return reviewsEnabled;
    }

    public void setReviewsEnabled(boolean reviewsEnabled) {
        this.reviewsEnabled = reviewsEnabled;
    }

    public boolean isProfileVisible() {
        return profileVisible;
    }

    public void setProfileVisible(boolean profileVisible) {
        this.profileVisible = profileVisible;
    }

    public boolean isContactInfoVisible() {
        return contactInfoVisible;
    }

    public void setContactInfoVisible(boolean contactInfoVisible) {
        this.contactInfoVisible = contactInfoVisible;
    }

    public Map<String, Object> getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Map<String, Object> privacy) {
        this.privacy = privacy;
    }

    public Map<String, Object> getPrivacySettings() {
        return privacySettings;
    }

    public void setPrivacySettings(Map<String, Object> privacySettings) {
        this.privacySettings = privacySettings;
    }

    // Utility methods
    public void incrementTotalOrders() {
        this.totalOrders++;
    }

    public void addEarnings(double amount) {
        this.totalEarnings += amount;
    }

    public boolean isCurrentlyOnline() {
        return online && (System.currentTimeMillis() - lastSeen) < 5 * 60 * 1000; // 5 minutes
    }

    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }

    public String getFormattedEarnings() {
        return String.format("₹%.2f", totalEarnings);
    }

    // Convert to Map for Firebase updates
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("name", name);
        result.put("email", email);
        result.put("phone", phone);
        result.put("vanName", vanName);
        result.put("description", description);
        result.put("avatarUrl", avatarUrl);
        result.put("address", address);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("online", online);
        result.put("isOnline", online); // Ensure consistency
        result.put("lastSeen", lastSeen);
        result.put("lastActiveTime", lastActiveTime);
        result.put("createdAt", createdAt);
        result.put("lastLocationUpdate", lastLocationUpdate);
        result.put("fcmToken", fcmToken);
        result.put("operatingHours", operatingHours);
        result.put("rating", rating);
        result.put("totalOrders", totalOrders);
        result.put("totalEarnings", totalEarnings);
        result.put("verified", verified);
        result.put("licenseNumber", licenseNumber);
        result.put("category", category);
        result.put("isActive", isActive);
        return result;
    }

    @Override
    public String toString() {
        return "Vendor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", vanName='" + vanName + '\'' +
                ", online=" + online +
                ", rating=" + rating +
                '}';
    }
}
