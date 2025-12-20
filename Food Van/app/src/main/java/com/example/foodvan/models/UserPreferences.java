package com.example.foodvan.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User preferences model for personalization
 */
public class UserPreferences implements Serializable {
    
    private boolean notificationsEnabled;
    private boolean emailNotifications;
    private boolean smsNotifications;
    private boolean pushNotifications;
    private boolean orderUpdates;
    private boolean promotionalOffers;
    private boolean newVendorAlerts;
    
    // App preferences
    private boolean darkModeEnabled;
    private String language;
    private String currency;
    private boolean locationServicesEnabled;
    private boolean autoLocationUpdate;
    
    // Food preferences
    private List<String> favoriteCuisines;
    private List<String> dietaryRestrictions;
    private String spiceLevel; // Mild, Medium, Hot, Extra Hot
    private boolean vegetarianOnly;
    private boolean veganOnly;
    private boolean glutenFree;
    private boolean dairyFree;
    
    // Order preferences
    private String defaultPaymentMethod;
    private boolean savePaymentMethods;
    private boolean quickReorder;
    private boolean contactlessDelivery;
    private String preferredDeliveryTime;
    private int maxDeliveryDistance; // in km
    
    // Privacy preferences
    private boolean shareLocationData;
    private boolean shareOrderHistory;
    private boolean allowDataCollection;
    private boolean allowPersonalization;
    
    public UserPreferences() {
        // Default values
        this.notificationsEnabled = true;
        this.emailNotifications = true;
        this.smsNotifications = false;
        this.pushNotifications = true;
        this.orderUpdates = true;
        this.promotionalOffers = false;
        this.newVendorAlerts = false;
        
        this.darkModeEnabled = false;
        this.language = "English";
        this.currency = "INR";
        this.locationServicesEnabled = true;
        this.autoLocationUpdate = true;
        
        this.favoriteCuisines = new ArrayList<>();
        this.dietaryRestrictions = new ArrayList<>();
        this.spiceLevel = "Medium";
        this.vegetarianOnly = false;
        this.veganOnly = false;
        this.glutenFree = false;
        this.dairyFree = false;
        
        this.defaultPaymentMethod = "Cash";
        this.savePaymentMethods = true;
        this.quickReorder = true;
        this.contactlessDelivery = false;
        this.preferredDeliveryTime = "ASAP";
        this.maxDeliveryDistance = 10;
        
        this.shareLocationData = true;
        this.shareOrderHistory = false;
        this.allowDataCollection = true;
        this.allowPersonalization = true;
    }
    
