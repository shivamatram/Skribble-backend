package com.example.foodvan.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.MenuItem;
import com.example.foodvan.repositories.MenuItemRepository;

public class AddFoodItemViewModel extends AndroidViewModel {

    private final MenuItemRepository repository;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AddFoodItemViewModel(@NonNull Application application) {
        super(application);
        repository = new MenuItemRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void addMenuItem(MenuItem menuItem) {
        isLoading.setValue(true);
        
        repository.addMenuItem(menuItem, new MenuItemRepository.OnMenuItemOperationListener() {
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
