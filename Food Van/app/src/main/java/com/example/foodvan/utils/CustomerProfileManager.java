package com.example.foodvan.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foodvan.models.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * CustomerProfileManager - Handles backend integration, caching, and data management
 * for the Customer Profile screen with Firebase and local storage
 * 
 * Firestore/Realtime DB Structure:
 * /customers/{uid}/profile:
 *   - fullName, displayName, email, phone, phoneVerified (bool)
 *   - profileImageUrl, bio, membershipTier, points
 *   - stats: {ordersCount, totalSpent, lastOrderAt}
 *   - preferences: {marketing, showPhoneOnReceipt}
 * 
 * /customers/{uid}/addresses - for saved addresses
 * /customers/{uid}/payments - for payment tokens/meta
 * /customers/{uid}/reviews - for user reviews
 * 
 * Image Storage: customers/{uid}/profile.jpg
 */
public class CustomerProfileManager {
    
    private static final String TAG = "CustomerProfileManager";
    private static final String PREFS_NAME = "customer_profile_cache";
    private static final String KEY_PROFILE_DATA = "cached_profile_data";
    private static final String KEY_LAST_SYNC = "last_sync_time";
    private static final String KEY_PENDING_CHANGES = "pending_changes";
    
    // Cache validity (1 hour)
    private static final long CACHE_VALIDITY_MS = 60 * 60 * 1000;
    
    private final Context context;
    private final SharedPreferences preferences;
    private final Gson gson;
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference customersRef;
    private final StorageReference storageRef;
    
    // Listeners
    public interface OnProfileLoadedListener {
        void onProfileLoaded(UserProfile profile);
        void onError(String error);
    }
    
    public interface OnProfileSavedListener {
        void onSuccess();
        void onError(String error);
    }
    
    public interface OnImageUploadListener {
        void onProgress(int progress);
        void onSuccess(String imageUrl);
        void onError(String error);
    }
    
    public interface OnPhoneVerificationListener {
        void onCodeSent(String verificationId);
        void onVerificationComplete();
        void onError(String error);
    }
    
    public interface OnStatsLoadedListener {
        void onStatsLoaded(int ordersCount, double totalSpent, long lastOrderAt);
        void onError(String error);
    }
    
    public CustomerProfileManager(Context context) {
        this.context = context;
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.customersRef = FirebaseDatabase.getInstance().getReference("customers");
        this.storageRef = FirebaseStorage.getInstance().getReference("customers");
    }
    
    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
    
    /**
     * Load profile with caching strategy
     * Load cached → Fetch remote → Update UI if changed
     */
    public void loadProfile(OnProfileLoadedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        // First load from cache for instant UI
        UserProfile cachedProfile = loadFromCache();
        if (cachedProfile != null) {
            listener.onProfileLoaded(cachedProfile);
        }
        
        // Then fetch from Firebase
        loadFromFirebase(userId, new OnProfileLoadedListener() {
            @Override
            public void onProfileLoaded(UserProfile profile) {
                // Cache the fresh data
                saveToCache(profile);
                // Update listener with fresh data
                listener.onProfileLoaded(profile);
            }
            
            @Override
            public void onError(String error) {
                // If we had cached data, we already delivered it
                if (cachedProfile == null) {
                    listener.onError(error);
                }
                Log.w(TAG, "Failed to load from Firebase, using cache: " + error);
            }
        });
    }
    
    /**
     * Force refresh from Firebase (bypass cache)
     */
    public void forceRefresh(OnProfileLoadedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        loadFromFirebase(userId, listener);
    }
    
