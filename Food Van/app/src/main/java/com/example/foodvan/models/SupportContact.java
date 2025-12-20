package com.example.foodvan.models;

import java.io.Serializable;

/**
 * SupportContact model class representing support contact information
 */
public class SupportContact implements Serializable {
    private String email;
    private String phone;
    private String whatsapp;
    private String workingHours;
    private String address;
    private boolean emailEnabled;
    private boolean phoneEnabled;
    private boolean whatsappEnabled;

    public SupportContact() {
        // Default constructor required for Firebase
        this.emailEnabled = true;
        this.phoneEnabled = true;
        this.whatsappEnabled = true;
    }

    public SupportContact(String email, String phone, String whatsapp) {
        this();
        this.email = email;
        this.phone = phone;
        this.whatsapp = whatsapp;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isPhoneEnabled() {
        return phoneEnabled;
    }

    public void setPhoneEnabled(boolean phoneEnabled) {
        this.phoneEnabled = phoneEnabled;
    }

    public boolean isWhatsappEnabled() {
        return whatsappEnabled;
    }

    public void setWhatsappEnabled(boolean whatsappEnabled) {
        this.whatsappEnabled = whatsappEnabled;
    }

    // Utility methods
    public boolean hasEmail() {
        return email != null && !email.isEmpty() && emailEnabled;
    }

    public boolean hasPhone() {
        return phone != null && !phone.isEmpty() && phoneEnabled;
    }

    public boolean hasWhatsapp() {
        return whatsapp != null && !whatsapp.isEmpty() && whatsappEnabled;
    }

    public String getFormattedPhone() {
        if (phone == null) return "";
        // Format phone number for display
        return phone.replaceFirst("(\\d{2})(\\d{5})(\\d{5})", "+$1 $2-$3");
    }

    public String getWhatsappUrl() {
        if (whatsapp == null || whatsapp.isEmpty()) return "";
        // Remove any non-digit characters and create WhatsApp URL
        String cleanNumber = whatsapp.replaceAll("[^0-9]", "");
        return "https://wa.me/" + cleanNumber;
    }

    public String getPhoneUri() {
        if (phone == null || phone.isEmpty()) return "";
        return "tel:" + phone.replaceAll("[^0-9+]", "");
    }

    public String getEmailUri() {
        if (email == null || email.isEmpty()) return "";
        return "mailto:" + email;
    }

    // Default support contact (fallback)
    public static SupportContact getDefaultContact() {
        SupportContact contact = new SupportContact();
        contact.setEmail("support@foodvan.com");
        contact.setPhone("+91 9876543210");
        contact.setWhatsapp("+919876543210");
        contact.setWorkingHours("9:00 AM - 9:00 PM (Mon-Sat)");
        contact.setAddress("Food Van Support Center, Bangalore, India");
        return contact;
    }
}
