package com.example.foodvan.utils;

import android.location.Location;

import androidx.annotation.NonNull;

import com.example.foodvan.models.FoodVan;
import com.example.foodvan.models.MenuItem;
import com.example.foodvan.models.Order;
import com.example.foodvan.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * FirebaseManager - Handles all Firebase Realtime Database operations
 */
public class FirebaseManager {
    
    private DatabaseReference databaseReference;
    
    // Database paths
    private static final String USERS_PATH = "users";
    private static final String FOOD_VANS_PATH = "food_vans";
    private static final String MENU_ITEMS_PATH = "menu_items";
    private static final String ORDERS_PATH = "orders";
    
    public FirebaseManager() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    // User Management
    public interface OnUserSaveListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserFetchListener {
        void onSuccess(User user);
        void onFailure(String error);
    }

    public void saveUser(User user, OnUserSaveListener listener) {
        databaseReference.child(USERS_PATH)
                .child(user.getUserId())
                .setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public void getUserById(String userId, OnUserFetchListener listener) {
        databaseReference.child(USERS_PATH)
                .child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                listener.onSuccess(user);
                            } else {
                                listener.onFailure("User data is null");
                            }
                        } else {
                            listener.onFailure("User not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Food Van Management
    public interface OnFoodVanSaveListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnFoodVanLoadListener {
        void onSuccess(FoodVan foodVan);
        void onFailure(String error);
    }

    public interface OnFoodVansLoadListener {
        void onSuccess(List<FoodVan> foodVans);
        void onFailure(String error);
    }

    public void saveFoodVan(FoodVan foodVan, OnFoodVanSaveListener listener) {
        databaseReference.child(FOOD_VANS_PATH)
                .child(foodVan.getVanId())
                .setValue(foodVan)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public void getFoodVanById(String vanId, OnFoodVanLoadListener listener) {
        databaseReference.child(FOOD_VANS_PATH)
                .child(vanId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            FoodVan foodVan = snapshot.getValue(FoodVan.class);
                            if (foodVan != null) {
                                listener.onSuccess(foodVan);
                            } else {
                                listener.onFailure("Food van data is null");
                            }
                        } else {
                            listener.onFailure("Food van not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    public void getNearbyFoodVans(double latitude, double longitude, double radiusKm, 
                                 OnFoodVansLoadListener listener) {
        databaseReference.child(FOOD_VANS_PATH)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<FoodVan> nearbyVans = new ArrayList<>();
                        
                        for (DataSnapshot vanSnapshot : snapshot.getChildren()) {
                            FoodVan foodVan = vanSnapshot.getValue(FoodVan.class);
                            if (foodVan != null && foodVan.isOnline()) {
                                double distance = calculateDistance(
                                    latitude, longitude,
                                    foodVan.getLatitude(), foodVan.getLongitude()
                                );
                                
                                if (distance <= radiusKm) {
                                    foodVan.setDistance(distance);
                                    nearbyVans.add(foodVan);
                                }
                            }
                        }
                        
                        listener.onSuccess(nearbyVans);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Menu Item Management
    public interface OnMenuItemSaveListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnMenuItemsLoadListener {
        void onSuccess(List<MenuItem> menuItems);
        void onFailure(String error);
    }

    public void saveMenuItem(MenuItem menuItem, OnMenuItemSaveListener listener) {
        databaseReference.child(MENU_ITEMS_PATH)
                .child(menuItem.getVanId())
                .child(menuItem.getItemId())
                .setValue(menuItem)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public void getMenuItems(String vanId, OnMenuItemsLoadListener listener) {
        databaseReference.child(MENU_ITEMS_PATH)
                .child(vanId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<MenuItem> menuItems = new ArrayList<>();
                        
                        for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                            MenuItem menuItem = itemSnapshot.getValue(MenuItem.class);
                            if (menuItem != null) {
                                menuItems.add(menuItem);
                            }
                        }
                        
                        listener.onSuccess(menuItems);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Order Management
    public interface OnOrderSaveListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnOrdersLoadListener {
        void onSuccess(List<Order> orders);
        void onFailure(String error);
    }

    public void saveOrder(Order order, OnOrderSaveListener listener) {
        databaseReference.child(ORDERS_PATH)
                .child(order.getOrderId())
                .setValue(order)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onSuccess();
                    } else {
                        listener.onFailure(task.getException() != null ? 
                            task.getException().getMessage() : "Unknown error");
                    }
                });
    }

    public void getUserOrders(String userId, OnOrdersLoadListener listener) {
        databaseReference.child(ORDERS_PATH)
                .orderByChild("customerId")
                .equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();
                        
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        
                        listener.onSuccess(orders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    public void getVendorOrders(String vendorId, OnOrdersLoadListener listener) {
        databaseReference.child(ORDERS_PATH)
                .orderByChild("vendorId")
                .equalTo(vendorId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Order> orders = new ArrayList<>();
                        
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            if (order != null) {
                                orders.add(order);
                            }
                        }
                        
                        listener.onSuccess(orders);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Utility Methods
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] / 1000.0; // Convert to kilometers
    }

    public void updateVendorLocation(String vendorId, double latitude, double longitude) {
        databaseReference.child(FOOD_VANS_PATH)
                .child(vendorId)
                .child("latitude")
                .setValue(latitude);
        
        databaseReference.child(FOOD_VANS_PATH)
                .child(vendorId)
                .child("longitude")
                .setValue(longitude);
        
        databaseReference.child(FOOD_VANS_PATH)
                .child(vendorId)
                .child("lastUpdated")
                .setValue(System.currentTimeMillis());
    }

    public void updateVendorOnlineStatus(String vendorId, boolean isOnline) {
        databaseReference.child(FOOD_VANS_PATH)
                .child(vendorId)
                .child("isOnline")
                .setValue(isOnline);
    }
}
