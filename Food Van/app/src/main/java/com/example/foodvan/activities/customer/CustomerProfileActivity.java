package com.example.foodvan.activities.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.models.UserProfile;
import com.example.foodvan.utils.CustomerProfileManager;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * CustomerProfileActivity - Comprehensive Customer Profile Management
 * 
 * Features:
 * - Profile header with photo, name, membership level, loyalty points
 * - Quick stats (orders, spent, reviews)
 * - Profile completion tracking
 * - Account management (personal info, phone verification, addresses, payments)
 * - Orders section (history, favorites, reviews)
 * - Privacy toggles
 * - Settings & support links
 * - Account actions (change password, logout, delete)
 * - Firebase backend integration with local caching
 */
public class CustomerProfileActivity extends AppCompatActivity {

    private static final String TAG = "CustomerProfileActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1001;
    private static final int REQUEST_IMAGE_GALLERY = 1002;
    private static final int REQUEST_CAMERA_PERMISSION = 1003;
    private static final int REQUEST_STORAGE_PERMISSION = 1004;
    private static final int REQUEST_EDIT_PROFILE = 1005;

    // UI Components - Header
    private MaterialToolbar toolbar;
    private ShapeableImageView ivProfilePicture;
    private FloatingActionButton fabEditPicture;
    private TextView tvUserName, tvUserEmail, tvMembershipLevel, tvLoyaltyPoints;
    private ImageView ivEditProfile;

    // UI Components - Stats
    private TextView tvTotalOrders, tvTotalSpent, tvReviewsCount;

    // UI Components - Profile Completion
    private View cardProfileCompletion;
    private TextView tvCompletionPercentage, tvCompletionHint;
    private LinearProgressIndicator progressCompletion;

    // UI Components - Menu Items
    private View itemPersonalInfo, itemPhoneVerification, itemAddresses, itemPaymentMethods;
    private View itemOrderHistory, itemFavorites, itemReviews;
    private View itemSettings, itemHelp, itemAbout;
    private LinearLayout itemChangePassword, itemLogout, itemDeleteAccount;

    // UI Components - Privacy Toggles
    private MaterialSwitch switchShowPhone, switchMarketing;

    // UI Components - Footer & Loading
    private TextView tvLastSynced;
    private FrameLayout loadingOverlay;
    private View rootLayout;

