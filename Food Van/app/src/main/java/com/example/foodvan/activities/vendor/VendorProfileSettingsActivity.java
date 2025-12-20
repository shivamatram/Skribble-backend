package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.foodvan.R;
import com.example.foodvan.adapters.ProfileSettingsAdapter;
import com.example.foodvan.models.ProfileSettingsItem;
import com.example.foodvan.models.Vendor;
import com.example.foodvan.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class VendorProfileSettingsActivity extends AppCompatActivity implements 
        ProfileSettingsAdapter.OnSettingsItemClickListener {

    // UI Components
    private MaterialToolbar toolbar;
    private ImageView ivProfileImage;
    private TextView tvBusinessName, tvBusinessType, tvVerificationStatus;
    private MaterialButton btnEditProfile, btnViewStats;
    private RecyclerView rvSettingsItems;
    
    // Data and Adapters
    private ProfileSettingsAdapter settingsAdapter;
    private List<ProfileSettingsItem> settingsItems;
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef;
    private String vendorId;
    private Vendor currentVendor;
    
    // Utils
    private SessionManager sessionManager;
    
    // Constants
    private static final int REQUEST_IMAGE_PICK = 1001;
    private static final int REQUEST_EDIT_PROFILE = 1002;
    private static final int REQUEST_ACCOUNT_INFO = 1003;
    private static final int REQUEST_CHANGE_PASSWORD = 1004;
    private static final int REQUEST_PAYMENT_SETTINGS = 1005;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_profile_settings);
        
        initializeComponents();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadVendorProfile();
        setupSettingsItems();
    }

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        vendorId = sessionManager.getUserId();
        
        if (vendorId != null) {
            vendorRef = FirebaseDatabase.getInstance().getReference("vendors").child(vendorId);
        }
        
        settingsItems = new ArrayList<>();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.iv_profile_image);
        tvBusinessName = findViewById(R.id.tv_business_name);
        tvBusinessType = findViewById(R.id.tv_business_type);
        tvVerificationStatus = findViewById(R.id.tv_verification_status);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnViewStats = findViewById(R.id.btn_view_stats);
        rvSettingsItems = findViewById(R.id.rv_settings_items);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Profile Settings");
        }
    }

    private void setupRecyclerView() {
        settingsAdapter = new ProfileSettingsAdapter(this, settingsItems, this);
        rvSettingsItems.setLayoutManager(new LinearLayoutManager(this));
        rvSettingsItems.setAdapter(settingsAdapter);
    }

    private void setupClickListeners() {
        ivProfileImage.setOnClickListener(v -> openImagePicker());
        btnEditProfile.setOnClickListener(v -> openEditProfile());
        btnViewStats.setOnClickListener(v -> openDashboardStats());
    }

    private void loadVendorProfile() {
        if (vendorRef == null) {
            showError("Error loading profile");
            return;
        }
        
        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentVendor = snapshot.getValue(Vendor.class);
                if (currentVendor != null) {
                    populateProfileHeader();
                } else {
                    showError("Profile not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Error loading profile: " + error.getMessage());
            }
        });
    }

    private void populateProfileHeader() {
        // Business name (using vanName from Vendor model)
        tvBusinessName.setText(currentVendor.getVanName());
        
        // Business type (using category from Vendor model)
        tvBusinessType.setText(currentVendor.getCategory() != null ? currentVendor.getCategory() : "Food Van");
        
        // Verification status (using verified field from Vendor model)
        if (currentVendor.isVerified()) {
            tvVerificationStatus.setText("✓ Verified");
            tvVerificationStatus.setTextColor(getColor(R.color.success_color));
        } else {
            tvVerificationStatus.setText("⚠ Pending Verification");
            tvVerificationStatus.setTextColor(getColor(R.color.warning_color));
        }
        
        // Profile image (using avatarUrl from Vendor model)
        if (!TextUtils.isEmpty(currentVendor.getAvatarUrl())) {
            Glide.with(this)
                    .load(currentVendor.getAvatarUrl())
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .into(ivProfileImage);
        } else {
            ivProfileImage.setImageResource(R.drawable.ic_person);
        }
    }

    private void setupSettingsItems() {
        settingsItems.clear();
        
        // Account Info Section
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_HEADER, 
                "Account Information", 
                "", 
                R.drawable.ic_account_circle
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Email Address", 
                getCurrentEmail(), 
                R.drawable.ic_email
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Phone Number", 
                getCurrentPhone(), 
                R.drawable.ic_phone
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Business Address", 
                "Manage saved addresses", 
                R.drawable.ic_location_on
        ));
        
        // Security Section
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_HEADER, 
                "Security", 
                "", 
                R.drawable.ic_security
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Change Password", 
                "Update your account password", 
                R.drawable.ic_lock
        ));
        
        // Payment Section
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_HEADER, 
                "Payment & Payout", 
                "", 
                R.drawable.ic_payment
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Bank Account", 
                "Manage payout methods", 
                R.drawable.ic_account_balance
        ));
        
        // Privacy Section
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_HEADER, 
                "Privacy & Notifications", 
                "", 
                R.drawable.ic_privacy_tip
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_TOGGLE, 
                "Show Profile to Customers", 
                "Allow customers to view your business profile", 
                R.drawable.ic_visibility
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_TOGGLE, 
                "Customer Messages", 
                "Allow customers to send you messages", 
                R.drawable.ic_message
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_TOGGLE, 
                "Order Notifications", 
                "Receive notifications for new orders", 
                R.drawable.ic_notifications
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_TOGGLE, 
                "Promotional Emails", 
                "Receive marketing and promotional content", 
                R.drawable.ic_email
        ));
        
        // Support Section
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_HEADER, 
                "Support", 
                "", 
                R.drawable.ic_help
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Help & Support", 
                "Get help with your account", 
                R.drawable.ic_help_outline
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_ITEM, 
                "Terms & Privacy", 
                "Review our policies", 
                R.drawable.ic_description
        ));
        
        // Danger Zone
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_HEADER, 
                "Account Actions", 
                "", 
                R.drawable.ic_warning
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_DANGER, 
                "Deactivate Account", 
                "Temporarily disable your account", 
                R.drawable.ic_pause_circle
        ));
        
        settingsItems.add(new ProfileSettingsItem(
                ProfileSettingsItem.TYPE_DANGER, 
                "Delete Account", 
                "Permanently delete your account", 
                R.drawable.ic_delete_forever
        ));
        
        settingsAdapter.notifyDataSetChanged();
    }

    private String getCurrentEmail() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getEmail() : "Not set";
    }

    private String getCurrentPhone() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null && user.getPhoneNumber() != null ? 
                user.getPhoneNumber() : "Not verified";
    }

    // ProfileSettingsAdapter.OnSettingsItemClickListener implementation
    @Override
    public void onSettingsItemClick(ProfileSettingsItem item) {
        switch (item.getTitle()) {
            case "Email Address":
                openAccountInfo("email");
                break;
            case "Phone Number":
                openAccountInfo("phone");
                break;
            case "Business Address":
                openBusinessAddress();
                break;
            case "Change Password":
                openChangePassword();
                break;
            case "Bank Account":
                openPaymentSettings();
                break;
            case "Help & Support":
                openHelpSupport();
                break;
            case "Terms & Privacy":
                openTermsPrivacy();
                break;
            case "Deactivate Account":
                showDeactivateDialog();
                break;
            case "Delete Account":
                showDeleteDialog();
                break;
        }
    }

    @Override
    public void onToggleChanged(ProfileSettingsItem item, boolean isChecked) {
        // Handle privacy toggle changes
        updatePrivacySetting(item.getTitle(), isChecked);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    private void openEditProfile() {
        // TODO: Create EditProfileActivity
        showToast("Edit Profile - Coming Soon!");
    }

    private void openDashboardStats() {
        Intent intent = new Intent(this, VendorAnalyticsActivity.class);
        startActivity(intent);
    }

    private void openAccountInfo(String field) {
        // TODO: Create AccountInfoActivity
        showToast("Account Info (" + field + ") - Coming Soon!");
    }

    private void openBusinessAddress() {
        // TODO: Create BusinessAddressActivity
        showToast("Business Address - Coming Soon!");
    }

    private void openChangePassword() {
        // TODO: Create ChangePasswordActivity
        showToast("Change Password - Coming Soon!");
    }

    private void openPaymentSettings() {
        // TODO: Create PaymentSettingsActivity
        showToast("Payment Settings - Coming Soon!");
    }

    private void openHelpSupport() {
        // TODO: Create HelpSupportActivity
        showToast("Help & Support - Coming Soon!");
    }

    private void openTermsPrivacy() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("https://foodvan.app/terms"));
        startActivity(intent);
    }

    private void updatePrivacySetting(String setting, boolean value) {
        if (vendorRef == null) return;
        
        String key = getPrivacyKey(setting);
        vendorRef.child("privacy").child(key).setValue(value)
                .addOnSuccessListener(aVoid -> {
                    showToast("Setting updated");
                })
                .addOnFailureListener(e -> {
                    showError("Failed to update setting");
                });
    }

    private String getPrivacyKey(String setting) {
        switch (setting) {
            case "Show Profile to Customers": return "showProfile";
            case "Customer Messages": return "allowMessages";
            case "Order Notifications": return "orderNotifications";
            case "Promotional Emails": return "promotionalEmails";
            default: return "";
        }
    }

    private void showDeactivateDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Deactivate Account")
                .setMessage("Your account will be temporarily disabled. You can reactivate it anytime by logging in.")
                .setPositiveButton("Deactivate", (dialog, which) -> {
                    // TODO: Implement account deactivation
                    showToast("Account deactivation - Coming Soon");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Account")
                .setMessage("This action cannot be undone. All your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement account deletion with re-authentication
                    showToast("Account deletion - Coming Soon");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_PICK:
                    if (data != null && data.getData() != null) {
                        // TODO: Handle image upload
                        showToast("Image upload - Coming Soon");
                    }
                    break;
                case REQUEST_EDIT_PROFILE:
                case REQUEST_ACCOUNT_INFO:
                case REQUEST_CHANGE_PASSWORD:
                case REQUEST_PAYMENT_SETTINGS:
                    // Refresh profile data
                    loadVendorProfile();
                    setupSettingsItems();
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
