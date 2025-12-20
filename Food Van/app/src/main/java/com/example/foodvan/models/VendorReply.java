package com.example.foodvan.models;

import java.io.Serializable;

/**
 * VendorReply model class for vendor responses to reviews
 */
public class VendorReply implements Serializable {
    private String replyId;
    private String vendorId;
    private String vendorName;
    private String text;
    private long createdAt;
    private long editedAt;
    private boolean isEdited;
    private String status; // "ACTIVE", "DELETED"

    public VendorReply() {
        // Default constructor required for Firebase
    }

    public VendorReply(String vendorId, String vendorName, String text) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
        this.editedAt = 0;
        this.isEdited = false;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
        this.editedAt = System.currentTimeMillis();
        this.isEdited = true;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getEditedAt() {
        return editedAt;
    }

    public void setEditedAt(long editedAt) {
        this.editedAt = editedAt;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Utility methods
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isEmpty() {
        return text == null || text.trim().isEmpty();
    }

    public String getDisplayText() {
        return text != null ? text.trim() : "";
    }

    public void markAsEdited() {
        this.editedAt = System.currentTimeMillis();
        this.isEdited = true;
    }

    public void delete() {
        this.status = "DELETED";
        this.editedAt = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return "VendorReply{" +
                "replyId='" + replyId + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", text='" + text + '\'' +
                ", createdAt=" + createdAt +
                ", isEdited=" + isEdited +
                '}';
    }
}
