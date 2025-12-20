package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.foodvan.R;
import com.example.foodvan.utils.SessionManager;
import com.example.foodvan.workers.PhoneVerificationSyncWorker;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * PhoneVerificationActivity - Comprehensive phone number verification using Firebase Phone Auth
 * Features:
 * - Phone number display & edit with validation
 * - OTP sending via Firebase Auth
 * - 6-digit OTP input with auto-focus and auto-submit
 * - Resend timer with 60-second cooldown
 * - Firebase Realtime Database persistence
 * - Local cache (SharedPreferences) sync
 * - Offline queueing via WorkManager
 * - Material 3 design with orange theme
 */
public class PhoneVerificationActivity extends AppCompatActivity {

    private static final String TAG = "PhoneVerification";
    private static final int OTP_TIMEOUT_SECONDS = 60;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final String DEFAULT_COUNTRY_CODE = "+91";
    
    // Test mode for development - set to false for production
    private static final boolean TEST_MODE = true;
    private static final String TEST_PHONE = "9999999999";
    private static final String TEST_OTP = "123456";

    // UI Components
    private MaterialToolbar toolbar;
    private LinearLayout layoutPhoneInput, layoutOtpVerification, layoutVerificationSuccess;
    private FrameLayout loadingOverlay;
    private TextView tvLoadingText, tvPhoneDisplay, tvTimer, tvOtpError, tvCurrentPhone;
    
    // Phone Input
    private TextInputLayout tilPhoneNumber;
    private TextInputEditText etPhoneNumber;
    private MaterialButton btnSendOtp, btnChangePhone;
    
