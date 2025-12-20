package com.example.foodvan.activities.vendor;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodvan.R;
import com.example.foodvan.fragments.vendor.address.AddNewAddressFragment;
import com.example.foodvan.fragments.vendor.address.DeleteAddressFragment;
import com.example.foodvan.fragments.vendor.address.EditPrimaryAddressFragment;
import com.example.foodvan.fragments.vendor.address.ViewSavedAddressesFragment;
import com.example.foodvan.repositories.AddressRepository;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Vendor Saved Addresses Activity with Material Design 3
 * Manages vendor business locations and addresses
 */
public class VendorSavedAddressesActivity extends AppCompatActivity {

    private static final String TAG = "VendorSavedAddresses";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private AddressPagerAdapter pagerAdapter;

    // Data & Services
    private SessionManager sessionManager;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference addressesRef;
    private AddressRepository addressRepository;

    // Tab titles
    private final String[] tabTitles = {
        "View Addresses",
        "Add New",
        "Edit Primary",
        "Delete"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_saved_addresses);

        initializeComponents();
        initializeViews();
        setupToolbar();
        setupViewPager();
        setupTabLayout();
    }

    private void initializeComponents() {
        sessionManager = new SessionManager(this);
        firebaseAuth = FirebaseAuth.getInstance();

        String vendorId = sessionManager.getUserId();
        if (vendorId != null) {
            addressesRef = FirebaseDatabase.getInstance().getReference("vendor_addresses").child(vendorId);
            addressRepository = new AddressRepository(vendorId);
        }
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
            getSupportActionBar().setTitle("Save Address");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        pagerAdapter = new AddressPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(4); // Keep all fragments in memory
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    // ViewPager2 Adapter
    private static class AddressPagerAdapter extends FragmentStateAdapter {

        public AddressPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new ViewSavedAddressesFragment();
                case 1:
                    return new AddNewAddressFragment();
                case 2:
                    return new EditPrimaryAddressFragment();
                case 3:
                    return new DeleteAddressFragment();
                default:
                    return new ViewSavedAddressesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4; // Number of tabs
        }
    }

    // Utility methods for fragments to use
    public void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.success_color))
            .show();
    }

    public void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error_color))
            .show();
    }

    public void showInfo(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.info_color))
            .show();
    }

    // Getters for fragments
    public DatabaseReference getAddressesRef() {
        return addressesRef;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    public AddressRepository getAddressRepository() {
        return addressRepository;
    }
    
    // Method to switch tabs programmatically
    public void switchToTab(int tabIndex) {
        if (viewPager != null && tabIndex >= 0 && tabIndex < 4) {
            viewPager.setCurrentItem(tabIndex, true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
