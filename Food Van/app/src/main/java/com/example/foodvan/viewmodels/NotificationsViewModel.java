package com.example.foodvan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodvan.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for managing notifications data
 * Handles Firebase integration and provides LiveData for UI updates
 */
public class NotificationsViewModel extends ViewModel {

    // Firebase components
    private FirebaseAuth firebaseAuth;
    private DatabaseReference notificationsRef;
    private String vendorId;
    private ValueEventListener notificationsListener;

    // LiveData for UI
    private MutableLiveData<List<Notification>> notifications = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<Integer> unreadCount = new MutableLiveData<>();

    public NotificationsViewModel() {
        initializeFirebase();
        loadInitialData();
    }

    /**
     * Initialize Firebase components
     */
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            vendorId = firebaseAuth.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            notificationsRef = database.getReference("notifications").child(vendorId);
        }
    }

    /**
     * Load initial sample data and start real-time updates
     */
    private void loadInitialData() {
        // Set initial sample data
        List<Notification> sampleNotifications = generateSampleNotifications();
        notifications.setValue(sampleNotifications);
        updateUnreadCount(sampleNotifications);
        isLoading.setValue(false);

        // Start real-time updates if Firebase is available
        if (vendorId != null) {
            loadRealTimeNotifications();
        }
    }

    /**
     * Load real-time notifications from Firebase
     */
    public void loadRealTimeNotifications() {
        if (vendorId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);

        // Query notifications ordered by timestamp (most recent first)
        Query query = notificationsRef.orderByChild("timestamp").limitToLast(50);
        
        notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Notification> notificationList = new ArrayList<>();
                
                for (DataSnapshot notificationSnapshot : snapshot.getChildren()) {
                    try {
                        Notification notification = notificationSnapshot.getValue(Notification.class);
                        if (notification != null) {
                            notification.setId(notificationSnapshot.getKey());
                            notificationList.add(notification);
                        }
                    } catch (Exception e) {
                        // Handle parsing errors gracefully
                        e.printStackTrace();
                    }
                }
                
                // Sort by timestamp (most recent first)
                Collections.sort(notificationList, (n1, n2) -> {
                    if (n1.getTimestamp() == null || n2.getTimestamp() == null) return 0;
                    return n2.getTimestamp().compareTo(n1.getTimestamp());
                });
                
                notifications.setValue(notificationList);
                updateUnreadCount(notificationList);
                isLoading.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorMessage.setValue("Failed to load notifications: " + error.getMessage());
                isLoading.setValue(false);
            }
        };

        query.addValueEventListener(notificationsListener);
    }

    /**
     * Refresh notifications data
     */
    public void refreshNotifications() {
        if (vendorId != null) {
            loadRealTimeNotifications();
        } else {
            loadInitialData();
        }
    }

    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId) {
        if (vendorId == null || notificationId == null) return;

        notificationsRef.child(notificationId).child("read").setValue(true)
                .addOnSuccessListener(aVoid -> {
                    // Update local data
                    List<Notification> currentNotifications = notifications.getValue();
                    if (currentNotifications != null) {
                        for (Notification notification : currentNotifications) {
                            if (notificationId.equals(notification.getId())) {
                                notification.setRead(true);
                                break;
                            }
                        }
                        notifications.setValue(currentNotifications);
                        updateUnreadCount(currentNotifications);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to mark as read: " + e.getMessage());
                });
    }

    /**
     * Mark all notifications as read
     */
    public void markAllAsRead() {
        List<Notification> currentNotifications = notifications.getValue();
        if (currentNotifications == null || vendorId == null) return;

        for (Notification notification : currentNotifications) {
            if (!notification.isRead()) {
                notificationsRef.child(notification.getId()).child("read").setValue(true);
                notification.setRead(true);
            }
        }

        notifications.setValue(currentNotifications);
        updateUnreadCount(currentNotifications);
    }

    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId) {
        if (vendorId == null || notificationId == null) return;

        notificationsRef.child(notificationId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Update local data
                    List<Notification> currentNotifications = notifications.getValue();
                    if (currentNotifications != null) {
                        currentNotifications.removeIf(n -> notificationId.equals(n.getId()));
                        notifications.setValue(currentNotifications);
                        updateUnreadCount(currentNotifications);
                    }
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to delete notification: " + e.getMessage());
                });
    }

    /**
     * Clear all notifications
     */
    public void clearAllNotifications() {
        if (vendorId == null) return;

        notificationsRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    notifications.setValue(new ArrayList<>());
                    unreadCount.setValue(0);
                })
                .addOnFailureListener(e -> {
                    errorMessage.setValue("Failed to clear notifications: " + e.getMessage());
                });
    }

    /**
     * Update unread count
     */
    private void updateUnreadCount(List<Notification> notificationList) {
        int count = 0;
        if (notificationList != null) {
            for (Notification notification : notificationList) {
                if (!notification.isRead()) {
                    count++;
                }
            }
        }
        unreadCount.setValue(count);
    }

    /**
     * Generate sample notifications for demonstration
     */
    private List<Notification> generateSampleNotifications() {
        List<Notification> sampleNotifications = new ArrayList<>();
        
        // Recent notifications
        sampleNotifications.add(new Notification(
            "1", "New Order Received", 
            "Order #12345 has been placed by John Doe. Please prepare the items.",
            Notification.TYPE_ORDER, new Date(System.currentTimeMillis() - 2 * 60 * 1000)
        ));
        
        sampleNotifications.add(new Notification(
            "2", "Payment Received", 
            "Payment of ₹450 has been received for Order #12344.",
            Notification.TYPE_PAYMENT, new Date(System.currentTimeMillis() - 15 * 60 * 1000)
        ));
        
        sampleNotifications.add(new Notification(
            "3", "Menu Item Approved", 
            "Your new menu item 'Butter Chicken' has been approved and is now live.",
            Notification.TYPE_MENU, new Date(System.currentTimeMillis() - 2 * 60 * 60 * 1000)
        ));
        
        sampleNotifications.add(new Notification(
            "4", "Order Delivered", 
            "Order #12343 has been successfully delivered to the customer.",
            Notification.TYPE_ORDER, new Date(System.currentTimeMillis() - 4 * 60 * 60 * 1000)
        ));
        
        sampleNotifications.add(new Notification(
            "5", "Profile Update Required", 
            "Please update your restaurant timings and contact information.",
            Notification.TYPE_PROFILE, new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        ));
        
        sampleNotifications.add(new Notification(
            "6", "System Maintenance", 
            "Scheduled maintenance will occur tonight from 2 AM to 4 AM.",
            Notification.TYPE_SYSTEM, new Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
        ));

        // Set some as read
        sampleNotifications.get(2).setRead(true);
        sampleNotifications.get(4).setRead(true);
        sampleNotifications.get(5).setRead(true);

        // Set order IDs for order notifications
        sampleNotifications.get(0).setOrderId("12345");
        sampleNotifications.get(3).setOrderId("12343");

        return sampleNotifications;
    }

    // Getters for LiveData
    public LiveData<List<Notification>> getNotifications() {
        return notifications;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up Firebase listeners
        if (notificationsListener != null && notificationsRef != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }
    }
}
