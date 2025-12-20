package com.example.foodvan.repositories;

import android.content.Context;

import com.example.foodvan.models.Order;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    private final Context context;
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public interface OnOrdersLoadListener {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }

    public interface OnOrderOperationListener {
        void onSuccess(String message);
        void onError(String error);
    }

    public OrderRepository(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.auth = FirebaseAuth.getInstance();
    }

    public void loadVendorOrders(OnOrdersLoadListener listener) {
        String vendorId = getCurrentVendorId();
        if (vendorId == null) {
            listener.onError("User not authenticated");
            return;
        }

        db.collection("orders")
                .whereEqualTo("vendorId", vendorId)
                .orderBy("orderTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orders.add(order);
                    }
                    listener.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load orders: " + e.getMessage());
                });
    }

    public void loadCustomerOrders(OnOrdersLoadListener listener) {
        String customerId = getCurrentCustomerId();
        if (customerId == null) {
            listener.onError("User not authenticated");
            return;
        }

        db.collection("orders")
                .whereEqualTo("customerId", customerId)
                .orderBy("orderTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Order> orders = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        order.setOrderId(document.getId());
                        orders.add(order);
                    }
                    listener.onSuccess(orders);
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load orders: " + e.getMessage());
                });
    }

    public void updateOrderStatus(String orderId, String newStatus, OnOrderOperationListener listener) {
        if (orderId == null || orderId.isEmpty()) {
            listener.onError("Order ID is required");
            return;
        }

        db.collection("orders")
                .document(orderId)
                .update("status", newStatus, "lastUpdated", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Order status updated successfully");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to update order status: " + e.getMessage());
                });
    }

    public void cancelOrder(String orderId, String reason, OnOrderOperationListener listener) {
        if (orderId == null || orderId.isEmpty()) {
            listener.onError("Order ID is required");
            return;
        }

        db.collection("orders")
                .document(orderId)
                .update(
                        "status", "cancelled",
                        "cancellationReason", reason,
                        "cancelledAt", System.currentTimeMillis(),
                        "lastUpdated", System.currentTimeMillis()
                )
                .addOnSuccessListener(aVoid -> {
                    listener.onSuccess("Order cancelled successfully");
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to cancel order: " + e.getMessage());
                });
    }

    public void getOrderById(String orderId, OnOrderLoadListener listener) {
        if (orderId == null || orderId.isEmpty()) {
            listener.onError("Order ID is required");
            return;
        }

        db.collection("orders")
                .document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Order order = documentSnapshot.toObject(Order.class);
                        if (order != null) {
                            order.setOrderId(documentSnapshot.getId());
                            listener.onSuccess(order);
                        } else {
                            listener.onError("Failed to parse order data");
                        }
                    } else {
                        listener.onError("Order not found");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Failed to load order: " + e.getMessage());
                });
    }

    private String getCurrentVendorId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    private String getCurrentCustomerId() {
        return auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
    }

    public interface OnOrderLoadListener {
        void onSuccess(Order order);
        void onError(String error);
    }
}
