package com.example.foodvan.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodvan.R;
import com.example.foodvan.activities.customer.CustomerHomeActivity;
import com.example.foodvan.activities.vendor.VendorDashboardActivity;
import com.example.foodvan.models.User;
import com.example.foodvan.utils.FirebaseManager;
import com.example.foodvan.utils.SessionManager;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * OtpVerificationActivity - Handles OTP verification for phone number authentication
 * Features: SMS OTP verification, resend OTP, timer countdown
 */
public class OtpVerificationActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp, btnResendOtp;
    private TextView tvPhoneNumber, tvTimer;
    private ProgressBar progressBar;
    
    private String phoneNumber;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer countDownTimer;
    
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);
        
        initializeViews();
        initializeFirebase();
        getIntentData();
        setupClickListeners();
        startTimer();
    }

    private void initializeViews() {
        etOtp = findViewById(R.id.et_otp);
        btnVerifyOtp = findViewById(R.id.btn_verify_otp);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvTimer = findViewById(R.id.tv_timer);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        firebaseManager = new FirebaseManager();
    }

    private void getIntentData() {
        phoneNumber = getIntent().getStringExtra("phone_number");
        verificationId = getIntent().getStringExtra("verification_id");
        
        if (phoneNumber != null) {
            tvPhoneNumber.setText("Enter OTP sent to " + phoneNumber);
        }
    }

    private void setupClickListeners() {
        btnVerifyOtp.setOnClickListener(v -> verifyOtp());
        btnResendOtp.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();
        
        if (TextUtils.isEmpty(otp)) {
            etOtp.setError("Enter OTP");
            etOtp.requestFocus();
            return;
        }
        
        if (otp.length() != 6) {
            etOtp.setError("Enter valid 6-digit OTP");
            etOtp.requestFocus();
            return;
        }

        showProgress(true);
        
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                showProgress(false);
                
                if (task.isSuccessful()) {
                    // OTP verification successful
                    String userId = mAuth.getCurrentUser().getUid();
                    checkUserExistsAndNavigate(userId);
                } else {
                    Toast.makeText(OtpVerificationActivity.this, 
                        "Verification failed: " + task.getException().getMessage(),
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void checkUserExistsAndNavigate(String userId) {
        firebaseManager.getUserById(userId, new FirebaseManager.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                sessionManager.createSession(user);
                
                Intent intent;
                if ("vendor".equals(user.getRole())) {
                    intent = new Intent(OtpVerificationActivity.this, VendorDashboardActivity.class);
                } else {
                    intent = new Intent(OtpVerificationActivity.this, CustomerHomeActivity.class);
                }
                
                startActivity(intent);
                finishAffinity();
            }

            @Override
            public void onFailure(String error) {
                // User doesn't exist, create new user profile
                createNewUserProfile(userId);
            }
        });
    }

    private void createNewUserProfile(String userId) {
        User user = new User();
        user.setUserId(userId);
        user.setPhone(phoneNumber);
        user.setRole("customer"); // Default role
        user.setCreatedAt(System.currentTimeMillis());
        user.setActive(true);

        firebaseManager.saveUser(user, new FirebaseManager.OnUserSaveListener() {
            @Override
            public void onSuccess() {
                sessionManager.createSession(user);
                Intent intent = new Intent(OtpVerificationActivity.this, CustomerHomeActivity.class);
                startActivity(intent);
                finishAffinity();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(OtpVerificationActivity.this, 
                    "Error creating profile: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resendOtp() {
        showProgress(true);
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                    showProgress(false);
                    signInWithPhoneAuthCredential(credential);
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    showProgress(false);
                    Toast.makeText(OtpVerificationActivity.this, 
                        "Verification failed: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCodeSent(@NonNull String verificationId, 
                                     @NonNull PhoneAuthProvider.ForceResendingToken token) {
                    showProgress(false);
                    OtpVerificationActivity.this.verificationId = verificationId;
                    resendToken = token;
                    
                    Toast.makeText(OtpVerificationActivity.this, 
                        "OTP sent successfully", Toast.LENGTH_SHORT).show();
                    
                    startTimer();
                }
            })
            .setForceResendingToken(resendToken)
            .build();
            
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void startTimer() {
        btnResendOtp.setEnabled(false);
        
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText("Resend OTP in " + seconds + "s");
            }

            @Override
            public void onFinish() {
                tvTimer.setText("");
                btnResendOtp.setEnabled(true);
            }
        }.start();
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerifyOtp.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
