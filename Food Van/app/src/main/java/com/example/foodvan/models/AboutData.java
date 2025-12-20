package com.example.foodvan.models;

import java.util.List;

/**
 * AboutData - Main data model for About screen content
 */
public class AboutData {
    private String tagline;
    private String shortDescription;
    private String mission;
    private String currentVersion;
    private String buildNumber;
    private String lastUpdated;
    private List<ChangelogEntry> changelog;
    private List<TeamMember> teamMembers;
    private ContactInfo contactInfo;
    
    public AboutData() {}
    
    public AboutData(String tagline, String shortDescription, String mission,
                    String currentVersion, String buildNumber, String lastUpdated,
                    List<ChangelogEntry> changelog, List<TeamMember> teamMembers,
                    ContactInfo contactInfo) {
        this.tagline = tagline;
        this.shortDescription = shortDescription;
        this.mission = mission;
        this.currentVersion = currentVersion;
        this.buildNumber = buildNumber;
        this.lastUpdated = lastUpdated;
        this.changelog = changelog;
        this.teamMembers = teamMembers;
        this.contactInfo = contactInfo;
    }
    
    // Getters and setters
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    
    public String getShortDescription() { return shortDescription; }
    public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
    
    public String getMission() { return mission; }
    public void setMission(String mission) { this.mission = mission; }
    
    public String getCurrentVersion() { return currentVersion; }
    public void setCurrentVersion(String currentVersion) { this.currentVersion = currentVersion; }
    
    public String getBuildNumber() { return buildNumber; }
    public void setBuildNumber(String buildNumber) { this.buildNumber = buildNumber; }
    
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    
    public List<ChangelogEntry> getChangelog() { return changelog; }
    public void setChangelog(List<ChangelogEntry> changelog) { this.changelog = changelog; }
    
    public List<TeamMember> getTeamMembers() { return teamMembers; }
    public void setTeamMembers(List<TeamMember> teamMembers) { this.teamMembers = teamMembers; }
    
    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }
}