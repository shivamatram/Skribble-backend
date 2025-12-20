package com.example.foodvan.fragments.vendor.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.foodvan.R;
import com.example.foodvan.viewmodels.LocationViewModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragment for updating current GPS location
 * Allows vendors to instantly update their live location for customers
 */
public class UpdateCurrentLocationFragment extends Fragment {

    private static final String TAG = "UpdateCurrentLocation";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // UI Components
    private ImageView ivStatusIcon;
    private TextView tvLocationStatus;
    private TextView tvLastUpdated;
    private TextView tvCoordinates;
    private LinearLayout layoutCoordinates;
    private MaterialButton btnUpdateLocation;
    private MaterialButton btnEnablePermissions;
    private LinearProgressIndicator progressLocation;
    private MaterialCardView cardPermissions;

    // Location Services
    private FusedLocationProviderClient fusedLocationClient;
    private LocationViewModel locationViewModel;

    // Date formatter
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationViewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update_current_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
        observeViewModel();
        checkLocationPermissions();
    }

    private void initializeViews(View view) {
        ivStatusIcon = view.findViewById(R.id.iv_status_icon);
        tvLocationStatus = view.findViewById(R.id.tv_location_status);
        tvLastUpdated = view.findViewById(R.id.tv_last_updated);
        tvCoordinates = view.findViewById(R.id.tv_coordinates);
        layoutCoordinates = view.findViewById(R.id.layout_coordinates);
        btnUpdateLocation = view.findViewById(R.id.btn_update_location);
        btnEnablePermissions = view.findViewById(R.id.btn_enable_permissions);
        progressLocation = view.findViewById(R.id.progress_location);
        cardPermissions = view.findViewById(R.id.card_permissions);
    }

    private void setupClickListeners() {
        btnUpdateLocation.setOnClickListener(v -> updateCurrentLocation());
        btnEnablePermissions.setOnClickListener(v -> requestLocationPermissions());
    }

    private void observeViewModel() {
        // Observe current location
        locationViewModel.getCurrentLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                updateLocationDisplay(location);
            }
        });

        // Observe last updated timestamp
        locationViewModel.getLastUpdated().observe(getViewLifecycleOwner(), timestamp -> {
            if (timestamp != null && timestamp > 0) {
                String formattedDate = dateFormatter.format(new Date(timestamp));
                tvLastUpdated.setText("Last Updated: " + formattedDate);
                updateStatusDisplay(true);
            } else {
                tvLastUpdated.setText("Last Updated: Never");
                updateStatusDisplay(false);
            }
        });

        // Observe loading state
        locationViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                if (isLoading) {
                    progressLocation.setVisibility(View.VISIBLE);
                    btnUpdateLocation.setEnabled(false);
                    btnUpdateLocation.setText("Updating...");
                } else {
                    resetUIState();
                }
            }
        });

        // Observe error messages
        locationViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                resetUIState();
                showError(error);
            }
        });
    }

    private void checkLocationPermissions() {
        if (hasLocationPermissions()) {
            cardPermissions.setVisibility(View.GONE);
            btnUpdateLocation.setEnabled(true);
            // Load last known location
            locationViewModel.loadLastKnownLocation();
        } else {
            cardPermissions.setVisibility(View.VISIBLE);
            btnUpdateLocation.setEnabled(false);
        }
    }

    private boolean hasLocationPermissions() {
        return ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        requestPermissions(
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPermissions();
                showSuccess("Location permissions granted!");
            } else {
                showError("Location permissions are required to update your position.");
            }
        }
    }

    private void updateCurrentLocation() {
        if (!hasLocationPermissions()) {
            showPermissionsCard();
            requestLocationPermissions();
            return;
        }

        // Show loading state
        progressLocation.setVisibility(View.VISIBLE);
        btnUpdateLocation.setEnabled(false);
        btnUpdateLocation.setText("Updating...");
        
        locationViewModel.updateCurrentLocation();
    }

    private void updateLocationDisplay(Location location) {
        if (location != null) {
            String coordinates = String.format(Locale.getDefault(), 
                "Lat: %.6f, Lng: %.6f", 
                location.getLatitude(), 
                location.getLongitude());
            
            tvCoordinates.setText(coordinates);
            layoutCoordinates.setVisibility(View.VISIBLE);
        }
    }

    private void updateStatusDisplay(boolean hasLocation) {
        if (hasLocation) {
            ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
            ivStatusIcon.setColorFilter(getResources().getColor(R.color.success_color, null));
            tvLocationStatus.setText("Location Active");
            tvLocationStatus.setTextColor(getResources().getColor(R.color.success_color, null));
        } else {
            ivStatusIcon.setImageResource(R.drawable.ic_error);
            ivStatusIcon.setColorFilter(getResources().getColor(R.color.warning_color, null));
            tvLocationStatus.setText("Location Not Set");
            tvLocationStatus.setTextColor(getResources().getColor(R.color.warning_color, null));
            layoutCoordinates.setVisibility(View.GONE);
        }
    }

    private void showLoading() {
        progressLocation.setVisibility(View.VISIBLE);
        btnUpdateLocation.setEnabled(false);
        btnUpdateLocation.setText("Updating...");
    }

    private void hideLoading() {
        progressLocation.setVisibility(View.GONE);
        btnUpdateLocation.setEnabled(true);
        btnUpdateLocation.setText("Update Location");
    }

    private void resetUIState() {
        progressLocation.setVisibility(View.GONE);
        btnUpdateLocation.setEnabled(true);
        btnUpdateLocation.setText("Update Location");
    }

    private void showPermissionsCard() {
        cardPermissions.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.success_color, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
        }
    }

    private void showError(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.error_color, null))
                .setTextColor(getResources().getColor(android.R.color.white, null))
                .show();
        }
    }
}
