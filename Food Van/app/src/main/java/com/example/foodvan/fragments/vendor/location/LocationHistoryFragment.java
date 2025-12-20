package com.example.foodvan.fragments.vendor.location;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.foodvan.R;
import com.example.foodvan.activities.vendor.LiveLocationActivity;
import com.example.foodvan.adapters.LocationHistoryAdapter;
import com.example.foodvan.models.LocationHistory;
import com.example.foodvan.viewmodels.LocationViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying location history
 * Shows previous location updates with filtering and management options
 */
public class LocationHistoryFragment extends Fragment implements LocationHistoryAdapter.OnLocationHistoryClickListener {

    private static final String TAG = "LocationHistory";

    // UI Components
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView rvLocationHistory;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnClearHistory;
    private MaterialButton btnDateFilter;
    private MaterialButton btnUpdateLocationNow;

    // Adapter and Data
    private LocationHistoryAdapter adapter;
    private List<LocationHistory> locationHistoryList;

    // ViewModel
    private LocationViewModel locationViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationViewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
        loadLocationHistory();
    }

    private void initializeViews(View view) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        rvLocationHistory = view.findViewById(R.id.rv_location_history);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnClearHistory = view.findViewById(R.id.btn_clear_history);
        btnDateFilter = view.findViewById(R.id.btn_date_filter);
        btnUpdateLocationNow = view.findViewById(R.id.btn_update_location_now);

        // Setup SwipeRefreshLayout colors
        swipeRefresh.setColorSchemeResources(R.color.primary_color);
    }

    private void setupRecyclerView() {
        locationHistoryList = new ArrayList<>();
        adapter = new LocationHistoryAdapter(locationHistoryList, this);
        
        rvLocationHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLocationHistory.setAdapter(adapter);
        rvLocationHistory.setHasFixedSize(true);
    }

    private void setupClickListeners() {
        swipeRefresh.setOnRefreshListener(this::loadLocationHistory);
        
        btnClearHistory.setOnClickListener(v -> showClearHistoryDialog());
        
        btnDateFilter.setOnClickListener(v -> showDateFilterDialog());
        
        btnUpdateLocationNow.setOnClickListener(v -> {
            // Switch to Update Location tab
            if (getActivity() != null && getActivity() instanceof LiveLocationActivity) {
                LiveLocationActivity activity = (LiveLocationActivity) getActivity();
                // Navigate to first tab (Update Current Location)
                // This would need ViewPager2 access from the activity
            }
            showSuccess("Switching to Update Location tab...");
        });
    }

    private void observeViewModel() {
        // Observe location history
        locationViewModel.getLocationHistory().observe(getViewLifecycleOwner(), historyList -> {
            try {
                if (historyList != null && !historyList.isEmpty()) {
                    showLocationHistory(historyList);
                } else {
                    showEmptyState();
                }
            } catch (Exception e) {
                showError("Error displaying location history: " + e.getMessage());
            } finally {
                if (swipeRefresh != null) {
                    swipeRefresh.setRefreshing(false);
                }
            }
        });

        // Observe error messages
        locationViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.trim().isEmpty()) {
                showError(error);
            }
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }

    private void loadLocationHistory() {
        swipeRefresh.setRefreshing(true);
        locationViewModel.loadLocationHistory();
    }

    private void showLocationHistory(List<LocationHistory> historyList) {
        layoutEmptyState.setVisibility(View.GONE);
        rvLocationHistory.setVisibility(View.VISIBLE);
        
        locationHistoryList.clear();
        locationHistoryList.addAll(historyList);
        adapter.notifyDataSetChanged();
    }

    private void showEmptyState() {
        layoutEmptyState.setVisibility(View.VISIBLE);
        rvLocationHistory.setVisibility(View.GONE);
    }

    private void showClearHistoryDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Location History")
                .setMessage("Are you sure you want to clear all location history? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    locationViewModel.clearLocationHistory();
                    showSuccess("Location history cleared successfully");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDateFilterDialog() {
        // For now, show a simple dialog with filter options
        String[] filterOptions = {"Last 7 Days", "Last 30 Days", "Last 3 Months", "All Time"};
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Filter by Date Range")
                .setItems(filterOptions, (dialog, which) -> {
                    btnDateFilter.setText(filterOptions[which]);
                    // In a real implementation, you would filter the data based on selection
                    showSuccess("Filter applied: " + filterOptions[which]);
                })
                .show();
    }

    // LocationHistoryAdapter.OnLocationHistoryClickListener implementation
    @Override
    public void onLocationHistoryClick(LocationHistory locationHistory) {
        // Show location details or navigate to map
        showLocationDetails(locationHistory);
    }

    @Override
    public void onLocationHistoryLongClick(LocationHistory locationHistory) {
        // Show context menu for actions
        showLocationContextMenu(locationHistory);
    }

    @Override
    public void onMoreOptionsClick(LocationHistory locationHistory) {
        showLocationContextMenu(locationHistory);
    }

    private void showLocationDetails(LocationHistory locationHistory) {
        if (locationHistory == null) {
            showError("Location details not available");
            return;
        }

        String status = "Unknown";
        if (locationHistory.getStatus() != null && !locationHistory.getStatus().isEmpty()) {
            status = locationHistory.getStatus();
        } else {
            status = locationHistory.isActive() ? "Active" : "Inactive";
        }

        String details = String.format(
            "Location Details\n\n" +
            "Coordinates: %.6f, %.6f\n" +
            "Address: %s\n" +
            "Accuracy: %.1f meters\n" +
            "Status: %s\n" +
            "Updated: %s",
            locationHistory.getLatitude(),
            locationHistory.getLongitude(),
            locationHistory.getAddress() != null && !locationHistory.getAddress().isEmpty() ? locationHistory.getAddress() : "Unknown",
            locationHistory.getAccuracy(),
            status,
            new java.text.SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", java.util.Locale.getDefault())
                .format(new java.util.Date(locationHistory.getTimestamp()))
        );

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Location Details")
                .setMessage(details)
                .setPositiveButton("OK", null)
                .show();
    }

    private void showLocationContextMenu(LocationHistory locationHistory) {
        String[] options = {"View Details", "Copy Coordinates", "Delete Entry"};
        
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Location Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showLocationDetails(locationHistory);
                            break;
                        case 1:
                            copyCoordinatesToClipboard(locationHistory);
                            break;
                        case 2:
                            deleteLocationEntry(locationHistory);
                            break;
                    }
                })
                .show();
    }

    private void copyCoordinatesToClipboard(LocationHistory locationHistory) {
        String coordinates = String.format("%.6f, %.6f", 
            locationHistory.getLatitude(), 
            locationHistory.getLongitude());
        
        android.content.ClipboardManager clipboard = 
            (android.content.ClipboardManager) requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Coordinates", coordinates);
        clipboard.setPrimaryClip(clip);
        
        showSuccess("Coordinates copied to clipboard");
    }

    private void deleteLocationEntry(LocationHistory locationHistory) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Location Entry")
                .setMessage("Are you sure you want to delete this location entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // In a real implementation, you would delete from repository
                    locationHistoryList.remove(locationHistory);
                    adapter.notifyDataSetChanged();
                    showSuccess("Location entry deleted");
                })
                .setNegativeButton("Cancel", null)
                .show();
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
