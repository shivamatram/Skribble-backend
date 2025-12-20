package com.example.foodvan.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.SavedAddressesAdapter;
import com.example.foodvan.models.Address;
import com.example.foodvan.utils.FirebaseManager;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.TextView;
import android.widget.ImageView;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SavedAddressesActivity - Comprehensive address management
 * Features: Add, edit, delete, set default addresses with Firebase integration
 */
public class SavedAddressesActivity extends AppCompatActivity implements SavedAddressesAdapter.OnAddressActionListener {

    private static final String TAG = "SavedAddressesActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvAddressCount;
    private LinearProgressIndicator progressIndicator;
    private MaterialCardView cardEmptyState;
    private RecyclerView recyclerAddresses;
    private FloatingActionButton fabAddAddress;
    private MaterialButton btnAddFirstAddress;

    // Adapter and Data
    private SavedAddressesAdapter addressesAdapter;
    private List<Address> addressesList;

    // Services
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;
    private DatabaseReference addressesRef;

    // Dialog Components
    private AlertDialog addEditDialog;
    private TextInputLayout tilCustomLabel, tilContactName, tilPhoneNumber;
    private TextInputLayout tilFlatBuilding, tilStreetAddress, tilLandmark;
    private TextInputLayout tilCity, tilPincode, tilDeliveryInstructions;
    private TextInputEditText etCustomLabel, etContactName, etPhoneNumber;
    private TextInputEditText etFlatBuilding, etStreetAddress, etLandmark;
    private TextInputEditText etCity, etPincode, etDeliveryInstructions;
    private ChipGroup chipGroupAddressType;
    private Chip chipHome, chipWork, chipOther;
    private MaterialCheckBox cbSetAsDefault;
    private MaterialButton btnSaveAddress, btnCancel;
    private LinearProgressIndicator progressSave;
    private ImageView ivCloseDialog;
    private TextView tvDialogTitle;

