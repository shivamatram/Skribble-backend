package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodvan.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

/**
 * AboutActivity - Displays comprehensive app information
 * Features: App details, version info, developer information, contact options
 * Material Design 3 implementation with professional presentation
 */
public class AboutActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvAppName;
    private TextView tvAppVersion;
    private TextView tvBuildInfo;
    private TextView tvDeveloperInfo;
    private TextView tvAppDescription;
    private MaterialButton btnContactEmail;
    private MaterialButton btnPrivacyPolicy;
    private MaterialButton btnTermsOfService;
    private MaterialButton btnRateApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initializeViews();
        setupToolbar();
        setupAppInfo();
        setupContactOptions();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar_about);
        tvAppName = findViewById(R.id.tv_app_name);
        tvAppVersion = findViewById(R.id.tv_app_version);
        tvBuildInfo = findViewById(R.id.tv_build_info);
        tvDeveloperInfo = findViewById(R.id.tv_developer_info);
        tvAppDescription = findViewById(R.id.tv_app_description);
        btnContactEmail = findViewById(R.id.btn_contact_email);
        btnPrivacyPolicy = findViewById(R.id.btn_privacy_policy);
        btnTermsOfService = findViewById(R.id.btn_terms_of_service);
        btnRateApp = findViewById(R.id.btn_rate_app);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("About");
        }
        
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void setupAppInfo() {
        // Set app name
        tvAppName.setText("Food Van");
        
        // Set app description
        tvAppDescription.setText("Food Van connects local food vendors with hungry customers, " +
                "providing real-time location tracking, easy ordering, secure payments, and " +
                "comprehensive feedback systems. Discover delicious street food, track your " +
                "favorite vendors, and enjoy fresh meals delivered right to your location.");
        
        // Set developer information
        tvDeveloperInfo.setText("Developed by Shivam Atram & Team\n" +
                "© 2024 Food Van App. All rights reserved.");
        
        // Get and display version information
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvAppVersion.setText("Version " + packageInfo.versionName);
            tvBuildInfo.setText("Build " + packageInfo.versionCode + " • " + 
                    "API Level " + android.os.Build.VERSION.SDK_INT);
        } catch (PackageManager.NameNotFoundException e) {
            tvAppVersion.setText("Version 1.0.0");
            tvBuildInfo.setText("Build 1 • API Level " + android.os.Build.VERSION.SDK_INT);
        }
    }

    private void setupContactOptions() {
        // Contact Email
        btnContactEmail.setOnClickListener(v -> openContactEmail());
        
        // Privacy Policy
        btnPrivacyPolicy.setOnClickListener(v -> openPrivacyPolicy());
        
        // Terms of Service
        btnTermsOfService.setOnClickListener(v -> openTermsOfService());
        
        // Rate App
        btnRateApp.setOnClickListener(v -> openRateApp());
    }

    private void openContactEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@foodvanapp.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Food Van App - General Inquiry");
            emailIntent.putExtra(Intent.EXTRA_TEXT, generateContactEmailTemplate());
            
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, "Send Email"));
            } else {
                // Fallback: copy email to clipboard and show message
                android.content.ClipboardManager clipboard = 
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Email", "support@foodvanapp.com");
                clipboard.setPrimaryClip(clip);
                
                showMessage("Email copied to clipboard: support@foodvanapp.com");
            }
        } catch (Exception e) {
            showMessage("Unable to open email client. Please contact: support@foodvanapp.com");
        }
    }

    private void openPrivacyPolicy() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://foodvanapp.com/privacy-policy"));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                showMessage("Privacy Policy: https://foodvanapp.com/privacy-policy");
            }
        } catch (Exception e) {
            showMessage("Privacy Policy available at: https://foodvanapp.com/privacy-policy");
        }
    }

    private void openTermsOfService() {
        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://foodvanapp.com/terms-of-service"));
            if (browserIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(browserIntent);
            } else {
                showMessage("Terms of Service: https://foodvanapp.com/terms-of-service");
            }
        } catch (Exception e) {
            showMessage("Terms of Service available at: https://foodvanapp.com/terms-of-service");
        }
    }

    private void openRateApp() {
        try {
            // Try to open Play Store app page
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://details?id=" + getPackageName()));
            if (playStoreIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(playStoreIntent);
            } else {
                // Fallback to web browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, 
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(browserIntent);
            }
        } catch (Exception e) {
            showMessage("Please rate us on Google Play Store");
        }
    }

    private String generateContactEmailTemplate() {
        return "Hi Food Van Team,\n\n" +
               "[Please describe your inquiry or feedback here]\n\n" +
               "App Information:\n" +
               "Version: " + getAppVersion() + "\n" +
               "Device: " + android.os.Build.MODEL + "\n" +
               "Android Version: " + android.os.Build.VERSION.RELEASE + "\n" +
               "Manufacturer: " + android.os.Build.MANUFACTURER + "\n\n" +
               "Thank you!";
    }

    private String getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0.0";
        }
    }

    private void showMessage(String message) {
        com.google.android.material.snackbar.Snackbar.make(
            findViewById(android.R.id.content), 
            message, 
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Add smooth exit animation back to profile
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any resources if needed
    }
}