    // Data & Services
    private CustomerProfileManager profileManager;
    private SessionManager sessionManager;
    private UserProfile currentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_profile);

        setupStatusBar();
        initializeServices();
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadProfile();
    }

    private void initializeServices() {
        profileManager = new CustomerProfileManager(this);
        sessionManager = new SessionManager(this);
    }

    /**
     * Setup orange status bar to match the navbar theme with seamless connection
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_color, getTheme()));
            
            // Make status bar icons light (white) for dark orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0); // Clear light status bar flag
            }
            
            // Ensure proper spacing - don't fit system windows
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            }
        }
    }

    private void initializeViews() {
        rootLayout = findViewById(R.id.root_layout);
        toolbar = findViewById(R.id.toolbar);
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Header
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        fabEditPicture = findViewById(R.id.fab_edit_picture);
        tvUserName = findViewById(R.id.tv_user_name);
        tvUserEmail = findViewById(R.id.tv_user_email);
        tvMembershipLevel = findViewById(R.id.tv_membership_level);
        tvLoyaltyPoints = findViewById(R.id.tv_loyalty_points);
        ivEditProfile = findViewById(R.id.iv_edit_profile);

        // Stats
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalSpent = findViewById(R.id.tv_total_spent);
        tvReviewsCount = findViewById(R.id.tv_reviews_count);

        // Profile Completion
        cardProfileCompletion = findViewById(R.id.card_profile_completion);
        tvCompletionPercentage = findViewById(R.id.tv_completion_percentage);
        tvCompletionHint = findViewById(R.id.tv_completion_hint);
        progressCompletion = findViewById(R.id.progress_completion);

        // Menu Items - Account
        itemPersonalInfo = findViewById(R.id.item_personal_info);
        itemPhoneVerification = findViewById(R.id.item_phone_verification);
        itemAddresses = findViewById(R.id.item_addresses);
        itemPaymentMethods = findViewById(R.id.item_payment_methods);

        // Menu Items - Orders
        itemOrderHistory = findViewById(R.id.item_order_history);
        itemFavorites = findViewById(R.id.item_favorites);
        itemReviews = findViewById(R.id.item_reviews);

        // Menu Items - More
        itemSettings = findViewById(R.id.item_settings);
        itemHelp = findViewById(R.id.item_help);
        itemAbout = findViewById(R.id.item_about);

        // Account Actions
        itemChangePassword = findViewById(R.id.item_change_password);
        itemLogout = findViewById(R.id.item_logout);
        itemDeleteAccount = findViewById(R.id.item_delete_account);

        // Privacy Toggles
        switchShowPhone = findViewById(R.id.switch_show_phone);
        switchMarketing = findViewById(R.id.switch_marketing);

        // Footer
        tvLastSynced = findViewById(R.id.tv_last_synced);

        // Setup menu item content
        setupMenuItems();
    }

    private void setupMenuItems() {
        // Account section
        setupMenuItem(itemPersonalInfo, R.drawable.ic_person, 
            getString(R.string.personal_information_title), 
            getString(R.string.personal_info_subtitle));
        
        setupMenuItem(itemPhoneVerification, R.drawable.ic_phone, 
            getString(R.string.phone_verification_title), 
            getString(R.string.phone_verification_subtitle));
        
        setupMenuItem(itemAddresses, R.drawable.ic_location_on, 
            getString(R.string.saved_addresses_title), 
            getString(R.string.addresses_subtitle));
        
        setupMenuItem(itemPaymentMethods, R.drawable.ic_payment, 
            getString(R.string.payment_methods_title), 
            getString(R.string.payment_methods_subtitle));

        // Orders section
        setupMenuItem(itemOrderHistory, R.drawable.ic_receipt, 
            getString(R.string.order_history_title), 
            getString(R.string.order_history_subtitle));
        
        setupMenuItem(itemFavorites, R.drawable.ic_favorite, 
            getString(R.string.favorites_title), 
            getString(R.string.favorites_subtitle));
        
        setupMenuItem(itemReviews, R.drawable.ic_star, 
            getString(R.string.reviews_ratings_title), 
            getString(R.string.reviews_subtitle));

        // More section
        setupMenuItem(itemSettings, R.drawable.ic_settings, 
            getString(R.string.settings_title), 
            getString(R.string.settings_subtitle));
        
        setupMenuItem(itemHelp, R.drawable.ic_help, 
            getString(R.string.help_support_title), 
            getString(R.string.help_subtitle));
        
        setupMenuItem(itemAbout, R.drawable.ic_info, 
            getString(R.string.about_title), 
            getString(R.string.about_subtitle));
    }

    private void setupMenuItem(View item, int iconRes, String title, String subtitle) {
        if (item == null) return;
        
        ImageView icon = item.findViewById(R.id.iv_icon);
        TextView tvTitle = item.findViewById(R.id.tv_title);
        TextView tvSubtitle = item.findViewById(R.id.tv_subtitle);
        
        if (icon != null) icon.setImageResource(iconRes);
        if (tvTitle != null) tvTitle.setText(title);
        if (tvSubtitle != null) {
            tvSubtitle.setText(subtitle);
            tvSubtitle.setVisibility(View.VISIBLE);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Profile header
        fabEditPicture.setOnClickListener(v -> showImagePickerDialog());
        ivEditProfile.setOnClickListener(v -> openEditProfile());

        // Account section
        if (itemPersonalInfo != null) itemPersonalInfo.setOnClickListener(v -> openEditProfile());
        if (itemPhoneVerification != null) itemPhoneVerification.setOnClickListener(v -> openPhoneVerification());
        if (itemAddresses != null) itemAddresses.setOnClickListener(v -> openAddresses());
        if (itemPaymentMethods != null) itemPaymentMethods.setOnClickListener(v -> openPaymentMethods());

        // Orders section
        if (itemOrderHistory != null) itemOrderHistory.setOnClickListener(v -> openOrderHistory());
        if (itemFavorites != null) itemFavorites.setOnClickListener(v -> openFavorites());
        if (itemReviews != null) itemReviews.setOnClickListener(v -> openReviews());

        // More section
        if (itemSettings != null) itemSettings.setOnClickListener(v -> openSettings());
        if (itemHelp != null) itemHelp.setOnClickListener(v -> openHelp());
        if (itemAbout != null) itemAbout.setOnClickListener(v -> openAbout());

        // Account actions
        itemChangePassword.setOnClickListener(v -> openChangePassword());
        itemLogout.setOnClickListener(v -> showLogoutDialog());
        itemDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());

        // Privacy toggles
        switchShowPhone.setOnCheckedChangeListener((buttonView, isChecked) -> 
            updatePrivacySetting("showPhoneOnReceipt", isChecked));
        switchMarketing.setOnCheckedChangeListener((buttonView, isChecked) -> 
            updatePrivacySetting("marketingEnabled", isChecked));

        // Profile completion card
        if (cardProfileCompletion != null) {
            cardProfileCompletion.setOnClickListener(v -> openEditProfile());
        }
    }

    // ============ Profile Loading ============

    private void loadProfile() {
        showLoading(true);
        
        profileManager.loadProfile(new CustomerProfileManager.OnProfileLoadedListener() {
            @Override
            public void onProfileLoaded(UserProfile profile) {
                runOnUiThread(() -> {
                    currentProfile = profile;
                    updateUI();
                    loadStats();
                    updateLastSyncedTime();
                    showLoading(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to load profile: " + error);
                });
            }
        });
    }

    private void loadStats() {
        profileManager.loadUserStats(new CustomerProfileManager.OnStatsLoadedListener() {
            @Override
            public void onStatsLoaded(int ordersCount, double totalSpent, long lastOrderAt) {
                runOnUiThread(() -> updateStats(ordersCount, totalSpent));
            }

            @Override
            public void onError(String error) {
                Log.w(TAG, "Failed to load stats: " + error);
            }
        });
    }

    private void updateUI() {
        if (currentProfile == null) return;

        // Name & Email
        String displayName = currentProfile.getDisplayName();
        if (TextUtils.isEmpty(displayName)) {
            displayName = currentProfile.getFullName();
        }
        if (TextUtils.isEmpty(displayName)) {
            displayName = getString(R.string.default_user_name);
        }
        tvUserName.setText(displayName);
        
        String email = currentProfile.getEmail();
        tvUserEmail.setText(!TextUtils.isEmpty(email) ? email : getString(R.string.default_user_email));

        // Membership & Points
        String membershipLevel = profileManager.calculateMembershipTier(currentProfile.getLoyaltyPoints());
        tvMembershipLevel.setText(membershipLevel + " Member");
        tvLoyaltyPoints.setText(NumberFormat.getNumberInstance().format(currentProfile.getLoyaltyPoints()));

        // Profile picture
        loadProfileImage();

        // Profile completion
        int completion = profileManager.calculateProfileCompletion(currentProfile);
        tvCompletionPercentage.setText(completion + "%");
        progressCompletion.setProgress(completion);
        
        if (completion >= 100) {
            cardProfileCompletion.setVisibility(View.GONE);
        } else {
            cardProfileCompletion.setVisibility(View.VISIBLE);
            tvCompletionHint.setText(getCompletionHint(completion));
        }

        // Privacy toggles - load from preferences
        if (currentProfile.getPreferences() != null) {
            // These would be loaded from UserPreferences if available
        }

        // Update phone verification status
        updatePhoneVerificationStatus();
    }

    private void loadProfileImage() {
        String imageUrl = currentProfile != null ? currentProfile.getProfileImageUrl() : null;
        
        if (!TextUtils.isEmpty(imageUrl)) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .circleCrop()
                .into(ivProfilePicture);
        } else {
            ivProfilePicture.setImageResource(R.drawable.ic_person);
        }
    }

    private void updateStats(int ordersCount, double totalSpent) {
        tvTotalOrders.setText(String.valueOf(ordersCount));
        tvTotalSpent.setText(String.format(Locale.getDefault(), "₹%.0f", totalSpent));
        
        // Update profile with stats
        if (currentProfile != null) {
            currentProfile.setTotalOrders(ordersCount);
            currentProfile.setTotalSpent(totalSpent);
        }
    }

    private void updatePhoneVerificationStatus() {
        if (itemPhoneVerification == null) return;
        
        TextView tvSubtitle = itemPhoneVerification.findViewById(R.id.tv_subtitle);
        TextView tvBadge = itemPhoneVerification.findViewById(R.id.tv_badge);
        
        if (currentProfile != null && currentProfile.isPhoneVerified()) {
            if (tvSubtitle != null) tvSubtitle.setText("✓ Verified");
            if (tvBadge != null) tvBadge.setVisibility(View.GONE);
        } else {
            if (tvSubtitle != null) tvSubtitle.setText("Not verified - Tap to verify");
            if (tvBadge != null) {
                tvBadge.setText("!");
                tvBadge.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getCompletionHint(int percentage) {
        if (percentage < 25) {
            return "Add your profile photo and phone number to get started";
        } else if (percentage < 50) {
            return "Verify your phone and add a delivery address";
        } else if (percentage < 75) {
            return "Complete your profile for a better experience";
        } else {
            return "Almost there! Complete all fields for 100%";
        }
    }

    private void updateLastSyncedTime() {
        long lastSync = profileManager.getLastSyncTime();
        if (lastSync > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
            tvLastSynced.setText("Last synced: " + sdf.format(new Date(lastSync)));
        } else {
            tvLastSynced.setText("Not synced yet");
        }
    }

    // ============ Image Handling ============

    private void showImagePickerDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.update_profile_picture)
            .setItems(new String[]{"Take Photo", "Choose from Gallery", "Remove Photo"}, 
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            checkCameraPermissionAndTakePhoto();
                            break;
                        case 1:
                            checkStoragePermissionAndOpenGallery();
                            break;
                        case 2:
                            removeProfilePicture();
                            break;
                    }
                })
            .show();
    }

    private void checkCameraPermissionAndTakePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermissionAndOpenGallery() {
        // For API 33+, we don't need READ_EXTERNAL_STORAGE for picking images
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            openGallery();
        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, 
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            showError("Camera not available");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_IMAGE_GALLERY);
    }

    private void removeProfilePicture() {
        ivProfilePicture.setImageResource(R.drawable.ic_person);
        if (currentProfile != null) {
            currentProfile.setProfileImageUrl(null);
            saveProfile();
        }
        showSuccess("Profile picture removed");
    }

    private void uploadProfileImage(Uri imageUri) {
        showLoading(true);
        
        profileManager.uploadProfileImage(imageUri, new CustomerProfileManager.OnImageUploadListener() {
            @Override
            public void onProgress(int progress) {
                // Could show progress if needed
            }

            @Override
            public void onSuccess(String imageUrl) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (currentProfile != null) {
                        currentProfile.setProfileImageUrl(imageUrl);
                    }
                    loadProfileImage();
                    showSuccess("Profile picture updated");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to upload image: " + error);
                });
            }
        });
    }

    // ============ Navigation Methods ============

    private void openEditProfile() {
        Intent intent = new Intent(this, EditPersonalInfoActivity.class);
        startActivityForResult(intent, REQUEST_EDIT_PROFILE);
    }

    private void openPhoneVerification() {
        // Could show a dialog or navigate to phone verification screen
        if (currentProfile != null && currentProfile.isPhoneVerified()) {
            showSuccess("Phone already verified");
        } else {
            // Show phone verification dialog
            showPhoneVerificationDialog();
        }
    }

    private void showPhoneVerificationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.phone_verification_title)
            .setMessage("We'll send a verification code to your phone number. Standard SMS rates may apply.")
            .setPositiveButton("Send Code", (dialog, which) -> {
                // Navigate to PhoneVerificationActivity
                Intent intent = new Intent(this, PhoneVerificationActivity.class);
                if (currentProfile != null && currentProfile.getPhoneNumber() != null) {
                    intent.putExtra("phone_number", currentProfile.getPhoneNumber());
                }
                startActivity(intent);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openAddresses() {
        try {
            Intent intent = new Intent(this, SavedAddressesActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("Saved Addresses - Coming soon");
        }
    }

    private void openPaymentMethods() {
        try {
            Intent intent = new Intent(this, PaymentMethodsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("Payment Methods - Coming soon");
        }
    }

    private void openOrderHistory() {
        try {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("Order History - Coming soon");
        }
    }

    private void openFavorites() {
        try {
            Intent intent = new Intent(this, FavoriteOrdersActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("Favorites - Coming soon");
        }
    }

    private void openReviews() {
        try {
            Intent intent = new Intent(this, ReviewsRatingsActivity.class);
            intent.putExtra("vendor_id", "all_vendors");
            intent.putExtra("vendor_name", "My Reviews");
            startActivity(intent);
        } catch (Exception e) {
            showError("Reviews - Coming soon");
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, CustomerSettingsActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        try {
            Intent intent = new Intent(this, CustomerHelpSupportActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("Help & Support - Coming soon");
        }
    }

    private void openAbout() {
        try {
            Intent intent = new Intent(this, AboutFoodVanActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("About - Coming soon");
        }
    }

    private void openChangePassword() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.change_password)
            .setMessage("You'll receive an email with instructions to reset your password.")
            .setPositiveButton("Send Email", (dialog, which) -> {
                sendPasswordResetEmail();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendPasswordResetEmail() {
        String email = currentProfile != null ? currentProfile.getEmail() : null;
        if (TextUtils.isEmpty(email)) {
            showError("No email address found");
            return;
        }

        showLoading(true);
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> {
                showLoading(false);
                showSuccess("Password reset email sent to " + email);
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                showError("Failed to send reset email: " + e.getMessage());
            });
    }

    // ============ Account Actions ============

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_confirmation)
            .setPositiveButton(R.string.logout, (dialog, which) -> performLogout())
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void performLogout() {
        // Clear profile cache
        profileManager.clearCache();
        
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Clear session
        sessionManager.logout();
        
        // Navigate to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, R.string.logged_out_successfully, Toast.LENGTH_SHORT).show();
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_account_warning)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                // Show confirmation dialog
                showDeleteConfirmation();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Are you absolutely sure?")
            .setMessage("This action cannot be undone. All your data including orders, reviews, and saved addresses will be permanently deleted.")
            .setPositiveButton("Delete My Account", (dialog, which) -> {
                deleteAccount();
            })
            .setNegativeButton("Keep Account", null)
            .show();
    }

    private void deleteAccount() {
        showLoading(true);
        
        profileManager.deleteAccount(new CustomerProfileManager.OnProfileSavedListener() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(CustomerProfileActivity.this, 
                        "Account deleted successfully", Toast.LENGTH_SHORT).show();
                    
                    // Navigate to login
                    Intent intent = new Intent(CustomerProfileActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError("Failed to delete account: " + error);
                });
            }
        });
    }

    // ============ Privacy Settings ============

    private void updatePrivacySetting(String key, boolean value) {
        profileManager.updatePrivacyPreference(key, value, new CustomerProfileManager.OnProfileSavedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Privacy setting updated: " + key + " = " + value);
            }

            @Override
            public void onError(String error) {
                showError("Failed to update setting");
                // Revert toggle
                if (key.equals("showPhoneOnReceipt")) {
                    switchShowPhone.setChecked(!value);
                } else if (key.equals("marketingEnabled")) {
                    switchMarketing.setChecked(!value);
                }
            }
        });
    }

    // ============ Profile Saving ============

    private void saveProfile() {
        if (currentProfile == null) return;
        
        profileManager.saveProfile(currentProfile, new CustomerProfileManager.OnProfileSavedListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Profile saved successfully");
                updateLastSyncedTime();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to save profile: " + error);
            }
        });
    }

    // ============ Permission Handling ============

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    openCamera();
                    break;
                case REQUEST_STORAGE_PERMISSION:
                    openGallery();
                    break;
            }
        } else {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    showError("Camera permission is required to take photos");
                    break;
                case REQUEST_STORAGE_PERMISSION:
                    showError("Storage permission is required to select photos");
                    break;
            }
        }
    }

    // ============ Activity Result Handling ============

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_IMAGE_CAPTURE:
                    if (data != null && data.getExtras() != null) {
                        Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                        if (imageBitmap != null) {
                            ivProfilePicture.setImageBitmap(imageBitmap);
                            // For camera, we'd need to save to a file and get URI
                            // This is a simplified version
                            showSuccess("Photo captured - upload coming soon");
                        }
                    }
                    break;
                    
                case REQUEST_IMAGE_GALLERY:
                    if (data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            ivProfilePicture.setImageBitmap(bitmap);
                            uploadProfileImage(imageUri);
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image from gallery", e);
                            showError("Error loading image");
                        }
                    }
                    break;
                    
                case REQUEST_EDIT_PROFILE:
                    // Refresh profile after editing
                    loadProfile();
                    break;
            }
        }
    }

    // ============ UI Helpers ============

    private void showLoading(boolean show) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void showError(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.error_color))
            .show();
    }

    private void showSuccess(String message) {
        Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(ContextCompat.getColor(this, R.color.success_color))
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh profile data when returning
        if (currentProfile != null) {
            loadProfile();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
