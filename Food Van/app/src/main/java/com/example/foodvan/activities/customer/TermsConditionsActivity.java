package com.example.foodvan.activities.customer;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.foodvan.R;
import com.example.foodvan.models.TermsConditions;
import com.example.foodvan.repositories.TermsConditionsRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TermsConditionsActivity - Displays the Terms & Conditions screen
 */
public class TermsConditionsActivity extends AppCompatActivity {
    private static final String TAG = "TermsConditionsActivity";
    
    private Toolbar toolbar;
    private TextView tvTermsTitle;
    private TextView tvTermsContent;
    private TextView tvTermsVersion;
    private TextView tvLastUpdated;
    
    private TermsConditionsRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_conditions);
        
        initializeViews();
        setupToolbar();
        setupRepository();
        loadTermsConditions();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvTermsTitle = findViewById(R.id.tv_terms_title);
        tvTermsContent = findViewById(R.id.tv_terms_content);
        tvTermsVersion = findViewById(R.id.tv_terms_version);
        tvLastUpdated = findViewById(R.id.tv_last_updated);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.terms_conditions);
        }
    }

    private void setupRepository() {
        repository = new TermsConditionsRepository(this);
    }

    private void loadTermsConditions() {
        repository.fetchTermsConditions(new TermsConditionsRepository.TermsConditionsCallback() {
            @Override
            public void onSuccess(TermsConditions termsConditions) {
                updateUI(termsConditions);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load Terms & Conditions: " + error);
                Toast.makeText(TermsConditionsActivity.this, 
                    "Failed to load Terms & Conditions. Showing cached version.", 
                    Toast.LENGTH_LONG).show();
                // Show cached version if available
                TermsConditions cached = repository.getCachedTerms();
                if (cached != null) {
                    updateUI(cached);
                }
            }
        });
    }

    private void updateUI(TermsConditions terms) {
        if (terms != null) {
            tvTermsTitle.setText(terms.getTitle());
            tvTermsContent.setText(formatTermsContent(terms.getContent()));
            tvTermsVersion.setText(getString(R.string.version_format, terms.getVersion()));
            
            // Format and display last updated time
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date(terms.getLastUpdated()));
            tvLastUpdated.setText(getString(R.string.last_updated, formattedDate));
        }
    }

    /**
     * Format the terms content with proper spacing and structure
     */
    private String formatTermsContent(String content) {
        if (content == null) return "";
        
        // Replace double newlines with section breaks for better readability
        return content.replace("\n\n", "\n\n\n");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}