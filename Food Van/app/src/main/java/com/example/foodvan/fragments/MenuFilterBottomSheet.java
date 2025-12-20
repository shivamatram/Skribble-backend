package com.example.foodvan.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.adapters.FilterPreviewAdapter;
import com.example.foodvan.models.MenuFilter;
import com.example.foodvan.models.MenuItem;
import com.example.foodvan.utils.FilterUtils;
import com.example.foodvan.viewmodels.MenuFilterViewModel;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet fragment for menu filtering and sorting
 */
public class MenuFilterBottomSheet extends BottomSheetDialogFragment {
    
    private static final String TAG = "MenuFilterBottomSheet";
    private static final int SEARCH_DEBOUNCE_DELAY = 300; // milliseconds
    
    // UI Components
    private TextInputEditText etSearchFilter;
    private ChipGroup chipGroupCategories;
    private ChipGroup chipGroupAvailability;
    private TextInputEditText etPriceMin, etPriceMax;
    private RangeSlider sliderPriceRange;
    private RadioGroup radioGroupSort;
    private MaterialCardView cardFilterPreview;
    private MaterialTextView tvFilterResultsCount;
    private RecyclerView rvFilterPreview;
    private MaterialButton btnClearAll, btnCloseFilter, btnResetFilter, btnSavePreset, btnApplyFilter;
    
    // Data
    private MenuFilterViewModel viewModel;
    private FilterPreviewAdapter previewAdapter;
    private List<MenuItem> allMenuItems = new ArrayList<>();
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private TextWatcher priceWatcher;
    
    // Callbacks
    private OnFilterAppliedListener filterAppliedListener;
    
