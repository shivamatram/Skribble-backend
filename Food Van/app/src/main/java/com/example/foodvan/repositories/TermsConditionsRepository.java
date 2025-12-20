package com.example.foodvan.repositories;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foodvan.models.TermsConditions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * TermsConditionsRepository - Handles Terms & Conditions data persistence
 * Uses Firebase Realtime Database for dynamic content and SharedPreferences for caching
 */
public class TermsConditionsRepository {
    private static final String TAG = "TermsConditionsRepo";
    private static final String PREF_NAME = "TermsConditionsCache";
    private static final String KEY_CACHED_TERMS = "cached_terms";
    private static final String KEY_TERMS_VERSION = "terms_version";
    private static final String KEY_LAST_FETCHED = "last_fetched";

    private final Context context;
    private final DatabaseReference databaseRef;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public interface TermsConditionsCallback {
        void onSuccess(TermsConditions termsConditions);
        void onError(String error);
    }

    public TermsConditionsRepository(Context context) {
        this.context = context;
        this.databaseRef = FirebaseDatabase.getInstance().getReference();
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = sharedPreferences.edit();
    }

    /**
     * Fetch Terms & Conditions from Firebase
     */
    public void fetchTermsConditions(TermsConditionsCallback callback) {
        // First try to get from cache if recently fetched
        if (isCacheValid()) {
            TermsConditions cachedTerms = getCachedTerms();
            if (cachedTerms != null) {
                callback.onSuccess(cachedTerms);
                return;
            }
        }

        // Fetch from Firebase Realtime Database
        fetchFromDatabase(callback);
    }

    /**
     * Fetch from Firebase Realtime Database
     */
    private void fetchFromDatabase(TermsConditionsCallback callback) {
        databaseRef.child("config").child("appTerms").child("currentVersion")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            TermsConditions terms = snapshot.getValue(TermsConditions.class);
                            if (terms != null) {
                                // Cache the result
                                cacheTerms(terms);
                                callback.onSuccess(terms);
                            } else {
                                // Provide default terms if none found
                                TermsConditions defaultTerms = createDefaultTerms();
                                callback.onSuccess(defaultTerms);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing Terms & Conditions from database", e);
                            callback.onError("Failed to parse Terms & Conditions");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to fetch Terms & Conditions from database", error.toException());
                        // Provide default terms if database fetch fails
                        TermsConditions defaultTerms = createDefaultTerms();
                        callback.onSuccess(defaultTerms);
                    }
                });
    }

    /**
     * Cache Terms & Conditions in SharedPreferences
     */
    private void cacheTerms(TermsConditions terms) {
        try {
            editor.putString(KEY_CACHED_TERMS, terms.getContent());
            editor.putString(KEY_TERMS_VERSION, terms.getVersion());
            editor.putLong(KEY_LAST_FETCHED, System.currentTimeMillis());
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to cache Terms & Conditions", e);
        }
    }

    /**
     * Get cached Terms & Conditions
     */
    public TermsConditions getCachedTerms() {
        try {
            String content = sharedPreferences.getString(KEY_CACHED_TERMS, null);
            String version = sharedPreferences.getString(KEY_TERMS_VERSION, "1.0");
            long lastFetched = sharedPreferences.getLong(KEY_LAST_FETCHED, 0);

            if (content != null) {
                TermsConditions terms = new TermsConditions();
                terms.setTitle("Terms & Conditions");
                terms.setContent(content);
                terms.setVersion(version);
                terms.setLastUpdated(lastFetched);
                return terms;
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get cached Terms & Conditions", e);
        }
        return null;
    }

    /**
     * Check if cached data is still valid (less than 1 day old)
     */
    private boolean isCacheValid() {
        long lastFetched = sharedPreferences.getLong(KEY_LAST_FETCHED, 0);
        long currentTime = System.currentTimeMillis();
        // Cache valid for 24 hours
        return (currentTime - lastFetched) < (24 * 60 * 60 * 1000);
    }

    /**
     * Create default Terms & Conditions content
     */
    private TermsConditions createDefaultTerms() {
        TermsConditions terms = new TermsConditions();
        terms.setTitle("Terms & Conditions");
        terms.setContent(getDefaultTermsContent());
        terms.setVersion("1.0");
        terms.setLastUpdated(System.currentTimeMillis());
        return terms;
    }

    /**
     * Get default Terms & Conditions content
     */
    private String getDefaultTermsContent() {
        return "1. Introduction\n" +
                "Welcome to Food Van. These terms and conditions outline the rules and regulations for the use of Food Van's Services.\n\n" +
                "By accessing this app, we assume you accept these terms and conditions. Do not continue to use Food Van if you do not agree to all of the terms and conditions stated here.\n\n" +
                "2. User Responsibilities\n" +
                "As a user of Food Van, you agree to:\n" +
                "- Provide accurate and complete information\n" +
                "- Maintain the security of your account\n" +
                "- Notify us immediately of any unauthorized use of your account\n" +
                "- Not use the app for any illegal or unauthorized purpose\n\n" +
                "3. Vendor Responsibilities\n" +
                "Food vendors using our platform agree to:\n" +
                "- Provide safe and quality food products\n" +
                "- Maintain accurate menu information\n" +
                "- Update their availability status in real-time\n" +
                "- Comply with all applicable health and safety regulations\n\n" +
                "4. Ordering & Payment Terms\n" +
                "When placing orders through Food Van:\n" +
                "- All payments are processed securely through our payment partners\n" +
                "- Prices are subject to change without prior notice\n" +
                "- We reserve the right to refuse or cancel any order\n\n" +
                "5. Delivery & Pickup Terms\n" +
                "Regarding delivery and pickup services:\n" +
                "- Delivery times are estimates only\n" +
                "- Customers are responsible for providing accurate delivery addresses\n" +
                "- Vendors are responsible for preparing orders within the specified time\n\n" +
                "6. Account & Authentication\n" +
                "For account security:\n" +
                "- You are responsible for maintaining the confidentiality of your account\n" +
                "- You must notify us immediately of any breach of security\n" +
                "- We may suspend or terminate accounts for violations of these terms\n\n" +
                "7. Privacy & Data Usage\n" +
                "We respect your privacy and are committed to protecting your personal data in accordance with our Privacy Policy.\n\n" +
                "8. Limitations of Liability\n" +
                "Food Van shall not be liable for any direct, indirect, incidental, consequential, or punitive damages arising out of your use of the service.\n\n" +
                "9. Changes to Terms\n" +
                "We reserve the right to modify these terms at any time. Changes will be effective immediately upon posting to the app.\n\n" +
                "10. Dispute Resolution\n" +
                "Any disputes arising from the use of Food Van shall be resolved through binding arbitration in accordance with the laws of your jurisdiction.\n\n" +
                "11. Contact Information\n" +
                "If you have any questions about these Terms & Conditions, please contact us at:\n" +
                "Email: support@foodvanapp.com\n" +
                "Phone: +1 (555) 123-4567";
    }
}