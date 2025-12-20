package com.example.foodvan.repositories;

import android.content.Context;
import android.net.Uri;

import com.example.foodvan.models.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MenuItemRepository {

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;

    public interface OnMenuItemOperationListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnMenuItemsLoadListener {
        void onSuccess(java.util.List<MenuItem> menuItems);
        void onError(String error);
    }

    public MenuItemRepository(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void addMenuItem(MenuItem menuItem, OnMenuItemOperationListener listener) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            listener.onError("User not authenticated");
            return;
        }

        // First upload image, then save menu item data
        uploadMenuItemImage(menuItem.getImageUri(), new OnImageUploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                // Update menu item with uploaded image URL
                menuItem.setImageUrl(imageUrl);
                menuItem.setVendorId(vendorId);
                menuItem.setVanId(vendorId); // Using vendorId as vanId for simplicity
                menuItem.setCreatedAt(System.currentTimeMillis());
                menuItem.setLastUpdated(System.currentTimeMillis());
                
                // Save to Firestore
                saveMenuItemToFirestore(menuItem, listener);
            }

            @Override
            public void onError(String error) {
                listener.onError("Failed to upload image: " + error);
            }
        });
    }

    private void uploadMenuItemImage(String imageUri, OnImageUploadListener listener) {
        if (imageUri == null || imageUri.isEmpty()) {
            listener.onError("No image selected");
            return;
        }

        try {
            Uri uri = Uri.parse(imageUri);
            String fileName = "menu_images/" + UUID.randomUUID().toString() + ".jpg";
            StorageReference imageRef = storage.getReference().child(fileName);

            imageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(downloadUri -> {
                                    listener.onSuccess(downloadUri.toString());
                                })
                                .addOnFailureListener(e -> {
                                    listener.onError("Failed to get download URL: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        listener.onError("Failed to upload image: " + e.getMessage());
                    });
        } catch (Exception e) {
            listener.onError("Invalid image URI: " + e.getMessage());
        }
    }

    private void saveMenuItemToFirestore(MenuItem menuItem, OnMenuItemOperationListener listener) {
        Map<String, Object> menuItemData = new HashMap<>();
        menuItemData.put("name", menuItem.getName());
        menuItemData.put("price", menuItem.getPrice());
        menuItemData.put("category", menuItem.getCategory());
        menuItemData.put("description", menuItem.getDescription());
        menuItemData.put("imageUrl", menuItem.getImageUrl());
        menuItemData.put("isAvailable", menuItem.isAvailable());
        menuItemData.put("vendorId", menuItem.getVendorId());
        menuItemData.put("vanId", menuItem.getVanId());
        menuItemData.put("createdAt", menuItem.getCreatedAt());
        menuItemData.put("lastUpdated", menuItem.getLastUpdated());
        menuItemData.put("isVegetarian", menuItem.isVegetarian());
        menuItemData.put("preparationTime", menuItem.getPreparationTime());
        menuItemData.put("rating", menuItem.getRating());
        menuItemData.put("totalRatings", menuItem.getTotalRatings());

        db.collection("menuItems")
                .add(menuItemData)
                .addOnSuccessListener(documentReference -> {
                    menuItem.setItemId(documentReference.getId());
                    listener.onSuccess("Food item added successfully!");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to save food item: " + e.getMessage());
                });
    }

    public void loadVendorMenuItems(OnMenuItemsLoadListener listener) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            listener.onError("User not authenticated");
            return;
        }

        db.collection("menuItems")
                .whereEqualTo("vendorId", vendorId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<MenuItem> menuItems = new java.util.ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        MenuItem menuItem = document.toObject(MenuItem.class);
                        menuItem.setItemId(document.getId());
                        menuItems.add(menuItem);
                    });
                    listener.onSuccess(menuItems);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load menu items: " + e.getMessage());
                });
    }

    public void updateMenuItem(MenuItem menuItem, OnMenuItemOperationListener listener) {
        if (menuItem.getItemId() == null || menuItem.getItemId().isEmpty()) {
            listener.onError("Menu item ID is required for update");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", menuItem.getName());
        updates.put("price", menuItem.getPrice());
        updates.put("category", menuItem.getCategory());
        updates.put("description", menuItem.getDescription());
        updates.put("isAvailable", menuItem.isAvailable());
        updates.put("lastUpdated", System.currentTimeMillis());

        db.collection("menuItems")
                .document(menuItem.getItemId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Menu item updated successfully!");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to update menu item: " + e.getMessage());
                });
    }

    public void deleteMenuItem(String menuItemId, OnMenuItemOperationListener listener) {
        if (menuItemId == null || menuItemId.isEmpty()) {
            listener.onError("Menu item ID is required for deletion");
            return;
        }

        db.collection("menuItems")
                .document(menuItemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Menu item deleted successfully!");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to delete menu item: " + e.getMessage());
                });
    }

    private String getCurrentVendorId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private interface OnImageUploadListener {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}
