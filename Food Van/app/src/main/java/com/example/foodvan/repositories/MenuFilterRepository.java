package com.example.foodvan.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;

import com.example.foodvan.models.MenuFilter;
import com.example.foodvan.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing menu filter data
 * Handles local persistence and Firebase storage of filter presets
 */
public class MenuFilterRepository {
    
    private static final String PREFS_NAME = "menu_filter_prefs";
    private static final String KEY_LAST_USED_FILTER = "last_used_filter";
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    private final SessionManager sessionManager;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseRef;
    
    public MenuFilterRepository(Context context) {
        this.context = context.getApplicationContext();
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.sessionManager = new SessionManager(context);
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
    }
    
    // Local persistence methods
    
    /**
     * Save the last used filter to SharedPreferences
     */
    public void saveLastUsedFilter(MenuFilter filter) {
        try {
            String filterJson = gson.toJson(filter);
            preferences.edit()
                    .putString(KEY_LAST_USED_FILTER, filterJson)
                    .apply();
        } catch (Exception e) {
            // Ignore errors in saving preferences
        }
    }
    
    /**
     * Get the last used filter from SharedPreferences
     */
    public MenuFilter getLastUsedFilter() {
        try {
            String filterJson = preferences.getString(KEY_LAST_USED_FILTER, null);
            if (filterJson != null) {
                return gson.fromJson(filterJson, MenuFilter.class);
            }
        } catch (JsonSyntaxException e) {
            // Clear corrupted data
            clearLastUsedFilter();
        }
        return null;
    }
    
    /**
     * Clear the last used filter from SharedPreferences
     */
    public void clearLastUsedFilter() {
        preferences.edit()
                .remove(KEY_LAST_USED_FILTER)
                .apply();
    }
    
    // Firebase methods for filter presets
    
