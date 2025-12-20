package com.example.foodvan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * AnalyticsViewModel - Manages analytics data for vendor dashboard
 * Provides real-time analytics data from Firebase
 */
public class AnalyticsViewModel extends ViewModel {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference vendorRef, ordersRef, earningsRef;
    private String vendorId;

    // LiveData for analytics metrics
    private MutableLiveData<Double> todayEarnings = new MutableLiveData<>();
    private MutableLiveData<Double> weekEarnings = new MutableLiveData<>();
    private MutableLiveData<Double> monthEarnings = new MutableLiveData<>();
    
    private MutableLiveData<Integer> totalOrders = new MutableLiveData<>();
    private MutableLiveData<Integer> completedOrders = new MutableLiveData<>();
    private MutableLiveData<Integer> pendingOrders = new MutableLiveData<>();
    private MutableLiveData<Integer> cancelledOrders = new MutableLiveData<>();
    
    private MutableLiveData<Double> avgOrderValue = new MutableLiveData<>();
    private MutableLiveData<Double> customerRating = new MutableLiveData<>();
    private MutableLiveData<Integer> completionRate = new MutableLiveData<>();
    
    private MutableLiveData<String> highestSellingItem = new MutableLiveData<>();
    private MutableLiveData<String> peakHours = new MutableLiveData<>();

    public AnalyticsViewModel() {
        initializeFirebase();
        loadSampleData(); // Load sample data initially
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            vendorId = firebaseAuth.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            vendorRef = database.getReference("vendors").child(vendorId);
            ordersRef = database.getReference("orders");
            earningsRef = database.getReference("earnings").child(vendorId);
        }
    }

    private void loadSampleData() {
        // Set sample data for demonstration
        todayEarnings.setValue(450.0);
        weekEarnings.setValue(2450.0);
        monthEarnings.setValue(9850.0);
        
        totalOrders.setValue(24);
        completedOrders.setValue(23);
        pendingOrders.setValue(1);
        cancelledOrders.setValue(0);
        
        avgOrderValue.setValue(102.0);
        customerRating.setValue(4.8);
        completionRate.setValue(96);
        
        highestSellingItem.setValue("Butter Chicken");
        peakHours.setValue("12-2 PM");
    }

    public void loadRealTimeData() {
        if (vendorId == null) return;

        // Load today's earnings
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        earningsRef.child(todayDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double earnings = snapshot.getValue(Double.class);
                    if (earnings != null) {
                        todayEarnings.setValue(earnings);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });

        // Load orders data
        ordersRef.orderByChild("vendorId").equalTo(vendorId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int total = 0, completed = 0, pending = 0, cancelled = 0;
                
                for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                    total++;
                    // You would parse order status here
                    // For now, using sample logic
                    completed = (int) (total * 0.95); // 95% completion rate
                    pending = total - completed;
                }
                
                totalOrders.setValue(total);
                completedOrders.setValue(completed);
                pendingOrders.setValue(pending);
                cancelledOrders.setValue(cancelled);
                
                // Calculate completion rate
                if (total > 0) {
                    completionRate.setValue((completed * 100) / total);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error
            }
        });
    }

    // Getters for LiveData
    public LiveData<Double> getTodayEarnings() { return todayEarnings; }
    public LiveData<Double> getWeekEarnings() { return weekEarnings; }
    public LiveData<Double> getMonthEarnings() { return monthEarnings; }
    
    public LiveData<Integer> getTotalOrders() { return totalOrders; }
    public LiveData<Integer> getCompletedOrders() { return completedOrders; }
    public LiveData<Integer> getPendingOrders() { return pendingOrders; }
    public LiveData<Integer> getCancelledOrders() { return cancelledOrders; }
    
    public LiveData<Double> getAvgOrderValue() { return avgOrderValue; }
    public LiveData<Double> getCustomerRating() { return customerRating; }
    public LiveData<Integer> getCompletionRate() { return completionRate; }
    
    public LiveData<String> getHighestSellingItem() { return highestSellingItem; }
    public LiveData<String> getPeakHours() { return peakHours; }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up listeners if needed
    }
}
