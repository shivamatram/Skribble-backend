package com.example.foodvan.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ReviewMeta model class for aggregated review statistics
 */
public class ReviewMeta implements Serializable {
    private String vendorId;
    private double averageRating;
    private int totalReviews;
    private Map<Integer, Integer> ratingCounts;
    private long lastUpdated;
    private double thirtyDayAverage;
    private int thirtyDayCount;
    private String topPraisedItems;
    private String commonCompliments;
    private String commonComplaints;

    public ReviewMeta() {
        // Default constructor required for Firebase
        this.ratingCounts = new HashMap<>();
        initializeRatingCounts();
    }

    public ReviewMeta(String vendorId) {
        this.vendorId = vendorId;
        this.averageRating = 0.0;
        this.totalReviews = 0;
        this.ratingCounts = new HashMap<>();
        this.lastUpdated = System.currentTimeMillis();
        this.thirtyDayAverage = 0.0;
        this.thirtyDayCount = 0;
        initializeRatingCounts();
    }

    private void initializeRatingCounts() {
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0);
        }
    }

    // Getters and Setters
    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public Map<Integer, Integer> getRatingCounts() {
        return ratingCounts;
    }

    public void setRatingCounts(Map<Integer, Integer> ratingCounts) {
        this.ratingCounts = ratingCounts;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getThirtyDayAverage() {
        return thirtyDayAverage;
    }

    public void setThirtyDayAverage(double thirtyDayAverage) {
        this.thirtyDayAverage = thirtyDayAverage;
    }

    public int getThirtyDayCount() {
        return thirtyDayCount;
    }

    public void setThirtyDayCount(int thirtyDayCount) {
        this.thirtyDayCount = thirtyDayCount;
    }

    public String getTopPraisedItems() {
        return topPraisedItems;
    }

    public void setTopPraisedItems(String topPraisedItems) {
        this.topPraisedItems = topPraisedItems;
    }

    public String getCommonCompliments() {
        return commonCompliments;
    }

    public void setCommonCompliments(String commonCompliments) {
        this.commonCompliments = commonCompliments;
    }

    public String getCommonComplaints() {
        return commonComplaints;
    }

    public void setCommonComplaints(String commonComplaints) {
        this.commonComplaints = commonComplaints;
    }

    // Utility methods
    public String getFormattedAverageRating() {
        return String.format("%.1f", averageRating);
    }

    public int getRatingCount(int rating) {
        return ratingCounts.getOrDefault(rating, 0);
    }

    public double getRatingPercentage(int rating) {
        if (totalReviews == 0) return 0.0;
        return (getRatingCount(rating) * 100.0) / totalReviews;
    }

    public String getFormattedRatingPercentage(int rating) {
        return String.format("%.1f%%", getRatingPercentage(rating));
    }

    public void updateWithNewReview(float rating) {
        // Update counts
        int ratingInt = Math.round(rating);
        ratingCounts.put(ratingInt, getRatingCount(ratingInt) + 1);
        totalReviews++;

        // Recalculate average
        double totalRatingSum = 0;
        for (Map.Entry<Integer, Integer> entry : ratingCounts.entrySet()) {
            totalRatingSum += entry.getKey() * entry.getValue();
        }
        averageRating = totalRatingSum / totalReviews;
        lastUpdated = System.currentTimeMillis();
    }

    public void removeReview(float rating) {
        if (totalReviews <= 0) return;

        int ratingInt = Math.round(rating);
        int currentCount = getRatingCount(ratingInt);
        if (currentCount > 0) {
            ratingCounts.put(ratingInt, currentCount - 1);
            totalReviews--;

            // Recalculate average
            if (totalReviews > 0) {
                double totalRatingSum = 0;
                for (Map.Entry<Integer, Integer> entry : ratingCounts.entrySet()) {
                    totalRatingSum += entry.getKey() * entry.getValue();
                }
                averageRating = totalRatingSum / totalReviews;
            } else {
                averageRating = 0.0;
            }
            lastUpdated = System.currentTimeMillis();
        }
    }

    public boolean hasReviews() {
        return totalReviews > 0;
    }

    public String getTrendIndicator() {
        if (thirtyDayCount == 0) return "No recent data";
        
        double difference = thirtyDayAverage - averageRating;
        if (Math.abs(difference) < 0.1) {
            return "Stable";
        } else if (difference > 0) {
            return "Improving";
        } else {
            return "Declining";
        }
    }

    @Override
    public String toString() {
        return "ReviewMeta{" +
                "vendorId='" + vendorId + '\'' +
                ", averageRating=" + averageRating +
                ", totalReviews=" + totalReviews +
                ", ratingCounts=" + ratingCounts +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
