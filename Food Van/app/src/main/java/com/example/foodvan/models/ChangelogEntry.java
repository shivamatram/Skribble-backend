package com.example.foodvan.models;

import java.util.List;

/**
 * ChangelogEntry - Model for app version changelog entries
 */
public class ChangelogEntry {
    private String version;
    private String date;
    private String title;
    private List<String> highlights;
    private boolean isExpanded;
    
    public ChangelogEntry() {}
    
    public ChangelogEntry(String version, String date, String title, List<String> highlights) {
        this.version = version;
        this.date = date;
        this.title = title;
        this.highlights = highlights;
        this.isExpanded = false;
    }
    
    // Getters and setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public List<String> getHighlights() { return highlights; }
    public void setHighlights(List<String> highlights) { this.highlights = highlights; }
    
    public boolean isExpanded() { return isExpanded; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}