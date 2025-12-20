package com.example.foodvan.activities.auth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodvan.R;
import com.example.foodvan.models.User;
import com.example.foodvan.utils.FirebaseManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SignupActivity - Handles user registration for both customers and vendors
 * Features: Email/Password signup, role selection, profile creation
 */
public class SignupActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etPassword, etConfirmPassword;
    private RadioGroup rgUserType;
    private RadioButton rbCustomer, rbVendor;
    private Button btnSignUp;
    private TextView tvLogin;
    private ProgressBar progressBar;
    
    // Animation elements
    private ImageView logoImage, floatingElement1, floatingElement2, floatingElement3, floatingElement4;
    private TextView signupTitle, signupSubtitle;
    private AnimatorSet masterAnimatorSet;
    
    private FirebaseAuth mAuth;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        initializeViews();
        initializeFirebase();
        setupClickListeners();
        startSignupAnimations();
    }

    private void initializeViews() {
        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        rgUserType = findViewById(R.id.rg_user_type);
        rbCustomer = findViewById(R.id.rb_customer);
        rbVendor = findViewById(R.id.rb_vendor);
        btnSignUp = findViewById(R.id.btn_sign_up);
        tvLogin = findViewById(R.id.tv_login);
        progressBar = findViewById(R.id.progress_bar);
        
        // Initialize animation elements
        logoImage = findViewById(R.id.logo_image);
        signupTitle = findViewById(R.id.signup_title);
        signupSubtitle = findViewById(R.id.signup_subtitle);
        floatingElement1 = findViewById(R.id.floating_element_1);
        floatingElement2 = findViewById(R.id.floating_element_2);
        floatingElement3 = findViewById(R.id.floating_element_3);
        floatingElement4 = findViewById(R.id.floating_element_4);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        firebaseManager = new FirebaseManager();
    }

    private void setupClickListeners() {
        btnSignUp.setOnClickListener(v -> registerUser());
        tvLogin.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        int selectedRoleId = rgUserType.getCheckedRadioButtonId();
        String role = selectedRoleId == R.id.rb_vendor ? "vendor" : "customer";

        if (validateInput(name, email, phone, password, confirmPassword, selectedRoleId)) {
            showProgress(true);
            
            // Check if this is a demo registration (for testing without Firebase)
            if (email.toLowerCase().contains("demo") || email.toLowerCase().contains("test")) {
                // Demo mode - simulate successful registration
                showProgress(false);
                Toast.makeText(this, 
                    "Demo registration successful! You can now login with these credentials.", 
                    Toast.LENGTH_LONG).show();
                navigateToLogin();
                return;
            }
            
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                createUserProfile(firebaseUser.getUid(), name, email, phone, role);
                            }
                        } else {
                            showProgress(false);
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Registration failed";
                            
                            // Handle specific Firebase configuration errors
                            if (errorMessage.contains("API key not valid")) {
                                Toast.makeText(SignupActivity.this, 
                                    "Firebase configuration error. Please check google-services.json file.", 
                                    Toast.LENGTH_LONG).show();
                            } else if (errorMessage.contains("reCAPTCHA")) {
                                Toast.makeText(SignupActivity.this, 
                                    "Security verification failed. Please try again.", 
                                    Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(SignupActivity.this, 
                                    "Registration failed: " + errorMessage,
                                    Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
        }
    }

    private void createUserProfile(String userId, String name, String email, String phone, String role) {
        User user = new User();
        user.setUserId(userId);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole(role);
        user.setCreatedAt(System.currentTimeMillis());
        user.setActive(true);

        firebaseManager.saveUser(user, new FirebaseManager.OnUserSaveListener() {
            @Override
            public void onSuccess() {
                showProgress(false);
                Toast.makeText(SignupActivity.this, 
                    "Registration successful! Please login to continue.", 
                    Toast.LENGTH_LONG).show();
                
                // Send verification email
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.sendEmailVerification();
                }
                
                navigateToLogin();
            }

            @Override
            public void onFailure(String error) {
                showProgress(false);
                Toast.makeText(SignupActivity.this, 
                    "Error creating profile: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String name, String email, String phone, String password, 
                                String confirmPassword, int selectedRoleId) {
        
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (phone.length() < 10) {
            etPhone.setError("Please enter a valid phone number");
            etPhone.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select user type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignUp.setEnabled(!show);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startSignupAnimations() {
        masterAnimatorSet = new AnimatorSet();
        
        // Logo bounce animation
        AnimatorSet logoAnimations = createLogoAnimations();
        
        // Title animations
        AnimatorSet titleAnimations = createTitleAnimations();
        
        // Floating elements animations
        AnimatorSet floatingAnimations = createFloatingElementsAnimations();
        
        // Start all animations together
        masterAnimatorSet.playTogether(logoAnimations, titleAnimations, floatingAnimations);
        masterAnimatorSet.start();
    }

    private AnimatorSet createLogoAnimations() {
        AnimatorSet logoSet = new AnimatorSet();
        
        if (logoImage != null) {
            // Scale animation with bounce
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImage, "scaleX", 0f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImage, "scaleY", 0f, 1.1f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(logoImage, "rotation", 0f, 360f);
            
            scaleX.setDuration(900);
            scaleY.setDuration(900);
            rotation.setDuration(1800);
            
            scaleX.setInterpolator(new BounceInterpolator());
            scaleY.setInterpolator(new BounceInterpolator());
            rotation.setInterpolator(new AccelerateDecelerateInterpolator());
            
            logoSet.playTogether(scaleX, scaleY, rotation);
        }
        
        return logoSet;
    }

    private AnimatorSet createTitleAnimations() {
        AnimatorSet titleSet = new AnimatorSet();
        
        if (signupTitle != null && signupSubtitle != null) {
            // Title slide up animation
            ObjectAnimator titleTranslation = ObjectAnimator.ofFloat(signupTitle, "translationY", 80f, 0f);
            ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(signupTitle, "alpha", 0f, 1f);
            
            // Subtitle slide up animation
            ObjectAnimator subtitleTranslation = ObjectAnimator.ofFloat(signupSubtitle, "translationY", 80f, 0f);
            ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(signupSubtitle, "alpha", 0f, 1f);
            
            titleTranslation.setDuration(700);
            titleAlpha.setDuration(700);
            subtitleTranslation.setDuration(700);
            subtitleAlpha.setDuration(700);
            
            titleTranslation.setStartDelay(200);
            titleAlpha.setStartDelay(200);
            subtitleTranslation.setStartDelay(400);
            subtitleAlpha.setStartDelay(400);
            
            titleTranslation.setInterpolator(new OvershootInterpolator(0.4f));
            titleAlpha.setInterpolator(new AccelerateDecelerateInterpolator());
            subtitleTranslation.setInterpolator(new OvershootInterpolator(0.4f));
            subtitleAlpha.setInterpolator(new AccelerateDecelerateInterpolator());
            
            titleSet.playTogether(titleTranslation, titleAlpha, subtitleTranslation, subtitleAlpha);
        }
        
        return titleSet;
    }

    private AnimatorSet createFloatingElementsAnimations() {
        AnimatorSet floatingSet = new AnimatorSet();
        
        ImageView[] floatingElements = {floatingElement1, floatingElement2, floatingElement3, floatingElement4};
        
        for (int i = 0; i < floatingElements.length; i++) {
            ImageView element = floatingElements[i];
            if (element != null) {
                // Floating animation with rotation and translation
                ObjectAnimator alpha = ObjectAnimator.ofFloat(element, "alpha", 0.2f, 0.7f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(element, "scaleX", 0.7f, 1.1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(element, "scaleY", 0.7f, 1.1f);
                ObjectAnimator rotation = ObjectAnimator.ofFloat(element, "rotation", 0f, 360f);
                ObjectAnimator translationY = ObjectAnimator.ofFloat(element, "translationY", -15f, 15f);
                
                alpha.setDuration(1800);
                scaleX.setDuration(2800);
                scaleY.setDuration(2800);
                rotation.setDuration(3500);
                translationY.setDuration(2200);
                
                alpha.setRepeatCount(ValueAnimator.INFINITE);
                alpha.setRepeatMode(ValueAnimator.REVERSE);
                scaleX.setRepeatCount(ValueAnimator.INFINITE);
                scaleY.setRepeatCount(ValueAnimator.INFINITE);
                scaleX.setRepeatMode(ValueAnimator.REVERSE);
                scaleY.setRepeatMode(ValueAnimator.REVERSE);
                rotation.setRepeatCount(ValueAnimator.INFINITE);
                translationY.setRepeatCount(ValueAnimator.INFINITE);
                translationY.setRepeatMode(ValueAnimator.REVERSE);
                
                scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
                scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
                rotation.setInterpolator(new AccelerateDecelerateInterpolator());
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
                
                AnimatorSet elementSet = new AnimatorSet();
                elementSet.playTogether(alpha, scaleX, scaleY, rotation, translationY);
                elementSet.setStartDelay(600 + (i * 150));
                
                floatingSet.playTogether(elementSet);
            }
        }
        
        return floatingSet;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (masterAnimatorSet != null) {
            masterAnimatorSet.cancel();
        }
    }
}
