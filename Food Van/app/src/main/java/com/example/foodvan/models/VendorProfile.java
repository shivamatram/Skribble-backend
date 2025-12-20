package com.example.foodvan.models;

import java.io.Serializable;

/**
 * VendorProfile model class representing vendor account information
 */
public class VendorProfile implements Serializable {
    
    private String vendorId;
    private String vendorName;
    private String email;
    private String phone;
    private String businessName;
    private String address;
    private String profileImageUrl;
    private boolean isActive;
    private long createdAt;
    private long updatedAt;
    
    // Additional business information
    private String businessType;
    private String businessDescription;
    private String gstNumber;
    private String fssaiNumber;
    
    // Location information
    private double latitude;
    private double longitude;
    private String city;
    private String state;
    private String pincode;

    public VendorProfile() {
        // Default constructor required for Firebase
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public VendorProfile(String vendorId, String vendorName, String email, String phone) {
        this();
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.email = email;
        this.phone = phone;
    }

    // Getters and Setters
    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
        updateTimestamp();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
        updateTimestamp();
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
        updateTimestamp();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        updateTimestamp();
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        updateTimestamp();
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
        updateTimestamp();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
        updateTimestamp();
    }

    public String getBusinessDescription() {
        return businessDescription;
    }

    public void setBusinessDescription(String businessDescription) {
        this.businessDescription = businessDescription;
        updateTimestamp();
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
        updateTimestamp();
    }

    public String getFssaiNumber() {
        return fssaiNumber;
    }

    public void setFssaiNumber(String fssaiNumber) {
        this.fssaiNumber = fssaiNumber;
        updateTimestamp();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
        updateTimestamp();
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
        updateTimestamp();
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
        updateTimestamp();
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
        updateTimestamp();
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
        updateTimestamp();
    }

    // Utility methods
    private void updateTimestamp() {
        this.updatedAt = System.currentTimeMillis();
    }

    public String getFormattedAddress() {
        if (address == null || address.isEmpty()) {
            return "Address not provided";
        }
        return address;
    }

    public String getDisplayName() {
        if (vendorName != null && !vendorName.isEmpty()) {
            return vendorName;
        } else if (businessName != null && !businessName.isEmpty()) {
            return businessName;
        } else {
            return "Vendor";
        }
    }

    public boolean hasCompleteProfile() {
        return vendorName != null && !vendorName.isEmpty() &&
               email != null && !email.isEmpty() &&
               phone != null && !phone.isEmpty() &&
               businessName != null && !businessName.isEmpty() &&
               address != null && !address.isEmpty();
    }

    @Override
    public String toString() {
        return "VendorProfile{" +
                "vendorId='" + vendorId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", businessName='" + businessName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
