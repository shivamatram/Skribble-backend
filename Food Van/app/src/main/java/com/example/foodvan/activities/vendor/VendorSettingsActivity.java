package com.example.foodvan.activities.vendor;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.foodvan.R;
import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.activities.vendor.LiveLocationActivity;
import com.example.foodvan.activities.vendor.VendorReviewsActivity;
import com.example.foodvan.activities.vendor.VendorSavedAddressesActivity;
import com.example.foodvan.models.Vendor;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Comprehensive Vendor Settings Activity with Material Design 3
 * Sections: Account Management, Business Operations, App Preferences, Support
 */
public class VendorSettingsActivity extends AppCompatActivity {
    
    // Request codes
    private static final int PHONE_VERIFICATION_REQUEST = 1001;

    private static final String TAG = "VendorSettingsActivity";
    private static final String PREFS_NAME = "VendorSettings";
    
    // UI Components
    private MaterialToolbar toolbar;
    private NestedScrollView scrollView;
    
    // Section A - Vendor Account Management
    private MaterialCardView cardEditProfile;
    private MaterialCardView cardPhoneVerification;
    private TextView tvVendorName, tvVendorPhone;
    private TextView tvPhoneStatus;
    private Chip phoneStatusChip;
    
    // Section B - Business & Operations
    private MaterialCardView cardBusinessHours;
    private MaterialCardView cardSavedAddresses;
    private MaterialCardView cardPaymentPayout;
    private MaterialCardView cardLocationUpdate;
    private TextView tvOpeningTime, tvClosingTime;
    private TextView tvSavedAddressesCount;
    private TextView tvPaymentMethod;
    private TextView tvCurrentLocation;
    
    // Section C - App Preferences
    private MaterialCardView cardNotificationSettings;
    private SwitchMaterial switchOrderAlerts;
    
    // Section D - Support & Misc
    private MaterialCardView cardReviewsRatings;
    private MaterialCardView cardAboutApp;
    private MaterialCardView cardLogout;
    private TextView tvAppVersion;
    
    // Data & Services
    private SessionManager sessionManager;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef;
    private Vendor currentVendor;
    private SharedPreferences preferences;
    