    // OTP Input
    private TextInputEditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private MaterialButton btnVerifyOtp, btnResendOtp, btnContinue;
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    
    // Data
    private SessionManager sessionManager;
    private String phoneNumber;
    private String countryCode = DEFAULT_COUNTRY_CODE;
    private CountDownTimer otpTimer;
    private boolean isOtpTimerRunning = false;
    private int otpAttempts = 0;
    private String originalPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        initializeServices();
        initializeViews();
        setupStatusBar();
        setupToolbar();
        setupClickListeners();
        setupOtpInputListeners();
        setupPhoneInputListener();
        loadCurrentPhoneNumber();
    }

    private void initializeServices() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sessionManager = new SessionManager(this);
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_color));
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        
        layoutPhoneInput = findViewById(R.id.layout_phone_input);
        layoutOtpVerification = findViewById(R.id.layout_otp_verification);
        layoutVerificationSuccess = findViewById(R.id.layout_verification_success);
        loadingOverlay = findViewById(R.id.loading_overlay);
        
        tvLoadingText = findViewById(R.id.tv_loading_text);
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        tvTimer = findViewById(R.id.tv_timer);
        tvOtpError = findViewById(R.id.tv_otp_error);
        tvCurrentPhone = findViewById(R.id.tv_current_phone);
        
        tilPhoneNumber = findViewById(R.id.til_phone_number);
        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        btnChangePhone = findViewById(R.id.btn_change_phone);
        
        etOtp1 = findViewById(R.id.et_otp_1);
        etOtp2 = findViewById(R.id.et_otp_2);
        etOtp3 = findViewById(R.id.et_otp_3);
        etOtp4 = findViewById(R.id.et_otp_4);
        etOtp5 = findViewById(R.id.et_otp_5);
        etOtp6 = findViewById(R.id.et_otp_6);
        
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        btnContinue = findViewById(R.id.btn_continue);
        
        if (tvOtpError != null) {
            tvOtpError.setVisibility(View.GONE);
        }
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void loadCurrentPhoneNumber() {
        String currentPhone = sessionManager.getUserPhone();
        originalPhoneNumber = currentPhone;
        
        String intentPhone = getIntent().getStringExtra("phone_number");
        if (intentPhone != null && !intentPhone.isEmpty()) {
            currentPhone = intentPhone;
        }
        
        if (tvCurrentPhone != null && currentPhone != null && !currentPhone.isEmpty()) {
            String formattedPhone = formatPhoneForDisplay(countryCode + currentPhone);
            tvCurrentPhone.setText("Current: " + formattedPhone);
            tvCurrentPhone.setVisibility(View.VISIBLE);
        }
        
        if (currentPhone != null && !currentPhone.isEmpty()) {
            etPhoneNumber.setText(currentPhone);
        }
        
        validatePhoneNumber();
    }

    private void setupClickListeners() {
        btnSendOtp.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                showSnackbar("No internet connection. Please check your network.");
                return;
            }
            sendVerificationCode();
        });
        
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        
        btnResendOtp.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                showSnackbar("No internet connection. Please check your network.");
                return;
            }
            resendVerificationCode();
        });
        
        btnContinue.setOnClickListener(v -> finishVerification());
        
        if (btnChangePhone != null) {
            btnChangePhone.setOnClickListener(v -> showPhoneInputLayout());
        }
    }

    private void setupPhoneInputListener() {
        etPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePhoneNumber();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String digits = s.toString().replaceAll("[^0-9]", "");
                if (!digits.equals(s.toString())) {
                    etPhoneNumber.setText(digits);
                    etPhoneNumber.setSelection(digits.length());
                }
            }
        });
    }

    private void setupOtpInputListeners() {
        TextInputEditText[] otpFields = {etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6};
        
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            final TextInputEditText currentField = otpFields[i];
            
            currentField.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    hideOtpError();
                    
                    if (s.length() == 1 && index < otpFields.length - 1) {
                        otpFields[index + 1].requestFocus();
                    } else if (s.length() == 0 && index > 0) {
                        otpFields[index - 1].requestFocus();
                    }
                    
                    validateOtp();
                    
                    if (getEnteredOtp().length() == 6) {
                        verifyOtp();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            currentField.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    currentField.getText().toString().isEmpty() && 
                    index > 0) {
                    otpFields[index - 1].requestFocus();
                    return true;
                }
                return false;
            });
        }
    }

    private void validatePhoneNumber() {
        String phone = etPhoneNumber.getText().toString().trim();
        boolean isValidLength = phone.length() == 10;
        boolean isValidFormat = phone.matches("^[6-9]\\d{9}$");
        
        boolean isValid = isValidLength && isValidFormat;
        
        // Enable button if phone number is valid - allow verification even for same number
        btnSendOtp.setEnabled(isValid);
        
        if (phone.length() > 0 && !isValid) {
            if (!isValidLength) {
                tilPhoneNumber.setError("Enter a valid 10-digit phone number");
            } else if (!isValidFormat) {
                tilPhoneNumber.setError("Phone number must start with 6, 7, 8, or 9");
            }
        } else {
            tilPhoneNumber.setError(null);
        }
        
        if (isValid) {
            tilPhoneNumber.setHelperText("Tap 'Send Verification Code' to receive OTP");
        } else {
            tilPhoneNumber.setHelperText("Enter your 10-digit mobile number");
        }
    }

    private void validateOtp() {
        String otp = getEnteredOtp();
        btnVerifyOtp.setEnabled(otp.length() == 6);
    }

    private String getEnteredOtp() {
        return (etOtp1.getText() != null ? etOtp1.getText().toString() : "") +
               (etOtp2.getText() != null ? etOtp2.getText().toString() : "") +
               (etOtp3.getText() != null ? etOtp3.getText().toString() : "") +
               (etOtp4.getText() != null ? etOtp4.getText().toString() : "") +
               (etOtp5.getText() != null ? etOtp5.getText().toString() : "") +
               (etOtp6.getText() != null ? etOtp6.getText().toString() : "");
    }

    private void clearOtpFields() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void sendVerificationCode() {
        String phone = etPhoneNumber.getText().toString().trim();
        
        if (phone.isEmpty() || phone.length() != 10) {
            tilPhoneNumber.setError("Enter a valid 10-digit phone number");
            return;
        }
        
        if (otpAttempts >= MAX_OTP_ATTEMPTS) {
            showSnackbar("Too many attempts. Please try again later.");
            btnSendOtp.setEnabled(false);
            return;
        }
        
        phoneNumber = countryCode + phone;
        
        // Test mode - simulate OTP sending for development
        if (TEST_MODE) {
            Log.d(TAG, "TEST MODE: Simulating OTP send to " + phoneNumber);
            showLoading(true, "Sending verification code...");
            btnSendOtp.setEnabled(false);
            
            // Simulate network delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                showLoading(false, "");
                verificationId = "test_verification_id";
                showOtpVerificationLayout();
                startOtpTimer();
                showSnackbar("Test Mode: Use code " + TEST_OTP + " to verify");
                otpAttempts++;
            }, 1500);
            return;
        }
        
        Log.d(TAG, "Initiating phone verification for: " + phoneNumber);
        showLoading(true, "Sending verification code...");
        btnSendOtp.setEnabled(false);
        
        try {
            PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(phoneAuthCallbacks)
                    .build();
            
            PhoneAuthProvider.verifyPhoneNumber(options);
            
            otpAttempts++;
            Log.d(TAG, "OTP request sent to: " + phoneNumber + " (attempt " + otpAttempts + ")");
        } catch (Exception e) {
            Log.e(TAG, "Error sending verification code", e);
            showLoading(false, "");
            btnSendOtp.setEnabled(true);
            showSnackbar("Failed to send verification code. Please try again.");
        }
    }

    private void resendVerificationCode() {
        if (resendToken == null) {
            showSnackbar("Cannot resend code yet. Please wait.");
            return;
        }
        
        if (otpAttempts >= MAX_OTP_ATTEMPTS) {
            showSnackbar("Too many attempts. Please try again later.");
            btnResendOtp.setEnabled(false);
            return;
        }
        
        showLoading(true, "Resending verification code...");
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(phoneAuthCallbacks)
                .setForceResendingToken(resendToken)
                .build();
        
        PhoneAuthProvider.verifyPhoneNumber(options);
        
        otpAttempts++;
        Log.d(TAG, "Resending OTP to: " + phoneNumber + " (attempt " + otpAttempts + ")");
    }

    private void verifyOtp() {
        String otp = getEnteredOtp();
        if (otp.length() != 6) {
            showOtpError("Please enter complete 6-digit code");
            return;
        }
        
        if (verificationId == null) {
            showOtpError("Verification session expired. Please resend code.");
            return;
        }
        
        // Test mode - verify with test OTP
        if (TEST_MODE) {
            Log.d(TAG, "TEST MODE: Verifying OTP " + otp);
            showLoading(true, "Verifying code...");
            
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                showLoading(false, "");
                if (otp.equals(TEST_OTP)) {
                    Log.d(TAG, "TEST MODE: OTP verified successfully");
                    onVerificationSuccess();
                } else {
                    showOtpError("Invalid OTP. Use " + TEST_OTP + " for testing.");
                    clearOtpFields();
                }
            }, 1000);
            return;
        }
        
        showLoading(true, "Verifying code...");
        
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
            signInWithPhoneAuthCredential(credential);
        } catch (Exception e) {
            showLoading(false, "");
            showOtpError("Invalid OTP format");
            Log.e(TAG, "OTP verification error", e);
        }
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks phoneAuthCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    Log.d(TAG, "onVerificationCompleted - Auto verification");
                    showLoading(false, "");
                    
                    String code = credential.getSmsCode();
                    if (code != null && code.length() == 6) {
                        autoFillOtp(code);
                    }
                    
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    Log.e(TAG, "onVerificationFailed: " + e.getMessage(), e);
                    showLoading(false, "");
                    btnSendOtp.setEnabled(true);
                    
                    String errorMessage;
                    String exMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
                    
                    if (exMsg.contains("quota")) {
                        errorMessage = "Too many verification requests. Please try again later.";
                    } else if (exMsg.contains("invalid") && exMsg.contains("phone")) {
                        errorMessage = "Invalid phone number format.";
                    } else if (exMsg.contains("network")) {
                        errorMessage = "Network error. Please check your connection.";
                    } else if (exMsg.contains("blocked")) {
                        errorMessage = "This phone number has been blocked.";
                    } else if (exMsg.contains("billing")) {
                        errorMessage = "Service temporarily unavailable. Please try again later.";
                    } else if (exMsg.contains("app not authorized")) {
                        errorMessage = "App verification failed. Please restart the app.";
                    } else {
                        errorMessage = "Verification failed: " + (e.getMessage() != null ? e.getMessage() : "Unknown error");
                    }
                    
                    Log.e(TAG, "Verification error message: " + errorMessage);
                    showSnackbar(errorMessage);
                }

                @Override
                public void onCodeSent(@NonNull String verId,
                                       @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    Log.d(TAG, "onCodeSent: " + verId);
                    showLoading(false, "");
                    
                    verificationId = verId;
                    resendToken = token;
                    
                    showOtpVerificationLayout();
                    startOtpTimer();
                    
                    showSnackbar("Verification code sent successfully");
                }

                @Override
                public void onCodeAutoRetrievalTimeOut(@NonNull String verId) {
                    Log.d(TAG, "onCodeAutoRetrievalTimeOut");
                }
            };

    private void autoFillOtp(String code) {
        if (code == null || code.length() != 6) return;
        
        etOtp1.setText(String.valueOf(code.charAt(0)));
        etOtp2.setText(String.valueOf(code.charAt(1)));
        etOtp3.setText(String.valueOf(code.charAt(2)));
        etOtp4.setText(String.valueOf(code.charAt(3)));
        etOtp5.setText(String.valueOf(code.charAt(4)));
        etOtp6.setText(String.valueOf(code.charAt(5)));
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false, "");
                    
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        onVerificationSuccess();
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        
                        String errorMessage = "Verification failed";
                        if (task.getException() != null) {
                            String exMsg = task.getException().getMessage();
                            if (exMsg != null && exMsg.toLowerCase().contains("invalid")) {
                                errorMessage = "Invalid OTP. Please check and try again.";
                            } else if (exMsg != null && exMsg.toLowerCase().contains("expired")) {
                                errorMessage = "OTP expired. Please request a new code.";
                            }
                        }
                        
                        showOtpError(errorMessage);
                        clearOtpFields();
                    }
                });
    }

    private void onVerificationSuccess() {
        String cleanPhone = phoneNumber.replace(countryCode, "");
        sessionManager.updatePhoneVerification(cleanPhone, countryCode, true);
        
        if (isNetworkAvailable()) {
            saveToFirebaseDatabase(cleanPhone, true);
        } else {
            queueOfflineSync(cleanPhone, countryCode, true);
        }
        
        showVerificationSuccessLayout();
        
        Intent resultIntent = new Intent();
        resultIntent.putExtra("verified_phone", cleanPhone);
        resultIntent.putExtra("country_code", countryCode);
        resultIntent.putExtra("is_verified", true);
        setResult(RESULT_OK, resultIntent);
        
        Log.d(TAG, "Phone verification successful: " + cleanPhone);
    }

    private void saveToFirebaseDatabase(String phone, boolean verified) {
        String userId = sessionManager.getUserId();
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null, cannot save to Firebase");
            return;
        }
        
        Map<String, Object> phoneData = new HashMap<>();
        phoneData.put("phone", phone);
        phoneData.put("countryCode", countryCode);
        phoneData.put("phoneVerified", verified);
        phoneData.put("lastUpdated", System.currentTimeMillis());
        
        databaseReference.child("customers")
                .child(userId)
                .child("profile")
                .updateChildren(phoneData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Phone data saved to Firebase"))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save phone data", e);
                    queueOfflineSync(phone, countryCode, verified);
                });
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("phone", phone);
        userData.put("phoneVerified", verified);
        
        databaseReference.child("users")
                .child(userId)
                .updateChildren(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "User phone data updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update user phone", e));
    }

    private void queueOfflineSync(String phone, String code, boolean verified) {
        Data inputData = new Data.Builder()
                .putString("user_id", sessionManager.getUserId())
                .putString("phone", phone)
                .putString("country_code", code)
                .putBoolean("verified", verified)
                .putLong("timestamp", System.currentTimeMillis())
                .build();
        
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        
        OneTimeWorkRequest syncWorkRequest = new OneTimeWorkRequest.Builder(PhoneVerificationSyncWorker.class)
                .setConstraints(constraints)
                .setInputData(inputData)
                .addTag("phone_verification_sync")
                .build();
        
        WorkManager.getInstance(this).enqueue(syncWorkRequest);
        
        Log.d(TAG, "Phone verification sync queued for offline processing");
        showSnackbar("Changes will be synced when online");
    }

    private void showPhoneInputLayout() {
        layoutPhoneInput.setVisibility(View.VISIBLE);
        layoutOtpVerification.setVisibility(View.GONE);
        layoutVerificationSuccess.setVisibility(View.GONE);
        
        clearOtpFields();
        hideOtpError();
        stopOtpTimer();
    }

    private void showOtpVerificationLayout() {
        layoutPhoneInput.setVisibility(View.GONE);
        layoutOtpVerification.setVisibility(View.VISIBLE);
        layoutVerificationSuccess.setVisibility(View.GONE);
        
        String formattedPhone = formatPhoneForDisplay(phoneNumber);
        tvPhoneDisplay.setText("Code sent to " + formattedPhone);
        
        etOtp1.requestFocus();
        clearOtpFields();
        hideOtpError();
    }

    private void showVerificationSuccessLayout() {
        layoutPhoneInput.setVisibility(View.GONE);
        layoutOtpVerification.setVisibility(View.GONE);
        layoutVerificationSuccess.setVisibility(View.VISIBLE);
        
        stopOtpTimer();
    }

    private String formatPhoneForDisplay(String phone) {
        if (phone == null || phone.length() < 10) return phone;
        
        String cleanPhone = phone.replaceAll("[^0-9+]", "");
        
        if (cleanPhone.startsWith("+91") && cleanPhone.length() == 13) {
            return cleanPhone.substring(0, 3) + " " + 
                   cleanPhone.substring(3, 8) + " " + 
                   cleanPhone.substring(8);
        } else if (cleanPhone.length() == 10) {
            return "+91 " + cleanPhone.substring(0, 5) + " " + cleanPhone.substring(5);
        }
        
        return phone;
    }

    private void showOtpError(String message) {
        if (tvOtpError != null) {
            tvOtpError.setText(message);
            tvOtpError.setVisibility(View.VISIBLE);
        } else {
            showSnackbar(message);
        }
    }

    private void hideOtpError() {
        if (tvOtpError != null) {
            tvOtpError.setVisibility(View.GONE);
        }
    }

    private void startOtpTimer() {
        if (isOtpTimerRunning) return;
        
        isOtpTimerRunning = true;
        btnResendOtp.setEnabled(false);
        
        otpTimer = new CountDownTimer(OTP_TIMEOUT_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(String.format("Resend code in %02d:%02d", seconds / 60, seconds % 60));
            }

            @Override
            public void onFinish() {
                isOtpTimerRunning = false;
                tvTimer.setText("Didn't receive code?");
                btnResendOtp.setEnabled(otpAttempts < MAX_OTP_ATTEMPTS);
            }
        };
        
        otpTimer.start();
    }

    private void stopOtpTimer() {
        if (otpTimer != null) {
            otpTimer.cancel();
            isOtpTimerRunning = false;
        }
    }

    private void finishVerification() {
        finish();
    }

    private void showLoading(boolean show, String message) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show && message != null && !message.isEmpty()) {
            tvLoadingText.setText(message);
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("phone_number", phoneNumber);
        outState.putString("verification_id", verificationId);
        outState.putInt("otp_attempts", otpAttempts);
        outState.putInt("current_layout", getCurrentLayoutState());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        phoneNumber = savedInstanceState.getString("phone_number");
        verificationId = savedInstanceState.getString("verification_id");
        otpAttempts = savedInstanceState.getInt("otp_attempts", 0);
        
        int layoutState = savedInstanceState.getInt("current_layout", 0);
        restoreLayoutState(layoutState);
    }

    private int getCurrentLayoutState() {
        if (layoutVerificationSuccess.getVisibility() == View.VISIBLE) {
            return 2;
        } else if (layoutOtpVerification.getVisibility() == View.VISIBLE) {
            return 1;
        }
        return 0;
    }

    private void restoreLayoutState(int state) {
        switch (state) {
            case 1:
                showOtpVerificationLayout();
                break;
            case 2:
                showVerificationSuccessLayout();
                break;
            default:
                showPhoneInputLayout();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopOtpTimer();
    }

    @Override
    public void onBackPressed() {
        if (layoutVerificationSuccess.getVisibility() == View.VISIBLE) {
            finishVerification();
        } else if (layoutOtpVerification.getVisibility() == View.VISIBLE) {
            new AlertDialog.Builder(this)
                    .setTitle("Cancel Verification?")
                    .setMessage("Are you sure you want to cancel phone verification?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        setResult(RESULT_CANCELED);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
