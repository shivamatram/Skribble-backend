package com.example.foodvan.activities.vendor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.foodvan.R;
// TODO: Import AddMenuItemActivity when it's created
import com.example.foodvan.adapters.MenuManagementAdapter;
import com.example.foodvan.fragments.MenuFilterBottomSheet;
// Note: Using fully qualified name for MenuItem model to avoid conflict with android.view.MenuItem
import com.example.foodvan.utils.FilterUtils;
import com.example.foodvan.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VendorMenuManagementActivity extends AppCompatActivity implements 
        MenuManagementAdapter.OnMenuItemActionListener {

    // UI Components
    private MaterialToolbar toolbar;
    private TextInputEditText etSearchMenu;
    private LinearLayout layoutSearchBar;
    private MaterialButton btnSearch, btnFilter, btnAddItem, btnCloseSearch;
    private MaterialButtonToggleGroup toggleViewType;
    private MaterialButton btnListView, btnGridView;
    private ChipGroup chipGroupCategories;
    private RecyclerView rvMenuItems;
    private LinearLayout layoutEmptyState;
    private TextView tvTotalItemsCount, tvActiveItemsCount;
    private ExtendedFloatingActionButton fabAddMenuItem;
    private MaterialButton btnAddFirstItem;

    // Data and Adapters
    private MenuManagementAdapter menuAdapter;
    private List<com.example.foodvan.models.MenuItem> allMenuItems;
    private List<com.example.foodvan.models.MenuItem> filteredMenuItems;
    
    // Firebase
    private FirebaseAuth firebaseAuth;
    private DatabaseReference menuRef;
    private String vendorId;
    
    // Utils
    private SessionManager sessionManager;
    
    // View State
    private boolean isGridView = false;
    private String currentSearchQuery = "";
    private String selectedCategory = "All";
    private String currentSortOption = "name"; // name, price_low, price_high, most_ordered

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendor_menu_management);
        
        initializeComponents();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupSearchAndFilters();
        
        // Load menu items (includes sample data for testing)
        loadMenuItems();
    }
    

    private void initializeComponents() {
        firebaseAuth = FirebaseAuth.getInstance();
        sessionManager = new SessionManager(this);
        vendorId = sessionManager.getUserId();
        
        if (vendorId != null) {
            menuRef = FirebaseDatabase.getInstance().getReference("vendors")
                    .child(vendorId).child("menuItems");
        }
        
        allMenuItems = new ArrayList<>();
        filteredMenuItems = new ArrayList<>();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        layoutSearchBar = findViewById(R.id.layout_search_bar);
        etSearchMenu = findViewById(R.id.et_search_menu);
        btnSearch = findViewById(R.id.btn_search);
        btnFilter = findViewById(R.id.btn_filter);
        btnAddItem = findViewById(R.id.btn_add_item);
        btnCloseSearch = findViewById(R.id.btn_close_search);
        toggleViewType = findViewById(R.id.toggle_view_type);
        btnListView = findViewById(R.id.btn_list_view);
        btnGridView = findViewById(R.id.btn_grid_view);
        chipGroupCategories = findViewById(R.id.chip_group_categories);
        rvMenuItems = findViewById(R.id.rv_menu_items);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        tvTotalItemsCount = findViewById(R.id.tv_total_items_count);
        tvActiveItemsCount = findViewById(R.id.tv_active_items_count);
        fabAddMenuItem = findViewById(R.id.fab_add_menu_item);
        btnAddFirstItem = findViewById(R.id.btn_add_first_item);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // Setup toolbar button click listeners
        setupToolbarClickListeners();
    }
    
    private void setupToolbarClickListeners() {
        // Search button - toggle search bar visibility
        if (btnSearch != null) {
            btnSearch.setOnClickListener(v -> toggleSearchBar());
        }
        
        // Filter button - show filter options
        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> showFilterOptions());
        }
        
        // Add item button - open add menu item activity
        if (btnAddItem != null) {
            btnAddItem.setOnClickListener(v -> openAddMenuItem());
        }
        
        // Close search button - hide search bar
        if (btnCloseSearch != null) {
            btnCloseSearch.setOnClickListener(v -> hideSearchBar());
        }
    }
    
    private void toggleSearchBar() {
        if (layoutSearchBar != null) {
            if (layoutSearchBar.getVisibility() == View.GONE) {
                layoutSearchBar.setVisibility(View.VISIBLE);
                if (etSearchMenu != null) {
                    etSearchMenu.requestFocus();
                }
            } else {
                hideSearchBar();
            }
        }
    }
    
    private void hideSearchBar() {
        if (layoutSearchBar != null) {
            layoutSearchBar.setVisibility(View.GONE);
            if (etSearchMenu != null) {
                etSearchMenu.setText("");
                etSearchMenu.clearFocus();
            }
        }
    }
    
    private void showFilterOptions() {
        try {
            // Ensure allMenuItems is initialized
            if (allMenuItems == null) {
                allMenuItems = new ArrayList<>();
            }
            
            // Ensure filteredMenuItems is initialized
            if (filteredMenuItems == null) {
                filteredMenuItems = new ArrayList<>();
            }
            
            android.util.Log.d("MenuFilter", "Opening filter with " + allMenuItems.size() + " items");
            
            MenuFilterBottomSheet filterBottomSheet = MenuFilterBottomSheet.newInstance();
            filterBottomSheet.setMenuItems(allMenuItems);
            filterBottomSheet.setOnFilterAppliedListener((filter, filteredItems) -> {
                try {
                    android.util.Log.d("MenuFilter", "Filter applied, got " + (filteredItems != null ? filteredItems.size() : 0) + " filtered items");
                    
                    // Update the filtered items list
                    if (this.filteredMenuItems != null && filteredItems != null) {
                        this.filteredMenuItems.clear();
                        this.filteredMenuItems.addAll(filteredItems);
                    }
                    
                    // Notify adapter of changes
                    if (menuAdapter != null) {
                        menuAdapter.notifyDataSetChanged();
                    }
                    
                    // Show filter applied message
                    if (filter != null && allMenuItems != null && filteredItems != null) {
                        String message = FilterUtils.generateFilterSummary(filter, allMenuItems.size(), filteredItems.size());
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    android.util.Log.e("MenuFilter", "Error applying filter", e);
                    Toast.makeText(this, "Error applying filter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            
            if (getSupportFragmentManager() != null) {
                filterBottomSheet.show(getSupportFragmentManager(), "MenuFilterBottomSheet");
            } else {
                android.util.Log.e("MenuFilter", "FragmentManager is null, cannot show filter");
                Toast.makeText(this, "Unable to open filter", Toast.LENGTH_SHORT).show();
            }
            
        } catch (Exception e) {
            android.util.Log.e("MenuFilter", "Error opening filter", e);
            Toast.makeText(this, "Error opening filter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        menuAdapter = new MenuManagementAdapter(this, filteredMenuItems, this);
        rvMenuItems.setAdapter(menuAdapter);
        updateLayoutManager();
    }

    private void updateLayoutManager() {
        if (isGridView) {
            rvMenuItems.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            rvMenuItems.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void setupClickListeners() {
        fabAddMenuItem.setOnClickListener(v -> openAddMenuItem());
        btnAddFirstItem.setOnClickListener(v -> openAddMenuItem());
        
        toggleViewType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_grid_view) {
                    isGridView = true;
                } else {
                    isGridView = false;
                }
                updateLayoutManager();
                menuAdapter.setGridView(isGridView);
            }
        });
        
        // Set default selection
        btnListView.setChecked(true);
    }

    private void setupSearchAndFilters() {
        // Search functionality
        etSearchMenu.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString().trim();
                filterMenuItems();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        // Category filter chips
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                Chip selectedChip = findViewById(checkedIds.get(0));
                selectedCategory = selectedChip.getText().toString();
                filterMenuItems();
            }
        });
    }

    private void loadMenuItems() {
        // Load sample data for testing
        allMenuItems.clear();
        addSampleMenuItems();
        updateStatistics();
        filterMenuItems();
        
        // Also try to load from Firebase (but don't wait for it)
        if (menuRef != null) {
            menuRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Only replace sample data if Firebase has actual data
                    if (snapshot.hasChildren()) {
                        allMenuItems.clear();
                        
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            com.example.foodvan.models.MenuItem menuItem = itemSnapshot.getValue(com.example.foodvan.models.MenuItem.class);
                            if (menuItem != null) {
                                menuItem.setId(itemSnapshot.getKey());
                                allMenuItems.add(menuItem);
                            }
                        }
                        
                        updateStatistics();
                        filterMenuItems();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    showToast("Error loading menu items: " + error.getMessage());
                }
            });
        }
    }
    
    private void addSampleMenuItems() {
        // Create sample menu items for testing
        allMenuItems.add(createSampleMenuItem("1", "Delicious Burger", "Juicy beef burger with fresh vegetables and special sauce", 299.99, "Main Course", true, 45));
        allMenuItems.add(createSampleMenuItem("2", "Chicken Pizza", "Wood-fired pizza with grilled chicken and cheese", 399.99, "Main Course", true, 32));
        allMenuItems.add(createSampleMenuItem("3", "French Fries", "Crispy golden french fries with seasoning", 149.99, "Snacks", true, 78));
        allMenuItems.add(createSampleMenuItem("4", "Cold Coffee", "Refreshing iced coffee with whipped cream", 179.99, "Beverages", true, 56));
        allMenuItems.add(createSampleMenuItem("5", "Chocolate Cake", "Rich chocolate cake with cream frosting", 249.99, "Desserts", false, 23));
        allMenuItems.add(createSampleMenuItem("6", "Veggie Sandwich", "Fresh vegetables with mayo and herbs", 199.99, "Snacks", true, 41));
        allMenuItems.add(createSampleMenuItem("7", "Mango Smoothie", "Fresh mango smoothie with yogurt", 159.99, "Beverages", true, 67));
        allMenuItems.add(createSampleMenuItem("8", "Pasta Alfredo", "Creamy alfredo pasta with herbs", 349.99, "Main Course", true, 29));
    }
    
    private com.example.foodvan.models.MenuItem createSampleMenuItem(String id, String name, String description, double price, String category, boolean available, int orderCount) {
        com.example.foodvan.models.MenuItem item = new com.example.foodvan.models.MenuItem();
        item.setId(id);
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setCategory(category);
        item.setAvailable(available);
        item.setOrderCount(orderCount);
        item.setImageUrl(""); // No image for sample data
        return item;
    }

    private void filterMenuItems() {
        filteredMenuItems.clear();
        
        for (com.example.foodvan.models.MenuItem item : allMenuItems) {
            boolean matchesSearch = currentSearchQuery.isEmpty() || 
                    item.getName().toLowerCase().contains(currentSearchQuery.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(currentSearchQuery.toLowerCase());
            
            boolean matchesCategory = selectedCategory.equals("All") || 
                    item.getCategory().equals(selectedCategory);
            
            if (matchesSearch && matchesCategory) {
                filteredMenuItems.add(item);
            }
        }
        
        sortMenuItems();
        updateUI();
    }

    private void sortMenuItems() {
        switch (currentSortOption) {
            case "name":
                Collections.sort(filteredMenuItems, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                break;
            case "price_low":
                Collections.sort(filteredMenuItems, Comparator.comparingDouble(com.example.foodvan.models.MenuItem::getPrice));
                break;
            case "price_high":
                Collections.sort(filteredMenuItems, (a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case "most_ordered":
                Collections.sort(filteredMenuItems, (a, b) -> Integer.compare(b.getOrderCount(), a.getOrderCount()));
                break;
        }
    }

    private void updateStatistics() {
        int totalItems = allMenuItems.size();
        int activeItems = 0;
        
        for (com.example.foodvan.models.MenuItem item : allMenuItems) {
            if (item.isAvailable()) {
                activeItems++;
            }
        }
        
        tvTotalItemsCount.setText(String.valueOf(totalItems));
        tvActiveItemsCount.setText(String.valueOf(activeItems));
    }

    private void updateUI() {
        if (filteredMenuItems.isEmpty()) {
            rvMenuItems.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvMenuItems.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
        
        menuAdapter.notifyDataSetChanged();
    }

    private void openAddMenuItem() {
        Intent intent = new Intent(this, AddEditMenuItemActivity.class);
        startActivity(intent);
    }

    private void showSortDialog() {
        String[] sortOptions = {"Name (A-Z)", "Price (Low to High)", "Price (High to Low)", "Most Ordered"};
        String[] sortValues = {"name", "price_low", "price_high", "most_ordered"};
        
        int selectedIndex = 0;
        for (int i = 0; i < sortValues.length; i++) {
            if (sortValues[i].equals(currentSortOption)) {
                selectedIndex = i;
                break;
            }
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Sort Menu Items")
                .setSingleChoiceItems(sortOptions, selectedIndex, (dialog, which) -> {
                    currentSortOption = sortValues[which];
                    filterMenuItems();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // MenuManagementAdapter.OnMenuItemActionListener implementation
    @Override
    public void onEditMenuItem(com.example.foodvan.models.MenuItem menuItem) {
        Intent intent = new Intent(this, AddEditMenuItemActivity.class);
        intent.putExtra("menu_item_id", menuItem.getId());
        intent.putExtra("is_edit_mode", true);
        startActivity(intent);
    }

    @Override
    public void onDeleteMenuItem(com.example.foodvan.models.MenuItem menuItem) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Menu Item")
                .setMessage("Are you sure you want to delete \"" + menuItem.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMenuItem(menuItem.getId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleAvailability(com.example.foodvan.models.MenuItem menuItem) {
        if (menuRef != null) {
            menuRef.child(menuItem.getId()).child("available").setValue(!menuItem.isAvailable())
                    .addOnSuccessListener(aVoid -> {
                        String status = menuItem.isAvailable() ? "disabled" : "enabled";
                        showToast("Item " + status + " successfully");
                    })
                    .addOnFailureListener(e -> showToast("Failed to update item status"));
        }
    }

    private void deleteMenuItem(String itemId) {
        if (menuRef != null) {
            menuRef.child(itemId).removeValue()
                    .addOnSuccessListener(aVoid -> showToast("Menu item deleted successfully"))
                    .addOnFailureListener(e -> showToast("Failed to delete menu item"));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_management_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        // Note: Search, Filter, and Add buttons are handled by toolbar click listeners
        // These menu items are kept for overflow menu compatibility but won't show in toolbar
        if (id == R.id.action_search) {
            toggleSearchBar();
            return true;
        } else if (id == R.id.action_filter) {
            showFilterOptions();
            return true;
        } else if (id == R.id.action_sort) {
            showSortDialog();
            return true;
        } else if (id == R.id.action_sort_filter) {
            showSortFilterBottomSheet();
            return true;
        } else if (id == R.id.action_bulk_actions) {
            enterBulkActionMode();
            return true;
        } else if (id == R.id.action_import_export) {
            openImportExportActivity();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (isBulkActionMode) {
            exitBulkActionMode();
        } else {
            super.onBackPressed();
            finish();
        }
    }

    private void showFilterDialog() {
        String[] filterOptions = {
            "All Items",
            "Available Items",
            "Out of Stock",
            "Popular Items",
            "Recently Added",
            "Price: Low to High",
            "Price: High to Low",
            "By Category"
        };

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Filter Menu Items")
                .setItems(filterOptions, (dialog, which) -> {
                    String selectedFilter = filterOptions[which];
                    applyFilter(selectedFilter);
                    showToast("Filter applied: " + selectedFilter);
                })
                .setNegativeButton("Cancel", null);

        // Create and show dialog
        builder.show();
    }

    private void applyFilter(String filterType) {
        // TODO: Implement actual filtering logic based on the selected filter type
        // This would typically filter the menu items list and update the RecyclerView
        switch (filterType) {
            case "All Items":
                // Show all items
                break;
            case "Available Items":
                // Show only available items
                break;
            case "Out of Stock":
                // Show only out of stock items
                break;
            case "Popular Items":
                // Show popular items based on order count
                break;
            case "Recently Added":
                // Show recently added items
                break;
            case "Price: Low to High":
                // Sort by price ascending
                break;
            case "Price: High to Low":
                // Sort by price descending
                break;
            case "By Category":
                // Show category selection dialog
                showCategoryFilterDialog();
                break;
        }
    }

    private void showCategoryFilterDialog() {
        String[] categories = {"All", "Snacks", "Beverages", "Main Course", "Desserts"};
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Filter by Category")
                .setItems(categories, (dialog, which) -> {
                    String selectedCategory = categories[which];
                    // Apply category filter
                    this.selectedCategory = selectedCategory;
                    filterMenuItems();
                    showToast("Category filter: " + selectedCategory);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSortFilterBottomSheet() {
        // Use our new advanced filter functionality instead of old implementation
        showFilterOptions();
    }
    
    private boolean isBulkActionMode = false;
    private java.util.Set<String> selectedItems = new java.util.HashSet<>();
    
    private void enterBulkActionMode() {
        isBulkActionMode = true;
        selectedItems.clear();
        
        // Change toolbar to bulk action mode
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Select Items");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        // Show bulk action toolbar
        showBulkActionToolbar();
        
        // Simulate some selected items for testing
        simulateItemSelection();
        
        // Update RecyclerView adapter to show checkboxes (if using enhanced adapter)
        // For now, show instructions
        showToast("Bulk Action mode activated. Use the floating buttons to perform bulk actions.");
    }
    
    private View bulkActionFabs;
    
    private void showBulkActionToolbar() {
        // Create floating action buttons for bulk actions
        bulkActionFabs = getLayoutInflater().inflate(R.layout.fab_bulk_actions, null);
        
        // Find action buttons
        com.google.android.material.floatingactionbutton.FloatingActionButton fabToggleAvailability = 
            bulkActionFabs.findViewById(R.id.fab_toggle_availability);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabChangeCategory = 
            bulkActionFabs.findViewById(R.id.fab_change_category);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabChangePrice = 
            bulkActionFabs.findViewById(R.id.fab_change_price);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabDeleteSelected = 
            bulkActionFabs.findViewById(R.id.fab_delete_selected);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabExitBulkMode = 
            bulkActionFabs.findViewById(R.id.fab_exit_bulk_mode);
        TextView tvSelectedCount = bulkActionFabs.findViewById(R.id.tv_selected_count_fab);
        
        // Set up click listeners
        fabToggleAvailability.setOnClickListener(v -> toggleSelectedItemsAvailability());
        fabChangeCategory.setOnClickListener(v -> showBulkCategoryDialog());
        fabChangePrice.setOnClickListener(v -> showBulkPriceDialog());
        fabDeleteSelected.setOnClickListener(v -> showBulkDeleteDialog());
        fabExitBulkMode.setOnClickListener(v -> exitBulkActionMode());
        
        // Update selected count
        tvSelectedCount.setText(selectedItems.size() + " selected");
        
        // Add to the main layout
        ViewGroup mainLayout = findViewById(android.R.id.content);
        if (mainLayout != null) {
            mainLayout.addView(bulkActionFabs);
        }
    }
    
    private void exitBulkActionMode() {
        isBulkActionMode = false;
        selectedItems.clear();
        
        // Restore normal toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Menu Management");
        }
        
        // Remove bulk action FABs
        if (bulkActionFabs != null) {
            ViewGroup mainLayout = findViewById(android.R.id.content);
            if (mainLayout != null) {
                mainLayout.removeView(bulkActionFabs);
            }
            bulkActionFabs = null;
        }
        
        showToast("Bulk Action mode deactivated.");
    }
    
    private void toggleSelectedItemsAvailability() {
        if (selectedItems.isEmpty()) {
            showToast("No items selected");
            return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Toggle Availability")
                .setMessage("Toggle availability for " + selectedItems.size() + " selected items?")
                .setPositiveButton("Toggle", (dialog, which) -> {
                    // TODO: Implement actual availability toggle
                    showToast("Availability toggled for " + selectedItems.size() + " items");
                    exitBulkActionMode();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    // Add method to simulate item selection for testing
    public void simulateItemSelection() {
        // Add some mock item IDs for testing
        selectedItems.add("item_1");
        selectedItems.add("item_2");
        selectedItems.add("item_3");
        showToast(selectedItems.size() + " items selected for testing");
    }
    
    private void showBulkCategoryDialog() {
        if (selectedItems.isEmpty()) {
            showToast("No items selected");
            return;
        }
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_category, null);
        
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        
        // Initialize dialog components
        TextView tvDescription = dialogView.findViewById(R.id.tv_category_description);
        ChipGroup chipGroupCategories = dialogView.findViewById(R.id.chip_group_categories);
        RecyclerView rvPreview = dialogView.findViewById(R.id.rv_selected_items_preview);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_category);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply_category);
        
        tvDescription.setText("Change category for " + selectedItems.size() + " selected items");
        
        // Setup preview (TODO: implement preview adapter)
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            int checkedId = chipGroupCategories.getCheckedChipId();
            if (checkedId != View.NO_ID) {
                Chip selectedChip = dialogView.findViewById(checkedId);
                String newCategory = selectedChip.getText().toString();
                applyBulkCategoryChange(newCategory);
                dialog.dismiss();
                exitBulkActionMode();
            } else {
                showToast("Please select a category");
            }
        });
        
        dialog.show();
    }
    
    private void showBulkPriceDialog() {
        if (selectedItems.isEmpty()) {
            showToast("No items selected");
            return;
        }
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_price_change, null);
        
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();
        
        // Initialize dialog components
        RadioGroup radioGroupType = dialogView.findViewById(R.id.radio_group_price_type);
        com.google.android.material.textfield.TextInputLayout tilValue = dialogView.findViewById(R.id.til_price_value);
        TextInputEditText etValue = dialogView.findViewById(R.id.et_price_value);
        TextView tvItemsAffected = dialogView.findViewById(R.id.tv_items_affected);
        TextView tvTotalChange = dialogView.findViewById(R.id.tv_total_change);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel_price);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply_price);
        
        tvItemsAffected.setText(String.valueOf(selectedItems.size()));
        
        // Update UI based on selected type
        radioGroupType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_percentage_increase || checkedId == R.id.radio_percentage_decrease) {
                tilValue.setSuffixText("%");
                tilValue.setPrefixText(null);
            } else {
                tilValue.setSuffixText(null);
                tilValue.setPrefixText("₹");
            }
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            String valueStr = etValue.getText() != null ? etValue.getText().toString() : "";
            if (!valueStr.isEmpty()) {
                try {
                    double value = Double.parseDouble(valueStr);
                    int selectedType = radioGroupType.getCheckedRadioButtonId();
                    applyBulkPriceChange(selectedType, value);
                    dialog.dismiss();
                    exitBulkActionMode();
                } catch (NumberFormatException e) {
                    showToast("Please enter a valid number");
                }
            } else {
                showToast("Please enter a value");
            }
        });
        
        dialog.show();
    }
    
    private void showBulkDeleteDialog() {
        if (selectedItems.isEmpty()) {
            showToast("No items selected");
            return;
        }
        
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Items")
                .setMessage("Are you sure you want to delete " + selectedItems.size() + " selected items? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    applyBulkDelete();
                    exitBulkActionMode();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    private void applyBulkCategoryChange(String newCategory) {
        // TODO: Implement Firestore batch write for category change
        showToast("Category changed to " + newCategory + " for " + selectedItems.size() + " items");
        
        // This would typically:
        // 1. Create Firestore batch write
        // 2. Update each selected item's category
        // 3. Log audit entry
        // 4. Update local cache/Room database
        // 5. Refresh RecyclerView
    }
    
    private void applyBulkPriceChange(int priceType, double value) {
        // TODO: Implement Firestore batch write for price change
        String changeDescription = getPriceChangeDescription(priceType, value);
        showToast("Price " + changeDescription + " applied to " + selectedItems.size() + " items");
        
        // This would typically:
        // 1. Calculate new prices for each item
        // 2. Create Firestore batch write
        // 3. Update each selected item's price
        // 4. Log audit entry
        // 5. Update local cache/Room database
        // 6. Refresh RecyclerView
    }
    
    private void applyBulkDelete() {
        // TODO: Implement Firestore batch write for soft delete
        showToast(selectedItems.size() + " items moved to trash");
        
        // This would typically:
        // 1. Create Firestore batch write
        // 2. Set visible=false for each selected item
        // 3. Move items to trash collection
        // 4. Log audit entry
        // 5. Update local cache/Room database
        // 6. Refresh RecyclerView
        // 7. Show undo Snackbar
    }
    
    private String getPriceChangeDescription(int priceType, double value) {
        if (priceType == R.id.radio_percentage_increase) {
            return "increased by " + value + "%";
        } else if (priceType == R.id.radio_percentage_decrease) {
            return "decreased by " + value + "%";
        } else if (priceType == R.id.radio_fixed_increase) {
            return "increased by ₹" + value;
        } else if (priceType == R.id.radio_fixed_decrease) {
            return "decreased by ₹" + value;
        } else if (priceType == R.id.radio_set_price) {
            return "set to ₹" + value;
        } else {
            return "changed";
        }
    }
    
    private void openImportExportActivity() {
        Intent intent = new Intent(this, ImportExportActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
