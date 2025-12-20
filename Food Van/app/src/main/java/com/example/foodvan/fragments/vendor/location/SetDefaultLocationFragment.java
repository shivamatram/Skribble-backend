package com.example.foodvan.fragments.vendor.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.models.DefaultLocation;
import com.example.foodvan.viewmodels.LocationViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;

/**
 * Fragment for setting default location
 * Allows vendors to manually set a fallback location when GPS is unavailable
 */
public class SetDefaultLocationFragment extends Fragment {

    private static final String TAG = "SetDefaultLocation";

    // UI Components
    private MaterialCardView cardCurrentDefault;
    private MaterialCardView cardMapPreview;
    private TextView tvCurrentAddress;
    private TextView tvCurrentCoordinates;
    private MaterialButton btnEditDefault;
    private MaterialButton btnUseCurrentLocation;
    private MaterialButton btnSaveDefaultLocation;
    private FrameLayout mapContainer;

    // Form Fields
    private TextInputLayout tilAddressLine1;
    private TextInputLayout tilAddressLine2;
    private TextInputLayout tilCity;
    private TextInputLayout tilState;
    private TextInputLayout tilPincode;
    private TextInputEditText etAddressLine1;
    private TextInputEditText etAddressLine2;
    private TextInputEditText etCity;
    private AutoCompleteTextView etState;
    private TextInputEditText etPincode;

    // ViewModel
    private LocationViewModel locationViewModel;

    // Indian States for dropdown
    private final String[] indianStates = {
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka",
        "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram",
        "Nagaland", "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu",
        "Telangana", "Tripura", "Uttar Pradesh", "Uttarakhand", "West Bengal",
        "Andaman and Nicobar Islands", "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu",
        "Delhi", "Jammu and Kashmir", "Ladakh", "Lakshadweep", "Puducherry"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationViewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_set_default_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupStateDropdown();
        setupClickListeners();
        observeViewModel();
        loadDefaultLocation();
    }

    private void initializeViews(View view) {
        cardCurrentDefault = view.findViewById(R.id.card_current_default);
        cardMapPreview = view.findViewById(R.id.card_map_preview);
        tvCurrentAddress = view.findViewById(R.id.tv_current_address);
        tvCurrentCoordinates = view.findViewById(R.id.tv_current_coordinates);
        btnEditDefault = view.findViewById(R.id.btn_edit_default);
        btnUseCurrentLocation = view.findViewById(R.id.btn_use_current_location);
        btnSaveDefaultLocation = view.findViewById(R.id.btn_save_default_location);
        mapContainer = view.findViewById(R.id.map_container);

        // Form fields
        tilAddressLine1 = view.findViewById(R.id.til_address_line1);
        tilAddressLine2 = view.findViewById(R.id.til_address_line2);
        tilCity = view.findViewById(R.id.til_city);
        tilState = view.findViewById(R.id.til_state);
        tilPincode = view.findViewById(R.id.til_pincode);
        etAddressLine1 = view.findViewById(R.id.et_address_line1);
        etAddressLine2 = view.findViewById(R.id.et_address_line2);
        etCity = view.findViewById(R.id.et_city);
        etState = view.findViewById(R.id.et_state);
        etPincode = view.findViewById(R.id.et_pincode);
    }

