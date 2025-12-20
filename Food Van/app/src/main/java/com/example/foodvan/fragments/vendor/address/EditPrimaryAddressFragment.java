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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment to edit primary vendor address
 */
public class EditPrimaryAddressFragment extends Fragment {

    private TextInputLayout tilBusinessName, tilAddressLine1, tilAddressLine2;
    private TextInputLayout tilCity, tilState, tilPincode;
    private TextInputEditText etBusinessName, etAddressLine1, etAddressLine2;
    private TextInputEditText etCity, etPincode;
    private AutoCompleteTextView etState;
    private MaterialButton btnCancel, btnSaveChanges;
    
    private VendorSavedAddressesActivity parentActivity;
    private Address currentPrimaryAddress;

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
        loadCurrentPrimaryAddress();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_primary_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupStateDropdown();
        setupClickListeners();
        populateFields();
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
        
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnSaveChanges = view.findViewById(R.id.btn_save_changes);
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
        btnCancel.setOnClickListener(v -> {
            // Reset form to original values
            populateFields();
            if (parentActivity != null) {
                parentActivity.showInfo("Changes cancelled");
                // Switch back to View Addresses tab
                parentActivity.switchToTab(0);
            }
        });

        btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void loadCurrentPrimaryAddress() {
        if (parentActivity != null && parentActivity.getAddressRepository() != null) {
            parentActivity.getAddressRepository().getPrimaryAddress(new AddressRepository.SingleAddressCallback() {
                @Override
                public void onSuccess(Address address) {
                    currentPrimaryAddress = address;
                    populateFields();
                }

                @Override
                public void onError(String error) {
                    if (parentActivity != null) {
                        parentActivity.showError("Failed to load primary address: " + error);
                    }
                    // Create sample address as fallback
                    createSamplePrimaryAddress();
                    populateFields();
                }
            });
        } else {
            // Fallback to sample data
            createSamplePrimaryAddress();
            populateFields();
        }
    }

    private void createSamplePrimaryAddress() {
        currentPrimaryAddress = new Address();
        currentPrimaryAddress.setAddressId("primary_001");
        currentPrimaryAddress.setBusinessName("Main Kitchen");
        currentPrimaryAddress.setAddressLine1("123 Food Street");
        currentPrimaryAddress.setAddressLine2("Near Central Market");
        currentPrimaryAddress.setCity("Mumbai");
        currentPrimaryAddress.setState("Maharashtra");
        currentPrimaryAddress.setPostalCode("400001");
    }

    private void populateFields() {
        if (currentPrimaryAddress != null) {
            etBusinessName.setText(currentPrimaryAddress.getBusinessName());
            etAddressLine1.setText(currentPrimaryAddress.getAddressLine1());
            etAddressLine2.setText(currentPrimaryAddress.getAddressLine2());
            etCity.setText(currentPrimaryAddress.getCity());
            etState.setText(currentPrimaryAddress.getState());
            etPincode.setText(currentPrimaryAddress.getPostalCode());
        }
    }

    private void saveChanges() {
        if (!validateInputs()) {
            return;
        }

        // Update current primary address
        if (currentPrimaryAddress != null) {
            currentPrimaryAddress.setBusinessName(etBusinessName.getText().toString().trim());
            currentPrimaryAddress.setAddressLine1(etAddressLine1.getText().toString().trim());
            currentPrimaryAddress.setAddressLine2(etAddressLine2.getText().toString().trim());
            currentPrimaryAddress.setCity(etCity.getText().toString().trim());
            currentPrimaryAddress.setState(etState.getText().toString().trim());
            currentPrimaryAddress.setPostalCode(etPincode.getText().toString().trim());

            // Build full address
            String fullAddress = buildFullAddress(currentPrimaryAddress);
            currentPrimaryAddress.setFullAddress(fullAddress);

            // Save to database using repository
            if (parentActivity != null && parentActivity.getAddressRepository() != null) {
                parentActivity.getAddressRepository().updateAddress(currentPrimaryAddress, new AddressRepository.AddressCallback() {
                    @Override
                    public void onSuccess() {
                        if (parentActivity != null) {
                            parentActivity.showSuccess("Primary address updated successfully!");
                            // Switch back to View Addresses tab to see the updated address
                            parentActivity.switchToTab(0);
                        }
                    }

                    @Override
                    public void onError(String error) {
                        if (parentActivity != null) {
                            parentActivity.showError("Failed to update address: " + error);
                        }
                    }
                });
            } else {
                // Fallback to old method
                saveAddressToDatabase(currentPrimaryAddress);
                
                // Show success message
                if (parentActivity != null) {
                    parentActivity.showSuccess("Primary address updated successfully!");
                    // Switch back to View Addresses tab to see the updated address
                    parentActivity.switchToTab(0);
                }
            }
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
}
