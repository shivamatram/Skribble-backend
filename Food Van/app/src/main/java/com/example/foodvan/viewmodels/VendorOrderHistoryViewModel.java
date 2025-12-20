package com.example.foodvan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.Order;
import com.example.foodvan.repositories.OrderRepository;

import java.util.List;

public class VendorOrderHistoryViewModel extends AndroidViewModel {

    private final OrderRepository repository;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public VendorOrderHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new OrderRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<Order>> getOrders() {
        return orders;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadVendorOrders() {
        isLoading.setValue(true);
        
        repository.loadVendorOrders(new OrderRepository.OnOrdersLoadListener() {
            @Override
            public void onSuccess(List<Order> orderList) {
                isLoading.postValue(false);
                orders.postValue(orderList);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void refreshOrders() {
        loadVendorOrders();
    }

    public void clearErrorMessage() {
        errorMessage.setValue(null);
    }
}
