package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.foodvan.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

/**
 * Privacy Policy Activity for Food Van App
 * Displays the privacy policy content with a clean, professional UI
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

    private static final String TAG = "PrivacyPolicyActivity";
    
    // UI Components
    private MaterialToolbar toolbar;
    private LinearLayout layoutDynamicContent;
    private TextView tvLastUpdated;
    private TextView tvVersion;
    private MaterialButton btnContactSupport;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        
        initializeViews();
        setupClickListeners();
        loadPrivacyPolicyContent();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutDynamicContent = findViewById(R.id.layout_dynamic_content);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
        tvVersion = findViewById(R.id.tv_version);
        btnContactSupport = findViewById(R.id.btn_contact_support);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        
        // Hide loading and error states, show content - with null checks
        View layoutLoading = findViewById(R.id.layout_loading);
        View layoutError = findViewById(R.id.layout_error);
        View layoutPrivacyContent = findViewById(R.id.layout_privacy_content);
        
        if (layoutLoading != null) layoutLoading.setVisibility(View.GONE);
        if (layoutError != null) layoutError.setVisibility(View.GONE);
        if (layoutPrivacyContent != null) layoutPrivacyContent.setVisibility(View.VISIBLE);
    }
    
    private void setupClickListeners() {
        btnContactSupport.setOnClickListener(v -> openContactSupport());
    }
    
    private void loadPrivacyPolicyContent() {
        // Update version info
        updateVersionInfo();
        
        // Create and display privacy policy sections
        createPrivacyPolicySections();
    }
    
    private void updateVersionInfo() {
        String version = "2.1.0";
        
        if (tvVersion != null) {
            tvVersion.setText("Version: " + version);
        }
        if (tvLastUpdated != null) {
            tvLastUpdated.setText("Last updated: Today");
        }
    }
    
    private void createPrivacyPolicySections() {
        if (layoutDynamicContent == null) {
            return;
        }
        
        // Clear existing content
        layoutDynamicContent.removeAllViews();
        
        // Add privacy policy sections with simple text
        String privacyContent = "Privacy Policy\n\n" +
                "Introduction\n" +
                "Welcome to Food Van, the mobile application that connects food lovers with local mobile food vendors. " +
                "This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use " +
                "our mobile application.\n\n" +
                
                "Information We Collect\n" +
                "We collect several types of information:\n" +
                "• Personal Information: Name, email, phone number\n" +
                "• Location Data: GPS location for vendor discovery\n" +
                "• Order Information: Purchase history and preferences\n" +
                "• Device Information: App usage statistics\n\n" +
                
                "How We Use Your Information\n" +
                "Your information enables core functionality:\n" +
                "• Account management and authentication\n" +
                "• Order processing and payment handling\n" +
                "• GPS-based vendor discovery\n" +
                "• Real-time order tracking\n\n" +
                
                "Information Sharing\n" +
                "We only share your information:\n" +
                "• With vendors for order fulfillment\n" +
                "• With payment processors for transactions\n" +
                "• As required by law\n\n" +
                
                "Data Security\n" +
                "We implement security measures including:\n" +
                "• Encryption in transit using HTTPS/TLS\n" +
                "• Secure cloud storage\n" +
                "• Regular security audits\n\n" +
                
                "Your Privacy Rights\n" +
                "You have the right to:\n" +
                "• Access and update your information\n" +
                "• Control location permissions\n" +
                "• Request data deletion\n\n" +
                
                "Contact Us\n" +
                "Email: support@foodvan.app\n" +
                "Help Center: Settings > Help & Support";
        
        TextView contentView = new TextView(this);
        contentView.setText(privacyContent);
        contentView.setTextSize(14f);
        contentView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        contentView.setPadding(32, 32, 32, 32);
        
        layoutDynamicContent.addView(contentView);
    }
    
    private void openContactSupport() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("message/rfc822");
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@foodvan.app"});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Privacy Policy Inquiry");
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi,\n\nI have a question regarding the Privacy Policy.\n\nRegards");
            
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open email client", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}