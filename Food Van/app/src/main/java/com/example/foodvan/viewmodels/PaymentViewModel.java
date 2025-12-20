package com.example.foodvan.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.BankAccountDetails;
import com.example.foodvan.models.PaymentTransaction;
import com.example.foodvan.models.PayoutSettings;
import com.example.foodvan.models.UpiDetails;
import com.example.foodvan.repositories.PaymentRepository;

import java.util.List;

/**
 * ViewModel for Payment & Payout functionality
 * Manages UI-related data and business logic for all payment operations
 */
public class PaymentViewModel extends AndroidViewModel {

    private PaymentRepository paymentRepository;

    // LiveData for Bank Account Details
    private MutableLiveData<BankAccountDetails> bankAccountDetails = new MutableLiveData<>();

    // LiveData for UPI Details
    private MutableLiveData<UpiDetails> upiDetails = new MutableLiveData<>();

    // LiveData for Payment History
    private MutableLiveData<List<PaymentTransaction>> paymentHistory = new MutableLiveData<>();

    // LiveData for Payout Settings
    private MutableLiveData<PayoutSettings> payoutSettings = new MutableLiveData<>();

    // LiveData for QR Code
    private MutableLiveData<Bitmap> qrCodeBitmap = new MutableLiveData<>();

    // LiveData for UI states
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();

    public PaymentViewModel(@NonNull Application application) {
        super(application);
        paymentRepository = new PaymentRepository(application);
    }

    // Bank Account Details Methods
    public LiveData<BankAccountDetails> getBankAccountDetails() {
        return bankAccountDetails;
    }

    public void loadBankAccountDetails() {
        isLoading.setValue(true);
        paymentRepository.getBankAccountDetails(new PaymentRepository.DataCallback<BankAccountDetails>() {
            @Override
            public void onSuccess(BankAccountDetails data) {
                isLoading.setValue(false);
                bankAccountDetails.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void saveBankAccountDetails(BankAccountDetails details) {
        isLoading.setValue(true);
        paymentRepository.saveBankAccountDetails(details, new PaymentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Bank account details saved successfully");
                bankAccountDetails.setValue(details);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void verifyBankAccount(BankAccountDetails details) {
        isLoading.setValue(true);
        paymentRepository.verifyBankAccount(details, new PaymentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Bank account verification initiated");
                // Update verification status
                details.setVerified(true);
                bankAccountDetails.setValue(details);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    // UPI Details Methods
    public LiveData<UpiDetails> getUpiDetails() {
        return upiDetails;
    }

    public void loadUpiDetails() {
        isLoading.setValue(true);
        paymentRepository.getUpiDetails(new PaymentRepository.DataCallback<UpiDetails>() {
            @Override
            public void onSuccess(UpiDetails data) {
                isLoading.setValue(false);
                upiDetails.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void saveUpiDetails(UpiDetails details) {
        isLoading.setValue(true);
        paymentRepository.saveUpiDetails(details, new PaymentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("UPI details saved successfully");
                upiDetails.setValue(details);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void verifyUpiId(String upiId) {
        isLoading.setValue(true);
        paymentRepository.verifyUpiId(upiId, new PaymentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("UPI ID verified successfully");
                // Update verification status
                UpiDetails current = upiDetails.getValue();
                if (current != null) {
                    current.setVerified(true);
                    upiDetails.setValue(current);
                }
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void generateUpiQrCode(String upiId) {
        isLoading.setValue(true);
        paymentRepository.generateUpiQrCode(upiId, new PaymentRepository.DataCallback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap data) {
                isLoading.setValue(false);
                qrCodeBitmap.setValue(data);
                successMessage.setValue("QR code generated successfully");
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void loadQrCodeFromData(String qrData) {
        paymentRepository.loadQrCodeFromData(qrData, new PaymentRepository.DataCallback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap data) {
                qrCodeBitmap.setValue(data);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    // Payment History Methods
    public LiveData<List<PaymentTransaction>> getPaymentHistory() {
        return paymentHistory;
    }

    public void loadPaymentHistory() {
        isLoading.setValue(true);
        paymentRepository.getPaymentHistory(new PaymentRepository.DataCallback<List<PaymentTransaction>>() {
            @Override
            public void onSuccess(List<PaymentTransaction> data) {
                isLoading.setValue(false);
                paymentHistory.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    // Payout Settings Methods
    public LiveData<PayoutSettings> getPayoutSettings() {
        return payoutSettings;
    }

    public void loadPayoutSettings() {
        isLoading.setValue(true);
        paymentRepository.getPayoutSettings(new PaymentRepository.DataCallback<PayoutSettings>() {
            @Override
            public void onSuccess(PayoutSettings data) {
                isLoading.setValue(false);
                payoutSettings.setValue(data);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void savePayoutSettings(PayoutSettings settings) {
        isLoading.setValue(true);
        paymentRepository.savePayoutSettings(settings, new PaymentRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                isLoading.setValue(false);
                successMessage.setValue("Payout settings saved successfully");
                payoutSettings.setValue(settings);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    // UI State Methods
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<Bitmap> getQrCodeBitmap() {
        return qrCodeBitmap;
    }

    // Clear messages
    public void clearMessages() {
        errorMessage.setValue("");
        successMessage.setValue("");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up resources if needed
    }
}
