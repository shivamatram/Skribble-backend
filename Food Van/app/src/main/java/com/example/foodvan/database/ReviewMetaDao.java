package com.example.foodvan.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Data Access Object for ReviewMeta operations
 */
@Dao
public interface ReviewMetaDao {

    @Query("SELECT * FROM review_meta WHERE vendorId = :vendorId")
    LiveData<ReviewMetaEntity> getReviewMetaForVendor(String vendorId);

    @Query("SELECT * FROM review_meta WHERE vendorId = :vendorId")
    ReviewMetaEntity getReviewMetaForVendorSync(String vendorId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReviewMeta(ReviewMetaEntity reviewMeta);

    @Update
    void updateReviewMeta(ReviewMetaEntity reviewMeta);

    @Query("DELETE FROM review_meta WHERE vendorId = :vendorId")
    void deleteReviewMetaForVendor(String vendorId);

    @Query("UPDATE review_meta SET averageRating = :averageRating, totalReviews = :totalReviews, lastUpdated = :lastUpdated WHERE vendorId = :vendorId")
    void updateBasicStats(String vendorId, double averageRating, int totalReviews, long lastUpdated);

    @Query("UPDATE review_meta SET rating1Count = :count WHERE vendorId = :vendorId")
    void updateRating1Count(String vendorId, int count);

    @Query("UPDATE review_meta SET rating2Count = :count WHERE vendorId = :vendorId")
    void updateRating2Count(String vendorId, int count);

    @Query("UPDATE review_meta SET rating3Count = :count WHERE vendorId = :vendorId")
    void updateRating3Count(String vendorId, int count);

    @Query("UPDATE review_meta SET rating4Count = :count WHERE vendorId = :vendorId")
    void updateRating4Count(String vendorId, int count);

    @Query("UPDATE review_meta SET rating5Count = :count WHERE vendorId = :vendorId")
    void updateRating5Count(String vendorId, int count);

    @Query("UPDATE review_meta SET thirtyDayAverage = :average, thirtyDayCount = :count WHERE vendorId = :vendorId")
    void updateThirtyDayStats(String vendorId, double average, int count);
}
