package com.example.foodvan.activities.vendor;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.foodvan.R;
import com.example.foodvan.fragments.vendor.BankAccountDetailsFragment;
import com.example.foodvan.fragments.vendor.PaymentHistoryFragment;
import com.example.foodvan.fragments.vendor.PayoutSettingsFragment;
import com.example.foodvan.fragments.vendor.UpiDetailsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Payment & Payout Activity with Material Design 3
 * Manages vendor payment methods and payout settings with 4 tabs:
 * 1. Bank Account Details
 * 2. UPI Details  
 * 3. Payment History
 * 4. Payout Settings
 */
public class PaymentPayoutActivity extends AppCompatActivity {

    private static final String TAG = "PaymentPayout";

    // UI Components
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private PaymentPagerAdapter pagerAdapter;

    // Tab titles
    private final String[] tabTitles = {
        "Bank Account",
        "UPI Details", 
        "Payment History",
        "Payout Settings"
    };

    // Tab icons
    private final int[] tabIcons = {
        R.drawable.ic_account_balance,
        R.drawable.ic_payment,
        R.drawable.ic_history,
        R.drawable.ic_settings
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_payout);

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
            getSupportActionBar().setTitle("Payment & Payout");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewPager() {
        pagerAdapter = new PaymentPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
    }

    private void setupTabs() {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
            tab.setIcon(tabIcons[position]);
        }).attach();
    }

    /**
     * ViewPager2 adapter for Payment & Payout tabs
     */
    private static class PaymentPagerAdapter extends FragmentStateAdapter {

        public PaymentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new BankAccountDetailsFragment();
                case 1:
                    return new UpiDetailsFragment();
                case 2:
                    return new PaymentHistoryFragment();
                case 3:
                    return new PayoutSettingsFragment();
                default:
                    return new BankAccountDetailsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
