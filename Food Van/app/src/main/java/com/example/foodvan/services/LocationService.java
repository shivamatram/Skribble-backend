package com.example.foodvan.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationService {
    
    private static final String TAG = "LocationService";
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds
    private static final long FASTEST_INTERVAL = 5000; // 5 seconds
    private static final float MIN_DISPLACEMENT = 10; // 10 meters

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LocationUpdateListener locationUpdateListener;

    public interface LocationUpdateListener {
        void onLocationReceived(Location location);
        default void onLocationError(String error) {
            Log.e(TAG, "Location error: " + error);
        }
    }

    public LocationService(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        initializeLocationRequest();
        initializeLocationCallback();
    }

    private void initializeLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL * 2)
                .setMinUpdateDistanceMeters(MIN_DISPLACEMENT)
                .build();
    }

    private void initializeLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    if (locationUpdateListener != null) {
                        locationUpdateListener.onLocationError("Location result is null");
                    }
                    return;
                }

                Location location = locationResult.getLastLocation();
                if (location != null && locationUpdateListener != null) {
                    Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    locationUpdateListener.onLocationReceived(location);
                } else if (locationUpdateListener != null) {
                    locationUpdateListener.onLocationError("Location is null");
                }
            }
        };
    }

    public void requestLocationUpdates(LocationUpdateListener listener) {
        this.locationUpdateListener = listener;

        if (!hasLocationPermission()) {
            if (locationUpdateListener != null) {
                locationUpdateListener.onLocationError("Location permission not granted");
            }
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );
            Log.d(TAG, "Location updates requested");
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location updates: " + e.getMessage());
            if (locationUpdateListener != null) {
                locationUpdateListener.onLocationError("Security exception: " + e.getMessage());
            }
        }
    }

    public void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d(TAG, "Location updates stopped");
        }
    }

    public void getCurrentLocation(LocationUpdateListener listener) {
        if (!hasLocationPermission()) {
            if (listener != null) {
                listener.onLocationError("Location permission not granted");
            }
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null && listener != null) {
                            Log.d(TAG, "Current location retrieved: " + location.getLatitude() + ", " + location.getLongitude());
                            listener.onLocationReceived(location);
                        } else if (listener != null) {
                            listener.onLocationError("Unable to get current location");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get current location: " + e.getMessage());
                        if (listener != null) {
                            listener.onLocationError("Failed to get location: " + e.getMessage());
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting current location: " + e.getMessage());
            if (listener != null) {
                listener.onLocationError("Security exception: " + e.getMessage());
            }
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    public void setUpdateInterval(long intervalMs) {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(intervalMs / 2)
                .setMaxUpdateDelayMillis(intervalMs * 2)
                .setMinUpdateDistanceMeters(MIN_DISPLACEMENT)
                .build();
    }

    public void setMinDisplacement(float meters) {
        // Recreate location request with new displacement
        long currentInterval = UPDATE_INTERVAL;
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, currentInterval)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(currentInterval * 2)
                .setMinUpdateDistanceMeters(meters)
                .build();
    }

    public boolean isLocationEnabled() {
        // This is a simplified check - in production, you might want to check
        // if location services are enabled on the device
        return hasLocationPermission();
    }

    public void cleanup() {
        stopLocationUpdates();
        locationUpdateListener = null;
    }
}
