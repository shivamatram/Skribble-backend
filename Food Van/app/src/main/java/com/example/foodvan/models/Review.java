package com.example.foodvan.models;

import java.io.Serializable;
import java.util.List;

/**
 * Review model class representing a user review for food vans or items
 */
public class Review implements Serializable {
    private String reviewId;
    private String userId;
    private String userName;
    private String userProfileImage;
    private String vendorId;
    private String itemId;
    private String itemName;
    private float rating;
    private String reviewText;
    private List<String> imageUrls;
    private long timestamp;
    private boolean isVerifiedPurchase;
    private boolean isAnonymous;
    private int helpfulCount;
    private String vendorReply;
    private long vendorReplyTimestamp;
    private String reviewType; // "VENDOR", "ITEM", "ORDER"
    private String status; // "ACTIVE", "HIDDEN", "REPORTED"

    public Review() {
        // Default constructor required for Firebase
    }

    public Review(String reviewId, String userId, String userName, String vendorId, 
                  String itemId, float rating, String reviewText) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.userName = userName;
        this.vendorId = vendorId;
        this.itemId = itemId;
        this.rating = rating;
        this.reviewText = reviewText;
        this.timestamp = System.currentTimeMillis();
        this.isVerifiedPurchase = false;
        this.isAnonymous = false;
        this.helpfulCount = 0;
        this.reviewType = "VENDOR";
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isVerifiedPurchase() {
        return isVerifiedPurchase;
    }

    public void setVerifiedPurchase(boolean verifiedPurchase) {
        isVerifiedPurchase = verifiedPurchase;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    public int getHelpfulCount() {
        return helpfulCount;
    }

    public void setHelpfulCount(int helpfulCount) {
        this.helpfulCount = helpfulCount;
    }

    public String getVendorReply() {
        return vendorReply;
    }

    public void setVendorReply(String vendorReply) {
        this.vendorReply = vendorReply;
    }

    public long getVendorReplyTimestamp() {
        return vendorReplyTimestamp;
    }

    public void setVendorReplyTimestamp(long vendorReplyTimestamp) {
        this.vendorReplyTimestamp = vendorReplyTimestamp;
    }

    public String getReviewType() {
        return reviewType;
    }

    public void setReviewType(String reviewType) {
        this.reviewType = reviewType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Utility methods
    public String getDisplayName() {
        if (isAnonymous) {
            return "Anonymous User";
        }
        return userName != null ? userName : "Unknown User";
    }

    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public boolean hasVendorReply() {
        return vendorReply != null && !vendorReply.trim().isEmpty();
    }

    public int getRatingAsInt() {
        return Math.round(rating);
    }

    public String getFormattedRating() {
        return String.format("%.1f", rating);
    }

    public boolean isOwnedBy(String currentUserId) {
        return userId != null && userId.equals(currentUserId);
    }

    public boolean canBeEdited() {
        return status.equals("ACTIVE");
    }

    public void incrementHelpfulCount() {
        this.helpfulCount++;
    }

    public void decrementHelpfulCount() {
        if (this.helpfulCount > 0) {
            this.helpfulCount--;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Review review = (Review) obj;
        return reviewId != null ? reviewId.equals(review.reviewId) : review.reviewId == null;
    }

    @Override
    public int hashCode() {
        return reviewId != null ? reviewId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId='" + reviewId + '\'' +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", rating=" + rating +
                ", reviewText='" + reviewText + '\'' +
                ", timestamp=" + timestamp +
                ", isVerifiedPurchase=" + isVerifiedPurchase +
                ", helpfulCount=" + helpfulCount +
                '}';
    }
}
