package com.example.foodvan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.MenuItem;
import com.example.foodvan.repositories.MenuItemRepository;

import java.util.List;

public class ManageMenuViewModel extends AndroidViewModel {

    private final MenuItemRepository repository;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<MenuItem>> menuItems = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public ManageMenuViewModel(@NonNull Application application) {
        super(application);
        repository = new MenuItemRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<List<MenuItem>> getMenuItems() {
        return menuItems;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadMenuItems() {
        isLoading.setValue(true);
        
        repository.loadVendorMenuItems(new MenuItemRepository.OnMenuItemsLoadListener() {
            @Override
            public void onSuccess(List<MenuItem> items) {
                isLoading.postValue(false);
                menuItems.postValue(items);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void updateMenuItem(MenuItem menuItem) {
        isLoading.setValue(true);
        
        repository.updateMenuItem(menuItem, new MenuItemRepository.OnMenuItemOperationListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void deleteMenuItem(String menuItemId) {
        isLoading.setValue(true);
        
        repository.deleteMenuItem(menuItemId, new MenuItemRepository.OnMenuItemOperationListener() {
            @Override
            public void onSuccess(String message) {
                isLoading.postValue(false);
                successMessage.postValue(message);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    public void clearMessages() {
        successMessage.setValue(null);
        errorMessage.setValue(null);
    }
}
