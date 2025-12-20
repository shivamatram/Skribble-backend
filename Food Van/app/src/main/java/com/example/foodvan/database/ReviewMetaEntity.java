package com.example.foodvan.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;

import com.example.foodvan.models.ReviewMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Room entity for caching review metadata locally
 */
@Entity(tableName = "review_meta")
@TypeConverters({Converters.class})
public class ReviewMetaEntity {
    @PrimaryKey
    @NonNull
    private String vendorId;
    
    private double averageRating;
    private int totalReviews;
    private int rating1Count;
    private int rating2Count;
    private int rating3Count;
    private int rating4Count;
    private int rating5Count;
    private long lastUpdated;
    private double thirtyDayAverage;
    private int thirtyDayCount;
    private String topPraisedItems;
    private String commonCompliments;
    private String commonComplaints;

    public ReviewMetaEntity() {
        // Default constructor required for Room
    }

    // Constructor from ReviewMeta model
    public static ReviewMetaEntity fromReviewMeta(ReviewMeta reviewMeta) {
        ReviewMetaEntity entity = new ReviewMetaEntity();
        entity.vendorId = reviewMeta.getVendorId();
        entity.averageRating = reviewMeta.getAverageRating();
        entity.totalReviews = reviewMeta.getTotalReviews();
        
        Map<Integer, Integer> ratingCounts = reviewMeta.getRatingCounts();
        entity.rating1Count = ratingCounts.getOrDefault(1, 0);
        entity.rating2Count = ratingCounts.getOrDefault(2, 0);
        entity.rating3Count = ratingCounts.getOrDefault(3, 0);
        entity.rating4Count = ratingCounts.getOrDefault(4, 0);
        entity.rating5Count = ratingCounts.getOrDefault(5, 0);
        
        entity.lastUpdated = reviewMeta.getLastUpdated();
        entity.thirtyDayAverage = reviewMeta.getThirtyDayAverage();
        entity.thirtyDayCount = reviewMeta.getThirtyDayCount();
        entity.topPraisedItems = reviewMeta.getTopPraisedItems();
        entity.commonCompliments = reviewMeta.getCommonCompliments();
        entity.commonComplaints = reviewMeta.getCommonComplaints();
        
        return entity;
    }

    // Convert to ReviewMeta model
    public ReviewMeta toReviewMeta() {
        ReviewMeta reviewMeta = new ReviewMeta(vendorId);
        reviewMeta.setAverageRating(averageRating);
        reviewMeta.setTotalReviews(totalReviews);
        
        Map<Integer, Integer> ratingCounts = new HashMap<>();
        ratingCounts.put(1, rating1Count);
        ratingCounts.put(2, rating2Count);
        ratingCounts.put(3, rating3Count);
        ratingCounts.put(4, rating4Count);
        ratingCounts.put(5, rating5Count);
        reviewMeta.setRatingCounts(ratingCounts);
        
        reviewMeta.setLastUpdated(lastUpdated);
        reviewMeta.setThirtyDayAverage(thirtyDayAverage);
        reviewMeta.setThirtyDayCount(thirtyDayCount);
        reviewMeta.setTopPraisedItems(topPraisedItems);
        reviewMeta.setCommonCompliments(commonCompliments);
        reviewMeta.setCommonComplaints(commonComplaints);
        
        return reviewMeta;
    }

    // Getters and Setters
    @NonNull
    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(@NonNull String vendorId) {
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

    public int getRating1Count() {
        return rating1Count;
    }

    public void setRating1Count(int rating1Count) {
        this.rating1Count = rating1Count;
    }

    public int getRating2Count() {
        return rating2Count;
    }

    public void setRating2Count(int rating2Count) {
        this.rating2Count = rating2Count;
    }

    public int getRating3Count() {
        return rating3Count;
    }

    public void setRating3Count(int rating3Count) {
        this.rating3Count = rating3Count;
    }

    public int getRating4Count() {
        return rating4Count;
    }

    public void setRating4Count(int rating4Count) {
        this.rating4Count = rating4Count;
    }

    public int getRating5Count() {
        return rating5Count;
    }

    public void setRating5Count(int rating5Count) {
        this.rating5Count = rating5Count;
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
}