    /**
     * Load profile from Firebase
     */
    private void loadFromFirebase(String userId, OnProfileLoadedListener listener) {
        customersRef.child(userId).child("profile")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        try {
                            UserProfile profile = snapshot.getValue(UserProfile.class);
                            if (profile != null) {
                                profile.setUserId(userId);
                                listener.onProfileLoaded(profile);
                            } else {
                                // Create default profile
                                UserProfile defaultProfile = createDefaultProfile(userId);
                                listener.onProfileLoaded(defaultProfile);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing profile", e);
                            listener.onError("Error parsing profile data");
                        }
                    } else {
                        // No profile exists, create default
                        UserProfile defaultProfile = createDefaultProfile(userId);
                        saveProfile(defaultProfile, new OnProfileSavedListener() {
                            @Override
                            public void onSuccess() {
                                listener.onProfileLoaded(defaultProfile);
                            }
                            
                            @Override
                            public void onError(String error) {
                                listener.onProfileLoaded(defaultProfile);
                            }
                        });
                    }
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onError(error.getMessage());
                }
            });
    }
    
    /**
     * Create default profile for new users
     */
    private UserProfile createDefaultProfile(String userId) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        
        if (user != null) {
            profile.setFullName(user.getDisplayName() != null ? user.getDisplayName() : "Food Van User");
            profile.setEmail(user.getEmail());
            profile.setPhoneNumber(user.getPhoneNumber());
            profile.setEmailVerified(user.isEmailVerified());
            profile.setPhoneVerified(user.getPhoneNumber() != null);
            
            if (user.getPhotoUrl() != null) {
                profile.setProfileImageUrl(user.getPhotoUrl().toString());
            }
        }
        
        profile.setCreatedAt(System.currentTimeMillis());
        profile.setLastUpdated(System.currentTimeMillis());
        
        return profile;
    }
    
    /**
     * Save profile to Firebase with local cache
     * Immediate local write → Push to backend asynchronously
     */
    public void saveProfile(UserProfile profile, OnProfileSavedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        profile.setLastUpdated(System.currentTimeMillis());
        
        // Immediate local cache
        saveToCache(profile);
        
        // Push to Firebase
        customersRef.child(userId).child("profile").setValue(profile)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Profile saved to Firebase");
                updateLastSyncTime();
                listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to save profile to Firebase", e);
                // Mark as pending for later sync
                markPendingChanges(true);
                listener.onError(e.getMessage());
            });
    }
    
    /**
     * Update specific profile fields
     */
    public void updateProfileFields(Map<String, Object> updates, OnProfileSavedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        updates.put("lastUpdated", System.currentTimeMillis());
        
        customersRef.child(userId).child("profile").updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Profile fields updated");
                updateLastSyncTime();
                listener.onSuccess();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to update profile fields", e);
                listener.onError(e.getMessage());
            });
    }
    
    /**
     * Upload profile image to Firebase Storage
     * Path: customers/{uid}/profile.jpg
     */
    public void uploadProfileImage(Uri imageUri, OnImageUploadListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        StorageReference imageRef = storageRef.child(userId).child("profile.jpg");
        
        UploadTask uploadTask = imageRef.putFile(imageUri);
        
        // Monitor progress
        uploadTask.addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            listener.onProgress((int) progress);
        });
        
        // Get download URL on success
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful() && task.getException() != null) {
                throw task.getException();
            }
            return imageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String downloadUrl = task.getResult().toString();
                
                // Update profile with new image URL
                Map<String, Object> updates = new HashMap<>();
                updates.put("profileImageUrl", downloadUrl);
                updateProfileFields(updates, new OnProfileSavedListener() {
                    @Override
                    public void onSuccess() {
                        listener.onSuccess(downloadUrl);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Image uploaded but profile update failed
                        listener.onSuccess(downloadUrl);
                    }
                });
            } else {
                listener.onError("Failed to upload image");
            }
        });
    }
    
    /**
     * Load user stats from orders collection
     */
    public void loadUserStats(OnStatsLoadedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        ordersRef.orderByChild("customerId").equalTo(userId)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int ordersCount = 0;
                    double totalSpent = 0;
                    long lastOrderAt = 0;
                    
                    for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                        ordersCount++;
                        
                        Double amount = orderSnapshot.child("totalAmount").getValue(Double.class);
                        if (amount != null) {
                            totalSpent += amount;
                        }
                        
                        Long timestamp = orderSnapshot.child("createdAt").getValue(Long.class);
                        if (timestamp != null && timestamp > lastOrderAt) {
                            lastOrderAt = timestamp;
                        }
                    }
                    
                    listener.onStatsLoaded(ordersCount, totalSpent, lastOrderAt);
                }
                
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onError(error.getMessage());
                }
            });
    }
    
    /**
     * Update privacy preferences
     */
    public void updatePrivacyPreference(String key, boolean value, OnProfileSavedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("preferences/" + key, value);
        updates.put("lastUpdated", System.currentTimeMillis());
        
        customersRef.child(userId).child("profile").updateChildren(updates)
            .addOnSuccessListener(aVoid -> listener.onSuccess())
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    
    /**
     * Logout - Clear local cache
     */
    public void clearCache() {
        preferences.edit().clear().apply();
        Log.d(TAG, "Profile cache cleared");
    }
    
    /**
     * Delete account (soft delete - mark as deleted)
     */
    public void deleteAccount(OnProfileSavedListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onError("User not authenticated");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("deleted", true);
        updates.put("deletedAt", System.currentTimeMillis());
        updates.put("isActive", false);
        
        customersRef.child(userId).updateChildren(updates)
            .addOnSuccessListener(aVoid -> {
                // Sign out user
                firebaseAuth.signOut();
                clearCache();
                listener.onSuccess();
            })
            .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }
    
    // ============ Cache Methods ============
    
    private void saveToCache(UserProfile profile) {
        try {
            String json = gson.toJson(profile);
            preferences.edit()
                .putString(KEY_PROFILE_DATA, json)
                .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
                .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving to cache", e);
        }
    }
    
    private UserProfile loadFromCache() {
        try {
            String json = preferences.getString(KEY_PROFILE_DATA, null);
            if (json != null) {
                return gson.fromJson(json, UserProfile.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading from cache", e);
        }
        return null;
    }
    
    public boolean isCacheValid() {
        long lastSync = preferences.getLong(KEY_LAST_SYNC, 0);
        return (System.currentTimeMillis() - lastSync) < CACHE_VALIDITY_MS;
    }
    
    private void updateLastSyncTime() {
        preferences.edit().putLong(KEY_LAST_SYNC, System.currentTimeMillis()).apply();
    }
    
    private void markPendingChanges(boolean hasPending) {
        preferences.edit().putBoolean(KEY_PENDING_CHANGES, hasPending).apply();
    }
    
    public boolean hasPendingChanges() {
        return preferences.getBoolean(KEY_PENDING_CHANGES, false);
    }
    
    public long getLastSyncTime() {
        return preferences.getLong(KEY_LAST_SYNC, 0);
    }
    
    /**
     * Sync pending changes (call from WorkManager)
     */
    public void syncPendingChanges(OnProfileSavedListener listener) {
        if (!hasPendingChanges()) {
            listener.onSuccess();
            return;
        }
        
        UserProfile cachedProfile = loadFromCache();
        if (cachedProfile != null) {
            saveProfile(cachedProfile, new OnProfileSavedListener() {
                @Override
                public void onSuccess() {
                    markPendingChanges(false);
                    listener.onSuccess();
                }
                
                @Override
                public void onError(String error) {
                    listener.onError(error);
                }
            });
        } else {
            markPendingChanges(false);
            listener.onSuccess();
        }
    }
    
    /**
     * Calculate membership tier based on points
     */
    public String calculateMembershipTier(int points) {
        if (points >= 10000) return "Platinum";
        if (points >= 5000) return "Gold";
        if (points >= 1000) return "Silver";
        return "Bronze";
    }
    
    /**
     * Calculate profile completion percentage
     */
    public int calculateProfileCompletion(UserProfile profile) {
        if (profile == null) return 0;
        
        int completed = 0;
        int total = 8;
        
        if (profile.getFullName() != null && !profile.getFullName().isEmpty()) completed++;
        if (profile.getEmail() != null && !profile.getEmail().isEmpty()) completed++;
        if (profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()) completed++;
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) completed++;
        if (profile.isEmailVerified()) completed++;
        if (profile.isPhoneVerified()) completed++;
        if (profile.getSavedAddresses() != null && !profile.getSavedAddresses().isEmpty()) completed++;
        if (profile.getDateOfBirth() != null && !profile.getDateOfBirth().isEmpty()) completed++;
        
        return (completed * 100) / total;
    }
}
