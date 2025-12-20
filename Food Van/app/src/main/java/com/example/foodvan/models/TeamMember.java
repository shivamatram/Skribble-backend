package com.example.foodvan.models;

/**
 * TeamMember - Model for team member information
 */
public class TeamMember {
    private String name;
    private String role;
    private String profileUrl;
    private String avatarUrl;
    
    public TeamMember() {}
    
    public TeamMember(String name, String role, String profileUrl, String avatarUrl) {
        this.name = name;
        this.role = role;
        this.profileUrl = profileUrl;
        this.avatarUrl = avatarUrl;
    }
    
    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public String getProfileUrl() { return profileUrl; }
    public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}