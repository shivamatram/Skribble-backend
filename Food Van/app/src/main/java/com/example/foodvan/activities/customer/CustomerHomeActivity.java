package com.example.foodvan.activities.customer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.util.Log;

import com.example.foodvan.R;
import com.example.foodvan.activities.auth.LoginActivity;
import com.example.foodvan.adapters.FoodVanAdapter;
import com.example.foodvan.models.FoodVan;
import com.example.foodvan.models.FilterCriteria;
import com.example.foodvan.models.User;
import com.example.foodvan.utils.FirebaseManager;
import com.example.foodvan.utils.LocationHelper;
import com.example.foodvan.utils.SessionManager;
import com.example.foodvan.utils.FilterManager;
import com.example.foodvan.fragments.FilterBottomSheetFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomerHomeActivity - Main dashboard for customers
 * Features: Google Maps with food van markers, nearby van list, location tracking
 */
public class CustomerHomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private RecyclerView rvNearbyVans;
    private FoodVanAdapter foodVanAdapter;
    private FloatingActionButton fabCart, fabFilter;
    
    private SessionManager sessionManager;
    private FirebaseManager firebaseManager;
    private LocationHelper locationHelper;
    
    private List<FoodVan> nearbyVans;
    private List<FoodVan> allVans;
    private Location currentLocation;
    
    // Filter Components
    private FilterManager filterManager;
    private FilterCriteria currentFilter;
    
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String TAG = "CustomerHomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_customer_home);
            
            initializeViews();
            initializeServices();
            setupRecyclerView();
            setupMap();
            requestLocationPermission();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            // Graceful fallback - finish activity if critical error
            finish();
        }
    }

    private void initializeViews() {
        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
            }
            
            rvNearbyVans = findViewById(R.id.rv_nearby_vans);
            fabCart = findViewById(R.id.fab_cart);
            fabFilter = findViewById(R.id.fab_filter);
            
            // Add null checks for FAB click listeners
            if (fabCart != null) {
                fabCart.setOnClickListener(v -> openCart());
            } else {
                Log.w(TAG, "fabCart not found in layout");
            }
            
            if (fabFilter != null) {
                fabFilter.setOnClickListener(v -> openFilters());
            } else {
                Log.w(TAG, "fabFilter not found in layout");
            }
            
            if (rvNearbyVans == null) {
                Log.e(TAG, "rvNearbyVans not found in layout");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void initializeServices() {
        try {
            sessionManager = new SessionManager(this);
            firebaseManager = new FirebaseManager();
            locationHelper = new LocationHelper(this);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            nearbyVans = new ArrayList<>();
            allVans = new ArrayList<>();
            
            // Initialize filter components
            initializeFilterManager();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing services", e);
            // Initialize with minimal fallbacks
            if (nearbyVans == null) nearbyVans = new ArrayList<>();
            if (allVans == null) allVans = new ArrayList<>();
        }
    }

    private void setupRecyclerView() {
        try {
            if (rvNearbyVans != null && nearbyVans != null) {
                foodVanAdapter = new FoodVanAdapter(nearbyVans, this::onFoodVanClick);
                rvNearbyVans.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
                rvNearbyVans.setAdapter(foodVanAdapter);
            } else {
                Log.e(TAG, "Cannot setup RecyclerView - rvNearbyVans or nearbyVans is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        try {
            mMap = googleMap;
            
            // Configure map settings
            if (mMap != null) {
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                
                // Set map click listener
                mMap.setOnMarkerClickListener(this::onMarkerClick);
                
                // Enable location if permission granted
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                    == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    getCurrentLocation();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map", e);
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
            != PackageManager.PERMISSION_GRANTED) {
            
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED && fusedLocationClient != null) {
                
                fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        try {
                            if (location != null && mMap != null) {
                                currentLocation = location;
                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                
                                // Move camera to current location
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                                
                                // Load nearby food vans
                                loadNearbyFoodVans();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing location", e);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                    });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting current location", e);
        }
    }

    private void loadNearbyFoodVans() {
        if (currentLocation == null) return;
        
        firebaseManager.getNearbyFoodVans(
            currentLocation.getLatitude(), 
            currentLocation.getLongitude(), 
            5.0, // 5km radius
            new FirebaseManager.OnFoodVansLoadListener() {
                @Override
                public void onSuccess(List<FoodVan> foodVans) {
                    // Store all vans for filtering
                    allVans.clear();
                    allVans.addAll(foodVans);
                    
                    nearbyVans.clear();
                    nearbyVans.addAll(foodVans);
                    foodVanAdapter.notifyDataSetChanged();
                    
                    // Add markers to map
                    addFoodVanMarkersToMap(foodVans);
                    
                    // Update filter manager with current location
                    if (filterManager != null && currentLocation != null) {
                        filterManager.setUserLocation(currentLocation);
                    }
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(CustomerHomeActivity.this, 
                        "Error loading food vans: " + error, Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void addFoodVanMarkersToMap(List<FoodVan> foodVans) {
        if (mMap == null) return;
        
        mMap.clear(); // Clear existing markers
        
        for (FoodVan van : foodVans) {
            LatLng vanLocation = new LatLng(van.getLatitude(), van.getLongitude());
            
            MarkerOptions markerOptions = new MarkerOptions()
                .position(vanLocation)
                .title(van.getName())
                .snippet(van.getCuisineType() + " • " + van.getDistance() + "km away")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            
            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(van);
            }
        }
    }

    private boolean onMarkerClick(Marker marker) {
        FoodVan van = (FoodVan) marker.getTag();
        if (van != null) {
            openFoodVanMenu(van);
        }
        return true;
    }

    private void onFoodVanClick(FoodVan van) {
        openFoodVanMenu(van);
    }

    private void openFoodVanMenu(FoodVan van) {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("food_van_id", van.getVanId());
        intent.putExtra("food_van_name", van.getName());
        startActivity(intent);
    }

    private void openCart() {
        Intent intent = new Intent(this, CartActivity.class);
        startActivity(intent);
    }

    private void openFilters() {
        Log.d(TAG, "Filter FAB clicked!");
        showFilterBottomSheet();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) 
                        == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }
                }
            } else {
                Toast.makeText(this, "Location permission is required to find nearby food vans", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.customer_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            openProfile();
            return true;
        } else if (id == R.id.action_orders) {
            openOrderHistory();
            return true;
        } else if (id == R.id.action_notifications) {
            openNotifications();
            return true;
        } else if (id == R.id.action_settings) {
            openSettings();
            return true;
        } else if (id == R.id.action_help_support) {
            openHelpSupport();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    private void openOrderHistory() {
        Intent intent = new Intent(this, OrderHistoryActivity.class);
        startActivity(intent);
    }

    private void openNotifications() {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openHelpSupport() {
        // Help & Support feature removed
        Toast.makeText(this, "Help & Support - Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh nearby vans when returning to activity
        if (currentLocation != null) {
            loadNearbyFoodVans();
        }
    }
    
    // Filter-related methods
    private void initializeFilterManager() {
        filterManager = new FilterManager(this);
        currentFilter = new FilterCriteria();
        
        // Set user location for distance calculations
        if (currentLocation != null) {
            filterManager.setUserLocation(currentLocation);
        }
    }
    
    private void showFilterBottomSheet() {
        Log.d(TAG, "Showing filter bottom sheet...");
        
        try {
            FilterBottomSheetFragment filterFragment = FilterBottomSheetFragment.newInstance(currentFilter);
            filterFragment.setFilterApplyListener(new FilterBottomSheetFragment.FilterApplyListener() {
                @Override
                public void onFiltersApplied(FilterCriteria criteria) {
                    currentFilter = criteria;
                    applyFiltersToVans();
                    updateFilterFabBadge();
                }
                
                @Override
                public void onFiltersCleared() {
                    currentFilter.reset();
                    applyFiltersToVans();
                    updateFilterFabBadge();
                }
            });
            
            filterFragment.show(getSupportFragmentManager(), "FilterBottomSheet");
            Log.d(TAG, "Filter bottom sheet shown successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing filter bottom sheet: " + e.getMessage());
            Toast.makeText(this, "Error opening filters: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void applyFiltersToVans() {
        if (filterManager == null || allVans.isEmpty()) {
            Log.d(TAG, "No filter manager or vans to filter");
            return;
        }
        
        // Show loading indicator
        Toast.makeText(this, "Applying filters...", Toast.LENGTH_SHORT).show();
        
        // Convert FoodVan list to User list for filtering
        List<User> vendorUsers = convertFoodVansToUsers(allVans);
        
        filterManager.applyFilters(currentFilter, new FilterManager.FilterResultCallback() {
            @Override
            public void onFilterResults(List<User> filteredVendors, int totalCount) {
                runOnUiThread(() -> {
                    // Convert back to FoodVan and update UI
                    List<FoodVan> filteredVans = convertUsersToFoodVans(filteredVendors);
                    updateVansList(filteredVans);
                    updateMapMarkers(filteredVans);
                    
                    String message = filteredVans.size() + " of " + totalCount + " food vans found";
                    Toast.makeText(CustomerHomeActivity.this, message, Toast.LENGTH_SHORT).show();
                    
                    Log.d(TAG, "Filters applied: " + message);
                });
            }
            
            @Override
            public void onFilterError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(CustomerHomeActivity.this, "Filter error: " + error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Filter error: " + error);
                });
            }
        });
    }
    
    private void updateVansList(List<FoodVan> filteredVans) {
        nearbyVans.clear();
        nearbyVans.addAll(filteredVans);
        foodVanAdapter.notifyDataSetChanged();
    }
    
    private void updateMapMarkers(List<FoodVan> filteredVans) {
        if (mMap == null) return;
        
        mMap.clear(); // Clear existing markers
        
        for (FoodVan van : filteredVans) {
            LatLng vanLocation = new LatLng(van.getLatitude(), van.getLongitude());
            
            MarkerOptions markerOptions = new MarkerOptions()
                .position(vanLocation)
                .title(van.getName())
                .snippet(van.getCuisineType() + " • " + van.getDistance() + "km away")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            
            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(van);
            }
        }
    }
    
    private void updateFilterFabBadge() {
        // Update filter FAB appearance based on active filters
        if (currentFilter.hasActiveFilters()) {
            // Change FAB color to indicate active filters
            fabFilter.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.primary_red)));
            fabFilter.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.white)));
        } else {
            // Reset to default appearance
            fabFilter.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, android.R.color.white)));
            fabFilter.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.text_primary)));
        }
    }
    
    private List<User> convertFoodVansToUsers(List<FoodVan> foodVans) {
        List<User> users = new ArrayList<>();
        for (FoodVan van : foodVans) {
            User user = new User();
            user.setUserId(van.getVanId());
            user.setBusinessName(van.getName());
            user.setLatitude(van.getLatitude());
            user.setLongitude(van.getLongitude());
            user.setRating(van.getRating());
            user.setOnline(van.isOnline());
            // Set other properties as needed
            users.add(user);
        }
        return users;
    }
    
    private List<FoodVan> convertUsersToFoodVans(List<User> users) {
        List<FoodVan> foodVans = new ArrayList<>();
        for (User user : users) {
            // Find the original FoodVan from allVans list
            for (FoodVan van : allVans) {
                if (van.getVanId().equals(user.getUserId())) {
                    foodVans.add(van);
                    break;
                }
            }
        }
        return foodVans;
    }
}
