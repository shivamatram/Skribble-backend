package com.example.foodvan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.foodvan.models.CustomerSettings;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

/**
 * SettingsManager - Handles customer settings persistence
 * Uses local SharedPreferences for fast access and Firebase for cloud sync
 */
public class SettingsManager {
    private static final String PREF_NAME = "CustomerSettings";
    private static final String TAG = "SettingsManager";
    
    // Preference Keys
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_ORDER_UPDATES = "order_updates";
    private static final String KEY_PROMOTIONS = "promotions";
    private static final String KEY_SYSTEM_ALERTS = "system_alerts";
    private static final String KEY_USE_GPS = "use_gps";
    private static final String KEY_MAP_VIEW = "map_view";
    private static final String KEY_SHARE_DATA = "share_data";
    private static final String KEY_RECOMMENDATIONS = "recommendations";
    private static final String KEY_THEME = "theme";
    private static final String KEY_HIGH_CONTRAST = "high_contrast";
    private static final String KEY_LANGUAGE = "app_language";
    private static final String KEY_REGION = "region";
    private static final String KEY_ORDER_SORT = "order_sort";
    private static final String KEY_ORDER_SUGGESTIONS = "order_suggestions";
    private static final String KEY_LAST_UPDATED = "last_updated";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDb;
    private SessionManager sessionManager;

    public SettingsManager(Context context) {
        this.context = context;
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseDb = FirebaseDatabase.getInstance().getReference();
        this.sessionManager = new SessionManager(context);
        
        // Initialize with defaults if first time
        initializeDefaults();
    }

    /**
     * Initialize default settings on first run
     */
    private void initializeDefaults() {
        if (!pref.contains(KEY_NOTIFICATIONS_ENABLED)) {
            saveDefaultSettings();
        }
    }

    /**
     * Save default settings
     */
    private void saveDefaultSettings() {
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        editor.putBoolean(KEY_ORDER_UPDATES, true);
        editor.putBoolean(KEY_PROMOTIONS, true);
        editor.putBoolean(KEY_SYSTEM_ALERTS, true);
        editor.putBoolean(KEY_USE_GPS, true);
        editor.putString(KEY_MAP_VIEW, "both");
        editor.putBoolean(KEY_SHARE_DATA, false);
        editor.putBoolean(KEY_RECOMMENDATIONS, true);
        editor.putString(KEY_THEME, "system");
        editor.putBoolean(KEY_HIGH_CONTRAST, false);
        editor.putString(KEY_LANGUAGE, "en");
        editor.putString(KEY_REGION, "IN");
        editor.putString(KEY_ORDER_SORT, "newest");
        editor.putBoolean(KEY_ORDER_SUGGESTIONS, true);
        editor.apply();
    }

    /**
     * Load settings from local cache
     */
    public CustomerSettings loadSettings() {
        CustomerSettings settings = new CustomerSettings();
        settings.setNotificationsEnabled(pref.getBoolean(KEY_NOTIFICATIONS_ENABLED, true));
        settings.setOrderUpdatesEnabled(pref.getBoolean(KEY_ORDER_UPDATES, true));
        settings.setPromotionsEnabled(pref.getBoolean(KEY_PROMOTIONS, true));
        settings.setSystemAlertsEnabled(pref.getBoolean(KEY_SYSTEM_ALERTS, true));
        settings.setUseGpsForNearbyVans(pref.getBoolean(KEY_USE_GPS, true));
        settings.setPreferredMapView(pref.getString(KEY_MAP_VIEW, "both"));
        settings.setShareOrderDataWithVendors(pref.getBoolean(KEY_SHARE_DATA, false));
        settings.setAllowPersonalizedRecommendations(pref.getBoolean(KEY_RECOMMENDATIONS, true));
        settings.setThemeMode(pref.getString(KEY_THEME, "system"));
        settings.setHighContrastMode(pref.getBoolean(KEY_HIGH_CONTRAST, false));
        settings.setAppLanguage(pref.getString(KEY_LANGUAGE, "en"));
        settings.setRegion(pref.getString(KEY_REGION, "IN"));
        settings.setDefaultOrderSort(pref.getString(KEY_ORDER_SORT, "newest"));
        settings.setShowOrderSuggestions(pref.getBoolean(KEY_ORDER_SUGGESTIONS, true));
        settings.setLastUpdated(pref.getLong(KEY_LAST_UPDATED, System.currentTimeMillis()));
        settings.setUserId(sessionManager.getUserId());
        
        return settings;
    }

