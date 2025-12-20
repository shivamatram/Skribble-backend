package com.example.foodvan.repositories;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.HashMap;
import java.util.Map;

import com.example.foodvan.models.DefaultLocation;
import com.example.foodvan.models.GpsSettings;
import com.example.foodvan.models.LocationHistory;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Live Location functionality
 * Handles data operations for location services, Firebase Firestore, and local storage
 */
public class LocationRepository {

    private static final String TAG = "LocationRepository";
    private static final String PREFS_NAME = "location_prefs";
    private static final String KEY_LAST_LOCATION_TIMESTAMP = "last_location_timestamp";

    // Firebase and Location Services
    private final FirebaseFirestore firestore;
    private final FusedLocationProviderClient fusedLocationClient;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private final String vendorId;

    // Firestore paths
    private static final String COLLECTION_VENDORS = "vendors";
    private static final String COLLECTION_LIVE_LOCATION = "liveLocation";
    private static final String COLLECTION_DEFAULT_LOCATION = "defaultLocation";
    private static final String COLLECTION_LOCATION_HISTORY = "locationHistory";
    private static final String COLLECTION_GPS_SETTINGS = "gpsSettings";

    public LocationRepository(Context context, String vendorId) {
        this.context = context;
        this.vendorId = vendorId;
        this.firestore = FirebaseFirestore.getInstance();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Callback interfaces
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    public interface DefaultLocationCallback {
        void onDefaultLocationReceived(DefaultLocation location);
        void onDefaultLocationError(String error);
    }

    public interface LocationHistoryCallback {
        void onLocationHistoryReceived(List<LocationHistory> history);
        void onLocationHistoryError(String error);
    }

    public interface GpsSettingsCallback {
        void onGpsSettingsReceived(GpsSettings settings);
        void onGpsSettingsError(String error);
    }

    // Current Location Operations
    public void getCurrentLocation(LocationCallback callback) {
        if (!hasLocationPermissions()) {
            callback.onLocationError("Location permissions not granted");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();

        try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Save to Firestore
                            saveLiveLocationToFirestore(location);
                            // Save timestamp locally
                            saveLastLocationTimestamp(System.currentTimeMillis());
                            callback.onLocationReceived(location);
                        } else {
                            callback.onLocationError("Unable to get current location");
                        }
                    })
                    .addOnFailureListener(e -> callback.onLocationError("Location error: " + e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationError("Location permission denied");
        }
    }

