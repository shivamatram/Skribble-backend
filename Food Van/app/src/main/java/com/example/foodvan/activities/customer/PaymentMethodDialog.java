package com.example.foodvan.activities.customer;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.foodvan.R;
import com.example.foodvan.models.PaymentMethod;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * Dialog for adding and editing payment methods
 */
public class PaymentMethodDialog {

    private Context context;
    private Dialog dialog;
    private PaymentMethod editingPaymentMethod;
    
    // Dialog views
    private TextView tvDialogTitle;
    private ImageView ivCloseDialog;
    private ChipGroup chipGroupPaymentType;
    private Chip chipCreditCard, chipDebitCard, chipUpi, chipWallet;
    
    // Card form views
    private LinearLayout layoutCardForm;
    private TextInputLayout tilCardNumber, tilCardholderName, tilExpiryDate, tilCvv, tilBankName;
    private TextInputEditText etCardNumber, etCardholderName, etExpiryDate, etCvv, etBankName;
    
    // UPI form views
    private LinearLayout layoutUpiForm;
    private TextInputLayout tilUpiId;
    private TextInputEditText etUpiId;
    private ChipGroup chipGroupUpiProvider;
    private Chip chipGpay, chipPhonepe, chipPaytm, chipOtherUpi;
    
    // Wallet form views
    private LinearLayout layoutWalletForm;
    private ChipGroup chipGroupWalletProvider;
    private Chip chipPaytmWallet, chipPhonepeWallet, chipAmazonPay;
    private TextInputLayout tilWalletId;
    private TextInputEditText etWalletId;
    
    // Common views
    private MaterialCheckBox cbSetDefault;
    private MaterialButton btnCancel, btnSave;
    
    // Current payment type
    private PaymentMethod.PaymentType currentPaymentType = PaymentMethod.PaymentType.CREDIT_CARD;

    public interface OnPaymentMethodSavedListener {
        void onPaymentMethodSaved(PaymentMethod paymentMethod);
    }

    public PaymentMethodDialog(Context context) {
        this.context = context;
        createDialog();
    }

    private void createDialog() {
        dialog = new Dialog(context, android.R.style.Theme_Material_Dialog);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_payment_method, null);
        dialog.setContentView(dialogView);
        
