package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodvan.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Phone Verification Activity for Vendor Dashboard
 * Handles phone number verification using Firebase Phone Authentication
 */
public class PhoneVerificationActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private View layoutPhoneInput, layoutOtpVerification, layoutVerificationSuccess, loadingOverlay;
    private TextInputLayout tilPhoneNumber;
    private TextInputEditText etPhoneNumber;
    private MaterialButton btnSendOtp, btnVerifyOtp, btnResendOtp, btnContinue;
    private TextView tvPhoneDisplay, tvTimer, tvLoadingText;
    
    // OTP Input Fields
    private TextInputEditText[] otpFields = new TextInputEditText[6];
    
    // Firebase Components
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    
    // Timer and State
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private String phoneNumber;
    private SharedPreferences sharedPreferences;
    
    // Test mode for development
    private static final boolean TEST_MODE = true; // Set to false for production
    private static final String TEST_OTP = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        
        initializeComponents();
        setupToolbar();
        setupOtpFields();
        setupClickListeners();
        setupFirebaseAuth();
        loadCurrentPhoneNumber();
    }

    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        toolbar = findViewById(R.id.toolbar);
        layoutPhoneInput = findViewById(R.id.layout_phone_input);
        layoutOtpVerification = findViewById(R.id.layout_otp_verification);
        layoutVerificationSuccess = findViewById(R.id.layout_verification_success);
        loadingOverlay = findViewById(R.id.loading_overlay);
        
        tilPhoneNumber = findViewById(R.id.til_phone_number);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        btnContinue = findViewById(R.id.btn_continue);
        
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        tvTimer = findViewById(R.id.tv_timer);
        tvLoadingText = findViewById(R.id.tv_loading_text);
        
        // Initialize OTP fields
        otpFields[0] = findViewById(R.id.et_otp_1);
        otpFields[1] = findViewById(R.id.et_otp_2);
        otpFields[2] = findViewById(R.id.et_otp_3);
        otpFields[3] = findViewById(R.id.et_otp_4);
        otpFields[4] = findViewById(R.id.et_otp_5);
        otpFields[5] = findViewById(R.id.et_otp_6);
        
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        sharedPreferences = getSharedPreferences("VendorPrefs", MODE_PRIVATE);
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * Setup OTP input fields with auto-focus functionality
     */
    private void setupOtpFields() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1) {
                        // Move to next field
                        if (index < otpFields.length - 1) {
                            otpFields[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        // Move to previous field
                        if (index > 0) {
                            otpFields[index - 1].requestFocus();
                        }
                    }
                    
                    // Check if all fields are filled
                    checkOtpComplete();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    /**
     * Setup click listeners for all buttons
     */
    private void setupClickListeners() {
        // Phone number input validation
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
        
        btnSendOtp.setOnClickListener(v -> sendOtp());
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnResendOtp.setOnClickListener(v -> resendOtp());
        btnContinue.setOnClickListener(v -> {
            // Set result to indicate successful verification
            Intent resultIntent = new Intent();
            resultIntent.putExtra("phone_verified", true);
            resultIntent.putExtra("verified_phone", phoneNumber);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    /**
     * Setup Firebase Phone Authentication
     */
    private void setupFirebaseAuth() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                // Auto-verification completed
                hideLoading();
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                hideLoading();
                String errorMessage = "Verification failed: ";
                
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    errorMessage += "Invalid phone number format";
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    errorMessage += "Too many requests. Please try again later";
                } else if (e instanceof FirebaseNetworkException) {
                    errorMessage += "Network error. Check your internet connection";
                } else {
                    errorMessage += e.getMessage();
                }
                
                showError(errorMessage);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, 
                                 @NonNull PhoneAuthProvider.ForceResendingToken token) {
                hideLoading();
                PhoneVerificationActivity.this.verificationId = verificationId;
                PhoneVerificationActivity.this.resendToken = token;
                showOtpVerificationLayout();
                startTimer();
            }
        };
    }

    /**
     * Load current phone number if available
     */
    private void loadCurrentPhoneNumber() {
        String currentPhone = sharedPreferences.getString("vendor_phone", "");
        if (!currentPhone.isEmpty()) {
            etPhoneNumber.setText(currentPhone);
        }
    }

    /**
     * Validate phone number input
     */
    private void validatePhoneNumber(String phone) {
        if (phone.length() == 10 && phone.matches("\\d+")) {
            tilPhoneNumber.setError(null);
            btnSendOtp.setEnabled(true);
            btnSendOtp.setBackgroundTintList(getResources().getColorStateList(R.color.primary_color, null));
        } else {
            if (phone.length() > 0) {
                tilPhoneNumber.setError("Enter a valid 10-digit phone number");
            }
            btnSendOtp.setEnabled(false);
            btnSendOtp.setBackgroundTintList(getResources().getColorStateList(R.color.gray_400, null));
        }
    }

    /**
     * Send OTP to phone number
     */
    private void sendOtp() {
        phoneNumber = etPhoneNumber.getText().toString().trim();
        
        // Validate phone number
        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) {
            tilPhoneNumber.setError("Enter a valid 10-digit phone number");
            return;
        }
        
        // Clear any previous errors
        tilPhoneNumber.setError(null);
        
        showLoading("Sending verification code...");
        
        // Format phone number with country code
        String fullPhoneNumber = "+91" + phoneNumber;
        
        try {
            if (TEST_MODE) {
                // In test mode, simulate OTP sending
                hideLoading();
                showOtpVerificationLayout();
                startTimer();
                showSuccess("Test mode: Use OTP " + TEST_OTP + " for verification");
            } else {
                // Production mode - use Firebase Phone Auth
                PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                        .setPhoneNumber(fullPhoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callbacks)
                        .build();
                
                PhoneAuthProvider.verifyPhoneNumber(options);
                
                // Show success message
                showSuccess("Verification code will be sent to " + fullPhoneNumber);
            }
            
        } catch (Exception e) {
            hideLoading();
            showError("Failed to send verification code: " + e.getMessage());
        }
    }

    /**
     * Resend OTP
     */
    private void resendOtp() {
        if (resendToken == null) {
            showError("Cannot resend OTP at this time");
            return;
        }
        
        showLoading("Resending verification code...");
        
        String fullPhoneNumber = "+91" + phoneNumber;
        
        try {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(fullPhoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(callbacks)
                    .setForceResendingToken(resendToken)
                    .build();
            
            PhoneAuthProvider.verifyPhoneNumber(options);
            
            // Show success message
            showSuccess("Verification code resent to " + fullPhoneNumber);
            
        } catch (Exception e) {
            hideLoading();
            showError("Failed to resend verification code: " + e.getMessage());
        }
    }

    /**
     * Verify entered OTP
     */
    private void verifyOtp() {
        String otp = getEnteredOtp();
        if (otp.length() != 6) {
            showError("Please enter complete OTP");
            return;
        }
        
        showLoading("Verifying code...");
        
        if (TEST_MODE) {
            // In test mode, check against test OTP
            if (otp.equals(TEST_OTP)) {
                hideLoading();
                saveVerifiedPhoneNumber();
                updateFirestoreProfile();
                showVerificationSuccess();
                
                // Immediately send result back to Settings activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("phone_verified", true);
                resultIntent.putExtra("verified_phone", phoneNumber);
                setResult(RESULT_OK, resultIntent);
            } else {
                hideLoading();
                showError("Invalid verification code. Use " + TEST_OTP + " for testing");
                clearOtpFields();
            }
        } else {
            // Production mode - use Firebase Phone Auth
            if (verificationId == null) {
                hideLoading();
                showError("Verification ID not found. Please resend OTP.");
                return;
            }
            
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            signInWithPhoneAuthCredential(credential);
        }
    }

    /**
     * Sign in with phone auth credential
     */
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    hideLoading();
                    if (task.isSuccessful()) {
                        // Verification successful
                        saveVerifiedPhoneNumber();
                        updateFirestoreProfile();
                        showVerificationSuccess();
                    } else {
                        showError("Invalid verification code");
                        clearOtpFields();
                    }
                });
    }

    /**
     * Save verified phone number to SharedPreferences
     */
    private void saveVerifiedPhoneNumber() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("vendor_phone", phoneNumber);
        editor.putBoolean("phone_verified", true);
        editor.apply();
    }

    /**
     * Update phone number in Firestore
     */
    private void updateFirestoreProfile() {
        String vendorId = sharedPreferences.getString("vendor_id", "");
        if (vendorId.isEmpty()) {
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("phone", phoneNumber);
        updates.put("phoneVerified", true);
        updates.put("updatedAt", System.currentTimeMillis());
        
        firestore.collection("vendors")
                .document(vendorId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Profile updated successfully
                })
                .addOnFailureListener(e -> {
                    // Handle error silently as the main verification is complete
                });
    }

    /**
     * Show OTP verification layout
     */
    private void showOtpVerificationLayout() {
        layoutPhoneInput.setVisibility(View.GONE);
        layoutOtpVerification.setVisibility(View.VISIBLE);
        layoutVerificationSuccess.setVisibility(View.GONE);
        
        String formattedPhone = "+91 " + phoneNumber.substring(0, 5) + " " + phoneNumber.substring(5);
        tvPhoneDisplay.setText("Code sent to " + formattedPhone);
        
        // Focus on first OTP field
        otpFields[0].requestFocus();
    }

    /**
     * Show verification success layout
     */
    private void showVerificationSuccess() {
        layoutPhoneInput.setVisibility(View.GONE);
        layoutOtpVerification.setVisibility(View.GONE);
        layoutVerificationSuccess.setVisibility(View.VISIBLE);
        
        stopTimer();
    }

    /**
     * Start countdown timer for resend OTP
     */
    private void startTimer() {
        isTimerRunning = true;
        btnResendOtp.setEnabled(false);
        
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(String.format("Resend code in 00:%02d", seconds));
            }

            @Override
            public void onFinish() {
                isTimerRunning = false;
                tvTimer.setText("Didn't receive code?");
                btnResendOtp.setEnabled(true);
            }
        };
        
        countDownTimer.start();
    }

    /**
     * Stop countdown timer
     */
    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isTimerRunning = false;
        }
    }

    /**
     * Check if OTP is complete and enable verify button
     */
    private void checkOtpComplete() {
        String otp = getEnteredOtp();
        boolean isComplete = otp.length() == 6;
        btnVerifyOtp.setEnabled(isComplete);
        
        if (isComplete) {
            btnVerifyOtp.setBackgroundTintList(getResources().getColorStateList(R.color.primary_color, null));
        } else {
            btnVerifyOtp.setBackgroundTintList(getResources().getColorStateList(R.color.gray_400, null));
        }
    }

    /**
     * Get entered OTP from all fields
     */
    private String getEnteredOtp() {
        StringBuilder otp = new StringBuilder();
        for (TextInputEditText field : otpFields) {
            otp.append(field.getText().toString());
        }
        return otp.toString();
    }

    /**
     * Clear all OTP fields
     */
    private void clearOtpFields() {
        for (TextInputEditText field : otpFields) {
            field.setText("");
        }
        otpFields[0].requestFocus();
        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setBackgroundTintList(getResources().getColorStateList(R.color.gray_400, null));
    }

    /**
     * Show loading overlay
     */
    private void showLoading(String message) {
        tvLoadingText.setText(message);
        loadingOverlay.setVisibility(View.VISIBLE);
    }

    /**
     * Hide loading overlay
     */
    private void hideLoading() {
        loadingOverlay.setVisibility(View.GONE);
    }

    /**
     * Show error message using Snackbar
     */
    private void showError(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.error_color, null));
        snackbar.show();
    }

    /**
     * Show success message using Snackbar
     */
    private void showSuccess(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG);
        snackbar.setBackgroundTint(getResources().getColor(R.color.success_color, null));
        snackbar.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    public void onBackPressed() {
        if (layoutVerificationSuccess.getVisibility() == View.VISIBLE) {
            // If verification is successful, send result back
            Intent resultIntent = new Intent();
            resultIntent.putExtra("phone_verified", true);
            resultIntent.putExtra("verified_phone", phoneNumber);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            // Show confirmation dialog for incomplete verification
            super.onBackPressed();
        }
    }
}
