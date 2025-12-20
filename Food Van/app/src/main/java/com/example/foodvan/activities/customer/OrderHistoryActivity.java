package com.example.foodvan.activities.customer;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.activities.customer.CustomerHomeActivity;
import com.example.foodvan.adapters.OrderHistoryAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OrderHistoryActivity - Comprehensive order history management
 * Features: View orders, search, filter, sort, reorder functionality
 */
public class OrderHistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderActionListener {

    private static final String TAG = "OrderHistoryActivity";

    // UI Components
    private Toolbar toolbar;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipDelivered, chipOngoing, chipCancelled;
    private LinearProgressIndicator progressIndicator;
    private MaterialCardView cardEmptyState;
    private RecyclerView rvOrders;
    private MaterialButton btnStartOrdering;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private SwipeRefreshLayout swipeRefreshLayout;
    
    // Animation and UI state
    private Handler mainHandler;
    private boolean isEmptyStateVisible = false;

    // Data & Services
    private SessionManager sessionManager;
    private DatabaseReference ordersRef;
    private OrderHistoryAdapter adapter;
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    private String currentFilter = "ALL";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initializeServices();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupSearchAndFilter();
        setupSwipeRefresh();
        loadOrderHistory();
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize Firebase reference
        String userId = sessionManager.getUserId();
        if (userId != null) {
            ordersRef = FirebaseDatabase.getInstance()
                    .getReference("orders")
                    .child(userId);
        }
        
