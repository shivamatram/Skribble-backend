package com.example.foodvan.fragments.vendor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.models.PayoutSettings;
import com.example.foodvan.viewmodels.PaymentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for managing payout settings
 * Features:
 * - Payout method selection (Bank/UPI)
 * - Auto payout toggle
 * - Payout frequency settings
 * - Minimum payout threshold
 * - Next payout schedule display
 */
public class PayoutSettingsFragment extends Fragment {

    private static final String TAG = "PayoutSettings";

    // UI Components
    private MaterialCardView cardCurrentSettings, cardPayoutSettings;
    private SwitchMaterial switchAutoPayout;
    private TextInputLayout tilPayoutMethod, tilPayoutFrequency, tilMinThreshold;
    private AutoCompleteTextView etPayoutMethod, etPayoutFrequency;
    private TextInputEditText etMinThreshold;
    private MaterialButton btnSaveSettings;
    private TextView tvNextPayout, tvCurrentMethod, tvCurrentFrequency, tvCurrentThreshold;

    // Data & Services
    private PaymentViewModel paymentViewModel;
    private PayoutSettings currentSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payout_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        initializeViewModel();
        setupDropdowns();
        setupClickListeners();
        observeData();
        loadPayoutSettings();
    }

    private void initializeViews(View view) {
        // Cards
        cardCurrentSettings = view.findViewById(R.id.card_current_settings);
        cardPayoutSettings = view.findViewById(R.id.card_payout_settings);

        // Current settings display
        tvNextPayout = view.findViewById(R.id.tv_next_payout);
        tvCurrentMethod = view.findViewById(R.id.tv_current_method);
        tvCurrentFrequency = view.findViewById(R.id.tv_current_frequency);
        tvCurrentThreshold = view.findViewById(R.id.tv_current_threshold);

        // Input fields
        switchAutoPayout = view.findViewById(R.id.switch_auto_payout);
        tilPayoutMethod = view.findViewById(R.id.til_payout_method);
        tilPayoutFrequency = view.findViewById(R.id.til_payout_frequency);
        tilMinThreshold = view.findViewById(R.id.til_min_threshold);
        etPayoutMethod = view.findViewById(R.id.et_payout_method);
        etPayoutFrequency = view.findViewById(R.id.et_payout_frequency);
        etMinThreshold = view.findViewById(R.id.et_min_threshold);

        // Buttons
        btnSaveSettings = view.findViewById(R.id.btn_save_settings);
    }

    private void initializeViewModel() {
        paymentViewModel = new ViewModelProvider(requireActivity()).get(PaymentViewModel.class);
    }

    private void setupDropdowns() {
        // Payout method dropdown
        String[] payoutMethods = {
            "Bank Transfer",
            "UPI Transfer"
        };
        ArrayAdapter<String> methodAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, payoutMethods);
        etPayoutMethod.setAdapter(methodAdapter);

        // Payout frequency dropdown
        String[] frequencies = {
            "Daily",
            "Weekly", 
            "Monthly"
        };
        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, frequencies);
        etPayoutFrequency.setAdapter(frequencyAdapter);
    }

    private void setupClickListeners() {
        btnSaveSettings.setOnClickListener(v -> savePayoutSettings());
        
        switchAutoPayout.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateFieldsVisibility(isChecked);
            if (currentSettings != null) {
                updateNextPayoutDisplay();
            }
        });
    }

    private void observeData() {
        paymentViewModel.getPayoutSettings().observe(getViewLifecycleOwner(), settings -> {
            currentSettings = settings;
            updateUI();
        });

        paymentViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnSaveSettings.setEnabled(!isLoading);
        });

        paymentViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });

        paymentViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSuccess(message);
            }
        });
    }

    private void loadPayoutSettings() {
        paymentViewModel.loadPayoutSettings();
    }

    private void updateUI() {
        if (currentSettings != null) {
            // Show current settings card
            cardCurrentSettings.setVisibility(View.VISIBLE);
            
            // Populate current settings display
            populateCurrentSettings();
            
            // Populate form fields
            populateFormFields();
        } else {
            // Hide current settings card if no settings exist
            cardCurrentSettings.setVisibility(View.GONE);
            
            // Set default values
            setDefaultValues();
        }
        
        updateNextPayoutDisplay();
    }

    private void populateCurrentSettings() {
        if (currentSettings == null) return;

        tvCurrentMethod.setText(currentSettings.getPayoutMethod());
        tvCurrentFrequency.setText(currentSettings.getPayoutFrequency());
        tvCurrentThreshold.setText("₹" + currentSettings.getMinThreshold());
    }

    private void populateFormFields() {
        if (currentSettings == null) return;

        switchAutoPayout.setChecked(currentSettings.isAutoPayoutEnabled());
        etPayoutMethod.setText(currentSettings.getPayoutMethod(), false);
        etPayoutFrequency.setText(currentSettings.getPayoutFrequency(), false);
        etMinThreshold.setText(String.valueOf(currentSettings.getMinThreshold()));
        
        updateFieldsVisibility(currentSettings.isAutoPayoutEnabled());
    }

    private void setDefaultValues() {
        switchAutoPayout.setChecked(false);
        etPayoutMethod.setText("Bank Transfer", false);
        etPayoutFrequency.setText("Weekly", false);
        etMinThreshold.setText("500");
        
        updateFieldsVisibility(false);
    }

    private void updateFieldsVisibility(boolean autoPayoutEnabled) {
        int visibility = autoPayoutEnabled ? View.VISIBLE : View.GONE;
        tilPayoutFrequency.setVisibility(visibility);
        tilMinThreshold.setVisibility(visibility);
    }

    private void updateNextPayoutDisplay() {
        if (!switchAutoPayout.isChecked()) {
            tvNextPayout.setText("Manual payout only");
            return;
        }

        String frequency = etPayoutFrequency.getText().toString();
        if (TextUtils.isEmpty(frequency)) {
            tvNextPayout.setText("Select frequency to see next payout date");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        
        switch (frequency) {
            case "Daily":
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Weekly":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "Monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String nextPayoutDate = sdf.format(calendar.getTime());
        tvNextPayout.setText("Next payout scheduled on " + nextPayoutDate);
    }

    private void savePayoutSettings() {
        if (!validateInputs()) return;

        PayoutSettings settings = new PayoutSettings();
        settings.setAutoPayoutEnabled(switchAutoPayout.isChecked());
        settings.setPayoutMethod(etPayoutMethod.getText().toString().trim());
        
        if (switchAutoPayout.isChecked()) {
            settings.setPayoutFrequency(etPayoutFrequency.getText().toString().trim());
            settings.setMinThreshold(Double.parseDouble(etMinThreshold.getText().toString().trim()));
        } else {
            settings.setPayoutFrequency("Manual");
            settings.setMinThreshold(0);
        }
        
        settings.setLastUpdated(System.currentTimeMillis());

        paymentViewModel.savePayoutSettings(settings);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        // Validate payout method
        String payoutMethod = etPayoutMethod.getText().toString().trim();
        if (TextUtils.isEmpty(payoutMethod)) {
            tilPayoutMethod.setError("Please select a payout method");
            isValid = false;
        }

        // Validate auto payout settings if enabled
        if (switchAutoPayout.isChecked()) {
            // Validate frequency
            String frequency = etPayoutFrequency.getText().toString().trim();
            if (TextUtils.isEmpty(frequency)) {
                tilPayoutFrequency.setError("Please select payout frequency");
                isValid = false;
            }

            // Validate minimum threshold
            String thresholdStr = etMinThreshold.getText().toString().trim();
            if (TextUtils.isEmpty(thresholdStr)) {
                tilMinThreshold.setError("Please enter minimum threshold amount");
                isValid = false;
            } else {
                try {
                    double threshold = Double.parseDouble(thresholdStr);
                    if (threshold < 100) {
                        tilMinThreshold.setError("Minimum threshold should be at least ₹100");
                        isValid = false;
                    } else if (threshold > 50000) {
                        tilMinThreshold.setError("Minimum threshold cannot exceed ₹50,000");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    tilMinThreshold.setError("Please enter a valid amount");
                    isValid = false;
                }
            }
        }

        return isValid;
    }

    private void clearErrors() {
        tilPayoutMethod.setError(null);
        tilPayoutFrequency.setError(null);
        tilMinThreshold.setError(null);
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                .setBackgroundTint(requireContext().getColor(R.color.success_color))
                .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(requireContext().getColor(R.color.error_color))
                .show();
        }
    }
}
