package com.example.foodvan.repositories;

import android.content.Context;
import android.net.Uri;

import com.example.foodvan.models.FoodItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FoodItemRepository {

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final FirebaseAuth auth;

    public interface OnFoodItemOperationListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface OnFoodItemsLoadListener {
        void onSuccess(java.util.List<FoodItem> foodItems);
        void onError(String error);
    }

    public FoodItemRepository(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.storage = FirebaseStorage.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void addFoodItem(FoodItem foodItem, OnFoodItemOperationListener listener) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            listener.onError("User not authenticated");
            return;
        }

        // First upload image, then save food item data
        uploadFoodImage(foodItem.getImageUri(), new OnImageUploadListener() {
            @Override
            public void onSuccess(String imageUrl) {
                // Update food item with uploaded image URL
                foodItem.setImageUrl(imageUrl);
                foodItem.setVendorId(vendorId);
                foodItem.setCreatedAt(new java.util.Date(System.currentTimeMillis()));
                
                // Save to Firestore
                saveFoodItemToFirestore(foodItem, listener);
            }

            @Override
            public void onError(String error) {
                listener.onError("Failed to upload image: " + error);
            }
        });
    }

    private void uploadFoodImage(String imageUri, OnImageUploadListener listener) {
        if (imageUri == null || imageUri.isEmpty()) {
            listener.onError("No image selected");
            return;
        }

        try {
            Uri uri = Uri.parse(imageUri);
            String fileName = "food_images/" + UUID.randomUUID().toString() + ".jpg";
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

    private void saveFoodItemToFirestore(FoodItem foodItem, OnFoodItemOperationListener listener) {
        Map<String, Object> foodItemData = new HashMap<>();
        foodItemData.put("name", foodItem.getName());
        foodItemData.put("price", foodItem.getPrice());
        foodItemData.put("category", foodItem.getCategory());
        foodItemData.put("description", foodItem.getDescription());
        foodItemData.put("imageUrl", foodItem.getImageUrl());
        foodItemData.put("isAvailable", foodItem.isAvailable());
        foodItemData.put("vendorId", foodItem.getVendorId());
        foodItemData.put("createdAt", foodItem.getCreatedAt());

        db.collection("foodItems")
                .add(foodItemData)
                .addOnSuccessListener(documentReference -> {
                    foodItem.setId(documentReference.getId());
                    listener.onSuccess("Food item added successfully!");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to save food item: " + e.getMessage());
                });
    }

    public void loadVendorFoodItems(OnFoodItemsLoadListener listener) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            listener.onError("User not authenticated");
            return;
        }

        db.collection("foodItems")
                .whereEqualTo("vendorId", vendorId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<FoodItem> foodItems = new java.util.ArrayList<>();
                    queryDocumentSnapshots.forEach(document -> {
                        FoodItem foodItem = document.toObject(FoodItem.class);
                        foodItem.setId(document.getId());
                        foodItems.add(foodItem);
                    });
                    listener.onSuccess(foodItems);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load food items: " + e.getMessage());
                });
    }

    public void updateFoodItem(FoodItem foodItem, OnFoodItemOperationListener listener) {
        if (foodItem.getId() == null || foodItem.getId().isEmpty()) {
            listener.onError("Food item ID is required for update");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", foodItem.getName());
        updates.put("price", foodItem.getPrice());
        updates.put("category", foodItem.getCategory());
        updates.put("description", foodItem.getDescription());
        updates.put("isAvailable", foodItem.isAvailable());
        updates.put("updatedAt", System.currentTimeMillis());

        db.collection("foodItems")
                .document(foodItem.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Food item updated successfully!");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to update food item: " + e.getMessage());
                });
    }

    public void deleteFoodItem(String foodItemId, OnFoodItemOperationListener listener) {
        if (foodItemId == null || foodItemId.isEmpty()) {
            listener.onError("Food item ID is required for deletion");
            return;
        }

        db.collection("foodItems")
                .document(foodItemId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Food item deleted successfully!");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to delete food item: " + e.getMessage());
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
