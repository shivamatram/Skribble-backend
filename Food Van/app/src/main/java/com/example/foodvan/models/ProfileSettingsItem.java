package com.example.foodvan.models;

public class ProfileSettingsItem {
    
    // Item Types
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_TOGGLE = 2;
    public static final int TYPE_DANGER = 3;
    
    private int type;
    private String title;
    private String subtitle;
    private int iconResId;
    private boolean isToggleEnabled;
    private boolean hasChevron;
    
    public ProfileSettingsItem(int type, String title, String subtitle, int iconResId) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
        this.hasChevron = (type == TYPE_ITEM || type == TYPE_DANGER);
        this.isToggleEnabled = false;
    }
    
    public ProfileSettingsItem(int type, String title, String subtitle, int iconResId, boolean isToggleEnabled) {
        this.type = type;
        this.title = title;
        this.subtitle = subtitle;
        this.iconResId = iconResId;
        this.isToggleEnabled = isToggleEnabled;
        this.hasChevron = false;
    }
    
    // Getters and Setters
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSubtitle() {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }
    
    public int getIconResId() {
        return iconResId;
    }
    
    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }
    
    public boolean isToggleEnabled() {
        return isToggleEnabled;
    }
    
    public void setToggleEnabled(boolean toggleEnabled) {
        isToggleEnabled = toggleEnabled;
    }
    
    public boolean hasChevron() {
        return hasChevron;
    }
    
    public void setHasChevron(boolean hasChevron) {
        this.hasChevron = hasChevron;
    }
}
