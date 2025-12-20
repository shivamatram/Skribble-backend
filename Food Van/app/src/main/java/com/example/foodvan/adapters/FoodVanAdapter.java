package com.example.foodvan.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.FoodVan;

import java.util.List;

/**
 * FoodVanAdapter - RecyclerView adapter for displaying food vans
 */
public class FoodVanAdapter extends RecyclerView.Adapter<FoodVanAdapter.FoodVanViewHolder> {

    private List<FoodVan> foodVans;
    private OnFoodVanClickListener clickListener;

    public interface OnFoodVanClickListener {
        void onFoodVanClick(FoodVan foodVan);
    }

    public FoodVanAdapter(List<FoodVan> foodVans, OnFoodVanClickListener clickListener) {
        this.foodVans = foodVans;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public FoodVanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_van, parent, false);
        return new FoodVanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodVanViewHolder holder, int position) {
        FoodVan foodVan = foodVans.get(position);
        holder.bind(foodVan);
    }

    @Override
    public int getItemCount() {
        return foodVans != null ? foodVans.size() : 0;
    }

    public void updateFoodVans(List<FoodVan> newFoodVans) {
        this.foodVans = newFoodVans;
        notifyDataSetChanged();
    }

    class FoodVanViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivVanImage;
        private TextView tvVanName;
        private TextView tvCuisineType;
        private TextView tvRating;
        private TextView tvDistance;
        private TextView tvDeliveryTime;
        private TextView tvStatus;
        private View statusIndicator;

        public FoodVanViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivVanImage = itemView.findViewById(R.id.iv_van_image);
            tvVanName = itemView.findViewById(R.id.tv_van_name);
            tvCuisineType = itemView.findViewById(R.id.tv_cuisine_type);
            tvRating = itemView.findViewById(R.id.tv_rating);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvDeliveryTime = itemView.findViewById(R.id.tv_delivery_time);
            tvStatus = itemView.findViewById(R.id.tv_status);
            statusIndicator = itemView.findViewById(R.id.status_indicator);

            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        clickListener.onFoodVanClick(foodVans.get(position));
                    }
                }
            });
        }

        public void bind(FoodVan foodVan) {
            tvVanName.setText(foodVan.getName());
            tvCuisineType.setText(foodVan.getCuisineType());
            tvRating.setText(foodVan.getFormattedRating() + " ★");
            tvDistance.setText(foodVan.getFormattedDistance());
            
            if (foodVan.getEstimatedDeliveryTime() > 0) {
                tvDeliveryTime.setText(foodVan.getEstimatedDeliveryTime() + " mins");
            } else {
                tvDeliveryTime.setText("15-20 mins");
            }

            // Set status
            if (foodVan.isAvailable()) {
                tvStatus.setText("Open");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                statusIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            } else {
                tvStatus.setText("Closed");
                tvStatus.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
                statusIndicator.setBackgroundColor(itemView.getContext().getResources().getColor(android.R.color.holo_red_dark));
            }

            // Load van image
            if (foodVan.getImageUrl() != null && !foodVan.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(foodVan.getImageUrl())
                        .placeholder(R.drawable.placeholder_food_van)
                        .error(R.drawable.placeholder_food_van)
                        .into(ivVanImage);
            } else {
                ivVanImage.setImageResource(R.drawable.placeholder_food_van);
            }
        }
    }
}
