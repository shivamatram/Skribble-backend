package com.example.foodvan.activities.vendor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.adapters.VendorOrderHistoryAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.viewmodels.VendorOrderHistoryViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class VendorOrderHistoryActivity extends AppCompatActivity implements VendorOrderHistoryAdapter.OnOrderActionListener {

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private LinearProgressIndicator progressIndicator;
    private MaterialCardView emptyStateCard;
    private MaterialButton startOrderingButton;
    private RecyclerView ordersRecyclerView;

    // Filter Chips
    private Chip chipAll, chipDelivered, chipOngoing, chipCancelled;

    // ViewModel and Adapter
    private VendorOrderHistoryViewModel viewModel;
    private VendorOrderHistoryAdapter adapter;
    private List<Order> allOrders = new ArrayList<>();
    private List<Order> filteredOrders = new ArrayList<>();

    // Current filter state
    private String currentFilter = "All";
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initializeViewModel();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        setupSwipeRefresh();
        observeViewModel();
        loadOrders();
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(VendorOrderHistoryViewModel.class);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        // searchEditText = findViewById(R.id.et_search); // Not in layout
        filterChipGroup = findViewById(R.id.chip_group_filter);
        progressIndicator = findViewById(R.id.progress_indicator);
        emptyStateCard = findViewById(R.id.card_empty_state);
        startOrderingButton = findViewById(R.id.btn_start_ordering);
        ordersRecyclerView = findViewById(R.id.rv_orders);

        // Filter chips
        chipAll = findViewById(R.id.chip_all);
        chipDelivered = findViewById(R.id.chip_delivered);
        chipOngoing = findViewById(R.id.chip_ongoing);
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
        adapter = new VendorOrderHistoryAdapter(this, filteredOrders, this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality - commented out since no search field in layout
        /*
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    applyFilters();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
        */

        // Filter chips
        if (filterChipGroup != null) {
            filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (!checkedIds.isEmpty()) {
                    Chip selectedChip = findViewById(checkedIds.get(0));
                    if (selectedChip != null) {
                        currentFilter = selectedChip.getText().toString();
                        applyFilters();
                    }
                }
            });
        }

        // Start ordering button - commented out since not in layout
        /*
        if (startOrderingButton != null) {
            startOrderingButton.setOnClickListener(v -> {
                // Navigate back to dashboard or main activity
                finish();
            });
        }
        */
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadOrders();
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressIndicator.setVisibility(View.VISIBLE);
            } else {
                progressIndicator.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        viewModel.getOrders().observe(this, orders -> {
            if (orders != null) {
                allOrders.clear();
                allOrders.addAll(orders);
                applyFilters();
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showToast(error);
                updateEmptyState();
            }
        });
    }

    private void loadOrders() {
        viewModel.loadVendorOrders();
    }

    private void applyFilters() {
        filteredOrders.clear();

        for (Order order : allOrders) {
            if (order == null) continue;
            
            // Null-safe search matching
            String customerName = order.getCustomerName() != null ? order.getCustomerName() : "";
            String orderId = order.getOrderId() != null ? order.getOrderId() : "";
            String status = order.getStatus() != null ? order.getStatus() : "";
            
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    customerName.toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    orderId.toLowerCase().contains(currentSearchQuery.toLowerCase());

            boolean matchesFilter = currentFilter.equals("All") ||
                    (currentFilter.equals("Completed") && status.equalsIgnoreCase("delivered")) ||
                    (currentFilter.equals("Pending") && isOngoingOrder(status)) ||
                    (currentFilter.equals("Cancelled") && status.equalsIgnoreCase("cancelled"));

            if (matchesSearch && matchesFilter) {
                filteredOrders.add(order);
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateEmptyState();
    }

    private boolean isOngoingOrder(String status) {
        if (status == null) return false;
        return status.equalsIgnoreCase("pending") ||
               status.equalsIgnoreCase("accepted") ||
               status.equalsIgnoreCase("preparing") ||
               status.equalsIgnoreCase("ready") ||
               status.equalsIgnoreCase("out_for_delivery");
    }

    private void updateEmptyState() {
        // Find empty state layout from the actual layout
        View emptyStateLayout = findViewById(R.id.empty_state_layout);
        
        if (filteredOrders.isEmpty()) {
            if (emptyStateLayout != null) {
                emptyStateLayout.setVisibility(View.VISIBLE);
            }
            if (ordersRecyclerView != null) {
                ordersRecyclerView.setVisibility(View.GONE);
            }
        } else {
            if (emptyStateLayout != null) {
                emptyStateLayout.setVisibility(View.GONE);
            }
            if (ordersRecyclerView != null) {
                ordersRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onViewOrderDetails(Order order) {
        // Navigate to order details screen
        showToast("View details for order: " + order.getOrderId());
        // TODO: Implement navigation to order details
    }

    @Override
    public void onReorderClicked(Order order) {
        // Handle reorder functionality
        showToast("Reorder functionality - Coming Soon");
        // TODO: Implement reorder functionality
    }

    @Override
    public void onRateOrder(Order order) {
        // Handle rating functionality
        showToast("Rating functionality - Coming Soon");
        // TODO: Implement rating dialog
    }

    @Override
    public void onTrackOrder(Order order) {
        // Handle order tracking
        showToast("Track order: " + order.getOrderId());
        // TODO: Implement order tracking
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
