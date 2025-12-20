package com.example.foodvan.fragments.vendor.location;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.models.GpsSettings;
import com.example.foodvan.viewmodels.LocationViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * Fragment for GPS settings and preferences
 * Allows vendors to configure location update behavior
 */
public class GpsSettingsFragment extends Fragment {

    private static final String TAG = "GpsSettings";

    // UI Components
    private SwitchMaterial switchAutoUpdate;
    private SwitchMaterial switchHighAccuracy;
    private RadioGroup rgUpdateFrequency;
    private MaterialButton btnOpenGpsSettings;
    private MaterialButton btnSaveSettings;

    // ViewModel
    private LocationViewModel locationViewModel;

    // Current settings
    private GpsSettings currentSettings;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationViewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gps_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
        observeViewModel();
        loadGpsSettings();
    }

    private void initializeViews(View view) {
        switchAutoUpdate = view.findViewById(R.id.switch_auto_update);
        switchHighAccuracy = view.findViewById(R.id.switch_high_accuracy);
        rgUpdateFrequency = view.findViewById(R.id.rg_update_frequency);
        btnOpenGpsSettings = view.findViewById(R.id.btn_open_gps_settings);
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
    }

    private void setupClickListeners() {
        btnOpenGpsSettings.setOnClickListener(v -> openDeviceGpsSettings());
        btnSaveSettings.setOnClickListener(v -> saveGpsSettings());

        // Enable/disable frequency options based on auto update toggle
        switchAutoUpdate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            rgUpdateFrequency.setEnabled(isChecked);
            for (int i = 0; i < rgUpdateFrequency.getChildCount(); i++) {
                rgUpdateFrequency.getChildAt(i).setEnabled(isChecked);
            }
        });
    }

    private void observeViewModel() {
        // Observe GPS settings
        locationViewModel.getGpsSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                currentSettings = settings;
                populateSettingsForm(settings);
            }
        });

        // Observe loading state
        locationViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSaveSettings.setEnabled(!isLoading);
            if (isLoading) {
                btnSaveSettings.setText("Saving...");
            } else {
                btnSaveSettings.setText("Save GPS Settings");
            }
        });

        // Observe error messages
        locationViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    private void loadGpsSettings() {
        locationViewModel.loadGpsSettings();
    }

    private void populateSettingsForm(GpsSettings settings) {
        // Set switch states
        switchAutoUpdate.setChecked(settings.isAutoUpdate());
        switchHighAccuracy.setChecked(settings.isHighAccuracy());

        // Set frequency radio button
        int updateInterval = settings.getUpdateInterval();
        switch (updateInterval) {
            case 1:
                rgUpdateFrequency.check(R.id.rb_1_minute);
                break;
            case 5:
                rgUpdateFrequency.check(R.id.rb_5_minutes);
                break;
            case 10:
                rgUpdateFrequency.check(R.id.rb_10_minutes);
                break;
            default:
                rgUpdateFrequency.check(R.id.rb_5_minutes); // Default to 5 minutes
                break;
        }

        // Enable/disable frequency options based on auto update
        rgUpdateFrequency.setEnabled(settings.isAutoUpdate());
        for (int i = 0; i < rgUpdateFrequency.getChildCount(); i++) {
            rgUpdateFrequency.getChildAt(i).setEnabled(settings.isAutoUpdate());
        }
    }

    private void saveGpsSettings() {
        if (currentSettings == null) {
            currentSettings = new GpsSettings();
        }

        // Get values from form
        boolean autoUpdate = switchAutoUpdate.isChecked();
        boolean highAccuracy = switchHighAccuracy.isChecked();
        int updateInterval = getSelectedUpdateInterval();

        // Update settings object
        currentSettings.setAutoUpdate(autoUpdate);
        currentSettings.setHighAccuracy(highAccuracy);
        currentSettings.setUpdateInterval(updateInterval);

        // Save to repository
        locationViewModel.saveGpsSettings(currentSettings);
        showSuccess("GPS settings saved successfully!");
    }

    private int getSelectedUpdateInterval() {
        int checkedId = rgUpdateFrequency.getCheckedRadioButtonId();
        
        if (checkedId == R.id.rb_1_minute) {
            return 1;
        } else if (checkedId == R.id.rb_5_minutes) {
            return 5;
        } else if (checkedId == R.id.rb_10_minutes) {
            return 10;
        } else {
            return 5; // Default to 5 minutes
        }
    }

    private void openDeviceGpsSettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback to general settings if location settings not available
            try {
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                showSuccess("Please enable location services in your device settings.");
            } catch (Exception ex) {
                showError("Unable to open device settings. Please enable location services manually.");
            }
        }
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.success_color, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error_color, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
        }
    }
}