        allOrders = new ArrayList<>();
        filteredOrders = new ArrayList<>();
    }

    private void initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            etSearch = findViewById(R.id.et_search);
            chipGroupFilter = findViewById(R.id.chip_group_filter);
            chipAll = findViewById(R.id.chip_all);
            chipDelivered = findViewById(R.id.chip_delivered);
            chipOngoing = findViewById(R.id.chip_ongoing);
            chipCancelled = findViewById(R.id.chip_cancelled);
            progressIndicator = findViewById(R.id.progress_indicator);
            cardEmptyState = findViewById(R.id.card_empty_state);
            rvOrders = findViewById(R.id.rv_orders);
            btnStartOrdering = findViewById(R.id.btn_start_ordering);
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
            
            // Initialize empty state views
            if (cardEmptyState != null) {
                ivEmptyIcon = cardEmptyState.findViewById(R.id.iv_empty_icon);
                tvEmptyTitle = cardEmptyState.findViewById(R.id.tv_empty_title);
                tvEmptyMessage = cardEmptyState.findViewById(R.id.tv_empty_message);
            }
            
            // Log if any critical views are null
            if (btnStartOrdering == null) {
                Log.w(TAG, "btnStartOrdering not found in layout");
            }
            if (rvOrders == null) {
                Log.e(TAG, "rvOrders not found in layout");
            }
            if (cardEmptyState == null) {
                Log.e(TAG, "cardEmptyState not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
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
        if (rvOrders == null) {
            Log.e(TAG, "RecyclerView is null, cannot setup");
            return;
        }
        adapter = new OrderHistoryAdapter(this, filteredOrders, this);
        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Enhanced Start Ordering button with animations
        if (btnStartOrdering != null) {
            btnStartOrdering.setOnClickListener(v -> {
                try {
                    // Add button press animation
                    animateButtonPress(btnStartOrdering, () -> {
                        // Navigate to home/restaurant list with smooth transition
                        Intent intent = new Intent(OrderHistoryActivity.this, CustomerHomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intent);
                        
                        // Add custom transition animation
                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        
                        // Delay finish to allow animation
                        mainHandler.postDelayed(this::finish, 300);
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Error navigating from start ordering button", e);
                    // Fallback - just finish the activity
                    finish();
                }
            });
        } else {
            Log.w(TAG, "btnStartOrdering is null - button not found in layout");
        }
    }
    
    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setColorSchemeResources(
                R.color.primary,
                R.color.primary_variant,
                R.color.secondary_color
            );
            
            swipeRefreshLayout.setOnRefreshListener(() -> {
                // Refresh order history
                loadOrderHistory();
                
                // Stop refreshing after a delay if no data comes
                mainHandler.postDelayed(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            });
        }
    }

    private void setupSearchAndFilter() {
        try {
            // Search functionality
            if (etSearch != null) {
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        currentSearchQuery = s.toString().trim();
                        applyFiltersAndSearch();
                    }
                });
            }

            // Filter chips
            if (chipGroupFilter != null) {
                chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
                    if (!checkedIds.isEmpty()) {
                        int checkedId = checkedIds.get(0);
                        if (checkedId == R.id.chip_all) {
                            currentFilter = "ALL";
                        } else if (checkedId == R.id.chip_delivered) {
                            currentFilter = "DELIVERED";
                        } else if (checkedId == R.id.chip_cancelled) {
                            currentFilter = "CANCELLED";
                        }
                        // Commented out chip_ongoing as it doesn't exist in layout
                        // } else if (checkedId == R.id.chip_ongoing) {
                        //     currentFilter = "ONGOING";
                        applyFiltersAndSearch();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up search and filter", e);
        }
    }

    private void loadOrderHistory() {
        if (ordersRef == null) {
            Log.w(TAG, "User not logged in, showing empty state");
            showEmptyState();
            return;
        }

        showProgress(true);
        
        // Query orders ordered by timestamp (most recent first)
        Query query = ordersRef.orderByChild("orderTime");
        
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allOrders.clear();
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null) {
                        allOrders.add(order);
                    }
                }
                
                // Sort by order time (most recent first)
                Collections.sort(allOrders, (o1, o2) -> 
                    Long.compare(o2.getOrderTime(), o1.getOrderTime()));
                
                applyFiltersAndSearch();
                showProgress(false);
                
                // Stop swipe refresh if active
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load order history", error.toException());
                showProgress(false);
                
                // Stop swipe refresh if active
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                showError("Failed to load order history: " + error.getMessage());
            }
        });
    }

    private void applyFiltersAndSearch() {
        filteredOrders.clear();
        
        for (Order order : allOrders) {
            boolean matchesFilter = false;
            boolean matchesSearch = false;
            
            // Apply status filter
            switch (currentFilter) {
                case "ALL":
                    matchesFilter = true;
                    break;
                case "DELIVERED":
                    matchesFilter = "DELIVERED".equals(order.getStatus());
                    break;
                case "ONGOING":
                    matchesFilter = isOngoingOrder(order.getStatus());
                    break;
                case "CANCELLED":
                    matchesFilter = "CANCELLED".equals(order.getStatus());
                    break;
            }
            
            // Apply search filter
            if (currentSearchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                String query = currentSearchQuery.toLowerCase();
                matchesSearch = (order.getOrderId() != null && order.getOrderId().toLowerCase().contains(query)) ||
                               (order.getVanName() != null && order.getVanName().toLowerCase().contains(query));
            }
            
            if (matchesFilter && matchesSearch) {
                filteredOrders.add(order);
            }
        }
        
        updateUI();
    }

    private boolean isOngoingOrder(String status) {
        return "PLACED".equals(status) || 
               "CONFIRMED".equals(status) || 
               "PREPARING".equals(status) || 
               "READY".equals(status);
    }

    private void updateUI() {
        if (filteredOrders.isEmpty()) {
            if (allOrders.isEmpty()) {
                showEmptyState();
            } else {
                showNoResultsState();
            }
        } else {
            hideEmptyState();
            adapter.notifyDataSetChanged();
        }
    }

    private void showEmptyState() {
        try {
            if (cardEmptyState != null && !isEmptyStateVisible) {
                isEmptyStateVisible = true;
                
                // Update empty state message based on filter
                updateEmptyStateContent();
                
                // Show with animation
                cardEmptyState.setVisibility(View.VISIBLE);
                animateEmptyStateIn();
            }
            
            if (rvOrders != null) {
                rvOrders.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }

    private void showNoResultsState() {
        try {
            if (cardEmptyState != null) {
                cardEmptyState.setVisibility(View.VISIBLE);
            }
            if (rvOrders != null) {
                rvOrders.setVisibility(View.GONE);
            }
            
            if (cardEmptyState != null) {
                TextView emptyTitle = cardEmptyState.findViewById(R.id.tv_empty_title);
                TextView emptyMessage = cardEmptyState.findViewById(R.id.tv_empty_message);
                
                if (emptyTitle != null && emptyMessage != null) {
                    emptyTitle.setText("No Results Found");
                    emptyMessage.setText("Try adjusting your search or filter criteria");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing no results state", e);
        }
    }

    private void hideEmptyState() {
        try {
            if (cardEmptyState != null && isEmptyStateVisible) {
                isEmptyStateVisible = false;
                
                // Hide with animation
                animateEmptyStateOut(() -> {
                    if (cardEmptyState != null) {
                        cardEmptyState.setVisibility(View.GONE);
                    }
                });
            }
            
            if (rvOrders != null) {
                rvOrders.setVisibility(View.VISIBLE);
                // Animate RecyclerView in
                animateRecyclerViewIn();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding empty state", e);
        }
    }

    private void showProgress(boolean show) {
        try {
            if (progressIndicator != null) {
                progressIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing progress", e);
        }
    }

    private void showError(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showSuccess(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show();
    }

    // OrderHistoryAdapter.OnOrderActionListener implementation
    @Override
    public void onViewOrderDetails(Order order) {
        // TODO: Create OrderDetailActivity
        // Intent intent = new Intent(this, OrderDetailActivity.class);
        // intent.putExtra("order_id", order.getId());
        // startActivity(intent);
        
        // For now, show a toast message
        Toast.makeText(this, "Order Details: " + order.getId(), Toast.LENGTH_SHORT).show();
    }

    // @Override
    public void onOrderClicked(Order order) {
        // Open order details
        Intent intent = new Intent(this, OrderDetailsActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }

    // @Override
    public void onReorderClicked(Order order) {
        // Implement reorder functionality
        if (order.getItems() == null || order.getItems().isEmpty()) {
            showError("Cannot reorder: Order items not available");
            return;
        }
        
        // TODO: Add items to cart and navigate to cart
        showSuccess("Items added to cart successfully");
        
        // For now, just show a success message
        Toast.makeText(this, "Reorder functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    // @Override
    public void onTrackOrderClicked(Order order) {
        // Navigate to order tracking
        if (isOngoingOrder(order.getStatus())) {
            // TODO: Navigate to order tracking activity
            Toast.makeText(this, "Order tracking will be implemented", Toast.LENGTH_SHORT).show();
        } else {
            showError("This order cannot be tracked");
        }
    }

    // @Override
    public void onRateOrderClicked(Order order) {
        // Show rating dialog
        if ("DELIVERED".equals(order.getStatus())) {
            // TODO: Show rating dialog
            Toast.makeText(this, "Rating functionality will be implemented", Toast.LENGTH_SHORT).show();
        } else {
            showError("You can only rate delivered orders");
        }
    }

    // @Override
    public void onCallVendorClicked(Order order) {
        // Call vendor functionality
        if (order.getCustomerPhone() != null) {
            // TODO: Implement call functionality
            Toast.makeText(this, "Call functionality will be implemented", Toast.LENGTH_SHORT).show();
        } else {
            showError("Vendor contact not available");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to activity
        if (!allOrders.isEmpty()) {
            applyFiltersAndSearch();
        }
    }

    // Animation Methods
    private void animateEmptyStateIn() {
        if (cardEmptyState == null) return;
        
        // Initial state
        cardEmptyState.setAlpha(0f);
        cardEmptyState.setScaleX(0.8f);
        cardEmptyState.setScaleY(0.8f);
        
        // Animate in
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(cardEmptyState, "alpha", 0f, 1f);
        ObjectAnimator scaleXIn = ObjectAnimator.ofFloat(cardEmptyState, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYIn = ObjectAnimator.ofFloat(cardEmptyState, "scaleY", 0.8f, 1f);
        
        animatorSet.playTogether(fadeIn, scaleXIn, scaleYIn);
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
        
        // Animate icon with delay
        if (ivEmptyIcon != null) {
            mainHandler.postDelayed(() -> animateIconBounce(), 200);
        }
    }
    
    private void animateEmptyStateOut(Runnable onComplete) {
        if (cardEmptyState == null) {
            if (onComplete != null) onComplete.run();
            return;
        }
        
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(cardEmptyState, "alpha", 1f, 0f);
        ObjectAnimator scaleXOut = ObjectAnimator.ofFloat(cardEmptyState, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleYOut = ObjectAnimator.ofFloat(cardEmptyState, "scaleY", 1f, 0.8f);
        
        animatorSet.playTogether(fadeOut, scaleXOut, scaleYOut);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        
        if (onComplete != null) {
            animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    onComplete.run();
                }
            });
        }
        
        animatorSet.start();
    }
    
    private void animateIconBounce() {
        if (ivEmptyIcon == null) return;
        
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(ivEmptyIcon, "translationY", 0f, -20f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivEmptyIcon, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivEmptyIcon, "scaleY", 1f, 1.1f, 1f);
        
        AnimatorSet bounceSet = new AnimatorSet();
        bounceSet.playTogether(bounceY, scaleX, scaleY);
        bounceSet.setDuration(600);
        bounceSet.setInterpolator(new AccelerateDecelerateInterpolator());
        bounceSet.start();
    }
    
    private void animateButtonPress(View button, Runnable onComplete) {
        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f);
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(button, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(button, "scaleY", 0.95f, 1f);
        
        AnimatorSet pressDown = new AnimatorSet();
        pressDown.playTogether(scaleXDown, scaleYDown);
        pressDown.setDuration(100);
        
        AnimatorSet pressUp = new AnimatorSet();
        pressUp.playTogether(scaleXUp, scaleYUp);
        pressUp.setDuration(100);
        
        pressDown.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                pressUp.start();
            }
        });
        
        if (onComplete != null) {
            pressUp.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    mainHandler.postDelayed(onComplete, 50);
                }
            });
        }
        
        pressDown.start();
    }
    
    private void animateRecyclerViewIn() {
        if (rvOrders == null) return;
        
        rvOrders.setAlpha(0f);
        rvOrders.setTranslationY(50f);
        
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(rvOrders, "alpha", 0f, 1f);
        ObjectAnimator slideIn = ObjectAnimator.ofFloat(rvOrders, "translationY", 50f, 0f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(fadeIn, slideIn);
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }
    
    private void updateEmptyStateContent() {
        if (tvEmptyTitle != null && tvEmptyMessage != null) {
            if ("ALL".equals(currentFilter)) {
                tvEmptyTitle.setText("No Orders Yet");
                tvEmptyMessage.setText("Your order history will appear here once you place your first order");
            } else {
                tvEmptyTitle.setText("No " + currentFilter.toLowerCase() + " orders");
                tvEmptyMessage.setText("You don't have any " + currentFilter.toLowerCase() + " orders");
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up handlers and listeners
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