    public void getLastKnownLocation(LocationCallback callback) {
        if (!hasLocationPermissions()) {
            callback.onLocationError("Location permissions not granted");
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> callback.onLocationReceived(location))
                    .addOnFailureListener(e -> callback.onLocationError("Error getting last location: " + e.getMessage()));
        } catch (SecurityException e) {
            callback.onLocationError("Location permission denied");
        }
    }

    private void saveLiveLocationToFirestore(Location location) {
        if (vendorId == null) return;

        // Create a proper data map for Firestore
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("geoPoint", new com.google.firebase.firestore.GeoPoint(location.getLatitude(), location.getLongitude()));
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("lastUpdated", System.currentTimeMillis());
        locationData.put("accuracy", location.getAccuracy());
        locationData.put("vendorId", vendorId);

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_LIVE_LOCATION)
                .document("current")
                .set(locationData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Live location saved to Firestore successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving live location to Firestore: " + e.getMessage(), e);
                });
    }

    // Default Location Operations
    public void getDefaultLocation(DefaultLocationCallback callback) {
        if (vendorId == null) {
            callback.onDefaultLocationError("Vendor ID not available");
            return;
        }

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_DEFAULT_LOCATION)
                .document("default")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DefaultLocation location = documentSnapshot.toObject(DefaultLocation.class);
                        callback.onDefaultLocationReceived(location);
                    } else {
                        callback.onDefaultLocationReceived(null);
                    }
                })
                .addOnFailureListener(e -> {
                    // Check if the error is due to offline state
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (errorMessage.contains("offline") || errorMessage.contains("UNAVAILABLE"))) {
                        // Silently handle offline state - don't show error to user
                        Log.w(TAG, "App is offline, cannot load default location");
                        callback.onDefaultLocationReceived(null);
                    } else {
                        // Show other errors
                        callback.onDefaultLocationError("Error loading default location: " + e.getMessage());
                    }
                });
    }

    public void saveDefaultLocation(DefaultLocation location, DefaultLocationCallback callback) {
        if (vendorId == null) {
            callback.onDefaultLocationError("Vendor ID not available");
            return;
        }

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_DEFAULT_LOCATION)
                .document("default")
                .set(location)
                .addOnSuccessListener(aVoid -> callback.onDefaultLocationReceived(location))
                .addOnFailureListener(e -> callback.onDefaultLocationError("Error saving default location: " + e.getMessage()));
    }

    // Location History Operations
    public void getLocationHistory(LocationHistoryCallback callback) {
        if (vendorId == null) {
            callback.onLocationHistoryError("Vendor ID not available");
            return;
        }

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_LOCATION_HISTORY)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50) // Limit to last 50 entries
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<LocationHistory> historyList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        LocationHistory history = document.toObject(LocationHistory.class);
                        historyList.add(history);
                    }
                    callback.onLocationHistoryReceived(historyList);
                })
                .addOnFailureListener(e -> {
                    // Check if the error is due to offline state
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (errorMessage.contains("offline") || errorMessage.contains("UNAVAILABLE"))) {
                        // Silently handle offline state - return empty list
                        Log.w(TAG, "App is offline, cannot load location history");
                        callback.onLocationHistoryReceived(new ArrayList<>());
                    } else {
                        // Show other errors
                        callback.onLocationHistoryError("Error loading location history: " + e.getMessage());
                    }
                });
    }

    public void saveLocationHistory(LocationHistory history, LocationHistoryCallback callback) {
        if (vendorId == null) {
            callback.onLocationHistoryError("Vendor ID not available");
            return;
        }

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_LOCATION_HISTORY)
                .document(history.getHistoryId())
                .set(history)
                .addOnSuccessListener(aVoid -> {
                    // Optionally return updated list
                    getLocationHistory(callback);
                })
                .addOnFailureListener(e -> callback.onLocationHistoryError("Error saving location history: " + e.getMessage()));
    }

    public void clearLocationHistory(LocationHistoryCallback callback) {
        if (vendorId == null) {
            callback.onLocationHistoryError("Vendor ID not available");
            return;
        }

        // Delete all documents in location history collection
        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_LOCATION_HISTORY)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    callback.onLocationHistoryReceived(new ArrayList<>());
                })
                .addOnFailureListener(e -> callback.onLocationHistoryError("Error clearing location history: " + e.getMessage()));
    }

    // GPS Settings Operations
    public void getGpsSettings(GpsSettingsCallback callback) {
        if (vendorId == null) {
            callback.onGpsSettingsError("Vendor ID not available");
            return;
        }

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_GPS_SETTINGS)
                .document("settings")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        GpsSettings settings = documentSnapshot.toObject(GpsSettings.class);
                        callback.onGpsSettingsReceived(settings);
                    } else {
                        // Return default settings
                        GpsSettings defaultSettings = new GpsSettings(vendorId);
                        callback.onGpsSettingsReceived(defaultSettings);
                    }
                })
                .addOnFailureListener(e -> {
                    // Check if the error is due to offline state
                    String errorMessage = e.getMessage();
                    if (errorMessage != null && (errorMessage.contains("offline") || errorMessage.contains("UNAVAILABLE"))) {
                        // Silently handle offline state - return default settings
                        Log.w(TAG, "App is offline, using default GPS settings");
                        GpsSettings defaultSettings = new GpsSettings(vendorId);
                        callback.onGpsSettingsReceived(defaultSettings);
                    } else {
                        // Show other errors
                        callback.onGpsSettingsError("Error loading GPS settings: " + e.getMessage());
                    }
                });
    }

    public void saveGpsSettings(GpsSettings settings, GpsSettingsCallback callback) {
        if (vendorId == null) {
            callback.onGpsSettingsError("Vendor ID not available");
            return;
        }

        firestore.collection(COLLECTION_VENDORS)
                .document(vendorId)
                .collection(COLLECTION_GPS_SETTINGS)
                .document("settings")
                .set(settings)
                .addOnSuccessListener(aVoid -> callback.onGpsSettingsReceived(settings))
                .addOnFailureListener(e -> callback.onGpsSettingsError("Error saving GPS settings: " + e.getMessage()));
    }

    // Utility methods
    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void saveLastLocationTimestamp(long timestamp) {
        sharedPreferences.edit()
                .putLong(KEY_LAST_LOCATION_TIMESTAMP, timestamp)
                .apply();
    }

    public long getLastLocationTimestamp() {
        return sharedPreferences.getLong(KEY_LAST_LOCATION_TIMESTAMP, 0);
    }
}
