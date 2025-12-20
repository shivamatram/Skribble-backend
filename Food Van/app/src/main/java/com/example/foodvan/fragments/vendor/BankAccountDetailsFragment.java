package com.example.foodvan.fragments.vendor;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.models.BankAccountDetails;
import com.example.foodvan.viewmodels.PaymentViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Fragment for managing bank account details
 * Features:
 * - Display existing bank account information
 * - Add/Edit bank account details
 * - Account verification status
 * - Account type selection (Savings/Current)
 */
public class BankAccountDetailsFragment extends Fragment {

    private static final String TAG = "BankAccountDetails";

    // UI Components
    private MaterialCardView cardExistingAccount, cardAddAccount;
    private TextInputLayout tilAccountHolder, tilAccountNumber, tilConfirmAccount, tilIfscCode, tilBankName;
    private TextInputEditText etAccountHolder, etAccountNumber, etConfirmAccount, etIfscCode;
    private AutoCompleteTextView etBankName;
    private RadioGroup rgAccountType;
    private RadioButton rbSavings, rbCurrent;
    private MaterialButton btnEditAccount, btnAddAccount, btnSaveAccount, btnVerifyAccount;

    // Data & Services
    private PaymentViewModel paymentViewModel;
    private BankAccountDetails currentAccount;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bank_account_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        initializeViewModel();
        setupBankNameDropdown();
        setupClickListeners();
        observeData();
        
