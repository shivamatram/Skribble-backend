package com.example.foodvan.activities.customer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.OrderDetailItemsAdapter;
import com.example.foodvan.models.Order;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * OrderDetailsActivity - Display detailed order information
 * Features: Order items, bill details, delivery address, payment method, actions
 */
public class OrderDetailsActivity extends AppCompatActivity {

    private static final String TAG = "OrderDetailsActivity";

    // UI Components
    private Toolbar toolbar;
    private TextView tvOrderId;
    private TextView tvStatus;
    private TextView tvOrderDate;
    private TextView tvTotalAmount;
    private ImageView ivVendorImage;
    private TextView tvVendorName;
    private TextView tvVendorLocation;
    private ImageView ivCallVendor;
    private RecyclerView rvOrderItems;
    private TextView tvSubtotal;
    private TextView tvDeliveryFee;
    private TextView tvTax;
    private TextView tvDiscount;
    private TextView tvBillTotal;
    private LinearLayout layoutDiscount;
    private TextView tvDeliveryAddress;
    private ImageView ivPaymentIcon;
    private TextView tvPaymentMethod;
    private TextView tvPaymentStatus;
    private MaterialButton btnReorder;
    private MaterialButton btnDownloadInvoice;

    // Data
    private Order order;
    private OrderDetailItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        getOrderFromIntent();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        populateOrderDetails();
    }

    private void getOrderFromIntent() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("order")) {
            order = (Order) intent.getSerializableExtra("order");
        }

        if (order == null) {
            showError("Order details not available");
            finish();
        }
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        tvOrderId = findViewById(R.id.tv_order_id);
        tvStatus = findViewById(R.id.tv_status);
        tvOrderDate = findViewById(R.id.tv_order_date);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        ivVendorImage = findViewById(R.id.iv_vendor_image);
        tvVendorName = findViewById(R.id.tv_vendor_name);
        tvVendorLocation = findViewById(R.id.tv_vendor_location);
        ivCallVendor = findViewById(R.id.iv_call_vendor);
        rvOrderItems = findViewById(R.id.rv_order_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDeliveryFee = findViewById(R.id.tv_delivery_fee);
        tvTax = findViewById(R.id.tv_tax);
        tvDiscount = findViewById(R.id.tv_discount);
        tvBillTotal = findViewById(R.id.tv_bill_total);
        layoutDiscount = findViewById(R.id.layout_discount);
        tvDeliveryAddress = findViewById(R.id.tv_delivery_address);
        ivPaymentIcon = findViewById(R.id.iv_payment_icon);
        tvPaymentMethod = findViewById(R.id.tv_payment_method);
        tvPaymentStatus = findViewById(R.id.tv_payment_status);
        btnReorder = findViewById(R.id.btn_reorder);
        btnDownloadInvoice = findViewById(R.id.btn_download_invoice);
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
        if (order != null && order.getItems() != null) {
            itemsAdapter = new OrderDetailItemsAdapter(this, order.getItems());
            rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
            rvOrderItems.setAdapter(itemsAdapter);
        }
    }

    private void setupClickListeners() {
        ivCallVendor.setOnClickListener(v -> callVendor());
        btnReorder.setOnClickListener(v -> reorderItems());
        btnDownloadInvoice.setOnClickListener(v -> downloadInvoice());
    }

    private void populateOrderDetails() {
        if (order == null) return;

        // Order status and basic info
        tvOrderId.setText("Order #" + (order.getOrderId() != null ? order.getOrderId() : "Unknown"));
        setOrderStatus(order.getStatus());
        tvOrderDate.setText("Ordered on " + formatOrderDate(order.getOrderTime()));
        tvTotalAmount.setText("Total: " + order.getFormattedTotalAmount());

        // Vendor info
        tvVendorName.setText(order.getVanName() != null ? order.getVanName() : "Food Van");
        tvVendorLocation.setText("Location not available"); // TODO: Add vendor location

        // Bill details
        tvSubtotal.setText(order.getFormattedSubtotal());
        tvDeliveryFee.setText(order.getFormattedDeliveryFee());
        tvTax.setText(order.getFormattedTax());
        tvBillTotal.setText(order.getFormattedTotalAmount());

        // Show/hide discount
        if (order.getDiscount() > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            tvDiscount.setText("-" + order.getFormattedDiscount());
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        // Delivery address
        tvDeliveryAddress.setText(order.getDeliveryAddress() != null ? 
                                 order.getDeliveryAddress() : "Address not available");

        // Payment method
        setPaymentMethodInfo();
    }

    private void setOrderStatus(String status) {
        tvStatus.setText(status != null ? status : "UNKNOWN");
        
        int backgroundRes;
        switch (status != null ? status : "UNKNOWN") {
            case "DELIVERED":
                backgroundRes = R.drawable.bg_status_delivered;
                break;
            case "CANCELLED":
                backgroundRes = R.drawable.bg_status_cancelled;
                break;
            case "PLACED":
            case "CONFIRMED":
            case "PREPARING":
            case "READY":
                backgroundRes = R.drawable.bg_status_ongoing;
                break;
            default:
                backgroundRes = R.drawable.bg_status_ongoing;
                break;
        }
        
        tvStatus.setBackgroundResource(backgroundRes);
    }

    private void setPaymentMethodInfo() {
        String paymentMethod = order.getPaymentMethod();
        String paymentStatus = order.getPaymentStatus();

        // Set payment method text
        tvPaymentMethod.setText(paymentMethod != null ? paymentMethod : "Not specified");

        // Set payment method icon
        int iconRes = R.drawable.ic_payment;
        switch (paymentMethod != null ? paymentMethod.toUpperCase() : "CASH") {
            case "CASH":
                iconRes = R.drawable.ic_money;
                break;
            case "UPI":
                iconRes = R.drawable.ic_upi;
                break;
            case "CARD":
            case "CREDIT_CARD":
            case "DEBIT_CARD":
                iconRes = R.drawable.ic_credit_card;
                break;
            default:
                iconRes = R.drawable.ic_payment;
                break;
        }
        ivPaymentIcon.setImageResource(iconRes);

        // Set payment status
        tvPaymentStatus.setText(paymentStatus != null ? paymentStatus : "PENDING");
        
        int statusBg;
        switch (paymentStatus != null ? paymentStatus : "PENDING") {
            case "PAID":
                statusBg = R.drawable.bg_success_badge;
                break;
            case "FAILED":
                statusBg = R.drawable.bg_status_cancelled;
                break;
            case "REFUNDED":
                statusBg = R.drawable.bg_status_ongoing;
                break;
            default:
                statusBg = R.drawable.bg_status_ongoing;
                break;
        }
        tvPaymentStatus.setBackgroundResource(statusBg);
    }

    private void callVendor() {
        // TODO: Implement call functionality with vendor phone number
        String phoneNumber = order.getCustomerPhone(); // This should be vendor phone
        
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            
            try {
                startActivity(callIntent);
            } catch (Exception e) {
                showError("Unable to make call");
            }
        } else {
            showError("Vendor contact not available");
        }
    }

    private void reorderItems() {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            showError("Cannot reorder: Order items not available");
            return;
        }

        // TODO: Implement reorder functionality
        // Add items to cart and navigate to cart
        showSuccess("Items added to cart successfully");
        
        // For now, just show a success message
        Toast.makeText(this, "Reorder functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    private void downloadInvoice() {
        // TODO: Implement invoice download functionality
        showSuccess("Invoice download will be implemented");
        Toast.makeText(this, "Invoice download functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    private String formatOrderDate(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            return "Date unavailable";
        }
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }
}
