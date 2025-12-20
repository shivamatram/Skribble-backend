package com.example.foodvan.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.foodvan.models.DefaultLocation;
import com.example.foodvan.models.GpsSettings;
import com.example.foodvan.models.LocationHistory;

import java.util.List;

/**
 * Data Access Object for location-related database operations
 */
@Dao
public interface LocationDao {

    // Default Location operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertDefaultLocation(DefaultLocation defaultLocation);

    @Update
    void updateDefaultLocation(DefaultLocation defaultLocation);

    @Query("SELECT * FROM default_locations WHERE vendorId = :vendorId LIMIT 1")
    DefaultLocation getDefaultLocation(String vendorId);

    @Delete
    void deleteDefaultLocation(DefaultLocation defaultLocation);

    // Location History operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertLocationHistory(LocationHistory locationHistory);

    @Query("SELECT * FROM location_history WHERE vendorId = :vendorId ORDER BY timestamp DESC")
    List<LocationHistory> getAllLocationHistory(String vendorId);

    @Query("SELECT * FROM location_history WHERE vendorId = :vendorId AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    List<LocationHistory> getLocationHistoryByDateRange(String vendorId, long startTime, long endTime);

    @Query("SELECT * FROM location_history WHERE vendorId = :vendorId ORDER BY timestamp DESC LIMIT :limit")
    List<LocationHistory> getRecentLocationHistory(String vendorId, int limit);

    @Delete
    void deleteLocationHistory(LocationHistory locationHistory);

    @Query("DELETE FROM location_history WHERE vendorId = :vendorId")
    void deleteAllLocationHistory(String vendorId);

    @Query("DELETE FROM location_history WHERE vendorId = :vendorId AND timestamp < :cutoffTime")
    void deleteOldLocationHistory(String vendorId, long cutoffTime);

    // GPS Settings operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGpsSettings(GpsSettings gpsSettings);

    @Update
    void updateGpsSettings(GpsSettings gpsSettings);

    @Query("SELECT * FROM gps_settings WHERE vendorId = :vendorId LIMIT 1")
    GpsSettings getGpsSettings(String vendorId);

    @Delete
    void deleteGpsSettings(GpsSettings gpsSettings);

    // Utility queries
    @Query("SELECT COUNT(*) FROM location_history WHERE vendorId = :vendorId")
    int getLocationHistoryCount(String vendorId);

    @Query("SELECT * FROM location_history WHERE vendorId = :vendorId AND status = :status ORDER BY timestamp DESC")
    List<LocationHistory> getLocationHistoryByStatus(String vendorId, String status);
}
