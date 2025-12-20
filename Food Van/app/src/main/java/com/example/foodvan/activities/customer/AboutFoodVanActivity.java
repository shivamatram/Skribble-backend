package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.ChangelogAdapter;
import com.example.foodvan.adapters.TeamMemberAdapter;
import com.example.foodvan.models.ChangelogEntry;
import com.example.foodvan.models.TeamMember;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AboutFoodVanActivity - Displays information about Food Van app
 */
public class AboutFoodVanActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvVersion;
    private TextView tvBuild;
    private TextView tvCopyright;
    
    // Quick Actions
    private LinearLayout btnRateApp;
    private LinearLayout btnShareApp;
    private LinearLayout btnContactSupport;
    private LinearLayout btnSendFeedback;
    
    // Social Media
    private MaterialButton btnInstagram;
    private MaterialButton btnTwitter;
    private MaterialButton btnWhatsapp;
    private MaterialButton btnWebsite;
    
    // RecyclerViews
    private RecyclerView rvTeamMembers;
    private RecyclerView rvChangelog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_food_van);
        
        initializeViews();
        setupToolbar();
        setupClickListeners();
        loadData();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvVersion = findViewById(R.id.tv_version);
        tvBuild = findViewById(R.id.tv_build);
        tvCopyright = findViewById(R.id.tv_copyright);
        
        // Quick Actions
        btnRateApp = findViewById(R.id.btn_rate_app);
        btnShareApp = findViewById(R.id.btn_share_app);
        btnContactSupport = findViewById(R.id.btn_contact_support);
        btnSendFeedback = findViewById(R.id.btn_send_feedback);
        
        // Social Media
        btnInstagram = findViewById(R.id.btn_instagram);
        btnTwitter = findViewById(R.id.btn_twitter);
        btnWhatsapp = findViewById(R.id.btn_whatsapp);
        btnWebsite = findViewById(R.id.btn_website);
        
        // RecyclerViews
        rvTeamMembers = findViewById(R.id.rv_team_members);
        rvChangelog = findViewById(R.id.rv_changelog);
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
        // Quick Actions
        btnRateApp.setOnClickListener(v -> openPlayStore());
        btnShareApp.setOnClickListener(v -> shareApp());
        btnContactSupport.setOnClickListener(v -> openEmail());
        btnSendFeedback.setOnClickListener(v -> openFeedback());
        
        // Social Media
        btnInstagram.setOnClickListener(v -> openUrl("https://instagram.com/foodvan"));
        btnTwitter.setOnClickListener(v -> openUrl("https://twitter.com/foodvan"));
        btnWhatsapp.setOnClickListener(v -> openWhatsApp());
        btnWebsite.setOnClickListener(v -> openUrl("https://foodvan.com"));
    }

    private void loadData() {
        // Set version info
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            tvVersion.setText(versionName);
            tvBuild.setText(String.valueOf(versionCode));
        } catch (Exception e) {
            tvVersion.setText("2.1.0");
            tvBuild.setText("203");
        }
        
        // Set copyright
        tvCopyright.setText(getString(R.string.default_copyright));
        
        // Setup Team Members RecyclerView
        setupTeamMembers();
        
        // Setup Changelog RecyclerView
        setupChangelog();
    }

    private void setupTeamMembers() {
        List<TeamMember> teamMembers = new ArrayList<>();
        teamMembers.add(new TeamMember("Development Team", "Android Development", 
            "Bringing Food Van to life with cutting-edge mobile technology", ""));
        teamMembers.add(new TeamMember("Design Team", "UI/UX Design", 
            "Creating beautiful and intuitive user experiences", ""));
        teamMembers.add(new TeamMember("QA Team", "Quality Assurance", 
            "Ensuring Food Van delivers excellent performance", ""));
        
        rvTeamMembers.setLayoutManager(new LinearLayoutManager(this));
        rvTeamMembers.setNestedScrollingEnabled(false);
        rvTeamMembers.setAdapter(new TeamMemberAdapter(teamMembers));
    }

    private void setupChangelog() {
        List<ChangelogEntry> changelog = new ArrayList<>();
        changelog.add(new ChangelogEntry("2.1.0", "Dec 9, 2025", 
            "Added comprehensive About screen with team credits", Arrays.asList("New About screen", "Team credits", "Social links")));
        changelog.add(new ChangelogEntry("2.0.5", "Dec 5, 2025", 
            "Improved UI with Material 3 design updates", Arrays.asList("Material 3 styling", "Enhanced cards")));
        changelog.add(new ChangelogEntry("2.0.0", "Dec 1, 2025", 
            "Major update with enhanced ordering system", Arrays.asList("New ordering flow", "Vendor dashboard")));
        
        rvChangelog.setLayoutManager(new LinearLayoutManager(this));
        rvChangelog.setNestedScrollingEnabled(false);
        rvChangelog.setAdapter(new ChangelogAdapter(changelog));
    }

    private void openPlayStore() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, 
                Uri.parse("market://details?id=" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
            } catch (Exception ex) {
                Toast.makeText(this, R.string.error_opening_store, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_title));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_content));
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_app)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_sharing_app, Toast.LENGTH_SHORT).show();
        }
    }

    private void openEmail() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:support@foodvan.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Food Van Support");
            startActivity(emailIntent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_opening_email, Toast.LENGTH_SHORT).show();
        }
    }

    private void openFeedback() {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:feedback@foodvan.com"));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Food Van Feedback");
            startActivity(emailIntent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_opening_email, Toast.LENGTH_SHORT).show();
        }
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_opening_website, Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsApp() {
        try {
            String url = "https://wa.me/1234567890";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_opening_whatsapp, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}