package com.example.foodvan.activities.customer;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.foodvan.MainActivity;
import com.example.foodvan.R;
import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Clean Settings Activity matching the provided image design
 * Implements only the essential functionality as specified
 */
public class SettingsActivityClean2 extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private LinearLayout layoutPersonalInfo;
    private LinearLayout layoutSavedAddresses;
    private LinearLayout layoutPaymentMethods;
    private LinearLayout layoutOrderHistory;
    private LinearLayout layoutFavoriteOrders;
    private LinearLayout layoutReviewsRatings;
    private LinearLayout layoutAppSettings;
    private LinearLayout layoutHelpSupport;
    private LinearLayout layoutAbout;
    private MaterialCardView cardLogout;
    
    // Data components
    private TextView textAddressesCount;
    private TextView textPaymentCount;
    
    // Managers
    private SessionManager sessionManager;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        initializeComponents();
        initializeViews();
        setupClickListeners();
        updateCounts();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        sharedPreferences = getSharedPreferences("foodvan_settings", MODE_PRIVATE);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutPersonalInfo = findViewById(R.id.layout_personal_info);
        layoutSavedAddresses = findViewById(R.id.layout_saved_addresses);
        layoutPaymentMethods = findViewById(R.id.layout_payment_methods);
        layoutOrderHistory = findViewById(R.id.layout_order_history);
        layoutFavoriteOrders = findViewById(R.id.layout_favorite_orders);
        layoutReviewsRatings = findViewById(R.id.layout_reviews_ratings);
        layoutAppSettings = findViewById(R.id.layout_app_settings);
        layoutHelpSupport = findViewById(R.id.layout_help_support);
        layoutAbout = findViewById(R.id.layout_about);
        cardLogout = findViewById(R.id.card_logout);
        
        textAddressesCount = findViewById(R.id.text_addresses_count);
        textPaymentCount = findViewById(R.id.text_payment_count);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupClickListeners() {
        // Account Section
        layoutPersonalInfo.setOnClickListener(v -> openPersonalInformation());
        layoutSavedAddresses.setOnClickListener(v -> openSavedAddresses());
        layoutPaymentMethods.setOnClickListener(v -> openPaymentMethods());
        
        // Food Section
        layoutOrderHistory.setOnClickListener(v -> openOrderHistory());
        layoutFavoriteOrders.setOnClickListener(v -> openFavoriteOrders());
        layoutReviewsRatings.setOnClickListener(v -> openReviewsRatings());
        
        // More Section
        layoutAppSettings.setOnClickListener(v -> openAppSettings());
        layoutHelpSupport.setOnClickListener(v -> openHelpSupport());
        layoutAbout.setOnClickListener(v -> openAbout());
        
        // Logout
        cardLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void updateCounts() {
        // Update addresses count (placeholder - integrate with actual data)
        int addressCount = sharedPreferences.getInt("saved_addresses_count", 0);
        textAddressesCount.setText(addressCount + " saved addresses");
        
        // Update payment methods count (placeholder - integrate with actual data)
        int paymentCount = sharedPreferences.getInt("payment_methods_count", 2);
        textPaymentCount.setText(paymentCount + " payment methods saved");
    }

    // Account Section Methods
    private void openPersonalInformation() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openSavedAddresses() {
        try {
            Intent intent = new Intent(this, SavedAddressesActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            // Create minimal stub if activity doesn't exist
            showComingSoonMessage("Saved Addresses");
        }
    }

    private void openPaymentMethods() {
        try {
            Intent intent = new Intent(this, PaymentMethodsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            // Create minimal stub if activity doesn't exist
            showComingSoonMessage("Payment Methods");
        }
    }

    // Food Section Methods
    private void openOrderHistory() {
        try {
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showComingSoonMessage("Order History");
        }
    }

    private void openFavoriteOrders() {
        try {
            Intent intent = new Intent(this, FavoriteOrdersActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showComingSoonMessage("Favorite Orders");
        }
    }

    private void openReviewsRatings() {
        try {
            Intent intent = new Intent(this, ReviewsRatingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showComingSoonMessage("Reviews & Ratings");
        }
    }

    // More Section Methods
    private void openAppSettings() {
        showAppSettingsDialog();
    }

    private void openHelpSupport() {
        showHelpSupportDialog();
    }

    private void openAbout() {
        showAboutDialog();
    }

    // Dialog Methods
    private void showAppSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_app_settings, null);
        
        SwitchMaterial notificationsSwitch = dialogView.findViewById(R.id.switch_notifications);
        RadioGroup themeGroup = dialogView.findViewById(R.id.radio_group_theme);
        MaterialButton languageButton = dialogView.findViewById(R.id.button_language);
        
        // Load current settings
        boolean notificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);
        notificationsSwitch.setChecked(notificationsEnabled);
        
        int themeMode = sharedPreferences.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        switch (themeMode) {
            case AppCompatDelegate.MODE_NIGHT_NO:
                themeGroup.check(R.id.radio_light);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                themeGroup.check(R.id.radio_dark);
                break;
            default:
                themeGroup.check(R.id.radio_system);
                break;
        }
        
        languageButton.setOnClickListener(v -> showLanguageDialog());
        
        Dialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle("App Settings")
                .setView(dialogView)
                .setPositiveButton("Save", (d, which) -> {
                    // Save notifications setting
                    boolean newNotificationsEnabled = notificationsSwitch.isChecked();
                    sharedPreferences.edit()
                            .putBoolean("notifications_enabled", newNotificationsEnabled)
                            .apply();
                    
                    // Save theme setting
                    int selectedThemeId = themeGroup.getCheckedRadioButtonId();
                    int newThemeMode;
                    if (selectedThemeId == R.id.radio_light) {
                        newThemeMode = AppCompatDelegate.MODE_NIGHT_NO;
                    } else if (selectedThemeId == R.id.radio_dark) {
                        newThemeMode = AppCompatDelegate.MODE_NIGHT_YES;
                    } else {
                        newThemeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    }
                    
                    if (newThemeMode != themeMode) {
                        sharedPreferences.edit().putInt("theme_mode", newThemeMode).apply();
                        AppCompatDelegate.setDefaultNightMode(newThemeMode);
                        showSuccessMessage("Theme applied successfully");
                    }
                    
                    if (newNotificationsEnabled != notificationsEnabled) {
                        showSuccessMessage("Notification settings saved");
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        
        dialog.show();
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Spanish", "French", "German"};
        String[] languageCodes = {"en", "es", "fr", "de"};
        
        String currentLanguage = sharedPreferences.getString("language_code", "en");
        int selectedIndex = 0;
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(currentLanguage)) {
                selectedIndex = i;
                break;
            }
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Language")
                .setSingleChoiceItems(languages, selectedIndex, (dialog, which) -> {
                    String selectedLanguageCode = languageCodes[which];
                    sharedPreferences.edit()
                            .putString("language_code", selectedLanguageCode)
                            .apply();
                    
                    showSuccessMessage("Language will be applied after app restart");
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showHelpSupportDialog() {
        String[] options = {"Send Email", "View FAQs", "Report Issue"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Help & Support")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            sendSupportEmail();
                            break;
                        case 1:
                            showComingSoonMessage("FAQs");
                            break;
                        case 2:
                            sendSupportEmail();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void sendSupportEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@foodvan.com"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Food Van App Support");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi Support Team,\n\nI need help with:\n\n");
        
        try {
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            showErrorMessage("No email app found");
        }
    }

    private void showAboutDialog() {
        String appVersion = "1.0";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            appVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Use default version
        }
        
        String aboutText = "Food Van v" + appVersion + "\n\n" +
                "Last updated: October 2025\n\n" +
                "Delivering delicious food to your doorstep.";
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("About Food Van")
                .setMessage(aboutText)
                .setPositiveButton("Privacy Policy", (d, w) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                            Uri.parse("https://foodvan.com/privacy"));
                    try {
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        showErrorMessage("Unable to open privacy policy");
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout? You'll need to sign in again to access your account.")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        try {
            // Sign out from Firebase
            if (firebaseAuth.getCurrentUser() != null) {
                firebaseAuth.signOut();
            }
            
            // Clear session
            sessionManager.logout();
            
            // Clear app preferences (optional - keep user preferences)
            // sharedPreferences.edit().clear().apply();
            
            // Navigate to login
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            
        } catch (Exception e) {
            showErrorMessage("Failed to logout. Please try again.");
        }
    }

    // Utility Methods
    private void showSuccessMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(getColor(R.color.colorSuccess))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getColor(R.color.colorError))
                .setTextColor(getColor(android.R.color.white))
                .show();
    }

    private void showComingSoonMessage(String feature) {
        Toast.makeText(this, feature + " - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounts();
    }
}
