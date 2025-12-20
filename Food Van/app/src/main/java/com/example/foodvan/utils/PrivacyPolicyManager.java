package com.example.foodvan.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.example.foodvan.models.PrivacyPolicy;
import com.example.foodvan.models.PrivacyPolicySection;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for handling Privacy Policy data
 * Handles backend integration, caching, and content parsing
 */
public class PrivacyPolicyManager {

    private static final String TAG = "PrivacyPolicyManager";
    
    // Firestore collection and document paths
    private static final String COLLECTION_CONFIG = "config";
    private static final String DOCUMENT_PRIVACY_POLICY = "privacyPolicy";
    private static final String SUBCOLLECTION_VERSIONS = "versions";
    private static final String DOCUMENT_CURRENT = "current";
    
    private final Context context;
    private final FirebaseFirestore firestore;
    private final Gson gson;
    
    public PrivacyPolicyManager(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.gson = new Gson();
    }
    
    /**
     * Interface for Privacy Policy callback events
     */
    public interface PrivacyPolicyCallback {
        void onPrivacyPolicyLoaded(List<PrivacyPolicySection> sections, String version, Date lastUpdated);
        void onPrivacyPolicyError(String error);
    }
    
    /**
     * Fetch privacy policy from Firestore
     */
    public void fetchPrivacyPolicy(PrivacyPolicyCallback callback) {
        Log.d(TAG, "Fetching privacy policy from Firestore");
        
        DocumentReference docRef = firestore
                .collection(COLLECTION_CONFIG)
                .document(DOCUMENT_PRIVACY_POLICY);
        
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    Map<String, Object> data = task.getResult().getData();
                    if (data != null) {
                        PrivacyPolicy policy = parsePrivacyPolicyFromFirestore(data);
                        
                        if (policy != null && policy.getSections() != null) {
                            Log.d(TAG, "Privacy policy loaded successfully, version: " + policy.getVersion());
                            callback.onPrivacyPolicyLoaded(
                                    policy.getSections(),
                                    policy.getVersion(),
                                    policy.getLastUpdated()
                            );
                        } else {
                            // Try to load from default/fallback content
                            loadDefaultPrivacyPolicy(callback);
                        }
                    } else {
                        loadDefaultPrivacyPolicy(callback);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing privacy policy data", e);
                    loadDefaultPrivacyPolicy(callback);
                }
            } else {
                Log.e(TAG, "Failed to fetch privacy policy", task.getException());
                loadDefaultPrivacyPolicy(callback);
            }
        });
    }
    
    /**
     * Check for privacy policy updates
     */
    public void checkForUpdates(PrivacyPolicyCallback callback) {
        DocumentReference docRef = firestore
                .collection(COLLECTION_CONFIG)
                .document(DOCUMENT_PRIVACY_POLICY);
        
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                try {
                    Map<String, Object> data = task.getResult().getData();
                    if (data != null) {
                        String version = (String) data.get("version");
                        Date lastUpdated = task.getResult().getDate("lastUpdated");
                        
                        // You can compare with cached version here
                        Log.d(TAG, "Current version in backend: " + version);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error checking for updates", e);
                }
            }
        });
    }
    
    /**
     * Load default privacy policy content as fallback
     */
    private void loadDefaultPrivacyPolicy(PrivacyPolicyCallback callback) {
        Log.d(TAG, "Loading default privacy policy content");
        
        try {
            List<PrivacyPolicySection> sections = createDefaultPrivacyPolicyContent();
            callback.onPrivacyPolicyLoaded(
                    sections,
                    "1.0.0", // Default version
                    new Date() // Current date
            );
        } catch (Exception e) {
            Log.e(TAG, "Error creating default content", e);
            callback.onPrivacyPolicyError("Unable to load privacy policy content");
        }
    }
    
    /**
     * Parse privacy policy data from Firestore document
     */
    private PrivacyPolicy parsePrivacyPolicyFromFirestore(Map<String, Object> data) {
        try {
            PrivacyPolicy policy = new PrivacyPolicy();
            
            policy.setVersion((String) data.get("version"));
            policy.setLanguageCode((String) data.getOrDefault("languageCode", "en"));
            policy.setActive((Boolean) data.getOrDefault("isActive", true));
            
            // Handle lastUpdated field
            Object lastUpdatedObj = data.get("lastUpdated");
            if (lastUpdatedObj instanceof com.google.firebase.Timestamp) {
                policy.setLastUpdated(((com.google.firebase.Timestamp) lastUpdatedObj).toDate());
            } else {
                policy.setLastUpdated(new Date());
            }
            
            // Parse sections
            Object sectionsObj = data.get("sections");
            if (sectionsObj instanceof List) {
                List<Map<String, Object>> sectionMaps = (List<Map<String, Object>>) sectionsObj;
                List<PrivacyPolicySection> sections = new ArrayList<>();
                
                for (Map<String, Object> sectionMap : sectionMaps) {
                    PrivacyPolicySection section = parseSectionFromMap(sectionMap);
                    if (section != null) {
                        sections.add(section);
                    }
                }
                
                // Sort sections by order
                Collections.sort(sections, Comparator.comparingInt(PrivacyPolicySection::getOrder));
                
                policy.setSections(sections);
            }
            
            return policy;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing privacy policy from Firestore", e);
            return null;
        }
    }
    
    /**
     * Parse a single section from Firestore map
     */
    private PrivacyPolicySection parseSectionFromMap(Map<String, Object> sectionMap) {
        try {
            PrivacyPolicySection section = new PrivacyPolicySection();
            
            section.setTitle((String) sectionMap.get("title"));
            section.setContent((String) sectionMap.get("content"));
            section.setSectionId((String) sectionMap.get("sectionId"));
            
            Object orderObj = sectionMap.get("order");
            if (orderObj instanceof Number) {
                section.setOrder(((Number) orderObj).intValue());
            }
            
            // Parse subsections if present
            Object subsectionsObj = sectionMap.get("subsections");
            if (subsectionsObj instanceof List) {
                List<Map<String, Object>> subsectionMaps = (List<Map<String, Object>>) subsectionsObj;
                List<PrivacyPolicySection> subsections = new ArrayList<>();
                
                for (Map<String, Object> subsectionMap : subsectionMaps) {
                    PrivacyPolicySection subsection = parseSectionFromMap(subsectionMap);
                    if (subsection != null) {
                        subsections.add(subsection);
                    }
                }
                
                section.setSubsections(subsections);
            }
            
            return section;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing section from map", e);
            return null;
        }
    }
    
    /**
     * Create default privacy policy content
     */
    private List<PrivacyPolicySection> createDefaultPrivacyPolicyContent() {
        List<PrivacyPolicySection> sections = new ArrayList<>();
        
        // 1. Introduction
        sections.add(new PrivacyPolicySection(
                "Introduction",
                "Welcome to Food Van, the mobile application that connects food lovers with local mobile food vendors. " +
                "This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use " +
                "our mobile application. By using the Food Van app, you agree to the collection and use of information " +
                "in accordance with this policy."
        ));
        sections.get(sections.size() - 1).setOrder(1);
        
        // 2. Information We Collect
        PrivacyPolicySection infoSection = new PrivacyPolicySection(
                "Information We Collect",
                "We collect several types of information to provide and improve our services to you."
        );
        infoSection.setOrder(2);
        
        List<PrivacyPolicySection> infoSubsections = new ArrayList<>();
        infoSubsections.add(new PrivacyPolicySection(
                "Personal Information",
                "When you create an account, we collect your name, email address, phone number, and profile photo. " +
                "For vendors, we also collect business information including business name, license details, and banking information for payments."
        ));
        
        infoSubsections.add(new PrivacyPolicySection(
                "Location Information",
                "We collect your GPS location to help you discover nearby food vans and to provide accurate delivery estimates. " +
                "For vendors, location data helps customers find your van and enables real-time tracking."
        ));
        
        infoSubsections.add(new PrivacyPolicySection(
                "Order and Payment Information",
                "We collect details about your orders, including items purchased, order history, preferences, and payment information. " +
                "Payment processing is handled securely through third-party payment processors."
        ));
        
        infoSubsections.add(new PrivacyPolicySection(
                "Device and Usage Information",
                "We automatically collect device information, app usage statistics, crash logs, and analytics data to improve our services."
        ));
        
        infoSection.setSubsections(infoSubsections);
        sections.add(infoSection);
        
        // 3. How We Use Your Information
        PrivacyPolicySection useSection = new PrivacyPolicySection(
                "How We Use Your Information",
                "We use the collected information for various purposes to provide and maintain our service."
        );
        useSection.setOrder(3);
        
        List<PrivacyPolicySection> useSubsections = new ArrayList<>();
        useSubsections.add(new PrivacyPolicySection(
                "Core App Functions",
                "Your information enables core functionality including account management, order processing, payment handling, " +
                "GPS-based vendor discovery, real-time order tracking, and push notifications for order updates."
        ));
        
        useSubsections.add(new PrivacyPolicySection(
                "Service Improvement",
                "We analyze usage patterns and feedback to enhance app performance, develop new features, and provide " +
                "personalized recommendations based on your order history and preferences."
        ));
        
        useSubsections.add(new PrivacyPolicySection(
                "Customer Support",
                "We use your information to provide customer support, resolve disputes, handle refunds, and respond to your inquiries."
        ));
        
        useSection.setSubsections(useSubsections);
        sections.add(useSection);
        
        // 4. Information Sharing and Disclosure
        sections.add(new PrivacyPolicySection(
                "Information Sharing and Disclosure",
                "We only share your personal information in specific circumstances: with vendors for order fulfillment " +
                "(name, contact info, delivery address, order details), with payment processors for transaction processing, " +
                "with analytics services for aggregated usage insights, and as required by law or to protect our rights. " +
                "We do not sell your personal information to third parties for marketing purposes."
        ));
        sections.get(sections.size() - 1).setOrder(4);
        
        // 5. Data Security
        sections.add(new PrivacyPolicySection(
                "Data Security",
                "We implement appropriate security measures to protect your personal information including encryption " +
                "in transit (HTTPS/TLS), secure cloud storage with access controls, regular security audits, and " +
                "role-based access restrictions. However, no method of transmission over the internet is 100% secure, " +
                "and we cannot guarantee absolute security."
        ));
        sections.get(sections.size() - 1).setOrder(5);
        
        // 6. Your Privacy Rights
        sections.add(new PrivacyPolicySection(
                "Your Privacy Rights",
                "You have the right to access, update, or delete your personal information through your account settings. " +
                "You can control location permissions, notification settings, and data sharing preferences. For data export " +
                "or deletion requests that cannot be completed through the app, please contact our support team."
        ));
        sections.get(sections.size() - 1).setOrder(6);
        
        // 7. Data Retention
        sections.add(new PrivacyPolicySection(
                "Data Retention",
                "We retain your personal information for as long as necessary to provide our services and fulfill legal obligations. " +
                "Account information is retained while your account is active. Order history and transaction data are retained " +
                "for business and legal purposes. You may request deletion of your account and associated data at any time."
        ));
        sections.get(sections.size() - 1).setOrder(7);
        
        // 8. Children's Privacy
        sections.add(new PrivacyPolicySection(
                "Children's Privacy",
                "Our service is not intended for children under the age of 13. We do not knowingly collect personal " +
                "information from children under 13. If you are a parent or guardian and believe your child has provided " +
                "personal information, please contact us to have the information removed."
        ));
        sections.get(sections.size() - 1).setOrder(8);
        
        // 9. Changes to This Policy
        sections.add(new PrivacyPolicySection(
                "Changes to This Privacy Policy",
                "We may update our Privacy Policy from time to time. When we make changes, we will notify you through " +
                "the app and update the 'Last Updated' date. Your continued use of the service after any changes " +
                "constitutes acceptance of the new Privacy Policy."
        ));
        sections.get(sections.size() - 1).setOrder(9);
        
        // 10. Contact Information
        sections.add(new PrivacyPolicySection(
                "Contact Us",
                "If you have any questions about this Privacy Policy or our data practices, please contact us at:\\n\\n" +
                "Email: support@foodvan.app\\n" +
                "Help Center: Available in the app under Settings > Help & Support\\n\\n" +
                "We will respond to your inquiry within 48 hours."
        ));
        sections.get(sections.size() - 1).setOrder(10);
        
        return sections;
    }
    
    /**
     * Parse privacy policy content from JSON string (for caching)
     */
    public List<PrivacyPolicySection> parsePrivacyPolicyContent(String jsonContent) {
        if (TextUtils.isEmpty(jsonContent)) {
            return new ArrayList<>();
        }
        
        try {
            Type listType = new TypeToken<List<PrivacyPolicySection>>(){}.getType();
            List<PrivacyPolicySection> sections = gson.fromJson(jsonContent, listType);
            
            // Sort by order
            if (sections != null) {
                Collections.sort(sections, Comparator.comparingInt(PrivacyPolicySection::getOrder));
                return sections;
            }
            
            return new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing JSON content", e);
            return createDefaultPrivacyPolicyContent();
        }
    }
    
    /**
     * Serialize privacy policy content to JSON string (for caching)
     */
    public String serializePrivacyPolicyContent(List<PrivacyPolicySection> sections) {
        try {
            return gson.toJson(sections);
        } catch (Exception e) {
            Log.e(TAG, "Error serializing content to JSON", e);
            return "";
        }
    }
    
    /**
     * Create or update privacy policy in Firestore (for backend management)
     */
    public void createOrUpdatePrivacyPolicy(PrivacyPolicy policy) {
        Map<String, Object> data = new HashMap<>();
        data.put("version", policy.getVersion());
        data.put("lastUpdated", policy.getLastUpdated());
        data.put("languageCode", policy.getLanguageCode());
        data.put("isActive", policy.isActive());
        data.put("sections", convertSectionsToMaps(policy.getSections()));
        
        firestore.collection(COLLECTION_CONFIG)
                .document(DOCUMENT_PRIVACY_POLICY)
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Privacy policy updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating privacy policy", e));
    }
    
    /**
     * Convert sections to maps for Firestore storage
     */
    private List<Map<String, Object>> convertSectionsToMaps(List<PrivacyPolicySection> sections) {
        List<Map<String, Object>> sectionMaps = new ArrayList<>();
        
        for (PrivacyPolicySection section : sections) {
            Map<String, Object> sectionMap = new HashMap<>();
            sectionMap.put("title", section.getTitle());
            sectionMap.put("content", section.getContent());
            sectionMap.put("order", section.getOrder());
            sectionMap.put("sectionId", section.getSectionId());
            
            if (section.hasSubsections()) {
                sectionMap.put("subsections", convertSectionsToMaps(section.getSubsections()));
            }
            
            sectionMaps.add(sectionMap);
        }
        
        return sectionMaps;
    }
}