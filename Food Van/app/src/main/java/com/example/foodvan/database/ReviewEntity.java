package com.example.foodvan.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.example.foodvan.models.Review;
import com.example.foodvan.models.VendorReply;

import java.util.List;

/**
 * Room entity for caching reviews locally
 */
@Entity(tableName = "reviews")
@TypeConverters({Converters.class})
public class ReviewEntity {
    @PrimaryKey
    @NonNull
    private String reviewId;
    
    private String customerId;
    private String customerName;
    private String vendorId;
    private String orderId;
    private float rating;
    private String text;
    private long createdAt;
    private long editedAt;
    private String vendorReplyText;
    private long vendorReplyCreatedAt;
    private long vendorReplyEditedAt;
    private boolean vendorReplyIsEdited;
    private boolean flagged;
    private String flagReason;
    private long flaggedAt;
    private boolean visible;
    private String status;
    private boolean isAnonymous;
    private boolean isVerifiedPurchase;
    private List<String> imageUrls;
    private long lastSyncedAt;

    public ReviewEntity() {
        // Default constructor required for Room
    }

    // Constructor from Review model
    public static ReviewEntity fromReview(Review review) {
        ReviewEntity entity = new ReviewEntity();
        entity.reviewId = review.getReviewId();
        entity.customerId = review.getUserId();
        entity.customerName = review.getUserName();
        entity.vendorId = review.getVendorId();
        entity.orderId = review.getItemId(); // Using itemId as orderId for now
        entity.rating = review.getRating();
        entity.text = review.getReviewText();
        entity.createdAt = review.getTimestamp();
        entity.editedAt = 0;
        entity.vendorReplyText = review.getVendorReply();
        entity.vendorReplyCreatedAt = review.getVendorReplyTimestamp();
        entity.vendorReplyEditedAt = 0;
        entity.vendorReplyIsEdited = false;
        entity.flagged = false;
        entity.flagReason = null;
        entity.flaggedAt = 0;
        entity.visible = "ACTIVE".equals(review.getStatus());
        entity.status = review.getStatus();
        entity.isAnonymous = review.isAnonymous();
        entity.isVerifiedPurchase = review.isVerifiedPurchase();
        entity.imageUrls = review.getImageUrls();
        entity.lastSyncedAt = System.currentTimeMillis();
        return entity;
    }

    // Convert to Review model
    public Review toReview() {
        Review review = new Review();
        review.setReviewId(reviewId);
        review.setUserId(customerId);
        review.setUserName(customerName);
        review.setVendorId(vendorId);
        review.setItemId(orderId);
        review.setRating(rating);
        review.setReviewText(text);
        review.setTimestamp(createdAt);
        review.setVendorReply(vendorReplyText);
        review.setVendorReplyTimestamp(vendorReplyCreatedAt);
        review.setStatus(status);
        review.setAnonymous(isAnonymous);
        review.setVerifiedPurchase(isVerifiedPurchase);
        review.setImageUrls(imageUrls);
        return review;
    }

    // Getters and Setters
    @NonNull
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(@NonNull String reviewId) {
        this.reviewId = reviewId;
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

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public String getVendorReplyText() {
        return vendorReplyText;
    }

    public void setVendorReplyText(String vendorReplyText) {
        this.vendorReplyText = vendorReplyText;
    }

    public long getVendorReplyCreatedAt() {
        return vendorReplyCreatedAt;
    }

    public void setVendorReplyCreatedAt(long vendorReplyCreatedAt) {
        this.vendorReplyCreatedAt = vendorReplyCreatedAt;
    }

    public long getVendorReplyEditedAt() {
        return vendorReplyEditedAt;
    }

    public void setVendorReplyEditedAt(long vendorReplyEditedAt) {
        this.vendorReplyEditedAt = vendorReplyEditedAt;
    }

    public boolean isVendorReplyIsEdited() {
        return vendorReplyIsEdited;
    }

    public void setVendorReplyIsEdited(boolean vendorReplyIsEdited) {
        this.vendorReplyIsEdited = vendorReplyIsEdited;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }

    public long getFlaggedAt() {
        return flaggedAt;
    }

    public void setFlaggedAt(long flaggedAt) {
        this.flaggedAt = flaggedAt;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public long getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(long lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
