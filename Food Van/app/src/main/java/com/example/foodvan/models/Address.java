package com.example.foodvan.models;

import java.io.Serializable;

/**
 * Address model for user saved addresses
 */
public class Address implements Serializable {
    
    private String addressId;
    private String businessName; // Business/Location name
    private String label; // Primary, Secondary, Weekend Location, etc.
    private String fullAddress;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private double latitude;
    private double longitude;
    private String landmark;
    private String instructions; // Delivery instructions
    private boolean isPrimary; // Primary business location
    private long createdAt;
    private long lastUsed;
    
    // Contact Information
    private String contactName;
    private String phoneNumber;
    private String flatBuilding;
    
    public Address() {
        this.addressId = generateAddressId();
        this.createdAt = System.currentTimeMillis();
        this.lastUsed = System.currentTimeMillis();
        this.country = "India";
        this.isPrimary = false;
    }
    
    public Address(String label, String fullAddress) {
        this();
        this.label = label;
        this.fullAddress = fullAddress;
    }
    
    private String generateAddressId() {
        return "addr_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
    
    // Getters and Setters
    public String getAddressId() {
        return addressId;
    }
    
    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }
    
    public String getBusinessName() {
        return businessName;
    }
    
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public String getFullAddress() {
        return fullAddress;
    }
    
    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }
    
    public String getAddressLine1() {
        return addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    
    public String getAddressLine2() {
        return addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    
    // Backward compatibility for customer addresses
    public String getStreetAddress() {
        return addressLine1;
    }
    
    public void setStreetAddress(String streetAddress) {
        this.addressLine1 = streetAddress;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getState() {
        return state;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    
    public String getLandmark() {
        return landmark;
    }
    
    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
    
    public boolean isPrimary() {
        return isPrimary;
    }
    
    public void setPrimary(boolean primary) {
        isPrimary = primary;
    }
    
    // Backward compatibility for customer addresses
    public boolean isDefault() {
        return isPrimary;
    }
    
    public void setDefault(boolean isDefault) {
        this.isPrimary = isDefault;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getLastUsed() {
        return lastUsed;
    }
    
    public void setLastUsed(long lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    // Helper methods
    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        
        if (addressLine1 != null && !addressLine1.trim().isEmpty()) {
            sb.append(addressLine1);
        }
        
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(addressLine2);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        
        if (state != null && !state.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state);
        }
        
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(postalCode);
        }
        
        return sb.toString();
    }
    
    public String getShortAddress() {
        if (fullAddress != null && fullAddress.length() > 50) {
            return fullAddress.substring(0, 47) + "...";
        }
        return fullAddress;
    }
    
    public void updateLastUsed() {
        this.lastUsed = System.currentTimeMillis();
    }
    
    public boolean hasCoordinates() {
        return latitude != 0.0 && longitude != 0.0;
    }
    
    // Contact Information Getters and Setters
    public String getContactName() {
        return contactName;
    }
    
    public void setContactName(String contactName) {
        this.contactName = contactName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getFlatBuilding() {
        return flatBuilding;
    }
    
    public void setFlatBuilding(String flatBuilding) {
        this.flatBuilding = flatBuilding;
    }
    
    @Override
    public String toString() {
        return label + ": " + getFormattedAddress();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Address address = (Address) obj;
        return addressId != null ? addressId.equals(address.addressId) : address.addressId == null;
    }
    
    @Override
    public int hashCode() {
        return addressId != null ? addressId.hashCode() : 0;
    }
}
