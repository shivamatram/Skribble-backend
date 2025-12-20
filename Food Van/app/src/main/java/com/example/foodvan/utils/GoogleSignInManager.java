package com.example.foodvan.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foodvan.R;
import com.example.foodvan.models.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * GoogleSignInManager - Handles Google Sign-In authentication with Firebase
 * Provides complete Google authentication flow with user profile management
 */
public class GoogleSignInManager {
    
    private static final String TAG = "GoogleSignInManager";
    public static final int RC_SIGN_IN = 9001;
    
    private Context context;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseManager firebaseManager;
    private SessionManager sessionManager;
    
    // Callback interface for authentication results
    public interface GoogleSignInCallback {
        void onSignInSuccess(FirebaseUser user, User userProfile);
        void onSignInFailure(String error);
        void onSignInCancelled();
    }
    
    private GoogleSignInCallback callback;
    
    public GoogleSignInManager(Context context) {
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
        this.firebaseManager = new FirebaseManager();
        this.sessionManager = new SessionManager(context);
        
        initializeGoogleSignIn();
    }
    
    /**
     * Initialize Google Sign-In configuration
     */
    private void initializeGoogleSignIn() {
        try {
            // Configure Google Sign-In options
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .requestProfile()
                    .build();
            
            mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
            Log.d(TAG, "Google Sign-In initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Google Sign-In: " + e.getMessage());
        }
    }
    
    /**
     * Start Google Sign-In flow
     */
    public void signIn(Activity activity, GoogleSignInCallback callback) {
        this.callback = callback;
        
        if (mGoogleSignInClient == null) {
            if (callback != null) {
                callback.onSignInFailure("Google Sign-In not properly initialized");
            }
            return;
        }
        
        try {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
            Log.d(TAG, "Google Sign-In intent started");
            
        } catch (Exception e) {
            Log.e(TAG, "Error starting Google Sign-In: " + e.getMessage());
            if (callback != null) {
                callback.onSignInFailure("Failed to start Google Sign-In: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handle Google Sign-In result from activity
     */
    public void handleSignInResult(Intent data) {
        try {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            GoogleSignInAccount account = task.getResult(ApiException.class);
            
            if (account != null) {
                Log.d(TAG, "Google Sign-In successful, authenticating with Firebase");
                firebaseAuthWithGoogle(account);
            } else {
                Log.w(TAG, "Google Sign-In account is null");
                if (callback != null) {
                    callback.onSignInFailure("Failed to get Google account information");
                }
            }
            
        } catch (ApiException e) {
            Log.e(TAG, "Google Sign-In failed with code: " + e.getStatusCode());
            
            String errorMessage;
            switch (e.getStatusCode()) {
                case 12501: // User cancelled
                    if (callback != null) {
                        callback.onSignInCancelled();
                    }
                    return;
                case 7: // Network error
                    errorMessage = "Network error. Please check your internet connection.";
                    break;
                case 10: // Developer error
                    errorMessage = "Configuration error. Please check Firebase setup.";
                    break;
                default:
                    errorMessage = "Google Sign-In failed. Please try again.";
                    break;
            }
            
            if (callback != null) {
                callback.onSignInFailure(errorMessage);
            }
        }
    }
    
    /**
     * Authenticate with Firebase using Google credentials
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener((Activity) context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            
                            if (firebaseUser != null) {
                                // Create or update user profile
                                createUserProfile(firebaseUser, acct);
                            }
                            
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Firebase authentication failed";
                            
                            if (callback != null) {
                                callback.onSignInFailure("Authentication failed: " + errorMessage);
                            }
                        }
                    }
                });
    }
    
    /**
     * Create or update user profile in Firebase Database
     */
    private void createUserProfile(FirebaseUser firebaseUser, GoogleSignInAccount googleAccount) {
        User user = new User();
        user.setUserId(firebaseUser.getUid());
        user.setName(googleAccount.getDisplayName() != null ? googleAccount.getDisplayName() : "");
        user.setEmail(googleAccount.getEmail() != null ? googleAccount.getEmail() : "");
        user.setPhone(""); // Phone not available from Google
        user.setRole("customer"); // Default role
        user.setProfileImageUrl(googleAccount.getPhotoUrl() != null ? googleAccount.getPhotoUrl().toString() : "");
        
        // Set timestamps
        long currentTime = System.currentTimeMillis();
        user.setCreatedAt(currentTime);
        user.setLastLoginAt(currentTime);
        user.setActive(true);
        
        // Save to Firebase Database
        firebaseManager.saveUser(user, new FirebaseManager.OnUserSaveListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "User profile saved successfully");
                
                // Save to session
                sessionManager.createSession(user);
                
                if (callback != null) {
                    callback.onSignInSuccess(firebaseUser, user);
                }
            }
            
            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to save user profile: " + error);
                
                // Still proceed with login even if profile save fails
                sessionManager.createSession(user);
                
                if (callback != null) {
                    callback.onSignInSuccess(firebaseUser, user);
                }
            }
        });
    }
    
    /**
     * Sign out from Google and Firebase
     */
    public void signOut(final Runnable onComplete) {
        // Sign out from Firebase
        mAuth.signOut();
        
        // Sign out from Google
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.signOut()
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "Google Sign-Out completed");
                            
                            // Clear session
                            sessionManager.logout();
                            
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        }
                    });
        } else {
            sessionManager.logout();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    /**
     * Check if user is currently signed in with Google
     */
    public boolean isSignedIn() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        return account != null && firebaseUser != null;
    }
    
    /**
     * Get current Google account
     */
    public GoogleSignInAccount getCurrentAccount() {
        return GoogleSignIn.getLastSignedInAccount(context);
    }
    
    /**
     * Revoke Google access (complete disconnect)
     */
    public void revokeAccess(final Runnable onComplete) {
        // Sign out from Firebase
        mAuth.signOut();
        
        // Revoke Google access
        if (mGoogleSignInClient != null) {
            mGoogleSignInClient.revokeAccess()
                    .addOnCompleteListener((Activity) context, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "Google access revoked");
                            
                            // Clear session
                            sessionManager.logout();
                            
                            if (onComplete != null) {
                                onComplete.run();
                            }
                        }
                    });
        } else {
            sessionManager.logout();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
}
