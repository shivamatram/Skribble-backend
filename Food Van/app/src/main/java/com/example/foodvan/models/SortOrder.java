package com.example.foodvan.models;

/**
 * Enum for sort order
 */
public enum SortOrder {
    ASCENDING("Low to High", "⬆️"),
    DESCENDING("High to Low", "⬇️");
    
    private final String displayName;
    private final String emoji;
    
    SortOrder(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    // Get all sort orders as array
    public static SortOrder[] getAllOrders() {
        return values();
    }
    
    // Get display names for UI
    public static String[] getDisplayNames() {
        SortOrder[] orders = values();
        String[] names = new String[orders.length];
        for (int i = 0; i < orders.length; i++) {
            names[i] = orders[i].getDisplayName();
        }
        return names;
    }
    
    // Find sort order by display name
    public static SortOrder fromDisplayName(String displayName) {
        for (SortOrder order : values()) {
            if (order.getDisplayName().equals(displayName)) {
                return order;
            }
        }
        return ASCENDING; // Default fallback
    }
}
