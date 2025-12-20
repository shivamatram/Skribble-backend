package com.example.foodvan.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User Profile model for comprehensive user data management
 */
public class UserProfile implements Serializable {
    
    private String userId;
    private String fullName;
    private String displayName; // Nickname or preferred display name
    private String email;
    private String phoneNumber;
    private String profileImageUrl;
    private String dateOfBirth;
    private String gender;
    private String pronouns; // he/him, she/her, they/them, custom
    private String bio; // Short bio or description
    private String timeZone; // User's timezone (e.g., Asia/Kolkata)
    private long createdAt;
    private long lastUpdated;
    private boolean isEmailVerified;
    private boolean isPhoneVerified;
    private boolean sharePhoneNumber; // Privacy setting for phone visibility
    private boolean receiveMarketing; // Marketing communications preference
    private boolean allowPersonalization; // Allow personalized recommendations
    
    // Address information
    private List<Address> savedAddresses;
    private String defaultAddressId;
    
    // Preferences
    private UserPreferences preferences;
    
    // Statistics
    private int totalOrders;
    private double totalSpent;
    private int loyaltyPoints;
    private String membershipLevel; // Bronze, Silver, Gold, Platinum
    
    public UserProfile() {
        this.savedAddresses = new ArrayList<>();
        this.preferences = new UserPreferences();
        this.createdAt = System.currentTimeMillis();
        this.lastUpdated = System.currentTimeMillis();
        this.membershipLevel = "Bronze";
        this.totalOrders = 0;
        this.totalSpent = 0.0;
        this.loyaltyPoints = 0;
        // Initialize new fields with defaults
        this.timeZone = java.util.TimeZone.getDefault().getID();
        this.sharePhoneNumber = false;
        this.receiveMarketing = false;
        this.allowPersonalization = true;
    }
    
    public UserProfile(String userId, String fullName, String email, String phoneNumber) {
        this();
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
        updateLastModified();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        updateLastModified();
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        updateLastModified();
    }
    
    public String getProfileImageUrl() {
        return profileImageUrl;
    }
    
    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        updateLastModified();
    }
    
    public String getDateOfBirth() {
        return dateOfBirth;
    }
    
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        updateLastModified();
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
        updateLastModified();
    }
    
    // New fields: displayName, pronouns, bio, timeZone, privacy settings
    public String getDisplayNameField() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        updateLastModified();
    }
    
    public String getPronouns() {
        return pronouns;
    }
    
    public void setPronouns(String pronouns) {
        this.pronouns = pronouns;
        updateLastModified();
    }
    
    public String getBio() {
        return bio;
    }
    
    public void setBio(String bio) {
        this.bio = bio;
        updateLastModified();
    }
    
    public String getTimeZone() {
        return timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        updateLastModified();
    }
    
    public boolean isSharePhoneNumber() {
        return sharePhoneNumber;
    }
    
    public void setSharePhoneNumber(boolean sharePhoneNumber) {
        this.sharePhoneNumber = sharePhoneNumber;
        updateLastModified();
    }
    
    public boolean isReceiveMarketing() {
        return receiveMarketing;
    }
    
    public void setReceiveMarketing(boolean receiveMarketing) {
        this.receiveMarketing = receiveMarketing;
        updateLastModified();
    }
    
    public boolean isAllowPersonalization() {
        return allowPersonalization;
    }
    
    public void setAllowPersonalization(boolean allowPersonalization) {
        this.allowPersonalization = allowPersonalization;
        updateLastModified();
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public boolean isEmailVerified() {
        return isEmailVerified;
    }
    
    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
        updateLastModified();
    }
    
    public boolean isPhoneVerified() {
        return isPhoneVerified;
    }
    
    public void setPhoneVerified(boolean phoneVerified) {
        isPhoneVerified = phoneVerified;
        updateLastModified();
    }
    
    public List<Address> getSavedAddresses() {
        return savedAddresses;
    }
    
    public void setSavedAddresses(List<Address> savedAddresses) {
        this.savedAddresses = savedAddresses;
        updateLastModified();
    }
    
    public String getDefaultAddressId() {
        return defaultAddressId;
    }
    
    public void setDefaultAddressId(String defaultAddressId) {
        this.defaultAddressId = defaultAddressId;
        updateLastModified();
    }
    
    public UserPreferences getPreferences() {
        return preferences;
    }
    
    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
        updateLastModified();
    }
    
    public int getTotalOrders() {
        return totalOrders;
    }
    
    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
        updateLastModified();
    }
    
    public double getTotalSpent() {
        return totalSpent;
    }
    
    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
        updateLastModified();
    }
    
    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }
    
    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
        updateLastModified();
    }
    
    public String getMembershipLevel() {
        return membershipLevel;
    }
    
    public void setMembershipLevel(String membershipLevel) {
        this.membershipLevel = membershipLevel;
        updateLastModified();
    }
    
    // Helper methods
    private void updateLastModified() {
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public void addAddress(Address address) {
        if (savedAddresses == null) {
            savedAddresses = new ArrayList<>();
        }
        savedAddresses.add(address);
        updateLastModified();
    }
    
    public void removeAddress(String addressId) {
        if (savedAddresses != null) {
            savedAddresses.removeIf(address -> address.getAddressId().equals(addressId));
            updateLastModified();
        }
    }
    
    public Address getDefaultAddress() {
        if (defaultAddressId != null && savedAddresses != null) {
            for (Address address : savedAddresses) {
                if (address.getAddressId().equals(defaultAddressId)) {
                    return address;
                }
            }
        }
        return savedAddresses != null && !savedAddresses.isEmpty() ? savedAddresses.get(0) : null;
    }
    
    public String getDisplayName() {
        if (fullName != null && !fullName.trim().isEmpty()) {
            return fullName;
        }
        if (email != null && !email.trim().isEmpty()) {
            return email.split("@")[0];
        }
        return "User";
    }
    
    public String getInitials() {
        String name = getDisplayName();
        if (name.length() == 0) return "U";
        
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].charAt(0) + "" + parts[1].charAt(0)).toUpperCase();
        } else {
            return name.substring(0, Math.min(2, name.length())).toUpperCase();
        }
    }
    
    public void incrementOrderCount() {
        this.totalOrders++;
        updateLastModified();
    }
    
    public void addToTotalSpent(double amount) {
        this.totalSpent += amount;
        updateLastModified();
        updateMembershipLevel();
    }
    
    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
        updateLastModified();
    }
    
    private void updateMembershipLevel() {
        if (totalSpent >= 10000) {
            membershipLevel = "Platinum";
        } else if (totalSpent >= 5000) {
            membershipLevel = "Gold";
        } else if (totalSpent >= 2000) {
            membershipLevel = "Silver";
        } else {
            membershipLevel = "Bronze";
        }
    }
    
    public boolean isProfileComplete() {
        return fullName != null && !fullName.trim().isEmpty() &&
               phoneNumber != null && !phoneNumber.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
    
    public int getProfileCompletionPercentage() {
        int total = 8;
        int completed = 0;
        
        if (fullName != null && !fullName.trim().isEmpty()) completed++;
        if (email != null && !email.trim().isEmpty()) completed++;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) completed++;
        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) completed++;
        if (dateOfBirth != null && !dateOfBirth.trim().isEmpty()) completed++;
        if (gender != null && !gender.trim().isEmpty()) completed++;
        if (savedAddresses != null && !savedAddresses.isEmpty()) completed++;
        if (isEmailVerified) completed++;
        
        return (completed * 100) / total;
    }
}
