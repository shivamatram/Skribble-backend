package com.example.foodvan.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Review operations
 */
@Dao
public interface ReviewDao {

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND visible = 1 ORDER BY createdAt DESC")
    LiveData<List<ReviewEntity>> getReviewsForVendor(String vendorId);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND visible = 1 ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    LiveData<List<ReviewEntity>> getReviewsForVendorPaged(String vendorId, int limit, int offset);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND rating = :rating AND visible = 1 ORDER BY createdAt DESC")
    LiveData<List<ReviewEntity>> getReviewsByRating(String vendorId, int rating);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND vendorReplyText IS NOT NULL AND visible = 1 ORDER BY createdAt DESC")
    LiveData<List<ReviewEntity>> getReviewsWithReplies(String vendorId);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND vendorReplyText IS NULL AND visible = 1 ORDER BY createdAt DESC")
    LiveData<List<ReviewEntity>> getReviewsWithoutReplies(String vendorId);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND (text LIKE '%' || :searchQuery || '%' OR customerName LIKE '%' || :searchQuery || '%' OR orderId LIKE '%' || :searchQuery || '%') AND visible = 1 ORDER BY createdAt DESC")
    LiveData<List<ReviewEntity>> searchReviews(String vendorId, String searchQuery);

    @Query("SELECT * FROM reviews WHERE reviewId = :reviewId")
    LiveData<ReviewEntity> getReviewById(String reviewId);

    @Query("SELECT COUNT(*) FROM reviews WHERE vendorId = :vendorId AND visible = 1")
    LiveData<Integer> getTotalReviewCount(String vendorId);

    @Query("SELECT AVG(rating) FROM reviews WHERE vendorId = :vendorId AND visible = 1")
    LiveData<Double> getAverageRating(String vendorId);

    @Query("SELECT COUNT(*) FROM reviews WHERE vendorId = :vendorId AND rating = :rating AND visible = 1")
    LiveData<Integer> getCountByRating(String vendorId, int rating);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND createdAt >= :thirtyDaysAgo AND visible = 1")
    LiveData<List<ReviewEntity>> getRecentReviews(String vendorId, long thirtyDaysAgo);

    @Query("SELECT * FROM reviews WHERE vendorId = :vendorId AND flagged = 1 AND visible = 1 ORDER BY flaggedAt DESC")
    LiveData<List<ReviewEntity>> getFlaggedReviews(String vendorId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReview(ReviewEntity review);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReviews(List<ReviewEntity> reviews);

    @Update
    void updateReview(ReviewEntity review);

    @Delete
    void deleteReview(ReviewEntity review);

    @Query("DELETE FROM reviews WHERE vendorId = :vendorId")
    void deleteAllReviewsForVendor(String vendorId);

    @Query("UPDATE reviews SET visible = 0 WHERE reviewId = :reviewId")
    void softDeleteReview(String reviewId);

    @Query("UPDATE reviews SET flagged = 1, flagReason = :reason, flaggedAt = :flaggedAt WHERE reviewId = :reviewId")
    void flagReview(String reviewId, String reason, long flaggedAt);

    @Query("UPDATE reviews SET vendorReplyText = :replyText, vendorReplyCreatedAt = :createdAt, vendorReplyEditedAt = :editedAt, vendorReplyIsEdited = :isEdited WHERE reviewId = :reviewId")
    void updateVendorReply(String reviewId, String replyText, long createdAt, long editedAt, boolean isEdited);

    @Query("UPDATE reviews SET vendorReplyText = NULL, vendorReplyCreatedAt = 0, vendorReplyEditedAt = 0, vendorReplyIsEdited = 0 WHERE reviewId = :reviewId")
    void deleteVendorReply(String reviewId);

    @Query("UPDATE reviews SET lastSyncedAt = :syncTime WHERE reviewId = :reviewId")
    void updateSyncTime(String reviewId, long syncTime);

    @Query("SELECT * FROM reviews WHERE lastSyncedAt < :lastSyncTime OR lastSyncedAt IS NULL")
    List<ReviewEntity> getUnsyncedReviews(long lastSyncTime);
}
