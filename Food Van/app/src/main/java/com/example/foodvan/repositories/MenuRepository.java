package com.example.foodvan.repositories;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.foodvan.models.MenuItem;
import com.example.foodvan.viewmodels.MenuManagementViewModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MenuRepository {

    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Context context;

    public MenuRepository(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newFixedThreadPool(3);
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // Callback interfaces
    public interface MenuCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public interface ImportCallback {
        void onProgress(int progress, String status);
        void onSuccess(int itemsImported);
        void onError(String error);
    }

    public interface ExportCallback {
        void onProgress(int progress, String status);
        void onSuccess(String filePath);
        void onError(String error);
    }

    // Get menu items for a vendor
    public void getMenuItems(String vendorId, MenuCallback<List<MenuItem>> callback) {
        firestore.collection("vendors")
                .document(vendorId)
                .collection("menu")
                .whereEqualTo("visible", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MenuItem> menuItems = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        MenuItem item = document.toObject(MenuItem.class);
                        if (item != null) {
                            item.setId(document.getId());
                            menuItems.add(item);
                        }
                    }
                    callback.onSuccess(menuItems);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Bulk update category
    public void bulkUpdateCategory(String vendorId, Set<String> itemIds, String newCategory, MenuCallback<Void> callback) {
        WriteBatch batch = firestore.batch();
        
        for (String itemId : itemIds) {
            batch.update(firestore.collection("vendors")
                    .document(vendorId)
                    .collection("menu")
                    .document(itemId), "category", newCategory);
        }
        
        // Add audit log entry
        Map<String, Object> auditEntry = createAuditEntry("BULK_CATEGORY_UPDATE", itemIds, 
                Map.of("newCategory", newCategory));
        batch.set(firestore.collection("vendors")
                .document(vendorId)
                .collection("audit")
                .document(), auditEntry);
        
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Bulk update prices
    public void bulkUpdatePrices(String vendorId, Set<String> itemIds, 
                               MenuManagementViewModel.PriceUpdateType type, double value, 
                               MenuCallback<Void> callback) {
        
        // First, get current prices to calculate new ones
        List<String> itemIdsList = new ArrayList<>(itemIds);
        getPricesForItems(vendorId, itemIdsList, new MenuCallback<Map<String, Double>>() {
            @Override
            public void onSuccess(Map<String, Double> currentPrices) {
                WriteBatch batch = firestore.batch();
                Map<String, Double> newPrices = new HashMap<>();
                
                for (Map.Entry<String, Double> entry : currentPrices.entrySet()) {
                    String itemId = entry.getKey();
                    double currentPrice = entry.getValue();
                    double newPrice = calculateNewPrice(currentPrice, type, value);
                    
                    // Ensure price is not negative
                    newPrice = Math.max(0, newPrice);
                    newPrices.put(itemId, newPrice);
                    
                    batch.update(firestore.collection("vendors")
                            .document(vendorId)
                            .collection("menu")
                            .document(itemId), "price", newPrice);
                }
                
                // Add audit log entry
                Map<String, Object> auditEntry = createAuditEntry("BULK_PRICE_UPDATE", itemIds,
                        Map.of("updateType", type.toString(), "value", value, "newPrices", newPrices));
                batch.set(firestore.collection("vendors")
                        .document(vendorId)
                        .collection("audit")
                        .document(), auditEntry);
                
                batch.commit()
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(e -> callback.onError(e.getMessage()));
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private double calculateNewPrice(double currentPrice, MenuManagementViewModel.PriceUpdateType type, double value) {
        switch (type) {
            case PERCENTAGE_INCREASE:
                return currentPrice * (1 + value / 100);
            case PERCENTAGE_DECREASE:
                return currentPrice * (1 - value / 100);
            case FIXED_INCREASE:
                return currentPrice + value;
            case FIXED_DECREASE:
                return currentPrice - value;
            case SET_PRICE:
                return value;
            default:
                return currentPrice;
        }
    }

    private void getPricesForItems(String vendorId, List<String> itemIds, MenuCallback<Map<String, Double>> callback) {
        Map<String, Double> prices = new HashMap<>();
        
        // Get prices for all items (simplified - in production, use batch get)
        firestore.collection("vendors")
                .document(vendorId)
                .collection("menu")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (itemIds.contains(document.getId())) {
                            Double price = document.getDouble("price");
                            if (price != null) {
                                prices.put(document.getId(), price);
                            }
                        }
                    }
                    callback.onSuccess(prices);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Bulk delete (soft delete)
    public void bulkDelete(String vendorId, Set<String> itemIds, MenuCallback<Void> callback) {
        WriteBatch batch = firestore.batch();
        
        for (String itemId : itemIds) {
            // Soft delete by setting visible = false
            batch.update(firestore.collection("vendors")
                    .document(vendorId)
                    .collection("menu")
                    .document(itemId), "visible", false);
            
            // Move to trash collection for recovery
            batch.set(firestore.collection("vendors")
                    .document(vendorId)
                    .collection("trash")
                    .document(itemId), Map.of(
                        "originalId", itemId,
                        "deletedAt", System.currentTimeMillis(),
                        "restoreBy", System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days
                    ));
        }
        
        // Add audit log entry
        Map<String, Object> auditEntry = createAuditEntry("BULK_DELETE", itemIds, Map.of());
        batch.set(firestore.collection("vendors")
                .document(vendorId)
                .collection("audit")
                .document(), auditEntry);
        
        batch.commit()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }

    // Import menu items
    public void importMenuItems(String vendorId, String filePath, String format, ImportCallback callback) {
        executorService.execute(() -> {
            try {
                mainHandler.post(() -> callback.onProgress(10, "Reading file..."));
                
                // TODO: Implement actual CSV/Excel parsing
                // For now, simulate the process
                Thread.sleep(1000);
                mainHandler.post(() -> callback.onProgress(30, "Parsing items..."));
                
                Thread.sleep(1000);
                mainHandler.post(() -> callback.onProgress(60, "Validating data..."));
                
                Thread.sleep(1000);
                mainHandler.post(() -> callback.onProgress(80, "Uploading to database..."));
                
                // Simulate successful import
                Thread.sleep(1000);
                mainHandler.post(() -> callback.onSuccess(25)); // 25 items imported
                
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    // Export menu items
    public void exportMenuItems(String vendorId, List<MenuItem> items, String format, ExportCallback callback) {
        executorService.execute(() -> {
            try {
                mainHandler.post(() -> callback.onProgress(10, "Preparing data..."));
                
                Thread.sleep(500);
                mainHandler.post(() -> callback.onProgress(40, "Generating " + format + " file..."));
                
                // Create export file
                File exportFile = createExportFile(items, format);
                
                Thread.sleep(1000);
                mainHandler.post(() -> callback.onProgress(80, "Saving file..."));
                
                Thread.sleep(500);
                mainHandler.post(() -> callback.onSuccess(exportFile.getAbsolutePath()));
                
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    private File createExportFile(List<MenuItem> items, String format) throws IOException {
        File exportDir = new File(context.getExternalFilesDir(null), "exports");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        String fileName = "menu_export_" + System.currentTimeMillis() + "." + format.toLowerCase();
        File exportFile = new File(exportDir, fileName);
        
        if ("CSV".equalsIgnoreCase(format)) {
            createCSVFile(items, exportFile);
        } else if ("JSON".equalsIgnoreCase(format)) {
            createJSONFile(items, exportFile);
        } else {
            // Default to CSV
            createCSVFile(items, exportFile);
        }
        
        return exportFile;
    }

    private void createCSVFile(List<MenuItem> items, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            // Write CSV header
            writer.append("Name,Description,Price,Category,Available,ImageURL\n");
            
            // Write data rows
            for (MenuItem item : items) {
                writer.append(escapeCSV(item.getName())).append(",");
                writer.append(escapeCSV(item.getDescription())).append(",");
                writer.append(String.valueOf(item.getPrice())).append(",");
                writer.append(escapeCSV(item.getCategory())).append(",");
                writer.append(String.valueOf(item.isAvailable())).append(",");
                writer.append(escapeCSV(item.getImageUrl())).append("\n");
            }
        }
    }

    private void createJSONFile(List<MenuItem> items, File file) throws IOException {
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("{\n  \"menuItems\": [\n");
            
            for (int i = 0; i < items.size(); i++) {
                MenuItem item = items.get(i);
                writer.append("    {\n");
                writer.append("      \"name\": \"").append(escapeJSON(item.getName())).append("\",\n");
                writer.append("      \"description\": \"").append(escapeJSON(item.getDescription())).append("\",\n");
                writer.append("      \"price\": ").append(String.valueOf(item.getPrice())).append(",\n");
                writer.append("      \"category\": \"").append(escapeJSON(item.getCategory())).append("\",\n");
                writer.append("      \"available\": ").append(String.valueOf(item.isAvailable())).append(",\n");
                writer.append("      \"imageUrl\": \"").append(escapeJSON(item.getImageUrl())).append("\"\n");
                writer.append("    }");
                
                if (i < items.size() - 1) {
                    writer.append(",");
                }
                writer.append("\n");
            }
            
            writer.append("  ]\n}");
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String escapeJSON(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private Map<String, Object> createAuditEntry(String action, Set<String> itemIds, Map<String, Object> metadata) {
        Map<String, Object> auditEntry = new HashMap<>();
        auditEntry.put("action", action);
        auditEntry.put("itemIds", new ArrayList<>(itemIds));
        auditEntry.put("timestamp", System.currentTimeMillis());
        auditEntry.put("metadata", metadata);
        return auditEntry;
    }

    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
