package com.example.foodvan.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.database.ReviewDao;
import com.example.foodvan.database.ReviewEntity;
import com.example.foodvan.database.ReviewMetaDao;
import com.example.foodvan.database.ReviewMetaEntity;
import com.example.foodvan.models.Review;
import com.example.foodvan.models.ReviewMeta;
import com.example.foodvan.models.VendorReply;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for managing reviews data from Firestore and local Room database
 */
public class ReviewRepository {
    private static final String TAG = "ReviewRepository";
    private static final String VENDORS_COLLECTION = "vendors";
    private static final String REVIEWS_COLLECTION = "reviews";
    private static final String REVIEWS_META_COLLECTION = "reviewsMeta";
    private static final String REVIEW_AUDIT_COLLECTION = "reviewAudit";

    private final FirebaseFirestore firestore;
    private final ReviewDao reviewDao;
    private final ReviewMetaDao reviewMetaDao;
    private final ExecutorService executor;
    private final Context context;

    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);

    public ReviewRepository(Context context, ReviewDao reviewDao, ReviewMetaDao reviewMetaDao) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.reviewDao = reviewDao;
        this.reviewMetaDao = reviewMetaDao;
        this.executor = Executors.newFixedThreadPool(3);
    }

    // LiveData getters
    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<Boolean> getIsLoadingLiveData() {
        return isLoadingLiveData;
    }

    // Get reviews for vendor from local database
    public LiveData<List<ReviewEntity>> getReviewsForVendor(String vendorId) {
        return reviewDao.getReviewsForVendor(vendorId);
    }

    public LiveData<List<ReviewEntity>> getReviewsForVendorPaged(String vendorId, int limit, int offset) {
        return reviewDao.getReviewsForVendorPaged(vendorId, limit, offset);
    }

    public LiveData<List<ReviewEntity>> getReviewsByRating(String vendorId, int rating) {
        return reviewDao.getReviewsByRating(vendorId, rating);
    }

    public LiveData<List<ReviewEntity>> getReviewsWithReplies(String vendorId) {
        return reviewDao.getReviewsWithReplies(vendorId);
    }

    public LiveData<List<ReviewEntity>> getReviewsWithoutReplies(String vendorId) {
        return reviewDao.getReviewsWithoutReplies(vendorId);
    }

    public LiveData<List<ReviewEntity>> searchReviews(String vendorId, String searchQuery) {
        return reviewDao.searchReviews(vendorId, searchQuery);
    }

    public LiveData<ReviewMetaEntity> getReviewMetaForVendor(String vendorId) {
        return reviewMetaDao.getReviewMetaForVendor(vendorId);
    }

    // Sync reviews from Firestore
    public void syncReviewsFromFirestore(String vendorId) {
        isLoadingLiveData.setValue(true);
        
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_COLLECTION)
                .whereEqualTo("visible", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    executor.execute(() -> {
                        try {
                            List<ReviewEntity> reviews = new ArrayList<>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Review review = document.toObject(Review.class);
                                review.setReviewId(document.getId());
                                reviews.add(ReviewEntity.fromReview(review));
                            }
                            
                            // Insert/update reviews in local database
                            reviewDao.insertReviews(reviews);
                            
                            Log.d(TAG, "Synced " + reviews.size() + " reviews for vendor " + vendorId);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing synced reviews", e);
                            errorLiveData.postValue("Error syncing reviews: " + e.getMessage());
                        } finally {
                            isLoadingLiveData.postValue(false);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error syncing reviews from Firestore", e);
                    errorLiveData.setValue("Failed to sync reviews: " + e.getMessage());
                    isLoadingLiveData.setValue(false);
                });
    }

    // Sync review metadata from Firestore
    public void syncReviewMetaFromFirestore(String vendorId) {
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_META_COLLECTION)
                .document("meta")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    executor.execute(() -> {
                        try {
                            if (documentSnapshot.exists()) {
                                ReviewMeta reviewMeta = documentSnapshot.toObject(ReviewMeta.class);
                                if (reviewMeta != null) {
                                    reviewMeta.setVendorId(vendorId);
                                    reviewMetaDao.insertReviewMeta(ReviewMetaEntity.fromReviewMeta(reviewMeta));
                                    Log.d(TAG, "Synced review meta for vendor " + vendorId);
                                }
                            } else {
                                // Create default meta if doesn't exist
                                ReviewMeta defaultMeta = new ReviewMeta(vendorId);
                                reviewMetaDao.insertReviewMeta(ReviewMetaEntity.fromReviewMeta(defaultMeta));
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing synced review meta", e);
                            errorLiveData.postValue("Error syncing review statistics: " + e.getMessage());
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error syncing review meta from Firestore", e);
                    errorLiveData.setValue("Failed to sync review statistics: " + e.getMessage());
                });
    }

    // Add vendor reply to review
    public void addVendorReply(String vendorId, String reviewId, String replyText, String vendorName) {
        isLoadingLiveData.setValue(true);
        
        VendorReply vendorReply = new VendorReply(vendorId, vendorName, replyText);
        
        Map<String, Object> replyData = new HashMap<>();
        replyData.put("vendorReply", vendorReply);
        replyData.put("vendorReplyTimestamp", System.currentTimeMillis());
        
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .update(replyData)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        // Update local database
                        reviewDao.updateVendorReply(reviewId, replyText, 
                                vendorReply.getCreatedAt(), 0, false);
                        Log.d(TAG, "Added vendor reply to review " + reviewId);
                    });
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding vendor reply", e);
                    errorLiveData.setValue("Failed to add reply: " + e.getMessage());
                    isLoadingLiveData.setValue(false);
                });
    }

    // Edit vendor reply
    public void editVendorReply(String vendorId, String reviewId, String newReplyText) {
        isLoadingLiveData.setValue(true);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("vendorReply.text", newReplyText);
        updateData.put("vendorReply.editedAt", System.currentTimeMillis());
        updateData.put("vendorReply.isEdited", true);
        
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        // Update local database
                        reviewDao.updateVendorReply(reviewId, newReplyText, 
                                0, System.currentTimeMillis(), true);
                        Log.d(TAG, "Updated vendor reply for review " + reviewId);
                    });
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error editing vendor reply", e);
                    errorLiveData.setValue("Failed to edit reply: " + e.getMessage());
                    isLoadingLiveData.setValue(false);
                });
    }

    // Delete vendor reply
    public void deleteVendorReply(String vendorId, String reviewId) {
        isLoadingLiveData.setValue(true);
        
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("vendorReply", null);
        updateData.put("vendorReplyTimestamp", null);
        
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        // Update local database
                        reviewDao.deleteVendorReply(reviewId);
                        Log.d(TAG, "Deleted vendor reply for review " + reviewId);
                    });
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting vendor reply", e);
                    errorLiveData.setValue("Failed to delete reply: " + e.getMessage());
                    isLoadingLiveData.setValue(false);
                });
    }

    // Flag review
    public void flagReview(String vendorId, String reviewId, String reason) {
        isLoadingLiveData.setValue(true);
        
        Map<String, Object> flagData = new HashMap<>();
        flagData.put("flagged", true);
        flagData.put("flagReason", reason);
        flagData.put("flaggedAt", System.currentTimeMillis());
        
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_COLLECTION)
                .document(reviewId)
                .update(flagData)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        // Update local database
                        reviewDao.flagReview(reviewId, reason, System.currentTimeMillis());
                        Log.d(TAG, "Flagged review " + reviewId);
                    });
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error flagging review", e);
                    errorLiveData.setValue("Failed to flag review: " + e.getMessage());
                    isLoadingLiveData.setValue(false);
                });
    }

    // Soft delete review (hide from view)
    public void softDeleteReview(String vendorId, String reviewId, String reason) {
        isLoadingLiveData.setValue(true);
        
        // Create audit log entry
        Map<String, Object> auditData = new HashMap<>();
        auditData.put("reviewId", reviewId);
        auditData.put("action", "SOFT_DELETE");
        auditData.put("reason", reason);
        auditData.put("timestamp", System.currentTimeMillis());
        auditData.put("vendorId", vendorId);
        
        WriteBatch batch = firestore.batch();
        
        // Update review visibility
        DocumentReference reviewRef = firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_COLLECTION)
                .document(reviewId);
        batch.update(reviewRef, "visible", false);
        
        // Add audit log
        DocumentReference auditRef = firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEW_AUDIT_COLLECTION)
                .document();
        batch.set(auditRef, auditData);
        
        batch.commit()
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        // Update local database
                        reviewDao.softDeleteReview(reviewId);
                        Log.d(TAG, "Soft deleted review " + reviewId);
                    });
                    isLoadingLiveData.setValue(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error soft deleting review", e);
                    errorLiveData.setValue("Failed to delete review: " + e.getMessage());
                    isLoadingLiveData.setValue(false);
                });
    }

    // Update review metadata
    public void updateReviewMeta(String vendorId, ReviewMeta reviewMeta) {
        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .collection(REVIEWS_META_COLLECTION)
                .document("meta")
                .set(reviewMeta)
                .addOnSuccessListener(aVoid -> {
                    executor.execute(() -> {
                        // Update local database
                        reviewMetaDao.insertReviewMeta(ReviewMetaEntity.fromReviewMeta(reviewMeta));
                        Log.d(TAG, "Updated review meta for vendor " + vendorId);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating review meta", e);
                    errorLiveData.setValue("Failed to update statistics: " + e.getMessage());
                });
    }

    // Calculate and update review statistics
    public void recalculateReviewStats(String vendorId) {
        executor.execute(() -> {
            try {
                // Get all reviews for vendor from local database
                // Note: This should be done with a synchronous call
                // For now, we'll trigger a Firestore recalculation
                
                firestore.collection(VENDORS_COLLECTION)
                        .document(vendorId)
                        .collection(REVIEWS_COLLECTION)
                        .whereEqualTo("visible", true)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            ReviewMeta meta = new ReviewMeta(vendorId);
                            
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Review review = document.toObject(Review.class);
                                meta.updateWithNewReview(review.getRating());
                            }
                            
                            // Calculate 30-day stats
                            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
                            int recentCount = 0;
                            double recentSum = 0;
                            
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Review review = document.toObject(Review.class);
                                if (review.getTimestamp() >= thirtyDaysAgo) {
                                    recentCount++;
                                    recentSum += review.getRating();
                                }
                            }
                            
                            if (recentCount > 0) {
                                meta.setThirtyDayAverage(recentSum / recentCount);
                                meta.setThirtyDayCount(recentCount);
                            }
                            
                            updateReviewMeta(vendorId, meta);
                        });
                        
            } catch (Exception e) {
                Log.e(TAG, "Error recalculating review stats", e);
                errorLiveData.postValue("Failed to recalculate statistics: " + e.getMessage());
            }
        });
    }

    // Clear error
    public void clearError() {
        errorLiveData.setValue(null);
    }
}