    // Current editing address
    private Address currentEditingAddress;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_addresses);

        initializeServices();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadSavedAddresses();
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        firebaseManager = new FirebaseManager();
        
        // Initialize Firebase reference for user addresses
        String userId = sessionManager.getUserId();
        if (userId != null) {
            addressesRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("addresses");
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvAddressCount = findViewById(R.id.tv_address_count);
        progressIndicator = findViewById(R.id.progress_indicator);
        cardEmptyState = findViewById(R.id.card_empty_state);
        recyclerAddresses = findViewById(R.id.recycler_addresses);
        fabAddAddress = findViewById(R.id.fab_add_address);
        btnAddFirstAddress = findViewById(R.id.btn_add_first_address);

        addressesList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        addressesAdapter = new SavedAddressesAdapter(this, addressesList, this);
        recyclerAddresses.setLayoutManager(new LinearLayoutManager(this));
        recyclerAddresses.setAdapter(addressesAdapter);
    }

    private void setupClickListeners() {
        fabAddAddress.setOnClickListener(v -> showAddEditAddressDialog(null));
        btnAddFirstAddress.setOnClickListener(v -> showAddEditAddressDialog(null));
    }

    private void loadSavedAddresses() {
        if (addressesRef == null) {
            showEmptyState();
            return;
        }

        showProgress(true);
        
        addressesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                addressesList.clear();
                
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    Address address = addressSnapshot.getValue(Address.class);
                    if (address != null) {
                        addressesList.add(address);
                    }
                }
                
                updateUI();
                showProgress(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showProgress(false);
                showSnackbar("Failed to load addresses: " + error.getMessage(), true);
            }
        });
    }

    private void updateUI() {
        int addressCount = addressesList.size();
        tvAddressCount.setText(addressCount + (addressCount == 1 ? " address" : " addresses"));
        
        if (addressCount == 0) {
            showEmptyState();
        } else {
            hideEmptyState();
            addressesAdapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        cardEmptyState.setVisibility(View.VISIBLE);
        recyclerAddresses.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        cardEmptyState.setVisibility(View.GONE);
        recyclerAddresses.setVisibility(View.VISIBLE);
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showAddEditAddressDialog(Address address) {
        isEditMode = address != null;
        currentEditingAddress = address;

        // Inflate dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_address, null);
        initializeDialogViews(dialogView);
        setupDialogClickListeners();

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        addEditDialog = builder.create();
        addEditDialog.show();

        // Setup dialog data
        if (isEditMode) {
            populateDialogWithAddress(address);
            tvDialogTitle.setText("Edit Address");
            btnSaveAddress.setText("Update Address");
        } else {
            tvDialogTitle.setText("Add New Address");
            btnSaveAddress.setText("Save Address");
            // Pre-fill with user data
            prefillUserData();
        }
    }

    private void initializeDialogViews(View dialogView) {
        tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        ivCloseDialog = dialogView.findViewById(R.id.iv_close_dialog);
        
        // Address type chips
        chipGroupAddressType = dialogView.findViewById(R.id.chip_group_address_type);
        chipHome = dialogView.findViewById(R.id.chip_home);
        chipWork = dialogView.findViewById(R.id.chip_work);
        chipOther = dialogView.findViewById(R.id.chip_other);
        
        // Input fields
        tilCustomLabel = dialogView.findViewById(R.id.til_custom_label);
        tilContactName = dialogView.findViewById(R.id.til_contact_name);
        tilPhoneNumber = dialogView.findViewById(R.id.til_phone_number);
        tilFlatBuilding = dialogView.findViewById(R.id.til_flat_building);
        tilStreetAddress = dialogView.findViewById(R.id.til_street_address);
        tilLandmark = dialogView.findViewById(R.id.til_landmark);
        tilCity = dialogView.findViewById(R.id.til_city);
        tilPincode = dialogView.findViewById(R.id.til_pincode);
        tilDeliveryInstructions = dialogView.findViewById(R.id.til_delivery_instructions);
        
        etCustomLabel = dialogView.findViewById(R.id.et_custom_label);
        etContactName = dialogView.findViewById(R.id.et_contact_name);
        etPhoneNumber = dialogView.findViewById(R.id.et_phone_number);
        etFlatBuilding = dialogView.findViewById(R.id.et_flat_building);
        etStreetAddress = dialogView.findViewById(R.id.et_street_address);
        etLandmark = dialogView.findViewById(R.id.et_landmark);
        etCity = dialogView.findViewById(R.id.et_city);
        etPincode = dialogView.findViewById(R.id.et_pincode);
        etDeliveryInstructions = dialogView.findViewById(R.id.et_delivery_instructions);
        
        // Checkbox and buttons
        cbSetAsDefault = dialogView.findViewById(R.id.cb_set_as_default);
        btnSaveAddress = dialogView.findViewById(R.id.btn_save_address);
        btnCancel = dialogView.findViewById(R.id.btn_cancel);
        progressSave = dialogView.findViewById(R.id.progress_save);
    }

    private void setupDialogClickListeners() {
        ivCloseDialog.setOnClickListener(v -> addEditDialog.dismiss());
        btnCancel.setOnClickListener(v -> addEditDialog.dismiss());
        btnSaveAddress.setOnClickListener(v -> validateAndSaveAddress());
        
        // Address type chip selection
        chipGroupAddressType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_other)) {
                tilCustomLabel.setVisibility(View.VISIBLE);
            } else {
                tilCustomLabel.setVisibility(View.GONE);
                etCustomLabel.setText("");
            }
        });

        // Add text watchers for validation
        addTextWatchers();
    }

    private void addTextWatchers() {
        // Phone number validation
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhoneNumber(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Pincode validation
        etPincode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePincode(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void validatePhoneNumber(String phone) {
        if (!TextUtils.isEmpty(phone) && (phone.length() < 10 || phone.length() > 15)) {
            tilPhoneNumber.setError("Enter valid phone number");
        } else {
            tilPhoneNumber.setError(null);
        }
    }

    private void validatePincode(String pincode) {
        if (!TextUtils.isEmpty(pincode) && pincode.length() != 6) {
            tilPincode.setError("Enter valid 6-digit pincode");
        } else {
            tilPincode.setError(null);
        }
    }

    private void prefillUserData() {
        // Pre-fill with user's saved information
        String userName = sessionManager.getUserName();
        String userPhone = sessionManager.getUserDetails() != null ? 
                          sessionManager.getUserDetails().getPhone() : "";
        
        if (!TextUtils.isEmpty(userName)) {
            etContactName.setText(userName);
        }
        if (!TextUtils.isEmpty(userPhone)) {
            etPhoneNumber.setText(userPhone);
        }
    }

    private void populateDialogWithAddress(Address address) {
        // Set address type
        String label = address.getLabel();
        if ("Home".equalsIgnoreCase(label)) {
            chipHome.setChecked(true);
        } else if ("Work".equalsIgnoreCase(label)) {
            chipWork.setChecked(true);
        } else {
            chipOther.setChecked(true);
            tilCustomLabel.setVisibility(View.VISIBLE);
            etCustomLabel.setText(label);
        }

        // Populate contact information
        if (address.getContactName() != null) {
            etContactName.setText(address.getContactName());
        }
        if (address.getPhoneNumber() != null) {
            etPhoneNumber.setText(address.getPhoneNumber());
        }

        // Populate address fields
        if (address.getFlatBuilding() != null) {
            etFlatBuilding.setText(address.getFlatBuilding());
        }
        etStreetAddress.setText(address.getStreetAddress());
        etLandmark.setText(address.getLandmark());
        etCity.setText(address.getCity());
        etPincode.setText(address.getPostalCode());
        etDeliveryInstructions.setText(address.getInstructions());
        
        cbSetAsDefault.setChecked(address.isDefault());
    }

    private void validateAndSaveAddress() {
        // Clear previous errors
        clearFieldErrors();

        // Get form data
        String addressType = getSelectedAddressType();
        String customLabel = etCustomLabel.getText().toString().trim();
        String contactName = etContactName.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String flatBuilding = etFlatBuilding.getText().toString().trim();
        String streetAddress = etStreetAddress.getText().toString().trim();
        String landmark = etLandmark.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String instructions = etDeliveryInstructions.getText().toString().trim();
        boolean setAsDefault = cbSetAsDefault.isChecked();

        // Validation
        boolean isValid = true;

        if (TextUtils.isEmpty(contactName)) {
            tilContactName.setError("Name is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            tilPhoneNumber.setError("Phone number is required");
            isValid = false;
        } else if (phoneNumber.length() < 10) {
            tilPhoneNumber.setError("Enter valid phone number");
            isValid = false;
        }

        if (TextUtils.isEmpty(flatBuilding)) {
            tilFlatBuilding.setError("Flat/Building is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(streetAddress)) {
            tilStreetAddress.setError("Street address is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(city)) {
            tilCity.setError("City is required");
            isValid = false;
        }

        if (TextUtils.isEmpty(pincode)) {
            tilPincode.setError("Pincode is required");
            isValid = false;
        } else if (pincode.length() != 6) {
            tilPincode.setError("Enter valid 6-digit pincode");
            isValid = false;
        }

        if ("Other".equals(addressType) && TextUtils.isEmpty(customLabel)) {
            tilCustomLabel.setError("Custom label is required");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create or update address
        Address address;
        if (isEditMode && currentEditingAddress != null) {
            address = currentEditingAddress;
        } else {
            address = new Address();
        }

        // Set address data
        String finalLabel = "Other".equals(addressType) ? customLabel : addressType;
        address.setLabel(finalLabel);
        
        // Set contact information
        address.setContactName(contactName);
        address.setPhoneNumber(phoneNumber);
        address.setFlatBuilding(flatBuilding);
        
        // Set address details
        address.setStreetAddress(streetAddress);
        address.setFullAddress(flatBuilding + ", " + streetAddress + ", " + city + ", " + pincode);
        address.setLandmark(landmark);
        address.setCity(city);
        address.setPostalCode(pincode);
        address.setInstructions(instructions);
        address.setDefault(setAsDefault);

        // Save to Firebase
        saveAddressToFirebase(address);
    }

    private String getSelectedAddressType() {
        int checkedId = chipGroupAddressType.getCheckedChipId();
        if (checkedId == R.id.chip_home) {
            return "Home";
        } else if (checkedId == R.id.chip_work) {
            return "Work";
        } else {
            return "Other";
        }
    }

    private void clearFieldErrors() {
        tilCustomLabel.setError(null);
        tilContactName.setError(null);
        tilPhoneNumber.setError(null);
        tilFlatBuilding.setError(null);
        tilStreetAddress.setError(null);
        tilCity.setError(null);
        tilPincode.setError(null);
    }

    private void saveAddressToFirebase(Address address) {
        if (addressesRef == null) {
            showSnackbar("Unable to save address. Please try again.", true);
            return;
        }

        progressSave.setVisibility(View.VISIBLE);
        btnSaveAddress.setEnabled(false);

        // If setting as default, first remove default from other addresses
        if (address.isDefault()) {
            removeDefaultFromOtherAddresses(() -> saveAddress(address));
        } else {
            saveAddress(address);
        }
    }

    private void removeDefaultFromOtherAddresses(Runnable onComplete) {
        addressesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> updates = new HashMap<>();
                
                for (DataSnapshot addressSnapshot : snapshot.getChildren()) {
                    Address existingAddress = addressSnapshot.getValue(Address.class);
                    if (existingAddress != null && existingAddress.isDefault()) {
                        updates.put(addressSnapshot.getKey() + "/default", false);
                    }
                }
                
                if (!updates.isEmpty()) {
                    addressesRef.updateChildren(updates).addOnCompleteListener(task -> {
                        onComplete.run();
                    });
                } else {
                    onComplete.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                onComplete.run();
            }
        });
    }

    private void saveAddress(Address address) {
        String addressId = address.getAddressId();
        if (addressId == null) {
            addressId = addressesRef.push().getKey();
            address.setAddressId(addressId);
        }

        addressesRef.child(addressId).setValue(address)
                .addOnSuccessListener(aVoid -> {
                    progressSave.setVisibility(View.GONE);
                    btnSaveAddress.setEnabled(true);
                    addEditDialog.dismiss();
                    showSnackbar(isEditMode ? "Address updated successfully" : "Address saved successfully", false);
                })
                .addOnFailureListener(e -> {
                    progressSave.setVisibility(View.GONE);
                    btnSaveAddress.setEnabled(true);
                    showSnackbar("Failed to save address: " + e.getMessage(), true);
                });
    }

    // SavedAddressesAdapter.OnAddressActionListener implementation
    @Override
    public void onEditAddress(Address address) {
        showAddEditAddressDialog(address);
    }

    @Override
    public void onDeleteAddress(Address address) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Address")
                .setMessage("Are you sure you want to delete this address?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAddress(address))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSetDefaultAddress(Address address) {
        setDefaultAddress(address);
    }

    private void deleteAddress(Address address) {
        if (addressesRef == null || address.getAddressId() == null) {
            showSnackbar("Unable to delete address", true);
            return;
        }

        addressesRef.child(address.getAddressId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Address deleted successfully", false);
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Failed to delete address: " + e.getMessage(), true);
                });
    }

    private void setDefaultAddress(Address address) {
        if (addressesRef == null || address.getAddressId() == null) {
            showSnackbar("Unable to set default address", true);
            return;
        }

        // First remove default from all addresses, then set this one as default
        removeDefaultFromOtherAddresses(() -> {
            addressesRef.child(address.getAddressId()).child("default").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        showSnackbar("Default address updated", false);
                    })
                    .addOnFailureListener(e -> {
                        showSnackbar("Failed to update default address", true);
                    });
        });
    }

    private void showSnackbar(String message, boolean isError) {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.snackbar_container), message, Snackbar.LENGTH_LONG);
        if (isError) {
            snackbar.setBackgroundTint(getResources().getColor(R.color.error, null));
        }
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (addEditDialog != null && addEditDialog.isShowing()) {
            addEditDialog.dismiss();
        }
    }
}
