package com.example.foodvan.models;

/**
 * Model class for Payout Settings
 */
public class PayoutSettings {
    private boolean autoPayoutEnabled;
    private String payoutMethod; // "Bank Transfer", "UPI Transfer"
    private String payoutFrequency; // "Daily", "Weekly", "Monthly", "Manual"
    private double minThreshold;
    private long lastUpdated;

    public PayoutSettings() {
        // Default constructor for Firebase
    }

    public PayoutSettings(boolean autoPayoutEnabled, String payoutMethod, 
                         String payoutFrequency, double minThreshold) {
        this.autoPayoutEnabled = autoPayoutEnabled;
        this.payoutMethod = payoutMethod;
        this.payoutFrequency = payoutFrequency;
        this.minThreshold = minThreshold;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters
    public boolean isAutoPayoutEnabled() {
        return autoPayoutEnabled;
    }

    public String getPayoutMethod() {
        return payoutMethod;
    }

    public String getPayoutFrequency() {
        return payoutFrequency;
    }

    public double getMinThreshold() {
        return minThreshold;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    // Setters
    public void setAutoPayoutEnabled(boolean autoPayoutEnabled) {
        this.autoPayoutEnabled = autoPayoutEnabled;
    }

    public void setPayoutMethod(String payoutMethod) {
        this.payoutMethod = payoutMethod;
    }

    public void setPayoutFrequency(String payoutFrequency) {
        this.payoutFrequency = payoutFrequency;
    }

    public void setMinThreshold(double minThreshold) {
        this.minThreshold = minThreshold;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "PayoutSettings{" +
                "autoPayoutEnabled=" + autoPayoutEnabled +
                ", payoutMethod='" + payoutMethod + '\'' +
                ", payoutFrequency='" + payoutFrequency + '\'' +
                ", minThreshold=" + minThreshold +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