        initializeViews(dialogView);
        setupClickListeners();
        setupTextWatchers();
    }

    private void initializeViews(View dialogView) {
        // Header
        tvDialogTitle = dialogView.findViewById(R.id.tv_dialog_title);
        ivCloseDialog = dialogView.findViewById(R.id.iv_close_dialog);
        
        // Payment type chips
        chipGroupPaymentType = dialogView.findViewById(R.id.chip_group_payment_type);
        chipCreditCard = dialogView.findViewById(R.id.chip_credit_card);
        chipDebitCard = dialogView.findViewById(R.id.chip_debit_card);
        chipUpi = dialogView.findViewById(R.id.chip_upi);
        chipWallet = dialogView.findViewById(R.id.chip_wallet);
        
        // Card form
        layoutCardForm = dialogView.findViewById(R.id.layout_card_form);
        tilCardNumber = dialogView.findViewById(R.id.til_card_number);
        tilCardholderName = dialogView.findViewById(R.id.til_cardholder_name);
        tilExpiryDate = dialogView.findViewById(R.id.til_expiry_date);
        tilCvv = dialogView.findViewById(R.id.til_cvv);
        tilBankName = dialogView.findViewById(R.id.til_bank_name);
        etCardNumber = dialogView.findViewById(R.id.et_card_number);
        etCardholderName = dialogView.findViewById(R.id.et_cardholder_name);
        etExpiryDate = dialogView.findViewById(R.id.et_expiry_date);
        etCvv = dialogView.findViewById(R.id.et_cvv);
        etBankName = dialogView.findViewById(R.id.et_bank_name);
        
        // UPI form
        layoutUpiForm = dialogView.findViewById(R.id.layout_upi_form);
        tilUpiId = dialogView.findViewById(R.id.til_upi_id);
        etUpiId = dialogView.findViewById(R.id.et_upi_id);
        chipGroupUpiProvider = dialogView.findViewById(R.id.chip_group_upi_provider);
        chipGpay = dialogView.findViewById(R.id.chip_gpay);
        chipPhonepe = dialogView.findViewById(R.id.chip_phonepe);
        chipPaytm = dialogView.findViewById(R.id.chip_paytm);
        chipOtherUpi = dialogView.findViewById(R.id.chip_other_upi);
        
        // Wallet form
        layoutWalletForm = dialogView.findViewById(R.id.layout_wallet_form);
        chipGroupWalletProvider = dialogView.findViewById(R.id.chip_group_wallet_provider);
        chipPaytmWallet = dialogView.findViewById(R.id.chip_paytm_wallet);
        chipPhonepeWallet = dialogView.findViewById(R.id.chip_phonepe_wallet);
        chipAmazonPay = dialogView.findViewById(R.id.chip_amazon_pay);
        tilWalletId = dialogView.findViewById(R.id.til_wallet_id);
        etWalletId = dialogView.findViewById(R.id.et_wallet_id);
        
        // Common
        cbSetDefault = dialogView.findViewById(R.id.cb_set_default);
        btnCancel = dialogView.findViewById(R.id.btn_cancel);
        btnSave = dialogView.findViewById(R.id.btn_save);
    }

    private void setupClickListeners() {
        ivCloseDialog.setOnClickListener(v -> dialog.dismiss());
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        // Payment type selection
        chipGroupPaymentType.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_credit_card) {
                    currentPaymentType = PaymentMethod.PaymentType.CREDIT_CARD;
                } else if (checkedId == R.id.chip_debit_card) {
                    currentPaymentType = PaymentMethod.PaymentType.DEBIT_CARD;
                } else if (checkedId == R.id.chip_upi) {
                    currentPaymentType = PaymentMethod.PaymentType.UPI;
                } else if (checkedId == R.id.chip_wallet) {
                    currentPaymentType = PaymentMethod.PaymentType.WALLET;
                }
                updateFormVisibility();
            }
        });
    }

    private void setupTextWatchers() {
        // Card number formatting
        etCardNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                
                isFormatting = true;
                String text = s.toString().replaceAll("\\s", "");
                StringBuilder formatted = new StringBuilder();
                
                for (int i = 0; i < text.length(); i++) {
                    if (i > 0 && i % 4 == 0) {
                        formatted.append(" ");
                    }
                    formatted.append(text.charAt(i));
                }
                
                s.replace(0, s.length(), formatted.toString());
                isFormatting = false;
            }
        });
        
        // Expiry date formatting (MM/YY)
        etExpiryDate.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting = false;
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                
                isFormatting = true;
                String text = s.toString().replaceAll("/", "");
                
                if (text.length() >= 2) {
                    text = text.substring(0, 2) + "/" + text.substring(2);
                }
                
                s.replace(0, s.length(), text);
                isFormatting = false;
            }
        });
    }

    private void updateFormVisibility() {
        // Hide all forms first
        layoutCardForm.setVisibility(View.GONE);
        layoutUpiForm.setVisibility(View.GONE);
        layoutWalletForm.setVisibility(View.GONE);
        
        // Show relevant form
        switch (currentPaymentType) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                layoutCardForm.setVisibility(View.VISIBLE);
                break;
            case UPI:
                layoutUpiForm.setVisibility(View.VISIBLE);
                break;
            case WALLET:
                layoutWalletForm.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void showAddDialog(OnPaymentMethodSavedListener listener) {
        editingPaymentMethod = null;
        tvDialogTitle.setText("Add Payment Method");
        btnSave.setText("Save Payment Method");
        
        clearAllFields();
        chipCreditCard.setChecked(true);
        currentPaymentType = PaymentMethod.PaymentType.CREDIT_CARD;
        updateFormVisibility();
        
        btnSave.setOnClickListener(v -> {
            if (validateAndSave(listener)) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }

    public void showEditDialog(PaymentMethod paymentMethod, OnPaymentMethodSavedListener listener) {
        editingPaymentMethod = paymentMethod;
        tvDialogTitle.setText("Edit Payment Method");
        btnSave.setText("Update Payment Method");
        
        populateFields(paymentMethod);
        
        btnSave.setOnClickListener(v -> {
            if (validateAndSave(listener)) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }

    private void clearAllFields() {
        // Clear card fields
        etCardNumber.setText("");
        etCardholderName.setText("");
        etExpiryDate.setText("");
        etCvv.setText("");
        etBankName.setText("");
        
        // Clear UPI fields
        etUpiId.setText("");
        chipGroupUpiProvider.clearCheck();
        
        // Clear wallet fields
        chipGroupWalletProvider.clearCheck();
        etWalletId.setText("");
        
        // Clear common fields
        cbSetDefault.setChecked(false);
        
        // Clear errors
        clearAllErrors();
    }

    private void populateFields(PaymentMethod paymentMethod) {
        clearAllFields();
        
        // Set payment type
        currentPaymentType = paymentMethod.getType();
        switch (currentPaymentType) {
            case CREDIT_CARD:
                chipCreditCard.setChecked(true);
                break;
            case DEBIT_CARD:
                chipDebitCard.setChecked(true);
                break;
            case UPI:
                chipUpi.setChecked(true);
                break;
            case WALLET:
                chipWallet.setChecked(true);
                break;
        }
        updateFormVisibility();
        
        // Populate fields based on type
        switch (currentPaymentType) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                if (paymentMethod.getCardNumber() != null) {
                    etCardNumber.setText(paymentMethod.getCardNumber());
                }
                if (paymentMethod.getHolderName() != null) {
                    etCardholderName.setText(paymentMethod.getHolderName());
                }
                if (paymentMethod.getExpiryDate() != null) {
                    etExpiryDate.setText(paymentMethod.getExpiryDate());
                }
                if (paymentMethod.getBankName() != null) {
                    etBankName.setText(paymentMethod.getBankName());
                }
                break;
                
            case UPI:
                if (paymentMethod.getUpiId() != null) {
                    etUpiId.setText(paymentMethod.getUpiId());
                }
                selectUpiProvider(paymentMethod.getUpiProvider());
                break;
                
            case WALLET:
                selectWalletProvider(paymentMethod.getWalletProvider());
                if (paymentMethod.getWalletId() != null) {
                    etWalletId.setText(paymentMethod.getWalletId());
                }
                break;
        }
        
        cbSetDefault.setChecked(paymentMethod.isDefault());
    }

    private void selectUpiProvider(String provider) {
        if (provider == null) return;
        
        switch (provider.toLowerCase()) {
            case "google pay":
            case "gpay":
                chipGpay.setChecked(true);
                break;
            case "phonepe":
                chipPhonepe.setChecked(true);
                break;
            case "paytm":
                chipPaytm.setChecked(true);
                break;
            default:
                chipOtherUpi.setChecked(true);
                break;
        }
    }

    private void selectWalletProvider(String provider) {
        if (provider == null) return;
        
        switch (provider.toLowerCase()) {
            case "paytm":
            case "paytm wallet":
                chipPaytmWallet.setChecked(true);
                break;
            case "phonepe":
            case "phonepe wallet":
                chipPhonepeWallet.setChecked(true);
                break;
            case "amazon pay":
                chipAmazonPay.setChecked(true);
                break;
        }
    }

    private boolean validateAndSave(OnPaymentMethodSavedListener listener) {
        clearAllErrors();
        
        boolean isValid = true;
        
        switch (currentPaymentType) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                isValid = validateCardForm();
                break;
            case UPI:
                isValid = validateUpiForm();
                break;
            case WALLET:
                isValid = validateWalletForm();
                break;
        }
        
        if (isValid) {
            PaymentMethod paymentMethod = createPaymentMethod();
            if (listener != null) {
                listener.onPaymentMethodSaved(paymentMethod);
            }
            return true;
        }
        
        return false;
    }

    private boolean validateCardForm() {
        boolean isValid = true;
        
        String cardNumber = etCardNumber.getText().toString().trim().replaceAll("\\s", "");
        String holderName = etCardholderName.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String cvv = etCvv.getText().toString().trim();
        
        // Validate card number
        if (cardNumber.isEmpty()) {
            tilCardNumber.setError("Card number is required");
            isValid = false;
        } else if (cardNumber.length() < 13 || cardNumber.length() > 19) {
            tilCardNumber.setError("Invalid card number");
            isValid = false;
        } else if (!cardNumber.matches("\\d+")) {
            tilCardNumber.setError("Card number must contain only digits");
            isValid = false;
        }
        
        // Validate holder name
        if (holderName.isEmpty()) {
            tilCardholderName.setError("Cardholder name is required");
            isValid = false;
        }
        
        // Validate expiry date
        if (expiryDate.isEmpty()) {
            tilExpiryDate.setError("Expiry date is required");
            isValid = false;
        } else if (!expiryDate.matches("\\d{2}/\\d{2}")) {
            tilExpiryDate.setError("Invalid expiry date format (MM/YY)");
            isValid = false;
        }
        
        // Validate CVV
        if (cvv.isEmpty()) {
            tilCvv.setError("CVV is required");
            isValid = false;
        } else if (cvv.length() < 3 || cvv.length() > 4) {
            tilCvv.setError("Invalid CVV");
            isValid = false;
        }
        
        return isValid;
    }

    private boolean validateUpiForm() {
        boolean isValid = true;
        
        String upiId = etUpiId.getText().toString().trim();
        
        // Validate UPI ID
        if (upiId.isEmpty()) {
            tilUpiId.setError("UPI ID is required");
            isValid = false;
        } else if (!isValidUpiId(upiId)) {
            tilUpiId.setError("Invalid UPI ID format");
            isValid = false;
        }
        
        return isValid;
    }

    private boolean validateWalletForm() {
        boolean isValid = true;
        
        String walletId = etWalletId.getText().toString().trim();
        
        // Validate wallet ID
        if (walletId.isEmpty()) {
            tilWalletId.setError("Wallet ID is required");
            isValid = false;
        }
        
        // Check if wallet provider is selected
        if (chipGroupWalletProvider.getCheckedChipId() == View.NO_ID) {
            // Show error somehow - maybe a toast or snackbar
            return false;
        }
        
        return isValid;
    }

    private boolean isValidUpiId(String upiId) {
        // Basic UPI ID validation: should contain @ and have valid format
        Pattern upiPattern = Pattern.compile("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$");
        return upiPattern.matcher(upiId).matches();
    }

    private PaymentMethod createPaymentMethod() {
        PaymentMethod paymentMethod;
        
        if (editingPaymentMethod != null) {
            paymentMethod = editingPaymentMethod;
        } else {
            paymentMethod = new PaymentMethod();
        }
        
        paymentMethod.setType(currentPaymentType);
        paymentMethod.setDefault(cbSetDefault.isChecked());
        
        switch (currentPaymentType) {
            case CREDIT_CARD:
            case DEBIT_CARD:
                String cardNumber = etCardNumber.getText().toString().trim().replaceAll("\\s", "");
                String holderName = etCardholderName.getText().toString().trim();
                String expiryDate = etExpiryDate.getText().toString().trim();
                String bankName = etBankName.getText().toString().trim();
                
                paymentMethod.setCardNumber(cardNumber);
                paymentMethod.setHolderName(holderName);
                paymentMethod.setExpiryDate(expiryDate);
                if (!bankName.isEmpty()) {
                    paymentMethod.setBankName(bankName);
                }
                
                // Determine card type based on card number
                paymentMethod.setCardType(determineCardType(cardNumber));
                break;
                
            case UPI:
                String upiId = etUpiId.getText().toString().trim();
                String upiProvider = getSelectedUpiProvider();
                
                paymentMethod.setUpiId(upiId);
                paymentMethod.setUpiProvider(upiProvider);
                break;
                
            case WALLET:
                String walletId = etWalletId.getText().toString().trim();
                String walletProvider = getSelectedWalletProvider();
                
                paymentMethod.setWalletId(walletId);
                paymentMethod.setWalletProvider(walletProvider);
                break;
        }
        
        return paymentMethod;
    }

    private String determineCardType(String cardNumber) {
        if (cardNumber.startsWith("4")) {
            return "Visa";
        } else if (cardNumber.startsWith("5") || cardNumber.startsWith("2")) {
            return "MasterCard";
        } else if (cardNumber.startsWith("6")) {
            return "RuPay";
        } else if (cardNumber.startsWith("3")) {
            return "American Express";
        }
        return "Unknown";
    }

    private String getSelectedUpiProvider() {
        int checkedId = chipGroupUpiProvider.getCheckedChipId();
        if (checkedId == R.id.chip_gpay) {
            return "Google Pay";
        } else if (checkedId == R.id.chip_phonepe) {
            return "PhonePe";
        } else if (checkedId == R.id.chip_paytm) {
            return "Paytm";
        } else {
            return "Other";
        }
    }

    private String getSelectedWalletProvider() {
        int checkedId = chipGroupWalletProvider.getCheckedChipId();
        if (checkedId == R.id.chip_paytm_wallet) {
            return "Paytm Wallet";
        } else if (checkedId == R.id.chip_phonepe_wallet) {
            return "PhonePe Wallet";
        } else if (checkedId == R.id.chip_amazon_pay) {
            return "Amazon Pay";
        }
        return "Unknown";
    }

    private void clearAllErrors() {
        tilCardNumber.setError(null);
        tilCardholderName.setError(null);
        tilExpiryDate.setError(null);
        tilCvv.setError(null);
        tilBankName.setError(null);
        tilUpiId.setError(null);
        tilWalletId.setError(null);
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
