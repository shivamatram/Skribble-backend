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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodvan.R;
import com.example.foodvan.activities.customer.CustomerHomeActivity;
import com.example.foodvan.activities.vendor.VendorDashboardActivity;
import com.example.foodvan.models.User;
import com.example.foodvan.utils.FirebaseManager;
import com.example.foodvan.utils.GoogleSignInManager;
import com.example.foodvan.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * LoginActivity - Handles user authentication for both customers and vendors
 * Features: Email/Password login, role-based navigation, session management
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogleLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    
    // Animation elements
    private ImageView logoImage, floatingElement1, floatingElement2, floatingElement3, floatingElement4;
    private TextView welcomeTitle, subtitleText;
    private AnimatorSet masterAnimatorSet;
    
    private FirebaseAuth mAuth;
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;
    private GoogleSignInManager googleSignInManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initializeViews();
        initializeFirebase();
        setupClickListeners();
        startLoginAnimations();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogleLogin = findViewById(R.id.btn_google_login);
        tvSignUp = findViewById(R.id.tv_sign_up);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progress_bar);
        
        // Initialize animation elements
        logoImage = findViewById(R.id.logo_image);
        welcomeTitle = findViewById(R.id.welcome_title);
        subtitleText = findViewById(R.id.subtitle_text);
        floatingElement1 = findViewById(R.id.floating_element_1);
        floatingElement2 = findViewById(R.id.floating_element_2);
        floatingElement3 = findViewById(R.id.floating_element_3);
        floatingElement4 = findViewById(R.id.floating_element_4);
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        firebaseManager = new FirebaseManager();
        googleSignInManager = new GoogleSignInManager(this);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnGoogleLogin.setOnClickListener(v -> loginWithGoogle());
        tvSignUp.setOnClickListener(v -> navigateToSignUp());
        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            showProgress(true);
            
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        showProgress(false);
                        
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                checkUserRoleAndNavigate(user.getUid());
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, 
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }
    }

    private void loginWithGoogle() {
        showProgress(true);
        
        googleSignInManager.signIn(this, new GoogleSignInManager.GoogleSignInCallback() {
            @Override
            public void onSignInSuccess(FirebaseUser user, User userProfile) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, 
                    "Welcome " + userProfile.getName() + "!", 
                    Toast.LENGTH_SHORT).show();
                
                // Navigate based on user role
                navigateToUserDashboard(userProfile);
            }
            
            @Override
            public void onSignInFailure(String error) {
                showProgress(false);
                Toast.makeText(LoginActivity.this, 
                    "Google Sign-In failed: " + error, 
                    Toast.LENGTH_LONG).show();
            }
            
            @Override
            public void onSignInCancelled() {
                showProgress(false);
                Toast.makeText(LoginActivity.this, 
                    "Sign-in cancelled", 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserRoleAndNavigate(String userId) {
        firebaseManager.getUserById(userId, new FirebaseManager.OnUserFetchListener() {
            @Override
            public void onSuccess(User user) {
                sessionManager.createSession(user);
                
                navigateToUserDashboard(user);
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, "Error fetching user data: " + error, 
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInput(String email, String password) {
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

        return true;
    }

    private void showProgress(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnGoogleLogin.setEnabled(!show);
    }

    private void navigateToUserDashboard(User user) {
        Intent intent;
        if ("vendor".equals(user.getRole())) {
            intent = new Intent(LoginActivity.this, VendorDashboardActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, CustomerHomeActivity.class);
        }
        startActivity(intent);
        finish();
    }
    
    private void navigateToSignUp() {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle Google Sign-In result
        if (requestCode == GoogleSignInManager.RC_SIGN_IN) {
            googleSignInManager.handleSignInResult(data);
        }
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();
        
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email to reset password");
            etEmail.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, 
                        "Password reset email sent to " + email, 
                        Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, 
                        "Error: " + task.getException().getMessage(), 
                        Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && sessionManager.isLoggedIn()) {
            User user = sessionManager.getUserDetails();
            Intent intent;
            if ("vendor".equals(user.getRole())) {
                intent = new Intent(this, VendorDashboardActivity.class);
            } else {
                intent = new Intent(this, CustomerHomeActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }

    private void startLoginAnimations() {
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
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoImage, "scaleX", 0f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoImage, "scaleY", 0f, 1.2f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(logoImage, "rotation", 0f, 360f);
            
            scaleX.setDuration(1000);
            scaleY.setDuration(1000);
            rotation.setDuration(2000);
            
            scaleX.setInterpolator(new BounceInterpolator());
            scaleY.setInterpolator(new BounceInterpolator());
            rotation.setInterpolator(new AccelerateDecelerateInterpolator());
            
            logoSet.playTogether(scaleX, scaleY, rotation);
        }
        
        return logoSet;
    }

    private AnimatorSet createTitleAnimations() {
        AnimatorSet titleSet = new AnimatorSet();
        
        if (welcomeTitle != null && subtitleText != null) {
            // Title slide up animation
            ObjectAnimator titleTranslation = ObjectAnimator.ofFloat(welcomeTitle, "translationY", 100f, 0f);
            ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(welcomeTitle, "alpha", 0f, 1f);
            
            // Subtitle slide up animation
            ObjectAnimator subtitleTranslation = ObjectAnimator.ofFloat(subtitleText, "translationY", 100f, 0f);
            ObjectAnimator subtitleAlpha = ObjectAnimator.ofFloat(subtitleText, "alpha", 0f, 1f);
            
            titleTranslation.setDuration(800);
            titleAlpha.setDuration(800);
            subtitleTranslation.setDuration(800);
            subtitleAlpha.setDuration(800);
            
            titleTranslation.setStartDelay(300);
            titleAlpha.setStartDelay(300);
            subtitleTranslation.setStartDelay(500);
            subtitleAlpha.setStartDelay(500);
            
            titleTranslation.setInterpolator(new OvershootInterpolator(0.5f));
            titleAlpha.setInterpolator(new AccelerateDecelerateInterpolator());
            subtitleTranslation.setInterpolator(new OvershootInterpolator(0.5f));
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
                ObjectAnimator alpha = ObjectAnimator.ofFloat(element, "alpha", 0.3f, 0.8f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(element, "scaleX", 0.8f, 1.2f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(element, "scaleY", 0.8f, 1.2f);
                ObjectAnimator rotation = ObjectAnimator.ofFloat(element, "rotation", 0f, 360f);
                ObjectAnimator translationY = ObjectAnimator.ofFloat(element, "translationY", -20f, 20f);
                
                alpha.setDuration(2000);
                scaleX.setDuration(3000);
                scaleY.setDuration(3000);
                rotation.setDuration(4000);
                translationY.setDuration(2500);
                
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
                elementSet.setStartDelay(800 + (i * 200));
                
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
