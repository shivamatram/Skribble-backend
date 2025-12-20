package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.adapters.ManageMenuAdapter;
import com.example.foodvan.models.MenuItem;
import com.example.foodvan.viewmodels.ManageMenuViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class ManageMenuActivity extends AppCompatActivity implements ManageMenuAdapter.OnMenuItemActionListener {

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputEditText searchEditText;
    private ChipGroup categoryChipGroup;
    private LinearProgressIndicator progressIndicator;
    private RecyclerView menuItemsRecyclerView;
    private LinearLayout emptyStateLayout;
    private MaterialButton addFirstItemButton;
    private ExtendedFloatingActionButton fabAddItem;
    
    // Bulk Operations UI
    private LinearLayout bulkActionsBar;
    private TextView selectedCountText;
    private MaterialButton btnSelectMode;
    private MaterialButton btnBulkDelete;
    private MaterialButton btnBulkToggleAvailability;

    // ViewModel and Adapter
    private ManageMenuViewModel viewModel;
    private ManageMenuAdapter adapter;
    private List<MenuItem> allMenuItems = new ArrayList<>();
    private List<MenuItem> filteredMenuItems = new ArrayList<>();
    private List<MenuItem> selectedMenuItems = new ArrayList<>();

    // Filter and Selection state
    private String currentCategory = "All";
    private String currentSearchQuery = "";
    private boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Set orange status bar to match navbar - BEFORE setContentView
        setupStatusBar();
        
        setContentView(R.layout.activity_manage_menu);

        initializeViewModel();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchAndFilters();
        setupSwipeRefresh();
        setupClickListeners();
        observeViewModel();
        loadMenuItems();
    }

    private void initializeViewModel() {
        viewModel = new ViewModelProvider(this).get(ManageMenuViewModel.class);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        searchEditText = findViewById(R.id.search_edit_text);
        categoryChipGroup = findViewById(R.id.category_chip_group);
        progressIndicator = findViewById(R.id.progress_indicator);
        menuItemsRecyclerView = findViewById(R.id.menu_items_recycler_view);
        emptyStateLayout = findViewById(R.id.empty_state_layout);
        addFirstItemButton = findViewById(R.id.add_first_item_button);
        fabAddItem = findViewById(R.id.fab_add_item);
        
        // Bulk Operations Views
        bulkActionsBar = findViewById(R.id.bulk_actions_bar);
        selectedCountText = findViewById(R.id.selected_count_text);
        btnSelectMode = findViewById(R.id.btn_select_mode);
        btnBulkDelete = findViewById(R.id.btn_bulk_delete);
        btnBulkToggleAvailability = findViewById(R.id.btn_bulk_toggle_availability);
        
        setupBulkOperations();
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
        adapter = new ManageMenuAdapter(this, filteredMenuItems, this);
        menuItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuItemsRecyclerView.setAdapter(adapter);
    }

    private void setupSearchAndFilters() {
        // Search functionality
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

        // Category filter chips
        categoryChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                currentCategory = selectedChip.getText().toString();
                applyFilters();
            }
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::loadMenuItems);
        swipeRefreshLayout.setColorSchemeResources(R.color.primary_color);
    }

    private void setupClickListeners() {
        fabAddItem.setOnClickListener(v -> openAddFoodItemActivity());
        addFirstItemButton.setOnClickListener(v -> openAddFoodItemActivity());
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

        viewModel.getMenuItems().observe(this, menuItems -> {
            if (menuItems != null) {
                allMenuItems.clear();
                allMenuItems.addAll(menuItems);
                applyFilters();
            }
        });

        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null) {
                showToast(message);
                loadMenuItems(); // Refresh the list
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                showToast(error);
            }
        });
    }

    private void loadMenuItems() {
        viewModel.loadMenuItems();
    }

    private void applyFilters() {
        filteredMenuItems.clear();

        for (MenuItem item : allMenuItems) {
            boolean matchesSearch = currentSearchQuery.isEmpty() ||
                    item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    item.getCategory().toLowerCase().contains(currentSearchQuery.toLowerCase());

            boolean matchesCategory = currentCategory.equals("All") ||
                    item.getCategory().equalsIgnoreCase(currentCategory);

            if (matchesSearch && matchesCategory) {
                filteredMenuItems.add(item);
            }
        }

        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredMenuItems.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            menuItemsRecyclerView.setVisibility(View.GONE);
            fabAddItem.hide();
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            menuItemsRecyclerView.setVisibility(View.VISIBLE);
            fabAddItem.show();
        }
    }

    private void openAddFoodItemActivity() {
        Intent intent = new Intent(this, AddFoodItemActivity.class);
        startActivity(intent);
    }

    @Override
    public void onEditMenuItem(MenuItem menuItem) {
        // Open edit dialog or activity
        Intent intent = new Intent(this, AddFoodItemActivity.class);
        intent.putExtra("EDIT_MODE", true);
        intent.putExtra("MENU_ITEM_ID", menuItem.getItemId());
        startActivity(intent);
    }

    @Override
    public void onDeleteMenuItem(MenuItem menuItem) {
        showDeleteConfirmationDialog(menuItem);
    }

    @Override
    public void onToggleAvailability(MenuItem menuItem) {
        menuItem.setAvailable(!menuItem.isAvailable());
        viewModel.updateMenuItem(menuItem);
    }

    @Override
    public void onItemSelectionChanged(MenuItem menuItem, boolean isSelected) {
        if (isSelected) {
            if (!selectedMenuItems.contains(menuItem)) {
                selectedMenuItems.add(menuItem);
            }
        } else {
            selectedMenuItems.remove(menuItem);
        }
        updateSelectedCount();
    }

    private void showDeleteConfirmationDialog(MenuItem menuItem) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Menu Item")
                .setMessage("Are you sure you want to delete \"" + menuItem.getName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteMenuItem(menuItem.getItemId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure orange status bar is applied when activity resumes
        setupStatusBar();
        // Refresh menu items when returning from Add/Edit activity
        loadMenuItems();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // Bulk Operations Methods
    private void setupBulkOperations() {
        btnSelectMode.setOnClickListener(v -> toggleSelectionMode());
        btnBulkDelete.setOnClickListener(v -> performBulkDelete());
        btnBulkToggleAvailability.setOnClickListener(v -> performBulkToggleAvailability());
    }

    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        
        if (isSelectionMode) {
            btnSelectMode.setText("Cancel");
            btnSelectMode.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_close));
            bulkActionsBar.setVisibility(View.VISIBLE);
            fabAddItem.hide();
        } else {
            btnSelectMode.setText("Select");
            btnSelectMode.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_check_box));
            bulkActionsBar.setVisibility(View.GONE);
            fabAddItem.show();
            clearSelection();
        }
        
        // Notify adapter about selection mode change
        if (adapter != null) {
            adapter.setSelectionMode(isSelectionMode);
        }
    }

    private void clearSelection() {
        selectedMenuItems.clear();
        updateSelectedCount();
        if (adapter != null) {
            adapter.clearSelection();
        }
    }

    private void updateSelectedCount() {
        int count = selectedMenuItems.size();
        selectedCountText.setText(count + " item" + (count != 1 ? "s" : "") + " selected");
        
        // Enable/disable bulk action buttons based on selection
        boolean hasSelection = count > 0;
        btnBulkDelete.setEnabled(hasSelection);
        btnBulkToggleAvailability.setEnabled(hasSelection);
    }

    private void performBulkDelete() {
        if (selectedMenuItems.isEmpty()) return;
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Menu Items")
                .setMessage("Are you sure you want to delete " + selectedMenuItems.size() + 
                           " selected item" + (selectedMenuItems.size() != 1 ? "s" : "") + "? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    for (MenuItem item : selectedMenuItems) {
                        viewModel.deleteMenuItem(item.getItemId());
                    }
                    showToast(selectedMenuItems.size() + " items deleted successfully");
                    toggleSelectionMode(); // Exit selection mode
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performBulkToggleAvailability() {
        if (selectedMenuItems.isEmpty()) return;
        
        // Determine if we should make items available or unavailable
        // If any selected item is unavailable, make all available; otherwise make all unavailable
        boolean makeAvailable = selectedMenuItems.stream().anyMatch(item -> !item.isAvailable());
        
        for (MenuItem item : selectedMenuItems) {
            item.setAvailable(makeAvailable);
            viewModel.updateMenuItem(item);
        }
        
        String action = makeAvailable ? "made available" : "made unavailable";
        showToast(selectedMenuItems.size() + " items " + action);
        toggleSelectionMode(); // Exit selection mode
    }

    /**
     * Setup orange status bar to match the navbar theme (same as Order History)
     */
    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            
            // Clear any existing flags
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            
            // Enable drawing system bar backgrounds
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            
            // Set orange status bar color - using exact color value
            int orangeColor = 0xFFFF6B35; // #FF6B35
            window.setStatusBarColor(orangeColor);
            
            // Ensure white status bar icons on orange background
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                View decorView = window.getDecorView();
                int flags = decorView.getSystemUiVisibility();
                // Remove light status bar flag to get white icons
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                decorView.setSystemUiVisibility(flags);
            }
        }
    }

}
