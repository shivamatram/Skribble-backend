# Menu Management System - Complete Implementation Guide

## 🎯 Overview

This document provides a comprehensive guide to the Menu Management system implemented for the Food Van Android application. The system includes advanced sorting, filtering, bulk operations, and import/export functionality with a professional Material 3 UI.

## 📋 Table of Contents

1. [Features Overview](#features-overview)
2. [Architecture](#architecture)
3. [Implementation Details](#implementation-details)
4. [Firestore Database Structure](#firestore-database-structure)
5. [API Reference](#api-reference)
6. [Testing Guide](#testing-guide)
7. [Deployment Notes](#deployment-notes)

## 🚀 Features Overview

### 1. Sort & Filter System
- **Advanced Sorting**: Newest, Popularity, Price (Low→High, High→Low), Name (A→Z, Z→A)
- **Category Filtering**: All, Snacks, Beverages, Main Course, Desserts
- **Availability Filtering**: All Items, Available, Out of Stock
- **Price Range Filtering**: Slider-based range selection (₹0-₹1000)
- **Search Functionality**: Debounced search with 300ms delay
- **Live Preview**: Real-time item count and preview thumbnails

### 2. Bulk Actions System
- **Multi-select Mode**: Toggle between normal and bulk selection modes
- **Bulk Operations**:
  - Toggle Availability (Available/Out of Stock)
  - Change Category (with preview dialog)
  - Price Updates (percentage/fixed/set price)
  - Soft Delete (with 30-day recovery window)
- **Professional Dialogs**: Category selection, price change, delete confirmation
- **Audit Logging**: Complete operation tracking and history

### 3. Import/Export System
- **Import Features**:
  - File picker with CSV/Excel support
  - Template download functionality
  - Column mapping and validation
  - Progress tracking with detailed status
  - Error reporting with line-by-line feedback
- **Export Features**:
  - Export All/Filtered/Selected items
  - Multiple formats (CSV, Excel, JSON)
  - Background processing with WorkManager
  - Progress tracking and completion notifications

## 🏗️ Architecture

### MVVM Pattern Implementation

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Activities    │───▶│   ViewModels     │───▶│  Repositories   │
│                 │    │                  │    │                 │
│ • Menu Mgmt     │    │ • MenuMgmtVM     │    │ • MenuRepo      │
│ • Import/Export │    │ • ImportExportVM │    │ • AuditRepo     │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Components │    │   LiveData       │    │   Data Sources  │
│                 │    │                  │    │                 │
│ • Bottom Sheets │    │ • Filtered Items │    │ • Firestore     │
│ • Dialogs       │    │ • Loading State  │    │ • Room Cache    │
│ • RecyclerViews │    │ • Error Messages │    │ • File System   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Key Components

#### Activities
- **VendorMenuManagementActivity**: Main menu management interface
- **ImportExportActivity**: Dedicated import/export functionality

#### ViewModels
- **MenuManagementViewModel**: Handles all menu operations, filtering, and bulk actions
- **ImportExportViewModel**: Manages import/export operations and progress tracking

#### Repositories
- **MenuRepository**: Firestore operations, caching, and data transformation
- **AuditRepository**: Audit logging and compliance tracking

## 🔧 Implementation Details

### 1. Sort & Filter Implementation

#### Bottom Sheet UI
```xml
<!-- Sort & Filter Bottom Sheet -->
<LinearLayout>
    <!-- Header with close button -->
    <!-- Sort options (Radio Group) -->
    <!-- Filter categories (Chip Group) -->
    <!-- Availability filters (Chip Group) -->
    <!-- Price range slider -->
    <!-- Search input with debouncing -->
    <!-- Live preview section -->
    <!-- Action buttons (Clear/Apply) -->
</LinearLayout>
```

#### ViewModel Integration
```java
// Filter state management
private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
private final MutableLiveData<String> selectedCategory = new MutableLiveData<>("All");
private final MutableLiveData<SortOption> sortOption = new MutableLiveData<>(SortOption.NEWEST);

// Live filtering with Transformations
LiveData<List<MenuItem>> filteredItems = Transformations.switchMap(
    allMenuItems, 
    items -> applyFiltersAndSort(items, query, category, availability, min, max, sort)
);
```

### 2. Bulk Actions Implementation

#### Multi-select State Management
```java
// Bulk action state
private final MutableLiveData<Boolean> isBulkMode = new MutableLiveData<>(false);
private final MutableLiveData<Set<String>> selectedItems = new MutableLiveData<>();

// Toggle selection
public void toggleItemSelection(String itemId) {
    Set<String> current = selectedItems.getValue();
    Set<String> updated = new HashSet<>(current);
    if (updated.contains(itemId)) {
        updated.remove(itemId);
    } else {
        updated.add(itemId);
    }
    selectedItems.setValue(updated);
}
```

#### Firestore Batch Operations
```java
// Bulk category update with audit logging
public void bulkUpdateCategory(String vendorId, Set<String> itemIds, String newCategory) {
    WriteBatch batch = firestore.batch();
    
    // Update all selected items
    for (String itemId : itemIds) {
        batch.update(menuRef.document(itemId), "category", newCategory);
    }
    
    // Add audit log entry
    Map<String, Object> auditEntry = createAuditEntry("BULK_CATEGORY_UPDATE", itemIds, 
            Map.of("newCategory", newCategory));
    batch.set(auditRef.document(), auditEntry);
    
    batch.commit();
}
```

### 3. Import/Export Implementation

#### File Processing
```java
// CSV Export with proper escaping
private void createCSVFile(List<MenuItem> items, File file) throws IOException {
    try (FileWriter writer = new FileWriter(file)) {
        writer.append("Name,Description,Price,Category,Available,ImageURL\n");
        
        for (MenuItem item : items) {
            writer.append(escapeCSV(item.getName())).append(",");
            writer.append(escapeCSV(item.getDescription())).append(",");
            writer.append(String.valueOf(item.getPrice())).append(",");
            // ... additional fields
        }
    }
}
```

#### Progress Tracking
```java
// Import with progress callbacks
public void importMenuItems(String vendorId, String filePath, ImportCallback callback) {
    executorService.execute(() -> {
        callback.onProgress(10, "Reading file...");
        // File parsing logic
        callback.onProgress(50, "Validating data...");
        // Validation logic
        callback.onProgress(80, "Uploading to database...");
        // Database operations
        callback.onSuccess(itemsImported);
    });
}
```

## 🗄️ Firestore Database Structure

### Menu Items Collection
```
/vendors/{vendorId}/menu/{itemId}
{
    "name": "Delicious Burger",
    "description": "Juicy beef burger with fresh vegetables",
    "price": 299.99,
    "category": "Main Course",
    "available": true,
    "visible": true,
    "imageUrl": "https://storage.googleapis.com/...",
    "createdAt": 1699123456789,
    "updatedAt": 1699123456789,
    "orderCount": 45,
    "rating": 4.5,
    "ingredients": ["beef", "lettuce", "tomato", "cheese"],
    "allergens": ["dairy", "gluten"],
    "nutritionInfo": {
        "calories": 650,
        "protein": 35,
        "carbs": 45,
        "fat": 28
    }
}
```

### Audit Log Collection
```
/vendors/{vendorId}/audit/{auditId}
{
    "action": "BULK_CATEGORY_UPDATE",
    "itemIds": ["item1", "item2", "item3"],
    "timestamp": 1699123456789,
    "userId": "vendor123",
    "metadata": {
        "oldCategory": "Snacks",
        "newCategory": "Main Course",
        "itemsAffected": 3
    },
    "ipAddress": "192.168.1.1",
    "userAgent": "FoodVan Android App v1.0"
}
```

### Trash Collection (Soft Delete)
```
/vendors/{vendorId}/trash/{itemId}
{
    "originalId": "menu_item_123",
    "deletedAt": 1699123456789,
    "restoreBy": 1701715456789,  // 30 days from deletion
    "deletedBy": "vendor123",
    "reason": "BULK_DELETE"
}
```

### Filter Presets Collection
```
/vendors/{vendorId}/filterPresets/{presetId}
{
    "name": "Popular Items",
    "filters": {
        "category": "All",
        "availability": "Available",
        "minPrice": 0,
        "maxPrice": 500,
        "sortBy": "POPULARITY"
    },
    "createdAt": 1699123456789,
    "isDefault": false
}
```

## 📚 API Reference

### MenuManagementViewModel Methods

#### Filtering and Sorting
```java
// Update search query with debouncing
void updateSearchQuery(String query)

// Update category filter
void updateCategoryFilter(String category)

// Update availability filter  
void updateAvailabilityFilter(String availability)

// Update price range
void updatePriceRange(float min, float max)

// Update sort option
void updateSortOption(SortOption option)

// Clear all filters
void clearFilters()
```

#### Bulk Operations
```java
// Enter/exit bulk selection mode
void enterBulkMode()
void exitBulkMode()

// Toggle item selection
void toggleItemSelection(String itemId)

// Bulk operations
void bulkUpdateCategory(String vendorId, String newCategory)
void bulkUpdatePrices(String vendorId, PriceUpdateType type, double value)
void bulkDelete(String vendorId)
```

#### Import/Export
```java
// Import menu items from file
void importMenuItems(String vendorId, String filePath, String format)

// Export menu items to file
void exportMenuItems(String vendorId, ExportType type, String format)
```

### LiveData Observables
```java
// Menu data
LiveData<List<MenuItem>> getFilteredMenuItems()
LiveData<Boolean> getIsLoading()
LiveData<String> getErrorMessage()

// Bulk actions
LiveData<Boolean> getIsBulkMode()
LiveData<Set<String>> getSelectedItems()

// Import/Export progress
LiveData<Integer> getImportProgress()
LiveData<Integer> getExportProgress()
LiveData<String> getImportStatus()
LiveData<String> getExportStatus()
```

## 🧪 Testing Guide

### Manual Testing Checklist

#### Sort & Filter Testing
- [ ] Test all sort options (Newest, Popularity, Price, Name)
- [ ] Test category filtering with all categories
- [ ] Test availability filtering (All, Available, Out of Stock)
- [ ] Test price range slider functionality
- [ ] Test search with various queries
- [ ] Test filter combinations
- [ ] Test clear filters functionality
- [ ] Verify live preview updates correctly

#### Bulk Actions Testing
- [ ] Test entering/exiting bulk mode
- [ ] Test item selection/deselection
- [ ] Test bulk category change with preview
- [ ] Test all price update types
- [ ] Test bulk delete with confirmation
- [ ] Verify audit logging for all operations
- [ ] Test undo functionality for destructive actions

#### Import/Export Testing
- [ ] Test file picker functionality
- [ ] Test template download
- [ ] Test CSV import with valid data
- [ ] Test CSV import with invalid data
- [ ] Test Excel import functionality
- [ ] Test export all items
- [ ] Test export filtered items
- [ ] Test export selected items
- [ ] Test all export formats (CSV, Excel, JSON)
- [ ] Verify progress tracking accuracy

### Unit Test Examples

```java
@Test
public void testFilterByCategory() {
    // Given
    List<MenuItem> items = createTestMenuItems();
    viewModel.getAllMenuItems().setValue(items);
    
    // When
    viewModel.updateCategoryFilter("Snacks");
    
    // Then
    List<MenuItem> filtered = viewModel.getFilteredMenuItems().getValue();
    assertThat(filtered).hasSize(2);
    assertThat(filtered).extracting(MenuItem::getCategory)
                        .containsOnly("Snacks");
}

@Test
public void testBulkPriceIncrease() {
    // Given
    Set<String> selectedItems = Set.of("item1", "item2");
    viewModel.getSelectedItems().setValue(selectedItems);
    
    // When
    viewModel.bulkUpdatePrices("vendor123", PERCENTAGE_INCREASE, 10.0);
    
    // Then
    verify(menuRepository).bulkUpdatePrices(eq("vendor123"), eq(selectedItems), 
                                          eq(PERCENTAGE_INCREASE), eq(10.0), any());
}
```

## 🚀 Deployment Notes

### Prerequisites
- Android API Level 26+ (Android 8.0)
- Firebase project with Firestore enabled
- Storage permissions for import/export functionality
- Network permissions for Firestore operations

### Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Vendors can only access their own data
    match /vendors/{vendorId} {
      allow read, write: if request.auth != null && 
                           request.auth.uid == vendorId;
      
      // Menu items
      match /menu/{itemId} {
        allow read, write: if request.auth != null && 
                             request.auth.uid == vendorId;
      }
      
      // Audit logs (write-only for security)
      match /audit/{auditId} {
        allow write: if request.auth != null && 
                       request.auth.uid == vendorId;
        allow read: if request.auth != null && 
                      request.auth.uid == vendorId &&
                      request.time < resource.data.timestamp + duration.P30D;
      }
      
      // Trash (soft delete recovery)
      match /trash/{itemId} {
        allow read, write: if request.auth != null && 
                             request.auth.uid == vendorId;
      }
    }
  }
}
```

### Performance Optimization
- **Firestore Indexes**: Create composite indexes for complex queries
- **Pagination**: Implement pagination for large menu datasets
- **Caching**: Use Room database for offline functionality
- **Image Optimization**: Compress images before upload
- **Background Processing**: Use WorkManager for heavy operations

### Monitoring and Analytics
- **Crashlytics**: Monitor app crashes and errors
- **Analytics**: Track feature usage and performance
- **Performance Monitoring**: Monitor Firestore query performance
- **User Feedback**: Implement feedback collection for improvements

## 📞 Support and Maintenance

### Common Issues and Solutions

#### Import/Export Issues
- **File Format Errors**: Validate file format before processing
- **Permission Denied**: Ensure storage permissions are granted
- **Large File Handling**: Implement chunked processing for large files

#### Performance Issues
- **Slow Filtering**: Implement debouncing and optimize queries
- **Memory Issues**: Use pagination and proper lifecycle management
- **Network Issues**: Implement offline functionality with Room

#### UI/UX Issues
- **Responsive Design**: Test on various screen sizes
- **Accessibility**: Ensure proper content descriptions and touch targets
- **Theme Consistency**: Maintain Material 3 design guidelines

### Future Enhancements
- **Advanced Analytics**: Detailed menu performance insights
- **AI-Powered Recommendations**: Suggest optimal pricing and categories
- **Multi-language Support**: Internationalization for global markets
- **Voice Commands**: Voice-activated menu management
- **Barcode Scanning**: Quick item addition via barcode scanning

---

## 📄 License and Credits

This Menu Management system is part of the Food Van Android application. All rights reserved.

**Development Team:**
- Architecture Design: MVVM with Repository Pattern
- UI/UX Design: Material 3 Design System
- Backend Integration: Firebase Firestore
- Testing: JUnit and Espresso

**Last Updated:** November 2024
**Version:** 1.0.0
**Compatibility:** Android 8.0+ (API 26+)
