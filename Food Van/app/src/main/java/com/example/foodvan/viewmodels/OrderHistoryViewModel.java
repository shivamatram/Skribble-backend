package com.example.foodvan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.foodvan.models.Order;
import com.example.foodvan.repositories.OrderRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for Order History screen
 * Manages order data and UI state using MVVM pattern
 */
public class OrderHistoryViewModel extends ViewModel {

    private OrderRepository orderRepository;
    
    // LiveData for UI state
    private MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();

    public OrderHistoryViewModel() {
        // Initialize repository (will be replaced with proper DI)
        // this.orderRepository = new OrderRepository();
        
        // Initialize with empty list
        orders.setValue(new ArrayList<>());
        isLoading.setValue(false);
    }

    // Getters for LiveData
    public LiveData<List<Order>> getOrders() {
        return orders;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    // Public methods for UI interactions
    public void loadOrders() {
        isLoading.setValue(true);
        
        // TODO: Replace with actual Firebase/Repository call
        // For now, create sample data
        createSampleOrders();
    }

    public void refreshOrders() {
        loadOrders();
    }

    public void filterOrders(String status) {
        // TODO: Implement filtering logic
        List<Order> allOrders = orders.getValue();
        if (allOrders == null) return;

        List<Order> filteredOrders = new ArrayList<>();
        
        if ("ALL".equals(status)) {
            filteredOrders.addAll(allOrders);
        } else {
            for (Order order : allOrders) {
                if (status.equals(order.getStatus())) {
                    filteredOrders.add(order);
                }
            }
        }
        
        orders.setValue(filteredOrders);
    }

    private void createSampleOrders() {
        // Create sample orders for testing
        List<Order> sampleOrders = new ArrayList<>();
        
        // Sample Order 1
        Order order1 = new Order();
        order1.setId("ORD12489");
        order1.setCustomerName("Rajesh Kumar");
        order1.setStatus("COMPLETED");
        order1.setPaymentStatus("Paid");
        order1.setTotalAmount(245.0);
        order1.setOrderTime(System.currentTimeMillis() - 3600000); // 1 hour ago
        sampleOrders.add(order1);
        
        // Sample Order 2
        Order order2 = new Order();
        order2.setId("ORD12490");
        order2.setCustomerName("Priya Sharma");
        order2.setStatus("PENDING");
        order2.setPaymentStatus("COD");
        order2.setTotalAmount(180.0);
        order2.setOrderTime(System.currentTimeMillis() - 1800000); // 30 minutes ago
        sampleOrders.add(order2);
        
        // Sample Order 3
        Order order3 = new Order();
        order3.setId("ORD12491");
        order3.setCustomerName("Amit Singh");
        order3.setStatus("CANCELLED");
        order3.setPaymentStatus("Refunded");
        order3.setTotalAmount(320.0);
        order3.setOrderTime(System.currentTimeMillis() - 7200000); // 2 hours ago
        sampleOrders.add(order3);
        
        // Update LiveData
        orders.setValue(sampleOrders);
        isLoading.setValue(false);
        successMessage.setValue("Orders loaded successfully");
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up resources if needed
    }
}
