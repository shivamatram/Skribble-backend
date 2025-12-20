package com.example.foodvan.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.foodvan.fragments.vendor.VendorProfileTabFragment;
import com.example.foodvan.fragments.vendor.VendorPrivacySettingsFragment;
import com.example.foodvan.fragments.vendor.ChangePasswordFragment;
import com.example.foodvan.fragments.vendor.AccountSettingsFragment;

/**
 * ViewPager2 Adapter for Vendor Profile Edit Activity
 * Handles switching between all profile management tabs
 */
public class VendorProfilePagerAdapter extends FragmentStateAdapter {

    public VendorProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new VendorProfileTabFragment();
            case 1:
                return new ChangePasswordFragment();
            case 2:
                return new AccountSettingsFragment();
            case 3:
                return new VendorPrivacySettingsFragment();
            default:
                return new VendorProfileTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Edit Profile, Change Password, Account Settings, Privacy Settings
    }

    public String getTabTitle(int position) {
        switch (position) {
            case 0:
                return "Edit Profile";
            case 1:
                return "Change Password";
            case 2:
                return "Account Settings";
            case 3:
                return "Privacy Settings";
            default:
                return "Edit Profile";
        }
    }
}
