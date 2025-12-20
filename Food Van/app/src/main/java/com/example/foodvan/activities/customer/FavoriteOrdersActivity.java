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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.adapters.FavoriteOrdersAdapter;
import com.example.foodvan.models.FavoriteOrder;
import com.example.foodvan.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * FavoriteOrdersActivity - Comprehensive favorite orders management
 * Features: View favorites, search, filter, multi-select, reorder functionality
 */
public class FavoriteOrdersActivity extends AppCompatActivity implements FavoriteOrdersAdapter.OnFavoriteActionListener {

    private static final String TAG = "FavoriteOrdersActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputEditText etSearch;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipFood, chipVendors, chipRecent;
    private CircularProgressIndicator progressIndicator;
    private MaterialCardView cardEmptyState, cardStats;
    private RecyclerView rvFavorites;
    private MaterialButton btnStartExploring;
    private ExtendedFloatingActionButton fabMultiSelect;
    private ImageView ivEmptyIcon;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private TextView tvTotalFavorites, tvFoodCount, tvVendorCount;

    // Data & Services
    private SessionManager sessionManager;
    private DatabaseReference favoritesRef;
    private FavoriteOrdersAdapter adapter;
    private List<FavoriteOrder> allFavorites;
    private List<FavoriteOrder> filteredFavorites;
    private String currentFilter = "ALL";
    private String currentSearchQuery = "";
    
