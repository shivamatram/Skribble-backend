package com.example.foodvan.fragments.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodvan.R;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Vendor Privacy Settings Fragment
 * Handles privacy controls, data management, and security settings
 */
public class VendorPrivacySettingsFragment extends Fragment {

    private static final String TAG = "VendorPrivacySettings";

    // UI Components - Privacy Controls
    private SwitchMaterial switchProfileVisibility;
    private SwitchMaterial switchReviewsVisibility;
    private SwitchMaterial switchLocationTracking;
    private SwitchMaterial switchNotifications;

    // UI Components - Data Management
    private LinearLayout layoutManagePermissions;
    private LinearLayout layoutDataSharing;
    private LinearLayout layoutPrivacyPolicy;

    // UI Components - Danger Zone
    private LinearLayout layoutClearData;
    private LinearLayout layoutResetSettings;

    // UI Components - Actions
    private MaterialButton btnSavePrivacySettings;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef;
    private SessionManager sessionManager;

    // Privacy Settings Data
    private Map<String, Object> privacySettings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vendor_privacy_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeComponents(view);
        setupClickListeners();
        loadPrivacySettings();
    }

    private void initializeComponents(View view) {
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(requireContext());
        
        String vendorId = sessionManager.getUserId();
        if (vendorId != null) {
            vendorRef = FirebaseDatabase.getInstance().getReference("vendors")
                    .child(vendorId).child("privacySettings");
        }

        // Initialize Privacy Controls
        switchProfileVisibility = view.findViewById(R.id.switch_profile_visibility);
        switchReviewsVisibility = view.findViewById(R.id.switch_reviews_visibility);
        switchLocationTracking = view.findViewById(R.id.switch_location_tracking);
        switchNotifications = view.findViewById(R.id.switch_notifications);

        // Initialize Data Management
        layoutManagePermissions = view.findViewById(R.id.layout_manage_permissions);
        layoutDataSharing = view.findViewById(R.id.layout_data_sharing);
        layoutPrivacyPolicy = view.findViewById(R.id.layout_privacy_policy);

        // Initialize Danger Zone
        layoutClearData = view.findViewById(R.id.layout_clear_data);
        layoutResetSettings = view.findViewById(R.id.layout_reset_settings);

        // Initialize Actions
        btnSavePrivacySettings = view.findViewById(R.id.btn_save_privacy_settings);

        // Initialize privacy settings map
        privacySettings = new HashMap<>();
    }

    private void setupClickListeners() {
        // Data Management Click Listeners
        if (layoutManagePermissions != null) {
            layoutManagePermissions.setOnClickListener(v -> openManagePermissions());
        }
        
        if (layoutDataSharing != null) {
            layoutDataSharing.setOnClickListener(v -> openDataSharingPreferences());
        }
        
        if (layoutPrivacyPolicy != null) {
            layoutPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        }

        // Danger Zone Click Listeners
        if (layoutClearData != null) {
            layoutClearData.setOnClickListener(v -> showClearDataDialog());
        }
        
        if (layoutResetSettings != null) {
            layoutResetSettings.setOnClickListener(v -> showResetSettingsDialog());
        }

        // Save Button Click Listener
        if (btnSavePrivacySettings != null) {
            btnSavePrivacySettings.setOnClickListener(v -> savePrivacySettings());
        }
    }

    private void loadPrivacySettings() {
        if (vendorRef == null) {
            // Set default values if no Firebase connection
            setDefaultPrivacySettings();
            return;
        }

        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load existing settings
                    Boolean profileVisibility = snapshot.child("profileVisibility").getValue(Boolean.class);
                    Boolean reviewsVisibility = snapshot.child("reviewsVisibility").getValue(Boolean.class);
                    Boolean locationTracking = snapshot.child("locationTracking").getValue(Boolean.class);
                    Boolean notifications = snapshot.child("notifications").getValue(Boolean.class);

                    // Apply settings to switches
                    if (switchProfileVisibility != null) {
                        switchProfileVisibility.setChecked(profileVisibility != null ? profileVisibility : true);
                    }
                    if (switchReviewsVisibility != null) {
                        switchReviewsVisibility.setChecked(reviewsVisibility != null ? reviewsVisibility : true);
                    }
                    if (switchLocationTracking != null) {
                        switchLocationTracking.setChecked(locationTracking != null ? locationTracking : true);
                    }
                    if (switchNotifications != null) {
                        switchNotifications.setChecked(notifications != null ? notifications : true);
                    }
                } else {
                    // Set default values for new vendor
                    setDefaultPrivacySettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load privacy settings: " + error.getMessage());
                setDefaultPrivacySettings();
            }
        });
    }

    private void setDefaultPrivacySettings() {
        if (switchProfileVisibility != null) {
            switchProfileVisibility.setChecked(true);
        }
        if (switchReviewsVisibility != null) {
            switchReviewsVisibility.setChecked(true);
        }
        if (switchLocationTracking != null) {
            switchLocationTracking.setChecked(true);
        }
        if (switchNotifications != null) {
            switchNotifications.setChecked(true);
        }
    }

    private void openManagePermissions() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Manage Permissions")
                .setMessage("This feature allows you to manage app permissions:\n\n" +
                        "• Location: For delivery tracking\n" +
                        "• Storage: For photo uploads\n" +
                        "• Camera: For taking photos\n\n" +
                        "You can manage these in your device settings.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + requireContext().getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        showError("Unable to open settings");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openDataSharingPreferences() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Data Sharing Preferences")
                .setMessage("Configure how your data is used:\n\n" +
                        "• Analytics: Help improve app performance\n" +
                        "• Personalization: Customize your experience\n" +
                        "• Marketing: Receive relevant offers\n\n" +
                        "Your privacy is important to us. All data is handled securely.")
                .setPositiveButton("Configure", (dialog, which) -> {
                    showInfo("Data sharing preferences saved");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openPrivacyPolicy() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Privacy Policy")
                .setMessage("View our comprehensive privacy policy to understand:\n\n" +
                        "• What data we collect\n" +
                        "• How we use your information\n" +
                        "• Your rights and choices\n" +
                        "• Security measures\n\n" +
                        "Would you like to view the full privacy policy?")
                .setPositiveButton("View Policy", (dialog, which) -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://foodvan.com/privacy-policy"));
                        startActivity(intent);
                    } catch (Exception e) {
                        showError("Unable to open privacy policy");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showClearDataDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Clear App Data")
                .setMessage("This will remove all app-specific data including:\n\n" +
                        "• Cached images and files\n" +
                        "• Temporary data\n" +
                        "• App preferences\n\n" +
                        "Your account data will remain safe. This action cannot be undone.")
                .setPositiveButton("Clear Data", (dialog, which) -> {
                    clearAppData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showResetSettingsDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Reset Settings")
                .setMessage("This will restore all privacy settings to their default values:\n\n" +
                        "• Profile visibility: ON\n" +
                        "• Reviews visibility: ON\n" +
                        "• Location tracking: ON\n" +
                        "• Notifications: ON\n\n" +
                        "Are you sure you want to continue?")
                .setPositiveButton("Reset", (dialog, which) -> {
                    resetToDefaultSettings();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void clearAppData() {
        try {
            // Clear app cache and temporary data
            requireContext().getCacheDir().delete();
            showSuccess("App data cleared successfully");
        } catch (Exception e) {
            showError("Failed to clear app data: " + e.getMessage());
        }
    }

    private void resetToDefaultSettings() {
        // Reset all switches to default (true)
        setDefaultPrivacySettings();
        
        // Save default settings to Firebase
        savePrivacySettings();
        
        showSuccess("Privacy settings reset to default");
    }

    private void savePrivacySettings() {
        if (vendorRef == null) {
            showError("Unable to save privacy settings");
            return;
        }

        // Disable save button during save
        if (btnSavePrivacySettings != null) {
            btnSavePrivacySettings.setEnabled(false);
            btnSavePrivacySettings.setText("Saving...");
        }

        // Collect current switch states
        privacySettings.clear();
        privacySettings.put("profileVisibility", switchProfileVisibility != null ? switchProfileVisibility.isChecked() : true);
        privacySettings.put("reviewsVisibility", switchReviewsVisibility != null ? switchReviewsVisibility.isChecked() : true);
        privacySettings.put("locationTracking", switchLocationTracking != null ? switchLocationTracking.isChecked() : true);
        privacySettings.put("notifications", switchNotifications != null ? switchNotifications.isChecked() : true);
        privacySettings.put("updatedAt", System.currentTimeMillis());

        // Save to Firebase
        vendorRef.setValue(privacySettings)
                .addOnSuccessListener(aVoid -> {
                    showSuccess("Privacy settings saved successfully");
                    // Re-enable save button
                    if (btnSavePrivacySettings != null) {
                        btnSavePrivacySettings.setEnabled(true);
                        btnSavePrivacySettings.setText("Save Privacy Settings");
                    }
                })
                .addOnFailureListener(e -> {
                    showError("Failed to save privacy settings: " + e.getMessage());
                    // Re-enable save button
                    if (btnSavePrivacySettings != null) {
                        btnSavePrivacySettings.setEnabled(true);
                        btnSavePrivacySettings.setText("Save Privacy Settings");
                    }
                });
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.green_500, null))
                    .show();
        }
    }

    private void showInfo(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(getResources().getColor(R.color.primary, null))
                    .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getResources().getColor(R.color.red_500, null))
                    .show();
        } else {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
