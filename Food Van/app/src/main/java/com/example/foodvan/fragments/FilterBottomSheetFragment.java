package com.example.foodvan.fragments;

import android.animation.ObjectAnimator;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.foodvan.R;
import com.example.foodvan.models.FilterCriteria;
import com.example.foodvan.models.CuisineType;
import com.example.foodvan.models.PriceRange;
import com.example.foodvan.models.ServiceType;
import com.example.foodvan.models.SortBy;
import com.example.foodvan.models.SortOrder;
import com.example.foodvan.utils.FilterManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;

/**
 * Bottom sheet fragment for food van filtering and sorting
 */
public class FilterBottomSheetFragment extends BottomSheetDialogFragment {
    
    private static final String TAG = "FilterBottomSheet";
    private static final String ARG_FILTER_CRITERIA = "filter_criteria";
    
    // UI Components
    private TextView tvFilterTitle;
    private MaterialButton btnResetFilters;
    private ChipGroup chipGroupActiveFilters;
    private ChipGroup chipGroupCuisine;
    private MaterialButton btnExpandCuisine;
    private ChipGroup chipGroupPriceRange;
    private RangeSlider sliderPriceRange;
    private TextView tvMinPrice, tvMaxPrice;
    private ChipGroup chipGroupRating;
    private Slider sliderDistance;
    private TextView tvDistanceValue;
    private SwitchMaterial switchOpenNow;
    private ChipGroup chipGroupServiceType;
    private ChipGroup chipGroupSortBy;
    private MaterialButton btnSortOrder;
    private MaterialButton btnClearFilters;
    private MaterialButton btnApplyFilters;
    
    // Data
    private FilterCriteria filterCriteria;
    private FilterManager filterManager;
    private boolean isCuisineExpanded = false;
    
    // Callback interface
    public interface FilterApplyListener {
        void onFiltersApplied(FilterCriteria criteria);
        void onFiltersCleared();
    }
    
    private FilterApplyListener filterApplyListener;
    
