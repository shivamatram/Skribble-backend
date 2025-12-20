package com.example.foodvan.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.firebase.firestore.PropertyName;

/**
 * Model class for default location data
 * Used when GPS is unavailable or as fallback location
 */
@Entity(tableName = "default_locations")
public class DefaultLocation {

    @PrimaryKey
    @NonNull
    @PropertyName("vendorId")
    private String vendorId;

    @PropertyName("addressLine1")
    private String addressLine1;

    @PropertyName("addressLine2")
    private String addressLine2;

    @PropertyName("city")
    private String city;

    @PropertyName("state")
    private String state;

    @PropertyName("pincode")
    private String pincode;

    @PropertyName("latitude")
    private double latitude;

    @PropertyName("longitude")
    private double longitude;

    @PropertyName("lastUpdated")
    private long lastUpdated;

    // Default constructor for Firebase
    public DefaultLocation() {
        this.lastUpdated = System.currentTimeMillis();
    }

    @Ignore
    public DefaultLocation(String vendorId, String addressLine1, String city, String state, String pincode, double latitude, double longitude) {
        this.vendorId = vendorId;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public String getVendorId() {
        return vendorId;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "DefaultLocation{" +
                "vendorId='" + vendorId + '\'' +
                ", addressLine1='" + addressLine1 + '\'' +
                ", addressLine2='" + addressLine2 + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", pincode='" + pincode + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
