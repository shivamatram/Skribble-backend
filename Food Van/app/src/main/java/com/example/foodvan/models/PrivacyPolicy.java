package com.example.foodvan.models;

import java.util.Date;
import java.util.List;

/**
 * Data model representing the complete Privacy Policy document
 */
public class PrivacyPolicy {
    
    private String version;
    private Date lastUpdated;
    private List<PrivacyPolicySection> sections;
    private String languageCode;
    private boolean isActive;
    
    // Empty constructor for Firebase
    public PrivacyPolicy() {}
    
    public PrivacyPolicy(String version, Date lastUpdated, List<PrivacyPolicySection> sections) {
        this.version = version;
        this.lastUpdated = lastUpdated;
        this.sections = sections;
        this.languageCode = "en"; // Default to English
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public List<PrivacyPolicySection> getSections() {
        return sections;
    }
    
    public void setSections(List<PrivacyPolicySection> sections) {
        this.sections = sections;
    }
    
    public String getLanguageCode() {
        return languageCode;
    }
    
    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @Override
    public String toString() {
        return "PrivacyPolicy{" +
                "version='" + version + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", sections=" + (sections != null ? sections.size() : 0) +
                ", languageCode='" + languageCode + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}