package com.example.foodvan.models;

/**
 * Model class for UPI Details
 */
public class UpiDetails {
    private String upiId;
    private boolean isVerified;
    private String qrCodeData;
    private long lastUpdated;

    public UpiDetails() {
        // Default constructor for Firebase
    }

    public UpiDetails(String upiId) {
        this.upiId = upiId;
        this.isVerified = false;
        this.qrCodeData = null;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public String getUpiId() {
        return upiId;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public String getQrCodeData() {
        return qrCodeData;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public void setQrCodeData(String qrCodeData) {
        this.qrCodeData = qrCodeData;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "UpiDetails{" +
                "upiId='" + upiId + '\'' +
                ", isVerified=" + isVerified +
                ", qrCodeData='" + qrCodeData + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
