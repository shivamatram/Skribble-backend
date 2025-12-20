package com.example.foodvan.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.foodvan.models.MenuFilter;
import com.example.foodvan.models.MenuItem;
import com.example.foodvan.repositories.MenuFilterRepository;
import com.example.foodvan.utils.FilterUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for Menu Filter functionality
 * Manages filter state and applies filters to menu items
 */
public class MenuFilterViewModel extends AndroidViewModel {
    
    private final MenuFilterRepository repository;
    private final ExecutorService executor;
    
    // Filter state
    private final MutableLiveData<MenuFilter> currentFilter = new MutableLiveData<>(new MenuFilter());
    private final MutableLiveData<List<MenuItem>> allMenuItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<MenuItem>> filteredMenuItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Filter preview
    private final MutableLiveData<Integer> filteredItemCount = new MutableLiveData<>(0);
    private final MutableLiveData<List<MenuItem>> previewItems = new MutableLiveData<>(new ArrayList<>());
    
    // Filter presets
    private final MutableLiveData<List<MenuFilter>> savedPresets = new MutableLiveData<>(new ArrayList<>());
    
    public MenuFilterViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MenuFilterRepository(application);
        this.executor = Executors.newFixedThreadPool(2);
        
        // Initialize with default filter
        currentFilter.setValue(new MenuFilter());
        
        // Load saved filter from preferences
        loadLastUsedFilter();
        