    // Runnables
    private final Runnable priceUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updatePriceRangeFromInputs();
        }
    };
    
    public interface OnFilterAppliedListener {
        void onFilterApplied(MenuFilter filter, List<MenuItem> filteredItems);
    }
    
    public static MenuFilterBottomSheet newInstance() {
        return new MenuFilterBottomSheet();
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Initialize ViewModel with this fragment's ViewModelProvider instead of requireActivity()
            // This prevents crashes when the activity context is not yet available
            viewModel = new ViewModelProvider(this).get(MenuFilterViewModel.class);
            android.util.Log.d(TAG, "MenuFilterBottomSheet created successfully");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error creating MenuFilterBottomSheet", e);
            // Initialize with default empty ViewModel if creation fails
            try {
                viewModel = new ViewModelProvider(this).get(MenuFilterViewModel.class);
            } catch (Exception ex) {
                android.util.Log.e(TAG, "Failed to create fallback ViewModel", ex);
            }
        }
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        
        // Set peek height and behavior
        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog d = (BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setPeekHeight(800);
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        
        return dialog;
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_menu_filter, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        try {
            android.util.Log.d(TAG, "Setting up filter bottom sheet views");
            initializeViews(view);
            setupRecyclerView();
            setupClickListeners();
            setupObservers();
            loadInitialData();
            android.util.Log.d(TAG, "Filter bottom sheet setup completed");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error setting up filter bottom sheet", e);
            if (getContext() != null) {
                Toast.makeText(getContext(), "Error loading filter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void initializeViews(View view) {
        try {
            etSearchFilter = view.findViewById(R.id.et_search_filter);
            chipGroupCategories = view.findViewById(R.id.chip_group_categories);
            chipGroupAvailability = view.findViewById(R.id.chip_group_availability);
            etPriceMin = view.findViewById(R.id.et_price_min);
            etPriceMax = view.findViewById(R.id.et_price_max);
            sliderPriceRange = view.findViewById(R.id.slider_price_range);
            radioGroupSort = view.findViewById(R.id.radio_group_sort);
            cardFilterPreview = view.findViewById(R.id.card_filter_preview);
            tvFilterResultsCount = view.findViewById(R.id.tv_filter_results_count);
            rvFilterPreview = view.findViewById(R.id.rv_filter_preview);
            btnClearAll = view.findViewById(R.id.btn_clear_all);
            btnCloseFilter = view.findViewById(R.id.btn_close_filter);
            btnResetFilter = view.findViewById(R.id.btn_reset_filter);
            btnSavePreset = view.findViewById(R.id.btn_save_preset);
            btnApplyFilter = view.findViewById(R.id.btn_apply_filter);
            
            // Log which views were found/not found
            android.util.Log.d(TAG, "Views initialized - Search: " + (etSearchFilter != null) + 
                ", Categories: " + (chipGroupCategories != null) + 
                ", Availability: " + (chipGroupAvailability != null) +
                ", Price Slider: " + (sliderPriceRange != null) +
                ", Sort: " + (radioGroupSort != null) +
                ", Buttons: " + (btnApplyFilter != null));
                
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error initializing views", e);
            throw e;
        }
    }
    
    private void setupRecyclerView() {
        if (rvFilterPreview != null && getContext() != null) {
            previewAdapter = new FilterPreviewAdapter();
            rvFilterPreview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            rvFilterPreview.setAdapter(previewAdapter);
        }
    }
    
    private void setupClickListeners() {
        // Check if viewModel is null
        if (viewModel == null) {
            android.util.Log.e(TAG, "ViewModel is null, cannot setup click listeners");
            return;
        }
        
        // Header buttons
        if (btnClearAll != null) btnClearAll.setOnClickListener(v -> clearAllFilters());
        if (btnCloseFilter != null) btnCloseFilter.setOnClickListener(v -> dismiss());
        
        // Action buttons
        if (btnResetFilter != null) btnResetFilter.setOnClickListener(v -> resetFilters());
        if (btnSavePreset != null) btnSavePreset.setOnClickListener(v -> showSavePresetDialog());
        if (btnApplyFilter != null) btnApplyFilter.setOnClickListener(v -> applyFilters());
        
        // Search with debounce
        if (etSearchFilter != null) {
        etSearchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                searchRunnable = () -> {
                    String query = s.toString().trim();
                    viewModel.updateSearchQuery(query);
                };
                
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
        }
        
        // Category chips
        if (chipGroupCategories != null) {
        chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> {
            List<String> selectedCategories = new ArrayList<>();
            boolean allSelected = false;
            
            for (int id : checkedIds) {
                Chip chip = group.findViewById(id);
                if (chip != null) {
                    String category = chip.getText().toString();
                    if ("All".equals(category)) {
                        allSelected = true;
                        selectedCategories.clear();
                        break;
                    } else {
                        selectedCategories.add(category);
                    }
                }
            }
            
            viewModel.updateSelectedCategories(selectedCategories, allSelected);
        });
        }
        
        // Availability chips
        if (chipGroupAvailability != null) {
        chipGroupAvailability.setOnCheckedStateChangeListener((group, checkedIds) -> {
            boolean showAvailable = false;
            boolean showOutOfStock = false;
            
            for (int id : checkedIds) {
                if (id == R.id.chip_available) {
                    showAvailable = true;
                } else if (id == R.id.chip_out_of_stock) {
                    showOutOfStock = true;
                }
            }
            
            // If nothing selected, show both
            if (!showAvailable && !showOutOfStock) {
                showAvailable = true;
                showOutOfStock = true;
            }
            
            viewModel.updateAvailabilityFilter(showAvailable, showOutOfStock);
        });
        }
        
        // Price range slider
        if (sliderPriceRange != null) {
        sliderPriceRange.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                List<Float> values = slider.getValues();
                if (values.size() >= 2) {
                    float minPrice = values.get(0);
                    float maxPrice = values.get(1);
                    
                    etPriceMin.setText(String.valueOf((int) minPrice));
                    etPriceMax.setText(String.valueOf((int) maxPrice));
                    
                    viewModel.updatePriceRange(minPrice, maxPrice);
                }
            }
        });
        }
        
        // Price input fields - store as instance variable to remove/add later
        priceWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                // Debounce to avoid excessive updates
                searchHandler.removeCallbacks(priceUpdateRunnable);
                searchHandler.postDelayed(priceUpdateRunnable, 500);
            }
        };
        
        if (etPriceMin != null) etPriceMin.addTextChangedListener(priceWatcher);
        if (etPriceMax != null) etPriceMax.addTextChangedListener(priceWatcher);
        
        // Sort options
        if (radioGroupSort != null) {
        radioGroupSort.setOnCheckedChangeListener((group, checkedId) -> {
            MenuFilter.SortOption sortOption = MenuFilter.SortOption.NEWEST;
            
            if (checkedId == R.id.radio_sort_newest) {
                sortOption = MenuFilter.SortOption.NEWEST;
            } else if (checkedId == R.id.radio_sort_popularity) {
                sortOption = MenuFilter.SortOption.POPULARITY;
            } else if (checkedId == R.id.radio_sort_price_low) {
                sortOption = MenuFilter.SortOption.PRICE_LOW_TO_HIGH;
            } else if (checkedId == R.id.radio_sort_price_high) {
                sortOption = MenuFilter.SortOption.PRICE_HIGH_TO_LOW;
            } else if (checkedId == R.id.radio_sort_name_az) {
                sortOption = MenuFilter.SortOption.NAME_A_TO_Z;
            } else if (checkedId == R.id.radio_sort_name_za) {
                sortOption = MenuFilter.SortOption.NAME_Z_TO_A;
            }
            
            viewModel.updateSortOption(sortOption);
        });
        }
    }
    
    private void setupObservers() {
        // Check if viewModel is null
        if (viewModel == null) {
            android.util.Log.e(TAG, "ViewModel is null, cannot setup observers");
            return;
        }
        
        // Observe current filter
        viewModel.getCurrentFilter().observe(getViewLifecycleOwner(), this::updateUIFromFilter);
        
        // Observe filtered items count
        viewModel.getFilteredItemCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && tvFilterResultsCount != null) {
                tvFilterResultsCount.setText(getString(R.string.showing_items_count, count));
            }
        });
        
        // Observe preview items
        viewModel.getPreviewItems().observe(getViewLifecycleOwner(), items -> {
            if (items != null && previewAdapter != null) {
                previewAdapter.setItems(items);
                if (cardFilterPreview != null) {
                    cardFilterPreview.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
                }
            }
        });
        
        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (btnApplyFilter != null) btnApplyFilter.setEnabled(!isLoading);
                if (btnSavePreset != null) btnSavePreset.setEnabled(!isLoading);
            }
        });
        
        // Observe errors
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getContext() != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void loadInitialData() {
        // Check if viewModel is null
        if (viewModel == null) {
            android.util.Log.e(TAG, "ViewModel is null, cannot load initial data");
            return;
        }
        
        // Set menu items in ViewModel on main thread (LiveData requires main thread)
        searchHandler.post(() -> {
            try {
                viewModel.setAllMenuItems(allMenuItems);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error setting menu items in ViewModel", e);
            }
        });
        
        // Setup price range based on available items - post to avoid blocking
        if (!allMenuItems.isEmpty() && sliderPriceRange != null) {
            searchHandler.post(() -> {
                try {
                    float[] priceRange = FilterUtils.getPriceRange(allMenuItems);
                    if (sliderPriceRange != null) {
                        sliderPriceRange.setValueFrom(priceRange[0]);
                        sliderPriceRange.setValueTo(priceRange[1]);
                    }
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error setting price range", e);
                }
            });
        }
        
        // Setup categories based on available items
        setupCategoryChips();
    }
    
    private void setupCategoryChips() {
        List<String> categories = FilterUtils.getAvailableCategories(allMenuItems);
        
        // Clear existing chips except "All"
        for (int i = chipGroupCategories.getChildCount() - 1; i >= 0; i--) {
            View child = chipGroupCategories.getChildAt(i);
            if (child.getId() != R.id.chip_all_categories) {
                chipGroupCategories.removeView(child);
            }
        }
        
        // Add category chips
        for (String category : categories) {
            Chip chip = new Chip(requireContext());
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setChipStrokeColorResource(R.color.primary_color);
            chipGroupCategories.addView(chip);
        }
    }
    
    private void updateUIFromFilter(MenuFilter filter) {
        if (filter == null) return;
        
        // Update search
        if (!filter.getSearchQuery().equals(etSearchFilter.getText().toString())) {
            etSearchFilter.setText(filter.getSearchQuery());
        }
        
        // Update categories
        updateCategoryChips(filter);
        
        // Update availability
        updateAvailabilityChips(filter);
        
        // Update price range
        updatePriceRange(filter);
        
        // Update sort option
        updateSortOption(filter);
    }
    
    private void updateCategoryChips(MenuFilter filter) {
        chipGroupCategories.clearCheck();
        
        if (filter.isAllCategoriesSelected()) {
            chipGroupCategories.check(R.id.chip_all_categories);
        } else {
            for (String category : filter.getSelectedCategories()) {
                for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                    View child = chipGroupCategories.getChildAt(i);
                    if (child instanceof Chip) {
                        Chip chip = (Chip) child;
                        if (category.equals(chip.getText().toString())) {
                            chip.setChecked(true);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void updateAvailabilityChips(MenuFilter filter) {
        chipGroupAvailability.clearCheck();
        
        if (filter.isShowAvailable()) {
            chipGroupAvailability.check(R.id.chip_available);
        }
        if (filter.isShowOutOfStock()) {
            chipGroupAvailability.check(R.id.chip_out_of_stock);
        }
    }
    
    private void updatePriceRange(MenuFilter filter) {
        // Update text fields without triggering text watchers
        if (etPriceMin != null) {
            etPriceMin.removeTextChangedListener(priceWatcher);
            etPriceMin.setText(String.valueOf((int) filter.getMinPrice()));
            etPriceMin.addTextChangedListener(priceWatcher);
        }
        
        if (etPriceMax != null) {
            etPriceMax.removeTextChangedListener(priceWatcher);
            etPriceMax.setText(String.valueOf((int) filter.getMaxPrice()));
            etPriceMax.addTextChangedListener(priceWatcher);
        }
        
        // Update slider
        if (sliderPriceRange != null) {
            try {
                List<Float> values = new ArrayList<>();
                values.add(filter.getMinPrice());
                values.add(filter.getMaxPrice());
                sliderPriceRange.setValues(values);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error updating price range slider", e);
            }
        }
    }
    
    private void updateSortOption(MenuFilter filter) {
        int radioId = R.id.radio_sort_newest;
        
        switch (filter.getSortBy()) {
            case POPULARITY:
                radioId = R.id.radio_sort_popularity;
                break;
            case PRICE_LOW_TO_HIGH:
                radioId = R.id.radio_sort_price_low;
                break;
            case PRICE_HIGH_TO_LOW:
                radioId = R.id.radio_sort_price_high;
                break;
            case NAME_A_TO_Z:
                radioId = R.id.radio_sort_name_az;
                break;
            case NAME_Z_TO_A:
                radioId = R.id.radio_sort_name_za;
                break;
            case NEWEST:
            default:
                radioId = R.id.radio_sort_newest;
                break;
        }
        
        radioGroupSort.check(radioId);
    }
    
    private void updatePriceRangeFromInputs() {
        try {
            String minText = etPriceMin.getText().toString().trim();
            String maxText = etPriceMax.getText().toString().trim();
            
            if (!minText.isEmpty() && !maxText.isEmpty()) {
                float minPrice = Float.parseFloat(minText);
                float maxPrice = Float.parseFloat(maxText);
                
                if (minPrice >= 0 && maxPrice >= 0 && minPrice <= maxPrice) {
                    List<Float> values = new ArrayList<>();
                    values.add(minPrice);
                    values.add(maxPrice);
                    sliderPriceRange.setValues(values);
                    
                    viewModel.updatePriceRange(minPrice, maxPrice);
                }
            }
        } catch (NumberFormatException e) {
            // Ignore invalid input
        }
    }
    
    private void clearAllFilters() {
        viewModel.resetFilter();
    }
    
    private void resetFilters() {
        viewModel.resetFilter();
    }
    
    private void showSavePresetDialog() {
        // TODO: Implement save preset dialog
        Toast.makeText(getContext(), "Save preset feature coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void applyFilters() {
        MenuFilter currentFilter = viewModel.getCurrentFilter().getValue();
        List<MenuItem> filteredItems = viewModel.getFilteredMenuItems().getValue();
        
        if (filterAppliedListener != null && currentFilter != null && filteredItems != null) {
            filterAppliedListener.onFilterApplied(currentFilter, filteredItems);
        }
        
        dismiss();
    }
    
    // Public methods
    
    public void setMenuItems(List<MenuItem> items) {
        this.allMenuItems = items != null ? items : new ArrayList<>();
    }
    
    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.filterAppliedListener = listener;
    }
    
    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        
        // Clean up handlers and callbacks
        if (searchHandler != null) {
            if (searchRunnable != null) {
                searchHandler.removeCallbacks(searchRunnable);
            }
            searchHandler.removeCallbacks(priceUpdateRunnable);
        }
        
        // Remove text watchers to prevent memory leaks
        if (priceWatcher != null) {
            if (etPriceMin != null) etPriceMin.removeTextChangedListener(priceWatcher);
            if (etPriceMax != null) etPriceMax.removeTextChangedListener(priceWatcher);
        }
    }
}
