package com.example.foodvan.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.VendorProfile;
import com.example.foodvan.repositories.AccountRepository;

/**
 * ViewModel for Account Settings functionality
 * Handles all business logic for profile management, password changes, and account operations
 */
public class AccountSettingsViewModel extends AndroidViewModel {

    private final AccountRepository repository;
    
    // LiveData for UI state management
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<VendorProfile> userProfile = new MutableLiveData<>();

    public AccountSettingsViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountRepository(application);
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<VendorProfile> getUserProfile() {
        return userProfile;
    }

    /**
     * Load user profile data
     */
    public void loadUserProfile() {
        isLoading.setValue(true);
        
        repository.getCurrentUserProfile(new AccountRepository.OnProfileLoadListener() {
            @Override
            public void onSuccess(VendorProfile profile) {
                isLoading.postValue(false);
                userProfile.postValue(profile);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to load profile: " + error);
            }
        });
    }

    /**
     * Update user profile without password change
     */
    public void updateProfile(VendorProfile updatedProfile) {
        isLoading.setValue(true);
        
        repository.updateUserProfile(updatedProfile, new AccountRepository.OnProfileUpdateListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
                // Reload profile to get updated data
                loadUserProfile();
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to update profile: " + error);
            }
        });
    }

    /**
     * Update user profile with password change
     */
    public void updateProfileWithPassword(VendorProfile updatedProfile, String currentPassword, String newPassword) {
        isLoading.setValue(true);
        
        repository.updateProfileWithPassword(updatedProfile, currentPassword, newPassword, 
            new AccountRepository.OnProfileUpdateListener() {
                @Override
                public void onSuccess(String message) {
                    isLoading.postValue(false);
                    successMessage.postValue(message);
                    // Reload profile to get updated data
                    loadUserProfile();
                }

                @Override
                public void onError(String error) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Failed to update profile: " + error);
                }
            });
    }

    /**
     * Upload profile image
     */
    public void uploadProfileImage(Uri imageUri) {
        isLoading.setValue(true);
        
        repository.uploadProfileImage(imageUri, new AccountRepository.OnImageUploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                isLoading.postValue(false);
                successMessage.postValue("Profile photo updated successfully");
                // Reload profile to get updated image URL
                loadUserProfile();
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to upload image: " + error);
            }
        });
    }

    /**
     * Deactivate user account
     */
    public void deactivateAccount() {
        isLoading.setValue(true);
        
        repository.deactivateAccount(new AccountRepository.OnAccountOperationListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to deactivate account: " + error);
            }
        });
    }

    /**
     * Delete user account permanently
     */
    public void deleteAccount(String password) {
        isLoading.setValue(true);
        
        repository.deleteAccount(password, new AccountRepository.OnAccountOperationListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to delete account: " + error);
            }
        });
    }

    /**
     * Clear success and error messages
     */
    public void clearMessages() {
        successMessage.setValue(null);
        errorMessage.setValue(null);
    }
}