    /**
     * Save settings to local cache and Firebase
     */
    public void saveSettings(CustomerSettings settings) {
        if (settings == null) return;
        
        // Save to local SharedPreferences
        editor.putBoolean(KEY_NOTIFICATIONS_ENABLED, settings.isNotificationsEnabled());
        editor.putBoolean(KEY_ORDER_UPDATES, settings.isOrderUpdatesEnabled());
        editor.putBoolean(KEY_PROMOTIONS, settings.isPromotionsEnabled());
        editor.putBoolean(KEY_SYSTEM_ALERTS, settings.isSystemAlertsEnabled());
        editor.putBoolean(KEY_USE_GPS, settings.isUseGpsForNearbyVans());
        editor.putString(KEY_MAP_VIEW, settings.getPreferredMapView());
        editor.putBoolean(KEY_SHARE_DATA, settings.isShareOrderDataWithVendors());
        editor.putBoolean(KEY_RECOMMENDATIONS, settings.isAllowPersonalizedRecommendations());
        editor.putString(KEY_THEME, settings.getThemeMode());
        editor.putBoolean(KEY_HIGH_CONTRAST, settings.isHighContrastMode());
        editor.putString(KEY_LANGUAGE, settings.getAppLanguage());
        editor.putString(KEY_REGION, settings.getRegion());
        editor.putString(KEY_ORDER_SORT, settings.getDefaultOrderSort());
        editor.putBoolean(KEY_ORDER_SUGGESTIONS, settings.isShowOrderSuggestions());
        editor.putLong(KEY_LAST_UPDATED, System.currentTimeMillis());
        editor.apply();
        
        // Sync to Firebase
        syncToFirebase(settings);
    }

    /**
     * Sync settings to Firebase (async)
     */
    private void syncToFirebase(CustomerSettings settings) {
        try {
            String userId = sessionManager.getUserId();
            if (userId == null || userId.isEmpty()) {
                Log.w(TAG, "No user ID available for Firebase sync");
                return;
            }

            Map<String, Object> settingsMap = new HashMap<>();
            settingsMap.put("notificationsEnabled", settings.isNotificationsEnabled());
            settingsMap.put("orderUpdatesEnabled", settings.isOrderUpdatesEnabled());
            settingsMap.put("promotionsEnabled", settings.isPromotionsEnabled());
            settingsMap.put("systemAlertsEnabled", settings.isSystemAlertsEnabled());
            settingsMap.put("useGpsForNearbyVans", settings.isUseGpsForNearbyVans());
            settingsMap.put("preferredMapView", settings.getPreferredMapView());
            settingsMap.put("shareOrderDataWithVendors", settings.isShareOrderDataWithVendors());
            settingsMap.put("allowPersonalizedRecommendations", settings.isAllowPersonalizedRecommendations());
            settingsMap.put("themeMode", settings.getThemeMode());
            settingsMap.put("highContrastMode", settings.isHighContrastMode());
            settingsMap.put("appLanguage", settings.getAppLanguage());
            settingsMap.put("region", settings.getRegion());
            settingsMap.put("defaultOrderSort", settings.getDefaultOrderSort());
            settingsMap.put("showOrderSuggestions", settings.isShowOrderSuggestions());
            settingsMap.put("lastUpdated", System.currentTimeMillis());
            settingsMap.put("userId", userId);

            firebaseDb.child("customers").child(userId).child("settings").updateChildren(settingsMap)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Settings synced to Firebase"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to sync settings to Firebase", e));
        } catch (Exception e) {
            Log.e(TAG, "Error syncing to Firebase", e);
        }
    }

    /**
     * Update individual notification setting
     */
    public void updateNotificationSetting(String settingKey, boolean value) {
        editor.putBoolean(settingKey, value);
        editor.apply();
        
        CustomerSettings settings = loadSettings();
        syncToFirebase(settings);
    }

    /**
     * Update individual setting
     */
    public void updateSetting(String key, String value) {
        editor.putString(key, value);
        editor.apply();
        
        CustomerSettings settings = loadSettings();
        syncToFirebase(settings);
    }

    public void updateSetting(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
        
        CustomerSettings settings = loadSettings();
        syncToFirebase(settings);
    }

    /**
     * Clear search history (simulate)
     */
    public void clearSearchHistory() {
        // This would integrate with actual search history storage
        Log.d(TAG, "Search history cleared");
    }

    /**
     * Clear recently viewed items (simulate)
     */
    public void clearRecentlyViewed() {
        // This would integrate with actual recently viewed storage
        Log.d(TAG, "Recently viewed items cleared");
    }

    /**
     * Get notification settings
     */
    public boolean areNotificationsEnabled() {
        return pref.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
    }

    /**
     * Get theme mode
     */
    public String getThemeMode() {
        return pref.getString(KEY_THEME, "system");
    }

    /**
     * Get app language
     */
    public String getAppLanguage() {
        return pref.getString(KEY_LANGUAGE, "en");
    }
}
