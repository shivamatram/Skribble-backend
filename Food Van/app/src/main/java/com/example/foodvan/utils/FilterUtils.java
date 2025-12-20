package com.example.foodvan.utils;

import com.example.foodvan.models.MenuFilter;
import com.example.foodvan.models.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for applying filters and sorting to menu items
 */
public class FilterUtils {
    
    /**
     * Apply the given filter to a list of menu items
     * @param items Original list of menu items
     * @param filter Filter criteria to apply
     * @return Filtered and sorted list of menu items
     */
    public static List<MenuItem> applyFilter(List<MenuItem> items, MenuFilter filter) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<MenuItem> filtered = new ArrayList<>();
        
        // Apply filters
        for (MenuItem item : items) {
            if (matchesFilter(item, filter)) {
                filtered.add(item);
            }
        }
        
        // Apply sorting
        applySorting(filtered, filter.getSortBy());
        
        return filtered;
    }
    
    /**
     * Check if a menu item matches the filter criteria
     */
    private static boolean matchesFilter(MenuItem item, MenuFilter filter) {
        // Search query filter
        if (!filter.getSearchQuery().isEmpty()) {
            String query = filter.getSearchQuery().toLowerCase().trim();
            String itemName = item.getName() != null ? item.getName().toLowerCase() : "";
            String itemDescription = item.getDescription() != null ? item.getDescription().toLowerCase() : "";
            String itemCategory = item.getCategory() != null ? item.getCategory().toLowerCase() : "";
            
            if (!itemName.contains(query) && 
                !itemDescription.contains(query) && 
                !itemCategory.contains(query)) {
                return false;
            }
        }
        
        // Category filter
        if (!filter.isAllCategoriesSelected() && !filter.getSelectedCategories().isEmpty()) {
            String itemCategory = item.getCategory();
            if (itemCategory == null || !filter.getSelectedCategories().contains(itemCategory)) {
                return false;
            }
        }
        
        // Availability filter
        boolean isAvailable = item.isAvailable();
        if (isAvailable && !filter.isShowAvailable()) {
            return false;
        }
        if (!isAvailable && !filter.isShowOutOfStock()) {
            return false;
        }
        
        // Price range filter
        double itemPrice = item.getPrice();
        if (itemPrice < filter.getMinPrice() || itemPrice > filter.getMaxPrice()) {
            return false;
        }
        
        // Vegetarian filter
        if (filter.isVegetarianOnly() && !item.isVegetarian()) {
            return false;
        }
        
        // Non-vegetarian filter
        if (filter.isNonVegetarianOnly() && item.isVegetarian()) {
            return false;
        }
        
        // Special offers filter (using discount > 0 as special offer indicator)
        if (filter.isSpecialOffersOnly() && item.getDiscount() <= 0) {
            return false;
        }
        
        // Stock threshold filter - skip for now as MenuItem doesn't have stock quantity
        // This can be implemented when stock management is added to MenuItem model
        
        // Tags filter - skip for now as MenuItem doesn't have tags
        // This can be implemented when tags are added to MenuItem model
        
        return true;
    }
    
    /**
     * Apply sorting to the filtered list
     */
    private static void applySorting(List<MenuItem> items, MenuFilter.SortOption sortBy) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
        Comparator<MenuItem> comparator;
        
        switch (sortBy) {
            case POPULARITY:
                comparator = (a, b) -> {
                    // Sort by order count (higher first), then by rating
                    int orderCompare = Integer.compare(b.getOrderCount(), a.getOrderCount());
                    if (orderCompare != 0) return orderCompare;
                    return Double.compare(b.getRating(), a.getRating());
                };
                break;
                
            case PRICE_LOW_TO_HIGH:
                comparator = (a, b) -> Double.compare(a.getPrice(), b.getPrice());
                break;
                
            case PRICE_HIGH_TO_LOW:
                comparator = (a, b) -> Double.compare(b.getPrice(), a.getPrice());
                break;
                
            case NAME_A_TO_Z:
                comparator = (a, b) -> {
                    String nameA = a.getName() != null ? a.getName() : "";
                    String nameB = b.getName() != null ? b.getName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                };
                break;
                
            case NAME_Z_TO_A:
                comparator = (a, b) -> {
                    String nameA = a.getName() != null ? a.getName() : "";
                    String nameB = b.getName() != null ? b.getName() : "";
                    return nameB.compareToIgnoreCase(nameA);
                };
                break;
                
            case NEWEST:
            default:
                comparator = (a, b) -> {
                    // Sort by creation date (newer first), then by last updated date
                    long createdA = a.getCreatedAt();
                    long createdB = b.getCreatedAt();
                    int createdCompare = Long.compare(createdB, createdA);
                    if (createdCompare != 0) return createdCompare;
                    
                    long updatedA = a.getLastUpdated();
                    long updatedB = b.getLastUpdated();
                    return Long.compare(updatedB, updatedA);
                };
                break;
        }
        
        Collections.sort(items, comparator);
    }
    
    /**
     * Get available categories from a list of menu items
     */
    public static List<String> getAvailableCategories(List<MenuItem> items) {
        List<String> categories = new ArrayList<>();
        
        if (items != null) {
            for (MenuItem item : items) {
                String category = item.getCategory();
                if (category != null && !category.isEmpty() && !categories.contains(category)) {
                    categories.add(category);
                }
            }
        }
        
        Collections.sort(categories);
        return categories;
    }
    
    /**
     * Get available tags from a list of menu items
     * Note: Currently MenuItem doesn't have tags, this is for future implementation
     */
    public static List<String> getAvailableTags(List<MenuItem> items) {
        List<String> allTags = new ArrayList<>();
        
        // TODO: Implement when MenuItem model includes tags
        // For now, return common food tags based on item properties
        if (items != null) {
            for (MenuItem item : items) {
                if (item.isVegetarian() && !allTags.contains("Vegetarian")) {
                    allTags.add("Vegetarian");
                }
                if (item.isVegan() && !allTags.contains("Vegan")) {
                    allTags.add("Vegan");
                }
                if (item.isGlutenFree() && !allTags.contains("Gluten Free")) {
                    allTags.add("Gluten Free");
                }
                if (item.isSpicy() && !allTags.contains("Spicy")) {
                    allTags.add("Spicy");
                }
                if (item.isBestSeller() && !allTags.contains("Best Seller")) {
                    allTags.add("Best Seller");
                }
                if (item.isNew() && !allTags.contains("New")) {
                    allTags.add("New");
                }
            }
        }
        
        Collections.sort(allTags);
        return allTags;
    }
    
    /**
     * Get price range from a list of menu items
     */
    public static float[] getPriceRange(List<MenuItem> items) {
        if (items == null || items.isEmpty()) {
            return new float[]{0f, 1000f};
        }
        
        float minPrice = Float.MAX_VALUE;
        float maxPrice = Float.MIN_VALUE;
        
        for (MenuItem item : items) {
            float price = (float) item.getPrice();
            if (price < minPrice) minPrice = price;
            if (price > maxPrice) maxPrice = price;
        }
        
        // Add some padding to the range
        minPrice = Math.max(0, minPrice - 10);
        maxPrice = maxPrice + 10;
        
        return new float[]{minPrice, maxPrice};
    }
    
    /**
     * Count items by availability status
     */
    public static int[] getAvailabilityCounts(List<MenuItem> items) {
        int availableCount = 0;
        int outOfStockCount = 0;
        
        if (items != null) {
            for (MenuItem item : items) {
                if (item.isAvailable()) {
                    availableCount++;
                } else {
                    outOfStockCount++;
                }
            }
        }
        
        return new int[]{availableCount, outOfStockCount};
    }
    
    /**
     * Generate filter summary text
     */
    public static String generateFilterSummary(MenuFilter filter, int totalItems, int filteredItems) {
        if (!filter.hasActiveFilters()) {
            return String.format("Showing all %d items", totalItems);
        }
        
        StringBuilder summary = new StringBuilder();
        summary.append(String.format("Showing %d of %d items", filteredItems, totalItems));
        
        List<String> activeFilters = new ArrayList<>();
        
        if (!filter.getSearchQuery().isEmpty()) {
            activeFilters.add("search");
        }
        
        if (!filter.isAllCategoriesSelected()) {
            activeFilters.add("category");
        }
        
        if (!filter.isShowAvailable() || !filter.isShowOutOfStock()) {
            activeFilters.add("availability");
        }
        
        if (filter.getMinPrice() > 0 || filter.getMaxPrice() < 1000) {
            activeFilters.add("price");
        }
        
        if (filter.getSortBy() != MenuFilter.SortOption.NEWEST) {
            activeFilters.add("sorted");
        }
        
        if (!activeFilters.isEmpty()) {
            summary.append(" (");
            for (int i = 0; i < activeFilters.size(); i++) {
                if (i > 0) summary.append(", ");
                summary.append(activeFilters.get(i));
            }
            summary.append(")");
        }
        
        return summary.toString();
    }
}
