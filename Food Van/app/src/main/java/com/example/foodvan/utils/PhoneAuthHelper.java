package com.example.foodvan.utils;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * PhoneAuthHelper - Utility class to simplify Firebase Phone Authentication
 * Provides easy-to-use methods for phone verification across the app
 */
public class PhoneAuthHelper {
    
    private static final String TAG = "PhoneAuthHelper";
    private static final long TIMEOUT_SECONDS = 60L;
    
    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    
    public interface PhoneAuthCallback {
        void onVerificationCompleted(PhoneAuthCredential credential);
        void onVerificationFailed(String error);
        void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token);
    }
    
    public PhoneAuthHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
    }
    
    /**
     * Start phone number verification
     * @param activity The activity context
     * @param phoneNumber Phone number in international format (+91XXXXXXXXXX)
     * @param callback Callback for verification events
     */
    public void startPhoneVerification(Activity activity, String phoneNumber, PhoneAuthCallback callback) {
        Log.d(TAG, "Starting phone verification for: " + phoneNumber);
        
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d(TAG, "Phone verification completed automatically");
                callback.onVerificationCompleted(credential);
            }
            
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.e(TAG, "Phone verification failed: " + e.getMessage());
                callback.onVerificationFailed(e.getMessage());
            }
            
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                Log.d(TAG, "Verification code sent successfully");
                callback.onCodeSent(verificationId, token);
            }
        };
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    
    /**
     * Resend verification code
     * @param activity The activity context
     * @param phoneNumber Phone number in international format
     * @param resendToken Token received from previous verification attempt
     * @param callback Callback for verification events
     */
    public void resendVerificationCode(Activity activity, String phoneNumber, 
                                     PhoneAuthProvider.ForceResendingToken resendToken, 
                                     PhoneAuthCallback callback) {
        Log.d(TAG, "Resending verification code for: " + phoneNumber);
        
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                callback.onVerificationCompleted(credential);
            }
            
            @Override
            public void onVerificationFailed(FirebaseException e) {
                callback.onVerificationFailed(e.getMessage());
            }
            
            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                callback.onCodeSent(verificationId, token);
            }
        };
        
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .setForceResendingToken(resendToken)
                .build();
        
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    
    /**
     * Verify OTP code
     * @param verificationId Verification ID received from onCodeSent
     * @param otpCode OTP code entered by user
     * @param callback Success/failure callback
     */
    public void verifyOtpCode(String verificationId, String otpCode, VerificationCallback callback) {
        Log.d(TAG, "Verifying OTP code");
        
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpCode);
        
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "OTP verification successful");
                        callback.onSuccess();
                    } else {
                        String error = task.getException() != null ? 
                                task.getException().getMessage() : "Verification failed";
                        Log.e(TAG, "OTP verification failed: " + error);
                        callback.onFailure(error);
                    }
                });
    }
    
    /**
     * Format phone number to international format
     * @param phoneNumber 10-digit phone number
     * @param countryCode Country code (default: +91 for India)
     * @return Formatted phone number
     */
    public static String formatPhoneNumber(String phoneNumber, String countryCode) {
        if (phoneNumber.startsWith("+")) {
            return phoneNumber;
        }
        
        if (countryCode == null) {
            countryCode = "+91"; // Default to India
        }
        
        return countryCode + phoneNumber;
    }
    
    /**
     * Format phone number with default country code (+91)
     */
    public static String formatPhoneNumber(String phoneNumber) {
        return formatPhoneNumber(phoneNumber, "+91");
    }
    
    /**
     * Validate phone number format
     * @param phoneNumber Phone number to validate
     * @return true if valid format
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        
        // Remove spaces and special characters
        String cleanNumber = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Check if it's a 10-digit number or international format
        return cleanNumber.matches("\\d{10}") || cleanNumber.matches("\\+\\d{10,15}");
    }
    
    /**
     * Get current authenticated user's phone number
     * @return Phone number if authenticated, null otherwise
     */
    public String getCurrentUserPhoneNumber() {
        if (firebaseAuth.getCurrentUser() != null) {
            return firebaseAuth.getCurrentUser().getPhoneNumber();
        }
        return null;
    }
    
    /**
     * Check if user is authenticated via phone
     * @return true if authenticated with phone
     */
    public boolean isPhoneAuthenticated() {
        return firebaseAuth.getCurrentUser() != null && 
               firebaseAuth.getCurrentUser().getPhoneNumber() != null;
    }
    
    /**
     * Sign out current user
     */
    public void signOut() {
        firebaseAuth.signOut();
        Log.d(TAG, "User signed out");
    }
    
    public interface VerificationCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
