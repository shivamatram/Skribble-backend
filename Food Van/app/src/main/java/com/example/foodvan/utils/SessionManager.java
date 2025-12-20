package com.example.foodvan.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.foodvan.models.User;

/**
 * SessionManager - Handles user session management using SharedPreferences
 */
public class SessionManager {
    private static final String PREF_NAME = "FoodVanSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_PROFILE_IMAGE = "userProfileImage";
    private static final String KEY_PHONE_VERIFIED = "phoneVerified";
    private static final String KEY_COUNTRY_CODE = "countryCode";
    private static final String KEY_PHONE_LAST_UPDATED = "phoneLastUpdated";
    
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, user.getUserId());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_PROFILE_IMAGE, user.getProfileImageUrl());
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get user details from session
     */
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }
        
        User user = new User();
        user.setUserId(pref.getString(KEY_USER_ID, ""));
        user.setName(pref.getString(KEY_USER_NAME, ""));
        user.setEmail(pref.getString(KEY_USER_EMAIL, ""));
        user.setPhone(pref.getString(KEY_USER_PHONE, ""));
        user.setRole(pref.getString(KEY_USER_ROLE, "customer"));
        user.setProfileImageUrl(pref.getString(KEY_USER_PROFILE_IMAGE, ""));
        
        return user;
    }

    /**
     * Get user ID from session
     */
    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    /**
     * Get user role from session
     */
    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "customer");
    }

    /**
     * Get user name from session
     */
    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    /**
     * Get user email from session
     */
    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    /**
     * Update user profile image
     */
    public void updateProfileImage(String imageUrl) {
        editor.putString(KEY_USER_PROFILE_IMAGE, imageUrl);
        editor.apply();
    }

    /**
     * Update user name
     */
    public void updateUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Clear session and logout user
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Check if user is vendor
     */
    public boolean isVendor() {
        return "vendor".equals(getUserRole());
    }

    /**
     * Check if user is customer
     */
    public boolean isCustomer() {
        return "customer".equals(getUserRole());
    }

    /**
     * Update user phone number
     */
    public void updateUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    /**
     * Get user phone from session
     */
    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, "");
    }

    /**
     * Set phone verified status
     */
    public void setPhoneVerified(boolean verified) {
        editor.putBoolean(KEY_PHONE_VERIFIED, verified);
        editor.putLong(KEY_PHONE_LAST_UPDATED, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Check if phone is verified
     */
    public boolean isPhoneVerified() {
        return pref.getBoolean(KEY_PHONE_VERIFIED, false);
    }

    /**
     * Set country code
     */
    public void setCountryCode(String countryCode) {
        editor.putString(KEY_COUNTRY_CODE, countryCode);
        editor.apply();
    }

    /**
     * Get country code
     */
    public String getCountryCode() {
        return pref.getString(KEY_COUNTRY_CODE, "+91");
    }

    /**
     * Update phone verification data (phone, verified status, country code)
     */
    public void updatePhoneVerification(String phone, String countryCode, boolean verified) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_COUNTRY_CODE, countryCode);
        editor.putBoolean(KEY_PHONE_VERIFIED, verified);
        editor.putLong(KEY_PHONE_LAST_UPDATED, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Get phone last updated timestamp
     */
    public long getPhoneLastUpdated() {
        return pref.getLong(KEY_PHONE_LAST_UPDATED, 0);
    }
}
