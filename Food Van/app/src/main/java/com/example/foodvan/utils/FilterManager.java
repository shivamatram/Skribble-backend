package com.example.foodvan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.example.foodvan.models.FilterCriteria;
import com.example.foodvan.models.CuisineType;
import com.example.foodvan.models.PriceRange;
import com.example.foodvan.models.ServiceType;
import com.example.foodvan.models.SortBy;
import com.example.foodvan.models.SortOrder;
import com.example.foodvan.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for managing food van filters and sorting
 */
public class FilterManager {
    
    private static final String TAG = "FilterManager";
    private static final String PREFS_NAME = "filter_preferences";
    private static final String KEY_FILTER_CRITERIA = "filter_criteria";
    
    private Context context;
    private SharedPreferences preferences;
    private FilterCriteria currentFilter;
    private Location userLocation;
    
    // Callback interfaces
    public interface FilterResultCallback {
        void onFilterResults(List<User> filteredVendors, int totalCount);
        void onFilterError(String error);
    }
    
    public interface FilterUpdateCallback {
        void onFilterUpdated(FilterCriteria criteria);
        void onFilterCleared();
    }
    
    public FilterManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.currentFilter = new FilterCriteria();
        loadSavedFilters();
    }
    
    /**
     * Set user location for distance calculations
     */
    public void setUserLocation(Location location) {
        this.userLocation = location;
        Log.d(TAG, "User location updated: " + location.getLatitude() + ", " + location.getLongitude());
    }
    
    /**
     * Apply filters to vendor list
     */
    public void applyFilters(FilterCriteria criteria, FilterResultCallback callback) {
        this.currentFilter = criteria.copy();
        saveFilters();
        
        // Query Firebase for vendors
        DatabaseReference vendorsRef = FirebaseDatabase.getInstance().getReference("vendors");
        
        vendorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> allVendors = new ArrayList<>();
                
                // Parse all vendors from Firebase
                for (DataSnapshot vendorSnapshot : dataSnapshot.getChildren()) {
                    try {
                        User vendor = vendorSnapshot.getValue(User.class);
                        if (vendor != null) {
                            allVendors.add(vendor);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing vendor: " + e.getMessage());
                    }
                }
                
                // Apply filters
                List<User> filteredVendors = filterVendors(allVendors, criteria);
                
                // Sort results
                sortVendors(filteredVendors, criteria.getSortBy(), criteria.getSortOrder());
                
                // Return results
                callback.onFilterResults(filteredVendors, allVendors.size());
                
                Log.d(TAG, "Filter applied: " + filteredVendors.size() + " out of " + allVendors.size() + " vendors");
            }
            
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Firebase error: " + databaseError.getMessage());
                callback.onFilterError("Failed to load vendors: " + databaseError.getMessage());
            }
        });
    }
    
    /**
     * Filter vendors based on criteria
     */
    private List<User> filterVendors(List<User> vendors, FilterCriteria criteria) {
        List<User> filtered = new ArrayList<>();
        
        for (User vendor : vendors) {
            if (matchesFilter(vendor, criteria)) {
                filtered.add(vendor);
            }
        }
        
        return filtered;
    }
    
    /**
     * Check if vendor matches filter criteria
     */
    private boolean matchesFilter(User vendor, FilterCriteria criteria) {
        
        // Cuisine filter
        if (!criteria.getSelectedCuisines().isEmpty()) {
            boolean matchesCuisine = false;
            String vendorCuisine = vendor.getCuisineType();
            
            for (CuisineType cuisineType : criteria.getSelectedCuisines()) {
                if (cuisineType.getDisplayName().equalsIgnoreCase(vendorCuisine)) {
                    matchesCuisine = true;
                    break;
                }
            }
            
            if (!matchesCuisine) {
                return false;
            }
        }
        
        // Rating filter
        if (criteria.getMinRating() > 0) {
            if (vendor.getRating() < criteria.getMinRating()) {
                return false;
            }
        }
        
        // Distance filter
        if (userLocation != null && criteria.getMaxDistance() > 0) {
            float distance = calculateDistance(
                userLocation.getLatitude(), 
                userLocation.getLongitude(),
                vendor.getLatitude(), 
                vendor.getLongitude()
            );
            
            if (distance > criteria.getMaxDistance()) {
                return false;
            }
        }
        
        // Availability filter
        if (criteria.isOnlyOpenVans()) {
            if (!vendor.isOnline()) {
                return false;
            }
        }
        
        // Service type filter
        if (criteria.getServiceType() != ServiceType.ALL) {
            // This would need to be implemented based on your vendor model
            // For now, we'll assume all vendors support all service types
        }
        
        // Price range filter (would need average price in vendor model)
        if (criteria.getPriceRange() != PriceRange.ALL) {
            // This would need average price calculation from vendor's menu
            // For now, we'll skip this filter
        }
        
        return true;
    }
    
    /**
     * Sort vendors based on criteria
     */
    private void sortVendors(List<User> vendors, SortBy sortBy, SortOrder sortOrder) {
        Comparator<User> comparator = null;
        
        switch (sortBy) {
            case DISTANCE:
                if (userLocation != null) {
                    comparator = (v1, v2) -> {
                        float dist1 = calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(), 
                                                      v1.getLatitude(), v1.getLongitude());
                        float dist2 = calculateDistance(userLocation.getLatitude(), userLocation.getLongitude(), 
                                                      v2.getLatitude(), v2.getLongitude());
                        return Float.compare(dist1, dist2);
                    };
                }
                break;
                
            case RATING:
                comparator = (v1, v2) -> Double.compare(v2.getRating(), v1.getRating()); // Higher rating first
                break;
                
            case POPULARITY:
                comparator = (v1, v2) -> Integer.compare(v2.getTotalOrders(), v1.getTotalOrders()); // More orders first
                break;
                
            case NEWEST:
                comparator = (v1, v2) -> Long.compare(v2.getCreatedAt(), v1.getCreatedAt()); // Newer first
                break;
                
            case NAME:
                comparator = (v1, v2) -> v1.getBusinessName().compareToIgnoreCase(v2.getBusinessName());
                break;
                
            case PRICE:
                // Would need average price calculation
                comparator = (v1, v2) -> v1.getBusinessName().compareToIgnoreCase(v2.getBusinessName()); // Fallback to name
                break;
        }
        
        if (comparator != null) {
            Collections.sort(vendors, comparator);
            
            // Reverse if descending order (except for rating and popularity which are already desc)
            if (sortOrder == SortOrder.DESCENDING && 
                sortBy != SortBy.RATING && 
                sortBy != SortBy.POPULARITY && 
                sortBy != SortBy.NEWEST) {
                Collections.reverse(vendors);
            }
        }
    }
    
    /**
     * Calculate distance between two points in kilometers
     */
    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        Location loc1 = new Location("");
        loc1.setLatitude(lat1);
        loc1.setLongitude(lon1);
        
        Location loc2 = new Location("");
        loc2.setLatitude(lat2);
        loc2.setLongitude(lon2);
        
        return loc1.distanceTo(loc2) / 1000f; // Convert to kilometers
    }
    
    /**
     * Get current filter criteria
     */
    public FilterCriteria getCurrentFilter() {
        return currentFilter.copy();
    }
    
    /**
     * Clear all filters
     */
    public void clearFilters() {
        currentFilter.reset();
        saveFilters();
        Log.d(TAG, "All filters cleared");
    }
    
    /**
     * Check if any filters are active
     */
    public boolean hasActiveFilters() {
        return currentFilter.hasActiveFilters();
    }
    
    /**
     * Save filters to SharedPreferences
     */
    private void saveFilters() {
        // For simplicity, we'll save key filter values
        SharedPreferences.Editor editor = preferences.edit();
        
        // Save cuisine types as comma-separated string
        StringBuilder cuisines = new StringBuilder();
        for (CuisineType cuisine : currentFilter.getSelectedCuisines()) {
            if (cuisines.length() > 0) cuisines.append(",");
            cuisines.append(cuisine.name());
        }
        editor.putString("selected_cuisines", cuisines.toString());
        
        // Save other filter values
        editor.putString("price_range", currentFilter.getPriceRange().name());
        editor.putFloat("min_rating", currentFilter.getMinRating());
        editor.putFloat("max_distance", currentFilter.getMaxDistance());
        editor.putBoolean("only_open_vans", currentFilter.isOnlyOpenVans());
        editor.putString("service_type", currentFilter.getServiceType().name());
        editor.putString("sort_by", currentFilter.getSortBy().name());
        editor.putString("sort_order", currentFilter.getSortOrder().name());
        
        editor.apply();
        Log.d(TAG, "Filters saved to preferences");
    }
    
    /**
     * Load filters from SharedPreferences
     */
    private void loadSavedFilters() {
        try {
            // Load cuisine types
            String cuisinesStr = preferences.getString("selected_cuisines", "");
            if (!cuisinesStr.isEmpty()) {
                String[] cuisineNames = cuisinesStr.split(",");
                List<CuisineType> cuisines = new ArrayList<>();
                for (String name : cuisineNames) {
                    try {
                        cuisines.add(CuisineType.valueOf(name));
                    } catch (IllegalArgumentException e) {
                        Log.w(TAG, "Invalid cuisine type: " + name);
                    }
                }
                currentFilter.setSelectedCuisines(cuisines);
            }
            
            // Load other filter values
            String priceRangeStr = preferences.getString("price_range", PriceRange.ALL.name());
            currentFilter.setPriceRange(PriceRange.valueOf(priceRangeStr));
            
            currentFilter.setMinRating(preferences.getFloat("min_rating", 0f));
            currentFilter.setMaxDistance(preferences.getFloat("max_distance", 10f));
            currentFilter.setOnlyOpenVans(preferences.getBoolean("only_open_vans", false));
            
            String serviceTypeStr = preferences.getString("service_type", ServiceType.ALL.name());
            currentFilter.setServiceType(ServiceType.valueOf(serviceTypeStr));
            
            String sortByStr = preferences.getString("sort_by", SortBy.DISTANCE.name());
            currentFilter.setSortBy(SortBy.valueOf(sortByStr));
            
            String sortOrderStr = preferences.getString("sort_order", SortOrder.ASCENDING.name());
            currentFilter.setSortOrder(SortOrder.valueOf(sortOrderStr));
            
            Log.d(TAG, "Filters loaded from preferences");
            
        } catch (Exception e) {
            Log.e(TAG, "Error loading saved filters: " + e.getMessage());
            currentFilter.reset(); // Reset to defaults on error
        }
    }
    
    /**
     * Get filter summary text for UI
     */
    public String getFilterSummary() {
        if (!hasActiveFilters()) {
            return "No filters applied";
        }
        
        StringBuilder summary = new StringBuilder();
        
        if (!currentFilter.getSelectedCuisines().isEmpty()) {
            summary.append(currentFilter.getSelectedCuisines().size()).append(" cuisine(s)");
        }
        
        if (currentFilter.getMinRating() > 0) {
            if (summary.length() > 0) summary.append(", ");
            summary.append(currentFilter.getMinRating()).append("★+");
        }
        
        if (currentFilter.getMaxDistance() < 10) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("within ").append((int)currentFilter.getMaxDistance()).append("km");
        }
        
        if (currentFilter.isOnlyOpenVans()) {
            if (summary.length() > 0) summary.append(", ");
            summary.append("open now");
        }
        
        return summary.toString();
    }
}
