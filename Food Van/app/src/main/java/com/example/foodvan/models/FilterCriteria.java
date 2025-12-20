package com.example.foodvan.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Filter criteria model for food van filtering
 */
public class FilterCriteria implements Serializable {
    
    // Cuisine filters
    private List<CuisineType> selectedCuisines;
    
    // Price range filter
    private PriceRange priceRange;
    private float minPrice;
    private float maxPrice;
    
    // Rating filter
    private float minRating;
    
    // Distance filter
    private float maxDistance; // in kilometers
    
    // Availability filter
    private boolean onlyOpenVans;
    
    // Service type filter
    private ServiceType serviceType;
    
    // Sorting options
    private SortBy sortBy;
    private SortOrder sortOrder;
    
    public FilterCriteria() {
        // Initialize with default values
        this.selectedCuisines = new ArrayList<>();
        this.priceRange = PriceRange.ALL;
        this.minPrice = 0f;
        this.maxPrice = 1000f;
        this.minRating = 0f;
        this.maxDistance = 10f; // 10km default
        this.onlyOpenVans = false;
        this.serviceType = ServiceType.ALL;
        this.sortBy = SortBy.DISTANCE;
        this.sortOrder = SortOrder.ASCENDING;
    }
    
    // Getters and Setters
    public List<CuisineType> getSelectedCuisines() {
        return selectedCuisines;
    }
    
    public void setSelectedCuisines(List<CuisineType> selectedCuisines) {
        this.selectedCuisines = selectedCuisines;
    }
    
    public PriceRange getPriceRange() {
        return priceRange;
    }
    
    public void setPriceRange(PriceRange priceRange) {
        this.priceRange = priceRange;
    }
    
    public float getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(float minPrice) {
        this.minPrice = minPrice;
    }
    
    public float getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(float maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public float getMinRating() {
        return minRating;
    }
    
    public void setMinRating(float minRating) {
        this.minRating = minRating;
    }
    
    public float getMaxDistance() {
        return maxDistance;
    }
    
    public void setMaxDistance(float maxDistance) {
        this.maxDistance = maxDistance;
    }
    
    public boolean isOnlyOpenVans() {
        return onlyOpenVans;
    }
    
    public void setOnlyOpenVans(boolean onlyOpenVans) {
        this.onlyOpenVans = onlyOpenVans;
    }
    
    public ServiceType getServiceType() {
        return serviceType;
    }
    
    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
    
    public SortBy getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }
    
    public SortOrder getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    // Helper methods
    public boolean hasActiveFilters() {
        return !selectedCuisines.isEmpty() ||
               priceRange != PriceRange.ALL ||
               minRating > 0 ||
               maxDistance < 10 ||
               onlyOpenVans ||
               serviceType != ServiceType.ALL;
    }
    
    public void reset() {
        selectedCuisines.clear();
        priceRange = PriceRange.ALL;
        minPrice = 0f;
        maxPrice = 1000f;
        minRating = 0f;
        maxDistance = 10f;
        onlyOpenVans = false;
        serviceType = ServiceType.ALL;
        sortBy = SortBy.DISTANCE;
        sortOrder = SortOrder.ASCENDING;
    }
    
    public FilterCriteria copy() {
        FilterCriteria copy = new FilterCriteria();
        copy.selectedCuisines = new ArrayList<>(this.selectedCuisines);
        copy.priceRange = this.priceRange;
        copy.minPrice = this.minPrice;
        copy.maxPrice = this.maxPrice;
        copy.minRating = this.minRating;
        copy.maxDistance = this.maxDistance;
        copy.onlyOpenVans = this.onlyOpenVans;
        copy.serviceType = this.serviceType;
        copy.sortBy = this.sortBy;
        copy.sortOrder = this.sortOrder;
        return copy;
    }
}
