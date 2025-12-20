package com.example.foodvan.viewmodels;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.foodvan.models.DefaultLocation;
import com.example.foodvan.models.GpsSettings;
import com.example.foodvan.models.LocationHistory;
import com.example.foodvan.repositories.LocationRepository;
import com.example.foodvan.utils.SessionManager;

import java.util.List;

/**
 * ViewModel for Live Location Update functionality
 * Manages location data, settings, and history using MVVM architecture
 */
public class LocationViewModel extends AndroidViewModel {

    private static final String TAG = "LocationViewModel";

    // Repository
    private final LocationRepository locationRepository;
    private final SessionManager sessionManager;

    // LiveData for UI observation
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<Long> lastUpdated = new MutableLiveData<>();
    private final MutableLiveData<DefaultLocation> defaultLocation = new MutableLiveData<>();
    private final MutableLiveData<GpsSettings> gpsSettings = new MutableLiveData<>();
    private final MutableLiveData<List<LocationHistory>> locationHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LocationViewModel(@NonNull Application application) {
        super(application);
        sessionManager = new SessionManager(application);
        locationRepository = new LocationRepository(application, sessionManager.getUserId());
        
        // Initialize default values
        lastUpdated.setValue(0L);
    }

    // Getters for LiveData
    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<Long> getLastUpdated() {
        return lastUpdated;
    }

    public LiveData<DefaultLocation> getDefaultLocation() {
        return defaultLocation;
    }

    public LiveData<GpsSettings> getGpsSettings() {
        return gpsSettings;
    }

    public LiveData<List<LocationHistory>> getLocationHistory() {
        return locationHistory;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    // Current Location Operations
    public void updateCurrentLocation() {
        isLoading.setValue(true);
        locationRepository.getCurrentLocation(new LocationRepository.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                currentLocation.setValue(location);
                lastUpdated.setValue(System.currentTimeMillis());
                isLoading.setValue(false);
                
                // Save to history
                saveLocationToHistory(location);
            }

            @Override
            public void onLocationError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    public void loadLastKnownLocation() {
        locationRepository.getLastKnownLocation(new LocationRepository.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                if (location != null) {
                    currentLocation.setValue(location);
                    // Load timestamp from preferences or database
                    long timestamp = locationRepository.getLastLocationTimestamp();
                    lastUpdated.setValue(timestamp);
                }
            }

            @Override
            public void onLocationError(String error) {
                // Silent error for last known location
            }
        });
    }

    public void getCurrentLocationForDefault() {
        isLoading.setValue(true);
        locationRepository.getCurrentLocation(new LocationRepository.LocationCallback() {
            @Override
            public void onLocationReceived(Location location) {
                // This can be used to populate default location form
                currentLocation.setValue(location);
                isLoading.setValue(false);
            }

            @Override
            public void onLocationError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    // Default Location Operations
    public void loadDefaultLocation() {
        locationRepository.getDefaultLocation(new LocationRepository.DefaultLocationCallback() {
            @Override
            public void onDefaultLocationReceived(DefaultLocation location) {
                defaultLocation.setValue(location);
            }

            @Override
            public void onDefaultLocationError(String error) {
                // Only show error if it's not an offline error
                if (error != null && !error.toLowerCase().contains("offline") && !error.toLowerCase().contains("unavailable")) {
                    errorMessage.setValue(error);
                }
                // For offline errors, silently handle them without showing error to user
            }
        });
    }

    public void saveDefaultLocation(DefaultLocation location) {
        isLoading.setValue(true);
        location.setVendorId(sessionManager.getUserId());
        location.setLastUpdated(System.currentTimeMillis());
        
        locationRepository.saveDefaultLocation(location, new LocationRepository.DefaultLocationCallback() {
            @Override
            public void onDefaultLocationReceived(DefaultLocation savedLocation) {
                defaultLocation.setValue(savedLocation);
                isLoading.setValue(false);
            }

            @Override
            public void onDefaultLocationError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    // Location History Operations
    public void loadLocationHistory() {
        locationRepository.getLocationHistory(new LocationRepository.LocationHistoryCallback() {
            @Override
            public void onLocationHistoryReceived(List<LocationHistory> history) {
                locationHistory.setValue(history);
            }

            @Override
            public void onLocationHistoryError(String error) {
                // Only show error if it's not an offline error
                if (error != null && !error.toLowerCase().contains("offline") && !error.toLowerCase().contains("unavailable")) {
                    errorMessage.setValue(error);
                }
                // For offline errors, silently handle them without showing error to user
            }
        });
    }

    public void clearLocationHistory() {
        locationRepository.clearLocationHistory(new LocationRepository.LocationHistoryCallback() {
            @Override
            public void onLocationHistoryReceived(List<LocationHistory> history) {
                locationHistory.setValue(history); // Should be empty
            }

            @Override
            public void onLocationHistoryError(String error) {
                errorMessage.setValue(error);
            }
        });
    }

    private void saveLocationToHistory(Location location) {
        if (location != null) {
            LocationHistory history = new LocationHistory(
                sessionManager.getUserId(),
                location.getLatitude(),
                location.getLongitude(),
                "Location updated", // This would be reverse-geocoded in a real app
                location.getAccuracy()
            );
            
            locationRepository.saveLocationHistory(history, new LocationRepository.LocationHistoryCallback() {
                @Override
                public void onLocationHistoryReceived(List<LocationHistory> historyList) {
                    // Optionally refresh history list
                }

                @Override
                public void onLocationHistoryError(String error) {
                    // Silent error for history saving
                }
            });
        }
    }

    // GPS Settings Operations
    public void loadGpsSettings() {
        locationRepository.getGpsSettings(new LocationRepository.GpsSettingsCallback() {
            @Override
            public void onGpsSettingsReceived(GpsSettings settings) {
                gpsSettings.setValue(settings);
            }

            @Override
            public void onGpsSettingsError(String error) {
                // Create default settings if none exist or if offline
                GpsSettings defaultSettings = new GpsSettings(sessionManager.getUserId());
                gpsSettings.setValue(defaultSettings);
                
                // Only show error if it's not an offline error
                if (error != null && !error.toLowerCase().contains("offline") && !error.toLowerCase().contains("unavailable")) {
                    errorMessage.setValue(error);
                }
            }
        });
    }

    public void saveGpsSettings(GpsSettings settings) {
        isLoading.setValue(true);
        settings.setVendorId(sessionManager.getUserId());
        settings.setLastUpdated(System.currentTimeMillis());
        
        locationRepository.saveGpsSettings(settings, new LocationRepository.GpsSettingsCallback() {
            @Override
            public void onGpsSettingsReceived(GpsSettings savedSettings) {
                gpsSettings.setValue(savedSettings);
                isLoading.setValue(false);
            }

            @Override
            public void onGpsSettingsError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    // Utility methods
    public void clearError() {
        errorMessage.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Clean up any resources if needed
    }
}
