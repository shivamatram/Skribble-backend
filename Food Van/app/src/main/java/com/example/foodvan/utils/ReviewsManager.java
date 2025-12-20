package com.example.foodvan.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foodvan.models.Review;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton utility class for managing reviews with Firebase Realtime Database
 */
public class ReviewsManager {
    private static final String TAG = "ReviewsManager";
    private static ReviewsManager instance;
    private DatabaseReference reviewsRef;
    private SessionManager sessionManager;
    private Context context;

    // Firebase paths
    private static final String REVIEWS_PATH = "reviews";
    private static final String VENDOR_REVIEWS_PATH = "vendor_reviews";
    private static final String USER_REVIEWS_PATH = "user_reviews";
    private static final String REVIEW_STATS_PATH = "review_stats";

    public interface ReviewCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface ReviewsLoadCallback {
        void onReviewsLoaded(List<Review> reviews);
        void onError(String error);
    }

    public interface ReviewStatsCallback {
        void onStatsLoaded(ReviewStats stats);
        void onError(String error);
    }

    private ReviewsManager(Context context) {
        this.context = context.getApplicationContext();
        this.sessionManager = new SessionManager(this.context);
        this.reviewsRef = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized ReviewsManager getInstance(Context context) {
        if (instance == null) {
            instance = new ReviewsManager(context);
        }
        return instance;
    }

    /**
     * Submit a new review
     */
    public void submitReview(String vendorId, String itemId, String itemName, 
                           float rating, String reviewText, List<String> imageUrls,
                           boolean isAnonymous, ReviewCallback callback) {
        
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        // Check if user has already reviewed this item/vendor
        checkExistingReview(vendorId, itemId, userId, new ReviewCallback() {
            @Override
            public void onSuccess(String message) {
                callback.onError("You have already reviewed this item");
            }

            @Override
            public void onError(String error) {
                // No existing review, proceed with submission
                proceedWithReviewSubmission(vendorId, itemId, itemName, rating, 
                                          reviewText, imageUrls, isAnonymous, callback);
            }
        });
    }

    private void proceedWithReviewSubmission(String vendorId, String itemId, String itemName,
                                           float rating, String reviewText, List<String> imageUrls,
                                           boolean isAnonymous, ReviewCallback callback) {
        
        String userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();
        String reviewId = reviewsRef.child(REVIEWS_PATH).push().getKey();

        if (reviewId == null) {
            callback.onError("Failed to generate review ID");
            return;
        }

        Review review = new Review(reviewId, userId, userName, vendorId, itemId, rating, reviewText);
        review.setItemName(itemName);
        review.setImageUrls(imageUrls);
        review.setAnonymous(isAnonymous);
        review.setVerifiedPurchase(checkVerifiedPurchase(userId, vendorId, itemId));

        Map<String, Object> updates = new HashMap<>();
        
        // Main reviews collection
        updates.put(REVIEWS_PATH + "/" + reviewId, review);
        
        // Vendor-specific reviews
        updates.put(VENDOR_REVIEWS_PATH + "/" + vendorId + "/" + reviewId, review);
        
        // User-specific reviews
        updates.put(USER_REVIEWS_PATH + "/" + userId + "/" + reviewId, review);

        reviewsRef.updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                updateReviewStats(vendorId, rating, true);
                callback.onSuccess("Review submitted successfully");
                Log.d(TAG, "Review submitted: " + reviewId);
            })
            .addOnFailureListener(e -> {
                callback.onError("Failed to submit review: " + e.getMessage());
                Log.e(TAG, "Failed to submit review", e);
            });
    }

    /**
     * Load reviews for a specific vendor
     */
    public void loadVendorReviews(String vendorId, String sortBy, String filterBy, 
                                 ReviewsLoadCallback callback) {
        
        // Add null check to prevent Firebase child() error
        if (vendorId == null || vendorId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid vendor ID");
            }
            return;
        }
        
        Query query = reviewsRef.child(VENDOR_REVIEWS_PATH).child(vendorId);
        
        // Apply sorting
        switch (sortBy) {
            case "newest":
                query = query.orderByChild("timestamp");
                break;
            case "oldest":
                query = query.orderByChild("timestamp");
                break;
            case "highest_rating":
                query = query.orderByChild("rating");
                break;
            case "lowest_rating":
                query = query.orderByChild("rating");
                break;
            default:
                query = query.orderByChild("timestamp");
                break;
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> reviews = new ArrayList<>();
                
                for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (review != null && review.getStatus().equals("ACTIVE")) {
                        // Apply filtering
                        if (shouldIncludeReview(review, filterBy)) {
                            reviews.add(review);
                        }
                    }
                }

                // Apply sorting direction
                if (sortBy.equals("newest") || sortBy.equals("highest_rating")) {
                    Collections.reverse(reviews);
                }

                callback.onReviewsLoaded(reviews);
                Log.d(TAG, "Loaded " + reviews.size() + " reviews for vendor: " + vendorId);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError("Failed to load reviews: " + error.getMessage());
                Log.e(TAG, "Failed to load reviews", error.toException());
            }
        });
    }

    /**
     * Update an existing review
     */
    public void updateReview(String reviewId, float rating, String reviewText, 
                           List<String> imageUrls, ReviewCallback callback) {
        
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        // First, get the existing review to verify ownership
        reviewsRef.child(REVIEWS_PATH).child(reviewId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Review existingReview = snapshot.getValue(Review.class);
                    if (existingReview == null) {
                        callback.onError("Review not found");
                        return;
                    }

                    if (!existingReview.isOwnedBy(userId)) {
                        callback.onError("You can only edit your own reviews");
                        return;
                    }

                    // Update the review
                    float oldRating = existingReview.getRating();
                    existingReview.setRating(rating);
                    existingReview.setReviewText(reviewText);
                    existingReview.setImageUrls(imageUrls);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put(REVIEWS_PATH + "/" + reviewId, existingReview);
                    updates.put(VENDOR_REVIEWS_PATH + "/" + existingReview.getVendorId() + "/" + reviewId, existingReview);
                    updates.put(USER_REVIEWS_PATH + "/" + userId + "/" + reviewId, existingReview);

                    reviewsRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            // Update stats if rating changed
                            if (oldRating != rating) {
                                updateReviewStatsForEdit(existingReview.getVendorId(), oldRating, rating);
                            }
                            callback.onSuccess("Review updated successfully");
                            Log.d(TAG, "Review updated: " + reviewId);
                        })
                        .addOnFailureListener(e -> {
                            callback.onError("Failed to update review: " + e.getMessage());
                            Log.e(TAG, "Failed to update review", e);
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Failed to load review: " + error.getMessage());
                }
            });
    }

    /**
     * Delete a review
     */
    public void deleteReview(String reviewId, ReviewCallback callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        // First, get the review to verify ownership and get vendor info
        reviewsRef.child(REVIEWS_PATH).child(reviewId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Review review = snapshot.getValue(Review.class);
                    if (review == null) {
                        callback.onError("Review not found");
                        return;
                    }

                    if (!review.isOwnedBy(userId)) {
                        callback.onError("You can only delete your own reviews");
                        return;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put(REVIEWS_PATH + "/" + reviewId, null);
                    updates.put(VENDOR_REVIEWS_PATH + "/" + review.getVendorId() + "/" + reviewId, null);
                    updates.put(USER_REVIEWS_PATH + "/" + userId + "/" + reviewId, null);

                    reviewsRef.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> {
                            updateReviewStats(review.getVendorId(), review.getRating(), false);
                            callback.onSuccess("Review deleted successfully");
                            Log.d(TAG, "Review deleted: " + reviewId);
                        })
                        .addOnFailureListener(e -> {
                            callback.onError("Failed to delete review: " + e.getMessage());
                            Log.e(TAG, "Failed to delete review", e);
                        });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Failed to load review: " + error.getMessage());
                }
            });
    }

    /**
     * Toggle helpful status for a review
     */
    public void toggleHelpful(String reviewId, boolean isHelpful, ReviewCallback callback) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            callback.onError("User not logged in");
            return;
        }

        DatabaseReference helpfulRef = reviewsRef.child("review_helpful").child(reviewId).child(userId);
        
        if (isHelpful) {
            helpfulRef.setValue(true)
                .addOnSuccessListener(aVoid -> {
                    incrementHelpfulCount(reviewId);
                    callback.onSuccess("Marked as helpful");
                })
                .addOnFailureListener(e -> callback.onError("Failed to mark as helpful"));
        } else {
            helpfulRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    decrementHelpfulCount(reviewId);
                    callback.onSuccess("Removed helpful mark");
                })
                .addOnFailureListener(e -> callback.onError("Failed to remove helpful mark"));
        }
    }

    /**
     * Load review statistics for a vendor
     */
    public void loadReviewStats(String vendorId, ReviewStatsCallback callback) {
        reviewsRef.child(REVIEW_STATS_PATH).child(vendorId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReviewStats stats = snapshot.getValue(ReviewStats.class);
                    if (stats == null) {
                        stats = new ReviewStats();
                    }
                    callback.onStatsLoaded(stats);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Failed to load stats: " + error.getMessage());
                }
            });
    }

    // Helper methods
    private void checkExistingReview(String vendorId, String itemId, String userId, ReviewCallback callback) {
        reviewsRef.child(USER_REVIEWS_PATH).child(userId)
            .orderByChild("vendorId").equalTo(vendorId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        Review review = reviewSnapshot.getValue(Review.class);
                        if (review != null && review.getItemId().equals(itemId)) {
                            callback.onSuccess("Existing review found");
                            return;
                        }
                    }
                    callback.onError("No existing review");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Error checking existing review");
                }
            });
    }

    private boolean checkVerifiedPurchase(String userId, String vendorId, String itemId) {
        // TODO: Implement logic to check if user has actually purchased from this vendor
        // This would involve checking order history
        return false;
    }

    private boolean shouldIncludeReview(Review review, String filterBy) {
        switch (filterBy) {
            case "5_star":
                return review.getRating() == 5.0f;
            case "4_star":
                return review.getRating() >= 4.0f && review.getRating() < 5.0f;
            case "3_star":
                return review.getRating() >= 3.0f && review.getRating() < 4.0f;
            case "2_star":
                return review.getRating() >= 2.0f && review.getRating() < 3.0f;
            case "1_star":
                return review.getRating() >= 1.0f && review.getRating() < 2.0f;
            case "recent":
                long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000);
                return review.getTimestamp() > weekAgo;
            case "verified":
                return review.isVerifiedPurchase();
            case "with_photos":
                return review.hasImages();
            default:
                return true;
        }
    }

    private void updateReviewStats(String vendorId, float rating, boolean isAdd) {
        reviewsRef.child(REVIEW_STATS_PATH).child(vendorId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReviewStats stats = snapshot.getValue(ReviewStats.class);
                    if (stats == null) {
                        stats = new ReviewStats();
                    }

                    if (isAdd) {
                        stats.addRating(rating);
                    } else {
                        stats.removeRating(rating);
                    }

                    reviewsRef.child(REVIEW_STATS_PATH).child(vendorId).setValue(stats);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to update review stats", error.toException());
                }
            });
    }

    private void updateReviewStatsForEdit(String vendorId, float oldRating, float newRating) {
        reviewsRef.child(REVIEW_STATS_PATH).child(vendorId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ReviewStats stats = snapshot.getValue(ReviewStats.class);
                    if (stats == null) {
                        stats = new ReviewStats();
                    }

                    stats.removeRating(oldRating);
                    stats.addRating(newRating);

                    reviewsRef.child(REVIEW_STATS_PATH).child(vendorId).setValue(stats);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to update review stats for edit", error.toException());
                }
            });
    }

    private void incrementHelpfulCount(String reviewId) {
        reviewsRef.child(REVIEWS_PATH).child(reviewId).child("helpfulCount")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer currentCount = snapshot.getValue(Integer.class);
                    if (currentCount == null) currentCount = 0;
                    reviewsRef.child(REVIEWS_PATH).child(reviewId).child("helpfulCount")
                        .setValue(currentCount + 1);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to increment helpful count", error.toException());
                }
            });
    }

    private void decrementHelpfulCount(String reviewId) {
        reviewsRef.child(REVIEWS_PATH).child(reviewId).child("helpfulCount")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer currentCount = snapshot.getValue(Integer.class);
                    if (currentCount == null) currentCount = 0;
                    if (currentCount > 0) {
                        reviewsRef.child(REVIEWS_PATH).child(reviewId).child("helpfulCount")
                            .setValue(currentCount - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to decrement helpful count", error.toException());
                }
            });
    }

    /**
     * Review statistics model
     */
    public static class ReviewStats {
        private float averageRating = 0.0f;
        private int totalReviews = 0;
        private int fiveStarCount = 0;
        private int fourStarCount = 0;
        private int threeStarCount = 0;
        private int twoStarCount = 0;
        private int oneStarCount = 0;

        public ReviewStats() {}

        public void addRating(float rating) {
            float totalRatingPoints = averageRating * totalReviews;
            totalReviews++;
            averageRating = (totalRatingPoints + rating) / totalReviews;
            
            incrementStarCount(rating);
        }

        public void removeRating(float rating) {
            if (totalReviews <= 1) {
                averageRating = 0.0f;
                totalReviews = 0;
                resetStarCounts();
            } else {
                float totalRatingPoints = averageRating * totalReviews;
                totalReviews--;
                averageRating = (totalRatingPoints - rating) / totalReviews;
                decrementStarCount(rating);
            }
        }

        private void incrementStarCount(float rating) {
            int ratingInt = Math.round(rating);
            switch (ratingInt) {
                case 5: fiveStarCount++; break;
                case 4: fourStarCount++; break;
                case 3: threeStarCount++; break;
                case 2: twoStarCount++; break;
                case 1: oneStarCount++; break;
            }
        }

        private void decrementStarCount(float rating) {
            int ratingInt = Math.round(rating);
            switch (ratingInt) {
                case 5: if (fiveStarCount > 0) fiveStarCount--; break;
                case 4: if (fourStarCount > 0) fourStarCount--; break;
                case 3: if (threeStarCount > 0) threeStarCount--; break;
                case 2: if (twoStarCount > 0) twoStarCount--; break;
                case 1: if (oneStarCount > 0) oneStarCount--; break;
            }
        }

        private void resetStarCounts() {
            fiveStarCount = fourStarCount = threeStarCount = twoStarCount = oneStarCount = 0;
        }

        // Getters
        public float getAverageRating() { return averageRating; }
        public int getTotalReviews() { return totalReviews; }
        public int getFiveStarCount() { return fiveStarCount; }
        public int getFourStarCount() { return fourStarCount; }
        public int getThreeStarCount() { return threeStarCount; }
        public int getTwoStarCount() { return twoStarCount; }
        public int getOneStarCount() { return oneStarCount; }

        public int getStarPercentage(int stars) {
            if (totalReviews == 0) return 0;
            int count = getStarCount(stars);
            return Math.round((count * 100.0f) / totalReviews);
        }

        public int getStarCount(int stars) {
            switch (stars) {
                case 5: return fiveStarCount;
                case 4: return fourStarCount;
                case 3: return threeStarCount;
                case 2: return twoStarCount;
                case 1: return oneStarCount;
                default: return 0;
            }
        }
    }

    /**
     * Load all reviews for a specific user
     */
    public void loadUserReviews(String userId, ReviewsLoadCallback callback) {
        if (userId == null || userId.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid user ID");
            }
            return;
        }

        reviewsRef.child(USER_REVIEWS_PATH).child(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Review> reviews = new ArrayList<>();
                    
                    for (DataSnapshot reviewSnapshot : snapshot.getChildren()) {
                        Review review = reviewSnapshot.getValue(Review.class);
                        if (review != null) {
                            reviews.add(review);
                        }
                    }

                    // Sort by most recent first
                    Collections.sort(reviews, (r1, r2) -> 
                        Long.compare(r2.getTimestamp(), r1.getTimestamp()));

                    callback.onReviewsLoaded(reviews);
                    Log.d(TAG, "Loaded " + reviews.size() + " user reviews for: " + userId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError("Failed to load user reviews: " + error.getMessage());
                    Log.e(TAG, "Failed to load user reviews", error.toException());
                }
            });
    }
}
