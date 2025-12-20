package com.example.foodvan.fragments.vendor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.viewmodels.AccountSettingsViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Account Settings Fragment - Professional implementation with full functionality
 * Handles vendor profile management and account operations
 */
public class AccountSettingsFragment extends Fragment {

    // UI Components
    private ShapeableImageView profileImage;
    private android.widget.TextView vendorName, businessEmail;
    private MaterialButton editPhotoButton, saveChangesButton;
    private MaterialButton deactivateAccountButton, deleteAccountButton;
    
    // Input Fields
    private TextInputLayout emailInputLayout, phoneInputLayout, businessNameInputLayout, addressInputLayout;
    private TextInputEditText emailEditText, phoneEditText, businessNameEditText, addressEditText;
    
    
    // ViewModel
    private AccountSettingsViewModel viewModel;
    
    // Activity Result Launchers
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    
    // State Variables
    private boolean hasUnsavedChanges = false;
    private String originalEmail, originalPhone, originalBusinessName, originalAddress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleImageSelection(imageUri);
                    }
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        initializeViewModel();
        setupClickListeners();
        setupTextWatchers();
        observeViewModel();
        loadUserData();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews(View view) {
        // Profile Header
        profileImage = view.findViewById(R.id.profile_image);
        vendorName = view.findViewById(R.id.vendor_name);
        businessEmail = view.findViewById(R.id.business_email);
        editPhotoButton = view.findViewById(R.id.edit_photo_button);
        
        // Account Information Fields
        emailInputLayout = view.findViewById(R.id.email_input_layout);
        phoneInputLayout = view.findViewById(R.id.phone_input_layout);
        businessNameInputLayout = view.findViewById(R.id.business_name_input_layout);
        addressInputLayout = view.findViewById(R.id.address_input_layout);
        
        emailEditText = view.findViewById(R.id.email_edit_text);
        phoneEditText = view.findViewById(R.id.phone_edit_text);
        businessNameEditText = view.findViewById(R.id.business_name_edit_text);
        addressEditText = view.findViewById(R.id.address_edit_text);
        
        // Action Buttons
        saveChangesButton = view.findViewById(R.id.save_changes_button);
        deactivateAccountButton = view.findViewById(R.id.deactivate_account_button);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);
        
    }

    /**
     * Initialize ViewModel
     */
    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(AccountSettingsViewModel.class);
    }

    /**
     * Setup click listeners for all interactive elements
     */
    private void setupClickListeners() {
        // Edit Photo
        editPhotoButton.setOnClickListener(v -> openImagePicker());
        
        // Edit Field Icons
        emailInputLayout.setEndIconOnClickListener(v -> toggleFieldEdit(emailEditText, emailInputLayout));
        phoneInputLayout.setEndIconOnClickListener(v -> toggleFieldEdit(phoneEditText, phoneInputLayout));
        businessNameInputLayout.setEndIconOnClickListener(v -> toggleFieldEdit(businessNameEditText, businessNameInputLayout));
        addressInputLayout.setEndIconOnClickListener(v -> toggleFieldEdit(addressEditText, addressInputLayout));
        
        // Save Changes
        saveChangesButton.setOnClickListener(v -> saveChanges());
        
        // Danger Zone Actions
        deactivateAccountButton.setOnClickListener(v -> showDeactivateAccountDialog());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
    }

    /**
     * Setup text watchers to detect changes
     */
    private void setupTextWatchers() {
        TextWatcher changeWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkForChanges();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
        
        emailEditText.addTextChangedListener(changeWatcher);
        phoneEditText.addTextChangedListener(changeWatcher);
        businessNameEditText.addTextChangedListener(changeWatcher);
        addressEditText.addTextChangedListener(changeWatcher);
    }

    /**
     * Observe ViewModel LiveData
     */
    private void observeViewModel() {
        // Loading State
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            saveChangesButton.setEnabled(!isLoading && hasUnsavedChanges);
        });
        
        // Success Messages
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSnackbar(message, false);
                viewModel.clearMessages();
                resetFormState();
            }
        });
        
        // Error Messages
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showSnackbar(error, true);
                viewModel.clearMessages();
            }
        });
        
        // User Profile Data
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                populateUserData(profile);
            }
        });
    }

    /**
     * Load user data from ViewModel
     */
    private void loadUserData() {
        viewModel.loadUserProfile();
    }

    /**
     * Populate UI with user data
     */
    private void populateUserData(com.example.foodvan.models.VendorProfile profile) {
        vendorName.setText(profile.getVendorName());
        businessEmail.setText(profile.getEmail());
        
        emailEditText.setText(profile.getEmail());
        phoneEditText.setText(profile.getPhone());
        businessNameEditText.setText(profile.getBusinessName());
        addressEditText.setText(profile.getAddress());
        
        // Store original values
        originalEmail = profile.getEmail();
        originalPhone = profile.getPhone();
        originalBusinessName = profile.getBusinessName();
        originalAddress = profile.getAddress();
        
        // Load profile image
        if (profile.getProfileImageUrl() != null && !profile.getProfileImageUrl().isEmpty()) {
            Glide.with(this)
                .load(profile.getProfileImageUrl())
                .placeholder(R.drawable.ic_account_circle)
                .error(R.drawable.ic_account_circle)
                .into(profileImage);
        }
    }

    /**
     * Open image picker for profile photo
     */
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Profile Photo"));
    }

    /**
     * Handle selected image
     */
    private void handleImageSelection(Uri imageUri) {
        // Display selected image immediately
        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.ic_account_circle)
            .error(R.drawable.ic_account_circle)
            .into(profileImage);
        
        // Upload image through ViewModel
        viewModel.uploadProfileImage(imageUri);
        hasUnsavedChanges = true;
        updateSaveButtonState();
    }

    /**
     * Toggle field edit mode
     */
    private void toggleFieldEdit(TextInputEditText editText, TextInputLayout inputLayout) {
        boolean isEnabled = editText.isEnabled();
        editText.setEnabled(!isEnabled);
        
        if (!isEnabled) {
            editText.requestFocus();
            editText.setSelection(editText.getText().length());
        }
        
        // Update end icon
        inputLayout.setEndIconDrawable(isEnabled ? R.drawable.ic_edit : R.drawable.ic_check);
    }


    /**
     * Check for unsaved changes
     */
    private void checkForChanges() {
        String currentEmail = emailEditText.getText().toString().trim();
        String currentPhone = phoneEditText.getText().toString().trim();
        String currentBusinessName = businessNameEditText.getText().toString().trim();
        String currentAddress = addressEditText.getText().toString().trim();
        
        hasUnsavedChanges = !currentEmail.equals(originalEmail != null ? originalEmail : "") ||
                           !currentPhone.equals(originalPhone != null ? originalPhone : "") ||
                           !currentBusinessName.equals(originalBusinessName != null ? originalBusinessName : "") ||
                           !currentAddress.equals(originalAddress != null ? originalAddress : "");
        
        updateSaveButtonState();
    }

    /**
     * Update save button state
     */
    private void updateSaveButtonState() {
        saveChangesButton.setEnabled(hasUnsavedChanges);
        saveChangesButton.setBackgroundTintList(getResources().getColorStateList(
            hasUnsavedChanges ? R.color.primary_color : R.color.gray_400, null));
    }

    /**
     * Save all changes
     */
    private void saveChanges() {
        if (!validateInputs()) {
            return;
        }
        
        
        // Prepare updated profile data
        com.example.foodvan.models.VendorProfile updatedProfile = new com.example.foodvan.models.VendorProfile();
        updatedProfile.setVendorName(businessNameEditText.getText().toString().trim());
        updatedProfile.setEmail(emailEditText.getText().toString().trim());
        updatedProfile.setPhone(phoneEditText.getText().toString().trim());
        updatedProfile.setBusinessName(businessNameEditText.getText().toString().trim());
        updatedProfile.setAddress(addressEditText.getText().toString().trim());
        
        // Update profile without password change
        viewModel.updateProfile(updatedProfile);
        
    }

    /**
     * Validate all inputs
     */
    private boolean validateInputs() {
        boolean isValid = true;
        
        // Clear previous errors
        clearAllErrors();
        
        // Validate email
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Invalid email format");
            isValid = false;
        }
        
        // Validate phone
        String phone = phoneEditText.getText().toString().trim();
        if (phone.isEmpty()) {
            phoneInputLayout.setError("Phone number is required");
            isValid = false;
        } else if (phone.length() < 10) {
            phoneInputLayout.setError("Invalid phone number");
            isValid = false;
        }
        
        // Validate business name
        String businessName = businessNameEditText.getText().toString().trim();
        if (businessName.isEmpty()) {
            businessNameInputLayout.setError("Business name is required");
            isValid = false;
        }
        
        // Validate address
        String address = addressEditText.getText().toString().trim();
        if (address.isEmpty()) {
            addressInputLayout.setError("Address is required");
            isValid = false;
        }
        
        
        return isValid;
    }

    /**
     * Clear all input errors
     */
    private void clearAllErrors() {
        emailInputLayout.setError(null);
        phoneInputLayout.setError(null);
        businessNameInputLayout.setError(null);
        addressInputLayout.setError(null);
    }


    /**
     * Reset form state after successful save
     */
    private void resetFormState() {
        hasUnsavedChanges = false;
        updateSaveButtonState();
        
        
        // Disable all fields
        emailEditText.setEnabled(false);
        phoneEditText.setEnabled(false);
        businessNameEditText.setEnabled(false);
        addressEditText.setEnabled(false);
        
        // Reset edit icons
        emailInputLayout.setEndIconDrawable(R.drawable.ic_edit);
        phoneInputLayout.setEndIconDrawable(R.drawable.ic_edit);
        businessNameInputLayout.setEndIconDrawable(R.drawable.ic_edit);
        addressInputLayout.setEndIconDrawable(R.drawable.ic_edit);
    }

    /**
     * Show deactivate account confirmation dialog
     */
    private void showDeactivateAccountDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Deactivate Account")
            .setMessage("Are you sure you want to deactivate your account? You can reactivate it later by logging in again.")
            .setPositiveButton("Deactivate", (dialog, which) -> {
                viewModel.deactivateAccount();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Show delete account confirmation dialog
     */
    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account Permanently")
            .setMessage("⚠️ WARNING: This action cannot be undone!\n\nAll your data, including orders, menu items, and account information will be permanently deleted.")
            .setPositiveButton("Delete Forever", (dialog, which) -> {
                showPasswordConfirmationDialog();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Show password confirmation for account deletion
     */
    private void showPasswordConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_password_confirmation, null);
        TextInputEditText passwordInput = dialogView.findViewById(R.id.password_input);
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Password")
            .setMessage("Please enter your password to confirm account deletion:")
            .setView(dialogView)
            .setPositiveButton("Delete Account", (dialog, which) -> {
                String password = passwordInput.getText().toString();
                if (!password.isEmpty()) {
                    viewModel.deleteAccount(password);
                } else {
                    showSnackbar("Password is required", true);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Show snackbar message
     */
    private void showSnackbar(String message, boolean isError) {
        if (getView() != null) {
            Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG);
            if (isError) {
                snackbar.setBackgroundTint(getResources().getColor(R.color.error_color, null));
            } else {
                snackbar.setBackgroundTint(getResources().getColor(R.color.success_color, null));
            }
            snackbar.show();
        }
    }
}
