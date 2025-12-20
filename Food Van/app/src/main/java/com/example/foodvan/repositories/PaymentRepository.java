package com.example.foodvan.repositories;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.foodvan.models.BankAccountDetails;
import com.example.foodvan.models.PaymentTransaction;
import com.example.foodvan.models.PayoutSettings;
import com.example.foodvan.models.UpiDetails;
import com.example.foodvan.utils.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class for Payment & Payout data operations
 * Handles Firebase Firestore operations and data management
 */
public class PaymentRepository {

    private static final String TAG = "PaymentRepository";
    
    // Firestore collections
    private static final String VENDORS_COLLECTION = "vendors";
    private static final String BANK_DETAILS_FIELD = "bankDetails";
    private static final String UPI_DETAILS_FIELD = "upiDetails";
    private static final String PAYOUT_SETTINGS_FIELD = "payoutSettings";
    private static final String PAYMENT_HISTORY_COLLECTION = "paymentHistory";

    private FirebaseFirestore firestore;
    private SessionManager sessionManager;
    private String vendorId;

    public PaymentRepository(Context context) {
        firestore = FirebaseFirestore.getInstance();
        sessionManager = new SessionManager(context);
        vendorId = sessionManager.getUserId();
    }

    // Callback interfaces
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }

    // Bank Account Details Operations
    public void getBankAccountDetails(DataCallback<BankAccountDetails> callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains(BANK_DETAILS_FIELD)) {
                        try {
                            BankAccountDetails details = documentSnapshot.get(BANK_DETAILS_FIELD, BankAccountDetails.class);
                            callback.onSuccess(details);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing bank details", e);
                            callback.onError("Error parsing bank account details");
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading bank details", e);
                    callback.onError("Failed to load bank account details: " + e.getMessage());
                });
    }

    public void saveBankAccountDetails(BankAccountDetails details, OperationCallback callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(BANK_DETAILS_FIELD, details);

        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Bank details saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating bank details, trying to create document", e);
                    // If update fails, try to create the document
                    firestore.collection(VENDORS_COLLECTION)
                            .document(vendorId)
                            .set(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Bank details saved successfully (new document)");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Error creating bank details document", e2);
                                callback.onError("Failed to save bank account details: " + e2.getMessage());
                            });
                });
    }

    public void verifyBankAccount(BankAccountDetails details, OperationCallback callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        if (details == null) {
            callback.onError("Bank account details not found");
            return;
        }

        // Validate all required fields before verification
        if (details.getAccountHolderName() == null || details.getAccountHolderName().trim().isEmpty()) {
            callback.onError("Account holder name is required for verification");
            return;
        }

        if (details.getAccountNumber() == null || details.getAccountNumber().trim().isEmpty()) {
            callback.onError("Account number is required for verification");
            return;
        }

        if (details.getIfscCode() == null || details.getIfscCode().trim().isEmpty()) {
            callback.onError("IFSC code is required for verification");
            return;
        }

        if (details.getBankName() == null || details.getBankName().trim().isEmpty()) {
            callback.onError("Bank name is required for verification");
            return;
        }

        Log.d(TAG, "Starting bank account verification for: " + details.getAccountHolderName());
        Log.d(TAG, "Account Number: " + maskAccountNumber(details.getAccountNumber()));
        Log.d(TAG, "IFSC Code: " + details.getIfscCode());
        Log.d(TAG, "Bank Name: " + details.getBankName());
        Log.d(TAG, "Account Type: " + details.getAccountType());

        // Simulate bank account verification process
        // In real implementation, this would call a bank verification API
        android.os.Handler handler = new android.os.Handler();
        handler.postDelayed(() -> {
            // Simulate verification success (90% success rate for demo)
            boolean verificationSuccess = Math.random() > 0.1;
            
            if (verificationSuccess) {
                details.setVerified(true);
                details.setLastUpdated(System.currentTimeMillis());
                
                // Save the verified details to Firebase
                saveBankAccountDetails(details, new OperationCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Bank account verified and saved successfully");
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error saving verified bank details: " + error);
                        callback.onError("Verification successful but failed to save: " + error);
                    }
                });
            } else {
                Log.w(TAG, "Bank account verification failed");
                callback.onError("Bank account verification failed. Please check your details and try again.");
            }
        }, 3000); // 3-second delay to simulate API call
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }
        return "****" + accountNumber.substring(accountNumber.length() - 4);
    }

    // UPI Details Operations
    public void getUpiDetails(DataCallback<UpiDetails> callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains(UPI_DETAILS_FIELD)) {
                        try {
                            UpiDetails details = documentSnapshot.get(UPI_DETAILS_FIELD, UpiDetails.class);
                            callback.onSuccess(details);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing UPI details", e);
                            callback.onError("Error parsing UPI details");
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading UPI details", e);
                    callback.onError("Failed to load UPI details: " + e.getMessage());
                });
    }

    public void saveUpiDetails(UpiDetails details, OperationCallback callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(UPI_DETAILS_FIELD, details);

        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "UPI details saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating UPI details, trying to create document", e);
                    // If update fails, try to create the document
                    firestore.collection(VENDORS_COLLECTION)
                            .document(vendorId)
                            .set(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "UPI details saved successfully (new document)");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Error creating UPI details document", e2);
                                callback.onError("Failed to save UPI details: " + e2.getMessage());
                            });
                });
    }

    public void verifyUpiId(String upiId, OperationCallback callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        if (upiId == null || upiId.trim().isEmpty()) {
            callback.onError("UPI ID is required for verification");
            return;
        }

        // Validate UPI ID format
        if (!isValidUpiIdFormat(upiId)) {
            callback.onError("Invalid UPI ID format. Please use format: username@provider");
            return;
        }

        Log.d(TAG, "Starting UPI verification for: " + upiId);

        // First, get current UPI details to update them
        getUpiDetails(new DataCallback<UpiDetails>() {
            @Override
            public void onSuccess(UpiDetails currentUpiDetails) {
                // Create or update UPI details
                UpiDetails upiDetails = currentUpiDetails != null ? currentUpiDetails : new UpiDetails();
                upiDetails.setUpiId(upiId);
                
                // Simulate UPI verification process
                android.os.Handler handler = new android.os.Handler();
                handler.postDelayed(() -> {
                    // Simulate verification success (85% success rate for demo)
                    boolean verificationSuccess = Math.random() > 0.15;
                    
                    if (verificationSuccess) {
                        upiDetails.setVerified(true);
                        upiDetails.setLastUpdated(System.currentTimeMillis());
                        
                        Log.d(TAG, "UPI verification successful for: " + upiId);
                        
                        // Save the verified UPI details to Firebase
                        saveUpiDetails(upiDetails, new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "UPI details verified and saved successfully");
                                callback.onSuccess();
                            }

                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Error saving verified UPI details: " + error);
                                callback.onError("Verification successful but failed to save: " + error);
                            }
                        });
                    } else {
                        Log.w(TAG, "UPI verification failed for: " + upiId);
                        callback.onError("UPI verification failed. Please check your UPI ID and try again.");
                    }
                }, 2500); // 2.5-second delay to simulate API call
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error getting current UPI details: " + error);
                callback.onError("Failed to verify UPI: " + error);
            }
        });
    }

    private boolean isValidUpiIdFormat(String upiId) {
        // UPI ID format: username@provider (e.g., user@paytm, user@phonepe)
        return upiId != null && upiId.matches("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$");
    }

    public void generateUpiQrCode(String upiId, DataCallback<Bitmap> callback) {
        try {
            // Create UPI payment URL
            String upiUrl = "upi://pay?pa=" + upiId + "&pn=Food Van Vendor&cu=INR";
            
            // Generate QR code
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix bitMatrix = writer.encode(upiUrl, BarcodeFormat.QR_CODE, 512, 512);
            
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            // Save QR code data to UPI details
            String qrData = bitmapToBase64(bitmap);
            getUpiDetails(new DataCallback<UpiDetails>() {
                @Override
                public void onSuccess(UpiDetails upiDetails) {
                    if (upiDetails != null) {
                        upiDetails.setQrCodeData(qrData);
                        saveUpiDetails(upiDetails, new OperationCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess(bitmap);
                            }

                            @Override
                            public void onError(String error) {
                                callback.onSuccess(bitmap); // Still return bitmap even if save fails
                            }
                        });
                    } else {
                        callback.onSuccess(bitmap);
                    }
                }

                @Override
                public void onError(String error) {
                    callback.onSuccess(bitmap); // Still return bitmap even if UPI details fetch fails
                }
            });
            
        } catch (WriterException e) {
            Log.e(TAG, "Error generating QR code", e);
            callback.onError("Failed to generate QR code: " + e.getMessage());
        }
    }

    public void loadQrCodeFromData(String qrData, DataCallback<Bitmap> callback) {
        try {
            Bitmap bitmap = base64ToBitmap(qrData);
            callback.onSuccess(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error loading QR code from data", e);
            callback.onError("Failed to load QR code");
        }
    }

    // Payment History Operations
    public void getPaymentHistory(DataCallback<List<PaymentTransaction>> callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        firestore.collection(PAYMENT_HISTORY_COLLECTION)
                .whereEqualTo("vendorId", vendorId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PaymentTransaction> transactions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        try {
                            PaymentTransaction transaction = document.toObject(PaymentTransaction.class);
                            if (transaction != null) {
                                transactions.add(transaction);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing transaction", e);
                        }
                    }
                    callback.onSuccess(transactions);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading payment history", e);
                    callback.onError("Failed to load payment history: " + e.getMessage());
                });
    }

    // Payout Settings Operations
    public void getPayoutSettings(DataCallback<PayoutSettings> callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains(PAYOUT_SETTINGS_FIELD)) {
                        try {
                            PayoutSettings settings = documentSnapshot.get(PAYOUT_SETTINGS_FIELD, PayoutSettings.class);
                            callback.onSuccess(settings);
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing payout settings", e);
                            callback.onError("Error parsing payout settings");
                        }
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading payout settings", e);
                    callback.onError("Failed to load payout settings: " + e.getMessage());
                });
    }

    public void savePayoutSettings(PayoutSettings settings, OperationCallback callback) {
        if (vendorId == null) {
            callback.onError("Vendor ID not found");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put(PAYOUT_SETTINGS_FIELD, settings);

        firestore.collection(VENDORS_COLLECTION)
                .document(vendorId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payout settings saved successfully");
                    callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating payout settings, trying to create document", e);
                    // If update fails, try to create the document
                    firestore.collection(VENDORS_COLLECTION)
                            .document(vendorId)
                            .set(updates)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Payout settings saved successfully (new document)");
                                callback.onSuccess();
                            })
                            .addOnFailureListener(e2 -> {
                                Log.e(TAG, "Error creating payout settings document", e2);
                                callback.onError("Failed to save payout settings: " + e2.getMessage());
                            });
                });
    }

    // Utility methods
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}
