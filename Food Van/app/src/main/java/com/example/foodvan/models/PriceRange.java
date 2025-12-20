package com.example.foodvan.models;

/**
 * Enum for price range categories
 */
public enum PriceRange {
    ALL("All Prices", "💰", 0f, 1000f),
    LOW("Budget Friendly", "💵", 0f, 150f),
    MEDIUM("Moderate", "💴", 150f, 300f),
    HIGH("Premium", "💎", 300f, 1000f);
    
    private final String displayName;
    private final String emoji;
    private final float minPrice;
    private final float maxPrice;
    
    PriceRange(String displayName, String emoji, float minPrice, float maxPrice) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public float getMinPrice() {
        return minPrice;
    }
    
    public float getMaxPrice() {
        return maxPrice;
    }
    
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    public String getPriceRangeText() {
        if (this == ALL) {
            return "All Prices";
        }
        return "₹" + (int)minPrice + " - ₹" + (int)maxPrice;
    }
    
    public String getFullDisplayText() {
        return getDisplayNameWithEmoji() + " (" + getPriceRangeText() + ")";
    }
    
    // Get all price ranges as array
    public static PriceRange[] getAllRanges() {
        return values();
    }
    
    // Get display names for UI
    public static String[] getDisplayNames() {
        PriceRange[] ranges = values();
        String[] names = new String[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            names[i] = ranges[i].getDisplayName();
        }
        return names;
    }
    
    // Get full display text for UI
    public static String[] getFullDisplayTexts() {
        PriceRange[] ranges = values();
        String[] texts = new String[ranges.length];
        for (int i = 0; i < ranges.length; i++) {
            texts[i] = ranges[i].getFullDisplayText();
        }
        return texts;
    }
    
    // Find price range by display name
    public static PriceRange fromDisplayName(String displayName) {
        for (PriceRange range : values()) {
            if (range.getDisplayName().equals(displayName)) {
                return range;
            }
        }
        return ALL; // Default fallback
    }
    
    // Get price range for a given price
    public static PriceRange getPriceRangeForPrice(float price) {
        if (price <= LOW.maxPrice) return LOW;
        if (price <= MEDIUM.maxPrice) return MEDIUM;
        return HIGH;
    }
}
