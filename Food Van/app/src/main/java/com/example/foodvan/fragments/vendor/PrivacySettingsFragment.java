package com.example.foodvan.fragments.vendor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodvan.R;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Privacy Settings Fragment with Material Design 3
 * Manages vendor privacy preferences and customer interaction settings
 */
public class PrivacySettingsFragment extends Fragment {

    private static final String TAG = "PrivacySettingsFragment";

    // UI Components
    private SwitchMaterial switchProfileVisibility;
    private SwitchMaterial switchContactInfo;
    private SwitchMaterial switchAllowReviews;
    private SwitchMaterial switchAllowMessages;
    private MaterialButton btnSavePrivacySettings;

    // Data & Services
    private SessionManager sessionManager;
    private DatabaseReference vendorRef;
    private DatabaseReference privacySettingsRef;

    // Privacy settings data
    private Map<String, Object> privacySettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_privacy_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeComponents();
        initializeViews(view);
        setupClickListeners();
        loadPrivacySettings();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(requireContext());
        privacySettings = new HashMap<>();

        String vendorId = sessionManager.getUserId();
        if (vendorId != null) {
            vendorRef = FirebaseDatabase.getInstance().getReference("vendors").child(vendorId);
            privacySettingsRef = FirebaseDatabase.getInstance().getReference("vendor_privacy_settings").child(vendorId);
        }
    }

    private void initializeViews(View view) {
        switchProfileVisibility = view.findViewById(R.id.switch_profile_visibility);
        switchContactInfo = view.findViewById(R.id.switch_contact_info);
        switchAllowReviews = view.findViewById(R.id.switch_allow_reviews);
        switchAllowMessages = view.findViewById(R.id.switch_allow_messages);
        btnSavePrivacySettings = view.findViewById(R.id.btn_save_privacy_settings);
    }

    private void setupClickListeners() {
        btnSavePrivacySettings.setOnClickListener(v -> savePrivacySettings());

        // Add listeners to switches for immediate feedback
        switchProfileVisibility.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showInfo("Your profile will be visible to customers");
            } else {
                showInfo("Your profile will be hidden from customers");
            }
        });

        switchContactInfo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showInfo("Contact information will be displayed to customers");
            } else {
                showInfo("Contact information will be hidden from customers");
            }
        });

        switchAllowReviews.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showInfo("Customers can leave reviews on your vendor page");
            } else {
                showInfo("Reviews will be disabled on your vendor page");
            }
        });

        switchAllowMessages.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showInfo("You will receive direct messages from customers");
            } else {
                showInfo("Direct messages from customers will be blocked");
            }
        });
    }

    private void loadPrivacySettings() {
        if (privacySettingsRef == null) {
            setDefaultSettings();
            return;
        }

        privacySettingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Load existing settings
                    Boolean profileVisibility = snapshot.child("profileVisibility").getValue(Boolean.class);
                    Boolean contactInfo = snapshot.child("contactInfo").getValue(Boolean.class);
                    Boolean allowReviews = snapshot.child("allowReviews").getValue(Boolean.class);
                    Boolean allowMessages = snapshot.child("allowMessages").getValue(Boolean.class);

                    switchProfileVisibility.setChecked(profileVisibility != null ? profileVisibility : true);
                    switchContactInfo.setChecked(contactInfo != null ? contactInfo : false);
                    switchAllowReviews.setChecked(allowReviews != null ? allowReviews : true);
                    switchAllowMessages.setChecked(allowMessages != null ? allowMessages : false);
                } else {
                    setDefaultSettings();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load privacy settings: " + error.getMessage());
                setDefaultSettings();
            }
        });
    }

    private void setDefaultSettings() {
        // Set default privacy settings
        switchProfileVisibility.setChecked(true);  // Profile visible by default
        switchContactInfo.setChecked(false);       // Contact info hidden by default
        switchAllowReviews.setChecked(true);       // Reviews allowed by default
        switchAllowMessages.setChecked(false);     // Messages blocked by default
    }

    private void savePrivacySettings() {
        // Collect current switch states
        privacySettings.put("profileVisibility", switchProfileVisibility.isChecked());
        privacySettings.put("contactInfo", switchContactInfo.isChecked());
        privacySettings.put("allowReviews", switchAllowReviews.isChecked());
        privacySettings.put("allowMessages", switchAllowMessages.isChecked());
        privacySettings.put("lastUpdated", System.currentTimeMillis());

        if (privacySettingsRef == null) {
            showError("Unable to save settings - user not authenticated");
            return;
        }

        btnSavePrivacySettings.setEnabled(false);
        btnSavePrivacySettings.setText("Saving...");

        privacySettingsRef.setValue(privacySettings)
            .addOnSuccessListener(aVoid -> {
                showSuccess("Privacy settings saved successfully");
                btnSavePrivacySettings.setEnabled(true);
                btnSavePrivacySettings.setText("Save Privacy Settings");
                
                // Also update vendor profile with visibility setting
                updateVendorProfileVisibility();
            })
            .addOnFailureListener(e -> {
                showError("Failed to save privacy settings: " + e.getMessage());
                btnSavePrivacySettings.setEnabled(true);
                btnSavePrivacySettings.setText("Save Privacy Settings");
            });
    }

    private void updateVendorProfileVisibility() {
        if (vendorRef != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("profileVisible", switchProfileVisibility.isChecked());
            updates.put("contactInfoVisible", switchContactInfo.isChecked());
            updates.put("reviewsEnabled", switchAllowReviews.isChecked());
            updates.put("messagesEnabled", switchAllowMessages.isChecked());

            vendorRef.updateChildren(updates);
        }
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.success_color))
                .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(requireContext().getColor(R.color.error_color))
                .show();
        }
    }

    private void showInfo(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.info_color))
                .show();
        }
    }
}
