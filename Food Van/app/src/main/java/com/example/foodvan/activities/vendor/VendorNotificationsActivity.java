package com.example.foodvan.activities.vendor;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.foodvan.R;
import com.example.foodvan.adapters.NotificationAdapter;
import com.example.foodvan.models.VendorNotification;

import java.util.ArrayList;
import java.util.List;

public class VendorNotificationsActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private RecyclerView rvNotifications;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnMarkAllRead;
    private ChipGroup chipGroupFilters;
    private FloatingActionButton fabNotificationSettings;
    
    private NotificationAdapter notificationAdapter;
    private List<VendorNotification> notificationList;
    private List<VendorNotification> filteredNotificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_notifications);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupFilterChips();
        loadNotifications();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvNotifications = findViewById(R.id.rv_notifications);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        btnMarkAllRead = findViewById(R.id.btn_mark_all_read);
        chipGroupFilters = findViewById(R.id.chip_group_filters);
        fabNotificationSettings = findViewById(R.id.fab_notification_settings);
        
        notificationList = new ArrayList<>();
        filteredNotificationList = new ArrayList<>();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        notificationAdapter = new NotificationAdapter(this, filteredNotificationList);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(notificationAdapter);
    }

    private void setupClickListeners() {
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());
        
        fabNotificationSettings.setOnClickListener(v -> {
            Toast.makeText(this, "Notification Settings - Coming Soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilterChips() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                filterNotifications(checkedId);
            }
        });
    }

    private void loadNotifications() {
        // Sample notifications - In real app, load from Firebase/API
        notificationList.clear();
        
        notificationList.add(new VendorNotification(
            "1", "New Order Received", 
            "You have received a new order from John Doe. Order ID: #12345",
            "ORDER", System.currentTimeMillis() - 7200000, false, // 2 hours ago
            R.drawable.ic_shopping_cart
        ));
        
        notificationList.add(new VendorNotification(
            "2", "Payment Received", 
            "Payment of ₹450 has been credited to your account",
            "PAYMENT", System.currentTimeMillis() - 14400000, true, // 4 hours ago
            R.drawable.ic_payment
        ));
        
        notificationList.add(new VendorNotification(
            "3", "Order Completed", 
            "Order #12344 has been successfully delivered to customer",
            "ORDER", System.currentTimeMillis() - 21600000, true, // 6 hours ago
            R.drawable.ic_check_circle
        ));
        
        notificationList.add(new VendorNotification(
            "4", "System Update", 
            "New features available! Update your app to access latest tools",
            "SYSTEM", System.currentTimeMillis() - 86400000, false, // 1 day ago
            R.drawable.ic_system_update
        ));
        
        notificationList.add(new VendorNotification(
            "5", "Weekly Report", 
            "Your weekly performance report is ready. Check your analytics",
            "SYSTEM", System.currentTimeMillis() - 172800000, true, // 2 days ago
            R.drawable.ic_analytics
        ));
        
        // Apply default filter (All)
        filterNotifications(R.id.chip_all);
    }

    private void filterNotifications(int chipId) {
        filteredNotificationList.clear();
        
        if (chipId == R.id.chip_all) {
            filteredNotificationList.addAll(notificationList);
        } else if (chipId == R.id.chip_orders) {
            for (VendorNotification notification : notificationList) {
                if ("ORDER".equals(notification.getType())) {
                    filteredNotificationList.add(notification);
                }
            }
        } else if (chipId == R.id.chip_payments) {
            for (VendorNotification notification : notificationList) {
                if ("PAYMENT".equals(notification.getType())) {
                    filteredNotificationList.add(notification);
                }
            }
        } else if (chipId == R.id.chip_system) {
            for (VendorNotification notification : notificationList) {
                if ("SYSTEM".equals(notification.getType())) {
                    filteredNotificationList.add(notification);
                }
            }
        }
        
        updateUI();
    }

    private void markAllAsRead() {
        for (VendorNotification notification : notificationList) {
            notification.setRead(true);
        }
        notificationAdapter.notifyDataSetChanged();
        Toast.makeText(this, "All notifications marked as read", Toast.LENGTH_SHORT).show();
    }

    private void updateUI() {
        if (filteredNotificationList.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
        
        notificationAdapter.notifyDataSetChanged();
        
        // Update mark all read button visibility
        boolean hasUnreadNotifications = false;
        for (VendorNotification notification : filteredNotificationList) {
            if (!notification.isRead()) {
                hasUnreadNotifications = true;
                break;
            }
        }
        btnMarkAllRead.setVisibility(hasUnreadNotifications ? View.VISIBLE : View.GONE);
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
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
