package com.example.foodvan.activities.vendor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodvan.R;
import com.example.foodvan.fragments.vendor.location.UpdateCurrentLocationFragment;
import com.example.foodvan.fragments.vendor.location.SetDefaultLocationFragment;
import com.example.foodvan.fragments.vendor.location.LocationHistoryFragment;
import com.example.foodvan.fragments.vendor.location.GpsSettingsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Live Location Update Activity with Material Design 3
 * Manages vendor location services with 4 tabs:
 * 1. Update Current Location
 * 2. Set Default Location
 * 3. Location History
 * 4. GPS Settings
 */
public class LiveLocationActivity extends AppCompatActivity {

    private static final String TAG = "LiveLocation";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private LocationPagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {
        "Update Location",
        "Default Location", 
        "Location History",
        "GPS Settings"
    };

    // Tab icons
    private final int[] tabIcons = {
        R.drawable.ic_my_location,
        R.drawable.ic_location_on,
        R.drawable.ic_history,
        R.drawable.ic_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);

        initializeViews();
        setupToolbar();
        setupViewPager();
        setupTabs();
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
            getSupportActionBar().setTitle("Live Location Update");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        pagerAdapter = new LocationPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();
    }

    /**
     * ViewPager2 adapter for Live Location tabs
     */
    private static class LocationPagerAdapter extends FragmentStateAdapter {

        public LocationPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new UpdateCurrentLocationFragment();
                case 1:
                    return new SetDefaultLocationFragment();
                case 2:
                    return new LocationHistoryFragment();
                case 3:
                    return new GpsSettingsFragment();
                default:
                    return new UpdateCurrentLocationFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }
}
