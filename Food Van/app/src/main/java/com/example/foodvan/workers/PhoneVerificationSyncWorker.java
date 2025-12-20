package com.example.foodvan.workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * PhoneVerificationSyncWorker - WorkManager worker for syncing phone verification data
 * when the device comes online after being offline during verification.
 * 
 * This ensures that phone verification status is persisted to Firebase even if
 * the user verifies their phone while offline.
 */
public class PhoneVerificationSyncWorker extends Worker {

    private static final String TAG = "PhoneVerificationSync";
    
    private final DatabaseReference databaseReference;

    public PhoneVerificationSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @NonNull
    @Override
    public Result doWork() {
        // Get input data
        String userId = getInputData().getString("user_id");
        String phone = getInputData().getString("phone");
        String countryCode = getInputData().getString("country_code");
        boolean verified = getInputData().getBoolean("verified", false);
        long timestamp = getInputData().getLong("timestamp", System.currentTimeMillis());

        // Validate input
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty, cannot sync");
            return Result.failure();
        }

        if (phone == null || phone.isEmpty()) {
            Log.e(TAG, "Phone number is null or empty, cannot sync");
            return Result.failure();
        }

        Log.d(TAG, "Starting phone verification sync for user: " + userId);

        try {
            // Prepare phone data for customers profile path
            Map<String, Object> phoneData = new HashMap<>();
            phoneData.put("phone", phone);
            phoneData.put("countryCode", countryCode != null ? countryCode : "+91");
            phoneData.put("phoneVerified", verified);
            phoneData.put("lastUpdated", timestamp);

            // Sync to customers/{uid}/profile
            databaseReference.child("customers")
                    .child(userId)
                    .child("profile")
                    .updateChildren(phoneData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Phone data synced to customers/profile successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to sync phone data to customers/profile", e);
                    });

            // Also sync to users path for backward compatibility
            Map<String, Object> userData = new HashMap<>();
            userData.put("phone", phone);
            userData.put("phoneVerified", verified);

            databaseReference.child("users")
                    .child(userId)
                    .updateChildren(userData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Phone data synced to users path successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to sync phone data to users path", e);
                    });

            Log.d(TAG, "Phone verification sync completed for user: " + userId);
            return Result.success();

        } catch (Exception e) {
            Log.e(TAG, "Error during phone verification sync", e);
            
            // Retry if it's a transient error
            if (getRunAttemptCount() < 3) {
                Log.d(TAG, "Retrying sync, attempt: " + (getRunAttemptCount() + 1));
                return Result.retry();
            }
            
            return Result.failure();
        }
    }
}
