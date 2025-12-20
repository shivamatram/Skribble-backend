package com.example.foodvan.activities.customer;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.foodvan.R;
import com.example.foodvan.models.FoodVan;
import com.example.foodvan.models.FilterCriteria;
import com.example.foodvan.models.User;
import com.example.foodvan.services.LocationService;
import com.example.foodvan.utils.MapStyleUtils;
import com.example.foodvan.utils.FilterManager;
import com.example.foodvan.fragments.FilterBottomSheetFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "CustomerMapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final float DEFAULT_ZOOM = 15f;
    private static final long LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds

    // UI Components
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    private FloatingActionButton fabMyLocation;
    private FloatingActionButton fabMapStyle;
    private FloatingActionButton fabFilter;
    private ImageButton btnBack;
    private MaterialCardView bottomSheet;
    private BottomSheetBehavior<MaterialCardView> bottomSheetBehavior;
    private TextView tvVendorName, tvVendorDistance, tvVendorETA, tvVendorRating;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationService locationService;
    private LatLng currentLocation;

    // Firebase
    private DatabaseReference vendorsRef;
    private ValueEventListener vendorsListener;

    // Map Data
    private Map<String, Marker> vendorMarkers = new HashMap<>();
    private List<FoodVan> nearbyVendors = new ArrayList<>();
    private List<User> allVendors = new ArrayList<>();
    private List<User> filteredVendors = new ArrayList<>();
    private boolean isMapStyleDark = false;
    private Handler locationUpdateHandler;
    private Runnable locationUpdateRunnable;
    
    // Filter Components
    private FilterManager filterManager;
    private FilterCriteria currentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);

        initializeViews();
        setupMapFragment();
        initializeLocationServices();
        setupFirebase();
        initializeFilterManager();
        setupBottomSheet();
        setupClickListeners();
        startLocationUpdates();
    }

    private void initializeViews() {
        fabMyLocation = findViewById(R.id.fab_my_location);
        fabMapStyle = findViewById(R.id.fab_map_style);
        fabFilter = findViewById(R.id.fab_filter);
        btnBack = findViewById(R.id.btn_back);
        bottomSheet = findViewById(R.id.bottom_sheet);
        tvVendorName = findViewById(R.id.tv_vendor_name);
        tvVendorDistance = findViewById(R.id.tv_vendor_distance);
        tvVendorETA = findViewById(R.id.tv_vendor_eta);
        tvVendorRating = findViewById(R.id.tv_vendor_rating);
    }

    private void setupMapFragment() {
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            Log.d(TAG, "Map fragment found, requesting map...");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment not found! Check if R.id.map_fragment exists in layout.");
            showToast("Error: Map fragment not found. Please check your Google Maps API key setup.");
        }
    }

    private void initializeLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationService = new LocationService(this);
    }

    private void setupFirebase() {
        vendorsRef = FirebaseDatabase.getInstance().getReference("vendors");
        
        vendorsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                updateVendorMarkers(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Firebase error: " + databaseError.getMessage());
                showToast("Error loading vendor data");
            }
        };
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    clearSelectedMarker();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Handle slide animations if needed
            }
        });
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            animateButtonClick(btnBack);
            onBackPressed();
        });

        fabMyLocation.setOnClickListener(v -> {
            animateButtonClick(fabMyLocation);
            moveToCurrentLocation();
        });

        fabMapStyle.setOnClickListener(v -> {
            animateButtonClick(fabMapStyle);
            toggleMapStyle();
        });

        fabFilter.setOnClickListener(v -> {
            animateButtonClick(fabFilter);
            Log.d(TAG, "Filter FAB clicked!");
            showFilterBottomSheet();
            // Uncomment below line to test simple dialog instead
            // showSimpleFilterDialog();
        });

        bottomSheet.setOnClickListener(v -> {
            // Handle bottom sheet click - navigate to vendor details
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.d(TAG, "Google Map is ready!");
        setupMapSettings();
        setupMapListeners();
        requestLocationPermission();
        
        // Show a default location (Delhi, India) if no location is available
        LatLng defaultLocation = new LatLng(28.6139, 77.2090);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
        showToast("Map loaded! Please set up your Google Maps API key for full functionality.");
    }

    private void setupMapSettings() {
        if (googleMap == null) return;

        // Enable map controls
        googleMap.getUiSettings().setZoomControlsEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Set map style
        MapStyleUtils.setMapStyle(googleMap, this, isMapStyleDark);

        // Set default camera position (can be updated when location is available)
        LatLng defaultLocation = new LatLng(28.6139, 77.2090); // Delhi, India
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
    }

    private void setupMapListeners() {
        if (googleMap == null) return;

        googleMap.setOnMarkerClickListener(marker -> {
            handleMarkerClick(marker);
            return true;
        });

        googleMap.setOnMapClickListener(latLng -> {
            hideBottomSheet();
        });

        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                hideBottomSheet();
            }
        });
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            enableLocationFeatures();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationFeatures();
            } else {
                showToast("Location permission is required for map functionality");
            }
        }
    }

    private void enableLocationFeatures() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED && googleMap != null) {
            
            googleMap.setMyLocationEnabled(true);
            getCurrentLocation();
            startListeningToVendors();
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        updateMapCamera(currentLocation);
                        
                        // Update user location in Firebase for vendors to see
                        updateUserLocationInFirebase(location);
                    } else {
                        // Request fresh location
                        locationService.requestLocationUpdates(this::onLocationReceived);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location: " + e.getMessage());
                    showToast("Unable to get current location");
                });
    }

    private void onLocationReceived(Location location) {
        if (location != null) {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            updateMapCamera(currentLocation);
            updateUserLocationInFirebase(location);
        }
    }

    private void updateMapCamera(LatLng location) {
        if (googleMap != null && location != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_ZOOM));
        }
    }

    private void updateUserLocationInFirebase(Location location) {
        // Update user's location in Firebase for vendors to see nearby customers
        DatabaseReference userLocationRef = FirebaseDatabase.getInstance()
                .getReference("user_locations")
                .child("customer_" + System.currentTimeMillis());
        
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("timestamp", System.currentTimeMillis());
        
        userLocationRef.setValue(locationData);
    }

    private void startListeningToVendors() {
        if (vendorsRef != null && vendorsListener != null) {
            vendorsRef.addValueEventListener(vendorsListener);
        }
    }

    private void updateVendorMarkers(DataSnapshot dataSnapshot) {
        // Clear existing markers
        clearVendorMarkers();
        nearbyVendors.clear();

        for (DataSnapshot vendorSnapshot : dataSnapshot.getChildren()) {
            try {
                FoodVan vendor = vendorSnapshot.getValue(FoodVan.class);
                if (vendor != null && vendor.isOnline() && vendor.getLatitude() != 0 && vendor.getLongitude() != 0) {
                    addVendorMarker(vendor);
                    nearbyVendors.add(vendor);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing vendor data: " + e.getMessage());
            }
        }

        Log.d(TAG, "Updated " + nearbyVendors.size() + " vendor markers");
    }

    private void addVendorMarker(FoodVan vendor) {
        if (googleMap == null) return;

        LatLng vendorLocation = new LatLng(vendor.getLatitude(), vendor.getLongitude());
        
        // Create custom marker icon
        BitmapDescriptor markerIcon = createCustomMarkerIcon(vendor);
        
        MarkerOptions markerOptions = new MarkerOptions()
                .position(vendorLocation)
                .title(vendor.getVanName())
                .snippet(vendor.getOwnerName())
                .icon(markerIcon);

        Marker marker = googleMap.addMarker(markerOptions);
        if (marker != null) {
            marker.setTag(vendor);
            vendorMarkers.put(vendor.getId(), marker);
            
            // Animate marker appearance
            animateMarkerAppearance(marker);
        }
    }

    private BitmapDescriptor createCustomMarkerIcon(FoodVan vendor) {
        try {
            // Create custom marker based on vendor type or use default
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_food_van_marker);
            if (drawable == null) {
                return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }

            // Convert drawable to bitmap
            Bitmap bitmap = Bitmap.createBitmap(
                    drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888
            );
            
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error creating custom marker: " + e.getMessage());
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
        }
    }

    private void animateMarkerAppearance(Marker marker) {
        // Animate marker scale from 0 to 1
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            // This is a simplified animation - in a real implementation,
            // you might need to recreate the marker with different scales
        });
        animator.start();
    }

    private void handleMarkerClick(Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof FoodVan) {
            FoodVan vendor = (FoodVan) tag;
            showVendorDetails(vendor, marker);
        }
    }

    private void showVendorDetails(FoodVan vendor, Marker marker) {
        // Update bottom sheet content
        tvVendorName.setText(vendor.getVanName());
        tvVendorRating.setText(String.format("%.1f ★", vendor.getRating()));
        
        // Calculate distance and ETA
        if (currentLocation != null) {
            LatLng vendorLocation = new LatLng(vendor.getLatitude(), vendor.getLongitude());
            float distance = calculateDistance(currentLocation, vendorLocation);
            int eta = calculateETA(distance);
            
            tvVendorDistance.setText(String.format("%.1f km", distance));
            tvVendorETA.setText(String.format("%d min", eta));
        } else {
            tvVendorDistance.setText("-- km");
            tvVendorETA.setText("-- min");
        }

        // Show bottom sheet with animation
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        
        // Animate camera to marker
        if (googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
    }

    private float calculateDistance(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(
                start.latitude, start.longitude,
                end.latitude, end.longitude,
                results
        );
        return results[0] / 1000; // Convert to kilometers
    }

    private int calculateETA(float distanceKm) {
        // Assume average speed of 30 km/h in city traffic
        return Math.round((distanceKm / 30) * 60); // Convert to minutes
    }

    private void moveToCurrentLocation() {
        if (currentLocation != null && googleMap != null) {
            googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(currentLocation, DEFAULT_ZOOM),
                    1000, null
            );
        } else {
            getCurrentLocation();
        }
    }

    private void toggleMapStyle() {
        if (googleMap != null) {
            isMapStyleDark = !isMapStyleDark;
            MapStyleUtils.setMapStyle(googleMap, this, isMapStyleDark);
            
            // Update FAB icon
            int iconRes = isMapStyleDark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode;
            fabMapStyle.setImageResource(iconRes);
            
            showToast(isMapStyleDark ? "Dark mode enabled" : "Light mode enabled");
        }
    }

    private void hideBottomSheet() {
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    private void clearSelectedMarker() {
        // Clear any selected marker highlighting if implemented
    }

    private void clearVendorMarkers() {
        for (Marker marker : vendorMarkers.values()) {
            marker.remove();
        }
        vendorMarkers.clear();
    }

    private void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(100)
                .withEndAction(() -> 
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start()
                )
                .start();
    }

    private void startLocationUpdates() {
        locationUpdateHandler = new Handler(Looper.getMainLooper());
        locationUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                getCurrentLocation();
                locationUpdateHandler.postDelayed(this, LOCATION_UPDATE_INTERVAL);
            }
        };
        locationUpdateHandler.post(locationUpdateRunnable);
    }

    private void stopLocationUpdates() {
        if (locationUpdateHandler != null && locationUpdateRunnable != null) {
            locationUpdateHandler.removeCallbacks(locationUpdateRunnable);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapFragment != null) {
            mapFragment.onResume();
        }
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapFragment != null) {
            mapFragment.onPause();
        }
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Clean up Firebase listeners
        if (vendorsRef != null && vendorsListener != null) {
            vendorsRef.removeEventListener(vendorsListener);
        }
        
        // Clean up location services
        if (locationService != null) {
            locationService.stopLocationUpdates();
        }
        
        stopLocationUpdates();
        
        if (mapFragment != null) {
            mapFragment.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapFragment != null) {
            mapFragment.onLowMemory();
        }
    }
    
    // Filter-related methods
    private void initializeFilterManager() {
        filterManager = new FilterManager(this);
        currentFilter = new FilterCriteria();
        
        // Set user location for distance calculations
        if (currentLocation != null) {
            Location location = new Location("");
            location.setLatitude(currentLocation.latitude);
            location.setLongitude(currentLocation.longitude);
            filterManager.setUserLocation(location);
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
                    applyFiltersToMap();
                    updateFilterFabBadge();
                }
                
                @Override
                public void onFiltersCleared() {
                    currentFilter.reset();
                    applyFiltersToMap();
                    updateFilterFabBadge();
                }
            });
            
            filterFragment.show(getSupportFragmentManager(), "FilterBottomSheet");
            Log.d(TAG, "Filter bottom sheet shown successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "Error showing filter bottom sheet: " + e.getMessage());
            showToast("Error opening filters: " + e.getMessage());
        }
    }
    
    private void applyFiltersToMap() {
        if (filterManager == null) return;
        
        // Show loading indicator
        showToast("Applying filters...");
        
        filterManager.applyFilters(currentFilter, new FilterManager.FilterResultCallback() {
            @Override
            public void onFilterResults(List<User> filteredVendors, int totalCount) {
                runOnUiThread(() -> {
                    CustomerMapActivity.this.filteredVendors = filteredVendors;
                    updateMapMarkers();
                    
                    String message = filteredVendors.size() + " of " + totalCount + " food vans found";
                    showToast(message);
                    
                    Log.d(TAG, "Filters applied: " + message);
                });
            }
            
            @Override
            public void onFilterError(String error) {
                runOnUiThread(() -> {
                    showToast("Filter error: " + error);
                    Log.e(TAG, "Filter error: " + error);
                });
            }
        });
    }
    
    private void updateMapMarkers() {
        if (googleMap == null) return;
        
        // Clear existing markers
        googleMap.clear();
        vendorMarkers.clear();
        
        // Add filtered vendor markers
        for (User vendor : filteredVendors) {
            if (vendor.getLatitude() != 0 && vendor.getLongitude() != 0) {
                LatLng position = new LatLng(vendor.getLatitude(), vendor.getLongitude());
                
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(vendor.getBusinessName())
                        .snippet("Rating: " + vendor.getRating() + "★");
                
                // Customize marker based on vendor status
                if (vendor.isOnline()) {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                
                Marker marker = googleMap.addMarker(markerOptions);
                if (marker != null) {
                    vendorMarkers.put(vendor.getUserId(), marker);
                }
            }
        }
        
        Log.d(TAG, "Updated map with " + filteredVendors.size() + " vendor markers");
    }
    
    private void updateFilterFabBadge() {
        // Update filter FAB appearance based on active filters
        if (currentFilter.hasActiveFilters()) {
            // Change FAB color to indicate active filters
            fabFilter.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.primary_red)));
            fabFilter.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.white)));
        } else {
            // Reset to default appearance
            fabFilter.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.white)));
            fabFilter.setImageTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.text_primary)));
        }
    }
    
    // Simple dialog fallback for testing
    private void showSimpleFilterDialog() {
        Log.d(TAG, "Showing simple filter dialog as fallback");
        
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("🎯 Filter Options");
        
        String[] options = {
            "🍔 Fast Food", "🍛 Indian", "🥡 Chinese", "🍕 Italian", 
            "💰 All Prices", "💵 Budget", "💎 Premium",
            "⭐ 4+ Rating", "📍 Nearby", "🟢 Open Now"
        };
        boolean[] checkedItems = new boolean[options.length];
        
        builder.setMultiChoiceItems(options, checkedItems, (dialog, which, isChecked) -> {
            Log.d(TAG, "Filter " + options[which] + " = " + isChecked);
        });
        
        builder.setPositiveButton("Apply Filters", (dialog, which) -> {
            int selectedCount = 0;
            for (boolean checked : checkedItems) {
                if (checked) selectedCount++;
            }
            showToast("Applied " + selectedCount + " filters!");
            updateFilterFabBadge();
        });
        
        builder.setNegativeButton("Cancel", null);
        
        builder.setNeutralButton("Clear All", (dialog, which) -> {
            showToast("All filters cleared!");
            updateFilterFabBadge();
        });
        
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }
}
