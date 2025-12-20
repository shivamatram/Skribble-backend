package com.example.foodvan.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * SupportTicket model class representing a customer support ticket
 */
public class SupportTicket implements Serializable {
    
    // Status constants
    public static final String STATUS_OPEN = "Open";
    public static final String STATUS_IN_PROGRESS = "In Progress";
    public static final String STATUS_RESOLVED = "Resolved";
    public static final String STATUS_CLOSED = "Closed";
    
    // Category constants
    public static final String CATEGORY_PAYMENT = "Payment Issue";
    public static final String CATEGORY_LATE_DELIVERY = "Late Delivery";
    public static final String CATEGORY_WRONG_ORDER = "Wrong Order";
    public static final String CATEGORY_APP_BUG = "App Bug";
    public static final String CATEGORY_OTHER = "Other";
    
    // Priority constants
    public static final String PRIORITY_LOW = "Low";
    public static final String PRIORITY_MEDIUM = "Medium";
    public static final String PRIORITY_HIGH = "High";
    public static final String PRIORITY_URGENT = "Urgent";
    
    private String ticketId;
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String orderId;
    private String vendorId;
    private String vendorName;
    private String category;
    private String description;
    private String status;
    private String priority;
    private String source;
    private String attachmentUrl;
    private String response;
    private long createdAt;
    private long updatedAt;
    private long resolvedAt;

    public SupportTicket() {
        // Default constructor required for Firebase
        this.status = STATUS_OPEN;
        this.priority = PRIORITY_MEDIUM;
        this.source = "customer-app";
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public SupportTicket(String customerId, String category, String description) {
        this();
        this.customerId = customerId;
        this.category = category;
        this.description = description;
    }

    // Getters and Setters
    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(long resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    // Utility methods
    public String getFormattedCreatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(createdAt));
    }

    public String getFormattedUpdatedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(updatedAt));
    }

    public String getShortTicketId() {
        if (ticketId != null && ticketId.length() > 8) {
            return "#" + ticketId.substring(0, 8).toUpperCase();
        }
        return "#" + (ticketId != null ? ticketId.toUpperCase() : "N/A");
    }

    public boolean isOpen() {
        return STATUS_OPEN.equals(status);
    }

    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equals(status);
    }

    public boolean isResolved() {
        return STATUS_RESOLVED.equals(status);
    }

    public boolean isClosed() {
        return STATUS_CLOSED.equals(status);
    }

    public boolean hasOrderContext() {
        return orderId != null && !orderId.isEmpty();
    }

    public static String[] getAllCategories() {
        return new String[]{
            CATEGORY_PAYMENT,
            CATEGORY_LATE_DELIVERY,
            CATEGORY_WRONG_ORDER,
            CATEGORY_APP_BUG,
            CATEGORY_OTHER
        };
    }
}
