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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * VendorAnalyticsViewModel - MVVM ViewModel for analytics data management
 * Provides LiveData for real-time analytics updates
 */
public class VendorAnalyticsViewModel extends ViewModel {

    // Firebase components
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
    
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public VendorAnalyticsViewModel() {
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
            vendorRef = database.getReference("vendors").child(vendorId);
            ordersRef = database.getReference("orders");
            earningsRef = database.getReference("earnings").child(vendorId);
        }
    }

    /**
     * Load initial sample data and start real-time updates
     */
    private void loadInitialData() {
        // Set initial sample data
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
        
        isLoading.setValue(false);
        
        // Start real-time updates
        if (vendorId != null) {
            loadRealTimeData();
        }
    }

    /**
     * Load real-time data from Firebase
     */
    public void loadRealTimeData() {
        if (vendorId == null) {
            errorMessage.setValue("User not authenticated");
            return;
        }

        isLoading.setValue(true);

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
                isLoading.setValue(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorMessage.setValue("Failed to load earnings: " + error.getMessage());
                isLoading.setValue(false);
            }
        });

        // Load orders data
        loadOrdersData();
    }

    /**
     * Load orders data and calculate metrics
     */
    private void loadOrdersData() {
        ordersRef.orderByChild("vendorId").equalTo(vendorId)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                calculateOrderMetrics(snapshot);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                errorMessage.setValue("Failed to load orders: " + error.getMessage());
            }
        });
    }

    /**
     * Calculate order metrics from Firebase data
     */
    private void calculateOrderMetrics(DataSnapshot snapshot) {
        int todayOrderCount = 0, weekOrderCount = 0, monthOrderCount = 0;
        int completed = 0, pending = 0, cancelled = 0;
        double totalRevenue = 0;
        
        Calendar cal = Calendar.getInstance();
        long todayStart = getStartOfDay(cal.getTime()).getTime();
        long weekStart = getStartOfWeek(cal.getTime()).getTime();
        long monthStart = getStartOfMonth(cal.getTime()).getTime();
        
        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
            try {
                // Parse order data (adjust based on your Order model)
                String status = orderSnapshot.child("status").getValue(String.class);
                Double amount = orderSnapshot.child("totalAmount").getValue(Double.class);
                Long timestamp = orderSnapshot.child("timestamp").getValue(Long.class);
                
                if (timestamp != null && timestamp > 0) {
                    if (timestamp >= todayStart) todayOrderCount++;
                    if (timestamp >= weekStart) weekOrderCount++;
                    if (timestamp >= monthStart) monthOrderCount++;
                    
                    if (amount != null) {
                        totalRevenue += amount;
                    }
                    
                    // Count order statuses
                    if ("completed".equalsIgnoreCase(status)) {
                        completed++;
                    } else if ("pending".equalsIgnoreCase(status)) {
                        pending++;
                    } else if ("cancelled".equalsIgnoreCase(status)) {
                        cancelled++;
                    }
                }
            } catch (Exception e) {
                // Handle parsing errors gracefully
            }
        }
        
        // Update LiveData
        totalOrders.setValue(weekOrderCount);
        completedOrders.setValue(completed);
        pendingOrders.setValue(pending);
        cancelledOrders.setValue(cancelled);
        
        // Calculate metrics
        if (weekOrderCount > 0) {
            avgOrderValue.setValue(totalRevenue / weekOrderCount);
            completionRate.setValue((completed * 100) / weekOrderCount);
        }
        
        // Update week earnings based on calculated revenue
        if (totalRevenue > 0) {
            weekEarnings.setValue(totalRevenue);
        }
    }

    /**
     * Refresh all analytics data
     */
    public void refreshData() {
        loadRealTimeData();
    }

    // Date utility methods
    private Date getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getStartOfWeek(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getStartOfMonth(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
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
    
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up Firebase listeners if needed
    }
}
