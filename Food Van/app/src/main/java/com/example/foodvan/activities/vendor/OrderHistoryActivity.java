package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.adapters.OrderHistoryAdapter;
import com.example.foodvan.models.Order;
// import com.example.foodvan.viewmodels.OrderHistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity implements OrderHistoryAdapter.OnOrderActionListener {

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView ordersRecyclerView;
    private LinearLayout emptyStateLayout;
    private LinearProgressIndicator progressIndicator;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipCompleted, chipPending, chipCancelled;

    // ViewModel and Adapter
    // private OrderHistoryViewModel viewModel;
    private OrderHistoryAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();

    // Filter state
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_order_history);

        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupFilterChips();
        setupViewModel();
        loadOrderHistory();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        ordersRecyclerView = findViewById(R.id.rv_orders);
        emptyStateLayout = findViewById(R.id.card_empty_state);
        progressIndicator = findViewById(R.id.progress_indicator);
        
        // Filter chips
        filterChipGroup = findViewById(R.id.chip_group_filter);
        chipAll = findViewById(R.id.chip_all);
        chipCompleted = findViewById(R.id.chip_delivered);
        chipPending = findViewById(R.id.chip_ongoing);
        chipCancelled = findViewById(R.id.chip_cancelled);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Order History");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        if (ordersRecyclerView != null) {
            adapter = new OrderHistoryAdapter(this, filteredOrders, this);
            ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            ordersRecyclerView.setAdapter(adapter);
        }
        
        // Setup swipe refresh
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshOrders);
        }
    }

    private void setupFilterChips() {
        if (filterChipGroup != null) {
            filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    int checkedId = checkedIds.get(0);
                    String filter = "ALL";
                    
                    if (checkedId == R.id.chip_all) {
                        filter = "ALL";
                    } else if (checkedId == R.id.chip_delivered) {
                        filter = "COMPLETED";
                    } else if (checkedId == R.id.chip_ongoing) {
                        filter = "PENDING";
                    } else if (checkedId == R.id.chip_cancelled) {
                        filter = "CANCELLED";
                    }
                    
                    applyFilter(filter);
                }
            });
        }
    }

    private void setupViewModel() {
        // viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);
        
        // TODO: Implement ViewModel integration
        // Observe orders
        // viewModel.getOrders().observe(this, orders -> {
        //     if (orders != null) {
        //         allOrders.clear();
        //         allOrders.addAll(orders);
        //         applyFilter(currentFilter);
        //     }
        // });
        
        // Observe loading state
        // viewModel.getIsLoading().observe(this, isLoading -> {
        //     if (progressIndicator != null) {
        //         progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        //     }
        //     if (swipeRefreshLayout != null) {
        //         swipeRefreshLayout.setRefreshing(isLoading);
        //     }
        // });
        
        // Observe error messages
        // viewModel.getErrorMessage().observe(this, error -> {
        //     if (error != null && !error.isEmpty()) {
        //         showToast(error);
        //     }
        // });
    }

    private void loadOrderHistory() {
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        
        // TODO: Load orders from repository/Firebase
        // if (viewModel != null) {
        //     viewModel.loadOrders();
        // }
        
        // For now, create sample data
        createSampleOrders();
    }

    private void refreshOrders() {
        // TODO: Refresh orders from repository/Firebase
        // if (viewModel != null) {
        //     viewModel.refreshOrders();
        // }
        
        // For now, recreate sample data
        createSampleOrders();
    }

    private void createSampleOrders() {
        // Create sample orders for testing
        allOrders.clear();
        
        // Sample Order 1
        Order order1 = new Order();
        order1.setId("ORD12489");
        order1.setCustomerName("Rajesh Kumar");
        order1.setStatus("COMPLETED");
        order1.setPaymentStatus("Paid");
        order1.setTotalAmount(245.0);
        order1.setOrderTime(System.currentTimeMillis() - 3600000); // 1 hour ago
        allOrders.add(order1);
        
        // Sample Order 2
        Order order2 = new Order();
        order2.setId("ORD12490");
        order2.setCustomerName("Priya Sharma");
        order2.setStatus("PENDING");
        order2.setPaymentStatus("COD");
        order2.setTotalAmount(180.0);
        order2.setOrderTime(System.currentTimeMillis() - 1800000); // 30 minutes ago
        allOrders.add(order2);
        
        // Sample Order 3
        Order order3 = new Order();
        order3.setId("ORD12491");
        order3.setCustomerName("Amit Singh");
        order3.setStatus("CANCELLED");
        order3.setPaymentStatus("Refunded");
        order3.setTotalAmount(320.0);
        order3.setOrderTime(System.currentTimeMillis() - 7200000); // 2 hours ago
        allOrders.add(order3);
        
        // Apply current filter
        applyFilter(currentFilter);
        
        // Hide loading indicator
        if (progressIndicator != null) {
            progressIndicator.setVisibility(View.GONE);
        }
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredOrders.clear();
        
        if ("ALL".equals(filter)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (filter.equals(order.getStatus())) {
                    filteredOrders.add(order);
                }
            }
        }
        
        updateUI();
    }

    private void updateUI() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        
        // Show/hide empty state
        if (emptyStateLayout != null && ordersRecyclerView != null) {
            if (filteredOrders.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                ordersRecyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                ordersRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Interface implementation for OrderHistoryAdapter
    @Override
    public void onViewOrderDetails(Order order) {
        Intent intent = new Intent(this, OrderDetailActivity.class);
        intent.putExtra("order_id", order.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
