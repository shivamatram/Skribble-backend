package com.example.foodvan.models;

import java.io.Serializable;

/**
 * TermsConditions - Model for storing Terms & Conditions content
 */
public class TermsConditions implements Serializable {
    private String title;
    private String content;
    private String version;
    private long lastUpdated;
    private String[] sections;

    public TermsConditions() {
        // Default constructor required for Firebase
    }

    public TermsConditions(String title, String content, String version, long lastUpdated, String[] sections) {
        this.title = title;
        this.content = content;
        this.version = version;
        this.lastUpdated = lastUpdated;
        this.sections = sections;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String[] getSections() {
        return sections;
    }

    public void setSections(String[] sections) {
        this.sections = sections;
    }
}