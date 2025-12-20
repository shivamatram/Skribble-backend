package com.example.foodvan.activities.vendor;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.TransactionAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.Transaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EarningDetailsActivity extends AppCompatActivity implements TransactionAdapter.OnTransactionClickListener {

    private MaterialToolbar toolbar;
    private TextView totalEarningsText;
    private TextView weeklyEarningsText;
    private TextView monthlyEarningsText;
    private LinearLayout chartContainer;
    private RecyclerView transactionsRecyclerView;
    private LinearLayout transactionsEmptyState;

    private TransactionAdapter transactionAdapter;
    private List<Transaction> transactions;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String vendorId;

    private double totalEarnings = 0.0;
    private double weeklyEarnings = 0.0;
    private double monthlyEarnings = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earning_details);

        initializeComponents();
        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        loadEarningsData();
    }

    private void initializeComponents() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        vendorId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        
        transactions = new ArrayList<>();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        totalEarningsText = findViewById(R.id.total_earnings_text);
        weeklyEarningsText = findViewById(R.id.weekly_earnings_text);
        monthlyEarningsText = findViewById(R.id.monthly_earnings_text);
        chartContainer = findViewById(R.id.chart_container);
        transactionsRecyclerView = findViewById(R.id.transactions_recycler_view);
        transactionsEmptyState = findViewById(R.id.transactions_empty_state);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        
        chartContainer.setOnClickListener(v -> {
            showToast("Chart functionality will be implemented with a charting library");
        });
    }

    private void setupRecyclerView() {
        transactionAdapter = new TransactionAdapter(this, transactions, this);
        transactionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        transactionsRecyclerView.setAdapter(transactionAdapter);
    }

    private void loadEarningsData() {
        if (vendorId.isEmpty()) {
            showToast("Authentication error. Please login again.");
            return;
        }

        // Load completed orders to calculate earnings
        db.collection("orders")
                .whereEqualTo("vendorId", vendorId)
                .whereEqualTo("status", "delivered")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    transactions.clear();
                    totalEarnings = 0.0;
                    weeklyEarnings = 0.0;
                    monthlyEarnings = 0.0;

                    Calendar weekStart = Calendar.getInstance();
                    weekStart.add(Calendar.DAY_OF_YEAR, -7);
                    
                    Calendar monthStart = Calendar.getInstance();
                    monthStart.add(Calendar.MONTH, -1);

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        
                        double amount = order.getTotalAmount();
                        totalEarnings += amount;

                        // Check if order is within last week
                        if (order.getTimestamp() != null && order.getTimestamp().after(weekStart.getTime())) {
                            weeklyEarnings += amount;
                        }

                        // Check if order is within last month
                        if (order.getTimestamp() != null && order.getTimestamp().after(monthStart.getTime())) {
                            monthlyEarnings += amount;
                        }

                        // Create transaction from order
                        Transaction transaction = new Transaction();
                        transaction.setOrderId(order.getOrderId());
                        transaction.setAmount(amount);
                        transaction.setTimestamp(order.getTimestamp());
                        transaction.setPaymentMethod(order.getPaymentMethod());
                        transactions.add(transaction);
                    }

                    updateEarningsDisplay();
                    transactionAdapter.notifyDataSetChanged();
                    updateTransactionsEmptyState();
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to load earnings data: " + e.getMessage());
                    updateTransactionsEmptyState();
                });
    }

    private void updateEarningsDisplay() {
        totalEarningsText.setText(String.format(Locale.getDefault(), "₹%.0f", totalEarnings));
        weeklyEarningsText.setText(String.format(Locale.getDefault(), "₹%.0f", weeklyEarnings));
        monthlyEarningsText.setText(String.format(Locale.getDefault(), "₹%.0f", monthlyEarnings));
    }

    private void updateTransactionsEmptyState() {
        if (transactions.isEmpty()) {
            transactionsRecyclerView.setVisibility(View.GONE);
            transactionsEmptyState.setVisibility(View.VISIBLE);
        } else {
            transactionsRecyclerView.setVisibility(View.VISIBLE);
            transactionsEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        // Navigate to order details for this transaction
        showToast("Transaction details for order " + transaction.getOrderId());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
