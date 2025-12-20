package com.example.foodvan.activities.vendor;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodvan.R;
import com.example.foodvan.fragments.vendor.AccountSettingsFragment;
import com.example.foodvan.fragments.vendor.ChangePasswordFragment;
import com.example.foodvan.fragments.vendor.EditProfileFragment;
import com.example.foodvan.fragments.vendor.PrivacySettingsFragment;
import com.example.foodvan.utils.FragmentTestHelper;

import android.util.Log;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Vendor Edit Profile Activity with Material Design 3
 * Contains four tabs: Edit Profile, Change Password, Account Settings, Privacy Settings
 */
public class VendorEditProfileActivity extends AppCompatActivity {

    private static final String TAG = "VendorEditProfile";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private EditProfilePagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {
        "Edit Profile",
        "Change Password", 
        "Account Settings",
        "Privacy Settings"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_edit_profile);

        initializeViews();
        setupToolbar();
        setupViewPager();
        setupTabLayout();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        pagerAdapter = new EditProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4); // Keep all fragments in memory
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * ViewPager2 Adapter for Edit Profile Tabs
     */
    private static class EditProfilePagerAdapter extends FragmentStateAdapter {

        public EditProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            Fragment fragment;
            String fragmentName;
            
            switch (position) {
                case 0:
                    fragment = new EditProfileFragment();
                    fragmentName = "EditProfileFragment";
                    break;
                case 1:
                    fragment = new ChangePasswordFragment();
                    fragmentName = "ChangePasswordFragment";
                    break;
                case 2:
                    fragment = new AccountSettingsFragment();
                    fragmentName = "AccountSettingsFragment";
                    break;
                case 3:
                    fragment = new PrivacySettingsFragment();
                    fragmentName = "PrivacySettingsFragment";
                    break;
                default:
                    fragment = new EditProfileFragment();
                    fragmentName = "EditProfileFragment (default)";
                    break;
            }
            
            Log.d("VendorEditProfile", "Creating fragment at position " + position + ": " + fragmentName);
            FragmentTestHelper.logFragmentCreation(fragment, fragmentName);
            
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 4; // Four tabs
        }
    }
}
