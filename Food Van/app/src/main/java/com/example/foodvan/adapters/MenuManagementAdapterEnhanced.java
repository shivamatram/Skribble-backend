package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.MenuItem;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MenuManagementAdapterEnhanced extends RecyclerView.Adapter<MenuManagementAdapterEnhanced.MenuItemViewHolder> {

    private final Context context;
    private List<MenuItem> menuItems;
    private final OnMenuItemActionListener listener;
    
    // Bulk selection state
    private boolean isBulkMode = false;
    private Set<String> selectedItems;

    public interface OnMenuItemActionListener {
        void onItemClick(MenuItem item);
        void onItemEdit(MenuItem item);
        void onItemDelete(MenuItem item);
        void onItemToggleAvailability(MenuItem item);
        void onItemSelectionChanged(String itemId, boolean isSelected);
    }

    public MenuManagementAdapterEnhanced(Context context, List<MenuItem> menuItems, OnMenuItemActionListener listener) {
        this.context = context;
        this.menuItems = menuItems != null ? menuItems : new ArrayList<>();
        this.listener = listener;
        this.selectedItems = new java.util.HashSet<>();
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_management_enhanced, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public void updateMenuItems(List<MenuItem> newItems) {
        this.menuItems = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setBulkMode(boolean bulkMode) {
        this.isBulkMode = bulkMode;
        if (!bulkMode) {
            selectedItems.clear();
        }
        notifyDataSetChanged();
    }

    public void setSelectedItems(Set<String> selectedItems) {
        this.selectedItems = selectedItems != null ? selectedItems : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView ivItemImage;
        private final TextView tvItemName;
        private final TextView tvItemDescription;
        private final TextView tvItemPrice;
        private final TextView tvItemCategory;
        private final Chip chipAvailability;
        private final CheckBox cbSelection;
        private final View layoutActions;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_menu_item);
            ivItemImage = itemView.findViewById(R.id.iv_item_image);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDescription = itemView.findViewById(R.id.tv_item_description);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            tvItemCategory = itemView.findViewById(R.id.tv_item_category);
            chipAvailability = itemView.findViewById(R.id.chip_availability);
            cbSelection = itemView.findViewById(R.id.cb_selection);
            layoutActions = itemView.findViewById(R.id.layout_actions);
        }

        public void bind(MenuItem item) {
            // Basic item information
            tvItemName.setText(item.getName());
            tvItemDescription.setText(item.getDescription());
            tvItemPrice.setText(String.format("₹%.2f", item.getPrice()));
            tvItemCategory.setText(item.getCategory());
            
            // MANDATORY ADAPTER OVERRIDE - Force text styling
            tvItemName.setTextColor(android.graphics.Color.parseColor("#000000"));
            tvItemName.setAlpha(1.0f);
            tvItemName.setTypeface(tvItemName.getTypeface(), android.graphics.Typeface.BOLD);
            
            tvItemPrice.setTextColor(android.graphics.Color.parseColor("#000000"));
            tvItemPrice.setAlpha(1.0f);
            tvItemPrice.setTypeface(tvItemPrice.getTypeface(), android.graphics.Typeface.BOLD);
            
            tvItemDescription.setTextColor(android.graphics.Color.parseColor("#3A3A3A"));
            tvItemDescription.setAlpha(1.0f);
            
            tvItemCategory.setTextColor(android.graphics.Color.parseColor("#3A3A3A"));
            tvItemCategory.setAlpha(1.0f);

            // Load item image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(context)
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.ic_restaurant_menu)
                    .error(R.drawable.ic_restaurant_menu)
                    .into(ivItemImage);
            } else {
                ivItemImage.setImageResource(R.drawable.ic_restaurant_menu);
            }

            // Availability chip
            if (item.isAvailable()) {
                chipAvailability.setText("Available");
                chipAvailability.setChipBackgroundColorResource(R.color.orangeAccent);
                chipAvailability.setTextColor(context.getResources().getColor(R.color.white, null));
            } else {
                chipAvailability.setText("Out of Stock");
                chipAvailability.setChipBackgroundColorResource(R.color.error_color);
                chipAvailability.setTextColor(context.getResources().getColor(R.color.white, null));
            }

            // Bulk selection mode
            if (isBulkMode) {
                cbSelection.setVisibility(View.VISIBLE);
                layoutActions.setVisibility(View.GONE);
                cbSelection.setChecked(selectedItems.contains(item.getId()));
                
                // Handle selection changes
                cbSelection.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (listener != null) {
                        listener.onItemSelectionChanged(item.getId(), isChecked);
                    }
                });
                
                // Card click for selection
                cardView.setOnClickListener(v -> {
                    cbSelection.setChecked(!cbSelection.isChecked());
                });
                
            } else {
                cbSelection.setVisibility(View.GONE);
                layoutActions.setVisibility(View.VISIBLE);
                
                // Normal mode click handlers
                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(item);
                    }
                });
                
                // Availability toggle
                chipAvailability.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemToggleAvailability(item);
                    }
                });
            }

            // Action buttons (visible only in normal mode)
            View btnEdit = itemView.findViewById(R.id.btn_edit_item);
            View btnDelete = itemView.findViewById(R.id.btn_delete_item);
            
            if (btnEdit != null) {
                btnEdit.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemEdit(item);
                    }
                });
            }
            
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemDelete(item);
                    }
                });
            }

            // Visual feedback for selection
            if (isBulkMode && selectedItems.contains(item.getId())) {
                cardView.setStrokeColor(context.getResources().getColor(R.color.primary_color, null));
                cardView.setStrokeWidth(4);
            } else {
                cardView.setStrokeColor(context.getResources().getColor(R.color.outline_variant, null));
                cardView.setStrokeWidth(1);
            }
        }
    }
}
