package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodvan.R;
import com.example.foodvan.models.MenuItem;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ManageMenuAdapter extends RecyclerView.Adapter<ManageMenuAdapter.MenuItemViewHolder> {

    private Context context;
    private List<MenuItem> menuItems;
    private OnMenuItemActionListener listener;
    private boolean isSelectionMode = false;
    private List<MenuItem> selectedItems = new ArrayList<>();

    public interface OnMenuItemActionListener {
        void onEditMenuItem(MenuItem menuItem);
        void onDeleteMenuItem(MenuItem menuItem);
        void onToggleAvailability(MenuItem menuItem);
        void onItemSelectionChanged(MenuItem menuItem, boolean isSelected);
    }

    public ManageMenuAdapter(Context context, List<MenuItem> menuItems, OnMenuItemActionListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manage_menu, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        MenuItem menuItem = menuItems.get(position);
        holder.bind(menuItem);
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        private ShapeableImageView foodImage;
        private TextView foodName, foodCategory, foodPrice, foodDescription, availabilityStatus;
        private MaterialButton editButton, deleteButton;
        private MaterialCheckBox selectionCheckbox;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            foodImage = itemView.findViewById(R.id.food_image);
            foodName = itemView.findViewById(R.id.food_name);
            foodCategory = itemView.findViewById(R.id.food_category);
            foodPrice = itemView.findViewById(R.id.food_price);
            foodDescription = itemView.findViewById(R.id.food_description);
            availabilityStatus = itemView.findViewById(R.id.availability_status);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);
            selectionCheckbox = itemView.findViewById(R.id.selection_checkbox);

            setupClickListeners();
        }

        private void setupClickListeners() {
            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditMenuItem(menuItems.get(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteMenuItem(menuItems.get(getAdapterPosition()));
                }
            });

            availabilityStatus.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleAvailability(menuItems.get(getAdapterPosition()));
                }
            });

            // Selection checkbox click listener
            if (selectionCheckbox != null) {
                selectionCheckbox.setOnClickListener(v -> {
                    MenuItem item = menuItems.get(getAdapterPosition());
                    toggleItemSelection(item);
                });
            }

            // Item click listener for selection mode or edit
            itemView.setOnClickListener(v -> {
                if (isSelectionMode) {
                    MenuItem item = menuItems.get(getAdapterPosition());
                    toggleItemSelection(item);
                } else {
                    if (listener != null) {
                        listener.onEditMenuItem(menuItems.get(getAdapterPosition()));
                    }
                }
            });
        }

        public void bind(MenuItem menuItem) {
            // Basic info
            foodName.setText(menuItem.getName());
            foodCategory.setText(menuItem.getCategory());
            foodPrice.setText(String.format(Locale.getDefault(), "₹%.0f", menuItem.getPrice()));
            foodDescription.setText(menuItem.getDescription());

            // Availability status
            if (menuItem.isAvailable()) {
                availabilityStatus.setText("Available");
                availabilityStatus.setBackgroundResource(R.drawable.bg_availability_available);
                availabilityStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                availabilityStatus.setText("Unavailable");
                availabilityStatus.setBackgroundResource(R.drawable.bg_availability_unavailable);
                availabilityStatus.setTextColor(ContextCompat.getColor(context, R.color.white));
            }

            // Load image
            if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(menuItem.getImageUrl())
                        .placeholder(R.drawable.ic_add_photo_placeholder)
                        .error(R.drawable.ic_add_photo_placeholder)
                        .centerCrop()
                        .into(foodImage);
            } else {
                foodImage.setImageResource(R.drawable.ic_add_photo_placeholder);
            }

            // Adjust UI based on availability
            float alpha = menuItem.isAvailable() ? 1.0f : 0.6f;
            foodImage.setAlpha(alpha);
            foodName.setAlpha(alpha);
            foodCategory.setAlpha(alpha);
            foodPrice.setAlpha(alpha);
            foodDescription.setAlpha(alpha);

            // Handle selection mode
            if (selectionCheckbox != null) {
                if (isSelectionMode) {
                    selectionCheckbox.setVisibility(View.VISIBLE);
                    selectionCheckbox.setChecked(isItemSelected(menuItem));
                    
                    // Hide action buttons in selection mode
                    editButton.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);
                } else {
                    selectionCheckbox.setVisibility(View.GONE);
                    
                    // Show action buttons in normal mode
                    editButton.setVisibility(View.VISIBLE);
                    deleteButton.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    // Selection Mode Methods
    public void setSelectionMode(boolean selectionMode) {
        this.isSelectionMode = selectionMode;
        if (!selectionMode) {
            selectedItems.clear();
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public boolean isItemSelected(MenuItem item) {
        return selectedItems.contains(item);
    }

    public void toggleItemSelection(MenuItem item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            if (listener != null) {
                listener.onItemSelectionChanged(item, false);
            }
        } else {
            selectedItems.add(item);
            if (listener != null) {
                listener.onItemSelectionChanged(item, true);
            }
        }
        notifyDataSetChanged();
    }

    public List<MenuItem> getSelectedItems() {
        return new ArrayList<>(selectedItems);
    }
}
