package com.example.foodvan.models;

import java.io.Serializable;

public class ChatMessage implements Serializable {

    public enum MessageType {
        SENT, RECEIVED, SYSTEM
    }

    private String messageId;
    private String text;
    private String senderId;
    private String receiverId;
    private long timestamp;
    private MessageType messageType;
    private boolean isRead;
    private String attachmentUrl;
    private String attachmentType;

    public ChatMessage() {
        // Default constructor for Firebase
    }

    public ChatMessage(String text, String senderId, String receiverId, MessageType messageType) {
        this.text = text;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
        this.isRead = false;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl) {
        this.attachmentUrl = attachmentUrl;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    // Utility methods
    public boolean hasAttachment() {
        return attachmentUrl != null && !attachmentUrl.isEmpty();
    }

    public String getFormattedTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
    
    // Additional methods for ChatMessageAdapter
    public String getMessage() {
        return text;
    }
    
    public void setMessage(String message) {
        this.text = message;
    }
    
    public boolean isSentByCurrentUser() {
        return messageType == MessageType.SENT;
    }
    
    public String getSenderName() {
        // This could be enhanced to fetch actual sender name from database
        return isSentByCurrentUser() ? "You" : "Vendor";
    }
}
