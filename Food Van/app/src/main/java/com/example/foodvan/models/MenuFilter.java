package com.example.foodvan.models;

import java.util.List;
import java.util.ArrayList;

/**
 * Model class representing filter criteria for menu items
 */
public class MenuFilter {
    
    // Search
    private String searchQuery = "";
    
    // Categories
    private List<String> selectedCategories = new ArrayList<>();
    private boolean allCategoriesSelected = true;
    
    // Availability
    private boolean showAvailable = true;
    private boolean showOutOfStock = true;
    
    // Price Range
    private float minPrice = 0f;
    private float maxPrice = 1000f;
    
    // Sort Options
    private SortOption sortBy = SortOption.NEWEST;
    
    // Advanced Filters
    private List<String> selectedTags = new ArrayList<>();
    private boolean vegetarianOnly = false;
    private boolean nonVegetarianOnly = false;
    private boolean specialOffersOnly = false;
    private int stockThreshold = -1; // -1 means no threshold
    
    // Filter Preset Info
    private String presetId;
    private String presetName;
    private long createdAt;
    
    public enum SortOption {
        NEWEST("newest"),
        POPULARITY("popularity"),
        PRICE_LOW_TO_HIGH("price_asc"),
        PRICE_HIGH_TO_LOW("price_desc"),
        NAME_A_TO_Z("name_asc"),
        NAME_Z_TO_A("name_desc");
        
        private final String value;
        
        SortOption(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static SortOption fromValue(String value) {
            for (SortOption option : values()) {
                if (option.value.equals(value)) {
                    return option;
                }
            }
            return NEWEST;
        }
    }
    
    // Constructors
    public MenuFilter() {}
    
    public MenuFilter(MenuFilter other) {
        this.searchQuery = other.searchQuery;
        this.selectedCategories = new ArrayList<>(other.selectedCategories);
        this.allCategoriesSelected = other.allCategoriesSelected;
        this.showAvailable = other.showAvailable;
        this.showOutOfStock = other.showOutOfStock;
        this.minPrice = other.minPrice;
        this.maxPrice = other.maxPrice;
        this.sortBy = other.sortBy;
        this.selectedTags = new ArrayList<>(other.selectedTags);
        this.vegetarianOnly = other.vegetarianOnly;
        this.nonVegetarianOnly = other.nonVegetarianOnly;
        this.specialOffersOnly = other.specialOffersOnly;
        this.stockThreshold = other.stockThreshold;
        this.presetId = other.presetId;
        this.presetName = other.presetName;
        this.createdAt = other.createdAt;
    }
    
    // Reset to default values
    public void reset() {
        searchQuery = "";
        selectedCategories.clear();
        allCategoriesSelected = true;
        showAvailable = true;
        showOutOfStock = true;
        minPrice = 0f;
        maxPrice = 1000f;
        sortBy = SortOption.NEWEST;
        selectedTags.clear();
        vegetarianOnly = false;
        nonVegetarianOnly = false;
        specialOffersOnly = false;
        stockThreshold = -1;
        presetId = null;
        presetName = null;
        createdAt = 0;
    }
    
    // Check if filter has any active criteria
    public boolean hasActiveFilters() {
        return !searchQuery.isEmpty() ||
               !allCategoriesSelected ||
               (!showAvailable || !showOutOfStock) ||
               minPrice > 0 ||
               maxPrice < 1000 ||
               sortBy != SortOption.NEWEST ||
               !selectedTags.isEmpty() ||
               vegetarianOnly ||
               nonVegetarianOnly ||
               specialOffersOnly ||
               stockThreshold > -1;
    }
    
    // Getters and Setters
    public String getSearchQuery() { return searchQuery; }
    public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery != null ? searchQuery : ""; }
    
    public List<String> getSelectedCategories() { return selectedCategories; }
    public void setSelectedCategories(List<String> selectedCategories) { 
        this.selectedCategories = selectedCategories != null ? selectedCategories : new ArrayList<>(); 
    }
    
    public boolean isAllCategoriesSelected() { return allCategoriesSelected; }
    public void setAllCategoriesSelected(boolean allCategoriesSelected) { this.allCategoriesSelected = allCategoriesSelected; }
    
    public boolean isShowAvailable() { return showAvailable; }
    public void setShowAvailable(boolean showAvailable) { this.showAvailable = showAvailable; }
    
    public boolean isShowOutOfStock() { return showOutOfStock; }
    public void setShowOutOfStock(boolean showOutOfStock) { this.showOutOfStock = showOutOfStock; }
    
    public float getMinPrice() { return minPrice; }
    public void setMinPrice(float minPrice) { this.minPrice = Math.max(0, minPrice); }
    
    public float getMaxPrice() { return maxPrice; }
    public void setMaxPrice(float maxPrice) { this.maxPrice = Math.max(0, maxPrice); }
    
    public SortOption getSortBy() { return sortBy; }
    public void setSortBy(SortOption sortBy) { this.sortBy = sortBy != null ? sortBy : SortOption.NEWEST; }
    
    public List<String> getSelectedTags() { return selectedTags; }
    public void setSelectedTags(List<String> selectedTags) { 
        this.selectedTags = selectedTags != null ? selectedTags : new ArrayList<>(); 
    }
    
    public boolean isVegetarianOnly() { return vegetarianOnly; }
    public void setVegetarianOnly(boolean vegetarianOnly) { this.vegetarianOnly = vegetarianOnly; }
    
    public boolean isNonVegetarianOnly() { return nonVegetarianOnly; }
    public void setNonVegetarianOnly(boolean nonVegetarianOnly) { this.nonVegetarianOnly = nonVegetarianOnly; }
    
    public boolean isSpecialOffersOnly() { return specialOffersOnly; }
    public void setSpecialOffersOnly(boolean specialOffersOnly) { this.specialOffersOnly = specialOffersOnly; }
    
    public int getStockThreshold() { return stockThreshold; }
    public void setStockThreshold(int stockThreshold) { this.stockThreshold = stockThreshold; }
    
    public String getPresetId() { return presetId; }
    public void setPresetId(String presetId) { this.presetId = presetId; }
    
    public String getPresetName() { return presetName; }
    public void setPresetName(String presetName) { this.presetName = presetName; }
    
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    
    // Validation methods
    public boolean isValidPriceRange() {
        return minPrice >= 0 && maxPrice >= 0 && minPrice <= maxPrice;
    }
    
    public void fixPriceRange() {
        if (minPrice < 0) minPrice = 0;
        if (maxPrice < 0) maxPrice = 1000;
        if (minPrice > maxPrice) {
            float temp = minPrice;
            minPrice = maxPrice;
            maxPrice = temp;
        }
    }
}
