package com.example.foodvan.models;

/**
 * Enum for sorting options
 */
public enum SortBy {
    DISTANCE("Distance", "📍", "Sort by proximity to your location"),
    RATING("Rating", "⭐", "Sort by customer ratings"),
    PRICE("Price", "💰", "Sort by average price"),
    POPULARITY("Popularity", "🔥", "Sort by number of orders"),
    NEWEST("Newest", "🆕", "Sort by recently added vans"),
    NAME("Name", "🔤", "Sort alphabetically by van name");
    
    private final String displayName;
    private final String emoji;
    private final String description;
    
    SortBy(String displayName, String emoji, String description) {
        this.displayName = displayName;
        this.emoji = emoji;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDisplayNameWithEmoji() {
        return emoji + " " + displayName;
    }
    
    // Get all sort options as array
    public static SortBy[] getAllOptions() {
        return values();
    }
    
    // Get display names for UI
    public static String[] getDisplayNames() {
        SortBy[] options = values();
        String[] names = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            names[i] = options[i].getDisplayName();
        }
        return names;
    }
    
    // Get display names with emojis for UI
    public static String[] getDisplayNamesWithEmojis() {
        SortBy[] options = values();
        String[] names = new String[options.length];
        for (int i = 0; i < options.length; i++) {
            names[i] = options[i].getDisplayNameWithEmoji();
        }
        return names;
    }
    
    // Find sort option by display name
    public static SortBy fromDisplayName(String displayName) {
        for (SortBy option : values()) {
            if (option.getDisplayName().equals(displayName)) {
                return option;
            }
        }
        return DISTANCE; // Default fallback
    }
}
