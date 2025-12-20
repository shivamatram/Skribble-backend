package com.example.foodvan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import com.example.foodvan.R;
// import com.example.foodvan.models.MenuItem; // Commented to avoid conflict

import java.util.List;

public class MenuManagementAdapter extends RecyclerView.Adapter<MenuManagementAdapter.MenuItemViewHolder> {

    private Context context;
    private List<com.example.foodvan.models.MenuItem> menuItems;
    private OnMenuItemActionListener listener;
    private boolean isGridView = false;

    public interface OnMenuItemActionListener {
        void onEditMenuItem(com.example.foodvan.models.MenuItem menuItem);
        void onDeleteMenuItem(com.example.foodvan.models.MenuItem menuItem);
        void onToggleAvailability(com.example.foodvan.models.MenuItem menuItem);
    }

    public MenuManagementAdapter(Context context, List<com.example.foodvan.models.MenuItem> menuItems, OnMenuItemActionListener listener) {
        this.context = context;
        this.menuItems = menuItems;
        this.listener = listener;
    }

    public void setGridView(boolean isGridView) {
        this.isGridView = isGridView;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_management_card, parent, false);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        com.example.foodvan.models.MenuItem menuItem = menuItems.get(position);
        
        // Bind data to views
        holder.tvItemName.setText(menuItem.getName());
        holder.tvItemDescription.setText(menuItem.getDescription());
        holder.tvItemPrice.setText(String.format("₹%.0f", menuItem.getPrice()));
        holder.tvItemCategory.setText(menuItem.getCategory());
        
        // MANDATORY ADAPTER OVERRIDE - Force text styling
        holder.tvItemName.setTextColor(android.graphics.Color.parseColor("#000000"));
        holder.tvItemName.setAlpha(1.0f);
        holder.tvItemName.setTypeface(holder.tvItemName.getTypeface(), android.graphics.Typeface.BOLD);
        
        holder.tvItemPrice.setTextColor(android.graphics.Color.parseColor("#000000"));
        holder.tvItemPrice.setAlpha(1.0f);
        holder.tvItemPrice.setTypeface(holder.tvItemPrice.getTypeface(), android.graphics.Typeface.BOLD);
        
        holder.tvItemDescription.setTextColor(android.graphics.Color.parseColor("#3A3A3A"));
        holder.tvItemDescription.setAlpha(1.0f);
        
        holder.tvItemCategory.setTextColor(android.graphics.Color.parseColor("#3A3A3A"));
        holder.tvItemCategory.setAlpha(1.0f);
        
        // Set availability status
        if (menuItem.isAvailable()) {
            holder.chipItemStatus.setText("Available");
            holder.chipItemStatus.setChipBackgroundColorResource(R.color.orangeAccent);
            holder.btnToggleAvailability.setText("Disable");
            holder.btnToggleAvailability.setTextColor(context.getColor(R.color.warning_color));
            holder.btnToggleAvailability.setIconResource(R.drawable.ic_visibility_off);
        } else {
            holder.chipItemStatus.setText("Out of Stock");
            holder.chipItemStatus.setChipBackgroundColorResource(R.color.error_color);
            holder.btnToggleAvailability.setText("Enable");
            holder.btnToggleAvailability.setTextColor(context.getColor(R.color.success_color));
            holder.btnToggleAvailability.setIconResource(R.drawable.ic_visibility);
        }
        
        // Load item image
        if (menuItem.getImageUrl() != null && !menuItem.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(menuItem.getImageUrl())
                    .placeholder(R.drawable.ic_fastfood)
                    .error(R.drawable.ic_fastfood)
                    .into(holder.ivMenuItemImage);
        } else {
            holder.ivMenuItemImage.setImageResource(R.drawable.ic_fastfood);
        }
        
        // Set click listeners
        holder.btnEditItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMenuItem(menuItem);
            }
        });
        
        holder.btnDeleteItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteMenuItem(menuItem);
            }
        });
        
        holder.btnToggleAvailability.setOnClickListener(v -> {
            if (listener != null) {
                listener.onToggleAvailability(menuItem);
            }
        });
        
        // Item click listener for details
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditMenuItem(menuItem);
            }
        });
        
        // Adjust layout for grid view
        if (isGridView) {
            // Modify layout parameters for grid view if needed
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) layoutParams;
                int margin = context.getResources().getDimensionPixelSize(R.dimen.spacing_small);
                marginParams.setMargins(margin, margin, margin, margin);
            }
        }
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    public static class MenuItemViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMenuItemImage;
        TextView tvItemName, tvItemDescription, tvItemPrice, tvItemCategory;
        Chip chipItemStatus;
        MaterialButton btnToggleAvailability, btnEditItem, btnDeleteItem;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivMenuItemImage = itemView.findViewById(R.id.iv_menu_item_image);
            tvItemName = itemView.findViewById(R.id.tv_item_name);
            tvItemDescription = itemView.findViewById(R.id.tv_item_description);
            tvItemPrice = itemView.findViewById(R.id.tv_item_price);
            tvItemCategory = itemView.findViewById(R.id.tv_item_category);
            chipItemStatus = itemView.findViewById(R.id.chip_item_status);
            btnToggleAvailability = itemView.findViewById(R.id.btn_toggle_availability);
            btnEditItem = itemView.findViewById(R.id.btn_edit_item);
            btnDeleteItem = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}
