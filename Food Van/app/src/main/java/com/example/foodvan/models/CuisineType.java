package com.example.foodvan.models;

/**
 * Enum for different cuisine types available in food vans
 */
public enum CuisineType {
    FAST_FOOD("Fast Food", "🍔", "#FF6B35"),
    INDIAN("Indian", "🍛", "#FF8C42"),
    CHINESE("Chinese", "🥡", "#FF6B6B"),
    ITALIAN("Italian", "🍕", "#4ECDC4"),
    MEXICAN("Mexican", "🌮", "#45B7D1"),
    DESSERTS("Desserts", "🍰", "#96CEB4"),
    BEVERAGES("Beverages", "🥤", "#FFEAA7"),
    STREET_FOOD("Street Food", "🌭", "#DDA0DD"),
    HEALTHY("Healthy", "🥗", "#98D8C8"),
    SNACKS("Snacks", "🍿", "#F7DC6F"),
    SEAFOOD("Seafood", "🦐", "#85C1E9"),
    VEGETARIAN("Vegetarian", "🥬", "#82E0AA"),
    VEGAN("Vegan", "🌱", "#A9DFBF"),
    BAKERY("Bakery", "🥖", "#F8C471"),
    COFFEE("Coffee", "☕", "#D7BDE2");
    
    private final String displayName;
    private final String emoji;
    private final String colorCode;
    
    CuisineType(String displayName, String emoji, String colorCode) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.colorCode = colorCode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    // Get all cuisine types as array for spinners/adapters
    public static CuisineType[] getAllTypes() {
        return values();
    }
    
    // Get display names for UI
    public static String[] getDisplayNames() {
        CuisineType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getDisplayName();
        }
        return names;
    }
    
    // Get display names with emojis for UI
    public static String[] getDisplayNamesWithEmojis() {
        CuisineType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getDisplayNameWithEmoji();
        }
        return names;
    }
    
    // Find cuisine type by display name
    public static CuisineType fromDisplayName(String displayName) {
        for (CuisineType type : values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        return FAST_FOOD; // Default fallback
    }
}
