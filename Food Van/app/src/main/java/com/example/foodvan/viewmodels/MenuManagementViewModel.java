package com.example.foodvan.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.foodvan.models.MenuItem;
import com.example.foodvan.repositories.MenuRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MenuManagementViewModel extends AndroidViewModel {

    private final MenuRepository menuRepository;
    
    // LiveData for menu items
    private final MutableLiveData<List<MenuItem>> allMenuItems = new MutableLiveData<>();
    private final MutableLiveData<List<MenuItem>> filteredMenuItems = new MutableLiveData<>();
    
    // Filter and sort state
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");
    private final MutableLiveData<String> availabilityFilter = new MutableLiveData<>("All");
    private final MutableLiveData<Float> minPrice = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> maxPrice = new MutableLiveData<>(1000f);
    private final MutableLiveData<SortOption> sortOption = new MutableLiveData<>(SortOption.NEWEST);
    
    // Bulk action state
    private final MutableLiveData<Boolean> isBulkMode = new MutableLiveData<>(false);
    private final MutableLiveData<Set<String>> selectedItems = new MutableLiveData<>();
    
    // Loading and error states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    
    // Import/Export state
    private final MutableLiveData<Integer> importProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> exportProgress = new MutableLiveData<>(0);
    private final MutableLiveData<String> importStatus = new MutableLiveData<>();
    private final MutableLiveData<String> exportStatus = new MutableLiveData<>();

    public enum SortOption {
        NEWEST, POPULARITY, PRICE_LOW_HIGH, PRICE_HIGH_LOW, NAME_A_Z, NAME_Z_A
    }

    public MenuManagementViewModel(@NonNull Application application) {
        super(application);
        this.menuRepository = new MenuRepository(application);
        
        // Initialize with empty sets
        selectedItems.setValue(new java.util.HashSet<>());
        
        // Setup filtered items transformation
        setupFilteredItemsTransformation();
    }

    private void setupFilteredItemsTransformation() {
        // Simplified approach - update filtered items when any filter changes
        // This will be called manually when filters are updated
    }
    
    private void updateFilteredItems() {
        List<MenuItem> items = allMenuItems.getValue();
        String query = searchQuery.getValue();
        String category = selectedCategory.getValue();
        String availability = availabilityFilter.getValue();
        Float min = minPrice.getValue();
        Float max = maxPrice.getValue();
        SortOption sort = sortOption.getValue();
        
        List<MenuItem> filtered = applyFiltersAndSort(items, query, category, availability, min, max, sort);
        filteredMenuItems.setValue(filtered);
    }

    private List<MenuItem> applyFiltersAndSort(List<MenuItem> items, String query, 
                                             String category, String availability, 
                                             Float min, Float max, SortOption sort) {
        if (items == null) return new ArrayList<>();
        
        List<MenuItem> filtered = items.stream()
            .filter(item -> matchesSearchQuery(item, query))
            .filter(item -> matchesCategory(item, category))
            .filter(item -> matchesAvailability(item, availability))
            .filter(item -> matchesPriceRange(item, min, max))
            .collect(Collectors.toList());
        
        // Apply sorting
        switch (sort) {
            case POPULARITY:
                filtered.sort((a, b) -> Integer.compare(b.getOrderCount(), a.getOrderCount()));
                break;
            case PRICE_LOW_HIGH:
                filtered.sort(Comparator.comparing(MenuItem::getPrice));
                break;
            case PRICE_HIGH_LOW:
                filtered.sort((a, b) -> Double.compare(b.getPrice(), a.getPrice()));
                break;
            case NAME_A_Z:
                filtered.sort(Comparator.comparing(MenuItem::getName));
                break;
            case NAME_Z_A:
                filtered.sort((a, b) -> b.getName().compareTo(a.getName()));
                break;
            case NEWEST:
            default:
                filtered.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                break;
        }
        
        return filtered;
    }

    private boolean matchesSearchQuery(MenuItem item, String query) {
        if (query == null || query.trim().isEmpty()) return true;
        String lowerQuery = query.toLowerCase();
        return item.getName().toLowerCase().contains(lowerQuery) ||
               (item.getDescription() != null && item.getDescription().toLowerCase().contains(lowerQuery));
    }

    private boolean matchesCategory(MenuItem item, String category) {
        if ("All".equals(category)) return true;
        return category.equals(item.getCategory());
    }

    private boolean matchesAvailability(MenuItem item, String availability) {
        if ("All".equals(availability)) return true;
        if ("Available".equals(availability)) return item.isAvailable();
        if ("Out of Stock".equals(availability)) return !item.isAvailable();
        return true;
    }

    private boolean matchesPriceRange(MenuItem item, Float min, Float max) {
        double price = item.getPrice();
        return price >= min && price <= max;
    }

    // Public methods for UI
    public void loadMenuItems(String vendorId) {
        isLoading.setValue(true);
        menuRepository.getMenuItems(vendorId, new MenuRepository.MenuCallback<List<MenuItem>>() {
            @Override
            public void onSuccess(List<MenuItem> result) {
                allMenuItems.setValue(result);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void updateSearchQuery(String query) {
        searchQuery.setValue(query);
        updateFilteredItems();
    }

    public void updateCategoryFilter(String category) {
        selectedCategory.setValue(category);
        updateFilteredItems();
    }

    public void updateAvailabilityFilter(String availability) {
        availabilityFilter.setValue(availability);
        updateFilteredItems();
    }

    public void updatePriceRange(float min, float max) {
        minPrice.setValue(min);
        maxPrice.setValue(max);
        updateFilteredItems();
    }

    public void updateSortOption(SortOption option) {
        sortOption.setValue(option);
        updateFilteredItems();
    }

    public void clearFilters() {
        searchQuery.setValue("");
        selectedCategory.setValue("All");
        availabilityFilter.setValue("All");
        minPrice.setValue(0f);
        maxPrice.setValue(1000f);
        sortOption.setValue(SortOption.NEWEST);
        updateFilteredItems();
    }

    // Bulk actions
    public void enterBulkMode() {
        isBulkMode.setValue(true);
        selectedItems.setValue(new java.util.HashSet<>());
    }

    public void exitBulkMode() {
        isBulkMode.setValue(false);
        selectedItems.setValue(new java.util.HashSet<>());
    }

    public void toggleItemSelection(String itemId) {
        Set<String> current = selectedItems.getValue();
        if (current != null) {
            Set<String> updated = new java.util.HashSet<>(current);
            if (updated.contains(itemId)) {
                updated.remove(itemId);
            } else {
                updated.add(itemId);
            }
            selectedItems.setValue(updated);
        }
    }

    public void bulkUpdateCategory(String vendorId, String newCategory) {
        Set<String> items = selectedItems.getValue();
        if (items != null && !items.isEmpty()) {
            isLoading.setValue(true);
            menuRepository.bulkUpdateCategory(vendorId, items, newCategory, new MenuRepository.MenuCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadMenuItems(vendorId); // Refresh data
                    exitBulkMode();
                }

                @Override
                public void onError(String error) {
                    errorMessage.setValue(error);
                    isLoading.setValue(false);
                }
            });
        }
    }

    public void bulkUpdatePrices(String vendorId, PriceUpdateType type, double value) {
        Set<String> items = selectedItems.getValue();
        if (items != null && !items.isEmpty()) {
            isLoading.setValue(true);
            menuRepository.bulkUpdatePrices(vendorId, items, type, value, new MenuRepository.MenuCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadMenuItems(vendorId); // Refresh data
                    exitBulkMode();
                }

                @Override
                public void onError(String error) {
                    errorMessage.setValue(error);
                    isLoading.setValue(false);
                }
            });
        }
    }

    public void bulkDelete(String vendorId) {
        Set<String> items = selectedItems.getValue();
        if (items != null && !items.isEmpty()) {
            isLoading.setValue(true);
            menuRepository.bulkDelete(vendorId, items, new MenuRepository.MenuCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadMenuItems(vendorId); // Refresh data
                    exitBulkMode();
                }

                @Override
                public void onError(String error) {
                    errorMessage.setValue(error);
                    isLoading.setValue(false);
                }
            });
        }
    }

    // Import/Export
    public void importMenuItems(String vendorId, String filePath, String format) {
        importProgress.setValue(0);
        importStatus.setValue("Starting import...");
        
        menuRepository.importMenuItems(vendorId, filePath, format, new MenuRepository.ImportCallback() {
            @Override
            public void onProgress(int progress, String status) {
                importProgress.setValue(progress);
                importStatus.setValue(status);
            }

            @Override
            public void onSuccess(int itemsImported) {
                importProgress.setValue(100);
                importStatus.setValue("Import completed: " + itemsImported + " items imported");
                loadMenuItems(vendorId); // Refresh data
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                importStatus.setValue("Import failed: " + error);
            }
        });
    }

    public void exportMenuItems(String vendorId, ExportType type, String format) {
        exportProgress.setValue(0);
        exportStatus.setValue("Starting export...");
        
        List<MenuItem> itemsToExport = getItemsForExport(type);
        
        menuRepository.exportMenuItems(vendorId, itemsToExport, format, new MenuRepository.ExportCallback() {
            @Override
            public void onProgress(int progress, String status) {
                exportProgress.setValue(progress);
                exportStatus.setValue(status);
            }

            @Override
            public void onSuccess(String filePath) {
                exportProgress.setValue(100);
                exportStatus.setValue("Export completed: " + filePath);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                exportStatus.setValue("Export failed: " + error);
            }
        });
    }

    private List<MenuItem> getItemsForExport(ExportType type) {
        switch (type) {
            case FILTERED:
                return filteredMenuItems.getValue() != null ? filteredMenuItems.getValue() : new ArrayList<>();
            case SELECTED:
                List<MenuItem> allItems = allMenuItems.getValue();
                Set<String> selected = selectedItems.getValue();
                if (allItems != null && selected != null) {
                    return allItems.stream()
                        .filter(item -> selected.contains(item.getId()))
                        .collect(Collectors.toList());
                }
                return new ArrayList<>();
            case ALL:
            default:
                return allMenuItems.getValue() != null ? allMenuItems.getValue() : new ArrayList<>();
        }
    }

    // Getters for LiveData
    public LiveData<List<MenuItem>> getFilteredMenuItems() { return filteredMenuItems; }
    public LiveData<Boolean> getIsBulkMode() { return isBulkMode; }
    public LiveData<Set<String>> getSelectedItems() { return selectedItems; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Integer> getImportProgress() { return importProgress; }
    public LiveData<Integer> getExportProgress() { return exportProgress; }
    public LiveData<String> getImportStatus() { return importStatus; }
    public LiveData<String> getExportStatus() { return exportStatus; }

    public enum PriceUpdateType {
        PERCENTAGE_INCREASE, PERCENTAGE_DECREASE, FIXED_INCREASE, FIXED_DECREASE, SET_PRICE
    }

    public enum ExportType {
        ALL, FILTERED, SELECTED
    }
}