    // Animation and UI state
    private Handler mainHandler;
    private boolean isEmptyStateVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_orders);

        initializeServices();
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupSearchAndFilter();
        setupSwipeRefresh();
        loadFavorites();
    }

    private void initializeServices() {
        sessionManager = new SessionManager(this);
        mainHandler = new Handler(Looper.getMainLooper());
        
        // Initialize Firebase reference
        String userId = sessionManager.getUserId();
        if (userId != null) {
            favoritesRef = FirebaseDatabase.getInstance()
                    .getReference("favorites")
                    .child(userId);
        }
        
        allFavorites = new ArrayList<>();
        filteredFavorites = new ArrayList<>();
    }

    private void initializeViews() {
        try {
            toolbar = findViewById(R.id.toolbar);
            swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
            etSearch = findViewById(R.id.et_search);
            chipGroupFilter = findViewById(R.id.chip_group_filter);
            chipAll = findViewById(R.id.chip_all);
            chipFood = findViewById(R.id.chip_food);
            chipVendors = findViewById(R.id.chip_vendors);
            chipRecent = findViewById(R.id.chip_recent);
            progressIndicator = findViewById(R.id.progress_indicator);
            cardEmptyState = findViewById(R.id.card_empty_state);
            cardStats = findViewById(R.id.card_stats);
            rvFavorites = findViewById(R.id.rv_favorites);
            btnStartExploring = findViewById(R.id.btn_start_exploring);
            fabMultiSelect = findViewById(R.id.fab_multi_select);
            
            // Initialize empty state views
            if (cardEmptyState != null) {
                ivEmptyIcon = cardEmptyState.findViewById(R.id.iv_empty_icon);
                tvEmptyTitle = cardEmptyState.findViewById(R.id.tv_empty_title);
                tvEmptyMessage = cardEmptyState.findViewById(R.id.tv_empty_message);
            }
            
            // Initialize stats views
            if (cardStats != null) {
                tvTotalFavorites = cardStats.findViewById(R.id.tv_total_favorites);
                tvFoodCount = cardStats.findViewById(R.id.tv_food_count);
                tvVendorCount = cardStats.findViewById(R.id.tv_vendor_count);
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
        adapter = new FavoriteOrdersAdapter(this, filteredFavorites, this);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Start Exploring button
        if (btnStartExploring != null) {
            btnStartExploring.setOnClickListener(v -> {
                animateButtonPress(btnStartExploring, () -> {
                    Intent intent = new Intent(this, CustomerHomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                });
            });
        }
        
        // Multi-select FAB
        if (fabMultiSelect != null) {
            fabMultiSelect.setOnClickListener(v -> {
                if (adapter.isMultiSelectMode()) {
                    exitMultiSelectMode();
                } else {
                    enterMultiSelectMode();
                }
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
                        } else if (checkedId == R.id.chip_food) {
                            currentFilter = "FOOD";
                        } else if (checkedId == R.id.chip_vendors) {
                            currentFilter = "VENDOR";
                        } else if (checkedId == R.id.chip_recent) {
                            currentFilter = "RECENT";
                        }
                        applyFiltersAndSearch();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up search and filter", e);
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
                loadFavorites();
                
                mainHandler.postDelayed(() -> {
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            });
        }
    }

    private void loadFavorites() {
        if (favoritesRef == null) {
            Log.w(TAG, "User not logged in, showing empty state");
            showEmptyState();
            return;
        }

        showProgress(true);
        
        favoritesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allFavorites.clear();
                
                for (DataSnapshot favoriteSnapshot : snapshot.getChildren()) {
                    FavoriteOrder favorite = favoriteSnapshot.getValue(FavoriteOrder.class);
                    if (favorite != null) {
                        allFavorites.add(favorite);
                    }
                }
                
                // Sort by added date (most recent first)
                Collections.sort(allFavorites, (f1, f2) -> 
                    Long.compare(f2.getAddedDate(), f1.getAddedDate()));
                
                applyFiltersAndSearch();
                updateStatistics();
                showProgress(false);
                
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load favorites", error.toException());
                showProgress(false);
                
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                
                showError("Failed to load favorites: " + error.getMessage());
            }
        });
    }

    private void applyFiltersAndSearch() {
        filteredFavorites.clear();
        
        for (FavoriteOrder favorite : allFavorites) {
            boolean matchesFilter = false;
            boolean matchesSearch = false;
            
            // Apply type filter
            switch (currentFilter) {
                case "ALL":
                    matchesFilter = true;
                    break;
                case "FOOD":
                    matchesFilter = "FOOD".equals(favorite.getType());
                    break;
                case "VENDOR":
                    matchesFilter = "VENDOR".equals(favorite.getType());
                    break;
                case "RECENT":
                    // Items added in last 7 days
                    long weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L);
                    matchesFilter = favorite.getAddedDate() > weekAgo;
                    break;
            }
            
            // Apply search filter
            if (currentSearchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                String query = currentSearchQuery.toLowerCase();
                matchesSearch = (favorite.getItemName() != null && favorite.getItemName().toLowerCase().contains(query)) ||
                               (favorite.getVendorName() != null && favorite.getVendorName().toLowerCase().contains(query)) ||
                               (favorite.getCategory() != null && favorite.getCategory().toLowerCase().contains(query));
            }
            
            if (matchesFilter && matchesSearch) {
                filteredFavorites.add(favorite);
            }
        }
        
        updateUI();
    }

    private void updateUI() {
        if (filteredFavorites.isEmpty()) {
            if (allFavorites.isEmpty()) {
                showEmptyState();
                hideStatistics();
            } else {
                showNoResultsState();
                showStatistics();
            }
        } else {
            hideEmptyState();
            showStatistics();
            adapter.updateFavorites(filteredFavorites);
        }
    }

    private void updateStatistics() {
        if (tvTotalFavorites != null && tvFoodCount != null && tvVendorCount != null) {
            int totalFavorites = allFavorites.size();
            int foodCount = 0;
            int vendorCount = 0;
            
            for (FavoriteOrder favorite : allFavorites) {
                if ("FOOD".equals(favorite.getType())) {
                    foodCount++;
                } else if ("VENDOR".equals(favorite.getType())) {
                    vendorCount++;
                }
            }
            
            tvTotalFavorites.setText(String.valueOf(totalFavorites));
            tvFoodCount.setText(String.valueOf(foodCount));
            tvVendorCount.setText(String.valueOf(vendorCount));
        }
    }

    private void showEmptyState() {
        try {
            if (cardEmptyState != null && !isEmptyStateVisible) {
                isEmptyStateVisible = true;
                cardEmptyState.setVisibility(View.VISIBLE);
                animateEmptyStateIn();
            }
            
            if (rvFavorites != null) {
                rvFavorites.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing empty state", e);
        }
    }

    private void showNoResultsState() {
        try {
            if (cardEmptyState != null) {
                cardEmptyState.setVisibility(View.VISIBLE);
                
                if (tvEmptyTitle != null && tvEmptyMessage != null) {
                    tvEmptyTitle.setText("No Results Found");
                    tvEmptyMessage.setText("Try adjusting your search or filter criteria to find your favorite items.");
                }
                
                if (btnStartExploring != null) {
                    btnStartExploring.setVisibility(View.GONE);
                }
            }
            
            if (rvFavorites != null) {
                rvFavorites.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing no results state", e);
        }
    }

    private void hideEmptyState() {
        try {
            if (cardEmptyState != null && isEmptyStateVisible) {
                isEmptyStateVisible = false;
                animateEmptyStateOut(() -> {
                    if (cardEmptyState != null) {
                        cardEmptyState.setVisibility(View.GONE);
                    }
                });
            }
            
            if (rvFavorites != null) {
                rvFavorites.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error hiding empty state", e);
        }
    }

    private void showStatistics() {
        if (cardStats != null && !allFavorites.isEmpty()) {
            cardStats.setVisibility(View.VISIBLE);
        }
    }

    private void hideStatistics() {
        if (cardStats != null) {
            cardStats.setVisibility(View.GONE);
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

    private void enterMultiSelectMode() {
        adapter.toggleMultiSelectMode();
        fabMultiSelect.setText("Cancel Selection");
        fabMultiSelect.setIcon(getDrawable(R.drawable.ic_close));
        invalidateOptionsMenu();
    }

    private void exitMultiSelectMode() {
        adapter.exitMultiSelectMode();
        fabMultiSelect.setText("Select Multiple");
        fabMultiSelect.setIcon(getDrawable(R.drawable.ic_checklist));
        invalidateOptionsMenu();
    }

    // Animation Methods
    private void animateEmptyStateIn() {
        if (cardEmptyState == null) return;
        
        cardEmptyState.setAlpha(0f);
        cardEmptyState.setScaleX(0.8f);
        cardEmptyState.setScaleY(0.8f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(cardEmptyState, "alpha", 0f, 1f);
        ObjectAnimator scaleXIn = ObjectAnimator.ofFloat(cardEmptyState, "scaleX", 0.8f, 1f);
        ObjectAnimator scaleYIn = ObjectAnimator.ofFloat(cardEmptyState, "scaleY", 0.8f, 1f);
        
        animatorSet.playTogether(fadeIn, scaleXIn, scaleYIn);
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
        
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
        
        ObjectAnimator bounceY = ObjectAnimator.ofFloat(ivEmptyIcon, "translationY", 0f, -30f, 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(ivEmptyIcon, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(ivEmptyIcon, "scaleY", 1f, 1.2f, 1f);
        
        AnimatorSet bounceSet = new AnimatorSet();
        bounceSet.playTogether(bounceY, scaleX, scaleY);
        bounceSet.setDuration(800);
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

    // FavoriteOrdersAdapter.OnFavoriteActionListener implementation
    @Override
    public void onFavoriteClicked(FavoriteOrder favorite) {
        // Open favorite details or vendor menu
        Toast.makeText(this, "Opening " + favorite.getItemName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteToggled(FavoriteOrder favorite, boolean isRemoved) {
        if (isRemoved) {
            // Remove from Firebase
            if (favoritesRef != null) {
                favoritesRef.child(favorite.getFavoriteId()).removeValue()
                    .addOnSuccessListener(aVoid -> showSuccess("Removed from favorites"))
                    .addOnFailureListener(e -> showError("Failed to remove favorite"));
            }
        }
    }

    @Override
    public void onReorderClicked(FavoriteOrder favorite) {
        // Add to cart and navigate to checkout
        showSuccess("Added " + favorite.getItemName() + " to cart");
        Toast.makeText(this, "Reorder functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewDetailsClicked(FavoriteOrder favorite) {
        // Navigate to item details or vendor menu
        Toast.makeText(this, "View details functionality will be implemented", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            fabMultiSelect.setText("Remove Selected (" + selectedCount + ")");
            fabMultiSelect.setIcon(getDrawable(R.drawable.ic_delete));
        } else {
            fabMultiSelect.setText("Cancel Selection");
            fabMultiSelect.setIcon(getDrawable(R.drawable.ic_close));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem selectAll = menu.findItem(R.id.action_select_all);
        MenuItem deleteSelected = menu.findItem(R.id.action_delete_selected);
        
        if (adapter != null && adapter.isMultiSelectMode()) {
            selectAll.setVisible(true);
            deleteSelected.setVisible(true);
        } else {
            selectAll.setVisible(false);
            deleteSelected.setVisible(false);
        }
        
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_select_all) {
            adapter.selectAll();
            return true;
        } else if (id == R.id.action_delete_selected) {
            deleteSelectedFavorites();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void deleteSelectedFavorites() {
        List<FavoriteOrder> selectedItems = adapter.getSelectedItems();
        if (selectedItems.isEmpty()) {
            showError("No items selected");
            return;
        }
        
        // Remove selected items from Firebase
        if (favoritesRef != null) {
            for (FavoriteOrder favorite : selectedItems) {
                favoritesRef.child(favorite.getFavoriteId()).removeValue();
            }
            showSuccess("Removed " + selectedItems.size() + " favorites");
            exitMultiSelectMode();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
    }
}
