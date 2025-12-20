package com.example.foodvan.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.PropertyName;

/**
 * Model class for location history entries
 * Tracks vendor's previous location updates
 */
@Entity(tableName = "location_history")
public class LocationHistory {

    @PrimaryKey
    @NonNull
    @PropertyName("historyId")
    private String historyId;

    @PropertyName("vendorId")
    private String vendorId;

    @PropertyName("latitude")
    private double latitude;

    @PropertyName("longitude")
    private double longitude;

    @PropertyName("address")
    private String address;

    @PropertyName("timestamp")
    private long timestamp;

    @PropertyName("accuracy")
    private float accuracy;

    @PropertyName("isActive")
    private boolean isActive;

    @PropertyName("status")
    private String status;

    // Default constructor for Firebase
    public LocationHistory() {
        this.timestamp = System.currentTimeMillis();
        this.isActive = true;
        this.status = "Active";
    }

    @Ignore
    public LocationHistory(String historyId, String vendorId, double latitude, double longitude, String address) {
        this.historyId = historyId;
        this.vendorId = vendorId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.timestamp = System.currentTimeMillis();
        this.isActive = true;
        this.status = "Active";
    }

    @Ignore
    public LocationHistory(String vendorId, double latitude, double longitude, String address, float accuracy) {
        this.historyId = generateHistoryId();
        this.vendorId = vendorId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.accuracy = accuracy;
        this.timestamp = System.currentTimeMillis();
        this.isActive = true;
        this.status = "Active";
    }

    // Generate unique history ID
    private String generateHistoryId() {
        return "loc_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }

    // Getters
    public String getHistoryId() {
        return historyId;
    }

    public String getVendorId() {
        return vendorId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public boolean isActive() {
        return isActive;
    }

    // Setters
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "LocationHistory{" +
                "historyId='" + historyId + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                ", timestamp=" + timestamp +
                ", accuracy=" + accuracy +
                ", isActive=" + isActive +
                '}';
    }
}
