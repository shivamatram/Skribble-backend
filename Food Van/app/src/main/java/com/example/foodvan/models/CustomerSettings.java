package com.example.foodvan.models;

import java.io.Serializable;

/**
 * CustomerSettings - Model for storing customer preferences and settings
 */
public class CustomerSettings implements Serializable {
    // Notification Settings
    private boolean notificationsEnabled;
    private boolean orderUpdatesEnabled;
    private boolean promotionsEnabled;
    private boolean systemAlertsEnabled;
    
    // Location & Map Settings
    private boolean useGpsForNearbyVans;
    private String preferredMapView; // "map", "list", "both"
    
    // Privacy & Data Settings
    private boolean shareOrderDataWithVendors;
    private boolean allowPersonalizedRecommendations;
    
    // Display & Theme Settings
    private String themeMode; // "system", "light", "dark"
    private boolean highContrastMode;
    private String appLanguage; // "en", "hi", "mr", etc.
    private String region; // "IN", "US", etc.
    
    // Order Preferences
    private String defaultOrderSort; // "newest", "oldest", "status"
    private boolean showOrderSuggestions;
    
    // Metadata
    private long lastUpdated;
    private String userId;

    public CustomerSettings() {
        // Default constructor for Firebase
        this.notificationsEnabled = true;
        this.orderUpdatesEnabled = true;
        this.promotionsEnabled = true;
        this.systemAlertsEnabled = true;
        this.useGpsForNearbyVans = true;
        this.preferredMapView = "both";
        this.shareOrderDataWithVendors = false;
        this.allowPersonalizedRecommendations = true;
        this.themeMode = "system";
        this.highContrastMode = false;
        this.appLanguage = "en";
        this.region = "IN";
        this.defaultOrderSort = "newest";
        this.showOrderSuggestions = true;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Notification Settings Getters & Setters
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isOrderUpdatesEnabled() {
        return orderUpdatesEnabled;
    }

    public void setOrderUpdatesEnabled(boolean orderUpdatesEnabled) {
        this.orderUpdatesEnabled = orderUpdatesEnabled;
    }

    public boolean isPromotionsEnabled() {
        return promotionsEnabled;
    }

    public void setPromotionsEnabled(boolean promotionsEnabled) {
        this.promotionsEnabled = promotionsEnabled;
    }

    public boolean isSystemAlertsEnabled() {
        return systemAlertsEnabled;
    }

    public void setSystemAlertsEnabled(boolean systemAlertsEnabled) {
        this.systemAlertsEnabled = systemAlertsEnabled;
    }

    // Location & Map Settings Getters & Setters
    public boolean isUseGpsForNearbyVans() {
        return useGpsForNearbyVans;
    }

    public void setUseGpsForNearbyVans(boolean useGpsForNearbyVans) {
        this.useGpsForNearbyVans = useGpsForNearbyVans;
    }

    public String getPreferredMapView() {
        return preferredMapView;
    }

    public void setPreferredMapView(String preferredMapView) {
        this.preferredMapView = preferredMapView;
    }

    // Privacy & Data Settings Getters & Setters
    public boolean isShareOrderDataWithVendors() {
        return shareOrderDataWithVendors;
    }

    public void setShareOrderDataWithVendors(boolean shareOrderDataWithVendors) {
        this.shareOrderDataWithVendors = shareOrderDataWithVendors;
    }

    public boolean isAllowPersonalizedRecommendations() {
        return allowPersonalizedRecommendations;
    }

    public void setAllowPersonalizedRecommendations(boolean allowPersonalizedRecommendations) {
        this.allowPersonalizedRecommendations = allowPersonalizedRecommendations;
    }

    // Display & Theme Settings Getters & Setters
    public String getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(String themeMode) {
        this.themeMode = themeMode;
    }

    public boolean isHighContrastMode() {
        return highContrastMode;
    }

    public void setHighContrastMode(boolean highContrastMode) {
        this.highContrastMode = highContrastMode;
    }

    public String getAppLanguage() {
        return appLanguage;
    }

    public void setAppLanguage(String appLanguage) {
        this.appLanguage = appLanguage;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    // Order Preferences Getters & Setters
    public String getDefaultOrderSort() {
        return defaultOrderSort;
    }

    public void setDefaultOrderSort(String defaultOrderSort) {
        this.defaultOrderSort = defaultOrderSort;
    }

    public boolean isShowOrderSuggestions() {
        return showOrderSuggestions;
    }

    public void setShowOrderSuggestions(boolean showOrderSuggestions) {
        this.showOrderSuggestions = showOrderSuggestions;
    }

    // Metadata Getters & Setters
    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
