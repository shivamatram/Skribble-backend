package com.example.foodvan.models;

/**
 * ContactInfo - Model for contact and social media information
 */
public class ContactInfo {
    private String email;
    private String whatsappNumber;
    private String website;
    private String instagram;
    private String twitter;
    private String facebook;
    
    public ContactInfo() {}
    
    public ContactInfo(String email, String whatsappNumber, String website,
                      String instagram, String twitter, String facebook) {
        this.email = email;
        this.whatsappNumber = whatsappNumber;
        this.website = website;
        this.instagram = instagram;
        this.twitter = twitter;
        this.facebook = facebook;
    }
    
    // Getters and setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }
    
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    
    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }
    
    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }
    
    public String getFacebook() { return facebook; }
    public void setFacebook(String facebook) { this.facebook = facebook; }
}