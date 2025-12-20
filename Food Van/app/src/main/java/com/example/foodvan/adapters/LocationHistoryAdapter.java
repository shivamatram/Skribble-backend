package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodvan.R;
import com.example.foodvan.models.LocationHistory;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for Location History RecyclerView
 * Displays location history entries with date, coordinates, and status
 */
public class LocationHistoryAdapter extends RecyclerView.Adapter<LocationHistoryAdapter.LocationHistoryViewHolder> {

    private final List<LocationHistory> locationHistoryList;
    private final OnLocationHistoryClickListener clickListener;
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());

    public interface OnLocationHistoryClickListener {
        void onLocationHistoryClick(LocationHistory locationHistory);
        void onLocationHistoryLongClick(LocationHistory locationHistory);
        void onMoreOptionsClick(LocationHistory locationHistory);
    }

    public LocationHistoryAdapter(List<LocationHistory> locationHistoryList, OnLocationHistoryClickListener clickListener) {
        this.locationHistoryList = locationHistoryList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public LocationHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_history, parent, false);
        return new LocationHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationHistoryViewHolder holder, int position) {
        LocationHistory locationHistory = locationHistoryList.get(position);
        holder.bind(locationHistory);
    }

    @Override
    public int getItemCount() {
        return locationHistoryList.size();
    }

    class LocationHistoryViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvDateTime;
        private final TextView tvAddress;
        private final TextView tvCoordinates;
        private final Chip chipStatus;
        private final ImageView ivMoreOptions;

        public LocationHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvDateTime = itemView.findViewById(R.id.tv_date_time);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvCoordinates = itemView.findViewById(R.id.tv_coordinates);
            chipStatus = itemView.findViewById(R.id.chip_status);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
        }

        public void bind(LocationHistory locationHistory) {
            if (locationHistory == null) {
                // Handle null location history gracefully
                tvDateTime.setText("Unknown time");
                tvAddress.setText("Location data unavailable");
                tvCoordinates.setText("Coordinates unavailable");
                updateStatusChip(false);
                return;
            }

            try {
                // Format and set date/time
                long timestamp = locationHistory.getTimestamp();
                if (timestamp > 0) {
                    String formattedDate = dateFormatter.format(new Date(timestamp));
                    tvDateTime.setText(formattedDate);
                } else {
                    tvDateTime.setText("Unknown time");
                }

                // Set address or default text
                String address = locationHistory.getAddress();
                if (address != null && !address.trim().isEmpty()) {
                    tvAddress.setText(address.trim());
                } else {
                    tvAddress.setText("Location updated");
                }

                // Format and set coordinates
                String coordinates = String.format(Locale.getDefault(), 
                    "Lat: %.6f, Lng: %.6f", 
                    locationHistory.getLatitude(), 
                    locationHistory.getLongitude());
                tvCoordinates.setText(coordinates);

                // Set status chip based on status field or isActive
                boolean isActive = locationHistory.isActive();
                String status = locationHistory.getStatus();
                if (status != null && !status.trim().isEmpty()) {
                    // Use status field if available
                    isActive = "Active".equalsIgnoreCase(status.trim());
                }
                updateStatusChip(isActive);

                // Set click listeners
                itemView.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onLocationHistoryClick(locationHistory);
                    }
                });

                itemView.setOnLongClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onLocationHistoryLongClick(locationHistory);
                    }
                    return true;
                });

                ivMoreOptions.setOnClickListener(v -> {
                    if (clickListener != null) {
                        clickListener.onMoreOptionsClick(locationHistory);
                    }
                });

            } catch (Exception e) {
                // Handle any unexpected errors gracefully
                tvDateTime.setText("Error loading data");
                tvAddress.setText("Unable to load location details");
                tvCoordinates.setText("Coordinates unavailable");
                updateStatusChip(false);
            }
        }

        private void updateStatusChip(boolean isActive) {
            if (isActive) {
                chipStatus.setText("Active");
                chipStatus.setChipBackgroundColorResource(R.color.success_light);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.success_color));
                chipStatus.setChipIconResource(R.drawable.ic_check_circle);
                chipStatus.setChipIconTintResource(R.color.success_color);
            } else {
                chipStatus.setText("Inactive");
                chipStatus.setChipBackgroundColorResource(R.color.warning_light);
                chipStatus.setTextColor(itemView.getContext().getColor(R.color.warning_color));
                chipStatus.setChipIconResource(R.drawable.ic_warning);
                chipStatus.setChipIconTintResource(R.color.warning_color);
            }
        }
    }
}