    // Notification preferences
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }
    
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
    
    public boolean isEmailNotifications() {
        return emailNotifications;
    }
    
    public void setEmailNotifications(boolean emailNotifications) {
        this.emailNotifications = emailNotifications;
    }
    
    public boolean isSmsNotifications() {
        return smsNotifications;
    }
    
    public void setSmsNotifications(boolean smsNotifications) {
        this.smsNotifications = smsNotifications;
    }
    
    public boolean isPushNotifications() {
        return pushNotifications;
    }
    
    public void setPushNotifications(boolean pushNotifications) {
        this.pushNotifications = pushNotifications;
    }
    
    public boolean isOrderUpdates() {
        return orderUpdates;
    }
    
    public void setOrderUpdates(boolean orderUpdates) {
        this.orderUpdates = orderUpdates;
    }
    
    public boolean isPromotionalOffers() {
        return promotionalOffers;
    }
    
    public void setPromotionalOffers(boolean promotionalOffers) {
        this.promotionalOffers = promotionalOffers;
    }
    
    public boolean isNewVendorAlerts() {
        return newVendorAlerts;
    }
    
    public void setNewVendorAlerts(boolean newVendorAlerts) {
        this.newVendorAlerts = newVendorAlerts;
    }
    
    // App preferences
    public boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
    
    public void setDarkModeEnabled(boolean darkModeEnabled) {
        this.darkModeEnabled = darkModeEnabled;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public boolean isLocationServicesEnabled() {
        return locationServicesEnabled;
    }
    
    public void setLocationServicesEnabled(boolean locationServicesEnabled) {
        this.locationServicesEnabled = locationServicesEnabled;
    }
    
    public boolean isAutoLocationUpdate() {
        return autoLocationUpdate;
    }
    
    public void setAutoLocationUpdate(boolean autoLocationUpdate) {
        this.autoLocationUpdate = autoLocationUpdate;
    }
    
    // Food preferences
    public List<String> getFavoriteCuisines() {
        return favoriteCuisines;
    }
    
    public void setFavoriteCuisines(List<String> favoriteCuisines) {
        this.favoriteCuisines = favoriteCuisines;
    }
    
    public List<String> getDietaryRestrictions() {
        return dietaryRestrictions;
    }
    
    public void setDietaryRestrictions(List<String> dietaryRestrictions) {
        this.dietaryRestrictions = dietaryRestrictions;
    }
    
    public String getSpiceLevel() {
        return spiceLevel;
    }
    
    public void setSpiceLevel(String spiceLevel) {
        this.spiceLevel = spiceLevel;
    }
    
    public boolean isVegetarianOnly() {
        return vegetarianOnly;
    }
    
    public void setVegetarianOnly(boolean vegetarianOnly) {
        this.vegetarianOnly = vegetarianOnly;
    }
    
    public boolean isVeganOnly() {
        return veganOnly;
    }
    
    public void setVeganOnly(boolean veganOnly) {
        this.veganOnly = veganOnly;
    }
    
    public boolean isGlutenFree() {
        return glutenFree;
    }
    
    public void setGlutenFree(boolean glutenFree) {
        this.glutenFree = glutenFree;
    }
    
    public boolean isDairyFree() {
        return dairyFree;
    }
    
    public void setDairyFree(boolean dairyFree) {
        this.dairyFree = dairyFree;
    }
    
    // Order preferences
    public String getDefaultPaymentMethod() {
        return defaultPaymentMethod;
    }
    
    public void setDefaultPaymentMethod(String defaultPaymentMethod) {
        this.defaultPaymentMethod = defaultPaymentMethod;
    }
    
    public boolean isSavePaymentMethods() {
        return savePaymentMethods;
    }
    
    public void setSavePaymentMethods(boolean savePaymentMethods) {
        this.savePaymentMethods = savePaymentMethods;
    }
    
    public boolean isQuickReorder() {
        return quickReorder;
    }
    
    public void setQuickReorder(boolean quickReorder) {
        this.quickReorder = quickReorder;
    }
    
    public boolean isContactlessDelivery() {
        return contactlessDelivery;
    }
    
    public void setContactlessDelivery(boolean contactlessDelivery) {
        this.contactlessDelivery = contactlessDelivery;
    }
    
    public String getPreferredDeliveryTime() {
        return preferredDeliveryTime;
    }
    
    public void setPreferredDeliveryTime(String preferredDeliveryTime) {
        this.preferredDeliveryTime = preferredDeliveryTime;
    }
    
    public int getMaxDeliveryDistance() {
        return maxDeliveryDistance;
    }
    
    public void setMaxDeliveryDistance(int maxDeliveryDistance) {
        this.maxDeliveryDistance = maxDeliveryDistance;
    }
    
    // Privacy preferences
    public boolean isShareLocationData() {
        return shareLocationData;
    }
    
    public void setShareLocationData(boolean shareLocationData) {
        this.shareLocationData = shareLocationData;
    }
    
    public boolean isShareOrderHistory() {
        return shareOrderHistory;
    }
    
    public void setShareOrderHistory(boolean shareOrderHistory) {
        this.shareOrderHistory = shareOrderHistory;
    }
    
    public boolean isAllowDataCollection() {
        return allowDataCollection;
    }
    
    public void setAllowDataCollection(boolean allowDataCollection) {
        this.allowDataCollection = allowDataCollection;
    }
    
    public boolean isAllowPersonalization() {
        return allowPersonalization;
    }
    
    public void setAllowPersonalization(boolean allowPersonalization) {
        this.allowPersonalization = allowPersonalization;
    }
    
    // Helper methods
    public void addFavoriteCuisine(String cuisine) {
        if (favoriteCuisines == null) {
            favoriteCuisines = new ArrayList<>();
        }
        if (!favoriteCuisines.contains(cuisine)) {
            favoriteCuisines.add(cuisine);
        }
    }
    
    public void removeFavoriteCuisine(String cuisine) {
        if (favoriteCuisines != null) {
            favoriteCuisines.remove(cuisine);
        }
    }
    
    public void addDietaryRestriction(String restriction) {
        if (dietaryRestrictions == null) {
            dietaryRestrictions = new ArrayList<>();
        }
        if (!dietaryRestrictions.contains(restriction)) {
            dietaryRestrictions.add(restriction);
        }
    }
    
    public void removeDietaryRestriction(String restriction) {
        if (dietaryRestrictions != null) {
            dietaryRestrictions.remove(restriction);
        }
    }
    
    public boolean hasDietaryRestrictions() {
        return vegetarianOnly || veganOnly || glutenFree || dairyFree || 
               (dietaryRestrictions != null && !dietaryRestrictions.isEmpty());
    }
    
    public void resetToDefaults() {
        UserPreferences defaults = new UserPreferences();
        
        this.notificationsEnabled = defaults.notificationsEnabled;
        this.emailNotifications = defaults.emailNotifications;
        this.smsNotifications = defaults.smsNotifications;
        this.pushNotifications = defaults.pushNotifications;
        this.orderUpdates = defaults.orderUpdates;
        this.promotionalOffers = defaults.promotionalOffers;
        this.newVendorAlerts = defaults.newVendorAlerts;
        
        this.darkModeEnabled = defaults.darkModeEnabled;
        this.language = defaults.language;
        this.currency = defaults.currency;
        this.locationServicesEnabled = defaults.locationServicesEnabled;
        this.autoLocationUpdate = defaults.autoLocationUpdate;
        
        this.favoriteCuisines = new ArrayList<>(defaults.favoriteCuisines);
        this.dietaryRestrictions = new ArrayList<>(defaults.dietaryRestrictions);
        this.spiceLevel = defaults.spiceLevel;
        this.vegetarianOnly = defaults.vegetarianOnly;
        this.veganOnly = defaults.veganOnly;
        this.glutenFree = defaults.glutenFree;
        this.dairyFree = defaults.dairyFree;
        
        this.defaultPaymentMethod = defaults.defaultPaymentMethod;
        this.savePaymentMethods = defaults.savePaymentMethods;
        this.quickReorder = defaults.quickReorder;
        this.contactlessDelivery = defaults.contactlessDelivery;
        this.preferredDeliveryTime = defaults.preferredDeliveryTime;
        this.maxDeliveryDistance = defaults.maxDeliveryDistance;
        
        this.shareLocationData = defaults.shareLocationData;
        this.shareOrderHistory = defaults.shareOrderHistory;
        this.allowDataCollection = defaults.allowDataCollection;
        this.allowPersonalization = defaults.allowPersonalization;
    }
}
