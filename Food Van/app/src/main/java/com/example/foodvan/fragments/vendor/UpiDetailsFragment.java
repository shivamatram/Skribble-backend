package com.example.foodvan.fragments.vendor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.models.UpiDetails;
import com.example.foodvan.viewmodels.PaymentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for managing UPI details
 * Features:
 * - Display existing UPI information
 * - Add/Edit UPI ID
 * - UPI verification
 * - QR code generation
 */
public class UpiDetailsFragment extends Fragment {

    private static final String TAG = "UpiDetails";

    // UI Components
    private MaterialCardView cardExistingUpi, cardAddUpi, cardQrCode;
    private TextInputLayout tilUpiId;
    private TextInputEditText etUpiId;
    private MaterialButton btnEditUpi, btnAddUpi, btnSaveUpi, btnVerifyUpi, btnGenerateQr;
    private Chip chipVerificationStatus;
    private ImageView ivQrCode;

    // Data & Services
    private PaymentViewModel paymentViewModel;
    private UpiDetails currentUpi;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_upi_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        initializeViewModel();
        setupClickListeners();
        observeData();
        
        // Load data after a short delay to ensure ViewModel is ready
        view.post(() -> loadUpiData());
    }

    private void initializeViews(View view) {
        // Cards
        cardExistingUpi = view.findViewById(R.id.card_existing_upi);
        cardAddUpi = view.findViewById(R.id.card_add_upi);
        cardQrCode = view.findViewById(R.id.card_qr_code);

        // Input fields
        tilUpiId = view.findViewById(R.id.til_upi_id);
        etUpiId = view.findViewById(R.id.et_upi_id);

        // Buttons
        btnEditUpi = view.findViewById(R.id.btn_edit_upi);
        btnAddUpi = view.findViewById(R.id.btn_add_upi);
        btnSaveUpi = view.findViewById(R.id.btn_save_upi);
        btnVerifyUpi = view.findViewById(R.id.btn_verify_upi);
        btnGenerateQr = view.findViewById(R.id.btn_generate_qr);

        // Other components
        chipVerificationStatus = view.findViewById(R.id.chip_verification_status);
        ivQrCode = view.findViewById(R.id.iv_qr_code);
    }

    private void initializeViewModel() {
        paymentViewModel = new ViewModelProvider(requireActivity()).get(PaymentViewModel.class);
    }

    private void setupClickListeners() {
        btnEditUpi.setOnClickListener(v -> enableEditMode());
        btnAddUpi.setOnClickListener(v -> showAddUpiForm());
        btnSaveUpi.setOnClickListener(v -> saveUpiDetails());
        btnVerifyUpi.setOnClickListener(v -> verifyUpiId());
        btnGenerateQr.setOnClickListener(v -> generateQrCode());
    }

    private void observeData() {
        paymentViewModel.getUpiDetails().observe(getViewLifecycleOwner(), upiDetails -> {
            currentUpi = upiDetails;
            updateUI();
        });

        paymentViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicators
            btnSaveUpi.setEnabled(!isLoading);
            btnVerifyUpi.setEnabled(!isLoading);
            btnGenerateQr.setEnabled(!isLoading);
        });

        paymentViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                
                // Reset verification button state if there was an error
                if (error.contains("verification") || error.contains("verify")) {
                    btnVerifyUpi.setEnabled(true);
                    btnVerifyUpi.setText("Verify UPI");
                }
            }
        });

        paymentViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSuccess(message);
                
                // Reset verification button state if verification was successful
                if (message.contains("verification") || message.contains("verified")) {
                    btnVerifyUpi.setEnabled(true);
                    btnVerifyUpi.setText("Verify UPI");
                    
                    // Reload data to get updated verification status
                    loadUpiData();
                }
            }
        });

        paymentViewModel.getQrCodeBitmap().observe(getViewLifecycleOwner(), qrBitmap -> {
            if (qrBitmap != null) {
                ivQrCode.setImageBitmap(qrBitmap);
                cardQrCode.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadUpiData() {
        paymentViewModel.loadUpiDetails();
    }

    private void updateUI() {
        if (currentUpi != null) {
            // Show existing UPI card
            cardExistingUpi.setVisibility(View.VISIBLE);
            cardAddUpi.setVisibility(View.GONE);
            
            // Populate fields with existing data
            populateExistingUpiData();
        } else {
            // Show add UPI form
            cardExistingUpi.setVisibility(View.GONE);
            cardAddUpi.setVisibility(View.VISIBLE);
            cardQrCode.setVisibility(View.GONE);
        }
    }

    private void populateExistingUpiData() {
        if (currentUpi == null) return;

        // Populate UPI ID field with actual saved data
        etUpiId.setText(currentUpi.getUpiId() != null ? currentUpi.getUpiId() : "");
        
        // Update verification status
        updateVerificationStatus(currentUpi.isVerified());
        
        // Show QR code if available
        if (currentUpi.getQrCodeData() != null && !currentUpi.getQrCodeData().isEmpty()) {
            cardQrCode.setVisibility(View.VISIBLE);
            // Load QR code from data
            paymentViewModel.loadQrCodeFromData(currentUpi.getQrCodeData());
        } else {
            cardQrCode.setVisibility(View.GONE);
        }

        // Disable editing initially
        setFieldsEnabled(false);
        
        // Show edit button
        btnEditUpi.setVisibility(View.VISIBLE);
        btnSaveUpi.setVisibility(View.GONE);
    }

    private void updateVerificationStatus(boolean isVerified) {
        if (isVerified) {
            chipVerificationStatus.setText("Verified");
            chipVerificationStatus.setChipBackgroundColorResource(R.color.success_light);
            chipVerificationStatus.setChipIconResource(R.drawable.ic_verified);
            chipVerificationStatus.setTextColor(getResources().getColor(R.color.success_color, null));
            chipVerificationStatus.setChipIconTintResource(R.color.success_color);
        } else {
            chipVerificationStatus.setText("Pending");
            chipVerificationStatus.setChipBackgroundColorResource(R.color.warning_light);
            chipVerificationStatus.setChipIconResource(R.drawable.ic_pending);
            chipVerificationStatus.setTextColor(getResources().getColor(R.color.warning_color, null));
            chipVerificationStatus.setChipIconTintResource(R.color.warning_color);
        }
    }

    private void enableEditMode() {
        isEditMode = true;
        
        // Enable fields first
        setFieldsEnabled(true);
        
        // Populate field with current UPI data for editing
        if (currentUpi != null) {
            etUpiId.setText(currentUpi.getUpiId() != null ? currentUpi.getUpiId() : "");
        }
        
        // Update button visibility
        btnEditUpi.setVisibility(View.GONE);
        btnSaveUpi.setVisibility(View.VISIBLE);
        
        // Focus on UPI ID field and position cursor at end for easy editing
        etUpiId.requestFocus();
        if (etUpiId.getText().length() > 0) {
            etUpiId.setSelection(etUpiId.getText().length());
        }
        
        // Show soft keyboard for immediate editing
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etUpiId, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
        
        // Show user feedback
        showSuccess("Edit mode enabled. You can now modify your UPI ID.");
    }

    private void showAddUpiForm() {
        cardExistingUpi.setVisibility(View.GONE);
        cardAddUpi.setVisibility(View.VISIBLE);
        cardQrCode.setVisibility(View.GONE);
        setFieldsEnabled(true);
        clearFields();
    }

    private void setFieldsEnabled(boolean enabled) {
        // Enable/disable UPI ID field
        etUpiId.setEnabled(enabled);
        etUpiId.setFocusable(enabled);
        etUpiId.setFocusableInTouchMode(enabled);
        
        // Set visual feedback for enabled/disabled state
        float alpha = enabled ? 1.0f : 0.6f;
        tilUpiId.setAlpha(alpha);
        
        // Update field appearance
        if (enabled) {
            etUpiId.setTextColor(getResources().getColor(android.R.color.black, null));
        } else {
            etUpiId.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
        }
    }

    private void clearFields() {
        etUpiId.setText("");
    }

    private void saveUpiDetails() {
        if (!validateInputs()) return;

        // Create or update UPI details
        UpiDetails upiDetails = currentUpi != null ? currentUpi : new UpiDetails();
        
        String newUpiId = etUpiId.getText().toString().trim();
        
        // Only reset verification if UPI ID changed
        if (currentUpi == null || !newUpiId.equals(currentUpi.getUpiId())) {
            upiDetails.setVerified(false); // Reset verification status for new/changed UPI ID
            upiDetails.setQrCodeData(null); // Clear old QR code data
        }
        
        // Update with form data
        upiDetails.setUpiId(newUpiId);
        upiDetails.setLastUpdated(System.currentTimeMillis());

        // Update current UPI reference
        currentUpi = upiDetails;
        
        // Save to Firebase
        paymentViewModel.saveUpiDetails(upiDetails);
        
        // Reset UI state
        isEditMode = false;
        setFieldsEnabled(false);
        btnEditUpi.setVisibility(View.VISIBLE);
        btnSaveUpi.setVisibility(View.GONE);
        
        // Update verification status display
        updateVerificationStatus(upiDetails.isVerified());
        
        showSuccess("UPI details saved successfully!");
    }

    private void verifyUpiId() {
        String upiIdToVerify = "";
        
        // Get UPI ID from current data or form field
        if (currentUpi != null && currentUpi.getUpiId() != null) {
            upiIdToVerify = currentUpi.getUpiId();
        } else {
            upiIdToVerify = etUpiId.getText().toString().trim();
        }

        // Validate UPI ID before verification
        if (upiIdToVerify.isEmpty()) {
            showError("Please enter UPI ID first");
            return;
        }

        if (!isValidUpiId(upiIdToVerify)) {
            showError("Please enter a valid UPI ID (e.g., name@paytm)");
            return;
        }

        // Show verification in progress
        btnVerifyUpi.setEnabled(false);
        btnVerifyUpi.setText("Verifying...");
        
        showSuccess("Starting UPI verification. This may take a few seconds...");

        // Start verification process
        paymentViewModel.verifyUpiId(upiIdToVerify);
    }

    private void generateQrCode() {
        String upiId = "";
        
        if (currentUpi != null) {
            upiId = currentUpi.getUpiId();
        } else {
            upiId = etUpiId.getText().toString().trim();
        }
        
        if (TextUtils.isEmpty(upiId)) {
            showError("Please enter UPI ID first");
            return;
        }
        
        if (!isValidUpiId(upiId)) {
            showError("Please enter a valid UPI ID");
            return;
        }

        paymentViewModel.generateUpiQrCode(upiId);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        // Validate UPI ID
        String upiId = etUpiId.getText().toString().trim();
        if (TextUtils.isEmpty(upiId)) {
            tilUpiId.setError("UPI ID is required");
            isValid = false;
        } else if (!isValidUpiId(upiId)) {
            tilUpiId.setError("Please enter a valid UPI ID (e.g., name@paytm)");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidUpiId(String upiId) {
        // UPI ID format: username@provider
        return upiId.matches("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$");
    }

    private void clearErrors() {
        tilUpiId.setError(null);
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