    /**
     * Save a filter preset to Firebase
     */
    public void saveFilterPreset(MenuFilter filter, SavePresetCallback callback) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        try {
            // Generate preset ID if not exists
            DatabaseReference presetsRef = databaseRef
                    .child("vendors")
                    .child(vendorId)
                    .child("menu")
                    .child("filterPresets");
            
            String presetId = filter.getPresetId();
            if (presetId == null || presetId.isEmpty()) {
                presetId = presetsRef.push().getKey();
                filter.setPresetId(presetId);
            }
            
            // Make presetId final for lambda
            final String finalPresetId = presetId;
            
            // Convert filter to Firebase-compatible map
            Map<String, Object> presetData = filterToMap(filter);
            
            presetsRef.child(finalPresetId)
                    .setValue(presetData)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(finalPresetId))
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    
        } catch (Exception e) {
            callback.onError("Error saving preset: " + e.getMessage());
        }
    }
    
    /**
     * Load a specific filter preset from Firebase
     */
    public void loadFilterPreset(String presetId, LoadPresetCallback callback) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        try {
            databaseRef.child("vendors")
                    .child(vendorId)
                    .child("menu")
                    .child("filterPresets")
                    .child(presetId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                if (snapshot.exists()) {
                                    MenuFilter filter = mapToFilter(snapshot);
                                    callback.onSuccess(filter);
                                } else {
                                    callback.onError("Preset not found");
                                }
                            } catch (Exception e) {
                                callback.onError("Error parsing preset: " + e.getMessage());
                            }
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError("Database error: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onError("Error loading preset: " + e.getMessage());
        }
    }
    
    /**
     * Load all filter presets for the current vendor
     */
    public void loadAllFilterPresets(LoadPresetsCallback callback) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        try {
            databaseRef.child("vendors")
                    .child(vendorId)
                    .child("menu")
                    .child("filterPresets")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                List<MenuFilter> presets = new ArrayList<>();
                                
                                for (DataSnapshot presetSnapshot : snapshot.getChildren()) {
                                    MenuFilter filter = mapToFilter(presetSnapshot);
                                    presets.add(filter);
                                }
                                
                                // Sort by creation date (newest first)
                                presets.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                                
                                callback.onSuccess(presets);
                            } catch (Exception e) {
                                callback.onError("Error parsing presets: " + e.getMessage());
                            }
                        }
                        
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            callback.onError("Database error: " + error.getMessage());
                        }
                    });
        } catch (Exception e) {
            callback.onError("Error loading presets: " + e.getMessage());
        }
    }
    
    /**
     * Delete a filter preset from Firebase
     */
    public void deleteFilterPreset(String presetId, DeletePresetCallback callback) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            callback.onError("User not authenticated");
            return;
        }
        
        try {
            databaseRef.child("vendors")
                    .child(vendorId)
                    .child("menu")
                    .child("filterPresets")
                    .child(presetId)
                    .removeValue()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        } catch (Exception e) {
            callback.onError("Error deleting preset: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private String getCurrentVendorId() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getUid();
        }
        return sessionManager.getUserId();
    }
    
    private Map<String, Object> filterToMap(MenuFilter filter) {
        Map<String, Object> map = new HashMap<>();
        
        map.put("searchQuery", filter.getSearchQuery());
        map.put("selectedCategories", filter.getSelectedCategories());
        map.put("allCategoriesSelected", filter.isAllCategoriesSelected());
        map.put("showAvailable", filter.isShowAvailable());
        map.put("showOutOfStock", filter.isShowOutOfStock());
        map.put("minPrice", filter.getMinPrice());
        map.put("maxPrice", filter.getMaxPrice());
        map.put("sortBy", filter.getSortBy().getValue());
        map.put("selectedTags", filter.getSelectedTags());
        map.put("vegetarianOnly", filter.isVegetarianOnly());
        map.put("nonVegetarianOnly", filter.isNonVegetarianOnly());
        map.put("specialOffersOnly", filter.isSpecialOffersOnly());
        map.put("stockThreshold", filter.getStockThreshold());
        map.put("presetId", filter.getPresetId());
        map.put("presetName", filter.getPresetName());
        map.put("createdAt", filter.getCreatedAt());
        
        return map;
    }
    
    @SuppressWarnings("unchecked")
    private MenuFilter mapToFilter(DataSnapshot snapshot) {
        MenuFilter filter = new MenuFilter();
        
        filter.setPresetId(snapshot.getKey());
        
        if (snapshot.child("searchQuery").exists()) {
            filter.setSearchQuery(snapshot.child("searchQuery").getValue(String.class));
        }
        
        if (snapshot.child("selectedCategories").exists()) {
            List<String> categories = new ArrayList<>();
            for (DataSnapshot categorySnapshot : snapshot.child("selectedCategories").getChildren()) {
                String category = categorySnapshot.getValue(String.class);
                if (category != null) {
                    categories.add(category);
                }
            }
            filter.setSelectedCategories(categories);
        }
        
        if (snapshot.child("allCategoriesSelected").exists()) {
            filter.setAllCategoriesSelected(snapshot.child("allCategoriesSelected").getValue(Boolean.class));
        }
        
        if (snapshot.child("showAvailable").exists()) {
            filter.setShowAvailable(snapshot.child("showAvailable").getValue(Boolean.class));
        }
        
        if (snapshot.child("showOutOfStock").exists()) {
            filter.setShowOutOfStock(snapshot.child("showOutOfStock").getValue(Boolean.class));
        }
        
        if (snapshot.child("minPrice").exists()) {
            Double minPrice = snapshot.child("minPrice").getValue(Double.class);
            if (minPrice != null) {
                filter.setMinPrice(minPrice.floatValue());
            }
        }
        
        if (snapshot.child("maxPrice").exists()) {
            Double maxPrice = snapshot.child("maxPrice").getValue(Double.class);
            if (maxPrice != null) {
                filter.setMaxPrice(maxPrice.floatValue());
            }
        }
        
        if (snapshot.child("sortBy").exists()) {
            String sortBy = snapshot.child("sortBy").getValue(String.class);
            if (sortBy != null) {
                filter.setSortBy(MenuFilter.SortOption.fromValue(sortBy));
            }
        }
        
        if (snapshot.child("selectedTags").exists()) {
            List<String> tags = new ArrayList<>();
            for (DataSnapshot tagSnapshot : snapshot.child("selectedTags").getChildren()) {
                String tag = tagSnapshot.getValue(String.class);
                if (tag != null) {
                    tags.add(tag);
                }
            }
            filter.setSelectedTags(tags);
        }
        
        if (snapshot.child("vegetarianOnly").exists()) {
            filter.setVegetarianOnly(snapshot.child("vegetarianOnly").getValue(Boolean.class));
        }
        
        if (snapshot.child("nonVegetarianOnly").exists()) {
            filter.setNonVegetarianOnly(snapshot.child("nonVegetarianOnly").getValue(Boolean.class));
        }
        
        if (snapshot.child("specialOffersOnly").exists()) {
            filter.setSpecialOffersOnly(snapshot.child("specialOffersOnly").getValue(Boolean.class));
        }
        
        if (snapshot.child("stockThreshold").exists()) {
            Integer threshold = snapshot.child("stockThreshold").getValue(Integer.class);
            if (threshold != null) {
                filter.setStockThreshold(threshold);
            }
        }
        
        if (snapshot.child("presetName").exists()) {
            filter.setPresetName(snapshot.child("presetName").getValue(String.class));
        }
        
        if (snapshot.child("createdAt").exists()) {
            Long createdAt = snapshot.child("createdAt").getValue(Long.class);
            if (createdAt != null) {
                filter.setCreatedAt(createdAt);
            }
        }
        
        return filter;
    }
    
    // Callback interfaces
    
    public interface SavePresetCallback {
        void onSuccess(String presetId);
        void onError(String error);
    }
    
    public interface LoadPresetCallback {
        void onSuccess(MenuFilter filter);
        void onError(String error);
    }
    
    public interface LoadPresetsCallback {
        void onSuccess(List<MenuFilter> presets);
        void onError(String error);
    }
    
    public interface DeletePresetCallback {
        void onSuccess();
        void onError(String error);
    }
}
