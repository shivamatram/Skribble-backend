package com.example.foodvan.models;

/**
 * Enum for service types offered by food vans
 */
public enum ServiceType {
    ALL("All Services", "🚚", "Show all food vans"),
    DELIVERY("Delivery", "🚚", "Vans that deliver to your location"),
    PICKUP("Pickup", "🏃", "Vans where you can pickup orders"),
    DINE_IN("Dine In", "🪑", "Vans with seating arrangements");
    
    private final String displayName;
    private final String emoji;
    private final String description;
    
    ServiceType(String displayName, String emoji, String description) {
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
    
    // Get all service types as array
    public static ServiceType[] getAllTypes() {
        return values();
    }
    
    // Get display names for UI
    public static String[] getDisplayNames() {
        ServiceType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getDisplayName();
        }
        return names;
    }
    
    // Get display names with emojis for UI
    public static String[] getDisplayNamesWithEmojis() {
        ServiceType[] types = values();
        String[] names = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            names[i] = types[i].getDisplayNameWithEmoji();
        }
        return names;
    }
    
    // Find service type by display name
    public static ServiceType fromDisplayName(String displayName) {
        for (ServiceType type : values()) {
            if (type.getDisplayName().equals(displayName)) {
                return type;
            }
        }
        return ALL; // Default fallback
    }
}
