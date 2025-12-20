package com.example.foodvan.activities.vendor;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.navigation.NavigationView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// New activity imports for dashboard features
import com.example.foodvan.activities.vendor.AllOrdersActivity;
import com.example.foodvan.activities.vendor.EarningDetailsActivity;
import com.example.foodvan.activities.vendor.AddFoodItemActivity;
import com.example.foodvan.activities.vendor.VendorOrderHistoryActivity;
import com.example.foodvan.activities.vendor.ManageMenuActivity;
import com.example.foodvan.activities.vendor.CustomerChatActivity;
import com.example.foodvan.activities.vendor.VendorMenuManagementActivity;
import com.example.foodvan.activities.vendor.AddEditMenuItemActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.foodvan.R;
import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.adapters.PendingOrdersAdapter;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.Vendor;
import com.example.foodvan.utils.SessionManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * VendorDashboardActivity - Complete Material UI dashboard for food van vendors
 * Features: Real-time orders, earnings analytics, location tracking, profile management
 */
public class VendorDashboardActivity extends AppCompatActivity implements 
        NavigationView.OnNavigationItemSelectedListener {

    // Navigation Drawer Components
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    
    // UI Components
    private MaterialToolbar toolbar;
    private ShapeableImageView vendorAvatar;
    private TextView vendorName, vendorLocation, vendorStatusText;
    private SwitchMaterial vendorStatusSwitch;
    private TextView totalOrdersCount, pendingOrdersCount, completedOrdersCount;
    private TextView todayEarnings, weeklyEarnings, monthlyEarnings;
    private MaterialButton btnAddFoodItem, btnOrderHistory, btnManageMenu, btnStartMessaging;
    private TextView viewAllOrders, viewEarningsDetails;
    
    // Navigation Drawer Components
    private Chip chipVendorStatus;
    private TextView navVendorName, navVendorEmail;
    private ShapeableImageView navVendorAvatar;
    private ImageButton btnNavSettings;

    // Optional UI Components (may not exist in all layouts)
    private TextView vanName;
    private SwitchMaterial onlineSwitch;
    private TextView todayOrdersCount;
    private Chip statusChip;
    private MaterialCardView menuManagementCard, profileManagementCard, analyticsCard, locationCard;
    private ExtendedFloatingActionButton addMenuItemFab;
    private MaterialButton viewAllOrdersBtn;
    private RecyclerView pendingOrdersRecyclerView;

    // Firebase Components
    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef, ordersRef, earningsRef;
    private String vendorId;

    // Data Components
    private SessionManager sessionManager;
    private PendingOrdersAdapter pendingOrdersAdapter;
    private List<Order> pendingOrdersList;
    private Vendor currentVendor;

    // Location Components
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Analytics Data
    private int totalOrdersToday = 0;
    private double totalEarningsToday = 0.0;
    private int pendingOrdersCountValue = 0;

    // Animation Handler
    private Handler animationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar
        setupStatusBar();
        
        setContentView(R.layout.activity_vendor_dashboard);
        
        initializeComponents();
        initializeViews();
        setupNavigationDrawer();
        setupFirebase();
        setupLocationServices();
        setupClickListeners();
        loadVendorData();
        startRealTimeUpdates();
        setupAnimations();
    }

    /**
     * Setup orange status bar with proper separation to prevent overlap with navbar
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

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        pendingOrdersList = new ArrayList<>();
        animationHandler = new Handler();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        // Get vendor ID from session or Firebase
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            vendorId = currentUser.getUid();
        } else {
            // Redirect to login if not authenticated
            logout();
            return;
        }
    }

    private void initializeViews() {
        // Navigation Drawer Setup
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        
        // Toolbar Setup
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Header Views
        vendorAvatar = findViewById(R.id.vendorAvatar);
        vendorName = findViewById(R.id.vendorName);
        vendorLocation = findViewById(R.id.vendorLocation);
        vendorStatusSwitch = findViewById(R.id.vendorStatusSwitch);
        vendorStatusText = findViewById(R.id.vendorStatusText);
        
        // Navigation Drawer Views
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            View headerView = navigationView.getHeaderView(0);
            if (headerView != null) {
                chipVendorStatus = headerView.findViewById(R.id.chip_vendor_status);
                navVendorName = headerView.findViewById(R.id.tv_vendor_name);
                navVendorEmail = headerView.findViewById(R.id.tv_vendor_email);
                navVendorAvatar = headerView.findViewById(R.id.iv_vendor_avatar);
                btnNavSettings = headerView.findViewById(R.id.btn_nav_settings);
                
                // Setup settings button click listener
                if (btnNavSettings != null) {
                    btnNavSettings.setOnClickListener(v -> openVendorSettings());
                }
            }
        }
        
        // Orders Statistics Views
        totalOrdersCount = findViewById(R.id.totalOrdersCount);
        pendingOrdersCount = findViewById(R.id.pendingOrdersCount);
        completedOrdersCount = findViewById(R.id.completedOrdersCount);
        viewAllOrders = findViewById(R.id.viewAllOrders);
        
        // Earnings Views
        todayEarnings = findViewById(R.id.todayEarnings);
        weeklyEarnings = findViewById(R.id.weeklyEarnings);
        monthlyEarnings = findViewById(R.id.monthlyEarnings);
        viewEarningsDetails = findViewById(R.id.viewEarningsDetails);
        
        // Quick Action Buttons
        btnAddFoodItem = findViewById(R.id.btnAddFoodItem);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnManageMenu = findViewById(R.id.btnManageMenu);
        btnStartMessaging = findViewById(R.id.btnStartMessaging);
        
        // Setup click listeners for new UI
        setupNewClickListeners();
    }

    private void setupNewClickListeners() {
        // Vendor Status Switch
        if (vendorStatusSwitch != null) {
            vendorStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Only update if user triggered the change, not programmatic
                if (buttonView.isPressed()) {
                    updateVendorOnlineStatus(isChecked);
                    animateStatusChange(isChecked);
                }
            });
        }
        
        // View All Orders
        if (viewAllOrders != null) {
            viewAllOrders.setOnClickListener(v -> viewAllOrders());
        }
        
        // View Earnings Details
        if (viewEarningsDetails != null) {
            viewEarningsDetails.setOnClickListener(v -> viewEarningsDetails());
        }
        
        // Quick Action Buttons
        if (btnAddFoodItem != null) {
            btnAddFoodItem.setOnClickListener(v -> addNewFoodItem());
        }
        if (btnOrderHistory != null) {
            btnOrderHistory.setOnClickListener(v -> viewOrderHistory());
        }
        if (btnManageMenu != null) {
            btnManageMenu.setOnClickListener(v -> manageMenu());
        }
        if (btnStartMessaging != null) {
            btnStartMessaging.setOnClickListener(v -> startCustomerMessaging());
        }
    }

    private void setupFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        vendorRef = database.getReference("vendors").child(vendorId);
        ordersRef = database.getReference("orders");
        earningsRef = database.getReference("earnings").child(vendorId);
    }

    private void setupLocationServices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void setupPendingOrdersRecyclerView() {
        pendingOrdersAdapter = new PendingOrdersAdapter(this, pendingOrdersList, 
                new PendingOrdersAdapter.OnOrderActionListener() {
            @Override
            public void onAcceptOrder(Order order) {
                acceptOrder(order);
            }

            @Override
            public void onRejectOrder(Order order) {
                rejectOrder(order);
            }

            @Override
            public void onViewOrderDetails(Order order) {
                viewOrderDetails(order);
            }
        });
        
        // Setup RecyclerView only if it exists in the layout
        if (pendingOrdersRecyclerView != null) {
            pendingOrdersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            pendingOrdersRecyclerView.setAdapter(pendingOrdersAdapter);
            pendingOrdersRecyclerView.setNestedScrollingEnabled(false);
        }
    }

    private void setupNavigationDrawer() {
        // Setup ActionBarDrawerToggle
        drawerToggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        
        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);
        
        // Set default selected item
        navigationView.setCheckedItem(R.id.nav_dashboard);
        
        // Update navigation header with vendor info
        updateNavigationHeader();
    }

    private void updateNavigationHeader() {
        if (currentVendor != null) {
            // Update vendor name
            if (navVendorName != null) {
                String vendorName = currentVendor.getName();
                if (vendorName != null && !vendorName.trim().isEmpty()) {
                    navVendorName.setText(vendorName);
                } else {
                    navVendorName.setText("Vendor Name");
                }
            }
            
            // Update vendor email
            if (navVendorEmail != null) {
                String vendorEmail = currentVendor.getEmail();
                if (vendorEmail != null && !vendorEmail.trim().isEmpty()) {
                    navVendorEmail.setText(vendorEmail);
                } else {
                    navVendorEmail.setText("vendor@foodvan.com");
                }
            }
            
            // Load vendor avatar
            if (navVendorAvatar != null) {
                String avatarUrl = currentVendor.getAvatarUrl();
                if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
                    // Load image from URL using Glide
                    Glide.with(this)
                        .load(avatarUrl)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.ic_account_circle)
                        .error(R.drawable.ic_account_circle)
                        .into(navVendorAvatar);
                } else {
                    // Set default avatar
                    navVendorAvatar.setImageResource(R.drawable.ic_account_circle);
                }
            }
        } else {
            // Set default values when no vendor data
            if (navVendorName != null) {
                navVendorName.setText("Vendor Name");
            }
            if (navVendorEmail != null) {
                navVendorEmail.setText("vendor@foodvan.com");
            }
            if (navVendorAvatar != null) {
                navVendorAvatar.setImageResource(R.drawable.ic_account_circle);
            }
        }
    }

    private void openVendorSettings() {
        try {
            Intent intent = new Intent(this, VendorSettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Error opening settings: " + e.getMessage());
        }
    }

    private void setupClickListeners() {
        // Vendor Status Switch (using the actual switch from layout)
        if (vendorStatusSwitch != null) {
            vendorStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Only update if user triggered the change, not programmatic
                if (buttonView.isPressed()) {
                    updateVendorOnlineStatus(isChecked);
                    animateStatusChange(isChecked);
                }
            });
        }
        
        // Handle onlineSwitch if it exists
        if (onlineSwitch != null) {
            onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Only update if user triggered the change, not programmatic
                if (buttonView.isPressed()) {
                    updateVendorOnlineStatus(isChecked);
                    animateStatusChange(isChecked);
                }
            });
        }

        // Quick Action Buttons (using actual buttons from layout)
        if (btnAddFoodItem != null) {
            btnAddFoodItem.setOnClickListener(v -> addNewFoodItem());
        }
        if (btnOrderHistory != null) {
            btnOrderHistory.setOnClickListener(v -> viewOrderHistory());
        }
        if (btnManageMenu != null) {
            btnManageMenu.setOnClickListener(v -> manageMenu());
        }
        if (btnStartMessaging != null) {
            btnStartMessaging.setOnClickListener(v -> startCustomerMessaging());
        }

        // View All Orders Text
        if (viewAllOrders != null) {
            viewAllOrders.setOnClickListener(v -> viewAllOrders());
        }

        // Optional UI components (may not exist in current layout)
        if (menuManagementCard != null) {
            menuManagementCard.setOnClickListener(v -> openMenuManagement());
        }
        if (profileManagementCard != null) {
            profileManagementCard.setOnClickListener(v -> openProfileManagement());
        }
        if (analyticsCard != null) {
            analyticsCard.setOnClickListener(v -> openAnalytics());
        }
        if (locationCard != null) {
            locationCard.setOnClickListener(v -> openLocationTracking());
        }
        if (addMenuItemFab != null) {
            addMenuItemFab.setOnClickListener(v -> addNewFoodItem());
        }
    }

    private void loadVendorData() {
        vendorRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentVendor = snapshot.getValue(Vendor.class);
                    if (currentVendor != null) {
                        updateVendorUI();
                    }
                } else {
                    createVendorProfile();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error loading vendor data: " + error.getMessage());
            }
        });
    }

    private void updateVendorUI() {
        if (currentVendor != null) {
            // Update vendor name (using actual view from layout)
            if (vendorName != null) {
                vendorName.setText(currentVendor.getName() != null ? currentVendor.getName() : "Vendor Name");
            }
            
            // Update van name (optional view)
            if (vanName != null) {
                vanName.setText(currentVendor.getVanName() != null ? currentVendor.getVanName() : "Food Van Name");
            }
            
            // Update online status using actual switch from layout
            boolean isOnline = currentVendor.isOnline();
            
            // Update switches without triggering listeners
            if (vendorStatusSwitch != null) {
                vendorStatusSwitch.setOnCheckedChangeListener(null);
                vendorStatusSwitch.setChecked(isOnline);
                vendorStatusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        updateVendorOnlineStatus(isChecked);
                        animateStatusChange(isChecked);
                    }
                });
            }
            if (onlineSwitch != null) {
                onlineSwitch.setOnCheckedChangeListener(null);
                onlineSwitch.setChecked(isOnline);
                onlineSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        updateVendorOnlineStatus(isChecked);
                        animateStatusChange(isChecked);
                    }
                });
            }
            
            // Update status text
            updateVendorStatusText(isOnline);
            
            updateStatusChip(isOnline);
            updateNavigationChipStatus(isOnline);
            
            // Update navigation header with vendor info
            updateNavigationHeader();
            
            // Load avatar if available
            if (vendorAvatar != null && currentVendor.getAvatarUrl() != null && !currentVendor.getAvatarUrl().isEmpty()) {
                // Load image using Glide or Picasso
                // Glide.with(this).load(currentVendor.getAvatarUrl()).into(vendorAvatar);
            }
        }
    }

    private void createVendorProfile() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            Vendor newVendor = new Vendor();
            newVendor.setId(vendorId);
            newVendor.setName(user.getDisplayName() != null ? user.getDisplayName() : "New Vendor");
            newVendor.setEmail(user.getEmail());
            newVendor.setVanName("My Food Van");
            newVendor.setOnline(true);
            newVendor.setCreatedAt(System.currentTimeMillis());
            
            vendorRef.setValue(newVendor).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    showToast("Vendor profile created successfully!");
                } else {
                    showToast("Failed to create vendor profile");
                }
            });
        }
        // Load vendor data
        loadVendorData();
        
        // Setup location tracking
        setupLocationServices();
        
        // Load pending orders
        loadPendingOrders();
        
        // Start real-time updates
        startRealTimeUpdates();
    }

    private void startRealTimeUpdates() {
        // Listen for pending orders
        loadPendingOrders();
        
        // Load today's statistics
        loadTodayStatistics();
        
        // Start periodic updates
        startPeriodicUpdates();
    }

    private void loadPendingOrders() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        ordersRef.orderByChild("vendorId").equalTo(vendorId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingOrdersList.clear();
                pendingOrdersCountValue = 0;
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && "pending".equals(order.getStatus())) {
                        pendingOrdersList.add(order);
                        pendingOrdersCountValue++;
                    }
                }
                
                updatePendingOrdersUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Error loading orders: " + error.getMessage());
            }
        });
    }

    private void loadTodayStatistics() {
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // Load today's earnings
        earningsRef.child(todayDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double earnings = snapshot.getValue(Double.class);
                    totalEarningsToday = earnings != null ? earnings : 0.0;
                } else {
                    totalEarningsToday = 0.0;
                }
                updateEarningsUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });

        // Load today's orders count
        ordersRef.orderByChild("vendorId").equalTo(vendorId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalOrdersToday = 0;
                String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    Order order = orderSnapshot.getValue(Order.class);
                    if (order != null && order.getOrderDate() != null && 
                        order.getOrderDate().startsWith(todayDate)) {
                        totalOrdersToday++;
                    }
                }
                updateOrdersCountUI();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void updatePendingOrdersUI() {
        if (pendingOrdersCount != null) {
            pendingOrdersCount.setText(String.valueOf(pendingOrdersCountValue));
            // Animate count update
            animateCounterUpdate(pendingOrdersCount, pendingOrdersCountValue);
        }
        
        if (pendingOrdersAdapter != null) {
            pendingOrdersAdapter.notifyDataSetChanged();
        }
    }

    private void updateEarningsUI() {
        String formattedEarnings = String.format(Locale.getDefault(), "₹%.0f", totalEarningsToday);
        if (todayEarnings != null) {
            todayEarnings.setText(formattedEarnings);
            // Animate earnings update
            animateEarningsUpdate();
        }
    }

    private void updateOrdersCountUI() {
        // Update total orders count (using actual view from layout)
        if (totalOrdersCount != null) {
            totalOrdersCount.setText(String.valueOf(totalOrdersToday));
            animateCounterUpdate(totalOrdersCount, totalOrdersToday);
        }
        
        // Update optional todayOrdersCount view if it exists
        if (todayOrdersCount != null) {
            todayOrdersCount.setText(String.valueOf(totalOrdersToday));
            animateCounterUpdate(todayOrdersCount, totalOrdersToday);
        }
    }

    private void updateVendorOnlineStatus(boolean isOnline) {
        if (vendorRef != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("online", isOnline);
            updates.put("isOnline", isOnline); // Update both fields for compatibility
            updates.put("lastSeen", System.currentTimeMillis());
            updates.put("lastActiveTime", System.currentTimeMillis());
            
            vendorRef.updateChildren(updates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Update UI elements
                    updateStatusChip(isOnline);
                    updateVendorStatusText(isOnline);
                    updateNavigationChipStatus(isOnline);
                    showToast(isOnline ? "You are now Online!" : "You are now Offline");
                } else {
                    // Revert switch if update failed
                    if (onlineSwitch != null) {
                        onlineSwitch.setOnCheckedChangeListener(null);
                        onlineSwitch.setChecked(!isOnline);
                        onlineSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
                            if (buttonView.isPressed()) {
                                updateVendorOnlineStatus(checked);
                                animateStatusChange(checked);
                            }
                        });
                    }
                    if (vendorStatusSwitch != null) {
                        vendorStatusSwitch.setOnCheckedChangeListener(null);
                        vendorStatusSwitch.setChecked(!isOnline);
                        vendorStatusSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
                            if (buttonView.isPressed()) {
                                updateVendorOnlineStatus(checked);
                                animateStatusChange(checked);
                            }
                        });
                    }
                    showToast("Failed to update status");
                }
            });
        }
    }

    private void updateStatusChip(boolean isOnline) {
        if (statusChip != null) {
            if (isOnline) {
                statusChip.setText("Online");
                statusChip.setChipBackgroundColorResource(R.color.success_color);
                statusChip.setChipIconResource(R.drawable.ic_online);
            } else {
                statusChip.setText("Offline");
                statusChip.setChipBackgroundColorResource(R.color.error_color);
                statusChip.setChipIconResource(R.drawable.ic_offline);
            }
        }
        
        // Also update navigation drawer chip
        updateNavigationChipStatus(isOnline);
    }

    private void setupAnimations() {
        // Setup entrance animations for cards (only if they exist)
        animationHandler.postDelayed(() -> {
            if (menuManagementCard != null) {
                animateCardEntrance(menuManagementCard, 100);
            }
            if (profileManagementCard != null) {
                animateCardEntrance(profileManagementCard, 200);
            }
            if (analyticsCard != null) {
                animateCardEntrance(analyticsCard, 300);
            }
            if (locationCard != null) {
                animateCardEntrance(locationCard, 400);
            }
        }, 500);
    }

    private void animateCardEntrance(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(50f);
        
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(delay)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateStatusChange(boolean isOnline) {
        if (statusChip != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(statusChip, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(statusChip, "scaleY", 1f, 1.1f, 1f);
            
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            
            scaleX.start();
            scaleY.start();
        }
    }

    private void animateCounterUpdate(TextView textView, int newValue) {
        if (textView != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "alpha", 0.5f, 1f);
            animator.setDuration(200);
            animator.start();
        }
    }

    private void animateEarningsUpdate() {
        if (todayEarnings != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(todayEarnings, "scaleX", 1f, 1.05f, 1f);
            ObjectAnimator animatorY = ObjectAnimator.ofFloat(todayEarnings, "scaleY", 1f, 1.05f, 1f);
            
            animator.setDuration(250);
            animatorY.setDuration(250);
            
            animator.start();
            animatorY.start();
        }
    }

    private void startPeriodicUpdates() {
        // Update location every 5 minutes if online
        Runnable locationUpdater = new Runnable() {
            @Override
            public void run() {
                // Check if vendor is online using available switches
                boolean isOnline = false;
                if (onlineSwitch != null) {
                    isOnline = onlineSwitch.isChecked();
                } else if (vendorStatusSwitch != null) {
                    isOnline = vendorStatusSwitch.isChecked();
                }
                
                if (isOnline) {
                    updateCurrentLocation();
                }
                animationHandler.postDelayed(this, 5 * 60 * 1000); // 5 minutes
            }
        };
        animationHandler.post(locationUpdater);
    }

    private void updateCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED) {
            
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && vendorRef != null) {
                                Map<String, Object> locationUpdate = new HashMap<>();
                                locationUpdate.put("latitude", location.getLatitude());
                                locationUpdate.put("longitude", location.getLongitude());
                                locationUpdate.put("lastLocationUpdate", System.currentTimeMillis());
                                
                                vendorRef.updateChildren(locationUpdate);
                            }
                        }
                    });
        }
    }

    // Order Management Methods
    private void acceptOrder(Order order) {
        if (order != null && order.getId() != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "accepted");
            updates.put("acceptedAt", System.currentTimeMillis());
            
            ordersRef.child(order.getId()).updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Order accepted successfully!");
                            // Send notification to customer
                            sendOrderStatusNotification(order, "accepted");
                        } else {
                            showToast("Failed to accept order");
                        }
                    });
        }
    }

    private void rejectOrder(Order order) {
        if (order != null && order.getId() != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "rejected");
            updates.put("rejectedAt", System.currentTimeMillis());
            
            ordersRef.child(order.getId()).updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            showToast("Order rejected");
                            // Send notification to customer
                            sendOrderStatusNotification(order, "rejected");
                        } else {
                            showToast("Failed to reject order");
                        }
                    });
        }
    }

    private void viewOrderDetails(Order order) {
        // TODO: Create OrderDetailsActivity
        showToast("Order details: " + order.getId());
    }

    private void sendOrderStatusNotification(Order order, String status) {
        // Implementation for sending push notifications
        // This would typically use Firebase Cloud Messaging
    }

    // Navigation Methods
    private void openMenuManagement() {
        Intent intent = new Intent(this, VendorMenuManagementActivity.class);
        startActivity(intent);
    }

    private void openProfileManagement() {
        try {
            Intent intent = new Intent(this, VendorProfileEditActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Profile Management - Coming Soon!");
        }
    }


    private void openLocationTracking() {
        // TODO: Create LocationTrackingActivity
        showToast("Location Tracking - Coming Soon!");
    }

    private void addNewMenuItem() {
        Intent intent = new Intent(this, AddEditMenuItemActivity.class);
        intent.putExtra("is_edit_mode", false);
        startActivity(intent);
    }

    private void viewAllOrders() {
        Intent intent = new Intent(this, AllOrdersActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.vendor_dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_settings) {
            openSettings();
            return true;
        } else if (itemId == R.id.action_logout) {
            showLogoutConfirmationDialog();
            return true;
        } else if (itemId == R.id.action_notifications) {
            openNotifications();
            return true;
        } else if (itemId == R.id.action_help) {
            openHelpSupport();
            return true;
        } else if (itemId == R.id.action_analytics) {
            openAnalytics();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void showLogoutConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout", (dialog, which) -> {
                logout();
            })
            .setNegativeButton("Cancel", (dialog, which) -> {
                dialog.dismiss();
            })
            .setIcon(R.drawable.ic_logout)
            .show();
    }

    // Removed duplicate onNavigationItemSelected method

    private void openNotifications() {
        try {
            Intent intent = new Intent(this, NotificationsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Error opening notifications: " + e.getMessage());
        }
    }


    private void openAnalytics() {
        try {
            Intent intent = new Intent(this, VendorAnalyticsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Error opening analytics: " + e.getMessage());
        }
    }

    private void openHelpSupport() {
        // Simple Help & Support dialog with basic options
        new MaterialAlertDialogBuilder(this)
            .setTitle("Help & Support")
            .setMessage("How can we help you today?")
            .setPositiveButton("Contact Support", (dialog, which) -> {
                // Open email app with pre-filled support email
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.parse("mailto:vendor-support@foodvan.com"));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Vendor Support Request");
                emailIntent.putExtra(Intent.EXTRA_TEXT, 
                    "Vendor ID: " + (sessionManager != null ? sessionManager.getUserId() : "Unknown") + "\n" +
                    "Issue Description: \n\n");
                
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send Email"));
                } catch (Exception e) {
                    showToast("No email app found. Please contact support at vendor-support@foodvan.com");
                }
            })
            .setNeutralButton("Call Support", (dialog, which) -> {
                // Open phone dialer with support number
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:+1234567890"));
                try {
                    startActivity(callIntent);
                } catch (Exception e) {
                    showToast("Unable to open dialer. Please call +1-234-567-890");
                }
            })
            .setNegativeButton("Close", null)
            .show();
    }

    private void openSettings() {
        try {
            Intent intent = new Intent(this, VendorSettingsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Settings - Coming Soon!");
        }
    }

    // Helper method to update vendor status text
    private void updateVendorStatusText(boolean isOnline) {
        if (vendorStatusText != null) {
            vendorStatusText.setText(isOnline ? "Online" : "Offline");
            vendorStatusText.setTextColor(getResources().getColor(
                isOnline ? R.color.success_color : R.color.error_color, null));
        }
    }

    private void updateNavigationChipStatus(boolean isOnline) {
        if (chipVendorStatus != null) {
            chipVendorStatus.setText(isOnline ? "Online" : "Offline");
            
            // Update chip appearance based on status
            if (isOnline) {
                chipVendorStatus.setChipBackgroundColorResource(R.color.success_color);
                chipVendorStatus.setChipIconResource(R.drawable.ic_circle);
                chipVendorStatus.setTextColor(getResources().getColor(R.color.white, null));
                chipVendorStatus.setChipIconTintResource(R.color.white);
            } else {
                chipVendorStatus.setChipBackgroundColorResource(R.color.error_color);
                chipVendorStatus.setChipIconResource(R.drawable.ic_circle);
                chipVendorStatus.setTextColor(getResources().getColor(R.color.white, null));
                chipVendorStatus.setChipIconTintResource(R.color.white);
            }
        }
    }

    private void viewEarningsDetails() {
        Intent intent = new Intent(this, EarningDetailsActivity.class);
        startActivity(intent);
    }

    private void addNewFoodItem() {
        Intent intent = new Intent(this, AddFoodItemActivity.class);
        startActivity(intent);
    }

    private void viewOrderHistory() {
        Intent intent = new Intent(this, VendorOrderHistoryActivity.class);
        startActivity(intent);
    }

    private void manageMenu() {
        Intent intent = new Intent(this, VendorMenuManagementActivity.class);
        startActivity(intent);
    }

    private void startCustomerMessaging() {
        Intent intent = new Intent(this, CustomerChatActivity.class);
        // Add sample customer data
        intent.putExtra("CUSTOMER_ID", "demo_customer_123");
        intent.putExtra("CUSTOMER_NAME", "John Doe");
        startActivity(intent);
    }

    private void openNavigationMenu() {
        // Create navigation menu options
        String[] menuOptions = {
            "Dashboard", 
            "Orders", 
            "Menu Management", 
            "Analytics", 
            "Profile", 
            "Settings", 
            "Help & Support",
            "Logout"
        };
        
        new MaterialAlertDialogBuilder(this)
            .setTitle("Navigation Menu")
            .setItems(menuOptions, (dialog, which) -> {
                switch (which) {
                    case 0: // Dashboard
                        // Already on dashboard
                        showToast("You are already on Dashboard");
                        break;
                    case 1: // Orders
                        showToast("Orders feature coming soon");
                        break;
                    case 2: // Menu Management
                        openMenuManagementTab();
                        break;
                    case 3: // Analytics
                        showToast("Analytics feature coming soon");
                        break;
                    case 4: // Profile
                        showToast("Profile feature coming soon");
                        break;
                    case 5: // Settings
                        openSettings();
                        break;
                    case 6: // Help & Support
                        showToast("Help & Support feature coming soon");
                        break;
                    case 7: // Logout
                        logout();
                        break;
                }
            })
            .show();
    }

    private void logout() {
        // Update vendor status to offline
        if (vendorRef != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("online", false);
            updates.put("lastSeen", System.currentTimeMillis());
            vendorRef.updateChildren(updates);
        }
        
        // Sign out from Firebase
        if (firebaseAuth != null) {
            firebaseAuth.signOut();
        }
        
        // Clear session data
        if (sessionManager != null) {
            sessionManager.logout();
        }
        
        // Clear all activity stack and navigate to login
        Intent intent = new Intent(this, com.example.foodvan.activities.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        // Show logout message
        showToast("Logged out successfully");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateCurrentLocation();
            } else {
                showToast("Location permission required for location tracking");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Update last seen timestamp
        if (vendorRef != null) {
            vendorRef.child("lastSeen").setValue(System.currentTimeMillis());
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Show exit confirmation dialog
            new MaterialAlertDialogBuilder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    super.onBackPressed();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.nav_dashboard) {
            // Already on dashboard, just close drawer
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.nav_orders) {
            openOrdersTab();
        } else if (id == R.id.nav_menu_management) {
            openMenuManagementTab();
        } else if (id == R.id.nav_analytics) {
            openAnalyticsTab();
        } else if (id == R.id.nav_help_support) {
            openHelpSupport();
        } else if (id == R.id.nav_logout) {
            showLogoutConfirmation();
        }
        
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Check if user is still logged in
        if (firebaseAuth != null && firebaseAuth.getCurrentUser() == null) {
            // User is not logged in, redirect to login
            Intent intent = new Intent(this, com.example.foodvan.activities.auth.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }


    private void openCustomerSupport() {
        // TODO: Implement customer support functionality
        showToast("Customer Support - Coming Soon!");
    }

    // Navigation Methods
    private void openOrdersTab() {
        Intent intent = new Intent(this, VendorOrdersActivity.class);
        startActivity(intent);
    }

    private void openMenuManagementTab() {
        Intent intent = new Intent(this, VendorMenuManagementActivity.class);
        startActivity(intent);
    }

    private void openAnalyticsTab() {
        try {
            Intent intent = new Intent(this, VendorAnalyticsActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            showToast("Error opening analytics: " + e.getMessage());
        }
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> logout())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
