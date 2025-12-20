package com.example.foodvan.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.PropertyName;

/**
 * Model class for GPS settings and preferences
 * Controls location update behavior and accuracy
 */
@Entity(tableName = "gps_settings")
public class GpsSettings {

    @PrimaryKey
    @NonNull
    @PropertyName("vendorId")
    private String vendorId;

    @PropertyName("autoUpdate")
    private boolean autoUpdate;

    @PropertyName("updateInterval")
    private int updateInterval; // in minutes

    @PropertyName("highAccuracy")
    private boolean highAccuracy;

    @PropertyName("lastUpdated")
    private long lastUpdated;

    // Default constructor for Firebase
    public GpsSettings() {
        this.autoUpdate = false;
        this.updateInterval = 5; // Default 5 minutes
        this.highAccuracy = true;
        this.lastUpdated = System.currentTimeMillis();
    }

    @Ignore
    public GpsSettings(String vendorId) {
        this.vendorId = vendorId;
        this.autoUpdate = false;
        this.updateInterval = 5;
        this.highAccuracy = true;
        this.lastUpdated = System.currentTimeMillis();
    }

    @Ignore
    public GpsSettings(String vendorId, boolean autoUpdate, int updateInterval, boolean highAccuracy) {
        this.vendorId = vendorId;
        this.autoUpdate = autoUpdate;
        this.updateInterval = updateInterval;
        this.highAccuracy = highAccuracy;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public String getVendorId() {
        return vendorId;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public boolean isHighAccuracy() {
        return highAccuracy;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
    }

    public void setHighAccuracy(boolean highAccuracy) {
        this.highAccuracy = highAccuracy;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    // Utility methods
    public long getUpdateIntervalMillis() {
        return updateInterval * 60 * 1000L; // Convert minutes to milliseconds
    }

    public String getUpdateIntervalText() {
        if (updateInterval == 1) {
            return "Every 1 minute";
        } else {
            return "Every " + updateInterval + " minutes";
        }
    }

    public String getAccuracyText() {
        return highAccuracy ? "High Accuracy" : "Balanced";
    }

    @Override
    public String toString() {
        return "GpsSettings{" +
                "vendorId='" + vendorId + '\'' +
                ", autoUpdate=" + autoUpdate +
                ", updateInterval=" + updateInterval +
                ", highAccuracy=" + highAccuracy +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
