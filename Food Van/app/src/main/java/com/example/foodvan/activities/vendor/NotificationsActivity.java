package com.example.foodvan.activities.vendor;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.NotificationsAdapter;
import com.example.foodvan.models.Notification;
import com.example.foodvan.viewmodels.NotificationsViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * NotificationsActivity - Professional notifications screen for vendors
 * Features: Real-time notifications, mark as read, delete, clear all
 */
public class NotificationsActivity extends AppCompatActivity implements 
        NotificationsAdapter.OnNotificationClickListener,
        NotificationsAdapter.OnNotificationLongClickListener {

    // UI Components
    private MaterialToolbar toolbar;
    private RecyclerView rvNotifications;
    private View layoutNotificationsContent;
    private View layoutEmptyState;
    private View layoutLoading;
    private View layoutError;
    private View layoutClearAll;
    private MaterialButton btnClearAll;
    private MaterialButton btnRetry;

    // ViewModel and Adapter
    private NotificationsViewModel viewModel;
    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_notifications);
        
        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        
        // Load notifications
        viewModel.refreshNotifications();
    }

    /**
     * Setup orange status bar to match the navbar theme with seamless connection
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.primary_color, getTheme()));
            
            // Make status bar icons light (white) for dark orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(0); // Clear light status bar flag
            }
            
            // Ensure proper spacing - don't fit system windows
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.setDecorFitsSystemWindows(false);
            }
        }
    }

    /**
     * Initialize all views
     */
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        rvNotifications = findViewById(R.id.rv_notifications);
        layoutNotificationsContent = findViewById(R.id.layout_notifications_content);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        layoutLoading = findViewById(R.id.layout_loading);
        layoutError = findViewById(R.id.layout_error);
        layoutClearAll = findViewById(R.id.layout_clear_all);
        btnClearAll = findViewById(R.id.btn_clear_all);
        btnRetry = findViewById(R.id.btn_retry);
    }

    /**
     * Setup toolbar with back navigation
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Notifications");
        }
    }

    /**
     * Setup RecyclerView with adapter
     */
    private void setupRecyclerView() {
        adapter = new NotificationsAdapter(this);
        adapter.setOnNotificationClickListener(this);
        adapter.setOnNotificationLongClickListener(this);
        
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
        rvNotifications.setHasFixedSize(true);
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        btnClearAll.setOnClickListener(v -> showClearAllConfirmation());
        btnRetry.setOnClickListener(v -> viewModel.refreshNotifications());
    }

    /**
     * Setup observers for ViewModel LiveData
     */
    private void setupObservers() {
        // Observe notifications list
        viewModel.getNotifications().observe(this, this::updateNotificationsList);
        
        // Observe loading state
        viewModel.getIsLoading().observe(this, this::updateLoadingState);
        
        // Observe error messages
        viewModel.getErrorMessage().observe(this, this::handleError);
        
        // Observe unread count
        viewModel.getUnreadCount().observe(this, this::updateUnreadCount);
    }

    /**
     * Update notifications list in adapter
     */
    private void updateNotificationsList(List<Notification> notifications) {
        if (notifications == null || notifications.isEmpty()) {
            showEmptyState();
        } else {
            showNotificationsList();
            adapter.updateNotifications(notifications);
        }
    }

    /**
     * Update loading state
     */
    private void updateLoadingState(Boolean isLoading) {
        if (isLoading != null && isLoading) {
            showLoadingState();
        }
    }

    /**
     * Handle error messages
     */
    private void handleError(String errorMessage) {
        if (errorMessage != null && !errorMessage.isEmpty()) {
            showErrorState();
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Update unread count and clear all button visibility
     */
    private void updateUnreadCount(Integer count) {
        if (count != null && count > 0) {
            layoutClearAll.setVisibility(View.VISIBLE);
        } else {
            layoutClearAll.setVisibility(View.GONE);
        }
    }

    /**
     * Show notifications list
     */
    private void showNotificationsList() {
        layoutNotificationsContent.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    /**
     * Show empty state
     */
    private void showEmptyState() {
        layoutNotificationsContent.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    /**
     * Show loading state
     */
    private void showLoadingState() {
        layoutNotificationsContent.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
    }

    /**
     * Show error state
     */
    private void showErrorState() {
        layoutNotificationsContent.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
    }

    /**
     * Show confirmation dialog for clearing all notifications
     */
    private void showClearAllConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Clear All Notifications")
                .setMessage("Are you sure you want to clear all notifications? This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    viewModel.clearAllNotifications();
                    Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Show options dialog for notification actions
     */
    private void showNotificationOptionsDialog(Notification notification, int position) {
        String[] options = notification.isRead() ? 
                new String[]{"Delete"} : 
                new String[]{"Mark as Read", "Delete"};
        
        new AlertDialog.Builder(this)
                .setTitle("Notification Options")
                .setItems(options, (dialog, which) -> {
                    if (notification.isRead()) {
                        // Only delete option
                        deleteNotification(notification, position);
                    } else {
                        // Mark as read or delete
                        if (which == 0) {
                            markAsRead(notification, position);
                        } else {
                            deleteNotification(notification, position);
                        }
                    }
                })
                .show();
    }

    /**
     * Mark notification as read
     */
    private void markAsRead(Notification notification, int position) {
        viewModel.markAsRead(notification.getId());
        adapter.markAsRead(position);
        Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
    }

    /**
     * Delete notification
     */
    private void deleteNotification(Notification notification, int position) {
        viewModel.deleteNotification(notification.getId());
        adapter.removeNotification(position);
        Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
    }

    // NotificationsAdapter.OnNotificationClickListener implementation
    @Override
    public void onNotificationClick(Notification notification, int position) {
        // Mark as read when clicked
        if (!notification.isRead()) {
            markAsRead(notification, position);
        }
        
        // Handle notification-specific actions
        handleNotificationAction(notification);
    }

    // NotificationsAdapter.OnNotificationLongClickListener implementation
    @Override
    public void onNotificationLongClick(Notification notification, int position) {
        showNotificationOptionsDialog(notification, position);
    }

    /**
     * Handle notification-specific actions (deep linking, etc.)
     */
    private void handleNotificationAction(Notification notification) {
        switch (notification.getType()) {
            case Notification.TYPE_ORDER:
                // Navigate to order details if order ID is available
                if (notification.getOrderId() != null) {
                    Toast.makeText(this, "Opening order #" + notification.getOrderId(), Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to order details activity
                }
                break;
            case Notification.TYPE_PAYMENT:
                // Navigate to payments/earnings screen
                Toast.makeText(this, "Opening payments screen", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to payments activity
                break;
            case Notification.TYPE_MENU:
                // Navigate to menu management
                Toast.makeText(this, "Opening menu management", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to menu activity
                break;
            case Notification.TYPE_PROFILE:
                // Navigate to profile settings
                Toast.makeText(this, "Opening profile settings", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to profile activity
                break;
            default:
                // Default action - just show the notification was tapped
                Toast.makeText(this, "Notification opened", Toast.LENGTH_SHORT).show();
                break;
        }
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
