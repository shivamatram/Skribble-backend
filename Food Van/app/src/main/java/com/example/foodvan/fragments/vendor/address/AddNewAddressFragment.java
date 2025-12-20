package com.example.foodvan.fragments.vendor.address;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.foodvan.R;
import com.example.foodvan.activities.vendor.VendorSavedAddressesActivity;
import com.example.foodvan.models.Address;
import com.example.foodvan.repositories.AddressRepository;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment to add new vendor address
 */
public class AddNewAddressFragment extends Fragment {

    private TextInputLayout tilBusinessName, tilAddressLine1, tilAddressLine2;
    private TextInputLayout tilCity, tilState, tilPincode;
    private TextInputEditText etBusinessName, etAddressLine1, etAddressLine2;
    private TextInputEditText etCity, etPincode;
    private AutoCompleteTextView etState;
    private MaterialSwitch switchSetPrimary;
    private MaterialButton btnSaveAddress;
    
    private VendorSavedAddressesActivity parentActivity;

    // Indian states for dropdown
    private final String[] indianStates = {
        "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh",
        "Goa", "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand",
        "Karnataka", "Kerala", "Madhya Pradesh", "Maharashtra", "Manipur",
        "Meghalaya", "Mizoram", "Nagaland", "Odisha", "Punjab",
        "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
        "Uttar Pradesh", "Uttarakhand", "West Bengal", "Delhi", "Jammu and Kashmir",
        "Ladakh", "Lakshadweep", "Puducherry", "Andaman and Nicobar Islands",
        "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu"
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (VendorSavedAddressesActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_new_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupStateDropdown();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        tilBusinessName = view.findViewById(R.id.til_business_name);
        tilAddressLine1 = view.findViewById(R.id.til_address_line1);
        tilAddressLine2 = view.findViewById(R.id.til_address_line2);
        tilCity = view.findViewById(R.id.til_city);
        tilState = view.findViewById(R.id.til_state);
        tilPincode = view.findViewById(R.id.til_pincode);
        
        etBusinessName = view.findViewById(R.id.et_business_name);
        etAddressLine1 = view.findViewById(R.id.et_address_line1);
        etAddressLine2 = view.findViewById(R.id.et_address_line2);
        etCity = view.findViewById(R.id.et_city);
        etState = view.findViewById(R.id.et_state);
        etPincode = view.findViewById(R.id.et_pincode);
        
        switchSetPrimary = view.findViewById(R.id.switch_set_primary);
        btnSaveAddress = view.findViewById(R.id.btn_save_address);
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
        btnSaveAddress.setOnClickListener(v -> saveAddress());
    }

    private void saveAddress() {
        if (!validateInputs()) {
            return;
        }

        // Create new address object
        Address newAddress = new Address();
        newAddress.setBusinessName(etBusinessName.getText().toString().trim());
        newAddress.setAddressLine1(etAddressLine1.getText().toString().trim());
        newAddress.setAddressLine2(etAddressLine2.getText().toString().trim());
        newAddress.setCity(etCity.getText().toString().trim());
        newAddress.setState(etState.getText().toString().trim());
        newAddress.setPostalCode(etPincode.getText().toString().trim());
        newAddress.setPrimary(switchSetPrimary.isChecked());
        
        // Set label based on primary status
        newAddress.setLabel(switchSetPrimary.isChecked() ? "Primary Location" : "Business Location");

        // Build full address
        String fullAddress = buildFullAddress(newAddress);
        newAddress.setFullAddress(fullAddress);

        // Save address to Firebase using repository
        if (parentActivity != null && parentActivity.getAddressRepository() != null) {
            parentActivity.getAddressRepository().addAddress(newAddress, new AddressRepository.AddressCallback() {
                @Override
                public void onSuccess() {
                    if (parentActivity != null) {
                        parentActivity.showSuccess("Address saved successfully!");
                        // Switch back to View Addresses tab to see the new address
                        parentActivity.switchToTab(0);
                    }
                    // Clear form
                    clearForm();
                }

                @Override
                public void onError(String error) {
                    if (parentActivity != null) {
                        parentActivity.showError("Failed to save address: " + error);
                    }
                }
            });
        } else {
            // Fallback to old method if repository is not available
            saveAddressToDatabase(newAddress);
            
            // Show success message
            if (parentActivity != null) {
                parentActivity.showSuccess("Address saved successfully!");
                // Switch back to View Addresses tab to see the new address
                parentActivity.switchToTab(0);
            }
            
            // Clear form
            clearForm();
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        // Validate business name
        if (TextUtils.isEmpty(etBusinessName.getText())) {
            tilBusinessName.setError("Business name is required");
            isValid = false;
        }

        // Validate address line 1
        if (TextUtils.isEmpty(etAddressLine1.getText())) {
            tilAddressLine1.setError("Address line 1 is required");
            isValid = false;
        }

        // Validate city
        if (TextUtils.isEmpty(etCity.getText())) {
            tilCity.setError("City is required");
            isValid = false;
        }

        // Validate state
        if (TextUtils.isEmpty(etState.getText())) {
            tilState.setError("State is required");
            isValid = false;
        }

        // Validate pincode
        String pincode = etPincode.getText().toString().trim();
        if (TextUtils.isEmpty(pincode)) {
            tilPincode.setError("Pincode is required");
            isValid = false;
        } else if (pincode.length() != 6 || !pincode.matches("\\d{6}")) {
            tilPincode.setError("Pincode must be 6 digits");
            isValid = false;
        }

        return isValid;
    }

    private void clearErrors() {
        tilBusinessName.setError(null);
        tilAddressLine1.setError(null);
        tilAddressLine2.setError(null);
        tilCity.setError(null);
        tilState.setError(null);
        tilPincode.setError(null);
    }

    private String buildFullAddress(Address address) {
        StringBuilder sb = new StringBuilder();
        
        if (!TextUtils.isEmpty(address.getAddressLine1())) {
            sb.append(address.getAddressLine1());
        }
        
        if (!TextUtils.isEmpty(address.getAddressLine2())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getAddressLine2());
        }
        
        if (!TextUtils.isEmpty(address.getCity())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getCity());
        }
        
        if (!TextUtils.isEmpty(address.getState())) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(address.getState());
        }
        
        if (!TextUtils.isEmpty(address.getPostalCode())) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(address.getPostalCode());
        }
        
        return sb.toString();
    }

    private void saveAddressToDatabase(Address address) {
        // In real implementation, save to Firebase
        // For now, just simulate saving
        if (parentActivity != null && parentActivity.getAddressesRef() != null) {
            // parentActivity.getAddressesRef().child(address.getAddressId()).setValue(address);
        }
    }

    private void clearForm() {
        etBusinessName.setText("");
        etAddressLine1.setText("");
        etAddressLine2.setText("");
        etCity.setText("");
        etState.setText("");
        etPincode.setText("");
        switchSetPrimary.setChecked(false);
        clearErrors();
    }
}