        // Load saved presets
        loadSavedPresets();
    }
    
    // Public methods for UI
    
    /**
     * Set the complete list of menu items to filter
     */
    public void setAllMenuItems(List<MenuItem> items) {
        allMenuItems.setValue(items != null ? items : new ArrayList<>());
        applyCurrentFilter();
    }
    
    /**
     * Update search query and apply filter
     */
    public void updateSearchQuery(String query) {
        MenuFilter filter = getCurrentFilterValue();
        filter.setSearchQuery(query);
        currentFilter.setValue(filter);
        applyCurrentFilter();
    }
    
    /**
     * Update selected categories and apply filter
     */
    public void updateSelectedCategories(List<String> categories, boolean allSelected) {
        MenuFilter filter = getCurrentFilterValue();
        filter.setSelectedCategories(categories);
        filter.setAllCategoriesSelected(allSelected);
        currentFilter.setValue(filter);
        applyCurrentFilter();
    }
    
    /**
     * Update availability filters
     */
    public void updateAvailabilityFilter(boolean showAvailable, boolean showOutOfStock) {
        MenuFilter filter = getCurrentFilterValue();
        filter.setShowAvailable(showAvailable);
        filter.setShowOutOfStock(showOutOfStock);
        currentFilter.setValue(filter);
        applyCurrentFilter();
    }
    
    /**
     * Update price range filter
     */
    public void updatePriceRange(float minPrice, float maxPrice) {
        MenuFilter filter = getCurrentFilterValue();
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.fixPriceRange(); // Ensure valid range
        currentFilter.setValue(filter);
        applyCurrentFilter();
    }
    
    /**
     * Update sort option
     */
    public void updateSortOption(MenuFilter.SortOption sortBy) {
        MenuFilter filter = getCurrentFilterValue();
        filter.setSortBy(sortBy);
        currentFilter.setValue(filter);
        applyCurrentFilter();
    }
    
    /**
     * Reset filter to default values
     */
    public void resetFilter() {
        MenuFilter filter = new MenuFilter();
        currentFilter.setValue(filter);
        applyCurrentFilter();
        
        // Clear saved last used filter
        executor.execute(() -> repository.clearLastUsedFilter());
    }
    
    /**
     * Apply current filter to menu items
     */
    public void applyCurrentFilter() {
        MenuFilter filter = getCurrentFilterValue();
        List<MenuItem> items = allMenuItems.getValue();
        
        if (items == null || items.isEmpty()) {
            filteredMenuItems.setValue(new ArrayList<>());
            updatePreview(new ArrayList<>());
            return;
        }
        
        executor.execute(() -> {
            try {
                // Apply filters
                List<MenuItem> filtered = FilterUtils.applyFilter(items, filter);
                
                // Update UI on main thread
                filteredMenuItems.postValue(filtered);
                updatePreview(filtered);
                
                // Save last used filter
                repository.saveLastUsedFilter(filter);
                
            } catch (Exception e) {
                errorMessage.postValue("Error applying filter: " + e.getMessage());
            }
        });
    }
    
    /**
     * Save current filter as a preset
     */
    public void saveFilterPreset(String presetName) {
        if (presetName == null || presetName.trim().isEmpty()) {
            errorMessage.setValue("Preset name cannot be empty");
            return;
        }
        
        MenuFilter filter = getCurrentFilterValue();
        filter.setPresetName(presetName.trim());
        filter.setCreatedAt(System.currentTimeMillis());
        
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                repository.saveFilterPreset(filter, new MenuFilterRepository.SavePresetCallback() {
                    @Override
                    public void onSuccess(String presetId) {
                        filter.setPresetId(presetId);
                        isLoading.postValue(false);
                        loadSavedPresets(); // Refresh presets list
                    }
                    
                    @Override
                    public void onError(String error) {
                        isLoading.postValue(false);
                        errorMessage.postValue("Failed to save preset: " + error);
                    }
                });
            } catch (Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error saving preset: " + e.getMessage());
            }
        });
    }
    
    /**
     * Load and apply a saved filter preset
     */
    public void loadFilterPreset(String presetId) {
        if (presetId == null || presetId.isEmpty()) return;
        
        isLoading.setValue(true);
        
        executor.execute(() -> {
            try {
                repository.loadFilterPreset(presetId, new MenuFilterRepository.LoadPresetCallback() {
                    @Override
                    public void onSuccess(MenuFilter filter) {
                        currentFilter.postValue(filter);
                        applyCurrentFilter();
                        isLoading.postValue(false);
                    }
                    
                    @Override
                    public void onError(String error) {
                        isLoading.postValue(false);
                        errorMessage.postValue("Failed to load preset: " + error);
                    }
                });
            } catch (Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Error loading preset: " + e.getMessage());
            }
        });
    }
    
    /**
     * Delete a saved filter preset
     */
    public void deleteFilterPreset(String presetId) {
        if (presetId == null || presetId.isEmpty()) return;
        
        executor.execute(() -> {
            try {
                repository.deleteFilterPreset(presetId, new MenuFilterRepository.DeletePresetCallback() {
                    @Override
                    public void onSuccess() {
                        loadSavedPresets(); // Refresh presets list
                    }
                    
                    @Override
                    public void onError(String error) {
                        errorMessage.postValue("Failed to delete preset: " + error);
                    }
                });
            } catch (Exception e) {
                errorMessage.postValue("Error deleting preset: " + e.getMessage());
            }
        });
    }
    
    // Private helper methods
    
    private MenuFilter getCurrentFilterValue() {
        MenuFilter filter = currentFilter.getValue();
        return filter != null ? new MenuFilter(filter) : new MenuFilter();
    }
    
    private void updatePreview(List<MenuItem> filtered) {
        filteredItemCount.postValue(filtered.size());
        
        // Show first 3 items as preview
        List<MenuItem> preview = new ArrayList<>();
        int previewCount = Math.min(3, filtered.size());
        for (int i = 0; i < previewCount; i++) {
            preview.add(filtered.get(i));
        }
        previewItems.postValue(preview);
    }
    
    private void loadLastUsedFilter() {
        executor.execute(() -> {
            try {
                MenuFilter lastFilter = repository.getLastUsedFilter();
                if (lastFilter != null) {
                    currentFilter.postValue(lastFilter);
                }
            } catch (Exception e) {
                // Ignore errors when loading last used filter
            }
        });
    }
    
    private void loadSavedPresets() {
        executor.execute(() -> {
            try {
                repository.loadAllFilterPresets(new MenuFilterRepository.LoadPresetsCallback() {
                    @Override
                    public void onSuccess(List<MenuFilter> presets) {
                        savedPresets.postValue(presets);
                    }
                    
                    @Override
                    public void onError(String error) {
                        // Don't show error for preset loading failures
                        savedPresets.postValue(new ArrayList<>());
                    }
                });
            } catch (Exception e) {
                savedPresets.postValue(new ArrayList<>());
            }
        });
    }
    
    // LiveData getters
    
    public LiveData<MenuFilter> getCurrentFilter() {
        return currentFilter;
    }
    
    public LiveData<List<MenuItem>> getFilteredMenuItems() {
        return filteredMenuItems;
    }
    
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    
    public LiveData<Integer> getFilteredItemCount() {
        return filteredItemCount;
    }
    
    public LiveData<List<MenuItem>> getPreviewItems() {
        return previewItems;
    }
    
    public LiveData<List<MenuFilter>> getSavedPresets() {
        return savedPresets;
    }
    
    // Computed LiveData
    
    public LiveData<Boolean> getHasActiveFilters() {
        return Transformations.map(currentFilter, MenuFilter::hasActiveFilters);
    }
    
    public LiveData<String> getFilterSummary() {
        return Transformations.map(currentFilter, filter -> {
            if (!filter.hasActiveFilters()) {
                return "No filters applied";
            }
            
            StringBuilder summary = new StringBuilder();
            
            if (!filter.getSearchQuery().isEmpty()) {
                summary.append("Search: ").append(filter.getSearchQuery()).append(" • ");
            }
            
            if (!filter.isAllCategoriesSelected() && !filter.getSelectedCategories().isEmpty()) {
                summary.append("Categories: ").append(filter.getSelectedCategories().size()).append(" • ");
            }
            
            if (!filter.isShowAvailable() || !filter.isShowOutOfStock()) {
                summary.append("Availability • ");
            }
            
            if (filter.getMinPrice() > 0 || filter.getMaxPrice() < 1000) {
                summary.append("Price: ₹").append((int)filter.getMinPrice())
                       .append("-₹").append((int)filter.getMaxPrice()).append(" • ");
            }
            
            if (filter.getSortBy() != MenuFilter.SortOption.NEWEST) {
                summary.append("Sorted • ");
            }
            
            // Remove trailing " • "
            if (summary.length() > 3) {
                summary.setLength(summary.length() - 3);
            }
            
            return summary.toString();
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
