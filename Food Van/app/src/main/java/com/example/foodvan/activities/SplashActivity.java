package com.example.foodvan.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.R;
import com.google.android.material.card.MaterialCardView;

/**
 * Professional Splash Screen with Material Design animations
 * Features: Logo animations, gradient backgrounds, progress indicators, smooth transitions
 */
public class SplashActivity extends AppCompatActivity {

    // UI Components
    private ImageView logoIcon;
    private TextView appTitle;
    private TextView tagline;
    private ProgressBar progressBar;
    private MaterialCardView logoContainer;
    private View backgroundGradient;
    private ImageView[] floatingElements;
    private TextView loadingText;

    // Animation variables
    private AnimatorSet masterAnimatorSet;
    private Handler splashHandler;
    private boolean animationsCompleted = false;
    
    // Timing constants
    private static final int SPLASH_DURATION = 3500; // 3.5 seconds
    private static final int LOGO_ANIMATION_DURATION = 1200;
    private static final int TEXT_ANIMATION_DURATION = 800;
    private static final int PROGRESS_ANIMATION_DURATION = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set full screen and hide status bar for immersive experience
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.activity_splash);
        
        initializeViews();
        setupAnimations();
        startSplashSequence();
    }

    private void initializeViews() {
        logoIcon = findViewById(R.id.logo_icon);
        appTitle = findViewById(R.id.app_title);
        tagline = findViewById(R.id.tagline);
        progressBar = findViewById(R.id.progress_bar);
        logoContainer = findViewById(R.id.logo_container);
        backgroundGradient = findViewById(R.id.background_gradient);
        loadingText = findViewById(R.id.loading_text);
        
        // Initialize floating elements
        floatingElements = new ImageView[]{
            findViewById(R.id.floating_element_1),
            findViewById(R.id.floating_element_2),
            findViewById(R.id.floating_element_3),
            findViewById(R.id.floating_element_4)
        };
        
        // Set initial states
        logoIcon.setScaleX(0f);
        logoIcon.setScaleY(0f);
        logoIcon.setAlpha(0f);
        
        appTitle.setTranslationY(100f);
        appTitle.setAlpha(0f);
        
        tagline.setTranslationY(50f);
        tagline.setAlpha(0f);
        
        progressBar.setAlpha(0f);
        loadingText.setAlpha(0f);
        
        logoContainer.setScaleX(0.8f);
        logoContainer.setScaleY(0.8f);
        logoContainer.setAlpha(0f);
        
        // Hide floating elements initially
        for (ImageView element : floatingElements) {
            if (element != null) {
                element.setAlpha(0f);
                element.setScaleX(0.5f);
                element.setScaleY(0.5f);
            }
        }
    }

    private void setupAnimations() {
        masterAnimatorSet = new AnimatorSet();
        
        // Logo container entrance animation
        AnimatorSet logoContainerAnim = createLogoContainerAnimation();
        
        // Logo icon bounce animation
        AnimatorSet logoIconAnim = createLogoIconAnimation();
        
        // Text animations
        AnimatorSet textAnimations = createTextAnimations();
        
        // Progress bar animation
        AnimatorSet progressAnimation = createProgressAnimation();
        
        // Floating elements animation
        AnimatorSet floatingAnimation = createFloatingElementsAnimation();
        
        // Background pulse animation
        AnimatorSet backgroundAnimation = createBackgroundAnimation();
        
        // Combine all animations
        masterAnimatorSet.playTogether(
            logoContainerAnim,
            logoIconAnim,
            textAnimations,
            progressAnimation,
            floatingAnimation,
            backgroundAnimation
        );
        
        masterAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationsCompleted = true;
            }
        });
    }

    private AnimatorSet createLogoContainerAnimation() {
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 0.8f, 1.1f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 0.8f, 1.1f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoContainer, "alpha", 0f, 1f);
        ObjectAnimator elevation = ObjectAnimator.ofFloat(logoContainer, "cardElevation", 0f, 16f, 8f);
        
        scaleX.setDuration(LOGO_ANIMATION_DURATION);
        scaleY.setDuration(LOGO_ANIMATION_DURATION);
        alpha.setDuration(600);
        elevation.setDuration(LOGO_ANIMATION_DURATION);
        
        scaleX.setInterpolator(new OvershootInterpolator(1.2f));
        scaleY.setInterpolator(new OvershootInterpolator(1.2f));
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());
        
        set.playTogether(scaleX, scaleY, alpha, elevation);
        set.setStartDelay(200);
        
        return set;
    }

    private AnimatorSet createLogoIconAnimation() {
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoIcon, "scaleX", 0f, 1.3f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoIcon, "scaleY", 0f, 1.3f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoIcon, "alpha", 0f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(logoIcon, "rotation", 0f, 360f);
        
        scaleX.setDuration(LOGO_ANIMATION_DURATION);
        scaleY.setDuration(LOGO_ANIMATION_DURATION);
        alpha.setDuration(800);
        rotation.setDuration(LOGO_ANIMATION_DURATION);
        
        scaleX.setInterpolator(new BounceInterpolator());
        scaleY.setInterpolator(new BounceInterpolator());
        rotation.setInterpolator(new AccelerateDecelerateInterpolator());
        
        set.playTogether(scaleX, scaleY, alpha, rotation);
        set.setStartDelay(500);
        
        return set;
    }

    private AnimatorSet createTextAnimations() {
        AnimatorSet set = new AnimatorSet();
        
        // App title animation
        ObjectAnimator titleTransY = ObjectAnimator.ofFloat(appTitle, "translationY", 100f, -20f, 0f);
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(appTitle, "alpha", 0f, 1f);
        ObjectAnimator titleScale = ObjectAnimator.ofFloat(appTitle, "scaleX", 0.8f, 1.1f, 1.0f);
        
        titleTransY.setDuration(TEXT_ANIMATION_DURATION);
        titleAlpha.setDuration(TEXT_ANIMATION_DURATION);
        titleScale.setDuration(TEXT_ANIMATION_DURATION);
        
        titleTransY.setInterpolator(new OvershootInterpolator(1.1f));
        titleAlpha.setInterpolator(new AccelerateDecelerateInterpolator());
        
        // Tagline animation
        ObjectAnimator taglineTransY = ObjectAnimator.ofFloat(tagline, "translationY", 50f, -10f, 0f);
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f);
        
        taglineTransY.setDuration(TEXT_ANIMATION_DURATION);
        taglineAlpha.setDuration(TEXT_ANIMATION_DURATION);
        taglineTransY.setInterpolator(new OvershootInterpolator(0.8f));
        
        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(titleTransY, titleAlpha, titleScale);
        titleSet.setStartDelay(800);
        
        AnimatorSet taglineSet = new AnimatorSet();
        taglineSet.playTogether(taglineTransY, taglineAlpha);
        taglineSet.setStartDelay(1000);
        
        set.playTogether(titleSet, taglineSet);
        
        return set;
    }

    private AnimatorSet createProgressAnimation() {
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
        ObjectAnimator loadingAlpha = ObjectAnimator.ofFloat(loadingText, "alpha", 0f, 1f);
        
        progressAlpha.setDuration(500);
        loadingAlpha.setDuration(500);
        
        // Animate progress bar progress
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(PROGRESS_ANIMATION_DURATION);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);
        });
        
        AnimatorSet alphaSet = new AnimatorSet();
        alphaSet.playTogether(progressAlpha, loadingAlpha);
        alphaSet.setStartDelay(1200);
        
        progressAnimator.setStartDelay(1400);
        
        set.playTogether(alphaSet, progressAnimator);
        
        return set;
    }

    private AnimatorSet createFloatingElementsAnimation() {
        AnimatorSet set = new AnimatorSet();
        
        for (int i = 0; i < floatingElements.length; i++) {
            ImageView element = floatingElements[i];
            if (element != null) {
                ObjectAnimator alpha = ObjectAnimator.ofFloat(element, "alpha", 0f, 0.6f, 0.3f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(element, "scaleX", 0.5f, 1.2f, 1.0f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(element, "scaleY", 0.5f, 1.2f, 1.0f);
                ObjectAnimator rotation = ObjectAnimator.ofFloat(element, "rotation", 0f, 360f);
                ObjectAnimator translationY = ObjectAnimator.ofFloat(element, "translationY", 0f, -30f, 0f);
                
                alpha.setDuration(2000);
                scaleX.setDuration(1500);
                scaleY.setDuration(1500);
                rotation.setDuration(3000);
                translationY.setDuration(2500);
                
                alpha.setRepeatCount(ValueAnimator.INFINITE);
                alpha.setRepeatMode(ValueAnimator.REVERSE);
                rotation.setRepeatCount(ValueAnimator.INFINITE);
                translationY.setRepeatCount(ValueAnimator.INFINITE);
                translationY.setRepeatMode(ValueAnimator.REVERSE);
                
                scaleX.setInterpolator(new OvershootInterpolator(0.5f));
                scaleY.setInterpolator(new OvershootInterpolator(0.5f));
                rotation.setInterpolator(new AccelerateDecelerateInterpolator());
                translationY.setInterpolator(new AccelerateDecelerateInterpolator());
                
                AnimatorSet elementSet = new AnimatorSet();
                elementSet.playTogether(alpha, scaleX, scaleY, rotation, translationY);
                elementSet.setStartDelay(600 + (i * 200));
                
                set.playTogether(elementSet);
            }
        }
        
        return set;
    }

    private AnimatorSet createBackgroundAnimation() {
        AnimatorSet set = new AnimatorSet();
        
        ObjectAnimator backgroundAlpha = ObjectAnimator.ofFloat(backgroundGradient, "alpha", 0.8f, 1.0f, 0.9f);
        backgroundAlpha.setDuration(3000);
        backgroundAlpha.setRepeatCount(ValueAnimator.INFINITE);
        backgroundAlpha.setRepeatMode(ValueAnimator.REVERSE);
        backgroundAlpha.setInterpolator(new AccelerateDecelerateInterpolator());
        
        set.play(backgroundAlpha);
        
        return set;
    }

    private void startSplashSequence() {
        // Start animations
        masterAnimatorSet.start();
        
        // Setup splash timer
        splashHandler = new Handler(Looper.getMainLooper());
        splashHandler.postDelayed(this::navigateToLoginActivity, SPLASH_DURATION);
    }

    private void navigateToLoginActivity() {
        if (isFinishing()) return;
        
        // Create smooth transition animation
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(findViewById(R.id.splash_root), "alpha", 1f, 0f);
        fadeOut.setDuration(500);
        fadeOut.setInterpolator(new AccelerateDecelerateInterpolator());
        
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Navigate to LoginActivity
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                
                // Custom transition animation
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                
                // Finish splash activity
                finish();
            }
        });
        
        fadeOut.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up animations and handlers to prevent memory leaks
        if (masterAnimatorSet != null) {
            masterAnimatorSet.cancel();
            masterAnimatorSet.removeAllListeners();
        }
        
        if (splashHandler != null) {
            splashHandler.removeCallbacksAndMessages(null);
        }
        
        // Clear references
        logoIcon = null;
        appTitle = null;
        tagline = null;
        progressBar = null;
        logoContainer = null;
        backgroundGradient = null;
        floatingElements = null;
        loadingText = null;
    }

    @Override
    public void onBackPressed() {
        // Disable back button during splash screen
        // This prevents users from accidentally exiting during the splash
    }
}