        // Load data after a short delay to ensure ViewModel is ready
        view.post(() -> loadBankAccountData());
    }

    private void initializeViews(View view) {
        // Cards
        cardExistingAccount = view.findViewById(R.id.card_existing_account);
        cardAddAccount = view.findViewById(R.id.card_add_account);

        // Input fields
        tilAccountHolder = view.findViewById(R.id.til_account_holder);
        tilAccountNumber = view.findViewById(R.id.til_account_number);
        tilConfirmAccount = view.findViewById(R.id.til_confirm_account);
        tilIfscCode = view.findViewById(R.id.til_ifsc_code);
        tilBankName = view.findViewById(R.id.til_bank_name);

        etAccountHolder = view.findViewById(R.id.et_account_holder);
        etAccountNumber = view.findViewById(R.id.et_account_number);
        etConfirmAccount = view.findViewById(R.id.et_confirm_account);
        etIfscCode = view.findViewById(R.id.et_ifsc_code);
        etBankName = view.findViewById(R.id.et_bank_name);

        // Radio buttons
        rgAccountType = view.findViewById(R.id.rg_account_type);
        rbSavings = view.findViewById(R.id.rb_savings);
        rbCurrent = view.findViewById(R.id.rb_current);

        // Buttons
        btnEditAccount = view.findViewById(R.id.btn_edit_account);
        btnAddAccount = view.findViewById(R.id.btn_add_account);
        btnSaveAccount = view.findViewById(R.id.btn_save_account);
        btnVerifyAccount = view.findViewById(R.id.btn_verify_account);
    }

    private void initializeViewModel() {
        paymentViewModel = new ViewModelProvider(requireActivity()).get(PaymentViewModel.class);
    }

    private void setupBankNameDropdown() {
        String[] bankNames = {
            "State Bank of India (SBI)",
            "HDFC Bank",
            "ICICI Bank", 
            "Axis Bank",
            "Punjab National Bank (PNB)",
            "Bank of Baroda (BOB)",
            "Bank of Maharashtra",
            "Canara Bank",
            "Union Bank of India",
            "Bank of India (BOI)",
            "Indian Bank",
            "Central Bank of India",
            "Yes Bank",
            "Kotak Mahindra Bank",
            "IndusInd Bank",
            "Federal Bank",
            "South Indian Bank",
            "Karnataka Bank",
            "City Union Bank",
            "DCB Bank",
            "RBL Bank",
            "IDBI Bank",
            "Indian Overseas Bank (IOB)",
            "UCO Bank",
            "Punjab & Sind Bank",
            "Bandhan Bank",
            "IDFC First Bank",
            "AU Small Finance Bank",
            "Equitas Small Finance Bank",
            "Jana Small Finance Bank",
            "Ujjivan Small Finance Bank",
            "Suryoday Small Finance Bank",
            "Fincare Small Finance Bank",
            "ESAF Small Finance Bank",
            "North East Small Finance Bank",
            "Capital Small Finance Bank",
            "Unity Small Finance Bank",
            "Paytm Payments Bank",
            "Airtel Payments Bank",
            "India Post Payments Bank",
            "Jio Payments Bank",
            "NSDL Payments Bank",
            "Fino Payments Bank",
            "Aditya Birla Idea Payments Bank",
            "Citibank",
            "Standard Chartered Bank",
            "HSBC Bank",
            "Deutsche Bank",
            "Barclays Bank",
            "American Express Banking Corp",
            "DBS Bank India",
            "Mizuho Bank",
            "Bank of America",
            "JPMorgan Chase Bank",
            "Shinhan Bank",
            "MUFG Bank",
            "Sumitomo Mitsui Banking Corporation",
            "BNP Paribas",
            "Credit Suisse AG",
            "UBS AG",
            "The Hongkong and Shanghai Banking Corporation",
            "Doha Bank",
            "Emirates NBD Bank",
            "First Abu Dhabi Bank",
            "Mashreq Bank",
            "National Bank of Bahrain",
            "Qatar National Bank",
            "Rakbank",
            "Nainital Bank",
            "Lakshmi Vilas Bank",
            "Tamilnad Mercantile Bank",
            "Karur Vysya Bank",
            "Dhanlaxmi Bank",
            "Catholic Syrian Bank",
            "Jammu & Kashmir Bank",
            "Rajasthan Marudhara Gramin Bank",
            "Assam Gramin Vikash Bank",
            "Aryavart Bank",
            "Uttar Bihar Gramin Bank",
            "Puduvai Bharathiar Grama Bank",
            "Madhya Pradesh Gramin Bank",
            "Himachal Pradesh Gramin Bank",
            "Chaitanya Godavari Gramin Bank",
            "Andhra Pradesh Grameena Vikas Bank",
            "Telangana Grameena Bank",
            "Karnataka Gramin Bank",
            "Kerala Gramin Bank",
            "Tamil Nadu Grama Bank",
            "Saptagiri Grameena Bank",
            "Prathama UP Gramin Bank",
            "Sarva Haryana Gramin Bank",
            "Punjab Gramin Bank",
            "Rajasthan Marudhara Gramin Bank",
            "Baroda Gujarat Gramin Bank",
            "Dena Gujarat Gramin Bank",
            "Maharashtra Gramin Bank",
            "Vidharbha Konkan Gramin Bank",
            "Jharkhand Rajya Gramin Bank",
            "Odisha Gramya Bank",
            "Utkal Grameen Bank",
            "West Bengal Gramin Bank",
            "Paschim Banga Gramin Bank",
            "Bangiya Gramin Vikash Bank",
            "Tripura Gramin Bank",
            "Manipur Rural Bank",
            "Meghalaya Rural Bank",
            "Arunachal Pradesh Rural Bank",
            "Nagaland Rural Bank",
            "Mizoram Rural Bank"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
            android.R.layout.simple_dropdown_item_1line, bankNames);
        etBankName.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnEditAccount.setOnClickListener(v -> enableEditMode());
        btnAddAccount.setOnClickListener(v -> showAddAccountForm());
        btnSaveAccount.setOnClickListener(v -> saveBankAccount());
        btnVerifyAccount.setOnClickListener(v -> verifyBankAccount());
    }

    private void observeData() {
        paymentViewModel.getBankAccountDetails().observe(getViewLifecycleOwner(), bankAccount -> {
            currentAccount = bankAccount;
            updateUI();
        });

        paymentViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicators
            btnSaveAccount.setEnabled(!isLoading);
            btnVerifyAccount.setEnabled(!isLoading);
        });

        paymentViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                showError(error);
                
                // Reset verification button state if there was an error
                if (error.contains("verification") || error.contains("verify")) {
                    btnVerifyAccount.setEnabled(true);
                    btnVerifyAccount.setText("Verify Account");
                }
            }
        });

        paymentViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSuccess(message);
                
                // Reset verification button state if verification was successful
                if (message.contains("verification") || message.contains("verified")) {
                    btnVerifyAccount.setEnabled(true);
                    btnVerifyAccount.setText("Verify Account");
                    
                    // Reload data to get updated verification status
                    loadBankAccountData();
                }
            }
        });
    }

    private void loadBankAccountData() {
        paymentViewModel.loadBankAccountDetails();
    }

    private void updateUI() {
        if (currentAccount != null) {
            // Show existing account card
            cardExistingAccount.setVisibility(View.VISIBLE);
            cardAddAccount.setVisibility(View.GONE);
            
            // Populate fields with existing data
            populateExistingAccountData();
        } else {
            // Show add account form
            cardExistingAccount.setVisibility(View.GONE);
            cardAddAccount.setVisibility(View.VISIBLE);
        }
    }

    private void populateExistingAccountData() {
        if (currentAccount == null) return;

        // Populate all fields with actual saved data
        etAccountHolder.setText(currentAccount.getAccountHolderName() != null ? currentAccount.getAccountHolderName() : "");
        etAccountNumber.setText(maskAccountNumber(currentAccount.getAccountNumber()));
        etConfirmAccount.setText(maskAccountNumber(currentAccount.getAccountNumber()));
        etIfscCode.setText(currentAccount.getIfscCode() != null ? currentAccount.getIfscCode() : "");
        etBankName.setText(currentAccount.getBankName() != null ? currentAccount.getBankName() : "", false);
        
        // Set account type radio button
        if ("Savings".equals(currentAccount.getAccountType())) {
            rbSavings.setChecked(true);
        } else if ("Current".equals(currentAccount.getAccountType())) {
            rbCurrent.setChecked(true);
        } else {
            rbSavings.setChecked(true); // Default to Savings
        }

        // Update verification status display if available
        updateVerificationStatusDisplay();

        // Disable editing initially
        setFieldsEnabled(false);
        
        // Show edit button
        btnEditAccount.setVisibility(View.VISIBLE);
        btnSaveAccount.setVisibility(View.GONE);
    }

    private void updateVerificationStatusDisplay() {
        if (currentAccount != null && currentAccount.isVerified()) {
            // Show verification status (you can add a chip or text view for this)
            showSuccess("Bank account is verified ✓");
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        
        String masked = "****" + accountNumber.substring(accountNumber.length() - 4);
        return masked;
    }

    private void enableEditMode() {
        isEditMode = true;
        
        // Enable all fields first
        setFieldsEnabled(true);
        
        // Show full account details for editing (unmask sensitive data)
        if (currentAccount != null) {
            etAccountHolder.setText(currentAccount.getAccountHolderName() != null ? currentAccount.getAccountHolderName() : "");
            etAccountNumber.setText(currentAccount.getAccountNumber() != null ? currentAccount.getAccountNumber() : "");
            etConfirmAccount.setText(currentAccount.getAccountNumber() != null ? currentAccount.getAccountNumber() : "");
            etIfscCode.setText(currentAccount.getIfscCode() != null ? currentAccount.getIfscCode() : "");
            etBankName.setText(currentAccount.getBankName() != null ? currentAccount.getBankName() : "", false);
            
            // Set account type
            if ("Savings".equals(currentAccount.getAccountType())) {
                rbSavings.setChecked(true);
            } else if ("Current".equals(currentAccount.getAccountType())) {
                rbCurrent.setChecked(true);
            } else {
                rbSavings.setChecked(true);
            }
        }
        
        // Update button visibility
        btnEditAccount.setVisibility(View.GONE);
        btnSaveAccount.setVisibility(View.VISIBLE);
        
        // Focus on first field and position cursor at end for easy editing
        etAccountHolder.requestFocus();
        if (etAccountHolder.getText().length() > 0) {
            etAccountHolder.setSelection(etAccountHolder.getText().length());
        }
        
        // Show soft keyboard for immediate editing
        android.view.inputmethod.InputMethodManager imm = 
            (android.view.inputmethod.InputMethodManager) requireActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(etAccountHolder, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }
        
        // Show user feedback
        showSuccess("Edit mode enabled. You can now modify your bank account details.");
    }

    private void showAddAccountForm() {
        cardExistingAccount.setVisibility(View.GONE);
        cardAddAccount.setVisibility(View.VISIBLE);
        setFieldsEnabled(true);
        clearFields();
    }

    private void setFieldsEnabled(boolean enabled) {
        // Enable/disable all input fields
        etAccountHolder.setEnabled(enabled);
        etAccountHolder.setFocusable(enabled);
        etAccountHolder.setFocusableInTouchMode(enabled);
        
        etAccountNumber.setEnabled(enabled);
        etAccountNumber.setFocusable(enabled);
        etAccountNumber.setFocusableInTouchMode(enabled);
        
        etConfirmAccount.setEnabled(enabled);
        etConfirmAccount.setFocusable(enabled);
        etConfirmAccount.setFocusableInTouchMode(enabled);
        
        etIfscCode.setEnabled(enabled);
        etIfscCode.setFocusable(enabled);
        etIfscCode.setFocusableInTouchMode(enabled);
        
        etBankName.setEnabled(enabled);
        etBankName.setFocusable(enabled);
        etBankName.setFocusableInTouchMode(enabled);
        
        // Enable/disable radio group and buttons
        rgAccountType.setEnabled(enabled);
        rbSavings.setEnabled(enabled);
        rbSavings.setClickable(enabled);
        rbCurrent.setEnabled(enabled);
        rbCurrent.setClickable(enabled);
        
        // Set visual feedback for enabled/disabled state
        float alpha = enabled ? 1.0f : 0.6f;
        tilAccountHolder.setAlpha(alpha);
        tilAccountNumber.setAlpha(alpha);
        tilConfirmAccount.setAlpha(alpha);
        tilIfscCode.setAlpha(alpha);
        tilBankName.setAlpha(alpha);
        rgAccountType.setAlpha(alpha);
    }

    private void clearFields() {
        etAccountHolder.setText("");
        etAccountNumber.setText("");
        etConfirmAccount.setText("");
        etIfscCode.setText("");
        etBankName.setText("", false);
        rbSavings.setChecked(true);
    }

    private void saveBankAccount() {
        if (!validateInputs()) return;

        // Create or update bank account details
        BankAccountDetails bankAccount = currentAccount != null ? currentAccount : new BankAccountDetails();
        
        // Update with form data
        bankAccount.setAccountHolderName(etAccountHolder.getText().toString().trim());
        bankAccount.setAccountNumber(etAccountNumber.getText().toString().trim());
        bankAccount.setIfscCode(etIfscCode.getText().toString().trim().toUpperCase());
        bankAccount.setBankName(etBankName.getText().toString().trim());
        bankAccount.setAccountType(rbSavings.isChecked() ? "Savings" : "Current");
        
        // Only reset verification if account details changed significantly
        if (currentAccount == null || 
            !bankAccount.getAccountNumber().equals(currentAccount.getAccountNumber()) ||
            !bankAccount.getIfscCode().equals(currentAccount.getIfscCode())) {
            bankAccount.setVerified(false); // Reset verification status for new/changed account
        }
        
        bankAccount.setLastUpdated(System.currentTimeMillis());

        // Update current account reference
        currentAccount = bankAccount;
        
        // Save to Firebase
        paymentViewModel.saveBankAccountDetails(bankAccount);
        
        // Reset UI state
        isEditMode = false;
        setFieldsEnabled(false);
        btnEditAccount.setVisibility(View.VISIBLE);
        btnSaveAccount.setVisibility(View.GONE);
        
        // Mask account number again for display
        etAccountNumber.setText(maskAccountNumber(bankAccount.getAccountNumber()));
        etConfirmAccount.setText(maskAccountNumber(bankAccount.getAccountNumber()));
        
        showSuccess("Bank account details saved successfully!");
    }

    private void verifyBankAccount() {
        // Check if we have current account data
        if (currentAccount == null) {
            showError("Please save bank account details first");
            return;
        }

        // Validate that all required fields are filled
        if (currentAccount.getAccountHolderName() == null || currentAccount.getAccountHolderName().trim().isEmpty()) {
            showError("Account holder name is required for verification");
            return;
        }

        if (currentAccount.getAccountNumber() == null || currentAccount.getAccountNumber().trim().isEmpty()) {
            showError("Account number is required for verification");
            return;
        }

        if (currentAccount.getIfscCode() == null || currentAccount.getIfscCode().trim().isEmpty()) {
            showError("IFSC code is required for verification");
            return;
        }

        if (currentAccount.getBankName() == null || currentAccount.getBankName().trim().isEmpty()) {
            showError("Bank name is required for verification");
            return;
        }

        // Show verification in progress
        btnVerifyAccount.setEnabled(false);
        btnVerifyAccount.setText("Verifying...");
        
        showSuccess("Starting bank account verification. This may take a few seconds...");

        // Start verification process
        paymentViewModel.verifyBankAccount(currentAccount);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        // Clear previous errors
        clearErrors();

        // Validate account holder name
        String accountHolder = etAccountHolder.getText().toString().trim();
        if (TextUtils.isEmpty(accountHolder)) {
            tilAccountHolder.setError("Account holder name is required");
            isValid = false;
        } else if (accountHolder.length() < 2) {
            tilAccountHolder.setError("Account holder name must be at least 2 characters");
            isValid = false;
        }

        // Validate account number
        String accountNumber = etAccountNumber.getText().toString().trim();
        if (TextUtils.isEmpty(accountNumber)) {
            tilAccountNumber.setError("Account number is required");
            isValid = false;
        } else if (accountNumber.length() < 9 || accountNumber.length() > 18) {
            tilAccountNumber.setError("Account number must be 9-18 digits");
            isValid = false;
        }

        // Validate confirm account number
        String confirmAccount = etConfirmAccount.getText().toString().trim();
        if (TextUtils.isEmpty(confirmAccount)) {
            tilConfirmAccount.setError("Please confirm account number");
            isValid = false;
        } else if (!accountNumber.equals(confirmAccount)) {
            tilConfirmAccount.setError("Account numbers do not match");
            isValid = false;
        }

        // Validate IFSC code
        String ifscCode = etIfscCode.getText().toString().trim();
        if (TextUtils.isEmpty(ifscCode)) {
            tilIfscCode.setError("IFSC code is required");
            isValid = false;
        } else if (!isValidIfscCode(ifscCode)) {
            tilIfscCode.setError("Please enter a valid IFSC code");
            isValid = false;
        }

        // Validate bank name
        String bankName = etBankName.getText().toString().trim();
        if (TextUtils.isEmpty(bankName)) {
            tilBankName.setError("Bank name is required");
            isValid = false;
        }

        return isValid;
    }

    private boolean isValidIfscCode(String ifscCode) {
        return ifscCode.matches("^[A-Z]{4}0[A-Z0-9]{6}$");
    }

    private void clearErrors() {
        tilAccountHolder.setError(null);
        tilAccountNumber.setError(null);
        tilConfirmAccount.setError(null);
        tilIfscCode.setError(null);
        tilBankName.setError(null);
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
