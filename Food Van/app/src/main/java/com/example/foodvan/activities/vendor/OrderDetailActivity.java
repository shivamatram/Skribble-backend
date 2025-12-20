package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.foodvan.R;
import com.example.foodvan.adapters.OrderItemsAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.OrderItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OrderDetailActivity extends AppCompatActivity {

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvOrderId, tvOrderDate, tvCustomerName, tvCustomerPhone;
    private TextView tvDeliveryAddress, tvSubtotal, tvDeliveryFee, tvTax, tvTotalAmount;
    private TextView tvPaymentMethod, tvCustomerNote;
    private Chip chipOrderStatus;
    private ImageView ivPaymentMethod;
    private MaterialCardView cardCustomerNote, cardDeliveryInfo, cardPrepTime;
    private LinearLayout layoutDeliveryFee, layoutBottomActions;
    private RecyclerView rvOrderItems;
    private MaterialButton btnCallCustomer, btnAcceptOrder, btnRejectOrder, btnUpdateStatus;
    private TextInputEditText etPrepTime;
    private MaterialButton btnUpdatePrepTime;

    // Data
    private String orderId;
    private Order currentOrder;
    private OrderItemsAdapter itemsAdapter;

    // Firebase
    private DatabaseReference orderRef;
    private ValueEventListener orderListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_order_detail);

        // Get order ID from intent
        orderId = getIntent().getStringExtra("order_id");
        if (orderId == null) {
            showError("Order ID not found");
            finish();
            return;
        }

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadOrderDetails();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTax = findViewById(R.id.tv_tax);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvCustomerNote = findViewById(R.id.tv_customer_note);
        
        chipOrderStatus = findViewById(R.id.chip_order_status);
        ivPaymentMethod = findViewById(R.id.iv_payment_method);
        
        cardCustomerNote = findViewById(R.id.card_customer_note);
        // cardDeliveryInfo = findViewById(R.id.layout_delivery_info);
        cardPrepTime = findViewById(R.id.card_prep_time);
        layoutDeliveryFee = findViewById(R.id.layout_delivery_fee);
        layoutBottomActions = findViewById(R.id.layout_bottom_actions);
        
        rvOrderItems = findViewById(R.id.rv_order_items);
        
        btnCallCustomer = findViewById(R.id.btn_call_customer);
        btnAcceptOrder = findViewById(R.id.btn_accept_order);
        btnRejectOrder = findViewById(R.id.btn_reject_order);
        btnUpdateStatus = findViewById(R.id.btn_update_status);
        
        etPrepTime = findViewById(R.id.et_prep_time);
        btnUpdatePrepTime = findViewById(R.id.btn_update_prep_time);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        itemsAdapter = new OrderItemsAdapter(this);
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        rvOrderItems.setAdapter(itemsAdapter);
    }

    private void setupClickListeners() {
        btnCallCustomer.setOnClickListener(v -> callCustomer());
        btnAcceptOrder.setOnClickListener(v -> acceptOrder());
        btnRejectOrder.setOnClickListener(v -> showRejectDialog());
        btnUpdateStatus.setOnClickListener(v -> updateOrderStatus());
        btnUpdatePrepTime.setOnClickListener(v -> updatePrepTime());
    }

    private void loadOrderDetails() {
        orderRef = FirebaseDatabase.getInstance().getReference("orders").child(orderId);
        
        orderListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentOrder = snapshot.getValue(Order.class);
                if (currentOrder != null) {
                    currentOrder.setOrderId(orderId);
                    populateOrderDetails();
                } else {
                    showError("Order not found");
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Error loading order: " + error.getMessage());
            }
        };
        
        orderRef.addValueEventListener(orderListener);
    }

    private void populateOrderDetails() {
        // Order header
        tvOrderId.setText("#" + currentOrder.getOrderId());
        tvOrderDate.setText(formatDateTime(currentOrder.getOrderTime()));
        
        // Customer info
        tvCustomerName.setText(currentOrder.getCustomerName());
        tvCustomerPhone.setText(currentOrder.getCustomerPhone());
        
        // Delivery info
        if (!TextUtils.isEmpty(currentOrder.getDeliveryAddress())) {
            cardDeliveryInfo.setVisibility(View.VISIBLE);
            tvDeliveryAddress.setText(currentOrder.getDeliveryAddress());
        } else {
            cardDeliveryInfo.setVisibility(View.GONE);
        }
        
        // Order status
        setOrderStatus(currentOrder.getStatus());
        
        // Order items
        if (currentOrder.getItems() != null) {
            itemsAdapter.updateItems(currentOrder.getItems());
        }
        
        // Customer note
        if (!TextUtils.isEmpty(currentOrder.getSpecialInstructions())) {
            cardCustomerNote.setVisibility(View.VISIBLE);
            tvCustomerNote.setText(currentOrder.getSpecialInstructions());
        } else {
            cardCustomerNote.setVisibility(View.GONE);
        }
        
        // Bill summary
        tvSubtotal.setText(String.format("₹%.2f", currentOrder.getSubtotal()));
        tvTax.setText(String.format("₹%.2f", currentOrder.getTax()));
        tvTotalAmount.setText(String.format("₹%.2f", currentOrder.getTotalAmount()));
        
        // Delivery fee (if applicable)
        if (currentOrder.getDeliveryFee() > 0) {
            layoutDeliveryFee.setVisibility(View.VISIBLE);
            tvDeliveryFee.setText(String.format("₹%.2f", currentOrder.getDeliveryFee()));
        } else {
            layoutDeliveryFee.setVisibility(View.GONE);
        }
        
        // Payment method
        setPaymentMethod(currentOrder.getPaymentMethod());
        
        // Setup action buttons
        setupActionButtons();
        
        // Prep time
        etPrepTime.setText(String.valueOf(currentOrder.getEstimatedDeliveryTime()));
    }

    private void setOrderStatus(String status) {
        chipOrderStatus.setText(getStatusDisplayText(status));
        
        switch (status) {
            case "PLACED":
                chipOrderStatus.setChipBackgroundColorResource(R.color.warning_color);
                break;
            case "CONFIRMED":
                chipOrderStatus.setChipBackgroundColorResource(R.color.info_color);
                break;
            case "PREPARING":
                chipOrderStatus.setChipBackgroundColorResource(R.color.primary_color);
                break;
            case "READY":
                chipOrderStatus.setChipBackgroundColorResource(R.color.accent);
                break;
            case "DELIVERED":
                chipOrderStatus.setChipBackgroundColorResource(R.color.success_color);
                break;
            case "CANCELLED":
                chipOrderStatus.setChipBackgroundColorResource(R.color.error_color);
                break;
        }
    }

    private String getStatusDisplayText(String status) {
        switch (status) {
            case "PLACED": return "Pending";
            case "CONFIRMED": return "Accepted";
            case "PREPARING": return "Preparing";
            case "READY": return "Ready";
            case "DELIVERED": return "Delivered";
            case "CANCELLED": return "Cancelled";
            default: return status;
        }
    }

    private void setPaymentMethod(String paymentMethod) {
        tvPaymentMethod.setText(paymentMethod);
        
        switch (paymentMethod.toUpperCase()) {
            case "UPI":
                ivPaymentMethod.setImageResource(R.drawable.ic_account_balance);
                break;
            case "CARD":
            case "ONLINE":
                ivPaymentMethod.setImageResource(R.drawable.ic_credit_card);
                break;
            case "CASH":
            case "COD":
                ivPaymentMethod.setImageResource(R.drawable.ic_currency_rupee);
                break;
            default:
                ivPaymentMethod.setImageResource(R.drawable.ic_credit_card);
                break;
        }
    }

    private void setupActionButtons() {
        // Hide all buttons first
        btnAcceptOrder.setVisibility(View.GONE);
        btnRejectOrder.setVisibility(View.GONE);
        btnUpdateStatus.setVisibility(View.GONE);
        cardPrepTime.setVisibility(View.GONE);
        
        String status = currentOrder.getStatus();
        
        switch (status) {
            case "PLACED":
                btnAcceptOrder.setVisibility(View.VISIBLE);
                btnRejectOrder.setVisibility(View.VISIBLE);
                cardPrepTime.setVisibility(View.VISIBLE);
                break;
            case "CONFIRMED":
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnUpdateStatus.setText("Start Preparing");
                cardPrepTime.setVisibility(View.VISIBLE);
                break;
            case "PREPARING":
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnUpdateStatus.setText("Mark Ready");
                cardPrepTime.setVisibility(View.VISIBLE);
                break;
            case "READY":
                btnUpdateStatus.setVisibility(View.VISIBLE);
                btnUpdateStatus.setText("Mark Delivered");
                break;
            case "DELIVERED":
            case "CANCELLED":
                // No action buttons for completed orders
                break;
        }
    }

    private void callCustomer() {
        if (!TextUtils.isEmpty(currentOrder.getCustomerPhone())) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + currentOrder.getCustomerPhone()));
            startActivity(callIntent);
        } else {
            showToast("Customer phone number not available");
        }
    }

    private void acceptOrder() {
        updateOrderStatusInDB("CONFIRMED");
    }

    private void showRejectDialog() {
        String[] reasons = {
                "Items not available",
                "Kitchen closed",
                "Too many orders",
                "Delivery area not covered",
                "Other"
        };
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Reject Order")
                .setMessage("Select reason for rejecting this order")
                .setSingleChoiceItems(reasons, -1, null)
                .setPositiveButton("Reject", (dialog, which) -> {
                    updateOrderStatusInDB("CANCELLED");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateOrderStatus() {
        String currentStatus = currentOrder.getStatus();
        String newStatus;
        
        switch (currentStatus) {
            case "CONFIRMED":
                newStatus = "PREPARING";
                break;
            case "PREPARING":
                newStatus = "READY";
                break;
            case "READY":
                newStatus = "DELIVERED";
                break;
            default:
                return;
        }
        
        updateOrderStatusInDB(newStatus);
    }

    private void updatePrepTime() {
        String prepTimeStr = etPrepTime.getText().toString().trim();
        if (TextUtils.isEmpty(prepTimeStr)) {
            showToast("Please enter preparation time");
            return;
        }
        
        try {
            int prepTime = Integer.parseInt(prepTimeStr);
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("estimatedDeliveryTime", prepTime);
            
            orderRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> showToast("Preparation time updated"))
                    .addOnFailureListener(e -> showError("Failed to update preparation time"));
                    
        } catch (NumberFormatException e) {
            showToast("Please enter a valid number");
        }
    }

    private void updateOrderStatusInDB(String newStatus) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        updates.put("lastUpdated", System.currentTimeMillis());
        
        // Add timestamp for specific status
        switch (newStatus) {
            case "CONFIRMED":
                updates.put("confirmedTime", System.currentTimeMillis());
                break;
            case "READY":
                updates.put("readyTime", System.currentTimeMillis());
                break;
            case "DELIVERED":
                updates.put("deliveredTime", System.currentTimeMillis());
                break;
        }
        
        orderRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    showToast("Order status updated successfully");
                    // TODO: Send FCM notification to customer
                })
                .addOnFailureListener(e -> showError("Failed to update order status"));
    }

    private String formatDateTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
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
        if (orderRef != null && orderListener != null) {
            orderRef.removeEventListener(orderListener);
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
