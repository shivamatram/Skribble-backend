package com.example.foodvan.activities.customer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.activities.customer.TermsConditionsActivity;
import com.example.foodvan.activities.customer.AboutFoodVanActivity;
import com.example.foodvan.models.CustomerSettings;
import com.example.foodvan.viewmodels.CustomerSettingsViewModel;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * CustomerSettingsActivity - Customer Settings screen
 * Allows users to configure notifications, location, privacy, theme, language, and more
 */
public class CustomerSettingsActivity extends AppCompatActivity {
    private static final String TAG = "CustomerSettingsActivity";
    
    private Toolbar toolbar;
    private CustomerSettingsViewModel viewModel;
    
    // Account & Profile Views
    private View btnEditProfile;
    private View btnPhoneVerification;
    private View btnSavedAddresses;
    private View btnPaymentMethods;
    private TextView tvPhoneVerificationStatus;
    
    // Notification Views
    private SwitchMaterial swNotificationsEnabled;
    private SwitchMaterial swOrderUpdates;
    private SwitchMaterial swPromotions;
    private SwitchMaterial swSystemAlerts;
    
    // Location & Map Views
    private SwitchMaterial swUseGps;
    private RadioGroup rgMapView;
    
    // Privacy & Data Views
    private SwitchMaterial swShareData;
    private SwitchMaterial swRecommendations;
    private View btnClearSearch;
    private View btnClearViewed;
    
    // Display & Theme Views
    private RadioGroup rgTheme;
    private SwitchMaterial swHighContrast;
    private View btnLanguage;
    private TextView tvLanguageSelected;
    
    // Order Preferences Views
    private RadioGroup rgOrderSort;
    private SwitchMaterial swOrderSuggestions;
    
