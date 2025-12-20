package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.AllOrdersAdapter;
import com.example.foodvan.models.Order;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllOrdersActivity extends AppCompatActivity implements AllOrdersAdapter.OnOrderClickListener {

    private MaterialToolbar toolbar;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private RecyclerView ordersRecyclerView;
    private LinearLayout emptyStateLayout;
    private MaterialButton backToDashboardButton;

    private AllOrdersAdapter ordersAdapter;
    private List<Order> allOrders;
    private List<Order> filteredOrders;
    private String currentFilter = "All";

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String vendorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_all_orders);

        initializeComponents();
        initializeViews();
        setupClickListeners();
        setupRecyclerView();
        setupSearch();
        setupFilters();
        loadOrders();
    }

    private void initializeComponents() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        vendorId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";
        
        allOrders = new ArrayList<>();
        filteredOrders = new ArrayList<>();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchEditText = findViewById(R.id.search_edit_text);
        filterChipGroup = findViewById(R.id.filter_chip_group);
        ordersRecyclerView = findViewById(R.id.orders_recycler_view);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        backToDashboardButton = findViewById(R.id.back_to_dashboard_button);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupClickListeners() {
        toolbar.setNavigationOnClickListener(v -> finish());
        
        backToDashboardButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        ordersAdapter = new AllOrdersAdapter(this, filteredOrders, this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ordersRecyclerView.setAdapter(ordersAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterOrders(s.toString(), currentFilter);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                currentFilter = selectedChip.getText().toString();
                String searchQuery = searchEditText.getText() != null ? searchEditText.getText().toString() : "";
                filterOrders(searchQuery, currentFilter);
            }
        });
    }

    private void loadOrders() {
        if (vendorId.isEmpty()) {
            showToast("Authentication error. Please login again.");
            return;
        }

        db.collection("orders")
                .whereEqualTo("vendorId", vendorId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allOrders.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        allOrders.add(order);
                    }
                    
                    String searchQuery = searchEditText.getText() != null ? searchEditText.getText().toString() : "";
                    filterOrders(searchQuery, currentFilter);
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to load orders: " + e.getMessage());
                    updateEmptyState();
                });
    }

    private void filterOrders(String searchQuery, String statusFilter) {
        filteredOrders.clear();
        
        for (Order order : allOrders) {
            boolean matchesSearch = searchQuery.isEmpty() || 
                    order.getCustomerName().toLowerCase().contains(searchQuery.toLowerCase()) ||
                    order.getOrderId().toLowerCase().contains(searchQuery.toLowerCase());
            
            boolean matchesStatus = statusFilter.equals("All") || 
                    order.getStatus().equalsIgnoreCase(statusFilter);
            
            if (matchesSearch && matchesStatus) {
                filteredOrders.add(order);
            }
        }
        
        ordersAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredOrders.isEmpty()) {
            ordersRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            ordersRecyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onOrderClick(Order order) {
        // Navigate to order details - placeholder for now
        showToast("Order details for " + order.getOrderId());
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
    public void onViewOrderDetails(Order order) {
        // Navigate to detailed order view - placeholder for now
        showToast("Viewing details for order " + order.getOrderId());
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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
