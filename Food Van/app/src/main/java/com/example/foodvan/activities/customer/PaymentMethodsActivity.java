package com.example.foodvan.activities.customer;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.PaymentMethodsAdapter;
import com.example.foodvan.models.PaymentMethod;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * PaymentMethodsActivity - Comprehensive payment methods management
 * Features: Add, edit, delete, set default payment methods (Cards, UPI, Wallets, Cash)
 */
public class PaymentMethodsActivity extends AppCompatActivity implements PaymentMethodsAdapter.OnPaymentMethodActionListener {

    private static final String TAG = "PaymentMethodsActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvPaymentCount;
    private LinearProgressIndicator progressIndicator;
    private MaterialCardView cardEmptyState;
    private RecyclerView rvPaymentMethods;
    private FloatingActionButton fabAddPayment;

    // Data & Services
    private SessionManager sessionManager;
    private DatabaseReference paymentMethodsRef;
    private PaymentMethodsAdapter adapter;
    private List<PaymentMethod> paymentMethods;
    private PaymentMethodDialog paymentMethodDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods);

        initializeServices();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadPaymentMethods();
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        
        // Initialize Firebase reference
        String userId = sessionManager.getUserId();
        if (userId != null) {
            paymentMethodsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("paymentMethods");
        }
        
        paymentMethods = new ArrayList<>();
        paymentMethodDialog = new PaymentMethodDialog(this);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvPaymentCount = findViewById(R.id.tv_payment_count);
        progressIndicator = findViewById(R.id.progress_indicator);
        cardEmptyState = findViewById(R.id.card_empty_state);
        rvPaymentMethods = findViewById(R.id.rv_payment_methods);
        fabAddPayment = findViewById(R.id.fab_add_payment);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new PaymentMethodsAdapter(this, paymentMethods, this);
        rvPaymentMethods.setLayoutManager(new LinearLayoutManager(this));
        rvPaymentMethods.setAdapter(adapter);
    }

    private void setupClickListeners() {
        fabAddPayment.setOnClickListener(v -> showAddPaymentMethodDialog());
        
        findViewById(R.id.btn_add_first_payment).setOnClickListener(v -> showAddPaymentMethodDialog());
    }

    private void loadPaymentMethods() {
        if (paymentMethodsRef == null) {
            Log.w(TAG, "User not logged in, showing empty state");
            showEmptyState();
            return;
        }

        showProgress(true);
        
        paymentMethodsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                paymentMethods.clear();
                
                for (DataSnapshot paymentSnapshot : snapshot.getChildren()) {
                    PaymentMethod paymentMethod = paymentSnapshot.getValue(PaymentMethod.class);
                    if (paymentMethod != null) {
                        paymentMethods.add(paymentMethod);
                    }
                }
                
                // Add default Cash on Delivery if no payment methods exist
                if (paymentMethods.isEmpty()) {
                    addDefaultCashPayment();
                } else {
                    updateUI();
                }
                
                showProgress(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load payment methods", error.toException());
                showProgress(false);
                showError("Failed to load payment methods: " + error.getMessage());
            }
        });
    }

    private void addDefaultCashPayment() {
        PaymentMethod cashPayment = PaymentMethod.createCashPayment();
        cashPayment.setDefault(true);
        
        if (paymentMethodsRef != null) {
            paymentMethodsRef.child(cashPayment.getPaymentId()).setValue(cashPayment)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Default cash payment added successfully");
                        // UI will be updated through the ValueEventListener
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to add default cash payment", e);
                        showError("Failed to add default payment method");
                    });
        }
    }

    private void updateUI() {
        if (paymentMethods.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            adapter.notifyDataSetChanged();
            updatePaymentCount();
        }
    }

    private void updatePaymentCount() {
        int count = paymentMethods.size();
        String countText = count == 1 ? "1 method" : count + " methods";
        tvPaymentCount.setText(countText);
    }

    private void showEmptyState() {
        cardEmptyState.setVisibility(View.VISIBLE);
        rvPaymentMethods.setVisibility(View.GONE);
        tvPaymentCount.setText("0 methods");
    }

    private void hideEmptyState() {
        cardEmptyState.setVisibility(View.GONE);
        rvPaymentMethods.setVisibility(View.VISIBLE);
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    private void showAddPaymentMethodDialog() {
        paymentMethodDialog.showAddDialog(new PaymentMethodDialog.OnPaymentMethodSavedListener() {
            @Override
            public void onPaymentMethodSaved(PaymentMethod paymentMethod) {
                savePaymentMethodToFirebase(paymentMethod);
            }
        });
    }

    private void showEditPaymentMethodDialog(PaymentMethod paymentMethod) {
        paymentMethodDialog.showEditDialog(paymentMethod, new PaymentMethodDialog.OnPaymentMethodSavedListener() {
            @Override
            public void onPaymentMethodSaved(PaymentMethod updatedPaymentMethod) {
                updatePaymentMethodInFirebase(updatedPaymentMethod);
            }
        });
    }

    private void savePaymentMethodToFirebase(PaymentMethod paymentMethod) {
        if (paymentMethodsRef == null) {
            showError("User not logged in");
            return;
        }

        showProgress(true);
        
        // If this is set as default, clear other defaults first
        if (paymentMethod.isDefault()) {
            clearOtherDefaults(paymentMethod.getPaymentId());
        }

        paymentMethodsRef.child(paymentMethod.getPaymentId()).setValue(paymentMethod)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payment method saved successfully");
                    showSuccess("Payment method added successfully");
                    showProgress(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save payment method", e);
                    showError("Failed to save payment method");
                    showProgress(false);
                });
    }

    private void updatePaymentMethodInFirebase(PaymentMethod paymentMethod) {
        if (paymentMethodsRef == null) {
            showError("User not logged in");
            return;
        }

        showProgress(true);
        
        // If this is set as default, clear other defaults first
        if (paymentMethod.isDefault()) {
            clearOtherDefaults(paymentMethod.getPaymentId());
        }

        paymentMethodsRef.child(paymentMethod.getPaymentId()).setValue(paymentMethod)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payment method updated successfully");
                    showSuccess("Payment method updated successfully");
                    showProgress(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update payment method", e);
                    showError("Failed to update payment method");
                    showProgress(false);
                });
    }

    private void deletePaymentMethodFromFirebase(PaymentMethod paymentMethod) {
        if (paymentMethodsRef == null) {
            showError("User not logged in");
            return;
        }

        showProgress(true);
        
        paymentMethodsRef.child(paymentMethod.getPaymentId()).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Payment method deleted successfully");
                    showSuccess("Payment method deleted successfully");
                    showProgress(false);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete payment method", e);
                    showError("Failed to delete payment method");
                    showProgress(false);
                });
    }

    private void clearOtherDefaults(String currentPaymentId) {
        for (PaymentMethod method : paymentMethods) {
            if (!method.getPaymentId().equals(currentPaymentId) && method.isDefault()) {
                method.setDefault(false);
                paymentMethodsRef.child(method.getPaymentId()).setValue(method);
            }
        }
    }

    // PaymentMethodsAdapter.OnPaymentMethodActionListener implementation
    @Override
    public void onEditPaymentMethod(PaymentMethod paymentMethod) {
        showEditPaymentMethodDialog(paymentMethod);
    }

    @Override
    public void onDeletePaymentMethod(PaymentMethod paymentMethod) {
        // Prevent deleting the last payment method
        if (paymentMethods.size() <= 1) {
            showError("You must have at least one payment method");
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Payment Method")
                .setMessage("Are you sure you want to delete this payment method?")
                .setPositiveButton("Delete", (dialog, which) -> deletePaymentMethodFromFirebase(paymentMethod))
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onSetDefaultPaymentMethod(PaymentMethod paymentMethod) {
        // Clear other defaults
        clearOtherDefaults(paymentMethod.getPaymentId());
        
        // Set this as default
        paymentMethod.setDefault(true);
        updatePaymentMethodInFirebase(paymentMethod);
    }

    @Override
    public void onPaymentMethodClicked(PaymentMethod paymentMethod) {
        // Show payment method details or quick actions
        Toast.makeText(this, "Payment method: " + paymentMethod.getFormattedDetails(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (paymentMethodDialog != null) {
            paymentMethodDialog.dismiss();
        }
    }
}