    // Help & Legal Views
    private View btnHelpSupport;
    private View btnTerms;
    private View btnPrivacy;
    private View btnAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);
        
        initializeViews();
        setupViewModel();
        setupToolbar();
        setupListeners();
    }

    private void initializeViews() {
        // Account & Profile Views
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnPhoneVerification = findViewById(R.id.btn_phone_verification);
        btnSavedAddresses = findViewById(R.id.btn_saved_addresses);
        btnPaymentMethods = findViewById(R.id.btn_payment_methods);
        tvPhoneVerificationStatus = findViewById(R.id.tv_phone_verification_status);
        
        // Notification Views
        swNotificationsEnabled = findViewById(R.id.sw_notifications_enabled);
        swOrderUpdates = findViewById(R.id.sw_order_updates);
        swPromotions = findViewById(R.id.sw_promotions);
        swSystemAlerts = findViewById(R.id.sw_system_alerts);
        
        // Location & Map Views
        swUseGps = findViewById(R.id.sw_use_gps);
        rgMapView = findViewById(R.id.rg_map_view);
        
        // Privacy & Data Views
        swShareData = findViewById(R.id.sw_share_data);
        swRecommendations = findViewById(R.id.sw_recommendations);
        btnClearSearch = findViewById(R.id.btn_clear_search);
        btnClearViewed = findViewById(R.id.btn_clear_viewed);
        
        // Display & Theme Views
        rgTheme = findViewById(R.id.rg_theme);
        swHighContrast = findViewById(R.id.sw_high_contrast);
        btnLanguage = findViewById(R.id.btn_language);
        tvLanguageSelected = findViewById(R.id.tv_language_selected);
        
        // Order Preferences Views
        rgOrderSort = findViewById(R.id.rg_order_sort);
        swOrderSuggestions = findViewById(R.id.sw_order_suggestions);
        
        // Help & Legal Views
        btnHelpSupport = findViewById(R.id.btn_help_support);
        btnTerms = findViewById(R.id.btn_terms);
        btnPrivacy = findViewById(R.id.btn_privacy);
        btnAbout = findViewById(R.id.btn_about);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(CustomerSettingsViewModel.class);
        
        // Observe settings
        viewModel.getSettingsLiveData().observe(this, this::updateUIWithSettings);
        
        // Observe loading state
        viewModel.getLoadingLiveData().observe(this, isLoading -> {
            // Could show/hide progress bar here if needed
        });
        
        // Observe error messages
        viewModel.getErrorLiveData().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        // Observe success messages
        viewModel.getSuccessMessageLiveData().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUIWithSettings(CustomerSettings settings) {
        if (settings == null) return;
        
        try {
            // Update Notification Settings
            swNotificationsEnabled.setChecked(settings.isNotificationsEnabled());
            swOrderUpdates.setEnabled(settings.isNotificationsEnabled());
            swOrderUpdates.setChecked(settings.isOrderUpdatesEnabled());
            swPromotions.setEnabled(settings.isNotificationsEnabled());
            swPromotions.setChecked(settings.isPromotionsEnabled());
            swSystemAlerts.setEnabled(settings.isNotificationsEnabled());
            swSystemAlerts.setChecked(settings.isSystemAlertsEnabled());
            
            // Update Location & Map Settings
            swUseGps.setChecked(settings.isUseGpsForNearbyVans());
            updateMapViewRadioGroup(settings.getPreferredMapView());
            
            // Update Privacy & Data Settings
            swShareData.setChecked(settings.isShareOrderDataWithVendors());
            swRecommendations.setChecked(settings.isAllowPersonalizedRecommendations());
            
            // Update Display & Theme Settings
            updateThemeRadioGroup(settings.getThemeMode());
            swHighContrast.setChecked(settings.isHighContrastMode());
            updateLanguageDisplay(settings.getAppLanguage());
            
            // Update Order Preferences
            updateOrderSortRadioGroup(settings.getDefaultOrderSort());
            swOrderSuggestions.setChecked(settings.isShowOrderSuggestions());
            
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI with settings", e);
        }
    }

    private void updateMapViewRadioGroup(String mapView) {
        switch (mapView) {
            case "map":
                rgMapView.check(R.id.rb_map_view);
                break;
            case "list":
                rgMapView.check(R.id.rb_list_view);
                break;
            case "both":
            default:
                rgMapView.check(R.id.rb_both_view);
                break;
        }
    }

    private void updateThemeRadioGroup(String themeMode) {
        switch (themeMode) {
            case "light":
                rgTheme.check(R.id.rb_light_theme);
                break;
            case "dark":
                rgTheme.check(R.id.rb_dark_theme);
                break;
            case "system":
            default:
                rgTheme.check(R.id.rb_system_theme);
                break;
        }
    }

    private void updateOrderSortRadioGroup(String sortBy) {
        switch (sortBy) {
            case "oldest":
                rgOrderSort.check(R.id.rb_oldest_first);
                break;
            case "status":
                rgOrderSort.check(R.id.rb_status_based);
                break;
            case "newest":
            default:
                rgOrderSort.check(R.id.rb_newest_first);
                break;
        }
    }

    private void updateLanguageDisplay(String language) {
        String displayName;
        switch (language) {
            case "hi":
                displayName = getString(R.string.hindi);
                break;
            case "mr":
                displayName = getString(R.string.marathi);
                break;
            case "en":
            default:
                displayName = getString(R.string.english);
                break;
        }
        tvLanguageSelected.setText(displayName);
    }

    private void setupListeners() {
        // Account & Profile Listeners
        btnEditProfile.setOnClickListener(v -> navigateToEditProfile());
        btnPhoneVerification.setOnClickListener(v -> navigateToPhoneVerification());
        btnSavedAddresses.setOnClickListener(v -> navigateToSavedAddresses());
        btnPaymentMethods.setOnClickListener(v -> navigateToPaymentMethods());
        
        // Notification Settings Listeners
        swNotificationsEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.toggleNotificationsEnabled(isChecked);
            // Enable/disable sub-settings
            swOrderUpdates.setEnabled(isChecked);
            swPromotions.setEnabled(isChecked);
            swSystemAlerts.setEnabled(isChecked);
        });
        
        swOrderUpdates.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleOrderUpdates(isChecked));
        
        swPromotions.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.togglePromotions(isChecked));
        
        swSystemAlerts.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleSystemAlerts(isChecked));
        
        // Location & Map Listeners
        swUseGps.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleUseGps(isChecked));
        
        rgMapView.setOnCheckedChangeListener((group, checkedId) -> {
            String mapView;
            if (checkedId == R.id.rb_map_view) {
                mapView = "map";
            } else if (checkedId == R.id.rb_list_view) {
                mapView = "list";
            } else {
                mapView = "both";
            }
            viewModel.setMapViewPreference(mapView);
        });
        
        // Privacy & Data Listeners
        swShareData.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleDataSharing(isChecked));
        
        swRecommendations.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleRecommendations(isChecked));
        
        btnClearSearch.setOnClickListener(v -> showClearSearchConfirmation());
        btnClearViewed.setOnClickListener(v -> showClearViewedConfirmation());
        
        // Display & Theme Listeners
        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            String themeMode;
            if (checkedId == R.id.rb_light_theme) {
                themeMode = "light";
            } else if (checkedId == R.id.rb_dark_theme) {
                themeMode = "dark";
            } else {
                themeMode = "system";
            }
            viewModel.setThemeMode(themeMode);
        });
        
        swHighContrast.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleHighContrast(isChecked));
        
        btnLanguage.setOnClickListener(v -> showLanguageDialog());
        
        // Order Preferences Listeners
        rgOrderSort.setOnCheckedChangeListener((group, checkedId) -> {
            String sortBy;
            if (checkedId == R.id.rb_oldest_first) {
                sortBy = "oldest";
            } else if (checkedId == R.id.rb_status_based) {
                sortBy = "status";
            } else {
                sortBy = "newest";
            }
            viewModel.setOrderSort(sortBy);
        });
        
        swOrderSuggestions.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.toggleOrderSuggestions(isChecked));
        
        // Help & Legal Listeners
        btnHelpSupport.setOnClickListener(v -> navigateToHelpSupport());
        btnTerms.setOnClickListener(v -> navigateToTermsConditions());
        btnPrivacy.setOnClickListener(v -> navigateToPrivacyPolicy());
        btnAbout.setOnClickListener(v -> navigateToAbout());
    }

    private void showLanguageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_language);
        
        CharSequence[] languages = {
                getString(R.string.english),
                getString(R.string.hindi),
                getString(R.string.marathi)
        };
        
        String[] languageCodes = {"en", "hi", "mr"};
        
        builder.setItems(languages, (dialog, which) -> {
            viewModel.setAppLanguage(languageCodes[which]);
        });
        
        builder.show();
    }

    private void showClearSearchConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_search_history)
                .setMessage(R.string.confirm_clear_search)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.clearSearchHistory();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void showClearViewedConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.clear_recently_viewed)
                .setMessage(R.string.confirm_clear_viewed)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    viewModel.clearRecentlyViewed();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    // Navigation Methods
    private void navigateToEditProfile() {
        try {
            Intent intent = new Intent(this, EditPersonalInfoActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to edit profile", e);
            Toast.makeText(this, R.string.error_opening_profile, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPhoneVerification() {
        try {
            Intent intent = new Intent(this, PhoneVerificationActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to phone verification", e);
            Toast.makeText(this, R.string.error_opening_verification, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToSavedAddresses() {
        try {
            Intent intent = new Intent(this, SavedAddressesActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to saved addresses", e);
            Toast.makeText(this, R.string.error_opening_addresses, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPaymentMethods() {
        try {
            Intent intent = new Intent(this, PaymentMethodsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to payment methods", e);
            Toast.makeText(this, R.string.error_opening_payment, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToHelpSupport() {
        try {
            Intent intent = new Intent(this, CustomerHelpSupportActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to help support", e);
            Toast.makeText(this, R.string.error_opening_help, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToTermsConditions() {
        try {
            Intent intent = new Intent(this, TermsConditionsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to terms & conditions", e);
            Toast.makeText(this, R.string.terms_opening_soon, Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToPrivacyPolicy() {
        try {
            Intent intent = new Intent(this, PrivacyPolicyActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open Privacy Policy: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToAbout() {
        try {
            Intent intent = new Intent(this, AboutFoodVanActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to about screen", e);
            Toast.makeText(this, R.string.about_opening_soon, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
