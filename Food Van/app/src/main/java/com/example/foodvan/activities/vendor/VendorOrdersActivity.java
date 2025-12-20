package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import com.example.foodvan.R;
import com.example.foodvan.adapters.VendorOrdersAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VendorOrdersActivity extends AppCompatActivity implements 
        VendorOrdersAdapter.OnOrderActionListener {

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputEditText etSearch;
    private MaterialButton btnRefresh, btnManageMenu;
    private ChipGroup chipGroupStatus;
    private RecyclerView rvOrders;
    private LinearLayout layoutEmptyState, layoutLoading;
    private ExtendedFloatingActionButton fabNewOrders;

    // Data and Adapters
    private VendorOrdersAdapter ordersAdapter;
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference ordersRef;
    private String vendorId;
    private ValueEventListener ordersListener;
    
    // Utils
    private SessionManager sessionManager;
    
    // Filter State
    private String currentSearchQuery = "";
    private String selectedStatus = "All";
    
    // Order Status Constants
    private static final String STATUS_ALL = "All";
    private static final String STATUS_PENDING = "PLACED";
    private static final String STATUS_ACCEPTED = "CONFIRMED";
    private static final String STATUS_PREPARING = "PREPARING";
    private static final String STATUS_READY = "READY";
    private static final String STATUS_DELIVERED = "DELIVERED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_vendor_orders);
        
        initializeComponents();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupSearchAndFilters();
        loadOrders();
        setupFCMNotifications();
    }

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        vendorId = sessionManager.getUserId();
        
        if (vendorId != null) {
            // Create a reference to orders, we'll filter in the listener
            ordersRef = FirebaseDatabase.getInstance().getReference("orders");
        }
        
        allOrders = new ArrayList<>();
        filteredOrders = new ArrayList<>();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearch = findViewById(R.id.et_search);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnManageMenu = findViewById(R.id.btn_manage_menu);
        chipGroupStatus = findViewById(R.id.chip_group_status);
        rvOrders = findViewById(R.id.rv_orders);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        layoutLoading = findViewById(R.id.layout_loading);
        fabNewOrders = findViewById(R.id.fab_new_orders);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        ordersAdapter = new VendorOrdersAdapter(this, filteredOrders, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(ordersAdapter);
    }

    private void setupClickListeners() {
        btnRefresh.setOnClickListener(v -> refreshOrders());
        btnManageMenu.setOnClickListener(v -> openMenuManagement());
        
        fabNewOrders.setOnClickListener(v -> {
            fabNewOrders.setVisibility(View.GONE);
            refreshOrders();
        });
    }

    private void setupSearchAndFilters() {
        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterOrders();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Status filter chips
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedStatus = getStatusFromChip(selectedChip.getId());
                filterOrders();
            }
        });
    }

    private String getStatusFromChip(int chipId) {
        if (chipId == R.id.chip_all) return STATUS_ALL;
        else if (chipId == R.id.chip_pending) return STATUS_PENDING;
        else if (chipId == R.id.chip_accepted) return STATUS_ACCEPTED;
        else if (chipId == R.id.chip_preparing) return STATUS_PREPARING;
        else if (chipId == R.id.chip_ready) return STATUS_READY;
        else if (chipId == R.id.chip_delivered) return STATUS_DELIVERED;
        return STATUS_ALL;
    }

    private void loadOrders() {
        if (ordersRef == null) {
            showError("Error: Unable to load orders");
            return;
        }
        
        showLoading(true);
        
        ordersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && vendorId.equals(order.getVendorId())) {
                        order.setOrderId(orderSnapshot.getKey());
                        allOrders.add(order);
                    }
                }
                
                // Sort orders by timestamp (newest first)
                Collections.sort(allOrders, (a, b) -> 
                    Long.compare(b.getOrderTime(), a.getOrderTime()));
                
                filterOrders();
                showLoading(false);
                
                // Check for new orders
                checkForNewOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showLoading(false);
                showError("Error loading orders: " + error.getMessage());
            }
        };
        
        ordersRef.addValueEventListener(ordersListener);
    }

    private void filterOrders() {
        filteredOrders.clear();
        
        for (Order order : allOrders) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                    order.getOrderId().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    order.getCustomerName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    order.getCustomerPhone().contains(currentSearchQuery);
            
            boolean matchesStatus = selectedStatus.equals(STATUS_ALL) || 
                    order.getStatus().equals(selectedStatus);
            
            if (matchesSearch && matchesStatus) {
                filteredOrders.add(order);
            }
        }
        
        updateUI();
    }

    private void updateUI() {
        if (filteredOrders.isEmpty()) {
            rvOrders.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvOrders.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
        
        ordersAdapter.notifyDataSetChanged();
    }

    private void showLoading(boolean show) {
        layoutLoading.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            rvOrders.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    private void checkForNewOrders() {
        // Check for pending orders to show notification
        long pendingCount = allOrders.stream()
                .filter(order -> STATUS_PENDING.equals(order.getStatus()))
                .count();
        
        if (pendingCount > 0) {
            fabNewOrders.setText(pendingCount + " New Orders");
            fabNewOrders.setVisibility(View.VISIBLE);
        } else {
            fabNewOrders.setVisibility(View.GONE);
        }
    }

    private void refreshOrders() {
        if (ordersRef != null) {
            loadOrders();
            showToast("Orders refreshed");
        }
    }

    private void openMenuManagement() {
        Intent intent = new Intent(this, VendorMenuManagementActivity.class);
        startActivity(intent);
    }

    private void setupFCMNotifications() {
        FirebaseMessaging.getInstance().subscribeToTopic("vendor_" + vendorId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully subscribed to vendor-specific notifications
                    }
                });
    }

    // VendorOrdersAdapter.OnOrderActionListener implementation
    @Override
    public void onOrderClick(Order order) {
        openOrderDetails(order);
    }

    @Override
    public void onCallCustomer(Order order) {
        if (order.getCustomerPhone() != null && !order.getCustomerPhone().isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + order.getCustomerPhone()));
            startActivity(callIntent);
        } else {
            showToast("Customer phone number not available");
        }
    }

    @Override
    public void onAcceptOrder(Order order) {
        showAcceptOrderDialog(order);
    }

    @Override
    public void onRejectOrder(Order order) {
        showRejectOrderDialog(order);
    }

    @Override
    public void onUpdateOrderStatus(Order order, String newStatus) {
        updateOrderStatus(order, newStatus);
    }

    private void openOrderDetails(Order order) {
        // TODO: Fix XML namespace issue in OrderDetailActivity
        Toast.makeText(this, "Order Details - Temporarily Unavailable (XML Issue)", Toast.LENGTH_SHORT).show();
    }

    private void showAcceptOrderDialog(Order order) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Accept Order")
                .setMessage("Accept order #" + order.getOrderId() + "?")
                .setPositiveButton("Accept", (dialog, which) -> {
                    updateOrderStatus(order, STATUS_ACCEPTED);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showRejectOrderDialog(Order order) {
        String[] reasons = {
                "Items not available",
                "Kitchen closed",
                "Too many orders",
                "Delivery area not covered",
                "Other"
        };
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reject Order")
                .setMessage("Select reason for rejecting order #" + order.getOrderId())
                .setSingleChoiceItems(reasons, -1, null)
                .setPositiveButton("Reject", (dialog, which) -> {
                    updateOrderStatus(order, "CANCELLED");
                    // TODO: Send rejection reason to customer
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        if (ordersRef == null) {
            showError("Unable to update order status");
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("lastUpdated", System.currentTimeMillis());
        
        // Add timestamp for specific status
        switch (newStatus) {
            case STATUS_ACCEPTED:
                updates.put("confirmedTime", System.currentTimeMillis());
                break;
            case STATUS_READY:
                updates.put("readyTime", System.currentTimeMillis());
                break;
            case STATUS_DELIVERED:
                updates.put("deliveredTime", System.currentTimeMillis());
                break;
        }
        
        FirebaseDatabase.getInstance().getReference("orders")
                .child(order.getOrderId())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showToast("Order status updated successfully");
                    // TODO: Send FCM notification to customer
                })
                .addOnFailureListener(e -> {
                    showError("Failed to update order status: " + e.getMessage());
                });
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setAction("Retry", v -> refreshOrders())
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ordersRef != null && ordersListener != null) {
            ordersRef.removeEventListener(ordersListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    /**
     * Setup orange status bar to match the navbar theme
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.primary_color));
            
            // Ensure white status bar icons on orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR; // Remove light status bar flag
                decorView.setSystemUiVisibility(flags);
            }
        }
    }
}
