package com.example.foodvan.viewmodels;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.CustomerSettings;
import com.example.foodvan.utils.SettingsManager;

/**
 * CustomerSettingsViewModel - Handles customer settings UI logic
 */
public class CustomerSettingsViewModel extends AndroidViewModel {
    private SettingsManager settingsManager;
    private MutableLiveData<CustomerSettings> settingsLiveData;
    private MutableLiveData<Boolean> loadingLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<String> successMessageLiveData;

    public CustomerSettingsViewModel(Application application) {
        super(application);
        this.settingsManager = new SettingsManager(application);
        this.settingsLiveData = new MutableLiveData<>();
        this.loadingLiveData = new MutableLiveData<>(false);
        this.errorLiveData = new MutableLiveData<>();
        this.successMessageLiveData = new MutableLiveData<>();
        
        loadSettings();
    }

    /**
     * Load settings from local cache
     */
    public void loadSettings() {
        loadingLiveData.setValue(true);
        try {
            CustomerSettings settings = settingsManager.loadSettings();
            settingsLiveData.setValue(settings);
            loadingLiveData.setValue(false);
        } catch (Exception e) {
            errorLiveData.setValue("Failed to load settings: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    /**
     * Save settings
     */
    public void saveSettings(CustomerSettings settings) {
        loadingLiveData.setValue(true);
        try {
            settingsManager.saveSettings(settings);
            settingsLiveData.setValue(settings);
            successMessageLiveData.setValue("Settings saved successfully");
            loadingLiveData.setValue(false);
        } catch (Exception e) {
            errorLiveData.setValue("Failed to save settings: " + e.getMessage());
            loadingLiveData.setValue(false);
        }
    }

    /**
     * Toggle notifications enabled
     */
    public void toggleNotificationsEnabled(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setNotificationsEnabled(enabled);
            settingsManager.updateNotificationSetting("notifications_enabled", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle order updates
     */
    public void toggleOrderUpdates(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setOrderUpdatesEnabled(enabled);
            settingsManager.updateNotificationSetting("order_updates", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle promotions
     */
    public void togglePromotions(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setPromotionsEnabled(enabled);
            settingsManager.updateNotificationSetting("promotions", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle system alerts
     */
    public void toggleSystemAlerts(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setSystemAlertsEnabled(enabled);
            settingsManager.updateNotificationSetting("system_alerts", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle GPS usage
     */
    public void toggleUseGps(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setUseGpsForNearbyVans(enabled);
            settingsManager.updateNotificationSetting("use_gps", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Set map view preference
     */
    public void setMapViewPreference(String mapView) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setPreferredMapView(mapView);
            settingsManager.updateSetting("map_view", mapView);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle data sharing
     */
    public void toggleDataSharing(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setShareOrderDataWithVendors(enabled);
            settingsManager.updateNotificationSetting("share_data", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle personalized recommendations
     */
    public void toggleRecommendations(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setAllowPersonalizedRecommendations(enabled);
            settingsManager.updateNotificationSetting("recommendations", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Set theme mode
     */
    public void setThemeMode(String themeMode) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setThemeMode(themeMode);
            settingsManager.updateSetting("theme", themeMode);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle high contrast mode
     */
    public void toggleHighContrast(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setHighContrastMode(enabled);
            settingsManager.updateNotificationSetting("high_contrast", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Set app language
     */
    public void setAppLanguage(String language) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setAppLanguage(language);
            settingsManager.updateSetting("app_language", language);
            settingsLiveData.setValue(settings);
            successMessageLiveData.setValue("Language changed to " + language);
        }
    }

    /**
     * Set region
     */
    public void setRegion(String region) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setRegion(region);
            settingsManager.updateSetting("region", region);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Set order sort preference
     */
    public void setOrderSort(String sortBy) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setDefaultOrderSort(sortBy);
            settingsManager.updateSetting("order_sort", sortBy);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Toggle order suggestions
     */
    public void toggleOrderSuggestions(boolean enabled) {
        CustomerSettings settings = settingsLiveData.getValue();
        if (settings != null) {
            settings.setShowOrderSuggestions(enabled);
            settingsManager.updateNotificationSetting("order_suggestions", enabled);
            settingsLiveData.setValue(settings);
        }
    }

    /**
     * Clear search history
     */
    public void clearSearchHistory() {
        try {
            settingsManager.clearSearchHistory();
            successMessageLiveData.setValue("Search history cleared");
        } catch (Exception e) {
            errorLiveData.setValue("Failed to clear search history");
        }
    }

    /**
     * Clear recently viewed items
     */
    public void clearRecentlyViewed() {
        try {
            settingsManager.clearRecentlyViewed();
            successMessageLiveData.setValue("Recently viewed items cleared");
        } catch (Exception e) {
            errorLiveData.setValue("Failed to clear recently viewed items");
        }
    }

    // LiveData Getters
    public LiveData<CustomerSettings> getSettingsLiveData() {
        return settingsLiveData;
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return loadingLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<String> getSuccessMessageLiveData() {
        return successMessageLiveData;
    }
}