    private void setupStateDropdown() {
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            indianStates
        );
        etState.setAdapter(stateAdapter);
    }

    private void setupClickListeners() {
        btnEditDefault.setOnClickListener(v -> enableEditMode());
        btnUseCurrentLocation.setOnClickListener(v -> useCurrentLocation());
        btnSaveDefaultLocation.setOnClickListener(v -> saveDefaultLocation());
    }

    private void observeViewModel() {
        // Observe default location
        locationViewModel.getDefaultLocation().observe(getViewLifecycleOwner(), defaultLocation -> {
            if (defaultLocation != null) {
                displayDefaultLocation(defaultLocation);
            } else {
                hideCurrentDefaultCard();
            }
        });

        // Observe current location for "Use Current Location" feature
        locationViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), location -> {
            // This will be used when user clicks "Use Current Location"
        });

        // Observe error messages
        locationViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
            }
        });
    }

    private void loadDefaultLocation() {
        locationViewModel.loadDefaultLocation();
    }

    private void displayDefaultLocation(DefaultLocation defaultLocation) {
        cardCurrentDefault.setVisibility(View.VISIBLE);
        
        // Build address string
        StringBuilder addressBuilder = new StringBuilder();
        if (defaultLocation.getAddressLine1() != null && !defaultLocation.getAddressLine1().isEmpty()) {
            addressBuilder.append(defaultLocation.getAddressLine1());
        }
        if (defaultLocation.getAddressLine2() != null && !defaultLocation.getAddressLine2().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(defaultLocation.getAddressLine2());
        }
        if (defaultLocation.getCity() != null && !defaultLocation.getCity().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(defaultLocation.getCity());
        }
        if (defaultLocation.getState() != null && !defaultLocation.getState().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(", ");
            addressBuilder.append(defaultLocation.getState());
        }
        if (defaultLocation.getPincode() != null && !defaultLocation.getPincode().isEmpty()) {
            if (addressBuilder.length() > 0) addressBuilder.append(" - ");
            addressBuilder.append(defaultLocation.getPincode());
        }

        tvCurrentAddress.setText(addressBuilder.toString());
        
        // Display coordinates
        String coordinates = String.format(Locale.getDefault(), 
            "Lat: %.6f, Lng: %.6f", 
            defaultLocation.getLatitude(), 
            defaultLocation.getLongitude());
        tvCurrentCoordinates.setText(coordinates);

        // Populate form fields
        etAddressLine1.setText(defaultLocation.getAddressLine1());
        etAddressLine2.setText(defaultLocation.getAddressLine2());
        etCity.setText(defaultLocation.getCity());
        etState.setText(defaultLocation.getState(), false);
        etPincode.setText(defaultLocation.getPincode());
    }

    private void hideCurrentDefaultCard() {
        cardCurrentDefault.setVisibility(View.GONE);
    }

    private void enableEditMode() {
        // Form is already enabled, just focus on first field
        etAddressLine1.requestFocus();
        showSuccess("You can now edit the default location details.");
    }

    private void useCurrentLocation() {
        locationViewModel.getCurrentLocationForDefault();
        showSuccess("Fetching current location...");
    }

    private void saveDefaultLocation() {
        if (!validateForm()) {
            return;
        }

        DefaultLocation defaultLocation = new DefaultLocation();
        defaultLocation.setAddressLine1(etAddressLine1.getText().toString().trim());
        defaultLocation.setAddressLine2(etAddressLine2.getText().toString().trim());
        defaultLocation.setCity(etCity.getText().toString().trim());
        defaultLocation.setState(etState.getText().toString().trim());
        defaultLocation.setPincode(etPincode.getText().toString().trim());

        // For now, set coordinates to 0,0 - in a real app, you'd geocode the address
        defaultLocation.setLatitude(0.0);
        defaultLocation.setLongitude(0.0);

        locationViewModel.saveDefaultLocation(defaultLocation);
        showSuccess("Default location saved successfully!");
    }

    private boolean validateForm() {
        boolean isValid = true;

        // Validate Address Line 1
        if (etAddressLine1.getText().toString().trim().isEmpty()) {
            tilAddressLine1.setError("Address Line 1 is required");
            isValid = false;
        } else {
            tilAddressLine1.setError(null);
        }

        // Validate City
        if (etCity.getText().toString().trim().isEmpty()) {
            tilCity.setError("City is required");
            isValid = false;
        } else {
            tilCity.setError(null);
        }

        // Validate State
        if (etState.getText().toString().trim().isEmpty()) {
            tilState.setError("State is required");
            isValid = false;
        } else {
            tilState.setError(null);
        }

        // Validate Pincode
        String pincode = etPincode.getText().toString().trim();
        if (pincode.isEmpty()) {
            tilPincode.setError("Pincode is required");
            isValid = false;
        } else if (pincode.length() != 6) {
            tilPincode.setError("Pincode must be 6 digits");
            isValid = false;
        } else {
            tilPincode.setError(null);
        }

        return isValid;
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