    // Business Hours
    private int openingHour = 9, openingMinute = 0;
    private int closingHour = 21, closingMinute = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_vendor_settings);
        
        initializeComponents();
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadVendorData();
        loadPreferences();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh verification status when activity resumes
        refreshPhoneVerificationStatus();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        firebaseAuth = FirebaseAuth.getInstance();
        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        String vendorId = sessionManager.getUserId();
        if (vendorId != null) {
            vendorRef = FirebaseDatabase.getInstance().getReference("vendors").child(vendorId);
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        scrollView = findViewById(R.id.scroll_view);
        
        // Section A - Account Management
        cardEditProfile = findViewById(R.id.card_edit_profile);
        cardPhoneVerification = findViewById(R.id.card_phone_verification);
        tvVendorName = findViewById(R.id.tv_vendor_name);
        tvVendorPhone = findViewById(R.id.tv_vendor_phone);
        tvPhoneStatus = findViewById(R.id.tv_phone_status);
        phoneStatusChip = findViewById(R.id.phone_status_chip);
        
        // Section B - Business Operations
        cardBusinessHours = findViewById(R.id.card_business_hours);
        cardSavedAddresses = findViewById(R.id.card_saved_addresses);
        cardPaymentPayout = findViewById(R.id.card_payment_payout);
        cardLocationUpdate = findViewById(R.id.card_location_update);
        tvOpeningTime = findViewById(R.id.tv_opening_time);
        tvClosingTime = findViewById(R.id.tv_closing_time);
        tvSavedAddressesCount = findViewById(R.id.tv_saved_addresses_count);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        
        // Section C - App Preferences
        cardNotificationSettings = findViewById(R.id.card_notification_settings);
        switchOrderAlerts = findViewById(R.id.switch_order_alerts);
        
        // Section D - Support & Misc
        cardReviewsRatings = findViewById(R.id.card_reviews_ratings);
        cardAboutApp = findViewById(R.id.card_about_app);
        cardLogout = findViewById(R.id.card_logout);
        tvAppVersion = findViewById(R.id.tv_app_version);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Profile & Account Section
        cardEditProfile.setOnClickListener(v -> openProfileAccount());
        cardPhoneVerification.setOnClickListener(v -> openPhoneVerification());
        
        // Business Operations
        cardBusinessHours.setOnClickListener(v -> openBusinessHours());
        cardSavedAddresses.setOnClickListener(v -> openSavedAddresses());
        cardPaymentPayout.setOnClickListener(v -> openPaymentPayout());
        cardLocationUpdate.setOnClickListener(v -> openLocationUpdate());
        
        // Notifications Preferences
        cardNotificationSettings.setOnClickListener(v -> toggleNotificationSettings());
        switchOrderAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveNotificationPreference("order_alerts", isChecked);
            showSuccess("Order alerts " + (isChecked ? "enabled" : "disabled"));
        });
        
        
        // Reviews & Ratings and About App
        cardReviewsRatings.setOnClickListener(v -> openReviewsRatings());
        cardAboutApp.setOnClickListener(v -> openAboutApp());
        
        // Logout
        cardLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadVendorData() {
        if (vendorRef == null) return;
        
        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentVendor = snapshot.getValue(Vendor.class);
                if (currentVendor != null) {
                    updateVendorInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Failed to load vendor data: " + error.getMessage());
            }
        });
    }

    private void updateVendorInfo() {
        if (currentVendor == null) return;
        
        // Update account info
        tvVendorName.setText(currentVendor.getName() != null ? currentVendor.getName() : "Not set");
        
        // Get phone verification status from SharedPreferences (more reliable)
        SharedPreferences vendorPrefs = getSharedPreferences("VendorPrefs", MODE_PRIVATE);
        boolean isPhoneVerified = vendorPrefs.getBoolean("phone_verified", false);
        String verifiedPhone = vendorPrefs.getString("vendor_phone", "");
        
        // Update phone number display
        if (!verifiedPhone.isEmpty()) {
            tvVendorPhone.setText(verifiedPhone);
        } else if (currentVendor.getPhone() != null) {
            tvVendorPhone.setText(currentVendor.getPhone());
        } else {
            tvVendorPhone.setText("Not set");
        }
        
        // Update phone verification status with explicit color setting
        if (phoneStatusChip != null) {
            if (isPhoneVerified) {
                tvPhoneStatus.setText("Verified");
                tvPhoneStatus.setTextColor(getResources().getColor(R.color.success_color, null));
                
                // Update chip to verified state
                phoneStatusChip.setText("Verified");
                phoneStatusChip.setTextColor(getResources().getColor(R.color.success_color, null));
                phoneStatusChip.setChipBackgroundColor(getResources().getColorStateList(R.color.success_light, null));
                phoneStatusChip.setChipIcon(getResources().getDrawable(R.drawable.ic_check_circle, null));
                phoneStatusChip.setChipIconTint(getResources().getColorStateList(R.color.success_color, null));
            } else {
                tvPhoneStatus.setText("Not Verified");
                tvPhoneStatus.setTextColor(getResources().getColor(R.color.error_color, null));
                
                // Update chip to not verified state
                phoneStatusChip.setText("Not Verified");
                phoneStatusChip.setTextColor(getResources().getColor(R.color.error_color, null));
                phoneStatusChip.setChipBackgroundColor(getResources().getColorStateList(R.color.warning_light, null));
                phoneStatusChip.setChipIcon(getResources().getDrawable(R.drawable.ic_warning, null));
                phoneStatusChip.setChipIconTint(getResources().getColorStateList(R.color.primary_color, null));
            }
        }
        
        // Update business info
        updateBusinessHoursDisplay();
        tvCurrentLocation.setText(currentVendor.getAddress() != null ? 
            currentVendor.getAddress() : "Location not set");
    }

    private void loadPreferences() {
        // Load notification preferences
        boolean orderAlerts = preferences.getBoolean("order_alerts", true);
        switchOrderAlerts.setChecked(orderAlerts);
        
        
        // Load app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            tvAppVersion.setText("Version 1.0.0");
        }
    }

    // Profile & Account Methods
    private void openProfileAccount() {
        try {
            // Navigate to Edit Profile Module with Tabs (includes Privacy Settings)
            Intent intent = new Intent(this, VendorProfileEditActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to dialog if activity doesn't exist
            showProfileAccountDialog();
        }
    }

    private void showProfileAccountDialog() {
        String[] options = {"Edit Profile", "Account Settings", "Privacy Settings"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Profile & Account")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showInfo("Edit Profile feature will be available soon");
                        break;
                    case 1:
                        showInfo("Account Settings feature will be available soon");
                        break;
                    case 2:
                        showInfo("Privacy Settings feature will be available soon");
                        break;
                }
            })
            .show();
    }

    private void openPhoneVerification() {
        // Check SharedPreferences for verification status (more reliable)
        SharedPreferences vendorPrefs = getSharedPreferences("VendorPrefs", MODE_PRIVATE);
        boolean isPhoneVerified = vendorPrefs.getBoolean("phone_verified", false);
        
        if (isPhoneVerified) {
            showSuccess("Phone number is already verified ✓");
            return;
        }
        
        try {
            Intent intent = new Intent(this, PhoneVerificationActivity.class);
            startActivityForResult(intent, PHONE_VERIFICATION_REQUEST);
        } catch (Exception e) {
            showError("Unable to open phone verification");
        }
    }


    // Section B - Business Operations Methods
    private void openBusinessHours() {
        showBusinessHoursDialog();
    }

    private void showBusinessHoursDialog() {
        // Simple time picker dialog until proper dialog layout is created
        showTimePicker("Opening Time", openingHour, openingMinute, (hour, minute) -> {
            openingHour = hour;
            openingMinute = minute;
            showTimePicker("Closing Time", closingHour, closingMinute, (closingH, closingM) -> {
                closingHour = closingH;
                closingMinute = closingM;
                saveBusinessHours();
            });
        });
    }

    private void showTimePicker(String title, int hour, int minute, TimePickerCallback callback) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, selectedHour, selectedMinute) -> callback.onTimeSelected(selectedHour, selectedMinute),
            hour,
            minute,
            false
        );
        timePickerDialog.setTitle(title);
        timePickerDialog.show();
    }

    private void updateTimeDisplay(TextView textView, int hour, int minute) {
        String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
        textView.setText(time);
    }

    private void saveBusinessHours() {
        if (vendorRef != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("openingTime", String.format(Locale.getDefault(), "%02d:%02d", openingHour, openingMinute));
            updates.put("closingTime", String.format(Locale.getDefault(), "%02d:%02d", closingHour, closingMinute));
            
            vendorRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showSuccess("Business hours updated successfully");
                    updateBusinessHoursDisplay();
                })
                .addOnFailureListener(e -> showError("Failed to update business hours"));
        }
    }

    private void updateBusinessHoursDisplay() {
        String openingTime = String.format(Locale.getDefault(), "%02d:%02d", openingHour, openingMinute);
        String closingTime = String.format(Locale.getDefault(), "%02d:%02d", closingHour, closingMinute);
        
        tvOpeningTime.setText(openingTime);
        tvClosingTime.setText(closingTime);
    }


    private void openSavedAddresses() {
        // Navigate to Saved Addresses Activity
        Intent intent = new Intent(this, VendorSavedAddressesActivity.class);
        startActivity(intent);
    }

    private void showSavedAddressesDialog() {
        String[] options = {"View Saved Addresses", "Add New Address", "Edit Primary Address", "Delete Address"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Saved Addresses")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showInfo("View saved addresses feature will be available soon");
                        break;
                    case 1:
                        showAddAddressDialog();
                        break;
                    case 2:
                        showInfo("Edit address feature will be available soon");
                        break;
                    case 3:
                        showInfo("Delete address feature will be available soon");
                        break;
                }
            })
            .show();
    }

    private void showAddAddressDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Add New Address")
            .setMessage("Address management feature will be available soon with map integration and GPS location.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void openPaymentPayout() {
        try {
            // Navigate to Payment & Payout Activity
            Intent intent = new Intent(this, PaymentPayoutActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to dialog if activity doesn't exist
            showPaymentPayoutDialog();
        }
    }

    private void showPaymentPayoutDialog() {
        String[] options = {"Bank Account Details", "UPI Details", "Payment History", "Payout Settings"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Payment & Payout")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showBankDetailsDialog();
                        break;
                    case 1:
                        showUPIDetailsDialog();
                        break;
                    case 2:
                        showInfo("Payment history feature will be available soon");
                        break;
                    case 3:
                        showInfo("Payout settings feature will be available soon");
                        break;
                }
            })
            .show();
    }

    private void showBankDetailsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Bank Account Details")
            .setMessage("Bank account management feature will be available soon with secure validation and verification.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void showUPIDetailsDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("UPI Details")
            .setMessage("UPI management feature will be available soon with QR code generation and validation.")
            .setPositiveButton("OK", null)
            .show();
    }


    private void openLocationUpdate() {
        try {
            // Navigate to Live Location Update Activity
            Intent intent = new Intent(this, LiveLocationActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            // Log the error and show fallback dialog
            e.printStackTrace();
            showError("Error opening Live Location: " + e.getMessage());
            showLocationUpdateDialog();
        }
    }

    private void showLocationUpdateDialog() {
        String[] options = {"Update Current Location", "Set Default Location", "Location History", "GPS Settings"};
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Live Location Update")
            .setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showInfo("Live location update feature will be available soon with GPS integration");
                        break;
                    case 1:
                        showInfo("Default location setting will be available soon");
                        break;
                    case 2:
                        showInfo("Location history feature will be available soon");
                        break;
                    case 3:
                        showInfo("GPS settings feature will be available soon");
                        break;
                }
            })
            .show();
    }

    private void toggleNotificationSettings() {
        // Toggle the switch when card is clicked
        switchOrderAlerts.setChecked(!switchOrderAlerts.isChecked());
    }

    private void saveNotificationPreference(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    // Section D - Support & Misc Methods

    private void openEmailSupport() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:vendor-support@foodvan.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Vendor Support Request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, 
            "Vendor ID: " + sessionManager.getUserId() + "\n" +
            "Issue Description: \n\n");
        
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            showError("No email app found");
        }
    }

    private void openWhatsAppSupport() {
        try {
            String phoneNumber = "+1234567890"; // Replace with actual support number
            String message = "Hi, I need help with my Food Van vendor account.";
            
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phoneNumber + "?text=" + Uri.encode(message)));
            startActivity(intent);
        } catch (Exception e) {
            showError("WhatsApp not installed");
        }
    }

    private void openFAQ() {
        showInfo("FAQ section will be available soon");
    }

    private void openReportIssue() {
        showInfo("Issue reporting feature will be available soon");
    }

    private void openReviewsRatings() {
        try {
            Intent intent = new Intent(this, VendorReviewsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showError("Error opening reviews: " + e.getMessage());
        }
    }

    private void openAboutApp() {
        showAboutDialog();
    }

    private void showAboutDialog() {
        String aboutText = "Food Van - Vendor App\n\n" +
                          "Version: " + tvAppVersion.getText().toString() + "\n" +
                          "Developer: Food Van Team\n" +
                          "Contact: support@foodvan.com\n\n" +
                          "© 2024 Food Van. All rights reserved.";
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("About App")
            .setMessage(aboutText)
            .setPositiveButton("OK", null)
            .show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? You will need to login again to access your vendor dashboard.")
            .setPositiveButton("Logout", (dialog, which) -> performLogout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void performLogout() {
        // Clear session
        sessionManager.logout();
        
        // Sign out from Firebase
        firebaseAuth.signOut();
        
        // Clear preferences
        preferences.edit().clear().apply();
        
        // Redirect to login
        Intent intent = new Intent(this, com.example.foodvan.activities.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        showSuccess("Logged out successfully");
    }

    // Utility Methods
    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.success_color))
            .show();
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error_color))
            .show();
    }

    private void showInfo(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.info_color))
            .show();
    }

    /**
     * Setup orange status bar to match the navbar theme with no overlapping
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PHONE_VERIFICATION_REQUEST) {
            if (resultCode == RESULT_OK && data != null) {
                boolean phoneVerified = data.getBooleanExtra("phone_verified", false);
                String verifiedPhone = data.getStringExtra("verified_phone");
                
                if (phoneVerified) {
                    // Update UI to show verified status immediately
                    updatePhoneVerificationStatus(true, verifiedPhone);
                    
                    // Also refresh from SharedPreferences to ensure consistency
                    refreshPhoneVerificationStatus();
                    
                    // Show success message
                    showSuccess("Phone number verified successfully!");
                    
                    // Reload vendor data to reflect changes
                    loadVendorData();
                } else {
                    showError("Phone verification failed");
                }
            } else {
                // User cancelled or verification failed
                showInfo("Phone verification cancelled");
            }
        }
    }
    
    /**
     * Update phone verification status in UI
     */
    private void updatePhoneVerificationStatus(boolean isVerified, String phoneNumber) {
        if (tvVendorPhone != null && tvPhoneStatus != null && phoneStatusChip != null) {
            // Update phone number display
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                tvVendorPhone.setText(phoneNumber);
            }
            
            // Update verification status with explicit color setting
            if (isVerified) {
                tvPhoneStatus.setText("Verified");
                tvPhoneStatus.setTextColor(getResources().getColor(R.color.success_color, null));
                
                // Update chip to verified state
                phoneStatusChip.setText("Verified");
                phoneStatusChip.setTextColor(getResources().getColor(R.color.success_color, null));
                phoneStatusChip.setChipBackgroundColor(getResources().getColorStateList(R.color.success_light, null));
                phoneStatusChip.setChipIcon(getResources().getDrawable(R.drawable.ic_check_circle, null));
                phoneStatusChip.setChipIconTint(getResources().getColorStateList(R.color.success_color, null));
            } else {
                tvPhoneStatus.setText("Not Verified");
                tvPhoneStatus.setTextColor(getResources().getColor(R.color.error_color, null));
                
                // Update chip to not verified state
                phoneStatusChip.setText("Not Verified");
                phoneStatusChip.setTextColor(getResources().getColor(R.color.error_color, null));
                phoneStatusChip.setChipBackgroundColor(getResources().getColorStateList(R.color.warning_light, null));
                phoneStatusChip.setChipIcon(getResources().getDrawable(R.drawable.ic_warning, null));
                phoneStatusChip.setChipIconTint(getResources().getColorStateList(R.color.primary_color, null));
            }
        }
    }
    
    /**
     * Refresh phone verification status from SharedPreferences
     */
    private void refreshPhoneVerificationStatus() {
        if (tvVendorPhone != null && tvPhoneStatus != null && phoneStatusChip != null) {
            SharedPreferences vendorPrefs = getSharedPreferences("VendorPrefs", MODE_PRIVATE);
            boolean isPhoneVerified = vendorPrefs.getBoolean("phone_verified", false);
            String verifiedPhone = vendorPrefs.getString("vendor_phone", "");
            
            // Update phone number if available
            if (!verifiedPhone.isEmpty()) {
                tvVendorPhone.setText(verifiedPhone);
            }
            
            // Update verification status with explicit color setting
            if (isPhoneVerified) {
                tvPhoneStatus.setText("Verified");
                tvPhoneStatus.setTextColor(getResources().getColor(R.color.success_color, null));
                
                // Update chip to verified state
                phoneStatusChip.setText("Verified");
                phoneStatusChip.setTextColor(getResources().getColor(R.color.success_color, null));
                phoneStatusChip.setChipBackgroundColor(getResources().getColorStateList(R.color.success_light, null));
                phoneStatusChip.setChipIcon(getResources().getDrawable(R.drawable.ic_check_circle, null));
                phoneStatusChip.setChipIconTint(getResources().getColorStateList(R.color.success_color, null));
            } else {
                tvPhoneStatus.setText("Not Verified");
                tvPhoneStatus.setTextColor(getResources().getColor(R.color.error_color, null));
                
                // Update chip to not verified state
                phoneStatusChip.setText("Not Verified");
                phoneStatusChip.setTextColor(getResources().getColor(R.color.error_color, null));
                phoneStatusChip.setChipBackgroundColor(getResources().getColorStateList(R.color.warning_light, null));
                phoneStatusChip.setChipIcon(getResources().getDrawable(R.drawable.ic_warning, null));
                phoneStatusChip.setChipIconTint(getResources().getColorStateList(R.color.primary_color, null));
            }
        }
    }
    
    /**
     * Manually set phone as verified for immediate testing
     */
    private void setPhoneAsVerified() {
        // Get the current phone number from the display or use a default
        String phoneNumber = "9876543210"; // Default verified phone number
        
        // Save to SharedPreferences
        SharedPreferences vendorPrefs = getSharedPreferences("VendorPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = vendorPrefs.edit();
        editor.putString("vendor_phone", phoneNumber);
        editor.putBoolean("phone_verified", true);
        editor.apply();
        
        // Update UI immediately
        if (tvVendorPhone != null && tvPhoneStatus != null) {
            tvVendorPhone.setText(phoneNumber);
            tvPhoneStatus.setText("Verified");
            tvPhoneStatus.setTextColor(getResources().getColor(R.color.success_color, null));
        }
    }

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

    // Interface for time picker callback
    private interface TimePickerCallback {
        void onTimeSelected(int hour, int minute);
    }
}
