package com.example.foodvan.utils;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import com.example.foodvan.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for managing profile screen animations and interactive elements
 * Provides smooth, professional animations for Material Design 3 components
 */
public class ProfileAnimationHelper {
    
    private static final String TAG = "ProfileAnimationHelper";
    private static final int STAGGER_DELAY = 100; // milliseconds between staggered animations
    private static final int CARD_ANIMATION_DURATION = 600;
    private static final int FAB_ANIMATION_DURATION = 300;
    private static final int PROGRESS_ANIMATION_DURATION = 1500;
    
    private Context context;
    private Handler mainHandler;
    private List<Animator> activeAnimators;
    
    public ProfileAnimationHelper(Context context) {
        this.context = context;
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.activeAnimators = new ArrayList<>();
    }
    
    /**
     * Animate cards with staggered entrance effect
     */
    public void animateCardsStaggered(List<MaterialCardView> cards) {
        if (cards == null || cards.isEmpty()) return;
        
        for (int i = 0; i < cards.size(); i++) {
            final MaterialCardView card = cards.get(i);
            final int delay = i * STAGGER_DELAY;
            
            // Initial state
            card.setAlpha(0f);
            card.setTranslationY(100f);
            card.setScaleX(0.9f);
            card.setScaleY(0.9f);
            
            mainHandler.postDelayed(() -> {
                AnimatorSet animatorSet = new AnimatorSet();
                
                ObjectAnimator fadeIn = ObjectAnimator.ofFloat(card, "alpha", 0f, 1f);
                ObjectAnimator slideUp = ObjectAnimator.ofFloat(card, "translationY", 100f, 0f);
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 0.9f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 0.9f, 1f);
                
                animatorSet.playTogether(fadeIn, slideUp, scaleX, scaleY);
                animatorSet.setDuration(CARD_ANIMATION_DURATION);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                
                activeAnimators.add(animatorSet);
                animatorSet.start();
                
            }, delay);
        }
    }
    
    /**
     * Animate FAB with scale and rotation effect
     */
    public void animateFAB(FloatingActionButton fab, boolean show) {
        if (fab == null) return;
        
        AnimatorSet animatorSet = new AnimatorSet();
        
        if (show) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 0.8f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 0.8f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(fab, "alpha", 0f, 1f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(fab, "rotation", -90f, 0f);
            
            animatorSet.playTogether(scaleX, scaleY, alpha, rotation);
            animatorSet.setInterpolator(new OvershootInterpolator());
            
            fab.setVisibility(View.VISIBLE);
        } else {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 1f, 0.8f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 1f, 0.8f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(fab, "alpha", 1f, 0f);
            ObjectAnimator rotation = ObjectAnimator.ofFloat(fab, "rotation", 0f, 90f);
            
            animatorSet.playTogether(scaleX, scaleY, alpha, rotation);
            animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
            
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fab.setVisibility(View.GONE);
                }
            });
        }
        
        animatorSet.setDuration(FAB_ANIMATION_DURATION);
        activeAnimators.add(animatorSet);
        animatorSet.start();
    }
    
    /**
     * Animate progress bar with smooth fill effect
     */
    public void animateProgressBar(LinearProgressIndicator progressBar, int targetProgress) {
        if (progressBar == null) return;
        
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, targetProgress);
        progressAnimator.setDuration(PROGRESS_ANIMATION_DURATION);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);
        });
        
        activeAnimators.add(progressAnimator);
        progressAnimator.start();
    }
    
    /**
     * Add card press animation with elevation change
     */
    public void addCardPressAnimation(MaterialCardView card) {
        if (card == null) return;
        
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    animateCardElevation(card, true);
                    break;
                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    animateCardElevation(card, false);
                    break;
            }
            return false; // Don't consume the event
        });
    }
    
    /**
     * Animate card elevation on press
     */
    private void animateCardElevation(MaterialCardView card, boolean pressed) {
        float fromElevation = pressed ? 4f : 12f;
        float toElevation = pressed ? 12f : 4f;
        int duration = pressed ? 150 : 200;
        
        ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(card, "cardElevation", fromElevation, toElevation);
        elevationAnimator.setDuration(duration);
        elevationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        activeAnimators.add(elevationAnimator);
        elevationAnimator.start();
    }
    
    /**
     * Animate icon rotation
     */
    public void animateIconRotation(View icon, float fromRotation, float toRotation) {
        if (icon == null) return;
        
        ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(icon, "rotation", fromRotation, toRotation);
        rotationAnimator.setDuration(300);
        rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        activeAnimators.add(rotationAnimator);
        rotationAnimator.start();
    }
    
    /**
     * Create pulse animation for profile image
     */
    public void startProfileImagePulse(View profileImage) {
        if (profileImage == null) return;
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(profileImage, "scaleX", 1f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(profileImage, "scaleY", 1f, 1.05f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(profileImage, "alpha", 1f, 0.9f, 1f);
        
        // Set repeat properties on individual animators
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        alpha.setRepeatCount(ValueAnimator.INFINITE);
        alpha.setRepeatMode(ValueAnimator.REVERSE);
        
        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.playTogether(scaleX, scaleY, alpha);
        pulseSet.setDuration(2000);
        pulseSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        activeAnimators.add(pulseSet);
        pulseSet.start();
    }
    
    /**
     * Animate switch toggle with scale effect
     */
    public void animateSwitchToggle(View switchView) {
        if (switchView == null) return;
        
        AnimatorSet switchSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(switchView, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(switchView, "scaleY", 1f, 1.2f, 1f);
        
        switchSet.playTogether(scaleX, scaleY);
        switchSet.setDuration(250);
        switchSet.setInterpolator(new OvershootInterpolator());
        
        activeAnimators.add(switchSet);
        switchSet.start();
    }
    
    /**
     * Create shimmer effect for loading states
     */
    public void startShimmerEffect(View view) {
        if (view == null) return;
        
        ObjectAnimator shimmerAnimator = ObjectAnimator.ofFloat(view, "translationX", -view.getWidth(), view.getWidth());
        shimmerAnimator.setDuration(1500);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.RESTART);
        shimmerAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        
        activeAnimators.add(shimmerAnimator);
        shimmerAnimator.start();
    }
    
    /**
     * Stop all active animations
     */
    public void stopAllAnimations() {
        for (Animator animator : activeAnimators) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimators.clear();
    }
    
    /**
     * Clean up resources
     */
    public void cleanup() {
        stopAllAnimations();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
    
    /**
     * Animate view with bounce effect
     */
    public void bounceView(View view) {
        if (view == null) return;
        
        AnimatorSet bounceSet = new AnimatorSet();
        
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 0.9f, 1f);
        
        bounceSet.playTogether(scaleX, scaleY);
        bounceSet.setDuration(400);
        bounceSet.setInterpolator(new OvershootInterpolator());
        
        activeAnimators.add(bounceSet);
        bounceSet.start();
    }
    
    /**
     * Create typing indicator animation
     */
    public void startTypingIndicator(View... dots) {
        if (dots == null || dots.length == 0) return;
        
        for (int i = 0; i < dots.length; i++) {
            final View dot = dots[i];
            final int delay = i * 200;
            
            ObjectAnimator fadeAnimator = ObjectAnimator.ofFloat(dot, "alpha", 0.3f, 1f, 0.3f);
            fadeAnimator.setDuration(600);
            fadeAnimator.setRepeatCount(ValueAnimator.INFINITE);
            fadeAnimator.setStartDelay(delay);
            fadeAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            
            activeAnimators.add(fadeAnimator);
            fadeAnimator.start();
        }
    }
}
