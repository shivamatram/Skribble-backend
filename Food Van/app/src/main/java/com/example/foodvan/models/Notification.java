package com.example.foodvan.models;

import java.util.Date;

/**
 * Notification model class for vendor notifications
 * Represents different types of notifications (orders, payments, system alerts)
 */
public class Notification {
    
    // Notification types
    public static final String TYPE_ORDER = "order";
    public static final String TYPE_PAYMENT = "payment";
    public static final String TYPE_SYSTEM = "system";
    public static final String TYPE_MENU = "menu";
    public static final String TYPE_PROFILE = "profile";
    
    private String id;
    private String title;
    private String message;
    private String type;
    private Date timestamp;
    private boolean isRead;
    private String orderId; // For order-related notifications
    private String imageUrl; // Optional notification image
    private String actionUrl; // Deep link or action
    
    // Default constructor for Firebase
    public Notification() {}
    
    // Full constructor
    public Notification(String id, String title, String message, String type, 
                       Date timestamp, boolean isRead, String orderId, 
                       String imageUrl, String actionUrl) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.orderId = orderId;
        this.imageUrl = imageUrl;
        this.actionUrl = actionUrl;
    }
    
    // Simplified constructor for common use
    public Notification(String id, String title, String message, String type, Date timestamp) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.isRead = false;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isRead() {
        return isRead;
    }
    
    public void setRead(boolean read) {
        isRead = read;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    /**
     * Get icon resource based on notification type
     */
    public int getIconResource() {
        switch (type) {
            case TYPE_ORDER:
                return android.R.drawable.ic_menu_agenda;
            case TYPE_PAYMENT:
                return android.R.drawable.ic_dialog_info;
            case TYPE_SYSTEM:
                return android.R.drawable.ic_dialog_alert;
            case TYPE_MENU:
                return android.R.drawable.ic_menu_edit;
            case TYPE_PROFILE:
                return android.R.drawable.ic_menu_manage;
            default:
                return android.R.drawable.ic_dialog_info;
        }
    }
    
    /**
     * Get formatted time string for display
     */
    public String getFormattedTime() {
        if (timestamp == null) return "";
        
        long now = System.currentTimeMillis();
        long notificationTime = timestamp.getTime();
        long diff = now - notificationTime;
        
        // Convert to minutes, hours, days
        long minutes = diff / (1000 * 60);
        long hours = diff / (1000 * 60 * 60);
        long days = diff / (1000 * 60 * 60 * 24);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " min" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else {
            return new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault()).format(timestamp);
        }
    }
    
    @Override
    public String toString() {
        return "Notification{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                '}';
    }
}
