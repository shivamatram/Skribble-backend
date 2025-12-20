package com.example.foodvan.models;

import java.util.List;

/**
 * Data model representing a Privacy Policy section or subsection
 * Used to structure the privacy policy content dynamically
 */
public class PrivacyPolicySection {
    
    private String title;
    private String content;
    private List<PrivacyPolicySection> subsections;
    private int order;
    private String sectionId;
    
    // Empty constructor for Firebase
    public PrivacyPolicySection() {}
    
    public PrivacyPolicySection(String title, String content) {
        this.title = title;
        this.content = content;
    }
    
    public PrivacyPolicySection(String title, String content, List<PrivacyPolicySection> subsections) {
        this.title = title;
        this.content = content;
        this.subsections = subsections;
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
    
    public List<PrivacyPolicySection> getSubsections() {
        return subsections;
    }
    
    public void setSubsections(List<PrivacyPolicySection> subsections) {
        this.subsections = subsections;
    }
    
    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order) {
        this.order = order;
    }
    
    public String getSectionId() {
        return sectionId;
    }
    
    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
    
    /**
     * Check if this section has subsections
     */
    public boolean hasSubsections() {
        return subsections != null && !subsections.isEmpty();
    }
    
    @Override
    public String toString() {
        return "PrivacyPolicySection{" +
                "title='" + title + '\'' +
                ", content='" + (content != null ? content.substring(0, Math.min(50, content.length())) + "..." : "null") + '\'' +
                ", subsections=" + (subsections != null ? subsections.size() : 0) +
                ", order=" + order +
                ", sectionId='" + sectionId + '\'' +
                '}';
    }
}