    public static FilterBottomSheetFragment newInstance(FilterCriteria criteria) {
        Log.d(TAG, "Creating new FilterBottomSheetFragment instance");
        FilterBottomSheetFragment fragment = new FilterBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FILTER_CRITERIA, criteria);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "FilterBottomSheetFragment onCreate called");
        
        // Get filter criteria from arguments
        if (getArguments() != null) {
            filterCriteria = (FilterCriteria) getArguments().getSerializable(ARG_FILTER_CRITERIA);
        }
        
        if (filterCriteria == null) {
            filterCriteria = new FilterCriteria();
        }
        
        filterManager = new FilterManager(requireContext());
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "FilterBottomSheetFragment onCreateView called");
        try {
            View view = inflater.inflate(R.layout.fragment_filter_bottom_sheet, container, false);
            Log.d(TAG, "Layout inflated successfully");
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error inflating layout: " + e.getMessage());
            throw e;
        }
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "FilterBottomSheetFragment onViewCreated called");
        
        try {
            initializeViews(view);
            setupClickListeners();
            populateFilterOptions();
            applyCurrentFilters();
            setupAnimations();
            Log.d(TAG, "FilterBottomSheetFragment setup completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onViewCreated: " + e.getMessage());
        }
    }
    
    private void initializeViews(View view) {
        tvFilterTitle = view.findViewById(R.id.tv_filter_title);
        btnResetFilters = view.findViewById(R.id.btn_reset_filters);
        chipGroupActiveFilters = view.findViewById(R.id.chip_group_active_filters);
        chipGroupCuisine = view.findViewById(R.id.chip_group_cuisine);
        btnExpandCuisine = view.findViewById(R.id.btn_expand_cuisine);
        chipGroupPriceRange = view.findViewById(R.id.chip_group_price_range);
        sliderPriceRange = view.findViewById(R.id.slider_price_range);
        tvMinPrice = view.findViewById(R.id.tv_min_price);
        tvMaxPrice = view.findViewById(R.id.tv_max_price);
        chipGroupRating = view.findViewById(R.id.chip_group_rating);
        sliderDistance = view.findViewById(R.id.slider_distance);
        tvDistanceValue = view.findViewById(R.id.tv_distance_value);
        switchOpenNow = view.findViewById(R.id.switch_open_now);
        chipGroupServiceType = view.findViewById(R.id.chip_group_service_type);
        chipGroupSortBy = view.findViewById(R.id.chip_group_sort_by);
        btnSortOrder = view.findViewById(R.id.btn_sort_order);
        btnClearFilters = view.findViewById(R.id.btn_clear_filters);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
    }
    
    private void setupClickListeners() {
        btnResetFilters.setOnClickListener(v -> resetFilters());
        btnExpandCuisine.setOnClickListener(v -> toggleCuisineExpansion());
        btnSortOrder.setOnClickListener(v -> toggleSortOrder());
        btnClearFilters.setOnClickListener(v -> clearAllFilters());
        btnApplyFilters.setOnClickListener(v -> applyFilters());
        
        // Slider listeners
        sliderDistance.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                tvDistanceValue.setText((int) value + " km");
                filterCriteria.setMaxDistance(value);
            }
        });
        
        sliderPriceRange.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                List<Float> values = slider.getValues();
                if (values.size() >= 2) {
                    float min = values.get(0);
                    float max = values.get(1);
                    tvMinPrice.setText("₹" + (int) min);
                    tvMaxPrice.setText("₹" + (int) max);
                    filterCriteria.setMinPrice(min);
                    filterCriteria.setMaxPrice(max);
                }
            }
        });
        
        // Switch listener
        switchOpenNow.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterCriteria.setOnlyOpenVans(isChecked);
        });
    }
    
    private void populateFilterOptions() {
        populateCuisineChips();
        populatePriceRangeChips();
        populateRatingChips();
        populateServiceTypeChips();
        populateSortByChips();
    }
    
    private void populateCuisineChips() {
        chipGroupCuisine.removeAllViews();
        
        CuisineType[] cuisineTypes = CuisineType.getAllTypes();
        int maxVisible = 6; // Show only first 6 initially
        
        for (int i = 0; i < cuisineTypes.length; i++) {
            CuisineType cuisineType = cuisineTypes[i];
            Chip chip = createCuisineChip(cuisineType);
            
            // Hide chips beyond maxVisible if not expanded
            if (i >= maxVisible && !isCuisineExpanded) {
                chip.setVisibility(View.GONE);
            }
            
            chipGroupCuisine.addView(chip);
        }
        
        updateCuisineExpandButton();
    }
    
    private Chip createCuisineChip(CuisineType cuisineType) {
        Chip chip = new Chip(requireContext());
        chip.setText(cuisineType.getDisplayNameWithEmoji());
        chip.setCheckable(true);
        chip.setChipBackgroundColorResource(R.color.white);
        chip.setChipStrokeColorResource(R.color.gray_light);
        chip.setChipStrokeWidth(2f);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        
        // Set checked state based on current filter
        boolean isSelected = filterCriteria.getSelectedCuisines().contains(cuisineType);
        chip.setChecked(isSelected);
        
        if (isSelected) {
            chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(cuisineType.getColorCode())));
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
        
        chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            List<CuisineType> selected = new ArrayList<>(filterCriteria.getSelectedCuisines());
            
            if (isChecked) {
                if (!selected.contains(cuisineType)) {
                    selected.add(cuisineType);
                }
                chip.setChipBackgroundColor(ColorStateList.valueOf(Color.parseColor(cuisineType.getColorCode())));
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                selected.remove(cuisineType);
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
            
            filterCriteria.setSelectedCuisines(selected);
            updateActiveFiltersChips();
        });
        
        return chip;
    }
    
    private void populatePriceRangeChips() {
        chipGroupPriceRange.removeAllViews();
        
        PriceRange[] priceRanges = PriceRange.getAllRanges();
        
        for (PriceRange priceRange : priceRanges) {
            Chip chip = new Chip(requireContext());
            chip.setText(priceRange.getFullDisplayText());
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.gray_light);
            chip.setChipStrokeWidth(2f);
            
            // Set checked state
            boolean isSelected = filterCriteria.getPriceRange() == priceRange;
            chip.setChecked(isSelected);
            
            if (isSelected) {
                chip.setChipBackgroundColorResource(R.color.primary_red);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterCriteria.setPriceRange(priceRange);
                    updatePriceRangeChips();
                }
            });
            
            chipGroupPriceRange.addView(chip);
        }
    }
    
    private void populateRatingChips() {
        chipGroupRating.removeAllViews();
        
        String[] ratingOptions = {"Any", "3.0★+", "3.5★+", "4.0★+", "4.5★+"};
        float[] ratingValues = {0f, 3.0f, 3.5f, 4.0f, 4.5f};
        
        for (int i = 0; i < ratingOptions.length; i++) {
            Chip chip = new Chip(requireContext());
            chip.setText(ratingOptions[i]);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.gray_light);
            chip.setChipStrokeWidth(2f);
            
            final float ratingValue = ratingValues[i];
            
            // Set checked state
            boolean isSelected = filterCriteria.getMinRating() == ratingValue;
            chip.setChecked(isSelected);
            
            if (isSelected) {
                chip.setChipBackgroundColorResource(R.color.rating_yellow);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterCriteria.setMinRating(ratingValue);
                    updateRatingChips();
                }
            });
            
            chipGroupRating.addView(chip);
        }
    }
    
    private void populateServiceTypeChips() {
        chipGroupServiceType.removeAllViews();
        
        ServiceType[] serviceTypes = ServiceType.getAllTypes();
        
        for (ServiceType serviceType : serviceTypes) {
            Chip chip = new Chip(requireContext());
            chip.setText(serviceType.getDisplayNameWithEmoji());
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.gray_light);
            chip.setChipStrokeWidth(2f);
            
            // Set checked state
            boolean isSelected = filterCriteria.getServiceType() == serviceType;
            chip.setChecked(isSelected);
            
            if (isSelected) {
                chip.setChipBackgroundColorResource(R.color.primary_red);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterCriteria.setServiceType(serviceType);
                    updateServiceTypeChips();
                }
            });
            
            chipGroupServiceType.addView(chip);
        }
    }
    
    private void populateSortByChips() {
        chipGroupSortBy.removeAllViews();
        
        SortBy[] sortOptions = SortBy.getAllOptions();
        
        for (SortBy sortBy : sortOptions) {
            Chip chip = new Chip(requireContext());
            chip.setText(sortBy.getDisplayNameWithEmoji());
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.white);
            chip.setChipStrokeColorResource(R.color.gray_light);
            chip.setChipStrokeWidth(2f);
            
            // Set checked state
            boolean isSelected = filterCriteria.getSortBy() == sortBy;
            chip.setChecked(isSelected);
            
            if (isSelected) {
                chip.setChipBackgroundColorResource(R.color.primary_red);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    filterCriteria.setSortBy(sortBy);
                    updateSortByChips();
                }
            });
            
            chipGroupSortBy.addView(chip);
        }
    }
    
    private void applyCurrentFilters() {
        // Apply distance
        sliderDistance.setValue(filterCriteria.getMaxDistance());
        tvDistanceValue.setText((int) filterCriteria.getMaxDistance() + " km");
        
        // Apply price range
        List<Float> priceValues = new ArrayList<>();
        priceValues.add(filterCriteria.getMinPrice());
        priceValues.add(filterCriteria.getMaxPrice());
        sliderPriceRange.setValues(priceValues);
        tvMinPrice.setText("₹" + (int) filterCriteria.getMinPrice());
        tvMaxPrice.setText("₹" + (int) filterCriteria.getMaxPrice());
        
        // Apply availability
        switchOpenNow.setChecked(filterCriteria.isOnlyOpenVans());
        
        // Update sort order button
        updateSortOrderButton();
        
        // Update active filters
        updateActiveFiltersChips();
    }
    
    private void updateActiveFiltersChips() {
        chipGroupActiveFilters.removeAllViews();
        
        if (!filterCriteria.hasActiveFilters()) {
            chipGroupActiveFilters.setVisibility(View.GONE);
            return;
        }
        
        chipGroupActiveFilters.setVisibility(View.VISIBLE);
        
        // Add cuisine chips
        for (CuisineType cuisine : filterCriteria.getSelectedCuisines()) {
            Chip chip = createActiveFilterChip(cuisine.getDisplayName());
            chipGroupActiveFilters.addView(chip);
        }
        
        // Add other active filters
        if (filterCriteria.getMinRating() > 0) {
            Chip chip = createActiveFilterChip(filterCriteria.getMinRating() + "★+");
            chipGroupActiveFilters.addView(chip);
        }
        
        if (filterCriteria.getMaxDistance() < 10) {
            Chip chip = createActiveFilterChip("< " + (int) filterCriteria.getMaxDistance() + "km");
            chipGroupActiveFilters.addView(chip);
        }
        
        if (filterCriteria.isOnlyOpenVans()) {
            Chip chip = createActiveFilterChip("Open Now");
            chipGroupActiveFilters.addView(chip);
        }
    }
    
    private Chip createActiveFilterChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.primary_red);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        chip.setCloseIconVisible(true);
        chip.setCloseIconTintResource(R.color.white);
        
        chip.setOnCloseIconClickListener(v -> {
            // Handle removing specific filter
            chipGroupActiveFilters.removeView(chip);
            // You could add logic here to remove the specific filter
        });
        
        return chip;
    }
    
    private void toggleCuisineExpansion() {
        isCuisineExpanded = !isCuisineExpanded;
        
        // Show/hide additional cuisine chips
        int childCount = chipGroupCuisine.getChildCount();
        int maxVisible = 6;
        
        for (int i = maxVisible; i < childCount; i++) {
            View child = chipGroupCuisine.getChildAt(i);
            if (child != null) {
                child.setVisibility(isCuisineExpanded ? View.VISIBLE : View.GONE);
            }
        }
        
        updateCuisineExpandButton();
        animateExpandButton(btnExpandCuisine, isCuisineExpanded);
    }
    
    private void updateCuisineExpandButton() {
        if (isCuisineExpanded) {
            btnExpandCuisine.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_less));
        } else {
            btnExpandCuisine.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_expand_more));
        }
    }
    
    private void toggleSortOrder() {
        SortOrder currentOrder = filterCriteria.getSortOrder();
        SortOrder newOrder = (currentOrder == SortOrder.ASCENDING) ? SortOrder.DESCENDING : SortOrder.ASCENDING;
        filterCriteria.setSortOrder(newOrder);
        updateSortOrderButton();
        animateButton(btnSortOrder);
    }
    
    private void updateSortOrderButton() {
        SortOrder order = filterCriteria.getSortOrder();
        btnSortOrder.setText(order.getDisplayNameWithEmoji());
    }
    
    private void updatePriceRangeChips() {
        for (int i = 0; i < chipGroupPriceRange.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupPriceRange.getChildAt(i);
            if (chip.isChecked()) {
                chip.setChipBackgroundColorResource(R.color.primary_red);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }
    }
    
    private void updateRatingChips() {
        for (int i = 0; i < chipGroupRating.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupRating.getChildAt(i);
            if (chip.isChecked()) {
                chip.setChipBackgroundColorResource(R.color.rating_yellow);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }
    }
    
    private void updateServiceTypeChips() {
        for (int i = 0; i < chipGroupServiceType.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupServiceType.getChildAt(i);
            if (chip.isChecked()) {
                chip.setChipBackgroundColorResource(R.color.primary_red);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }
    }
    
    private void updateSortByChips() {
        for (int i = 0; i < chipGroupSortBy.getChildCount(); i++) {
            Chip chip = (Chip) chipGroupSortBy.getChildAt(i);
            if (chip.isChecked()) {
                chip.setChipBackgroundColorResource(R.color.primary_red);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            } else {
                chip.setChipBackgroundColorResource(R.color.white);
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }
    }
    
    private void resetFilters() {
        filterCriteria.reset();
        applyCurrentFilters();
        populateFilterOptions();
        animateButton(btnResetFilters);
    }
    
    private void clearAllFilters() {
        resetFilters();
        if (filterApplyListener != null) {
            filterApplyListener.onFiltersCleared();
        }
        dismiss();
    }
    
    private void applyFilters() {
        if (filterApplyListener != null) {
            filterApplyListener.onFiltersApplied(filterCriteria);
        }
        animateButton(btnApplyFilters);
        dismiss();
    }
    
    private void setupAnimations() {
        // Add entrance animation for the bottom sheet content
        View content = getView();
        if (content != null) {
            content.setAlpha(0f);
            content.setTranslationY(100f);
            content.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }
    
    private void animateButton(View button) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.95f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.95f, 1f);
        scaleX.setDuration(150);
        scaleY.setDuration(150);
        scaleX.start();
        scaleY.start();
    }
    
    private void animateExpandButton(View button, boolean expanded) {
        float rotation = expanded ? 180f : 0f;
        ObjectAnimator rotateAnimator = ObjectAnimator.ofFloat(button, "rotation", rotation);
        rotateAnimator.setDuration(200);
        rotateAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotateAnimator.start();
    }
    
    public void setFilterApplyListener(FilterApplyListener listener) {
        this.filterApplyListener = listener;
    }
}
