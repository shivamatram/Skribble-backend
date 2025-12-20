package com.example.foodvan.repositories;

import android.content.Context;
import android.net.Uri;

import com.example.foodvan.models.VendorProfile;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository for Account Settings operations
 * Handles Firebase Authentication and Firestore operations for vendor profiles
 */
public class AccountRepository {

    private final Context context;
    private final FirebaseAuth auth;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;

    // Collection names
    private static final String VENDORS_COLLECTION = "vendors";
    private static final String PROFILE_IMAGES_PATH = "profile_images";

    public AccountRepository(Context context) {
        this.context = context;
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
    }

    // Listener interfaces
    public interface OnProfileLoadListener {
        void onSuccess(VendorProfile profile);
        void onError(String error);
    }

    public interface OnProfileUpdateListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnImageUploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public interface OnAccountOperationListener {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Get current user profile from Firestore
     */
    public void getCurrentUserProfile(OnProfileLoadListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        
        db.collection(VENDORS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        VendorProfile profile = documentSnapshot.toObject(VendorProfile.class);
                        if (profile != null) {
                            profile.setVendorId(userId);
                            listener.onSuccess(profile);
                        } else {
                            // Create default profile if doesn't exist
                            createDefaultProfile(currentUser, listener);
                        }
                    } else {
                        // Create default profile if document doesn't exist
                        createDefaultProfile(currentUser, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError(e.getMessage());
                });
    }

    /**
     * Create default profile for new users
     */
    private void createDefaultProfile(FirebaseUser user, OnProfileLoadListener listener) {
        VendorProfile defaultProfile = new VendorProfile();
        defaultProfile.setVendorId(user.getUid());
        defaultProfile.setEmail(user.getEmail() != null ? user.getEmail() : "");
        defaultProfile.setVendorName(user.getDisplayName() != null ? user.getDisplayName() : "");
        defaultProfile.setPhone(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        
        // Save default profile to Firestore
        db.collection(VENDORS_COLLECTION)
                .document(user.getUid())
                .set(defaultProfile)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(defaultProfile);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to create profile: " + e.getMessage());
                });
    }

    /**
     * Update user profile without password change
     */
    public void updateUserProfile(VendorProfile updatedProfile, OnProfileUpdateListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        updatedProfile.setVendorId(userId);
        updatedProfile.setUpdatedAt(System.currentTimeMillis());

        // Check if email needs to be updated in Firebase Auth
        String newEmail = updatedProfile.getEmail();
        String currentEmail = currentUser.getEmail();
        
        if (newEmail != null && !newEmail.equals(currentEmail)) {
            // Update email in Firebase Auth first
            currentUser.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        // Update profile in Firestore after successful email update
                        updateProfileInFirestore(userId, updatedProfile, listener);
                    })
                    .addOnFailureListener(e -> {
                        listener.onError("Failed to update email: " + e.getMessage());
                    });
        } else {
            // Update profile in Firestore
            updateProfileInFirestore(userId, updatedProfile, listener);
        }
    }

    /**
     * Update user profile with password change
     */
    public void updateProfileWithPassword(VendorProfile updatedProfile, String currentPassword, 
                                        String newPassword, OnProfileUpdateListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }

        String currentEmail = currentUser.getEmail();
        if (currentEmail == null) {
            listener.onError("Current email not found");
            return;
        }

        // Re-authenticate user with current password
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, currentPassword);
        
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Update password if provided
                    if (newPassword != null && !newPassword.isEmpty()) {
                        currentUser.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid1 -> {
                                    // Update email if changed
                                    updateEmailIfChanged(currentUser, updatedProfile, listener);
                                })
                                .addOnFailureListener(e -> {
                                    listener.onError("Failed to update password: " + e.getMessage());
                                });
                    } else {
                        // Update email if changed
                        updateEmailIfChanged(currentUser, updatedProfile, listener);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Current password is incorrect");
                });
    }

    /**
     * Update email if it has changed
     */
    private void updateEmailIfChanged(FirebaseUser user, VendorProfile updatedProfile, 
                                    OnProfileUpdateListener listener) {
        String newEmail = updatedProfile.getEmail();
        String currentEmail = user.getEmail();
        
        if (newEmail != null && !newEmail.equals(currentEmail)) {
            user.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        // Update profile in Firestore
                        updateProfileInFirestore(user.getUid(), updatedProfile, listener);
                    })
                    .addOnFailureListener(e -> {
                        listener.onError("Failed to update email: " + e.getMessage());
                    });
        } else {
            // Update profile in Firestore
            updateProfileInFirestore(user.getUid(), updatedProfile, listener);
        }
    }

    /**
     * Update profile in Firestore
     */
    private void updateProfileInFirestore(String userId, VendorProfile updatedProfile, 
                                        OnProfileUpdateListener listener) {
        db.collection(VENDORS_COLLECTION)
                .document(userId)
                .set(updatedProfile)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Profile updated successfully");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to update profile: " + e.getMessage());
                });
    }

    /**
     * Upload profile image to Firebase Storage
     */
    public void uploadProfileImage(Uri imageUri, OnImageUploadListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + ".jpg";
        
        StorageReference imageRef = storage.getReference()
                .child(PROFILE_IMAGES_PATH)
                .child(fileName);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get download URL
                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                // Update profile with new image URL
                                updateProfileImageUrl(userId, downloadUri.toString(), listener);
                            })
                            .addOnFailureListener(e -> {
                                listener.onError("Failed to get image URL: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to upload image: " + e.getMessage());
                });
    }

    /**
     * Update profile image URL in Firestore
     */
    private void updateProfileImageUrl(String userId, String imageUrl, OnImageUploadListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", imageUrl);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(VENDORS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess(imageUrl);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to update profile image: " + e.getMessage());
                });
    }

    /**
     * Deactivate user account
     */
    public void deactivateAccount(OnAccountOperationListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }

        String userId = currentUser.getUid();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("isActive", false);
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection(VENDORS_COLLECTION)
                .document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Sign out user
                    auth.signOut();
                    listener.onSuccess("Account deactivated successfully");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to deactivate account: " + e.getMessage());
                });
    }

    /**
     * Delete user account permanently
     */
    public void deleteAccount(String password, OnAccountOperationListener listener) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            listener.onError("User not authenticated");
            return;
        }

        String currentEmail = currentUser.getEmail();
        if (currentEmail == null) {
            listener.onError("Current email not found");
            return;
        }

        String userId = currentUser.getUid();

        // Re-authenticate user
        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail, password);
        
        currentUser.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    // Delete user data from Firestore
                    db.collection(VENDORS_COLLECTION)
                            .document(userId)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                // Delete user account from Firebase Auth
                                currentUser.delete()
                                        .addOnSuccessListener(aVoid2 -> {
                                            listener.onSuccess("Account deleted permanently");
                                        })
                                        .addOnFailureListener(e -> {
                                            listener.onError("Failed to delete account: " + e.getMessage());
                                        });
                            })
                            .addOnFailureListener(e -> {
                                listener.onError("Failed to delete user data: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    listener.onError("Password is incorrect");
                });
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isUserAuthenticated() {
        return auth.getCurrentUser() != null;
    }
}
